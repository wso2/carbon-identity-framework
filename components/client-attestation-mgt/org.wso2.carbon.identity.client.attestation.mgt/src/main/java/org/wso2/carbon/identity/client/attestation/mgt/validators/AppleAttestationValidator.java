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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClientAttestationMetaData;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.internal.ClientAttestationMgtDataHolder;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.ATT_STMT;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.AUTH_DATA;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.CERTIFICATE_EXPIRY_THRESHOLD;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.MILLI_SECOND_IN_DAY;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.PKIX;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.SHA_256;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.X5C;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.X_509_CERTIFICATE_TYPE;

/**
 * Implementation of the {@link ClientAttestationValidator} interface specific to Apple attestation.
 * This class validates attestation responses from Apple devices, ensuring the integrity and authenticity
 * of the attested information. It performs checks on the attestation statement, certificate chain,
 * and authentication data to verify the device's identity.
 * The validation process involves decoding the Base64-encoded attestation object, parsing the CBOR data,
 * and performing various checks on the attestation statement and authentication data. Additionally,
 * it validates the certificate chain using the Apple Root CA and ensures that the reply party Id
 * matches the configured Apple App ID.
 * This method developed using following documentation
 * <a href="https://developer.apple.com/documentation/devicecheck/validating_apps_that_connect_to_your_server">
 *     Validating Apps That Connect to Your Servers
 * </a>
 */
public class AppleAttestationValidator implements ClientAttestationValidator {

    private static final Log LOG = LogFactory.getLog(AppleAttestationValidator.class);
    private static final String IOS = "IOS";
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

        // Create a CBOR factory and an ObjectMapper for CBOR serialization.
        CBORFactory factory = new CBORFactory();
        ObjectMapper cborMapper = new ObjectMapper(factory);

        // Decode the Base64-encoded attestation object.
        byte[] cborData = Base64.getDecoder().decode(attestationHeader);

        try {
            // Parse the CBOR data into a Map.
            Map<String, Object> cborMap = cborMapper.readValue(cborData, new TypeReference<Map<String, Object>>() { });

            if (verifyAppleAttestationStatement(cborMap, clientAttestationContext)
                    && verifyAppleAuthData(cborMap, clientAttestationContext)) {
                clientAttestationContext.setAttested(true);
            }

        } catch (IOException e) {
            // An exception occurred, indicating it's not an Apple Attestation.
            throw new ClientAttestationMgtException("Unable to validate attestation, cause " +
                    "Attestation object is not in expected statement : " + applicationResourceId +
                    "tenant domain : " + tenantDomain);
        }
    }

    /**
     * Verifies the Apple attestation statement by validating the certificate chain.
     *
     * @param cborMap                 CBOR map containing the attestation statement.
     * @param clientAttestationContext Context containing information related to client attestation.
     * @return True if the attestation statement is successfully verified, false otherwise.
     * @throws ClientAttestationMgtException If an error occurs during the verification process.
     */
    private boolean verifyAppleAttestationStatement(Map<String, Object> cborMap,
                                                    ClientAttestationContext clientAttestationContext)
            throws ClientAttestationMgtException {

        // Extract attestation statement from CBOR map
        Object attStmtObject = cborMap.get(ATT_STMT);
        if (!(attStmtObject instanceof Map)) {
            clientAttestationContext.setAttested(false);
            clientAttestationContext.setValidationFailureMessage("Attestation statement validation failed. " +
                    "Attestation statement is not in expected format.");
            return false;
        }
        Map<String, Object> attStmt = (Map<String, Object>) attStmtObject;

        // Extract X5C (certificate chain) from attestation statement
        Object x5cObject = attStmt.get(X5C);
        if (!(x5cObject instanceof ArrayList)) {
            clientAttestationContext.setAttested(false);
            clientAttestationContext.setValidationFailureMessage("Attestation statement validation failed. " +
                    "X5C is not in expected format.");
            return false;
        }
        ArrayList<byte[]> x5c = (ArrayList<byte[]>) x5cObject;

        try {
            // Retrieve the Apple attestation root certificate
            X509Certificate appleRootCA = ClientAttestationMgtDataHolder.getInstance()
                    .getAppleAttestationRootCertificate();
            if (appleRootCA == null) {
                throw new ClientAttestationMgtException("Unable to validate attestation, apple attestation root " +
                        "certificate is not found. ");
            }
            // Warn if the Apple attestation root certificate is expiring soon
            if (isCertificateExpiringSoon(appleRootCA)) {
                LOG.warn("Provided apple attestation root certificate is going to expire soon. " +
                        "Please add the latest certificate.");
            }

            // Load the attestation certificate and intermediate CA certificate from the attestation object
            CertificateFactory certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE_TYPE);
            X509Certificate credCert = (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(x5c.get(0)));
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(
                    new ByteArrayInputStream(x5c.get(1)));

            // Create a CertPath
            List<X509Certificate> certs = Arrays.asList(credCert, caCert);
            CertPath certPath = certificateFactory.generateCertPath(certs);

            // Create a CertPathValidator and validate the certificate chain
            CertPathValidator certPathValidator = CertPathValidator.getInstance(PKIX);
            PKIXParameters params = new PKIXParameters(Collections.singleton(new TrustAnchor(appleRootCA, null)));
            // In the context of PKIX (Public Key Infrastructure for X.509), revocation refers to the process of
            // declaring a digital certificate as invalid before its natural expiration date.
            // In Development, this is not necessary; hence, provide a config to configure revocation.
            params.setRevocationEnabled(ClientAttestationMgtDataHolder.getInstance()
                    .isAppleAttestationRevocationCheckEnabled());

            try {
                certPathValidator.validate(certPath, params);
                return true;
            } catch (CertPathValidatorException e) {
                // Handle the validation error
                clientAttestationContext.setAttested(false);
                clientAttestationContext.setValidationFailureMessage("Attestation statement validation failed. " +
                        "Certificate path validation failed for application : " + applicationResourceId +
                        " tenant domain : " + tenantDomain);
                return false;
            }
        } catch (InvalidAlgorithmParameterException | CertificateException | NoSuchAlgorithmException e) {
            throw new ClientAttestationMgtException("Unable to validate attestation, due to exception while " +
                    "validating attestation statement ", e);
        }
    }


    /**
     * Verifies Apple Attestation data by comparing the reply party Id (rpIdHash) with the configured Apple App ID.
     *
     * @param cborMap The CBOR map containing authentication data.
     * @param clientAttestationContext The context object for client attestation.
     * @return True if the verification is successful, false otherwise.
     * @throws ClientAttestationMgtException If an error occurs during the verification process.
     */
    private boolean verifyAppleAuthData(Map<String, Object> cborMap,
                                        ClientAttestationContext clientAttestationContext)
            throws ClientAttestationMgtException {

        // Extract rpIdHash from authData
        Object authDataObject = cborMap.get(AUTH_DATA);

        if (!(authDataObject instanceof byte[])) {
            clientAttestationContext.setAttested(false);
            clientAttestationContext.setValidationFailureMessage("Attestation Auth data validation failed. " +
                    "Auth data is not in expected format.");
            return false;
        }
        byte[] authData = (byte[]) authDataObject;
        // As per the official documentation at
        // https://developer.apple.com/documentation/devicecheck/validating_apps_that_connect_to_your_server#3576643
        // rpIdHash(Relaying Party Id) is the hash of the app’s App ID is represented with first 31 byte on authData.
        byte[] rpIdHash = Arrays.copyOfRange(authData, 0, 32);

        // Get the configured Apple App ID
        String appID = clientAttestationMetaData.getAppleAppId();

        if (StringUtils.isNotEmpty(appID)) {
            // Calculate the SHA-256 hash of the App ID
            byte[] appIdHash = getSHA256Hash(appID);

            // Compare the rpIdHash with the calculated App ID hash
            if (MessageDigest.isEqual(appIdHash, rpIdHash)) {
                return true;
            } else {
                // If the comparison fails, update context and return false
                clientAttestationContext.setAttested(false);
                clientAttestationContext.setValidationFailureMessage("Attestation Auth data validation failed. " +
                        "Replying party Id is not matched with app ID for application: " + applicationResourceId +
                        " tenant: " + tenantDomain);
                return false;
            }
        }
        // If the App ID is not configured, update context and return false
        clientAttestationContext.setAttested(false);
        clientAttestationContext.setValidationFailureMessage("Attestation Auth data validation failed. " +
                "App Id is not configured for application: " + applicationResourceId + " tenant: " + tenantDomain);
        return false;
    }

    /**
     * Computes the SHA-256 hash of the input string.
     *
     * @param input The input string for which the SHA-256 hash is to be computed.
     * @return A byte array representing the SHA-256 hash of the input string.
     * @throws ClientAttestationMgtException If the SHA-256 algorithm is not available.
     */
    private byte[] getSHA256Hash(String input) throws ClientAttestationMgtException {

        try {
            // Create a MessageDigest object using the SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance(SHA_256);

            // Compute the hash by converting the input string to bytes and digesting it
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // Throw a client attestation exception if the SHA-256 algorithm is not available
            throw new ClientAttestationMgtException("Unable to validate attestation, cause " +
                    "SHA-256 algorithm is not available." , e);
        }
    }

    private boolean isCertificateExpiringSoon(X509Certificate certificate) {

        Date currentDate = new Date();
        Date expirationDate = certificate.getNotAfter();

        // Calculate the difference in days
        long differenceInDays = (expirationDate.getTime() - currentDate.getTime()) / MILLI_SECOND_IN_DAY;

        // Check if the certificate is expiring within 3 months.
        return differenceInDays <= CERTIFICATE_EXPIRY_THRESHOLD;
    }

    /**
     * Method to indicate that this class handles Android OS.
     *
     * @return The attestation validation type for iOS.
     */
    @Override
    public String getAttestationValidationType() {

        return IOS;
    }
}
