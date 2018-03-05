/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception
        .SSOConsentServiceException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.List;

/**
 * Interface for SSOConsentService which handles consent during SSO.
 */
public interface SSOConsentService {

    /**
     * Get consent required claims for a given service from a user considering existing user consents.
     *
     * @param serviceProvider       Service provider requesting consent.
     * @param authenticatedUser     Authenticated user requesting consent form.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    ConsentClaimsData getConsentRequiredClaimsWithExistingConsents(ServiceProvider serviceProvider,
                                                                   AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException;

    /**
     * Get consent required claims for a given service from a user ignoring existing user consents.
     *
     * @param serviceProvider       Service provider requesting consent.
     * @param authenticatedUser     Authenticated user requesting consent form.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    ConsentClaimsData getConsentRequiredClaimsWithoutExistingConsents(ServiceProvider serviceProvider,
                                                                      AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException;

    /**
     * Process the provided user consent and creates a consent receipt.
     *
     * @param consentApprovedClaimIds   Consent approved claims by the user.
     * @param serviceProvider           Service provider receiving consent.
     * @param authenticatedUser         Authenticated user providing consent.
     * @param consentClaimsData         Claims which the consent requested for.
     * @throws SSOConsentServiceException If error occurs while processing user consent.
     */
    void processConsent(List<Integer> consentApprovedClaimIds, ServiceProvider serviceProvider,
                                      AuthenticatedUser authenticatedUser, ConsentClaimsData consentClaimsData)
            throws SSOConsentServiceException;

    /**
     * Retrieves claims which a user has provided consent for a given service provider.
     *
     * @param serviceProvider   Service provider to retrieve the consent against.
     * @param authenticatedUser Authenticated user to related to consent claim retrieval.
     * @return List of claim which the user has provided consent for the given service provider.
     * @throws SSOConsentServiceException If error occurs while retrieve user consents.
     */
    List<ClaimMetaData> getClaimsWithConsents(ServiceProvider serviceProvider, AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException;


    /**
     * Specifies whether consent management for SSO is enabled or disabled.
     * @param serviceProvider Service provider to check whether consent management is enabled.
     * @return true if enabled, false otherwise.
     */
    boolean isSSOConsentManagementEnabled(ServiceProvider serviceProvider);
}
