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
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.model.OperationResult.OperationType;

/**
 * Model class for Token Invocation Result.
 */
public class TokenInvocationResult {

    private static final Log LOG = LogFactory.getLog(TokenInvocationResult.class);

    private final Status status;
    private final TokenResponse tokenResponse;
    private final OperationResult accessTokenParsingOperationResult;
    private final OperationResult refreshTokenParsingOperationResult;

    private TokenInvocationResult(Builder builder) {

        this.status = builder.status;
        this.tokenResponse = builder.tokenResponse;
        this.accessTokenParsingOperationResult = builder.accessTokenParsingResult;
        this.refreshTokenParsingOperationResult = builder.refreshTokenParsingResult;
    }

    public Status getStatus() {

        return status;
    }

    public TokenResponse getTokenResponse() {

        return tokenResponse;
    }

    public OperationResult getAccessTokenParsingResult() {

        return accessTokenParsingOperationResult;
    }

    public OperationResult getRefreshTokenParsingResult() {

        return refreshTokenParsingOperationResult;
    }

    /**
     * Builder class for TokenInvocationResult.
     */
    public static class Builder {

        private APIResponse apiResponse;
        private Status status;
        private TokenResponse tokenResponse;
        private OperationResult accessTokenParsingResult;
        private OperationResult refreshTokenParsingResult;

        public Builder apiResponse(APIResponse response) {

            this.apiResponse = response;
            return this;
        }

        /**
         * Build TokenInvocationResult.
         *
         * @return TokenInvocationResult instance.
         */
        public TokenInvocationResult build() {

            String responseBody =  apiResponse.getResponseBody();
            String accessToken = null;
            String refreshToken = null;

            if (responseBody != null) {
                try {
                    JsonElement element = JsonParser.parseString(responseBody);
                    if (element.isJsonObject()) {
                        JsonObject jsonObject = element.getAsJsonObject();

                        String accessTokenName = OperationType.ACCESS_TOKEN_PARSING.getTokenName();
                        if (jsonObject.has(accessTokenName) && !jsonObject.get(accessTokenName).isJsonNull()) {
                            accessToken = jsonObject.get(accessTokenName).getAsString();
                            accessTokenParsingResult = OperationResult.success(OperationType.ACCESS_TOKEN_PARSING);
                        } else {
                            accessTokenParsingResult = OperationResult.failure(OperationType.ACCESS_TOKEN_PARSING,
                                    ErrorMessage.ERROR_CODE_TOKEN_NOT_FOUND, accessTokenName, null);
                        }

                        String refreshTokenName = OperationType.REFRESH_TOKEN_PARSING.getTokenName();
                        if (jsonObject.has(refreshTokenName) && !jsonObject.get(refreshTokenName).isJsonNull()) {
                            refreshToken = jsonObject.get(refreshTokenName).getAsString();
                            refreshTokenParsingResult = OperationResult.success(OperationType.REFRESH_TOKEN_PARSING);
                        } else {
                            refreshTokenParsingResult = OperationResult.failure(OperationType.REFRESH_TOKEN_PARSING,
                                    ErrorMessage.ERROR_CODE_TOKEN_NOT_FOUND, refreshTokenName, null);
                        }
                    } else {
                        LOG.debug("Response body contains invalid JSON object. " +
                                "Tokens cannot be extracted and will remain unset.");
                        buildOperationFailureResult(ErrorMessage.ERROR_CODE_INVALID_JSON_OBJECT, null);
                    }
                } catch (JsonSyntaxException e) {
                    LOG.debug("Failed to parse response body as JSON. " +
                                    "Tokens cannot be extracted and will remain unset.", e);
                    buildOperationFailureResult(ErrorMessage.ERROR_CODE_INVALID_TOKEN_RESPONSE, e);
                }
            } else {
                LOG.debug("Response body is null. Tokens cannot be extracted and will remain unset.");
                buildOperationFailureResult(ErrorMessage.ERROR_CODE_NULL_TOKEN_RESPONSE, null);
            }
            tokenResponse = new TokenResponse(apiResponse, accessToken, refreshToken);
            buildStatus();

            return new TokenInvocationResult(this);
        }

        private void buildStatus() {

            if (status != null) {
                return;
            }

            /* As per the OAuth 2.0 specification, the refresh token is option in the token response, only checking
            access token is properly set. */
            if (tokenResponse.getAccessToken() != null) {
                status = Status.SUCCESS;
                return;
            }
            status = Status.INCOMPLETE;
        }

        private void buildOperationFailureResult(ErrorMessage errorMessage, Throwable e) {

            status = Status.ERROR;
            accessTokenParsingResult = OperationResult.failure(OperationType.ACCESS_TOKEN_PARSING,
                    errorMessage, OperationType.ACCESS_TOKEN_PARSING.getTokenName(), e);
            refreshTokenParsingResult = OperationResult.failure(OperationType.REFRESH_TOKEN_PARSING,
                    errorMessage, OperationType.REFRESH_TOKEN_PARSING.getTokenName(), e);
        }
    }

    /**
     * Enum for Operation Types.
     */
    public enum Status {

        SUCCESS,
        ERROR,
        INCOMPLETE
    }
}
