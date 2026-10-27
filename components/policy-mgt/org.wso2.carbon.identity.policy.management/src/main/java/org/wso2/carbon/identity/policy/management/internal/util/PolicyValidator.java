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
import org.wso2.carbon.identity.policy.management.api.type.PolicyTypeDefinition;
import org.wso2.carbon.identity.policy.management.internal.component.PolicyMgtComponentServiceHolder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validates policy level rules that cannot be enforced by {@link Policy.Builder}, namely the presence of
 * the policy itself, of a name and type on creation, and that the type and resource targets are supported by
 * a registered {@link PolicyTypeDefinition}. Resource level validation is performed by the builders.
 * Every failure is raised as a {@link PolicyManagementClientException} so that the API layer reports it
 * as a client error rather than a server error.
 */
public class PolicyValidator {

    private static final String POLICY_FIELD = "Policy";
    private static final String POLICY_NAME_FIELD = "Policy name";
    private static final String POLICY_TYPE_FIELD = "Policy type";
    private static final String TENANT_DOMAIN_FIELD = "Tenant domain";

    /**
     * Validate a policy supplied for creation.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy is null, its name or type is missing, its type is not
     *                                         registered, or a resource target is not supported by the type.
     */
    public void validateForAdd(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
        if (StringUtils.isBlank(policy.getName())) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_NAME_FIELD);
        }
        validatePolicyType(policy.getType());
        validateTargets(policy.getType(), policy.getResources());
    }

    /**
     * Validate a policy supplied for an update.
     * The name and type are immutable on update and are therefore not validated here.
     *
     * @param policy Policy to validate.
     * @throws PolicyManagementClientException If the policy is null.
     */
    public void validateForUpdate(Policy policy) throws PolicyManagementClientException {

        validateNotNull(policy);
    }

    /**
     * Validate that a policy type is provided and registered.
     *
     * @param type Policy type.
     * @throws PolicyManagementClientException If the type is blank or not registered.
     */
    public void validatePolicyType(String type) throws PolicyManagementClientException {

        if (StringUtils.isBlank(type)) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD, POLICY_TYPE_FIELD);
        }
        if (PolicyMgtComponentServiceHolder.getInstance().getPolicyType(type) == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_UNSUPPORTED_POLICY_TYPE, type);
        }
    }

    /**
     * Validate that every resource target is supported by the policy type.
     *
     * @param type      Policy type.
     * @param resources Policy resources.
     * @throws PolicyManagementClientException If the type is not registered, or a resource target is not supported
     *                                         by the type.
     */
    public void validateTargets(String type, List<PolicyResource> resources) throws PolicyManagementClientException {

        // On update the stored type is used, which may no longer be registered if its bundle has been removed.
        PolicyTypeDefinition definition = PolicyMgtComponentServiceHolder.getInstance().getPolicyType(type);
        if (definition == null) {
            throw PolicyManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_UNSUPPORTED_POLICY_TYPE, type);
        }
        Set<String> supportedTargets = definition.getSupportedTargets() != null
                ? definition.getSupportedTargets() : Collections.emptySet();
        for (PolicyResource resource : resources) {
            if (!supportedTargets.contains(resource.getTarget().toLowerCase(Locale.ROOT))) {
                throw PolicyManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_UNSUPPORTED_POLICY_TARGET, resource.getTarget(), type);
            }
        }
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
