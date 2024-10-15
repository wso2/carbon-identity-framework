package org.wso2.carbon.identity.application.mgt.ai;

import com.google.gson.JsonSyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoginFlowAITokenServiceTest {

    private LoginFlowAITokenService.AccessTokenRequestHelper requestHelper;
    private CloseableHttpAsyncClient mockHttpClient;
    private HttpResponse mockHttpResponse;
    private StatusLine mockStatusLine;

    @BeforeMethod
    public void setUp() {
        mockHttpClient = mock(CloseableHttpAsyncClient.class);
        requestHelper = new LoginFlowAITokenService.AccessTokenRequestHelper("mockKey",
                "https://mock.endpoint", mockHttpClient);
    }

    @Test
    public void testRequestAccessToken_Success() throws Exception {
        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("{\"access_token\":\"mockAccessToken\"}"));

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockHttpResponse);
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        String accessToken = requestHelper.requestAccessToken();

        Assert.assertEquals(accessToken, "mockAccessToken");
    }

    @Test
    public void testRequestAccessToken_SetsClientId() throws Exception {
        String clientId = "mockClientId";
        String header = Base64.getUrlEncoder().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));
        String payload = Base64.getUrlEncoder().encodeToString(("{\"client_id\":\"" + clientId + "\"}")
                .getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getUrlEncoder().encodeToString("dummySignature".getBytes(StandardCharsets.UTF_8));
        String mockAccessToken = String.format("%s.%s.%s", header, payload, signature);

        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getEntity())
                .thenReturn(new StringEntity("{\"access_token\":\"" + mockAccessToken + "\"}"));

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockHttpResponse);
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        LoginFlowAITokenService service = LoginFlowAITokenService.getInstance();
        service.setAccessTokenRequestHelper(requestHelper);

        String accessToken = service.getAccessToken(true);

        Assert.assertEquals(accessToken, mockAccessToken);
        Assert.assertEquals(service.getClientId(), clientId);
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testRequestAccessToken_Non200Response() throws Exception {
        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(401);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("Unauthorized"));

        final int[] executeCount = {0};

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            executeCount[0]++;
            callback.completed(mockHttpResponse);
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        try {
            requestHelper.requestAccessToken();
        } finally {
            Assert.assertEquals(executeCount[0], 3);
        }
    }

    @Test(expectedExceptions = JsonSyntaxException.class)
    public void testRequestAccessToken_ResponseParsingException() throws Exception {
        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("Invalid JSON"));

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockHttpResponse);
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        requestHelper.requestAccessToken();
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testRequestAccessToken_FailedCallback() throws Exception {
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.failed(new IOException("Simulated failure"));
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        requestHelper.requestAccessToken();
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testRequestAccessToken_Timeout() throws Exception {
        doAnswer(invocation -> null).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        requestHelper.requestAccessToken();
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testRequestAccessToken_CancelledCallback() throws Exception {
        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.cancelled();
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        requestHelper.requestAccessToken();
    }

    @Test
    public void testRequestAccessToken_ExceptionOnClientClose() throws Exception {
        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("{\"access_token\":\"mockAccessToken\"}"));

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            callback.completed(mockHttpResponse);
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        doThrow(new IOException("Simulated close exception")).when(mockHttpClient).close();

        String accessToken = requestHelper.requestAccessToken();

        Assert.assertEquals(accessToken, "mockAccessToken");
    }

    @Test
    public void testGetAccessToken_RenewToken() throws Exception {
        LoginFlowAITokenService service = LoginFlowAITokenService.getInstance();
        LoginFlowAITokenService.AccessTokenRequestHelper mockHelper = mock(
                LoginFlowAITokenService.AccessTokenRequestHelper.class);
        service.setAccessTokenRequestHelper(mockHelper);

        when(mockHelper.requestAccessToken()).thenReturn("newAccessToken");

        String accessToken = service.getAccessToken(true);

        Assert.assertEquals(accessToken, "newAccessToken");
    }

    @Test
    public void testGetAccessToken_CachedToken() throws Exception {
        LoginFlowAITokenService service = LoginFlowAITokenService.getInstance();
        LoginFlowAITokenService.AccessTokenRequestHelper mockHelper = mock(
                LoginFlowAITokenService.AccessTokenRequestHelper.class);
        service.setAccessTokenRequestHelper(mockHelper);

        when(mockHelper.requestAccessToken()).thenReturn("cachedAccessToken");

        String accessToken1 = service.getAccessToken(false);
        String accessToken2 = service.getAccessToken(false);

        verify(mockHelper, times(1)).requestAccessToken();

        Assert.assertEquals(accessToken1, "cachedAccessToken");
        Assert.assertEquals(accessToken2, "cachedAccessToken");
    }

    @Test
    public void testRequestAccessToken_RetrySuccess() throws Exception {
        mockHttpResponse = mock(HttpResponse.class);
        mockStatusLine = mock(StatusLine.class);
        when(mockHttpResponse.getEntity()).thenReturn(new StringEntity("{\"access_token\":\"mockAccessToken\"}"));

        when(mockStatusLine.getStatusCode())
                .thenReturn(500)
                .thenReturn(200);

        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);

        final int[] executeCount = {0};

        doAnswer(invocation -> {
            FutureCallback<HttpResponse> callback = invocation.getArgument(1);
            executeCount[0]++;

            if (executeCount[0] == 1) {
                callback.completed(mockHttpResponse);
            } else if (executeCount[0] == 2) {
                callback.completed(mockHttpResponse);
            }
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        String accessToken = requestHelper.requestAccessToken();

        Assert.assertEquals(accessToken, "mockAccessToken");
        Assert.assertEquals(executeCount[0], 2);
    }

    @Test(expectedExceptions = LoginFlowAIServerException.class)
    public void testRequestAccessToken_TimeoutRetries() throws Exception {
        final int[] executeCount = {0};

        doAnswer(invocation -> {
            executeCount[0]++;
            return null;
        }).when(mockHttpClient).execute(any(), any(FutureCallback.class));

        try {
            requestHelper.requestAccessToken();
        } finally {
            Assert.assertEquals(executeCount[0], 3);
        }
    }

    @Test
    public void testSetAccessTokenRequestHelper() {
        LoginFlowAITokenService service = LoginFlowAITokenService.getInstance();
        LoginFlowAITokenService.AccessTokenRequestHelper helper = new LoginFlowAITokenService
                .AccessTokenRequestHelper("key", "endpoint", mockHttpClient);

        service.setAccessTokenRequestHelper(helper);

        LoginFlowAITokenService.AccessTokenRequestHelper mockHelper = mock(
                LoginFlowAITokenService.AccessTokenRequestHelper.class);
        service.setAccessTokenRequestHelper(mockHelper);
        try {
            when(mockHelper.requestAccessToken()).thenReturn("testToken");
            String token = service.getAccessToken(true);
            Assert.assertEquals(token, "testToken");
        } catch (LoginFlowAIServerException e) {
            Assert.fail("Exception should not be thrown");
        }
    }
}
