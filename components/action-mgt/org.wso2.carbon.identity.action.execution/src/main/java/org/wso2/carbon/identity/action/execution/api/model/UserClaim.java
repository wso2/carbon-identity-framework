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

package org.wso2.carbon.identity.action.execution.api.model;

/**
 * This class models the user claims.
 * User claim is the entity that represents the user claims of the user for whom the action is triggered for.
 */
public class UserClaim {

    private final String uri;
    private final String value;

    public UserClaim(String uri, String value) {
        this.uri = uri;
        this.value = value;
    }

    /**
     * Retrieve value of the user claim.
     *
     * @return Claim value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Retrieve URI of the user claim.
     *
     * @return Claim URI.
     */
    public String getUri() {
        return uri;
    }
}
