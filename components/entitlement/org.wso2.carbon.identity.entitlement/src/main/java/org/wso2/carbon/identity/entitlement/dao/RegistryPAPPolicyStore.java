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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class RegistryPAPPolicyStore implements PAPPolicyStoreModule {

    // The logger we'll use for all messages
    private static final Log log = LogFactory.getLog(RegistryPAPPolicyStore.class);
    private final Registry registry;


    public RegistryPAPPolicyStore() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }


    /**
     * This returns all policy ids as a string list. Here we assume registry resource name as
     * the policy id.
     *
     * @return policy ids as a string list
     * @throws EntitlementException throws if fails
     */
    @Override
    public String[] getAllPolicyIds() throws EntitlementException {
        String path;
        Collection collection;
        List<String> resources = new ArrayList<>();
        String[] children;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {
            path = PDPConstants.ENTITLEMENT_POLICY_PAP;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            for (String child : children) {
                String[] resourcePath = child.split("/");
                if (resourcePath.length > 0) {
                    resources.add(resourcePath[resourcePath.length - 1]);
                }
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving all entitlement policy identifiers from PAP policy store", e);
            throw new EntitlementException("Error while retrieving entitlement policy " +
                    "identifiers from PAP policy store");
        }

        return resources.toArray(new String[0]);
    }


    /**
     * This returns given policy as a PolicyDTO
     *
     * @param policyId policy id
     * @return policy as a policyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO getPolicy(String policyId) throws EntitlementException {

        String collection = PDPConstants.ENTITLEMENT_POLICY_PAP;
        String path;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy");
        }

        try {
            path = collection + policyId;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }

            Resource resource = registry.get(path);
            if (resource == null) {
                return null;
            }

            PolicyDTO dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(new String((byte[]) resource.getContent(), StandardCharsets.UTF_8));
            dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }

            String version = resource.getProperty(PDPConstants.POLICY_VERSION);
            if (version != null) {
                dto.setVersion(version);
            }
            String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
            if (lastModifiedTime != null) {
                dto.setLastModifiedTime(lastModifiedTime);
            }
            String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
            if (lastModifiedUser != null) {
                dto.setLastModifiedUser(lastModifiedUser);
            }
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if (policyReferences != null && !policyReferences.trim().isEmpty()) {
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if (policySetReferences != null && !policySetReferences.trim().isEmpty()) {
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount =
                    resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if (basicPolicyEditorMetaDataAmount != null) {
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for (int i = 0; i < amount; i++) {
                    basicPolicyEditorMetaData[i] = resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(
                    policyAttributeBuilder.getPolicyMetaDataFromRegistryProperties(resource.getProperties()));

            return dto;

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy " + policyId + " PAP policy store", e);
            throw new EntitlementException("Error while retrieving entitlement policy " + policyId +
                    " PAP policy store");
        }
    }


    /**
     * This returns given policy version as a PolicyDTO
     *
     * @param version version
     * @param policyId  policy id
     * @return policy as a policyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO getPolicy(String version, String policyId) throws EntitlementException {

        String collection = PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId + RegistryConstants.PATH_SEPARATOR;
        String path;

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy");
        }

        try {
            path = collection + version;

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }

            Resource resource = registry.get(path);
            if (resource == null) {
                return null;
            }

            PolicyDTO dto = new PolicyDTO();
            dto.setPolicyId(policyId);
            dto.setPolicy(new String((byte[]) resource.getContent(), StandardCharsets.UTF_8));
            dto.setActive(Boolean.parseBoolean(resource.getProperty(PDPConstants.ACTIVE_POLICY)));
            String policyOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }

            String policyVersion = resource.getProperty(PDPConstants.POLICY_VERSION);
            if (policyVersion != null) {
                dto.setVersion(policyVersion);
            }
            String lastModifiedTime = resource.getProperty(PDPConstants.LAST_MODIFIED_TIME);
            if (lastModifiedTime != null) {
                dto.setLastModifiedTime(lastModifiedTime);
            }
            String lastModifiedUser = resource.getProperty(PDPConstants.LAST_MODIFIED_USER);
            if (lastModifiedUser != null) {
                dto.setLastModifiedUser(lastModifiedUser);
            }
            dto.setPolicyType(resource.getProperty(PDPConstants.POLICY_TYPE));

            String policyReferences = resource.getProperty(PDPConstants.POLICY_REFERENCE);
            if (policyReferences != null && !policyReferences.trim().isEmpty()) {
                dto.setPolicyIdReferences(policyReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            String policySetReferences = resource.getProperty(PDPConstants.POLICY_SET_REFERENCE);
            if (policySetReferences != null && !policySetReferences.trim().isEmpty()) {
                dto.setPolicySetIdReferences(policySetReferences.split(PDPConstants.ATTRIBUTE_SEPARATOR));
            }

            dto.setPolicyEditor(resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE));
            String basicPolicyEditorMetaDataAmount =
                    resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
            if (basicPolicyEditorMetaDataAmount != null) {
                int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
                String[] basicPolicyEditorMetaData = new String[amount];
                for (int i = 0; i < amount; i++) {
                    basicPolicyEditorMetaData[i] = resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                }
                dto.setPolicyEditorData(basicPolicyEditorMetaData);
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(
                    policyAttributeBuilder.getPolicyMetaDataFromRegistryProperties(resource.getProperties()));

            return dto;

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy " + version + " PAP policy store", e);
            throw new EntitlementException("Error while retrieving entitlement policy " + version +
                    " PAP policy store");
        }
    }


    /**
     * This returns all policies as a PolicyDTO array
     *
     * @return policies as a PolicyDTO array
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO[] getAllPolicies() throws EntitlementException {

        String[] resources;
        resources = getAllPolicyIds();

        if (resources == null) {
            return new PolicyDTO[0];
        }
        List<PolicyDTO> policyDTOList = new ArrayList<>();
        for (String resource : resources) {
            PolicyDTO dto = getPolicy(resource);
            if (dto != null) {
                dto.setPolicy(null);
                AttributeDTO[] arr = new AttributeDTO[0];
                dto.setAttributeDTOs(arr);
                String[] arr2 = new String[0];
                dto.setPolicyEditorData(arr2);
            }
            policyDTOList.add(dto);
        }
        return policyDTOList.toArray(new PolicyDTO[0]);
    }


    /**
     * This adds or updates the given policy
     *
     * @param policy PolicyDTO
     * @param toPAP  whether the policy is in PAP or not
     * @throws EntitlementException throws, if fails
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, Boolean toPAP) throws EntitlementException {

        String path;
        String policyId;
        String policyPath;
        Resource resource;
        boolean newPolicy = false;
        OMElement omElement = null;

        if (toPAP) {
            policyId = policy.getPolicyId();
            policyPath = PDPConstants.ENTITLEMENT_POLICY_PAP;
        } else {
            policyId = policy.getVersion();
            policyPath = PDPConstants.ENTITLEMENT_POLICY_VERSION + policy.getPolicyId() +
                    RegistryConstants.PATH_SEPARATOR;
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating or updating entitlement policy");
        }

        if (policyId == null) {
            log.error("Error while creating or updating entitlement policy: " +
                    "Policy DTO or Policy Id can not be null");
            throw new EntitlementException("Invalid Entitlement Policy. Policy or policyId can not be Null");
        }

        try {
            path = policyPath + policyId;

            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newResource();
            }

            Collection policyCollection;
            if (registry.resourceExists(policyPath)) {
                policyCollection = (Collection) registry.get(policyPath);
            } else {
                policyCollection = registry.newCollection();
            }


            if (policy.getPolicyOrder() > 0) {
                String noOfPolicies = policyCollection.getProperty(PDPConstants.MAX_POLICY_ORDER);
                if (noOfPolicies != null && Integer.parseInt(noOfPolicies) < policy.getPolicyOrder()) {
                    policyCollection.setProperty(PDPConstants.MAX_POLICY_ORDER,
                            Integer.toString(policy.getPolicyOrder()));
                    registry.put(policyPath, policyCollection);
                }
                resource.setProperty(PDPConstants.POLICY_ORDER, Integer.toString(policy.getPolicyOrder()));
            } else {
                String previousOrder = resource.getProperty(PDPConstants.POLICY_ORDER);
                if (previousOrder == null) {
                    if (policyCollection != null) {
                        int policyOrder = 1;
                        String noOfPolicies = policyCollection.getProperty(PDPConstants.MAX_POLICY_ORDER);
                        if (noOfPolicies != null) {
                            policyOrder = policyOrder + Integer.parseInt(noOfPolicies);
                        }
                        policyCollection.setProperty(PDPConstants.MAX_POLICY_ORDER, Integer.toString(policyOrder));
                        resource.setProperty(PDPConstants.POLICY_ORDER, Integer.toString(policyOrder));
                    }
                    registry.put(policyPath, policyCollection);
                }
            }

            if (StringUtils.isNotBlank(policy.getPolicy())) {
                resource.setContent(policy.getPolicy());
                newPolicy = true;
                PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder(policy.getPolicy());
                Properties properties = policyAttributeBuilder.getPolicyMetaDataFromPolicy();
                Properties resourceProperties = new Properties();
                for (Object o : properties.keySet()) {
                    String key = o.toString();
                    resourceProperties.put(key, Collections.singletonList(properties.get(key)));
                }
                resource.setProperties(resourceProperties);
            }

            resource.setProperty(PDPConstants.ACTIVE_POLICY, Boolean.toString(policy.isActive()));
            resource.setProperty(PDPConstants.PROMOTED_POLICY, Boolean.toString(policy.isPromote()));

            if (policy.getVersion() != null) {
                resource.setProperty(PDPConstants.POLICY_VERSION, policy.getVersion());
            }
            resource.setProperty(PDPConstants.LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
            resource.setProperty(PDPConstants.LAST_MODIFIED_USER,
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            if (policy.getPolicyType() != null && !policy.getPolicyType().trim().isEmpty()) {
                resource.setProperty(PDPConstants.POLICY_TYPE, policy.getPolicyType());
            } else {
                try {
                    if (newPolicy) {
                        omElement = AXIOMUtil.stringToOM(policy.getPolicy());
                        resource.setProperty(PDPConstants.POLICY_TYPE, omElement.getLocalName());
                    }
                } catch (XMLStreamException e) {
                    policy.setPolicyType(PDPConstants.POLICY_ELEMENT);
                    log.warn("Policy Type can not be found. Default type is set");
                }
            }

            if (omElement != null) {
                Iterator iterator1 = omElement.getChildrenWithLocalName(PDPConstants.POLICY_REFERENCE);
                if (iterator1 != null) {
                    String policyReferences = "";
                    while (iterator1.hasNext()) {
                        OMElement policyReference = (OMElement) iterator1.next();
                        if (!"".equals(policyReferences)) {
                            policyReferences = policyReferences + PDPConstants.ATTRIBUTE_SEPARATOR +
                                    policyReference.getText();
                        } else {
                            policyReferences = policyReference.getText();
                        }
                    }
                    resource.setProperty(PDPConstants.POLICY_REFERENCE, policyReferences);
                }

                Iterator iterator2 = omElement.getChildrenWithLocalName(PDPConstants.POLICY_SET_REFERENCE);
                if (iterator2 != null) {
                    String policySetReferences = "";
                    while (true) {
                        assert iterator1 != null;
                        if (!iterator1.hasNext()) {
                            break;
                        }
                        OMElement policySetReference = (OMElement) iterator2.next();
                        if (!"".equals(policySetReferences)) {
                            policySetReferences = policySetReferences + PDPConstants.ATTRIBUTE_SEPARATOR +
                                    policySetReference.getText();
                        } else {
                            policySetReferences = policySetReference.getText();
                        }
                    }
                    resource.setProperty(PDPConstants.POLICY_SET_REFERENCE, policySetReferences);
                }
            }

            //before writing basic policy editor metadata as properties, delete any properties related to them
            String policyEditor = resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE);
            if (newPolicy && policyEditor != null) {
                resource.removeProperty(PDPConstants.POLICY_EDITOR_TYPE);
            }

            //write policy metadata that is used for basic policy editor
            if (policy.getPolicyEditor() != null && !policy.getPolicyEditor().trim().isEmpty()) {
                resource.setProperty(PDPConstants.POLICY_EDITOR_TYPE, policy.getPolicyEditor().trim());
            }
            String[] policyMetaData = policy.getPolicyEditorData();
            if (policyMetaData != null && policyMetaData.length > 0) {
                String BasicPolicyEditorMetaDataAmount =
                        resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
                if (newPolicy && BasicPolicyEditorMetaDataAmount != null) {
                    int amount = Integer.parseInt(BasicPolicyEditorMetaDataAmount);
                    for (int i = 0; i < amount; i++) {
                        resource.removeProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i);
                    }
                    resource.removeProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
                }

                int i = 0;
                for (String policyData : policyMetaData) {
                    if (policyData != null && !policyData.isEmpty()) {
                        resource.setProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA + i, policyData);
                    }
                    i++;
                }
                resource.setProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT, Integer.toString(i));
            }

            registry.put(path, resource);

        } catch (RegistryException e) {
            log.error("Error while adding or updating entitlement policy " + policyId + " in policy store", e);
            throw new EntitlementException("Error while adding or updating entitlement policy in policy store");
        }
    }


    /**
     * This removes the given policy from the policy store
     *
     * @param policyId policyId
     * @throws EntitlementException throws, if fails
     */
    @Override
    public void removePolicy(String policyId) throws EntitlementException {
        String path;

        if (log.isDebugEnabled()) {
            log.debug("Removing entitlement policy");
        }

        try {
            path = PDPConstants.ENTITLEMENT_POLICY_PAP + policyId;
            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return;
            }
            registry.delete(path);
        } catch (RegistryException e) {
            log.error("Error while removing entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while removing policy " + policyId + " from PAP policy store");
        }
    }


    /**
     * This removes the given policy version from the policy store
     *
     * @param policyId policyId
     * @param version  version
     */
    @Override
    public void removePolicy(String policyId, int version) {

    }
}
