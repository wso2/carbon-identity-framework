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

import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.cors.mgt.core.constant.ErrorMessages;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceClientException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;
import org.wso2.carbon.identity.cors.mgt.core.model.Origin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
            corsConfiguration.setAllowAnyOrigin(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.ALLOW_ANY_ORIGIN)));

            // Set allowSubdomains.
            corsConfiguration.setAllowSubdomains(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.ALLOW_SUBDOMAINS)));

            // Set supportedMethods.
            Set<String> supportedMethods = new HashSet(readPropertyArray(IdentityConstants.CORS.SUPPORTED_METHODS));
            corsConfiguration.setSupportedMethods(supportedMethods);

            // Set supportAnyHeader.
            corsConfiguration.setSupportAnyHeader(Boolean.parseBoolean(IdentityUtil.getProperty(
                    IdentityConstants.CORS.SUPPORT_ANY_HEADER)));

            // Set supportedHeaders.
            Set<String> supportedHeaders = readPropertyArray(IdentityConstants.CORS.SUPPORTED_HEADERS)
                    .stream().map(header -> {
                        try {
                            return HeaderUtils.formatCanonical(header);
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(String.format(ErrorMessages.ERROR_CODE_BAD_HEADER
                                    .getDescription(), header));
                        }
                    }).collect(Collectors.toSet());
            corsConfiguration.setSupportedHeaders(supportedHeaders);

            // Set exposedHeaders.
            Set<String> exposedHeaders = readPropertyArray(IdentityConstants.CORS.EXPOSED_HEADERS)
                    .stream().map(header -> {
                        try {
                            return HeaderUtils.formatCanonical(header);
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(String.format(ErrorMessages.ERROR_CODE_BAD_HEADER
                                    .getDescription(), header));
                        }
                    }).collect(Collectors.toSet());
            corsConfiguration.setExposedHeaders(exposedHeaders);

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

    public static List<String> readPropertyArray(String property) {

        Object value = IdentityConfigParser.getInstance().getConfiguration().get(property);
        if (value == null) {
            return new ArrayList<>();
        } else if (value instanceof ArrayList) {
            return (ArrayList) value;
        } else {
            return new ArrayList<>(Collections.singletonList((String) value));
        }
    }

    /**
     * Check for duplicate entries of origins.
     *
     * @param origins List of the origin names.
     * @return Whether the list has duplicate entries or not.
     */
    public static boolean hasDuplicates(List<String> origins) {

        // Treat null as empty list, no duplicates in empty or null list
        if (origins == null || origins.isEmpty()) {
            return false;
        }

        Set<String> originsHashSet = new HashSet<>(origins);
        return origins.size() != originsHashSet.size();
    }

    /**
     * Convert origin strings to Origin instances.
     *
     * @param originNames List of origin names.
     * @return Origins as a list.
     * @throws CORSManagementServiceClientException
     */
    public static List<Origin> createOriginList(List<String> originNames) throws CORSManagementServiceClientException {

        List<Origin> originList = new ArrayList<>();
        for (String origin : originNames) {
            originList.add(new Origin(origin));
        }
        return originList;
    }
}
