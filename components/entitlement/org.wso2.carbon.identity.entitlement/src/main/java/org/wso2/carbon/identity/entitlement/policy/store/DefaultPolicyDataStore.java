/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.entitlement.policy.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.balana.combine.xacml3.DenyOverridesPolicyAlg;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This is default implementation, where data are stored in carbon registry
 */
public class DefaultPolicyDataStore implements PolicyDataStore {

    public static final String POLICY_COMBINING_PREFIX_1 =
            "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:";
    public static final String POLICY_COMBINING_PREFIX_3 =
            "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:";
    private static Log log = LogFactory.getLog(DefaultPolicyDataStore.class);
    private String policyDataCollection = PDPConstants.ENTITLEMENT_POLICY_DATA;

    @Override
    public void init(Properties properties) throws EntitlementException {

    }

    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        String algorithm = null;
        try {

            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }

            if (algorithm == null || algorithm.trim().length() == 0) {
                // read algorithm from entitlement.properties file
                algorithm = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties().
                        getProperty(PDPConstants.PDP_GLOBAL_COMBINING_ALGORITHM);
                log.info("Using Global policy combining algorithm that is defined in configuration file.");
                try {
                    return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
                } catch (Exception e) {
                    log.debug(e);
                }
            }

            if (algorithm != null && algorithm.trim().length() > 0) {
                if ("first-applicable".equals(algorithm) || "only-one-applicable".equals(algorithm)) {
                    algorithm = POLICY_COMBINING_PREFIX_1 + algorithm;
                } else {
                    algorithm = POLICY_COMBINING_PREFIX_3 + algorithm;
                }
                return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
            }

        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        } catch (EntitlementException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }

        log.warn("Global policy combining algorithm is not defined. Therefore using default one");
        return new DenyOverridesPolicyAlg();
    }

    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        try {
            Collection policyCollection;
            if (registry.resourceExists(policyDataCollection)) {
                policyCollection = (Collection) registry.get(policyDataCollection);
            } else {
                policyCollection = registry.newCollection();
            }
            policyCollection.setMediaType(PDPConstants.REGISTRY_MEDIA_TYPE);
            policyCollection.setProperty("globalPolicyCombiningAlgorithm", policyCombiningAlgorithm);
            registry.put(policyDataCollection, policyCollection);

            // performing cache invalidation
            EntitlementEngine.getInstance().invalidatePolicyCache();

        } catch (RegistryException e) {
            log.error("Error while updating Global combing algorithm in policy store ", e);
            throw new EntitlementException("Error while updating combing algorithm in policy store");
        }
    }

    @Override
    public String getGlobalPolicyAlgorithmName() {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        String algorithm = null;
        try {

            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }

        // set default
        if (algorithm == null) {
            algorithm = "deny-overrides";
        }

        return algorithm;
    }

    @Override
    public String[] getAllGlobalPolicyAlgorithmNames() {

        return new String[]{"deny-overrides", "permit-overrides", "first-applicable",
                "ordered-deny-overrides", "ordered-permit-overrides", "only-one-applicable"};
    }

    @Override
    public PolicyStoreDTO getPolicyData(String policyId) {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        PolicyStoreDTO dataDTO = new PolicyStoreDTO();
        try {
            String path = policyDataCollection + policyId;
            if (registry.resourceExists(path)) {
                Resource resource = registry.get(path);
                String order = resource.getProperty("order");
                String active = resource.getProperty("active");
                if (order != null && order.trim().length() > 0) {
                    dataDTO.setPolicyOrder(Integer.parseInt(order));
                }
                dataDTO.setActive(Boolean.parseBoolean(active));
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
        return dataDTO;
    }


    @Override
    public PolicyStoreDTO[] getPolicyData() {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        List<PolicyStoreDTO> policyStoreDTOs = new ArrayList<PolicyStoreDTO>();
        try {
            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                String[] paths = collection.getChildren();
                for (String path : paths) {
                    if (registry.resourceExists(path)) {
                        PolicyStoreDTO dataDTO = new PolicyStoreDTO();
                        Resource resource = registry.get(path);
                        String order = resource.getProperty("order");
                        String active = resource.getProperty("active");
                        String id = path.substring(path.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                        dataDTO.setPolicyId(id);
                        if (order != null && order.trim().length() > 0) {
                            dataDTO.setPolicyOrder(Integer.parseInt(order));
                        }
                        dataDTO.setActive(Boolean.parseBoolean(active));
                        policyStoreDTOs.add(dataDTO);
                    }
                }
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
        return policyStoreDTOs.toArray(new PolicyStoreDTO[policyStoreDTOs.size()]);
    }

    @Override
    public void setPolicyData(String policyId, PolicyStoreDTO policyDataDTO) throws EntitlementException {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        try {
            String path = policyDataCollection + policyId;
            Resource resource;
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newCollection();
            }
            resource.setMediaType(PDPConstants.REGISTRY_MEDIA_TYPE);
            if (policyDataDTO.isSetActive()) {
                resource.setProperty("active", Boolean.toString(policyDataDTO.isActive()));
            }
            if (policyDataDTO.isSetOrder()) {
                int order = policyDataDTO.getPolicyOrder();
                if (order > 0) {
                    resource.setProperty("order", Integer.toString(order));
                }
            }
            registry.put(path, resource);
        } catch (RegistryException e) {
            log.error("Error while updating Policy data in policy store ", e);
            throw new EntitlementException("Error while updating Policy data in policy store");
        }
    }

    @Override
    public void removePolicyData(String policyId) throws EntitlementException {

        Registry registry = EntitlementServiceComponent.
                getGovernanceRegistry(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        try {
            String path = policyDataCollection + policyId;
            if (registry.resourceExists(path)) {
                registry.delete(path);
            }
        } catch (RegistryException e) {
            log.error("Error while deleting Policy data in policy store ", e);
            throw new EntitlementException("Error while deleting Policy data in policy store");
        }

    }
}
