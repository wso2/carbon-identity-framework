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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;

/**
 * Client for calling Admin Services with mutual ssl authentication
 */
public class TenantMgtAdminServiceClient {

    /**
     * Logger for TenantMgtAdminServiceClient class
     */
    private static final Log log = LogFactory.getLog(TenantMgtAdminServiceClient.class);
    /**
     * HTTP POST
     */
    private static final String HTTP_POST = "POST";
    /**
     * Default keystore type of the client
     */
    @Deprecated
    private static String keyStoreType = KeystoreUtils.getKeyStoreFileType(
            MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    /**
     * Default truststore type of the client
     */
    private static String trustStoreType = KeystoreUtils.getTrustStoreFileType();
    /**
     * Default keymanager type of the client
     */
    private static String keyManagerType = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static String trustManagerType = "SunX509"; //Default Trust Manager Type
    /**
     * Default ssl protocol for client
     */
    private static String protocol = "TLSv1.2";
    private static KeyStore keyStore;
    private static KeyStore trustStore;
    private static char[] keyStorePassword;
    private static HttpsURLConnection httpsURLConnection;
    private static SSLSocketFactory sslSocketFactory;

    private TenantMgtAdminServiceClient() {

    }

    /**
     * Load key store with given keystore file.
     *
     * @param keyStorePath     Path to keystore
     * @param keyStorePassword Password of keystore
     * @throws AuthenticationException
     */
    public static void loadKeyStore(String keyStorePath, String keyStorePassword)
            throws AuthenticationException {

        try (InputStream fis = new FileInputStream(keyStorePath)) {
            String fileExtension = keyStorePath.substring(keyStorePath.lastIndexOf("."));
            TenantMgtAdminServiceClient.keyStorePassword = keyStorePassword.toCharArray();
            keyStore = KeystoreUtils.getKeystoreInstance(KeystoreUtils.getFileTypeByExtension(fileExtension));
            keyStore.load(fis, TenantMgtAdminServiceClient.keyStorePassword);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | CarbonException |
                 NoSuchProviderException e) {
            throw new AuthenticationException("Error while trying to load Key Store.", e);
        }
    }

    /**
     * Load trust store with given truststore file
     *
     * @param trustStorePath     Path to truststore
     * @param trustStorePassword Password of truststore
     * @throws AuthenticationException
     */
    public static void loadTrustStore(String trustStorePath, String trustStorePassword)
            throws AuthenticationException {

        try (InputStream is = new FileInputStream(trustStorePath)) {
            trustStore = KeystoreUtils.getKeystoreInstance(trustStoreType);
            trustStore.load(is, trustStorePassword.toCharArray());
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException |
                 NoSuchProviderException e) {
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

    /**
     * Send mutual ssl https post request and return data
     *
     * @param backendURL   URL of the service
     * @param message      Message sent to the URL
     * @param requestProps Request properties
     * @return Received data
     * @throws java.io.IOException
     */
    public static String sendPostRequest(String backendURL, String message, Map<String, String> requestProps) {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        BufferedReader reader = null;
        String response = null;
        URL url = null;

        try {
            url = new URL(backendURL);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestMethod(HTTP_POST);

            if (requestProps != null) {
                for (Map.Entry<String, String> entry : requestProps.entrySet()) {
                    httpsURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            outputStream = httpsURLConnection.getOutputStream();

            if (StringUtils.isNotEmpty(message)) {
                outputStream.write(message.getBytes(StandardCharsets.UTF_8));
            }
            inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;

            while (StringUtils.isNotEmpty(line = reader.readLine())) {
                builder.append(line);
            }
            response = builder.toString();
        } catch (IOException e) {
            log.error("Sending " + HTTP_POST + " request to URL : " + url + "failed.", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("Closing stream for " + url + " failed", e);
            }
        }
        return response;
    }

    @Deprecated
    public static String getKeyStoreType() {
        return keyStoreType;
    }

    @Deprecated
    public static void setKeyStoreType(String keyStoreType) {
        TenantMgtAdminServiceClient.keyStoreType = keyStoreType;
    }

    public static String getTrustStoreType() {
        return trustStoreType;
    }

    public static void setTrustStoreType(String trustStoreType) {
        TenantMgtAdminServiceClient.trustStoreType = trustStoreType;
    }

    public static String getKeyManagerType() {
        return keyManagerType;
    }

    public static void setKeyManagerType(String keyManagerType) {
        TenantMgtAdminServiceClient.keyManagerType = keyManagerType;
    }

    public static String getTrustManagerType() {
        return trustManagerType;
    }

    public static void setTrustManagerType(String trustManagerType) {
        TenantMgtAdminServiceClient.trustManagerType = trustManagerType;
    }

    public static HttpsURLConnection getHttpsURLConnection() {
        return httpsURLConnection;
    }

    public static void setProtocol(String protocol) {
        TenantMgtAdminServiceClient.protocol = protocol;
    }
}
