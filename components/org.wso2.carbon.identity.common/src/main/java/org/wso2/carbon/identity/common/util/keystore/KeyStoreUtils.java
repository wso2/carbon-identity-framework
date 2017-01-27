///*
// * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.wso2.carbon.identity.common.util.keystore;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.cert.Certificate;
//import java.security.cert.CertificateException;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//import java.util.Base64;
//
///**
// * Keystore utils.
// */
//public class KeyStoreUtils {
//
//    private static final Logger logger = LoggerFactory.getLogger(KeyStoreUtils.class);
//
//    private static volatile KeyStoreUtils instance = null;
//
//    private KeyStoreUtils() {
//
//    }
//
//    public static KeyStoreUtils getInstance() {
//        if (instance == null) {
//            synchronized (KeyStoreUtils.class) {
//                if (instance == null) {
//                    instance = new KeyStoreUtils();
//                }
//            }
//        }
//        return instance;
//    }
//
//    /**
//     * Extract key store filename.
//     *
//     * @param filePath File path of a key store
//     * @return Key store file name
//     */
//    public String extractKeyStoreFileName(String filePath) {
//
//        if (filePath != null) {
//            String name = null;
//            int index = filePath.lastIndexOf('/');
//            if (index != -1) {
//                name = filePath.substring(index + 1);
//            } else {
//                index = filePath.lastIndexOf(File.separatorChar);
//                if (index != -1) {
//                    name = filePath.substring(filePath.lastIndexOf(File.separatorChar));
//                } else {
//                    name = filePath;
//                }
//            }
//            return name;
//        } else {
//            String errorMsg = "Invalid file path: \'NULL\'";
//            logger.debug(errorMsg);
//            throw new IllegalArgumentException(errorMsg);
//        }
//    }
//
//    /**
//     * Generate thumbprint of certificate.
//     *
//     * @param encodedCert Base64 encoded certificate
//     * @return Certificate thumbprint
//     * @throws java.security.NoSuchAlgorithmException Unsupported hash algorithm
//     */
//    public String generateThumbPrint(String encodedCert) throws NoSuchAlgorithmException {
//
//        if (encodedCert != null) {
//            MessageDigest digestValue = null;
//            digestValue = MessageDigest.getInstance("SHA-1");
//            byte[] der = Base64.getDecoder().decode(encodedCert);
//            digestValue.update(der);
//            byte[] digestInBytes = digestValue.digest();
//            String publicCertThumbprint = hexify(digestInBytes);
//            return publicCertThumbprint;
//        } else {
//            String errorMsg = "Invalid encoded certificate: \'NULL\'";
//            logger.debug(errorMsg);
//            throw new IllegalArgumentException(errorMsg);
//        }
//    }
//
//    /**
//     * Generate thumbprint of certificate.
//     *
//     * @param encodedCert Base64 encoded certificate
//     * @return Decoded <code>Certificate</code>
//     * @throws java.security.cert.CertificateException Error when decoding certificate
//     */
//    public Certificate decodeCertificate(String encodedCert) throws CertificateException {
//
//        if (encodedCert != null) {
//            byte[] bytes = Base64.getDecoder().decode(encodedCert);
//            CertificateFactory factory = CertificateFactory.getInstance("X.509");
//            X509Certificate cert = (X509Certificate) factory
//                    .generateCertificate(new ByteArrayInputStream(bytes));
//            return cert;
//        } else {
//            String errorMsg = "Invalid encoded certificate: \'NULL\'";
//            logger.debug(errorMsg);
//            throw new IllegalArgumentException(errorMsg);
//        }
//    }
//
//    /**
//     * Helper method to hexify a byte array.
//     * TODO:need to verify the logic
//     *
//     * @param bytes
//     * @return hexadecimal representation
//     */
//    public String hexify(byte bytes[]) {
//
//        if (bytes != null) {
//            char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c',
//                    'd', 'e', 'f'};
//            StringBuilder buf = new StringBuilder(bytes.length * 2);
//            for (int i = 0; i < bytes.length; ++i) {
//                buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
//                buf.append(hexDigits[bytes[i] & 0x0f]);
//            }
//            return buf.toString();
//        } else {
//            String errorMsg = "Invalid byte array: \'NULL\'";
//            logger.debug(errorMsg);
//            throw new IllegalArgumentException(errorMsg);
//        }
//    }
//}
