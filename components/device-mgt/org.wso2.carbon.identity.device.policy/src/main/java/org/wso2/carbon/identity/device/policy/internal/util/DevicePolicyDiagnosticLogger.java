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

package org.wso2.carbon.identity.device.policy.internal.util;

import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.DiagnosticLog;

/**
 * Diagnostic logger for the device policy runtime: device token validation, integrity enrichment
 * and adaptive-auth policy evaluation. Every method is gated on {@code isDiagnosticLogsEnabled()}
 * so nothing is emitted unless diagnostic logging is turned on.
 */
public class DevicePolicyDiagnosticLogger {

    private static final String COMPONENT_ID = "device-policy";
    private static final String UNKNOWN = "unknown";

    private static final String ACTION_VALIDATE_TOKEN = "validate-device-token";
    private static final String ACTION_ENRICH_INTEGRITY = "enrich-device-integrity";
    private static final String ACTION_EVALUATE_POLICY = "evaluate-device-policy";

    private static final String PARAM_DEVICE_ID = "deviceId";
    private static final String PARAM_APP_ID = "applicationId";
    private static final String PARAM_POLICY_NAME = "policyName";
    private static final String PARAM_REASON = "reason";

    /**
     * Log a failed device token validation, capturing the specific rejection reason.
     *
     * @param deviceId Device ID the token claimed, or null if not yet resolved.
     * @param reason   Reason the token was rejected.
     */
    public void logTokenValidationFailure(String deviceId, String reason) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_VALIDATE_TOKEN,
                "Device token validation failed.", DiagnosticLog.ResultStatus.FAILED)
                .inputParam(PARAM_DEVICE_ID, deviceId != null ? deviceId : UNKNOWN)
                .inputParam(PARAM_REASON, reason);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log a successful device token validation.
     *
     * @param deviceId Device ID whose token was verified.
     */
    public void logTokenValidationSuccess(String deviceId) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_VALIDATE_TOKEN,
                "Device token verified successfully.", DiagnosticLog.ResultStatus.SUCCESS)
                .inputParam(PARAM_DEVICE_ID, deviceId != null ? deviceId : UNKNOWN);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log a failure to enrich device data with client attestation / integrity verdicts.
     *
     * @param appId  Application ID the attestation was scoped to.
     * @param reason Reason enrichment failed.
     */
    public void logIntegrityEnrichmentFailure(String appId, String reason) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_ENRICH_INTEGRITY,
                "Device integrity enrichment failed; treating device as integrity failed.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam(PARAM_APP_ID, appId != null ? appId : UNKNOWN)
                .inputParam(PARAM_REASON, reason);
        LoggerUtils.triggerDiagnosticLogEvent(builder);
    }

    /**
     * Log that no verified device data was present on the authentication context for a policy check.
     *
     * @param policyName Policy that could not be evaluated.
     */
    public void logMissingDeviceData(String policyName) {

        if (!LoggerUtils.isDiagnosticLogsEnabled()) {
            return;
        }
        DiagnosticLog.DiagnosticLogBuilder builder = newBuilder(ACTION_EVALUATE_POLICY,
                "No verified device data on the authentication context; device policy cannot be evaluated.",
                DiagnosticLog.ResultStatus.FAILED)
                .inputParam(PARAM_POLICY_NAME, policyName != null ? policyName : UNKNOWN);
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
