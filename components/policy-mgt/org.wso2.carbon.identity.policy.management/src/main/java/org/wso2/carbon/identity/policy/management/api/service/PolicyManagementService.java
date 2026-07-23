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

package org.wso2.carbon.identity.policy.management.api.service;

import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;

import java.util.List;

/**
 * Service interface for managing policies.
 */
public interface PolicyManagementService {

    /**
     * Creates a new policy. The supplied ID is ignored; a server-generated UUID is assigned.
     *
     * @param policy       Policy to create. Name must be non-blank.
     * @param tenantDomain Tenant domain.
     * @return Created policy with the server-assigned ID.
     * @throws PolicyManagementException If the policy name is blank or persistence fails.
     */
    Policy addPolicy(Policy policy, String tenantDomain) throws PolicyManagementException;

    /**
     * Replaces an existing policy (PUT semantics). Resources, rules, and actions are replaced wholesale, but the
     * policy name is immutable: the stored name is retained and any name on the supplied model is ignored.
     *
     * @param policy       Policy with the new state. ID must reference an existing policy; any supplied name is
     *                     ignored — the stored name is retained.
     * @param tenantDomain Tenant domain.
     * @return Updated policy as persisted.
     * @throws PolicyManagementException If the policy is not found or persistence fails.
     */
    Policy updatePolicy(Policy policy, String tenantDomain) throws PolicyManagementException;

    /**
     * Deletes the policy. Idempotent — deleting a non-existent policy is a no-op.
     *
     * @param policyId     Policy ID.
     * @param tenantDomain Tenant domain.
     * @throws PolicyManagementException If persistence fails.
     */
    void deletePolicy(String policyId, String tenantDomain) throws PolicyManagementException;

    /**
     * Returns the policy by ID with each rule-typed {@link PolicyResource} hydrated from rule-mgt.
     *
     * @param policyId     Policy ID.
     * @param tenantDomain Tenant domain.
     * @return Policy with hydrated rules, or {@code null} if not found.
     * @throws PolicyManagementException If retrieval or rule hydration fails.
     */
    Policy getPolicyById(String policyId, String tenantDomain) throws PolicyManagementException;

    /**
     * Returns the policy by name with each rule-typed {@link PolicyResource} hydrated from rule-mgt.
     *
     * @param policyName   Policy name.
     * @param tenantDomain Tenant domain.
     * @return Policy with hydrated rules, or {@code null} if not found.
     * @throws PolicyManagementException If retrieval or rule hydration fails.
     */
    Policy getPolicyByName(String policyName, String tenantDomain) throws PolicyManagementException;

    /**
     * Resolves a policy name to its immutable policy ID. Useful for callers that reference a policy
     * by name but need its ID (e.g. to evaluate the policy by ID).
     *
     * @param policyName   Policy name.
     * @param tenantDomain Tenant domain.
     * @return Policy ID, or {@code null} if no policy with the given name exists.
     * @throws PolicyManagementException If retrieval fails.
     */
    String getPolicyIdByName(String policyName, String tenantDomain) throws PolicyManagementException;

    /**
     * Returns a page of policy summaries for the tenant, optionally filtered by name. Summaries are
     * lightweight (no hydrated rules); call {@link #getPolicyById} for the full hydrated policy.
     *
     * @param tenantDomain Tenant domain.
     * @param filter       Name filter; {@code null} or blank returns all policies.
     * @param offset       Zero-based start index.
     * @param limit        Maximum number of results to return.
     * @return Page of policy summaries (never {@code null}).
     * @throws PolicyManagementException If retrieval fails.
     */
    List<PolicyBasicInfo> getPolicies(String tenantDomain, String filter, int offset, int limit)
            throws PolicyManagementException;

    /**
     * Returns the total number of policies matching the given filter, for pagination.
     *
     * @param tenantDomain Tenant domain.
     * @param filter       Name filter; {@code null} or blank counts all policies.
     * @return Total number of matching policies.
     * @throws PolicyManagementException If retrieval fails.
     */
    int getPolicyCount(String tenantDomain, String filter) throws PolicyManagementException;
}
