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
import org.wso2.carbon.identity.client.attestation.mgt.validators.ClientAttestationValidator;

import java.text.ParseException;

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
 *     clientAttestationService.validateAttestation(attestationObject, clientId, tenantDomain);
 * // Check the validation result and obtain client attestation context.
 * ```
 */
public class ClientAttestationServiceImpl implements ClientAttestationService {

    private static final Log LOG = LogFactory.getLog(ClientAttestationServiceImpl.class);

    @Override
    public ClientAttestationContext validateAttestation(String attestationObject,
                                                        String clientId, String tenantDomain) {

        ClientAttestationContext clientAttestationContext = new ClientAttestationContext();
        clientAttestationContext.setClientId(clientId);
        clientAttestationContext.setTenantDomain(tenantDomain);

        try {
            ServiceProvider serviceProvider = getServiceProvider(clientId, tenantDomain);

            // Check if the app is subscribed to API-Based Authentication.
            if (!serviceProvider.isAPIBasedAuthenticationEnabled()) {
                // App is not subscribed to API-Based Authentication but is calling for validation.
                // This is a potential attack, so reject the request.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("App :" + serviceProvider.getApplicationResourceId() + " in tenant : " + tenantDomain +
                            " is not subscribed to API-Based Authentication.");
                }
                clientAttestationContext.setApiBasedAuthenticationEnabled(false);
                clientAttestationContext.setAttestationEnabled(false);
                clientAttestationContext.setAttested(false);
                clientAttestationContext.setErrorMessage("App is not subscribed to API-Based Authentication.");
                return clientAttestationContext;
            }

            // Check if the app is subscribed to client attestation validation.
            if (serviceProvider.getClientAttestationMetaData() == null
                    || !serviceProvider.getClientAttestationMetaData().isAttestationEnabled()) {
                // App is not subscribed to client attestation validation, proceed without validation.
                // This may be a testing scenario, so approve the request.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("App :" + serviceProvider.getApplicationResourceId() + " in tenant : " + tenantDomain +
                            " is not subscribed to Client Attestation Service.");
                }
                clientAttestationContext.setApiBasedAuthenticationEnabled(true);
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
                clientAttestationContext.setApiBasedAuthenticationEnabled(true);
                clientAttestationContext.setAttestationEnabled(true);
                clientAttestationContext.setAttested(false);
                clientAttestationContext.setErrorMessage("App is configured to validate attestation " +
                        "but attestation object is empty.");
                return clientAttestationContext;
            }

            if (isAndroidAttestation(attestationObject)) {

                clientAttestationContext.setApiBasedAuthenticationEnabled(true);
                clientAttestationContext.setAttestationEnabled(true);
                clientAttestationContext.setClientType(Constants.ClientTypes.ANDROID);

                ClientAttestationValidator androidAttestationValidator = new AndroidAttestationValidator(clientId,
                        tenantDomain, serviceProvider.getClientAttestationMetaData());
                androidAttestationValidator.validateAttestation(attestationObject, clientAttestationContext);
                return clientAttestationContext;
            } else {
                handleInvalidAttestationObject(clientAttestationContext);
                return clientAttestationContext;
            }
        } catch (ClientAttestationMgtException e) {
            handleClientAttestationException(e, clientAttestationContext);
            return clientAttestationContext;
        }
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
        clientAttestationContext.setErrorMessage(message);
    }

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

    private ServiceProvider getServiceProvider(String clientId, String tenantDomain)
            throws ClientAttestationMgtException {

        ServiceProvider serviceProvider;
        try {
            serviceProvider = ClientAttestationMgtDataHolder.getInstance().getApplicationManagementService()
                    .getServiceProviderByClientId(clientId, OAUTH2, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new ClientAttestationMgtException("Error occurred while retrieving OAuth2 " +
                    "application data for client id " +
                    clientId, e);
        }
        if (serviceProvider == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not find an application for client id: " + clientId
                        + ", scope: " + OAUTH2 + ", tenant: " + tenantDomain);
            }
            throw new ClientAttestationMgtException("Service Provider not found.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieved service provider: " + serviceProvider.getApplicationName() + " for client: " +
                    clientId + ", scope: " + OAUTH2 + ", tenant: " +
                    tenantDomain);
        }
        return serviceProvider;
    }
}
