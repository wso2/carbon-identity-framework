/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.identity.action.execution.api.constant.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationErrorResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationFailureResponse;
import org.wso2.carbon.identity.action.execution.api.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.util.List;

/**
 * This utility class is for generating and printing diagnostic logs related to
 * action execution
 */
public class ActionExecutionDiagnosticLogger {

    public void logActionInitiation(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION,
                        action.getType().getDisplayName() + " action execution is initiated.",
                        DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logActionExecution(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION,
                        action.getType().getDisplayName() + " action execution started.",
                        DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logActionRuleEvaluation(Action action, boolean ruleEvaluationResult) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.EVALUATE_RULE,
                        "Rule of " + action.getType().getDisplayName() + " action evaluated to " +
                                ruleEvaluationResult, DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logNoRuleConfiguredForAction(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.EVALUATE_RULE,
                        "No rule configured for action " + action.getType().getDisplayName() +
                                ". Proceed executing the action."
                        , DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logActionRequest(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.PROCESS_ACTION_REQUEST,
                "Call external service endpoint " + action.getEndpoint().getUri() + " for "
                        + action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.SUCCESS);

        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logSuccessResponse(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received success response from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.SUCCESS);
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logIncompleteResponse(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received response with INCOMPLETE status from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.SUCCESS);
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logErrorResponse(Action action, ActionInvocationErrorResponse errorResponse) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received error response from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam("Error Message", errorResponse.getErrorMessage())
                .inputParam("Error Description", errorResponse.getErrorDescription());
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logFailureResponse(Action action, ActionInvocationFailureResponse failureResponse) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received failure response from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam("Failure Message", failureResponse.getFailureReason())
                .inputParam("Failure Description", failureResponse.getFailureDescription());
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logErrorResponse(Action action, ActionInvocationResponse actionInvocationResponse) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Failed to call external endpoint for " + action.getType().getDisplayName()
                        + " action. " +
                        (actionInvocationResponse.getErrorLog() != null ?
                                actionInvocationResponse.getErrorLog() : "Unknown error occured."),
                DiagnosticLog.ResultStatus.FAILED);
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logPerformableOperations(Action action, List<String> allowedOps, List<String> notAllowedOps) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.VALIDATE_ACTION_RESPONSE,
                "Validated operations to perform on " + action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.SUCCESS);
        triggerLogEvent(
                addActionConfigParams(diagnosticLogBuilder, action)
                        .configParam("allowedOperations", allowedOps.isEmpty() ? "empty" : allowedOps)
                        .configParam("notAllowedOperations", notAllowedOps.isEmpty() ? "empty" : notAllowedOps));
    }

    public void logAPICallRetry(HttpPost request, int currentAttempt, int retryCount) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        String message = "External endpoint " + request.getURI() + " for action execution seems to be unavailable. " +
                (currentAttempt < retryCount
                        ? "Retrying API call attempt " + currentAttempt + " of " + (retryCount - 1) + "."
                        : "Maximum retry attempts reached.");

        triggerLogEvent(initializeDiagnosticLogBuilder(ActionExecutionLogConstants.ActionIDs.SEND_ACTION_REQUEST,
                message, DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logAPICallTimeout(HttpPost request, int currentAttempt, int retryCount) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        String message = "Request to the external endpoint " + request.getURI() + " for action execution timed out. " +
                (currentAttempt < retryCount
                        ? "Retrying attempt " + currentAttempt + " of " + (retryCount - 1) + "."
                        : "Maximum retry attempts reached.");

        triggerLogEvent(initializeDiagnosticLogBuilder(ActionExecutionLogConstants.ActionIDs.SEND_ACTION_REQUEST,
                message, DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logAPICallError(HttpPost request) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.SEND_ACTION_REQUEST,
                        "Request for external endpoint " + request.getURI() + " for action failed" +
                                " due to an error.",
                        DiagnosticLog.ResultStatus.FAILED));
    }

    private DiagnosticLog.DiagnosticLogBuilder addActionConfigParams(
            DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder, Action action) {

        return diagnosticLogBuilder
                .configParam("resourceId", action.getId())
                .configParam("actionType", action.getType().getDisplayName())
                .configParam("actionEndpoint", action.getEndpoint().getUri())
                .configParam("actionEndpointAuthenticationType",
                        action.getEndpoint().getAuthentication().getType().getName());

    }

    private DiagnosticLog.DiagnosticLogBuilder initializeDiagnosticLogBuilder(String actionID, String message,
                                                                              DiagnosticLog.ResultStatus resultStatus) {

        DiagnosticLog.DiagnosticLogBuilder diagLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                ActionExecutionLogConstants.ACTION_EXECUTION_COMPONENT_ID,
                actionID);
        diagLogBuilder
                .resultMessage(message)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(resultStatus);
        return diagLogBuilder;
    }

    private void triggerLogEvent(DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder) {

        LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
    }
}
