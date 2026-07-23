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

package org.wso2.carbon.identity.policy.management.api.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.policy.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Represents a policy.
 * Instances are created through {@link Builder}, which validates the resource list so that a policy
 * holding invalid or conflicting resources can never exist. The name is not validated here because it
 * is optional on update, where the stored name is retained.
 */
public class Policy {

    private final String id;
    private final String name;
    private final String tenantDomain;
    private final List<PolicyResource> resources;

    private Policy(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.tenantDomain = builder.tenantDomain;
        this.resources = builder.resources != null
                ? Collections.unmodifiableList(new ArrayList<>(builder.resources)) : Collections.emptyList();
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public List<PolicyResource> getResources() {

        return resources;
    }

    /**
     * Builder for {@link Policy}.
     */
    public static class Builder {

        private static final String RESOURCE_FIELD = "Resource";
        private static final String RESOURCE_TYPE_FIELD = "Resource type";
        private static final String TARGET_FIELD = "Target";

        private String id;
        private String name;
        private String tenantDomain;
        private List<PolicyResource> resources;

        /**
         * Creates an empty builder.
         */
        public Builder() {

        }

        /**
         * Creates a builder pre-populated from an existing policy.
         *
         * @param policy Policy to copy the field values from.
         */
        public Builder(Policy policy) {

            this.id = policy.getId();
            this.name = policy.getName();
            this.tenantDomain = policy.getTenantDomain();
            this.resources = policy.getResources();
        }

        /**
         * Sets the policy ID.
         *
         * @param id Policy ID, or {@code null} if not yet persisted.
         * @return This builder.
         */
        public Builder id(String id) {

            this.id = id;
            return this;
        }

        /**
         * Sets the policy name.
         *
         * @param name Policy name.
         * @return This builder.
         */
        public Builder name(String name) {

            this.name = name;
            return this;
        }

        /**
         * Sets the tenant domain.
         *
         * @param tenantDomain Tenant domain.
         * @return This builder.
         */
        public Builder tenantDomain(String tenantDomain) {

            this.tenantDomain = tenantDomain;
            return this;
        }

        /**
         * Sets the policy resources.
         *
         * @param resources Policy resources.
         * @return This builder.
         */
        public Builder resources(List<PolicyResource> resources) {

            this.resources = resources;
            return this;
        }

        /**
         * Builds the policy after validating its resources.
         *
         * @return Policy instance.
         * @throws PolicyManagementClientException If a resource is null, reports no resource type, or if two
         *                                         resources of the same type share a target.
         */
        public Policy build() throws PolicyManagementClientException {

            validateResources();
            return new Policy(this);
        }

        private void validateResources() throws PolicyManagementClientException {

            if (resources == null) {
                return;
            }
            Set<String> seenTargets = new HashSet<>();
            for (PolicyResource resource : resources) {
                if (resource == null) {
                    throw invalidField(RESOURCE_FIELD);
                }
                if (resource.getResourceType() == null) {
                    throw invalidField(RESOURCE_TYPE_FIELD);
                }
                if (StringUtils.isBlank(resource.getTarget())) {
                    throw invalidField(TARGET_FIELD);
                }
                String key = resource.getResourceType().name() + "|"
                        + resource.getTarget().toLowerCase(Locale.ROOT);
                if (!seenTargets.add(key)) {
                    throw new PolicyManagementClientException(
                            ErrorMessage.ERROR_DUPLICATE_TARGET_IN_POLICY.getMessage(),
                            String.format(ErrorMessage.ERROR_DUPLICATE_TARGET_IN_POLICY.getDescription(),
                                    name, resource.getTarget()),
                            ErrorMessage.ERROR_DUPLICATE_TARGET_IN_POLICY.getCode());
                }
            }
        }

        private PolicyManagementClientException invalidField(String field) {

            return new PolicyManagementClientException(
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getMessage(),
                    String.format(ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getDescription(), field),
                    ErrorMessage.ERROR_INVALID_POLICY_REQUEST_FIELD.getCode());
        }
    }
}
