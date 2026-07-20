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
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Processes responses from Flow Extension actions, applying {@code REPLACE} operations on user
 * claims and credentials into pending maps on the {@link FlowContext} for the executor to forward.
 * Only {@code REPLACE} is supported and {@code /flow/} paths are read-only.
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
    public ActionExecutionStatus<Success> processSuccessResponse(FlowContext actionFlowContext,
                                                                 ActionExecutionResponseContext<ActionInvocationSuccessResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = getFlowExecutionContext(actionFlowContext);
        String tenantDomain = execCtx.getTenantDomain();

        List<ContextPath> modifyPaths = actionFlowContext.getValue(
                FlowExtensionConstants.MODIFY_PATHS_KEY, List.class);
        AccessConfig accessConfig = modifyPaths != null ? new AccessConfig(null, modifyPaths) : null;

        Map<String, Object> pendingClaims = new HashMap<>();
        Map<String, char[]> pendingCredentials = new HashMap<>();

        List<OperationExecutionResult> results = new ArrayList<>();

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();

        if (operations != null && !operations.isEmpty()) {
            for (PerformableOperation operation : operations) {
                if (operation.getOp() == Operation.REPLACE) {
                    operation = decryptOperationValueIfNeeded(operation, accessConfig, tenantDomain);
                }
                results.add(processOperation(
                        operation, pendingClaims, pendingCredentials, tenantDomain));
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow Extension SUCCESS response contained no operations. No context updates applied.");
            }
        }

        if (!pendingClaims.isEmpty()) {
            actionFlowContext.add(FlowExtensionConstants.PENDING_CLAIMS_KEY, pendingClaims);
        }
        if (!pendingCredentials.isEmpty()) {
            actionFlowContext.add(FlowExtensionConstants.PENDING_CREDENTIALS_KEY, pendingCredentials);
        }

        logOperationExecutionResults(results);

        return new SuccessStatus.Builder().setResponseContext(actionFlowContext.getContextData()).build();
    }


    /**
     * Process a single operation by validating and collecting it into the appropriate pending map.
     * Updates are not applied directly — they are stored in the pending maps and forwarded to
     * {@code TaskExecutionNode} via {@link org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse}.
     *
     * @param operation           The operation to process.
     * @param pendingClaims       Accumulator map for user claim updates.
     * @param pendingCredentials  Accumulator map for user credential updates.
     * @param tenantDomain        Tenant domain, used for claim URI validation.
     * @return The result of the operation execution.
     */
    private OperationExecutionResult processOperation(PerformableOperation operation,
                                                      Map<String, Object> pendingClaims,
                                                      Map<String, char[]> pendingCredentials,
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

        if (path.startsWith(FlowExtensionConstants.FlowContextPaths.USER_CLAIMS_SELECTOR_PREFIX)) {
            return handleUserClaimOperation(operation, pendingClaims, tenantDomain);
        } else if (path.startsWith(FlowExtensionConstants.FlowContextPaths.USER_CREDENTIALS_PATH_PREFIX)) {
            return handleUserCredentialOperation(operation, pendingCredentials);
        }

        return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                "Unknown path");
    }

    /**
     * Handle a REPLACE operation on a user claim by validating the claim URI and collecting the value
     * into the pending claims map. The claim URI must be in the WSO2 local dialect
     * ({@code http://wso2.org/claims/}), must not be an identity-system claim
     * ({@code http://wso2.org/claims/identity/}), and must resolve to a registered local claim in the
     * tenant. A single-valued claim takes a {@code String} value and a multi-valued claim takes a list
     * joined with commas. Failing any of these validations drops the operation, while an unavailable
     * claim metadata service or a lookup error aborts the response.
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
                    "Identity-system claims cannot be modified by Flow Extensions: " + claimUri);
        }

        Optional<LocalClaim> optionalLocalClaim = getLocalClaim(claimUri, tenantDomain);
        if (optionalLocalClaim.isEmpty()) {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Unknown local claim URI: " + claimUri);
        }

        Object claimValue = operation.getValue();
        String resolvedClaimValue;
        if (isMultiValuedClaim(optionalLocalClaim.get())) {
            if (!(claimValue instanceof List)) {
                return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                        "Expected a list value for multi-valued claim: " + claimUri);
            }
            resolvedClaimValue = StringUtils.join((List<?>) claimValue, ",");
        } else if (claimValue instanceof String) {
            resolvedClaimValue = (String) claimValue;
        } else {
            return new OperationExecutionResult(operation, OperationExecutionResult.Status.FAILURE,
                    "Expected a string value for single-valued claim: " + claimUri);
        }

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

    @Override
    public ActionExecutionStatus<Incomplete> processIncompleteResponse(FlowContext actionFlowContext,
                                                                       ActionExecutionResponseContext<ActionInvocationIncompleteResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        List<PerformableOperation> operations =
                responseContext.getActionInvocationResponse().getOperations();
        validateOperationForIncompleteStatus(operations);

        String redirectUrl = operations.get(0).getUrl();
        actionFlowContext.add(FlowExtensionConstants.PENDING_REDIRECT_URL_KEY, redirectUrl);

        return new IncompleteStatus.Builder()
                .responseContext(actionFlowContext.getContextData())
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
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE)
                    .resultMessage(message)
                    .configParam(DIAG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.FAILED));
        }
        throw new ActionExecutionResponseProcessorException(message);
    }

    @Override
    public ActionExecutionStatus<Error> processErrorResponse(FlowContext actionFlowContext,
                                                             ActionExecutionResponseContext<ActionInvocationErrorResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String errorMessage = responseContext.getActionInvocationResponse().getErrorMessage();
        String errorDescription = responseContext.getActionInvocationResponse().getErrorDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing error response from Flow Extension. Error: " + errorMessage +
                    ", Description: " + errorDescription);
        }

        return new ErrorStatus(new Error(errorMessage, errorDescription));
    }

    @Override
    public ActionExecutionStatus<Failure> processFailureResponse(FlowContext actionFlowContext,
                                                                 ActionExecutionResponseContext<ActionInvocationFailureResponse> responseContext)
            throws ActionExecutionResponseProcessorException {

        String failureReason = responseContext.getActionInvocationResponse().getFailureReason();
        String failureDescription = responseContext.getActionInvocationResponse().getFailureDescription();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing failure response from Flow Extension. Reason: " + failureReason +
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
     * Uses {@link AccessConfig#isModifyPathEncrypted(String)} for canonical checking. A scalar value
     * is treated as a single JWE compact string, while a multi-valued claim arrives as a
     * single-element array holding one JWE string that encrypts the comma-joined values; that element
     * is decrypted and its plaintext split on commas back into a list. The decrypted value(s) replace
     * the original on the returned operation.
     *
     * @param operation    The operation to potentially decrypt.
     * @param accessConfig The access config with encryption flags (may be null).
     * @param tenantDomain Tenant domain for IS private key resolution.
     * @return The operation with decrypted value(s), or the original operation if no decryption needed.
     */
    private PerformableOperation decryptOperationValueIfNeeded(PerformableOperation operation,
                                                               AccessConfig accessConfig, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        if (accessConfig == null || operation.getValue() == null) {
            return operation;
        }

        if (!accessConfig.isModifyPathEncrypted(operation.getPath())) {
            return operation;
        }

        Object value = operation.getValue();
        Object decryptedValue;
        if (value instanceof List<?> encryptedList) {

            if (encryptedList.size() != 1 || !(encryptedList.getFirst() instanceof String jweString)) {
                emitEncryptionContractViolation(operation.getPath(),
                        "Encrypted multi-valued claim must be a single-element array holding one JWE string.");
                throw new ActionExecutionResponseProcessorException(
                        "Value for encrypted modify path '" + operation.getPath() +
                                "' must be a single-element array holding one JWE-encrypted string.");
            }

            String plaintext = decryptJWEValue(jweString, operation.getPath(), tenantDomain);
            decryptedValue = Arrays.asList(plaintext.split(","));
        } else {
            decryptedValue = decryptJWEValue(value, operation.getPath(), tenantDomain);
        }

        PerformableOperation decryptedOp = new PerformableOperation();
        decryptedOp.setOp(operation.getOp());
        decryptedOp.setPath(operation.getPath());
        decryptedOp.setValue(decryptedValue);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Successfully decrypted inbound JWE value for path: " + operation.getPath());
        }
        return decryptedOp;
    }

    /**
     * Decrypt a single JWE-encrypted value from an encrypted modify path using the IS private key.
     * The value must be a JWE compact string; a non-String value or a non-JWE string breaks the
     * encryption contract and aborts the response.
     *
     * @param value        The value to decrypt, expected to be a JWE compact string.
     * @param path         The modify path, used for diagnostics and error messages.
     * @param tenantDomain Tenant domain for IS private key resolution.
     * @return The decrypted plaintext value.
     */
    private String decryptJWEValue(Object value, String path, String tenantDomain)
            throws ActionExecutionResponseProcessorException {

        if (!(value instanceof String)) {
            emitEncryptionContractViolation(path, "Value for encrypted modify path is not a String.");
            throw new ActionExecutionResponseProcessorException(
                    "Value for encrypted modify path '" + path +
                            "' must be a JWE-encrypted string, but received a non-String value.");
        }

        String stringValue = (String) value;
        if (!JWEEncryptionUtil.isJWEEncrypted(stringValue)) {
            emitEncryptionContractViolation(path, "Value for encrypted modify path is not JWE-encrypted.");
            throw new ActionExecutionResponseProcessorException(
                    "Value for encrypted modify path '" + path +
                            "' must be JWE-encrypted, but received a plaintext value.");
        }

        try {
            return JWEEncryptionUtil.decrypt(stringValue, tenantDomain);
        } catch (ActionExecutionException e) {
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                        ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE)
                        .resultMessage("Failed to decrypt inbound JWE value for modify path.")
                        .configParam("actionType", ActionType.FLOW_EXTENSION.getDisplayName())
                        .inputParam("path", path)
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED));
            }
            throw new ActionExecutionResponseProcessorException(
                    "Failed to decrypt inbound JWE value for path: " + path, e);
        }
    }

    /**
     * Emit a diagnostic failure event for a broken encryption contract on a modify path.
     * Extracted to keep {@link #decryptOperationValueIfNeeded} readable.
     */
    private void emitEncryptionContractViolation(String path, String reason) {

        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                    ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                    ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_RESPONSE)
                    .resultMessage(reason)
                    .configParam(DIAG_PARAM_ACTION_TYPE, ActionType.FLOW_EXTENSION.getDisplayName())
                    .inputParam("path", path)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                    .resultStatus(DiagnosticLog.ResultStatus.FAILED));
        }
    }

    private FlowExecutionContext getFlowExecutionContext(FlowContext actionFlowContext)
            throws ActionExecutionResponseProcessorException {

        FlowExecutionContext execCtx = actionFlowContext.getValue(
                FlowExtensionConstants.FLOW_EXECUTION_CONTEXT_KEY, FlowExecutionContext.class);
        if (execCtx == null) {
            throw new ActionExecutionResponseProcessorException(
                    "FlowExecutionContext not found in FlowContext.");
        }
        return execCtx;
    }
}
