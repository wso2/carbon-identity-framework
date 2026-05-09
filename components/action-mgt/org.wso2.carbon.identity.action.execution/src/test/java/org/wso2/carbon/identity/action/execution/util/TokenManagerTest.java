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

package org.wso2.carbon.identity.action.execution.util;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.action.execution.internal.util.TokenManager;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenInvocationResult;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenResponse;
import org.wso2.carbon.identity.external.api.token.handler.api.service.TokenAcquirerService;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Unit tests for {@link TokenManager}, the OAuth2 client-credential token retrieval helper.
 */
public class TokenManagerTest {

    private static final String ACTION_ID = "action-id";

    private TokenAcquirerService originalTokenAcquirerService;
    private TokenAcquirerService tokenAcquirerService;
    private TokenManager tokenManager;

    @BeforeClass
    public void initIdentityConfig() throws URISyntaxException {

        URL identityXml = this.getClass().getClassLoader()
                .getResource("repository/conf/identity/identity.xml");
        if (identityXml != null) {
            File configFile = new File(identityXml.toURI());
            File carbonHome = configFile.getParentFile().getParentFile()
                    .getParentFile().getParentFile();
            if (System.getProperty("carbon.home") == null) {
                System.setProperty("carbon.home", carbonHome.getAbsolutePath());
            }
            IdentityConfigParser.getInstance(configFile.getAbsolutePath());
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {

        tokenManager = TokenManager.getInstance();
        tokenAcquirerService = mock(TokenAcquirerService.class);
        // Save the originally constructed service so other tests are unaffected.
        Field field = TokenManager.class.getDeclaredField("tokenAcquirerService");
        field.setAccessible(true);
        if (originalTokenAcquirerService == null) {
            originalTokenAcquirerService = (TokenAcquirerService) field.get(tokenManager);
        }
        field.set(tokenManager, tokenAcquirerService);

        // Stub the secret manager so persistence inside getNewAccessToken does not NPE.
        SecretManager secretManager = mock(SecretManager.class);
        SecretResolveManager secretResolveManager = mock(SecretResolveManager.class);
        SecretType secretType = mock(SecretType.class);
        when(secretType.getId()).thenReturn("secret-type-id");
        when(secretManager.getSecretType(any())).thenReturn(secretType);
        when(secretManager.isSecretExist(any(), any())).thenReturn(false);
        ActionExecutionServiceComponentHolder.getInstance().setSecretManager(secretManager);
        ActionExecutionServiceComponentHolder.getInstance().setSecretResolveManager(secretResolveManager);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        // Restore the original tokenAcquirerService to avoid polluting subsequent tests.
        Field field = TokenManager.class.getDeclaredField("tokenAcquirerService");
        field.setAccessible(true);
        field.set(tokenManager, originalTokenAcquirerService);

        // Clear the holder state so other test classes are not affected.
        ActionExecutionServiceComponentHolder.getInstance().setSecretManager(null);
        ActionExecutionServiceComponentHolder.getInstance().setSecretResolveManager(null);
    }

    @Test
    public void testGetNewAccessTokenReturnsAccessTokenOnSuccess() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.CLIENT_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                clientCredentialProperties("client-id", "client-secret", "https://token.endpoint", "openid email"));

        TokenInvocationResult successResult = mock(TokenInvocationResult.class);
        when(successResult.getStatus()).thenReturn(TokenInvocationResult.Status.SUCCESS);
        TokenResponse tokenResponse = mock(TokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("new-access-token");
        when(tokenResponse.getRefreshToken()).thenReturn("new-refresh-token");
        when(successResult.getTokenResponse()).thenReturn(tokenResponse);

        when(tokenAcquirerService.getNewAccessToken("existing-refresh-token")).thenReturn(successResult);

        String result = tokenManager.getNewAccessToken(ACTION_ID, authentication, "existing-refresh-token");

        assertEquals(result, "new-access-token");

        // The token request context must carry the supplied client credentials and endpoint.
        ArgumentCaptor<TokenRequestContext> contextCaptor = ArgumentCaptor.forClass(TokenRequestContext.class);
        verify(tokenAcquirerService).setTokenRequestContext(contextCaptor.capture());
        TokenRequestContext capturedContext = contextCaptor.getValue();
        assertEquals(capturedContext.getTokenEndpointUrl(), "https://token.endpoint");
    }

    @Test
    public void testGetNewAccessTokenWithoutRefreshTokenPassesNullToService() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.CLIENT_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                clientCredentialProperties("client-id", "client-secret", "https://token.endpoint", null));

        TokenInvocationResult successResult = mock(TokenInvocationResult.class);
        when(successResult.getStatus()).thenReturn(TokenInvocationResult.Status.SUCCESS);
        TokenResponse tokenResponse = mock(TokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("a");
        when(tokenResponse.getRefreshToken()).thenReturn("r");
        when(successResult.getTokenResponse()).thenReturn(tokenResponse);

        when(tokenAcquirerService.getNewAccessToken(any())).thenReturn(successResult);

        // No refresh token — service should be called with null.
        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);

        verify(tokenAcquirerService).getNewAccessToken((String) null);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving access token for actions.")
    public void testGetNewAccessTokenWrapsTokenHandlerException() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.CLIENT_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                clientCredentialProperties("c", "s", "http://t", null));

        when(tokenAcquirerService.getNewAccessToken(any()))
                .thenThrow(new TokenHandlerException(
                        ErrorMessage.ERROR_CODE_GETTING_ACCESS_TOKEN, "client_credentials"));

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving access token for actions.")
    public void testGetNewAccessTokenThrowsWhenStatusIsNotSuccess() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.CLIENT_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                clientCredentialProperties("c", "s", "http://t", "scope"));

        TokenInvocationResult result = mock(TokenInvocationResult.class);
        when(result.getStatus()).thenReturn(TokenInvocationResult.Status.ERROR);
        when(tokenAcquirerService.getNewAccessToken(any())).thenReturn(result);

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Unsupported authentication type for access token request.*")
    public void testGetNewAccessTokenRejectsNonClientCredentialAuthentication() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.BASIC);

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);
    }

    @Test
    public void testGetInstanceReturnsSingleton() {

        assertEquals(TokenManager.getInstance(), TokenManager.getInstance());
    }

    @Test
    public void testGetNewAccessTokenForPasswordCredentialReturnsAccessTokenOnSuccess() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.PASSWORD_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                passwordCredentialProperties("client-id", "client-secret", "https://token.endpoint",
                        "openid email", "user1", "pass1"));

        TokenInvocationResult successResult = mock(TokenInvocationResult.class);
        when(successResult.getStatus()).thenReturn(TokenInvocationResult.Status.SUCCESS);
        TokenResponse tokenResponse = mock(TokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("new-access-token");
        when(tokenResponse.getRefreshToken()).thenReturn("new-refresh-token");
        when(successResult.getTokenResponse()).thenReturn(tokenResponse);

        when(tokenAcquirerService.getNewAccessToken("existing-refresh-token")).thenReturn(successResult);

        String result = tokenManager.getNewAccessToken(ACTION_ID, authentication, "existing-refresh-token");

        assertEquals(result, "new-access-token");

        ArgumentCaptor<TokenRequestContext> contextCaptor = ArgumentCaptor.forClass(TokenRequestContext.class);
        verify(tokenAcquirerService).setTokenRequestContext(contextCaptor.capture());
        TokenRequestContext capturedContext = contextCaptor.getValue();
        assertEquals(capturedContext.getTokenEndpointUrl(), "https://token.endpoint");
    }

    @Test
    public void testGetNewAccessTokenForPasswordCredentialWithoutRefreshTokenPassesNullToService() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.PASSWORD_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                passwordCredentialProperties("client-id", "client-secret", "https://token.endpoint",
                        null, "user1", "pass1"));

        TokenInvocationResult successResult = mock(TokenInvocationResult.class);
        when(successResult.getStatus()).thenReturn(TokenInvocationResult.Status.SUCCESS);
        TokenResponse tokenResponse = mock(TokenResponse.class);
        when(tokenResponse.getAccessToken()).thenReturn("a");
        when(tokenResponse.getRefreshToken()).thenReturn("r");
        when(successResult.getTokenResponse()).thenReturn(tokenResponse);

        when(tokenAcquirerService.getNewAccessToken(any())).thenReturn(successResult);

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);

        verify(tokenAcquirerService).getNewAccessToken((String) null);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving access token for actions.")
    public void testGetNewAccessTokenForPasswordCredentialWrapsTokenHandlerException() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.PASSWORD_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                passwordCredentialProperties("c", "s", "http://t", null, "u", "p"));

        when(tokenAcquirerService.getNewAccessToken(any()))
                .thenThrow(new TokenHandlerException(
                        ErrorMessage.ERROR_CODE_GETTING_ACCESS_TOKEN, "password"));

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);
    }

    @Test(expectedExceptions = ActionExecutionException.class,
            expectedExceptionsMessageRegExp = "Error occurred while retrieving access token for actions.")
    public void testGetNewAccessTokenForPasswordCredentialThrowsWhenStatusIsNotSuccess() throws Exception {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getType()).thenReturn(Authentication.Type.PASSWORD_CREDENTIAL);
        when(authentication.getPropertiesWithDecryptedValues(ACTION_ID)).thenReturn(
                passwordCredentialProperties("c", "s", "http://t", "scope", "u", "p"));

        TokenInvocationResult result = mock(TokenInvocationResult.class);
        when(result.getStatus()).thenReturn(TokenInvocationResult.Status.ERROR);
        when(tokenAcquirerService.getNewAccessToken(any())).thenReturn(result);

        tokenManager.getNewAccessToken(ACTION_ID, authentication, null);
    }

    private List<AuthProperty> clientCredentialProperties(String clientId, String clientSecret,
                                                          String tokenEndpoint, String scopes) {

        return Arrays.asList(
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.CLIENT_ID.getName())
                        .value(clientId).isConfidential(true).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.CLIENT_SECRET.getName())
                        .value(clientSecret).isConfidential(true).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.TOKEN_ENDPOINT.getName())
                        .value(tokenEndpoint).isConfidential(false).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.SCOPES.getName())
                        .value(scopes).isConfidential(false).build()
        );
    }

    private List<AuthProperty> passwordCredentialProperties(String clientId, String clientSecret,
                                                            String tokenEndpoint, String scopes,
                                                            String username, String password) {

        return Arrays.asList(
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.CLIENT_ID.getName())
                        .value(clientId).isConfidential(true).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.CLIENT_SECRET.getName())
                        .value(clientSecret).isConfidential(true).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.TOKEN_ENDPOINT.getName())
                        .value(tokenEndpoint).isConfidential(false).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.SCOPES.getName())
                        .value(scopes).isConfidential(false).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.USERNAME.getName())
                        .value(username).isConfidential(true).build(),
                new AuthProperty.AuthPropertyBuilder()
                        .name(Authentication.Property.PASSWORD.getName())
                        .value(password).isConfidential(true).build()
        );
    }
}
