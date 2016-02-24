/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

/**
 * AuthenticationEndpointUtil defines utility methods used across the authenticationendpoint web application.
 */
public class AuthenticationEndpointUtil {
    private static final String CUSTOM_PAGE_APP_SPECIFIC_CONFIG_KEY_SEPARATOR = "-";
    private static final String QUERY_STRING_APPENDER = "&";
    private static final String QUERY_STRING_INITIATOR = "?";

    private AuthenticationEndpointUtil() {
    }

    /**
     * Returns the application specific custom page configuration servlet context parameter key given the service
     * provider name and the relative URL path.
     *
     * @param serviceProviderName name of the service provider configured at IdP
     * @param relativePath        relative URL path
     * @return the possible servlet context parameter key configured for the given application
     */
    public static String getApplicationSpecificCustomPageConfigKey(String serviceProviderName, String relativePath) {
        return serviceProviderName + CUSTOM_PAGE_APP_SPECIFIC_CONFIG_KEY_SEPARATOR + relativePath;
    }

    /**
     * Populate and return the redirect url for the given context parameter configuration value and the given
     * query string. Returns null if the given context param configuration value is null.
     *
     * @param customPageConfigValue configured custom page url value as a servlet context param
     * @param queryString           query string of the incoming request
     * @return redirect url of the custom page configuration
     */
    public static String getCustomPageRedirectUrl(String customPageConfigValue, String queryString) {

        String redirectUrl = customPageConfigValue;
        if (customPageConfigValue != null && queryString != null && !queryString.isEmpty()) {
            if (customPageConfigValue.indexOf(QUERY_STRING_INITIATOR) > 0) {
                redirectUrl = customPageConfigValue + QUERY_STRING_APPENDER + queryString;
            } else {
                redirectUrl = customPageConfigValue + QUERY_STRING_INITIATOR + queryString;
            }
        }
        return redirectUrl;
    }
}
