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
 * Authorized API model class.
 */
public class AuthorizedAPI {

    private String appId;
    private String apiId;
    private String apiIdentifier;
    private String apiName;
    private String policyId;
    private List<Scope> scopes;
    private String type;

    public AuthorizedAPI(String appId, String apiId, String policyId, List<Scope> scopes, String type) {

        this.appId = appId;
        this.apiId = apiId;
        this.policyId = policyId;
        this.scopes = scopes;
        this.type = type;
    }

    public AuthorizedAPI() {

    }

    public String getAppId() {

        return appId;
    }

    public void setAppId(String appId) {

        this.appId = appId;
    }

    public String getAPIId() {

        return apiId;
    }

    public void setAPIId(String apiId) {

        this.apiId = apiId;
    }

    public String getAPIIdentifier() {

        return apiIdentifier;
    }

    public void setAPIIdentifier(String apiIdentifier) {

        this.apiIdentifier = apiIdentifier;
    }

    public String getAPIName() {

        return apiName;
    }

    public void setAPIName(String apiName) {
        
        this.apiName = apiName;
    }

    public String getPolicyId() {

        return policyId;
    }

    public void setPolicyId(String policyId) {

        this.policyId = policyId;
    }

    public List<Scope> getScopes() {

        return scopes;
    }

    public void setScopes(List<Scope> scopes) {

        this.scopes = scopes;
    }

    public void addScope(Scope scope) {

        this.scopes.add(scope);
    }

    public void setType(String type) {

        this.type = type;
    }

    public String getType() {

        return type;
    }

    /**
     * Builder class for {@link AuthorizedAPI}.
     */
    public static class AuthorizedAPIBuilder {

        private String appId;
        private String apiId;
        private String policyId;
        private List<Scope> scopes;
        private String type;

        public AuthorizedAPIBuilder() {

        }

        public AuthorizedAPIBuilder appId(String appId) {

            this.appId = appId;
            return this;
        }

        public AuthorizedAPIBuilder apiId(String apiId) {

            this.apiId = apiId;
            return this;
        }

        public AuthorizedAPIBuilder policyId(String policyId) {

            this.policyId = policyId;
            return this;
        }

        public AuthorizedAPIBuilder scopes(List<Scope> scopes) {

            this.scopes = scopes;
            return this;
        }

        public AuthorizedAPIBuilder type(String type) {

            this.type = type;
            return this;
        }

        public AuthorizedAPI build() {

            return new AuthorizedAPI(appId, apiId, policyId, scopes, type);
        }
    }
}
