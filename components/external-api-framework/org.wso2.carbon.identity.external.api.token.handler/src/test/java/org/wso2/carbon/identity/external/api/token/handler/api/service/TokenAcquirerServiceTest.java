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

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.token.handler.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenHandlerException;
import org.wso2.carbon.identity.external.api.token.handler.api.exception.TokenRequestException;
import org.wso2.carbon.identity.external.api.token.handler.api.model.GrantContext;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenInvocationResult;
import org.wso2.carbon.identity.external.api.token.handler.api.model.TokenRequestContext;
import org.wso2.carbon.identity.external.api.token.handler.internal.util.TokenRequestBuilderUtils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for TokenAcquirerService class.
 */
public class TokenAcquirerServiceTest {

    private TokenAcquirerService tokenAcquirerService;
    private APIClientConfig apiClientConfig;
    private TokenRequestContext tokenRequestContext;
    private APIInvocationConfig apiInvocationConfig;
    private MockedStatic<TokenRequestBuilderUtils> mockedUtils;

    private static final String CLIENT_ID = "test-client-id";
    private static final String CLIENT_SECRET = "test-client-secret";
    private static final String SCOPE = "test-scope";
    private static final String TOKEN_ENDPOINT_URL = "https://example.com/token";
    private static final String ACCESS_TOKEN = "test-access-token";
    private static final String REFRESH_TOKEN = "test-refresh-token";

    /**
     * Sets up test environment before any tests run.
     * This must run before any instance initialization.
     */
    @BeforeClass
    public void setUpClass() {

        String testResourcesPath = new File(
                "src/test/resources/repository/conf/identity/identity.xml").getAbsolutePath();
        System.setProperty("carbon.home", testResourcesPath);
        IdentityConfigParser.getInstance(testResourcesPath);
    }

    @AfterClass
    public void tearDownClass() {

        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    /**
     * Sets up test fixtures before each test method.
     * Initializes mocks and creates test configuration.
     */
    @BeforeMethod
    public void setUp() throws Exception {

        // Create test configuration with default values.
        apiClientConfig = new APIClientConfig.Builder()
                .httpConnectionTimeoutInMillis(30000)
                .httpConnectionRequestTimeoutInMillis(30000)
                .httpReadTimeoutInMillis(30000)
                .poolSizeToBeSet(50)
                .defaultMaxPerRoute(20)
                .build();

        // Create grant context with client credentials.
        Map<String, String> properties = new HashMap<>();
        properties.put("client_id", CLIENT_ID);
        properties.put("client_secret", CLIENT_SECRET);
        properties.put("scope", SCOPE);

        GrantContext grantContext = new GrantContext.Builder()
                .grantType(GrantContext.GrantType.CLIENT_CREDENTIAL)
                .properties(properties)
                .build();

        // Initialize token request context with grant context.
        tokenRequestContext = new TokenRequestContext.Builder()
                .grantContext(grantContext)
                .endpointUrl(TOKEN_ENDPOINT_URL)
                .build();

        // Initialize API invocation config.
        apiInvocationConfig = new APIInvocationConfig();

        // Initialize MockedStatic for TokenRequestBuilderUtils.
        mockedUtils = Mockito.mockStatic(TokenRequestBuilderUtils.class);

        // Initialize service with test configuration and null response (will be set per test).
        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);
    }

    @AfterMethod
    public void tearDown() {

        if (mockedUtils != null) {
            mockedUtils.close();
        }
    }

    /**
     * Test successful token acquisition.
     */
    @Test
    public void testGetNewAccessTokenSuccess() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600}", ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken();

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getStatusCode(), 200);
        assertEquals(result.getTokenResponse().getAccessToken(), ACCESS_TOKEN);
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)), 
                times(1));
    }

    /**
     * Test token acquisition with both access and refresh tokens.
     */
    @Test
    public void testGetNewAccessTokenWithRefreshToken() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        String responseBody = String.format("{\"access_token\":\"%s\",\"refresh_token\":\"%s\"," +
                "\"token_type\":\"Bearer\",\"expires_in\":3600}", ACCESS_TOKEN, REFRESH_TOKEN);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken();

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getAccessToken(), ACCESS_TOKEN);
        assertEquals(result.getTokenResponse().getRefreshToken(), REFRESH_TOKEN);
    }

    /**
     * Test token acquisition without token request context should throw exception.
     */
    @Test
    public void testGetNewAccessTokenWithoutContext() {

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65009");
            assertEquals(e.getDescription(), "The TokenRequestContext is not initialized.");
        }
    }

    /**
     * Test token acquisition with non-200 status code should throw exception.
     */
    @Test
    public void testGetNewAccessTokenWithErrorResponse() {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        String errorResponseBody = "{\"error\":\"invalid_client\"}";
        APIResponse apiResponse = new APIResponse(401, errorResponseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65012");
            assertEquals(e.getDescription(),
                    "Unexpected response status code: 401. Expected: 200.");
        }
    }

    /**
     * Test token acquisition when API client throws exception.
     */
    @Test
    public void testGetNewAccessTokenWithAPIClientException() {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        APIClientException mockException = Mockito.mock(APIClientException.class);
        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, mockException);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65010");
            assertEquals(e.getDescription(),
                    "Error occurred while getting access token from CLIENT_CREDENTIAL grant type.");
            assertNotNull(e.getCause());
        }
    }

    /**
     * Test token acquisition when TokenRequestBuilderUtils throws exception.
     */
    @Test
    public void testGetNewAccessTokenWithBuilderException() {

        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenThrow(new TokenRequestException(ErrorMessage.ERROR_CODE_BUILDING_API_REQUEST, "test"));

        APIResponse apiResponse = new APIResponse(200, "{\"access_token\":\"test\"}");
        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessToken();
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test successful token acquisition using refresh grant.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantSuccess() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString())).thenReturn(mockRequestContext);

        String newAccessToken = "new-access-token";
        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600}", newAccessToken);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getAccessToken(), newAccessToken);
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString()), times(1));
    }

    /**
     * Test refresh grant without token request context should throw exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithoutContext() {

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65009");
            assertEquals(e.getDescription(), "The TokenRequestContext is not initialized.");
        }
    }

    /**
     * Test refresh grant with null refresh token should throw exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithNullToken() {

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(null);
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65011");
            assertEquals(e.getDescription(), "Error occurred while getting access token from refresh grant" +
                    " type. Refresh token cannot be null or empty.");
        }
    }

    /**
     * Test refresh grant with empty refresh token should throw exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithEmptyToken() {

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant("");
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65011");
            assertEquals(e.getDescription(), "Error occurred while getting access token from refresh " +
                    "grant type. Refresh token cannot be null or empty.");
        }
    }

    /**
     * Test refresh grant with blank refresh token should throw exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithBlankToken() {

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, (APIResponse) null);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant("   ");
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65011");
            assertEquals(e.getDescription(), "Error occurred while getting access token from refresh " +
                    "grant type. Refresh token cannot be null or empty.");
        }
    }

    /**
     * Test refresh grant with error response.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithErrorResponse() {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString())).thenReturn(mockRequestContext);

        String errorResponseBody = "{\"error\":\"invalid_grant\"}";
        APIResponse apiResponse = new APIResponse(400, errorResponseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65012");
            assertEquals(e.getDescription(),
                    "Unexpected response status code: 400. Expected: 200.");
        }
    }

    /**
     * Test refresh grant when API client throws exception.
     */
    @Test
    public void testGetNewAccessTokenFromRefreshGrantWithAPIClientException() {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString())).thenReturn(mockRequestContext);

        APIClientException mockException = Mockito.mock(APIClientException.class);
        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, mockException);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessTokenFromRefreshGrant(REFRESH_TOKEN);
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65010");
            assertEquals(e.getDescription(),
                    "Error occurred while getting access token from refresh grant type.");
            assertNotNull(e.getCause());
        }
    }

    /**
     * Test getNewAccessToken with valid refresh token should use refresh grant.
     */
    @Test
    public void testGetNewAccessTokenWithValidRefreshToken() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString())).thenReturn(mockRequestContext);

        String newAccessToken = "new-access-token-from-refresh";
        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600}", newAccessToken);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken(REFRESH_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getAccessToken(), newAccessToken);
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString()), times(1));
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)),
                times(0));
    }

    /**
     * Test getNewAccessToken with null refresh token should use the configured grant type.
     */
    @Test
    public void testGetNewAccessTokenWithNullRefreshToken() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600}", ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken(null);

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getAccessToken(), ACCESS_TOKEN);
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)),
                times(1));
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString()), times(0));
    }

    /**
     * Test getNewAccessToken with refresh token that fails should fallback to the configured grant type.
     */
    @Test
    public void testGetNewAccessTokenWithRefreshTokenFallbackToClientCredentials() throws Exception {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        APIRequestContext mockRequestContextForClientCreds = Mockito.mock(APIRequestContext.class);

        // First call for refresh grant throws exception.
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                        any(TokenRequestContext.class), anyString()))
                .thenReturn(mockRequestContext);

        // Second call for client credentials succeeds.
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContextForClientCreds);

        String responseBody = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\"," +
                "\"expires_in\":3600}", ACCESS_TOKEN);
        APIResponse successResponse = new APIResponse(200, responseBody);
        APIResponse errorResponse = new APIResponse(400, "{\"error\":\"invalid_grant\"}");

        // Create a testable service that returns error first, then success.
        tokenAcquirerService = new MultiResponseTokenAcquirerService(apiClientConfig, errorResponse, successResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken(REFRESH_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
        assertEquals(result.getTokenResponse().getAccessToken(), ACCESS_TOKEN);
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                any(TokenRequestContext.class), anyString()), times(1));
        mockedUtils.verify(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)),
                times(1));
    }

    /**
     * Test getNewAccessToken with refresh token when both refresh grant and client credentials fail.
     */
    @Test
    public void testGetNewAccessTokenWithRefreshTokenBothGrantsFail() {

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContextForRefreshGrant(
                        any(TokenRequestContext.class), anyString()))
                .thenReturn(mockRequestContext);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        APIResponse errorResponse = new APIResponse(401, "{\"error\":\"unauthorized\"}");

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, errorResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(apiInvocationConfig);

        try {
            tokenAcquirerService.getNewAccessToken(REFRESH_TOKEN);
            fail("Expected TokenHandlerException was not thrown.");
        } catch (TokenHandlerException e) {
            assertEquals(e.getErrorCode(), "TOKENMGT-65012");
            assertEquals(e.getDescription(),
                    "Unexpected response status code: 401. Expected: 200.");
        }
    }

    /**
     * Test setting API invocation config.
     */
    @Test
    public void testSetApiInvocationConfig() throws Exception {

        APIInvocationConfig customConfig = new APIInvocationConfig();
        customConfig.setAllowedRetryCount(3);

        APIRequestContext mockRequestContext = Mockito.mock(APIRequestContext.class);
        mockedUtils.when(() -> TokenRequestBuilderUtils.buildAPIRequestContext(any(TokenRequestContext.class)))
                .thenReturn(mockRequestContext);

        String responseBody = String.format("{\"access_token\":\"%s\"}", ACCESS_TOKEN);
        APIResponse apiResponse = new APIResponse(200, responseBody);

        tokenAcquirerService = new TestableTokenAcquirerService(apiClientConfig, apiResponse);
        tokenAcquirerService.setTokenRequestContext(tokenRequestContext);
        tokenAcquirerService.setApiInvocationConfig(customConfig);

        TokenInvocationResult result = tokenAcquirerService.getNewAccessToken();
        assertNotNull(result);
        assertNotNull(result.getTokenResponse());
    }

    /**
     * Testable subclass of TokenAcquirerService to override callAPI method for testing.
     */
    private static class TestableTokenAcquirerService extends TokenAcquirerService {

        private final APIResponse mockResponse;
        private final APIClientException mockException;

        public TestableTokenAcquirerService(APIClientConfig apiClientConfig, APIResponse mockResponse) {

            super(apiClientConfig);
            this.mockResponse = mockResponse;
            this.mockException = null;
        }

        public TestableTokenAcquirerService(APIClientConfig apiClientConfig, APIClientException mockException) {

            super(apiClientConfig);
            this.mockResponse = null;
            this.mockException = mockException;
        }

        @Override
        public APIResponse callAPI(APIRequestContext requestContext, APIInvocationConfig apiInvocationConfig)
                throws APIClientException {

            if (mockException != null) {
                throw mockException;
            }
            return mockResponse;
        }
    }

    /**
     * Testable subclass to simulate multiple different responses for fallback testing.
     */
    private static class MultiResponseTokenAcquirerService extends TokenAcquirerService {

        private final APIResponse firstResponse;
        private final APIResponse secondResponse;
        private int callCount = 0;

        public MultiResponseTokenAcquirerService(APIClientConfig apiClientConfig, APIResponse firstResponse,
                                                  APIResponse secondResponse) {

            super(apiClientConfig);
            this.firstResponse = firstResponse;
            this.secondResponse = secondResponse;
        }

        @Override
        public APIResponse callAPI(APIRequestContext requestContext, APIInvocationConfig apiInvocationConfig)
                throws APIClientException {

            callCount++;
            if (callCount == 1) {
                return firstResponse;
            }
            return secondResponse;
        }
    }
}
