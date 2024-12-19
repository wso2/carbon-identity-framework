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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.playintegrity.v1.PlayIntegrity;
import com.google.api.services.playintegrity.v1.PlayIntegrityRequestInitializer;
import com.google.api.services.playintegrity.v1.model.AppIntegrity;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest;
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenResponse;
import com.google.api.services.playintegrity.v1.model.RequestDetails;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClientAttestationMetaData;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.TimeZone;

import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.CLIENT_ATTESTATION_ALLOWED_WINDOW_IN_MILL_SECOND;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.PLAY_RECOGNIZED;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.UTC;

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
public class AndroidAttestationValidator implements ClientAttestationValidator {

    private static final Log LOG = LogFactory.getLog(AndroidAttestationValidator.class);

    private static final String ANDROID = "ANDROID";
    private String applicationResourceId;
    private String tenantDomain;

    private ClientAttestationMetaData clientAttestationMetaData;

    public AndroidAttestationValidator(String applicationResourceId,
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

    /**
     * Decodes an integrity token and verifies its authenticity using the Google Play Integrity API.
     *
     * @param attestationObject The integrity token to be decoded and verified.
     * @param clientAttestationContext  The context to store the validation results and updated information.
     * @return The response containing the decoded integrity token data.
     * @throws ClientAttestationMgtException Thrown when there is an issue with decoding or verifying the
     * integrity token.
     */
    private DecodeIntegrityTokenResponse decodeIntegrityToken(String attestationObject,
                                                             ClientAttestationContext clientAttestationContext)
            throws ClientAttestationMgtException {
        try {
            String jsonData = clientAttestationMetaData.getAndroidAttestationServiceCredentials();
            if (jsonData == null) {

                throw new ClientAttestationMgtException("Unable to validate attestation, cause " +
                        "AndroidAttestationServiceCredentials is null for application : " + applicationResourceId +
                        "tenant domain : " + tenantDomain);
            }
            // Create an input stream from the JSON data.
            ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonData.getBytes(StandardCharsets.UTF_8));

            // Create a DecodeIntegrityTokenRequest and set the integrity token.
            DecodeIntegrityTokenRequest decodeIntegrityTokenRequest = new DecodeIntegrityTokenRequest();
            decodeIntegrityTokenRequest.setIntegrityToken(attestationObject);

            // Configure credentials from the JSON data.
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromStream(jsonInputStream);
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(serviceAccountCredentials);

            // Initialize HTTP transport and other necessary components.
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            PlayIntegrityRequestInitializer initializer = new PlayIntegrityRequestInitializer();

            // Create the PlayIntegrity service for interacting with the Google Play Integrity API.
            PlayIntegrity playIntegrity = new PlayIntegrity.Builder(httpTransport, jsonFactory, requestInitializer)
                    .setApplicationName(clientAttestationMetaData.getAndroidPackageName())
                    .setGoogleClientRequestInitializer(initializer)
                    .build();

            // Execute the decodeIntegrityToken request and return the response.
            return playIntegrity.v1()
                    .decodeIntegrityToken(clientAttestationMetaData.getAndroidPackageName(),
                            decodeIntegrityTokenRequest)
                    .execute();

        } catch (IOException | GeneralSecurityException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Unable to decode or verify attestation request from Client :" + applicationResourceId +
                        " in tenant : " + tenantDomain + " from google play integrity service." , e);
            }
            throw new ClientAttestationMgtException("Unable to decode or verify the integrity token " +
                    "from google play integrity service.", e);
        }
    }

    /**
     * Validates the overall integrity of the response by checking various components including
     * request details, device integrity, account details, and application integrity.
     *
     * @param decodeIntegrityTokenResponse The response containing the integrity token and various integrity details.
     * @param clientAttestationContext  The context to store the validation results and updated information.
     */
    private void validateIntegrityResponse(DecodeIntegrityTokenResponse decodeIntegrityTokenResponse,
                                           ClientAttestationContext clientAttestationContext) {

        // Validate different aspects of the integrity response, and return true if all checks pass.
        if (validateRequestDetails(decodeIntegrityTokenResponse, clientAttestationContext) &&
                validateAppIntegrity(decodeIntegrityTokenResponse, clientAttestationContext)) {
            clientAttestationContext.setAttested(true);
        }
    }

    /**
     * Validates the request details contained in a DecodeIntegrityTokenResponse to ensure the integrity
     * of the client's request.
     *
     * @param decodeIntegrityTokenResponse The response containing the integrity token and request details.
     * @param clientAttestationContext  The context to store the validation results and updated information.
     * @return True if the request details are valid; false otherwise.
     */
    private boolean validateRequestDetails(DecodeIntegrityTokenResponse decodeIntegrityTokenResponse,
                                           ClientAttestationContext clientAttestationContext) {

        // Extract request details from the response.
        RequestDetails requestDetails = decodeIntegrityTokenResponse.getTokenPayloadExternal().getRequestDetails();

        String expectedPackageName = clientAttestationMetaData.getAndroidPackageName();

        // Get the current time in milliseconds.
        long currentTimeInMillis = Calendar.getInstance(TimeZone.getTimeZone(UTC)).getTimeInMillis();

        // Get the request time from the token response.
        long requestTimeInMillis = requestDetails.getTimestampMillis();

        long allowedWindowMillis;

        String allowedWindow = IdentityUtil.getProperty(CLIENT_ATTESTATION_ALLOWED_WINDOW_IN_MILL_SECOND);


        if (!StringUtils.equals(requestDetails.getRequestPackageName(), expectedPackageName)) {
            // The package name in the request details does not match the requested client.
            clientAttestationContext.setAttested(false);
            clientAttestationContext.setValidationFailureMessage("Package name in the request details does " +
                    "not match with the requested client.");
            return false;
        } else if (StringUtils.isNotEmpty(allowedWindow)) {
            try {
                allowedWindowMillis = Long.parseLong(allowedWindow);
                if (currentTimeInMillis - requestTimeInMillis > allowedWindowMillis) {
                    // The generated Integrity token is considered old, likely due to a replay attack.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Attestation request provided by Client :" + applicationResourceId +
                                " in tenant : " + tenantDomain + " is older than required window.");
                    }
                    clientAttestationContext.setAttested(false);
                    clientAttestationContext.setValidationFailureMessage("Attestation request provided by Client " +
                            "is older than required window.");
                    return false;
                } else {
                    // Request details are valid.
                    return true;
                }
            } catch (NumberFormatException e) {
                LOG.error("Error while parsing attestation allowed window timeout config: " + allowedWindow, e);
                clientAttestationContext.setAttested(false);
                clientAttestationContext.setValidationFailureMessage("Error while parsing attestation allowed window " +
                        "timeout config. Probably a misconfiguration, hence rejecting the request.");
                return false;
            }
        } else {
            // Request details are valid.
            return true;
        }
    }

    /**
     * Validates the integrity of the application based on the recognition verdict provided in
     * a DecodeIntegrityTokenResponse.
     *
     * @param decodeIntegrityTokenResponse The response containing the integrity token and
     *                                     application integrity details.
     * @param clientAttestationContext  The context to store the validation results and updated information.
     * @return True if the application is recognized as "PLAY_RECOGNIZED"; false otherwise.
     * recognition verdict is not as expected.
     */
    private boolean validateAppIntegrity(DecodeIntegrityTokenResponse decodeIntegrityTokenResponse,
                                         ClientAttestationContext clientAttestationContext) {

        // Extract application integrity details from the response.
        AppIntegrity appIntegrity = decodeIntegrityTokenResponse.getTokenPayloadExternal().getAppIntegrity();

        // Check if the application is recognized as "PLAY_RECOGNIZED."
        if (StringUtils.equals(appIntegrity.getAppRecognitionVerdict(), PLAY_RECOGNIZED)) {
            return true;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Attestation request provided by Client :" + applicationResourceId + " in tenant : "
                    + tenantDomain + " is invalid. Application integrity validation failed."
                    + " Unexpected recognition verdict: " + appIntegrity.getAppRecognitionVerdict());
        }
        clientAttestationContext.setAttested(false);
        clientAttestationContext.setValidationFailureMessage("Application integrity validation failed." +
                " Unexpected recognition verdict: " + appIntegrity.getAppRecognitionVerdict());
        return false;
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
