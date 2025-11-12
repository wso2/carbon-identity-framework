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

package org.wso2.carbon.identity.external.api.client.internal.util;

import org.wso2.carbon.identity.core.util.IdentityConfigParser;
/**
 * Utility class for API Client component.
 */
public class APIClientUtils {

    private static final IdentityConfigParser identityConfigParser = IdentityConfigParser.getInstance();
    private static final int DEFAULT_HTTP_READ_TIMEOUT_IN_MILLIS;
    private static final int DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS;
    private static final int DEFAULT_HTTP_CONNECTION_TIMEOUT_IN_MILLIS;
    private static final int DEFAULT_POOL_SIZE_TO_BE_SET;

    static {
        DEFAULT_HTTP_READ_TIMEOUT_IN_MILLIS = getProperty("ExternalAPIClient.HTTPClient.HTTPReadTimeout");
        DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS =
                getProperty("ExternalAPIClient.HTTPClient.HTTPConnectionRequestTimeout");
        DEFAULT_HTTP_CONNECTION_TIMEOUT_IN_MILLIS =
                getProperty("ExternalAPIClient.HTTPClient.HTTPConnectionTimeout");
        DEFAULT_POOL_SIZE_TO_BE_SET = getProperty("ExternalAPIClient.HTTPClient.HTTPConnectionPoolSize");
    }

    /**
     * Gets the default HTTP read timeout in milliseconds.
     *
     * @return HTTP read timeout value.
     */
    public static int getDefaultHttpReadTimeoutInMillis() {

        return DEFAULT_HTTP_READ_TIMEOUT_IN_MILLIS;
    }

    /**
     * Gets the default HTTP connection request timeout in milliseconds.
     *
     * @return HTTP connection request timeout value.
     */
    public static int getDefaultHttpConnectionRequestTimeoutInMillis() {

        return DEFAULT_HTTP_CONNECTION_REQUEST_TIMEOUT_IN_MILLIS;
    }

    /**
     * Gets the default HTTP connection timeout in milliseconds.
     *
     * @return HTTP connection timeout value.
     */
    public static int getDefaultHttpConnectionTimeoutInMillis() {

        return DEFAULT_HTTP_CONNECTION_TIMEOUT_IN_MILLIS;
    }

    /**
     * Gets the default pool size to be set.
     *
     * @return Pool size value.
     */
    public static int getDefaultPoolSizeToBeSet() {

        return DEFAULT_POOL_SIZE_TO_BE_SET;
    }

    private static int getProperty(String propertyName) {

        Object configValue = identityConfigParser.getConfiguration().get(propertyName);
        try {
            if (configValue == null) {
                throw new IllegalArgumentException(
                        String.format("The API client configuration %s value must be an integer.", propertyName));
            }
            return Integer.parseInt(configValue.toString());
            } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
                String.format("The API client configuration %s value must be an integer.", propertyName));
        }
    }
}
