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
import org.wso2.balana.combine.PolicyCombiningAlgorithm;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This implementation handles the XACML policy management in the Registry.
 */
public class RegistryPolicyStore extends AbstractPolicyStore {


    // The logger that is used for all messages
    private static final Log log = LogFactory.getLog(RegistryPAPPolicyStore.class);
    private final Registry registry;
    private static final String MODULE_NAME = "Registry Policy Finder Module";
    private static final String PROPERTY_POLICY_STORE_PATH = "policyStorePath";
    private static final String DEFAULT_POLICY_STORE_PATH = "/repository/identity/entitlement" +
            "/policy/pdp/";
    private static final String KEY_VALUE_POLICY_META_DATA = "policyMetaData";
    private String policyStorePath;


    @Override
    public void init(Properties properties) {
        super.init(properties);
        policyStorePath = properties.getProperty(PROPERTY_POLICY_STORE_PATH);
        if (policyStorePath == null) {
            policyStorePath = DEFAULT_POLICY_STORE_PATH;
        }
    }


    public RegistryPolicyStore() {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        registry = EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }


    /**
     * Adds or updates the given PAP policy
     *
     * @param policy policy
     * @throws EntitlementException throws, if fails
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy) throws EntitlementException {

        String version = createVersion(policy);
        policy.setVersion(version);

        addOrUpdatePAPPolicy(policy, policy.getVersion(), PDPConstants.ENTITLEMENT_POLICY_VERSION +
                policy.getPolicyId() + RegistryConstants.PATH_SEPARATOR);
        addOrUpdatePAPPolicy(policy, policy.getPolicyId(), PDPConstants.ENTITLEMENT_POLICY_PAP);

    }


    /**
     * Gets the requested policy
     *
     * @param policyId policy ID
     * @return policyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO getPolicy(String policyId) throws EntitlementException {

        boolean fromPDP = false;
        if (fromPDP) {
            return null;
        } else {
            String path = PDPConstants.ENTITLEMENT_POLICY_PAP + policyId;
            return getPAPPolicy(policyId, path);
        }

    }


    /**
     * Gets the requested policy version from PAP
     *
     * @param policyId policy ID
     * @param version policy version
     * @return policyDTO
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        // Zero means the current version
        if (version == null || version.trim().isEmpty()) {
            try {
                assert registry != null;
                Collection collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
                if (collection != null) {
                    version = collection.getProperty("version");
                }
            } catch (RegistryException e) {
                log.error(e);
                throw new EntitlementException("Invalid policy version");
            }
        }

        String collection = PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId + RegistryConstants.PATH_SEPARATOR;
        String path = collection + version;
        PolicyDTO dto =  getPAPPolicy(policyId, path);

        if (dto == null) {
            throw new EntitlementException("Invalid policy version");
        }
        return dto;
    }


    /**
     * Gets all policy IDs
     *
     * @return array of policy IDs
     * @throws EntitlementException throws, if fails
     */
    @Override
    public String[] listPolicyIds() throws EntitlementException {

        boolean fromPDP = false;
        if (fromPDP) {
            return null;
        } else {
            String path = PDPConstants.ENTITLEMENT_POLICY_PAP;
            return getAllPolicyIds(path);
        }

    }


    /**
     * Gets all policies
     *
     * @return array of policyDTOs
     * @throws EntitlementException throws, if fails
     */
    @Override
    public PolicyDTO[] listPolicies() throws EntitlementException {

//        boolean fromPDP = false;
//        if (fromPDP) {
//            return null;
//        } else {
//            String[] resources;
//            resources = getAllPolicyIds(false);
//
//            if (resources == null) {
//                return new PolicyDTO[0];
//            }
//            List<PolicyDTO> policyDTOList = new ArrayList<>();
//            for (String resource : resources) {
//                PolicyDTO dto = getPolicy(resource, false);
//                if (dto != null) {
//                    dto.setPolicy(null);
//                    AttributeDTO[] arr = new AttributeDTO[0];
//                    dto.setAttributeDTOs(arr);
//                    String[] arr2 = new String[0];
//                    dto.setPolicyEditorData(arr2);
//                }
//                policyDTOList.add(dto);
//            }
//            return policyDTOList.toArray(new PolicyDTO[0]);
//        }
        return null;

    }


    /**
     * Gets all versions of the given policy ID
     *
     * @param policyId policy ID
     * @return array of policy versions
     */
    @Override
    public String[] getVersions(String policyId) {

        List<String> versions = new ArrayList<>();
        Collection collection = null;
        try {
            try {
                assert registry != null;
                collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
            } catch (ResourceNotFoundException e) {
                // ignore
            }
            if (collection != null && collection.getChildren() != null) {
                String[] children = collection.getChildren();
                for (String child : children) {
                    versions.add(RegistryUtils.getResourceName(child));
                }
            }
        } catch (RegistryException e) {
            log.error("Error while creating new version of policy", e);
        }
        return versions.toArray(new String[0]);

    }


    /**
     * Removes the given policy from PAP
     *
     * @param policyId policy ID
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

            // Removes versions
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId)) {
                registry.delete(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
            }

        } catch (RegistryException e) {
            log.error("Error while removing entitlement policy " + policyId + " from PAP policy store", e);
            throw new EntitlementException("Error while removing policy " + policyId + " from PAP policy store");
        }

    }


    /**
     * Checks whether the given policy is published or not
     *
     * @param policyId policy ID
     * @return whether the given policy is published or not
     */
    @Override
    public boolean isPublished(String policyId) {

        String policyPath;
        if (policyId == null || policyId.trim().isEmpty()) {
            return false;
        }
        try {
            policyPath = policyStorePath + policyId;
            return registry.resourceExists(policyPath);
        } catch (RegistryException e) {
            //ignore
            return false;
        }
    }


    /**
     * Gets all published policy IDs
     */
    public String[] listPublishedPolicyIds() throws EntitlementException{
        return null;
    }


    /**
     * Publishes the given policy
     *
     * @param policy policy to be published
     * @throws EntitlementException throws, if fails
     */
    @Override
    public void publishPolicy(PolicyStoreDTO policy) throws EntitlementException {

        String policyPath;
        Collection policyCollection;
        Resource resource;

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy can not be null");
        }

        try {
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


    /**
     * De-promotes the policy
     */
    @Override
    public void unpublishPolicy(String policyId) throws EntitlementException {

    }


    /**
     * Sets the global policy combining algorithm
     */
    @Override
    public void setGlobalPolicyAlgorithm(String policyCombiningAlgorithm) throws EntitlementException {

    }


    /**
     * Gets the policy combining algorithm
     */
    @Override
    public PolicyCombiningAlgorithm getGlobalPolicyAlgorithm() {
        return null;
    }


    /**
     * Gets the policy combining algorithm name
     */
    @Override
    public String getGlobalPolicyAlgorithmName() {
        return null;
    }



    /**
     * Creates a new policy version
     *
     * @param policyDTO policy
     * @return new policy version
     */
    private String createVersion(PolicyDTO policyDTO) {

        String version = "0";

        try {
            Collection collection = null;
            try {
                assert registry != null;
                collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION +
                        policyDTO.getPolicyId());
            } catch (ResourceNotFoundException e) {
                // ignore
            }

            if (collection != null) {
                version = collection.getProperty("version");
            } else {
                collection = registry.newCollection();
                collection.setProperty("version", "1");
                registry.put(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId(), collection);
            }

            int versionInt = Integer.parseInt(version);
            String policyPath = PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId() +
                    RegistryConstants.PATH_SEPARATOR;

            // Checks whether the version is larger than the maximum version
            if (versionInt > maxVersions) {
                // Deletes the older version
                int olderVersion = versionInt - maxVersions;
                if (registry.resourceExists(policyPath + olderVersion)) {
                    registry.delete(policyPath + olderVersion);
                }
            }

            // Creates the new version
            version = Integer.toString(versionInt + 1);
            policyDTO.setVersion(version);

            // Sets the new version
            collection.setProperty("version", version);
            registry.put(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId(), collection);

        } catch (RegistryException e) {
            log.error("Error while creating a new version for the policy", e);
        }
        return version;
    }


    /**
     * Adds or updates the given policy to PAP
     *
     * @param policy     policyDTO
     * @param policyId   policyID
     * @param policyPath registry destination path
     * @throws EntitlementException throws, if fails
     */
    private void addOrUpdatePAPPolicy(PolicyDTO policy, String policyId, String policyPath)
            throws EntitlementException {

        String path;
        Resource resource;
        boolean newPolicy = false;
        OMElement omElement = null;

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

            //Before writing basic policy editor metadata as properties, deletes any properties related to them
            String policyEditor = resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE);
            if (newPolicy && policyEditor != null) {
                resource.removeProperty(PDPConstants.POLICY_EDITOR_TYPE);
            }

            //Writes policy metadata that is used for basic policy editor
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
     * Gets the requested policy from PAP
     *
     * @param policyId policy ID
     * @return policyDTO
     * @throws EntitlementException throws, if fails
     */
    private PolicyDTO getPAPPolicy(String policyId, String path) throws EntitlementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving entitlement policy");
        }

        try {

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
            log.error("Error while retrieving entitlement policy PAP policy store", e);
            throw new EntitlementException("Error while retrieving entitlement policy PAP policy store");
        }

    }


    /**
     * Gets all policy IDs
     *
     * @param path policy registry path
     * @return array of policy IDs
     * @throws EntitlementException throws, if fails
     */
    private String[] getAllPolicyIds(String path) throws EntitlementException {

        Collection collection;
        String[] children;
        List<String> resources = new ArrayList<>();

        if (log.isDebugEnabled()) {
            log.debug("Retrieving all entitlement policies");
        }

        try {

            if (!registry.resourceExists(path)) {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to access an entitlement policy which does not exist");
                }
                return null;
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            for (String child : children) {
                String id = child.substring(child.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                resources.add(id);
            }

        } catch (RegistryException e) {
            log.error("Error while retrieving entitlement policy resources", e);
            throw new EntitlementException("Error while retrieving entitlement policy resources", e);
        }

        return resources.toArray(new String[0]);
    }


    /**
     * Creates a property object which contains the policy metadata.
     *
     * @param attributeDTOs list of AttributeDTO
     * @param resource registry resource
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

}
