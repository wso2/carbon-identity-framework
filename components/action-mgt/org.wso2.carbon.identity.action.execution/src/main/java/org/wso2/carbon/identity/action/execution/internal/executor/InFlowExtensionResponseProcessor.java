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

package org.wso2.carbon.identity.action.execution.internal.executor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.internal.component.ActionExecutionServiceComponentHolder;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for processing the response from In-Flow Extension actions.
 *
 * <p><b>Responsibility</b>: operation processing and applying context updates directly to
 * the {@link FlowExecutionContext}. It processes operations (ADD, REMOVE, REPLACE) on flow
 * properties, user claims, and user inputs.</p>
 *
 * <p>The {@code allowedOperations} list (sent to the external service in the request and enforced
 * upstream by {@code ActionExecutorServiceImpl}) is the sole mechanism for gating which operations
 * are permitted. This processor performs two additional validations:</p>
 * <ul>
 *   <li><b>Read-only areas</b>: No modifications allowed to {@code /flow/} or {@code /graph/}.</li>
 *   <li><b>Claim URI validation</b>: For ADD operations on {@code /user/claims/}, validates that
 *       the claim URI exists in the system via {@code ClaimMetadataManagementService}.</li>
 * </ul>
 */

// TODO: Consider separating claim validation and read-only path checks into utility classes.

public class InFlowExtensionResponseProcessor implements ActionExecutionResponseProcessor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionResponseProcessor.class);

    // Path prefixes for In-Flow Extension context (unified hierarchy)
    private static final String PROPERTIES_PATH_PREFIX = "/properties/";
    private static final String USER_CLAIMS_PATH_PREFIX = "/user/claims/";
    private static final String USER_INPUTS_PATH_PREFIX = "/input/";
    // Legacy prefix for backward compatibility
    private static final String LEGACY_USER_INPUTS_PATH_PREFIX = "/userInputs/";

    private static final char PATH_SEPARATOR = '/';
    private static final String LAST_ELEMENT_CHARACTER = "-";

    // Cache for valid claim URIs (per tenant)
    private Map<String, Set<String>> validClaimUrisCache = new HashMap<>();

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.IN_FLOW_EXTENSION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = flowContext.getValue(
                InFlowExtensionExecutor.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        String tenantDomain = execCtx != null ? execCtx.getTenantDomain() : null;

        // Read path type annotations set by the request builder.
        // Maps clean paths (e.g., "/properties/riskFactors") to annotation content
        // ("" for string arrays, or schema content for complex object arrays).
        Map<String, String> pathTypeAnnotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        if (pathTypeAnnotations == null) {
            pathTypeAnnotations = Collections.emptyMap();
        }

        List<OperationExecutionResult> results = new ArrayList<>();

        // Get operations from the response (already filtered by ActionExecutorServiceImpl).
        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        if (operations != null && !operations.isEmpty()) {
            for (PerformableOperation operation : operations) {
                // Normalize legacy paths.
                // TODO: Remove this normalization logic in future once external services are updated
                // to use unified paths.
                String normalizedPath = HierarchicalPrefixMatcher.normalizePath(operation.getPath());
                if (!normalizedPath.equals(operation.getPath())) {
                    PerformableOperation normalizedOp = new PerformableOperation();
                    normalizedOp.setOp(operation.getOp());
                    normalizedOp.setPath(normalizedPath);
                    normalizedOp.setValue(operation.getValue());
                    operation = normalizedOp;
                }

                results.add(processOperation(operation, execCtx, tenantDomain, pathTypeAnnotations));
            }
        }

        logOperationExecutionResults(results);

        return new SuccessStatus.Builder()
                .setSuccess(new InFlowExtensionSuccess())
                .setResponseContext(Collections.emptyMap())
                .build();
    }


    /**
     * Process a single operation by validating and applying it directly to the
     * {@link FlowExecutionContext}.
     *
     * @param operation           The operation to process.
     * @param context             The FlowExecutionContext to apply updates to.
     * @param tenantDomain        The tenant domain for claim validation.
     * @param pathTypeAnnotations Map of clean paths to their type annotations from allowed operations.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult processOperation(PerformableOperation operation,
            FlowExecutionContext context, String tenantDomain,
            Map<String, String> pathTypeAnnotations) {

        String path = operation.getPath();

        // Check if operation is on a read-only area.
        if (HierarchicalPrefixMatcher.isReadOnly(path)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Path is in a read-only area. Modifications not allowed: " + path);
        }

        // Route to appropriate handler based on path prefix.
        if (path.startsWith(PROPERTIES_PATH_PREFIX)) {
            return handlePropertyOperation(operation, context, pathTypeAnnotations);
        } else if (path.startsWith(USER_CLAIMS_PATH_PREFIX)) {
            return handleUserClaimOperation(operation, context, tenantDomain);
        } else if (path.startsWith(USER_INPUTS_PATH_PREFIX) || path.startsWith(LEGACY_USER_INPUTS_PATH_PREFIX)) {
            return handleUserInputOperation(operation, context);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path prefix. Supported: " + PROPERTIES_PATH_PREFIX +
                        ", " + USER_CLAIMS_PATH_PREFIX + ", " + USER_INPUTS_PATH_PREFIX);
    }

    // ========================= Property operations =========================

    /**
     * Handle operations on flow properties — apply directly to {@link FlowExecutionContext}.
     *
     * <p>Supports terminal paths with nested segments. For example:</p>
     * <ul>
     *   <li>{@code /properties/riskScore} → flat property "riskScore"</li>
     *   <li>{@code /properties/risk_margin/lower_margin} → nested: sets "lower_margin" inside
     *       a "risk_margin" Map, auto-creating the parent Map if needed.</li>
     * </ul>
     *
     * <p>Value type rules (based on path type annotations from allowed operations):</p>
     * <ul>
     *   <li>No annotation: value must be a string (or convertible via String.valueOf).</li>
     *   <li>{@code []} annotation: value must be a List of strings.</li>
     *   <li>{@code [schema]} annotation: value must be a List of objects matching the schema.</li>
     * </ul>
     *
     * <p>REPLACE validation: the target path must already exist in the context.</p>
     *
     * @param operation           The performable operation.
     * @param context             The FlowExecutionContext.
     * @param pathTypeAnnotations Path type annotations map from request builder.
     */
    @SuppressWarnings("unchecked")
    private OperationExecutionResult handlePropertyOperation(PerformableOperation operation,
            FlowExecutionContext context, Map<String, String> pathTypeAnnotations) {

        String remaining = extractNameFromPath(operation.getPath(), PROPERTIES_PATH_PREFIX);

        if (remaining == null || remaining.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid property path. Property name is required.");
        }

        // Split into path segments for nested property support.
        // e.g., "risk_margin/lower_margin" -> ["risk_margin", "lower_margin"]
        String[] segments = remaining.split("/");

        switch (operation.getOp()) {
            case ADD:
                return handlePropertyAdd(operation, context, segments, pathTypeAnnotations);

            case REPLACE:
                return handlePropertyReplace(operation, context, segments, pathTypeAnnotations);

            case REMOVE:
                return handlePropertyRemove(operation, context, segments);

            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }

    /**
     * Handle ADD operation on properties. Auto-creates parent Maps for nested paths.
     */
    @SuppressWarnings("unchecked")
    private OperationExecutionResult handlePropertyAdd(PerformableOperation operation,
            FlowExecutionContext context, String[] segments,
            Map<String, String> pathTypeAnnotations) {

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for ADD operation.");
        }

        // Coerce value based on path type annotation.
        Object coercedValue = coerceValue(operation.getPath(), operation.getValue(), pathTypeAnnotations);

        if (segments.length == 1) {
            // Flat property: /properties/riskScore -> setProperty("riskScore", value)
            context.setProperty(segments[0], coercedValue);
        } else {
            // Nested property: /properties/risk_margin/lower_margin
            // Auto-create parent Map(s) and set the leaf value.
            setNestedProperty(context, segments, coercedValue);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property add applied.");
    }

    /**
     * Handle REPLACE operation on properties. Validates that the target path exists.
     */
    @SuppressWarnings("unchecked")
    private OperationExecutionResult handlePropertyReplace(PerformableOperation operation,
            FlowExecutionContext context, String[] segments,
            Map<String, String> pathTypeAnnotations) {

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
        }

        // Validate that the target path exists before replacing.
        if (segments.length == 1) {
            if (!context.getProperties().containsKey(segments[0])) {
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Cannot REPLACE: property '" + segments[0] + "' does not exist in context.");
            }
        } else {
            // For nested paths, validate the full path exists.
            Object existing = resolveNestedProperty(context, segments);
            if (existing == null) {
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Cannot REPLACE: nested property path '" + String.join("/", segments) +
                                "' does not exist in context.");
            }
        }

        // Coerce value based on path type annotation.
        Object coercedValue = coerceValue(operation.getPath(), operation.getValue(), pathTypeAnnotations);

        if (segments.length == 1) {
            context.setProperty(segments[0], coercedValue);
        } else {
            setNestedProperty(context, segments, coercedValue);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property replace applied.");
    }

    /**
     * Handle REMOVE operation on properties.
     */
    @SuppressWarnings("unchecked")
    private OperationExecutionResult handlePropertyRemove(PerformableOperation operation,
            FlowExecutionContext context, String[] segments) {

        if (segments.length == 1) {
            context.getProperties().remove(segments[0]);
        } else {
            // For nested paths, remove the leaf key from the parent Map.
            removeNestedProperty(context, segments);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property removed.");
    }

    /**
     * Set a value at a nested path in the properties map, auto-creating parent Maps.
     * e.g., segments=["risk_margin", "lower_margin"], value="20"
     * Creates: properties.risk_margin = { lower_margin: "20" }
     */
    @SuppressWarnings("unchecked")
    private void setNestedProperty(FlowExecutionContext context, String[] segments, Object value) {

        Map<String, Object> current = context.getProperties();

        // Navigate/create parent maps for all segments except the last.
        for (int i = 0; i < segments.length - 1; i++) {
            Object child = current.get(segments[i]);
            if (child instanceof Map) {
                current = (Map<String, Object>) child;
            } else {
                // Auto-create parent Map.
                Map<String, Object> newMap = new HashMap<>();
                current.put(segments[i], newMap);
                current = newMap;
            }
        }

        // Set the leaf value.
        current.put(segments[segments.length - 1], value);
    }

    /**
     * Resolve a nested property path, returning the leaf value or null if any segment is missing.
     */
    @SuppressWarnings("unchecked")
    private Object resolveNestedProperty(FlowExecutionContext context, String[] segments) {

        Map<String, Object> current = context.getProperties();

        for (int i = 0; i < segments.length - 1; i++) {
            Object child = current.get(segments[i]);
            if (child instanceof Map) {
                current = (Map<String, Object>) child;
            } else {
                return null;
            }
        }

        return current.get(segments[segments.length - 1]);
    }

    /**
     * Remove a leaf key from a nested property path.
     */
    @SuppressWarnings("unchecked")
    private void removeNestedProperty(FlowExecutionContext context, String[] segments) {

        Map<String, Object> current = context.getProperties();

        for (int i = 0; i < segments.length - 1; i++) {
            Object child = current.get(segments[i]);
            if (child instanceof Map) {
                current = (Map<String, Object>) child;
            } else {
                return; // Parent doesn't exist, nothing to remove.
            }
        }

        current.remove(segments[segments.length - 1]);
    }

    /**
     * Coerce a value based on path type annotations.
     *
     * <ul>
     *   <li>No annotation: value is coerced to String via String.valueOf().</li>
     *   <li>"" annotation (from []): value is expected to be a List; each element is coerced to String.</li>
     *   <li>Non-empty annotation (from [schema]): value is expected to be a List of objects;
     *       passed through as-is (schema validation can be added later).</li>
     * </ul>
     *
     * @param path                The operation path.
     * @param value               The raw value from the operation.
     * @param pathTypeAnnotations Path type annotations map.
     * @return The coerced value.
     */
    @SuppressWarnings("unchecked")
    private Object coerceValue(String path, Object value,
                               Map<String, String> pathTypeAnnotations) {

        String annotation = pathTypeAnnotations.get(path);

        if (annotation == null) {
            // No annotation: coerce to String.
            return String.valueOf(value);
        }

        if (annotation.isEmpty()) {
            // [] annotation: expect a List of strings.
            if (value instanceof List) {
                List<Object> rawList = (List<Object>) value;
                List<String> stringList = new ArrayList<>();
                for (Object item : rawList) {
                    stringList.add(String.valueOf(item));
                }
                return stringList;
            }
            // Single value — wrap in a list.
            List<String> singleList = new ArrayList<>();
            singleList.add(String.valueOf(value));
            return singleList;
        }

        // [schema] annotation: pass through as-is for now.
        // TODO: Add schema validation for complex object arrays.
        return value;
    }

    // ========================= User claim operations =========================

    /**
     * Handle operations on user claims — validate and apply directly to {@link FlowUser}.
     */
    private OperationExecutionResult handleUserClaimOperation(PerformableOperation operation,
            FlowExecutionContext context, String tenantDomain) {

        String claimUri = extractNameFromPath(operation.getPath(), USER_CLAIMS_PATH_PREFIX);

        if (claimUri == null || claimUri.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim path. Claim URI is required.");
        }

        // For ADD operations, validate that the claim URI exists in system configuration.
        if (operation.getOp() == Operation.ADD) {
            if (!isValidClaimUri(claimUri, tenantDomain)) {
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Invalid claim URI. Claim must be configured in the system: " + claimUri);
            }
        }

        FlowUser user = context.getFlowUser();
        if (user == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "No FlowUser in FlowExecutionContext. Cannot apply user claim operation.");
        }

        switch (operation.getOp()) {
            case ADD:
            case REPLACE:
                if (operation.getValue() == null) {
                    return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                            "Value is required for " + operation.getOp() + " operation.");
                }
                user.addClaim(claimUri, String.valueOf(operation.getValue()));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User claim " + operation.getOp().name().toLowerCase(Locale.ENGLISH) + " applied.");

            case REMOVE:
                if (user.getClaims() != null) {
                    user.getClaims().remove(claimUri);
                }
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User claim removed.");

            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }

    /**
     * Handle operations on user inputs — apply directly to {@link FlowExecutionContext}.
     */
    private OperationExecutionResult handleUserInputOperation(PerformableOperation operation,
            FlowExecutionContext context) {

        String inputName = extractNameFromPath(operation.getPath(), USER_INPUTS_PATH_PREFIX);

        if (inputName == null || inputName.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid user input path. Input name is required.");
        }

        switch (operation.getOp()) {
            case ADD:
            case REPLACE:
                if (operation.getValue() == null) {
                    return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                            "Value is required for " + operation.getOp() + " operation.");
                }
                context.addUserInputData(inputName, String.valueOf(operation.getValue()));
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User input " + operation.getOp().name().toLowerCase(Locale.ENGLISH) + " applied.");

            case REMOVE:
                context.getUserInputData().remove(inputName);
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                        "User input removed.");

            default:
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Unsupported operation: " + operation.getOp());
        }
    }

    /**
     * Validate if a claim URI exists in system configuration.
     */
    private boolean isValidClaimUri(String claimUri, String tenantDomain) {

        if (claimUri == null || claimUri.isEmpty()) {
            return false;
        }
        return getValidClaimUris(tenantDomain).contains(claimUri);
    }

    /**
     * Get valid claim URIs for a tenant, loading from ClaimMetadataManagementService if not cached.
     */
    private Set<String> getValidClaimUris(String tenantDomain) {

        String cacheKey = tenantDomain != null ? tenantDomain : "carbon.super";

        if (validClaimUrisCache.containsKey(cacheKey)) {
            return validClaimUrisCache.get(cacheKey);
        }

        Set<String> validUris = new HashSet<>();
        try {
            ClaimMetadataManagementService claimService = getClaimMetadataManagementService();
            if (claimService != null) {
                List<LocalClaim> localClaims = claimService.getLocalClaims(tenantDomain);
                if (localClaims != null) {
                    for (LocalClaim claim : localClaims) {
                        validUris.add(claim.getClaimURI());
                    }
                }
            }
        } catch (ClaimMetadataException e) {
            LOG.error("Failed to load claim URIs for tenant: " + tenantDomain, e);
        }

        validClaimUrisCache.put(cacheKey, validUris);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loaded " + validUris.size() + " valid claim URIs for tenant: " + cacheKey);
        }
        return validUris;
    }

    private ClaimMetadataManagementService getClaimMetadataManagementService() {

        return ActionExecutionServiceComponentHolder.getInstance().getClaimMetadataManagementService();
    }

    /**
     * Extract the name/key from the operation path after the prefix.
     */
    private String extractNameFromPath(String path, String prefix) {

        if (path == null || !path.startsWith(prefix)) {
            return null;
        }

        String remaining = path.substring(prefix.length());

        if (remaining.endsWith("/")) {
            remaining = remaining.substring(0, remaining.length() - 1);
        }

        // Handle array index notation (e.g., /properties/items/0).
        int lastSlash = remaining.lastIndexOf(PATH_SEPARATOR);
        if (lastSlash > 0) {
            String possibleIndex = remaining.substring(lastSlash + 1);
            if (LAST_ELEMENT_CHARACTER.equals(possibleIndex) || isNumeric(possibleIndex)) {
                return remaining.substring(0, lastSlash);
            }
        }

        return remaining;
    }

    private boolean isNumeric(String str) {

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public ActionExecutionStatus<Error> processErrorResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationErrorResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String errorMessage = responseContext.getActionInvocationResponse().getErrorMessage();
        String errorDescription = responseContext.getActionInvocationResponse().getErrorDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing error response from In-Flow Extension. Error: " + errorMessage +
                    ", Description: " + errorDescription);
        }
        return new ErrorStatus(new Error(errorMessage, errorDescription));
    }

    @Override
    public ActionExecutionStatus<Failure> processFailureResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String failureReason = responseContext.getActionInvocationResponse().getFailureReason();
        String failureDescription = responseContext.getActionInvocationResponse().getFailureDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing failure response from In-Flow Extension. Reason: " + failureReason +
                    ", Description: " + failureDescription);
        }
        return new FailedStatus(new Failure(failureReason, failureDescription));
    }

    /**
     * Log operation execution results for diagnostics and debugging.
     */
    private void logOperationExecutionResults(List<OperationExecutionResult> results) {

        if (results.isEmpty()) {
            return;
        }

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            List<Map<String, String>> operationDetailsList = new ArrayList<>();
            results.forEach(result -> {
                Map<String, String> details = new HashMap<>();
                details.put("operation", result.getOperation().getOp() + " path: " +
                        result.getOperation().getPath());
                details.put("status", result.getStatus().toString());
                details.put("message", result.getMessage());
                operationDetailsList.add(details);
            });

            DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE);
            diagLogBuilder
                    .inputParam("executedOperations", operationDetailsList)
                    .resultMessage("Processed operations for " + getSupportedActionType().getDisplayName() +
                            " action.")
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagLogBuilder);
        }

        if (LOG.isDebugEnabled()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            try {
                String summary = mapper.writeValueAsString(results);
                LOG.debug(String.format("Processed response for action type: %s. Results: %s",
                        getSupportedActionType(), summary));
            } catch (JsonProcessingException e) {
                LOG.debug("Error occurred while logging operation execution results.", e);
            }
        }
    }

    /**
     * Inner class representing a successful In-Flow Extension execution result.
     */
    public static class InFlowExtensionSuccess implements Success {

        // Marker class for successful execution.
    }
}
