/*
 * Copyright (c) 2021, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.central.log.mgt.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.internal.CentralLogMgtServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.utils.AuditLog;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.DiagnosticLog;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ENABLE_LOG_MASKING;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_AUDIT_LOG;
import static org.wso2.carbon.identity.event.IdentityEventConstants.Event.PUBLISH_DIAGNOSTIC_LOG;

/**
 * Utils class of central logger.
 */
public class LoggerUtils {

    private static final Log log = LogFactory.getLog(LoggerUtils.class);
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private static final String FLOW_ID_MDC = "Flow-ID";
    private static final String TENANT_DOMAIN = "tenantDomain";
    public static final String ENABLE_V2_AUDIT_LOGS = "enableV2AuditLogs";

    /**
     * Defines the Initiators of the logs.
     */
    public enum Initiator {
        User, System
    }

    /**
     * Defines the Targets of the logs.
     */
    public enum Target {
        User, Role, Group, Application
    }

    /**
     * Config value related to masking sensitive information from logs.
     */
    public static boolean isLogMaskingEnable;

    /**
     * This method is used to trigger audit log event
     *
     * @param auditLogBuilder  Audit log builder
     * @param isLoggingEnabled Is new audit logging enabled in the component
     */
    public static void triggerAuditLogEvent(AuditLog.AuditLogBuilder auditLogBuilder, boolean isLoggingEnabled) {

        try {
            // Publish new audit logs only if the old audit log publishing is disabled.
            if (!isLoggingEnabled) {
                return;
            }
            IdentityEventService eventMgtService =
                    CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
            Event auditEvent = new Event(PUBLISH_AUDIT_LOG,
                    Map.of(CarbonConstants.LogEventConstants.AUDIT_LOG, auditLogBuilder.build()));
            eventMgtService.handleEvent(auditEvent);
        } catch (IdentityEventException e) {
            String errorLog = "Error occurred when firing the event. Unable to audit the request.";
            log.error(errorLog, e);
        }
    }

    /**
     * This method is used to trigger audit log event whence the new audit log publishing is enabled by default.
     *
     * @param auditLogBuilder  Audit log builder
     */
    public static void triggerAuditLogEvent(AuditLog.AuditLogBuilder auditLogBuilder) {

        triggerAuditLogEvent(auditLogBuilder, true);
    }

    /**
     * Trigger Diagnostic Log Event.
     *
     * @param componentId    Component ID.
     * @param input          Input parameters.
     * @param resultStatus   Result status.
     * @param resultMessage  Result message.
     * @param actionId       Action ID.
     * @param configurations System/application level configurations.
     * @Deprecated This method is deprecated. Use the method with {@link #triggerDiagnosticLogEvent(
     *DiagnosticLog.DiagnosticLogBuilder)}.
     */
    @Deprecated
    public static void triggerDiagnosticLogEvent(String componentId, Map<String, Object> input, String resultStatus,
                                                 String resultMessage, String actionId,
                                                 Map<String, Object> configurations) {

        try {
            Map<String, Object> diagnosticLogProperties = new HashMap<>();
            String id = UUID.randomUUID().toString();
            Instant recordedAt = parseDateTime(Instant.now().toString());
            String requestId = MDC.get(CORRELATION_ID_MDC);
            String flowId = MDC.get(FLOW_ID_MDC);
            DiagnosticLog diagnosticLog = new DiagnosticLog(id, recordedAt, requestId, flowId, resultStatus,
                    resultMessage, actionId, componentId, input, configurations);
            IdentityEventService eventMgtService =
                    CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
            diagnosticLogProperties.put(CarbonConstants.LogEventConstants.DIAGNOSTIC_LOG, diagnosticLog);
            int tenantId =
                    IdentityTenantUtil.getTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            diagnosticLogProperties.put(CarbonConstants.LogEventConstants.TENANT_ID, tenantId);
            Event diagnosticLogEvent = new Event(PUBLISH_DIAGNOSTIC_LOG, diagnosticLogProperties);
            eventMgtService.handleEvent(diagnosticLogEvent);
        } catch (IdentityEventException e) {
            String errorLog = "Error occurred when firing the diagnostic log event.";
            log.error(errorLog, e);
        }
    }

    /**
     * Trigger Diagnostic Log Event.
     *
     * @param diagnosticLogBuilder Diagnostic log builder.
     */
    public static void triggerDiagnosticLogEvent(DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder) {

        try {
            Map<String, Object> diagnosticLogProperties = new HashMap<>();
            DiagnosticLog diagnosticLog = diagnosticLogBuilder.build();
            IdentityEventService eventMgtService =
                    CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
            diagnosticLogProperties.put(CarbonConstants.LogEventConstants.DIAGNOSTIC_LOG, diagnosticLog);
            diagnosticLogProperties.put(CarbonConstants.LogEventConstants.TENANT_ID, resolveTenantId());
            Event diagnosticLogEvent = new Event(PUBLISH_DIAGNOSTIC_LOG, diagnosticLogProperties);
            eventMgtService.handleEvent(diagnosticLogEvent);
        } catch (IdentityEventException e) {
            String errorLog = "Error occurred when firing the diagnostic log event.";
            log.error(errorLog, e);
        }
    }

    /**
     * Resolves the tenant id
     *
     * @return tenant id
     */
    public static int resolveTenantId() {

        String tenantDomain = MDC.get(TENANT_DOMAIN);
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        return IdentityTenantUtil.getTenantId(tenantDomain);
    }

    /**
     * Checks whether diagnostic logs are enabled.
     *
     * @return false if DiagnosticLogMode is NONE, true otherwise.
     */
    public static boolean isDiagnosticLogsEnabled() {

        CarbonConstants.DiagnosticLogMode diagnosticLogMode = CarbonUtils.getDiagnosticLogMode(resolveTenantId());

        return !CarbonConstants.DiagnosticLogMode.NONE.equals(diagnosticLogMode);
    }

    /**
     * Parse Date Time into UTC format.
     *
     * @param dateTimeString Date time.
     * @return Date time in ISO_OFFSET_DATE_TIME format.
     */
    private static Instant parseDateTime(String dateTimeString) {

        Instant localDateTime = null;
        if (StringUtils.isEmpty(dateTimeString)) {
            return null;
        }
        try {
            localDateTime = LocalDateTime.parse(dateTimeString).toInstant(ZoneOffset.UTC);
        } catch (DateTimeException e) {
            try {
                return OffsetDateTime.parse(dateTimeString).toInstant();
            } catch (DateTimeException dte) {
                log.error("Error in parsing date time. Date time should adhere to ISO_OFFSET_DATE_TIME format", e);
            }
        }
        return localDateTime;
    }

    /**
     * Get the log masking config value from config file.
     */
    public static void getLogMaskingConfigValue() {

        isLogMaskingEnable = Boolean.parseBoolean(IdentityUtil.getProperty(ENABLE_LOG_MASKING));
    }

    /**
     * Util function to mask content.
     *
     * @param content Content that needs to be masked.
     * @return masked content.
     */
    public static String getMaskedContent(String content) {

        if (StringUtils.isNotEmpty(content)) {
            return LogConstants.LOG_MASKING_PATTERN.matcher(content).replaceAll(LogConstants.MASKING_CHARACTER);
        }
        return content;
    }

    /**
     * Util function to mask claim values except userid claim.
     *
     * @param claims Map of user claims.
     * @return masked map of user claims.
     */
    public static Map<String, String> getMaskedClaimsMap(Map<String, String> claims) {

        Map<String, String> maskedClaims = new HashMap<>();
        if (MapUtils.isNotEmpty(claims)) {
            for (Map.Entry<String, String> entry : claims.entrySet()) {
                if (LogConstants.USER_ID_CLAIM_URI.equals(entry.getKey())) {
                    maskedClaims.put(entry.getKey(), entry.getValue());
                } else {
                    maskedClaims.put(entry.getKey(), getMaskedContent(entry.getValue()));
                }
            }
        }
        return maskedClaims;
    }

    /**
     * Util function to mask claim value except userId claim when there is only one claim.
     *
     * @param claimURI   Claim URI.
     * @param claimValue Claim value that will be masked.
     * @return masked claim value.
     */
    public static String getMaskedClaimValue(String claimURI, String claimValue) {

        if (LogConstants.USER_ID_CLAIM_URI.equals(claimURI)) {
            return claimValue;
        }
        return getMaskedContent(claimValue);
    }

    /**
     * Util function to mask array of values.
     *
     * @param values Array of values need to be masked.
     * @return masked values of array.
     */
    public static String[] getMaskedArraysOfValues(String[] values) {

        String[] maskedArraysOfValues = new String[values.length];
        for (int index = 0; index < values.length; index++) {
            maskedArraysOfValues[index] = LoggerUtils.getMaskedContent(values[index]);
        }
        return maskedArraysOfValues;
    }

    /**
     * Get the masked username if the log masking is enabled.
     *
     * @param errorMessage Error message.
     * @param userName     Username.
     * @return Masked error message.
     */
    public static String getSanitizedErrorMessage(String errorMessage, String userName) {

        if (LoggerUtils.isLogMaskingEnable && errorMessage.contains(userName)) {
            return errorMessage.replace(userName, LoggerUtils.getMaskedContent(userName));
        }
        return errorMessage;
    }

    /**
     * Check if the V2 audit log is enabled.
     *
     * @return if the V2 Audit logs is enabled.
     */
    public static boolean isEnableV2AuditLogs() {

        return Boolean.parseBoolean(System.getProperty(ENABLE_V2_AUDIT_LOGS));
    }

    /**
     * Get the data in a Map from the JSONObject.
     *
     * @param jsonObject jsonObject that has the data to be returned.
     * @return Map of String and Object.
     */
    public static Map<String, Object> jsonObjectToMap(JSONObject jsonObject) {

        Gson gson = new Gson();
        return gson.fromJson(jsonObject.toString(), new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    /**
     * Get the Initiator type.
     *
     * @param initiator Initiator for the logs.
     * @return Type of the initiator.
     */
    public static String getInitiatorType(String initiator) {

        if (initiator.equals(LoggerUtils.Initiator.System.name())) {
            return LoggerUtils.Initiator.System.name();
        }
        return LoggerUtils.Initiator.User.toString();
    }
}
