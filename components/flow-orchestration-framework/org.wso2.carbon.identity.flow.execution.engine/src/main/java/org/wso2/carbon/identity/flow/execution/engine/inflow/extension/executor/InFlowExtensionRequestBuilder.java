/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequest;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionRequestContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Application;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.Tenant;
import org.wso2.carbon.identity.action.execution.api.model.User;
import org.wso2.carbon.identity.action.execution.api.model.UserClaim;
import org.wso2.carbon.identity.action.execution.api.model.UserStore;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.config.FlowContextHandoverPolicy;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.Encryption;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.InFlowExtensionAction;
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
 * to the expose configuration and maps the result into the {@link InFlowExtensionEvent} model.
 * Modify paths from the access config are converted to a single REPLACE {@link AllowedOperation}.</p>
 */
public class InFlowExtensionRequestBuilder implements ActionExecutionRequestBuilder {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionRequestBuilder.class);

    public static final String MODIFY_PATHS_KEY = "modifyPaths";
    public static final String ACTION_NAME_KEY = "actionName";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.IN_FLOW_EXTENSION;
    }

    @Override
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = flowContext.getValue(
                InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        if (execCtx == null) {
            throw new ActionExecutionRequestBuilderException(
                    "FlowExecutionContext not found in FlowContext.");
        }

        // Resolve action-specific config. The action is already resolved by ActionExecutorServiceImpl.
        Action rawAction = actionExecutionContext.getAction();
        AccessConfig accessConfig = null;
        Encryption encryption = null;
        String actionName = null;
        if (rawAction instanceof InFlowExtensionAction) {
            InFlowExtensionAction ext = (InFlowExtensionAction) rawAction;
            accessConfig = ext.resolveAccessConfig(execCtx.getFlowType());
            encryption = ext.getEncryption();
            actionName = ext.getName();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No InFlowExtensionAction resolved. Proceeding with empty access configuration.");
            }
        }

        List<String> expose;
        if (accessConfig != null && accessConfig.getExpose() != null) {
            expose = accessConfig.getExposePaths();
        } else {
            expose = Collections.emptyList();
        }

        // Store resolved modify paths (with encryption flags) for the response processor.
        List<ContextPath> modifyPaths = (accessConfig != null && accessConfig.getModify() != null)
                ? accessConfig.getModify() : Collections.emptyList();
        flowContext.add(MODIFY_PATHS_KEY, modifyPaths);

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_REQUEST)
                    .resultMessage("Building request for In-Flow Extension action.")
                    .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                    .configParam("flowType", execCtx.getFlowType())
                    .configParam("exposePaths", expose.size())
                    .configParam("modifyPaths", modifyPaths.size())
                    .configParam("outboundEncryption", encryption != null)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
        }

        // Store action name for i18n error key prefixing in the response processor.
        if (actionName != null) {
            flowContext.add(ACTION_NAME_KEY, actionName);
        }

        // Build allowed operations: REPLACE (from modify paths, if any) + REDIRECT (always).
        List<AllowedOperation> allowedOperations = buildAllowedOperations(accessConfig, flowContext);

        // Determine certificate PEM for outbound encryption.
        String certificatePEM = null;
        if (accessConfig != null && encryption != null && encryption.getCertificate() != null) {
            certificatePEM = encryption.getCertificate().getCertificateContent();
        }

        InFlowExtensionEvent event = buildEvent(execCtx, expose, accessConfig, certificatePEM);

        return new ActionExecutionRequest.Builder()
                .actionType(ActionType.IN_FLOW_EXTENSION)
                .flowId(execCtx.getContextIdentifier())
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    /**
     * Build the allowed operations advertised to the external extension service. Always
     * includes a REDIRECT operation so the extension may signal external redirection. A
     * REPLACE operation is added only when the access config defines modify paths. Path
     * type annotations (e.g. {@code []} or {@code [schema]}) are stripped and stored in
     * the {@link FlowContext} under {@link InFlowExtensionExecutor#PATH_TYPE_ANNOTATIONS_KEY}
     * for the response processor.
     *
     * @param accessConfig The access config containing modify paths (may be null).
     * @param flowContext  The FlowContext to store path annotations.
     * @return List of allowed operations (REPLACE if applicable, plus REDIRECT).
     */
    private List<AllowedOperation> buildAllowedOperations(AccessConfig accessConfig,
                                                          FlowContext flowContext) {

        List<AllowedOperation> allowedOps = new ArrayList<>();

        if (accessConfig != null && accessConfig.getModify() != null && !accessConfig.getModify().isEmpty()) {
            List<String> cleanPaths = new ArrayList<>();
            Map<String, String> pathTypeAnnotations = new HashMap<>();

            for (ContextPath modifyPath : accessConfig.getModify()) {
                String rawPath = modifyPath.getPath();
                if (rawPath == null) {
                    continue;
                }

                String[] stripped = PathTypeAnnotationUtil.stripAnnotation(rawPath);
                String cleanPath = stripped[0];
                String annotation = stripped[1];
                if (annotation != null) {
                    if (!PathTypeAnnotationUtil.validateAnnotationLimits(annotation)) {
                        LOG.warn("Annotation for path " + cleanPath
                                + " exceeds maximum attribute limit. Skipping path.");
                        continue;
                    }
                    pathTypeAnnotations.put(cleanPath, annotation);
                }
                cleanPaths.add(cleanPath);
            }

            if (!cleanPaths.isEmpty()) {
                AllowedOperation replaceOp = new AllowedOperation();
                replaceOp.setOp(Operation.REPLACE);
                replaceOp.setPaths(cleanPaths);
                allowedOps.add(replaceOp);

                if (!pathTypeAnnotations.isEmpty()) {
                    flowContext.add(InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Built REPLACE allowed operation with " + cleanPaths.size()
                            + " modify paths. Path annotations: " + pathTypeAnnotations.size());
                }
            }
        }

        // REDIRECT is advertised when the per-flow-type handover policy allows it. A null
        // policy (test fixtures or unconfigured deployments) preserves the prior behaviour of
        // always permitting REDIRECT.
        FlowContextHandoverPolicy policy = flowContext.getValue(
                InFlowExtensionExecutor.HANDOVER_POLICY_KEY, FlowContextHandoverPolicy.class);
        if (policy == null || policy.isRedirectionEnabled()) {
            AllowedOperation redirectOp = new AllowedOperation();
            redirectOp.setOp(Operation.REDIRECT);
            allowedOps.add(redirectOp);
        }

        return allowedOps;
    }

    /**
     * Build the {@link InFlowExtensionEvent} from the {@link FlowExecutionContext},
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
    private InFlowExtensionEvent buildEvent(FlowExecutionContext context, List<String> expose,
                                            AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        InFlowExtensionEvent.Builder eventBuilder = new InFlowExtensionEvent.Builder();

        // Tenant
        if (isLeafExposed(HierarchicalPrefixMatcher.FLOW_TENANT_PATH, expose)) {
            String tenantDomain = context.getTenantDomain();
            if (tenantDomain != null) {
                int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                eventBuilder.tenant(new Tenant(String.valueOf(tenantId), tenantDomain));
            }
        }

        // Application
        if (isLeafExposed(HierarchicalPrefixMatcher.FLOW_APP_ID_PATH, expose)) {
            String appId = context.getApplicationId();
            if (appId != null) {
                eventBuilder.application(new Application(appId, null));
            }
        }

        // User
        if (isAreaExposed(HierarchicalPrefixMatcher.USER_PREFIX, expose)) {
            FlowUser flowUser = context.getFlowUser();
            if (flowUser != null) {
                eventBuilder.user(buildUser(flowUser, expose, accessConfig, certificatePEM));

                if (isLeafExposed(HierarchicalPrefixMatcher.USER_STORE_DOMAIN_PATH, expose)) {
                    String userStoreDomain = flowUser.getUserStoreDomain();
                    if (userStoreDomain != null) {
                        eventBuilder.userStore(new UserStore(userStoreDomain));
                    }
                }
            }
        }

        // Flow type
        if (isLeafExposed(HierarchicalPrefixMatcher.FLOW_TYPE_PATH, expose)) {
            eventBuilder.flowType(context.getFlowType());
        }

        // Flow ID — always set from context identifier.
        eventBuilder.flowId(context.getContextIdentifier());

        // Callback URL
        if (isLeafExposed(HierarchicalPrefixMatcher.FLOW_CALLBACK_URL_PATH, expose)) {
            String callbackUrl = context.getCallbackUrl();
            if (callbackUrl != null) {
                eventBuilder.callbackUrl(callbackUrl);
            }
        }

        // Portal URL
        if (isLeafExposed(HierarchicalPrefixMatcher.FLOW_PORTAL_URL_PATH, expose)) {
            String portalUrl = context.getPortalUrl();
            if (portalUrl != null) {
                eventBuilder.portalUrl(portalUrl);
            }
        }

        // Flow properties
        if (isAreaExposed(HierarchicalPrefixMatcher.PROPERTIES_PREFIX, expose)) {
            Map<String, Object> properties = context.getProperties();
            if (properties != null && !properties.isEmpty()) {
                eventBuilder.flowProperties(
                        filterMap(properties, HierarchicalPrefixMatcher.PROPERTIES_PREFIX,
                                expose, accessConfig, certificatePEM));
            }
        }

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

        String userId = isLeafExposed(HierarchicalPrefixMatcher.USER_ID_PATH, expose)
                ? flowUser.getUserId() : null;
        User.Builder userBuilder = new User.Builder(userId);

        if (isAreaExposed(HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX, expose)) {
            Map<String, String> claims = flowUser.getClaims();
            if (claims != null && !claims.isEmpty()) {
                List<UserClaim> userClaims = new ArrayList<>();

                for (Map.Entry<String, String> claim : claims.entrySet()) {
                    String claimPath = HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX + claim.getKey();
                    if (isLeafExposed(claimPath, expose)) {
                        String claimValue = claim.getValue();
                        // Encrypt claim value if the expose path is marked as encrypted.
                        if (shouldEncrypt(claimPath, accessConfig, certificatePEM)) {
                            claimValue = encryptValue(claimValue, certificatePEM);
                        }
                        userClaims.add(new UserClaim(claim.getKey(), claimValue));
                    }
                }
                if (!userClaims.isEmpty()) {
                    userBuilder.claims(userClaims);
                }
            }
        }

        if (isAreaExposed(HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX, expose)) {
            Map<String, char[]> credentials = flowUser.getUserCredentials();
            if (credentials != null && !credentials.isEmpty()) {
                Map<String, char[]> filteredCredentials = new HashMap<>();
                for (Map.Entry<String, char[]> entry : credentials.entrySet()) {
                    String credPath = HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX + entry.getKey();
                    if (isLeafExposed(credPath, expose)) {
                        String plaintext = new String(entry.getValue());
                        java.util.Arrays.fill(entry.getValue(), '\0');
                        if (shouldEncrypt(credPath, accessConfig, certificatePEM)) {
                            filteredCredentials.put(entry.getKey(),
                                    encryptValue(plaintext, certificatePEM).toCharArray());
                        } else {
                            filteredCredentials.put(entry.getKey(), plaintext.toCharArray());
                        }
                    }
                }
                if (!filteredCredentials.isEmpty()) {
                    userBuilder.userCredentials(filteredCredentials);
                }
            }
        }

        return userBuilder.build();
    }

    /**
     * Filter a map to only include entries whose paths are exposed.
     * Values for expose paths marked as encrypted are JWE-encrypted.
     *
     * @param map            The source map.
     * @param areaPrefix     The area prefix (e.g. "/properties/").
     * @param expose         The expose prefix list.
     * @param accessConfig   The access config with encryption flags (may be null).
     * @param certificatePEM The certificate PEM for JWE encryption (may be null).
     * @param <T>            The value type.
     * @return A new map containing only exposed entries, with encrypted values where configured.
     */
    @SuppressWarnings("unchecked")
    private <T> Map<String, T> filterMap(Map<String, T> map, String areaPrefix, List<String> expose,
                                         AccessConfig accessConfig, String certificatePEM)
            throws ActionExecutionRequestBuilderException {

        if (map == null) {
            return null;
        }

        Map<String, T> filtered = new HashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String fullPath = areaPrefix + entry.getKey();
            if (isLeafExposed(fullPath, expose)) {
                T value = entry.getValue();
                if (shouldEncrypt(fullPath, accessConfig, certificatePEM)) {
                    value = (T) encryptValue(String.valueOf(value), certificatePEM);
                }
                filtered.put(entry.getKey(), value);
            }
        }
        return filtered;
    }

    /**
     * Check if any exposed leaf path falls under the given area prefix.
     * Used as a gate before iterating a data block (claims, credentials, properties).
     */
    private boolean isAreaExposed(String areaPrefix, List<String> expose) {

        return HierarchicalPrefixMatcher.anyExposedUnder(areaPrefix, expose);
    }

    /**
     * Check if a specific leaf path is present in the expose list.
     * Used for exact leaf-level inclusion decisions.
     */
    private boolean isLeafExposed(String leafPath, List<String> expose) {

        return HierarchicalPrefixMatcher.isExposedPath(leafPath, expose);
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
