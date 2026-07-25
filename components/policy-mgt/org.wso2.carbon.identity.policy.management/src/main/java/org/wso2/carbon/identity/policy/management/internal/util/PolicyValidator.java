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

/**
 * Validates policy level rules that cannot be enforced by {@link Policy.Builder}, namely the presence of
 * the policy itself and of a name on creation. Resource level validation is performed by the builders.
 * Every failure is raised as a {@link PolicyManagementClientException} so that the API layer reports it
 * as a client error rather than a server error.
 */
public class PolicyValidator {

    private static final String POLICY_FIELD = "Policy";
    private static final String POLICY_NAME_FIELD = "Policy name";
    private static final String TENANT_DOMAIN_FIELD = "Tenant domain";

    /**
     * Validate a policy supplied for creation.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy is null or its name is missing.
     */
    public void validateForAdd(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
        if (StringUtils.isBlank(policy.getName())) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_NAME_FIELD);
        }
    }

    /**
     * Validate a policy supplied for an update.
     * The name is immutable on update and is therefore not validated here.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy is null.
     */
    public void validateForUpdate(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
    }

    /**
     * Validate tenant domain parameter.
     *
     * @param tenantDomain Tenant domain.
     * @throws PolicyManagementClientException If tenant domain is blank.
     */
    public void validateTenantDomain(String tenantDomain) throws PolicyManagementClientException {

        if (StringUtils.isBlank(tenantDomain)) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, TENANT_DOMAIN_FIELD);
        }
    }

    private void validateNotNull(Policy policy) throws PolicyManagementClientException {

        if (policy == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_FIELD);
        }
    }
}
