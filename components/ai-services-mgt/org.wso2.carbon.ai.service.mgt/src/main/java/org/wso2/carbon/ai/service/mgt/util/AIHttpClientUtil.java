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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.axis2.transport.http.HTTPConstants.HEADER_CONTENT_TYPE;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.CONTENT_TYPE_JSON;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.ERROR_RETRIEVING_ACCESS_TOKEN;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.HTTP_BEARER;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.TENANT_CONTEXT_PREFIX;

/**
 * Utility class for AI Services to send HTTP requests.
 */
public class AIHttpClientUtil {

    private static final Log LOG = LogFactory.getLog(AIHttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int HTTP_CONNECTION_POOL_SIZE = IdentityUtil.getProperty(
            HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME) != null ? Integer.parseInt(IdentityUtil.getProperty(
                    HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME)) : 20;


    // Singleton instance of CloseableHttpAsyncClient with connection pooling.
    private static final CloseableHttpAsyncClient httpClient;

    static {
        // Configure the IO reactor.
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .build();
        ConnectingIOReactor ioReactor;
        try {
            // Create the IO reactor.
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOException e) {
            throw new RuntimeException("Error initializing IO Reactor", e);
        }
        // Create a connection manager with the IO reactor.
        PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(ioReactor);
        // Maximum total connections.
        connectionManager.setMaxTotal(HTTP_CONNECTION_POOL_SIZE);
        // Initialize the HttpClient with the connection manager.
        httpClient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        // Start the HttpClient.
        httpClient.start();
        // Add a shutdown hook to close the client when the application stops.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOG.error("Error while shutting down HTTP client: " + e.getMessage());
            }
        }));
    }



    /**
     * Execute a request to the AI service.
     *
     * @param path              The endpoint to which the request should be sent.
     * @param requestType       The type of the request (GET, POST).
     * @param requestBody       The request body(Only for POST requests).
     * @param aiServiceEndpoint The endpoint of the AI service.
     * @return The response from the AI service as a map.
     * @throws AIServerException If a server error occurred while accessing the AI service.
     * @throws AIClientException If a client error occurred while accessing the AI service.
     */
    public static Map<String, Object> executeRequest(String aiServiceEndpoint, String path,
                                                     Class<? extends HttpUriRequest> requestType, Object requestBody)
            throws AIServerException, AIClientException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try {
            String accessToken = AIAccessTokenManager.getInstance().getAccessToken(false);
            String orgName = AIAccessTokenManager.getInstance().getClientId();

            HttpUriRequest request = createRequest(aiServiceEndpoint + TENANT_CONTEXT_PREFIX + orgName + path,
                    requestType, accessToken, requestBody);
            HttpResponseWrapper aiServiceResponse = executeRequestWithRetry(httpClient, request);

            int statusCode = aiServiceResponse.getStatusCode();
            String responseBody = aiServiceResponse.getResponseBody();

            if (statusCode >= 400) {
                handleErrorResponse(statusCode, responseBody, tenantDomain);
            }
            return convertJsonStringToMap(responseBody);
        } catch (IOException | ExecutionException e) {
            throw new AIServerException("An error occurred while connecting to the AI Service.",
                    SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode(), e);
        } catch (InterruptedException e) {
            // Restore the interrupted status of the thread to ensure it is not swallowed
            // and can be handled appropriately by other parts of the program. This is
            // important for proper thread coordination and graceful shutdown in a
            // multithreaded environment.
            Thread.currentThread().interrupt();

            // Wrap and rethrow the exception as a custom AIServerException to provide
            // a meaningful error message and maintain the original exception for debugging.
            throw new AIServerException("An error occurred while connecting to the AI Service.",
                    SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode(), e);
        }
    }

    private static HttpUriRequest createRequest(String url, Class<? extends HttpUriRequest> requestType,
                                                String accessToken, Object requestBody) throws IOException {

        HttpUriRequest request;
        if (requestType == HttpPost.class) {
            HttpPost post = new HttpPost(url);
            if (requestBody != null) {
                post.setEntity(new StringEntity(objectMapper.writeValueAsString(requestBody)));
            }
            request = post;
        } else if (requestType == HttpGet.class) {
            request = new HttpGet(url);
        } else {
            throw new IllegalArgumentException("Unsupported request type: " + requestType.getName());
        }

        request.setHeader(AUTHORIZATION, HTTP_BEARER + " " + accessToken);
        request.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        return request;
    }

    protected static HttpResponseWrapper executeRequestWithRetry(CloseableHttpAsyncClient client,
                                                                 HttpUriRequest request)
            throws InterruptedException, ExecutionException, IOException, AIServerException {

        HttpResponseWrapper response = executeHttpRequest(client, request);

        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            String newAccessToken = AIAccessTokenManager.getInstance().getAccessToken(true);
            if (newAccessToken == null) {
                throw new AIServerException("Failed to renew access token.", ERROR_RETRIEVING_ACCESS_TOKEN.getCode());
            }
            request.setHeader(AUTHORIZATION, "Bearer " + newAccessToken);
            response = executeHttpRequest(client, request);
        }

        return response;
    }

    private static void handleErrorResponse(int statusCode, String responseBody, String tenantDomain)
            throws AIServerException, AIClientException {

        if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
            throw new AIServerException("Failed to access AI service with renewed access token for " +
                    "the tenant domain: " + tenantDomain,
                    UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN.getCode());
        } else if (statusCode >= 400 && statusCode < 500) {
            throw new AIClientException(new HttpResponseWrapper(statusCode, responseBody),
                    "Client error occurred from tenant: " + tenantDomain + " with status code: '" + statusCode
                            + "' while accessing AI service.", CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode());
        } else if (statusCode >= 500) {
            throw new AIServerException(new HttpResponseWrapper(statusCode, responseBody),
                    "Server error occurred from tenant: " + tenantDomain + " with status code: '" + statusCode
                            + "' while accessing AI service.", SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode());
        }
    }

    private static Map<String, Object> convertJsonStringToMap(String jsonString) throws AIServerException {

        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            throw new AIServerException("Error occurred while parsing the JSON response from the AI service.", e);
        }
    }

    protected static HttpResponseWrapper executeHttpRequest(CloseableHttpAsyncClient client, HttpUriRequest httpRequest)
            throws InterruptedException, ExecutionException, IOException, AIServerException {

        Future<HttpResponse> apiResponse = client.execute(httpRequest, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {

                LOG.info("API request completed with status code: " + response.getStatusLine().getStatusCode());
            }

            @Override
            public void failed(Exception e) {

                LOG.error("API request failed: " + e.getMessage(), e);
            }

            @Override
            public void cancelled() {

                LOG.warn("API request was cancelled");
            }
        });
        if (apiResponse == null) {
            throw new AIServerException("Unable to get the response from the AI service.",
                    SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode());
        }
        HttpResponse httpResponse = apiResponse.get();
        int status = httpResponse.getStatusLine().getStatusCode();
        String response = EntityUtils.toString(httpResponse.getEntity());
        return new HttpResponseWrapper(status, response);
    }

    /**
     * Wrapper class for HTTP response.
     */
    public static class HttpResponseWrapper implements Serializable {

        private final int statusCode;
        private final String responseBody;

        public HttpResponseWrapper(int statusCode, String responseBody) {

            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        public int getStatusCode() {

            return statusCode;
        }

        public String getResponseBody() {

            return responseBody;
        }
    }
}
