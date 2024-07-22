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

package org.wso2.carbon.identity.action.execution.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.action.execution.exception.ActionInvocationException;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationSuccessResponse;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * This class is responsible for invoking the HTTP endpoint.
 */
public class APIClient {

    private static final Log log = LogFactory.getLog(APIClient.class);
    private final CloseableHttpClient httpClient;

    public APIClient() {

        // todo: read connection configurations related to the http client of actions from the server configuration.
        // Initialize the http client. Set connection time out to 2s and read time out to 5s.
        int readTimeout = 5000;
        int connectionRequestTimeout = 2000;
        int connectionTimeout = 2000;

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(readTimeout)
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(connectionManager)
                .build();
    }

    public ActionInvocationResponse callAPI(String url, AuthMethods.AuthMethod authMethod,
                                            String payload) {

        HttpPost httpPost = new HttpPost(url);
        setRequestEntity(httpPost, payload, authMethod);

        return executeRequest(httpPost).orElse(new ActionInvocationResponse.Builder()
                .setErrorLog("Failed to execute the action request or maximum retry attempts reached.")
                .build());
    }

    private void setRequestEntity(HttpPost httpPost, String jsonRequest, AuthMethods.AuthMethod authMethod) {

        StringEntity entity = new StringEntity(jsonRequest, StandardCharsets.UTF_8);
        if (authMethod != null) {
            authMethod.applyAuth(httpPost);
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }

    private Optional<ActionInvocationResponse> executeRequest(HttpPost request) {

        int attempts = 0;
        int retryCount = 2; // todo: read from server configurations

        while (attempts < retryCount) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                ActionInvocationResponse actionInvocationResponse = handleResponse(response);
                if (!actionInvocationResponse.isError() || !actionInvocationResponse.isRetry()) {
                    return Optional.of(actionInvocationResponse);
                }
                //todo: add to diagnostic logs
                log.warn("API: " + request.getURI() + " seems to be unavailable. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                //todo: add to diagnostic logs
                log.warn("Request for API: " + request.getURI() + " timed out. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (Exception e) {
                //todo: add to diagnostic logs
                log.error("Request for API: " + request.getURI() + " failed due to an error.", e);
                break;
            } finally {
                request.releaseConnection();
            }
            attempts++;
        }

        log.warn("Maximum retry attempts reached for API: " + request.getURI());
        return Optional.empty();
    }

    private ActionInvocationResponse handleResponse(HttpResponse response) {

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        ActionInvocationResponse.Builder actionInvocationResponseBuilder = new ActionInvocationResponse.Builder();
        actionInvocationResponseBuilder.setHttpStatusCode(statusCode);

        try {
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    ActionInvocationSuccessResponse successResponse = handleSuccessResponse(responseEntity);
                    actionInvocationResponseBuilder.setResponse(successResponse);
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    ActionInvocationErrorResponse errorResponse = handleErrorResponse(responseEntity);
                    actionInvocationResponseBuilder.setResponse(errorResponse);
                    break;
                case HttpStatus.SC_UNAUTHORIZED:
                    break;
                case HttpStatus.SC_BAD_GATEWAY:
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                case HttpStatus.SC_GATEWAY_TIMEOUT:
                    actionInvocationResponseBuilder.setRetry(true);
                    break;
                default:
                    throw new ActionInvocationException("Unexpected response status code: " + statusCode);
            }
        } catch (ActionInvocationException e) {
            // Set error in response to be logged at diagnostic logs for troubleshooting.
            actionInvocationResponseBuilder.setErrorLog("Unexpected response. Error: " + e.getMessage());
        }

        return actionInvocationResponseBuilder.build();
    }

    private ActionInvocationSuccessResponse handleSuccessResponse(HttpEntity responseEntity)
            throws ActionInvocationException {

        if (!isAcceptablePayload(responseEntity)) {
            throw new ActionInvocationException("The response content type is not application/json.");
        }
        return deserialize(responseEntity, ActionInvocationSuccessResponse.class);
    }

    private ActionInvocationErrorResponse handleErrorResponse(HttpEntity responseEntity)
            throws ActionInvocationException {

        // If an error response is received, return the error response in order to communicate back to the client.
        if (isAcceptablePayload(responseEntity)) {
            return deserialize(responseEntity, ActionInvocationErrorResponse.class);
        }
        return null;
    }

    private <T> T deserialize(HttpEntity responseEntity, Class<T> returnType) throws ActionInvocationException {

        if (!isAcceptablePayload(responseEntity)) {
            throw new ActionInvocationException("The response content type is not application/json.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonResponse = EntityUtils.toString(responseEntity);
            return objectMapper.readValue(jsonResponse, returnType);
        } catch (IOException e) {
            throw new ActionInvocationException("Error parsing the JSON response.", e);
        }
    }

    private boolean isAcceptablePayload(HttpEntity responseEntity) {

        return responseEntity != null && responseEntity.getContentType() != null &&
                responseEntity.getContentType().getValue().contains("application/json");
    }
}
