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
package org.wso2.carbon.identity.fraud.detection.core.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

/**
 * HttpClientManager class to manage HttpClient instances.
 */
public class HttpClientManager {

    private static final Log LOG = LogFactory.getLog(HttpClientManager.class);
    private static final HttpClientManager instance = new HttpClientManager();

    /**
     * Private constructor to prevent instantiation.
     */
    private HttpClientManager() {

    }

    /**
     * Get the singleton instance of HttpClientManager.
     *
     * @return HttpClientManager instance.
     */
    public static HttpClientManager getInstance() {

        return instance;
    }

    /**
     * Create and return a CloseableHttpClient instance based on the provided connection configuration.
     *
     * @param connectionConfig HttpClientConnectionConfig instance.
     * @return CloseableHttpClient instance.
     */
    public CloseableHttpClient getHttpClient(HttpClientConnectionConfig connectionConfig) {

        return HttpClientBuilder.create().setDefaultRequestConfig(getRequestConfig(connectionConfig)).build();
    }

    /**
     * Close the provided CloseableHttpClient instance.
     *
     * @param httpClient CloseableHttpClient instance to be closed.
     */
    public void closeHttpClient(CloseableHttpClient httpClient) {

        if (httpClient == null) {
            return;
        }

        try {
            httpClient.close();
        } catch (IOException e) {
            LOG.error("Error occurred while closing the HttpClient.", e);
        }
    }

    /**
     * Create RequestConfig based on the provided connection configuration.
     *
     * @param connectionConfig HttpClientConnectionConfig instance.
     * @return RequestConfig instance.
     */
    private RequestConfig getRequestConfig(HttpClientConnectionConfig connectionConfig) {

        return RequestConfig.custom()
                .setConnectTimeout(connectionConfig.getConnectionTimeout())
                .setConnectionRequestTimeout(connectionConfig.getConnectionRequestTimeout())
                .setSocketTimeout(connectionConfig.getReadTimeout())
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
    }
}
