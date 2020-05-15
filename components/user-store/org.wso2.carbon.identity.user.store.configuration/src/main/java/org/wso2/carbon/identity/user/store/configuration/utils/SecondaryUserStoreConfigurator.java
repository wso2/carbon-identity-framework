/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.user.store.configuration.utils;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigComponent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import javax.crypto.Cipher;

/**
 * Configuration utility to for encrypting secondary user store properties.
 */
public class SecondaryUserStoreConfigurator {

    private static final Log LOG = LogFactory.getLog(SecondaryUserStoreConfigurator.class);
    private static final String SERVER_KEYSTORE_FILE = "Security.KeyStore.Location";
    private static final String SERVER_KEYSTORE_TYPE = "Security.KeyStore.Type";
    private static final String SERVER_KEYSTORE_PASSWORD = "Security.KeyStore.Password";
    private static final String SERVER_KEYSTORE_KEY_ALIAS = "Security.KeyStore.KeyAlias";
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";
    private static final String SERVER_INTERNAL_KEYSTORE_FILE = "Security.InternalKeyStore.Location";
    private static final String SERVER_INTERNAL_KEYSTORE_TYPE = "Security.InternalKeyStore.Type";
    private static final String SERVER_INTERNAL_KEYSTORE_PASSWORD = "Security.InternalKeyStore.Password";
    private static final String SERVER_INTERNAL_KEYSTORE_KEY_ALIAS = "Security.InternalKeyStore.KeyAlias";
    private static final String ENCRYPTION_KEYSTORE = "Security.UserStorePasswordEncryption";
    private static final String INTERNAL_KEYSTORE = "InternalKeystore";
    private static final String CRYPTO_PROVIDER = "CryptoService.InternalCryptoProviderClassName";
    private static final String SYMMETRIC_KEY_CRYPTO_PROVIDER = "org.wso2.carbon.crypto.provider" +
            ".SymmetricKeyInternalCryptoProvider";
    private Cipher cipher = null;
    private String cipherTransformation = null;
    private Certificate certificate = null;

    /**
     * Initializes the key store and assign it to Cipher object.
     *
     * @throws IdentityUserStoreMgtException Cipher object creation failed
     */
    private void initializeKeyStore(ServerConfigurationService config) throws IdentityUserStoreMgtException {

        if (cipher == null) {

            if (config != null) {

                String filePath = config.getFirstProperty(SERVER_KEYSTORE_FILE);
                String keyStoreType = config.getFirstProperty(SERVER_KEYSTORE_TYPE);
                String password = config.getFirstProperty(SERVER_KEYSTORE_PASSWORD);
                String keyAlias = config.getFirstProperty(SERVER_KEYSTORE_KEY_ALIAS);


                KeyStore store;
                InputStream inputStream = null;

                try {
                    inputStream = new FileInputStream(new File(filePath).getAbsolutePath());
                    store = KeyStore.getInstance(keyStoreType);
                    store.load(inputStream, password.toCharArray());
                    Certificate cert = store.getCertificate(keyAlias);
                    if (cert == null) {
                        throw new IdentityUserStoreMgtException("No certificate found for the given alias.");
                    }
                    if (System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY) != null) {
                        cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
                        certificate = cert;
                        cipher = Cipher.getInstance(cipherTransformation, "BC");
                    } else {
                        cipher = Cipher.getInstance("RSA", "BC");
                    }
                    cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
                } catch (FileNotFoundException e) {
                    String errorMsg = "Keystore File Not Found in configured location";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (IOException e) {
                    String errorMsg = "Keystore File IO operation failed";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (InvalidKeyException e) {
                    String errorMsg = "Invalid key is used to access keystore";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (KeyStoreException e) {
                    String errorMsg = "Faulty keystore";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } catch (GeneralSecurityException e) {
                    String errorMsg = "Some parameters assigned to access the " +
                            "keystore is invalid";
                    throw new IdentityUserStoreMgtException(errorMsg, e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            LOG.error("Exception occurred while trying to close the keystore " +
                                      "file", e);
                        }
                    }
                }
            } else {
                String errMsg = "ServerConfigurationService is null - this situation can't occur";
                LOG.error(errMsg);
            }
        }
    }

    /**
     * Encrypt a given text.
     *
     * @param plainText Cipher text to be encrypted
     * @return Returns the encrypted text
     * @throws IdentityUserStoreMgtException Encryption failed
     */
    public String encryptPlainText(String plainText) throws IdentityUserStoreMgtException {

        boolean isInternalKeyStoreEncryptionEnabled = false;
        boolean isSymmetricKeyEncryptionEnabled = false;
        ServerConfigurationService config =
                UserStoreConfigComponent.getServerConfigurationService();
        if (config != null) {
            String encryptionKeyStore = config.getFirstProperty(ENCRYPTION_KEYSTORE);

            if (INTERNAL_KEYSTORE.equalsIgnoreCase(encryptionKeyStore)) {
                isInternalKeyStoreEncryptionEnabled = true;
            }
            String cryptoProvider = config.getFirstProperty(CRYPTO_PROVIDER);
            if (SYMMETRIC_KEY_CRYPTO_PROVIDER.equalsIgnoreCase(cryptoProvider)) {
                isSymmetricKeyEncryptionEnabled = true;
            }
        }

        if (isInternalKeyStoreEncryptionEnabled && isSymmetricKeyEncryptionEnabled) {

            throw new IdentityUserStoreMgtException(String.format("Userstore encryption can not be supported due to " +
                    "conflicting configurations: '%s' and '%s'. When using internal keystore, assymetric crypto " +
                    "provider should be used.", INTERNAL_KEYSTORE, SYMMETRIC_KEY_CRYPTO_PROVIDER));
        } else if (isInternalKeyStoreEncryptionEnabled || isSymmetricKeyEncryptionEnabled) {

            try {
                return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(plainText.getBytes());
            } catch (CryptoException e) {
                String errorMessage = "Error while encrypting the plain text using internal keystore.";
                throw new IdentityUserStoreMgtException(errorMessage, e);
            }
        } else {
            return encryptWithPrimaryKeyStore(config, plainText);
        }
    }

    private String encryptWithPrimaryKeyStore(ServerConfigurationService config, String plainText)
            throws IdentityUserStoreMgtException {

        try {
            if (config != null) {
                initializeKeyStore(config);
            }
            byte[] encryptedKey = cipher.doFinal((plainText.getBytes()));
            if (cipherTransformation != null) {
                // If cipher transformation is configured via carbon.properties
                encryptedKey = CryptoUtil.getDefaultCryptoUtil()
                        .createSelfContainedCiphertext(encryptedKey, cipherTransformation, certificate);
            }
            return Base64.encode(encryptedKey);
        } catch (GeneralSecurityException e) {
            String errMsg = "Failed to generate the cipher text";
            throw new IdentityUserStoreMgtException(errMsg, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            LOG.error("Error while adding the password - too much data for RSA block");
            throw e;
        }
    }
}
