/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategoryBinding;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentReceiptUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REJECTED_STATE;
import static org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.constant.SSOConsentConstants.RESIDENT_IDP;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.PURPOSE_GROUP_TYPE_POLICY;

/**
 * Post-authentication handler that enforces system-wide policy consent (e.g. Terms of Use, Privacy Policy).
 * Mandatory policies (those with a mandatory element on the latest version) block login if not accepted.
 * Optional policies are shown once per version; skipping records a REJECTED receipt so the version
 * is not shown again until a new version is published.
 * Skipped for API-based authentication flows where browser redirects are not possible.
 */
public class PolicyConsentPostAuthnHandler extends AbstractPostAuthnHandler {

    private static final Log LOG = LogFactory.getLog(PolicyConsentPostAuthnHandler.class);

    private static final String POLICY_CONSENT_PROMPTED = "policyConsentPrompted";
    private static final String USER_CONSENT_INPUT = "consent";
    private static final String USER_CONSENT_APPROVE = "approve";
    private static final String LOGIN_ENDPOINT = "login.do";
    private static final String POLICY_CONSENT_ENDPOINT = "policy_consent.do";
    private static final String OPTIONAL_PURPOSE_ID_PARAM = "optionalPurposeId";
    private static final String POLICY_MANDATORY_IDS_PARAM = "policyMandatoryIds";
    private static final String POLICY_OPTIONAL_IDS_PARAM = "policyOptionalIds";

    @Override
    public String getName() {

        return "PolicyConsentPostAuthenticationHandler";
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        if (!FrameworkUtils.isConsentV2APIEnabled()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Consent V2 API is disabled. Skipping policy consent handling.");
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (authenticatedUser == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User not available in AuthenticationContext. Skipping policy consent handling.");
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (authenticatedUser.isFederatedUser() || authenticatedUser.isSharedUser()) {
            if (LOG.isDebugEnabled()) {
                String userType = authenticatedUser.isFederatedUser() ? "Federated" : "Shared";
                LOG.debug(userType + " user detected. Skipping policy consent handling for user: "
                        + (LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(
                        authenticatedUser.getAuthenticatedSubjectIdentifier())
                        : authenticatedUser.getAuthenticatedSubjectIdentifier()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (getServiceProvider(context).isSaasApp()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("SaaS application detected. Skipping policy consent handling for user: "
                        + (LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(
                                authenticatedUser.getAuthenticatedSubjectIdentifier())
                                : authenticatedUser.getAuthenticatedSubjectIdentifier()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (FrameworkUtils.isAPIBasedAuthenticationFlow(request)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("API-based authentication flow detected. Skipping policy consent redirect for user: "
                        + (LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(
                                authenticatedUser.getAuthenticatedSubjectIdentifier())
                                : authenticatedUser.getAuthenticatedSubjectIdentifier()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isPolicyConsentPrompted(context)) {
            return handlePostPolicyConsent(request, context);
        }
        return handlePrePolicyConsent(response, context);
    }

    /**
     * Checks for unconsented policy purposes and redirects the user to the policy consent page if any are found.
     * Only policies mapped to the current application via the consent-purpose-mapping resource type are considered.
     * The JSP retrieves the actual purpose lists via {@link PolicyConsentUtil} at render time.
     */
    private PostAuthnHandlerFlowStatus handlePrePolicyConsent(HttpServletResponse response,
                                                              AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        String subjectId = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(),
                authenticatedUser.getUserStoreDomain());
        String tenantDomain = authenticatedUser.getTenantDomain();

        Set<String> mappedPolicyIds = getMappedPolicyIds(context);
        if (mappedPolicyIds.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("No policy mappings found for application: %s. "
                        + "Skipping policy consent handling.",
                        getServiceProvider(context).getApplicationResourceId()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        boolean hasUnconsentedPolicies;
        try {
            hasUnconsentedPolicies = PolicyConsentUtil.hasUnconsentedPolicies(subjectId, tenantDomain, mappedPolicyIds);
        } catch (ConsentManagementException e) {
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.PROCESS_POLICY_CONSENT)
                        .inputParam(LogConstants.InputKeys.USER, LoggerUtils.isLogMaskingEnable ?
                                LoggerUtils.getMaskedContent(subjectId) : subjectId)
                        .inputParam(LogConstants.InputKeys.TENANT_DOMAIN, tenantDomain)
                        .inputParam(LogConstants.InputKeys.ERROR_MESSAGE, e.getMessage())
                        .resultMessage("Error occurred while retrieving unconsented policy purposes.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    String.format("Error retrieving unconsented policy purposes for user: %s in tenant: %s.",
                            subjectId, tenantDomain), e);
        }

        if (!hasUnconsentedPolicies) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("No unconsented policy purposes found for user: %s. "
                        + "Policy consent handling complete.", LoggerUtils.isLogMaskingEnable ?
                        LoggerUtils.getMaskedContent(subjectId) : subjectId));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        setPolicyConsentPromptedState(context);
        try {
            redirectToPolicyConsentPage(response, context);
        } catch (IOException | URISyntaxException e) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    "Error while redirecting to the policy consent page.", e);
        }
        return PostAuthnHandlerFlowStatus.INCOMPLETE;
    }

    /**
     * Processes the user's response from the policy consent page.
     * Mandatory and optional purpose IDs are read from hidden form fields rendered by the JSP
     * ({@value POLICY_MANDATORY_IDS_PARAM} and {@value POLICY_OPTIONAL_IDS_PARAM}).
     */
    private PostAuthnHandlerFlowStatus handlePostPolicyConsent(HttpServletRequest request,
                                                                 AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        String subjectId = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(),
                authenticatedUser.getUserStoreDomain());
        String tenantDomain = authenticatedUser.getTenantDomain();

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = null;
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                    FrameworkConstants.LogConstants.ActionIDs.PROCESS_POLICY_CONSENT)
                    .inputParam(LogConstants.InputKeys.USER, LoggerUtils.isLogMaskingEnable ?
                            LoggerUtils.getMaskedContent(subjectId) : subjectId)
                    .inputParam(LogConstants.InputKeys.TENANT_DOMAIN, tenantDomain)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION);
        }

        String consentInput = request.getParameter(USER_CONSENT_INPUT);
        if (!USER_CONSENT_APPROVE.equalsIgnoreCase(consentInput)) {
            if (diagnosticLogBuilder != null) {
                diagnosticLogBuilder.resultMessage("User denied consent for the required policies.")
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED);
                LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
            }
            return handleDeniedConsent(context);
        }

        String[] mandatoryParam = request.getParameterValues(POLICY_MANDATORY_IDS_PARAM);
        List<String> mandatoryIds = (mandatoryParam != null)
                ? new ArrayList<>(Arrays.asList(mandatoryParam))
                : Collections.emptyList();

        String[] optionalParam = request.getParameterValues(POLICY_OPTIONAL_IDS_PARAM);
        List<String> optionalIds = (optionalParam != null)
                ? new ArrayList<>(Arrays.asList(optionalParam))
                : Collections.emptyList();

        Set<String> mappedPolicyIds = getMappedPolicyIds(context);

        Set<String> allowedMandatoryIds;
        Set<String> allowedOptionalIds;
        try {
            PolicyConsentUtil.ClassifiedPolicies classified =
                    PolicyConsentUtil.classifyUnconsentedPolicies(subjectId, tenantDomain, mappedPolicyIds);
            Set<String> expectedMandatoryIds = new HashSet<>(classified.getMandatoryUnconsentedIds());
            expectedMandatoryIds.addAll(classified.getMandatoryNewVersionIds());
            if (!new HashSet<>(mandatoryIds).containsAll(expectedMandatoryIds)) {
                if (diagnosticLogBuilder != null) {
                    diagnosticLogBuilder
                            .resultMessage("Submitted mandatory policy IDs do not cover all required policies.")
                            .resultStatus(DiagnosticLog.ResultStatus.FAILED);
                    LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                }
                return handleDeniedConsent(context);
            }
            allowedMandatoryIds = expectedMandatoryIds;
            allowedOptionalIds = new HashSet<>(classified.getOptionalUnconsentedIds());
            allowedOptionalIds.addAll(classified.getOptionalNewVersionIds());
        } catch (ConsentManagementException e) {
            if (diagnosticLogBuilder != null) {
                diagnosticLogBuilder.inputParam(LogConstants.InputKeys.ERROR_MESSAGE, e.getMessage())
                        .resultMessage("Error occurred while validating mandatory policy consent.")
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED);
                LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
            }
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    String.format("Error validating mandatory policy consent for user: %s in tenant: %s.",
                            subjectId, tenantDomain), e);
        }

        mandatoryIds = mandatoryIds.stream().filter(allowedMandatoryIds::contains).toList();
        optionalIds = optionalIds.stream().filter(allowedOptionalIds::contains).toList();

        String[] approvedOptionalParam = request.getParameterValues(OPTIONAL_PURPOSE_ID_PARAM);
        Set<String> approvedOptionalIds = (approvedOptionalParam != null)
                ? new HashSet<>(Arrays.asList(approvedOptionalParam))
                : Collections.emptySet();

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(subjectId);
            for (String purposeId : mandatoryIds) {
                recordPolicyConsent(subjectId, tenantDomain, purposeId, ACTIVE_STATE);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Recorded ACTIVE policy consent for user: %s, purpose: %s.",
                            LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(subjectId) : subjectId,
                            purposeId));
                }
            }
            for (String purposeId : optionalIds) {
                String state = approvedOptionalIds.contains(purposeId) ? ACTIVE_STATE : REJECTED_STATE;
                recordPolicyConsent(subjectId, tenantDomain, purposeId, state);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Recorded %s policy consent for user: %s, purpose: %s.",
                            state,
                            LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(subjectId) : subjectId,
                            purposeId));
                }
            }
        } catch (ConsentManagementException e) {
            if (diagnosticLogBuilder != null) {
                diagnosticLogBuilder.inputParam(LogConstants.InputKeys.ERROR_MESSAGE, e.getMessage())
                        .resultMessage("Error occurred while processing policy consent.")
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED);
                LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
            }
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    String.format("Error recording policy consent for user: %s in tenant: %s.",
                            subjectId, tenantDomain), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private void recordPolicyConsent(String subjectId, String tenantDomain, String purposeUuid, String state)
            throws ConsentManagementException {

        PIICategory piiCategory = ConsentReceiptUtils.getDefaultPiiCategory(
                PURPOSE_GROUP_TYPE_POLICY, getConsentManager());
        List<PurposePIICategoryBinding> purposeBindings = new ArrayList<>();
        purposeBindings.add(new PurposePIICategoryBinding(purposeUuid, Collections.singletonList(piiCategory)));
        ReceiptInput receiptInput = ConsentReceiptUtils.buildReceiptInput("en", subjectId, tenantDomain,
                null, REJECTED_STATE.equals(state), null, null, RESIDENT_IDP, purposeBindings,
                getConsentManager());
        getConsentManager().addConsent(receiptInput);
    }

    private void redirectToPolicyConsentPage(HttpServletResponse response, AuthenticationContext context)
            throws IOException, URISyntaxException {

        String policyConsentUrl = ConfigurationFacade.getInstance()
                .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, POLICY_CONSENT_ENDPOINT);
        URIBuilder uriBuilder = new URIBuilder(policyConsentUrl);
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY, context.getContextIdentifier());
        response.sendRedirect(uriBuilder.build().toString());
    }

    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext context) {

        return context.getSequenceConfig().getAuthenticatedUser();
    }

    @SuppressWarnings("unchecked")
    private void setPolicyConsentPromptedState(AuthenticationContext context) {

        context.addParameter(POLICY_CONSENT_PROMPTED, true);
    }

    @SuppressWarnings("unchecked")
    private boolean isPolicyConsentPrompted(AuthenticationContext context) {

        return context.getParameter(POLICY_CONSENT_PROMPTED) != null;
    }

    private ConsentManager getConsentManager() {

        return FrameworkServiceDataHolder.getInstance().getConsentManager();
    }

    private PostAuthnHandlerFlowStatus handleDeniedConsent(AuthenticationContext context) {

        context.setRequestAuthenticated(false);
        return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
    }

    private Set<String> getMappedPolicyIds(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        String appResourceId = getServiceProvider(context).getApplicationResourceId();
        try {
            List<String> purposeIds = FrameworkServiceDataHolder.getInstance()
                    .getConsentAppMappingService()
                    .getPurposesForApplication(appResourceId);
            return new HashSet<>(purposeIds);
        } catch (ConsentAppMappingException e) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    String.format("Error retrieving policy config mappings for application: %s in tenant: %s.",
                            appResourceId, context.getTenantDomain()), e);
        }
    }

    /**
     * Resolves the service provider for the authentication context.
     * Prefers the service provider already loaded into the sequence configuration to avoid a redundant
     * database lookup, and only falls back to the application management service if it is unavailable.
     */
    private ServiceProvider getServiceProvider(AuthenticationContext context) throws PostAuthenticationFailedException {

        if (context.getSequenceConfig() != null && context.getSequenceConfig().getApplicationConfig() != null) {
            ServiceProvider serviceProvider = context.getSequenceConfig().getApplicationConfig().getServiceProvider();
            if (serviceProvider != null) {
                return serviceProvider;
            }
        }
        try {
            return FrameworkServiceDataHolder.getInstance().getApplicationManagementService()
                    .getServiceProvider(context.getServiceProviderName(), context.getTenantDomain());
        } catch (IdentityApplicationManagementException e) {
            throw new PostAuthenticationFailedException(
                    ErrorMessages.ERROR_WHILE_PROCESSING_POLICY_CONSENT.getCode(),
                    String.format("Error retrieving service provider: %s in tenant: %s.",
                            context.getServiceProviderName(), context.getTenantDomain()), e);
        }
    }
}
