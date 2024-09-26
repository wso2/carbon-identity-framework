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

import org.apache.commons.lang.StringUtils;
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

import static org.wso2.carbon.identity.entitlement.PDPConstants.MODULE_NAME;

/**
 * HybridPolicyPersistenceManager is a hybrid implementation of PolicyPersistenceManager. It uses both JDBC and Registry
 * implementations to handle policy data. If the policy is already in the registry, it will be maintained there,
 * including new versions. New policies will be persisted in the database.
 */
public class HybridPolicyPersistenceManager extends AbstractPolicyFinderModule implements PolicyPersistenceManager {

    private final JDBCPolicyPersistenceManager jdbcPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
    private final RegistryPolicyPersistenceManager registryPolicyPersistenceManager =
            new RegistryPolicyPersistenceManager();

    @Override
    public void init(Properties properties) {

        jdbcPolicyPersistenceManager.init(properties);
        registryPolicyPersistenceManager.init(properties);
    }

    /**
     * Checks the data source of the policy and proceeds with add or update. If registry already contains older
     * versions of the policy, new versions are created there.
     *
     * @param policy          policy.
     * @param isFromPapAction true if the operation originated from a PAP action, false if it is from a PDP action.
     * @throws EntitlementException If an error occurs.
     */
    @Override
    public void addOrUpdatePolicy(PolicyDTO policy, boolean isFromPapAction) throws EntitlementException {

        if (registryPolicyPersistenceManager.isPolicyExistsInPap(policy.getPolicyId())) {
            registryPolicyPersistenceManager.addOrUpdatePolicy(policy, isFromPapAction);
        } else {
            jdbcPolicyPersistenceManager.addOrUpdatePolicy(policy, isFromPapAction);
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

        PolicyDTO policyDTO = jdbcPolicyPersistenceManager.getPAPPolicy(policyId);
        if (policyDTO == null) {
            policyDTO = registryPolicyPersistenceManager.getPAPPolicy(policyId);
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

        List<PolicyDTO> policyDTOs = jdbcPolicyPersistenceManager.getPAPPolicies(policyIds);
        List<PolicyDTO> regPolicyDTOs = registryPolicyPersistenceManager.getPAPPolicies(policyIds);
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

        if (jdbcPolicyPersistenceManager.isPolicyExistsInPap(policyId)) {
            return jdbcPolicyPersistenceManager.getPolicy(policyId, version);
        } else {
            return registryPolicyPersistenceManager.getPolicy(policyId, version);
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

        String[] versions = jdbcPolicyPersistenceManager.getVersions(policyId);
        if (versions.length == 0) {
            versions = registryPolicyPersistenceManager.getVersions(policyId);
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

        String policy = jdbcPolicyPersistenceManager.getPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyPersistenceManager.getPolicy(policyId);
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

        if (jdbcPolicyPersistenceManager.isPolicyExist(policyId)) {
            return jdbcPolicyPersistenceManager.getPolicyOrder(policyId);
        } else {
            return registryPolicyPersistenceManager.getPolicyOrder(policyId);
        }
    }

    /**
     * Gets all supported active, published policies from both DB and registry.
     * If policy ordering is supported by the module itself, these policies must be ordered.
     *
     * @return array of policies as Strings.
     */
    @Override
    public String[] getActivePolicies() {

        String[] dbActivePolicies = jdbcPolicyPersistenceManager.getActivePolicies();
        String[] regActivePolicies = registryPolicyPersistenceManager.getActivePolicies();
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

        String[] dbPolicyIds = jdbcPolicyPersistenceManager.getOrderedPolicyIdentifiers();
        String[] regPolicyIds = registryPolicyPersistenceManager.getOrderedPolicyIdentifiers();
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

        String[] dbPolicyIds = jdbcPolicyPersistenceManager.getPolicyIdentifiers();
        String[] regPolicyIds = registryPolicyPersistenceManager.getPolicyIdentifiers();
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

        String policy = jdbcPolicyPersistenceManager.getReferencedPolicy(policyId);
        if (policy == null) {
            policy = registryPolicyPersistenceManager.getReferencedPolicy(policyId);
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

        Map<String, Set<AttributeDTO>> searchAttributes =
                jdbcPolicyPersistenceManager.getSearchAttributes(identifier, givenAttribute);
        Map<String, Set<AttributeDTO>> regSearchAttributes =
                registryPolicyPersistenceManager.getSearchAttributes(identifier, givenAttribute);
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

        List<String> policyIds = jdbcPolicyPersistenceManager.listPolicyIds();
        List<String> regPolicyIds = registryPolicyPersistenceManager.listPolicyIds();
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

        if (jdbcPolicyPersistenceManager.isPolicyExistsInPap(policyId)) {
            jdbcPolicyPersistenceManager.removePolicy(policyId);
        } else {
            registryPolicyPersistenceManager.removePolicy(policyId);
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

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy and policy id can not be null");
        }
        if (jdbcPolicyPersistenceManager.isPolicyExistsInPap(policy.getPolicyId())) {
            jdbcPolicyPersistenceManager.addPolicy(policy);
        } else {
            registryPolicyPersistenceManager.addPolicy(policy);
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

        if (policy == null || StringUtils.isBlank(policy.getPolicyId())) {
            throw new EntitlementException("Policy and policy id can not be null");
        }
        if (jdbcPolicyPersistenceManager.isPolicyExistsInPap(policy.getPolicyId())) {
            jdbcPolicyPersistenceManager.updatePolicy(policy);
        } else {
            registryPolicyPersistenceManager.updatePolicy(policy);
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

        return jdbcPolicyPersistenceManager.isPolicyExist(policyId) ||
                registryPolicyPersistenceManager.isPolicyExist(policyId);
    }

    /**
     * Gets the requested published policy from either DB or registry.
     *
     * @param policyId policy ID.
     * @return requested policy.
     */
    @Override
    public PolicyStoreDTO getPublishedPolicy(String policyId) {

        PolicyStoreDTO policyDTO = jdbcPolicyPersistenceManager.getPublishedPolicy(policyId);
        if (policyDTO == null || policyDTO.getPolicy() == null) {
            policyDTO = registryPolicyPersistenceManager.getPublishedPolicy(policyId);
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

        List<String> dbPolicyIds = jdbcPolicyPersistenceManager.listPublishedPolicyIds();
        List<String> regPolicyIds = registryPolicyPersistenceManager.listPublishedPolicyIds();
        return EntitlementUtil.mergeLists(dbPolicyIds, regPolicyIds);
    }

    /**
     * Un-publishes the policy from either DB or registry according to the existence.
     *
     * @param policyId policy ID.
     */
    @Override
    public boolean deletePolicy(String policyId) {

        if (jdbcPolicyPersistenceManager.isPolicyExist(policyId)) {
            return jdbcPolicyPersistenceManager.deletePolicy(policyId);
        } else {
            return registryPolicyPersistenceManager.deletePolicy(policyId);
        }
    }
}
