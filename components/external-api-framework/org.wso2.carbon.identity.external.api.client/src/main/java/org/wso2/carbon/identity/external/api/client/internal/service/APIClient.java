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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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
        connectionManager.setDefaultMaxPerRoute(apiClientConfig.getMaxPerRoute());
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

        if (requestContext == null || apiInvocationConfig == null) {
            throw new APIClientInvocationException(ErrorMessage.ERROR_CODE_NULL_API_DATA, null);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Making API call to endpoint: %s with HTTP method: %s, retry count: %d",
                    requestContext.getEndpointUrl(),
                    requestContext.getHttpMethod().getName(),
                    apiInvocationConfig.getAllowedRetryCount()
            ));
        }

        HttpRequestBase httpRequestBase;
        switch (requestContext.getHttpMethod()) {
            case POST:
                HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase =
                        new HttpPost(requestContext.getEndpointUrl());
                httpEntityEnclosingRequestBase.setEntity(requestContext.getPayload());
                httpRequestBase = httpEntityEnclosingRequestBase;
                break;
            case GET:
                httpRequestBase = new HttpGet(requestContext.getEndpointUrl());
                break;
            default:
                throw new APIClientInvocationException(
                        ErrorMessage.ERROR_CODE_UNSUPPORTED_HTTP_METHOD, requestContext.getHttpMethod().getName());
        }
        setRequestHeaders(httpRequestBase, requestContext);

        try {
            return executeRequest(httpRequestBase, apiInvocationConfig);
        } finally {
            httpRequestBase.releaseConnection();
        }
    }

    private void setRequestHeaders(HttpRequestBase httpRequestBase, APIRequestContext requestContext) {

        Header authHeader = APIRequestBuildingUtils.buildAuthenticationHeader(requestContext.getApiAuthentication());
        if (authHeader != null) {
            httpRequestBase.setHeader(authHeader);
        }
        for (Map.Entry<String, String> header : requestContext.getHeaders().entrySet()) {
            httpRequestBase.setHeader(header.getKey(), header.getValue());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Request entity configured successfully for endpoint: %s with headers count: %d",
                    requestContext.getEndpointUrl(),
                    requestContext.getHeaders().size()
            ));
        }
    }

    private APIResponse executeRequest(HttpRequestBase request, APIInvocationConfig apiInvocationConfig)
            throws APIClientInvocationException {


        int allowedAttemptCount = apiInvocationConfig.getAllowedRetryCount() + 1;
        for (int attempt = 1; attempt <= allowedAttemptCount; attempt++) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Executing request to URI: %s, attempt: %d/%d",
                        request.getURI(), attempt , allowedAttemptCount
                ));
            }

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Request executed successfully to URI: %s, response status: %d",
                            request.getURI(), response.getStatusLine().getStatusCode()
                    ));
                }
                return handleResponse(response);
            } catch (IOException e) {
                if (attempt == allowedAttemptCount) {
                    throw new APIClientInvocationException(ErrorMessage.ERROR_CODE_WHILE_INVOKING_API,
                            request.getURI().toString(), e
                    );
                }
            }
        }
        throw new APIClientInvocationException(ErrorMessage.ERROR_CODE_WHILE_INVOKING_API, request.getURI().toString());
    }

    private APIResponse handleResponse(HttpResponse response) throws IOException {

        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity responseEntity = response.getEntity();

        String responseBody = null;
        if (responseEntity != null) {
            responseBody = EntityUtils.toString(responseEntity);
        }

        return new APIResponse(statusCode, responseBody);
    }
}
