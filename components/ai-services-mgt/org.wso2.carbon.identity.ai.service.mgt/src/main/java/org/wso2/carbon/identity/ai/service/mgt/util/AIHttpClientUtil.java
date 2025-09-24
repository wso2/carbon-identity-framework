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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.ai.service.mgt.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.ai.service.mgt.token.AIAccessTokenManager;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.Map;

import static org.apache.axis2.transport.http.HTTPConstants.HEADER_CONTENT_TYPE;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.CONTENT_TYPE_JSON;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_HTTP_CONNECTION_POOL_SIZE;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_HTTP_CONNECTION_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.DEFAULT_HTTP_SOCKET_TIMEOUT;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ErrorMessages.ERROR_RETRIEVING_ACCESS_TOKEN;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.ErrorMessages.UNABLE_TO_ACCESS_AI_SERVICE_WITH_RENEW_ACCESS_TOKEN;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_BEARER;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_CONNECTION_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.HTTP_SOCKET_TIMEOUT_PROPERTY_NAME;
import static org.wso2.carbon.identity.ai.service.mgt.constants.AIConstants.TENANT_CONTEXT_PREFIX;

/**
 * Utility class for AI Services to send HTTP requests.
 */
public class AIHttpClientUtil {

    private static final Log LOG = LogFactory.getLog(AIHttpClientUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int HTTP_CONNECTION_POOL_SIZE = readIntProperty(HTTP_CONNECTION_POOL_SIZE_PROPERTY_NAME,
            DEFAULT_HTTP_CONNECTION_POOL_SIZE);
    private static final int HTTP_CONNECTION_TIMEOUT = readIntProperty(HTTP_CONNECTION_TIMEOUT_PROPERTY_NAME,
            DEFAULT_HTTP_CONNECTION_TIMEOUT);
    private static final int HTTP_CONNECTION_REQUEST_TIMEOUT = readIntProperty(
            HTTP_CONNECTION_REQUEST_TIMEOUT_PROPERTY_NAME, DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT);
    private static final int HTTP_SOCKET_TIMEOUT = readIntProperty(HTTP_SOCKET_TIMEOUT_PROPERTY_NAME,
            DEFAULT_HTTP_SOCKET_TIMEOUT);
    private static final String USER_AGENT_VALUE = "IAM-AI-Services";

    // Singleton instance of CloseableHttpClient with connection pooling.
    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setMaxConnTotal(HTTP_CONNECTION_POOL_SIZE)
            .setDefaultRequestConfig(
                    org.apache.http.client.config.RequestConfig.custom()
                            .setSocketTimeout(HTTP_SOCKET_TIMEOUT)
                            .setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
                            .setConnectionRequestTimeout(HTTP_CONNECTION_REQUEST_TIMEOUT)
                            .build()
            ).build();

    private AIHttpClientUtil() {

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
            String clientId = AIAccessTokenManager.getInstance().getClientId();

            HttpUriRequest request = createRequest(aiServiceEndpoint + TENANT_CONTEXT_PREFIX + clientId + path,
                    requestType, accessToken, requestBody);
            HttpResponseWrapper aiServiceResponse = executeRequestWithRetry(request);

            int statusCode = aiServiceResponse.getStatusCode();
            String responseBody = aiServiceResponse.getResponseBody();

            if (statusCode >= 400) {
                handleErrorResponse(statusCode, responseBody, tenantDomain);
            }
            return convertJsonStringToMap(responseBody);
        } catch (IOException e) {
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

    private static HttpResponseWrapper executeRequestWithRetry(HttpUriRequest request)
            throws IOException, AIServerException {

        HttpResponseWrapper response = executeHttpRequest(request);

        if (response.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            String newAccessToken = AIAccessTokenManager.getInstance().getAccessToken(true);
            if (newAccessToken == null) {
                throw new AIServerException("Failed to renew access token.", ERROR_RETRIEVING_ACCESS_TOKEN.getCode());
            }
            request.setHeader(AUTHORIZATION, HTTP_BEARER + " " + newAccessToken);
            response = executeHttpRequest(request);
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
            throw new AIClientException("Client error occurred from tenant: " + tenantDomain + " with status code: '"
                    + statusCode + "' while accessing AI service.",
                    CLIENT_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode(), statusCode, responseBody);
        } else if (statusCode >= 500) {
            throw new AIServerException("Server error occurred from tenant: " + tenantDomain + " with status code: '"
                    + statusCode + "' while accessing AI service.",
                    SERVER_ERROR_WHILE_CONNECTING_TO_AI_SERVICE.getCode(), statusCode, responseBody);
        }
    }

    private static Map<String, Object> convertJsonStringToMap(String jsonString) throws AIServerException {

        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            throw new AIServerException("Error occurred while parsing the JSON response from the AI service.", e);
        }
    }

    private static HttpResponseWrapper executeHttpRequest(HttpUriRequest httpRequest)
            throws IOException {

        // Here we don't close the client connection since we are using a connection pool.
        httpRequest.addHeader("User-Agent", USER_AGENT_VALUE);
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        int status = httpResponse.getStatusLine().getStatusCode();
        String response = EntityUtils.toString(httpResponse.getEntity());
        return new HttpResponseWrapper(status, response);
    }

    private static int readIntProperty(String key, int defaultValue) {

        String value = IdentityUtil.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Wrapper class for HTTP response.
     */
    public static class HttpResponseWrapper {

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
