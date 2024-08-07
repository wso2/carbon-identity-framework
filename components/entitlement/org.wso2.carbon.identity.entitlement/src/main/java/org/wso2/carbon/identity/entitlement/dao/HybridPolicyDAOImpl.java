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

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.EntitlementUtil;
import org.wso2.carbon.identity.entitlement.dto.AttributeDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.policy.finder.AbstractPolicyFinderModule;
import org.wso2.carbon.identity.entitlement.policy.finder.PolicyFinderModule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * HybridPolicyDAOImpl is a hybrid implementation of PolicyDAO. It uses both JDBC and Registry implementations to handle
 * policy data. If the policy is already in the registry, it will be maintained there, including new versions.
 * New policies will be persisted in the database.
 */
public class HybridPolicyDAOImpl extends AbstractPolicyFinderModule implements PolicyDAO {

    private final JDBCPolicyDAOImpl jdbcPolicyDAO = new JDBCPolicyDAOImpl();
    private final RegistryPolicyDAOImpl registryPolicyDAO = new RegistryPolicyDAOImpl();
    private static final String MODULE_NAME = "Hybrid Policy Finder Module";

    @Override
    public void init(Properties properties) {

        jdbcPolicyDAO.init(properties);
        registryPolicyDAO.init(properties);
    }

    /**
     * Checks the data source of the policy and proceeds with add or update. If registry already contains older
     * versions of the policy, new versions are created there.
     *
     * @param policy      policy.
     * @param isFromPapAction true if the operation originated from a PAP action, false if it is from a PDP action.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, boolean isFromPapAction) throws EntitlementException {

        if (registryPolicyDAO.isPolicyExistsInPap(policy.getPolicyId())) {
            registryPolicyDAO.addOrUpdatePolicy(policy, isFromPapAction);
        } else {
            jdbcPolicyDAO.addOrUpdatePolicy(policy, isFromPapAction);
        }
    }

    /**
     * Gets the requested policy from DB or registry.
     *
     * @param policyId policy ID.
     * @return policyDTO object.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PolicyDTO getPAPPolicy(String policyId) throws EntitlementException {

        PolicyDTO policyDTO = jdbcPolicyDAO.getPAPPolicy(policyId);
        if (policyDTO == null) {
            policyDTO = registryPolicyDAO.getPAPPolicy(policyId);
        }
        return policyDTO;
    }

    /**
     * Gets the requested policy lists from both DB and registry to create the complete policy ID list.
     *
     * @param policyIds policy ID list.
     * @return policyDTO.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<PolicyDTO> getPAPPolicies(List<String> policyIds) throws EntitlementException {

        List<PolicyDTO> policyDTOs = jdbcPolicyDAO.getPAPPolicies(policyIds);
        List<PolicyDTO> regPolicyDTOs = registryPolicyDAO.getPAPPolicies(policyIds);
        return EntitlementUtil.mergeLists(policyDTOs, regPolicyDTOs);
    }

    /**
     * Gets the requested policy version from DB or registry as per the existence.
     *
     * @param policyId policy ID.
     * @param version  policy version.
     * @return policyDTO object.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        if (jdbcPolicyDAO.isPolicyExistsInPap(policyId)) {
            return jdbcPolicyDAO.getPolicy(policyId, version);
        } else {
            return registryPolicyDAO.getPolicy(policyId, version);
        }
    }

    /**
     * Gets all versions of the given policy ID. If an empty array is returned, tries to retrieve the versions form
     * registry.
     *
     * @param policyId policy ID.
     * @return array of policy versions.
     */
    @Override
    public String[] getVersions(String policyId) {

        String[] versions = jdbcPolicyDAO.getVersions(policyId);
        if (versions.length == 0) {
            versions = registryPolicyDAO.getVersions(policyId);
        }
        return versions;
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
     * Gets the published policy for the given policy ID from DB. If null, queries the registry.
     *
     * @param policyId policy id as a string value.
     * @return policy as string.
     */
    @Override
    public String getPolicy(String policyId) {

        String policy = jdbcPolicyDAO.getPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyDAO.getPolicy(policyId);
        }
        return policy;
    }

    /**
     * Gets the policy order from DB or registry.
     *
     * @param policyId policy id as a string value.
     * @return policy order.
     */
    @Override
    public int getPolicyOrder(String policyId) {

        int policyOrder = jdbcPolicyDAO.getPolicyOrder(policyId);
        if (policyOrder == -1) {
            policyOrder = registryPolicyDAO.getPolicyOrder(policyId);
        }
        return policyOrder;
    }

    /**
     * Gets all supported active, published policies from both DB and registry.
     * If policy ordering is supported by the module itself, these policies must be ordered.
     *
     * @return array of policies as Strings.
     */
    @Override
    public String[] getActivePolicies() {

        String[] dbActivePolicies = jdbcPolicyDAO.getActivePolicies();
        String[] regActivePolicies = registryPolicyDAO.getActivePolicies();
        return EntitlementUtil.mergeLists(Arrays.asList(dbActivePolicies),
                Arrays.asList(regActivePolicies)).toArray(new String[0]);
    }

    /**
     * Gets all supported ordered policy ids from both DB and registry.
     * If policy ordering is supported by the module itself, these policy ids must be ordered.
     *
     * @return array of policy ids as Strings.
     */
    @Override
    public String[] getOrderedPolicyIdentifiers() {

        String[] dbPolicyIds = jdbcPolicyDAO.getOrderedPolicyIdentifiers();
        String[] regPolicyIds = registryPolicyDAO.getOrderedPolicyIdentifiers();
        return EntitlementUtil.mergeLists(Arrays.asList(dbPolicyIds), Arrays.asList(regPolicyIds))
                .toArray(new String[0]);
    }

    /**
     * Gets all published policy ids from both DB and registry.
     *
     * @return array of policy ids as Strings.
     */
    @Override
    public String[] getPolicyIdentifiers() {

        String[] dbPolicyIds = jdbcPolicyDAO.getPolicyIdentifiers();
        String[] regPolicyIds = registryPolicyDAO.getPolicyIdentifiers();
        return EntitlementUtil.mergeLists(Arrays.asList(dbPolicyIds), Arrays.asList(regPolicyIds))
                .toArray(new String[0]);
    }

    /**
     * Gets reference policy for the given policy ID from DB or registry.
     *
     * @param policyId policy id as String value.
     * @return reference policy as String.
     */
    @Override
    public String getReferencedPolicy(String policyId) {

        String policy = jdbcPolicyDAO.getReferencedPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyDAO.getReferencedPolicy(policyId);
        }
        return policy;
    }

    /**
     * Gets attributes that are used for policy searching from both DB and registry.
     *
     * @param identifier     unique identifier to separate out search attributes.
     * @param givenAttribute pre-given attributes to retrieve other attributes.
     * @return return search attributes based on a given policy, Map of policy id with search attributes.
     */
    @Override
    public Map<String, Set<AttributeDTO>> getSearchAttributes(String identifier, Set<AttributeDTO> givenAttribute) {

        Map<String, Set<AttributeDTO>> searchAttributes = jdbcPolicyDAO.getSearchAttributes(identifier, givenAttribute);
        Map<String, Set<AttributeDTO>> regSearchAttributes =
                registryPolicyDAO.getSearchAttributes(identifier, givenAttribute);
        for (Map.Entry<String, Set<AttributeDTO>> entry : regSearchAttributes.entrySet()) {
            searchAttributes.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return searchAttributes;
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
     * Lists all PAP policy IDs from both DB and registry.
     *
     * @return list of policy IDs.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<String> listPolicyIds() throws EntitlementException {

        List<String> policyIds = jdbcPolicyDAO.listPolicyIds();
        List<String> regPolicyIds = registryPolicyDAO.listPolicyIds();
        return EntitlementUtil.mergeLists(policyIds, regPolicyIds);
    }

    /**
     * Removes the given policy from PAP from either DB or registry according to the existence.
     *
     * @param policyId policy ID.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void removePolicy(String policyId) throws EntitlementException {

        if (jdbcPolicyDAO.isPolicyExistsInPap(policyId)) {
            jdbcPolicyDAO.removePolicy(policyId);
        } else {
            registryPolicyDAO.removePolicy(policyId);
        }
    }

    /**
     * Publishes the given policy in either DB or registry according to the existence.
     *
     * @param policy policy to be published.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addPolicy(PolicyStoreDTO policy) throws EntitlementException {

        if (jdbcPolicyDAO.isPolicyExistsInPap(policy.getPolicyId())) {
            jdbcPolicyDAO.addPolicy(policy);
        } else {
            registryPolicyDAO.addPolicy(policy);
        }
    }

    /**
     * Updates the policy in either DB or registry according to the existence.
     *
     * @param policy policy.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void updatePolicy(PolicyStoreDTO policy) throws EntitlementException {

        if (jdbcPolicyDAO.isPolicyExistsInPap(policy.getPolicyId())) {
            jdbcPolicyDAO.updatePolicy(policy);
        } else {
            registryPolicyDAO.updatePolicy(policy);
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

        return jdbcPolicyDAO.isPolicyExist(policyId) || registryPolicyDAO.isPolicyExist(policyId);
    }

    /**
     * Gets the requested published policy from either DB or registry.
     *
     * @param policyId policy ID.
     * @return requested policy.
     */
    @Override
    public PolicyDTO getPublishedPolicy(String policyId) {

        PolicyDTO policyDTO = jdbcPolicyDAO.getPublishedPolicy(policyId);
        if (policyDTO == null || policyDTO.getPolicy() == null) {
            policyDTO = registryPolicyDAO.getPublishedPolicy(policyId);
        }
        return policyDTO;
    }

    /**
     * Lists all published policy IDs from both DB and registry.
     *
     * @return list of published policy IDs.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public List<String> listPublishedPolicyIds() throws EntitlementException {

        List<String> dbPolicyIds = jdbcPolicyDAO.listPublishedPolicyIds();
        List<String> regPolicyIds = registryPolicyDAO.listPublishedPolicyIds();
        return EntitlementUtil.mergeLists(dbPolicyIds, regPolicyIds);
    }

    /**
     * Un-publishes the policy from either DB or registry according to the existence.
     *
     * @param policyId policy ID.
     */
    @Override
    public boolean deletePolicy(String policyId) {

        if (jdbcPolicyDAO.isPolicyExistsInPap(policyId)) {
            return jdbcPolicyDAO.deletePolicy(policyId);
        } else {
            return registryPolicyDAO.deletePolicy(policyId);
        }
    }
}
