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

package org.wso2.carbon.identity.action.execution.util;

import org.apache.http.client.methods.HttpPost;
import org.wso2.carbon.identity.action.execution.ActionExecutionLogConstants;
import org.wso2.carbon.identity.action.execution.model.ActionInvocationResponse;
import org.wso2.carbon.identity.action.execution.model.ActionType;
import org.wso2.carbon.identity.action.management.model.Action;
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

    public void logSkippedActionExecution(ActionType actionType) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.EXECUTE_ACTION,
                        "Skip executing action for " + actionType + " type.",
                        DiagnosticLog.ResultStatus.FAILED));
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

    public void logErrorResponse(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received error response from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.FAILED);
        triggerLogEvent(addActionConfigParams(diagnosticLogBuilder, action));
    }

    public void logFailureResponse(Action action) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                ActionExecutionLogConstants.ActionIDs.RECEIVE_ACTION_RESPONSE,
                "Received failure response from external endpoint " +
                        action.getEndpoint().getUri() + " for " +
                        action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.FAILED);
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
                ActionExecutionLogConstants.ActionIDs.VALIDATE_ACTION_OPERATIONS,
                "Validated operations to perform on " + action.getType().getDisplayName() + " action.",
                DiagnosticLog.ResultStatus.SUCCESS);
        triggerLogEvent(
                addActionConfigParams(diagnosticLogBuilder, action)
                        .configParam("allowed operations", allowedOps.isEmpty() ? "empty" : allowedOps)
                        .configParam("not allowed operations", notAllowedOps.isEmpty() ? "empty" : notAllowedOps));
    }

    public void logAPICallRetry(HttpPost request, int attempts, int retryCount) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.SEND_ACTION_REQUEST,
                        "External endpoint " + request.getURI() + " for action " +
                                "execution seems to be unavailable. Retrying API call attempt " +
                                attempts + " of " + retryCount + ".",
                        DiagnosticLog.ResultStatus.SUCCESS));
    }

    public void logAPICallTimeout(HttpPost request, int attempts, int retryCount) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        triggerLogEvent(
                initializeDiagnosticLogBuilder(
                        ActionExecutionLogConstants.ActionIDs.SEND_ACTION_REQUEST,
                        "Request for external endpont " + request.getURI() + " for action is " +
                                "timed out. Retrying API call attempt " + attempts + " of " + retryCount + ".",
                        DiagnosticLog.ResultStatus.SUCCESS));
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
                .configParam("action id", action.getId())
                .configParam("action type", action.getType().getDisplayName())
                .configParam("action endpoint", action.getEndpoint().getUri())
                .configParam("action endpoint authentication type",
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
