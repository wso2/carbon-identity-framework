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

package org.wso2.carbon.identity.application.mgt.util;

/**
 * A class to hold the scope authorization information.
 */
public class ScopeAuthorizationInfo {

    private final String scopeId;
    private final String apiId;
    private final String scopeName;

    /**
     * Constructor to initialize the scope authorization information.
     *
     * @param scopeId   Scope id.
     * @param apiId     API id.
     * @param scopeName Scope name.
     */
    public ScopeAuthorizationInfo(String scopeId, String apiId, String scopeName) {
        this.scopeId = scopeId;
        this.apiId = apiId;
        this.scopeName = scopeName;
    }

    /**
     * Get the scope id.
     * @return scope id.
     */
    public String getScopeId() {

        return scopeId;
    }

    /**
     * Get the api id.
     * @return api id.
     */
    public String getApiId() {

        return apiId;
    }

    /**
     * Get the scope name.
     * @return scope name.
     */
    public String getScopeName() {

        return scopeName;
    }
}
