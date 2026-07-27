/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.httpclient5.HTTPClientUtils;

import java.io.IOException;

/**
 * Manage HTTP client creation with pool.
 */
public class HTTPClientManager {

    public static final String HTTP_CLIENT_MAX_TOTAL_CONNECTIONS = "HttpClient.ConnectionPool.MaxTotalConnections";
    public static final String HTTP_CLIENT_DEFAULT_MAX_CONNECTIONS_PER_ROUTE =
            "HttpClient.ConnectionPool.DefaultMaxConnectionsPerRoute";
    public static final String HTTP_CLIENT_ADD_KEEP_ALIVE_STRATEGY =
            "HttpClient.ConnectionPool.AddKeepAliveStrategy";
    public static final String HTTP_CLIENT_POOL_ENABLED = "HttpClient.ConnectionPool.Enabled";

    private static final Log LOG = LogFactory.getLog(HTTPClientManager.class);
    private static final CloseableHttpClient httpClient;
    private static final boolean isConnectionPoolEnabled;

    private HTTPClientManager() {
    }

    static {

        isConnectionPoolEnabled = Boolean.parseBoolean(
                ServerConfiguration.getInstance().getFirstProperty(HTTP_CLIENT_POOL_ENABLED));

        HttpClientBuilder clientBuilder = HTTPClientUtils.createClientWithCustomHostnameVerifier();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initializing HTTPClientManager with connection pool enabled: " + isConnectionPoolEnabled);
        }
        if (isConnectionPoolEnabled) {

            String maxTotalConnectionProp = ServerConfiguration.getInstance()
                    .getFirstProperty(HTTP_CLIENT_MAX_TOTAL_CONNECTIONS);
            String defaultMaxPerRouteProp = ServerConfiguration.getInstance()
                    .getFirstProperty(HTTP_CLIENT_DEFAULT_MAX_CONNECTIONS_PER_ROUTE);
            String addKeepAliveStrategy = ServerConfiguration.getInstance()
                    .getFirstProperty(HTTP_CLIENT_ADD_KEEP_ALIVE_STRATEGY);

            int maxTotalConnections = 100;
            int defaultMaxPerRoute = 100;

            if (maxTotalConnectionProp != null) {
                try {
                    maxTotalConnections = Integer.parseInt(maxTotalConnectionProp);
                } catch (NumberFormatException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Parsing issue for maxTotalConnections property: " + maxTotalConnectionProp);
                    }
                }
            }
            if (defaultMaxPerRouteProp != null) {
                try {
                    defaultMaxPerRoute = Integer.parseInt(defaultMaxPerRouteProp);
                } catch (NumberFormatException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Parsing issue for defaultMaxPerRoute property: " + defaultMaxPerRouteProp);
                    }

                }
            }
            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            connManager.setMaxTotal(maxTotalConnections);
            connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
            clientBuilder.setConnectionManager(connManager);
            if (Boolean.parseBoolean(addKeepAliveStrategy)) {
                clientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
            }
        }
        httpClient = clientBuilder.build();
    }

    /**
     * Returns the configured HttpClient instance.
     * When pooling is enabled, returns a shared pooled client.
     * Otherwise, returns a non-pooled client that should be closed after use.
     *
     * @return CloseableHttpClient instance.
     */
    public static CloseableHttpClient getHttpClient() {

        return httpClient;
    }

    /**
     * Executes an operation with appropriate HttpClient management.
     * Handles both pooled and non-pooled clients, ensuring proper cleanup.
     *
     * @param operation The operation to execute with the HttpClient
     * @param <T>       Return type of the operation
     * @param <E>       Exception type that the operation may throw
     * @return Result of the operation
     * @throws E if operation fails
     */
    public static <T, E extends Exception> T executeWithHttpClient(HttpClientOperation<T, E> operation)
            throws E {

        boolean usePooling = isConnectionPoolEnabled;
        CloseableHttpClient httpClient = usePooling ? getHttpClient()
                : HTTPClientUtils.createClientWithCustomHostnameVerifier().build();

        try {
            return operation.execute(httpClient);
        } finally {
            closeHttpClientIfNeeded(httpClient);
        }
    }

    /**
     * Closes HttpClient only if not using connection pooling.
     *
     * @param httpClient The client to close
     */
    private static void closeHttpClientIfNeeded(CloseableHttpClient httpClient) {

        if (!isConnectionPoolEnabled) {
            try {
                httpClient.close();
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to close non-pooled HttpClient", e);
                }
            }
        }
    }

    /**
     * Returns whether HTTP client connection pooling is enabled.
     *
     * @return true if connection pooling is enabled, false otherwise.
     */
    public static boolean isConnectionPoolEnabled() {

        return isConnectionPoolEnabled;
    }

    /**
     * Functional interface for operations that use HttpClient.
     * Generalized to work with any exception type.
     *
     * @param <T> Return type of the operation
     * @param <E> Exception type that may be thrown
     */
    @FunctionalInterface
    public interface HttpClientOperation<T, E extends Exception> {

        T execute(CloseableHttpClient httpClient) throws E;
    }
}
