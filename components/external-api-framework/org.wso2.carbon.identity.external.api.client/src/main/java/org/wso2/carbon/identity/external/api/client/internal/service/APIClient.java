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
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.external.api.client.internal.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage;
import org.wso2.carbon.identity.external.api.client.api.exception.APIClientInvocationException;
import org.wso2.carbon.identity.external.api.client.api.model.APIClientConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIInvocationConfig;
import org.wso2.carbon.identity.external.api.client.api.model.APIRequestContext;
import org.wso2.carbon.identity.external.api.client.api.model.APIResponse;
import org.wso2.carbon.identity.external.api.client.internal.util.APIRequestBuildingUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This class is responsible for making API calls to the external endpoints.
 */
public class APIClient {

    private static final Log LOG = LogFactory.getLog(APIClient.class);
    private final CloseableHttpClient httpClient;

    /**
     * Constructor to initialize the APIClient with the given configuration.
     *
     * @param apiClientConfig API client configuration.
     */
    public APIClient(APIClientConfig apiClientConfig) {

        int readTimeout = apiClientConfig.getHttpReadTimeoutInMillis();
        int connectionRequestTimeout = apiClientConfig.getHttpConnectionRequestTimeoutInMillis();
        int connectionTimeout = apiClientConfig.getHttpConnectionTimeoutInMillis();

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(readTimeout)
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(apiClientConfig.getPoolSizeToBeSet());
        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(connectionManager)
                .build();

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Initialized APIClient with configuration: readTimeout=%d, " +
                            "connectionRequestTimeout=%d, connectionTimeout=%d, poolSize=%d",
                    apiClientConfig.getHttpReadTimeoutInMillis(),
                    apiClientConfig.getHttpConnectionRequestTimeoutInMillis(),
                    apiClientConfig.getHttpConnectionTimeoutInMillis(),
                    apiClientConfig.getPoolSizeToBeSet()
            ));
        }
    }

    /**
     * Makes a API call to the given endpoint URL with the provided request context.
     *
     * @param requestContext        Request context containing endpoint URL, headers, and payload.
     * @param apiInvocationConfig   Configuration for API invocation.
     * @return APIResponse containing the response from the API call.
     * @throws APIClientInvocationException if an error occurs during the API call.
     */
    public APIResponse callAPI(APIRequestContext requestContext, APIInvocationConfig apiInvocationConfig)
            throws APIClientInvocationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Making API call to endpoint: %s with HTTP method: %s, retry count: %d",
                    requestContext.getEndpointUrl(),
                    requestContext.getHttpMethod().getName(),
                    apiInvocationConfig.getAllowedRetryCount()
            ));
        }

        HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase;
        switch (requestContext.getHttpMethod()) {
            case POST:
                httpEntityEnclosingRequestBase = new HttpPost(requestContext.getEndpointUrl());
                break;
            default:
                LOG.error("Unsupported HTTP method: " + requestContext.getHttpMethod().getName());
                throw new APIClientInvocationException(
                        ErrorMessage.ERROR_CODE_UNSUPPORTED_HTTP_METHOD, requestContext.getHttpMethod().getName());
        }
        setRequestEntity(httpEntityEnclosingRequestBase, requestContext);
        try {
            return executeRequest(httpEntityEnclosingRequestBase, apiInvocationConfig);
        } finally {
            httpEntityEnclosingRequestBase.releaseConnection();
        }
    }

    private void setRequestEntity(HttpEntityEnclosingRequestBase httpRequestBase, APIRequestContext requestContext) {

        StringEntity entity = new StringEntity(requestContext.getPayload(), StandardCharsets.UTF_8);
        httpRequestBase.setEntity(entity);

        httpRequestBase.setHeader("Accept", "application/json");
        httpRequestBase.setHeader("Content-type", "application/json");
        Header authHeader = APIRequestBuildingUtils.buildAuthenticationHeader(requestContext.getApiAuthentication());
        if (authHeader != null) {
            httpRequestBase.setHeader(authHeader);
        }
        for (Map.Entry<String, String> header : requestContext.getHeaders().entrySet()) {
            httpRequestBase.setHeader(header.getKey(), header.getValue());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Request entity configured successfully for endpoint: %s with payload length: " +
                            "%d and headers count: %d",
                    requestContext.getEndpointUrl(),
                    requestContext.getPayload() != null ? requestContext.getPayload().length() : 0,
                    requestContext.getHeaders().size()
            ));
        }
    }

    private APIResponse executeRequest(HttpEntityEnclosingRequestBase request, APIInvocationConfig apiInvocationConfig)
            throws APIClientInvocationException {


        int allowedRetryCount = apiInvocationConfig.getAllowedRetryCount();
        int attempt = 0;
        while (true) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Executing request to URI: %s, retry attempt: %d/%d",
                        request.getURI(), attempt, apiInvocationConfig.getAllowedRetryCount()
                ));
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Request executed successfully to URI: %s, response status: %d",
                            request.getURI(), response.getStatusLine().getStatusCode()));
                }
                return handleResponse(response);
            } catch (IOException e) {
                if (attempt >= allowedRetryCount) {
                    LOG.error(String.format("Request to API: %s failed after %d retries. Throwing exception.",
                            request.getURI(), attempt));
                    throw new APIClientInvocationException(
                            ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, request.getURI().toString(), e);
                }
                int nextAttempt = attempt + 1;
                LOG.warn(String.format("Request to API: %s failed. Retrying %d/%d",
                        request.getURI(), nextAttempt, allowedRetryCount));
                attempt = nextAttempt;
            }
        }
    }

    private APIResponse handleResponse(HttpResponse response) throws IOException {

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        String responseBody = null;
        if (responseEntity != null) {
            responseBody = EntityUtils.toString(responseEntity);
        }
        APIResponse.Builder apiResponseBuilder = new APIResponse.Builder(statusCode, responseBody);
        return apiResponseBuilder.build();
    }
}
