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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.HTTPClientUtils;

/**
 * Manage HTTP client creation with pool.
 */
public class HTTPClientManager {

    public static final String HTTP_CLIENT_MAX_TOTAL_CONNECTIONS = "HttpClient.ConnectionPool.MaxTotalConnections";
    public static final String HTTP_CLIENT_DEFAULT_MAX_CONNECTIONS_PER_ROUTE =
            "HttpClient.ConnectionPool.DefaultMaxConnectionsPerRoute";
    public static final String HTTP_CLIENT_ADD_KEEP_ALIVE_STRATEGY =
            "HttpClient.ConnectionPool.AddKeepAliveStrategy";

    private static final Log log = LogFactory.getLog(HTTPClientManager.class);
    private static final CloseableHttpClient httpClient;
    private static final String CLIENT = "Client ";

    private HTTPClientManager() {
    }

    static {

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
                log.debug("Parsing issue for maxTotalConnections property: " + maxTotalConnectionProp);
            }
        }
        if (defaultMaxPerRouteProp != null) {
            try {
                maxTotalConnections = Integer.parseInt(defaultMaxPerRouteProp);
            } catch (NumberFormatException ignore) {
                log.debug("Parsing issue for defaultMaxPerRoute property: " + defaultMaxPerRouteProp);
            }
        }

        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(maxTotalConnections);
        connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);

        HttpClientBuilder clientBuilder = HTTPClientUtils.createClientWithCustomVerifier();
        clientBuilder.setConnectionManager(connManager);
        if (Boolean.parseBoolean(addKeepAliveStrategy)) {
            clientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        }
        httpClient = clientBuilder.build();
    }

    public static CloseableHttpClient getHttpClient() {

        return httpClient;
    }
}
