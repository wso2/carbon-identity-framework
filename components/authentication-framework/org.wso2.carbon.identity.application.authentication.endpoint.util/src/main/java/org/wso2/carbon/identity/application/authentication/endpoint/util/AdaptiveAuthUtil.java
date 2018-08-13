/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * AuthenticationEndpointUtil defines utility methods used across the authenticationendpoint web application.
 */
public class AdaptiveAuthUtil {

    private static final Log log = LogFactory.getLog(AdaptiveAuthUtil.class);

    private static Map<String, String> adaptiveAuthConfigs = new HashMap<>();

    private AdaptiveAuthUtil() {

    }

    public static void init() {

        try {
            String configFilePath = buildFilePath(Constants.TenantConstants.IDENTITY_XML_RELATIVE_PATH);
            File configFile = new File(configFilePath);

            if (configFile.exists()) {
                adaptiveAuthConfigs.put("AdaptiveAuth.HTTPConnectionTimeout", IdentityUtil.getProperty("AdaptiveAuth.HTTPConnectionTimeout"));
                adaptiveAuthConfigs.put("AdaptiveAuth.RefreshInterval", IdentityUtil.getProperty("AdaptiveAuth.RefreshInterval"));
            } else {
                Properties props = new Properties();
                try (InputStream inputStream = AdaptiveAuthUtil.class.getClassLoader()
                        .getResourceAsStream(Constants.TenantConstants.CONFIG_FILE_NAME)) {
                    if (inputStream != null) {
                        props.load(inputStream);
                        if (log.isDebugEnabled()) {
                            log.debug(Constants.TenantConstants.CONFIG_FILE_NAME
                                    + " file loaded from authentication endpoint webapp");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Input stream is null while loading authentication endpoint from webapp");
                        }
                    }
                }
                Set<String> keys = props.stringPropertyNames();
                for (String key : keys) {
                    switch (key) {
                        case "AdaptiveAuth.HTTPConnectionTimeout":
                        case "AdaptiveAuth.RefreshInterval":
                            adaptiveAuthConfigs.put(key, props.getProperty(key));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error while loading configs for adaptive authentication.", e);
        }
    }

    /**
     * Build the absolute path of a give file path
     *
     * @param path File path
     * @return Absolute file path
     * @throws java.io.IOException
     */
    private static String buildFilePath(String path) throws IOException {

        if (StringUtils.isNotEmpty(path) && path.startsWith(Constants.TenantConstants.RELATIVE_PATH_START_CHAR)) {
            // Relative file path is given
            File currentDirectory = new File(new File(Constants.TenantConstants.RELATIVE_PATH_START_CHAR)
                    .getAbsolutePath());
            path = currentDirectory.getCanonicalPath() + File.separator + path;
        }
        return path;
    }

    public static int getRequestTimeout() {

        return getConfiguredTime(5000, "AdaptiveAuth.HTTPConnectionTimeout");
    }

    public static int getRefreshInterval() {

        return getConfiguredTime(500, "AdaptiveAuth.RefreshInterval");
    }

    private static int getConfiguredTime(int defaultValue, String propertyName) {

        String connectionTimeoutString = adaptiveAuthConfigs.get(propertyName);

        int connectionTimeout = defaultValue;
        if (connectionTimeoutString != null) {
            try {
                connectionTimeout = Integer.parseInt(connectionTimeoutString);
            } catch (NumberFormatException e) {
                // Default value will be used.
            }
        }
        return connectionTimeout;
    }
}
