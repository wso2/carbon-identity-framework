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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.core.util.KeyStoreManager;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
 *   <li>Ensures thread-safe operations with locking mechanisms for multi-tenant environments.</li>
 *   <li>Provides explainable methods for certificate creation, storage, and retrieval.</li>
 * </ul>
 *
 * <p><b>Concurrency:</b></p>
 * This implementation uses {@link ReentrantLock} to handle concurrent access to keystores for
 * specific tenants, ensuring thread safety and data integrity.
 *
 * <p><b>Usage:</b></p>
 * This class is intended to be used in environments where context-specific cryptographic needs
 * must be met dynamically.
 *
 * <p><b>Exceptions:</b></p>
 * The methods in this class throw {@link KeyStoreManagementException} for errors encountered during
 * keystore creation, management, or persistence.
 */
public class IdentityKeyStoreGeneratorImpl implements IdentityKeyStoreGenerator{

    private static final Log LOG = LogFactory.getLog(IdentityKeyStoreGeneratorImpl.class);
    private String tenantDomain;
    private String password;
    private KeyStoreManager keyStoreManager;
    private static final String SIGNING_ALG = "Tenant.SigningAlgorithm";

    // Supported signature algorithms for public certificate generation.
    private static final String DSA_SHA1 = "SHA1withDSA";
    private static final String ECDSA_SHA1 = "SHA1withECDSA";
    private static final String ECDSA_SHA256 = "SHA256withECDSA";
    private static final String ECDSA_SHA384 = "SHA384withECDSA";
    private static final String ECDSA_SHA512 = "SHA512withECDSA";
    private static final String RSA_MD5 = "MD5withRSA";
    private static final String RSA_SHA1 = "SHA1withRSA";
    private static final String RSA_SHA256 = "SHA256withRSA";
    private static final String RSA_SHA384 = "SHA384withRSA";
    private static final String RSA_SHA512 = "SHA512withRSA";
    private static final String[] signatureAlgorithms = new String[]{
            DSA_SHA1, ECDSA_SHA1, ECDSA_SHA256, ECDSA_SHA384, ECDSA_SHA512, RSA_MD5, RSA_SHA1, RSA_SHA256,
            RSA_SHA384, RSA_SHA512
    };
    private static final ConcurrentHashMap<Integer, ReentrantLock> tenantLocks = new ConcurrentHashMap<>();

    /**
     * Generates a context-specific KeyStore for a given tenant domain.
     * <p>
     * This method creates a new KeyStore for the specified tenant domain and context if it does not already exist.
     * It ensures thread safety by using a lock mechanism to avoid concurrent modifications when creating the KeyStore.
     * If the KeyStore for the given context already exists, the method exits without performing any operations.
     * </p>
     *
     * @param tenantDomain the tenant domain for which the KeyStore is to be created.
     * @param context      the context for which the KeyStore is to be generated.
     * @throws KeyStoreManagementException if an error occurs during KeyStore creation or initialization.
     */
    public void generateContextKeyStore(String tenantDomain, String context) throws KeyStoreManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        this.keyStoreManager = KeyStoreManager.getInstance(tenantId);
        this.tenantDomain = tenantDomain;
        ReentrantLock lock = tenantLocks.computeIfAbsent(tenantId, id -> new ReentrantLock());
        boolean lockAcquired = false; // Track if the lock was acquired

        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            if (isContextKeyStoreExists(context)) {
                return; // KeyStore already exists, no need to create again
            }

            lock.lock(); // Acquire the lock
            lockAcquired = true; // Mark the lock as acquired

            if (!isContextKeyStoreExists(context)) {
                // Create the KeyStore
                password = generatePassword();
                KeyStore keyStore = KeystoreUtils.getKeystoreInstance(
                        KeystoreUtils.getKeyStoreFileType(tenantDomain));
                keyStore.load(null, password.toCharArray());
                generateContextKeyPair(keyStore, context);
                persistContextKeyStore(keyStore, context);
            }

        } catch (Exception e) {
            String msg = "Error while instantiating a keystore";
            LOG.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        } finally {
            if (lockAcquired) { // Only release the lock if it was acquired
                lock.unlock();
            }
            tenantLocks.remove(tenantId); // Clean up locks for this tenant if not needed anymore
        }
    }

    private boolean isContextKeyStoreExists(String context) {

        String keyStoreName = KeystoreUtils.getKeyStoreFileLocation(tenantDomain +
                KEY_STORE_CONTEXT_SEPARATOR + context);
        boolean isKeyStoreExists = false;
        try {
            keyStoreManager.getKeyStore(keyStoreName);
            isKeyStoreExists = true;
        } catch (Exception e) {
            String msg = "Error while checking the existence of keystore.  ";
            LOG.error(msg + e.getMessage());
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
     * Persist the keystore in the gov.registry
     *
     * @param keyStore created Keystore of the tenant
     * @throws KeyStoreManagementException Exception when storing the keystore in the registry
     */
    private void persistContextKeyStore(KeyStore keyStore, String context)
            throws KeyStoreManagementException {

        String keyStoreName = generateContextKSNameFromDomainName(context);
        try {
            char[] passwordChar = password.toCharArray();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            keyStore.store(outputStream, passwordChar);
            outputStream.flush();
            outputStream.close();

            keyStoreManager.addKeyStore(outputStream.toByteArray(), keyStoreName,
                    passwordChar, " ", KeystoreUtils.getKeyStoreFileType(tenantDomain), passwordChar);
        } catch (SecurityException e) {
            if (e.getMessage() != null && e.getMessage().contains("Key store " + keyStoreName + " already available")) {

                LOG.warn("Key store " + keyStoreName + " is already available, ignoring.");
            } else {

                String msg = "Error when adding a keyStore";
                LOG.error(msg, e);
                throw new KeyStoreManagementException(msg, e);
            }
        } catch (Exception e) {

            String msg = "Error when processing keystore/pub. cert to be stored in registry";
            LOG.error(msg, e);
            throw new KeyStoreManagementException(msg, e);
        }
    }

    /**
     * This method generates the keypair and stores it in the keystore
     *
     * @param keyStore A keystore instance
     * @throws KeyStoreManagementException Error when generating key pair
     */
    private void generateContextKeyPair(KeyStore keyStore, String context)
            throws KeyStoreManagementException {
        try {
            CryptoUtil.getDefaultCryptoUtil();
            //generate key pair
            String keyGenerationAlgorithm = getKeyGenerationAlgorithm();
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyGenerationAlgorithm);
            int keySize = getKeySize(keyGenerationAlgorithm);
            if (keySize != 0) {
                keyPairGenerator.initialize(keySize);
            }
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            // Common Name and alias for the generated certificate
            String commonName = "CN=" + tenantDomain +
                    KEY_STORE_CONTEXT_SEPARATOR + context + ", OU=None, O=None, L=None, C=None";

            //generate certificates
            X500Name distinguishedName = new X500Name(commonName);

            Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
            Date notAfter = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10));

            SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            BigInteger serialNumber = BigInteger.valueOf(new SecureRandom().nextInt());

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

            //add private key to KS
            keyStore.setKeyEntry(tenantDomain +KEY_STORE_CONTEXT_SEPARATOR + context,
                    keyPair.getPrivate(), password.toCharArray(),
                    new java.security.cert.Certificate[]{x509Cert});
        } catch (Exception ex) {
            String msg = "Error while generating the Context certificate for tenant :" +
                    tenantDomain + ".";
            LOG.error(msg, ex);
            throw new KeyStoreManagementException(msg, ex);
        }
    }

    private static String getKeyGenerationAlgorithm() {

        String signatureAlgorithm = getSignatureAlgorithm();
        // If the algorithm naming format is {digest}with{encryption}, we need to extract the encryption part.
        int withIndex = signatureAlgorithm.indexOf("with");
        if (withIndex != -1 && withIndex + 4 < signatureAlgorithm.length()) {
            return signatureAlgorithm.substring(withIndex + 4);
        } else {
            // The algorithm name is same as the encryption algorithm.
            // This need to be updated if more algorithms are supported.
            return signatureAlgorithm;
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

    private static int getKeySize(String algorithm) {

        // Initialize the key size according to the FIPS standard.
        // This need to be updated if more algorithms are supported.
        if ("ECDSA".equalsIgnoreCase(algorithm)) {
            return 384;
        } else if ("RSA".equalsIgnoreCase(algorithm) || "DSA".equalsIgnoreCase(algorithm)) {
            return 2048;
        }
        return 0;
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
    private String generateContextKSNameFromDomainName(String context){

        String ksName = tenantDomain.trim().replace(".", "-");
        ksName = ksName + KEY_STORE_CONTEXT_SEPARATOR + context;
        return (ksName + KeystoreUtils.getExtensionByFileType(KeystoreUtils.StoreFileType.defaultFileType()));
    }
}
