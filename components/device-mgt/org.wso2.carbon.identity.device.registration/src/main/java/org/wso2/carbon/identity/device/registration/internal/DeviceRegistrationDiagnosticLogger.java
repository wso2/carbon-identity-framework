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

package org.wso2.carbon.identity.device.registration.internal;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

/**
 * Diagnostic logger for the device registration flow executor. Records the notable transitions of
 * the two-phase challenge-response registration so administrators can trace why a registration
 * succeeded or failed. Every method is gated on {@code isDiagnosticLogsEnabled()}.
 */
public class DeviceRegistrationDiagnosticLogger {

    private static final String COMPONENT_ID = "device-registration";

    private static final String ACTION_INITIATE = "initiate-device-registration";
    private static final String ACTION_COMPLETE = "complete-device-registration";
    private static final String ACTION_EVALUATE_POLICY = "evaluate-device-policy";

    private static final String PARAM_REGISTRATION_ID = "registrationId";
    private static final String PARAM_POLICY_NAME = "policyName";
    private static final String PARAM_FAILED_FIELDS = "failedFields";
    private static final String PARAM_REASON = "reason";

    /**
     * Log that a device registration challenge was issued.
     *
     * @param registrationId Registration ID generated for this attempt.
     */
    public void logRegistrationInitiated(String registrationId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_INITIATE,
                "Device registration challenge issued.", DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(PARAM_REGISTRATION_ID, registrationId);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log that a device registration was verified and persisted.
     *
     * @param registrationId Registration ID that completed.
     */
    public void logRegistrationCompleted(String registrationId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_COMPLETE,
                "Device registration verified and persisted.", DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(PARAM_REGISTRATION_ID, registrationId);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log the outcome of a device policy compliance check during registration.
     *
     * @param policyName   Policy evaluated.
     * @param compliant    Whether the device satisfied the policy.
     * @param failedFields Comma-separated failed fields when not compliant; ignored when compliant.
     */
    public void logPolicyEvaluation(String policyName, boolean compliant, String failedFields) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_EVALUATE_POLICY,
                compliant ? "Device passed the configured device policy."
                        : "Device did not satisfy the configured device policy.",
                compliant ? DiagnosticLog.ResultStatus.SUCCESS : DiagnosticLog.ResultStatus.FAILED)
                .inputParam(PARAM_POLICY_NAME, policyName);
        if (!compliant && failedFields != null) {
            builder.inputParam(PARAM_FAILED_FIELDS, failedFields);
        }
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log a failure encountered during the device registration flow.
     *
     * @param reason Reason the registration step failed.
     */
    public void logRegistrationFailure(String reason) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_COMPLETE,
                "Device registration flow failed.", DiagnosticLog.ResultStatus.FAILED)
                .inputParam(PARAM_REASON, reason);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    private DiagnosticLog.DiagnosticLogBuilder newBuilder(String actionId, String message,
                                                          DiagnosticLog.ResultStatus status) {

        DiagnosticLog.DiagnosticLogBuilder builder = new DiagnosticLog.DiagnosticLogBuilder(COMPONENT_ID, actionId);
        builder.resultMessage(message)
                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                .resultStatus(status);
        return builder;
    }
}
