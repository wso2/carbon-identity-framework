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

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MutualSSLManager {

    private static final Log log = LogFactory.getLog(MutualSSLManager.class);
    private static final String PROTECTED_TOKENS = "protectedTokens";
    private static final String DEFAULT_CALLBACK_HANDLER = "org.wso2.carbon.securevault.DefaultSecretCallbackHandler";
    private static final String SECRET_PROVIDER = "secretProvider";
    private static Properties prop;
    private static String carbonLogin = "";
    private static String usernameHeaderName = "";

    /**
     * Default keystore type of the client
     */
    private static final String keyStoreType = "JKS";
    /**
     * Default truststore type of the client
     */
    private static final String trustStoreType = "JKS";
    /**
     * Default keymanager type of the client
     */
    private static final String keyManagerType = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static final String trustManagerType = "SunX509"; //Default Trust Manager Type
    /**
     * Default ssl protocol for client
     */
    private static final String protocol = "TLSv1.2";
    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static char[] keyStorePassword;
    private static SSLSocketFactory sslSocketFactory;

    private MutualSSLManager() {

    }

    /**
     * Initialize Tenant data manager
     */
    public static synchronized void init() {

        try {
            prop = new Properties();
            String configFilePath = buildFilePath(Constants.TenantConstants.CONFIG_RELATIVE_PATH);
            File configFile = new File(configFilePath);

            if (configFile.exists()) {
                log.info(Constants.TenantConstants.CONFIG_FILE_NAME + " file loaded from " + Constants
                        .TenantConstants.CONFIG_RELATIVE_PATH);
                try (InputStream inputStream = new FileInputStream(configFile)) {

                    prop.load(inputStream);
                    //Initialize the keystores in EndpointConfig.properties only if the "mutualSSLManagerEnabled"
                    // feature is enabled.
                    if (isMutualSSLManagerEnabled(getPropertyValue(Constants.TenantConstants.MUTUAL_SSL_MANAGER_ENABLED))) {
                        if (isSecuredPropertyAvailable(prop)) {
                            // Resolve encrypted properties with secure vault
                            resolveSecrets(prop);
                        }
                    }
                }

            } else {
                try (InputStream inputStream = MutualSSLManager.class.getClassLoader()
                        .getResourceAsStream(Constants.TenantConstants.CONFIG_FILE_NAME)) {

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
            }

            //Initialize the keystores in EndpointConfig.properties only if the "mutualSSLManagerEnabled"
            // feature is enabled.
            if (isMutualSSLManagerEnabled(getPropertyValue(Constants.TenantConstants.MUTUAL_SSL_MANAGER_ENABLED))) {
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

                loadKeyStore(clientKeyStorePath, getPropertyValue(Constants.TenantConstants
                        .CLIENT_KEY_STORE_PASSWORD));
                loadTrustStore(clientTrustStorePath, getPropertyValue(Constants.TenantConstants
                        .CLIENT_TRUST_STORE_PASSWORD));
                initMutualSSLConnection(Boolean.parseBoolean(
                        getPropertyValue(Constants.TenantConstants.HOSTNAME_VERIFICATION_ENABLED)));
            }
        } catch (AuthenticationException | IOException e) {
            log.error("Initialization failed : ", e);
        }
    }

    /**
     * Get status of the mutualSSLManagerEnabled feature.
     *
     * @return availability of mutualSSLManagerEnabled feature.
     */
    private static boolean isMutualSSLManagerEnabled(String mutualSSLManagerEnabled) {
        boolean isMutualSSLManagerEnabled = true;
        if (StringUtils.isNotEmpty(mutualSSLManagerEnabled)) {
            isMutualSSLManagerEnabled = Boolean.parseBoolean(mutualSSLManagerEnabled);
        }
        return isMutualSSLManagerEnabled;
    }

    /**
     * Build the absolute path of a give file path
     *
     * @param path File path
     * @return Absolute file path
     * @throws IOException
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
     * There can be sensitive information like passwords in configuration file. If they are encrypted using secure
     * vault, this method will resolve them and replace with original values.
     */
    private static void resolveSecrets(Properties properties) {

        String protectedTokens = (String) properties.get(PROTECTED_TOKENS);

        if (StringUtils.isNotBlank(protectedTokens)) {
            String secretProvider = (String) properties.get(SECRET_PROVIDER);
            SecretResolver secretResolver;

            if (StringUtils.isBlank(secretProvider)) {
                properties.put(SECRET_PROVIDER, DEFAULT_CALLBACK_HANDLER);
            }

            secretResolver = SecretResolverFactory.create(properties, "");
            StringTokenizer st = new StringTokenizer(protectedTokens, ",");

            while (st.hasMoreElements()) {
                String element = st.nextElement().toString().trim();

                if (secretResolver.isTokenProtected(element)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resolving and replacing secret for " + element);
                    }
                    // Replaces the original encrypted property with resolved property
                    properties.put(element, secretResolver.resolve(element));
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

    /**
     * Load key store with given keystore.jks
     *
     * @param keyStorePath     Path to keystore
     * @param keyStorePassword Password of keystore
     * @throws AuthenticationException
     */
    public static void loadKeyStore(String keyStorePath, String keyStorePassword)
            throws AuthenticationException {

        try {
            MutualSSLManager.keyStorePassword = keyStorePassword.toCharArray();
            keyStore = KeyStore.getInstance(keyStoreType);
            try (InputStream fis = new FileInputStream(keyStorePath)) {
                keyStore.load(fis, MutualSSLManager.keyStorePassword);
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new AuthenticationException("Error while trying to load Key Store.", e);
        }
    }

    /**
     * Load trust store with given .jks file
     *
     * @param trustStorePath     Path to truststore
     * @param trustStorePassword Password of truststore
     * @throws AuthenticationException
     */
    public static void loadTrustStore(String trustStorePath, String trustStorePassword)
            throws AuthenticationException {

        try {
            trustStore = KeyStore.getInstance(trustStoreType);
            try (InputStream is = new FileInputStream(trustStorePath)) {
                trustStore.load(is, trustStorePassword.toCharArray());
            }
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new AuthenticationException("Error while trying to load Trust Store.", e);
        }
    }

    /**
     * Create basic SSL connection factory
     *
     * @throws AuthenticationException
     */
    public static void initMutualSSLConnection(boolean hostNameVerificationEnabled)
            throws AuthenticationException {

        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerType);
            keyManagerFactory.init(keyStore, keyStorePassword);
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerType);
            trustManagerFactory.init(trustStore);

            // Create and initialize SSLContext for HTTPS communication
            SSLContext sslContext = SSLContext.getInstance(protocol);

            if (hostNameVerificationEnabled) {
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
                sslSocketFactory = sslContext.getSocketFactory();

                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL Client initialized with Hostname Verification enabled");
                }
            } else {
                // All the code below is to overcome host name verification failure we get in certificate
                // validation due to self signed certificate.

                // Create empty HostnameVerifier
                HostnameVerifier hv = new HostnameVerifier() {
                    @Override
                    public boolean verify(String urlHostName, SSLSession session) {

                        return true;
                    }
                };

                // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {

                        return new java.security.cert.X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                                   String authType) {
                        /*
                             skipped implementation
                        */
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                                   String authType) {
                        /*
                             skipped implementation
                         */
                    }
                }};

                sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());

                if (log.isDebugEnabled()) {
                    log.debug("SSL Context is initialized with trust manager for excluding certificate validation");
                }
                SSLContext.setDefault(sslContext);
                sslSocketFactory = sslContext.getSocketFactory();
                HttpsURLConnection.setDefaultHostnameVerifier(hv);

                if (log.isDebugEnabled()) {
                    log.debug("Mutual SSL Client initialized with Hostname Verification disabled");
                }
            }
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new AuthenticationException("Error while trying to load Trust Store.", e);
        }
    }

    public static SSLSocketFactory getSslSocketFactory() {

        return sslSocketFactory;
    }

    public static String getCarbonLogin() {

        return carbonLogin;
    }

    public static String getUsernameHeaderName() {

        return usernameHeaderName;
    }
}
