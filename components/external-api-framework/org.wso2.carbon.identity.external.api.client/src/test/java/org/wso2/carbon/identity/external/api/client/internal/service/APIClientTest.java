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

package org.wso2.carbon.identity.external.api.client.internal.service;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.entity.StringEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientInvocationException;
import org.wso2.carbon.identity.external.api.client.api.model.APIAuthentication;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Integration tests for APIClient class using embedded HTTP server.
 * Tests complex scenarios without mocking internal components.
 */
public class APIClientTest {

    private HttpServer httpServer;
    private APIClient apiClient;
    private int serverPort;
    private String baseUrl;
    private static final String TEST_ENDPOINT = "/api/test";
    private static final String RESPONSE_BODY = "{\"result\":\"success\"}";

    @BeforeClass
    public void setUpClass() throws Exception {

        // Set carbon home to test resources directory.
        String testResourcesPath = new File(
                "src/test/resources/repository/conf/identity/identity.xml").getAbsolutePath();
        System.setProperty("carbon.home", testResourcesPath);
    }

    @BeforeMethod
    public void setUp() throws Exception {

        // Find an available port.
        serverPort = 8000 + (int) (Math.random() * 1000);

        // Create real APIClient with actual configuration.
        APIClientConfig config = new APIClientConfig.Builder()
                .httpReadTimeoutInMillis(5000)
                .httpConnectionRequestTimeoutInMillis(3000)
                .httpConnectionTimeoutInMillis(3000)
                .poolSizeToBeSet(20)
                .defaultMaxPerRoute(10)
                .build();

        apiClient = new APIClient(config);
    }

    @AfterMethod
    public void tearDown() {

        if (httpServer != null) {
            httpServer.stop(0);
        }
        System.clearProperty(ServerConstants.CARBON_HOME);
    }

    /**
     * Test successful API call with POST method and JSON payload.
     */
    @Test
    public void testCallAPISuccessfulPostRequest() throws Exception {

        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        httpServer.createContext(TEST_ENDPOINT, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + serverPort;

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(baseUrl + TEST_ENDPOINT)
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(0);

        APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test API call with BASIC authentication header verification.
     */
    @Test
    public void testCallAPIWithBasicAuthentication() throws Exception {

        final String expectedUsername = "testuser";
        final String expectedPassword = "testpass@123";
        final String expectedAuth = "Basic " + Base64.getEncoder()
                .encodeToString((expectedUsername + ":" + expectedPassword).getBytes(StandardCharsets.UTF_8));

        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        httpServer.createContext(TEST_ENDPOINT, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
                if (authHeader != null && authHeader.equals(expectedAuth)) {
                    exchange.sendResponseHeaders(200, response.length);
                } else {
                    exchange.sendResponseHeaders(401, -1);
                    return;
                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + serverPort;

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.USERNAME.getName(), expectedUsername);
        authProperties.put(APIAuthentication.Property.PASSWORD.getName(), expectedPassword);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BASIC)
                .properties(authProperties)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(baseUrl + TEST_ENDPOINT)
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(0);

        APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test API call with BEARER token authentication.
     */
    @Test
    public void testCallAPIWithBearerAuthentication() throws Exception {

        final String expectedToken = "test-bearer-token-12345";

        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        httpServer.createContext(TEST_ENDPOINT, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
                if (authHeader != null && authHeader.equals("Bearer " + expectedToken)) {
                    exchange.sendResponseHeaders(200, response.length);
                } else {
                    exchange.sendResponseHeaders(401, -1);
                    return;
                }
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + serverPort;

        Map<String, String> authProperties = new HashMap<>();
        authProperties.put(APIAuthentication.Property.ACCESS_TOKEN.getName(), expectedToken);

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.BEARER)
                .properties(authProperties)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(baseUrl + TEST_ENDPOINT)
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(0);

        APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.getResponseBody(), RESPONSE_BODY);
    }

    /**
     * Test API call with retry logic on temporary failures.
     */
    @Test
    public void testCallAPIWithRetryOnFailure() throws Exception {

        final AtomicInteger attemptCount = new AtomicInteger(0);

        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        httpServer.createContext(TEST_ENDPOINT, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                int attempt = attemptCount.incrementAndGet();
                if (attempt < 3) {
                    exchange.close();
                    return;
                }
                byte[] response = RESPONSE_BODY.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response);
                }
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + serverPort;

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(baseUrl + TEST_ENDPOINT)
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(3);

        APIResponse response = apiClient.callAPI(requestContext, invocationConfig);

        assertNotNull(response);
        assertEquals(response.getStatusCode(), 200);
        assertTrue(attemptCount.get() >= 3);
    }

    /**
     * Test API call with retry exhaustion.
     */
    @Test
    public void testCallAPIWithRetryExhaustion() throws Exception {

        httpServer = HttpServer.create(new InetSocketAddress(serverPort), 0);
        httpServer.createContext(TEST_ENDPOINT, new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {

                exchange.close();
            }
        });
        httpServer.start();
        baseUrl = "http://localhost:" + serverPort;

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl(baseUrl + TEST_ENDPOINT)
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(2);

        try {
            apiClient.callAPI(requestContext, invocationConfig);
            fail("Expected APIClientInvocationException was not thrown");
        } catch (APIClientInvocationException e) {
            assertEquals(e.getErrorCode(),
                    ErrorMessageConstant.ErrorMessage.ERROR_CODE_WHILE_INVOKING_API.getCode());
        }
    }

    /**
     * Test API call with null context throws exception.
     */
    @Test(expectedExceptions = APIClientInvocationException.class)
    public void testCallAPIWithNullContext() throws Exception {

        APIInvocationConfig invocationConfig = new APIInvocationConfig();
        invocationConfig.setAllowedRetryCount(0);

        apiClient.callAPI(null, invocationConfig);
    }

    /**
     * Test API call with null invocation config throws exception.
     */
    @Test(expectedExceptions = APIClientInvocationException.class)
    public void testCallAPIWithNullInvocationConfig() throws Exception {

        APIAuthentication authentication = new APIAuthentication.Builder()
                .authType(APIAuthentication.AuthType.NONE)
                .build();

        APIRequestContext requestContext = new APIRequestContext.Builder()
                .httpMethod(APIRequestContext.HttpMethod.POST)
                .apiAuthentication(authentication)
                .endpointUrl("http://localhost:8080/test")
                .headers(new HashMap<>())
                .payload(new StringEntity("{\"test\":\"data\"}", StandardCharsets.UTF_8))
                .build();

        apiClient.callAPI(requestContext, null);
    }
}
