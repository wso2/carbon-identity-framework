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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception.SSOConsentDisabledException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception.SSOConsentServiceException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This is an extension of {@link AbstractPostAuthnHandler} which handles user consent management upon successful
 * user authentication.
 */
public class ConsentMgtPostAuthnHandler extends AbstractPostAuthnHandler {

    private static final String HTTP_WSO2_ORG_OIDC_CLAIM = "http://wso2.org/oidc/claim";
    private static final String HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY
            = "http://schemas.xmlsoap.org/ws/2005/05/identity";
    private static final String HTTP_AXSCHEMA_ORG = "http://axschema.org";
    private static final String URN_SCIM_SCHEMAS_CORE_1_0 = "urn:scim:schemas:core:1.0";
    private static final String CONSENT_PROMPTED = "consentPrompted";
    private static final String CLAIM_SEPARATOR = ",";
    private static final String REQUESTED_CLAIMS_PARAM = "requestedClaims";
    private static final String MANDATORY_CLAIMS_PARAM = "mandatoryClaims";
    private static final String CONSENT_CLAIM_META_DATA = "consentClaimMetaData";
    private static final String REQUEST_TYPE_OAUTH2 = "oauth2";
    private static final String SP_NAME_DEFAULT = "DEFAULT";
    private static final String USER_CONSENT_INPUT = "consent";
    private static final String USER_CONSENT_APPROVE = "approve";
    private static final String LOGIN_ENDPOINT = "login.do";
    private static final String CONSENT_ENDPOINT = "consent.do";
    private static final Log LOG = LogFactory.getLog(ConsentMgtPostAuthnHandler.class);

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (authenticatedUser == null) {
            if (isDebugEnabled()) {
                String message = "User not available in AuthenticationContext. Returning";
                logDebug(message);
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        // If OAuth flow, skip handling consent from the authentication handler. OAuth related consent will be
        // handled from OAuth endpoint. OpenID flow is skipped as it is deprecated.
        if (isOAuthFlow(context) || isOpenIDFlow(context)) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        // Check whether currently engaged SP has skipConsent enabled
        if (FrameworkUtils.isConsentPageSkippedForSP(getServiceProvider(context))) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isConsentPrompted(context)) {
            return handlePostConsent(request, response, context);
        } else {
            return handlePreConsent(request, response, context);
        }
    }

    private boolean isOAuthFlow(AuthenticationContext context) {

        return FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(context.getRequestType()) || REQUEST_TYPE_OAUTH2
                .equalsIgnoreCase(context.getRequestType());
    }

    private boolean isOpenIDFlow(AuthenticationContext context) {

        return FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(context.getRequestType());
    }

    private boolean isDebugEnabled() {

        return LOG.isDebugEnabled();
    }

    private void logDebug(String message) {

        LOG.debug(message);
    }

    protected PostAuthnHandlerFlowStatus handlePreConsent(HttpServletRequest request, HttpServletResponse response,
                                                          AuthenticationContext context)
            throws PostAuthenticationFailedException {

        String spName = context.getSequenceConfig().getApplicationConfig().getApplicationName();
        Map<String, String> claimMappings = context.getSequenceConfig().getApplicationConfig().getClaimMappings();

        // Due to: https://github.com/wso2/product-is/issues/2317.
        // Should be removed once the issue is fixed
        if (SP_NAME_DEFAULT.equalsIgnoreCase(spName)) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        ServiceProvider serviceProvider = getServiceProvider(context);
        try {
            ConsentClaimsData consentClaimsData = getSSOConsentService().getConsentRequiredClaimsWithExistingConsents
                    (serviceProvider, authenticatedUser);

            if (isDebugEnabled()) {
                String message = String.format("Retrieving required consent data of user: %s for service " +
                                               "provider: %s in tenant domain: %s.",
                                               authenticatedUser.getAuthenticatedSubjectIdentifier(),
                                               serviceProvider.getApplicationName(),
                                               getSPTenantDomain(serviceProvider));
                logDebug(message);
            }

            removeClaimsWithoutConsent(context, consentClaimsData);
            // Remove the claims which dont have values given by the user.
            consentClaimsData.setRequestedClaims(
                    removeConsentRequestedNullUserAttributes(consentClaimsData.getRequestedClaims(),
                            authenticatedUser.getUserAttributes(), claimMappings));
            if (hasConsentForRequiredClaims(consentClaimsData)) {

                if (isDebugEnabled()) {
                    String message = String.format("Required consent data is empty for user: %s for service " +
                                                   "provider: %s in tenant domain: %s. Post authentication completed.",
                                                   authenticatedUser.getAuthenticatedSubjectIdentifier(),
                                                   serviceProvider.getApplicationName(),
                                                   getSPTenantDomain(serviceProvider));
                    logDebug(message);
                }
                return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
            } else {
                String mandatoryLocalClaims = buildConsentClaimString(consentClaimsData.getMandatoryClaims());
                String requestedLocalClaims = buildConsentClaimString(consentClaimsData.getRequestedClaims());

                if (isDebugEnabled()) {
                    String message = "Require consent for mandatory claims: %s, requested claims: %s, from user: %s " +
                            "for service provider: %s in tenant domain: %s.";

                    message = String.format(message, consentClaimsData.getMandatoryClaims(),
                            consentClaimsData.getRequestedClaims(),
                            authenticatedUser.getAuthenticatedSubjectIdentifier(),
                            serviceProvider.getApplicationName(), getSPTenantDomain(serviceProvider));
                    logDebug(message);
                }
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put(FrameworkConstants.LogConstants.MANDATORY_CLAIMS,
                            consentClaimsData.getMandatoryClaims());
                    params.put(FrameworkConstants.LogConstants.REQUESTED_CLAIMS,
                            consentClaimsData.getRequestedClaims());
                    params.put(FrameworkConstants.LogConstants.USER,
                            LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(authenticatedUser
                                    .getAuthenticatedSubjectIdentifier())
                                    : authenticatedUser.getAuthenticatedSubjectIdentifier());
                    params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, serviceProvider.getApplicationName());
                    params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, getSPTenantDomain(serviceProvider));
                    LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                            params, LogConstants.SUCCESS, "Require consent for claims from user",
                            FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
                }

                redirectToConsentPage(response, context, requestedLocalClaims, mandatoryLocalClaims);
                setConsentPoppedUpState(context);
                context.addParameter(CONSENT_CLAIM_META_DATA, consentClaimsData);

                return PostAuthnHandlerFlowStatus.INCOMPLETE;
            }
        } catch (SSOConsentDisabledException e) {
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        } catch (SSOConsentServiceException e) {
            String error = String.format("Error occurred while retrieving consent data of user: %s for service " +
                    "provider: %s in tenant domain: %s.", authenticatedUser
                    .getAuthenticatedSubjectIdentifier(), serviceProvider.getApplicationName(), getSPTenantDomain
                    (serviceProvider));
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                Map<String, Object> params = new HashMap<>();
                params.put(FrameworkConstants.LogConstants.USER, LoggerUtils.isLogMaskingEnable ?
                        LoggerUtils.getMaskedContent(authenticatedUser.getAuthenticatedSubjectIdentifier()) :
                        authenticatedUser.getAuthenticatedSubjectIdentifier());
                params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, serviceProvider.getApplicationName());
                params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, getSPTenantDomain(serviceProvider));
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.FAILED, "Error occurred while processing consent data of user: " + e.getMessage(),
                        FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
            }
            throw new PostAuthenticationFailedException("Authentication failed. Error occurred while processing user " +
                    "consent.", error, e);
        }
    }

    private boolean hasConsentForRequiredClaims(ConsentClaimsData consentClaimsData) {

        return isEmpty(consentClaimsData.getMandatoryClaims()) && isEmpty(consentClaimsData.getRequestedClaims());
    }

    private void removeClaimsWithoutConsent(AuthenticationContext context, ConsentClaimsData consentClaimsData)
            throws PostAuthenticationFailedException {

        List<ClaimMetaData> approvedAndNewlyRequestedConsents = consentClaimsData.getClaimsWithConsent();
        approvedAndNewlyRequestedConsents.addAll(consentClaimsData.getRequestedClaims());
        approvedAndNewlyRequestedConsents.addAll(consentClaimsData.getMandatoryClaims());
        List<String> claimsURIsOfApprovedAndNewlyRequestedConsents =
                getClaimsFromMetaData(approvedAndNewlyRequestedConsents);
        List<String> claimsWithoutConsent =
                getClaimsWithoutConsent(claimsURIsOfApprovedAndNewlyRequestedConsents, context);
        String spStandardDialect = getStandardDialect(context);
        removeUserClaimsFromContext(context, claimsWithoutConsent, spStandardDialect);
    }

    /**
     * Filter out the requested claims with the user attributes.
     *
     * @param requestedClaims List of requested claims metadata.
     * @param userAttributes  Authenticated users' attributes.
     * @param claimMappings   Claim mappings of the application.
     * @return Filtered claims with user attributes.
     */
    private List<ClaimMetaData> removeConsentRequestedNullUserAttributes(List<ClaimMetaData> requestedClaims,
                                                                         Map<ClaimMapping, String> userAttributes,
                                                                         Map<String, String> claimMappings) {

        List<ClaimMetaData> filteredRequestedClaims = new ArrayList<>();
        if (requestedClaims != null && userAttributes != null && claimMappings != null) {
            for (ClaimMetaData claimMetaData : requestedClaims) {
                for (Map.Entry<ClaimMapping, String> attribute : userAttributes.entrySet()) {
                    if (claimMetaData.getClaimUri()
                            .equals(claimMappings.get(attribute.getKey().getLocalClaim().getClaimUri()))) {
                        filteredRequestedClaims.add(claimMetaData);
                        break;
                    }
                }
            }
        }
        return filteredRequestedClaims;
    }

    private ServiceProvider getServiceProvider(AuthenticationContext context) {

        return context.getSequenceConfig().getApplicationConfig().getServiceProvider();
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

    private List<String> getClaimsWithoutConsent(List<String> approvedAndNewlyRequestedConsents,
                                                 AuthenticationContext context)
            throws PostAuthenticationFailedException {

        List<String> claimsWithoutConsent = getSPRequestedLocalClaims(context);
        claimsWithoutConsent.removeAll(approvedAndNewlyRequestedConsents);
        return claimsWithoutConsent;
    }

    private String buildConsentClaimString(List<ClaimMetaData> consentClaimsData) {

        StringJoiner joiner = new StringJoiner(CLAIM_SEPARATOR);
        for (ClaimMetaData claimMetaData : consentClaimsData) {
            joiner.add(claimMetaData.getId() + "_" + claimMetaData.getDisplayName());
        }
        return joiner.toString();
    }

    protected PostAuthnHandlerFlowStatus handlePostConsent(HttpServletRequest request, HttpServletResponse response,
                                                           AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        Map<String, String> claimMappings = applicationConfig.getClaimMappings();
        ServiceProvider serviceProvider = getServiceProvider(context);
        Map<String, Object> params = new HashMap<>();
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            params.put(FrameworkConstants.LogConstants.USER, LoggerUtils.isLogMaskingEnable ?
                    LoggerUtils.getMaskedContent(authenticatedUser.getAuthenticatedSubjectIdentifier()) :
                    authenticatedUser.getAuthenticatedSubjectIdentifier());
            params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, serviceProvider.getApplicationName());
            params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, getSPTenantDomain(serviceProvider));
        }
        if (request.getParameter(USER_CONSENT_INPUT).equalsIgnoreCase(USER_CONSENT_APPROVE)) {
            if (isDebugEnabled()) {

                String message = "User: %s has approved consent for service provider: %s in tenant domain %s.";
                message = String.format(message, authenticatedUser.getAuthenticatedSubjectIdentifier(),
                                        serviceProvider.getApplicationName(), getSPTenantDomain(serviceProvider));
                logDebug(message);
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.SUCCESS, "User has approved consent for service provider.",
                        FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
            }
            UserConsent userConsent = processUserConsent(request, context);
            ConsentClaimsData consentClaimsData = getConsentClaimsData(context, authenticatedUser, serviceProvider);
            // Remove the claims which dont have values given by the user.
            consentClaimsData.setRequestedClaims(
                    removeConsentRequestedNullUserAttributes(consentClaimsData.getRequestedClaims(),
                    authenticatedUser.getUserAttributes(), claimMappings));
            try {

                List<Integer> claimIdsWithConsent = getClaimIdsWithConsent(userConsent);
                getSSOConsentService().processConsent(claimIdsWithConsent, serviceProvider, authenticatedUser,
                                                 consentClaimsData);
                removeDisapprovedClaims(context, authenticatedUser);
            } catch (SSOConsentDisabledException e) {
                String error = "Authentication Failure: Consent management is disabled for SSO.";
                String errorDesc = "Illegal operation. Consent management is disabled, but post authentication for " +
                                   "sso consent management is invoked.";
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                            params, LogConstants.FAILED, "Consent management is disabled for SSO: " + e.getMessage(),
                            FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
                }
                throw new PostAuthenticationFailedException(error, errorDesc, e);
            } catch (SSOConsentServiceException e) {
                String error = "Error occurred while processing consent input of user: %s, for service provider: %s " +
                               "in tenant domain: %s.";
                error = String.format(error, authenticatedUser.getAuthenticatedSubjectIdentifier(), serviceProvider
                        .getApplicationName(), getSPTenantDomain(serviceProvider));
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                            params, LogConstants.FAILED, "Error occurred while processing consent input: "
                                    + e.getMessage(), FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT,
                            null);
                }
                throw new PostAuthenticationFailedException("Authentication failed. Error while processing user " +
                                                            "consent input.", error, e);
            }
        } else {

            String error = String.format("Authentication failed. User denied consent to share information with %s.",
                    applicationConfig.getApplicationName());
            if (isDebugEnabled()) {
                logDebug(String.format("User: %s denied consent to share information with the service " +
                                "provider: %s.", authenticatedUser.getAuthenticatedSubjectIdentifier(),
                        applicationConfig.getApplicationName()));
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.FAILED, "User denied consent to share information with the service provider.",
                        FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
            }
            throw new PostAuthenticationFailedException(error, error);
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private ConsentClaimsData getConsentClaimsData(AuthenticationContext context, AuthenticatedUser authenticatedUser,
                                                   ServiceProvider serviceProvider)
            throws PostAuthenticationFailedException {

        ConsentClaimsData consentClaimsData = (ConsentClaimsData) context.getParameter(CONSENT_CLAIM_META_DATA);

        if (consentClaimsData == null) {

            if (isDebugEnabled()) {
                logDebug("Cannot find " + CONSENT_CLAIM_META_DATA + " entry in AuthenticationContext. Retrieving from" +
                         " SSOConsentService.");
            }
            try {
                consentClaimsData = getSSOConsentService().getConsentRequiredClaimsWithExistingConsents(serviceProvider,
                                                                                                   authenticatedUser);
            } catch (SSOConsentDisabledException e) {
                String error = "Authentication Failure: Consent management is disabled for SSO.";
                String errorDesc = "Illegal operation. Consent management is disabled, but post authentication for " +
                                   "sso consent management is invoked.";
                throw new PostAuthenticationFailedException(error, errorDesc, e);
            } catch (SSOConsentServiceException e) {
                String error = String.format("Error occurred while retrieving consent data of user: %s for service " +
                                             "provider: %s in tenant domain: %s.",
                                             authenticatedUser.getAuthenticatedSubjectIdentifier(),
                                             serviceProvider.getApplicationName(), getSPTenantDomain(serviceProvider));
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put(FrameworkConstants.LogConstants.USER, LoggerUtils.isLogMaskingEnable ?
                            LoggerUtils.getMaskedContent(authenticatedUser.getAuthenticatedSubjectIdentifier()) :
                            authenticatedUser.getAuthenticatedSubjectIdentifier());
                    params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, serviceProvider.getApplicationName());
                    params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, getSPTenantDomain(serviceProvider));
                    LoggerUtils.triggerDiagnosticLogEvent(
                            FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params, LogConstants.FAILED,
                            "Error occurred while processing user consent: " + e.getMessage(),
                            FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT, null);
                }
                throw new PostAuthenticationFailedException("Authentication failed. Error occurred while processing " +
                                                            "user consent.", error, e);
            }
        }
        return consentClaimsData;
    }

    private List<Integer> getClaimIdsWithConsent(UserConsent userConsent) {

        return userConsent.getApprovedClaims().stream().map(ClaimMetaData::getId).collect(Collectors.toList());
    }

    private void removeDisapprovedClaims(AuthenticationContext context, AuthenticatedUser authenticatedUser)
            throws SSOConsentServiceException, PostAuthenticationFailedException {

        String spStandardDialect = getStandardDialect(context);
        List<String> claimWithConsent = getClaimsFromMetaData(getSSOConsentService().
                getClaimsWithConsents(getServiceProvider(context), authenticatedUser));
        List<String> disapprovedClaims = getClaimsWithoutConsent(claimWithConsent, context);
        if (isDebugEnabled()) {
            String message = "Removing disapproved claims: %s in the dialect: %s by user: %s for service provider: %s" +
                             " in tenant domain: %s.";
            ServiceProvider serviceProvider = getServiceProvider(context);
            message = String.format(message, disapprovedClaims, defaultString(spStandardDialect),
                                    getAuthenticatedUser(context).getAuthenticatedSubjectIdentifier(),
                                    serviceProvider.getApplicationName(), getSPTenantDomain(serviceProvider));
            logDebug(message);
        }
        removeUserClaimsFromContext(context, disapprovedClaims, spStandardDialect);
    }

    private List<String> getClaimsFromMetaData(List<ClaimMetaData> claimMetaDataList) {

        List<String> claims = new ArrayList<>();
        for (ClaimMetaData claimMetaData : claimMetaDataList) {
            claims.add(claimMetaData.getClaimUri());
        }
        return claims;
    }

    private UserConsent processUserConsent(HttpServletRequest request, AuthenticationContext context) throws
            PostAuthenticationFailedException {

        String consentClaimsPrefix = "consent_";
        UserConsent userConsent = new UserConsent();

        ConsentClaimsData consentClaimsData = (ConsentClaimsData) context.getParameter(CONSENT_CLAIM_META_DATA);

        Map<String, String[]> requestParams = request.getParameterMap();
        List<ClaimMetaData> approvedClamMetaData = buildApprovedClaimList(consentClaimsPrefix, requestParams,
                                                                    consentClaimsData);

        List<ClaimMetaData> consentRequiredClaimMetaData = getConsentRequiredClaimMetaData(consentClaimsData);
        List<ClaimMetaData> disapprovedClaims = buildDisapprovedClaimList(consentRequiredClaimMetaData,
                                                                          approvedClamMetaData);

        if (isMandatoryClaimsDisapproved(consentClaimsData.getMandatoryClaims(), disapprovedClaims)) {
            throw new PostAuthenticationFailedException("Authentication failed. Consent denied for mandatory " +
                                                        "attributes.", "User denied consent to share mandatory " +
                                                                       "attributes.");
        }

        userConsent.setApprovedClaims(approvedClamMetaData);
        userConsent.setDisapprovedClaims(disapprovedClaims);

        return userConsent;
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

    private boolean isMandatoryClaimsDisapproved(List<ClaimMetaData> consentMandatoryClaims, List<ClaimMetaData>
            disapprovedClaims) {

        return isNotEmpty(consentMandatoryClaims) && !Collections.disjoint(disapprovedClaims, consentMandatoryClaims);
    }

    private List<ClaimMetaData> buildDisapprovedClaimList(List<ClaimMetaData> consentRequiredClaims, List<ClaimMetaData>
            approvedClaims) {

        List<ClaimMetaData> disapprovedClaims = new ArrayList<>();

        if (isNotEmpty(consentRequiredClaims)) {
            consentRequiredClaims.removeAll(approvedClaims);
            disapprovedClaims = consentRequiredClaims;
        }
        return disapprovedClaims;
    }

    private List<ClaimMetaData> buildApprovedClaimList(String consentClaimsPrefix, Map<String, String[]> requestParams,
                                                ConsentClaimsData consentClaimsData) {

        List<ClaimMetaData> approvedClaims = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            if (entry.getKey().startsWith(consentClaimsPrefix)) {
                String claimId = entry.getKey().substring(consentClaimsPrefix.length());

                ClaimMetaData consentClaim = new ClaimMetaData();

                try {
                    consentClaim.setId(Integer.parseInt(claimId));
                } catch (NumberFormatException e) {
                    // Invalid consent claim input. Ignore.
                    continue;
                }
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
        }
        return approvedClaims;
    }

    private void redirectToConsentPage(HttpServletResponse response, AuthenticationContext context,
                                       String requestedLocalClaims, String mandatoryLocalClaims) throws
            PostAuthenticationFailedException {

        URIBuilder uriBuilder;
        try {
            uriBuilder = getUriBuilder(context, requestedLocalClaims, mandatoryLocalClaims);
            response.sendRedirect(uriBuilder.build().toString());
        } catch (IOException e) {
            throw new PostAuthenticationFailedException("Authentication failed. Error while processing consent " +
                                                        "requirements.", "Error while redirecting to consent page.", e);
        } catch (URISyntaxException e) {
            throw new PostAuthenticationFailedException("Authentication failed. Error while processing consent " +
                                                        "requirements.", "Error while building redirect URI.", e);
        }
    }

    private String getSubjectClaimUri(ApplicationConfig applicationConfig) {

        String subjectClaimUri = applicationConfig.getSubjectClaimUri();
        if (isEmpty(subjectClaimUri)) {
            subjectClaimUri = SSOConsentConstants.USERNAME_CLAIM;
        }
        return subjectClaimUri;
    }

    private List<String> getSPRequestedLocalClaims(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        List<String> spRequestedLocalClaims = new ArrayList<>();
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        if (applicationConfig == null) {

            ServiceProvider serviceProvider = getServiceProvider(context);
            String error = "Application configs are null in AuthenticationContext for SP: " + serviceProvider
                    .getApplicationName() + " in tenant domain: " + getSPTenantDomain(serviceProvider);
            throw new PostAuthenticationFailedException("Authentication failed. Error while processing application " +
                                                        "claim configurations.", error);
        }

        Map<String, String> claimMappings = applicationConfig.getRequestedClaimMappings();

        if (isNotEmpty(claimMappings) && isNotEmpty(claimMappings.values())) {
            spRequestedLocalClaims = new ArrayList<>(claimMappings.values());
        }

        String subjectClaimUri = getSubjectClaimUri(applicationConfig);
        spRequestedLocalClaims.remove(subjectClaimUri);

        if (isDebugEnabled()) {
            String message = String.format("Requested claims for SP: %s - " + spRequestedLocalClaims,
                    applicationConfig.getApplicationName());
            logDebug(message);
        }

        return spRequestedLocalClaims;
    }

    private List<String> getSPMandatoryLocalClaims(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        List<String> spMandatoryLocalClaims = new ArrayList<>();
        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();

        if (applicationConfig == null) {
            ServiceProvider serviceProvider = getServiceProvider(context);
            String error = "Application configs are null in AuthenticationContext for SP: " + serviceProvider
                    .getApplicationName() + " in tenant domain: " + getSPTenantDomain(serviceProvider);
            throw new PostAuthenticationFailedException("Authentication failed. Error while processing application " +
                                                        "claim configurations.", error);
        }

        Map<String, String> claimMappings = applicationConfig.getMandatoryClaimMappings();

        if (isNotEmpty(claimMappings) && isNotEmpty(claimMappings.values())) {
            spMandatoryLocalClaims = new ArrayList<>(claimMappings.values());
        }
        String subjectClaimUri = getSubjectClaimUri(applicationConfig);
        if (!spMandatoryLocalClaims.contains(subjectClaimUri)) {
            spMandatoryLocalClaims.add(subjectClaimUri);
        }

        if (isDebugEnabled()) {
            String message = String.format("Mandatory claims for SP: %s - " + spMandatoryLocalClaims,
                    applicationConfig.getApplicationName());
            logDebug(message);
        }
        return spMandatoryLocalClaims;
    }

    private URIBuilder getUriBuilder(AuthenticationContext context, String requestedLocalClaims, String
            mandatoryLocalClaims) throws URISyntaxException {

        String consentEndpointUrl = ConfigurationFacade.getInstance()
                .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, CONSENT_ENDPOINT);
        URIBuilder uriBuilder;
        uriBuilder = new URIBuilder(consentEndpointUrl);

        if (isNotBlank(requestedLocalClaims)) {
            if (isDebugEnabled()) {
                logDebug("Appending requested local claims to redirect URI: " + requestedLocalClaims);
            }
            uriBuilder.addParameter(REQUESTED_CLAIMS_PARAM, requestedLocalClaims);
        }

        if (isNotBlank(mandatoryLocalClaims)) {
            if (isDebugEnabled()) {
                logDebug("Appending mandatory local claims to redirect URI: " + mandatoryLocalClaims);
            }
            uriBuilder.addParameter(MANDATORY_CLAIMS_PARAM, mandatoryLocalClaims);
        }
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                context.getContextIdentifier());
        uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_SP,
                context.getSequenceConfig().getApplicationConfig().getApplicationName());
        return uriBuilder;
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

    private void removeUserClaimsFromContext(AuthenticationContext context, List<String> disapprovedClaims,
                                             String spStandardDialect) {

        Map<ClaimMapping, String> userAttributes = getUserAttributes(context);
        Map<ClaimMapping, String> modifiedUserAttributes = new HashMap<>();

        if (isDebugEnabled()) {

            String message = "Removing disapproved claims: %s from context of user: %s for service provider: %s in " +
                             "tenant domain: %s";
            ServiceProvider serviceProvider = getServiceProvider(context);
            message = String.format(message, disapprovedClaims,
                                    getAuthenticatedUser(context).getAuthenticatedSubjectIdentifier(),
                                    serviceProvider.getApplicationName(), getSPTenantDomain(serviceProvider));
            logDebug(message);
        }

        if (isStandardDialect(spStandardDialect)) {
            Map<String, String> standardToCarbonClaimMappings = getSPToCarbonClaimMappings(context);
            filterClaims(userAttributes, disapprovedClaims, standardToCarbonClaimMappings, modifiedUserAttributes);
        } else {
            // WSO2 dialect or Non standards custom claim mappings.
            Map<String, String> customToLocalClaimMappings = context.getSequenceConfig().getApplicationConfig()
                    .getRequestedClaimMappings();
            filterClaims(userAttributes, disapprovedClaims, customToLocalClaimMappings, modifiedUserAttributes);
        }
        context.getSequenceConfig().getAuthenticatedUser().setUserAttributes(modifiedUserAttributes);
    }

    private boolean isWSO2StandardDialect(String spStandardDialect) {

        return StringUtils.equals(spStandardDialect, ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT);
    }

    private boolean isStandardDialect(String spStandardDialect) {

        return isNotBlank(spStandardDialect) && (!isWSO2StandardDialect(spStandardDialect));
    }

    private Map<ClaimMapping, String> getUserAttributes(AuthenticationContext context) {

        return context.getSequenceConfig().getAuthenticatedUser()
                .getUserAttributes();
    }

    private void filterClaims(Map<ClaimMapping, String> userAttributes, List<String> disapprovedClaims,
                              Map<String, String> claimMappings, Map<ClaimMapping, String> modifiedUserAttributes) {

        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            String claimKey = entry.getKey().getLocalClaim().getClaimUri();
            if (isConsentApprovedForClaim(disapprovedClaims, claimMappings, claimKey)) {
                modifiedUserAttributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private Map<String, String> getSPToCarbonClaimMappings(AuthenticationContext context) {

        Object mapping = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);
        if (mapping != null && mapping instanceof HashMap) {
            return (Map<String, String>) mapping;
        }
        return new HashMap<>();
    }

    private boolean isConsentApprovedForClaim(List<String> disapprovedClaims,
                                              Map<String, String> carbonToSPClaimMapping, String localClaimUri) {

        return !disapprovedClaims.contains(localClaimUri) &&
                !disapprovedClaims.contains(carbonToSPClaimMapping.get(localClaimUri));
    }

    private String getStandardDialect(AuthenticationContext context) {

        String clientType = context.getRequestType();
        ApplicationConfig appConfig = context.getSequenceConfig().getApplicationConfig();
        Map<String, String> claimMappings = appConfig.getClaimMappings();
        if (FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(clientType)) {
            return HTTP_WSO2_ORG_OIDC_CLAIM;
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_STS.equals(clientType)) {
            return HTTP_SCHEMAS_XMLSOAP_ORG_WS_2005_05_IDENTITY;
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(clientType)) {
            return HTTP_AXSCHEMA_ORG;
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_WSO2.equals(clientType)) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_SCIM.equals(clientType)) {
            return URN_SCIM_SCHEMAS_CORE_1_0;
        } else if (claimMappings == null || claimMappings.isEmpty()) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else {
            boolean isAtLeastOneNotEqual = false;
            for (Map.Entry<String, String> entry : claimMappings.entrySet()) {
                if (!entry.getKey().equalsIgnoreCase(entry.getValue())) {
                    isAtLeastOneNotEqual = true;
                    break;
                }
            }
            if (!isAtLeastOneNotEqual) {
                return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
            }
        }
        return null;
    }

    private SSOConsentService getSSOConsentService() {
        return FrameworkServiceDataHolder.getInstance().getSSOConsentService();
    }

    @Override
    public String getName() {

        return "ConsentMgtPostAuthenticationHandler";
    }
}
