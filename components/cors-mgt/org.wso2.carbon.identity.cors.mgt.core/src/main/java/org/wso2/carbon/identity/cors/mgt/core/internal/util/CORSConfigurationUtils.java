/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class for CORS configuration operations.
 */
public class CORSConfigurationUtils {

    private static CORSConfiguration corsConfiguration = null;

    /**
     * Private constructor of CORSConfigurationUtil.
     */
    private CORSConfigurationUtils() {

    }

    /**
     * Read the default CORS configuration properties in the identity.xml.
     *
     * @return Server default {@code CORSConfiguration} object.
     */
    public static synchronized CORSConfiguration getServerCORSConfiguration() {

        if (corsConfiguration == null) {
            corsConfiguration = new CORSConfiguration();

            // Set allowGenericHttpRequests.
            corsConfiguration.setAllowGenericHttpRequests(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.ALLOW_GENERIC_HTTP_REQUESTS)));

            // Set allowAnyOrigin.
            String allowedOriginsProperty = IdentityUtil.getProperty(IdentityConstants.CORS.ALLOWED_ORIGINS);
            if (StringUtils.isNotBlank(allowedOriginsProperty) && allowedOriginsProperty.equals("*")) {
                corsConfiguration.setAllowAnyOrigin(true);
            }

            // Set allowSubdomains.
            corsConfiguration.setAllowSubdomains(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.ALLOW_SUBDOMAINS)));

            // Set supportedMethods.
            String supportedMethodsProperty = IdentityUtil.getProperty(IdentityConstants.CORS.SUPPORTED_METHODS);
            if (StringUtils.isNotBlank(supportedMethodsProperty)) {
                Set<String> supportedMethods = new HashSet<>(parseWords(supportedMethodsProperty));
                corsConfiguration.setSupportedMethods(supportedMethods);
            }

            // Set supportAnyHeader and supportedHeaders.
            String supportedHeadersProperty = IdentityUtil.getProperty(IdentityConstants.CORS.SUPPORTED_HEADERS);
            if (StringUtils.isNotBlank(supportedHeadersProperty)) {
                if (supportedHeadersProperty.equals("*")) {
                    corsConfiguration.setSupportAnyHeader(true);
                } else {
                    corsConfiguration.setSupportAnyHeader(false);

                    Set<String> supportedHeaders = new HashSet<>();
                    for (String header : parseWords(supportedHeadersProperty)) {
                        try {
                            supportedHeaders.add(HeaderUtils.formatCanonical(header));
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(String.format(
                                    ErrorMessages.ERROR_CODE_BAD_HEADER.getDescription(), header));
                        }
                    }
                    corsConfiguration.setSupportedHeaders(supportedHeaders);
                }
            }

            // Set exposedHeaders.
            Set<String> exposedHeaders = new HashSet<>();
            String exposedHeadersProperty = IdentityUtil.getProperty(IdentityConstants.CORS.EXPOSED_HEADERS);
            if (StringUtils.isNotBlank(exposedHeadersProperty)) {
                for (String header : parseWords(exposedHeadersProperty)) {
                    try {
                        exposedHeaders.add(HeaderUtils.formatCanonical(header));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(String.format(
                                ErrorMessages.ERROR_CODE_BAD_HEADER.getDescription(), header));
                    }
                }
                corsConfiguration.setExposedHeaders(exposedHeaders);
            }

            // Set supportsCredentials.
            corsConfiguration.setSupportsCredentials(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.SUPPORTS_CREDENTIALS)));

            // Set maxAge.
            String maxAgeProperty = Objects.requireNonNull(IdentityUtil.getProperty(IdentityConstants.CORS.MAX_AGE));
            corsConfiguration.setMaxAge(Double.valueOf(maxAgeProperty).intValue());

            // Set tagRequests.
            corsConfiguration.setTagRequests(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.TAG_REQUESTS)));

        }

        return corsConfiguration;
    }

    /**
     * Parses a string containing words separated by space and/or comma.
     *
     * @param word The string to parse. Must not be {@code null}.
     * @return An array of the parsed words, empty if none were found.
     */
    public static List<String> parseWords(final String word) {

        String trimmedWord = word.trim();

        if (trimmedWord.isEmpty()) {
            return new ArrayList<>();
        } else {
            return Arrays.asList(trimmedWord.split("\\s*,\\s*|\\s+"));
        }
    }
}
