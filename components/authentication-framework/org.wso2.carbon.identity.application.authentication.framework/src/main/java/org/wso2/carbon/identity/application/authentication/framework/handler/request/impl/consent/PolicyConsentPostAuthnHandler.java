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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategoryBinding;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentReceiptUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
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
import java.util.stream.Collectors;

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
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework.handler.request." +
                        "PostAuthenticationHandler",
                "service.scope=singleton"
        }
)
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

        if (authenticatedUser.isOrganizationUser()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sub-organization user detected. Skipping policy consent handling for user: "
                        + (LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(
                                authenticatedUser.getAuthenticatedSubjectIdentifier())
                                : authenticatedUser.getAuthenticatedSubjectIdentifier()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isSystemApplication(context)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("System application detected. Skipping policy consent handling for user: "
                        + (LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(
                                authenticatedUser.getAuthenticatedSubjectIdentifier())
                                : authenticatedUser.getAuthenticatedSubjectIdentifier()));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (FrameworkUtils.isConsentPageSkippedForSP(getServiceProvider(context))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Consent page skipped for service provider. Skipping policy consent handling for user: "
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
        return handlePrePolicyConsent(request, response, context);
    }

    /**
     * Checks for unconsented policy purposes and redirects the user to the policy consent page if any are found.
     * The JSP retrieves the actual purpose lists via {@link PolicyConsentUtil} at render time.
     */
    protected PostAuthnHandlerFlowStatus handlePrePolicyConsent(HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        String subjectId = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(),
                authenticatedUser.getUserStoreDomain());
        String tenantDomain = authenticatedUser.getTenantDomain();

        boolean hasUnconsentedPolicies;
        try {
            hasUnconsentedPolicies = PolicyConsentUtil.hasUnconsentedPolicies(subjectId, tenantDomain);
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
                    "Authentication failed. Error occurred while retrieving unconsented policy purposes.",
                    String.format("Error retrieving unconsented policy purposes for user: %s in tenant: %s.",
                            subjectId, tenantDomain), e);
        }

        if (!hasUnconsentedPolicies) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("No unconsented policy purposes found for user: %s. "
                        + "Policy consent handling complete.", subjectId));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        setPolicyConsentPromptedState(context);
        try {
            redirectToPolicyConsentPage(response, context);
        } catch (IOException | URISyntaxException e) {
            throw new PostAuthenticationFailedException(
                    "Authentication failed. Error while redirecting to policy consent page.",
                    "Error while redirecting to policy consent page.", e);
        }
        return PostAuthnHandlerFlowStatus.INCOMPLETE;
    }

    /**
     * Processes the user's response from the policy consent page.
     * Mandatory and optional purpose IDs are read from hidden form fields rendered by the JSP
     * ({@value POLICY_MANDATORY_IDS_PARAM} and {@value POLICY_OPTIONAL_IDS_PARAM}).
     */
    protected PostAuthnHandlerFlowStatus handlePostPolicyConsent(HttpServletRequest request,
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
            throw new PostAuthenticationFailedException(
                    "Authentication failed. User denied policy consent.",
                    "User denied consent to accept the required policies.");
        }

        String[] mandatoryParam = request.getParameterValues(POLICY_MANDATORY_IDS_PARAM);
        List<String> mandatoryIds = (mandatoryParam != null)
                ? new ArrayList<>(Arrays.asList(mandatoryParam))
                : Collections.emptyList();

        String[] optionalParam = request.getParameterValues(POLICY_OPTIONAL_IDS_PARAM);
        List<String> optionalIds = (optionalParam != null)
                ? new ArrayList<>(Arrays.asList(optionalParam))
                : Collections.emptyList();

        // Re-derive the full allowed ID sets from the backend to reject tampered form submissions
        // that either dropped mandatory IDs to skip consent or injected extra IDs to persist
        // consents for purposes the server never presented.
        Set<String> allowedMandatoryIds;
        Set<String> allowedOptionalIds;
        try {
            PolicyConsentUtil.ClassifiedPolicies classified =
                    PolicyConsentUtil.classifyUnconsentedPolicies(subjectId, tenantDomain);
            Set<String> expectedMandatoryIds = new HashSet<>(classified.getMandatoryUnconsentedIds());
            expectedMandatoryIds.addAll(classified.getMandatoryNewVersionIds());
            if (!mandatoryIds.containsAll(expectedMandatoryIds)) {
                if (diagnosticLogBuilder != null) {
                    diagnosticLogBuilder
                            .resultMessage("Submitted mandatory policy IDs do not cover all required policies.")
                            .resultStatus(DiagnosticLog.ResultStatus.FAILED);
                    LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                }
                throw new PostAuthenticationFailedException(
                        "Authentication failed. Mandatory policy consent is incomplete.",
                        String.format("User: %s did not consent to all mandatory policies in tenant: %s.",
                                subjectId, tenantDomain));
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
                    "Authentication failed. Error occurred while validating mandatory policy consent.",
                    String.format("Error validating mandatory policy consent for user: %s in tenant: %s.",
                            subjectId, tenantDomain), e);
        }

        mandatoryIds = mandatoryIds.stream()
                .filter(allowedMandatoryIds::contains)
                .collect(Collectors.toList());
        optionalIds = optionalIds.stream()
                .filter(allowedOptionalIds::contains)
                .collect(Collectors.toList());

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
                            subjectId, purposeId));
                }
            }
            for (String purposeId : optionalIds) {
                String state = approvedOptionalIds.contains(purposeId) ? ACTIVE_STATE : REJECTED_STATE;
                recordPolicyConsent(subjectId, tenantDomain, purposeId, state);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Recorded %s policy consent for user: %s, purpose: %s.",
                            state, subjectId, purposeId));
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
                    "Authentication failed. Error occurred while processing policy consent.",
                    String.format("Error processing policy consent for user: %s in tenant: %s.",
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
        boolean rejected = REJECTED_STATE.equals(state);
        ReceiptInput receiptInput = ConsentReceiptUtils.buildReceiptInput("en", subjectId, tenantDomain,
                null, rejected, null, null, RESIDENT_IDP, purposeBindings,
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

    private void setPolicyConsentPromptedState(AuthenticationContext context) {

        context.addParameter(POLICY_CONSENT_PROMPTED, true);
    }

    private boolean isSystemApplication(AuthenticationContext context) {

        String applicationName = context.getServiceProviderName();
        if (applicationName == null && context.getSequenceConfig() != null
                && context.getSequenceConfig().getApplicationConfig() != null) {
            applicationName = context.getSequenceConfig().getApplicationConfig().getApplicationName();
        }
        return FrameworkConstants.Application.CONSOLE_APP.equalsIgnoreCase(applicationName)
                || FrameworkConstants.Application.MY_ACCOUNT_APP.equalsIgnoreCase(applicationName);
    }

    private boolean isPolicyConsentPrompted(AuthenticationContext context) {

        return context.getParameter(POLICY_CONSENT_PROMPTED) != null;
    }

    private ConsentManager getConsentManager() {

        return FrameworkServiceDataHolder.getInstance().getConsentManager();
    }

    @Override
    public String getName() {

        return "PolicyConsentPostAuthenticationHandler";
    }

    private ServiceProvider getServiceProvider(AuthenticationContext context) {

        return context.getSequenceConfig().getApplicationConfig().getServiceProvider();
    }
}
