/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.dao;

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
public class RegistryPolicyDataStore implements PolicyDataStoreModule {

    public static final String POLICY_COMBINING_PREFIX_1 =
            "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:";
    public static final String POLICY_COMBINING_PREFIX_3 =
            "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:";
    private static final Log log = LogFactory.getLog(RegistryPolicyDataStore.class);
    private final String policyDataCollection = PDPConstants.ENTITLEMENT_POLICY_DATA;


    @Override
    public void init(Properties properties) throws EntitlementException {

    }


    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {


        String algorithm = null;
        try {
            Registry registry = getGovernanceRegistry();
            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }

            if (algorithm == null || algorithm.trim().isEmpty()) {
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

            if (algorithm != null && !algorithm.trim().isEmpty()) {
                if ("first-applicable".equals(algorithm) || "only-one-applicable".equals(algorithm)) {
                    algorithm = POLICY_COMBINING_PREFIX_1 + algorithm;
                } else {
                    algorithm = POLICY_COMBINING_PREFIX_3 + algorithm;
                }
                return EntitlementUtil.getPolicyCombiningAlgorithm(algorithm);
            }

        } catch (RegistryException | EntitlementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception while getting Global Policy Algorithm from policy data store.", e);
            }
        }

        log.warn("Global policy combining algorithm is not defined. Therefore using default one");
        return new DenyOverridesPolicyAlg();
    }


    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

        Registry registry = getGovernanceRegistry();
        try {
            Collection policyCollection;
            if (registry.resourceExists(policyDataCollection)) {
                policyCollection = (Collection) registry.get(policyDataCollection);
            } else {
                policyCollection = registry.newCollection();
            }

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

        String algorithm = null;
        try {

            Registry registry = getGovernanceRegistry();
            if (registry.resourceExists(policyDataCollection)) {
                Collection collection = (Collection) registry.get(policyDataCollection);
                algorithm = collection.getProperty("globalPolicyCombiningAlgorithm");
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        } catch (EntitlementException e) {
            log.error("Error while getting Global Policy Combining Algorithm Name.", e);
        }

        // set default
        if (algorithm == null) {
            algorithm = "deny-overrides";
        }

        return algorithm;
    }


    @Override
    public String[] getAllGlobalPolicyAlgorithmNames() {

        return new String[] {"deny-overrides", "permit-overrides", "first-applicable",
                "ordered-deny-overrides", "ordered-permit-overrides", "only-one-applicable"};
    }


    @Override
    public PolicyStoreDTO getPolicyData(String policyId) {

        PolicyStoreDTO dataDTO = new PolicyStoreDTO();
        try {
            Registry registry = getGovernanceRegistry();
            String path = policyDataCollection + policyId;
            if (registry.resourceExists(path)) {
                Resource resource = registry.get(path);
                String order = resource.getProperty("order");
                String active = resource.getProperty("active");
                if (order != null && !order.trim().isEmpty()) {
                    dataDTO.setPolicyOrder(Integer.parseInt(order));
                }
                dataDTO.setActive(Boolean.parseBoolean(active));
            }
        } catch (RegistryException e) {
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        } catch (EntitlementException e) {
            log.error("Error while getting policy data for policyId: " + policyId, e);
        }
        return dataDTO;
    }


    @Override
    public PolicyStoreDTO[] getPolicyData() {


        List<PolicyStoreDTO> policyStoreDTOs = new ArrayList<>();
        try {
            Registry registry = getGovernanceRegistry();
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
                        if (order != null && !order.trim().isEmpty()) {
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
        } catch (EntitlementException e) {
            log.error("Error while getting all policy data.", e);
        }
        return policyStoreDTOs.toArray(new PolicyStoreDTO[0]);
    }


    @Override
    public void setPolicyData(String policyId, PolicyStoreDTO policyDataDTO) throws EntitlementException {

        Registry registry = getGovernanceRegistry();
        try {
            String path = policyDataCollection + policyId;
            Resource resource;
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newCollection();
            }

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

        Registry registry = getGovernanceRegistry();
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


    private Registry getGovernanceRegistry() throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Registry registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);

        if (registry == null) {
            throw new EntitlementException("Unable to get governance registry for tenant: " + tenantId);
        }

        return registry;
    }
}
