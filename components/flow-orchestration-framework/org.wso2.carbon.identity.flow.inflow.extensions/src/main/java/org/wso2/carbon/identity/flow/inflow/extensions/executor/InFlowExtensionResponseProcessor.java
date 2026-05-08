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

package org.wso2.carbon.identity.flow.inflow.extensions.executor;

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
import org.wso2.carbon.identity.flow.inflow.extensions.InFlowExtensionConstants;
import org.wso2.carbon.identity.flow.inflow.extensions.model.AccessConfig;
import org.wso2.carbon.identity.flow.inflow.extensions.model.ContextPath;
import org.wso2.carbon.identity.flow.inflow.extensions.model.OperationExecutionResult;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for processing the response from In-Flow Extension actions.
 *
 * <p><b>Responsibility</b>: operation processing and collecting context updates into pending maps
 * that are stored in {@link FlowContext} for the executor to forward to {@code TaskExecutionNode}
 * via {@link org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse} fields.
 * It processes {@code REPLACE} operations on flow properties, user claims, and user credentials.</p>
 *
 * <p>Only {@code REPLACE} operations are supported. The {@code allowedOperations} list
 * (derived from modify paths and sent to the external service in the request, enforced
 * upstream by {@code ActionExecutorServiceImpl}) is the sole mechanism for gating which
 * operations are permitted. This processor performs additional validations:</p>
 * <ul>
 *   <li><b>Read-only areas</b>: No modifications allowed to {@code /flow/} paths.</li>
 * </ul>
 */
public class InFlowExtensionResponseProcessor implements ActionExecutionResponseProcessor {

    private static final Log LOG = LogFactory.getLog(InFlowExtensionResponseProcessor.class);

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
                InFlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        String tenantDomain = execCtx != null ? execCtx.getTenantDomain() : null;

        // Read path type annotations set by the request builder.
        // Maps clean paths to annotation content.
        Map<String, String> pathTypeAnnotations = flowContext.getValue(
                InFlowExtensionConstants.PATH_TYPE_ANNOTATIONS_KEY, Map.class);
        if (pathTypeAnnotations == null) {
            pathTypeAnnotations = Collections.emptyMap();
        }

        // Reconstruct AccessConfig from the resolved modify paths stored by the request builder.
        // This reuses AccessConfig.isModifyPathEncrypted() for canonical prefix-based encryption checking.
        List<ContextPath> modifyPaths = flowContext.getValue(
                InFlowExtensionConstants.MODIFY_PATHS_KEY, List.class);
        AccessConfig accessConfig = modifyPaths != null ? new AccessConfig(null, modifyPaths) : null;

        // Accumulate pending updates — applied by TaskExecutionNode via ExecutorResponse fields.
        Map<String, Object> pendingClaims = new HashMap<>();
        Map<String, char[]> pendingCredentials = new HashMap<>();
        Map<String, Object> pendingProperties = new HashMap<>();

        List<OperationExecutionResult> results = new ArrayList<>();

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        if (operations != null && !operations.isEmpty()) {
            for (PerformableOperation operation : operations) {
                operation = decryptOperationValueIfNeeded(operation, accessConfig, tenantDomain);
                results.add(processOperation(
                        operation, pathTypeAnnotations, pendingClaims, pendingCredentials, pendingProperties));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("In-Flow Extension SUCCESS response contained no operations. No context updates applied.");
            }
        }

        // Store non-empty pending maps in FlowContext for the executor to forward to TaskExecutionNode.
        if (!pendingClaims.isEmpty()) {
            flowContext.add(InFlowExtensionConstants.PENDING_CLAIMS_KEY, pendingClaims);
        }
        if (!pendingCredentials.isEmpty()) {
            flowContext.add(InFlowExtensionConstants.PENDING_CREDENTIALS_KEY, pendingCredentials);
        }
        if (!pendingProperties.isEmpty()) {
            flowContext.add(InFlowExtensionConstants.PENDING_PROPERTIES_KEY, pendingProperties);
        }

        logOperationExecutionResults(results);

        return new SuccessStatus.Builder()
                .setSuccess(new InFlowExtensionSuccess())
                .setResponseContext(Collections.emptyMap())
                .build();
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
     * @return The result of the operation execution.
     */
    private OperationExecutionResult processOperation(PerformableOperation operation,
            Map<String, String> pathTypeAnnotations,
            Map<String, Object> pendingClaims,
            Map<String, char[]> pendingCredentials,
            Map<String, Object> pendingProperties) {

        String path = operation.getPath();

        // Check if operation is on a read-only area.
        if (HierarchicalPrefixMatcher.isReadOnly(path)) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Path is in a read-only area. Modifications not allowed: " + path);
        }

        // Route to appropriate handler based on path prefix.
        if (path.startsWith(InFlowExtensionConstants.PROPERTIES_PATH_PREFIX)) {
            return handlePropertyOperation(operation, pathTypeAnnotations, pendingProperties);
        } else if (path.startsWith(InFlowExtensionConstants.USER_CLAIMS_PATH_PREFIX)) {
            return handleUserClaimOperation(operation, pendingClaims);
        } else if (path.startsWith(InFlowExtensionConstants.USER_CREDENTIALS_PATH_PREFIX)) {
            return handleUserCredentialOperation(operation, pendingCredentials);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path prefix. Supported: " + InFlowExtensionConstants.PROPERTIES_PATH_PREFIX +
                        ", " + InFlowExtensionConstants.USER_CLAIMS_PATH_PREFIX +
                        ", " + InFlowExtensionConstants.USER_CREDENTIALS_PATH_PREFIX);
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
                InFlowExtensionConstants.PROPERTIES_PATH_PREFIX);

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

        pendingProperties.put(propertyName, coercedValue);

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "Property replace applied.");
    }

    /**
     * Handle REPLACE operation on user claims — collect into pending claims map.
     * The value is always stringified via {@code String.valueOf()}.
     * Claim URI validation is intentionally omitted — validation is the responsibility
     * of flow validators, not the response processor.
     *
     * @param operation     The performable operation.
     * @param pendingClaims Accumulator map for user claim updates.
     */
    private OperationExecutionResult handleUserClaimOperation(PerformableOperation operation,
            Map<String, Object> pendingClaims) {

        String claimUri = extractNameFromPath(operation.getPath(),
                InFlowExtensionConstants.USER_CLAIMS_PATH_PREFIX);

        if (claimUri == null || claimUri.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid claim path. Claim URI is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
        }

        pendingClaims.put(claimUri, String.valueOf(operation.getValue()));
        return new OperationExecutionResult(operation, OperationExecutionResult.Status.SUCCESS,
                "User claim replace applied.");
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
                InFlowExtensionConstants.USER_CREDENTIALS_PATH_PREFIX);

        if (credentialKey == null || credentialKey.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Invalid credential path. Credential key is required.");
        }

        if (operation.getValue() == null) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Value is required for REPLACE operation.");
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

    @Override
    public ActionExecutionStatus<Incomplete> processIncompleteResponse(FlowContext flowContext,
            ActionExecutionResponseContext<ActionInvocationIncompleteResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        // Contract: INCOMPLETE must carry a REDIRECT op. If present, every other op is
        // intentionally discarded — the extension is expected to resend (possibly with
        // different values) on the resume call after the user returns from the redirect.
        // REDIRECT operations carry their target in the dedicated `url` field
        // (PerformableOperation rejects path/value for REDIRECT and rejects url for everything else).
        String redirectUrl = null;
        int ignoredOpCount = 0;
        if (operations != null) {
            for (PerformableOperation op : operations) {
                if (op.getOp() == Operation.REDIRECT) {
                    redirectUrl = op.getUrl();
                } else {
                    ignoredOpCount++;
                }
            }
        }

        if (redirectUrl == null || redirectUrl.isEmpty()) {
            LOG.warn("In-Flow Extension INCOMPLETE response is missing a REDIRECT operation.");
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        InFlowExtensionLogConstants.COMPONENT_ID,
                        InFlowExtensionLogConstants.ActionIDs.PROCESS_RESPONSE)
                        .resultMessage("INCOMPLETE response from In-Flow Extension is missing a REDIRECT operation.")
                        .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            throw new ActionExecutionResponseProcessorException(
                    "INCOMPLETE response from In-Flow Extension must contain a REDIRECT operation.");
        }

        if (ignoredOpCount > 0 && LOG.isDebugEnabled()) {
            LOG.debug("Ignored " + ignoredOpCount + " non-REDIRECT operation(s) on INCOMPLETE response. "
                    + "REPLACE ops on the redirect call are by-contract dropped — the extension is "
                    + "expected to resend them on the resume call after callback.");
        }

        flowContext.add(InFlowExtensionConstants.PENDING_REDIRECT_URL_KEY, redirectUrl);

        if (LOG.isDebugEnabled()) {
            try {
                String host = new java.net.URI(redirectUrl).getHost();
                LOG.debug("In-Flow Extension INCOMPLETE: redirect URL host resolved: " + host);
            } catch (java.net.URISyntaxException ignored) {
                LOG.debug("In-Flow Extension INCOMPLETE: redirect URL stored in flow context.");
            }
        }
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                    InFlowExtensionLogConstants.COMPONENT_ID,
                    InFlowExtensionLogConstants.ActionIDs.PROCESS_RESPONSE)
                    .resultMessage("In-Flow Extension INCOMPLETE response processed. Redirect URL stored in flow context.")
                    .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
        }

        return new IncompleteStatus.Builder()
                .responseContext(Collections.emptyMap())
                .build();
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
            AccessConfig accessConfig, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        if (accessConfig == null || operation.getValue() == null) {
            return operation;
        }

        // Check if this operation path has encryption enabled via modify paths in AccessConfig.
        if (!accessConfig.isModifyPathEncrypted(operation.getPath())) {
            return operation;
        }

        // Only decrypt string values that look like JWE compact serialization.
        Object value = operation.getValue();

        if (!(value instanceof String)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Value for encrypted path " + operation.getPath() +
                        " is not a String. Using as-is.");
            }
            return operation;
        }

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
            LOG.error("Failed to decrypt inbound JWE value for path '" + operation.getPath()
                    + "', tenant: " + tenantDomain, e);
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        InFlowExtensionLogConstants.COMPONENT_ID,
                        InFlowExtensionLogConstants.ActionIDs.PROCESS_RESPONSE)
                        .resultMessage("Failed to decrypt inbound JWE value for modify path.")
                        .configParam("actionType", ActionType.IN_FLOW_EXTENSION.getDisplayName())
                        .inputParam("path", operation.getPath())
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            throw new ActionExecutionResponseProcessorException(
                    "Failed to decrypt inbound JWE value for path: " + operation.getPath(), e);
        }
    }

}
