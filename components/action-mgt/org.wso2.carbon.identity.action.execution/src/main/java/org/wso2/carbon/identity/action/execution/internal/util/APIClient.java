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

package org.wso2.carbon.identity.action.execution.internal.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import org.wso2.carbon.identity.action.execution.api.exception.ActionInvocationException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.ResponseData;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ResponseDataDeserializer;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * This class is responsible for making API calls to the external services.
 */
public class APIClient {

    private static final Log LOG = LogFactory.getLog(APIClient.class);
    private static final ActionExecutionDiagnosticLogger DIAGNOSTIC_LOGGER = new ActionExecutionDiagnosticLogger();
    private static final String ACTION_STATUS = "actionStatus";
    private final CloseableHttpClient httpClient;

    public APIClient() {

        // Initialize the http client. Set connection time out to 2s and read time out to 5s.
        int readTimeout = ActionExecutorConfig.getInstance().getHttpReadTimeoutInMillis();
        int connectionRequestTimeout = ActionExecutorConfig.getInstance().getHttpConnectionRequestTimeoutInMillis();
        int connectionTimeout = ActionExecutorConfig.getInstance().getHttpConnectionTimeoutInMillis();

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(readTimeout)
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(ActionExecutorConfig.getInstance().getHttpConnectionPoolSize());
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(connectionManager)
                .build();
    }

    public ActionInvocationResponse callAPI(ActionType actionType, String url, AuthMethods.AuthMethod authMethod,
                                            String payload) {

        HttpPost httpPost = new HttpPost(url);
        setRequestEntity(httpPost, payload, authMethod, actionType);

        return executeRequest(actionType, httpPost);
    }

    private void setRequestEntity(HttpPost httpPost, String jsonRequest, AuthMethods.AuthMethod authMethod,
                                  ActionType actionType) {

        StringEntity entity = new StringEntity(jsonRequest, StandardCharsets.UTF_8);
        if (authMethod != null) {
            authMethod.applyAuth(httpPost);
        }
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader(ActionAPIVersionResolver.API_VERSION_HEADER,
                ActionAPIVersionResolver.resolveAPIVersion(actionType));
    }

    private ActionInvocationResponse executeRequest(ActionType actionType, HttpPost request) {

        int attempts = 0;
        int retryCount = ActionExecutorConfig.getInstance().getHttpRequestRetryCount();
        ActionInvocationResponse actionInvocationResponse = null;

        while (attempts < retryCount) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                actionInvocationResponse = handleResponse(actionType, response);
                if (!actionInvocationResponse.isError() || !actionInvocationResponse.isRetry()) {
                    return actionInvocationResponse;
                }
                DIAGNOSTIC_LOGGER.logAPICallRetry(request, attempts + 1, retryCount);
                LOG.debug("API: " + request.getURI() + " seems to be unavailable. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                DIAGNOSTIC_LOGGER.logAPICallTimeout(request, attempts + 1, retryCount);
                LOG.debug("Request for API: " + request.getURI() + " timed out. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (Exception e) {
                DIAGNOSTIC_LOGGER.logAPICallError(request);
                LOG.error("Request for API: " + request.getURI() + " failed due to an error.", e);
                break;
            } finally {
                request.releaseConnection();
            }
            attempts++;
        }

        LOG.warn("Maximum retry attempts reached for API: " + request.getURI());
        return actionInvocationResponse != null ? actionInvocationResponse : new ActionInvocationResponse.Builder()
                .errorLog("Failed to execute the action request or maximum retry attempts reached.").build();
    }

    private ActionInvocationResponse handleResponse(ActionType actionType, HttpResponse response) {

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        ActionInvocationResponse.Builder actionInvocationResponseBuilder = new ActionInvocationResponse.Builder();

        switch (statusCode) {
            case HttpStatus.SC_OK:
                handleSuccessOrFailure(actionType, actionInvocationResponseBuilder, responseEntity, statusCode);
                break;
            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_UNAUTHORIZED:
                handleClientError(actionInvocationResponseBuilder, responseEntity, statusCode);
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                handleServerError(actionInvocationResponseBuilder, responseEntity, statusCode);
                break;
            case HttpStatus.SC_BAD_GATEWAY:
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
            case HttpStatus.SC_GATEWAY_TIMEOUT:
                handleServerError(actionInvocationResponseBuilder, responseEntity, statusCode);
                actionInvocationResponseBuilder.retry(true);
                break;
            default:
                actionInvocationResponseBuilder.errorLog("Unexpected response received with status code: " + statusCode
                        + ".");
                break;
        }

        return actionInvocationResponseBuilder.build();
    }

    private void handleSuccessOrFailure(ActionType actionType, ActionInvocationResponse.Builder builder,
                                        HttpEntity entity, int statusCode) {

        try {
            builder.response(handleSuccessOrFailureResponse(actionType, entity));
        } catch (ActionInvocationException e) {
            builder.errorLog("Unexpected response for status code: " + statusCode + ". " + e.getMessage());
        }
    }

    private void handleClientError(ActionInvocationResponse.Builder builder, HttpEntity entity, int statusCode) {

        try {
            ActionInvocationResponse.APIResponse errorResponse = handleErrorResponse(entity);
            if (errorResponse != null) {
                builder.response(errorResponse);
            } else {
                builder.errorLog("Failed to execute the action request. Received status code: " + statusCode + ".");
            }
        } catch (ActionInvocationException e) {
            LOG.debug("JSON payload received for status code: " + statusCode +
                    " is not of the expected error response format. ", e);
            builder.errorLog("Unexpected error response received for the status code: " + statusCode + ". "
                    + e.getMessage());
        }
    }

    private void handleServerError(ActionInvocationResponse.Builder builder, HttpEntity entity, int statusCode) {

        try {
            ActionInvocationResponse.APIResponse errorResponse = handleErrorResponse(entity);
            if (errorResponse != null) {
                builder.response(errorResponse);
            } else {
                builder.errorLog("Failed to execute the action request. Received status code: " + statusCode + ".");
                builder.retry(true);
            }
        } catch (ActionInvocationException e) {
            LOG.debug("JSON payload received for status code: " + statusCode +
                    " is not of the expected error response format. ", e);
            builder.errorLog("Unexpected error response received for the status code: " + statusCode + ". "
                    + e.getMessage());
            builder.retry(true);
        }
    }

    private ActionInvocationResponse.APIResponse handleSuccessOrFailureResponse(ActionType actionType,
                                                                                HttpEntity responseEntity)
            throws ActionInvocationException {

        return deserializeSuccessOrFailureResponse(actionType, responseEntity);
    }

    private ActionInvocationResponse.APIResponse handleErrorResponse(HttpEntity responseEntity)
            throws ActionInvocationException {

        // If an error response is received, return the error response in order to communicate back to the client.
        if (isAcceptablePayload(responseEntity)) {
            return deserialize(responseEntity, ActionInvocationErrorResponse.class);
        }
        return null;
    }

    private String validateJsonResponse(HttpEntity responseEntity) throws ActionInvocationException {

        if (!isAcceptablePayload(responseEntity)) {
            throw new ActionInvocationException("The response content type is not application/json.");
        }

        try {
            return EntityUtils.toString(responseEntity);
        } catch (IOException e) {
            throw new ActionInvocationException("Reading JSON response failed.", e);
        }
    }

    private ActionInvocationResponse.APIResponse deserializeSuccessOrFailureResponse(ActionType actionType,
                                                                                     HttpEntity responseEntity)
            throws ActionInvocationException {

        try {
            String jsonResponse = validateJsonResponse(responseEntity);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            String actionStatus = rootNode.path(ACTION_STATUS).asText();
            if (actionStatus.isEmpty()) {
                throw new ActionInvocationException("Reading JSON response failed.");
            }
            if (actionStatus.equals(ActionExecutionStatus.Status.SUCCESS.name())) {
                // Configure dynamic deserializer for the extended ResponseData class based on the action type.
                SimpleModule module = new SimpleModule();
                module.addDeserializer(ResponseData.class, new ResponseDataDeserializer());
                objectMapper.setConfig(objectMapper.getDeserializationConfig()
                        .withAttribute(ResponseDataDeserializer.ACTION_TYPE_ATTR_NAME, actionType));
                objectMapper.registerModule(module);
                return objectMapper.readValue(jsonResponse, ActionInvocationSuccessResponse.class);
            } else if (actionStatus.equals(ActionExecutionStatus.Status.INCOMPLETE.name())) {
                return objectMapper.readValue(jsonResponse, ActionInvocationIncompleteResponse.class);
            } else {
                return objectMapper.readValue(jsonResponse, ActionInvocationFailureResponse.class);
            }
        } catch (IOException e) {
            throw new ActionInvocationException("Reading JSON response failed.", e);
        }
    }

    private <T> T deserialize(HttpEntity responseEntity, Class<T> returnType) throws ActionInvocationException {

        try {
            String jsonResponse = validateJsonResponse(responseEntity);
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonResponse, returnType);
        } catch (IOException e) {
            throw new ActionInvocationException("Parsing JSON response failed.", e);
        }
    }

    private boolean isAcceptablePayload(HttpEntity responseEntity) {

        return responseEntity != null && responseEntity.getContentType() != null &&
                responseEntity.getContentType().getValue().contains("application/json");
    }
}
