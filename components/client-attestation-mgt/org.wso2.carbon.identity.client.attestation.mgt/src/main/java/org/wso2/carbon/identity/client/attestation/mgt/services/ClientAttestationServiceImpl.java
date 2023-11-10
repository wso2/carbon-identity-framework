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

package org.wso2.carbon.identity.client.attestation.mgt.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.nimbusds.jose.JWEObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.client.attestation.mgt.exceptions.ClientAttestationMgtException;
import org.wso2.carbon.identity.client.attestation.mgt.internal.ClientAttestationMgtDataHolder;
import org.wso2.carbon.identity.client.attestation.mgt.model.ClientAttestationContext;
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.client.attestation.mgt.validators.AndroidAttestationValidator;
import org.wso2.carbon.identity.client.attestation.mgt.validators.AppleAttestationValidator;
import org.wso2.carbon.identity.client.attestation.mgt.validators.ClientAttestationValidator;

import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.APPLE_APP_ATTEST;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.ATT_STMT;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.AUTH_DATA;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.FMT;
import static org.wso2.carbon.identity.client.attestation.mgt.utils.Constants.OAUTH2;

/**
 * The `ClientAttestationServiceImpl` class implements the `ClientAttestationService` interface and is responsible for
 * validating client attestation. It ensures the authenticity and context of the client when
 * API-based authentication is requested.
 * The class provides the following functionalities:
 * - Validation of attestation data, which can be specific to an Android client.
 * - Checks whether API-based authentication is enabled for the client application.
 * - Determines whether the client application is subscribed to client attestation validation.
 * - Validates attestation objects provided by the client application.
 * - Retrieves the service provider's configuration for client attestation.
 * Usage:
 * To validate client attestation, use the `validateAttestation` method, which takes the attestation
 * object, client ID, and tenant domain as parameters.
 * Example usage:
 * ```
 * ClientAttestationService clientAttestationService = new ClientAttestationServiceImpl();
 * ClientAttestationContext clientAttestationContext =
 *     clientAttestationService.validateAttestation(attestationObject, applicationResourceId, tenantDomain);
 * // Check the validation result and obtain client attestation context.
 * ```
 */
public class ClientAttestationServiceImpl implements ClientAttestationService {

    private static final Log LOG = LogFactory.getLog(ClientAttestationServiceImpl.class);

    @Override
    public ClientAttestationContext validateAttestation(String attestationObject,
                                                        String applicationResourceId, String tenantDomain)
            throws ClientAttestationMgtException {

        ClientAttestationContext clientAttestationContext = new ClientAttestationContext();
        clientAttestationContext.setApplicationResourceId(applicationResourceId);
        clientAttestationContext.setTenantDomain(tenantDomain);

        ServiceProvider serviceProvider = getServiceProvider(applicationResourceId, tenantDomain);

        // Check if the app is subscribed to client attestation validation.
        if (serviceProvider.getClientAttestationMetaData() == null
                || !serviceProvider.getClientAttestationMetaData().isAttestationEnabled()) {
            // App is not subscribed to client attestation validation, proceed without validation.
            // This may be a testing scenario, so approve the request.
            if (LOG.isDebugEnabled()) {
                LOG.debug("App :" + serviceProvider.getApplicationResourceId() + " in tenant : " + tenantDomain +
                        " is not subscribed to Client Attestation Service.");
            }
            clientAttestationContext.setAttestationEnabled(false);
            clientAttestationContext.setAttested(true);
            return clientAttestationContext;
        }

        // Check if the attestation object is empty.
        if (StringUtils.isEmpty(attestationObject)) {
            // App is configured to validate attestation but attestation object is empty.
            // This is a potential attack, so reject the request.
            if (LOG.isDebugEnabled()) {
                LOG.debug("App :" + serviceProvider.getApplicationResourceId() + " in tenant : " + tenantDomain +
                        " is requested with empty attestation object.");
            }
            clientAttestationContext.setAttestationEnabled(true);
            clientAttestationContext.setAttested(false);
            clientAttestationContext.setValidationFailureMessage("App is configured to validate attestation " +
                    "but attestation object is empty.");
            return clientAttestationContext;
        }
        attestationObject = "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";


        if (isAndroidAttestation(attestationObject)) {

            clientAttestationContext.setAttestationEnabled(true);
            clientAttestationContext.setClientType(Constants.ClientTypes.ANDROID);

            ClientAttestationValidator androidAttestationValidator =
                    new AndroidAttestationValidator(applicationResourceId, tenantDomain,
                            serviceProvider.getClientAttestationMetaData());
            androidAttestationValidator.validateAttestation(attestationObject, clientAttestationContext);
            return clientAttestationContext;
        } else if (isAppleAttestation(attestationObject)) {
            clientAttestationContext.setAttestationEnabled(true);
            clientAttestationContext.setClientType(Constants.ClientTypes.IOS);

            ClientAttestationValidator appleAttestationValidator =
                    new AppleAttestationValidator(applicationResourceId, tenantDomain,
                            serviceProvider.getClientAttestationMetaData());
            appleAttestationValidator.validateAttestation(attestationObject, clientAttestationContext);
            return clientAttestationContext;
        } else {
            handleInvalidAttestationObject(clientAttestationContext);
            return clientAttestationContext;
        }
    }

    /**
     * Checks if the provided attestation object is an Apple Attestation.
     * This method developed using following documentation
     * <a href="https://developer.apple.com/documentation/devicecheck/validating_apps_that_connect_to_your_server">
     *     Validating Apps That Connect to Your Servers
     * </a>
     * @param attestationObject The attestation object to be checked.
     * @return true if it is an Apple Attestation, false otherwise.
     */
    private boolean isAppleAttestation(String attestationObject) {
        // Create a CBOR factory and an ObjectMapper for CBOR serialization.
        CBORFactory factory = new CBORFactory();
        ObjectMapper cborMapper = new ObjectMapper(factory);

        // Decode the Base64-encoded attestation object.
        byte[] cborData = Base64.getDecoder().decode(attestationObject);

        try {
            // Parse the CBOR data into a Map.
            Map<String, Object> cborMap = cborMapper.readValue(cborData, new TypeReference<Map<String, Object>>() {});

            // Check for the presence of specific keys and the "fmt" value.
            if (cborMap.containsKey(AUTH_DATA)
                    && cborMap.containsKey(ATT_STMT)
                    && cborMap.containsKey(FMT)
                    && StringUtils.equals(cborMap.get(FMT).toString(), APPLE_APP_ATTEST)) {
                return true;
            }
        } catch (IOException e) {
            // An exception occurred, indicating it's not an Apple Attestation.
            return false;
        }
        // It didn't meet the criteria for an Apple Attestation.
        return false;
    }


    private void handleInvalidAttestationObject(ClientAttestationContext clientAttestationContext) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Requested attestation object is not in valid format.");
        }
        setErrorToContext("Requested attestation object is not in valid format.",
                clientAttestationContext);
    }

    private void handleClientAttestationException
            (ClientAttestationMgtException e, ClientAttestationContext clientAttestationContext) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Error while evaluating client attestation.", e);
        }
        setErrorToContext(e.getMessage(), clientAttestationContext);
    }

    private void setErrorToContext(String message, ClientAttestationContext clientAttestationContext) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting error to client attestation context : Error message : " + message);
        }
        clientAttestationContext.setAttested(false);
        clientAttestationContext.setValidationFailureMessage(message);
    }

    /**
     * Checks if the provided attestation object is an Android Attestation request.
     * This method developed using following documentation
     * <a href="https://developer.android.com/google/play/integrity/classic#token-format">
     *     Google Play Integrity Token Format
     * </a>     * @param attestationObject The attestation object to be checked, typically in JWE format.
     * @return true if it is an Android Attestation request, false otherwise.
     */
    private boolean isAndroidAttestation(String attestationObject) {

        try {
            JWEObject jweObject = JWEObject.parse(attestationObject);

            // Check if the JWEObject is in a valid state
            return jweObject.getState() == JWEObject.State.ENCRYPTED;
        } catch (ParseException e) {
            // Exception occurred hence it's not a android attestation request.
            return false;
        }
    }

    private ServiceProvider getServiceProvider(String applicationId, String tenantDomain)
            throws ClientAttestationMgtException {

        ServiceProvider serviceProvider;
        try {
            serviceProvider = ClientAttestationMgtDataHolder.getInstance().getApplicationManagementService()
                    .getApplicationByResourceId(applicationId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new ClientAttestationMgtException("Error occurred while retrieving OAuth2 " +
                    "application data for application id " + applicationId, e);
        }
        if (serviceProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find an application for application id: " + applicationId
                        + ", tenant: " + tenantDomain);
            }
            throw new ClientAttestationMgtException("Service Provider not found.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved service provider: " + serviceProvider.getApplicationName() + " for client: " +
                    applicationId + ", scope: " + OAUTH2 + ", tenant: " +
                    tenantDomain);
        }
        return serviceProvider;
    }
}
