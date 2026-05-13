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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.HTTPClientManager;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.FlowDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.flow.v1.FlowExecutionResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Test class for FlowDataRetrievalClient.
 */
@Listeners(MockitoTestNGListener.class)
public class FlowDataRetrievalClientTest {

    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String VALID_JSON_BODY =
            "{\"flowId\":\"c8e06de8-7123-44ac-8209-02be5b55387e\",\"actionId\":\"button-a2f1\","
                    + "\"inputs\":{\"http://wso2.com/claims/emailaddress\":\"sasha@example.com\"}}";
    private static final String MOCK_RESPONSE_JSON =
            "{\"flowId\":\"c8e06de8-7123-44ac-8209-02be5b55387e\",\"flowType\":\"REGISTRATION\","
                    + "\"flowStatus\":\"INCOMPLETE\",\"type\":\"VIEW\",\"data\":{\"components\":[]}}";

    private final FlowDataRetrievalClient flowDataRetrievalClient = new FlowDataRetrievalClient();

    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private ClassicHttpResponse httpResponse;
    @Mock
    private HttpEntity httpEntity;

    /**
     * Set up mock behavior before each test method.
     *
     * @throws IOException If an error occurs during mock setup.
     */
    @BeforeMethod
    public void setUp() throws IOException {

        when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                .thenAnswer(invocation -> {
                    HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                    return handler.handleResponse(httpResponse);
                });
        when(httpResponse.getCode()).thenReturn(200);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        InputStream inputStream = new ByteArrayInputStream(MOCK_RESPONSE_JSON.getBytes());
        lenient().when(httpEntity.getContent()).thenReturn(inputStream);
    }

    /**
     * Test successful flow execution request.
     *
     * @throws ApiException If an API error occurs.
     */
    @Test
    public void testExecuteFlowSuccess() throws ApiException {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                     IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class)) {

            endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(
                    SUPER_TENANT_DOMAIN, "/api/server/v1/flow/execute", true, false))
                    .thenReturn("https://localhost:9443/api/server/v1/flow/execute");
            httpClientManager.when(() -> HTTPClientManager.executeWithHttpClient(any()))
                    .thenAnswer(invocation -> {
                        HTTPClientManager.HttpClientOperation<?, ?> operation = invocation.getArgument(0);
                        return operation.execute(httpClient);
                    });

            FlowExecutionResponse response = flowDataRetrievalClient.executeFlow(
                    VALID_JSON_BODY, SUPER_TENANT_DOMAIN);

            Assert.assertNotNull(response, "Response should not be null.");
            Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
            Assert.assertNotNull(response.getResponse(), "Response body should not be null.");
            Assert.assertEquals(response.getResponse().getString("flowId"),
                    "c8e06de8-7123-44ac-8209-02be5b55387e",
                    "Flow ID should match the expected value.");
            Assert.assertEquals(response.getResponse().getString("flowStatus"), "INCOMPLETE",
                    "Flow status should match the expected value.");
            Assert.assertEquals(response.getResponse().getString("flowType"), "REGISTRATION",
                    "Flow type should match the expected value.");
        }
    }

    /**
     * Test flow execution when the response entity is null.
     *
     * @throws ApiException If an API error occurs.
     */
    @Test
    public void testExecuteFlowWithNullEntity() throws ApiException {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                     IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class)) {

            endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(
                    SUPER_TENANT_DOMAIN, "/api/server/v1/flow/execute", true, false))
                    .thenReturn("https://localhost:9443/api/server/v1/flow/execute");
            httpClientManager.when(() -> HTTPClientManager.executeWithHttpClient(any()))
                    .thenAnswer(invocation -> {
                        HTTPClientManager.HttpClientOperation<?, ?> operation = invocation.getArgument(0);
                        return operation.execute(httpClient);
                    });

            // Override the entity to return null.
            when(httpResponse.getEntity()).thenReturn(null);

            FlowExecutionResponse response = flowDataRetrievalClient.executeFlow(
                    VALID_JSON_BODY, SUPER_TENANT_DOMAIN);

            Assert.assertNotNull(response, "Response should not be null.");
            Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
            Assert.assertNotNull(response.getResponse(), "Response body should not be null.");
            Assert.assertEquals(response.getResponse().length(), 0,
                    "Response body should be an empty JSON object.");
        }
    }

    /**
     * Data provider for invalid input scenarios.
     *
     * @return Test data with null/empty json body and tenant domain combinations.
     */
    @DataProvider(name = "invalidInputProvider")
    public Object[][] invalidInputProvider() {

        return new Object[][]{
                {null, SUPER_TENANT_DOMAIN},
                {"", SUPER_TENANT_DOMAIN},
                {VALID_JSON_BODY, null},
                {VALID_JSON_BODY, ""},
                {null, null},
                {"", ""}
        };
    }

    /**
     * Test that invalid inputs throw ApiException.
     *
     * @param jsonBody     The request body.
     * @param tenantDomain The tenant domain.
     * @throws ApiException Expected exception for invalid inputs.
     */
    @Test(dataProvider = "invalidInputProvider", expectedExceptions = ApiException.class,
            expectedExceptionsMessageRegExp = "Missing the.*")
    public void testExecuteFlowWithInvalidInputs(String jsonBody, String tenantDomain)
            throws ApiException {

        flowDataRetrievalClient.executeFlow(jsonBody, tenantDomain);
    }

    /**
     * Test that IOException during HTTP execution throws ApiException.
     *
     * @throws ApiException Expected exception wrapping the IOException.
     * @throws IOException  If an I/O error occurs during mock setup.
     */
    @Test(expectedExceptions = ApiException.class,
            expectedExceptionsMessageRegExp = "Error while invoking flow execution request.")
    public void testExecuteFlowIOException() throws ApiException, IOException {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                     IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class)) {

            endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(
                    SUPER_TENANT_DOMAIN, "/api/server/v1/flow/execute", true, false))
                    .thenReturn("https://localhost:9443/api/server/v1/flow/execute");
            httpClientManager.when(() -> HTTPClientManager.executeWithHttpClient(any()))
                    .thenAnswer(invocation -> {
                        HTTPClientManager.HttpClientOperation<?, ?> operation = invocation.getArgument(0);
                        return operation.execute(httpClient);
                    });

            // Override to throw IOException.
            when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                    .thenThrow(new IOException("Connection refused"));

            flowDataRetrievalClient.executeFlow(VALID_JSON_BODY, SUPER_TENANT_DOMAIN);
        }
    }
}
