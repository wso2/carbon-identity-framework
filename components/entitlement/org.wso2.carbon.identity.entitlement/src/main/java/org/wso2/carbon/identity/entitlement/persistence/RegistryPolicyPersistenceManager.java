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

package org.wso2.carbon.identity.entitlement.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pap.PAPPolicyReader;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.identity.entitlement.PDPConstants.MODULE_NAME;

/**
 * This implementation handles the XACML policy management in the Registry.
 */
public class RegistryPolicyPersistenceManager extends AbstractPolicyFinderModule implements PolicyPersistenceManager {

    // The logger that is used for all messages
    private static final Log LOG = LogFactory.getLog(RegistryPolicyPersistenceManager.class);
    private static final String KEY_VALUE_POLICY_META_DATA = "policyMetaData";
    private static final String POLICY_STORE_PATH = "policyStorePath";
    private static final String DEFAULT_POLICY_STORE_PATH = "/repository/identity/entitlement/policy/pdp/";
    private static final String INVALID_POLICY_VERSION = "Invalid policy version";
    private static final String ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER =
            "Policies can not be retrieved from registry policy finder module";
    private static final String INVALID_ENTITLEMENT_POLICY = "Trying to access an entitlement policy %s which does " +
            "not exist";
    private static final String ERROR_PUBLISHING_POLICY = "Error while publishing policy";
    private String policyStorePath;
    private final int maxVersions;

    public RegistryPolicyPersistenceManager() {

        maxVersions = EntitlementUtil.getMaxNoOfPolicyVersions();
    }

    @Override
    public void init(Properties properties) {

        policyStorePath = properties.getProperty(POLICY_STORE_PATH);
        if (policyStorePath == null) {
            policyStorePath = DEFAULT_POLICY_STORE_PATH;
        }
    }

    /**
     * Adds or updates the given PAP policy.
     *
     * @param policy          policy.
     * @param isFromPapAction true if the operation originated from a PAP action, false if it is from a PDP action.
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, boolean isFromPapAction) throws EntitlementException {

        if (isFromPapAction) {
            String version = createVersion(policy);
            policy.setVersion(version);
            addOrUpdatePAPPolicy(policy, policy.getVersion(), PDPConstants.ENTITLEMENT_POLICY_VERSION +
                    policy.getPolicyId() + RegistryConstants.PATH_SEPARATOR);
        }
        addOrUpdatePAPPolicy(policy, policy.getPolicyId(), PDPConstants.ENTITLEMENT_POLICY_PAP);
    }

    /**
     * Gets the requested policy.
     *
     * @param policyId policy ID
     * @return policyDTO
     * @throws EntitlementException If an error occurs
     */
    @Override
    public PolicyDTO getPAPPolicy(String policyId) throws EntitlementException {

        String path = PDPConstants.ENTITLEMENT_POLICY_PAP + policyId;
        return getPolicyDTO(policyId, path);
    }

    /**
     * Gets the requested policy list.
     *
     * @param policyIds policy ID list
     * @return policyDTO
     * @throws EntitlementException If an error occurs
     */
    @Override
    public List<PolicyDTO> getPAPPolicies(List<String> policyIds) throws EntitlementException {

        if (policyIds == null || policyIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<PolicyDTO> policyDTOs = new ArrayList<>();
        for (String policyId : policyIds) {
            policyDTOs.add(getPAPPolicy(policyId));
        }
        return policyDTOs;
    }

    /**
     * Gets the requested policy version.
     *
     * @param policyId policy ID
     * @param version  policy version
     * @return policyDTO
     * @throws EntitlementException If an error occurs
     */
    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        // Zero indicates the current version
        if (version == null || version.trim().isEmpty()) {
            try {
                Registry registry = getRegistry();
                Collection collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
                if (collection != null) {
                    version = collection.getProperty(PDPConstants.POLICY_VERSION);
                }
            } catch (RegistryException e) {
                throw new EntitlementException(INVALID_POLICY_VERSION, e);
            }
        }

        String collection = PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId + RegistryConstants.PATH_SEPARATOR;
        String path = collection + version;
        PolicyDTO dto = getPolicyDTO(policyId, path);

        if (dto == null) {
            throw new EntitlementException(INVALID_POLICY_VERSION);
        }
        return dto;
    }

    /**
     * Gets all versions of the given policy ID.
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
                Registry registry = getRegistry();
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
            LOG.error(String.format("Error while retrieving policy versions for policy %s", policyId), e);
        }
        return versions.toArray(new String[0]);

    }

    /**
     * Gets the name of the module.
     *
     * @return name as String
     */
    @Override
    public String getModuleName() {

        return MODULE_NAME;
    }

    /**
     * Gets the policy for the given policy ID.
     *
     * @param policyId policy id as a string value
     * @return policy as string
     */
    @Override
    public String getPolicy(String policyId) {

        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        return dto.getPolicy();
    }

    /**
     * Gets the policy order.
     *
     * @param policyId policy id as a string value
     * @return policy order
     */
    @Override
    public int getPolicyOrder(String policyId) {

        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        return dto.getPolicyOrder();
    }

    /**
     * Gets all supported active policies.
     * If policy ordering is supported by the module itself, these policies must be ordered.
     *
     * @return array of policies as Strings
     */
    @Override
    public String[] getActivePolicies() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Active policies are started at %s", new Date()));
        }

        List<String> policies = new ArrayList<>();

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(true, true);
            for (PolicyStoreDTO dto : policyDTOs) {
                if (dto.getPolicy() != null) {
                    policies.add(dto.getPolicy());
                }
            }
        } catch (EntitlementException e) {
            LOG.error(ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Active policies are finished at %s", new Date()));
        }

        return policies.toArray(new String[0]);
    }

    /**
     * Gets all supported policy ids.
     * If policy ordering is supported by the module itself, these policy ids must be ordered.
     *
     * @return array of policy ids as Strings
     */
    @Override
    public String[] getOrderedPolicyIdentifiers() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Order Policy Ids are started at %s ", new Date()));
        }

        List<String> policies = new ArrayList<>();

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(false, true);
            for (PolicyStoreDTO dto : policyDTOs) {
                if (dto.getPolicy() != null) {
                    policies.add(dto.getPolicyId());
                }
            }
        } catch (EntitlementException e) {
            LOG.error(ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Order Policy Ids are finished at %s ", new Date()));
        }

        return policies.toArray(new String[0]);

    }

    /**
     * Gets all policy ids.
     *
     * @return array of policy ids as Strings
     */
    @Override
    public String[] getPolicyIdentifiers() {

        String[] policyIds = null;
        try {
            policyIds = listPublishedPolicyIds().toArray(new String[0]);
        } catch (EntitlementException e) {
            LOG.error("Policy identifiers can not be retrieved from registry policy finder module", e);
        }
        return policyIds;
    }

    /**
     * Gets reference policy for the given policy ID.
     * Reference policy can not be with PDP policy store,  may be in some external policy store.
     * Therefore, a new method has been added to retrieve reference policies.
     *
     * @param policyId policy id as String value
     * @return reference policy as String
     */
    @Override
    public String getReferencedPolicy(String policyId) {

        // Retrieves for policies that are not active
        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        if (dto != null && dto.getPolicy() != null && !dto.isActive()) {
            return dto.getPolicy();
        }

        return null;
    }

    /**
     * Gets attributes that are used for policy searching.
     *
     * @param identifier     unique identifier to separate out search attributes
     * @param givenAttribute pre-given attributes to retrieve other attributes
     * @return return search attributes based on a given policy, Map of policy id with search attributes.
     */
    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(true, true);
            List<String> policyIds = new ArrayList<>();
            for (PolicyStoreDTO policyStoreDTO : policyDTOs) {
                policyIds.add(policyStoreDTO.getPolicyId());
            }
            List<PolicyDTO> policyDTOList = getPAPPolicies(policyIds);
            if (policyDTOs.length > 0) {
                return EntitlementUtil.getAttributesFromPolicies(policyDTOList.toArray(new PolicyDTO[0]));
            }
        } catch (EntitlementException e) {
            LOG.error(ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER, e);
        }

        return Collections.emptyMap();
    }

    /**
     * Gets support attribute searching scheme of the module.
     *
     * @return return scheme identifier value
     */
    @Override
    public int getSupportedSearchAttributesScheme() {

        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }

    /**
     * Lists all PAP policy IDs.
     *
     * @return list of policy IDs
     * @throws EntitlementException If an error occurs
     */
    @Override
    public List<String> listPolicyIds() throws EntitlementException {

        String path = PDPConstants.ENTITLEMENT_POLICY_PAP;
        return listAllPolicyIds(path);

    }

    /**
     * Removes the given policy from PAP.
     *
     * @param policyId policy ID
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void removePolicy(String policyId) throws EntitlementException {

        String path;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Removing entitlement policy %s", policyId));
        }

        try {
            path = PDPConstants.ENTITLEMENT_POLICY_PAP + policyId;
            Registry registry = getRegistry();
            if (!registry.resourceExists(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(INVALID_ENTITLEMENT_POLICY, policyId));
                }
                return;
            }
            registry.delete(path);

            // Removes versions
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId)) {
                registry.delete(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
            }

        } catch (RegistryException e) {
            throw new EntitlementException(String.format("Error while removing policy %s from PAP policy store",
                    policyId), e);
        }

    }

    /**
     * Publishes the given policy.
     *
     * @param policy policy to be published
     * @throws EntitlementException If an error occurs
     */
    @Override
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {

        String policyPath;
        Collection policyCollection;
        Resource resource;
        String papPath;
        Resource papResource;

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy can not be null");
        }

        try {

            // Restricts publishing policies that are not in PAP
            papPath = PDPConstants.ENTITLEMENT_POLICY_PAP + policy.getPolicyId();
            Registry registry = getRegistry();
            if (!registry.resourceExists(papPath)) {
                throw new EntitlementException("Policies that are not included in the PAP, cannot be published");
            }

            // Publishes policy to PDP
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
                if (attributeDTOs != null && EntitlementUtil.isPolicyMetadataStoringEnabled()) {
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
                LOG.info(String.format("Prevented adding null content to resource %s", policyPath));
                return;
            }
            // Store policy metadata based on the configured property.
            if (!EntitlementUtil.isPolicyMetadataStoringEnabled()) {
                for (Map.Entry<Object, Object> entry : resource.getProperties().entrySet()) {
                    if (entry.getKey().toString().startsWith(PDPConstants.POLICY_META_DATA)) {
                        resource.getProperties().remove(entry.getKey());
                    }
                }
            }
            registry.put(policyPath, resource);

            // Updates the relevant resource in version store
            String version = policy.getVersion();
            if (version == null || version.trim().isEmpty()) {
                try {
                    Collection collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION +
                            policy.getPolicyId());
                    if (collection != null) {
                        version = collection.getProperty(PDPConstants.POLICY_VERSION);
                    }
                } catch (RegistryException e) {
                    throw new EntitlementException(INVALID_POLICY_VERSION, e);
                }
            }
            String versionCollectionPath = PDPConstants.ENTITLEMENT_POLICY_VERSION + policy.getPolicyId() +
                    RegistryConstants.PATH_SEPARATOR;
            String versionPolicyPath = PDPConstants.ENTITLEMENT_POLICY_VERSION + policy.getPolicyId() +
                    RegistryConstants.PATH_SEPARATOR + version;
            updateResource(policy, versionCollectionPath, versionPolicyPath);

            // If the publishing version is the latest version, updates the relevant resource in PAP
            if (registry.resourceExists(papPath)) {
                papResource = registry.get(papPath);
                if (Objects.equals(papResource.getProperty(PDPConstants.POLICY_VERSION), version)) {
                    updateResource(policy, PDPConstants.ENTITLEMENT_POLICY_PAP, papPath);
                }
            }

        } catch (RegistryException e) {
            throw new EntitlementException(ERROR_PUBLISHING_POLICY, e);
        }
    }

    @Override
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {

        if (LOG.isDebugEnabled() && policy != null) {
            LOG.debug(String.format("Updating policy %s", policy.getPolicyId()));
        }
        addPolicy(policy);
    }

    /**
     * Checks whether the given policy is published or not.
     *
     * @param policyId policy ID
     * @return whether the given policy is published or not
     */
    @Override
    public boolean isPolicyExist(String policyId) {

        String policyPath;
        if (policyId == null || policyId.trim().isEmpty()) {
            return false;
        }
        try {
            Registry registry = getRegistry();
            policyPath = policyStorePath + policyId;
            return registry.resourceExists(policyPath);
        } catch (RegistryException e) {
            //ignore
            return false;
        }
    }

    /**
     * Gets the requested published policy.
     *
     * @param policyId policy ID
     * @return requested policy
     */
    @Override
    public PolicyStoreDTO getPublishedPolicy(String policyId) {

        try {
            Resource resource;
            resource = getPolicyResource(policyId);
            if (resource == null) {
                return new PolicyStoreDTO();
            }
            return readPolicy(resource);
        } catch (EntitlementException e) {
            LOG.error(String.format("Error while retrieving PDP policy : %s", policyId), e);
            return new PolicyStoreDTO();
        }

    }

    /**
     * Lists all published policy IDs.
     *
     * @return list of published policy IDs
     * @throws EntitlementException If an error occurs
     */
    @Override
    public List<String> listPublishedPolicyIds() throws EntitlementException {

        return listAllPolicyIds(policyStorePath);
    }

    /**
     * Un-publishes the policy.
     *
     * @param policyId policy ID
     */
    @Override
    public boolean deletePolicy(String policyId) {

        String policyPath;

        if (policyId == null || policyId.trim().isEmpty()) {
            return false;
        }

        try {
            Registry registry = getRegistry();
            // Removes from PDP
            policyPath = policyStorePath + policyId;
            registry.delete(policyPath);
            return true;
        } catch (RegistryException e) {
            LOG.error(e);
            return false;
        }
    }

    /**
     * Checks the existence of the policy in PAP
     *
     * @param policyId policy ID.
     * @return whether the policy exists in PAP or not.
     */
    public boolean isPolicyExistsInPap(String policyId) {

        String path = PDPConstants.ENTITLEMENT_POLICY_PAP + policyId;
        try {
            Registry registry = getRegistry();
            return registry.resourceExists(path);
        } catch (RegistryException e) {
            LOG.error("Error while checking the existence of the policy in PAP", e);
            return false;
        }
    }

    /**
     * Adds or updates the given policy to PAP.
     *
     * @param policy     policyDTO
     * @param policyId   policyID
     * @param policyPath registry destination path
     * @throws EntitlementException If an error occurs
     */
    private void addOrUpdatePAPPolicy(PolicyDTO policy, String policyId, String policyPath)
            throws EntitlementException {

        String path;
        Resource resource;
        boolean newPolicy = false;
        OMElement omElement = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating or updating entitlement policy %s", policyId));
        }

        if (policyId == null) {
            throw new EntitlementException("Invalid Entitlement Policy. Policy or policyId can not be Null");
        }

        try {
            path = policyPath + policyId;
            Registry registry = getRegistry();
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
                // Store policy metadata based on the configured property.
                if (EntitlementUtil.isPolicyMetadataStoringEnabled()) {
                    for (Object keyObject : properties.keySet()) {
                        String key = keyObject.toString();
                        resource.setProperty(key, properties.get(key).toString());
                    }
                }
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
                    LOG.warn("Policy Type can not be found. Default type is set");
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

            // Before writing basic policy editor metadata as properties, deletes any properties related to them
            String policyEditor = resource.getProperty(PDPConstants.POLICY_EDITOR_TYPE);
            if (newPolicy && policyEditor != null) {
                resource.removeProperty(PDPConstants.POLICY_EDITOR_TYPE);
            }

            // Writes policy metadata that is used for basic policy editor
            if (policy.getPolicyEditor() != null && !policy.getPolicyEditor().trim().isEmpty()) {
                resource.setProperty(PDPConstants.POLICY_EDITOR_TYPE, policy.getPolicyEditor().trim());
            }
            String[] policyMetaData = policy.getPolicyEditorData();
            if (policyMetaData != null && policyMetaData.length > 0) {
                String basicPolicyEditorMetaDataAmount =
                        resource.getProperty(PDPConstants.BASIC_POLICY_EDITOR_META_DATA_AMOUNT);
                if (newPolicy && basicPolicyEditorMetaDataAmount != null) {
                    int amount = Integer.parseInt(basicPolicyEditorMetaDataAmount);
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
            // Store policy metadata based on the configured property.
            if (!EntitlementUtil.isPolicyMetadataStoringEnabled()) {
                for (Map.Entry<Object, Object> entry : resource.getProperties().entrySet()) {
                    if (entry.getKey().toString().startsWith(PDPConstants.POLICY_META_DATA)) {
                        resource.getProperties().remove(entry.getKey());
                    }
                }
            }

            registry.put(path, resource);

        } catch (RegistryException e) {
            throw new EntitlementException(
                    String.format("Error while adding or updating entitlement policy %s in policy store", policyId), e);
        }
    }

    /**
     * Creates a new policy version.
     *
     * @param policyDTO policy
     * @return new policy version
     */
    private String createVersion(PolicyDTO policyDTO) {

        String version = "0";

        try {
            Collection collection = null;
            Registry registry = getRegistry();
            try {
                collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION +
                        policyDTO.getPolicyId());
            } catch (ResourceNotFoundException e) {
                // ignore
            }

            if (collection != null) {
                version = collection.getProperty(PDPConstants.POLICY_VERSION);
            } else {
                collection = registry.newCollection();
                collection.setProperty(PDPConstants.POLICY_VERSION, "1");
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
            LOG.error("Error while creating a new version for the policy", e);
        }
        return version;
    }

    /**
     * Creates a property object which contains the policy metadata.
     *
     * @param attributeDTOs list of AttributeDTO
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

    /**
     * Gets the requested policy from PAP.
     *
     * @param policyId policy ID
     * @return policyDTO
     * @throws EntitlementException If an error occurs
     */
    private PolicyDTO getPolicyDTO(String policyId, String path) throws EntitlementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving entitlement policy %s", policyId));
        }

        try {
            Registry registry = getRegistry();
            if (!registry.resourceExists(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(INVALID_ENTITLEMENT_POLICY, policyId));
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
            throw new EntitlementException("Error while retrieving entitlement policy PAP policy store", e);
        }

    }

    /**
     * Returns given policy as a registry resource.
     *
     * @param policyId policy id
     * @return policy as a registry resource
     * @throws EntitlementException If an error occurs
     */
    private Resource getPolicyResource(String policyId) throws EntitlementException {

        String path;

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving entitlement policy %s", policyId));
        }

        try {
            path = policyStorePath + policyId;
            Registry registry = getRegistry();
            if (!registry.resourceExists(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(INVALID_ENTITLEMENT_POLICY, policyId));
                }
                return null;
            }
            return registry.get(path);
        } catch (RegistryException e) {
            throw new EntitlementException(String.format("Error while retrieving entitlement policy : %s", policyId),
                    e);
        }
    }

    /**
     * Reads All ordered active policies as PolicyDTO.
     *
     * @param active only return active policies
     * @param order  return ordered policy
     * @return Array of PolicyDTO
     * @throws EntitlementException If an error occurs
     */
    private PolicyStoreDTO[] getAllPolicies(boolean active, boolean order) throws EntitlementException {

        Resource[] resources;
        resources = getAllPolicyResource();

        if (resources.length == 0) {
            return new PolicyStoreDTO[0];
        }
        List<PolicyStoreDTO> policyDTOList = new ArrayList<>();
        for (Resource resource : resources) {
            PolicyStoreDTO policyDTO = readPolicy(resource);
            if (active) {
                if (policyDTO.isActive()) {
                    policyDTOList.add(policyDTO);
                }
            } else {
                policyDTOList.add(policyDTO);
            }
        }

        PolicyStoreDTO[] policyDTOs = policyDTOList.toArray(new PolicyStoreDTO[0]);

        if (order) {
            Arrays.sort(policyDTOs, new PolicyOrderComparator());
        }
        return policyDTOs;
    }

    /**
     * Returns all the policies as registry resources.
     *
     * @return policies as Resource[]
     * @throws EntitlementException If an error occurs
     */
    private Resource[] getAllPolicyResource() throws EntitlementException {

        String path;
        Collection collection;
        List<Resource> resources = new ArrayList<>();
        String[] children;

        LOG.debug("Retrieving all entitlement policies");
        try {
            path = policyStorePath;
            Registry registry = getRegistry();
            if (!registry.resourceExists(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Invalid policy store path %s", path));
                }
                return new Resource[0];
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();

            for (String aChildren : children) {
                resources.add(registry.get(aChildren));
            }

        } catch (RegistryException e) {
            throw new EntitlementException("Error while retrieving entitlement policies", e);
        }

        return resources.toArray(new Resource[0]);
    }

    /**
     * Gets all policy IDs.
     *
     * @param path policy registry path
     * @return list of policy IDs
     * @throws EntitlementException If an error occurs
     */
    private List<String> listAllPolicyIds(String path) throws EntitlementException {

        Collection collection;
        String[] children;
        List<String> resources = new ArrayList<>();

        LOG.debug("Retrieving all entitlement policy ids");
        try {
            Registry registry = getRegistry();
            if (!registry.resourceExists(path)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Invalid policy path %s", path));
                }
                return Collections.emptyList();
            }
            collection = (Collection) registry.get(path);
            children = collection.getChildren();
            for (String child : children) {
                String id = child.substring(child.lastIndexOf(RegistryConstants.PATH_SEPARATOR) + 1);
                resources.add(id);
            }

        } catch (RegistryException e) {
            throw new EntitlementException("Error while retrieving entitlement policy resources", e);
        }

        return resources;
    }

    /**
     * Reads PolicyDTO for given registry resource.
     *
     * @param resource Registry resource
     * @return PolicyDTO
     * @throws EntitlementException If an error occurs
     */
    private PolicyStoreDTO readPolicy(Resource resource) throws EntitlementException {

        String policy;
        AbstractPolicy absPolicy;
        PolicyStoreDTO dto;

        try {
            if (resource.getContent() == null) {
                throw new EntitlementException("Error while loading entitlement policy. Policy content is null");
            }
            policy = new String((byte[]) resource.getContent(), StandardCharsets.UTF_8);
            absPolicy = PAPPolicyReader.getInstance(null).getPolicy(policy);
            dto = new PolicyStoreDTO();
            dto.setPolicyId(absPolicy.getId().toASCIIString());
            dto.setPolicy(policy);
            String policyOrder = resource.getProperty("order");
            if (policyOrder != null) {
                dto.setPolicyOrder(Integer.parseInt(policyOrder));
            } else {
                dto.setPolicyOrder(0);
            }
            String policyActive = resource.getProperty("active");
            if (policyActive != null) {
                dto.setActive(Boolean.parseBoolean(policyActive));
            }
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder();
            dto.setAttributeDTOs(policyAttributeBuilder.
                    getPolicyMetaDataFromRegistryProperties(resource.getProperties()));
            return dto;
        } catch (RegistryException e) {
            throw new EntitlementException("Error while loading entitlement policy", e);
        }
    }

    /**
     * Updates the given registry resource.
     *
     * @param policy         publishing policy
     * @param collectionPath registry collection path
     * @param policyPath     registry resource path
     * @throws EntitlementException If an error occurs
     */
    private void updateResource(PolicyStoreDTO policy, String collectionPath, String policyPath)
            throws EntitlementException {

        Collection policyCollection;
        Resource resource;

        try {
            Registry registry = getRegistry();
            policyCollection = (Collection) registry.get(collectionPath);
            resource = registry.get(policyPath);

            if (policy.isSetActive()) {
                resource.setProperty(PDPConstants.ACTIVE_POLICY, Boolean.toString(policy.isActive()));
            }
            if (policy.isSetOrder()) {
                int order = policy.getPolicyOrder();
                if (order > 0) {
                    if (Objects.equals(collectionPath, PDPConstants.ENTITLEMENT_POLICY_PAP)) {
                        String noOfPolicies = policyCollection.getProperty(PDPConstants.MAX_POLICY_ORDER);
                        if (noOfPolicies != null && Integer.parseInt(noOfPolicies) < order) {
                            policyCollection.setProperty(PDPConstants.MAX_POLICY_ORDER, Integer.toString(order));
                            registry.put(PDPConstants.ENTITLEMENT_POLICY_PAP, policyCollection);
                        }
                    }
                    resource.setProperty(PDPConstants.POLICY_ORDER, Integer.toString(order));
                }
            }

            if (policy.isSetOrder() || policy.isSetActive()) {
                resource.setProperty(PDPConstants.LAST_MODIFIED_TIME, Long.toString(System.currentTimeMillis()));
                resource.setProperty(PDPConstants.LAST_MODIFIED_USER,
                        CarbonContext.getThreadLocalCarbonContext().getUsername());
            }

            registry.put(policyPath, resource);

        } catch (RegistryException e) {
            throw new EntitlementException(ERROR_PUBLISHING_POLICY, e);
        }
    }

    private Registry getRegistry() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return EntitlementServiceComponent.getGovernanceRegistry(tenantId);
    }
}
