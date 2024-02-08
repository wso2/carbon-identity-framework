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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class RegistryPDPPolicyStore extends AbstractPolicyFinderModule
        implements PDPPolicyStoreModule {

    private static final String MODULE_NAME = "Registry Policy Finder Module";
    private static final String PROPERTY_POLICY_STORE_PATH = "policyStorePath";
    private static final String DEFAULT_POLICY_STORE_PATH = "/repository/identity/entitlement" +
            "/policy/pdp/";
    private static final String KEY_VALUE_POLICY_META_DATA = "policyMetaData";
    private static final Log log = LogFactory.getLog(RegistryPDPPolicyStore.class);
    private String policyStorePath;

    @Override
    public void init(Properties properties) {
        policyStorePath = properties.getProperty(PROPERTY_POLICY_STORE_PATH);
        if (policyStorePath == null) {
            policyStorePath = DEFAULT_POLICY_STORE_PATH;
        }
    }


    @Override
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {

        Registry registry;
        String policyPath;
        Collection policyCollection;
        Resource resource;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy can not be null");
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            if (registry.resourceExists(policyStorePath)) {
                policyCollection = (Collection) registry.get(policyStorePath);
            } else {
                policyCollection = registry.newCollection();
            }

            registry.put(policyStorePath, policyCollection);

            policyPath = policyStorePath + policy.getPolicyId();

            if (registry.resourceExists(policyPath)) {
                resource = registry.get(policyPath);
            } else {
                resource = registry.newResource();
            }

            if (policy.getPolicy() != null && !policy.getPolicy().trim().isEmpty()) {
                resource.setContent(policy.getPolicy());
                resource.setMediaType(PDPConstants.REGISTRY_MEDIA_TYPE);
                AttributeDTO[] attributeDTOs = policy.getAttributeDTOs();
                if (attributeDTOs != null) {
                    setAttributesAsProperties(attributeDTOs, resource);
                }
            }
            if (policy.isSetActive()) {
                resource.setProperty("active", Boolean.toString(policy.isActive()));
            }
            if (policy.isSetOrder()) {
                int order = policy.getPolicyOrder();
                if (order > 0) {
                    resource.setProperty("order", Integer.toString(order));
                }
            }
            if (resource.getContent() == null) {
                log.info("Prevented adding null content to resource " + policyPath);
                return;
            }
            registry.put(policyPath, resource);
        } catch (RegistryException e) {
            log.error("Error while persisting policy", e);
            throw new EntitlementException("Error while persisting policy", e);
        }
    }


    @Override
    public boolean isPolicyExist(String policyId) {

        Registry registry;
        String policyPath;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (policyId == null || policyId.trim().isEmpty()) {
            return false;
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            policyPath = policyStorePath + policyId;
            return registry.resourceExists(policyPath);
        } catch (RegistryException e) {
            //ignore
            return false;
        }
    }


    @Override
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {
        addPolicy(policy);
    }


    @Override
    public void deletePolicy(String policyIdentifier) {

        Registry registry;
        String policyPath;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (policyIdentifier == null || policyIdentifier.trim().isEmpty()) {
            return;
        }

        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);

            policyPath = policyStorePath + policyIdentifier;
            registry.delete(policyPath);
        } catch (RegistryException e) {
            log.error(e);
        }
    }


    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }


    @Override
    public String getPolicy(String policyId) {
        PolicyDTO dto;
        try {
            dto = getPolicyReader().readPolicy(policyId);
            return dto.getPolicy();
        } catch (Exception e) {
            log.error("Policy with identifier " + policyId + " can not be retrieved " +
                    "from registry policy finder module", e);
        }
        return null;
    }


    @Override
    public int getPolicyOrder(String policyId) {
        PolicyDTO dto;
        try {
            dto = getPolicyReader().readPolicy(policyId);
            return dto.getPolicyOrder();
        } catch (Exception e) {
            log.error("Policy with identifier " + policyId + " can not be retrieved " +
                    "from registry policy finder module", e);
        }
        return -1;
    }


    @Override
    public String[] getActivePolicies() {

        log.debug("Retrieving of Active policies are started. " + new Date());

        List<String> policies = new ArrayList<>();

        try {
            PolicyDTO[] policyDTOs = getPolicyReader().readAllPolicies(true, true);
            for (PolicyDTO dto : policyDTOs) {
                if (dto.getPolicy() != null) {
                    policies.add(dto.getPolicy());
                }
            }
        } catch (Exception e) {
            log.error("Policies can not be retrieved from registry policy finder module", e);
        }

        log.debug("Retrieving of Active policies are finished.   " + new Date());

        return policies.toArray(new String[0]);
    }


    @Override
    public String[] getOrderedPolicyIdentifiers() {

        log.debug("Retrieving of Order Policy Ids are started. " + new Date());

        List<String> policies = new ArrayList<>();

        try {
            PolicyDTO[] policyDTOs = getPolicyReader().readAllPolicies(false, true);
            for (PolicyDTO dto : policyDTOs) {
                if (dto.getPolicy() != null) {
                    policies.add(dto.getPolicyId());
                }
            }
        } catch (Exception e) {
            log.error("Policies can not be retrieved from registry policy finder module", e);
        }

        log.debug("Retrieving of Order Policy Ids are finish. " + new Date());

        return policies.toArray(new String[0]);

    }


    @Override
    public String[] getPolicyIdentifiers() {
        String[] policyIds = null;
        try {
            policyIds = getPolicyReader().getAllPolicyIds();
        } catch (Exception e) {
            log.error("Policy identifiers can not be retrieved from registry policy finder module", e);
        }
        return policyIds;
    }


    @Override
    public String getReferencedPolicy(String policyId) {

        // retrieve for policies that are not active
        try {
            PolicyDTO dto = getPolicyReader().readPolicy(policyId);
            if (dto != null && dto.getPolicy() != null && !dto.isActive()) {
                return dto.getPolicy();
            }
        } catch (EntitlementException e) {
            log.error("Error while retrieving reference policy " + policyId);
            // ignore
        }

        return null;
    }


    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {

        PolicyDTO[] policyDTOs = null;
        Map<String, Set<AttributeDTO>> attributeMap = null;
        try {
            policyDTOs = getPolicyReader().readAllPolicies(true, true);
        } catch (Exception e) {
            log.error("Policies can not be retrieved from registry policy finder module", e);
        }

        if (policyDTOs != null) {
            attributeMap = new HashMap<>();
            for (PolicyDTO policyDTO : policyDTOs) {
                Set<AttributeDTO> attributeDTOs =
                        new HashSet<>(Arrays.asList(policyDTO.getAttributeDTOs()));
                String[] policyIdRef = policyDTO.getPolicyIdReferences();
                String[] policySetIdRef = policyDTO.getPolicySetIdReferences();

                if (policyIdRef != null && policyIdRef.length > 0 || policySetIdRef != null &&
                        policySetIdRef.length > 0) {
                    for (PolicyDTO dto : policyDTOs) {
                        if (policyIdRef != null) {
                            for (String policyId : policyIdRef) {
                                if (dto.getPolicyId().equals(policyId)) {
                                    attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                                }
                            }
                        }
                        for (String policySetId : policySetIdRef) {
                            if (dto.getPolicyId().equals(policySetId)) {
                                attributeDTOs.addAll(Arrays.asList(dto.getAttributeDTOs()));
                            }
                        }
                    }
                }
                attributeMap.put(policyDTO.getPolicyId(), attributeDTOs);
            }
        }

        return attributeMap;
    }


    @Override
    public int getSupportedSearchAttributesScheme() {
        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }


    /**
     * creates policy reader instance
     *
     * @return RegistryPDPPolicyReader
     */
    private RegistryPDPPolicyReader getPolicyReader() {

        Registry registry = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            registry = EntitlementServiceComponent.getRegistryService().
                    getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            log.error("Error while obtaining registry for tenant :  " + tenantId, e);
        }
        return new RegistryPDPPolicyReader(registry, policyStorePath);
    }


    /**
     * This helper method creates properties object which contains the policy metadata.
     *
     * @param attributeDTOs List of AttributeDTO
     * @param resource      registry resource
     */
    private void setAttributesAsProperties(AttributeDTO[] attributeDTOs, Resource resource) {

        int attributeElementNo = 0;
        if (attributeDTOs != null) {
            for (AttributeDTO attributeDTO : attributeDTOs) {
                resource.setProperty(KEY_VALUE_POLICY_META_DATA + attributeElementNo,
                        attributeDTO.getCategory() + "," +
                                attributeDTO.getAttributeValue() + "," +
                                attributeDTO.getAttributeId() + "," +
                                attributeDTO.getAttributeDataType());
                attributeElementNo++;
            }
        }
    }


    @Override
    public boolean isPolicyOrderingSupport() {
        return true;
    }


    @Override
    public boolean isPolicyDeActivationSupport() {
        return true;
    }

}
