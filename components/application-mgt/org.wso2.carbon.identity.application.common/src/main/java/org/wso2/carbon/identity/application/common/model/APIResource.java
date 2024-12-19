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

import java.util.List;

/**
 * API resource.
 */
public class APIResource {

    private String id;
    private Integer cursorKey;
    private String name;
    private String type;
    private String identifier;
    private String description;
    private Integer tenantId;
    private boolean requiresAuthorization;
    private List<Scope> scopes;
    private List<ApplicationBasicInfo> subscribedApplications;
    private List<APIResourceProperty> properties;

    public APIResource() {
    }

    public APIResource(APIResourceBuilder apiResourceBuilder) {

        this.id = apiResourceBuilder.id;
        this.cursorKey = apiResourceBuilder.cursorKey;
        this.name = apiResourceBuilder.name;
        this.type = apiResourceBuilder.type;
        this.identifier = apiResourceBuilder.identifier;
        this.description = apiResourceBuilder.description;
        this.tenantId = apiResourceBuilder.tenantId;
        this.requiresAuthorization = apiResourceBuilder.requiresAuthorization;
        this.scopes = apiResourceBuilder.scopes;
        this.subscribedApplications = apiResourceBuilder.subscribedApplications;
        this.properties = apiResourceBuilder.properties;
    }

    public String getId() {

        return id;
    }

    public Integer getCursorKey() {

        return cursorKey;
    }

    public String getName() {

        return name;
    }

    public String getType() {

        return type;
    }

    public String getIdentifier() {

        return identifier;
    }

    public String getDescription() {

        return description;
    }

    public Integer getTenantId() {

        return tenantId;
    }

    public boolean isAuthorizationRequired() {

        return requiresAuthorization;
    }

    public List<Scope> getScopes() {

        return scopes;
    }

    public void setScopes(List<Scope> scopes) {

        this.scopes = scopes;
    }

    public List<ApplicationBasicInfo> getSubscribedApplications() {

        return subscribedApplications;
    }

    public void setSubscribedApplications(List<ApplicationBasicInfo> subscribedApplications) {

        this.subscribedApplications = subscribedApplications;
    }

    public List<APIResourceProperty> getProperties() {

        return properties;
    }

    public void setProperties(List<APIResourceProperty> properties) {

        this.properties = properties;
    }

    /**
     * API resource builder.
     */
    public static class APIResourceBuilder {

        private String id;
        private Integer cursorKey;
        private String name;
        private String type;
        private String identifier;
        private String description;
        private Integer tenantId;
        private boolean requiresAuthorization;
        private List<Scope> scopes;
        private List<ApplicationBasicInfo> subscribedApplications;
        private List<APIResourceProperty> properties;

        public APIResourceBuilder() {
        }

        public APIResourceBuilder name(String name) {

            this.name = name;
            return this;
        }

        public APIResourceBuilder identifier(String identifier) {

            this.identifier = identifier;
            return this;
        }

        public APIResourceBuilder id(String id) {

            this.id = id;
            return this;
        }

        public APIResourceBuilder cursorKey(Integer cursorKey) {

            this.cursorKey = cursorKey;
            return this;
        }

        public APIResourceBuilder type(String type) {

            this.type = type;
            return this;
        }

        public APIResourceBuilder description(String description) {

            this.description = description;
            return this;
        }

        public APIResourceBuilder tenantId(Integer tenantId) {

            this.tenantId = tenantId;
            return this;
        }

        public APIResourceBuilder requiresAuthorization(boolean requiresAuthorization) {

            this.requiresAuthorization = requiresAuthorization;
            return this;
        }

        public APIResourceBuilder scopes(List<Scope> scopes) {

            this.scopes = scopes;
            return this;
        }

        public APIResourceBuilder subscribedApplications(List<ApplicationBasicInfo> subscribedApplications) {

            this.subscribedApplications = subscribedApplications;
            return this;
        }

        public APIResourceBuilder properties(List<APIResourceProperty> properties) {

            this.properties = properties;
            return this;
        }

        public APIResource build() {

            return new APIResource(this);
        }
    }
}
