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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a policy.
 * Instances are created through {@link Builder}. Field validation is not performed here; user supplied
 * policies are validated by the service layer so that failures surface as client errors.
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
         * Builds the policy.
         *
         * @return Policy instance.
         */
        public Policy build() {

            return new Policy(this);
        }
    }
}
