/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.context.model;

/**
 * Represents the tenant information in the context of an identity flow.
 * When the flow is an organization flow, this class holds the details of the root organization.
 */
public class RootOrganization {

    private final int associatedTenantId;
    private final String associatedTenantDomain;
    private final String organizationId;

    private RootOrganization(Builder builder) {

        this.associatedTenantId = builder.associatedTenantId;
        this.associatedTenantDomain = builder.associatedTenantDomain;
        this.organizationId = builder.organizationId;
    }

    /**
     * Returns the integer ID of the tenant associated with the root organization.
     * @return ID of the tenant.
     */
    public int getAssociatedTenantId() {

        return associatedTenantId;
    }

    /**
     * Returns the tenant domain of the tenant associated with the root organization.
     *
     * @return tenant domain of the tenant.
     */
    public String getAssociatedTenantDomain() {

        return associatedTenantDomain;
    }

    /**
     * Returns the id of the organization associated with the root organization.
     *
     * @return Id of the root organization.
     */
    public String getOrganizationId() {

        return organizationId;
    }

    /**
     * Builder for the RootOrganization.
     */
    public static class Builder {

        private int associatedTenantId;
        private String associatedTenantDomain;
        private String organizationId;

        public Builder associatedTenantId(int associatedTenantId) {

            this.associatedTenantId = associatedTenantId;
            return this;
        }

        public Builder associatedTenantDomain(String associatedTenantDomain) {

            this.associatedTenantDomain = associatedTenantDomain;
            return this;
        }

        public Builder organizationId(String organizationId) {

            this.organizationId = organizationId;
            return this;
        }

        public RootOrganization build() {

            return new RootOrganization(this);
        }
    }
}
