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

package org.wso2.carbon.ai.service.mgt.token;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for AIAccessTokenManager.
 */
public class AIAccessTokenManagerTest {

    @Mock
    private CloseableHttpAsyncClient mockHttpClient;

    @Mock
    private Future<HttpResponse> mockFuture;

    @Mock
    private HttpResponse mockResponse;

    @Mock
    private StatusLine mockStatusLine;

    private AIAccessTokenManager tokenManager;
    private TestAccessTokenRequestHelper testHelper;
    private AIAccessTokenManager.AccessTokenRequestHelper helper;

    @BeforeMethod
    public void setUp() throws NoSuchFieldException, IllegalAccessException {

        MockitoAnnotations.openMocks(this);
        testHelper = new TestAccessTokenRequestHelper(mockHttpClient);
        String key = Base64.getEncoder().encodeToString("testClientId:testClientSecret".getBytes());
        assignAIKey(key);
        tokenManager = AIAccessTokenManager.getInstance();
        tokenManager.setAccessTokenRequestHelper(testHelper);

        helper = new AIAccessTokenManager.AccessTokenRequestHelper(key, "endpoint", mockHttpClient);
    }

    @AfterMethod
    public void tearDown() {

        // Reset other mocks and state.
        tokenManager = null;
        testHelper = null;
    }

    @Test
    public void testGetInstance() {

        AIAccessTokenManager instance1 = AIAccessTokenManager.getInstance();
        AIAccessTokenManager instance2 = AIAccessTokenManager.getInstance();
        Assert.assertSame(instance1, instance2, "getInstance should always return the same instance");
    }

    @Test
    public void testGetAccessToken_Success() throws Exception {

        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjbGllbnRfaWQiOiJ0ZXN0Q2xpZW50SWQifQ.signature";
        setupMockHttpResponse(HttpStatus.SC_OK, "{\"access_token\":\"" + expectedToken + "\"}");

        String token = tokenManager.getAccessToken(true);
        Assert.assertEquals(token, expectedToken);
        Assert.assertEquals(tokenManager.getClientId(), "testClientId");
    }

    @Test
    public void testGetAccessToken_Renewal() throws Exception {

        setupMockHttpResponse(HttpStatus.SC_OK, "{\"access_token\":\"oldToken\"}");
        String token1 = tokenManager.getAccessToken(false);

        setupMockHttpResponse(HttpStatus.SC_OK, "{\"access_token\":\"newToken\"}");
        String token2 = tokenManager.getAccessToken(true);

        Assert.assertNotEquals(token1, token2, "Tokens should be different after renewal");
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessToken_HttpError() throws Exception {

        setupMockHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        tokenManager.getAccessToken(false);
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessToken_Timeout() throws Exception {

        when(mockHttpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenReturn(mockFuture);
        when(mockFuture.get()).thenThrow(new InterruptedException("Timeout"));
        tokenManager.getAccessToken(true);
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessToken_MaxRetriesExceeded() throws Exception {
        setupMockHttpResponse(HttpStatus.SC_BAD_REQUEST, "Bad Request");
        tokenManager.getAccessToken(false);
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testGetAccessToken_InterruptedDuringRequest() throws Exception {
        when(mockHttpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenAnswer(invocation -> {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Request interrupted");
        });

        try {
            tokenManager.getAccessToken(false);
        } finally {
            Assert.assertTrue(Thread.interrupted(), "Thread interrupt flag should be cleared");
        }
    }

    @Test
    public void completed_InvalidJsonResponse() throws Exception {
        setupMockHttpResponse(HttpStatus.SC_OK, "Invalid JSON");

        try {
            tokenManager.getAccessToken(true);
            Assert.fail("Expected AIServerException to be thrown");
        } catch (AIServerException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to obtain access token after 3 attempts."));
        }
    }

    @Test
    public void testFailedScenario() throws Exception {
        ArgumentCaptor<FutureCallback<HttpResponse>> captor = ArgumentCaptor.forClass(FutureCallback.class);
        doNothing().when(mockHttpClient).start();
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = captor.getValue();
            callback.failed(new Exception("Test Exception"));
            return null;
        }).when(mockHttpClient).execute(any(), captor.capture());

        try {
            helper.requestAccessToken();
        } catch (AIServerException e) {
            assertEquals("Failed to obtain access token after 3 attempts.", e.getMessage());
        }

        verify(mockHttpClient, times(1)).start();
        verify(mockHttpClient, times(3)).execute(any(), any(FutureCallback.class));
    }

    @Test
    public void testCancelledScenario() throws Exception {

        ArgumentCaptor<FutureCallback<HttpResponse>> captor = ArgumentCaptor.forClass(FutureCallback.class);
        doNothing().when(mockHttpClient).start();
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = captor.getValue();
            callback.cancelled();
            return null;
        }).when(mockHttpClient).execute(any(), captor.capture());

        try {
            helper.requestAccessToken();
        } catch (AIServerException e) {
            assertEquals("Failed to obtain access token after 3 attempts.", e.getMessage());
        }

        verify(mockHttpClient, times(1)).start();
        verify(mockHttpClient, times(3)).execute(any(), any(FutureCallback.class));
    }

    @Test(expectedExceptions = AIServerException.class)
    public void testRequestAccessToken_IOException() throws Exception {

        CloseableHttpAsyncClient mockClient = mock(CloseableHttpAsyncClient.class);
        doThrow(new IOException("Test IOException")).when(mockClient).close();

        AIAccessTokenManager.AccessTokenRequestHelper helper =
                new AIAccessTokenManager.AccessTokenRequestHelper("key", "endpoint", mockClient);

        helper.requestAccessToken();
    }

    private void setupMockHttpResponse(int statusCode, String responseBody) throws Exception {

        when(mockHttpClient.execute(any(HttpPost.class), any(FutureCallback.class))).thenAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
            when(mockStatusLine.getStatusCode()).thenReturn(statusCode);
            when(mockResponse.getEntity()).thenReturn(new StringEntity(responseBody));
            callback.completed(mockResponse);
            return mockFuture;
        });
        when(mockFuture.get()).thenReturn(mockResponse);
    }

    // Custom AccessTokenRequestHelper for testing.
    private class TestAccessTokenRequestHelper extends AIAccessTokenManager.AccessTokenRequestHelper {
        public TestAccessTokenRequestHelper(CloseableHttpAsyncClient client) {
            super("testKey", "https://test.endpoint", client);
        }

        @Override
        public String requestAccessToken() throws AIServerException {
            try {
                return super.requestAccessToken();
            } catch (AIServerException e) {
                // Rethrow AIServerException directly for testing purposes.
                throw e;
            } catch (Exception e) {
                throw new AIServerException("Test exception", e);
            }
        }
    }

    private static void assignAIKey(String key) throws NoSuchFieldException, IllegalAccessException {

        // Target class and field.
        Class<?> targetClass = AIAccessTokenManager.class;
        Field aiKeyField = targetClass.getDeclaredField("AI_KEY");

        // Make the field accessible.
        aiKeyField.setAccessible(true);

        // Remove the "final" modifier.
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(aiKeyField, aiKeyField.getModifiers() & ~Modifier.FINAL);

        // Set the new value.
        aiKeyField.set(null, key); // null because it's a static field.
    }
}
