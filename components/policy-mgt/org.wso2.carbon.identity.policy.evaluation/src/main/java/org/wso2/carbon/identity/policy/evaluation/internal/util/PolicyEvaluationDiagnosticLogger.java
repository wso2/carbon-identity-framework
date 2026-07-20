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

package org.wso2.carbon.identity.policy.evaluation.internal.util;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.policy.evaluation.api.model.PolicyEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.ResourceEvaluationResult;
import org.wso2.carbon.identity.policy.evaluation.api.model.RuleResourceEvaluationResult;
import org.wso2.carbon.identity.policy.management.api.model.PolicyResource;
import org.wso2.carbon.utils.DiagnosticLog;

/**
 * Utility class for generating and printing diagnostic logs related to policy evaluation.
 * Every method is gated by {@link LoggerUtils#isDiagnosticLogsEnabled()} so that diagnostic
 * logging is a no-op unless troubleshooting has been enabled for the request.
 */
public class PolicyEvaluationDiagnosticLogger {

    /**
     * Logs that a policy evaluation has been initiated.
     *
     * @param policyId Policy ID being evaluated.
     * @param target   Resource target selector for the evaluation.
     */
    public void logEvaluationInitiated(String policyId, String target) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_POLICY, "Policy evaluation initiated.",
                DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(LogConstants.POLICY_ID, policyId)
                .inputParam(LogConstants.TARGET, target);
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs that the policy to be evaluated could not be found.
     *
     * @param policyId Policy ID that was looked up.
     */
    public void logPolicyNotFound(String policyId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_POLICY, "Policy not found for evaluation.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam(LogConstants.POLICY_ID, policyId);
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs that no target was specified for the evaluation, so the policy is treated as compliant.
     *
     * @param policyId Policy ID being evaluated.
     */
    public void logNoTargetSpecified(String policyId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_POLICY, "No target specified — policy treated as compliant.",
                DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(LogConstants.POLICY_ID, policyId);
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs that no resources matched the given target, so the policy is treated as compliant.
     *
     * @param policyId Policy ID being evaluated.
     * @param target   Resource target selector that had no matching resources.
     */
    public void logNoMatchingResources(String policyId, String target) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_POLICY, "No resources for target — treated as compliant.",
                DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(LogConstants.POLICY_ID, policyId)
                .inputParam(LogConstants.TARGET, target);
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs that no evaluator is registered for a resource's type.
     *
     * @param resource Policy resource whose type has no registered evaluator.
     */
    public void logNoEvaluatorForResourceType(PolicyResource resource) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_RESOURCE, "No evaluator registered for resource type.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam(LogConstants.RESOURCE_TYPE, resource.getResourceType())
                .inputParam(LogConstants.TARGET, resource.getTarget());
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs the outcome of evaluating a single resource. For rule resources, also logs the fields
     * that caused the rule to fail when the resource was not satisfied.
     *
     * @param result Outcome of evaluating a single policy resource.
     */
    public void logResourceEvaluationResult(ResourceEvaluationResult result) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_RESOURCE, "Resource evaluated.",
                result.isSatisfied() ? DiagnosticLog.ResultStatus.SUCCESS : DiagnosticLog.ResultStatus.FAILED)
                .inputParam(LogConstants.RESOURCE_TYPE, result.getResourceType().name())
                .inputParam(LogConstants.TARGET, result.getResource().getTarget())
                .inputParam(LogConstants.RESOURCE_ID, result.getResourceId())
                .inputParam(LogConstants.SATISFIED, result.isSatisfied());
        if (result instanceof RuleResourceEvaluationResult) {
            diagnosticLogBuilder.inputParam(
                    LogConstants.FAILED_FIELDS, ((RuleResourceEvaluationResult) result).getFailedFields());
        }
        triggerLogEvent(diagnosticLogBuilder);
    }

    /**
     * Logs the final outcome of a policy evaluation.
     *
     * @param policyId Policy ID that was evaluated.
     * @param target   Resource target selector for the evaluation.
     * @param result   Aggregate result of the evaluation.
     */
    public void logEvaluationCompleted(String policyId, String target, PolicyEvaluationResult result) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }

        DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = initializeDiagnosticLogBuilder(
                LogConstants.EVALUATE_POLICY, "Policy evaluation completed.",
                result.isSatisfied() ? DiagnosticLog.ResultStatus.SUCCESS : DiagnosticLog.ResultStatus.FAILED)
                .inputParam(LogConstants.POLICY_ID, policyId)
                .inputParam(LogConstants.TARGET, target)
                .inputParam(LogConstants.SATISFIED, result.isSatisfied());
        triggerLogEvent(diagnosticLogBuilder);
    }

    private DiagnosticLog.DiagnosticLogBuilder initializeDiagnosticLogBuilder(
            String actionId, String message, DiagnosticLog.ResultStatus status) {

        return new DiagnosticLog.DiagnosticLogBuilder(LogConstants.COMPONENT_ID, actionId)
                .resultMessage(message)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(status);
    }

    private void triggerLogEvent(DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder) {

        LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
    }

    /**
     * Policy evaluation diagnostic log constants.
     */
    private static final class LogConstants {

        private static final String COMPONENT_ID = "policy-evaluation";

        private static final String EVALUATE_POLICY = "evaluate-policy";
        private static final String EVALUATE_RESOURCE = "evaluate-resource";

        private static final String POLICY_ID = "policyId";
        private static final String TARGET = "target";
        private static final String RESOURCE_TYPE = "resourceType";
        private static final String RESOURCE_ID = "resourceId";
        private static final String SATISFIED = "satisfied";
        private static final String FAILED_FIELDS = "failedFields";

        private LogConstants() {

        }
    }
}
