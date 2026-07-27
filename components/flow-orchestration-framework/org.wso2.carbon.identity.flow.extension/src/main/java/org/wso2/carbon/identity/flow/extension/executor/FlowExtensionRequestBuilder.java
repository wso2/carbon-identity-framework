/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.extension.executor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionAction;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionEvent;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionFlow;
import org.wso2.carbon.identity.flow.extension.model.FlowExtensionUser;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.Organization;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants.FlowContextPaths;
import org.wso2.carbon.identity.flow.extension.util.CredentialWireFormatUtil;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.flow.extension.executor.JWEEncryptionUtil.encrypt;

/**
 * This class is responsible for building the {@link ActionExecutionRequest} for Flow Extension
 * actions.
 *
 * <p><b>Responsibility</b>: expose-based filtering and request construction.
 * It receives a {@link FlowContext} containing the full {@link FlowExecutionContext}, the expose
 * list, and the access config. It filters the {@link FlowExecutionContext} data according
 * to the expose configuration and maps the result into the {@link FlowExtensionEvent} model.
 * Modify paths from the access config are converted to a single REPLACE {@link AllowedOperation}.</p>
 */
public class FlowExtensionRequestBuilder implements ActionExecutionRequestBuilder {

    private static final Log LOG = LogFactory.getLog(FlowExtensionRequestBuilder.class);
    private static final int MAX_DIAGNOSTIC_PATHS = 20;
    private static final String DIAGNOSTIC_REASON = "reason";
    private static final String REASON_OMITTED_ENCRYPTED_EXPOSE_PATHS = "omitted-encrypted-expose-paths";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.FLOW_EXTENSION;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext actionFlowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = getFlowExecutionContext(actionFlowContext);
        FlowExtensionAction flowExtensionAction = getFlowExtensionAction(actionExecutionContext);

        AccessConfig accessConfig = flowExtensionAction.getAccessConfig();
        Certificate certificate = flowExtensionAction.getCertificate();

        List<String> exposePaths = resolveExposePaths(accessConfig);
        List<ContextPath> modifyPaths = resolveModifyPaths(accessConfig);
        actionFlowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY, modifyPaths);

        List<AllowedOperation> allowedOperations = buildAllowedOperations(accessConfig, actionFlowContext);
        String certificatePEM = resolveOutboundCertificate(accessConfig, certificate);
        ExposeResolution exposeResolution = pruneEncryptedExposePathsWithoutCertificate(
                exposePaths, accessConfig, certificatePEM);

        triggerRequestBuildDiagnostic(execCtx, exposeResolution.getEffectiveExposePaths(),
                modifyPaths, certificate, exposeResolution.getOmittedEncryptedExposePaths());

        FlowExtensionEvent event = buildEvent(execCtx, exposeResolution.getEffectiveExposePaths(),
                accessConfig, certificatePEM);
        return buildRequestPayload(event, allowedOperations);
    }

    /**
     * Build the allowed operations advertised to the external extension service. Always
     * includes a REDIRECT operation so the extension may signal external redirection. A
     * REPLACE operation is added only when the access config defines modify paths. Path
     * type annotations (e.g. {@code []} or {@code [schema]}) are stripped and stored in
     * the {@link FlowContext} under {@link FlowExtensionConstants#PATH_TYPE_ANNOTATIONS_KEY}
     * for the response processor.
     *
     * @param accessConfig The access config containing modify paths (may be null).
     * @param actionFlowContext  The FlowContext to store path annotations.
     * @return List of allowed operations (REPLACE if applicable, plus REDIRECT).
     */
    private List<AllowedOperation> buildAllowedOperations(AccessConfig accessConfig,
                                                          FlowContext actionFlowContext) {

        List<AllowedOperation> allowedOps = new ArrayList<>();

        if (hasModifyPaths(accessConfig)) {
            AllowedModifyExtraction extraction = extractAllowedModifyPaths(accessConfig.getModify());
            addReplaceOperationIfAny(allowedOps, extraction.getCleanPaths());
            storeAnnotationsIfAny(actionFlowContext, extraction.getPathTypeAnnotations());

            if (LOG.isDebugEnabled() && !extraction.getSkippedPaths().isEmpty()) {
                LOG.debug("Skipped " + extraction.getSkippedPaths().size()
                        + " modify path(s) due to invalid or missing path definitions.");
            }
        }

        addRedirectOperation(allowedOps);

        return allowedOps;
    }

    /**
     * Build the {@link FlowExtensionEvent} from the {@link FlowExecutionContext},
     * filtering data according to the expose configuration.
     * Values for expose paths with {@code encrypted: true} are JWE-encrypted using the
     * external service's certificate before being included in the event.
     *
     * @param context        The FlowExecutionContext (full source of truth).
     * @param expose         The expose prefix list controlling which data is included.
     * @param accessConfig   The access config (may be null if no encryption).
     * @param certificatePEM The external service's certificate PEM for JWE encryption (may be null).
     * @return The FlowExtensionEvent.
     */
    private FlowExtensionEvent buildEvent(FlowExecutionContext context, List<String> expose,
                                            AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        FlowExtensionEvent.Builder eventBuilder = new FlowExtensionEvent.Builder();
        FlowExtensionFlow.Builder flowBuilder = new FlowExtensionFlow.Builder();

        applyTenant(eventBuilder, context, expose);
        applyOrganization(eventBuilder, expose);
        applyApplication(eventBuilder, context, expose);
        applyUser(flowBuilder, context, expose, accessConfig, certificatePEM);
        applyFlowMetadata(flowBuilder, context, expose);

        eventBuilder.flow(flowBuilder.build());
        return eventBuilder.build();
    }

    /**
     * Build the {@link User} model from {@link FlowUser}, filtering by expose config.
     * Encrypts credential and claim values for expose paths marked as encrypted.
     *
     * @param flowUser       The FlowUser from the FlowExecutionContext.
     * @param expose         The expose prefix list.
     * @param accessConfig   The access config with encryption flags (may be null).
     * @param certificatePEM The certificate PEM for JWE encryption (may be null).
     * @return The filtered User model with encrypted values where configured.
     */
    private User buildUser(FlowUser flowUser, List<String> expose,
                           AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        FlowExtensionUser.Builder userBuilder =
                new FlowExtensionUser.Builder(resolveUserId(flowUser, expose, accessConfig, certificatePEM));



        if (isLeafExposed(FlowContextPaths.USER_USERNAME_PATH, expose)) {
            String username = encryptIfConfigured(
                    FlowContextPaths.USER_USERNAME_PATH, UserCoreUtil.removeDomainFromName(flowUser.getUsername()),
                    accessConfig, certificatePEM);
            if (username != null) {
                userBuilder.username(username);
            }
        }
        if (isLeafExposed(FlowContextPaths.USER_STORE_DOMAIN_PATH, expose)) {
            String userStoreDomain = encryptIfConfigured(
                    FlowContextPaths.USER_STORE_DOMAIN_PATH, normalizeUserStoreDomain(flowUser.getUsername(),
                            flowUser.getUserStoreDomain()), accessConfig, certificatePEM);
            if (userStoreDomain != null) {
                userBuilder.userStoreDomain(userStoreDomain);
            }
        }

        List<UserClaim> userClaims = buildFilteredClaims(flowUser, expose, accessConfig, certificatePEM);
        if (!userClaims.isEmpty()) {
            userBuilder.claims(userClaims);
        }

        Map<String, Object> credentials = buildFilteredCredentials(flowUser, expose, accessConfig, certificatePEM);
        if (!credentials.isEmpty()) {
            userBuilder.credentials(credentials);
        }

        return userBuilder.build();
    }

    private FlowExecutionContext getFlowExecutionContext(FlowContext actionFlowContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = actionFlowContext.getValue(
                FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        if (execCtx == null) {
            throw new ActionExecutionRequestBuilderException("FlowExecutionContext not found in FlowContext.");
        }
        return execCtx;
    }

    private FlowExtensionAction getFlowExtensionAction(ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        Action rawAction = actionExecutionContext.getAction();
        if (!(rawAction instanceof FlowExtensionAction)) {
            throw new ActionExecutionRequestBuilderException(
                    "Expected a FlowExtensionAction but received: "
                            + (rawAction == null ? "null" : rawAction.getClass().getName()));
        }

        return (FlowExtensionAction) rawAction;
    }

    private List<String> resolveExposePaths(AccessConfig accessConfig) {

        if (accessConfig == null || accessConfig.getExpose() == null) {
            return Collections.emptyList();
        }
        return accessConfig.getExposePaths();
    }

    private List<ContextPath> resolveModifyPaths(AccessConfig accessConfig) {

        if (accessConfig == null || accessConfig.getModify() == null) {
            return Collections.emptyList();
        }
        return accessConfig.getModify();
    }

    private String resolveOutboundCertificate(AccessConfig accessConfig, Certificate certificate) {

        if (accessConfig == null || certificate == null) {
            return null;
        }
        return certificate.getCertificateContent();
    }

    private ExposeResolution pruneEncryptedExposePathsWithoutCertificate(List<String> exposePaths,
                                                                         AccessConfig accessConfig,
                                                                         String certificatePEM) {

        if (certificatePEM != null || accessConfig == null || exposePaths.isEmpty()) {
            return new ExposeResolution(exposePaths, Collections.emptyList());
        }

        List<String> effectiveExposePaths = new ArrayList<>();
        List<String> omittedEncryptedExposePaths = new ArrayList<>();
        for (String exposePath : exposePaths) {
            if (accessConfig.isExposePathEncrypted(exposePath)) {
                omittedEncryptedExposePaths.add(exposePath);
            } else {
                effectiveExposePaths.add(exposePath);
            }
        }

        if (!omittedEncryptedExposePaths.isEmpty()) {
            LOG.warn("Outbound certificate is not configured. Omitted " + omittedEncryptedExposePaths.size()
                    + " encrypted expose path(s).");
            triggerOmittedExposeDiagnostic(omittedEncryptedExposePaths);
        }

        return new ExposeResolution(effectiveExposePaths, omittedEncryptedExposePaths);
    }

    private ActionExecutionRequest buildRequestPayload(FlowExtensionEvent event,
                                                       List<AllowedOperation> allowedOperations) {

        return new ActionExecutionRequest.Builder()
                .actionType(ActionType.FLOW_EXTENSION)
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    private void triggerRequestBuildDiagnostic(FlowExecutionContext execCtx, List<String> exposePaths,
                                               List<ContextPath> modifyPaths, Certificate certificate,
                                               List<String> omittedEncryptedExposePaths) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_REQUEST)
                .resultMessage("Building request for In-Flow Extension action.")
                .configParam("actionType", ActionType.FLOW_EXTENSION.getDisplayName())
                .configParam("flowType", execCtx.getFlowType())
                .configParam("exposePaths", exposePaths.size())
                .configParam("modifyPaths", modifyPaths.size())
                .configParam("outboundEncryption", certificate != null)
                .inputParam("exposePathKeys", limitForDiagnostic(exposePaths))
                .inputParam("modifyPathKeys", limitForDiagnostic(extractModifyPathKeys(modifyPaths)))
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS);

        if (!omittedEncryptedExposePaths.isEmpty()) {
            diagnosticLogBuilder
                    .configParam(DIAGNOSTIC_REASON, REASON_OMITTED_ENCRYPTED_EXPOSE_PATHS)
                    .inputParam("omittedEncryptedExposePaths", limitForDiagnostic(omittedEncryptedExposePaths));
        }

        LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
    }

    private List<String> extractModifyPathKeys(List<ContextPath> modifyPaths) {

        List<String> modifyPathKeys = new ArrayList<>();
        for (ContextPath modifyPath : modifyPaths) {
            if (modifyPath != null && modifyPath.getPath() != null) {
                modifyPathKeys.add(modifyPath.getPath());
            }
        }
        return modifyPathKeys;
    }

    private List<String> limitForDiagnostic(List<String> values) {

        if (values.size() <= MAX_DIAGNOSTIC_PATHS) {
            return values;
        }
        return new ArrayList<>(values.subList(0, MAX_DIAGNOSTIC_PATHS));
    }

    private void triggerOmittedExposeDiagnostic(List<String> omittedEncryptedExposePaths) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_REQUEST)
                .resultMessage("Omitted encrypted expose paths because outbound certificate is not configured.")
                .configParam("actionType", ActionType.FLOW_EXTENSION.getDisplayName())
                .configParam(DIAGNOSTIC_REASON, REASON_OMITTED_ENCRYPTED_EXPOSE_PATHS)
                .inputParam("omittedEncryptedExposePaths", limitForDiagnostic(omittedEncryptedExposePaths))
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
    }

    private boolean hasModifyPaths(AccessConfig accessConfig) {

        return accessConfig != null && accessConfig.getModify() != null && !accessConfig.getModify().isEmpty();
    }

    private AllowedModifyExtraction extractAllowedModifyPaths(List<ContextPath> modifyPaths) {

        List<String> cleanPaths = new ArrayList<>();
        Map<String, String> pathTypeAnnotations = new HashMap<>();
        List<String> skippedPaths = new ArrayList<>();

        for (ContextPath modifyPath : modifyPaths) {
            String rawPath = modifyPath == null ? null : modifyPath.getPath();
            if (rawPath == null) {
                skippedPaths.add("<null-path>");
            } else {
                String[] strippedPath = PathTypeAnnotationUtil.stripAnnotation(rawPath);
                String cleanPath = strippedPath[0];
                String annotation = strippedPath[1];

                boolean isAnnotationValid = annotation == null || PathTypeAnnotationUtil
                        .validateAnnotationLimits(annotation);
                if (isAnnotationValid) {
                    if (annotation != null) {
                        pathTypeAnnotations.put(cleanPath, annotation);
                    }
                    cleanPaths.add(cleanPath);
                } else {
                    LOG.warn("Annotation for path " + cleanPath
                            + " exceeds maximum attribute limit. Skipping path.");
                    skippedPaths.add(cleanPath);
                }
            }
        }

        return new AllowedModifyExtraction(cleanPaths, pathTypeAnnotations, skippedPaths);
    }

    private void addReplaceOperationIfAny(List<AllowedOperation> allowedOperations, List<String> cleanPaths) {

        if (cleanPaths.isEmpty()) {
            return;
        }

        AllowedOperation replaceOp = new AllowedOperation();
        replaceOp.setOp(Operation.REPLACE);
        replaceOp.setPaths(cleanPaths);
        allowedOperations.add(replaceOp);
    }

    private void storeAnnotationsIfAny(FlowContext actionFlowContext, Map<String, String> pathTypeAnnotations) {

        if (!pathTypeAnnotations.isEmpty()) {
            actionFlowContext.add(FlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
        }
    }

    private void addRedirectOperation(List<AllowedOperation> allowedOperations) {

        AllowedOperation redirectOp = new AllowedOperation();
        redirectOp.setOp(Operation.REDIRECT);
        allowedOperations.add(redirectOp);
    }

    private void applyTenant(FlowExtensionEvent.Builder eventBuilder, FlowExecutionContext context,
                             List<String> expose) {

        if (!isLeafExposed(FlowContextPaths.TENANT_DOMAIN_PATH, expose)) {
            return;
        }

        String tenantDomain = context.getTenantDomain();
        if (tenantDomain != null) {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            eventBuilder.tenant(new Tenant(String.valueOf(tenantId), tenantDomain));
        } else {
            eventBuilder.tenant(new Tenant("", ""));
        }
    }

    private void applyOrganization(FlowExtensionEvent.Builder eventBuilder, List<String> expose) {

        if (!isAreaExposed(FlowContextPaths.ORGANIZATION_PREFIX, expose)) {
            return;
        }

        org.wso2.carbon.identity.core.context.model.Organization coreOrg =
                IdentityContext.getThreadLocalIdentityContext().getOrganization();
        if (coreOrg == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization is not available in the IdentityContext. "
                        + "Skipping organization details in the request.");
            }
            return;
        }

        Organization.Builder orgBuilder = new Organization.Builder();

        if (isLeafExposed(FlowContextPaths.ORGANIZATION_ID_PATH, expose)) {
            orgBuilder.id(coreOrg.getId());
        }
        if (isLeafExposed(FlowContextPaths.ORGANIZATION_NAME_PATH, expose)) {
            orgBuilder.name(coreOrg.getName());
        }
        if (isLeafExposed(FlowContextPaths.ORGANIZATION_HANDLE_PATH, expose)) {
            orgBuilder.orgHandle(coreOrg.getOrganizationHandle());
        }
        if (isLeafExposed(FlowContextPaths.ORGANIZATION_DEPTH_PATH, expose)) {
            orgBuilder.depth(coreOrg.getDepth());
        }

        eventBuilder.organization(orgBuilder.build());
    }

    private void applyApplication(FlowExtensionEvent.Builder eventBuilder, FlowExecutionContext context,
                                  List<String> expose) {

        if (!isLeafExposed(FlowContextPaths.APPLICATION_ID_PATH, expose)) {
            return;
        }

        String appId = context.getApplicationId();
        eventBuilder.application(new Application(appId != null ? appId : "", null));
    }

    private void applyUser(FlowExtensionFlow.Builder flowBuilder, FlowExecutionContext context,
                           List<String> expose, AccessConfig accessConfig,
                           String certificatePEM) throws ActionExecutionRequestBuilderException {

        if (!isAreaExposed(FlowContextPaths.USER_PREFIX, expose)) {
            return;
        }

        FlowUser flowUser = context.getFlowUser();
        if (flowUser == null) {
            return;
        }

        flowBuilder.user(buildUser(flowUser, expose, accessConfig, certificatePEM));
    }

    private void applyFlowMetadata(FlowExtensionFlow.Builder flowBuilder, FlowExecutionContext context,
                                   List<String> expose) {

        if (isLeafExposed(FlowContextPaths.FLOW_TYPE_PATH, expose)) {
            flowBuilder.flowType(context.getFlowType() != null ? context.getFlowType() : "");
        }

        // flowId is always sent (not expose-gated): the external service needs it to correlate
        // its response with the originating flow.
        flowBuilder.flowId(context.getContextIdentifier());

        if (isLeafExposed(FlowContextPaths.FLOW_PORTAL_URL_PATH, expose)) {
            flowBuilder.portalUrl(context.getPortalUrl() != null ? context.getPortalUrl() : "");
        }
    }

    private String resolveUserId(FlowUser flowUser, List<String> expose,
                                 AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        if (isLeafExposed(FlowContextPaths.USER_ID_PATH, expose)) {
            return encryptIfConfigured(
                    FlowContextPaths.USER_ID_PATH, flowUser.getUserId(), accessConfig, certificatePEM);
        }
        return null;
    }

    private List<UserClaim> buildFilteredClaims(FlowUser flowUser, List<String> expose,
                                                AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        if (!isAreaExposed(FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX, expose)) {
            return Collections.emptyList();
        }

        Map<String, String> claims = flowUser.getClaims();
        List<UserClaim> userClaims = new ArrayList<>();

        for (String exposePath : expose) {
            String claimUri = extractClaimUriFromSelectorPath(exposePath);
            if (claimUri == null) {
                continue;
            }
            String claimValue = claims != null ? claims.get(claimUri) : null;
            claimValue = encryptIfConfigured(exposePath, claimValue, accessConfig, certificatePEM);
            if (claimValue != null) {
                userClaims.add(new UserClaim(claimUri, claimValue));
            }
        }
        return userClaims;
    }

    /**
     * Build the exposed user credentials map, reading each exposed {@code /user/credentials/<key>}
     * leaf from {@link FlowUser#getUserCredentials()} and routing it through
     * {@link #buildCredentialValue} to produce its per-path wire form (typed object when plaintext,
     * JWE compact string when {@code encrypted: true}).
     */
    private Map<String, Object> buildFilteredCredentials(FlowUser flowUser, List<String> expose,
                                                         AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        if (!isAreaExposed(FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX, expose)) {
            return Collections.emptyMap();
        }

        Map<String, char[]> userCredentials = flowUser.getUserCredentials();
        if (userCredentials == null || userCredentials.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> credentials = new HashMap<>();
        for (String exposePath : expose) {
            String credentialKey = extractCredentialKeyFromPath(exposePath);
            if (credentialKey == null) {
                continue;
            }
            char[] secret = userCredentials.get(credentialKey);
            if (isBlankExposedValue(secret)) {
                continue;
            }
            credentials.put(credentialKey,
                    buildCredentialValue(exposePath, secret, accessConfig, certificatePEM));
        }
        return credentials;
    }

    /**
     * Build the wire form of a single exposed credential. The secret is always wrapped in the typed
     * credential object {@code {"type": "PLAIN_TEXT", "value": "<secret>"}}. When the path is
     * configured for encryption that object is serialized and JWE-encrypted, and the returned value
     * is the JWE compact string; otherwise the typed object is returned directly for the mapper to
     * serialize as a nested JSON object.
     *
     * @param exposePath     The credential expose path, used to look up the {@code encrypted} flag.
     * @param secret         The raw credential secret.
     * @param accessConfig   The access config with encryption flags (may be null).
     * @param certificatePEM The certificate PEM for JWE encryption (may be null).
     * @return A {@code Map} typed credential object, or a JWE compact {@code String} when encrypted.
     * @throws ActionExecutionRequestBuilderException if the path requires encryption but no
     *                                                outbound certificate is configured.
     */
    private Object buildCredentialValue(String exposePath, char[] secret, AccessConfig accessConfig,
                                        String certificatePEM) throws ActionExecutionRequestBuilderException {

        boolean encryptionRequired = accessConfig != null && accessConfig.isExposePathEncrypted(exposePath);
        if (!encryptionRequired) {
            return buildPlainTextCredential(secret);
        }

        if (certificatePEM == null) {
            LOG.error("Outbound encryption is configured for path: " + exposePath
                    + " but no outbound certificate is available.");
            throw new ActionExecutionRequestBuilderException(
                    "Missing outbound certificate for encrypted expose path: " + exposePath);
        }

        char[] credentialJson = CredentialWireFormatUtil.toPlainTextCredentialJson(secret);
        try {
            return encryptValue(credentialJson, certificatePEM);
        } finally {
            Arrays.fill(credentialJson, '\0');
        }
    }

    /**
     * Build the typed credential object {@code {"type": "PLAIN_TEXT", "value": "<secret>"}} for the
     * unencrypted path, to be serialized by the mapper as a nested JSON object.
     */
    private Map<String, String> buildPlainTextCredential(char[] secret) {

        Map<String, String> credential = new LinkedHashMap<>();
        credential.put(FlowExtensionConstants.Credentials.TYPE_KEY,
                FlowExtensionConstants.Credentials.TYPE_PLAIN_TEXT);
        credential.put(FlowExtensionConstants.Credentials.VALUE_KEY, new String(secret));
        return credential;
    }

    /**
     * Extract the credential key from a credentials leaf path.
     * Accepts {@code /user/credentials/<key>} and returns {@code <key>}.
     * Returns {@code null} if the path is not a single-segment credentials leaf.
     */
    private static String extractCredentialKeyFromPath(String path) {

        String prefix = FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX;
        if (path == null || !path.startsWith(prefix)) {
            return null;
        }
        String remaining = path.substring(prefix.length());
        if (remaining.isEmpty() || remaining.contains("/")) {
            return null;
        }
        return remaining;
    }

    /**
     * Extract the claim URI from a selector-format path.
     * Accepts {@code /user/claims[uri=<claimUri>]} and returns the claim URI.
     * Returns {@code null} if the path does not match the selector format.
     */
    private static String extractClaimUriFromSelectorPath(String path) {

        if (path != null
                && path.startsWith(FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX)
                && path.endsWith(FlowContextPaths.USER_CLAIMS_SELECTOR_SUFFIX)) {
            return path.substring(
                    FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX.length(),
                    path.length() - FlowContextPaths.USER_CLAIMS_SELECTOR_SUFFIX.length());
        }
        return null;
    }

    private static class ExposeResolution {

        private final List<String> effectiveExposePaths;
        private final List<String> omittedEncryptedExposePaths;

        private ExposeResolution(List<String> effectiveExposePaths, List<String> omittedEncryptedExposePaths) {

            this.effectiveExposePaths = effectiveExposePaths;
            this.omittedEncryptedExposePaths = omittedEncryptedExposePaths;
        }

        private List<String> getEffectiveExposePaths() {

            return effectiveExposePaths;
        }

        private List<String> getOmittedEncryptedExposePaths() {

            return omittedEncryptedExposePaths;
        }
    }

    private static class AllowedModifyExtraction {

        private final List<String> cleanPaths;
        private final Map<String, String> pathTypeAnnotations;
        private final List<String> skippedPaths;

        private AllowedModifyExtraction(List<String> cleanPaths, Map<String, String> pathTypeAnnotations,
                                        List<String> skippedPaths) {

            this.cleanPaths = cleanPaths;
            this.pathTypeAnnotations = pathTypeAnnotations;
            this.skippedPaths = skippedPaths;
        }

        private List<String> getCleanPaths() {

            return cleanPaths;
        }

        private Map<String, String> getPathTypeAnnotations() {

            return pathTypeAnnotations;
        }

        private List<String> getSkippedPaths() {

            return skippedPaths;
        }
    }

    /**
     * Check if any exposed leaf path falls under the given area prefix.
     * Used as a gate before iterating a data block (claims, credentials).
     */
    private boolean isAreaExposed(String areaPrefix, List<String> expose) {

        return AccessConfig.anyExposedUnder(areaPrefix, expose);
    }

    /**
     * Check if a specific leaf path is present in the expose list.
     * Used for exact leaf-level inclusion decisions.
     */
    private boolean isLeafExposed(String leafPath, List<String> expose) {

        return AccessConfig.isExposedPath(leafPath, expose);
    }

    /**
     * Outbound encryption gate for every exposed user value: returns {@code null} to signal the value
     * must be omitted from the payload when it is blank, the JWE compact form when the path is
     * configured for encryption, otherwise the value unchanged. Blank values are never emitted, so an
     * encrypted path that yields a value always yields a JWE compact string.
     *
     * @param path           The target context path to check for encryption rules.
     * @param value          The raw value to be evaluated and potentially encrypted.
     * @param accessConfig   The access configuration containing encryption rules.
     * @param certificatePEM The public certificate content used for encryption.
     * @return The plain text string, the encrypted payload, or null if the value is blank.
     * @throws ActionExecutionRequestBuilderException If encryption is required but the certificate is missing.
     */
    private String encryptIfConfigured(String path, Object value, AccessConfig accessConfig,
                                       String certificatePEM) throws ActionExecutionRequestBuilderException {

        if (isBlankExposedValue(value)) {
            return null;
        }

        boolean encryptionRequired = accessConfig != null && accessConfig.isExposePathEncrypted(path);
        if (!encryptionRequired) {
            return value instanceof char[] ? new String((char[]) value) : value.toString();
        }

        if (certificatePEM == null) {
            throw new ActionExecutionRequestBuilderException(
                    "Missing outbound certificate for encrypted expose path: " + path);
        }

        return encryptValue(value, certificatePEM);
    }

    /**
     * JWE-encrypt a value using the external service's certificate.
     *
     * @param value      The value to encrypt.
     * @param certificatePEM The external service's certificate PEM.
     * @return The JWE compact serialization string.
     * @throws ActionExecutionRequestBuilderException If encryption fails.
     */
    private String encryptValue(Object value, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        try {
            if (value instanceof char[]) {
                return encrypt((char[]) value, certificatePEM);
            } else if (value instanceof String) {
                return encrypt((String) value, certificatePEM);
            } else {
                throw new ActionExecutionRequestBuilderException(
                        "Unsupported value type for JWE encryption: " +
                                (value == null ? "null" : value.getClass().getName()));
            }
        } catch (ActionExecutionException e) {
            throw new ActionExecutionRequestBuilderException(
                    "Failed to JWE-encrypt outbound value for Flow Extension action.", e);
        }
    }

    /**
     * Resolve the userstore domain, deriving it from a domain-qualified username (e.g. {@code "LDAP/john"} -> {@code
     * "LDAP"}) when it is not already set explicitly.
     *
     * @param username        The raw username (may be null or domain-qualified).
     * @param userStoreDomain The raw userstore domain (may be null or blank).
     * @return The resolved userstore domain.
     */
    private String normalizeUserStoreDomain(String username, String userStoreDomain) {

        userStoreDomain = userStoreDomain != null ? userStoreDomain : "";
        if (StringUtils.isBlank(userStoreDomain) && username != null
                && username.indexOf(UserCoreConstants.DOMAIN_SEPARATOR) > 0) {
            userStoreDomain = UserCoreUtil.extractDomainFromName(username);
        }
        return userStoreDomain;
    }

    /**
     * Determines whether an exposed value should be treated as blank: null, an empty or
     * all-whitespace char array, or a blank String. Any other type is never blank.
     */
    private boolean isBlankExposedValue(Object value) {

        if (value == null) {
            return true;
        }
        if (value instanceof char[]) {
            char[] chars = (char[]) value;
            for (char c : chars) {
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
            return true;
        }
        if (value instanceof String) {
            return StringUtils.isBlank((String) value);
        }
        return false;
    }
}
