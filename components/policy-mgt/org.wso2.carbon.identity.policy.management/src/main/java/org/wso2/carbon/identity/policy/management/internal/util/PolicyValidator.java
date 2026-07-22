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

package org.wso2.carbon.identity.policy.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates user supplied policies before they are persisted.
 * Every failure is raised as a {@link PolicyManagementClientException} so that the API layer reports it
 * as a client error rather than a server error.
 */
public class PolicyValidator {

    private static final String POLICY_FIELD = "Policy";
    private static final String POLICY_NAME_FIELD = "Policy name";
    private static final String RESOURCE_FIELD = "Resource";
    private static final String RESOURCE_TYPE_FIELD = "Resource type";
    private static final String TARGET_FIELD = "Target";

    /**
     * Validate a policy supplied for creation.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy, its name or any of its resources are invalid.
     */
    public void validateForAdd(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
        if (StringUtils.isBlank(policy.getName())) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_NAME_FIELD);
        }
        validateResources(policy);
    }

    /**
     * Validate a policy supplied for an update.
     * The name is immutable on update and is therefore not validated here.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy or any of its resources are invalid.
     */
    public void validateForUpdate(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
        validateResources(policy);
    }

    /**
     * Validate that a policy name was supplied.
     *
     * @param policyName Policy name to validate.
     * @throws PolicyManagementClientException If the policy name is null or blank.
     */
    public void validatePolicyName(String policyName) throws PolicyManagementClientException {

        if (StringUtils.isBlank(policyName)) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_NAME_FIELD);
        }
    }

    private void validateNotNull(Policy policy) throws PolicyManagementClientException {

        if (policy == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_FIELD);
        }
    }

    /**
     * Validate that every resource is present, typed, targeted, and that no two resources of the same
     * type share a target.
     *
     * @param policy Policy whose resources are validated.
     * @throws PolicyManagementClientException If a resource is invalid or duplicates another target.
     */
    private void validateResources(Policy policy) throws PolicyManagementClientException {

        Set<String> seenTargets = new HashSet<>();
        for (PolicyResource resource : policy.getResources()) {
            if (resource == null) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, RESOURCE_FIELD);
            }
            if (resource.getResourceType() == null) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, RESOURCE_TYPE_FIELD);
            }
            if (StringUtils.isBlank(resource.getTarget())) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, TARGET_FIELD);
            }
            String key = resource.getResourceType().name() + "|"
                    + resource.getTarget().toLowerCase(Locale.ROOT);
            if (!seenTargets.add(key)) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_DUPLICATE_TARGET_IN_POLICY,
                        policy.getName(), resource.getTarget());
            }
        }
    }
}
