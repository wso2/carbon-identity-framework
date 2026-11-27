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
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.HTTPClientManager;
import org.wso2.carbon.identity.mgt.endpoint.util.client.ApiException;
import org.wso2.carbon.identity.mgt.endpoint.util.client.OrganizationDataRetrievalClient;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.organization.v1.OrganizationCheckResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for OrganizationDataRetrievalClient.
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationDataRetrievalClientTest {

    private static final String SUPER_TENANT_DOMAIN = "carbon.super";
    private static final String VALID_JSON_BODY = "{\"organizationHandle\":\"wardrobe\"}";
    private static final String MOCK_RESPONSE_JSON = "{\"available\":true}";
    private static final String CHECK_HANDLE_PATH = "/api/server/v1/organizations/check-handle";
    private static final String CHECK_HANDLE_URL = "https://localhost:9443" + CHECK_HANDLE_PATH;
    private static final String APP_NAME = "admin";
    private static final String APP_PASSWORD = "p@ssw0rd";

    private final OrganizationDataRetrievalClient organizationDataRetrievalClient =
            new OrganizationDataRetrievalClient();

    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private ClassicHttpResponse httpResponse;
    @Mock
    private HttpEntity httpEntity;
    @Mock
    private IdentityManagementServiceUtil identityManagementServiceUtil;

    /**
     * Set up mock behavior before each test method.
     *
     * @throws IOException If an error occurs during mock setup.
     */
    @BeforeMethod
    public void setUp() throws IOException {

        lenient().when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                .thenAnswer(invocation -> {
                    HttpClientResponseHandler<?> handler = invocation.getArgument(1);
                    return handler.handleResponse(httpResponse);
                });
        lenient().when(httpResponse.getCode()).thenReturn(200);
        lenient().when(httpResponse.getEntity()).thenReturn(httpEntity);
        InputStream inputStream = new ByteArrayInputStream(MOCK_RESPONSE_JSON.getBytes(StandardCharsets.UTF_8));
        lenient().when(httpEntity.getContent()).thenReturn(inputStream);
    }

    /**
     * Test a successful organization handle check request.
     *
     * @throws Exception If an error occurs.
     */
    @Test
    public void testCheckHandleSuccess() throws Exception {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class);
             MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                     IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockEndpointAndHttp(endpointUtil, httpClientManager);
            mockCredentials(serviceUtil, APP_PASSWORD.toCharArray());
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);

            OrganizationCheckResponse response = organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);

            Assert.assertNotNull(response, "Response should not be null.");
            Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
            Assert.assertNotNull(response.getResponse(), "Response body should not be null.");
            Assert.assertTrue(response.getResponse().getBoolean("available"),
                    "Handle availability should match the expected value.");

            ArgumentCaptor<ClassicHttpRequest> request = ArgumentCaptor.forClass(ClassicHttpRequest.class);
            verify(httpClient).execute(request.capture(), any(HttpClientResponseHandler.class));
            Assert.assertEquals(request.getValue().getMethod(), "POST");
            Assert.assertEquals(request.getValue().getUri().toString(), CHECK_HANDLE_URL);
            Assert.assertEquals(request.getValue().getFirstHeader(HttpHeaders.ACCEPT).getValue(),
                    "application/json; charset=UTF-8");

            String authorizationHeader = request.getValue().getFirstHeader(HttpHeaders.AUTHORIZATION).getValue();
            Assert.assertEquals(authorizationHeader, "Client " + Base64.getEncoder().encodeToString(
                    (APP_NAME + ":" + APP_PASSWORD).getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Test that only the organization handle is forwarded to the API, even if the incoming body has more fields.
     *
     * @throws Exception If an error occurs.
     */
    @Test
    public void testCheckHandleForwardsOnlyTheHandle() throws Exception {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class);
             MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                     IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockEndpointAndHttp(endpointUtil, httpClientManager);
            mockCredentials(serviceUtil, APP_PASSWORD.toCharArray());
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);

            organizationDataRetrievalClient.checkHandle(
                    "{\"organizationHandle\":\"wardrobe\",\"unexpectedField\":\"value\"}");

            ArgumentCaptor<ClassicHttpRequest> request = ArgumentCaptor.forClass(ClassicHttpRequest.class);
            verify(httpClient).execute(request.capture(), any(HttpClientResponseHandler.class));
            String forwardedBody = new String(request.getValue().getEntity().getContent().readAllBytes(),
                    StandardCharsets.UTF_8);
            Assert.assertEquals(forwardedBody, VALID_JSON_BODY,
                    "Only the organization handle should be forwarded to the API.");
        }
    }

    /**
     * Test that a non-JSON error response keeps the real status code.
     *
     * @throws Exception If an error occurs.
     */
    @Test
    public void testCheckHandleWithNonJsonErrorResponse() throws Exception {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class);
             MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                     IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockEndpointAndHttp(endpointUtil, httpClientManager);
            mockCredentials(serviceUtil, APP_PASSWORD.toCharArray());
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);
            when(httpResponse.getCode()).thenReturn(401);
            when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(
                    "<html>Unauthorized</html>".getBytes(StandardCharsets.UTF_8)));

            OrganizationCheckResponse response = organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);

            Assert.assertEquals(response.getStatusCode(), 401, "Status code should be preserved.");
            Assert.assertEquals(response.getResponse().length(), 0,
                    "Response body should be an empty JSON object.");
        }
    }

    /**
     * Test the organization handle check when the response entity is null.
     *
     * @throws Exception If an error occurs.
     */
    @Test
    public void testCheckHandleWithNullEntity() throws Exception {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class);
             MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                     IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockEndpointAndHttp(endpointUtil, httpClientManager);
            mockCredentials(serviceUtil, APP_PASSWORD.toCharArray());
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);
            when(httpResponse.getEntity()).thenReturn(null);

            OrganizationCheckResponse response = organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);

            Assert.assertNotNull(response, "Response should not be null.");
            Assert.assertEquals(response.getStatusCode(), 200, "Status code should be 200.");
            Assert.assertEquals(response.getResponse().length(), 0,
                    "Response body should be an empty JSON object.");
        }
    }

    /**
     * Test that missing client credentials throw ApiException instead of a NullPointerException.
     */
    @Test
    public void testCheckHandleWithMissingClientCredentials() {

        try (MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockCredentials(serviceUtil, null);
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);

            try {
                organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);
                Assert.fail("Expected ApiException for missing client credentials.");
            } catch (ApiException e) {
                Assert.assertTrue(e.getMessage().contains("Client authentication credentials are not configured."),
                        "Unexpected error message: " + e.getMessage());
            }
        }
    }

    /**
     * Test that a missing tenant domain in the current context throws ApiException.
     */
    @Test
    public void testCheckHandleWithMissingTenantDomain() {

        try (MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockTenant(carbonContext, null);

            try {
                organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);
                Assert.fail("Expected ApiException for missing tenant domain.");
            } catch (ApiException e) {
                Assert.assertTrue(e.getMessage().contains("Missing the tenant domain in the current context."),
                        "Unexpected error message: " + e.getMessage());
            }
        }
    }

    /**
     * Data provider for invalid input scenarios.
     *
     * @return Test data with invalid request bodies.
     */
    @DataProvider(name = "invalidInputProvider")
    public Object[][] invalidInputProvider() {

        return new Object[][]{
                {null},
                {""},
                {"not-a-json"},
                {"{}"},
                {"{\"organizationHandle\":123}"},
                {"{\"organizationHandle\":\"\"}"},
                {"{\"organizationHandle\":\"  \"}"}
        };
    }

    /**
     * Test that invalid inputs throw ApiException.
     *
     * @param jsonBody The request body.
     * @throws ApiException Expected exception for invalid inputs.
     */
    @Test(dataProvider = "invalidInputProvider", expectedExceptions = ApiException.class,
            expectedExceptionsMessageRegExp = "Missing the.*|Invalid request body.*")
    public void testCheckHandleWithInvalidInputs(String jsonBody) throws ApiException {

        organizationDataRetrievalClient.checkHandle(jsonBody);
    }

    /**
     * Test that IOException during HTTP execution throws ApiException.
     *
     * @throws Exception Expected exception wrapping the IOException.
     */
    @Test(expectedExceptions = ApiException.class,
            expectedExceptionsMessageRegExp = "Error while invoking organization handle check request.")
    public void testCheckHandleIOException() throws Exception {

        try (MockedStatic<IdentityManagementEndpointUtil> endpointUtil = mockStatic(
                IdentityManagementEndpointUtil.class);
             MockedStatic<HTTPClientManager> httpClientManager = mockStatic(HTTPClientManager.class);
             MockedStatic<IdentityManagementServiceUtil> serviceUtil = mockStatic(
                     IdentityManagementServiceUtil.class);
             MockedStatic<PrivilegedCarbonContext> carbonContext = mockStatic(PrivilegedCarbonContext.class)) {

            mockEndpointAndHttp(endpointUtil, httpClientManager);
            mockCredentials(serviceUtil, APP_PASSWORD.toCharArray());
            mockTenant(carbonContext, SUPER_TENANT_DOMAIN);
            when(httpClient.execute(any(ClassicHttpRequest.class), any(HttpClientResponseHandler.class)))
                    .thenThrow(new IOException("Connection refused"));

            organizationDataRetrievalClient.checkHandle(VALID_JSON_BODY);
        }
    }

    private void mockEndpointAndHttp(MockedStatic<IdentityManagementEndpointUtil> endpointUtil,
                                     MockedStatic<HTTPClientManager> httpClientManager) {

        endpointUtil.when(() -> IdentityManagementEndpointUtil.getBasePath(
                        SUPER_TENANT_DOMAIN, CHECK_HANDLE_PATH, true, false))
                .thenReturn(CHECK_HANDLE_URL);
        httpClientManager.when(() -> HTTPClientManager.executeWithHttpClient(any()))
                .thenAnswer(invocation -> {
                    HTTPClientManager.HttpClientOperation<?, ?> operation = invocation.getArgument(0);
                    return operation.execute(httpClient);
                });
    }

    private void mockCredentials(MockedStatic<IdentityManagementServiceUtil> serviceUtil, char[] appPassword) {

        serviceUtil.when(IdentityManagementServiceUtil::getInstance).thenReturn(identityManagementServiceUtil);
        when(identityManagementServiceUtil.getAppName()).thenReturn(APP_NAME);
        when(identityManagementServiceUtil.getAppPassword()).thenReturn(appPassword);
    }

    private void mockTenant(MockedStatic<PrivilegedCarbonContext> carbonContext, String tenantDomain) {

        PrivilegedCarbonContext context = mock(PrivilegedCarbonContext.class);
        carbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext).thenReturn(context);
        when(context.getTenantDomain()).thenReturn(tenantDomain);
    }
}
