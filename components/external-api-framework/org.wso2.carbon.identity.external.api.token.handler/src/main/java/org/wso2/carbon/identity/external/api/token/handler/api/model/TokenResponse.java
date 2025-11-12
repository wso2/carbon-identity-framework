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
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

/**
 * Model class for Token Response.
 */
public class TokenResponse extends APIResponse {

    private final String accessToken;
    private final String refreshToken;

    private TokenResponse(Builder builder) {

        super(new APIResponse.Builder(builder.statusCode, builder.responseBody));
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
    }

    public String getAccessToken() {

        return accessToken;
    }

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

        public TokenResponse build() {

            statusCode = response.getStatusCode();
            responseBody = response.getResponseBody();
            if (responseBody != null) {
                try {
                    JsonElement element = JsonParser.parseString(responseBody);
                    if (element.isJsonObject()) {
                        JsonObject jsonObject = element.getAsJsonObject();
                        if (jsonObject.has("access_token")
                                && !jsonObject.get("access_token").isJsonNull()) {
                            accessToken = jsonObject.get("access_token").getAsString();
                        }
                        if (jsonObject.has("refresh_token")
                                && !jsonObject.get("refresh_token").isJsonNull()) {
                            refreshToken = jsonObject.get("refresh_token").getAsString();
                        }
                    }
                } catch (JsonSyntaxException ignore) {
                    // Leave tokens unset when the payload is not valid JSON.
                }
            }
            return new TokenResponse(this);
        }
    }
}
