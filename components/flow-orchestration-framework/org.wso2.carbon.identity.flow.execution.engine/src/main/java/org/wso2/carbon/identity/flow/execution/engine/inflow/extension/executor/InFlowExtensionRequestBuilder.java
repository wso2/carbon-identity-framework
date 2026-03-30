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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionRequestBuilderException;
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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.Encryption;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.ContextPath;
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

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.IN_FLOW_EXTENSION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionExecutionRequest buildActionExecutionRequest(FlowContext flowContext,
                                                              ActionExecutionRequestContext actionExecutionContext)
            throws ActionExecutionRequestBuilderException {

        FlowExecutionContext execCtx = flowContext.getValue(
                InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        if (execCtx == null) {
            throw new ActionExecutionRequestBuilderException(
                    "FlowExecutionContext not found in FlowContext.");
        }

        List<String> expose = flowContext.getValue(InFlowExtensionExecutor.EXPOSE_KEY, List.class);
        if (expose == null) {
            expose = HierarchicalPrefixMatcher.DEFAULT_EXPOSE;
        }

        // Read access config for encryption metadata.
        AccessConfig accessConfig = flowContext.getValue(
                InFlowExtensionExecutor.ACCESS_CONFIG_KEY, AccessConfig.class);

        // Read encryption configuration (certificate) separately.
        Encryption encryption = flowContext.getValue(
                InFlowExtensionExecutor.ENCRYPTION_KEY, Encryption.class);

        // Build allowed operations from modify paths.
        List<AllowedOperation> allowedOperations = buildAllowedOperationsFromModify(
                accessConfig, flowContext);

        // Determine certificate PEM for outbound encryption.
        String certificatePEM = null;
        if (accessConfig != null && encryption != null && encryption.getCertificate() != null) {
            certificatePEM = encryption.getCertificate().getCertificateContent();
        }

        InFlowExtensionEvent event = buildEvent(execCtx, expose, accessConfig, certificatePEM);

        return new ActionExecutionRequest.Builder()
                .actionType(ActionType.IN_FLOW_EXTENSION)
                .flowId(execCtx.getCorrelationId())
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    /**
     * Build allowed operations from the modify paths in {@link AccessConfig}.
     * All modify paths are mapped to a single REPLACE {@link AllowedOperation}.
     * Path type annotations (e.g. {@code []} or {@code [schema]}) are stripped and stored
     * in the {@link FlowContext} under {@link InFlowExtensionExecutor#PATH_TYPE_ANNOTATIONS_KEY}
     * for the response processor.
     *
     * @param accessConfig The access config containing modify paths (may be null).
     * @param flowContext  The FlowContext to store path annotations.
     * @return A singleton list containing one REPLACE operation, or empty list if no modify paths.
     */
    private List<AllowedOperation> buildAllowedOperationsFromModify(AccessConfig accessConfig,
                                                                    FlowContext flowContext) {

        if (accessConfig == null || accessConfig.getModify() == null || accessConfig.getModify().isEmpty()) {
            return Collections.emptyList();
        }

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

        if (cleanPaths.isEmpty()) {
            return Collections.emptyList();
        }

        AllowedOperation replaceOp = new AllowedOperation();
        replaceOp.setOp(Operation.REPLACE);
        replaceOp.setPaths(cleanPaths);

        if (!pathTypeAnnotations.isEmpty()) {
            flowContext.add(InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Built REPLACE allowed operation with " + cleanPaths.size()
                    + " modify paths. Path annotations: " + pathTypeAnnotations.size());
        }
        return Collections.singletonList(replaceOp);
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
                                            AccessConfig accessConfig, String certificatePEM) {

        InFlowExtensionEvent.Builder eventBuilder = new InFlowExtensionEvent.Builder();

        // Tenant
        if (isExposed(HierarchicalPrefixMatcher.FLOW_TENANT_PATH, expose)) {
            String tenantDomain = context.getTenantDomain();
            if (tenantDomain != null) {
                int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                eventBuilder.tenant(new Tenant(String.valueOf(tenantId), tenantDomain));
            }
        }

        // Application
        if (isExposed(HierarchicalPrefixMatcher.FLOW_APP_ID_PATH, expose)) {
            String appId = context.getApplicationId();
            if (appId != null) {
                eventBuilder.application(new Application(appId, null));
            }
        }

        // User
        if (isExposed(HierarchicalPrefixMatcher.USER_PREFIX, expose)) {
            FlowUser flowUser = context.getFlowUser();
            if (flowUser != null) {
                eventBuilder.user(buildUser(flowUser, expose, accessConfig, certificatePEM));

                if (isExposed(HierarchicalPrefixMatcher.USER_STORE_DOMAIN_PATH, expose)) {
                    String userStoreDomain = flowUser.getUserStoreDomain();
                    if (userStoreDomain != null) {
                        eventBuilder.userStore(new UserStore(userStoreDomain));
                    }
                }
            }
        }

        // Flow type
        if (isExposed(HierarchicalPrefixMatcher.FLOW_TYPE_PATH, expose)) {
            eventBuilder.flowType(context.getFlowType());
        }

        // User inputs
        if (isExposed(HierarchicalPrefixMatcher.INPUT_PREFIX, expose)) {
            Map<String, String> userInputs = context.getUserInputData();
            if (userInputs != null && !userInputs.isEmpty()) {
                eventBuilder.userInputs(filterMap(userInputs, HierarchicalPrefixMatcher.INPUT_PREFIX,
                        expose, accessConfig, certificatePEM));
            }
        }

        // Flow properties
        if (isExposed(HierarchicalPrefixMatcher.PROPERTIES_PREFIX, expose)) {
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
                           AccessConfig accessConfig, String certificatePEM) {

        String userId = isExposed(HierarchicalPrefixMatcher.USER_ID_PATH, expose)
                ? flowUser.getUserId() : null;
        User.Builder userBuilder = new User.Builder(userId);

        if (isExposed(HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX, expose)) {
            Map<String, String> claims = flowUser.getClaims();
            if (claims != null && !claims.isEmpty()) {
                List<UserClaim> userClaims = new ArrayList<>();
                boolean hasSpecificFilter = hasSpecificSubPathFilter(
                        expose, HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX);

                for (Map.Entry<String, String> claim : claims.entrySet()) {
                    if (!hasSpecificFilter || isExposed(
                            HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX + claim.getKey(), expose)) {
                        String claimValue = claim.getValue();
                        // Encrypt claim value if the expose path is marked as encrypted.
                        String claimPath = HierarchicalPrefixMatcher.USER_CLAIMS_PREFIX + claim.getKey();
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

        if (isExposed(HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX, expose)) {
            Map<String, char[]> credentials = flowUser.getUserCredentials();
            if (credentials != null && !credentials.isEmpty()) {
                // Check if credentials expose path is encrypted.
                if (shouldEncrypt(HierarchicalPrefixMatcher.USER_CREDENTIALS_PREFIX, accessConfig,
                        certificatePEM)) {
                    // Encrypt each credential value and store as char[] of the JWE compact string.
                    Map<String, char[]> encryptedCredentials = new HashMap<>();
                    for (Map.Entry<String, char[]> entry : credentials.entrySet()) {
                        String plaintext = new String(entry.getValue());
                        String encrypted = encryptValue(plaintext, certificatePEM);
                        encryptedCredentials.put(entry.getKey(), encrypted.toCharArray());
                    }
                    userBuilder.userCredentials(encryptedCredentials);
                } else {
                    userBuilder.userCredentials(new HashMap<>(credentials));
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
                                         AccessConfig accessConfig, String certificatePEM) {

        if (map == null) {
            return null;
        }

        boolean hasSpecificFilter = hasSpecificSubPathFilter(expose, areaPrefix);

        Map<String, T> filtered = new HashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            String fullPath = areaPrefix + entry.getKey();
            if (!hasSpecificFilter || isExposed(fullPath, expose)) {
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
     * Check if a path is exposed using bidirectional prefix matching.
     */
    private boolean isExposed(String path, List<String> expose) {

        return HierarchicalPrefixMatcher.matchesAnyExpose(path, expose);
    }

    /**
     * Check whether the expose list contains specific sub-paths under a given area prefix,
     * indicating that per-key filtering is required rather than exposing the entire area.
     */
    private boolean hasSpecificSubPathFilter(List<String> expose, String areaPrefix) {

        for (String prefix : expose) {
            if (prefix.startsWith(areaPrefix) && !prefix.equals(areaPrefix)) {
                return true;
            }
        }
        return false;
    }

    // ---- Encryption helpers ----

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
     * If encryption fails, the original value is returned and a warning is logged.
     *
     * @param plaintext      The value to encrypt.
     * @param certificatePEM The external service's certificate PEM.
     * @return The JWE compact serialization string, or the original value on failure.
     */
    private String encryptValue(String plaintext, String certificatePEM) {

        try {
            return JWEEncryptionUtil.encrypt(plaintext, certificatePEM);
        } catch (Exception e) {
            LOG.warn("Failed to JWE-encrypt outbound value. Sending plaintext.", e);
            return plaintext;
        }
    }
}
