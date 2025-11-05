/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.client;

/**
 * Simple holder for OAuth2 token response values and error details.
 */
public class TokenResponse {

    private final String accessToken;
    private final String idToken;
    private final String refreshToken;
    private final String tokenType;
    private final String errorCode;
    private final String errorDescription;
    private final String errorDetails;

    /**
     * Successful token response.
     */
    public TokenResponse(String accessToken, String idToken, String refreshToken, String tokenType) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.errorCode = null;
        this.errorDescription = null;
        this.errorDetails = null;
    }

    /**
     * Error token response with error details.
     */
    public TokenResponse(String errorCode, String errorDescription, String errorDetails) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.errorDetails = errorDetails;
        this.accessToken = null;
        this.idToken = null;
        this.refreshToken = null;
        this.tokenType = null;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns true if this response contains an error.
     */
    public boolean hasError() {
        return errorCode != null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getErrorDetails() {
        return errorDetails;
    }
}
