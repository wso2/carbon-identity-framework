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
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.execution.engine.internal.FlowExecutionEngineDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowUser;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for processing the response from In-Flow Extension actions.
 *
 * <p><b>Responsibility</b>: operation processing and applying context updates directly to
 * the {@link FlowExecutionContext}. It processes REPLACE operations on flow
 * properties, user claims, and user inputs.</p>
 *
 * <p>Only {@code REPLACE} operations are supported. The {@code allowedOperations} list
 * (derived from modify paths and sent to the external service in the request, enforced
 * upstream by {@code ActionExecutorServiceImpl}) is the sole mechanism for gating which
 * operations are permitted. This processor performs additional validations:</p>
 * <ul>
 *   <li><b>Read-only areas</b>: No modifications allowed to {@code /flow/} or {@code /graph/}.</li>
 *   <li><b>Claim URI validation</b>: For {@code /user/claims/} operations, validates that
 *       the claim URI exists in the local claim dialect and is not an identity claim.</li>
 * </ul>
 */
public class InFlowExtensionResponseProcessor implements ActionExecutionResponseProcessor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionResponseProcessor.class);

    private static final String PROPERTIES_PATH_PREFIX = "/properties/";
    private static final String USER_CLAIMS_PATH_PREFIX = "/user/claims/";
    private static final String USER_INPUTS_PATH_PREFIX = "/input/";

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
        // Maps clean paths to annotation content
        Map<String, String> pathTypeAnnotations = flowContext.getValue(
                InFlowExtensionExecutor.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        if (pathTypeAnnotations == null) {
            pathTypeAnnotations = Collections.emptyMap();
        }

        // Read access config for encryption metadata.
        // Uses AccessConfig.isModifyPathEncrypted() for canonical encryption checking.
        AccessConfig accessConfig = flowContext.getValue(
                InFlowExtensionExecutor.ACCESS_CONFIG_KEY, AccessConfig.class);

        List<OperationExecutionResult> results = new ArrayList<>();

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        if (operations != null && !operations.isEmpty()) {
            for (PerformableOperation operation : operations) {
                // Decrypt inbound value if this operation path is marked as encrypted in AccessConfig.
                operation = decryptOperationValueIfNeeded(operation, accessConfig, tenantDomain);

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
        } else if (path.startsWith(USER_INPUTS_PATH_PREFIX)) {
            return handleUserInputOperation(operation, context);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path prefix. Supported: " + PROPERTIES_PATH_PREFIX +
                        ", " + USER_CLAIMS_PATH_PREFIX + ", " + USER_INPUTS_PATH_PREFIX);
    }

    /**
     * Handle operation on flow properties — apply directly to {@link FlowExecutionContext}.
     *
     * <p>Only flat property paths are supported (e.g., {@code /properties/riskScore}).
     * The property is created if it does not already exist. Value coercion is applied
     * based on path type annotations from the request builder via
     * {@link PathTypeAnnotationUtil#coerceValue}.</p>
     *
     * @param operation           The performable operation.
     * @param context             The FlowExecutionContext.
     * @param pathTypeAnnotations Path type annotations map from request builder.
     */
    private OperationExecutionResult handlePropertyOperation(PerformableOperation operation,
            FlowExecutionContext context, Map<String, String> pathTypeAnnotations) {

        String propertyName = extractNameFromPath(operation.getPath(), PROPERTIES_PATH_PREFIX);

        if (propertyName == null || propertyName.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid property path. Property name is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
        }

        // Validate complex object structure against annotation schema before coercion.
        if (!PathTypeAnnotationUtil.validateValueAgainstAnnotation(
                operation.getPath(), operation.getValue(), pathTypeAnnotations)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value does not match annotation schema for path: " + operation.getPath());
        }

        // Coerce value based on path type annotation.
        Object coercedValue = PathTypeAnnotationUtil.coerceValue(
                operation.getPath(), operation.getValue(), pathTypeAnnotations);

        // Enforce array item limits after coercion.
        if (!PathTypeAnnotationUtil.enforceArrayItemLimit(
                operation.getPath(), coercedValue, pathTypeAnnotations)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Array value exceeds maximum item limit for path: " + operation.getPath());
        }

        context.setProperty(propertyName, coercedValue);

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property replace applied.");
    }

    /**
     * Handle REPLACE operation on user claims — validate and apply directly to {@link FlowUser}.
     *
     * <p>Validates that the claim URI:</p>
     * <ul>
     *   <li>Exists in the local claim dialect (via {@link ClaimMetadataManagementService}).</li>
     *   <li>Is not an identity claim ({@code http://wso2.org/claims/identity/}).</li>
     * </ul>
     * <p>The value is always stringified via {@code String.valueOf()}.</p>
     */
    private OperationExecutionResult handleUserClaimOperation(PerformableOperation operation,
            FlowExecutionContext context, String tenantDomain) {

        String claimUri = extractNameFromPath(operation.getPath(), USER_CLAIMS_PATH_PREFIX);

        if (claimUri == null || claimUri.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim path. Claim URI is required.");
        }

        // Reject identity claims — these are system-managed and not user-modifiable.
        if (claimUri.startsWith(PathTypeAnnotationUtil.IDENTITY_CLAIM_URI_PREFIX)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Identity claims cannot be modified via extensions: " + claimUri);
        }

        // Validate claim exists in local claim dialect.
        if (!isValidClaimUri(claimUri, tenantDomain)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim URI. Claim must be configured in the local claim dialect: " + claimUri);
        }

        FlowUser user = context.getFlowUser();
        if (user == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "No FlowUser in FlowExecutionContext. Cannot apply user claim operation.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
        }

        user.addClaim(claimUri, String.valueOf(operation.getValue()));
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "User claim replace applied.");
    }

    /**
     * Handle REPLACE operation on user inputs — apply directly to {@link FlowExecutionContext}.
     * The value is always stringified via {@code String.valueOf()}.
     */
    private OperationExecutionResult handleUserInputOperation(PerformableOperation operation,
            FlowExecutionContext context) {

        String inputName = extractNameFromPath(operation.getPath(), USER_INPUTS_PATH_PREFIX);

        if (inputName == null || inputName.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid user input path. Input name is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
        }

        context.addUserInputData(inputName, String.valueOf(operation.getValue()));
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "User input replace applied.");
    }

    //TODO: These validations can be removed once the attribute executor is introduced.
    /**
     * Validate if a claim URI exists in the local claim dialect.
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

        return FlowExecutionEngineDataHolder.getInstance().getClaimMetadataManagementService();
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

        return remaining;
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

    // ========================= Inbound decryption =========================

    /**
     * Decrypt the operation value if the operation path has encryption enabled in the AccessConfig.
     * Uses {@link AccessConfig#isModifyPathEncrypted(String)} for canonical checking.
     * For operations with encrypted paths, checks if the value looks like a JWE compact
     * serialization and decrypts it using the IS's private key.
     *
     * @param operation    The operation to potentially decrypt.
     * @param accessConfig The access config with encryption flags (may be null).
     * @param tenantDomain Tenant domain for IS private key resolution.
     * @return The operation with decrypted value, or the original operation if no decryption needed.
     */
    private PerformableOperation decryptOperationValueIfNeeded(PerformableOperation operation,
            AccessConfig accessConfig, String tenantDomain) {

        if (accessConfig == null || operation.getValue() == null) {
            return operation;
        }

        // Check if this operation path has encryption enabled via modify paths in AccessConfig.
        if (!accessConfig.isModifyPathEncrypted(operation.getPath())) {
            return operation;
        }

        // Only decrypt string values that look like JWE compact serialization.
        Object value = operation.getValue();

        String stringValue = (String) value;
        if (!JWEEncryptionUtil.isJWEEncrypted(stringValue)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Value for encrypted path " + operation.getPath() +
                        " is not JWE-encrypted. Using as-is.");
            }
            return operation;
        }

        try {
            String decrypted = JWEEncryptionUtil.decrypt(stringValue, tenantDomain);
            PerformableOperation decryptedOp = new PerformableOperation();
            decryptedOp.setOp(operation.getOp());
            decryptedOp.setPath(operation.getPath());
            decryptedOp.setValue(decrypted);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully decrypted inbound JWE value for path: " + operation.getPath());
            }
            return decryptedOp;
        } catch (Exception e) {
            LOG.warn("Failed to decrypt inbound JWE value for path: " + operation.getPath() +
                    ". Using raw value.", e);
            return operation;
        }
    }
}
