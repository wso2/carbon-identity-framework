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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for building the {@link ActionExecutionRequest} for In-Flow Extension
 * actions.
 *
 * <p><b>Responsibility</b>: expose-based filtering and request construction.
 * It receives a {@link FlowContext} containing the full {@link FlowExecutionContext}, the expose
 * list, and the allowed-operations JSON. It filters the {@link FlowExecutionContext} data according
 * to the expose configuration and maps the result into the {@link InFlowExtensionEvent} model.</p>
 */
public class InFlowExtensionRequestBuilder implements ActionExecutionRequestBuilder {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionRequestBuilder.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference<List<Map<String, Object>>> OPERATION_LIST_TYPE_REF =
            new TypeReference<List<Map<String, Object>>>() { };

    /**
     * Regex pattern to match path type annotations.
     * Matches a trailing bracket expression at the end of a path:
     * - "[]" — denotes a string array value.
     * - "[field1, field2, field3[]]" — denotes a complex object array with a schema.
     * Group 1 captures the content inside the brackets (empty for simple arrays).
     */
    private static final Pattern PATH_ANNOTATION_PATTERN = Pattern.compile("\\[([^\\]]*)]$");

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

        // Build allowed operations first so REPLACE paths can augment the expose list.
        List<AllowedOperation> allowedOperations = buildAllowedOperations(
                flowContext.getValue(InFlowExtensionExecutor.ALLOWED_OPERATIONS_KEY, String.class),
                flowContext, accessConfig);

        // Augment expose with REPLACE and REMOVE paths so the external service can see
        // the current values it may replace or remove.
        expose = augmentExposeWithOperationPaths(expose, allowedOperations);

        // Determine certificate PEM for outbound encryption (if any paths are encrypted).
        String certificatePEM = null;
        if (accessConfig != null && accessConfig.hasAnyEncryptedPath()
                && encryption != null && encryption.getCertificate() != null) {
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
     * Parse the allowed-operations JSON into typed {@link AllowedOperation} objects.
     * Handles the nested format where each entry is {@code {op, paths: [{path, encrypted}]}}.
     * Groups entries by operation type and strips path type annotations.
     * The original annotations are stored in the FlowContext under
     * {@link InFlowExtensionExecutor#PATH_TYPE_ANNOTATIONS_KEY} for the response processor.
     * <p>
     * Encryption metadata is NOT extracted from the JSON. Instead, the {@link AccessConfig}
     * (already in FlowContext) is the single source of truth for per-path encryption flags.
     * The response processor uses {@code AccessConfig.isOperationPathEncrypted()} directly.
     *
     * @param allowedOperationsJson The JSON string (maybe null).
     * @param flowContext           The FlowContext to store path annotations.
     * @param accessConfig          The access config (may be null). Reserved for future use.
     * @return List of AllowedOperation objects with clean (annotation-free) paths.
     */
    private List<AllowedOperation> buildAllowedOperations(String allowedOperationsJson,
                                                          FlowContext flowContext,
                                                          AccessConfig accessConfig) {

        if (allowedOperationsJson == null || allowedOperationsJson.isEmpty()) {
            LOG.debug("No allowed operations configured. Using empty list.");
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> operationConfigs = objectMapper.readValue(
                    allowedOperationsJson, OPERATION_LIST_TYPE_REF);

            // Map to store path type annotations: cleanPath -> annotation content.
            Map<String, String> pathTypeAnnotations = new HashMap<>();

            // Group entries by operation type, handling nested paths [{path, encrypted}].
            Map<String, List<String>> groupedByOp = new HashMap<>();
            for (Map<String, Object> config : operationConfigs) {
                String opString = (String) config.get("op");
                Object pathsObj = config.get("paths");

                if (opString == null) {
                    LOG.warn("Invalid allowed operation config: missing 'op'.");
                    continue;
                }
                if (!(pathsObj instanceof List)) {
                    LOG.warn("Invalid allowed operation config: missing or invalid 'paths'.");
                    continue;
                }

                List<?> pathsList = (List<?>) pathsObj;
                for (Object pathEntry : pathsList) {
                    String rawPath;

                    if (pathEntry instanceof Map) {
                        // Nested format: {path, encrypted} — only extract path here.
                        // Encryption metadata is resolved from AccessConfig at runtime.
                        Map<?, ?> pathMap = (Map<?, ?>) pathEntry;
                        rawPath = (String) pathMap.get("path");
                    } else if (pathEntry instanceof String) {
                        rawPath = (String) pathEntry;
                    } else {
                        LOG.warn("Invalid path entry in allowed operation.");
                        continue;
                    }

                    if (rawPath == null) {
                        continue;
                    }

                    // Strip annotations and accumulate paths into groups.
                    Matcher matcher = PATH_ANNOTATION_PATTERN.matcher(rawPath);
                    String cleanPath;
                    if (matcher.find()) {
                        cleanPath = rawPath.substring(0, matcher.start());
                        pathTypeAnnotations.put(cleanPath, matcher.group(1));
                    } else {
                        cleanPath = rawPath;
                    }

                    groupedByOp.computeIfAbsent(opString.toUpperCase(Locale.ENGLISH),
                            k -> new ArrayList<>()).add(cleanPath);
                }
            }

            // Build AllowedOperation objects from grouped entries.
            List<AllowedOperation> allowedOperations = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : groupedByOp.entrySet()) {
                try {
                    Operation operation = Operation.valueOf(entry.getKey());
                    AllowedOperation allowedOperation = new AllowedOperation();
                    allowedOperation.setOp(operation);
                    allowedOperation.setPaths(entry.getValue());
                    allowedOperations.add(allowedOperation);
                } catch (IllegalArgumentException e) {
                    LOG.warn("Unknown operation type: " + entry.getKey());
                }
            }

            // Store annotations in FlowContext for the response processor.
            if (!pathTypeAnnotations.isEmpty()) {
                flowContext.add(InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, pathTypeAnnotations);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Built " + allowedOperations.size() + " allowed operations. " +
                        "Path annotations: " + pathTypeAnnotations.size());
            }
            return allowedOperations;
        } catch (JsonProcessingException e) {
            LOG.error("Failed to parse allowed operations: " + e.getMessage(), e);
            return Collections.emptyList();
        }
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

        // TODO: Consider moving to a dynamic approach if the number of fields grows, to avoid hardcoding each field.

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

        // Current node
        if (isExposed(HierarchicalPrefixMatcher.GRAPH_CURRENT_NODE_PREFIX, expose)) {
            NodeConfig currentNode = context.getCurrentNode();
            if (currentNode != null) {
                eventBuilder.currentNodeId(currentNode.getId());
            }
        }

        // User inputs
        if (isExposed(HierarchicalPrefixMatcher.INPUT_PREFIX, expose)) {
            Map<String, String> userInputs = context.getUserInputData();
            if (userInputs != null && !userInputs.isEmpty()) {
                eventBuilder.userInputs(filterMap(userInputs, HierarchicalPrefixMatcher.INPUT_PREFIX, expose));
            }
        }

        // Flow properties
        if (isExposed(HierarchicalPrefixMatcher.PROPERTIES_PREFIX, expose)) {
            Map<String, Object> properties = context.getProperties();
            if (properties != null && !properties.isEmpty()) {
                eventBuilder.flowProperties(
                        filterMap(properties, HierarchicalPrefixMatcher.PROPERTIES_PREFIX, expose));
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
     *
     * @param map        The source map.
     * @param areaPrefix The area prefix (e.g. "/properties/").
     * @param expose     The expose prefix list.
     * @param <T>        The value type.
     * @return A new map containing only exposed entries.
     */
    private <T> Map<String, T> filterMap(Map<String, T> map, String areaPrefix, List<String> expose) {

        if (map == null) {
            return null;
        }

        boolean hasSpecificFilter = hasSpecificSubPathFilter(expose, areaPrefix);
        if (!hasSpecificFilter) {
            // The entire area is exposed — return a copy.
            return new HashMap<>(map);
        }

        Map<String, T> filtered = new HashMap<>();
        for (Map.Entry<String, T> entry : map.entrySet()) {
            if (isExposed(areaPrefix + entry.getKey(), expose)) {
                filtered.put(entry.getKey(), entry.getValue());
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

    /**
     * Augment the expose list with paths from REPLACE and REMOVE operations.
     * REPLACE operations require the current values to be visible to the external service
     * so it can decide what replacement to send. REMOVE operations require current values
     * to be visible so the external service knows what data would be removed.
     *
     * @param expose            The current expose list.
     * @param allowedOperations The parsed allowed operations.
     * @return The augmented expose list (new list if modified, original if unchanged).
     */
    private List<String> augmentExposeWithOperationPaths(List<String> expose,
                                                        List<AllowedOperation> allowedOperations) {

        if (allowedOperations == null || allowedOperations.isEmpty()) {
            return expose;
        }

        List<String> additionalPaths = new ArrayList<>();
        for (AllowedOperation op : allowedOperations) {
            if ((op.getOp() == Operation.REPLACE || op.getOp() == Operation.REMOVE)
                    && op.getPaths() != null) {
                for (String path : op.getPaths()) {
                    if (!isExposed(path, expose)) {
                        additionalPaths.add(path);
                    }
                }
            }
        }

        if (additionalPaths.isEmpty()) {
            return expose;
        }

        List<String> augmented = new ArrayList<>(expose);
        augmented.addAll(additionalPaths);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Augmented expose list with REPLACE/REMOVE paths: " + additionalPaths);
        }
        return augmented;
    }

    // ---- Encryption helpers ----

    /**
     * Determine if a value at the given path should be JWE-encrypted before sending to the external service.
     * <p>
     * Checks both the explicit expose config and the operation paths (REPLACE / REMOVE) that may
     * have been dynamically augmented into the expose list. REPLACE and REMOVE operation paths
     * carry their own encryption flags in the allowedOperations config — when such a path is
     * augmented into expose for visibility, its encryption flag must be honoured.
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
        // Check explicit expose paths first.
        if (accessConfig.isExposePathEncrypted(path)) {
            return true;
        }
        // Also check REPLACE and REMOVE operation paths that were augmented into expose.
        // These paths may have their own encryption flags in the allowedOperations config.
        return accessConfig.isOperationPathEncrypted("REPLACE", path)
                || accessConfig.isOperationPathEncrypted("REMOVE", path);
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
