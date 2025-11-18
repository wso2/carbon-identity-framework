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

package org.wso2.carbon.identity.external.api.token.handler.api.model;

import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

/**
 * Model class for Token Response.
 */
public class TokenResponse extends APIResponse {

    private String accessToken = null;
    private String refreshToken = null;

    public TokenResponse(APIResponse response, String accessToken, String refreshToken) {

        super(response.getStatusCode(), response.getResponseBody());
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * Get Access Token.
     *
     * @return Access Token.
     */
    public String getAccessToken() {

        return accessToken;
    }

    /**
     * Get Refresh Token.
     *
     * @return Refresh Token.
     */
    public String getRefreshToken() {

        return refreshToken;
    }
}
