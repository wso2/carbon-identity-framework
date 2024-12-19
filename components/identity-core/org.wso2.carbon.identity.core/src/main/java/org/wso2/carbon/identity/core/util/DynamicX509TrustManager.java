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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.utils.security.KeystoreUtils;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.wso2.carbon.identity.core.util.IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED;


/**
 * Gives a singleton javax.net.ssl.X509TrustManager implementation that uses the default carbon trust store.
 * This will load any changes (addition/removal of certificates) done to the default trust store on the fly.
 */
public class DynamicX509TrustManager implements X509TrustManager {

    private static Log log = LogFactory.getLog(DynamicX509TrustManager.class);
    private X509TrustManager trustManager;
    private static DynamicX509TrustManager instance;

    //Configuration Options
    private static final ServerConfiguration config = ServerConfiguration.getInstance();
    private static final String TRUST_STORE_LOCATION = config.getFirstProperty("Security.TrustStore.Location");
    private static final String TRUST_STORE_TYPE = config.getFirstProperty("Security.TrustStore.Type");

    private DynamicX509TrustManager() throws Exception {

        setupTrustManager();
    }

    public static DynamicX509TrustManager getInstance() throws Exception {

        if (instance == null) {
            instance = new DynamicX509TrustManager();
        }
        return instance;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        trustManager.checkClientTrusted(x509Certificates, s);
    }

    /**
     * Checks the validity of passed x509Certificate certificate chain
     *
     * @param x509Certificates
     * @param s
     * @throws CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        try {
            //if changes were made to the trust store, reload the trust store and initialize the trustManager instance.
            if (Boolean.parseBoolean(System.getProperty(PROP_TRUST_STORE_UPDATE_REQUIRED))) {
                setupTrustManager();
            }
            trustManager.checkServerTrusted(x509Certificates, s);
        } catch (CertificateException e) {
            // Reload the truststore once if SSL validation fails.
            try {
                setupTrustManager();
                trustManager.checkServerTrusted(x509Certificates, s);
            } catch (Exception e1) {
                throw new CertificateException("Certificate validation failed due to " + e1.getCause(), e1);
            }
        } catch (Exception e) {
            throw new CertificateException("Certificate validation failed due to " + e.getCause(), e);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {

        return trustManager.getAcceptedIssuers();
    }


    /**
     * This method reloads the TrustManager by reading the carbon server's default trust store file
     *
     * @throws Exception
     */
    private void setupTrustManager() throws Exception {

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore clientTrustStore = null;
        try (InputStream trustStoreInputStream = new FileInputStream(TRUST_STORE_LOCATION)) {

            clientTrustStore = KeystoreUtils.getKeystoreInstance(TRUST_STORE_TYPE);
            clientTrustStore.load(trustStoreInputStream, null);
            trustManagerFactory.init(clientTrustStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            for (TrustManager t : trustManagers) {
                if (t instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) t;
                    System.setProperty(IdentityUtil.PROP_TRUST_STORE_UPDATE_REQUIRED, Boolean.FALSE.toString());
                    return;
                }
            }
            throw new IdentityException("No X509TrustManager in TrustManagerFactory");
        }
    }
}
