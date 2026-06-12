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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
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
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for building the {@link ActionExecutionRequest} for In-Flow Extension
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
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = getFlowExecutionContext(flowContext);
        FlowExtensionAction flowExtensionAction = getFlowExtensionAction(actionExecutionContext);

        AccessConfig accessConfig = flowExtensionAction.getAccessConfig();
        Certificate certificate = flowExtensionAction.getCertificate();

        List<String> exposePaths = resolveExposePaths(accessConfig);
        List<ContextPath> modifyPaths = resolveModifyPaths(accessConfig);
        flowContext.add(FlowExtensionConstants.MODIFY_PATHS_KEY, modifyPaths);

        List<AllowedOperation> allowedOperations = buildAllowedOperations(accessConfig, flowContext);
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
     * @param flowContext  The FlowContext to store path annotations.
     * @return List of allowed operations (REPLACE if applicable, plus REDIRECT).
     */
    private List<AllowedOperation> buildAllowedOperations(AccessConfig accessConfig,
                                                          FlowContext flowContext) {

        List<AllowedOperation> allowedOps = new ArrayList<>();

        if (hasModifyPaths(accessConfig)) {
            AllowedModifyExtraction extraction = extractAllowedModifyPaths(accessConfig.getModify());
            addReplaceOperationIfAny(allowedOps, extraction.getCleanPaths());
            storeAnnotationsIfAny(flowContext, extraction.getPathTypeAnnotations());

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
     * @return The InFlowExtensionEvent.
     */
    private FlowExtensionEvent buildEvent(FlowExecutionContext context, List<String> expose,
                                            AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        FlowExtensionEvent.Builder eventBuilder = new FlowExtensionEvent.Builder();
        FlowExtensionFlow.Builder flowBuilder = new FlowExtensionFlow.Builder();

        applyTenant(eventBuilder, context, expose);
        applyOrganization(eventBuilder, expose);
        applyApplication(eventBuilder, context, expose);
        applyUserAndUserStore(flowBuilder, context, expose, accessConfig, certificatePEM);
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
                new FlowExtensionUser.Builder(resolveUserId(flowUser, expose));

        if (isLeafExposed(FlowContextPaths.USER_USERNAME_PATH, expose)) {
            String username = flowUser.getUsername();
            userBuilder.username(username != null ? username : "");
        }
        if (isLeafExposed(FlowContextPaths.USER_STORE_DOMAIN_PATH, expose)) {
            String userStoreDomain = flowUser.getUserStoreDomain();
            userBuilder.userStoreDomain(userStoreDomain != null ? userStoreDomain : "");
        }

        List<UserClaim> userClaims = buildFilteredClaims(flowUser, expose, accessConfig, certificatePEM);
        if (!userClaims.isEmpty()) {
            userBuilder.claims(userClaims);
        }

        return userBuilder.build();
    }

    private FlowExecutionContext getFlowExecutionContext(FlowContext flowContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = flowContext.getValue(
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

    private void storeAnnotationsIfAny(FlowContext flowContext, Map<String, String> pathTypeAnnotations) {

        if (!pathTypeAnnotations.isEmpty()) {
            flowContext.add(FlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
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

    private void applyUserAndUserStore(FlowExtensionFlow.Builder flowBuilder, FlowExecutionContext context,
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

    private String resolveUserId(FlowUser flowUser, List<String> expose) {

        if (isLeafExposed(FlowContextPaths.USER_ID_PATH, expose)) {
            String userId = flowUser.getUserId();
            return userId != null ? userId : "";
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
            claimValue = claimValue != null ? claimValue : "";
            if (!claimValue.isEmpty() && shouldEncrypt(exposePath, accessConfig, certificatePEM)) {
                claimValue = encryptValue(claimValue, certificatePEM);
            }
            userClaims.add(new UserClaim(claimUri, claimValue));
        }
        return userClaims;
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
     * Determine if a value at the given path should be JWE-encrypted before sending to the
     * external service. Only expose paths with {@code encrypted: true} trigger outbound encryption.
     *
     * @param path           The expose path.
     * @param accessConfig   The access config with encryption flags.
     * @param certificatePEM The certificate PEM (null if no encryption configured).
     * @return {@code true} if the value should be encrypted.
     */
    private boolean shouldEncrypt(String path, AccessConfig accessConfig, String certificatePEM) {

        if (certificatePEM == null || accessConfig == null) {
            return false;
        }
        return accessConfig.isExposePathEncrypted(path);
    }

    /**
     * JWE-encrypt a plaintext value using the external service's certificate.
     *
     * @param plaintext      The value to encrypt.
     * @param certificatePEM The external service's certificate PEM.
     * @return The JWE compact serialization string.
     * @throws ActionExecutionRequestBuilderException If encryption fails.
     */
    private String encryptValue(String plaintext, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        try {
            return JWEEncryptionUtil.encrypt(plaintext, certificatePEM);
        } catch (Exception e) {
            throw new ActionExecutionRequestBuilderException(
                    "Failed to JWE-encrypt outbound value for In-Flow Extension action.", e);
        }
    }
}
