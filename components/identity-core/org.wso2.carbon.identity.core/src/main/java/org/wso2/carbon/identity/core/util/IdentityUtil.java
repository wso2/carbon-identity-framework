/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.core.util;

import org.apache.axiom.om.impl.dom.factory.OMDOMFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.SecurityManager;
import org.apache.xml.security.utils.Base64;
import org.opensaml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.identity.base.CarbonEntityResolver;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.model.IdentityCacheConfigKey;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfigKey;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentityUtil {

    public static final ThreadLocal<Map<String, Object>> threadLocalProperties = new
            ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private final static char[] ppidDisplayCharMap = new char[]{'Q', 'L', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'J', 'K',
            'M', 'N', 'P', 'R', 'S', 'T', 'U',
            'V', 'W', 'X', 'Y', 'Z'};
    public static final String DEFAULT_FILE_NAME_REGEX = "^(?!(?:CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\.[^.]*)?$)" +
                                                         "[^<>:\"/\\\\|?*\\x00-\\x1F]*[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]$";
    private static Log log = LogFactory.getLog(IdentityUtil.class);
    private static Map<String, Object> configuration = new HashMap<String, Object>();
    private static Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> eventListenerConfiguration = new
            HashMap<>();
    private static Map<IdentityCacheConfigKey, IdentityCacheConfig> identityCacheConfigurationHolder = new HashMap<>();
    private static Document importerDoc = null;
    private static ThreadLocal<IdentityErrorMsgContext> IdentityError = new ThreadLocal<IdentityErrorMsgContext>();
    private static final String SECURITY_MANAGER_PROPERTY = Constants.XERCES_PROPERTY_PREFIX +
            Constants.SECURITY_MANAGER_PROPERTY;
    private static final int ENTITY_EXPANSION_LIMIT = 0;

    /**
     * @return
     */
    public static IdentityErrorMsgContext getIdentityErrorMsg() {
        if (IdentityError.get() == null) {
            return null;
        }
        return IdentityError.get();
    }

    /**
     * @param error
     */
    public static void setIdentityErrorMsg(IdentityErrorMsgContext error) {
        IdentityError.set(error);
    }

    /**
     *
     */
    public static void clearIdentityErrorMsg() {
        IdentityError.remove();
    }

    /**
     * Read configuration elements from the identity.xml
     *
     * @param key Element Name as specified from the parent elements in the XML structure.
     *            To read the element value of b in {@code<a><b>text</b></a>}, the property
     *            name should be passed as "a.b"
     * @return Element text value, "text" for the above element.
     */
    public static String getProperty(String key) {

        Object value = configuration.get(key);
        String strValue;

        if (value instanceof ArrayList) {
            strValue = (String) ((ArrayList) value).get(0);
        } else {
            strValue = (String) value;
        }

        strValue = fillURLPlaceholders(strValue);

        return strValue;
    }

    public static IdentityEventListenerConfig readEventListenerProperty(String type, String name) {
        IdentityEventListenerConfigKey identityEventListenerConfigKey = new IdentityEventListenerConfigKey(type, name);
        IdentityEventListenerConfig identityEventListenerConfig = eventListenerConfiguration.get(identityEventListenerConfigKey);
        return identityEventListenerConfig;
    }

    public static IdentityCacheConfig getIdentityCacheConfig(String cacheManagerName, String cacheName) {
        IdentityCacheConfigKey configKey = new IdentityCacheConfigKey(cacheManagerName, cacheName);
        IdentityCacheConfig identityCacheConfig = identityCacheConfigurationHolder.get(configKey);
        return identityCacheConfig;
    }

    public static void populateProperties() {
        configuration = IdentityConfigParser.getInstance().getConfiguration();
        eventListenerConfiguration = IdentityConfigParser.getInstance().getEventListenerConfiguration();
        identityCacheConfigurationHolder = IdentityConfigParser.getInstance().getIdentityCacheConfigurationHolder();
    }

    public static String getPPIDDisplayValue(String value) throws Exception {
        log.info("Generating display value of PPID : " + value);
        byte[] rawPpid = Base64.decode(value);
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(rawPpid);
        byte[] hashId = sha1.digest();
        char[] returnChars = new char[10];
        for (int i = 0; i < 10; i++) {
            int rawValue = (hashId[i] + 128) % 32;
            returnChars[i] = ppidDisplayCharMap[rawValue];
        }
        StringBuilder sb = new StringBuilder();
        sb.append(returnChars, 0, 3);
        sb.append("-");
        sb.append(returnChars, 3, 4);
        sb.append("-");
        sb.append(returnChars, 6, 3);
        return sb.toString();

    }

    /**
     * Serialize the given node to a String.
     *
     * @param node Node to be serialized.
     * @return The serialized node as a java.lang.String instance.
     */
    public static String nodeToString(Node node) {

        if (importerDoc == null) {
            OMDOMFactory fac = new OMDOMFactory();
            importerDoc = (Document) fac.createOMDocument();
        }
        // Import the node as an AXIOM-DOOM node and use toSting()
        Node axiomNode = importerDoc.importNode(node, true);
        return axiomNode.toString();
    }

    public static String getHMAC(String secretKey, String baseString) throws SignatureException {
        try {
            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), HMAC_SHA1_ALGORITHM);
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            return Base64.encode(rawHmac);
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
    }

    /**
     * Generates a secure random hexadecimal string using SHA1 PRNG and digest
     *
     * @return Random hexadecimal encoded String
     * @throws Exception
     */
    public static String generateUUID() throws Exception {

        try {
            // SHA1 Pseudo Random Number Generator
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");

            // random number
            String randomNum = Integer.toString(prng.nextInt());
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha.digest(randomNum.getBytes());

            // Hexadecimal encoding
            return new String(Hex.encodeHex(digest));

        } catch (NoSuchAlgorithmException e) {
            throw new Exception("Failed to generate UUID ", e);
        }
    }

    /**
     * Generates a random number using two UUIDs and HMAC-SHA1
     *
     * @return Random Number generated.
     * @throws IdentityException Exception due to Invalid Algorithm or Invalid Key
     */
    public static String getRandomNumber() throws IdentityException {
        try {
            String secretKey = UUIDGenerator.generateUUID();
            String baseString = UUIDGenerator.generateUUID();

            SecretKeySpec key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            String random = Base64.encode(rawHmac);
            // Registry doesn't have support for these character.
            random = random.replace("/", "_");
            random = random.replace("=", "a");
            random = random.replace("+", "f");
            return random;
        } catch (Exception e) {
            log.error("Error when generating a random number.", e);
            throw IdentityException.error("Error when generating a random number.", e);
        }
    }

    public static int getRandomInteger() throws IdentityException {

        try {
            SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
            int number = prng.nextInt();
            while (number < 0) {
                number = prng.nextInt();
            }
            return number;
        } catch (NoSuchAlgorithmException e) {
            log.error("Error when generating a random number.", e);
            throw IdentityException.error("Error when generating a random number.", e);
        }

    }

    public static String getIdentityConfigDirPath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator + "identity";
    }

    public static String getServerURL(String endpoint, boolean addProxyContextPath, boolean addWebContextRoot)
            throws IdentityRuntimeException {
        String hostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);

        try {
            if (hostName == null) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            throw IdentityRuntimeException.error("Error while trying to read hostname.", e);
        }

        String mgtTransport = CarbonUtils.getManagementTransport();
        AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                getServerConfigContext().getAxisConfiguration();
        int mgtTransportPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
        if (mgtTransportPort <= 0) {
            mgtTransportPort = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
        }
        StringBuilder serverUrl = new StringBuilder(mgtTransport).append("://").append(hostName.toLowerCase());
        // If it's well known HTTPS port, skip adding port
        if (mgtTransportPort != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            serverUrl.append(":").append(mgtTransportPort);
        }

        // If ProxyContextPath is defined then append it
        if(addProxyContextPath) {
            // If ProxyContextPath is defined then append it
            String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .PROXY_CONTEXT_PATH);
            if (StringUtils.isNotBlank(proxyContextPath)) {
                if (!serverUrl.toString().endsWith("/") && proxyContextPath.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(proxyContextPath.trim());
                } else if (serverUrl.toString().endsWith("/") && proxyContextPath.trim().charAt(0) == '/') {
                    serverUrl.append(proxyContextPath.trim().substring(1));
                } else {
                    serverUrl.append(proxyContextPath.trim());
                }
            }
        }

        // If webContextRoot is defined then append it
        if (addWebContextRoot) {
            String webContextRoot = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .WEB_CONTEXT_ROOT);
            if (StringUtils.isNotBlank(webContextRoot)) {
                if (!serverUrl.toString().endsWith("/") && webContextRoot.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(webContextRoot.trim());
                } else if (serverUrl.toString().endsWith("/") && webContextRoot.trim().charAt(0) == '/') {
                    serverUrl.append(webContextRoot.trim().substring(1));
                } else {
                    serverUrl.append(webContextRoot.trim());
                }
            }
        }
        if (StringUtils.isNotBlank(endpoint)) {
            if (!serverUrl.toString().endsWith("/") && endpoint.trim().charAt(0) != '/') {
                serverUrl.append("/").append(endpoint.trim());
            } else if (serverUrl.toString().endsWith("/") && endpoint.trim().charAt(0) == '/') {
                serverUrl.append(endpoint.trim().substring(1));
            } else {
                serverUrl.append(endpoint.trim());
            }
        }
        if (serverUrl.toString().endsWith("/")) {
            serverUrl.deleteCharAt(serverUrl.length() - 1);
        }
        return serverUrl.toString();
    }

    /**
     * Get the axis service path
     *
     * @return String
     */
    public static String getServicePath() {
        return IdentityCoreServiceComponent.getConfigurationContextService().getServerConfigContext().getServicePath();
    }

    /**
     * Constructing the SAML or XACML Objects from a String
     *
     * @param xmlString Decoded SAML or XACML String
     * @return SAML or XACML Object
     * @throws org.wso2.carbon.identity.base.IdentityException
     */
    public static XMLObject unmarshall(String xmlString) throws IdentityException {

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);

            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            org.apache.xerces.util.SecurityManager securityManager = new SecurityManager();
            securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
            documentBuilderFactory.setAttribute(SECURITY_MANAGER_PROPERTY, securityManager);

            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new CarbonEntityResolver());
            Document document = docBuilder.parse(new ByteArrayInputStream(xmlString.trim().getBytes(Charsets.UTF_8)));
            Element element = document.getDocumentElement();
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            return unmarshaller.unmarshall(element);
        } catch (ParserConfigurationException | UnmarshallingException | SAXException | IOException e) {
            String message = "Error in constructing XML Object from the encoded String";
            throw IdentityException.error(message, e);
        }
    }

    /**
     * Check the case sensitivity of the user store in which the user is in.
     *
     * @param username Full qualified username
     * @return
     */
    public static boolean isUserStoreInUsernameCaseSensitive(String username) {

        boolean isUsernameCaseSensitive = true;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            int tenantId = IdentityTenantUtil.getRealmService().getTenantManager().getTenantId(tenantDomain);
            return isUserStoreInUsernameCaseSensitive(username, tenantId);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while reading user store property CaseInsensitiveUsername. Considering as case " +
                        "sensitive.");
            }
        }
        return isUsernameCaseSensitive;
    }

    /**
     * Check the case sensitivity of the user store in which the user is in.
     *
     * @param username user name with user store domain
     * @param tenantId tenant id of the user
     * @return
     */
    public static boolean isUserStoreInUsernameCaseSensitive(String username, int tenantId) {

        return isUserStoreCaseSensitive(IdentityUtil.extractDomainFromName(username), tenantId);
    }

    /**
     * Check the case sensitivity of the user store.
     *
     * @param userStoreDomain user store domain
     * @param tenantId        tenant id of the user store
     * @return
     */
    public static boolean isUserStoreCaseSensitive(String userStoreDomain, int tenantId) {

        boolean isUsernameCaseSensitive = true;
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID){
            //this is to handle federated scenarios
            return true;
        }
        try {
            org.wso2.carbon.user.core.UserStoreManager userStoreManager = (org.wso2.carbon.user.core
                    .UserStoreManager) IdentityTenantUtil.getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager();
            org.wso2.carbon.user.core.UserStoreManager userAvailableUserStoreManager = userStoreManager
                    .getSecondaryUserStoreManager(userStoreDomain);
            return isUserStoreCaseSensitive(userAvailableUserStoreManager);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while reading user store property CaseInsensitiveUsername. Considering as case " +
                        "sensitive.");
            }
        }
        return isUsernameCaseSensitive;
    }

    /**
     * Check the case sensitivity of the user store.
     *
     * @param userStoreManager
     * @return
     */
    public static boolean isUserStoreCaseSensitive(UserStoreManager userStoreManager) {

        if (userStoreManager == null) {
            //this is done to handle federated scenarios. For federated scenarios, there is no user store manager for
            // the user
            return true;
        }
        String caseInsensitiveUsername = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(IdentityCoreConstants.CASE_INSENSITIVE_USERNAME);
        if (caseInsensitiveUsername == null && log.isDebugEnabled()) {
            log.debug("Error while reading user store property CaseInsensitiveUsername. Considering as case sensitive" +
                    ".");
        }
        return !Boolean.parseBoolean(caseInsensitiveUsername);
    }

    public static boolean isNotBlank(String input) {
        if (StringUtils.isNotBlank(input) && !"null".equals(input.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBlank(String input) {
        if (StringUtils.isBlank(input) || "null".equals(input.trim())) {
            return true;
        } else {
            return false;
        }
    }

    public static int getCleanUpTimeout() {

        String cleanUpTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT);
        if (StringUtils.isBlank(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT_DEFAULT;
        }
        return Integer.parseInt(cleanUpTimeout);
    }

    public static int getCleanUpPeriod(String tenantDomain) {

        String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
        if (StringUtils.isBlank(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.CLEAN_UP_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.CLEAN_UP_PERIOD_DEFAULT;
        }
        return Integer.parseInt(cleanUpPeriod);
    }

    public static String extractDomainFromName(String nameWithDomain) {

        if(nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0){
            String domain = nameWithDomain.substring(0, nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));
            return domain.toUpperCase();
        } else {
            return getPrimaryDomainName();
        }
    }

    /**
     * Appends domain name to the user/role name
     *
     * @param name       user/role name
     * @param domainName domain name
     * @return application name with domain name
     */
    public static String addDomainToName(String name, String domainName) {
        if (domainName != null && name != null && name.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) < 0) {
            if (!UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equalsIgnoreCase(domainName)) {
                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domainName) ||
                        "Workflow".equalsIgnoreCase(domainName) || "Application".equalsIgnoreCase(domainName)) {
                    name = domainName + UserCoreConstants.DOMAIN_SEPARATOR + name;
                } else {
                    name = domainName.toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR + name;
                }
            }
        }
        return name;
    }

    public static String getPrimaryDomainName() {
        RealmConfiguration realmConfiguration = IdentityTenantUtil.getRealmService().getBootstrapRealmConfiguration();
        if(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) != null){
            return realmConfiguration.getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME).toUpperCase();
        } else {
            return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
    }

    public static boolean isValidFileName(String fileName){
        String fileNameRegEx = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.FILE_NAME_REGEX);

        if(isBlank(fileNameRegEx)){
            fileNameRegEx = DEFAULT_FILE_NAME_REGEX;
        }

        Pattern pattern = Pattern.compile(fileNameRegEx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE |
                                                                   Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }

    /**
     * Replace the placeholders with the related values in the URL.
     * @param urlWithPlaceholders URL with the placeholders.
     * @return URL filled with the placeholder values.
     */
    public static String fillURLPlaceholders(String urlWithPlaceholders) {

        if (StringUtils.isBlank(urlWithPlaceholders)) {
            return urlWithPlaceholders;
        }

        // First replace carbon placeholders and then move on to identity related placeholders.
        urlWithPlaceholders = Utils.replaceSystemProperty(urlWithPlaceholders);

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_HOST)) {

            String hostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);

            if (hostName == null) {
                try {
                    hostName = NetworkUtils.getLocalHostname();
                } catch (SocketException e) {
                    throw IdentityRuntimeException.error("Error while trying to read hostname.", e);
                }
            }

            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_HOST,
                    hostName);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_PORT)) {

            String mgtTransport = CarbonUtils.getManagementTransport();
            AxisConfiguration axisConfiguration = IdentityCoreServiceComponent.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();

            int mgtTransportProxyPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
            String mgtTransportPort = Integer.toString(mgtTransportProxyPort);

            if (mgtTransportProxyPort <= 0) {
                if (StringUtils.equals(mgtTransport, "http")) {
                    mgtTransportPort = System.getProperty(
                            IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY);
                } else {
                    mgtTransportPort = System.getProperty(
                            IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY);
                }
            }

            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_PORT, mgtTransportPort);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP)) {

            String httpPort = System.getProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP_PROPERTY);
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTP,
                    httpPort);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS)) {

            String httpsPort = System.getProperty(IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS_PROPERTY);
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_PORT_HTTPS,
                    httpsPort);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_PROTOCOL)) {

            String mgtTransport = CarbonUtils.getManagementTransport();
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_PROTOCOL,
                    mgtTransport);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_PROXY_CONTEXT_PATH)) {

            String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .PROXY_CONTEXT_PATH);
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_PROXY_CONTEXT_PATH,
                    proxyContextPath);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_WEB_CONTEXT_ROOT)) {

            String webContextRoot = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .WEB_CONTEXT_ROOT);
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_WEB_CONTEXT_ROOT,
                    webContextRoot);
        }

        if (StringUtils.contains(urlWithPlaceholders, CarbonConstants.CARBON_HOME_PARAMETER)) {

            String carbonHome = CarbonUtils.getCarbonHome();
            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    CarbonConstants.CARBON_HOME_PARAMETER,
                    carbonHome);
        }

        if (StringUtils.contains(urlWithPlaceholders, IdentityConstants.CarbonPlaceholders.CARBON_CONTEXT)) {

            String carbonContext = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .WEB_CONTEXT_ROOT);

            if (carbonContext.equals("/")) {
                carbonContext = "";
            }

            urlWithPlaceholders = StringUtils.replace(urlWithPlaceholders,
                    IdentityConstants.CarbonPlaceholders.CARBON_CONTEXT,
                    carbonContext);
        }

        return urlWithPlaceholders;
    }

    /**
     * Check whether the given token value is appropriate to log.
     * @param tokenName Name of the token.
     * @return True if token is appropriate to log.
     */
    public static boolean isTokenLoggable(String tokenName) {

        IdentityLogTokenParser identityLogTokenParser = IdentityLogTokenParser.getInstance();
        Map<String, String> logTokenMap = identityLogTokenParser.getLogTokenMap();

        return Boolean.valueOf(logTokenMap.get(tokenName));
    }

    /**
     * Get the host name of the server.
     * @return Hostname
     */
    public static String getHostName() {

        return ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);
    }

    public static String buildQueryString(Map<String,String[]> parameterMap) throws UnsupportedEncodingException {

        StringBuilder queryString = new StringBuilder("?");
        boolean isFirst = true;
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            for(String paramValue:entry.getValue()) {
                if (isFirst) {
                    queryString.append(entry.getKey());
                    queryString.append("=");
                    queryString.append(paramValue);
                    isFirst = false;
                }
                queryString.append("&");
                queryString.append(entry.getKey());
                queryString.append("=");
                queryString.append(paramValue);

            }
        }
        return URLEncoder.encode(queryString.toString(), "UTF-8");
    }
}
