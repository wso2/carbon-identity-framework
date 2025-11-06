package org.wso2.carbon.identity.fraud.detectors.core.http;

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
