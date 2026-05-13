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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.PIICategory;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategory;
import org.wso2.carbon.consent.mgt.core.model.PurposePIICategoryBinding;
import org.wso2.carbon.consent.mgt.core.model.PurposeVersion;
import org.wso2.carbon.consent.mgt.core.model.Receipt;
import org.wso2.carbon.consent.mgt.core.model.ReceiptInput;
import org.wso2.carbon.consent.mgt.core.util.ConsentReceiptUtils;
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
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ACTIVE_STATE;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.REJECTED_STATE;
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
    private static final String POLICY_MANDATORY_UNCONSENTED_IDS = "policyMandatoryUnconsentedIds";
    private static final String POLICY_OPTIONAL_UNCONSENTED_IDS = "policyOptionalUnconsentedIds";
    private static final String USER_CONSENT_INPUT = "consent";
    private static final String USER_CONSENT_APPROVE = "approve";
    private static final String LOGIN_ENDPOINT = "login.do";
    private static final String POLICY_CONSENT_ENDPOINT = "policy_consent.do";
    private static final String MANDATORY_PURPOSE_IDS_PARAM = "mandatoryPurposeIds";
    private static final String OPTIONAL_PURPOSE_IDS_PARAM = "optionalPurposeIds";
    private static final String OPTIONAL_PURPOSE_ID_PARAM = "optionalPurposeId";
    private static final String PURPOSE_METADATA_PARAM = "purposeMetadata";
    private static final String PURPOSE_ID_SEPARATOR = ",";
    private static final String SYSTEM_APP_ID = "SYSTEM";
    private static final String POLICY_URL_PROPERTY_KEY = "policyUrl";

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        if (authenticatedUser == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User not available in AuthenticationContext. Skipping policy consent handling.");
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isSystemApplication(context)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("System application detected. Skipping policy consent handling for user: "
                        + authenticatedUser.getAuthenticatedSubjectIdentifier());
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        // Check whether currently engaged SP has skipConsent enabled
        if (FrameworkUtils.isConsentPageSkippedForSP(getServiceProvider(context))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Consent page skipped for service provider. Skipping policy consent handling for user: "
                        + authenticatedUser.getAuthenticatedSubjectIdentifier());
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (FrameworkUtils.isAPIBasedAuthenticationFlow(request)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("API-based authentication flow detected. Skipping policy consent redirect for user: "
                        + authenticatedUser.getAuthenticatedSubjectIdentifier());
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (isPolicyConsentPrompted(context)) {
            return handlePostPolicyConsent(request, response, context);
        }
        return handlePrePolicyConsent(request, response, context);
    }

    /**
     * Checks for unconsented policy purposes and redirects the user to the policy consent page if any are found.
     * Mandatory purposes (those with a mandatory element on the latest version) are shown when the user has no
     * ACTIVE receipt for the latest version. Optional purposes are shown when the latest version has not been
     * rejected by the user.
     */
    protected PostAuthnHandlerFlowStatus handlePrePolicyConsent(HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        String subjectId = UserCoreUtil.addDomainToName(authenticatedUser.getUserName(),
                authenticatedUser.getUserStoreDomain());
        String tenantDomain = authenticatedUser.getTenantDomain();

        List<Purpose> policyPurposes;
        List<String> mandatoryUnconsentedIds;
        List<String> optionalUnseenIds;
        try {
            policyPurposes = getPolicyPurposes();
            mandatoryUnconsentedIds = new ArrayList<>();
            optionalUnseenIds = new ArrayList<>();
            for (Purpose purpose : policyPurposes) {
                if (isMandatoryPurpose(purpose)) {
                    // Check if there are new versions of mandatory policies missing acceptance.
                    if (missingPolicyConsentWithState(subjectId, purpose, ACTIVE_STATE)) {
                        mandatoryUnconsentedIds.add(purpose.getUuid());
                    }
                } else {
                    // Check if there are new versions of optional policies missing acceptance or has not been rejected.
                    if (missingPolicyConsentWithState(subjectId, purpose, REJECTED_STATE)) {
                        optionalUnseenIds.add(purpose.getUuid());
                    }
                }
            }
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

        if (mandatoryUnconsentedIds.isEmpty() && optionalUnseenIds.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("No unconsented policy purposes found for user: %s. "
                        + "Policy consent handling complete.", subjectId));
            }
            return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Found %d mandatory and %d optional unconsented policy purpose(s) for user: %s."
                    + " Redirecting to policy consent page.",
                    mandatoryUnconsentedIds.size(), optionalUnseenIds.size(), subjectId));
        }

        context.addParameter(POLICY_MANDATORY_UNCONSENTED_IDS, mandatoryUnconsentedIds);
        context.addParameter(POLICY_OPTIONAL_UNCONSENTED_IDS, optionalUnseenIds);
        setPolicyConsentPromptedState(context);

        Set<String> mandatoryIdSet = new HashSet<>(mandatoryUnconsentedIds);
        List<Purpose> relevantPurposes = new ArrayList<>();
        for (Purpose purpose : policyPurposes) {
            if (mandatoryUnconsentedIds.contains(purpose.getUuid())
                    || optionalUnseenIds.contains(purpose.getUuid())) {
                relevantPurposes.add(purpose);
            }
        }

        try {
            redirectToPolicyConsentPage(response, context, mandatoryUnconsentedIds, optionalUnseenIds,
                    relevantPurposes, mandatoryIdSet);
        } catch (IOException e) {
            throw new PostAuthenticationFailedException(
                    "Authentication failed. Error while redirecting to policy consent page.",
                    "Error while redirecting to policy consent page.", e);
        } catch (URISyntaxException e) {
            throw new PostAuthenticationFailedException(
                    "Authentication failed. Error while building policy consent redirect URI.",
                    "Error while building redirect URI for policy consent page.", e);
        }

        return PostAuthnHandlerFlowStatus.INCOMPLETE;
    }

    /**
     * Processes the user's response from the policy consent page.
     * All mandatory purposes must be accepted (consent=approve); declining blocks login.
     * Optional purposes that the user skipped are recorded as REJECTED receipts so the version
     * is not shown again. Accepted purposes (mandatory and optional) are recorded as ACTIVE receipts.
     */
    @SuppressWarnings("unchecked")
    protected PostAuthnHandlerFlowStatus handlePostPolicyConsent(HttpServletRequest request,
                                                                 HttpServletResponse response,
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

        List<String> mandatoryIds = (List<String>) context.getParameter(POLICY_MANDATORY_UNCONSENTED_IDS);
        List<String> optionalIds = (List<String>) context.getParameter(POLICY_OPTIONAL_UNCONSENTED_IDS);

        // Only checked optional checkboxes are submitted; absent ones are treated as rejected.
        String[] approvedOptionalParam = request.getParameterValues(OPTIONAL_PURPOSE_ID_PARAM);
        Set<String> approvedOptionalIds = (approvedOptionalParam != null)
                ? new HashSet<>(Arrays.asList(approvedOptionalParam))
                : Collections.emptySet();

        try {
            if (mandatoryIds != null) {
                for (String purposeId : mandatoryIds) {
                    recordPolicyConsent(subjectId, tenantDomain, purposeId, ACTIVE_STATE);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Recorded ACTIVE policy consent for user: %s, purpose: %s.",
                                subjectId, purposeId));
                    }
                }
            }
            if (optionalIds != null) {
                for (String purposeId : optionalIds) {
                    String state = approvedOptionalIds.contains(purposeId) ? ACTIVE_STATE : REJECTED_STATE;
                    recordPolicyConsent(subjectId, tenantDomain, purposeId, state);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("Recorded %s policy consent for user: %s, purpose: %s.",
                                state, subjectId, purposeId));
                    }
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
        }

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private List<Purpose> getPolicyPurposes() throws ConsentManagementException {

        ExpressionNode expressionNode = new ExpressionNode();
        expressionNode.setAttributeValue("type");
        expressionNode.setOperation("eq");
        expressionNode.setValue(PURPOSE_GROUP_TYPE_POLICY);
        List<Purpose> purposes = getConsentManager().listPurposes(Collections.singletonList(expressionNode), 200);
        return purposes != null ? purposes : Collections.emptyList();
    }

    private boolean isMandatoryPurpose(Purpose purpose) {

        PurposeVersion latestVersion = purpose.getLatestVersion();
        if (latestVersion == null) {
            return false;
        }
        List<PurposePIICategory> categories = latestVersion.getPurposePIICategories();
        if (categories == null) {
            return false;
        }
        return categories.stream().anyMatch(cat -> Boolean.TRUE.equals(cat.getMandatory()));
    }

    private boolean missingPolicyConsentWithState(String subjectId, Purpose purpose, String state)
            throws ConsentManagementException {

        PurposeVersion latestVersion = purpose.getLatestVersion();
        String versionUuid = latestVersion != null ? latestVersion.getUuid() : null;
        List<Receipt> receipts = getConsentManager().listReceipts(subjectId, SYSTEM_APP_ID,
                state, purpose.getUuid(), versionUuid, null, null, 1);
        return receipts == null || receipts.isEmpty();
    }

    private void recordPolicyConsent(String subjectId, String tenantDomain, String purposeUuid, String state)
            throws ConsentManagementException {

        PIICategory piiCategory = ConsentReceiptUtils.getDefaultPiiCategory(
                PURPOSE_GROUP_TYPE_POLICY, getConsentManager());
        List<PurposePIICategoryBinding> purposeBindings = new ArrayList<>();
        purposeBindings.add(new PurposePIICategoryBinding(purposeUuid, Collections.singletonList(piiCategory)));
        boolean rejected = REJECTED_STATE.equals(state);
        ReceiptInput receiptInput = ConsentReceiptUtils.buildReceiptInput("", subjectId, tenantDomain,
                null, rejected, null, null, SYSTEM_APP_ID, purposeBindings,
                getConsentManager());
        getConsentManager().addConsent(receiptInput);
    }

    private void redirectToPolicyConsentPage(HttpServletResponse response, AuthenticationContext context,
                                             List<String> mandatoryIds, List<String> optionalIds,
                                             List<Purpose> purposes, Set<String> mandatoryIdSet)
            throws IOException, URISyntaxException {

        String policyConsentUrl = ConfigurationFacade.getInstance()
                .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, POLICY_CONSENT_ENDPOINT);

        URIBuilder uriBuilder = new URIBuilder(policyConsentUrl);
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY, context.getContextIdentifier());
        if (!mandatoryIds.isEmpty()) {
            uriBuilder.addParameter(MANDATORY_PURPOSE_IDS_PARAM, joinIds(mandatoryIds));
        }
        if (!optionalIds.isEmpty()) {
            uriBuilder.addParameter(OPTIONAL_PURPOSE_IDS_PARAM, joinIds(optionalIds));
        }
        uriBuilder.addParameter(PURPOSE_METADATA_PARAM, buildPurposeMetadataJson(purposes, mandatoryIdSet));
        response.sendRedirect(uriBuilder.build().toString());
    }

    private String buildPurposeMetadataJson(List<Purpose> purposes, Set<String> mandatoryIdSet) {

        List<Map<String, Object>> metadataList = new ArrayList<>();
        for (Purpose purpose : purposes) {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("purposeId", purpose.getUuid() != null ? purpose.getUuid() : "");
            metadata.put("name", purpose.getName() != null ? purpose.getName() : purpose.getUuid());
            metadata.put("mandatory", mandatoryIdSet.contains(purpose.getUuid()));

            PurposeVersion latestVersion = purpose.getLatestVersion();
            String description = "";
            if (latestVersion != null && latestVersion.getDescription() != null) {
                description = latestVersion.getDescription();
            } else if (purpose.getDescription() != null) {
                description = purpose.getDescription();
            }
            metadata.put("description", description);

            String policyUrl = "";
            if (latestVersion != null && latestVersion.getProperties() != null) {
                String url = latestVersion.getProperties().get(POLICY_URL_PROPERTY_KEY);
                if (url != null) {
                    policyUrl = url;
                }
            }
            metadata.put("policyUrl", policyUrl);

            metadataList.add(metadata);
        }
        return new Gson().toJson(metadataList);
    }

    private String joinIds(List<String> ids) {

        StringJoiner joiner = new StringJoiner(PURPOSE_ID_SEPARATOR);
        ids.forEach(joiner::add);
        return joiner.toString();
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
