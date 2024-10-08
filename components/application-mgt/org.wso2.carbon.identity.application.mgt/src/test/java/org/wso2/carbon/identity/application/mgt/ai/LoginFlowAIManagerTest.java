/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.ai;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.realm.InMemoryRealmService;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID;

public class LoginFlowAIManagerTest {

    private MockedStatic<LoginFlowAITokenService> loginFlowTokenServiceMock;
    private MockedStatic<LoginFlowAIManagerImpl.HttpClientHelper> httpClientHelperMockedStatic;

    @InjectMocks
    private LoginFlowAIManagerImpl loginFlowAIManager;

    @BeforeMethod
    public void setUp() throws UserStoreException {
        initMocks(this);
        setCarbonHome();
        setCarbonContextForTenant(SUPER_TENANT_DOMAIN_NAME, SUPER_TENANT_ID);
        loginFlowTokenServiceMock = mockStatic(LoginFlowAITokenService.class);
        httpClientHelperMockedStatic = mockStatic(LoginFlowAIManagerImpl.HttpClientHelper.class);
    }

    @Test
    public void testGenerateAuthenticationSequence_Success() throws Exception {
        mockSuccessfulResponse("{\"operation_id\": \"12345\"}", HttpPost.class);
        String result = loginFlowAIManager.generateAuthenticationSequence("Need username and password as" +
                " the first step", new JSONArray(), new JSONObject());
        Assert.assertEquals(result, "12345");
    }

    @Test
    public void testGetAuthenticationSequenceGenerationStatus_Success() throws Exception {
        mockSuccessfulResponse("{\"status\":\"COMPLETED\"}", HttpGet.class);
        Object result = loginFlowAIManager.getAuthenticationSequenceGenerationStatus("operation123");

        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals("COMPLETED", resultMap.get("status"));
    }

    @Test
    public void testGetAuthenticationSequenceGenerationResult_Success() throws Exception {
        mockSuccessfulResponse("{\"result\":\"SUCCESS\"}", HttpGet.class);
        Object result = loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");

        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals("SUCCESS", resultMap.get("result"));
    }

    @Test
    public void testTokenRenewalVerification() throws Exception {
        LoginFlowAITokenService mockTokenService = mock(LoginFlowAITokenService.class);
        loginFlowTokenServiceMock.when(LoginFlowAITokenService::getInstance).thenReturn(mockTokenService);
        when(mockTokenService.getAccessToken(false)).thenReturn("initialMockToken");
        when(mockTokenService.getAccessToken(true)).thenReturn("renewedMockToken");
        when(mockTokenService.getClientId()).thenReturn("mockOrgName");

        LoginFlowAIManagerImpl.HttpResponseWrapper unauthorizedResponse =
                new LoginFlowAIManagerImpl.HttpResponseWrapper(401, "Unauthorized");
        LoginFlowAIManagerImpl.HttpResponseWrapper successResponse =
                new LoginFlowAIManagerImpl.HttpResponseWrapper(200, "{\"result\":\"SUCCESS\"}");

        httpClientHelperMockedStatic.when(() -> LoginFlowAIManagerImpl.HttpClientHelper.executeRequest(
                any(CloseableHttpAsyncClient.class), any(HttpUriRequest.class)))
                .thenReturn(unauthorizedResponse)
                .thenReturn(successResponse);


        Object result = loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
        Assert.assertTrue(result instanceof Map);
        Map<String, Object> resultMap = (Map<String, Object>) result;
        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        verify(mockTokenService).getAccessToken(true);
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testTokenRenewalFailure() throws Exception {
        LoginFlowAITokenService mockTokenService = mock(LoginFlowAITokenService.class);
        loginFlowTokenServiceMock.when(LoginFlowAITokenService::getInstance).thenReturn(mockTokenService);
        when(mockTokenService.getAccessToken(false)).thenReturn("initialMockToken");
        when(mockTokenService.getAccessToken(true)).thenReturn(null);
        when(mockTokenService.getClientId()).thenReturn("mockClientId");

        LoginFlowAIManagerImpl.HttpResponseWrapper unauthorizedResponse =
                new LoginFlowAIManagerImpl.HttpResponseWrapper(401, "Unauthorized");
        httpClientHelperMockedStatic.when(() -> LoginFlowAIManagerImpl.HttpClientHelper.executeRequest(
                any(CloseableHttpAsyncClient.class), any(HttpUriRequest.class))).thenReturn(unauthorizedResponse);

        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    @Test(expectedExceptions = LoginFlowAIClientException.class)
    public void testClientError() throws Exception {
        mockErrorResponse(400, "Client Error");
        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testServerError() throws Exception {
        mockErrorResponse(500, "Server Error");
        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testParsingError() throws Exception {
        mockSuccessfulResponse("{invalid_json}", HttpGet.class);
        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testIOException() throws Exception {
        mockExceptionDuringRequest(new IOException("Simulated IOException"));
        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testInterruptedException() throws Exception {
        mockExceptionDuringRequest(new InterruptedException("Simulated InterruptedException"));
        loginFlowAIManager.getAuthenticationSequenceGenerationResult("operation123");
    }

    private void mockSuccessfulResponse(String responseBody, Class<? extends HttpUriRequest> requestClass)
            throws Exception {
        LoginFlowAITokenService mockTokenService = mock(LoginFlowAITokenService.class);
        loginFlowTokenServiceMock.when(LoginFlowAITokenService::getInstance).thenReturn(mockTokenService);
        when(mockTokenService.getAccessToken(anyBoolean())).thenReturn("mockAccessToken");

        LoginFlowAIManagerImpl.HttpResponseWrapper mockResponse = new LoginFlowAIManagerImpl
                .HttpResponseWrapper(200, responseBody);
        httpClientHelperMockedStatic.when(() -> LoginFlowAIManagerImpl.HttpClientHelper.executeRequest(
                any(CloseableHttpAsyncClient.class), any(requestClass))).thenReturn(mockResponse);
    }

    private void mockErrorResponse(int statusCode, String responseBody) throws Exception {
        LoginFlowAITokenService mockTokenService = mock(LoginFlowAITokenService.class);
        loginFlowTokenServiceMock.when(LoginFlowAITokenService::getInstance).thenReturn(mockTokenService);
        when(mockTokenService.getAccessToken(false)).thenReturn("mockAccessToken");

        LoginFlowAIManagerImpl.HttpResponseWrapper errorResponse =
                new LoginFlowAIManagerImpl.HttpResponseWrapper(statusCode, responseBody);
        httpClientHelperMockedStatic.when(() -> LoginFlowAIManagerImpl.HttpClientHelper.executeRequest(
                any(CloseableHttpAsyncClient.class), any(HttpUriRequest.class))).thenReturn(errorResponse);
    }

    private void mockExceptionDuringRequest(Exception exception) throws Exception {
        LoginFlowAITokenService mockTokenService = mock(LoginFlowAITokenService.class);
        loginFlowTokenServiceMock.when(LoginFlowAITokenService::getInstance).thenReturn(mockTokenService);
        when(mockTokenService.getAccessToken(false)).thenReturn("mockAccessToken");

        httpClientHelperMockedStatic.when(() -> LoginFlowAIManagerImpl.HttpClientHelper.executeRequest(
                any(CloseableHttpAsyncClient.class), any(HttpUriRequest.class))).thenThrow(exception);
    }

    private void setCarbonHome() {
        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository")
                .toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    private void setCarbonContextForTenant(String tenantDomain, int tenantId) throws UserStoreException {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        InMemoryRealmService testSessionRealmService = new InMemoryRealmService(tenantId);
        IdentityTenantUtil.setRealmService(testSessionRealmService);
    }

    @AfterMethod
    public void tearDown() {
        loginFlowTokenServiceMock.close();
        httpClientHelperMockedStatic.close();
    }
}
