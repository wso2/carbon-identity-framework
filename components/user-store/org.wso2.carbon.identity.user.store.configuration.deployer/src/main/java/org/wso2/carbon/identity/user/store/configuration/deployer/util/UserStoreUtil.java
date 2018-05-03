/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.configuration.deployer.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.user.store.configuration.deployer.internal.UserStoreConfigComponent;
import org.wso2.carbon.user.core.UserStoreException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

/**
 * Utility class to perform utility functions when deployer get triggered
 */

public class UserStoreUtil {

    private static Log log = LogFactory.getLog(UserStoreUtil.class);
    private static final String CIPHER_TRANSFORMATION_SYSTEM_PROPERTY = "org.wso2.CipherTransformation";

    /**
     * Function to retrieve @{@link Cipher} initialized for super tenant keystore certificate
     *
     * @return Created Cipher
     * @throws UserStoreException
     * @deprecated
     */
    public static Cipher getCipherOfSuperTenant() throws UserStoreException {
        Cipher cipher;
        try {
            String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
            if (cipherTransformation != null) {
                cipher = Cipher.getInstance(cipherTransformation, "BC");
            } else {
                cipher = Cipher.getInstance("RSA", "BC");
            }
            cipher.init(Cipher.ENCRYPT_MODE, getCertificate().getPublicKey());
        } catch (InvalidKeyException e) {
            String errorMsg = "Invalid key is used to access keystore";
            throw new UserStoreException(errorMsg, e);
        } catch (KeyStoreException e) {
            String errorMsg = "Faulty keystore";
            throw new UserStoreException(errorMsg, e);
        } catch (GeneralSecurityException e) {
            String errorMsg = "Some parameters assigned to access the " +
                    "keystore is invalid";
            throw new UserStoreException(errorMsg, e);
        }
        return cipher;
    }

    /**
     * Util function to encrypt given plain text using given cipher
     *
     * @param cipher cipher that used to encrypt
     * @param plainTextBytes target plain text to encrypt using the cipher
     * @return Cipher text
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     */
    public static byte[] encrypt(Cipher cipher, byte[] plainTextBytes)
            throws BadPaddingException, IllegalBlockSizeException, UserStoreException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException {

        byte[] cipherText = cipher.doFinal(plainTextBytes);
        // Check whether custom transformation is configured
        if (System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY) != null) {
            cipherText = CryptoUtil.getDefaultCryptoUtil().createSelfContainedCiphertext(cipherText,
                    System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY), getCertificate());
        }
        return cipherText;
    }

    /**
     * Util function to encrypt given plain text using given cipher
     *
     * @param plainTextBytes target plain text to encrypt using the cipher
     * @return Cipher text
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     */
    public static byte[] encrypt(byte[] plainTextBytes) throws CryptoException {

        Cipher cipher;
        Certificate certificate;
        String cipherTransformation = System.getProperty(CIPHER_TRANSFORMATION_SYSTEM_PROPERTY);
        try {
            certificate = getCertificate();
        } catch (UserStoreException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new CryptoException("Error occurred while retrieving the certificate.", e);
        }

        try {
            if (cipherTransformation != null) {
                cipher = Cipher.getInstance(cipherTransformation, "BC");
            } else {
                cipher = Cipher.getInstance("RSA", "BC");
            }
            cipher.init(Cipher.ENCRYPT_MODE, certificate.getPublicKey());
        } catch (NoSuchPaddingException | NoSuchProviderException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new CryptoException("Error occurred while creating the cipher.", e);
        }

        try {
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            // Check whether custom transformation is configured.
            if (cipherTransformation != null) {
                cipherText = CryptoUtil.getDefaultCryptoUtil()
                        .createSelfContainedCiphertext(cipherText, cipherTransformation, certificate);
            }
            return cipherText;
        } catch (BadPaddingException | NoSuchAlgorithmException | CertificateEncodingException |
                IllegalBlockSizeException e) {
            throw new CryptoException("Error occurred while encrypting.", e);
        }
    }

    /**
     * Function to retrieve certificate
     *
     * @return certificate
     * @throws UserStoreException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    private static Certificate getCertificate()
            throws UserStoreException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        ServerConfigurationService config = UserStoreConfigComponent.getServerConfigurationService();

        if (config == null) {
            String errMsg = "ServerConfigurationService is null - this situation can't occur";
            throw new UserStoreException(errMsg);
        }

        String filePath = config.getFirstProperty(UserStoreConfigurationConstants.SERVER_KEYSTORE_FILE);
        String keyStoreType = config.getFirstProperty(UserStoreConfigurationConstants.SERVER_KEYSTORE_TYPE);
        String password = config.getFirstProperty(UserStoreConfigurationConstants.SERVER_KEYSTORE_PASSWORD);
        String keyAlias = config.getFirstProperty(UserStoreConfigurationConstants.SERVER_KEYSTORE_KEY_ALIAS);

        KeyStore store;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath).getAbsolutePath());
            store = KeyStore.getInstance(keyStoreType);
            store.load(inputStream, password.toCharArray());
            return store.getCertificateChain(keyAlias)[0];
        } catch (FileNotFoundException e) {
            String errorMsg = "Keystore File Not Found in configured location";
            throw new UserStoreException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Keystore File IO operation failed";
            throw new UserStoreException(errorMsg, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Key store file closing failed");
                }
            }
        }
    }
}
