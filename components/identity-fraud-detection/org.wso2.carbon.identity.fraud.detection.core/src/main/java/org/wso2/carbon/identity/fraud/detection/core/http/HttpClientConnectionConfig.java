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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.fraud.detection.core.constant.HttpClientConnectionConstants;

/**
 * Configuration class for HTTP client connection settings.
 */
public class HttpClientConnectionConfig {

    private final int connectionTimeout;
    private final int readTimeout;
    private final int connectionRequestTimeout;

    /**
     * Private constructor to enforce the use of the Builder.
     *
     * @param builder Builder instance.
     */
    private HttpClientConnectionConfig(Builder builder) {

        this.connectionTimeout = builder.connectionTimeout;
        this.readTimeout = builder.readTimeout;
        this.connectionRequestTimeout = builder.connectionRequestTimeout;
    }

    /**
     * Get the connection timeout.
     *
     * @return Connection timeout in milliseconds.
     */
    public int getConnectionTimeout() {

        return connectionTimeout;
    }

    /**
     * Get the read timeout.
     *
     * @return Read timeout in milliseconds.
     */
    public int getReadTimeout() {

        return readTimeout;
    }

    /**
     * Get the connection request timeout.
     *
     * @return Connection request timeout in milliseconds.
     */
    public int getConnectionRequestTimeout() {

        return connectionRequestTimeout;
    }

    /**
     * Builder class for HttpClientConnectionConfig.
     */
    public static class Builder {

        private static final Log LOG = LogFactory.getLog(HttpClientConnectionConfig.class);
        private int connectionTimeout;
        private int readTimeout;
        private int connectionRequestTimeout;

        /**
         * Default constructor that initializes timeouts from configuration or defaults.
         */
        public Builder() {

            String connectionTimeoutConfig = IdentityUtil.getProperty(
                    HttpClientConnectionConstants.CONNECTION_TIMEOUT_CONFIG);
            try {
                this.connectionTimeout = StringUtils.isNotBlank(connectionTimeoutConfig) ?
                        Integer.parseInt(connectionTimeoutConfig) : HttpClientConnectionConstants.CONNECTION_TIMEOUT;
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing connection timeout : " + connectionTimeoutConfig +
                        " defaulting to system default : " + HttpClientConnectionConstants.CONNECTION_TIMEOUT, e);
                this.connectionTimeout = HttpClientConnectionConstants.CONNECTION_TIMEOUT;
            }

            String readTimeoutConfig = IdentityUtil.getProperty(HttpClientConnectionConstants.READ_TIMEOUT_CONFIG);
            try {
                this.readTimeout = StringUtils.isNotBlank(readTimeoutConfig) ?
                        Integer.parseInt(readTimeoutConfig) : HttpClientConnectionConstants.READ_TIMEOUT;
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing read timeout : " + readTimeoutConfig +
                        " defaulting to system default : " + HttpClientConnectionConstants.READ_TIMEOUT, e);
                this.readTimeout = HttpClientConnectionConstants.READ_TIMEOUT;
            }

            String connectionRequestTimeoutConfig =
                    IdentityUtil.getProperty(HttpClientConnectionConstants.CONNECTION_REQUEST_TIMEOUT_CONFIG);
            try {
                this.connectionRequestTimeout = StringUtils.isNotBlank(connectionRequestTimeoutConfig) ?
                        Integer.parseInt(connectionRequestTimeoutConfig) :
                        HttpClientConnectionConstants.CONNECTION_REQUEST_TIMEOUT;
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing connection request timeout : " + connectionRequestTimeoutConfig +
                        " defaulting to system default : " +
                        HttpClientConnectionConstants.CONNECTION_REQUEST_TIMEOUT, e);
                this.connectionRequestTimeout = HttpClientConnectionConstants.CONNECTION_REQUEST_TIMEOUT;
            }
        }

        /**
         * Set the connection timeout.
         *
         * @param connectionTimeout Connection timeout in milliseconds.
         * @return Builder instance.
         */
        public Builder setConnectionTimeout(int connectionTimeout) {

            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Set the read timeout.
         *
         * @param readTimeout Read timeout in milliseconds.
         * @return Builder instance.
         */
        public Builder setReadTimeout(int readTimeout) {

            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Set the connection request timeout.
         *
         * @param connectionRequestTimeout Connection request timeout in milliseconds.
         * @return Builder instance.
         */
        public Builder setConnectionRequestTimeout(int connectionRequestTimeout) {

            this.connectionRequestTimeout = connectionRequestTimeout;
            return this;
        }

        /**
         * Build the HttpClientConnectionConfig instance.
         *
         * @return HttpClientConnectionConfig instance.
         */
        public HttpClientConnectionConfig build() {

            return new HttpClientConnectionConfig(this);
        }
    }
}
