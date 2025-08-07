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
 * Represents an organization in the context of an identity flow.
 * This class holds the details of an organization, in an organization flow.
 */
public class Organization {

    private final String id;
    private final String name;
    private final String organizationHandle;
    private final String parentOrganizationId;
    private final int depth;

    private Organization(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.organizationHandle = builder.organizationHandle;
        this.parentOrganizationId = builder.parentOrganizationId;
        this.depth = builder.depth;
    }

    /**
     * Returns the UUID of the organization.
     *
     * @return ID of the organization.
     */
    public String getId() {

        return id;
    }

    /**
     * Returns the name of the organization.
     *
     * @return Name of the organization.
     */
    public String getName() {

        return name;
    }

    /**
     * Returns the organization handle of the organization.
     *
     * @return Organization handle of the organization.
     */
    public String getOrganizationHandle() {

        return organizationHandle;
    }

    /**
     * Returns the ID of the parent organization.
     *
     * @return ID of the parent organization.
     */
    public String getParentOrganizationId() {

        return parentOrganizationId;
    }

    /**
     * Returns the depth of the organization in the organization hierarchy.
     *
     * @return Depth of the organization.
     */
    public int getDepth() {

        return depth;
    }

    /**
     * Builder for the Organization.
     */
    public static class Builder {

        private String id;
        private String name;
        private String organizationHandle;
        private String parentOrganizationId;
        private int depth;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder organizationHandle(String organizationHandle) {

            this.organizationHandle = organizationHandle;
            return this;
        }

        public Builder parentOrganizationId(String parentOrganizationId) {

            this.parentOrganizationId = parentOrganizationId;
            return this;
        }

        public Builder depth(int depth) {

            this.depth = depth;
            return this;
        }

        public Organization build() {

            return new Organization(this);
        }
    }
}
