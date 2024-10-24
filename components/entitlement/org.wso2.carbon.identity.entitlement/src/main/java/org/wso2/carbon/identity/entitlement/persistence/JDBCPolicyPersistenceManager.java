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
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.PolicyOrderComparator;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.pap.store.PAPPolicyStoreManager;
import org.wso2.carbon.identity.entitlement.persistence.cache.CacheBackedPolicyDAO;
import org.wso2.carbon.identity.entitlement.policy.PolicyAttributeBuilder;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.identity.entitlement.PDPConstants.MODULE_NAME;

/**
 * This class handles the policy operations in the JDBC data store.
 */
public class JDBCPolicyPersistenceManager extends AbstractPolicyFinderModule implements PolicyPersistenceManager {

    private static final Log LOG = LogFactory.getLog(JDBCPolicyPersistenceManager.class);
    private static final String ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER = "Policies can not be retrieved from " +
            "the policy finder module";
    private final int maxVersions;
    private static final CacheBackedPolicyDAO policyDAO = CacheBackedPolicyDAO.getInstance();

    public JDBCPolicyPersistenceManager() {

        maxVersions = EntitlementUtil.getMaxNoOfPolicyVersions();
    }

    @Override
    public void init(Properties properties) {

        // Nothing to initialize
    }

    /**
     * Adds or updates the given PAP policy.
     *
     * @param policy          policy.
     * @param isFromPapAction true if the operation originated from a PAP action, false if it is from a PDP action.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, boolean isFromPapAction) throws EntitlementException {

        // In the JDBC impl we use this method only to add a new policy version
        if (!isFromPapAction) {
            return;
        }

        String policyId = policy.getPolicyId();
        if (StringUtils.isBlank(policyId)) {
            throw new EntitlementException("Invalid Entitlement Policy. Policy or policyId can not be Null");
        }
        boolean newPolicy = false;
        OMElement omElement = null;

        String version = createVersion(policy);
        policy.setVersion(version);
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Creating entitlement policy %s version %s", policyId, version));
        }

        if (StringUtils.isNotBlank(policy.getPolicy())) {
            newPolicy = true;
        }

        // Find policy type
        String policyType = null;
        if (StringUtils.isNotBlank(policy.getPolicyType())) {
            policyType = policy.getPolicyType();
        } else {
            try {
                if (newPolicy) {
                    omElement = AXIOMUtil.stringToOM(policy.getPolicy());
                    policyType = omElement.getLocalName();
                }
            } catch (XMLStreamException e) {
                policyType = PDPConstants.POLICY_ELEMENT;
                LOG.warn("Policy Type can not be found. Default type is set");
            }
        }
        policy.setPolicyType(policyType);

        // Trim policy editor type
        String policyEditorType = null;
        if (StringUtils.isNotBlank(policy.getPolicyEditor())) {
            policyEditorType = policy.getPolicyEditor().trim();
        }
        policy.setPolicyEditor(policyEditorType);

        // Resolve policy references and policy set references of the policy
        if (omElement != null) {
            Iterator iterator1 = omElement.getChildrenWithLocalName(PDPConstants.POLICY_REFERENCE);
            List<String> policyReferences = new ArrayList<>();
            while (iterator1.hasNext()) {
                OMElement policyReference = (OMElement) iterator1.next();
                policyReferences.add(policyReference.getText());
            }
            policy.setPolicyIdReferences(policyReferences.toArray(new String[0]));

            Iterator iterator2 = omElement.getChildrenWithLocalName(PDPConstants.POLICY_SET_REFERENCE);
            List<String> policySetReferences = new ArrayList<>();
            while (iterator2.hasNext()) {
                OMElement policySetReference = (OMElement) iterator2.next();
                policySetReferences.add(policySetReference.getText());
            }
            policy.setPolicySetIdReferences(policySetReferences.toArray(new String[0]));
        }

        // Find policy attributes
        List<AttributeDTO> attributeDTOs = null;
        if (StringUtils.isNotBlank(policy.getPolicy()) && EntitlementUtil.isPolicyMetadataStoringEnabled()) {
            PolicyAttributeBuilder policyAttributeBuilder = new PolicyAttributeBuilder(policy.getPolicy());
            attributeDTOs = policyAttributeBuilder.getAttributesFromPolicy();
        }
        if (attributeDTOs != null && !attributeDTOs.isEmpty()) {
            policy.setAttributeDTOs(attributeDTOs.toArray(new AttributeDTO[0]));
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        policyDAO.insertPolicy(policy, tenantId);
    }

    /**
     * Gets the requested policy.
     *
     * @param policyId policy ID.
     * @return policyDTO object.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PolicyDTO getPAPPolicy(String policyId) throws EntitlementException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving entitlement policy %s", policyId));
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        return policyDAO.getPAPPolicy(policyId, tenantId);
    }

    /**
     * Gets the requested policy list.
     * <p>
     * Note: The `policyIds` parameter is ignored. This method retrieves the full list of PAP policies from the database
     * regardless of the provided policy IDs.
     * </p>
     *
     * @param policyIds A list of policy IDs. This parameter is ignored.
     * @return policyDTO.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<PolicyDTO> getPAPPolicies(List<String> policyIds) throws EntitlementException {

        LOG.debug("Retrieving all PAP entitlement policies");

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return policyDAO.getAllPAPPolicies(tenantId);
    }

    /**
     * Gets the requested policy version. Returns the latest version if version is not specified.
     *
     * @param policyId policy ID.
     * @param version  policy version.
     * @return policyDTO object.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        // Zero means current version
        if (StringUtils.isBlank(version)) {
            version = policyDAO.getLatestPolicyVersion(policyId, tenantId);
            if (StringUtils.isBlank(version)) {
                throw new EntitlementException("Invalid policy version");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving entitlement policy %s for the given version %s", policyId, version));
        }
        return policyDAO.getPapPolicyByVersion(policyId, version, tenantId);
    }

    /**
     * Gets all versions of the given policy ID.
     *
     * @param policyId policy ID.
     * @return array of policy versions.
     */
    @Override
    public String[] getVersions(String policyId) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<String> versions = policyDAO.getPolicyVersions(policyId, tenantId);
        return versions.toArray(new String[0]);
    }

    /**
     * Lists all PAP policy IDs.
     *
     * @return list of policy IDs.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<String> listPolicyIds() throws EntitlementException {

        LOG.debug("Retrieving all entitlement policy IDs");

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return policyDAO.getPAPPolicyIds(tenantId);
    }

    /**
     * Removes the given policy from PAP.
     *
     * @param policyId policy ID.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void removePolicy(String policyId) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Removing entitlement policy %s", policyId));
        }
        if (StringUtils.isBlank(policyId)) {
            throw new EntitlementException("Invalid policy id. Policy id can not be null");
        }
        policyDAO.deletePAPPolicy(policyId, tenantId);
    }

    /**
     * Gets the name of the module.
     *
     * @return name as String.
     */
    @Override
    public String getModuleName() {

        return MODULE_NAME;
    }

    /**
     * Gets the published policy for the given policy ID.
     *
     * @param policyId policy id as a string value.
     * @return policy as string.
     */
    @Override
    public String getPolicy(String policyId) {

        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        return dto.getPolicy();
    }

    /**
     * Gets the policy order.
     *
     * @param policyId policy id as a string value.
     * @return policy order.
     */
    @Override
    public int getPolicyOrder(String policyId) {

        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        return dto.getPolicyOrder();
    }

    /**
     * Gets all supported active, published policies.
     * If policy ordering is supported by the module itself, these policies must be ordered.
     *
     * @return array of policies as Strings.
     */
    @Override
    public String[] getActivePolicies() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Active policies has started at %s", new Date()));
        }

        List<String> policies = new ArrayList<>();

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(true, true);
            for (PolicyStoreDTO dto : policyDTOs) {
                if (StringUtils.isNotBlank(dto.getPolicy())) {
                    policies.add(dto.getPolicy());
                }
            }
        } catch (EntitlementException e) {
            LOG.error(ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Active policies has finished at %s", new Date()));
        }

        return policies.toArray(new String[0]);
    }

    /**
     * Gets all supported ordered policy ids.
     * If policy ordering is supported by the module itself, these policy ids must be ordered.
     *
     * @return array of policy ids as Strings.
     */
    @Override
    public String[] getOrderedPolicyIdentifiers() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Ordered Policy Ids has started at %s", new Date()));
        }

        List<String> policies = new ArrayList<>();

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(false, true);
            for (PolicyStoreDTO dto : policyDTOs) {
                if (StringUtils.isNotBlank(dto.getPolicy())) {
                    policies.add(dto.getPolicyId());
                }
            }
        } catch (EntitlementException e) {
            LOG.error(ERROR_RETRIEVING_POLICIES_FROM_POLICY_FINDER, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving of Ordered Policy Ids is finished at %s", new Date()));
        }

        return policies.toArray(new String[0]);
    }

    /**
     * Gets all published policy ids.
     *
     * @return array of policy ids as Strings.
     */
    @Override
    public String[] getPolicyIdentifiers() {

        String[] policyIds = null;
        try {
            policyIds = listPublishedPolicyIds().toArray(new String[0]);
        } catch (EntitlementException e) {
            LOG.error("Policy identifiers can not be retrieved from the policy finder module", e);
        }
        return policyIds;
    }

    /**
     * Gets reference policy for the given policy ID.
     * Reference policy can not be with PDP policy store, may be in some external policy store.
     * Therefore, a new method has been added to retrieve reference policies.
     *
     * @param policyId policy id as String value.
     * @return reference policy as String.
     */
    @Override
    public String getReferencedPolicy(String policyId) {

        // Retrieve policies that are not active
        PolicyStoreDTO dto = getPublishedPolicy(policyId);
        if (dto != null && StringUtils.isNotBlank(dto.getPolicy()) && !dto.isActive()) {
            return dto.getPolicy();
        }
        return null;
    }

    /**
     * Gets attributes that are used for policy searching.
     *
     * @param identifier     unique identifier to separate out search attributes.
     * @param givenAttribute pre-given attributes to retrieve other attributes.
     * @return return search attributes based on a given policy, Map of policy id with search attributes.
     */
    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {

        try {
            PolicyStoreDTO[] policyDTOs = getAllPolicies(true, true);
            List<PolicyDTO> policyDTOList = new ArrayList<>();
            for (PolicyStoreDTO policyStoreDTO : policyDTOs) {
                PolicyDTO policyDTO = getPAPPolicy(policyStoreDTO.getPolicyId());
                policyDTOList.add(policyDTO);
            }
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
     * @return return scheme identifier value.
     */
    @Override
    public int getSupportedSearchAttributesScheme() {

        return PolicyFinderModule.COMBINATIONS_BY_CATEGORY_AND_PARAMETER;
    }

    /**
     * Publishes the given policy.
     *
     * @param policy policy to be published.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy and policy id can not be null");
        }
        if (StringUtils.isBlank(policy.getVersion())) {
            throw new EntitlementException(String.format("Cannot publish policy %s. Invalid policy version.",
                    policy.getPolicyId()));
        }
        policyDAO.insertOrUpdatePolicy(policy, tenantId);
    }

    /**
     * Updates the policy.
     *
     * @param policy policy.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy and policy id can not be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating policy %s", policy.getPolicyId()));
        }
        if (policy.isSetActive() != policy.isSetOrder()) {
            if (StringUtils.isBlank(policy.getVersion())) {
                // Get published version
                int version = policyDAO.getPublishedVersion(policy, tenantId);
                if (version == -1) {
                    throw new EntitlementException(String.format("Cannot update policy %s. Invalid policy version.",
                            policy.getPolicyId()));
                }
                policy.setVersion(String.valueOf(version));
            }
            policyDAO.updateActiveStatusAndOrder(policy, tenantId);
        } else {
            addPolicy(policy);
        }
    }

    /**
     * Checks whether the given policy is published or not.
     *
     * @param policyId policy ID.
     * @return whether the given policy is published or not.
     */
    @Override
    public boolean isPolicyExist(String policyId) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (StringUtils.isBlank(policyId)) {
            return false;
        }
        return policyDAO.isPolicyPublished(policyId, tenantId);
    }

    /**
     * Gets the requested published policy.
     *
     * @param policyId policy ID.
     * @return requested policy.
     */
    @Override
    public PolicyStoreDTO getPublishedPolicy(String policyId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving entitlement policy %s", policyId));
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PolicyStoreDTO dto = policyDAO.getPDPPolicy(policyId, tenantId);
        if (dto != null) {
            return dto;
        }
        return new PolicyStoreDTO();
    }

    /**
     * Lists all published policy IDs.
     *
     * @return list of published policy IDs.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<String> listPublishedPolicyIds() throws EntitlementException {

        LOG.debug("Retrieving all PDP entitlement policy ids");
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return policyDAO.getPublishedPolicyIds(tenantId);
    }

    /**
     * Un-publishes the policy.
     *
     * @param policyId policy ID.
     */
    @Override
    public boolean deletePolicy(String policyId) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (StringUtils.isBlank(policyId)) {
            return false;
        }
        return policyDAO.unpublishPolicy(policyId, tenantId);
    }

    /**
     * Checks the existence of the policy in PAP
     *
     * @param policyId policy ID.
     * @return whether the policy exists in PAP or not.
     */
    public boolean isPolicyExistsInPap(String policyId) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (policyId == null || policyId.trim().isEmpty()) {
            return false;
        }
        return policyDAO.isPAPPolicyExists(policyId, tenantId);
    }

    /**
     * Creates policy versions.
     *
     * @param policyDTO policyDTO.
     * @return version.
     * @throws EntitlementException throws, if fails.
     */
    private String createVersion(PolicyDTO policyDTO) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PAPPolicyStoreManager manager = new PAPPolicyStoreManager();
        String version = "0";

        if (manager.isExistPolicy(policyDTO.getPolicyId())) {
            PolicyDTO dto = manager.getLightPolicy(policyDTO.getPolicyId());
            version = dto.getVersion();
        }

        int versionInt = Integer.parseInt(version);

        // Check whether this is larger than max version
        if (versionInt > maxVersions) {
            // delete the older version
            int olderVersion = versionInt - maxVersions;
            policyDAO.deletePAPPolicyVersion(policyDTO.getPolicyId(), olderVersion, tenantId);
        }

        // New version
        version = Integer.toString(versionInt + 1);
        return version;
    }

    /**
     * Reads all ordered and active policies as PolicyDTO.
     *
     * @param active only return active policies. Else return all policies.
     * @param order  return ordered policy.
     * @return Array of PolicyDTO.
     * @throws EntitlementException If an error occurs.
     */
    private PolicyStoreDTO[] getAllPolicies(boolean active, boolean order) throws EntitlementException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PolicyStoreDTO[] policies;
        policies = policyDAO.getAllPDPPolicies(tenantId);

        if (policies.length == 0) {
            return new PolicyStoreDTO[0];
        }
        List<PolicyStoreDTO> policyDTOList = new ArrayList<>();
        for (PolicyStoreDTO policy : policies) {
            if (active) {
                if (policy.isActive()) {
                    policyDTOList.add(policy);
                }
            } else {
                policyDTOList.add(policy);
            }
        }

        PolicyStoreDTO[] policyDTOs = policyDTOList.toArray(new PolicyStoreDTO[0]);

        if (order) {
            Arrays.sort(policyDTOs, new PolicyOrderComparator());
        }
        return policyDTOs;
    }
}
