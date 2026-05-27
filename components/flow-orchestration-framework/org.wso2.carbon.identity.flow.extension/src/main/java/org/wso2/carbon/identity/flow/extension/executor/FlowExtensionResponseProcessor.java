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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.flow.extension.internal.FlowExtensionDataHolder;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionResponseProcessorException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionResponseContext;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationIncompleteResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationSuccessResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.FlowContext;
import org.wso2.carbon.identity.action.execution.api.model.Incomplete;
import org.wso2.carbon.identity.action.execution.api.model.IncompleteStatus;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.flow.extension.FlowExtensionConstants;
import org.wso2.carbon.identity.flow.extension.model.AccessConfig;
import org.wso2.carbon.identity.flow.extension.model.ContextPath;
import org.wso2.carbon.identity.flow.extension.model.OperationExecutionResult;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Processes responses from In-Flow Extension actions, applying {@code REPLACE} operations on
 * flow properties, user claims, and user credentials. Updates are collected into pending maps on
 * {@link FlowContext} for the executor to forward via
 * {@link org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse}.
 *
 * <p>Only {@code REPLACE} is supported; gating is done upstream via {@code allowedOperations}
 * (enforced by {@code ActionExecutorServiceImpl}). {@code /flow/} paths are read-only.</p>
 *
 * <p><b>Failure handling.</b> Contract-level failures (non-String on an encrypted path, missing
 * JWE envelope, JWE decryption failure) abort the call via
 * {@link ActionExecutionResponseProcessorException}. Per-operation validation failures (unknown
 * path, read-only path, missing value, unknown claim URI, unknown credential key) drop the
 * offending op, keep the rest, and return {@link SuccessStatus} — surfaced under
 * {@link FlowExtensionConstants.ResponseContext#FAILED_OPERATIONS_KEY} alongside
 * {@link FlowExtensionConstants.ResponseContext#TOTAL_OPERATIONS_KEY} for callers wanting strict
 * semantics.</p>
 */
public class FlowExtensionResponseProcessor implements ActionExecutionResponseProcessor {

    private static final Log LOG = LogFactory.getLog(FlowExtensionResponseProcessor.class);
    private static final String ERROR_VALUE_REQUIRED_FOR_REPLACE = "Value is required for REPLACE operation.";
    private static final String DIAG_PARAM_ACTION_TYPE = "actionType";
    private static final String MULTI_VALUED_CLAIM_PROPERTY = "multiValued";

    @Override
    public ActionType getSupportedActionType() {

        return ActionType.FLOW_EXTENSION;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ActionExecutionStatus<Success> processSuccessResponse(FlowContext flowContext,
                                                                 ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = getFlowExecutionContext(flowContext);
        String tenantDomain = execCtx.getTenantDomain();

        Map<String, String> pathTypeAnnotations = flowContext.getValue(
                FlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        if (pathTypeAnnotations == null) {
            pathTypeAnnotations = Collections.emptyMap();
        }

        List<ContextPath> modifyPaths = flowContext.getValue(
                FlowExtensionConstants.MODIFY_PATHS_KEY, List.class);
        AccessConfig accessConfig = modifyPaths != null ? new AccessConfig(null, modifyPaths) : null;

        Map<String, Object> pendingClaims = new HashMap<>();
        Map<String, char[]> pendingCredentials = new HashMap<>();
        Map<String, Object> pendingProperties = new HashMap<>();

        List<OperationExecutionResult> results = new ArrayList<>();

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        if (operations != null && !operations.isEmpty()) {
            for (PerformableOperation operation : operations) {
                if (operation.getOp() == Operation.REPLACE) {
                    operation = decryptOperationValueIfNeeded(operation, accessConfig, tenantDomain);
                }
                results.add(processOperation(
                        operation, pathTypeAnnotations, pendingClaims, pendingCredentials,
                        pendingProperties, tenantDomain));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In-Flow Extension SUCCESS response contained no operations. No context updates applied.");
            }
        }

        if (!pendingClaims.isEmpty()) {
            flowContext.add(FlowExtensionConstants.PENDING_CLAIMS_KEY, pendingClaims);
        }
        if (!pendingCredentials.isEmpty()) {
            flowContext.add(FlowExtensionConstants.PENDING_CREDENTIALS_KEY, pendingCredentials);
        }
        if (!pendingProperties.isEmpty()) {
            flowContext.add(FlowExtensionConstants.PENDING_PROPERTIES_KEY, pendingProperties);
        }

        logOperationExecutionResults(results);

        return new SuccessStatus.Builder().setResponseContext(flowContext.getContextData()).build();
    }


    /**
     * Process a single operation by validating and collecting it into the appropriate pending map.
     * Updates are not applied directly — they are stored in the pending maps and forwarded to
     * {@code TaskExecutionNode} via {@link org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse}.
     *
     * @param operation           The operation to process.
     * @param pathTypeAnnotations Map of clean paths to their type annotations from allowed operations.
     * @param pendingClaims       Accumulator map for user claim updates.
     * @param pendingCredentials  Accumulator map for user credential updates.
     * @param pendingProperties   Accumulator map for flow property updates.
     * @param tenantDomain        Tenant domain, used for claim URI validation.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult processOperation(PerformableOperation operation,
                                                      Map<String, String> pathTypeAnnotations,
                                                      Map<String, Object> pendingClaims,
                                                      Map<String, char[]> pendingCredentials,
                                                      Map<String, Object> pendingProperties,
                                                      String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        String path = operation.getPath();
        if (StringUtils.isBlank(path)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Operation path is null or empty.");
        }

        if (operation.getOp() != Operation.REPLACE) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Unsupported operation type: " + operation.getOp() + ". Only REPLACE is supported.");
        }

        if (AccessConfig.isReadOnly(path)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Modifications are not allowed for the read-only paths" );
        }

        if (path.startsWith(FlowExtensionConstants.FlowContextPaths.PROPERTIES_PATH_PREFIX)) {
            return handlePropertyOperation(operation, pathTypeAnnotations, pendingProperties);
        } else if (path.startsWith(FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX)) {
            return handleUserClaimOperation(operation, pendingClaims, tenantDomain);
        } else if (path.startsWith(FlowExtensionConstants.FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX)) {
            return handleUserCredentialOperation(operation, pendingCredentials);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path");
    }

    /**
     * Handle operation on flow properties — collect into pending properties map.
     *
     * <p>Only flat property paths are supported (e.g., {@code /properties/riskScore}).
     * Value coercion is applied based on path type annotations from the request builder via
     * {@link PathTypeAnnotationUtil#coerceValue}.</p>
     *
     * @param operation           The performable operation.
     * @param pathTypeAnnotations Path type annotations map from request builder.
     * @param pendingProperties   Accumulator map for property updates.
     */
    private OperationExecutionResult handlePropertyOperation(PerformableOperation operation,
                                                             Map<String, String> pathTypeAnnotations, Map<String, Object> pendingProperties) {

        String propertyName = extractNameFromPath(operation.getPath(),
                FlowExtensionConstants.FlowContextPaths.PROPERTIES_PATH_PREFIX);

        if (StringUtils.isBlank(propertyName)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid property path. Property name is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    ERROR_VALUE_REQUIRED_FOR_REPLACE);
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
                    "Array value exceeds maximum item limit for path.");
        }

        pendingProperties.put(propertyName, coercedValue);

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property replace applied.");
    }

    /**
     * Handle REPLACE operation on user claims — validate the claim URI then collect into pending
     * claims map. Validation gates:
     * <ol>
     *   <li>Claim URI must be in the WSO2 local claim dialect ({@code http://wso2.org/claims/}).</li>
     *   <li>Identity-system claims ({@code http://wso2.org/claims/identity/}) are rejected.</li>
     *   <li>The claim URI must correspond to a registered local claim in the tenant; unknown
     *       claims are rejected per-operation. If the
     *       {@link org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService}
     *       is unavailable or the lookup throws, the whole response aborts via
     *       {@link ActionExecutionResponseProcessorException}.</li>
     * </ol>
     * The value is always stringified via {@code String.valueOf()}.
     *
     * @param operation     The performable operation.
     * @param pendingClaims Accumulator map for user claim updates.
     * @param tenantDomain  Tenant domain for claim existence lookup.
     */
    private OperationExecutionResult handleUserClaimOperation(PerformableOperation operation,
                                                              Map<String, Object> pendingClaims, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        String claimUri = extractClaimUriFromPath(operation.getPath());

        if (StringUtils.isBlank(claimUri)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim path. Claim URI is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    ERROR_VALUE_REQUIRED_FOR_REPLACE);
        }

        if (!claimUri.startsWith(PathTypeAnnotationUtil.LOCAL_CLAIM_DIALECT_PREFIX)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Claim URI must be in the local dialect (" +
                            PathTypeAnnotationUtil.LOCAL_CLAIM_DIALECT_PREFIX + "): " + claimUri);
        }
        if (claimUri.startsWith(PathTypeAnnotationUtil.IDENTITY_CLAIM_URI_PREFIX)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Identity-system claims cannot be modified by In-Flow Extensions: " + claimUri);
        }

        Optional<LocalClaim> optionalLocalClaim = getLocalClaim(claimUri, tenantDomain);
        if (optionalLocalClaim.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Unknown local claim URI: " + claimUri);
        }

        String resolvedClaimValue = getResolvedClaimValue(operation.getValue(), optionalLocalClaim.get());
        pendingClaims.put(claimUri, resolvedClaimValue);
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "User claim replace applied.");
    }

    private Optional<LocalClaim> getLocalClaim(String claimUri, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService claimService =
                FlowExtensionDataHolder.getInstance().getClaimMetadataManagementService();
        if (claimService == null) {
            throw new ActionExecutionResponseProcessorException(
                    "ClaimMetadataManagementService is unavailable; cannot validate claim URI: " + claimUri);
        }
        try {
            return claimService.getLocalClaim(claimUri, tenantDomain);
        } catch (ClaimMetadataException e) {
            throw new ActionExecutionResponseProcessorException(
                    "Failed to look up local claim '" + claimUri + "' in tenant '" + tenantDomain + "'.", e);
        }
    }

    private String getResolvedClaimValue(Object operationValue, LocalClaim localClaim) throws
            ActionExecutionResponseProcessorException {

        if (isMultiValuedClaim(localClaim)) {
            if (operationValue instanceof List) {
                List<String> valueList = (List<String>) operationValue;
                return StringUtils.join(valueList, ",");
            }
            throw new ActionExecutionResponseProcessorException(
                    "Expected a list value for multi-valued claim: " + localClaim.getClaimURI());
        } else if (operationValue instanceof String) {
            return String.valueOf(operationValue);
        }
        throw new ActionExecutionResponseProcessorException(
                "Expected a string value for single-valued claim: " + localClaim.getClaimURI());
    }

    private boolean isMultiValuedClaim(LocalClaim localClaim) {

        return Boolean.parseBoolean(localClaim.getClaimProperty(MULTI_VALUED_CLAIM_PROPERTY));
    }
    /**
     * Handle REPLACE operation on user credentials — collect into pending credentials map.
     * No key validation is applied; any credential key is accepted. The value is converted
     * to {@code char[]} immediately to avoid holding the secret as a plain {@code String}
     * any longer than necessary.
     *
     * @param operation          The performable operation.
     * @param pendingCredentials Accumulator map for user credential updates.
     */
    private OperationExecutionResult handleUserCredentialOperation(PerformableOperation operation,
                                                                   Map<String, char[]> pendingCredentials) {

        String credentialKey = extractNameFromPath(operation.getPath(),
                FlowExtensionConstants.FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX);

        if (StringUtils.isBlank(credentialKey)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid credential path. Credential key is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    ERROR_VALUE_REQUIRED_FOR_REPLACE);
        }

        pendingCredentials.put(credentialKey, String.valueOf(operation.getValue()).toCharArray());
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "User credential replace applied.");
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

    /**
     * Extract the claim URI from an external-format claim path.
     * Accepts the selector form {@code /user/claims[uri=<claimUri>]}.
     * Returns {@code null} if the path is null or does not match the expected format.
     */
    private String extractClaimUriFromPath(String path) {

        if (path == null) {
            return null;
        }
        if (path.startsWith(FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX)
                && path.endsWith(FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_SUFFIX)) {
            return path.substring(
                    FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX.length(),
                    path.length() - FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_SUFFIX.length());
        }
        return null;
    }

    /**
     * Normalize a claim path for encryption checks.
     * Internal and external formats now both use the selector form
     * {@code /user/claims[uri=<claimUri>]}, so no conversion is needed.
     * All paths are returned unchanged.
     */
    private static String normalizeToInternalPath(String path) {

        return path;
    }

    @Override
    public ActionExecutionStatus<Incomplete> processIncompleteResponse(FlowContext flowContext,
                                                                       ActionExecutionResponseContext<ActionInvocationIncompleteResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();
        validateOperationForIncompleteStatus(operations);

        String redirectUrl = operations.get(0).getUrl();
        flowContext.add(FlowExtensionConstants.PENDING_REDIRECT_URL_KEY, redirectUrl);

        return new IncompleteStatus.Builder()
                .responseContext(flowContext.getContextData())
                .build();
    }

    /**
     * Strictly validate the operations list on an INCOMPLETE response: it must contain exactly one
     * REDIRECT operation with a non-empty URL. Any deviation aborts the response.
     */
    private void validateOperationForIncompleteStatus(List<PerformableOperation> operations)
            throws ActionExecutionResponseProcessorException {

        if (operations == null || operations.isEmpty()) {
            throwIncompleteValidationError(
                    "INCOMPLETE response from Flow Extension must contain a REDIRECT operation.");
        }
        if (operations.size() != 1) {
            throwIncompleteValidationError(
                    "INCOMPLETE response from Flow Extension must contain exactly one operation.");
        }
        if (operations.get(0).getOp() != Operation.REDIRECT) {
            throwIncompleteValidationError(
                    "The operation in an INCOMPLETE response from Flow Extension must be a REDIRECT operation.");
        }
        String url = operations.get(0).getUrl();
        if (url == null || url.isEmpty()) {
            throwIncompleteValidationError(
                    "The REDIRECT operation in an INCOMPLETE response from Flow Extension must have a valid URL.");
        }
    }

    private void throwIncompleteValidationError(String message) throws ActionExecutionResponseProcessorException {

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                    FlowExtensionConstants.Log.COMPONENT_ID,
                    FlowExtensionConstants.Log.ActionIDs.PROCESS_RESPONSE)
                    .resultMessage(message)
                    .configParam(DIAG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.FAILED));
        }
        throw new ActionExecutionResponseProcessorException(message);
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
     * Only op / path / status / message are ever logged. The operation {@code value} is
     * intentionally omitted: by this point in the pipeline encrypted values have been
     * decrypted in place and credential REPLACE operations carry plaintext secrets, so
     * serialising the full operation would leak sensitive data into log aggregators.
     */
    private void logOperationExecutionResults(List<OperationExecutionResult> results) {

        if (results.isEmpty()) {
            return;
        }

        List<Map<String, String>> operationDetailsList = new ArrayList<>();
        results.forEach(result -> {
            Map<String, String> details = new HashMap<>();
            details.put("operation", result.getOperation().getOp() + " path: " +
                    result.getOperation().getPath());
            details.put("status", result.getStatus().toString());
            details.put("message", result.getMessage());
            operationDetailsList.add(details);
        });

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE);
            diagnosticLogBuilder
                    .inputParam("executedOperations", operationDetailsList)
                    .resultMessage("Processed operations for " + getSupportedActionType().getDisplayName() +
                            " action.")
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .build();
            LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
        }

        if (LOG.isDebugEnabled()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            try {
                String summary = mapper.writeValueAsString(operationDetailsList);
                LOG.debug(String.format("Processed response for action type: %s. Results: %s",
                        getSupportedActionType(), summary));
            } catch (JsonProcessingException e) {
                LOG.debug("Error occurred while logging operation execution results.", e);
            }
        }
    }

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
                                                               AccessConfig accessConfig, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        if (accessConfig == null || operation.getValue() == null) {
            return operation;
        }

        String internalPath = normalizeToInternalPath(operation.getPath());
        if (!accessConfig.isModifyPathEncrypted(internalPath)) {
            return operation;
        }

        Object value = operation.getValue();

        if (!(value instanceof String)) {
            emitEncryptionContractViolation(operation.getPath(),
                    "Value for encrypted modify path is not a String.");
            throw new ActionExecutionResponseProcessorException(
                    "Value for encrypted modify path '" + operation.getPath() +
                            "' must be a JWE-encrypted string, but received a non-String value.");
        }

        String stringValue = (String) value;
        if (!JWEEncryptionUtil.isJWEEncrypted(stringValue)) {
            emitEncryptionContractViolation(operation.getPath(),
                    "Value for encrypted modify path is not JWE-encrypted.");
            throw new ActionExecutionResponseProcessorException(
                    "Value for encrypted modify path '" + operation.getPath() +
                            "' must be JWE-encrypted, but received a plaintext value.");
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
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FlowExtensionConstants.Log.COMPONENT_ID,
                        FlowExtensionConstants.Log.ActionIDs.PROCESS_RESPONSE)
                        .resultMessage("Failed to decrypt inbound JWE value for modify path.")
                        .configParam("actionType", ActionType.FLOW_EXTENSION.getDisplayName())
                        .inputParam("path", operation.getPath())
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            throw new ActionExecutionResponseProcessorException(
                    "Failed to decrypt inbound JWE value for path: " + operation.getPath(), e);
        }
    }

    /**
     * Emit a diagnostic failure event for a broken encryption contract on a modify path.
     * Extracted to keep {@link #decryptOperationValueIfNeeded} readable.
     */
    private void emitEncryptionContractViolation(String path, String reason) {

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                    FlowExtensionConstants.Log.COMPONENT_ID,
                    FlowExtensionConstants.Log.ActionIDs.PROCESS_RESPONSE)
                    .resultMessage(reason)
                    .configParam(DIAG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                    .inputParam("path", path)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.FAILED));
        }
    }

    private FlowExecutionContext getFlowExecutionContext(FlowContext flowContext)
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = flowContext.getValue(
                FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        if (execCtx == null) {
            throw new ActionExecutionResponseProcessorException(
                    "FlowExecutionContext not found in FlowContext.");
        }
        return execCtx;
    }
}
