/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class provides methods to handle endpoint configurations extracted
 * from EndpointConfig.properties file
 */
public class EndpointConfigManager {

    private static final Log log = LogFactory.getLog(EndpointConfigManager.class);
    private static Properties prop;
    private static String appName = null;
    private static String appPassword = null;
    private static boolean isLocalTransportEnabled = true;
    private static String serverOrigin;

    public static void init() {

        prop = new Properties();

        try {
            String configFilePath = buildFilePath(Constants.TenantConstants.CONFIG_RELATIVE_PATH);
            File configFile = new File(configFilePath);

            InputStream inputStream;
            if (configFile.exists()) {
                log.info(Constants.TenantConstants.CONFIG_FILE_NAME + " file loaded from " + Constants
                        .TenantConstants.CONFIG_RELATIVE_PATH);
                inputStream = new FileInputStream(configFile);

                prop.load(inputStream);

            } else {
                inputStream = EndpointConfigManager.class.getClassLoader()
                        .getResourceAsStream(Constants.TenantConstants.CONFIG_FILE_NAME);
                if (inputStream != null) {
                    prop.load(inputStream);
                    log.debug(Constants.TenantConstants.CONFIG_FILE_NAME
                            + " file loaded from authentication endpoint webapp");
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Input stream is null while loading authentication endpoint from webapp");
                    }
                }
            }

            appName = getPropertyValue(Constants.CONFIG_APP_NAME);
            appPassword = getPropertyValue(Constants.CONFIG_APP_PASSWORD);
            isLocalTransportEnabled = Boolean.parseBoolean(getPropertyValue(Constants
                    .CONFIG_LOCAL_TRANSPORT_ENABLED));
            serverOrigin = IdentityUtil.fillURLPlaceholders(getPropertyValue(Constants.CONFIG_SERVER_ORIGIN));

        } catch (IOException e) {
            log.error("Initialization failed : ", e);
        }
    }

    /**
     * Get application name
     *
     * @return Application path
     */
    public static String getAppName() {

        return appName;
    }

    /**
     * Get application password
     *
     * @return Application password
     */
    public static String getAppPassword() {

        return appPassword;
    }

    /**
     * Check if local transport is enabled
     *
     * @return True if local transport is enabled
     */
    public static boolean isIsLocalTransportEnabled() {

        return isLocalTransportEnabled;
    }

    /**
     * Get server origin
     *
     * @return ServerOrigin
     */
    public static String getServerOrigin() {

        return serverOrigin;
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

        if (log.isDebugEnabled()) {
            log.debug("File path for KeyStore/TrustStore : " + path);
        }
        return path;
    }

    /**
     * Get property value by key
     *
     * @param key Property key
     * @return Property value
     */
    private static String getPropertyValue(String key) {
        
        if ((Constants.SERVICES_URL.equals(key)) && !prop.containsKey(Constants.SERVICES_URL)) {
            String serviceUrl = IdentityUtil.getServicePath();
            return IdentityUtil.getServerURL(serviceUrl, true, true);
        }
        return prop.getProperty(key);
    }
}
