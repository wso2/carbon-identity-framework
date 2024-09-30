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
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * This class is responsible for making API calls to the external services.
 */
public class APIClient {

    private static final Log LOG = LogFactory.getLog(APIClient.class);
    private final CloseableHttpClient httpClient;

    public APIClient() {

        // todo: read connection configurations related to the http client of actions from the server configuration.
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

    public ActionInvocationResponse callAPI(String url, AuthMethods.AuthMethod authMethod,
                                            String payload) {

        HttpPost httpPost = new HttpPost(url);
        setRequestEntity(httpPost, payload, authMethod);

        return executeRequest(httpPost);
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

    private ActionInvocationResponse executeRequest(HttpPost request) {

        int attempts = 0;
        int retryCount = ActionExecutorConfig.getInstance().getHttpRequestRetryCount();
        ActionInvocationResponse actionInvocationResponse = null;

        while (attempts < retryCount) {
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                actionInvocationResponse = handleResponse(response);
                if (!actionInvocationResponse.isError() || !actionInvocationResponse.isRetry()) {
                    return actionInvocationResponse;
                }
                //todo: add to diagnostic logs
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionConstants.LogConstants.ACTION_EXECUTION,
                            ActionExecutionConstants.LogConstants.ActionIDs.SEND_ACTION_REQUEST);
                    diagLogBuilder
                            .resultMessage("External endpoint " + request.getURI() + " for action execution seems to " +
                                    "be unavailable. Retrying api call attempt " + (attempts + 1) + " of "
                                    + retryCount + ".")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
                LOG.debug("API: " + request.getURI() + " seems to be unavailable. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (ConnectTimeoutException | SocketTimeoutException e) {
                //todo: add to diagnostic logs
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionConstants.LogConstants.ACTION_EXECUTION,
                            ActionExecutionConstants.LogConstants.ActionIDs.SEND_ACTION_REQUEST);
                    diagLogBuilder
                            .resultMessage("Request for external endpont " + request.getURI() + " for action is " +
                                    "timed out. Retrying api call attempt " + (attempts + 1) + " of "
                                    + retryCount + ".")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
                LOG.debug("Request for API: " + request.getURI() + " timed out. Retrying the request. Attempt " +
                        (attempts + 1) + " of " + retryCount);
            } catch (Exception e) {
                //todo: add to diagnostic logs
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                            ActionExecutionConstants.LogConstants.ACTION_EXECUTION,
                            ActionExecutionConstants.LogConstants.ActionIDs.SEND_ACTION_REQUEST);
                    diagLogBuilder
                            .resultMessage("Request for external endpoint " + request.getURI() + " for action failed" +
                                    " due to an error.")
                            .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                            .resultStatus(DiagnosticLog.ResultStatus.FAILED)
                            .build();
                    LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
                }
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

    private ActionInvocationResponse handleResponse(HttpResponse response) {

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        ActionInvocationResponse.Builder actionInvocationResponseBuilder = new ActionInvocationResponse.Builder();

        switch (statusCode) {
            case HttpStatus.SC_OK:
                handleSuccess(actionInvocationResponseBuilder, responseEntity, statusCode);
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
                actionInvocationResponseBuilder.errorLog(
                        "Failed to execute the action request for status code " + statusCode + ".");
                actionInvocationResponseBuilder.retry(true);
                break;
            default:
                actionInvocationResponseBuilder.errorLog("Unexpected response received for status code " + statusCode
                        + ".");
                break;
        }

        return actionInvocationResponseBuilder.build();
    }

    private void handleSuccess(ActionInvocationResponse.Builder builder, HttpEntity entity, int statusCode) {

        try {
            builder.response(handleSuccessResponse(entity));
        } catch (ActionInvocationException e) {
            builder.errorLog("Unexpected error occured on action execution response for status code " + statusCode
                    + ". " + e.getMessage());
        }
    }

    private void handleClientError(ActionInvocationResponse.Builder builder, HttpEntity entity, int statusCode) {

        try {
            ActionInvocationErrorResponse errorResponse = handleErrorResponse(entity);
            if (errorResponse != null) {
                builder.response(errorResponse);
            } else {
                builder.errorLog("Failed to execute the action request for status code " + statusCode
                        + " due to no error response is available.");
            }
        } catch (ActionInvocationException e) {
            //todo: add to diagnostic logs
            LOG.debug("JSON payload received for status code: " + statusCode +
                    " is not of the expected error response format. ", e);
            builder.errorLog("Failed to execute the action request for status code " + statusCode + ". "
                    + e.getMessage());
        }
    }

    private void handleServerError(ActionInvocationResponse.Builder builder, HttpEntity entity, int statusCode) {

        try {
            ActionInvocationErrorResponse errorResponse = handleErrorResponse(entity);
            if (errorResponse != null) {
                builder.response(errorResponse);
            } else {
                builder.errorLog("Failed to execute the action request for status code " + statusCode
                        + " due to no error response is available.");
                builder.retry(true);
            }
        } catch (ActionInvocationException e) {
            //todo: add to diagnostic logs
            LOG.debug("JSON payload received for status code: " + statusCode +
                    " is not of the expected error response format. ", e);
            builder.errorLog("Failed to execute the action request for status code " + statusCode + ". "
                    + e.getMessage());
            builder.retry(true);
        }
    }

    private ActionInvocationSuccessResponse handleSuccessResponse(HttpEntity responseEntity)
            throws ActionInvocationException {

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
            //todo: It is good to log the parsing error for detailed troubleshooting for the extension developer.
            // It's not a good idea to expose the error as that disclose internal details,
            // so better log against a trace id.
            throw new ActionInvocationException("Parsing JSON response failed.", e);
        }
    }

    private boolean isAcceptablePayload(HttpEntity responseEntity) {

        return responseEntity != null && responseEntity.getContentType() != null &&
                responseEntity.getContentType().getValue().contains("application/json");
    }
}
