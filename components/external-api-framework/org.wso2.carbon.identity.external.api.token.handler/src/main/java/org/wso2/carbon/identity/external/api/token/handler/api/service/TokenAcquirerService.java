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

package org.wso2.carbon.identity.external.api.token.handler.api.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.client.api.service.AbstractAPIClientManager;
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenInvocationResult;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.internal.util.TokenRequestBuilderUtils;

/**
 * Service class for acquiring tokens using different grant types.
 */
public class TokenAcquirerService extends AbstractAPIClientManager {

    private static final Log LOG = LogFactory.getLog(TokenAcquirerService.class);

    private TokenRequestContext tokenRequestContext;
    private APIInvocationConfig apiInvocationConfig = new APIInvocationConfig();

    /**
     * Constructor to initialize API client configuration.
     *
     * @param apiClientConfig API client configuration.
     */
    public TokenAcquirerService(APIClientConfig apiClientConfig) {

        super(apiClientConfig);
    }

    /**
     * Set API invocation configuration.
     *
     * @param apiInvocationConfig API invocation configuration.
     */
    public void setApiInvocationConfig(APIInvocationConfig apiInvocationConfig) {

        this.apiInvocationConfig = apiInvocationConfig;
    }

    /**
     * Set token request context.
     *
     * @param tokenRequestContext Token request context.
     */
    public void setTokenRequestContext(TokenRequestContext tokenRequestContext) {

        this.tokenRequestContext = tokenRequestContext;
    }

    /**
     * Get a new access token using the token request context.
     *
     * @return Token response.
     * @throws TokenHandlerException TokenHandlerException.
     */
    public TokenInvocationResult getNewAccessToken() throws TokenHandlerException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempting to get a new access token.");
        }

        if (tokenRequestContext == null) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_UNINITIALIZED_TOKEN_REQUEST, null);
        }

        try {
            APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContext(tokenRequestContext);
            APIResponse apiResponse = callAPI(apiRequestContext, apiInvocationConfig);
            return handleResponse(apiResponse);
        } catch (APIClientException e) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_GETTING_ACCESS_TOKEN,
                    tokenRequestContext.getGrantContext().getGrantType().name(), e);
        }
    }

    /**
     * Get a new access token using the refresh token if provided, else using the configured grant type.
     *
     * @param refreshToken Refresh token.
     * @return Token response.
     * @throws TokenHandlerException TokenHandlerException.
     */
    public TokenInvocationResult getNewAccessToken(String refreshToken) throws TokenHandlerException {

        /* Try to get a new access token using the refresh token grant if a refresh token is provided. If it fails,
         fall back to the corresponding grant type. */
        if (refreshToken != null) {
            try {
                return getNewAccessTokenFromRefreshGrant(refreshToken);
            } catch (TokenHandlerException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to retrieve access token using refresh token grant. " +
                            "Falling back to configured grant type.", e);
                }
                return getNewAccessToken();
            }
        }
        return getNewAccessToken();
    }

    /**
     * Get a new access token using the refresh token grant.
     *
     * @param refreshToken Refresh token.
     * @return Token response.
     * @throws TokenHandlerException TokenHandlerException.
     */
    public TokenInvocationResult getNewAccessTokenFromRefreshGrant(String refreshToken) throws TokenHandlerException {

        if (tokenRequestContext == null) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_UNINITIALIZED_TOKEN_REQUEST, null);
        }
        if (StringUtils.isBlank(refreshToken)) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_NULL_REFRESH_TOKEN_PROVIDED, null);
        }

        try {
            APIRequestContext apiRequestContext = TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                    tokenRequestContext, refreshToken);
            APIResponse apiResponse = callAPI(apiRequestContext, apiInvocationConfig);
            return handleResponse(apiResponse);
        } catch (APIClientException e) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_GETTING_ACCESS_TOKEN, "refresh", e);
        }
    }

    private TokenInvocationResult handleResponse(APIResponse response) throws TokenHandlerException {

        if (response == null) {
            throw new TokenHandlerException(ErrorMessage.ERROR_CODE_NULL_API_RESPONSE, null);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {
            throw new TokenHandlerException(
                    ErrorMessage.ERROR_CODE_UNEXPECTED_STATUS_CODE, String.valueOf(response.getStatusCode()));
        }

        return new TokenInvocationResult.Builder().apiResponse(response).build();
    }
}
