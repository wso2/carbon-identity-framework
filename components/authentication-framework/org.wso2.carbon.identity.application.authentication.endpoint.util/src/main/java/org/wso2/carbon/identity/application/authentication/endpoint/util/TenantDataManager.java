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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;
import org.wso2.securevault.commons.MiscellaneousUtil;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class TenantDataManager {

    private static final Log log = LogFactory.getLog(TenantDataManager.class);
    private static final String PROTECTED_TOKENS = "protectedTokens";
    private static final String DEFAULT_CALLBACK_HANDLER = "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
    private static final String SECRET_PROVIDER = "secretProvider";
    private static Properties prop;
    private static String carbonLogin = "";
    private static String serviceURL;
    private static String usernameHeaderName = "";
    private static final List<String> tenantDomainList = new ArrayList<>();
    private static boolean initialized = false;
    private static boolean initAttempted = false;

    private TenantDataManager() {
    }

    /**
     * Initialize Tenant data manager
     */
    public static synchronized void init() {

        InputStream inputStream = null;
        initAttempted = true;

        try {
            if (!initialized) {
                prop = new Properties();
                String configFilePath = buildFilePath(Constants.TenantConstants.CONFIG_RELATIVE_PATH);
                File configFile = new File(configFilePath);

                if (configFile.exists()) {
                    log.info(Constants.TenantConstants.CONFIG_FILE_NAME + " file loaded from " + Constants
                            .TenantConstants.CONFIG_RELATIVE_PATH);
                    inputStream = new FileInputStream(configFile);

                    prop.load(inputStream);
                    if (Boolean.parseBoolean(getPropertyValue(Constants.TenantConstants.TENANT_LIST_ENABLED))) {
                        // Resolve encrypted properties with secure vault
                        resolveSecrets(prop);
                    }

                } else {
                    inputStream = TenantDataManager.class.getClassLoader()
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

                if (Boolean.parseBoolean(getPropertyValue(Constants.TenantConstants.TENANT_LIST_ENABLED))) {

                    usernameHeaderName = getPropertyValue(Constants.TenantConstants.USERNAME_HEADER);

                    carbonLogin = getPropertyValue(Constants.TenantConstants.USERNAME);

                    // Base64 encoded username
                    carbonLogin = Base64.encode(carbonLogin.getBytes(Constants.TenantConstants.CHARACTER_ENCODING));

                    String clientKeyStorePath = buildFilePath(getPropertyValue(Constants.TenantConstants.CLIENT_KEY_STORE));
                    String clientTrustStorePath = buildFilePath(getPropertyValue(Constants.TenantConstants
                            .CLIENT_TRUST_STORE));

                    if (StringUtils.isNotEmpty(getPropertyValue(Constants.TenantConstants.TLS_PROTOCOL))) {
                        TenantMgtAdminServiceClient.setProtocol(getPropertyValue(Constants.TenantConstants
                                .TLS_PROTOCOL));
                    }

                    if (StringUtils.isNotEmpty(getPropertyValue(Constants.TenantConstants.KEY_MANAGER_TYPE))) {
                        TenantMgtAdminServiceClient.setKeyManagerType(getPropertyValue(Constants.TenantConstants
                                .KEY_MANAGER_TYPE));
                    }
                    if (StringUtils.isNotEmpty(getPropertyValue(Constants.TenantConstants.TRUST_MANAGER_TYPE))) {
                        TenantMgtAdminServiceClient.setTrustManagerType(getPropertyValue(Constants.TenantConstants
                                .TRUST_MANAGER_TYPE));
                    }

                    TenantMgtAdminServiceClient
                            .loadKeyStore(clientKeyStorePath, getPropertyValue(Constants.TenantConstants
                                    .CLIENT_KEY_STORE_PASSWORD));
                    TenantMgtAdminServiceClient
                            .loadTrustStore(clientTrustStorePath, getPropertyValue(Constants.TenantConstants
                                    .CLIENT_TRUST_STORE_PASSWORD));
                    TenantMgtAdminServiceClient.initMutualSSLConnection(Boolean.parseBoolean(
                            getPropertyValue(Constants.TenantConstants.HOSTNAME_VERIFICATION_ENABLED)));

                        // Build the service URL of tenant management admin service
                        StringBuilder builder = new StringBuilder();
                        serviceURL = builder.append(getPropertyValue(Constants.SERVICES_URL)).append(Constants.TenantConstants
                                .TENANT_MGT_ADMIN_SERVICE_URL).toString();

                        initialized = true;
                }
            }

        } catch (AuthenticationException | IOException e) {
            log.error("Initialization failed : ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Failed to close the FileInputStream, file : " + Constants.TenantConstants
                            .CONFIG_FILE_NAME, e);
                }
            }
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
    protected static String getPropertyValue(String key) {
        if ((Constants.SERVICES_URL.equals(key)) && !prop.containsKey(Constants.SERVICES_URL)) {
            String serviceUrl = IdentityUtil.getServicePath();
            return IdentityUtil.getServerURL(serviceUrl, true, true);
        }
        return prop.getProperty(key);
    }

    /**
     * Call service and return response
     *
     * @param url Service URL
     * @return Response from service
     */
    private static String getServiceResponse(String url) {

        String serviceResponse;
        Map<String, String> headerParams = new HashMap<>();
        // Set the username in HTTP header for mutual ssl authentication
        headerParams.put(usernameHeaderName, carbonLogin);
        serviceResponse = TenantMgtAdminServiceClient.sendPostRequest(url, null, headerParams);
        return serviceResponse;
    }

    /**
     * Get active tenants list
     *
     * @return List of tenant domains
     */
    public static List<String> getAllActiveTenantDomains() {

        if (initialized) {
            refreshActiveTenantDomainsList();
        }
        return tenantDomainList;
    }

    /**
     * Reset the tenant domains list
     *
     */
    public static void resetTenantDataList() {

        if (!initialized) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domains list not set as TenantDataManager is not initialized.");
            }
            return;
        }
        synchronized (tenantDomainList) {
            tenantDomainList.clear();
            refreshActiveTenantDomainsList();
        }
    }

    /**
     * Retrieve latest active tenant domains list
     */
    private static void refreshActiveTenantDomainsList() {

        try {
            String xmlString = getServiceResponse(serviceURL);

            if (StringUtils.isNotEmpty(xmlString)) {

                XPathFactory xpf = XPathFactory.newInstance();
                XPath xpath = xpf.newXPath();

                InputSource inputSource = new InputSource(new StringReader(xmlString));

                DocumentBuilderFactory factory = IdentityUtil.getSecuredDocumentBuilderFactory();

                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inputSource);

                String xPathExpression = "/*[local-name() = '" + Constants.TenantConstants.RETRIEVE_TENANTS_RESPONSE
                        + "']/*[local-name() = '" +
                        Constants.TenantConstants.RETURN + "']";

                XPathExpression expr = xpath.compile(xPathExpression);
                NodeList nodeList;
                nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                // Reset existing tenant domains list
                tenantDomainList.clear();

                // For each loop is not supported for NodeList
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        NodeList tenantData = element.getChildNodes();
                        boolean activeChecked = false;
                        boolean domainChecked = false;
                        boolean isActive = false;
                        String tenantDomain = null;

                        // For each loop is not supported for NodeList
                        for (int j = 0; j < tenantData.getLength(); j++) {
                            Node dataItem = tenantData.item(j);
                            String localName = dataItem.getLocalName();

                            if (Constants.TenantConstants.ACTIVE.equals(localName)) {
                                // Current element has domain status active or inactive
                                activeChecked = true;
                                if (Boolean.parseBoolean(dataItem.getTextContent())) {
                                    isActive = true;
                                }
                            }

                            if (Constants.TenantConstants.TENANT_DOMAIN.equals(localName)) {
                                // Current element has domain name of the tenant
                                domainChecked = true;
                                tenantDomain = dataItem.getTextContent();
                            }

                            if (activeChecked && domainChecked) {
                                if (isActive) {
                                    tenantDomainList.add(tenantDomain);

                                    if (log.isDebugEnabled()) {
                                        log.debug(tenantDomain + " is active and added to the dropdown list");
                                    }
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug(tenantDomain + " is inactive and not added to the dropdown list");
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                // Sort the list of tenant domains alphabetically
                Collections.sort(tenantDomainList);
            }
        } catch (Exception e) {
            // Catching the general exception as if no tenants are available it should stop processing
            log.error("Retrieving list of active tenant domains failed. Ignore this if there are no tenants : ", e);
        }
    }

    /**
     * Get status of the tenant list dropdown enabled or disabled
     *
     * @return Tenant list enabled or disabled status
     */
    public static boolean isTenantListEnabled() {
        if (!initAttempted && !initialized) {
            init();
        }
        return Boolean.parseBoolean(getPropertyValue(Constants.TenantConstants.TENANT_LIST_ENABLED));
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
                    // Replaces the original encrypted property with resolved property.
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
}
