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

package org.wso2.carbon.identity.mgt.endpoint;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.endpoint.client.model.Claim;
import org.wso2.carbon.identity.mgt.endpoint.client.model.User;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
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
    private String appName;
    private char[] appPassword;

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
                if (isSecuredPropertyAvailable(properties)) {
                    resolveSecrets(properties);
                }

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
            this.serviceContextURL = StringUtils.isBlank(serviceContextURL) ? IdentityUtil.getServerURL(
                    IdentityUtil.getServicePath(), true, true) : serviceContextURL;

        } catch (IOException e) {
            log.error("Failed to load service configurations.", e);
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
     * Authenticates the service client from the username and password configured in RecoveryEndpointConfig.properties file
     *
     * @param client service client
     */
    public void authenticate(ServiceClient client) {
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

        SecretResolver secretResolver = SecretResolverFactory.create(properties);
        Enumeration propertyNames = properties.propertyNames();
        if (secretResolver != null && secretResolver.isInitialized()) {
            // Iterate through config file, find encrypted properties and resolve them
            while (propertyNames.hasMoreElements()) {
                String key = (String) propertyNames.nextElement();
                if (StringUtils
                        .startsWith(properties.getProperty(key), IdentityManagementEndpointConstants.SECRET_ALIAS)) {
                    String secretAlias = properties.getProperty(key)
                                                   .split(IdentityManagementEndpointConstants.SECRET_ALIAS_SEPARATOR,
                                                          2)[1];
                    if (secretResolver.isTokenProtected(secretAlias)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Resolving and replacing secret for " + secretAlias);
                        }
                        // Resolving the secret password.
                        String value = secretResolver.resolve(secretAlias);
                        // Replaces the original encrypted property with resolved property
                        properties.put(key, value);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("No encryption done for value with key :" + key);
                        }
                    }
                }
            }
        } else {
            log.warn("Secret Resolver is not present. Failed to resolve encryption in " +
                     IdentityManagementEndpointConstants.SERVICE_CONFIG_FILE_NAME + " file");
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
