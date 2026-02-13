/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.security.keystore.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.security.keystore.KeyStoreManagementException;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.security.KeystoreUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.wso2.carbon.security.SecurityConstants.KeyStoreMgtConstants.KEY_STORE_CONTEXT_SEPARATOR;

/**
 * Implementation of the {@link IdentityKeyStoreGenerator} interface for generating and managing
 * context-specific tenant key stores. This class provides functionality to create, manage, and store
 * keystores in a multi-tenant environment, adhering to specific cryptographic requirements.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Generates keystores for tenants dynamically.</li>
 *   <li>Supports various cryptographic algorithms and key generation techniques.</li>
 *   <li>Handles secure persistence of keystores using {@link KeyStoreManager}.</li>
 *   <li>Provides explainable methods for certificate creation, storage, and retrieval.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * This class is intended to be used in environments where context-specific cryptographic needs
 * must be met dynamically.
 *
 * <p><b>Exceptions:</b></p>
 * The methods in this class throw {@link KeyStoreManagementException} for errors encountered during
 * keystore creation, management, or persistence.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.security.keystore.service.IdentityKeyStoreGenerator",
                "service.scope=singleton"
        }
)
public class IdentityKeyStoreGeneratorImpl implements IdentityKeyStoreGenerator {

    private static final Log LOG = LogFactory.getLog(IdentityKeyStoreGeneratorImpl.class);
    private static final String SIGNING_ALG = "Tenant.SigningAlgorithm";

    // Supported signature algorithms for public certificate generation.
    private static final String RSA_MD5 = "MD5withRSA";
    private static final String RSA_SHA1 = "SHA1withRSA";
    private static final String RSA_SHA256 = "SHA256withRSA";
    private static final String RSA_SHA384 = "SHA384withRSA";
    private static final String RSA_SHA512 = "SHA512withRSA";
    private static final String[] signatureAlgorithms = new String[]{
            RSA_MD5, RSA_SHA1, RSA_SHA256, RSA_SHA384, RSA_SHA512
    };
    private static final long CERT_NOT_BEFORE_TIME = 1000L * 60 * 60 * 24 * 30; // 30 days in milliseconds
    private static final long CERT_NOT_AFTER_TIME = 1000L * 60 * 60 * 24 * 365 * 10; // 10 years in milliseconds

    /**
     * Generates a context-specific KeyStore for a given tenant domain if it does not already exist.
     * <p>
     * This method checks whether a KeyStore exists for the specified tenant domain and context.
     * If the KeyStore does not exist, it creates a new one, initializes it, generates the necessary
     * key pairs, and persists it.
     * </p>
     *
     * @param tenantDomain the tenant domain for which the KeyStore is to be generated.
     * @param context      the specific context for which the KeyStore is to be generated.
     * @throws KeyStoreManagementException if an error occurs during the KeyStore creation or initialization.
     */
    public void generateKeyStore(String tenantDomain, String context) throws KeyStoreManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);

        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            if (isContextKeyStoreExists(context, tenantDomain, keyStoreManager)) {
                return; // KeyStore already exists, no need to create again
            }
            // Create the KeyStore
            String password = generatePassword();
            KeyStore keyStore = KeystoreUtils.getKeystoreInstance(
                    KeystoreUtils.getKeyStoreFileType(tenantDomain));
            keyStore.load(null, password.toCharArray());
            generateContextKeyPair(keyStore, context, tenantDomain, password);
            persistContextKeyStore(keyStore, context, tenantDomain, password, keyStoreManager);
        } catch (Exception e) {
            String msg = "Error while instantiating a keystore";
            throw new KeyStoreManagementException(msg, e);
        }
    }

    private boolean isContextKeyStoreExists(String context, String tenantDomain, KeyStoreManager keyStoreManager)
            throws KeyStoreManagementException {

        String ksName = tenantDomain.trim().replace(".", "-");
        ksName = buildDomainWithContext(ksName, context);
        String keyStoreName = KeystoreUtils.getKeyStoreFileLocation(ksName, tenantDomain);
        boolean isKeyStoreExists = false;
        try {
            keyStoreManager.getKeyStore(keyStoreName);
            isKeyStoreExists = true;
        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().contains("Key Store with a name: " + keyStoreName
                    + " does not exist.")) {

                String msg = "Key store not exits. Proceeding to create keystore : " + keyStoreName;
                LOG.debug(msg + e.getMessage());
            } else {
                String msg = "Error while checking the existence of keystore.";
                throw new KeyStoreManagementException(msg, e);
            }
        } catch (Exception e) {
            String msg = "Error while checking the existence of keystore.";
            throw new KeyStoreManagementException(msg, e);
        }
        return isKeyStoreExists;
    }

    /**
     * This method is used to generate a random password for the generated keystore
     *
     * @return generated password
     */
    private String generatePassword() {

        SecureRandom random = new SecureRandom();
        String randString = new BigInteger(130, random).toString(12);
        return randString.substring(randString.length() - 10);
    }

    /**
     * Persists a context-specific KeyStore for a given tenant domain.
     * <p>
     * This method stores the provided KeyStore in a persistent storage using the {@code KeyStoreManager}.
     * It generates a KeyStore name based on the tenant domain and context, converts the KeyStore
     * into a byte array, and saves it securely along with the provided password.
     * </p>
     *
     * @param keyStore       the KeyStore to be persisted.
     * @param context        the specific context for which the KeyStore is being persisted.
     * @param tenantDomain   the tenant domain associated with the KeyStore.
     * @param password       the password used to protect the KeyStore.
     * @param keyStoreManager the {@code KeyStoreManager} instance responsible for managing the persistence of the KeyStore.
     * @throws KeyStoreManagementException if an error occurs while persisting the KeyStore or if security issues arise.
     */
    private void persistContextKeyStore(KeyStore keyStore, String context, String tenantDomain, String password,
                                        KeyStoreManager keyStoreManager) throws KeyStoreManagementException {

        String keyStoreName = generateContextKSNameFromDomainName(context, tenantDomain);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        char[] passwordChar = password.toCharArray();
        try {
            keyStore.store(outputStream, passwordChar);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            String msg = "Error occurred while storing the keystore or processing the public certificate for tenant: "
                    + tenantDomain + " and context: " + context + ". Ensure the keystore is valid and writable.";
            throw new KeyStoreManagementException(msg, e);
        }

        try {

            keyStoreManager.addKeyStore(outputStream.toByteArray(), keyStoreName,
                    passwordChar, " ", KeystoreUtils.getKeyStoreFileType(tenantDomain), passwordChar);
        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().contains("Key store " + keyStoreName + " already available")) {

                LOG.warn("Key store " + keyStoreName + " is already available, ignoring.");
            } else {

                String msg = "Error when adding a keyStore";
                throw new KeyStoreManagementException(msg, e);
            }
        }
    }

    /**
     * This method generates the keypair and stores it in the keystore.
     *
     * @param keyStore       the KeyStore to be persisted.
     * @param context        the specific context for which the KeyStore is being persisted.
     * @param tenantDomain   the tenant domain associated with the KeyStore.
     * @param password       the password used to protect the KeyStore.
     * @throws KeyStoreManagementException Error when generating key pair
     */
    private void generateContextKeyPair(KeyStore keyStore, String context, String tenantDomain, String password)
            throws KeyStoreManagementException {

        try {
            CryptoUtil.getDefaultCryptoUtil();
            // Generate key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Common Name and alias for the generated certificate
            String commonName = "CN=" + buildDomainWithContext(tenantDomain, context)
                    + ", OU=None, O=None, L=None, C=None";

            // Generate certificates
            X500Name distinguishedName = new X500Name(commonName);

            Date notBefore = new Date(System.currentTimeMillis() - CERT_NOT_BEFORE_TIME);
            Date notAfter = new Date(System.currentTimeMillis() + CERT_NOT_AFTER_TIME);

            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            BigInteger serialNumber = new BigInteger(32, new SecureRandom());

            X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                    distinguishedName,
                    serialNumber,
                    notBefore,
                    notAfter,
                    distinguishedName,
                    subPubKeyInfo
            );

            String algorithmName = getSignatureAlgorithm();
            JcaContentSignerBuilder signerBuilder =
                    new JcaContentSignerBuilder(algorithmName).setProvider(getJCEProvider());
            PrivateKey privateKey = keyPair.getPrivate();
            X509Certificate x509Cert = new JcaX509CertificateConverter().setProvider(getJCEProvider())
                    .getCertificate(certificateBuilder.build(signerBuilder.build(privateKey)));

            // Add private key to KS
            keyStore.setKeyEntry(buildDomainWithContext(tenantDomain, context),
                    keyPair.getPrivate(), password.toCharArray(),
                    new java.security.cert.Certificate[]{x509Cert});
        } catch (Exception ex) {
            String msg = "Error while generating the Context certificate for tenant :" +
                    tenantDomain + ".";
            throw new KeyStoreManagementException(msg, ex);
        }
    }

    private static String getSignatureAlgorithm() {

        String algorithm = ServerConfiguration.getInstance().getFirstProperty(SIGNING_ALG);
        // Find in a list of supported signature algorithms.
        for (String supportedAlgorithm : signatureAlgorithms) {
            if (supportedAlgorithm.equalsIgnoreCase(algorithm)) {
                return supportedAlgorithm;
            }
        }
        return RSA_MD5;
    }

    private static String getJCEProvider() {

        String provider = ServerConfiguration.getInstance().getFirstProperty(ServerConstants.JCE_PROVIDER);
        if (!StringUtils.isBlank(provider)) {
            return provider;
        }
        return ServerConstants.JCE_PROVIDER_BC;
    }

    /**
     * This method generates the key store file name from the Domain Name
     *  @return keystore name.
     */
    private String generateContextKSNameFromDomainName(String context, String tenantDomain)
            throws KeyStoreManagementException {

        String ksName = tenantDomain.trim().replace(".", "-");
        ksName = buildDomainWithContext(ksName, context);
        return (ksName + KeystoreUtils.getKeyStoreFileExtension(tenantDomain));
    }

    /**
     * Concatenates ksName and context with the separator.
     *
     * @param ksName the key store name
     * @param context the context
     * @return a concatenated string in the format ksName:context
     */
    private String buildDomainWithContext(String ksName, String context) throws KeyStoreManagementException {

        if (ksName == null || context == null) {
            throw new KeyStoreManagementException("ksName and context must not be null");
        }
        return ksName + KEY_STORE_CONTEXT_SEPARATOR + context;
    }
}
