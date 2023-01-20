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
import org.json.JSONArray;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This class provides methods to handle endpoint configurations extracted
 * from EndpointConfig.properties file
 */
public class EndpointConfigManager {

    private static final Log log = LogFactory.getLog(EndpointConfigManager.class);
    private static final String PROTECTED_TOKENS = "protectedTokens";
    private static final String DEFAULT_CALLBACK_HANDLER = "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
    private static final String SECRET_PROVIDER = "secretProvider";
    private static Properties prop;
    private static String appName = null;
    private static char[] appPassword = null;
    private static String serverOrigin;
    private static boolean initialized = false;
    private static String googleOneTapRestrictedBrowsers = "";

    /**
     * Initialize Tenant data manager
     */
    public static void init() {

        InputStream inputStream = null;
        try {
            if (!initialized) {
                prop = new Properties();
                String configFilePath = buildFilePath(Constants.TenantConstants.CONFIG_RELATIVE_PATH);
                File configFile = new File(configFilePath);

                if (configFile.exists()) {
                    log.info(Constants.TenantConstants.CONFIG_FILE_NAME + " file loaded from " +
                            Constants.TenantConstants.CONFIG_RELATIVE_PATH);
                    inputStream = new FileInputStream(configFile);

                    prop.load(inputStream);

                    // Resolve encrypted properties with secure vault
                    resolveSecrets(prop);
                } else {
                    inputStream = EndpointConfigManager.class.getClassLoader().getResourceAsStream
                            (Constants.TenantConstants.CONFIG_FILE_NAME);
                    if (inputStream != null) {
                        prop.load(inputStream);
                        if (log.isDebugEnabled()) {
                            log.debug(Constants.TenantConstants.CONFIG_FILE_NAME +
                                    " file loaded from authentication endpoint webapp");
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(Constants.TenantConstants.CONFIG_FILE_NAME + " could not be located in " +
                                    Constants.TenantConstants.CONFIG_RELATIVE_PATH + " or authentication endpoint webapp");
                        }
                    }
                }
                appName = getPropertyValue(Constants.CONFIG_APP_NAME);
                appPassword = getPropertyValue(Constants.CONFIG_APP_PASSWORD).toCharArray();
                serverOrigin = getPropertyValue(Constants.CONFIG_SERVER_ORIGIN);
                if (StringUtils.isNotBlank(serverOrigin)) {
                    serverOrigin = IdentityUtil.fillURLPlaceholders(serverOrigin);
                }
                initialized = true;
                JSONArray restrictedBrowserJArray = new JSONArray(prop.getProperty
                        (Constants.CONFIG_GOOGLE_ONETAP_RESTRICTED_BROWSERS));
                if (restrictedBrowserJArray != null && restrictedBrowserJArray.length() > 0) {
                    googleOneTapRestrictedBrowsers = prop.getProperty(
                            Constants.CONFIG_GOOGLE_ONETAP_RESTRICTED_BROWSERS);
                }
            }
        } catch (IOException e) {
            log.error("Initialization failed : ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error occurred while closing file input stream.", e);
                }
            }
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
    public static char[] getAppPassword() {

        return appPassword;
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
     * Get restricted browser list for Google One Tap.
     *
     * @return The list of comma separated browsers names on which
     * Google  One Tap should be restricted.
     */
    public static String getGoogleOneTapRestrictedBrowsers() {

        return googleOneTapRestrictedBrowsers;
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

    /**
     * Get status of the availability of secured (with secure vault) properties
     *
     * @return availability of secured properties
     */
    private static boolean isSecuredPropertyAvailable(Properties properties) {

        Enumeration propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            if (PROTECTED_TOKENS.equals(key) && StringUtils.isNotBlank(properties.getProperty(key))) {
                return true;
            }
        }
        return false;
    }

    /**
     * There can be sensitive information like passwords in configuration file. If they are encrypted using secure
     * vault, this method will resolve them and replace with original values.
     */
    private static void resolveSecrets(Properties properties) {

        String secretProvider = (String) properties.get(SECRET_PROVIDER);
        if (StringUtils.isBlank(secretProvider)) {
            properties.put(SECRET_PROVIDER, DEFAULT_CALLBACK_HANDLER);
        }
        SecretResolver secretResolver = SecretResolverFactory.create(properties);
        if (secretResolver != null && secretResolver.isInitialized()) {
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                if (value != null) {
                    value = MiscellaneousUtil.resolve(value, secretResolver);
                }
                properties.put(key, value);
            }
        }
        // Support the protectedToken alias used for encryption. ProtectedToken alias is deprecated.
        if (isSecuredPropertyAvailable(properties)) {
            SecretResolver resolver = SecretResolverFactory.create(properties, "");
            String protectedTokens = (String) properties.get(PROTECTED_TOKENS);
            StringTokenizer st = new StringTokenizer(protectedTokens, ",");
            while (st.hasMoreElements()) {
                String element = st.nextElement().toString().trim();

                if (resolver.isTokenProtected(element)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resolving and replacing secret for " + element);
                    }
                    // Replaces the original encrypted property with resolved property
                    properties.put(element, resolver.resolve(element));
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No encryption done for value with key :" + element);
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Secure vault encryption ignored since no protected tokens available");
            }
        }
    }
}
