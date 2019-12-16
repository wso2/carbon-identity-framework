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

public class SecondaryUserStoreConfigurator {

    public static final Log log = LogFactory.getLog(SecondaryUserStoreConfigurator.class);
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
    private Cipher cipher = null;
    private String cipherTransformation = null;
    private Certificate certificate = null;

    /**
     * Initializes the key store and assign it to Cipher object.
     *
     * @throws IdentityUserStoreMgtException Cipher object creation failed
     */
    private void initializeKeyStore() throws IdentityUserStoreMgtException {

        if (cipher == null) {
            ServerConfigurationService config =
                    UserStoreConfigComponent.getServerConfigurationService();

            if (config != null) {
                String encryptionKeyStore = config.getFirstProperty(ENCRYPTION_KEYSTORE);

                String filePath = config.getFirstProperty(SERVER_KEYSTORE_FILE);
                String keyStoreType = config.getFirstProperty(SERVER_KEYSTORE_TYPE);
                String password = config.getFirstProperty(SERVER_KEYSTORE_PASSWORD);
                String keyAlias = config.getFirstProperty(SERVER_KEYSTORE_KEY_ALIAS);

                //use internal keystore
                if (INTERNAL_KEYSTORE.equalsIgnoreCase(encryptionKeyStore)) {
                    filePath = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_FILE);
                    keyStoreType = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_TYPE);
                    password = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_PASSWORD);
                    keyAlias = config.getFirstProperty(SERVER_INTERNAL_KEYSTORE_KEY_ALIAS);
                }

                KeyStore store;
                InputStream inputStream = null;

                try {
                    inputStream = new FileInputStream(new File(filePath).getAbsolutePath());
                    store = KeyStore.getInstance(keyStoreType);
                    store.load(inputStream, password.toCharArray());
                    Certificate[] certs = store.getCertificateChain(keyAlias);
                    if(System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY) != null) {
                        cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
                        certificate = certs[0];
                        cipher = Cipher.getInstance(cipherTransformation, "BC");
                    } else {
                        cipher = Cipher.getInstance("RSA", "BC");
                    }
                    cipher.init(Cipher.ENCRYPT_MODE, certs[0].getPublicKey());
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
                            log.error("Exception occurred while trying to close the keystore " +
                                    "file", e);
                        }
                    }
                }
            } else {
                String errMsg = "ServerConfigurationService is null - this situation can't occur";
                log.error(errMsg);
            }
        }
    }

    /**
     * @param plainText Cipher text to be encrypted
     * @return Returns the encrypted text
     * @throws IdentityUserStoreMgtException Encryption failed
     */
    public String encryptPlainText(String plainText) throws IdentityUserStoreMgtException {

        if (cipher == null) {
            initializeKeyStore();
        }

        try {
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
            log.error("Error while adding the password - too much data for RSA block");
            throw e;
        }
    }
}
