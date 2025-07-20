/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.api.model;

/**
 * This class models the Organization.
 * Organization is the entity that represents the organization of the user for whom the action is triggered for.
 */
public class Organization {

    private final String id;
    private final String name;
    private String orgHandle;
    private int depth;

    public Organization(String id, String name) {

        this.id = id;
        this.name = name;
    }

    public Organization(String id, String name, String orgHandle) {

        this.id = id;
        this.name = name;
        this.orgHandle = orgHandle;
    }

    private Organization(Builder builder) {

        this.id = builder.id;
        this.name = builder.name;
        this.orgHandle = builder.orgHandle;
        this.depth = builder.depth;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getOrgHandle() {

        return orgHandle;
    }

    public int getDepth() {

        return depth;
    }

    /**
     * Builder class for Organization.
     */
    public static class Builder {

        private String id;
        private String name;
        private String orgHandle;
        private int depth;

        public Builder id(String id) {

            this.id = id;
            return this;
        }

        public Builder name(String name) {

            this.name = name;
            return this;
        }

        public Builder orgHandle(String orgHandle) {

            this.orgHandle = orgHandle;
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
