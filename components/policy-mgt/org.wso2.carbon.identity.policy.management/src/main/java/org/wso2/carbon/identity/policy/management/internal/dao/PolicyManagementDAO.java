/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.internal.dao;

import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;

import java.util.List;

/**
 * Interface for Policy Management DAO.
 * Handles CRUD for IDN_POLICY and IDN_POLICY_RESOURCE tables.
 * Returns Policy objects with PolicyResource lists populated (resourceIds only — rules are hydrated by the
 * service layer).
 */
public interface PolicyManagementDAO {

    /**
     * Persists a new policy and its resources.
     *
     * @param policy   Policy to persist (id pre-assigned).
     * @param tenantId Tenant ID.
     * @return Persisted policy.
     * @throws PolicyManagementException If persistence fails.
     */
    Policy addPolicy(Policy policy, int tenantId) throws PolicyManagementException;

    /**
     * Replaces an existing policy and its resources (the resource rows are deleted and re-inserted).
     *
     * @param policy   Policy with the new state.
     * @param tenantId Tenant ID.
     * @return Updated policy.
     * @throws PolicyManagementException If persistence fails.
     */
    Policy updatePolicy(Policy policy, int tenantId) throws PolicyManagementException;

    /**
     * Deletes the policy and its resources (cascaded). Idempotent.
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @throws PolicyManagementException If persistence fails.
     */
    void deletePolicy(String policyId, int tenantId) throws PolicyManagementException;

    /**
     * Returns the policy by ID with its resources (resource ids only; rules are not hydrated).
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @return Policy, or {@code null} if no policy exists for the given ID and tenant.
     * @throws PolicyManagementException If retrieval fails.
     */
    Policy getPolicyById(String policyId, int tenantId) throws PolicyManagementException;

    /**
     * Returns the policy by name with its resources (resource ids only; rules are not hydrated).
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy, or {@code null} if no policy exists for the given name and tenant.
     * @throws PolicyManagementException If retrieval fails.
     */
    Policy getPolicyByName(String policyName, int tenantId) throws PolicyManagementException;

    /**
     * Returns a paginated, optionally name-filtered list of policies (basic info only), ordered by name.
     *
     * @param tenantId Tenant ID.
     * @param filter   Case-insensitive substring to match against the policy name; {@code null}/blank for no filter.
     * @param offset   Number of records to skip.
     * @param limit    Maximum number of records to return; a non-positive value yields an empty list.
     * @return List of matching policies (never {@code null}).
     * @throws PolicyManagementException If retrieval fails.
     */
    List<PolicyBasicInfo> getPolicies(int tenantId, String filter, int offset, int limit)
            throws PolicyManagementException;

    /**
     * Returns the total number of policies for the tenant, applying the same name filter as
     * {@link #getPolicies(int, String, int, int)}.
     *
     * @param tenantId Tenant ID.
     * @param filter   Case-insensitive substring to match against the policy name; {@code null}/blank for no filter.
     * @return Matching policy count.
     * @throws PolicyManagementException If retrieval fails.
     */
    int getPolicyCount(int tenantId, String filter) throws PolicyManagementException;

    /**
     * Returns the ID of the policy with the given name, used for name-uniqueness checks.
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy ID, or {@code null} if no policy exists with that name for the tenant.
     * @throws PolicyManagementException If retrieval fails.
     */
    String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException;
}
