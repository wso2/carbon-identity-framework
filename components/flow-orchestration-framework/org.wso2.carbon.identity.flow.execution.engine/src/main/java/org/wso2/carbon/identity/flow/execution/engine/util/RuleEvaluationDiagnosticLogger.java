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

package org.wso2.carbon.identity.flow.execution.engine.util;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

/**
 * Diagnostic logs for decision steps: which branch a decision took, or why it could not take one. Only ids and names
 * are logged, never the values a condition compared.
 */
public class RuleEvaluationDiagnosticLogger {

    private static final String COMPONENT_ID = "flow-execution";
    private static final String RULE_EVALUATION = "rule-evaluation";

    private static final String FLOW_TYPE = "flowType";
    private static final String STEP_ID = "stepId";
    private static final String BRANCH_ID = "branchId";
    private static final String BRANCH_NAME = "branchName";
    private static final String NEXT_ID = "nextId";

    /**
     * Logs that a branch's condition held, so the decision took it.
     *
     * @param flowType   Flow type.
     * @param stepId     Id of the decision step.
     * @param branchId   Id of the branch taken.
     * @param branchName Name of the branch taken.
     * @param nextId     Id of the step the branch leads to.
     */
    public void logBranchSelected(String flowType, String stepId, String branchId, String branchName,
                                  String nextId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        triggerLogEvent(builder("Branch rule matched.",
                DiagnosticLog.ResultStatus.SUCCESS, flowType, stepId)
                .inputParam(BRANCH_ID, branchId)
                .inputParam(BRANCH_NAME, branchName)
                .inputParam(NEXT_ID, nextId));
    }

    /**
     * Logs that no branch's condition held, so the decision took its default branch.
     *
     * @param flowType   Flow type.
     * @param stepId     Id of the decision step.
     * @param branchId   Id of the default branch.
     * @param branchName Name of the default branch.
     */
    public void logDefaultBranchSelected(String flowType, String stepId, String branchId, String branchName) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        triggerLogEvent(builder("No branch rule matched. Continued on the default branch.",
                DiagnosticLog.ResultStatus.SUCCESS, flowType, stepId)
                .inputParam(BRANCH_ID, branchId)
                .inputParam(BRANCH_NAME, branchName));
    }

    /**
     * Logs that a branch's condition could not be evaluated, which fails the flow.
     *
     * @param flowType   Flow type.
     * @param stepId     Id of the decision step.
     * @param branchName Name of the branch whose condition could not be evaluated.
     * @param reason     Why the condition could not be evaluated.
     */
    public void logBranchEvaluationFailed(String flowType, String stepId, String branchName, String reason) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        triggerLogEvent(builder("Failed to evaluate the branch rule. Reason: " + reason,
                DiagnosticLog.ResultStatus.FAILED, flowType, stepId)
                .inputParam(BRANCH_NAME, branchName));
    }

    /**
     * Logs that no branch could be taken, as no condition held and the decision step has no default branch.
     *
     * @param flowType Flow type.
     * @param stepId   Id of the decision step.
     */
    public void logNoBranchSelected(String flowType, String stepId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        triggerLogEvent(builder("No branch rule matched and no default branch is configured.",
                DiagnosticLog.ResultStatus.FAILED, flowType, stepId));
    }

    /**
     * Logs that a decision step was reached while conditional branching is disabled on the server.
     *
     * @param flowType Flow type.
     * @param stepId   Id of the decision step.
     */
    public void logConditionalBranchingDisabled(String flowType, String stepId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        triggerLogEvent(builder("Conditional branching is not enabled.",
                DiagnosticLog.ResultStatus.FAILED, flowType, stepId));
    }

    private static DiagnosticLog.DiagnosticLogBuilder builder(String resultMessage,
                                                              DiagnosticLog.ResultStatus resultStatus,
                                                              String flowType, String stepId) {

        return new DiagnosticLog.DiagnosticLogBuilder(COMPONENT_ID, RULE_EVALUATION)
                .resultMessage(resultMessage)
                .resultStatus(resultStatus)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .inputParam(FLOW_TYPE, flowType)
                .inputParam(STEP_ID, stepId);
    }

    private static void triggerLogEvent(DiagnosticLog.DiagnosticLogBuilder builder) {

        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }
}
