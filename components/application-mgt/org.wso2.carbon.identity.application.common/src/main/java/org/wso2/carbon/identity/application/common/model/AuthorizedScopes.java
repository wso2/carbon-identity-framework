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
 * Authorized Scopes model class.
 */
public class AuthorizedScopes {

    private String policyId;
    private List<String> scopes;

    public AuthorizedScopes(String policyId, List<String> scopes) {

        this.policyId = policyId;
        this.scopes = scopes;
    }

    public AuthorizedScopes() {

    }

    public String getPolicyId() {

        return policyId;
    }

    public List<String> getScopes() {

        return scopes;
    }

    public void setScopes(List<String> scopes) {

        this.scopes = scopes;
    }

    /**
     * Builder class for {@link AuthorizedScopes}.
     */
    public static class AuthorizedScopesBuilder {

        private String policyId;
        private List<String> scopes;

        public AuthorizedScopesBuilder policyId(String policyId) {

            this.policyId = policyId;
            return this;
        }

        public AuthorizedScopesBuilder scopes(List<String> scopes) {

            this.scopes = scopes;
            return this;
        }

        public AuthorizedScopes build() {

            return new AuthorizedScopes(policyId, scopes);
        }
    }
}
