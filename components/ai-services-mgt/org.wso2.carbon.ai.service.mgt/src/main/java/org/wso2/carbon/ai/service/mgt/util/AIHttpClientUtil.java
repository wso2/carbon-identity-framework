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
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.ERROR_RETRIEVING_ACCESS_TOKEN;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.ai.service.mgt.constants.AIConstants.ErrorMessages.UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN;

/**
 * Utility class for AI Services to send HTTP requests.
 */
public class AIHttpClientUtil {

    private static final Log LOG = LogFactory.getLog(AIHttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

        try (CloseableHttpAsyncClient client = HttpAsyncClients.createDefault()) {
            client.start();
            String accessToken = AIAccessTokenManager.getInstance().getAccessToken(false);
            String orgName = AIAccessTokenManager.getInstance().getClientId();

            HttpUriRequest request = createRequest(aiServiceEndpoint + "/t/" + orgName + path, requestType,
                    accessToken, requestBody);
            HttpResponseWrapper aiServiceResponse = executeRequestWithRetry(client, request);

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
            Thread.currentThread().interrupt();
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

        request.setHeader("Authorization", "Bearer " + accessToken);
        request.setHeader("Content-Type", "application/json");
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
            request.setHeader("Authorization", "Bearer " + newAccessToken);
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
