/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.client.model.User;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class loads recovery service access properties and provides service access capabilities.
 */
public class IdentityManagementServiceUtil {

    private static IdentityManagementServiceUtil instance = new IdentityManagementServiceUtil();
    private JSONProvider jsonProvider = new JSONProvider();
    private List providers = new ArrayList();

    private String accessUsername;
    private String accessPassword;
    private String serviceContextURL;
    private String contextURL;
    private String appName;
    private char[] appPassword;

    private static final String DEFAULT_CALLBACK_HANDLER = "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
    private static final String SECRET_PROVIDER = "secretProvider";

    private static final Log log = LogFactory.getLog(IdentityManagementServiceUtil.class);

    /**
     * Returns the singleton instance of IdentityManagementServiceUtil
     *
     * @return an instance of IdentityManagementServiceUtil
     */
    public static IdentityManagementServiceUtil getInstance() {
        return instance;
    }

    /**
     * Loads the properties defined in RecoveryEndpointConfig.properties file
     */
    public void init() {

        InputStream inputStream = null;
        jsonProvider.setDropRootElement(true);
        jsonProvider.setIgnoreNamespaces(true);
        jsonProvider.setValidateOutput(true);
        jsonProvider.setSupportUnwrapped(true);
        providers.add(jsonProvider);

        try {
            Properties properties = new Properties();
            File currentDirectory =
                    new File(new File(IdentityManagementEndpointConstants.RELATIVE_PATH_START_CHAR).getAbsolutePath());
            String configFilePath = currentDirectory.getCanonicalPath() + File.separator +
                                    IdentityManagementEndpointConstants.SERVICE_CONFIG_RELATIVE_PATH;
            File configFile = new File(configFilePath);
            if (configFile.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug(IdentityManagementEndpointConstants.SERVICE_CONFIG_FILE_NAME +
                              " file loaded from " + IdentityManagementEndpointConstants.SERVICE_CONFIG_RELATIVE_PATH);
                }

                inputStream = new FileInputStream(configFile);
                properties.load(inputStream);
                resolveSecrets(properties);

            } else {
                if (log.isDebugEnabled()) {
                    log.debug(IdentityManagementEndpointConstants.SERVICE_CONFIG_FILE_NAME +
                              " file loaded from account recovery endpoint webapp");
                }

                inputStream = IdentityManagementServiceUtil.class.getClassLoader().getResourceAsStream
                        (IdentityManagementEndpointConstants.SERVICE_CONFIG_FILE_NAME);

                properties.load(inputStream);
            }

            accessUsername = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                                                            .SERVICE_ACCESS_USERNAME);
            accessPassword = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                                                            .SERVICE_ACCESS_PASSWORD);
            appName = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                    .APP_NAME);
            appPassword = properties.getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants
                    .APP_PASSWORD).toCharArray();
            String serviceContextURL = properties
                    .getProperty(IdentityManagementEndpointConstants.ServiceConfigConstants.SERVICE_CONTEXT_URL);
            contextURL = serviceContextURL;
            // If the service context URL is not configured, use the serviceURLBuilder to build the URL.
            this.serviceContextURL = StringUtils.isBlank(serviceContextURL) ? ServiceURLBuilder.create().
                    build().getAbsoluteInternalURL() : serviceContextURL;

        } catch (IOException e) {
            log.error("Failed to load service configurations.", e);
        } catch (URLBuilderException e) {
            log.error("Error occurred while building service URL.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the FileInputStream for file : " + IdentityManagementEndpointConstants
                            .SERVICE_CONFIG_FILE_NAME, e);
                }
            }
        }
    }

    /**
     * Returns the service context URL
     *
     * @return service context URL
     */
    public String getServiceContextURL() {
        return serviceContextURL;
    }

    /**
     * Returns the context URL configured as identity.server.service.contextURL of RecoveryEndpointConfig.properties
     * file.
     *
     * @return context URL.
     */
    public String getContextURLFromFile() {

        return contextURL;
    }

    /**
     * Authenticates the service client from the username and password configured in RecoveryEndpointConfig.properties file
     *
     * @param client service client
     */
    public void authenticate(ServiceClient client) {
        setAutheticationOptions(client, accessUsername, accessPassword);
    }

    static void setAutheticationOptions(ServiceClient client, String accessUsername, String accessPassword) {
        Options option = client.getOptions();
        HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
        auth.setUsername(accessUsername);
        auth.setPassword(accessPassword);
        auth.setPreemptiveAuthentication(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.AUTHENTICATE, auth);
        option.setManageSession(true);
    }

    public List getJSONProvider(){
        return providers;
    }

    private static boolean isSecuredPropertyAvailable(Properties properties) {

        Enumeration propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            if (StringUtils.startsWith(properties.getProperty(key), IdentityManagementEndpointConstants.SECRET_ALIAS)) {
                return true;
            }
        }
        return false;
    }

    private static void resolveSecrets(Properties properties) {

        String secretProvider = (String) properties.get(SECRET_PROVIDER);
        if (StringUtils.isBlank(secretProvider)) {
            properties.put(SECRET_PROVIDER, DEFAULT_CALLBACK_HANDLER);
        }
        SecretResolver secretResolver = SecretResolverFactory.create(properties);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            if (value != null) {
                value = MiscellaneousUtil.resolve(value, secretResolver);
            }
            properties.put(key, value);
        }
    }

    /**
     * Build user object from complete username
     * @param userName
     * @return
     */
    public User getUser(String userName) {

        if (userName == null) {
            return null;
        }

        String userStoreDomain = extractDomainFromName(userName);
        String tenantDomain = MultitenantUtils.getTenantDomain(userName);
        String userNameWithoutTenantDomainAndUserStoreDomain = MultitenantUtils
                .getTenantAwareUsername(UserCoreUtil.removeDomainFromName(userName));

        User user = new User();
        user.setUsername(userNameWithoutTenantDomainAndUserStoreDomain);
        user.setRealm(userStoreDomain);
        user.setTenantDomain(tenantDomain);

        return user;
    }

    /**
     * Build a user object from tenant domain and username.
     *
     * @param username username provided by user
     * @param tenantDomain tenant domain of the application
     * @return User
     */
    public User resolveUser(String username, String tenantDomain, boolean isSaaSEnabled) {

        if (username == null) {
            return null;
        }
        String userStoreDomain = extractDomainFromName(username);
        User user = new User();
        user.setUsername(MultitenantUtils
                .getTenantAwareUsername(UserCoreUtil.removeDomainFromName(username)));
        if (isSaaSEnabled) {
            user.setTenantDomain(MultitenantUtils.getTenantDomain(username));
        } else {
            user.setTenantDomain(tenantDomain);
        }
        user.setRealm(userStoreDomain);
        return user;
    }

    public String getAppName() {
        return appName;
    }

    public char[] getAppPassword() {
        return appPassword;
    }

    private String extractDomainFromName(String nameWithDomain) {
        if (nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            String domain = nameWithDomain.substring(0, nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));
            return domain.toUpperCase();
        } else {
            return null;
        }
    }
}
