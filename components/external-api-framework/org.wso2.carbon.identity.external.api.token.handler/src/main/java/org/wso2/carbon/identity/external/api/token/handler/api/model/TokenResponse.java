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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

/**
 * Model class for Token Response.
 */
public class TokenResponse extends APIResponse {

    private static final Log LOG = LogFactory.getLog(TokenResponse.class);

    private final String accessToken;
    private final String refreshToken;

    private TokenResponse(Builder builder) {

        super(new APIResponse.Builder(builder.statusCode, builder.responseBody));
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;

        if (LOG.isDebugEnabled()) {
            LOG.debug("TokenResponse created with status code: " + builder.statusCode);
        }
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

    /**
     * Builder class for TokenResponse.
     */
    public static class Builder {

        private int statusCode;
        private String responseBody;
        private String accessToken;
        private String refreshToken;
        private final APIResponse response;

        public Builder(APIResponse response) {

            this.response = response;
        }

        /**
         * Build TokenResponse from APIResponse.
         *
         * @return TokenResponse.
         */
        public TokenResponse build() {

            statusCode = response.getStatusCode();
            responseBody = response.getResponseBody();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building TokenResponse with status code: " + statusCode);
            }
            if (responseBody != null) {
                try {
                    JsonElement element = JsonParser.parseString(responseBody);
                    if (element.isJsonObject()) {
                        JsonObject jsonObject = element.getAsJsonObject();
                        accessToken = extractToken(jsonObject, "access_token");
                        refreshToken = extractToken(jsonObject, "refresh_token");
                    } else {
                        LOG.warn("Response body is not a valid JSON object. Tokens will remain unset.");
                    }
                } catch (JsonSyntaxException e) {
                    // Leave tokens unset when the payload is not valid JSON.
                    LOG.warn("Failed to parse response body as JSON. Tokens will remain unset.", e);
                }
            } else {
                LOG.warn("Response body is null. Cannot extract tokens.");
            }
            return new TokenResponse(this);
        }

        private String extractToken(JsonObject jsonObject, String tokenName) {

            String token = null;
            if (jsonObject.has(tokenName) && !jsonObject.get(tokenName).isJsonNull()) {
                token = jsonObject.get(tokenName).getAsString();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("The %s extracted successfully from response.", tokenName));
                }
            } else {
                LOG.warn(String.format("The %s not found in the token response.", tokenName));
            }
            return token;
        }
    }
}
