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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientConfigException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenInvocationResult;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.service.TokenAcquirerService;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * Manager class for handling Client Credentials Token acquisition and management for actions.
 */
public class TokenManager {

    private static final Log LOG = LogFactory.getLog(TokenManager.class);
    private static final TokenManager tokenManager = new TokenManager();

    private final TokenAcquirerService tokenAcquirerService;

    private TokenManager() {

        APIClientConfig apiClientConfig = null;
        try {
            apiClientConfig = buildAPIClientConfig();
        } catch (APIClientConfigException e) {
            LOG.error("Error initializing TokenManager due to API client configuration error.", e);
        }
        this.tokenAcquirerService = new TokenAcquirerService(apiClientConfig);
    }

    public static TokenManager getInstance() {

        return tokenManager;
    }

    /**
     * Retrieve a new access token for the given action and persist the new access and refresh tokens
     * to the secret store.
     *
     * @param actionId       The action identifier the tokens belong to.
     * @param authentication The authentication configuration of the action endpoint.
     * @param refreshToken   The current refresh token, or {@code null} when none is available.
     * @return The newly issued access token.
     * @throws ActionExecutionException If an error occurs while retrieving the access token.
     * @throws ActionMgtException       If an error occurs while decrypting the authentication properties.
     */
    public String getNewAccessToken(String actionId, Authentication authentication, String refreshToken)
            throws ActionExecutionException, ActionMgtException {

        if (authentication.getType() != Authentication.Type.CLIENT_CREDENTIAL) {
            throw new ActionExecutionException("Unsupported authentication type for access token request, only " +
                    "supported for client credential type.");
        }

        List<AuthProperty> authProperties = authentication.getPropertiesWithDecryptedValues(actionId);
        TokenRequestContext tokenRequestContext = buildTokenRequestContext(authProperties);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);

        TokenInvocationResult tokenInvocationResult;
        try {
            tokenInvocationResult = tokenAcquirerService.getNewAccessToken(refreshToken);
        } catch (TokenHandlerException e) {
            throw new ActionExecutionException("Error occurred while retrieving access token for actions.", e);
        }

        if (tokenInvocationResult.getStatus() != TokenInvocationResult.Status.SUCCESS) {
            throw new ActionExecutionException("Error occurred while retrieving access token for actions.");
        }

        String newAccessToken = tokenInvocationResult.getTokenResponse().getAccessToken();
        String newRefreshToken = tokenInvocationResult.getTokenResponse().getRefreshToken();
        if (newAccessToken == null) {
            throw new ActionExecutionException(
                    "Token response did not contain an access token for action: " + actionId);
        }

        persistInternalTokens(actionId, authentication, refreshToken, newAccessToken, newRefreshToken);

        return newAccessToken;
    }

    private void persistInternalTokens(String actionId, Authentication authentication, String oldRefreshToken,
                                       String newAccessToken, String newRefreshToken) {

        ActionSecretProcessor secretProcessor = new ActionSecretProcessor();
        String authType = authentication.getType().name();

        persistTokenIfChanged(secretProcessor, actionId, authType,
                Authentication.Property.INTERNAL_ACCESS_TOKEN.getName(), newAccessToken, null);
        persistTokenIfChanged(secretProcessor, actionId, authType,
                Authentication.Property.INTERNAL_REFRESH_TOKEN.getName(), newRefreshToken, oldRefreshToken);
    }

    private void persistTokenIfChanged(ActionSecretProcessor secretProcessor, String actionId, String authType,
                                       String propertyName, String newValue, String oldValue) {

        if (StringUtils.isBlank(newValue)) {
            return;
        }
        if (oldValue != null && newValue.equals(oldValue)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping persist for unchanged internal token property: " + propertyName
                        + " for action: " + actionId + ".");
            }
            return;
        }
        AuthProperty newProp = new AuthProperty.AuthPropertyBuilder()
                .name(propertyName)
                .value(newValue)
                .isConfidential(true)
                .build();
        try {
            secretProcessor.encryptProperty(newProp, authType, actionId);
        } catch (SecretManagementException e) {
            LOG.error("Error while persisting internal token for action: " + actionId, e);
        }
    }

    private TokenRequestContext buildTokenRequestContext(List<AuthProperty> decryptedProperties)
            throws ActionExecutionException {

        try {
            Map<String, String> grantTypeProperties = new HashMap<>();
            grantTypeProperties.put(
                    GrantContext.Property.CLIENT_ID.getName(),
                    getPropertyValue(decryptedProperties, Authentication.Property.CLIENT_ID));
            grantTypeProperties.put(
                    GrantContext.Property.CLIENT_SECRET.getName(),
                    getPropertyValue(decryptedProperties, Authentication.Property.CLIENT_SECRET));
            String scopes = getPropertyValue(decryptedProperties, Authentication.Property.SCOPES);
            if (StringUtils.isNotBlank(scopes)) {
                grantTypeProperties.put(GrantContext.Property.SCOPE.getName(), scopes);
            }

            GrantContext grantContext = new GrantContext.Builder()
                    .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                    .properties(grantTypeProperties)
                    .build();

            TokenRequestContext.Builder builder = new TokenRequestContext.Builder()
                    .grantContext(grantContext)
                    .endpointUrl(getPropertyValue(decryptedProperties, Authentication.Property.TOKEN_ENDPOINT));

            return builder.build();
        } catch (TokenHandlerException e) {
            throw new ActionExecutionException(
                    "Error while building token request context for Client Credential grant.", e);
        }
    }

    private String getPropertyValue(List<AuthProperty> properties, Authentication.Property property) {

        return properties.stream()
                .filter(p -> property.getName().equals(p.getName()))
                .findFirst()
                .map(AuthProperty::getValue)
                .orElse(null);
    }

    private APIClientConfig buildAPIClientConfig() throws APIClientConfigException {

        APIClientConfig.Builder builder = new APIClientConfig.Builder();

        // Initialize the http client. Set connection time out to 2s and read time out to 5s.
        int readTimeout = ActionExecutorConfig.getInstance().getHttpReadTimeoutInMillis();
        int connectionRequestTimeout = ActionExecutorConfig.getInstance().getHttpConnectionRequestTimeoutInMillis();
        int connectionTimeout = ActionExecutorConfig.getInstance().getHttpConnectionTimeoutInMillis();
        int poolSizeToBeSet = ActionExecutorConfig.getInstance().getHttpConnectionPoolSize();

        applyIfPositive(builder::httpConnectionTimeoutInMillis, connectionTimeout);
        applyIfPositive(builder::httpReadTimeoutInMillis, readTimeout);
        applyIfPositive(builder::httpConnectionRequestTimeoutInMillis, connectionRequestTimeout);
        applyIfPositive(builder::poolSizeToBeSet, poolSizeToBeSet);

        return builder.build();
    }

    private void applyIfPositive(IntConsumer setter, int propertyValue) {

        if (propertyValue > 0) {
            setter.accept(propertyValue);
        }
    }
}
