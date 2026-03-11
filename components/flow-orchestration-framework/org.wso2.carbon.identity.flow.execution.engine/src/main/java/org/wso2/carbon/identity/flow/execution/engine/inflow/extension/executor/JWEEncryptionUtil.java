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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.core.IdentityKeyStoreResolver;
import org.wso2.carbon.identity.core.util.IdentityKeyStoreResolverConstants;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

/**
 * Utility class for JWE encryption and decryption operations used by In-Flow Extension actions.
 *
 * <p><b>Outbound encryption</b> (IS → external service): uses the external service's X.509
 * certificate public key with {@code RSA-OAEP-256} + {@code A256GCM}. This follows the same
 * pattern as {@code PasswordUpdatingUser.encryptCredential()}.</p>
 *
 * <p><b>Inbound decryption</b> (external service → IS): uses the IS server's RSA private key
 * retrieved via {@link IdentityKeyStoreResolver} with
 * {@link IdentityKeyStoreResolverConstants.InboundProtocol#ACTIONS}. This follows the same
 * pattern as {@code OIDCSessionManagementUtil.decryptWithRSA()}.</p>
 */
public final class JWEEncryptionUtil {

    private static final Log LOG = LogFactory.getLog(JWEEncryptionUtil.class);

    /** Number of dot-separated parts in a JWE compact serialization. */
    private static final int JWE_PART_COUNT = 5;

    /** Standard PEM line length per RFC 7468. */
    private static final int PEM_LINE_LENGTH = 64;

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
     * The private key is resolved via {@link IdentityKeyStoreResolver} using
     * {@link IdentityKeyStoreResolverConstants.InboundProtocol#ACTIONS}.
     *
     * @param jweString    The JWE compact serialization string.
     * @param tenantDomain The tenant domain to resolve the private key.
     * @return Decrypted plaintext string.
     * @throws ActionExecutionException If decryption fails.
     */
    public static String decrypt(String jweString, String tenantDomain) throws ActionExecutionException {

        try {
            JWEObject jweObject = JWEObject.parse(jweString);

            Key privateKey = IdentityKeyStoreResolver.getInstance()
                    .getPrivateKey(tenantDomain,
                            IdentityKeyStoreResolverConstants.InboundProtocol.ACTIONS);

            RSADecrypter decrypter = new RSADecrypter((RSAPrivateKey) privateKey);
            jweObject.decrypt(decrypter);

            return jweObject.getPayload().toString();
        } catch (ParseException e) {
            throw new ActionExecutionException(
                    "Failed to parse JWE string from In-Flow Extension response.", e);
        } catch (JOSEException e) {
            throw new ActionExecutionException(
                    "Failed to decrypt JWE value from In-Flow Extension response.", e);
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Error resolving IS private key for In-Flow Extension JWE decryption.", e);
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
     * Parse a PEM-encoded X.509 certificate string into an {@link X509Certificate} object.
     * <p>
     * Handles PEM strings that may have lost their newlines during database storage/retrieval
     * (e.g., via {@code CertificateManagementDAOImpl.getStringValueFromBlob()} which strips
     * line breaks). The method normalizes the PEM format before parsing to ensure the
     * header/footer are on separate lines and Base64 content is properly structured.
     * </p>
     *
     * @param pem The PEM string (with or without proper line breaks).
     * @return The parsed X509Certificate.
     * @throws ActionExecutionException If parsing fails.
     */
    public static X509Certificate parsePEMCertificate(String pem) throws ActionExecutionException {

        try {
            String normalizedPEM = normalizePEM(pem);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(
                    new ByteArrayInputStream(normalizedPEM.getBytes(StandardCharsets.UTF_8)));
        } catch (ActionExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ActionExecutionException(
                    "Failed to parse PEM certificate for In-Flow Extension action.", e);
        }
    }

    /**
     * Normalize a PEM string to ensure it has proper line structure.
     * <p>
     * The CertificateManagementService's DAO layer reads BLOB data using
     * {@code BufferedReader.readLine()} + {@code StringBuilder.append(line)}, which strips
     * all newline characters. This produces a single-line string like:
     * {@code -----BEGIN CERTIFICATE-----MIICxjCC...-----END CERTIFICATE-----}
     * which Java's X509 parser cannot handle.
     * </p>
     * <p>
     * This method extracts the Base64 body, strips all whitespace, and re-wraps it at
     * 64-character lines between proper PEM header/footer markers.
     * </p>
     *
     * @param pem The PEM string (possibly with stripped newlines).
     * @return A properly formatted PEM string.
     * @throws ActionExecutionException If the PEM structure is invalid.
     */
    private static String normalizePEM(String pem) throws ActionExecutionException {

        if (pem == null || pem.isEmpty()) {
            throw new ActionExecutionException("Certificate PEM string is null or empty.");
        }

        String trimmed = pem.trim();

        // Extract the Base64 body by stripping header/footer markers.
        String base64Body = trimmed
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s+", "");

        if (base64Body.isEmpty()) {
            throw new ActionExecutionException("Certificate PEM contains no Base64 data.");
        }

        // Reconstruct proper PEM with 64-char line wrapping (RFC 7468).
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN CERTIFICATE-----\n");
        for (int i = 0; i < base64Body.length(); i += PEM_LINE_LENGTH) {
            sb.append(base64Body, i, Math.min(i + PEM_LINE_LENGTH, base64Body.length()));
            sb.append('\n');
        }
        sb.append("-----END CERTIFICATE-----\n");
        return sb.toString();
    }
}
