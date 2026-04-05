/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor;

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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for JWE encryption and decryption operations used by In-Flow Extension actions.
 *
 * <p><b>Outbound encryption</b> (IS → external service): uses the external service's X.509
 * certificate public key with {@code RSA-OAEP-256} + {@code A256GCM}. This follows the same
 * pattern as {@code PasswordUpdatingUser.encryptCredential()}.</p>
 *
 * <p><b>Inbound decryption</b> (external service → IS): uses the IS server's RSA private key
 * retrieved via {@link KeyStoreManager} for the tenant. This follows the same
 * pattern as {@code UserAssertionUtils.getPrivateKey()}.</p>
 */
public final class JWEEncryptionUtil {

    private static final Log LOG = LogFactory.getLog(JWEEncryptionUtil.class);

    /** Cache of resolved private keys, keyed by tenant ID. */
    private static final Map<String, Key> PRIVATE_KEYS = new ConcurrentHashMap<>();

    private JWEEncryptionUtil() {

    }

    /**
     * Encrypt a plaintext string using JWE with the external service's X.509 certificate.
     * Uses RSA-OAEP-256 key encryption and A256GCM content encryption.
     *
     * @param plaintext      The plaintext JSON string to encrypt.
     * @param certificatePEM The external service's X.509 certificate in PEM format.
     * @return JWE compact serialization string (5-part dot-separated).
     * @throws ActionExecutionException If encryption fails.
     */
    public static String encrypt(String plaintext, String certificatePEM) throws ActionExecutionException {

        try {
            X509Certificate certificate = parsePEMCertificate(certificatePEM);
            RSAPublicKey publicKey = (RSAPublicKey) certificate.getPublicKey();

            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                    .contentType("application/json")
                    .build();

            JWEObject jweObject = new JWEObject(header, new Payload(plaintext));
            jweObject.encrypt(new RSAEncrypter(publicKey));

            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new ActionExecutionException(
                    "Failed to JWE-encrypt data for In-Flow Extension action.", e);
        }
    }

    /**
     * Decrypt a JWE compact string using the IS server's RSA private key.
     * The private key is resolved via {@link KeyStoreManager} for the tenant.
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
                    "Failed to parse JWE string from In-Flow Extension response.", e);
        } catch (JOSEException e) {
            throw new ActionExecutionException(
                    "Failed to decrypt JWE value from In-Flow Extension response.", e);
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Error resolving IS private key for In-Flow Extension JWE decryption.", e);
        }
    }

    /**
     * Retrieve the IS server's RSA private key for the given tenant.
     * Uses {@link KeyStoreManager} directly, bypassing IdentityKeyStoreResolver,
     * to avoid requiring a protocol-specific keystore mapping.
     *
     * @param tenantDomain The tenant domain.
     * @return The RSA private key.
     * @throws ActionExecutionException If retrieval fails.
     */
    private static Key getPrivateKey(String tenantDomain) throws ActionExecutionException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String cacheKey = String.valueOf(tenantId);
        Key cachedKey = PRIVATE_KEYS.get(cacheKey);
        if (cachedKey != null) {
            return cachedKey;
        }

        try {
            IdentityTenantUtil.initializeRegistry(tenantId);
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(tenantId);
            Key privateKey;

            if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                privateKey = keyStoreManager.getDefaultPrivateKey();
            } else {
                String tenantKeyStoreName = tenantDomain.trim().replace(".", "-") + ".jks";
                privateKey = keyStoreManager.getPrivateKey(tenantKeyStoreName, tenantDomain);
            }

            PRIVATE_KEYS.put(cacheKey, privateKey);
            return privateKey;
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Error retrieving private key for tenant: " + tenantDomain, e);
        }
    }

    /**
     * Detect whether a string value is a JWE compact serialization.
     * A JWE compact serialization has exactly 5 dot-separated Base64url-encoded parts.
     *
     * @param value The value to check.
     * @return {@code true} if the value appears to be a JWE compact string.
     */
    public static boolean isJWEEncrypted(String value) {

        if (value == null || value.isEmpty()) {
            return false;
        }
        // Count dots — JWE compact serialization has exactly 4 dots (5 parts).
        int dotCount = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '.') {
                dotCount++;
                if (dotCount > 4) {
                    return false;
                }
            }
        }
        return dotCount == 4;
    }

    /**
     * Parse a Base64-encoded PEM X.509 certificate string into an {@link X509Certificate} object.
     * Expects the input to be a fully Base64-encoded string of a standard PEM file.
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
            return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(decodedPemBytes));

        } catch (IllegalArgumentException e) {
            throw new ActionExecutionException(
                    "Failed to decode certificate: Input is not valid Base64.", e);
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Failed to parse the decoded PEM certificate for In-Flow Extension action.", e);
        }
    }
}
