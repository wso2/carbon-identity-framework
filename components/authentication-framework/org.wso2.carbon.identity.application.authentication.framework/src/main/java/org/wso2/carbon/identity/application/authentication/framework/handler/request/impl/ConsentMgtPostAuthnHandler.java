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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
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
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PII_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages
        .ERROR_CODE_PURPOSE_CAT_NAME_INVALID;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_NAME_INVALID;

/**
 * This is an extension of {@link AbstractPostAuthnHandler} which handles user consent management upon successful
 * user authentication.
 */
public class ConsentMgtPostAuthnHandler extends AbstractPostAuthnHandler {

    private static final String CONSENT_RECEIPT_ID_PARAM = "ConsentReceiptId";
    private String CONSENT_PROMPTED = "consentPrompted";
    private ConsentManager consentManager;
    private static final String DEFAULT_PURPOSE = "DEFAULT";
    private static final String DEFAULT_PURPOSE_CATEGORY = "DEFAULT";
    private static final String CLAIM_SEPARATOR = ",";
    private static final String REQUESTED_CLAIMS_PARAM = "requestedClaims";
    private static final String MANDATORY_CLAIMS_PARAM = "mandatoryClaims";
    private static final Log log = LogFactory.getLog(ConsentMgtPostAuthnHandler.class);

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (authenticatedUser == null) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isConsentPrompted(context)) {
            return handlePostConsent(request, response, context);
        } else {
            return handlePreConsent(request, response, context);
        }
    }

    protected PostAuthnHandlerFlowStatus handlePreConsent(HttpServletRequest request, HttpServletResponse response,
                                                          AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        String serviceProvider = context.getSequenceConfig().getApplicationConfig().getApplicationName();

        // Due to: https://github.com/wso2/product-is/issues/2317.
        // Should be removed once the issue is fixed
        if ("DEFAULT".equalsIgnoreCase(serviceProvider)) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        String spTenantDomain;
        User owner = context.getSequenceConfig().getApplicationConfig().getServiceProvider().getOwner();
        if (owner != null) {
            spTenantDomain = owner.getTenantDomain();
        } else {
            spTenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        String subject = buildSubjectWithUserStoreDomain(authenticatedUser);

        try {

            List<ReceiptListResponse> receiptListResponses;
            try {
                startTenantFlow(authenticatedUser.getTenantDomain());
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subject);

                receiptListResponses = consentManager.searchReceipts(2, 0, subject,
                                                                     spTenantDomain, serviceProvider, ACTIVE_STATE);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            if (receiptListResponses.size() > 1) {

                throw new PostAuthenticationFailedException("Consent Management Error", "User cannot have more " +
                                                                "than one ACTIVE consent per service provider.");
            } else if (receiptListResponses.size() == 0) {

                List<String> requestedClaims = new ArrayList<>(getSPRequestedLocalClaims(context));
                List<String> mandatoryClaims = new ArrayList<>(getSPMandatoryLocalClaims(context));

                if (CollectionUtils.isEmpty(requestedClaims) && CollectionUtils.isEmpty(mandatoryClaims)) {
                    return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
                }

                Set<String> consentClaims = getUniqueLocalClaims(requestedClaims, mandatoryClaims);

                // Retain requested claims which are not mandatory.
                consentClaims.removeAll(mandatoryClaims);

                String mandatoryLocalClaims = StringUtils.join(mandatoryClaims, CLAIM_SEPARATOR);
                String requestedLocalClaims = null;

                if (CollectionUtils.isNotEmpty(consentClaims)) {
                    requestedLocalClaims = StringUtils.join(consentClaims, CLAIM_SEPARATOR);
                }

                redirectToConsentPage(response, context, requestedLocalClaims, mandatoryLocalClaims);
                setConsentPoppedUpState(context);

                return PostAuthnHandlerFlowStatus.INCOMPLETE;

            } else {

                Receipt receipt;
                try {
                    startTenantFlow(authenticatedUser.getTenantDomain());
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subject);

                    receipt = consentManager.getReceipt(receiptListResponses.get(0).getConsentReceiptId());
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }

                List<ReceiptService> services = receipt.getServices();
                List<PIICategoryValidity> piiCategories = getPIICategoriesFromServices(services);
                Set<String> claims = getClaimsFromPIICategoryValidity(piiCategories);

                List<String> requestedClaims = new ArrayList<>(getSPRequestedLocalClaims(context));
                List<String> mandatoryClaims = new ArrayList<>(getSPMandatoryLocalClaims(context));

                Set<String> consentClaims = getUniqueLocalClaims(requestedClaims, mandatoryClaims);
                consentClaims.removeAll(claims);
                consentClaims.removeAll(mandatoryClaims);

                removeUserClaimsFromContext(context, new ArrayList<>(consentClaims));

                mandatoryClaims.removeAll(claims);

                if (CollectionUtils.isEmpty(mandatoryClaims)) {

                    return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
                }

                String mandatoryLocalClaims = StringUtils.join(mandatoryClaims, CLAIM_SEPARATOR);
                redirectToConsentPage(response, context, null, mandatoryLocalClaims);
                setConsentPoppedUpState(context);
                context.addParameter(CONSENT_RECEIPT_ID_PARAM, receipt.getConsentReceiptId());

                return PostAuthnHandlerFlowStatus.INCOMPLETE;
            }
        } catch (ConsentManagementException e) {
            throw new PostAuthenticationFailedException("Consent Management Error", "Error while retrieving user " +
                                                                                    "consents.", e);
        }
    }

    private void startTenantFlow(String tenantDomain) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

    private Set<String> getUniqueLocalClaims(List<String> requestedClaims, List<String> mandatoryClaims) {
        return Stream.concat(requestedClaims.stream(), mandatoryClaims.stream()).collect
                (Collectors.toSet());
    }

    protected PostAuthnHandlerFlowStatus handlePostConsent(HttpServletRequest request, HttpServletResponse response,
                                                           AuthenticationContext context)
            throws PostAuthenticationFailedException {

        final String USER_CONSENT_INPUT = "consent";
        final String USER_CONSENT_APPROVE = "approve";

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (request.getParameter(USER_CONSENT_INPUT).equalsIgnoreCase(USER_CONSENT_APPROVE)) {

            Set<String> claimsWithConsent = new HashSet<>();

            UserConsent userConsent = processUserConsent(request, context);
            claimsWithConsent.addAll(userConsent.getApprovedClaims());

            Object receiptIdObject = context.getParameter(CONSENT_RECEIPT_ID_PARAM);

            String subject = buildSubjectWithUserStoreDomain(authenticatedUser);
            String subjectTenantDomain = authenticatedUser.getTenantDomain();

            if (receiptIdObject != null && receiptIdObject instanceof String) {

                String receiptId = (String) receiptIdObject;
                Receipt currentReceipt;
                try {
                    startTenantFlow(authenticatedUser.getTenantDomain());
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subject);

                    currentReceipt = consentManager.getReceipt(receiptId);
                } catch (ConsentManagementException e) {
                    throw new PostAuthenticationFailedException("Consent Management Error", "Error while " +
                                                                                    "retrieving user consents.", e);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
                List<PIICategoryValidity> piiCategoriesFromServices = getPIICategoriesFromServices
                        (currentReceipt.getServices());
                Set<String> claimsFromPIICategoryValidity = getClaimsFromPIICategoryValidity
                        (piiCategoriesFromServices);
                claimsWithConsent.addAll(claimsFromPIICategoryValidity);
            }

            ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
            String spTenantDomain = null;
            User owner = context.getSequenceConfig().getApplicationConfig().getServiceProvider().getOwner();
            if (owner != null) {
                spTenantDomain = owner.getTenantDomain();
            }
            removeUserClaimsFromContext(context, userConsent.getDisapprovedClaims());
            if (CollectionUtils.isNotEmpty(claimsWithConsent)) {
                addReceipt(subject, subjectTenantDomain, applicationConfig, spTenantDomain, claimsWithConsent);
            }
        } else {
            throw new PostAuthenticationFailedException("Consent Management Error", "User denied consent" +
                                                                                    " to share information.");
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private String buildSubjectWithUserStoreDomain(AuthenticatedUser authenticatedUser) {

        return UserCoreUtil.addDomainToName(authenticatedUser.getUserName(), authenticatedUser.getUserStoreDomain());
    }


    private UserConsent processUserConsent(HttpServletRequest request, AuthenticationContext context) throws
            PostAuthenticationFailedException {

        String consentClaimsPrefix = "consent_";

        Map<String, String[]> requestParams = request.getParameterMap();

        Object consentRequestedObj = context.getParameter(REQUESTED_CLAIMS_PARAM);
        Object consentMandatoryObj = context.getParameter(MANDATORY_CLAIMS_PARAM);

        String consentRequestedClaims = null;
        String consentMandatoryClaims = null;

        if (consentRequestedObj != null && consentRequestedObj instanceof String) {
            consentRequestedClaims = (String) consentRequestedObj;
        }

        if (consentMandatoryObj != null && consentMandatoryObj instanceof String) {
            consentMandatoryClaims = (String) consentMandatoryObj;
        }

        String consentRequiredClaims = Stream.of(consentMandatoryClaims, consentRequestedClaims)
                                             .filter(StringUtils::isNotBlank)
                                             .collect(Collectors.joining(CLAIM_SEPARATOR));

        List<String> approvedClaims = new ArrayList<>();
        List<String> disapprovedClaims = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            if (entry.getKey().startsWith(consentClaimsPrefix)) {
                String localClaimURI = entry.getKey().substring(consentClaimsPrefix.length());
                approvedClaims.add(localClaimURI);
            }
        }

        if (StringUtils.isNotEmpty(consentRequiredClaims)) {
            String[] requiredClaims = consentRequiredClaims.split(CLAIM_SEPARATOR);
            List<String> consentClaims = new ArrayList<>(Arrays.asList(requiredClaims));
            consentClaims.removeAll(approvedClaims);
            disapprovedClaims = consentClaims;
        }

        if (StringUtils.isNotBlank(consentMandatoryClaims) &&
            !Collections.disjoint(disapprovedClaims, Arrays.asList(consentMandatoryClaims.split(CLAIM_SEPARATOR)))) {

            throw new PostAuthenticationFailedException("Consent Denied for Mandatory Attributes",
                                                        "User denied consent to share mandatory attributes.");
        }

        UserConsent userConsent = new UserConsent();

        userConsent.setApprovedClaims(approvedClaims);
        userConsent.setDisapprovedClaims(disapprovedClaims);

        return userConsent;
    }

    private void redirectToConsentPage(HttpServletResponse response, AuthenticationContext context,
                                       String requestedLocalClaims, String mandatoryLocalClaims) throws
            PostAuthenticationFailedException {

        URIBuilder uriBuilder;
        try {
            uriBuilder = getUriBuilder(context, requestedLocalClaims, mandatoryLocalClaims);
            response.sendRedirect(uriBuilder.build().toString());
        } catch (IOException e) {
            throw new PostAuthenticationFailedException("Error while handling consents", "Error while " +
                                                                                         "redirecting to " +
                                                                                         "consent page", e);
        } catch (URISyntaxException e) {
            throw new PostAuthenticationFailedException("Error while handling consents",
                                                        "Error while building redirect URI", e);
        }
    }

    private Collection<String> getSPRequestedLocalClaims(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        Collection<String> spRequestedLocalClaims = new ArrayList<>();
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        if (applicationConfig == null) {
            throw new PostAuthenticationFailedException("Claim config error", "Application configs are null in " +
                                                                "AuthenticationContext.");
        }

        Map<String, String> claimMappings = applicationConfig.getRequestedClaimMappings();

        if (MapUtils.isNotEmpty(claimMappings) && CollectionUtils.isNotEmpty(claimMappings.values())) {
            spRequestedLocalClaims = claimMappings.values();
        }

        return spRequestedLocalClaims;
    }

    private Collection<String> getSPMandatoryLocalClaims(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        Collection<String> spMandatoryLocalClaims = new ArrayList<>();
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        if (applicationConfig == null) {
            throw new PostAuthenticationFailedException("Claim config error", "Application configs are null in " +
                                                                              "AuthenticationContext.");
        }

        Map<String, String> claimMappings = applicationConfig.getMandatoryClaimMappings();

        if (MapUtils.isNotEmpty(claimMappings) && CollectionUtils.isNotEmpty(claimMappings.values())) {
            spMandatoryLocalClaims = claimMappings.values();
        }

        return spMandatoryLocalClaims;
    }


    private URIBuilder getUriBuilder(AuthenticationContext context, String requestedLocalClaims, String
            mandatoryLocalClaims) throws URISyntaxException {

        final String LOGIN_ENDPOINT = "login.do";
        final String CONSENT_ENDPOINT = "consent.do";

        String CONSENT_ENDPOINT_URL = ConfigurationFacade.getInstance()
                                             .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, CONSENT_ENDPOINT);
        URIBuilder uriBuilder;
        uriBuilder = new URIBuilder(CONSENT_ENDPOINT_URL);

        if (StringUtils.isNotBlank(requestedLocalClaims)) {
            uriBuilder.addParameter(REQUESTED_CLAIMS_PARAM, requestedLocalClaims);
            context.addParameter(REQUESTED_CLAIMS_PARAM, requestedLocalClaims);
        }

        if (StringUtils.isNotBlank(mandatoryLocalClaims)) {
            uriBuilder.addParameter(MANDATORY_CLAIMS_PARAM, mandatoryLocalClaims);
            context.addParameter(MANDATORY_CLAIMS_PARAM, mandatoryLocalClaims);
        }
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                                context.getContextIdentifier());
        uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_SP,
                                context.getSequenceConfig().getApplicationConfig().getApplicationName());
        return uriBuilder;
    }

    private Set<String> getClaimsFromPIICategoryValidity(List<PIICategoryValidity> piiCategories) {

        HashSet<String> claims = new HashSet<>();
        for (PIICategoryValidity piiCategoryValidity : piiCategories) {

            if (isConsentForClaimValid(piiCategoryValidity)) {
                claims.add(piiCategoryValidity.getName());
            }
        }
        return claims;
    }

    @Override
    public int getPriority() {

        return 110;
    }

    @Override
    public String getName() {

        return "ConsentMgtPostAuthenticationHandler";
    }

    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext authenticationContext) {

        return authenticationContext.getSequenceConfig().getAuthenticatedUser();
    }

    private void setConsentPoppedUpState(AuthenticationContext authenticationContext) {

        authenticationContext.addParameter(CONSENT_PROMPTED, true);
    }

    private boolean isConsentPrompted(AuthenticationContext authenticationContext) {

        return authenticationContext.getParameter(CONSENT_PROMPTED) != null;
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

    public void setConsentManager(ConsentManager consentManager) {
        this.consentManager = consentManager;
    }


    private AddReceiptResponse addReceipt(String subject, String subjectTenantDomain, ApplicationConfig
            applicationConfig, String spTenantDomain, Set<String> claims) throws PostAuthenticationFailedException {

        String collectionMethod = "Web Form - Sign-in";
        String jurisdiction = "LK";
        String language = "us_EN";
        String consentType = "EXPLICIT";
        String termination = "DATE_UNTIL:INDEFINITE";
        String policyUrl = "http://nolink";

        Purpose purpose;

        try {
            purpose = consentManager.getPurposeByName(DEFAULT_PURPOSE);
        } catch (ConsentManagementClientException e) {

            if (ERROR_CODE_PURPOSE_NAME_INVALID.getCode().equals(e.getErrorCode())) {

                Purpose defaultPurpose = new Purpose(DEFAULT_PURPOSE, "Core functionality");
                try {
                    purpose = consentManager.addPurpose(defaultPurpose);
                } catch (ConsentManagementException e1) {
                    throw new PostAuthenticationFailedException("Consent purpose error", "Error while adding " +
                                                                                         "purpose: " +
                                                                                         DEFAULT_PURPOSE, e);
                }
            } else {
                throw new PostAuthenticationFailedException("Consent purpose error", "Error while retrieving purpose: "
                                                                                     + DEFAULT_PURPOSE, e);
            }
        } catch (ConsentManagementException e) {
            throw new PostAuthenticationFailedException("Consent purpose error", "Error while retrieving purpose: " +
                                                                                 DEFAULT_PURPOSE, e);
        }

        PurposeCategory purposeCategory;

        try {
            purposeCategory = consentManager.getPurposeCategoryByName(DEFAULT_PURPOSE_CATEGORY);
        } catch (ConsentManagementClientException e) {

            if (ERROR_CODE_PURPOSE_CAT_NAME_INVALID.getCode().equals(e.getErrorCode())) {

                PurposeCategory defaultPurposeCategory = new PurposeCategory(DEFAULT_PURPOSE_CATEGORY, "Core " +
                                                                                                       "functionality");
                try {
                    purposeCategory = consentManager.addPurposeCategory(defaultPurposeCategory);
                } catch (ConsentManagementException e1) {
                    throw new PostAuthenticationFailedException("Consent purpose category error", "Error while adding" +
                                                                  " purpose category:" + DEFAULT_PURPOSE_CATEGORY, e);
                }
            } else {
                throw new PostAuthenticationFailedException("Consent purpose category error", "Error while retrieving" +
                                                                  " purpose category: " + DEFAULT_PURPOSE_CATEGORY, e);
            }
        } catch (ConsentManagementException e) {
            throw new PostAuthenticationFailedException("Consent purpose category error", "Error while retrieving " +
                                                                  "purpose category: " + DEFAULT_PURPOSE_CATEGORY, e);
        }

        List<PIICategoryValidity> piiCategoryIds = new ArrayList<>();

        for (String claim : claims) {

            PIICategory piiCategory;
            try {
                piiCategory = consentManager.getPIICategoryByName(claim);
            } catch (ConsentManagementClientException e) {

                if (ERROR_CODE_PII_CAT_NAME_INVALID.getCode().equals(e.getErrorCode())) {

                    PIICategory defaultPIICategory = new PIICategory(claim, claim, false);
                    try {
                        piiCategory = consentManager.addPIICategory(defaultPIICategory);
                    } catch (ConsentManagementException e1) {
                        throw new PostAuthenticationFailedException("Consent PII category error", "Error while adding" +
                                                                      " PII category:" + DEFAULT_PURPOSE_CATEGORY, e);
                    }
                } else {
                    throw new PostAuthenticationFailedException("Consent PII category error", "Error while retrieving" +
                                                                      " PII category: " + DEFAULT_PURPOSE_CATEGORY, e);
                }
            } catch (ConsentManagementException e) {
                throw new PostAuthenticationFailedException("Consent PII category error", "Error while retrieving " +
                                                                      "PII category: " + DEFAULT_PURPOSE_CATEGORY, e);
            }
            piiCategoryIds.add(new PIICategoryValidity(piiCategory.getId(), termination));
        }

        List<ReceiptServiceInput> serviceInputs = new ArrayList<>();
        List<ReceiptPurposeInput> purposeInputs = new ArrayList<>();
        List<Integer> purposeCategoryIds = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        purposeCategoryIds.add(purposeCategory.getId());

        ReceiptPurposeInput purposeInput = new ReceiptPurposeInput();
        purposeInput.setPrimaryPurpose(true);
        purposeInput.setTermination(termination);
        purposeInput.setConsentType(consentType);
        purposeInput.setThirdPartyDisclosure(false);
        purposeInput.setPurposeId(purpose.getId());
        purposeInput.setPurposeCategoryId(purposeCategoryIds);
        purposeInput.setPiiCategory(piiCategoryIds);

        purposeInputs.add(purposeInput);

        ReceiptServiceInput serviceInput = new ReceiptServiceInput();
        serviceInput.setPurposes(purposeInputs);
        serviceInput.setTenantDomain(spTenantDomain);

        String spName = applicationConfig.getApplicationName();
        ServiceProvider serviceProvider = applicationConfig.getServiceProvider();
        String spDescription = null;
        if (serviceProvider != null) {
            spDescription = serviceProvider.getDescription();
        }
        if (StringUtils.isBlank(spDescription)) {
            spDescription = spName;
        }
        serviceInput.setService(spName);
        serviceInput.setSpDisplayName(spDescription);
        serviceInput.setSpDescription(spDescription);

        serviceInputs.add(serviceInput);

        ReceiptInput receiptInput = new ReceiptInput();

        receiptInput.setCollectionMethod(collectionMethod);
        receiptInput.setJurisdiction(jurisdiction);
        receiptInput.setLanguage(language);
        receiptInput.setPolicyUrl(policyUrl);
        receiptInput.setServices(serviceInputs);
        receiptInput.setProperties(properties);
        receiptInput.setPiiPrincipalId(subject);

        AddReceiptResponse receiptResponse;
        try {
            startTenantFlow(subjectTenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subject);
            receiptResponse = consentManager.addConsent(receiptInput);
        } catch (ConsentManagementException e) {
            throw new PostAuthenticationFailedException("Consent receipt error", "Error while adding the consent " +
                                                                                 "receipt", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        if (log.isDebugEnabled()) {
            log.info("Successfully added consent receipt: " + receiptResponse.getConsentReceiptId());
        }
        return receiptResponse;
    }

    private void removeUserClaimsFromContext(AuthenticationContext context, List<String> disapprovedClaims) {

        Map<String, String> carbonToSPClaimMapping = getCarbonToSPClaimMapping(context);

        Map<ClaimMapping, String> userAttributes = context.getSequenceConfig().getAuthenticatedUser()
                                                          .getUserAttributes();
        Map<ClaimMapping, String> modifiedUserAttributes = new HashMap<>();

        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {

            String localClaimUri = entry.getKey().getLocalClaim().getClaimUri();

            if (!disapprovedClaims.contains(localClaimUri) &&
                !disapprovedClaims.contains(carbonToSPClaimMapping.get(localClaimUri))) {
                modifiedUserAttributes.put(entry.getKey(), entry.getValue());
            }
        }

        context.getSequenceConfig().getAuthenticatedUser().setUserAttributes(modifiedUserAttributes);
    }

    private Map<String, String> getCarbonToSPClaimMapping(AuthenticationContext context) {

        Map<String, String> carbonToSPClaimMapping = new HashMap<>();
        Object spToCarbonClaimMappingObject = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);

        if (spToCarbonClaimMappingObject != null && spToCarbonClaimMappingObject instanceof Map) {

            Map<String, String> spToCarbonClaimMapping = (Map<String, String>) spToCarbonClaimMappingObject;

            for (Map.Entry<String, String> entry : spToCarbonClaimMapping.entrySet()) {
                carbonToSPClaimMapping.put(entry.getValue(), entry.getKey());
            }
        } else {

            Map<String, String> claimMappings = context.getSequenceConfig().getApplicationConfig()
                                                       .getRequestedClaimMappings();

            if (MapUtils.isNotEmpty(claimMappings)) {
                carbonToSPClaimMapping = claimMappings;
            }
        }
        return carbonToSPClaimMapping;
    }

    protected boolean isConsentForClaimValid(PIICategoryValidity piiCategoryValidity) {

        return true;
    }

    private class UserConsent {

        private List<String> approvedClaims = new ArrayList<>();
        private List<String> disapprovedClaims = new ArrayList<>();

        List<String> getApprovedClaims() {
            return approvedClaims;
        }

        void setApprovedClaims(List<String> approvedClaims) {
            this.approvedClaims = approvedClaims;
        }

        List<String> getDisapprovedClaims() {
            return disapprovedClaims;
        }

        void setDisapprovedClaims(List<String> disapprovedClaims) {
            this.disapprovedClaims = disapprovedClaims;
        }
    }
}


