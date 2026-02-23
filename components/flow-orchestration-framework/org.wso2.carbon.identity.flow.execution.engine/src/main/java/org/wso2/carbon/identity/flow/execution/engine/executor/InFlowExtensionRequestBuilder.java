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

package org.wso2.carbon.identity.flow.execution.engine.executor;

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

        // Build allowed operations first so REPLACE paths can augment the expose list.
        List<AllowedOperation> allowedOperations = buildAllowedOperations(
                flowContext.getValue(InFlowExtensionExecutor.ALLOWED_OPERATIONS_KEY, String.class),
                flowContext);

        // Augment expose with REPLACE paths so the external service can see the current values
        // it may replace.
        expose = augmentExposeWithReplacePaths(expose, allowedOperations);

        InFlowExtensionEvent event = buildEvent(execCtx, expose);

        return new ActionExecutionRequest.Builder()
                .actionType(ActionType.IN_FLOW_EXTENSION)
                .flowId(execCtx.getCorrelationId())
                .event(event)
                .allowedOperations(allowedOperations)
                .build();
    }

    /**
     * Parse the allowed-operations JSON into typed {@link AllowedOperation} objects.
     * Strips path type annotations (e.g., "[]", "[schema]") from paths before creating
     * AllowedOperation objects so that {@code OperationComparator} receives clean paths.
     * The original annotations are stored in the FlowContext under
     * {@link InFlowExtensionExecutor#PATH_TYPE_ANNOTATIONS_KEY} for the response processor.
     *
     * @param allowedOperationsJson The JSON string (maybe null).
     * @param flowContext           The FlowContext to store path annotations in.
     * @return List of AllowedOperation objects with clean (annotation-free) paths.
     */
    private List<AllowedOperation> buildAllowedOperations(String allowedOperationsJson,
                                                          FlowContext flowContext) {

        if (allowedOperationsJson == null || allowedOperationsJson.isEmpty()) {
            LOG.debug("No allowed operations configured. Using empty list.");
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> operationConfigs = objectMapper.readValue(
                    allowedOperationsJson, OPERATION_LIST_TYPE_REF);

            // Map to store path type annotations: cleanPath -> annotation content.
            // "" means simple string array (from []), non-empty means schema (from [schema]).
            Map<String, String> pathTypeAnnotations = new HashMap<>();

            List<AllowedOperation> allowedOperations = new ArrayList<>();
            for (Map<String, Object> config : operationConfigs) {
                AllowedOperation operation = createAllowedOperationFromConfig(config, pathTypeAnnotations);
                if (operation != null) {
                    allowedOperations.add(operation);
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
     * Create an AllowedOperation from a configuration map.
     * Strips path type annotations and records them in the annotations map.
     *
     * @param config              The raw configuration map from JSON.
     * @param pathTypeAnnotations Map to record extracted annotations (cleanPath → annotation).
     * @return The AllowedOperation with clean paths, or null if invalid.
     */
    @SuppressWarnings("unchecked")
    private AllowedOperation createAllowedOperationFromConfig(Map<String, Object> config,
                                                              Map<String, String> pathTypeAnnotations) {

        String opString = (String) config.get("op");
        Object pathsObj = config.get("paths");

        if (opString == null || pathsObj == null) {
            LOG.warn("Invalid allowed operation config: missing 'op' or 'paths'.");
            return null;
        }

        Operation operation;
        try {
            operation = Operation.valueOf(opString.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            LOG.warn("Unknown operation type: " + opString);
            return null;
        }

        List<String> paths;
        if (pathsObj instanceof List) {
            paths = (List<String>) pathsObj;
        } else {
            LOG.warn("Invalid 'paths' type in allowed operation config.");
            return null;
        }

        // Strip type annotations from paths and record them.
        List<String> cleanPaths = new ArrayList<>();
        for (String rawPath : paths) {
            Matcher matcher = PATH_ANNOTATION_PATTERN.matcher(rawPath);
            if (matcher.find()) {
                String cleanPath = rawPath.substring(0, matcher.start());
                String annotationContent = matcher.group(1); // "" for [], or schema content for [schema]
                cleanPaths.add(cleanPath);
                pathTypeAnnotations.put(cleanPath, annotationContent);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Path annotation extracted: " + rawPath + " -> clean: " + cleanPath +
                            ", annotation: [" + annotationContent + "]");
                }
            } else {
                cleanPaths.add(rawPath);
            }
        }

        AllowedOperation allowedOperation = new AllowedOperation();
        allowedOperation.setOp(operation);
        allowedOperation.setPaths(cleanPaths);
        return allowedOperation;
    }

    /**
     * Build the {@link InFlowExtensionEvent} from the {@link FlowExecutionContext},
     * filtering data according to the expose configuration.
     *
     * @param context The FlowExecutionContext (full source of truth).
     * @param expose  The expose prefix list controlling which data is included.
     * @return The InFlowExtensionEvent.
     */
    private InFlowExtensionEvent buildEvent(FlowExecutionContext context, List<String> expose) {

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
                eventBuilder.user(buildUser(flowUser, expose));

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
     *
     * @param flowUser The FlowUser from the FlowExecutionContext.
     * @param expose   The expose prefix list.
     * @return The filtered User model.
     */
    private User buildUser(FlowUser flowUser, List<String> expose) {

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
                        userClaims.add(new UserClaim(claim.getKey(), claim.getValue()));
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

                userBuilder.userCredentials(new HashMap<>(credentials));
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
     * Augment the expose list with paths from REPLACE operations.
     * REPLACE operations require the current values to be visible to the external service,
     * so their paths must be exposed even if not explicitly in the expose configuration.
     *
     * @param expose            The current expose list.
     * @param allowedOperations The parsed allowed operations.
     * @return The augmented expose list (new list if modified, original if unchanged).
     */
    private List<String> augmentExposeWithReplacePaths(List<String> expose,
                                                       List<AllowedOperation> allowedOperations) {

        if (allowedOperations == null || allowedOperations.isEmpty()) {
            return expose;
        }

        List<String> replacePaths = new ArrayList<>();
        for (AllowedOperation op : allowedOperations) {
            if (op.getOp() == Operation.REPLACE && op.getPaths() != null) {
                for (String path : op.getPaths()) {
                    if (!isExposed(path, expose)) {
                        replacePaths.add(path);
                    }
                }
            }
        }

        if (replacePaths.isEmpty()) {
            return expose;
        }

        List<String> augmented = new ArrayList<>(expose);
        augmented.addAll(replacePaths);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Augmented expose list with REPLACE paths: " + replacePaths);
        }
        return augmented;
    }
}
