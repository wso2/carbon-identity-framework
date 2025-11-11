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

package org.wso2.carbon.identity.external.api.client.api.model;

import org.wso2.carbon.identity.external.api.client.api.exception.APIClientConfigException;
import org.wso2.carbon.identity.external.api.client.internal.util.APIClientUtils;

import static org.wso2.carbon.identity.external.api.client.api.constant.ErrorMessageConstant.ErrorMessage.ERROR_CODE_INVALID_CONFIG_VALUE;

/**
 * Model class for API Client Configuration.
 */
public class APIClientConfig {

    private final int httpReadTimeoutInMillis;
    private final int httpConnectionRequestTimeoutInMillis;
    private final int httpConnectionTimeoutInMillis;
    private final int poolSizeToBeSet;
    private final int maxPerRoute;

    public APIClientConfig(Builder builder) {

        this.httpReadTimeoutInMillis = builder.httpReadTimeoutInMillis;
        this.httpConnectionRequestTimeoutInMillis = builder.httpConnectionRequestTimeoutInMillis;
        this.httpConnectionTimeoutInMillis = builder.httpConnectionTimeoutInMillis;
        this.poolSizeToBeSet = builder.poolSizeToBeSet;
        this.maxPerRoute = builder.defaultMaxPerRoute;
    }

    /**
     * Get the HTTP read timeout in milliseconds.
     *
     * @return HTTP read timeout in milliseconds.
     */
    public int getHttpReadTimeoutInMillis() {

        return httpReadTimeoutInMillis;
    }

    /**
     * Get the HTTP connection request timeout in milliseconds.
     *
     * @return HTTP connection request timeout in milliseconds.
     */
    public int getHttpConnectionRequestTimeoutInMillis() {

        return httpConnectionRequestTimeoutInMillis;
    }

    /**
     * Get the HTTP connection timeout in milliseconds.
     *
     * @return HTTP connection timeout in milliseconds.
     */
    public int getHttpConnectionTimeoutInMillis() {

        return httpConnectionTimeoutInMillis;
    }

    /**
     * Get the pool size to be set.
     *
     * @return pool size to be set.
     */
    public int getPoolSizeToBeSet() {

        return poolSizeToBeSet;
    }

    /**
     * Get the default max per route.
     *
     * @return default max per route.
     */
    public int getMaxPerRoute() {

        return maxPerRoute;
    }

    /**
     * Builder class for APIClientConfig.
     */
    public static class Builder {

        protected int httpReadTimeoutInMillis = APIClientUtils.getDefaultHttpReadTimeoutInMillis();
        protected int httpConnectionRequestTimeoutInMillis =
                APIClientUtils.getDefaultHttpConnectionRequestTimeoutInMillis();
        protected int httpConnectionTimeoutInMillis = APIClientUtils.getDefaultHttpConnectionTimeoutInMillis();
        protected int poolSizeToBeSet = APIClientUtils.getDefaultPoolSizeToBeSet();
        protected int defaultMaxPerRoute = APIClientUtils.getDefaultMaxPerRoute();

        public APIClientConfig.Builder httpReadTimeoutInMillis(int httpReadTimeoutInMillis) {

            this.httpReadTimeoutInMillis = httpReadTimeoutInMillis;
            return this;
        }

        public APIClientConfig.Builder httpConnectionRequestTimeoutInMillis(int connectionRequestTimeoutInMillis) {

            this.httpConnectionRequestTimeoutInMillis = connectionRequestTimeoutInMillis;
            return this;
        }

        public APIClientConfig.Builder httpConnectionTimeoutInMillis(int connectionTimeoutInMillis) {

            this.httpConnectionTimeoutInMillis = connectionTimeoutInMillis;
            return this;
        }

        public APIClientConfig.Builder poolSizeToBeSet(int poolSizeToBeSet) {

            this.poolSizeToBeSet = poolSizeToBeSet;
            return this;
        }

        public APIClientConfig.Builder defaultMaxPerRoute(int defaultMaxPerRoute) {

            this.defaultMaxPerRoute = defaultMaxPerRoute;
            return this;
        }

        public APIClientConfig build() throws APIClientConfigException {

            validateConfigurationValues(httpReadTimeoutInMillis);
            validateConfigurationValues(httpConnectionRequestTimeoutInMillis);
            validateConfigurationValues(httpConnectionTimeoutInMillis);
            validateConfigurationValues(poolSizeToBeSet);
            validateConfigurationValues(defaultMaxPerRoute);
            return new APIClientConfig(this);
        }

        private void validateConfigurationValues(int value) throws APIClientConfigException {

            if (value <= 0) {
                throw new APIClientConfigException(ERROR_CODE_INVALID_CONFIG_VALUE, Integer.toString(value));
            }
        }
    }
}
