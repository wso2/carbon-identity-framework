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

package org.wso2.carbon.identity.external.api.client.internal.service;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientInvocationException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

/**
 * Unit tests for APIClient class.
 * 
 * NOTE: This test class requires special setup to handle APIClientUtils static initialization.
 * The APIClient depends on APIClientConfig which requires APIClientUtils and IdentityConfigParser
 * that are normally not available in the unit test environment.
 * 
 * This test uses mocking to bypass the dependency constraints and demonstrate comprehensive
 * test patterns for APIClient functionality.
 * 
 * The test patterns include:
 * - Successful API calls with different authentication types
 * - Error handling and retry logic
 * - Different HTTP status codes
 * - Custom headers and payloads
 * - Large payload handling
 * - Exception scenarios
 */
public class APIClientTest {

    // Set up system properties before class loading to avoid IdentityConfigParser issues
    static {
        System.setProperty("carbon.home", System.getProperty("java.io.tmpdir"));
        System.setProperty("carbon.config.dir.path", System.getProperty("java.io.tmpdir"));
    }

    @Mock
    private CloseableHttpClient mockHttpClient;

    @Mock
    private CloseableHttpResponse mockHttpResponse;

    @Mock
    private HttpEntity mockHttpEntity;

    @Mock
    private StatusLine mockStatusLine;

    @Mock
    private HttpClientBuilder mockHttpClientBuilder;

    private APIClient apiClient;
    private APIRequestContext requestContext;
    private APIInvocationConfig invocationConfig;
    private MockedStatic<HttpClientBuilder> mockedHttpClientBuilder;

    private static final String ENDPOINT_URL = "https://api.example.com/test";
    private static final String PAYLOAD = "{\"test\":\"data\"}";
    private static final String RESPONSE_BODY = "{\"result\":\"success\"}";
    private static final int SUCCESS_STATUS_CODE = 200;

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);

        // Mock HttpClientBuilder static methods
        mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class);
        mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.setDefaultRequestConfig(any())).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.setConnectionManager(any())).thenReturn(mockHttpClientBuilder);
        when(mockHttpClientBuilder.build()).thenReturn(mockHttpClient);

        // Create APIClient using reflection to avoid APIClientConfig dependency
        apiClient = createAPIClientWithReflection();

        // Create test request context
        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(ENDPOINT_URL)
                .headers(headers)
                .payload(PAYLOAD)
                .build();

        // Create invocation config
        invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(0);
    }

    @AfterMethod
    public void tearDown() {

        if (mockedHttpClientBuilder != null) {
            mockedHttpClientBuilder.close();
        }
    }

    /**
     * Creates APIClient instance using reflection to avoid APIClientUtils dependencies.
     */
    private APIClient createAPIClientWithReflection() throws Exception {

        // Create APIClient directly without APIClientConfig to avoid the Builder issue
        APIClient client = createAPIClientDirectly();

        // Set the httpClient field using reflection to use our mock
        Field httpClientField = APIClient.class.getDeclaredField("httpClient");
        httpClientField.setAccessible(true);
        httpClientField.set(client, mockHttpClient);

        return client;
    }

    /**
     * Creates APIClient directly using reflection without going through APIClientConfig.
     */
    @SuppressWarnings("unchecked")
    private APIClient createAPIClientDirectly() throws Exception {

        // Create APIClient instance without calling constructor
        Class<?> apiClientClass = APIClient.class;
        Constructor<APIClient> constructor = (Constructor<APIClient>) apiClientClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        // Create a mock APIClientConfig using only the values we need
        APIClientConfig mockConfig = createMockConfig();
        
        return constructor.newInstance(mockConfig);
    }

    /**
     * Creates a mock APIClientConfig using reflection to set fields directly.
     */
    private APIClientConfig createMockConfig() throws Exception {

        // Create a simple APIClientConfig instance and set fields directly
        APIClientConfig config = org.mockito.Mockito.mock(APIClientConfig.class);
        
        // Mock the getter methods to return our desired values
        when(config.getHttpReadTimeoutInMillis()).thenReturn(30000);
        when(config.getHttpConnectionRequestTimeoutInMillis()).thenReturn(15000);
        when(config.getHttpConnectionTimeoutInMillis()).thenReturn(10000);
        when(config.getPoolSizeToBeSet()).thenReturn(20);
        
        return config;
    }

    /**
     * Test successful API call with POST method.
     */
    @Test
    public void testCallAPISuccessfulPostRequest() throws Exception {

        // Setup mock response
        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        
        // Mock EntityUtils.toString() behavior by returning our response body
        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with IOException and retry logic.
     */
    @Test
    public void testCallAPIWithIOExceptionAndRetry() throws Exception {

        invocationConfig.setAllowedRetryCount(2);

        // First two calls throw IOException, third succeeds
        when(mockHttpClient.execute(any()))
                .thenThrow(new IOException("Connection timeout"))
                .thenThrow(new IOException("Connection timeout"))
                .thenReturn(mockHttpResponse);

        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with IOException and retry exhaustion.
     */
    @Test
    public void testCallAPIWithIOExceptionRetryExhaustion() throws Exception {

        invocationConfig.setAllowedRetryCount(1);

        // All calls throw IOException
        when(mockHttpClient.execute(any())).thenThrow(new IOException("Connection timeout"));

        try {
            apiClient.callAPI(requestContext, invocationConfig);
            fail("Expected APIClientInvocationException was not thrown");
        } catch (APIClientInvocationException e) {
            assertEquals(e.getErrorCode(), ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API.getCode());
        }
    }

    /**
     * Test API call with different HTTP status codes.
     */
    @Test
    public void testCallAPIWithDifferentStatusCodes() throws Exception {

        int[] statusCodes = {200, 201, 400, 401, 404, 500, 502};

        for (int statusCode : statusCodes) {
            when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
            when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
            when(mockStatusLine.getStatusCode()).thenReturn(statusCode);
            when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

            try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
                 mockStatic(org.apache.http.util.EntityUtils.class)) {
                mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                        .thenReturn(RESPONSE_BODY);

                APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

                assertNotNull(response);
                assertEquals(response.getStatusCode(), statusCode);
                assertEquals(response.getResponseBody(), RESPONSE_BODY);
            }
        }
    }

    /**
     * Test API call with empty response body.
     */
    @Test
    public void testCallAPIWithEmptyResponseBody() throws Exception {

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn("");

            APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), "");
        }
    }

    /**
     * Test API call with null response body.
     */
    @Test
    public void testCallAPIWithNullResponseBody() throws Exception {

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(null);

            APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), null);
        }
    }

    /**
     * Test API call with BASIC authentication.
     */
    @Test
    public void testCallAPIWithBasicAuthentication() throws Exception {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put("username", "testuser");
        authProperties.put("password", "testpass");

        APIAuthentication basicAuth = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BASIC)
                .properties(authProperties)
                .build();

        APIRequestContext basicAuthContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(basicAuth)
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(basicAuthContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with BEARER authentication.
     */
    @Test
    public void testCallAPIWithBearerAuthentication() throws Exception {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put("accessToken", "test-bearer-token");

        APIAuthentication bearerAuth = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BEARER)
                .properties(authProperties)
                .build();

        APIRequestContext bearerAuthContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(bearerAuth)
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(bearerAuthContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with API_KEY authentication.
     */
    @Test
    public void testCallAPIWithApiKeyAuthentication() throws Exception {

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put("header", "X-API-Key");
        authProperties.put("value", "test-api-key");

        APIAuthentication apiKeyAuth = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.API_KEY)
                .properties(authProperties)
                .build();

        APIRequestContext apiKeyAuthContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(apiKeyAuth)
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(apiKeyAuthContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with custom headers.
     */
    @Test
    public void testCallAPIWithCustomHeaders() throws Exception {

        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("X-Custom-Header", "custom-value");
        customHeaders.put("X-Request-ID", "12345");

        APIRequestContext customHeadersContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(requestContext.getApiAuthentication())
                .endpointUrl(ENDPOINT_URL)
                .headers(customHeaders)
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(customHeadersContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with large payload.
     */
    @Test
    public void testCallAPIWithLargePayload() throws Exception {

        StringBuilder largePayload = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largePayload.append("{\"data\":\"This is a large payload with lots of data. \"}");
        }

        APIRequestContext largePayloadContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(requestContext.getApiAuthentication())
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(largePayload.toString())
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(largePayloadContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with maximum retry count.
     */
    @Test
    public void testCallAPIWithMaxRetryCount() throws Exception {

        invocationConfig.setAllowedRetryCount(Integer.MAX_VALUE);

        // First call throws IOException, second succeeds
        when(mockHttpClient.execute(any()))
                .thenThrow(new IOException("Connection timeout"))
                .thenReturn(mockHttpResponse);

        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with null response entity.
     */
    @Test
    public void testCallAPIWithNullResponseEntity() throws Exception {

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(204); // No content
        when(mockHttpResponse.getEntity()).thenReturn(null);

        APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 204);
        assertEquals(response.getResponseBody(), null);
    }

    /**
     * Test API call with empty headers map.
     */
    @Test
    public void testCallAPIWithEmptyHeaders() throws Exception {

        APIRequestContext emptyHeadersContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(requestContext.getApiAuthentication())
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(emptyHeadersContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with IOException on first attempt and zero retry count.
     */
    @Test
    public void testCallAPIWithIOExceptionAndZeroRetryCount() throws Exception {

        invocationConfig.setAllowedRetryCount(0);

        // First call throws IOException
        when(mockHttpClient.execute(any())).thenThrow(new IOException("Connection refused"));

        try {
            apiClient.callAPI(requestContext, invocationConfig);
            fail("Expected APIClientInvocationException was not thrown");
        } catch (APIClientInvocationException e) {
            assertEquals(e.getErrorCode(), ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API.getCode());
        }
    }

    /**
     * Test API call with null authentication header.
     */
    @Test
    public void testCallAPIWithNullAuthenticationHeader() throws Exception {

        APIAuthentication noneAuth = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext noneAuthContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(noneAuth)
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(noneAuthContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with multiple custom headers.
     */
    @Test
    public void testCallAPIWithMultipleCustomHeaders() throws Exception {

        Map<String, String> multipleHeaders = new HashMap<>();
        multipleHeaders.put("X-Custom-Header-1", "value1");
        multipleHeaders.put("X-Custom-Header-2", "value2");
        multipleHeaders.put("X-Custom-Header-3", "value3");
        multipleHeaders.put("X-Request-ID", "abc-123");
        multipleHeaders.put("X-Correlation-ID", "xyz-789");

        APIRequestContext multipleHeadersContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(requestContext.getApiAuthentication())
                .endpointUrl(ENDPOINT_URL)
                .headers(multipleHeaders)
                .payload(PAYLOAD)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(multipleHeadersContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }

    /**
     * Test API call with special characters in payload.
     */
    @Test
    public void testCallAPIWithSpecialCharactersInPayload() throws Exception {

        String specialPayload = "{\"data\":\"Test with special chars: äöü, ñ, 中文, 日本語, 한글, ® © ™\"}";

        APIRequestContext specialCharsContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(requestContext.getApiAuthentication())
                .endpointUrl(ENDPOINT_URL)
                .headers(new HashMap<>())
                .payload(specialPayload)
                .build();

        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SUCCESS_STATUS_CODE);
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);

        try (MockedStatic<org.apache.http.util.EntityUtils> mockedEntityUtils = 
             mockStatic(org.apache.http.util.EntityUtils.class)) {
            mockedEntityUtils.when(() -> org.apache.http.util.EntityUtils.toString(mockHttpEntity))
                    .thenReturn(RESPONSE_BODY);

            APIResponse response = apiClient.callAPI(specialCharsContext, invocationConfig);

            assertNotNull(response);
            assertEquals(response.getStatusCode(), SUCCESS_STATUS_CODE);
            assertEquals(response.getResponseBody(), RESPONSE_BODY);
        }
    }
}
