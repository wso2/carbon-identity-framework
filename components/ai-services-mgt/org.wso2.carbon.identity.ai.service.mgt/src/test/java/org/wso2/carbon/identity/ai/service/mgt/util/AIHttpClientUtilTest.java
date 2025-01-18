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

package org.wso2.carbon.identity.ai.service.mgt.util;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.TENANT_CONTEXT_PREFIX;

/**
 * Test class for AIHttpClientUtil.
 */
public class AIHttpClientUtilTest {

    private WireMockServer wireMockServer;
    private final String clientId = "testClientId";

    @Mock
    private AIAccessTokenManager mockTokenManager;

    private MockedStatic<AIAccessTokenManager> aiAccessTokenManagerMockedStatic;
    private MockedStatic<IdentityUtil> identityUtilMockedStatic;

    @BeforeClass
    public void init() {

        identityUtilMockedStatic = Mockito.mockStatic(IdentityUtil.class);
        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME))
                .thenReturn("10");
    }

    @BeforeMethod
    public void setUp() throws Exception {

        openMocks(this);
        setCarbonHome();
        setCarbonContextForTenant();

        aiAccessTokenManagerMockedStatic = mockStatic(AIAccessTokenManager.class);
        when(AIAccessTokenManager.getInstance()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken(false)).thenReturn("testToken");
        when(mockTokenManager.getClientId()).thenReturn(clientId);

        // Start WireMock server on a random port.
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();

        // Reset WireMock state for each test.
        wireMockServer.resetAll();
    }

    @Test
    public void testExecuteRequestSuccess() throws Exception {

        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        String baseUrl = wireMockServer.baseUrl();
        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );

        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        wireMockServer.verify(getRequestedFor(urlEqualTo(fullPath)));
    }

    @Test
    public void testExecuteRequestPostSuccess() throws Exception {

        String expectedResponse = "{\"result\":\"POST_SUCCESS\"}";
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        String requestBody = "{\"key\":\"value\"}";

        // Stub the POST request with the expected response.
        wireMockServer.stubFor(post(urlEqualTo(fullPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)) // Ensure the request body matches.
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        String baseUrl = wireMockServer.baseUrl();
        Map<String, String> requestBodyMap = new HashMap<>();
        requestBodyMap.put("key", "value");
        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpPost.class,
                requestBodyMap
        );

        Assert.assertEquals(resultMap.get("result"), "POST_SUCCESS");

        // Verify that the POST request was made with the correct path and body.
        wireMockServer.verify(postRequestedFor(urlEqualTo(fullPath))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(equalToJson(requestBody)));
    }

    @Test(expectedExceptions = AIClientException.class)
    public void testExecuteRequestClientError() throws Exception {

        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Bad Request")));

        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestServerError() throws Exception {

        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Internal Server Error")));

        // Act & Assert: Execute the HTTP request and expect AIServerException.
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test
    public void testExecuteRequestTokenRenewal() throws Exception {

        // Mock the AccessTokenManager to simulate token renewal.
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        // Arrange: Mock token renewal flow.
        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // First response: 401 Unauthorized.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Unauthorized"))
                .willSetStateTo("Token Renewed")); // Transition to the next state.

        // Second response: 200 OK.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal")
                .whenScenarioStateIs("Token Renewed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(expectedResponse)));

        // Act: Execute the HTTP request.
        String baseUrl = wireMockServer.baseUrl();
        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );

        // Assert: Verify the response.
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");

        // Verify the requests were made twice: once for 401 and once for 200.
        wireMockServer.verify(2, getRequestedFor(urlEqualTo(fullPath)));

        // Verify token renewal was called once.
        verify(mockTokenManager, times(1)).getAccessToken(true);
    }

    @Test(expectedExceptions = AIClientException.class)
    public void testExecuteRequestTokenRenewalErrorAfterRenewal() throws Exception {
        // Mock the AccessTokenManager to simulate token renewal.
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        // Arrange: Define paths and mock token renewal flow.
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // First response: 401 Unauthorized.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal with Error")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Unauthorized"))
                .willSetStateTo("Token Renewed")); // Transition to the next state.

        // Second response: 400 Bad Request (or you can use 500 for Internal Server Error).
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal with Error")
                .whenScenarioStateIs("Token Renewed")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Bad Request\"}"))); // Error response body.

        // Act: Execute the HTTP request.
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestIOException() throws Exception {

        // Arrange: Mock a server that simulates a connection reset.
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // Simulate a connection reset.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withFault(Fault.CONNECTION_RESET_BY_PEER))); // Simulates a connection reset.

        // Act & Assert: Expect AIServerException due to simulated IOException (connection reset).
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestExecutionException() throws Exception {

        // Arrange: Mock a server that simulates an unexpected response.
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // Simulate an unexpected response that triggers an ExecutionException.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withFault(Fault.MALFORMED_RESPONSE_CHUNK))); // Simulates a malformed response

        // Act & Assert: Expect AIServerException due to simulated ExecutionException.
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExecuteRequestUnsupportedRequestType() throws Exception {

        // Arrange: Define the path and base URL.
        String path = "/test-endpoint";
        String baseUrl = "https://ai-service.example.com";

        // Act & Assert: Pass an unsupported request type and expect IllegalArgumentException.
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpUriRequest.class, // Unsupported request type.
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestUnauthorizedAfterTokenRenewal() throws Exception {

        // Mock the AccessTokenManager for token renewal.
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        // Arrange: Define paths.
        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // First response: 401 Unauthorized.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal Fails")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Unauthorized"))
                .willSetStateTo("Retry"));

        // Second response: 401 Unauthorized again.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .inScenario("Token Renewal Fails")
                .whenScenarioStateIs("Retry")
                .willReturn(aResponse()
                        .withStatus(401) // Still Unauthorized.
                        .withHeader("Content-Type", "application/json")
                        .withBody("Still Unauthorized")));

        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestJsonParsingError() throws Exception {

        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // Mock the server to return invalid JSON.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ invalid json }"))); // Invalid JSON.

        // Act: Execute the HTTP request, expecting AIServerException due to JSON parsing error.
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequestFailedTokenRenewal() throws Exception {

        // Mock the AccessTokenManager to simulate failed token renewal.
        when(mockTokenManager.getAccessToken(false)).thenReturn("oldToken");
        when(mockTokenManager.getAccessToken(true)).thenReturn(null); // Simulate failed token renewal.

        String path = "/test-endpoint";
        String fullPath = TENANT_CONTEXT_PREFIX + clientId + path; // This is the path that AIHttpClientUtil will use.

        // Mock the server to return 401 Unauthorized.
        wireMockServer.stubFor(get(urlEqualTo(fullPath))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Unauthorized")));

        // Act: Execute the HTTP request, expecting AIServerException due to failed token renewal.
        String baseUrl = wireMockServer.baseUrl();
        AIHttpClientUtil.executeRequest(
                baseUrl,
                path,
                HttpGet.class,
                null
        );
    }

    private void setCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    private void setCarbonContextForTenant() {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    @AfterMethod
    public void tearDown() {

        aiAccessTokenManagerMockedStatic.close();
        PrivilegedCarbonContext.endTenantFlow();
        wireMockServer.stop();
    }

    @AfterClass
    public void destroy() {

        identityUtilMockedStatic.close();
    }
}
