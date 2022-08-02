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

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.claim.mgt.ClaimManagementException;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementClientException;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.AddReceiptResponse;
import org.wso2.carbon.consent.mgt.core.model.ConsentPurpose;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PIICategoryValidity;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposeCategory;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptListResponse;
import org.wso2.carbon.consent.mgt.core.model.ReceiptPurposeInput;
import org.wso2.carbon.consent.mgt.core.model.ReceiptService;
import org.wso2.carbon.consent.mgt.core.model.ReceiptServiceInput;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception.SSOConsentDisabledException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception.SSOConsentServiceException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.ArrayUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_NAME_INVALID;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONFIG_ELEM_CONSENT;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONFIG_ELEM_ENABLE_SSO_CONSENT_MANAGEMENT;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONFIG_PROMPT_SUBJECT_CLAIM_REQUESTED_CONSENT;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONSENT_VALIDITY_TYPE_SEPARATOR;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONSENT_VALIDITY_TYPE_VALID_UNTIL;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.CONSENT_VALIDITY_TYPE_VALID_UNTIL_INDEFINITE;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.FEDERATED_USER_DOMAIN_PREFIX;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.FEDERATED_USER_DOMAIN_SEPARATOR;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.USERNAME_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CONFIG_ENABLE_SCOPE_BASED_CLAIM_FILTERING;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.DESCRIPTION_PROPERTY;
import static org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants.DISPLAY_NAME_PROPERTY;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE;

/**
 * Implementation of {@link SSOConsentService}.
 */
public class SSOConsentServiceImpl implements SSOConsentService {

    private static final Log log = LogFactory.getLog(SSOConsentServiceImpl.class);
    private static final String DEFAULT_PURPOSE = "DEFAULT";
    private static final String DEFAULT_PURPOSE_CATEGORY = "DEFAULT";
    private static final String DEFAULT_PURPOSE_GROUP = "DEFAULT";
    private static final String DEFAULT_PURPOSE_GROUP_TYPE = "SP";
    private boolean ssoConsentEnabled = true;

    public SSOConsentServiceImpl() {

        readSSOConsentEnabledConfig();
    }

    /**
     * Get consent required claims for a given service from a user considering existing user consents.
     *
     * @param serviceProvider   Service provider requesting consent.
     * @param authenticatedUser Authenticated user requesting consent form.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    @Override
    public ConsentClaimsData getConsentRequiredClaimsWithExistingConsents(ServiceProvider serviceProvider,
                                                                          AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException {

        return getConsentRequiredClaims(serviceProvider, authenticatedUser, true, null);
    }

    /**
     * Get consent required claims for a given service from a user considering existing user consents.
     *
     * @param serviceProvider    Service provider requesting consent.
     * @param authenticatedUser  Authenticated user requesting consent form.
     * @param claimsListOfScopes Claims list of requested scopes.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    @Override
    public ConsentClaimsData getConsentRequiredClaimsWithExistingConsents(ServiceProvider serviceProvider,
                                                                          AuthenticatedUser authenticatedUser,
                                                                          List<String> claimsListOfScopes)
            throws SSOConsentServiceException {

        return getConsentRequiredClaims(serviceProvider, authenticatedUser, true, claimsListOfScopes);
    }

    /**
     * Get consent required claims for a given service from a user ignoring existing user consents.
     *
     * @param serviceProvider   Service provider requesting consent.
     * @param authenticatedUser Authenticated user requesting consent form.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    @Override
    public ConsentClaimsData getConsentRequiredClaimsWithoutExistingConsents(ServiceProvider serviceProvider,
                                                                             AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException {

        return getConsentRequiredClaims(serviceProvider, authenticatedUser, false, null);
    }

    /**
     * Get consent required claims for a given service from a user ignoring existing user consents  and considering
     * requested scopes.
     *
     * @param serviceProvider    Service provider requesting consent.
     * @param authenticatedUser  Authenticated user requesting consent form.
     * @param claimsListOfScopes Claims list of requested scopes.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    @Override
    public ConsentClaimsData getConsentRequiredClaimsWithoutExistingConsents(ServiceProvider serviceProvider,
                                                                             AuthenticatedUser authenticatedUser,
                                                                             List<String> claimsListOfScopes)
            throws SSOConsentServiceException {

        return getConsentRequiredClaims(serviceProvider, authenticatedUser, false, claimsListOfScopes);
    }

    /**
     * Get consent required claims for a given service from a user.
     *
     * @param serviceProvider     Service provider requesting consent.
     * @param authenticatedUser   Authenticated user requesting consent form.
     * @param useExistingConsents Use existing consent given by the user.
     * @param claimsListOfScopes  Claims list of requested scopes.
     * @return ConsentClaimsData which contains mandatory and required claims for consent.
     * @throws SSOConsentServiceException If error occurs while building claim information.
     */
    protected ConsentClaimsData getConsentRequiredClaims(ServiceProvider serviceProvider,
                                                         AuthenticatedUser authenticatedUser,
                                                         boolean useExistingConsents, List<String> claimsListOfScopes)
            throws SSOConsentServiceException {

        if (!isSSOConsentManagementEnabled(serviceProvider)) {
            String message = "Consent management for SSO is disabled.";
            throw new SSOConsentDisabledException(message, message);
        }
        if (serviceProvider == null) {
            throw new SSOConsentServiceException("Service provider cannot be null.");
        }

        String spName = serviceProvider.getApplicationName();
        String spTenantDomain = getSPTenantDomain(serviceProvider);
        String subject = buildSubjectWithUserStoreDomain(authenticatedUser);
        boolean scopeBasedClaimFilteringEnabled = true;

        if (StringUtils.isNotBlank(IdentityUtil.getProperty(CONFIG_ENABLE_SCOPE_BASED_CLAIM_FILTERING))) {
            scopeBasedClaimFilteringEnabled =
                    Boolean.parseBoolean(IdentityUtil.getProperty(CONFIG_ENABLE_SCOPE_BASED_CLAIM_FILTERING));
        }

        ClaimMapping[] claimMappings = getSpClaimMappings(serviceProvider);

        if (scopeBasedClaimFilteringEnabled && (claimMappings == null || claimMappings.length == 0)) {
            if (log.isDebugEnabled()) {
                log.debug("No claim mapping configured from the application. Hence skipping getting consent.");
            }
            return new ConsentClaimsData();
        }
        if (scopeBasedClaimFilteringEnabled && claimsListOfScopes != null) {
            try {
                claimMappings = FrameworkUtils.getFilteredScopeClaims(claimsListOfScopes,
                        Arrays.asList(claimMappings), serviceProvider.getOwner().getTenantDomain())
                        .toArray(new ClaimMapping[0]);
            } catch (ClaimManagementException e) {
                throw new SSOConsentServiceException("Error occurred while filtering claims of requested scopes");
            }
        }
        List<String> requestedClaims = new ArrayList<>();
        List<String> mandatoryClaims = new ArrayList<>();

        Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();

        String subjectClaimUri = getSubjectClaimUri(serviceProvider);

        boolean subjectClaimUriRequested = false;
        boolean subjectClaimUriMandatory = false;
        boolean promptSubjectClaimRequestedConsent = true;

        if (StringUtils.isNotBlank(IdentityUtil.getProperty(CONFIG_PROMPT_SUBJECT_CLAIM_REQUESTED_CONSENT))) {
            promptSubjectClaimRequestedConsent =
                    Boolean.parseBoolean(IdentityUtil.getProperty(CONFIG_PROMPT_SUBJECT_CLAIM_REQUESTED_CONSENT));
        }

        if (!scopeBasedClaimFilteringEnabled && isPassThroughScenario(claimMappings, userAttributes)) {
            for (Map.Entry<ClaimMapping, String> userAttribute : userAttributes.entrySet()) {
                String remoteClaimUri = userAttribute.getKey().getRemoteClaim().getClaimUri();
                if (subjectClaimUri.equals(remoteClaimUri) ||
                        IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR.equals(remoteClaimUri)) {
                    continue;
                }
                mandatoryClaims.add(remoteClaimUri);
            }
        } else {

            boolean isCustomClaimMapping = isCustomClaimMapping(serviceProvider);
            for (ClaimMapping claimMapping : claimMappings) {
                if (isCustomClaimMapping) {
                    if (subjectClaimUri.equals(claimMapping.getRemoteClaim().getClaimUri())) {
                        subjectClaimUri = claimMapping.getLocalClaim().getClaimUri();
                        if (promptSubjectClaimRequestedConsent) {
                            if (claimMapping.isMandatory()) {
                                subjectClaimUriMandatory = true;
                            } else if (claimMapping.isRequested()) {
                                subjectClaimUriRequested = true;
                            }
                        }
                        continue;
                    }
                } else {
                    if (subjectClaimUri.equals(claimMapping.getLocalClaim().getClaimUri())) {
                        if (promptSubjectClaimRequestedConsent) {
                            if (claimMapping.isMandatory()) {
                                subjectClaimUriMandatory = true;
                            } else if (claimMapping.isRequested()) {
                                subjectClaimUriRequested = true;
                            }
                        }
                        continue;
                    }
                }
                if (claimMapping.isMandatory()) {
                    mandatoryClaims.add(claimMapping.getLocalClaim().getClaimUri());
                } else if (claimMapping.isRequested()) {
                    requestedClaims.add(claimMapping.getLocalClaim().getClaimUri());
                }
            }
        }

        if (promptSubjectClaimRequestedConsent) {
            if (subjectClaimUriMandatory) {
                mandatoryClaims.add(subjectClaimUri);
            } else if (subjectClaimUriRequested) {
                requestedClaims.add(subjectClaimUri);
            }
        }

        List<ClaimMetaData> receiptConsentMetaData = new ArrayList<>();
        List<ClaimMetaData> receiptConsentDeniedMetaData;
        Receipt receipt = getConsentReceiptOfUser(serviceProvider, authenticatedUser, spName, spTenantDomain, subject);
        if (useExistingConsents && receipt != null) {
            receiptConsentMetaData = getRequestedClaimsFromReceipt(receipt, true);
            List<String> claimsWithConsent = getClaimsFromConsentMetaData(receiptConsentMetaData);
            receiptConsentDeniedMetaData = getRequestedClaimsFromReceipt(receipt, false);
            List<String> claimsDeniedConsent = getClaimsFromConsentMetaData(receiptConsentDeniedMetaData);
            mandatoryClaims.removeAll(claimsWithConsent);
            requestedClaims.removeAll(claimsWithConsent);
            requestedClaims.removeAll(claimsDeniedConsent);
        }
        ConsentClaimsData consentClaimsData = getConsentRequiredClaimData(mandatoryClaims, requestedClaims,
                spTenantDomain);
        consentClaimsData.setClaimsWithConsent(receiptConsentMetaData);
        return consentClaimsData;
    }

    private boolean isCustomClaimMapping(ServiceProvider serviceProvider) {

        return !serviceProvider.getClaimConfig().isLocalClaimDialect();
    }

    private boolean isPassThroughScenario(ClaimMapping[] claimMappings, Map<ClaimMapping, String> userAttributes) {

        return isEmpty(claimMappings) && isNotEmpty(userAttributes);
    }

    private String getSubjectClaimUri(ServiceProvider serviceProvider) {

        String subjectClaimUri = serviceProvider.getLocalAndOutBoundAuthenticationConfig().getSubjectClaimUri();
        if (isBlank(subjectClaimUri)) {
            subjectClaimUri = USERNAME_CLAIM;
        }
        return subjectClaimUri;
    }

    private void readSSOConsentEnabledConfig() {

        IdentityConfigParser identityConfigParser = IdentityConfigParser.getInstance();
        OMElement consentElement = identityConfigParser.getConfigElement(CONFIG_ELEM_CONSENT);

        if (consentElement != null) {

            OMElement ssoConsentEnabledElem = consentElement.getFirstChildWithName(
                    new QName(IDENTITY_DEFAULT_NAMESPACE, CONFIG_ELEM_ENABLE_SSO_CONSENT_MANAGEMENT));

            if (ssoConsentEnabledElem != null) {
                String ssoConsentEnabledElemText = ssoConsentEnabledElem.getText();
                if (isNotBlank(ssoConsentEnabledElemText)) {
                    ssoConsentEnabled = Boolean.parseBoolean(ssoConsentEnabledElemText);
                    if (isDebugEnabled()) {
                        logDebug("Consent management for SSO is set to " + ssoConsentEnabled + " from configurations.");
                    }
                    return;
                }
            }
        }
        ssoConsentEnabled = true;
    }

    private ClaimMapping[] getSpClaimMappings(ServiceProvider serviceProvider) {

        if (serviceProvider.getClaimConfig() != null) {
            return serviceProvider.getClaimConfig().getClaimMappings();
        } else {
            return new ClaimMapping[0];
        }
    }

    private String getSPTenantDomain(ServiceProvider serviceProvider) {

        String spTenantDomain;
        User owner = serviceProvider.getOwner();
        if (owner != null) {
            spTenantDomain = owner.getTenantDomain();
        } else {
            spTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return spTenantDomain;
    }

    /**
     * Process the provided user consent and creates a consent receipt.
     *
     * @param consentApprovedClaimIds Consent approved claims by the user.
     * @param serviceProvider         Service provider receiving consent.
     * @param authenticatedUser       Authenticated user providing consent.
     * @param consentClaimsData       Claims which the consent requested for.
     * @throws SSOConsentServiceException If error occurs while processing user consent.
     */
    @Override
    public void processConsent(List<Integer> consentApprovedClaimIds, ServiceProvider serviceProvider,
                               AuthenticatedUser authenticatedUser, ConsentClaimsData consentClaimsData)
            throws SSOConsentServiceException {

        processConsent(consentApprovedClaimIds, serviceProvider, authenticatedUser, consentClaimsData, false);
    }

    @Override
    public void processConsent(List<Integer> consentApprovedClaimIds, ServiceProvider serviceProvider,
                               AuthenticatedUser authenticatedUser, ConsentClaimsData consentClaimsData,
                               boolean overrideExistingConsent)
            throws SSOConsentServiceException {

        if (!isSSOConsentManagementEnabled(serviceProvider)) {
            String message = "Consent management for SSO is disabled.";
            throw new SSOConsentDisabledException(message, message);
        }
        if (isDebugEnabled()) {
            logDebug("User: " + authenticatedUser.getAuthenticatedSubjectIdentifier() + " has approved consent.");
        }
        UserConsent userConsent = processUserConsent(consentApprovedClaimIds, consentClaimsData);
        if (isEmpty(userConsent.getApprovedClaims()) && isEmpty(userConsent.getDisapprovedClaims())) {
            if (isDebugEnabled()) {
                logDebug("User: " + authenticatedUser.getAuthenticatedSubjectIdentifier() + " has not provided new " +
                        "approved/disapproved consent. Hence skipping the consent progress.");
            }
            return;
        }
        String subject = buildSubjectWithUserStoreDomain(authenticatedUser);

        List<ClaimMetaData> claimsWithConsent;
        List<ClaimMetaData> claimsDeniedConsent;
        if (!overrideExistingConsent) {
            String spName = serviceProvider.getApplicationName();
            String spTenantDomain = getSPTenantDomain(serviceProvider);
            Receipt receipt =
                    getConsentReceiptOfUser(serviceProvider, authenticatedUser, spName, spTenantDomain, subject);
            claimsWithConsent =
                    getUserRequestedClaims(receipt, userConsent, true);
            claimsDeniedConsent =
                    getUserRequestedClaims(receipt, userConsent, false);
        } else {
            claimsWithConsent = userConsent.getApprovedClaims();
            claimsDeniedConsent = userConsent.getDisapprovedClaims();
        }

        String spTenantDomain = getSPTenantDomain(serviceProvider);
        String subjectTenantDomain = authenticatedUser.getTenantDomain();

        if (isNotEmpty(claimsWithConsent) || isNotEmpty(claimsDeniedConsent)) {
            addReceipt(subject, subjectTenantDomain, serviceProvider, spTenantDomain, claimsWithConsent,
                    claimsDeniedConsent);
        }
    }

    /**
     * Retrieves claims which a user has provided consent for a given service provider.
     *
     * @param serviceProvider   Service provider to retrieve the consent against.
     * @param authenticatedUser Authenticated user to related to consent claim retrieval.
     * @return List of claim which the user has provided consent for the given service provider.
     * @throws SSOConsentServiceException If error occurs while retrieve user consents.
     */
    @Override
    public List<ClaimMetaData> getClaimsWithConsents(ServiceProvider serviceProvider, AuthenticatedUser
            authenticatedUser) throws SSOConsentServiceException {

        if (!isSSOConsentManagementEnabled(serviceProvider)) {
            String message = "Consent management for SSO is disabled.";
            throw new SSOConsentDisabledException(message, message);
        }
        if (serviceProvider == null) {
            throw new SSOConsentServiceException("Service provider cannot be null.");
        }
        String spName = serviceProvider.getApplicationName();
        List<ClaimMetaData> receiptConsentMetaData = new ArrayList<>();

        String spTenantDomain = getSPTenantDomain(serviceProvider);

        String subject = buildSubjectWithUserStoreDomain(authenticatedUser);

        Receipt receipt = getConsentReceiptOfUser(serviceProvider, authenticatedUser, spName, spTenantDomain, subject);
        if (receipt == null) {
            return receiptConsentMetaData;
        } else {
            receiptConsentMetaData = getRequestedClaimsFromReceipt(receipt, true);
        }
        return receiptConsentMetaData;
    }

    /**
     * Specifies whether consent management for SSO is enabled or disabled.
     *
     * @param serviceProvider Service provider to check whether consent management is enabled.
     * @return true if enabled, false otherwise.
     */
    @Override
    public boolean isSSOConsentManagementEnabled(ServiceProvider serviceProvider) {

        return ssoConsentEnabled;
    }

    private Receipt getConsentReceiptOfUser(ServiceProvider serviceProvider, AuthenticatedUser authenticatedUser,
                                            String spName, String spTenantDomain,
                                            String subject) throws SSOConsentServiceException {

        int receiptListLimit = 2;
        List<ReceiptListResponse> receiptListResponses;
        try {
            receiptListResponses = getReceiptListOfUserForSP(authenticatedUser, spName, spTenantDomain, subject,
                    receiptListLimit);
            if (isDebugEnabled()) {
                String message = String.format("Retrieved %s receipts for user: %s, service provider: %s in tenant " +
                                "domain %s", receiptListResponses.size(), subject, serviceProvider,
                        spTenantDomain);
                logDebug(message);
            }

            if (hasUserMultipleReceipts(receiptListResponses)) {
                throw new SSOConsentServiceException("Consent Management Error", "User cannot have more than one " +
                        "ACTIVE consent per service provider.");
            } else if (hasUserSingleReceipt(receiptListResponses)) {
                String receiptId = getFirstConsentReceiptFromList(receiptListResponses);
                return getReceipt(authenticatedUser, receiptId);
            } else {
                return null;
            }
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent Management Error",
                    "Error while retrieving user consents.", e);
        }
    }

    private void addReceipt(String subject, String subjectTenantDomain, ServiceProvider serviceProvider,
                            String spTenantDomain, List<ClaimMetaData> claimsWithConsent,
                            List<ClaimMetaData> claimsDeniedConsent) throws SSOConsentServiceException {

        ReceiptInput receiptInput =
                buildReceiptInput(subject, serviceProvider, spTenantDomain, claimsWithConsent, claimsDeniedConsent);
        AddReceiptResponse receiptResponse;
        try {
            startTenantFlowWithUser(subject, subjectTenantDomain);
            receiptResponse = getConsentManager().addConsent(receiptInput);
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent receipt error", "Error while adding the consent " +
                    "receipt", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        if (isDebugEnabled()) {
            logDebug("Successfully added consent receipt: " + receiptResponse.getConsentReceiptId());
        }
    }

    private ReceiptInput buildReceiptInput(String subject, ServiceProvider serviceProvider, String spTenantDomain,
                                           List<ClaimMetaData> claimsWithConsent,
                                           List<ClaimMetaData> claimsDeniedConsent)
            throws SSOConsentServiceException {

        String collectionMethod = "Web Form - Sign-in";
        String jurisdiction = "NONE";
        String language = "us_EN";
        String consentType = "EXPLICIT";
        String termination = CONSENT_VALIDITY_TYPE_VALID_UNTIL + CONSENT_VALIDITY_TYPE_SEPARATOR +
                CONSENT_VALIDITY_TYPE_VALID_UNTIL_INDEFINITE;
        String policyUrl = "NONE";

        Purpose purpose = getDefaultPurpose();
        PurposeCategory purposeCategory = getDefaultPurposeCategory();
        List<PIICategoryValidity> piiCategoryIds =
                getPiiCategoryValidityForClaims(claimsWithConsent, claimsDeniedConsent, termination);
        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(purposeCategory.getId());

        ReceiptPurposeInput purposeInput = getReceiptPurposeInput(consentType, termination, purpose, piiCategoryIds,
                purposeCategoryIds);
        purposeInputs.add(purposeInput);

        ReceiptServiceInput serviceInput = getReceiptServiceInput(serviceProvider, spTenantDomain, purposeInputs);
        serviceInputs.add(serviceInput);

        return getReceiptInput(subject, collectionMethod, jurisdiction, language, policyUrl, serviceInputs, properties);
    }

    private ReceiptInput getReceiptInput(String subject, String collectionMethod, String jurisdiction, String
            language, String policyUrl, List<ReceiptServiceInput> serviceInputs, Map<String, String> properties) {

        ReceiptInput receiptInput = new ReceiptInput();
        receiptInput.setCollectionMethod(collectionMethod);
        receiptInput.setJurisdiction(jurisdiction);
        receiptInput.setLanguage(language);
        receiptInput.setPolicyUrl(policyUrl);
        receiptInput.setServices(serviceInputs);
        receiptInput.setProperties(properties);
        receiptInput.setPiiPrincipalId(subject);
        return receiptInput;
    }

    private ReceiptServiceInput getReceiptServiceInput(ServiceProvider serviceProvider, String spTenantDomain,
                                                       List<ReceiptPurposeInput> purposeInputs) {

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(spTenantDomain);

        if (serviceProvider == null) {
            return serviceInput;
        }
        String spName = serviceProvider.getApplicationName();
        String spDescription;
        spDescription = serviceProvider.getDescription();
        if (StringUtils.isBlank(spDescription)) {
            spDescription = spName;
        }
        serviceInput.setService(spName);
        serviceInput.setSpDisplayName(spName);
        serviceInput.setSpDescription(spDescription);
        return serviceInput;
    }

    private ReceiptPurposeInput getReceiptPurposeInput(String consentType, String termination, Purpose purpose,
                                                       List<PIICategoryValidity> piiCategoryIds,
                                                       List<Integer> purposeCategoryIds) {

        ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();
        purposeInput.setPrimaryPurpose(true);
        purposeInput.setTermination(termination);
        purposeInput.setConsentType(consentType);
        purposeInput.setThirdPartyDisclosure(false);
        purposeInput.setPurposeId(purpose.getId());
        purposeInput.setPurposeCategoryId(purposeCategoryIds);
        purposeInput.setPiiCategory(piiCategoryIds);
        return purposeInput;
    }

    private List<PIICategoryValidity> getPiiCategoryValidityForClaims(List<ClaimMetaData> claimsWithConsent,
                                                                      List<ClaimMetaData> claimsDeniedConsent,
                                                                      String termination)
            throws SSOConsentServiceException {

        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();
        List<PIICategoryValidity> piiCategoryIdsForClaimsWithConsent =
                getPiiCategoryValidityForRequestedClaims(claimsWithConsent, true, termination);
        List<PIICategoryValidity> piiCategoryIdsForDeniedConsentClaims =
                getPiiCategoryValidityForRequestedClaims(claimsDeniedConsent, false, termination);
        piiCategoryIds.addAll(piiCategoryIdsForClaimsWithConsent);
        piiCategoryIds.addAll(piiCategoryIdsForDeniedConsentClaims);
        return piiCategoryIds;
    }

    private List<PIICategoryValidity> getPiiCategoryValidityForRequestedClaims(List<ClaimMetaData> requestedClaims,
                                                                               boolean isConsented, String termination)
            throws SSOConsentServiceException {

        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();

        if (CollectionUtils.isEmpty(requestedClaims)) {
            return piiCategoryIds;
        }

        for (ClaimMetaData requestedClaim : requestedClaims) {

            if (requestedClaim == null || requestedClaim.getClaimUri() == null) {
                continue;
            }

            PIICategory piiCategory;
            try {
                piiCategory = getConsentManager().getPIICategoryByName(requestedClaim.getClaimUri());
            } catch (ConsentManagementClientException e) {

                if (isInvalidPIICategoryError(e)) {
                    piiCategory = addPIICategoryForClaim(requestedClaim);
                } else {
                    throw new SSOConsentServiceException("Consent PII category error", "Error while retrieving" +
                            " PII category: " + DEFAULT_PURPOSE_CATEGORY, e);
                }
            } catch (ConsentManagementException e) {
                throw new SSOConsentServiceException("Consent PII category error", "Error while retrieving " +
                        "PII category: " + DEFAULT_PURPOSE_CATEGORY, e);
            }
            PIICategoryValidity piiCategoryValidity = new PIICategoryValidity(piiCategory.getId(), termination);
            piiCategoryValidity.setConsented(isConsented);
            piiCategoryIds.add(piiCategoryValidity);
        }
        return piiCategoryIds;
    }

    private PIICategory addPIICategoryForClaim(ClaimMetaData claim) throws SSOConsentServiceException {

        PIICategory piiCategory;
        PIICategory piiCategoryInput = new PIICategory(claim.getClaimUri(), claim.getDescription(), false, claim
                .getDisplayName());
        try {
            piiCategory = getConsentManager().addPIICategory(piiCategoryInput);
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent PII category error", "Error while adding" +
                    " PII category:" + DEFAULT_PURPOSE_CATEGORY, e);
        }
        return piiCategory;
    }

    private boolean isInvalidPIICategoryError(ConsentManagementClientException e) {

        return ERROR_CODE_PII_CAT_NAME_INVALID.getCode().equals(e.getErrorCode());
    }

    private PurposeCategory getDefaultPurposeCategory() throws SSOConsentServiceException {

        PurposeCategory purposeCategory;
        try {
            purposeCategory = getConsentManager().getPurposeCategoryByName(DEFAULT_PURPOSE_CATEGORY);
        } catch (ConsentManagementClientException e) {

            if (isInvalidPurposeCategoryError(e)) {
                purposeCategory = addDefaultPurposeCategory();
            } else {
                throw new SSOConsentServiceException("Consent purpose category error", "Error while retrieving" +
                        " purpose category: " + DEFAULT_PURPOSE_CATEGORY, e);
            }
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent purpose category error", "Error while retrieving " +
                    "purpose category: " + DEFAULT_PURPOSE_CATEGORY, e);
        }
        return purposeCategory;
    }

    private PurposeCategory addDefaultPurposeCategory() throws SSOConsentServiceException {

        PurposeCategory purposeCategory;
        PurposeCategory defaultPurposeCategory = new PurposeCategory(DEFAULT_PURPOSE_CATEGORY,
                "For core functionalities of the product");
        try {
            purposeCategory = getConsentManager().addPurposeCategory(defaultPurposeCategory);
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent purpose category error", "Error while adding" +
                    " purpose category: " + DEFAULT_PURPOSE_CATEGORY, e);
        }
        return purposeCategory;
    }

    private boolean isInvalidPurposeCategoryError(ConsentManagementClientException e) {

        return ERROR_CODE_PURPOSE_CAT_NAME_INVALID.getCode().equals(e.getErrorCode());
    }

    private Purpose getDefaultPurpose() throws SSOConsentServiceException {

        Purpose purpose;

        try {
            purpose = getConsentManager().getPurposeByName(DEFAULT_PURPOSE, DEFAULT_PURPOSE_GROUP,
                                                           DEFAULT_PURPOSE_GROUP_TYPE);
        } catch (ConsentManagementClientException e) {

            if (isInvalidPurposeError(e)) {
                purpose = addDefaultPurpose();
            } else {
                throw new SSOConsentServiceException("Consent purpose error", "Error while retrieving purpose: " +
                        DEFAULT_PURPOSE, e);
            }
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent purpose error", "Error while retrieving purpose: " +
                    DEFAULT_PURPOSE, e);
        }
        return purpose;
    }

    private Purpose addDefaultPurpose() throws SSOConsentServiceException {

        Purpose purpose;
        Purpose defaultPurpose = new Purpose(DEFAULT_PURPOSE, "For core functionalities of the product",
                                             DEFAULT_PURPOSE_GROUP, DEFAULT_PURPOSE_GROUP_TYPE);
        try {
            purpose = getConsentManager().addPurpose(defaultPurpose);
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent purpose error",
                    "Error while adding purpose: " + DEFAULT_PURPOSE, e);
        }
        return purpose;
    }

    private boolean isInvalidPurposeError(ConsentManagementClientException e) {

        return ERROR_CODE_PURPOSE_NAME_INVALID.getCode().equals(e.getErrorCode());
    }

    private UserConsent processUserConsent(List<Integer> consentApprovedClaimIds, ConsentClaimsData
            consentClaimsData) throws SSOConsentServiceException {

        UserConsent userConsent = new UserConsent();
        List<ClaimMetaData> approvedClamMetaData = buildApprovedClaimList(consentApprovedClaimIds, consentClaimsData);

        List<ClaimMetaData> consentRequiredClaimMetaData = getConsentRequiredClaimMetaData(consentClaimsData);
        List<ClaimMetaData> disapprovedClaims = buildDisapprovedClaimList(consentRequiredClaimMetaData,
                approvedClamMetaData);

        if (isMandatoryClaimsDisapproved(consentClaimsData.getMandatoryClaims(), disapprovedClaims)) {
            throw new SSOConsentServiceException("Consent Denied for Mandatory Attributes",
                    "User denied consent to share mandatory attributes.");
        }

        userConsent.setApprovedClaims(approvedClamMetaData);
        userConsent.setDisapprovedClaims(disapprovedClaims);

        return userConsent;
    }

    private List<ClaimMetaData> getUserRequestedClaims(Receipt receipt,
                                                       UserConsent userConsent, boolean isConsented) {

        List<ClaimMetaData> requestedClaims = new ArrayList<>();
        if (isConsented) {
            requestedClaims.addAll(userConsent.getApprovedClaims());
        } else {
            requestedClaims.addAll(userConsent.getDisapprovedClaims());
        }
        if (receipt == null) {
            return requestedClaims;
        }

        List<PIICategoryValidity> piiCategoriesFromServices = getPIICategoriesFromServices(receipt.getServices());
        if (isConsented) {
            piiCategoriesFromServices.removeIf(piiCategoryValidity -> !piiCategoryValidity.isConsented());
        } else {
            piiCategoriesFromServices.removeIf(PIICategoryValidity::isConsented);
        }

        List<ClaimMetaData> claimsFromPIICategoryValidity = getClaimsFromPIICategoryValidity(piiCategoriesFromServices);
        requestedClaims.addAll(claimsFromPIICategoryValidity);

        /* When consent denied requested claim is updated as mandatory, that claim should remove from the
        requested denied claims list. */
        if (!isConsented && CollectionUtils.isNotEmpty(requestedClaims)
                && CollectionUtils.isNotEmpty(userConsent.getApprovedClaims())) {
            requestedClaims.removeAll(userConsent.getApprovedClaims());
        }
        return getDistinctClaims(requestedClaims);
    }

    private List<ClaimMetaData> getDistinctClaims(List<ClaimMetaData> claimsWithConsent) {

        return claimsWithConsent.stream()
                .filter(distinctByKey(ClaimMetaData::getClaimUri))
                .collect(Collectors.toList());
    }

    private Predicate<ClaimMetaData> distinctByKey(Function<ClaimMetaData, String> keyExtractor) {

        final Set<String> claimUris = new HashSet<>();
        return claimUri -> claimUris.add(keyExtractor.apply(claimUri));
    }

    private List<ClaimMetaData> getConsentRequiredClaimMetaData(ConsentClaimsData consentClaimsData) {

        List<ClaimMetaData> consentRequiredClaims = new ArrayList<>();

        if (isNotEmpty(consentClaimsData.getMandatoryClaims())) {
            consentRequiredClaims.addAll(consentClaimsData.getMandatoryClaims());
        }
        if (isNotEmpty(consentClaimsData.getRequestedClaims())) {
            consentRequiredClaims.addAll(consentClaimsData.getRequestedClaims());
        }
        return consentRequiredClaims;
    }

    private List<ClaimMetaData> buildDisapprovedClaimList(List<ClaimMetaData> consentRequiredClaims,
                                                          List<ClaimMetaData> approvedClaims) {

        List<ClaimMetaData> disapprovedClaims = new ArrayList<>();

        if (isNotEmpty(consentRequiredClaims)) {
            consentRequiredClaims.removeAll(approvedClaims);
            disapprovedClaims = consentRequiredClaims;
        }
        return disapprovedClaims;
    }

    private List<ClaimMetaData> buildApprovedClaimList(List<Integer> consentApprovedClaimIds, ConsentClaimsData
            consentClaimsData) {

        List<ClaimMetaData> approvedClaims = new ArrayList<>();

        for (Integer claimId : consentApprovedClaimIds) {
            ClaimMetaData consentClaim = new ClaimMetaData();
            consentClaim.setId(claimId);
            List<ClaimMetaData> mandatoryClaims = consentClaimsData.getMandatoryClaims();

            int claimIndex = mandatoryClaims.indexOf(consentClaim);
            if (claimIndex != -1) {
                approvedClaims.add(mandatoryClaims.get(claimIndex));
            }

            List<ClaimMetaData> requestedClaims = consentClaimsData.getRequestedClaims();
            claimIndex = requestedClaims.indexOf(consentClaim);
            if (claimIndex != -1) {
                approvedClaims.add(requestedClaims.get(claimIndex));
            }
        }
        return approvedClaims;
    }

    private String buildSubjectWithUserStoreDomain(AuthenticatedUser authenticatedUser) {

        String userStoreDomain;
        if (authenticatedUser.isFederatedUser()) {
            userStoreDomain = getFederatedUserDomain(authenticatedUser.getFederatedIdPName());
        } else {
            userStoreDomain = authenticatedUser.getUserStoreDomain();
        }

        return UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), userStoreDomain);
    }

    private String getFederatedUserDomain(String authenticatedIDP) {

        if (isNotBlank(authenticatedIDP)) {
            return FEDERATED_USER_DOMAIN_PREFIX + FEDERATED_USER_DOMAIN_SEPARATOR + authenticatedIDP;
        } else {
            return FEDERATED_USER_DOMAIN_PREFIX;
        }
    }

    private List<ReceiptListResponse> getReceiptListOfUserForSP(AuthenticatedUser authenticatedUser,
                                                                String serviceProvider, String spTenantDomain,
                                                                String subject, int limit) throws
            ConsentManagementException {

        List<ReceiptListResponse> receiptListResponses;
        startTenantFlowWithUser(subject, authenticatedUser.getTenantDomain());
        try {
            receiptListResponses = getConsentManager().searchReceipts(limit, 0, subject, spTenantDomain,
                    serviceProvider, ACTIVE_STATE);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return receiptListResponses;
    }

    private List<PIICategoryValidity> getPIICategoriesFromServices(List<ReceiptService> receiptServices) {

        List<PIICategoryValidity> piiCategoryValidityMap = new ArrayList<>();
        for (ReceiptService receiptService : receiptServices) {

            List<ConsentPurpose> purposes = receiptService.getPurposes();

            for (ConsentPurpose purpose : purposes) {
                piiCategoryValidityMap.addAll(piiCategoryValidityMap.size(), purpose.getPiiCategory());
            }
        }
        return piiCategoryValidityMap;
    }

    private List<ClaimMetaData> getRequestedClaimsFromReceipt(Receipt receipt, boolean isConsented) {

        List<ReceiptService> services = receipt.getServices();
        List<PIICategoryValidity> piiCategories = getPIICategoriesFromServices(services);
        if (isConsented) {
            piiCategories.removeIf(piiCategoryValidity -> !piiCategoryValidity.isConsented());
        } else {
            piiCategories.removeIf(PIICategoryValidity::isConsented);
        }
        List<ClaimMetaData> claimsFromPIICategoryValidity = getClaimsFromPIICategoryValidity(piiCategories);
        if (isDebugEnabled()) {
            String message = String.format("User: %s has provided consent in receipt: %s for claims: " +
                            claimsFromPIICategoryValidity, receipt.getPiiPrincipalId(),
                    receipt.getConsentReceiptId());
            logDebug(message);
        }
        return claimsFromPIICategoryValidity;
    }

    private List<ClaimMetaData> getClaimsFromPIICategoryValidity(List<PIICategoryValidity> piiCategories) {

        List<ClaimMetaData> claimMetaDataList = new ArrayList<>();
        for (PIICategoryValidity piiCategoryValidity : piiCategories) {

            if (isConsentForClaimValid(piiCategoryValidity)) {
                ClaimMetaData claimMetaData = new ClaimMetaData();
                claimMetaData.setClaimUri(piiCategoryValidity.getName());
                claimMetaData.setDisplayName(piiCategoryValidity.getDisplayName());
                claimMetaDataList.add(claimMetaData);
            }
        }
        return claimMetaDataList;
    }

    protected boolean isConsentForClaimValid(PIICategoryValidity piiCategoryValidity) {

        String consentValidity = piiCategoryValidity.getValidity();

        if (isEmpty(consentValidity)) {
            return true;
        }

        List<String> consentValidityEntries = Arrays.asList(consentValidity.split(SSOConsentConstants
                .CONSENT_VALIDITY_SEPARATOR));
        for (String consentValidityEntry : consentValidityEntries) {
            if (isSupportedExpiryType(consentValidityEntry)) {
                String[] validityEntry = consentValidityEntry.split(CONSENT_VALIDITY_TYPE_SEPARATOR, 2);
                if (validityEntry.length == 2) {
                    try {
                        String validTime = validityEntry[1];
                        if (isDebugEnabled()) {
                            String message = String.format("Validity time for PII category: %s is %s.",
                                    piiCategoryValidity.getName(), validTime);
                            logDebug(message);
                        }
                        if (isExpiryIndefinite(validTime)) {
                            return true;
                        }
                        long consentExpiryInMillis = Long.parseLong(validTime);
                        long currentTimeMillis = System.currentTimeMillis();
                        return isExpired(currentTimeMillis, consentExpiryInMillis);
                    } catch (NumberFormatException e) {
                        if (isDebugEnabled()) {
                            String message = String.format("Cannot parse timestamp: %s. for PII category %s.",
                                    consentValidity, piiCategoryValidity.getName());
                            logDebug(message);
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean isSupportedExpiryType(String consentValidityEntry) {

        return consentValidityEntry.toUpperCase().startsWith(CONSENT_VALIDITY_TYPE_VALID_UNTIL);
    }

    private boolean isExpired(long currentTimeMillis, long consentExpiryInMillis) {

        return consentExpiryInMillis > currentTimeMillis;
    }

    private boolean isExpiryIndefinite(String validTime) {

        return CONSENT_VALIDITY_TYPE_VALID_UNTIL_INDEFINITE.equalsIgnoreCase(validTime);
    }

    private boolean isMandatoryClaimsDisapproved(List<ClaimMetaData> consentMandatoryClaims, List<ClaimMetaData>
            disapprovedClaims) {

        return isNotEmpty(consentMandatoryClaims) && !Collections.disjoint(disapprovedClaims, consentMandatoryClaims);
    }

    private ConsentClaimsData getConsentRequiredClaimData(List<String> mandatoryClaims, List<String>
            requestedClaims, String tenantDomain) throws SSOConsentServiceException {

        ConsentClaimsData consentClaimsData = new ConsentClaimsData();

        try {
            List<LocalClaim> localClaims = getClaimMetadataManagementService().getLocalClaims(tenantDomain);
            List<ClaimMetaData> mandatoryClaimsMetaData = new ArrayList<>();
            List<ClaimMetaData> requestedClaimsMetaData = new ArrayList<>();

            int claimId = 0;
            if (isNotEmpty(localClaims)) {
                for (LocalClaim localClaim : localClaims) {
                    if (isAllRequiredClaimsChecked(mandatoryClaims, requestedClaims)) {
                        break;
                    }
                    String claimURI = localClaim.getClaimURI();
                    if (mandatoryClaims.remove(claimURI)) {
                        ClaimMetaData claimMetaData = buildClaimMetaData(claimId, localClaim, claimURI);
                        mandatoryClaimsMetaData.add(claimMetaData);
                        claimId++;
                    } else if (requestedClaims.remove(claimURI)) {
                        ClaimMetaData claimMetaData = buildClaimMetaData(claimId, localClaim, claimURI);
                        requestedClaimsMetaData.add(claimMetaData);
                        claimId++;
                    }
                }

            }
            if (isNotEmpty(mandatoryClaims)) {
                for (String claimUri : mandatoryClaims) {
                    ClaimMetaData claimMetaData = buildClaimMetaData(claimId, claimUri);
                    mandatoryClaimsMetaData.add(claimMetaData);
                    claimId++;
                }
            }
            if (isNotEmpty(requestedClaims)) {
                for (String claimUri : mandatoryClaims) {
                    ClaimMetaData claimMetaData = buildClaimMetaData(claimId, claimUri);
                    requestedClaimsMetaData.add(claimMetaData);
                    claimId++;
                }
            }
            consentClaimsData.setMandatoryClaims(mandatoryClaimsMetaData);
            consentClaimsData.setRequestedClaims(requestedClaimsMetaData);
        } catch (ClaimMetadataException e) {
            throw new SSOConsentServiceException("Error while retrieving local claims", "Error occurred while " +
                    "retrieving local claims for tenant: " + tenantDomain, e);
        }
        return consentClaimsData;
    }

    private boolean isAllRequiredClaimsChecked(List<String> mandatoryClaims, List<String> requestedClaims) {

        return isEmpty(mandatoryClaims) && isEmpty(requestedClaims);
    }

    private ClaimMetaData buildClaimMetaData(int claimId, String claimUri) {

        LocalClaim localClaim = new LocalClaim(claimUri);
        return buildClaimMetaData(claimId, localClaim, claimUri);
    }

    private ClaimMetaData buildClaimMetaData(int claimId, LocalClaim localClaim, String claimURI) {

        ClaimMetaData claimMetaData = new ClaimMetaData();
        claimMetaData.setId(claimId);
        claimMetaData.setClaimUri(claimURI);
        String displayName = localClaim.getClaimProperties().get(DISPLAY_NAME_PROPERTY);

        if (isNotBlank(displayName)) {
            claimMetaData.setDisplayName(displayName);
        } else {
            claimMetaData.setDisplayName(claimURI);
        }

        String description = localClaim.getClaimProperty(DESCRIPTION_PROPERTY);
        if (isNotBlank(description)) {
            claimMetaData.setDescription(description);
        } else {
            claimMetaData.setDescription(EMPTY);
        }
        return claimMetaData;
    }

    private List<String> getClaimsFromConsentMetaData(List<ClaimMetaData> claimMetaDataList) {

        List<String> claims = new ArrayList<>();
        for (ClaimMetaData claimMetaData : claimMetaDataList) {
            claims.add(claimMetaData.getClaimUri());
        }
        return claims;
    }

    private String getFirstConsentReceiptFromList(List<ReceiptListResponse> receiptListResponses) {

        return receiptListResponses.get(0).getConsentReceiptId();
    }

    private Receipt getReceipt(AuthenticatedUser authenticatedUser, String receiptId) throws
            SSOConsentServiceException {

        Receipt currentReceipt;
        String subject = buildSubjectWithUserStoreDomain(authenticatedUser);
        try {
            initializeTenantRegistry(authenticatedUser);
            startTenantFlowWithUser(subject, authenticatedUser.getTenantDomain());
            currentReceipt = getConsentManager().getReceipt(receiptId);
        } catch (ConsentManagementException e) {
            throw new SSOConsentServiceException("Consent Management Error",
                    "Error while retrieving user consents.", e);
        } catch (IdentityException e) {
            throw new SSOConsentServiceException("Consent Management Error", "Error while initializing registry for " +
                    "the tenant domain: " + authenticatedUser.getTenantDomain(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return currentReceipt;
    }

    private void initializeTenantRegistry(AuthenticatedUser authenticatedUser) throws IdentityException {

        IdentityTenantUtil.initializeRegistry(
                IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain()));
    }

    private boolean hasUserSingleReceipt(List<ReceiptListResponse> receiptListResponses) {

        return receiptListResponses.size() == 1;
    }

    private boolean hasUserMultipleReceipts(List<ReceiptListResponse> receiptListResponses) {

        return receiptListResponses.size() > 1;
    }

    private void startTenantFlowWithUser(String subject, String subjectTenantDomain) {

        startTenantFlow(subjectTenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subject);
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    private boolean isDebugEnabled() {

        return log.isDebugEnabled();
    }

    private void logDebug(String message) {

        log.debug(message);
    }

    private ConsentManager getConsentManager() {

        return FrameworkServiceDataHolder.getInstance().getConsentManager();
    }

    private ClaimMetadataManagementService getClaimMetadataManagementService() {

        return FrameworkServiceDataHolder.getInstance().getClaimMetadataManagementService();
    }
}
