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

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.MapUtils;
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
import org.w3c.dom.NodeList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceComponent;
import org.wso2.carbon.identity.core.model.IdentityCacheConfig;
import org.wso2.carbon.identity.core.model.IdentityCacheConfigKey;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfigKey;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;
import sun.security.provider.X509Factory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ALPHABET;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ENCODED_ZERO;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.INDEXES;

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
    private static final String ENABLE_RECOVERY_ENDPOINT = "EnableRecoveryEndpoint";
    private static final String ENABLE_SELF_SIGN_UP_ENDPOINT = "EnableSelfSignUpEndpoint";
    private static Log log = LogFactory.getLog(IdentityUtil.class);
    private static Map<String, Object> configuration = new HashMap<>();
    private static Map<IdentityEventListenerConfigKey, IdentityEventListenerConfig> eventListenerConfiguration = new
            HashMap<>();
    private static Map<IdentityCacheConfigKey, IdentityCacheConfig> identityCacheConfigurationHolder = new HashMap<>();
    private static Map<String, IdentityCookieConfig> identityCookiesConfigurationHolder = new HashMap<>();
    private static Document importerDoc = null;
    private static ThreadLocal<IdentityErrorMsgContext> IdentityError = new ThreadLocal<IdentityErrorMsgContext>();
    private static final int ENTITY_EXPANSION_LIMIT = 0;
    public static final String PEM_BEGIN_CERTFICATE = "-----BEGIN CERTIFICATE-----";
    public static final String PEM_END_CERTIFICATE = "-----END CERTIFICATE-----";
    private static final String APPLICATION_DOMAIN = "Application";
    private static final String WORKFLOW_DOMAIN = "Workflow";

    // System Property for trust managers.
    public static final String PROP_TRUST_STORE_UPDATE_REQUIRED =
            "org.wso2.carbon.identity.core.util.TRUST_STORE_UPDATE_REQUIRED";


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

        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            value = ((List) value).get(0);
        }
        if (value instanceof String) {
            strValue = (String) value;
        } else {
            strValue = String.valueOf(value);
        }
        strValue = fillURLPlaceholders(strValue);
        return strValue;
    }

    public static IdentityEventListenerConfig readEventListenerProperty(String type, String name) {
        IdentityEventListenerConfigKey identityEventListenerConfigKey = new IdentityEventListenerConfigKey(type, name);
        IdentityEventListenerConfig identityEventListenerConfig = eventListenerConfiguration.get(identityEventListenerConfigKey);
        return identityEventListenerConfig;
    }

    /**
     * This reads the &lt;CacheConfig&gt; configuration in identity.xml.
     * Since the name of the cache is different between the distributed mode and local mode,
     * that is specially handled.
     * <p>
     * When calling this method, only pass the cacheManagerName and cacheName parameters considering
     * how the names are set in a clustered environment i.e. without the CachingConstants.LOCAL_CACHE_PREFIX.
     */
    public static IdentityCacheConfig getIdentityCacheConfig(String cacheManagerName, String cacheName) {
        IdentityCacheConfigKey configKey = new IdentityCacheConfigKey(cacheManagerName, cacheName);
        IdentityCacheConfig identityCacheConfig = identityCacheConfigurationHolder.get(configKey);
        if (identityCacheConfig == null && cacheName.startsWith(CachingConstants.LOCAL_CACHE_PREFIX)) {
            configKey = new IdentityCacheConfigKey(cacheManagerName,
                    cacheName.replace(CachingConstants.LOCAL_CACHE_PREFIX, ""));
            identityCacheConfig = identityCacheConfigurationHolder.get(configKey);
        }
        return identityCacheConfig;
    }

    public static IdentityCookieConfig getIdentityCookieConfig(String cookieName) {
        return identityCookiesConfigurationHolder.get(cookieName);
    }


    public static Map<String, IdentityCookieConfig> getIdentityCookiesConfigurationHolder() {
        return identityCookiesConfigurationHolder;
    }

    public static void populateProperties() {
        configuration = IdentityConfigParser.getInstance().getConfiguration();
        eventListenerConfiguration = IdentityConfigParser.getInstance().getEventListenerConfiguration();
        identityCacheConfigurationHolder = IdentityConfigParser.getInstance().getIdentityCacheConfigurationHolder();
        identityCookiesConfigurationHolder = IdentityConfigParser.getIdentityCookieConfigurationHolder();
    }

    public static String getPPIDDisplayValue(String value) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Generating display value of PPID : " + value);
        }
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
        return DOM2Writer.nodeToString(node);
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
        if (hostName.endsWith("/")) {
            hostName = hostName.substring(0, hostName.length() - 1);
        }
        StringBuilder serverUrl = new StringBuilder(mgtTransport).append("://").append(hostName.toLowerCase());
        // If it's well known HTTPS port, skip adding port
        if (mgtTransportPort != IdentityCoreConstants.DEFAULT_HTTPS_PORT) {
            serverUrl.append(":").append(mgtTransportPort);
        }

        // If ProxyContextPath is defined then append it
        if (addProxyContextPath) {
            // If ProxyContextPath is defined then append it
            String proxyContextPath = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants
                    .PROXY_CONTEXT_PATH);
            if (StringUtils.isNotBlank(proxyContextPath)) {
                if (proxyContextPath.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(proxyContextPath.trim());
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
                if (webContextRoot.trim().charAt(0) != '/') {
                    serverUrl.append("/").append(webContextRoot.trim());
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
            serverUrl.setLength(serverUrl.length() - 1);
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
            DocumentBuilderFactory documentBuilderFactory = getSecuredDocumentBuilderFactory();
            documentBuilderFactory.setIgnoringComments(true);
            Document document = getDocument(documentBuilderFactory, xmlString);
            if (isSignedWithComments(document)) {
                documentBuilderFactory.setIgnoringComments(false);
                document = getDocument(documentBuilderFactory, xmlString);
            }
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
     * Return whether SAML Assertion has the canonicalization method
     * set to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'.
     *
     * @param document
     * @return true if canonicalization method equals to 'http://www.w3.org/2001/10/xml-exc-c14n#WithComments'
     */
    private static boolean isSignedWithComments(Document document) {

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            String assertionId = (String) xPath.compile("//*[local-name()='Assertion']/@ID")
                    .evaluate(document, XPathConstants.STRING);

            if (StringUtils.isBlank(assertionId)) {
                return false;
            }

            NodeList nodeList = ((NodeList) xPath.compile(
                    "//*[local-name()='Assertion']" +
                            "/*[local-name()='Signature']" +
                            "/*[local-name()='SignedInfo']" +
                            "/*[local-name()='Reference'][@URI='#" + assertionId + "']" +
                            "/*[local-name()='Transforms']" +
                            "/*[local-name()='Transform']" +
                            "[@Algorithm='http://www.w3.org/2001/10/xml-exc-c14n#WithComments']")
                    .evaluate(document, XPathConstants.NODESET));
            return nodeList != null && nodeList.getLength() > 0;
        } catch (XPathExpressionException e) {
            String message = "Failed to find the canonicalization algorithm of the assertion. Defaulting to: " +
                    "http://www.w3.org/2001/10/xml-exc-c14n#";
            log.warn(message);
            if (log.isDebugEnabled()) {
                log.debug(message, e);
            }
            return false;
        }
    }

    private static Document getDocument(DocumentBuilderFactory documentBuilderFactory, String samlString)
            throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(samlString.getBytes());
        return docBuilder.parse(inputStream);
    }

    /**
     * Create DocumentBuilderFactory with the XXE and XEE prevention measurements.
     *
     * @return DocumentBuilderFactory instance
     */
    public static DocumentBuilderFactory getSecuredDocumentBuilderFactory() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                    Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE +
                    " or secure-processing.");
        }

        SecurityManager securityManager = new SecurityManager();
        securityManager.setEntityExpansionLimit(ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;

    }

    /**
     * Create TransformerFactory with the XXE and XEE prevention measurements.
     *
     * @return TransformerFactory instance
     */
    public static TransformerFactory getSecuredTransformerFactory() {

        TransformerFactory trfactory = TransformerFactory.newInstance();
        try {
            trfactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException e) {
            log.error("Failed to load XML Processor Feature " + XMLConstants.FEATURE_SECURE_PROCESSING +
                    " for secure-processing.");
        }
        trfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        trfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        return trfactory;
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
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            //this is to handle federated scenarios
            return true;
        }
        try {
            UserRealm tenantUserRealm = IdentityTenantUtil.getRealmService().getTenantUserRealm(tenantId);
            if (tenantUserRealm != null) {
                org.wso2.carbon.user.core.UserStoreManager userStoreManager = (org.wso2.carbon.user.core.UserStoreManager) tenantUserRealm
                        .getUserStoreManager();
                org.wso2.carbon.user.core.UserStoreManager userAvailableUserStoreManager = userStoreManager.getSecondaryUserStoreManager(userStoreDomain);
                return isUserStoreCaseSensitive(userAvailableUserStoreManager);
            }
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

    /**
     * This returns whether case sensitive user name can be used as the cache key.
     *
     * @param userStoreManager user-store manager
     * @return true if case sensitive username can be use as cache key
     */
    public static boolean isUseCaseSensitiveUsernameForCacheKeys(UserStoreManager userStoreManager) {

        if (userStoreManager == null) {
            //this is done to handle federated scenarios. For federated scenarios, there is no user store manager for
            // the user
            return true;
        }
        String useCaseSensitiveUsernameForCacheKeys = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(IdentityCoreConstants.USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS);
        if (StringUtils.isBlank(useCaseSensitiveUsernameForCacheKeys)) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to read user store property UseCaseSensitiveUsernameForCacheKeys. Considering as "
                        + "case sensitive.");
            }
            return true;
        }
        return Boolean.parseBoolean(useCaseSensitiveUsernameForCacheKeys);
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

    public static long getCleanUpTimeout() {

        String cleanUpTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT);
        if (StringUtils.isBlank(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.CLEAN_UP_TIMEOUT_DEFAULT;
        }
        return Long.parseLong(cleanUpTimeout);
    }

    public static long getCleanUpPeriod(String tenantDomain) {

        String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLEAN_UP_PERIOD);
        if (StringUtils.isBlank(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.CLEAN_UP_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.CLEAN_UP_PERIOD_DEFAULT;
        }
        return Long.parseLong(cleanUpPeriod);
    }

    public static long getOperationCleanUpTimeout() {

        String cleanUpTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_TIMEOUT);
        if (StringUtils.isBlank(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_TIMEOUT_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_TIMEOUT_DEFAULT;
        }
        return Long.parseLong(cleanUpTimeout);
    }

    public static long getTempDataCleanUpTimeout() {

        String cleanUpTimeout = IdentityUtil.getProperty(IdentityConstants.ServerConfig.TEMP_DATA_CLEAN_UP_TIMEOUT);
        if (StringUtils.isBlank(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.TEMP_DATA_CLEAN_UP_TIMEOUT_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpTimeout)) {
            cleanUpTimeout = IdentityConstants.ServerConfig.TEMP_DATA_CLEAN_UP_TIMEOUT_DEFAULT;
        }
        return Long.parseLong(cleanUpTimeout);
    }

    public static long getOperationCleanUpPeriod(String tenantDomain) {

        String cleanUpPeriod = IdentityUtil.getProperty(IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_PERIOD);
        if (StringUtils.isBlank(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_PERIOD_DEFAULT;
        } else if (!StringUtils.isNumeric(cleanUpPeriod)) {
            cleanUpPeriod = IdentityConstants.ServerConfig.OPERATION_CLEAN_UP_PERIOD_DEFAULT;
        }
        return Long.parseLong(cleanUpPeriod);
    }

    public static String extractDomainFromName(String nameWithDomain) {

        if (nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            String domain = nameWithDomain.substring(0, nameWithDomain.indexOf(UserCoreConstants.DOMAIN_SEPARATOR));
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || WORKFLOW_DOMAIN.equalsIgnoreCase(domain)
                    || APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                return domain.substring(0, 1).toUpperCase() + domain.substring(1).toLowerCase();
            }
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

        if (domainName != null && name != null && !name.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            if (!UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equalsIgnoreCase(domainName)) {
                if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domainName) ||
                        WORKFLOW_DOMAIN.equalsIgnoreCase(domainName) || APPLICATION_DOMAIN.equalsIgnoreCase(domainName)) {
                    name = domainName.substring(0, 1).toUpperCase() + domainName.substring(1).toLowerCase() +
                            UserCoreConstants.DOMAIN_SEPARATOR + name;
                } else {
                    name = domainName.toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR + name;
                }
            }
        }
        return name;
    }

    public static String getPrimaryDomainName() {
        RealmConfiguration realmConfiguration = IdentityTenantUtil.getRealmService().getBootstrapRealmConfiguration();
        if (realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME) != null) {
            return realmConfiguration.getUserStoreProperty(
                    UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME).toUpperCase();
        } else {
            return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
        }
    }

    public static boolean isValidFileName(String fileName) {
        String fileNameRegEx = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.FILE_NAME_REGEX);

        if (isBlank(fileNameRegEx)) {
            fileNameRegEx = DEFAULT_FILE_NAME_REGEX;
        }

        Pattern pattern = Pattern.compile(fileNameRegEx, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE |
                Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }

    /**
     * Replace the placeholders with the related values in the URL.
     *
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
     *
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
     *
     * @return Hostname
     */
    public static String getHostName() {

        String hostName = ServerConfiguration.getInstance().getFirstProperty(IdentityCoreConstants.HOST_NAME);
        if (hostName == null) {
            try {
                hostName = NetworkUtils.getLocalHostname();
            } catch (SocketException e) {
                throw IdentityRuntimeException.error("Error while trying to read hostname.", e);
            }
        }
        return hostName;
    }

    public static String buildQueryString(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {

        return "?" + buildQueryComponent(parameterMap);
    }

    public static String buildFragmentString(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {

        return "#" + buildQueryComponent(parameterMap);
    }

    public static String buildQueryUrl(String baseUrl, Map<String, String[]> parameterMap) throws
            UnsupportedEncodingException {


        if (StringUtils.isBlank(baseUrl)) {
            throw IdentityRuntimeException.error("Base URL is blank: " + baseUrl);
        } else if (baseUrl.contains("#")) {
            throw IdentityRuntimeException.error("Query URL cannot contain \'#\': " + baseUrl);
        }
        StringBuilder queryString = new StringBuilder(baseUrl);

        if (parameterMap != null && parameterMap.size() > 0) {
            if(queryString.indexOf("?") < 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            queryString.append(buildQueryComponent(parameterMap));
        }

        return queryString.toString();
    }

    public static String buildFragmentUrl(String baseUrl, Map<String, String[]> parameterMap) throws
            UnsupportedEncodingException {


        if (StringUtils.isBlank(baseUrl)) {
            throw IdentityRuntimeException.error("Base URL is blank: " + baseUrl);
        } else if (baseUrl.contains("?")) {
            throw IdentityRuntimeException.error("Fragment URL cannot contain \'?\': " + baseUrl);
        }
        StringBuilder queryString = new StringBuilder(baseUrl);
        if (queryString.indexOf("#") < 0) {
            queryString.append("#");
        }
        queryString.append(buildQueryComponent(parameterMap));
        return queryString.toString();
    }

    public static String buildQueryComponent(Map<String, String[]> parameterMap) throws UnsupportedEncodingException {
        if (MapUtils.isEmpty(parameterMap)) {
            return StringUtils.EMPTY;
        }
        StringBuilder queryString = new StringBuilder("");
        boolean isFirst = true;
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            //param value may be empty, hence empty check is avoided.
            if (StringUtils.isBlank(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            for (String paramValue : entry.getValue()) {
                if (paramValue == null) {
                    continue;
                }
                if (isFirst) {
                    isFirst = false;
                } else {
                    queryString.append("&");
                }
                queryString.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
                queryString.append("=");
                queryString.append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8.name()));

            }
        }
        return queryString.toString();
    }

    /**
     * Get client IP address from the http request
     *
     * @param request http servlet request
     * @return IP address of the initial client
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IdentityConstants.HEADERS_WITH_IP) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !IdentityConstants.UNKNOWN.equalsIgnoreCase(ip)) {
                return getFirstIP(ip);
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Get the first IP from a comma separated list of IPs
     *
     * @param commaSeparatedIPs String which contains comma+space separated IPs
     * @return First IP
     */
    public static String getFirstIP(String commaSeparatedIPs) {
        if (StringUtils.isNotEmpty(commaSeparatedIPs) && commaSeparatedIPs.contains(",")) {
            return commaSeparatedIPs.split(",")[0];
        }
        return commaSeparatedIPs;
    }

    /**
     * Get the server synchronization tolerance value in seconds
     *
     * @return clock skew in seconds
     */
    public static int getClockSkewInSeconds() {

        String clockSkewConfigValue = IdentityUtil.getProperty(IdentityConstants.ServerConfig.CLOCK_SKEW);
        if (StringUtils.isBlank(clockSkewConfigValue) || !StringUtils.isNumeric(clockSkewConfigValue)) {
            clockSkewConfigValue = IdentityConstants.ServerConfig.CLOCK_SKEW_DEFAULT;
        }
        return Integer.parseInt(clockSkewConfigValue);
    }

    /**
     * Get the server config for enabling federated user association
     *
     * @return isFederatedUserAssociationEnabled value
     */
    public static boolean isFederatedUserAssociationEnabled() {

        String enableFedUserAssocicationConfigValue = IdentityUtil.getProperty(
                IdentityConstants.ServerConfig.ENABLE_FEDERATED_USER_ASSOCIATION);
        if (StringUtils.isBlank(enableFedUserAssocicationConfigValue)) {
            enableFedUserAssocicationConfigValue =
                    IdentityConstants.ServerConfig.ENABLE_FEDERATED_USER_ASSOCIATION_DEFAULT;
        }

        return Boolean.parseBoolean(enableFedUserAssocicationConfigValue);
    }

    /**
     * Returns whether the passed operation is supported by userstore or not
     *
     * @param userStoreManager User Store
     * @param operation        Operation name
     * @return true if the operation is supported by userstore. False if it doesnt
     */
    public static boolean isSupportedByUserStore(UserStoreManager userStoreManager, String operation) {
        boolean isOperationSupported = true;
        if (userStoreManager != null) {
            String isOperationSupportedProperty = userStoreManager.getRealmConfiguration().getUserStoreProperty
                    (operation);
            if (StringUtils.isNotBlank(isOperationSupportedProperty)) {
                isOperationSupported = Boolean.parseBoolean(isOperationSupportedProperty);
            }
        }
        return isOperationSupported;
    }

    public static boolean isRecoveryEPAvailable() {
        String enableRecoveryEPUrlProperty = getProperty(ENABLE_RECOVERY_ENDPOINT);
        if (StringUtils.isNotBlank(enableRecoveryEPUrlProperty)) {
            return Boolean.parseBoolean(enableRecoveryEPUrlProperty);
        }
        return false;
    }

    public static boolean isSelfSignUpEPAvailable() {
        String enableSelfSignEPUpUrlProperty = getProperty(ENABLE_SELF_SIGN_UP_ENDPOINT);
        if (StringUtils.isNotBlank(enableSelfSignEPUpUrlProperty)) {
            return Boolean.parseBoolean(enableSelfSignEPUpUrlProperty);
        }
        return false;
    }

     /**
     *
     * Converts and returns a {@link Certificate} object for given PEM content.
     *
     * @param certificateContent
     * @return
     * @throws CertificateException
     */
    public static Certificate convertPEMEncodedContentToCertificate(String certificateContent) throws CertificateException {

        certificateContent = getCertificateString(certificateContent);
        byte[] bytes = org.apache.axiom.om.util.Base64.decode(certificateContent);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(bytes));
        return certificate;
    }

    /**
     * Extract certificate content and returns a {@link String} object for given PEM content.
     *
     * @param certificateContent initial certificate content
     * @return
     * @throws CertificateException
     */
    public static String getCertificateString(String certificateContent) {
        String certificateContentString = StringUtils.stripEnd(StringUtils.stripStart(certificateContent,
                PEM_BEGIN_CERTFICATE),
                PEM_END_CERTIFICATE);
        return certificateContentString;
    }

    /**
     *
     * Returns the PEM encoded certificate out of the given certificate object.
     *
     * @param certificate
     * @return PEM encoded certificate as a {@link String}
     * @throws CertificateException
     */
    public static String convertCertificateToPEM(Certificate certificate) throws CertificateException {

        byte[] encodedCertificate = org.apache.commons.codec.binary.Base64.encodeBase64(certificate.getEncoded());

        String encodedPEM = String.format("%s\n%s\n%s", X509Factory.BEGIN_CERT, new String(encodedCertificate),
                X509Factory.END_CERT);

        return encodedPEM;
    }

    /**
     * Checks whether the PEM content is valid.
     *
     * For now only checks whether the certificate is not malformed.
     *
     * @param certificateContent PEM content to be validated.
     * @return true if the content is not malformed, false otherwise.
     */
    public static boolean isValidPEMCertificate(String certificateContent) {

        // Empty content is a valid input since it means no certificate. We only validate if the content is there.
        if (StringUtils.isBlank(certificateContent)) {
            return true;
        }

        try {
            convertPEMEncodedContentToCertificate(certificateContent);
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }

    /**
     * Encodes the given bytes as a base58 string (no checksum is appended).
     *
     * @param input the bytes to encode
     * @return the base58-encoded string
     */
    public static String base58Encode(byte[] input) {

        if (input.length == 0) {
            return "";
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        input = Arrays.copyOf(input, input.length); // since we modify it in-place
        char[] encoded = new char[input.length * 2]; // upper bound
        int outputStart = encoded.length;
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = ALPHABET[divmod(input, inputStart, 256, 58)];
            if (input[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    /**
     * Decodes the given base58 string into the original data bytes.
     *
     * @param input the base58-encoded string to decode
     * @return the decoded data bytes
     * @throws RuntimeException if the given string is not a valid base58 string
     */
    public static byte[] base58Decode(String input) throws RuntimeException {

        if (input.length() == 0) {
            return new byte[0];
        }
        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new RuntimeException(String.format("Invalid character %s at %s", c, i));
            }
            input58[i] = (byte) digit;
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            ++zeros;
        }
        // Convert base-58 digits to base-256 digits.
        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < input58.length; ) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        // Return decoded data (including original number of leading zeros).
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    /**
     * Divides a number, represented as an array of bytes each containing a single digit
     * in the specified base, by the given divisor. The given number is modified in-place
     * to contain the quotient, and the return value is the remainder.
     *
     * @param number     the number to divide
     * @param firstDigit the index within the array of the first non-zero digit
     *                   (this is used for optimization by skipping the leading zeros)
     * @param base       the base in which the number's digits are represented (up to 256)
     * @param divisor    the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    private static byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        // this is just long division which accounts for the base of the input digits
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }
}
