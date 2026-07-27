/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.executor;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Arrays;

/**
 * JWE encryption and decryption helpers for Flow Extension actions.
 */
public final class JWEEncryptionUtil {

    private static final Log LOG = LogFactory.getLog(JWEEncryptionUtil.class);

    // Number of segments in a JWE compact serialization
    private static final int JWE_COMPACT_SEGMENT_COUNT = 5;

    private JWEEncryptionUtil() {

    }

    /**
     * Encrypt a plaintext string using JWE with the external service's X.509 certificate.
     *
     * @param plaintext      The plaintext JSON string to encrypt.
     * @param certificatePEM The external service's X.509 certificate in PEM format.
     * @return JWE compact serialization string.
     * @throws ActionExecutionException If encryption fails.
     */
    public static String encrypt(String plaintext, String certificatePEM) throws ActionExecutionException {

        return encryptPayload(new Payload(plaintext), certificatePEM);
    }

    /**
     * Char-array overload of {@link #encrypt(String, String)}. Encrypts credentials held as a
     * {@code char[]} using JWE, without ever materializing the credentials as an immutable String.
     *
     * @param credentials    The credentials JSON content to encrypt, as a char array.
     * @param certificatePEM The external service's X.509 certificate in PEM format.
     * @return JWE compact serialization string.
     * @throws ActionExecutionException If encryption fails.
     */
    public static String encrypt(char[] credentials, String certificatePEM) throws ActionExecutionException {

        byte[] credentialsBytes = charsToUtf8Bytes(credentials);
        try {
            return encryptPayload(new Payload(credentialsBytes), certificatePEM);
        } finally {
            // Securely wipe the temporary byte array
            Arrays.fill(credentialsBytes, (byte) 0);
        }
    }

    /**
     * Core encryption logic shared by all overloads.
     * @param payload        The Nimbus Payload object containing the data to encrypt.
     * @param certificatePEM The external service's X.509 certificate in PEM format.
     * @return JWE compact serialization string.
     * @throws ActionExecutionException If encryption fails.
     */
    private static String encryptPayload(Payload payload, String certificatePEM) throws ActionExecutionException {

        try {
            X509Certificate certificate = parsePEMCertificate(certificatePEM);
            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                    .contentType("application/json")
                    .build();

            JWEObject jweObject = new JWEObject(header, payload);
            jweObject.encrypt(new RSAEncrypter(publicKey));

            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new ActionExecutionException(
                    "Failed to JWE-encrypt data for Flow Extension action.", e);
        }
    }

    /**
     * Converts a char array to UTF-8 encoded bytes without allocating an intermediate String,
     * so sensitive plaintext never lands in the String pool or as an immutable object.
     */
    private static byte[] charsToUtf8Bytes(char[] chars) {

        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        if (byteBuffer.hasArray()) {
            Arrays.fill(byteBuffer.array(), (byte) 0);
        }
        return bytes;
    }

    /**
     * Decrypt a JWE compact string using the IS server's RSA private key.
     *
     * @param jweString    The JWE compact serialization string.
     * @param tenantDomain The tenant domain to resolve the private key.
     * @return Decrypted plaintext string.
     * @throws ActionExecutionException If decryption fails.
     */
    public static String decrypt(String jweString, String tenantDomain) throws ActionExecutionException {

        try {
            JWEObject jweObject = JWEObject.parse(jweString);

            Key privateKey = getPrivateKey(tenantDomain);

            RSADecrypter decrypter = new RSADecrypter((RSAPrivateKey) privateKey);
            jweObject.decrypt(decrypter);

            return jweObject.getPayload().toString();
        } catch (ParseException e) {
            throw new ActionExecutionException(
                    "Failed to parse JWE string from Flow Extension response.", e);
        } catch (JOSEException e) {
            throw new ActionExecutionException(
                    "Failed to decrypt JWE value from Flow Extension response.", e);
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Error resolving IS private key for Flow Extension JWE decryption.", e);
        }
    }

    private static Key getPrivateKey(String tenantDomain) throws Exception {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        IdentityTenantUtil.initializeRegistry(tenantId);
        KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);

        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            return keyStoreManager.getDefaultPrivateKey();
        }
        String tenantKeyStoreName = IdentityKeyStoreResolverUtil.buildTenantKeyStoreName(tenantDomain);
        return keyStoreManager.getPrivateKey(tenantKeyStoreName, tenantDomain);
    }

    /**
     * Detect whether a string value is a JWE compact serialization.
     *
     * @param value The value to check.
     * @return {@code true} if the value appears to be a JWE compact string.
     */
    public static boolean isJWEEncrypted(String value) {

        if (value == null || value.isEmpty()) {
            return false;
        }
        return isJWECompactSerialization(value);
    }

    /**
     * Check whether {@code value} has the structure of a JWE compact serialization: exactly
     * {@value #JWE_COMPACT_SEGMENT_COUNT} non-empty Base64URL segments separated by dots
     * ({@code header.encryptedKey.iv.ciphertext.tag}). RSA-OAEP always yields a non-empty
     * encrypted-key segment, so an empty segment means this is not a value we produced.
     *
     * @param value The value to check.
     * @return {@code true} if the value has the JWE compact structure.
     */
    private static boolean isJWECompactSerialization(String value) {

        String[] segments = value.split("\\.", -1);
        if (segments.length != JWE_COMPACT_SEGMENT_COUNT) {
            return false;
        }
        for (String segment : segments) {
            if (segment.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parse a Base64-encoded PEM X.509 certificate string into an {@link X509Certificate} object.
     *
     * @param base64EncodedPem The Base64 encoded PEM string.
     * @return The parsed X509Certificate.
     * @throws ActionExecutionException If parsing or decoding fails.
     */
    public static X509Certificate parsePEMCertificate(String base64EncodedPem) throws ActionExecutionException {

        if (base64EncodedPem == null || base64EncodedPem.trim().isEmpty()) {
            throw new ActionExecutionException("Certificate string is null or empty.");
        }

        try {
            byte[] decodedPemBytes = java.util.Base64.getDecoder().decode(base64EncodedPem.trim());

            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate =
                    (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decodedPemBytes));
            certificate.checkValidity();
            return certificate;

        } catch (CertificateExpiredException e) {
            throw new ActionExecutionException(
                    "Certificate has expired for Flow Extension action.", e);
        } catch (CertificateNotYetValidException e) {
            throw new ActionExecutionException(
                    "Certificate is not yet valid for Flow Extension action.", e);
        } catch (IllegalArgumentException e) {
            throw new ActionExecutionException(
                    "Failed to decode certificate: Input is not valid Base64.", e);
        } catch (CertificateException e) {
            throw new ActionExecutionException(
                    "Failed to parse the decoded PEM certificate for Flow Extension action.", e);
        }
    }
}
