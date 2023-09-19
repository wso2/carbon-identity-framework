/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model;

/**
 * Represents a scope.
 */
public class Scope {

    private String id;
    private String name;
    private String displayName;
    private String description;
    private String apiID;
    private String orgID;

    public Scope() {

    }

    public Scope(String scopeId, String scopeQualifiedName, String scopeDisplayName, String scopeDescription) {

        this.id = scopeId;
        this.name = scopeQualifiedName;
        this.displayName = scopeDisplayName;
        this.description = scopeDescription;
    }

    public Scope(String scopeId, String scopeQualifiedName, String scopeDisplayName, String scopeDescription,
                 String apiID, String orgID) {

        this.id = scopeId;
        this.name = scopeQualifiedName;
        this.displayName = scopeDisplayName;
        this.description = scopeDescription;
        this.apiID = apiID;
        this.orgID = orgID;
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public String getDescription() {

        return description;
    }

    public String getApiID() {

        return apiID;
    }

    public String getOrgID() {

        return orgID;
    }

    /**
     * Scope builder.
     */
    public static class ScopeBuilder {

        private String id;
        private String name;
        private String displayName;
        private String description;
        private String apiID;
        private String orgID;

        public ScopeBuilder() {

        }

        public ScopeBuilder(String scopeId, String scopeQualifiedName, String scopeDisplayName,
                            String scopeDescription) {

            this.id = scopeId;
            this.name = scopeQualifiedName;
            this.displayName = scopeDisplayName;
            this.description = scopeDescription;
        }

        public ScopeBuilder id(String id) {

            this.id = id;
            return this;
        }

        public ScopeBuilder name(String name) {

            this.name = name;
            return this;
        }

        public ScopeBuilder displayName(String displayName) {

            this.displayName = displayName;
            return this;
        }

        public ScopeBuilder description(String description) {

            this.description = description;
            return this;
        }

        public ScopeBuilder apiID(String apiID) {

            this.apiID = apiID;
            return this;
        }

        public ScopeBuilder orgID(String orgID) {

            this.orgID = orgID;
            return this;
        }

        public Scope build() {

            return new Scope(id, name, displayName, description, apiID, orgID);
        }
    }
}
