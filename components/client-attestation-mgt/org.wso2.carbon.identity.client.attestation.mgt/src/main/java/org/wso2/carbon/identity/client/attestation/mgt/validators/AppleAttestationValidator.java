/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.client.attestation.mgt.validators;

import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClientAttestationMetaData;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The `AndroidAttestationValidator` class is responsible for validating client attestation for Android clients.
 * It ensures the authenticity and integrity of the client's attestation data, which is typically provided in the
 * form of an integrity token.
 * The class provides the following functionalities:
 * - Decoding and verifying the authenticity of the provided integrity token using the Google Play Integrity API.
 * - Validating the overall integrity of the client's request, including request details and application integrity.
 * - Checking if the application is recognized as "PLAY_RECOGNIZED" by the Google Play Integrity API.
 * Usage:
 * To validate client attestation for Android clients, use the `validateAttestation` method, which takes the
 * attestation header and a context to store validation results and updated information.
 * Example usage:
 * ```
 * AndroidAttestationValidator attestationValidator = new AndroidAttestationValidator(applicationResourceId,
 * tenantDomain, metaData);
 * attestationValidator.validateAttestation(attestationHeader, clientAttestationContext);
 * // Check the validation result and obtain client attestation context.
 * ```
 *
 * For more info on Integrity Token,
 * visit  <a href="https://developer.android.com/google/play/integrity/verdicts"> Integrity verdicts </a>}
 */
public class AppleAttestationValidator implements ClientAttestationValidator {

    private static final Log LOG = LogFactory.getLog(AppleAttestationValidator.class);

    private static final String ANDROID = "ANDROID";
    private String applicationResourceId;
    private String tenantDomain;

    private ClientAttestationMetaData clientAttestationMetaData;

    public AppleAttestationValidator(String applicationResourceId,
                                     String tenantDomain,
                                     ClientAttestationMetaData clientAttestationMetaData) {
        this.applicationResourceId = applicationResourceId;
        this.tenantDomain = tenantDomain;
        this.clientAttestationMetaData = clientAttestationMetaData;
    }

    @Override
    public void validateAttestation(String attestationHeader, ClientAttestationContext clientAttestationContext)
            throws ClientAttestationMgtException {

        DecodeIntegrityTokenResponse decodeIntegrityTokenResponse = decodeIntegrityToken(attestationHeader,
                clientAttestationContext);

        if (decodeIntegrityTokenResponse != null) {

            validateIntegrityResponse(decodeIntegrityTokenResponse, clientAttestationContext);
        } else {
            throw new ClientAttestationMgtException("Unable to validate attestation, cause " +
                    "decodeIntegrityTokenResponse is null for application : " + applicationResourceId +
                    "tenant domain : " + tenantDomain);
        }
    }

    public void verifyAppleAttestation(Map<String, Object> cborMap) {

        // Existing logic for extracting rpIdHash
        byte[] authData = (byte[]) cborMap.get("authData");
        byte[] rpIdHash = Arrays.copyOfRange(authData, 0, 32);

        // Additional logic for certificate validation
        Map<String, Object> attStmt = (Map<String, Object>) cborMap.get("attStmt");
        ArrayList<byte[]> x5c = (ArrayList<byte[]>) attStmt.get("x5c");

        try {
            // Assuming appleAttestationCertificate is the PEM-encoded content of the Apple root CA certificate
            String appleAttestationCertificate = "MIICITCCAaegAwIBAgIQC/O+DvHN0uD7jG5yH2IXmDAKBggqhkjOPQQDAzBSMSYw" +
                    "JAYDVQQDDB1BcHBsZSBBcHAgQXR0ZXN0YXRpb24gUm9vdCBDQTETMBEGA1UECgwK" +
                    "QXBwbGUgSW5jLjETMBEGA1UECAwKQ2FsaWZvcm5pYTAeFw0yMDAzMTgxODMyNTNa" +
                    "Fw00NTAzMTUwMDAwMDBaMFIxJjAkBgNVBAMMHUFwcGxlIEFwcCBBdHRlc3RhdGlv" +
                    "biBSb290IENBMRMwEQYDVQQKDApBcHBsZSBJbmMuMRMwEQYDVQQIDApDYWxpZm9y" +
                    "bmlhMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAERTHhmLW07ATaFQIEVwTtT4dyctdh" +
                    "NbJhFs/Ii2FdCgAHGbpphY3+d8qjuDngIN3WVhQUBHAoMeQ/cLiP1sOUtgjqK9au" +
                    "Yen1mMEvRq9Sk3Jm5X8U62H+xTD3FE9TgS41o0IwQDAPBgNVHRMBAf8EBTADAQH/" +
                    "MB0GA1UdDgQWBBSskRBTM72+aEH/pwyp5frq5eWKoTAOBgNVHQ8BAf8EBAMCAQYw" +
                    "CgYIKoZIzj0EAwMDaAAwZQIwQgFGnByvsiVbpTKwSga0kP0e8EeDS4+sQmTvb7vn" +
                    "53O5+FRXgeLhpJ06ysC5PrOyAjEAp5U4xDgEgllF7En3VcE3iexZZtKeYnpqtijV" +
                    "oyFraWVIyd/dganmrduC1bmTBGwD";
            X509Certificate appleRootCA = (X509Certificate) IdentityUtil.convertPEMEncodedContentToCertificate(appleAttestationCertificate);

            // Load the attestation certificate and intermediate CA certificate from the attestation object
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate credCert = (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(x5c.get(0)));
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(x5c.get(1)));

            // Create a CertPath
            List<X509Certificate> certs = Arrays.asList(credCert, caCert);
            CertPath certPath = certificateFactory.generateCertPath(certs);

            // Create a CertPathValidator and validate the certificate chain
            CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
            PKIXParameters params = new PKIXParameters(Collections.singleton(new TrustAnchor(appleRootCA, null)));
            params.setRevocationEnabled(false);

            try {
                certPathValidator.validate(certPath, params);
            } catch (CertPathValidatorException e) {
                // Handle the validation error
                throw new SecurityException("Cert chain validation failed", e);
            }

            // Check if the appIdHash and rpIdHash are equal
            String appId = "QH8DVR4443.com.wso2.attestationApp";
            byte[] appIdHash = getSHA256Hash(appId);

            if (MessageDigest.isEqual(appIdHash, rpIdHash)) {
                LOG.info("Apple attestation successful");
            }
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }


    public void verifyAppleAttestation(Map<String, Object> cborMap) {
        byte[] authData = (byte[]) cborMap.get("authData");
        byte[] rpIdHash = Arrays.copyOfRange(authData, 0, 32);

        String appId = "QH8DVR4443.com.wso2.attestationApp";
        byte[] appIdHash = getSHA256Hash(appId);

        if (MessageDigest.isEqual(appIdHash, rpIdHash)) {
            LOG.info("Apple attestation successful");
        }
    }

    private byte[] getSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to indicate that this class handles Android OS.
     *
     * @return ANDROID.
     */
    @Override
    public String getAttestationValidationType() {

        return ANDROID;
    }
}
