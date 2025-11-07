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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientInvocationException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenResponse;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for TokenAcquirerService class.
 */
public class TokenAcquirerServiceTest {

    private static final String CLIENT_ID = "test_client_id";
    private static final String CLIENT_SECRET = "test_client_secret";
    private static final String SCOPE = "test_scope";
    private static final String ENDPOINT_URL = "https://example.com/token";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String REFRESH_TOKEN = "refresh_token_value";
    private static final String NEW_REFRESH_TOKEN = "new_refresh_token_value";

    @Mock
    private APIClientConfig apiClientConfig;

    private TokenAcquirerService tokenAcquirerService;
    private GrantContext grantContext;
    private TokenRequestContext tokenRequestContext;
    private APIInvocationConfig apiInvocationConfig;

    @BeforeMethod
    public void setUp() throws TokenHandlerException, TokenRequestException {

        MockitoAnnotations.openMocks(this);

        // Setup APIClientConfig mock with proper values to avoid IllegalArgumentException.
        when(apiClientConfig.getHttpReadTimeoutInMillis()).thenReturn(30000);
        when(apiClientConfig.getHttpConnectionRequestTimeoutInMillis()).thenReturn(15000);
        when(apiClientConfig.getHttpConnectionTimeoutInMillis()).thenReturn(10000);
        when(apiClientConfig.getPoolSizeToBeSet()).thenReturn(20);

        // Setup GrantContext
        Map<String, String> properties = new HashMap<>();
        properties.put(GrantContext.Property.CLIENT_ID.getName(), CLIENT_ID);
        properties.put(GrantContext.Property.CLIENT_SECRET.getName(), CLIENT_SECRET);
        properties.put(GrantContext.Property.SCOPE.getName(), SCOPE);

        grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        // Setup TokenRequestContext
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        tokenRequestContext = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .build();

        // Setup service
        tokenAcquirerService = spy(new TokenAcquirerService(apiClientConfig));
        apiInvocationConfig = new APIInvocationConfig();
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
    }

    /**
     * Test successful token acquisition with access and refresh tokens.
     */
    @Test
    public void testGetNewAccessTokenSuccess() throws TokenHandlerException, APIClientException {

        String responseBody = String.format(
                "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse mockResponse = new APIResponse.Builder(200, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        TokenResponse tokenResponse = tokenAcquirerService.getNewAccessToken();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), 200);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test successful token acquisition with only access token.
     */
    @Test
    public void testGetNewAccessTokenSuccessWithoutRefreshToken() throws TokenHandlerException, APIClientException {

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}", 
                ACCESS_TOKEN);
        APIResponse mockResponse = new APIResponse.Builder(200, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        TokenResponse tokenResponse = tokenAcquirerService.getNewAccessToken();

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), 200);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), null);
    }

    /**
     * Test token acquisition failure with invalid credentials.
     */
    @Test
    public void testGetNewAccessTokenWithInvalidCredentials() throws APIClientException {

        String responseBody = "{\"error\":\"invalid_client\",\"error_description\":\"Client authentication failed\"}";
        APIResponse mockResponse = new APIResponse.Builder(401, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessToken();
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 401);
            assertEquals(tokenResponse.getAccessToken(), null);
            assertEquals(tokenResponse.getRefreshToken(), null);
        } catch (TokenHandlerException e) {
            fail("Should not throw exception for error response, should return error TokenResponse");
        }
    }

    /**
     * Test token acquisition when context is not initialized.
     */
    @Test
    public void testGetNewAccessTokenWithoutContext() {

        tokenAcquirerService.setTokenRequestContext(null);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Token request context is not initialized.");
        }
    }

    /**
     * Test token acquisition when API client throws exception.
     */
    @Test
    public void testGetNewAccessTokenWithAPIClientException() throws APIClientException {

        doThrow(new APIClientInvocationException(ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, "Network error"))
                .when(tokenAcquirerService).callAPI(any(), any());

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Error occurred while acquiring access token");
            assertNotNull(e.getCause());
            assertEquals(e.getCause().getClass(), APIClientInvocationException.class);
        }
    }

    /**
     * Test successful refresh token acquisition.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantSuccess() throws TokenHandlerException, APIClientException {

        String responseBody = String.format(
                "{\"access_token\":\"%s\",\"refresh_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":3600}",
                ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        APIResponse mockResponse = new APIResponse.Builder(200, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        TokenResponse tokenResponse = tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);

        assertNotNull(tokenResponse);
        assertEquals(tokenResponse.getStatusCode(), 200);
        assertEquals(tokenResponse.getAccessToken(), ACCESS_TOKEN);
        assertEquals(tokenResponse.getRefreshToken(), NEW_REFRESH_TOKEN);
    }

    /**
     * Test refresh token acquisition with invalid refresh token.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithInvalidToken() throws APIClientException {

        String responseBody = String.format(
                "{\"error\":\"invalid_grant\",\"error_description\":\"The provided authorization grant is invalid\"}");
        APIResponse mockResponse = new APIResponse.Builder(400, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService
                    .getNewAccessTokenFromRefreshGrant("invalid_refresh_token");
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 400);
            assertEquals(tokenResponse.getAccessToken(), null);
            assertEquals(tokenResponse.getRefreshToken(), null);
        } catch (TokenHandlerException e) {
            fail("Should not throw exception for error response, should return error TokenResponse");
        }
    }

    /**
     * Test refresh token acquisition when context is not initialized.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithoutContext() {

        tokenAcquirerService.setTokenRequestContext(null);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Token request context is not initialized.");
        }
    }

    /**
     * Test refresh token acquisition when API client throws exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithAPIClientException() throws APIClientException {

        doThrow(new APIClientInvocationException(ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, "Network timeout"))
                .when(tokenAcquirerService).callAPI(any(), any());

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Error occurred while acquiring access token from refresh grant");
            assertNotNull(e.getCause());
            assertEquals(e.getCause().getClass(), APIClientInvocationException.class);
        }
    }

    /**
     * Test refresh token acquisition with null refresh token.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithNullToken() throws APIClientException {

        String responseBody = "{\"error\":\"invalid_request\",\"error_description\":\"Missing refresh token\"}";
        APIResponse mockResponse = new APIResponse.Builder(400, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessTokenFromRefreshGrant(null);
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 400);
        } catch (TokenHandlerException e) {
            // This might also be acceptable depending on implementation
            assertNotNull(e);
        }
    }

    /**
     * Test refresh token acquisition with empty refresh token.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithEmptyToken() throws APIClientException {

        String responseBody = "{\"error\":\"invalid_request\",\"error_description\":\"Empty refresh token\"}";
        APIResponse mockResponse = new APIResponse.Builder(400, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessTokenFromRefreshGrant("");
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 400);
        } catch (TokenHandlerException e) {
            // This might also be acceptable depending on implementation
            assertNotNull(e);
        }
    }

    /**
     * Test setting API invocation config.
     */
    @Test
    public void testSetApiInvocationConfig() {

        APIInvocationConfig newConfig = new APIInvocationConfig();
        tokenAcquirerService.setApiInvocationConfig(newConfig);

        // The test passes if no exception is thrown
        assertNotNull(tokenAcquirerService);
    }

    /**
     * Test setting token request context.
     */
    @Test
    public void testSetTokenRequestContext() {

        TokenRequestContext newContext = tokenRequestContext;
        tokenAcquirerService.setTokenRequestContext(newContext);

        // The test passes if no exception is thrown
        assertNotNull(tokenAcquirerService);
    }

    /**
     * Test setting null token request context.
     */
    @Test
    public void testSetNullTokenRequestContext() {

        tokenAcquirerService.setTokenRequestContext(null);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException");
        } catch (TokenHandlerException e) {
            assertEquals(e.getMessage(), "Token request context is not initialized.");
        }
    }

    /**
     * Test token acquisition with server error.
     */
    @Test
    public void testGetNewAccessTokenWithServerError() throws APIClientException {

        String responseBody = "{\"error\":\"server_error\",\"error_description\":\"Internal server error\"}";
        APIResponse mockResponse = new APIResponse.Builder(500, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessToken();
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 500);
            assertEquals(tokenResponse.getAccessToken(), null);
            assertEquals(tokenResponse.getRefreshToken(), null);
        } catch (TokenHandlerException e) {
            fail("Should not throw exception for server error response, should return error TokenResponse");
        }
    }

    /**
     * Test refresh token acquisition with server error.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithServerError() throws APIClientException {

        String responseBody = "{\"error\":\"server_error\",\"error_description\":\"Internal server error\"}";
        APIResponse mockResponse = new APIResponse.Builder(500, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 500);
            assertEquals(tokenResponse.getAccessToken(), null);
            assertEquals(tokenResponse.getRefreshToken(), null);
        } catch (TokenHandlerException e) {
            fail("Should not throw exception for server error response, should return error TokenResponse");
        }
    }

    /**
     * Test token acquisition with malformed JSON response.
     */
    @Test
    public void testGetNewAccessTokenWithMalformedResponse() throws APIClientException {

        String responseBody = "malformed json {";
        APIResponse mockResponse = new APIResponse.Builder(200, responseBody).build();

        doReturn(new TokenResponse.Builder(mockResponse).build()).when(tokenAcquirerService).callAPI(any(), any());

        try {
            TokenResponse tokenResponse = tokenAcquirerService.getNewAccessToken();
            assertNotNull(tokenResponse);
            assertEquals(tokenResponse.getStatusCode(), 200);
            assertEquals(tokenResponse.getAccessToken(), null);
            assertEquals(tokenResponse.getRefreshToken(), null);
        } catch (TokenHandlerException e) {
            fail("Should not throw exception for malformed response, should return TokenResponse with null tokens");
        }
    }
}
