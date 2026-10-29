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
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource.ResourceType;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResourceMetadata;
import org.wso2.carbon.identity.policy.management.api.model.RulePolicyResourceMetadata;

import java.util.List;

/**
 * Service interface for managing policies.
 */
public interface PolicyManagementService {

    /**
     * Creates a new policy. The supplied ID is ignored; a server-generated UUID is assigned.
     *
     * @param policy       Policy to create. Name must be non-blank, the type must be registered, and every resource
     *                     target must be supported by the type.
     * @param tenantDomain Tenant domain.
     * @return Created policy with the server-assigned ID.
     * @throws PolicyManagementException If the policy is invalid, a policy of the same type and name exists, or
     *                                   persistence fails.
     */
    Policy addPolicy(Policy policy, String tenantDomain) throws PolicyManagementException;

    /**
     * Updates an existing policy's resources. Policy ID is immutable and must match an existing policy;
     * policy name and type are also immutable: attempting to update either will throw an exception.
     *
     * @param policy       Policy with the new state. ID must reference an existing policy.
     * @param tenantDomain Tenant domain.
     * @return Updated policy as persisted.
     * @throws PolicyManagementException If the policy is not found, a name or type update is attempted, a resource
     *                                   target is not supported by the policy type, or persistence fails.
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
     * Returns the policy by ID with each rule-typed {@link PolicyResource} enriched from rule-mgt.
     *
     * @param policyId     Policy ID.
     * @param tenantDomain Tenant domain.
     * @return Policy with enriched rules, or {@code null} if not found.
     * @throws PolicyManagementException If retrieval or rule enrichment fails.
     */
    Policy getPolicyById(String policyId, String tenantDomain) throws PolicyManagementException;

    /**
     * Returns the policy of the given type by name with each rule-typed {@link PolicyResource} enriched from
     * rule-mgt.
     *
     * @param policyType   Policy type.
     * @param policyName   Policy name.
     * @param tenantDomain Tenant domain.
     * @return Policy with enriched rules, or {@code null} if not found.
     * @throws PolicyManagementException If the type is not registered, or retrieval or rule enrichment fails.
     */
    Policy getPolicyByName(String policyType, String policyName, String tenantDomain)
            throws PolicyManagementException;

    /**
     * Resolves a policy name to its immutable policy ID. Useful for callers that reference a policy
     * by name but need its ID (e.g. to evaluate the policy by ID).
     *
     * @param policyType   Policy type.
     * @param policyName   Policy name.
     * @param tenantDomain Tenant domain.
     * @return Policy ID, or {@code null} if no policy of the type exists with the given name.
     * @throws PolicyManagementException If the type is not registered or retrieval fails.
     */
    String getPolicyIdByName(String policyType, String policyName, String tenantDomain)
            throws PolicyManagementException;

    /**
     * Returns a page of summaries of the policies of the given type, optionally filtered by name. Summaries are
     * lightweight (no enriched rules); call {@link #getPolicyById} for the full enriched policy.
     *
     * @param tenantDomain Tenant domain.
     * @param policyType   Policy type.
     * @param filter       Name filter; {@code null} or blank returns all policies of the type.
     * @param offset       Zero-based start index.
     * @param limit        Maximum number of results to return.
     * @return Page of policy summaries (never {@code null}).
     * @throws PolicyManagementException If the type is not registered or retrieval fails.
     */
    List<PolicyBasicInfo> getPolicies(String tenantDomain, String policyType, String filter, int offset, int limit)
            throws PolicyManagementException;

    /**
     * Returns the total number of policies of the given type matching the given filter, for pagination.
     *
     * @param tenantDomain Tenant domain.
     * @param policyType   Policy type.
     * @param filter       Name filter; {@code null} or blank counts all policies of the type.
     * @return Total number of matching policies.
     * @throws PolicyManagementException If the type is not registered or retrieval fails.
     */
    int getPolicyCount(String tenantDomain, String policyType, String filter) throws PolicyManagementException;

    /**
     * Returns how resources of a resource type can be built for a policy type and target. The format depends on the
     * resource type, for example {@link RulePolicyResourceMetadata} for rules.
     *
     * @param policyType   Policy type.
     * @param resourceType Resource type.
     * @param target       Resource target, for example a device platform.
     * @param tenantDomain Tenant domain.
     * @return Resource metadata.
     * @throws PolicyManagementException If the type is not registered, does not support the resource type or the
     *                                   target, or the metadata cannot be retrieved.
     */
    PolicyResourceMetadata getResourceMetadata(String policyType, ResourceType resourceType, String target,
                                               String tenantDomain) throws PolicyManagementException;
}
