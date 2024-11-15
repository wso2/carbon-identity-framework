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

package org.wso2.carbon.ai.service.mgt.util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class AIHttpClientUtilTest {

    @Mock
    private AIAccessTokenManager mockTokenManager;

    @Mock
    private CloseableHttpAsyncClient mockHttpClient;

    private MockedStatic<AIAccessTokenManager> aiAccessTokenManagerMockedStatic;

    private MockedStatic<org.apache.http.impl.nio.client.HttpAsyncClients> httpAsyncClientsMockedStatic;


    @BeforeMethod
    public void setUp() throws Exception {
        openMocks(this);
        setCarbonHome();
        setCarbonContextForTenant(SUPER_TENANT_DOMAIN_NAME);

        aiAccessTokenManagerMockedStatic = mockStatic(AIAccessTokenManager.class);
        when(AIAccessTokenManager.getInstance()).thenReturn(mockTokenManager);
        when(mockTokenManager.getAccessToken(false)).thenReturn("testToken");
        when(mockTokenManager.getClientId()).thenReturn("testClientId");

        // Mock HttpAsyncClients.createDefault() to return our mockHttpClient
        httpAsyncClientsMockedStatic = mockStatic(org.apache.http.impl.nio.client.HttpAsyncClients.class);
        httpAsyncClientsMockedStatic.when(org.apache.http.impl.nio.client.HttpAsyncClients::createDefault)
                .thenReturn(mockHttpClient);
    }

    @Test
    public void testExecuteRequest_Success() throws Exception {
        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        mockHttpResponse(HttpStatus.SC_OK, expectedResponse);

        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );

        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        verify(mockHttpClient, times(1)).execute(any(HttpUriRequest.class), any(FutureCallback.class));
    }

    @Test(expectedExceptions = AIClientException.class)
    public void testExecuteRequest_ClientError() throws Exception {
        mockHttpResponse(HttpStatus.SC_BAD_REQUEST, "Bad Request");

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_ServerError() throws Exception {
        mockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test
    public void testExecuteRequest_TokenRenewal() throws Exception {
        String expectedResponse = "{\"result\":\"SUCCESS\"}";
        when(mockTokenManager.getAccessToken(false)).thenReturn("oldToken");
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        mockHttpResponseSequence(
                HttpStatus.SC_UNAUTHORIZED, "Unauthorized",
                HttpStatus.SC_OK, expectedResponse
        );

        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );

        Assert.assertEquals(resultMap.get("result"), "SUCCESS");
        verify(mockHttpClient, times(2)).execute(any(HttpUriRequest.class), any(FutureCallback.class));
        verify(mockTokenManager, times(1)).getAccessToken(true);
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_IOException() throws Exception {
        doAnswer(invocation -> {
            throw new IOException("Simulated IO exception");
        }).when(mockHttpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_ExecutionException() throws Exception {
        Future<HttpResponse> mockFuture = mock(Future.class);
        when(mockFuture.get()).thenThrow(new ExecutionException("Simulated execution exception", new
                RuntimeException()));
        when(mockHttpClient.execute(any(HttpUriRequest.class), any(FutureCallback.class))).thenReturn(mockFuture);

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_InterruptedException() throws Exception {
        Future<HttpResponse> mockFuture = mock(Future.class);
        when(mockFuture.get()).thenThrow(new InterruptedException("Simulated interrupted exception"));
        when(mockHttpClient.execute(any(HttpUriRequest.class), any(FutureCallback.class))).thenReturn(mockFuture);

        try {
            AIHttpClientUtil.executeRequest(
                    "https://ai-service.example.com",
                    "/test-endpoint",
                    HttpGet.class,
                    null
            );
        } finally {
            Assert.assertTrue(Thread.currentThread().isInterrupted(), "Thread should be marked as interrupted");
        }
    }

    @Test
    public void testExecuteRequest_HttpPost() throws Exception {
        String expectedResponse = "{\"result\":\"POST_SUCCESS\"}";
        mockHttpResponse(HttpStatus.SC_OK, expectedResponse);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("key", "value");
        Map<String, Object> resultMap = AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpPost.class,
                requestBody
        );

        Assert.assertEquals(resultMap.get("result"), "POST_SUCCESS");
        verify(mockHttpClient, times(1)).execute(any(HttpPost.class), any(FutureCallback.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExecuteRequest_UnsupportedRequestType() throws Exception {
        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpUriRequest.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_UnauthorizedAfterTokenRenewal() throws Exception {
        when(mockTokenManager.getAccessToken(false)).thenReturn("oldToken");
        when(mockTokenManager.getAccessToken(true)).thenReturn("newToken");

        mockHttpResponseSequence(
                HttpStatus.SC_UNAUTHORIZED, "Unauthorized",
                HttpStatus.SC_UNAUTHORIZED, "Still Unauthorized"
        );

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_JsonParsingError() throws Exception {

        String invalidJson = "{ invalid json }";
        mockHttpResponse(HttpStatus.SC_OK, invalidJson);

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testExecuteRequest_FailedTokenRenewal() throws Exception {

        when(mockTokenManager.getAccessToken(false)).thenReturn("oldToken");
        when(mockTokenManager.getAccessToken(true)).thenReturn(null);

        mockHttpResponse(HttpStatus.SC_UNAUTHORIZED, "Unauthorized");

        AIHttpClientUtil.executeRequest(
                "https://ai-service.example.com",
                "/test-endpoint",
                HttpGet.class,
                null
        );
    }

    @Test
    public void testExecuteRequest_Failed() throws Exception {
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.failed(new Exception("Simulated failure"));
            return null;
        }).when(mockHttpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        try {
            AIHttpClientUtil.executeRequest(
                    "https://ai-service.example.com",
                    "/test-endpoint",
                    HttpGet.class,
                    null
            );
            Assert.fail("Expected AIServerException to be thrown");
        } catch (AIServerException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to get the response from the AI service"));
        }
    }

    @Test
    public void testExecuteRequest_Cancelled() throws Exception {
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.cancelled();
            return null;
        }).when(mockHttpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class));

        try {
            AIHttpClientUtil.executeRequest(
                    "https://ai-service.example.com",
                    "/test-endpoint",
                    HttpGet.class,
                    null
            );
            Assert.fail("Expected AIServerException to be thrown");
        } catch (AIServerException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to get the response from the AI service"));
        }
    }

    private void mockHttpResponse(int statusCode, String responseBody) throws Exception {

        HttpResponse mockResponse = createMockResponse(statusCode, responseBody);
        Future<HttpResponse> mockFuture = mock(Future.class);
        when(mockFuture.get()).thenReturn(mockResponse);
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockResponse);
            return mockFuture;
        }).when(mockHttpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class));
    }

    private void mockHttpResponseSequence(int statusCode1, String responseBody1,
                                          int statusCode2, String responseBody2) throws Exception {

        HttpResponse mockResponse1 = createMockResponse(statusCode1, responseBody1);
        HttpResponse mockResponse2 = createMockResponse(statusCode2, responseBody2);
        Future<HttpResponse> mockFuture1 = mock(Future.class);
        Future<HttpResponse> mockFuture2 = mock(Future.class);
        when(mockFuture1.get()).thenReturn(mockResponse1);
        when(mockFuture2.get()).thenReturn(mockResponse2);
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockResponse1);
            return mockFuture1;
        }).doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockResponse2);
            return mockFuture2;
        }).when(mockHttpClient).execute(any(HttpUriRequest.class), any(FutureCallback.class));
    }

    private HttpResponse createMockResponse(int statusCode, String responseBody) throws Exception {

        HttpResponse mockResponse = new BasicHttpResponse(
                new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), statusCode, ""));
        mockResponse.setEntity(new StringEntity(responseBody));
        return mockResponse;
    }

    private void setCarbonHome() {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    private void setCarbonContextForTenant(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
    }

    @AfterMethod
    public void tearDown() {

        httpAsyncClientsMockedStatic.close();
        aiAccessTokenManagerMockedStatic.close();
        PrivilegedCarbonContext.endTenantFlow();
    }
}
