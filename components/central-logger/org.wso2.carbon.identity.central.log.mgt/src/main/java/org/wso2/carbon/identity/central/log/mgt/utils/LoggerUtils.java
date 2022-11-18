/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.central.log.mgt.utils;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import static org.wso2.carbon.utils.CarbonUtils.isLegacyAuditLogsDisabled;

/**
 * Utils class of central logger.
 */
public class LoggerUtils {

    private static final Log log = LogFactory.getLog(LoggerUtils.class);
    private static final String CORRELATION_ID_MDC = "Correlation-ID";
    private static final String FLOW_ID_MDC = "Flow-ID";
    private static final String CLIENT_COMPONENT = "clientComponent";

   /**
    * Config value related to masking sensitive information from logs.
    */
    public static boolean isLogMaskingEnable;

    /**
     * @param initiatorId   Request initiator's id.
     * @param initiatorName Request initiator's name.
     * @param initiatorType Request initiator's type.
     * @param evenType      State changing event name.
     * @param targetId      Target resource's id.
     * @param targetName    Target resource's name.
     * @param targetType    Target resource type.
     * @param dataChange    Changing data.
     */
    public static void triggerAuditLogEvent(String initiatorId, String initiatorName, String initiatorType,
                                            String evenType, String targetId, String targetName, String targetType,
                                            String dataChange) {

        try {
            // Publish new audit logs only if the old audit log publishing is disabled.
            if (isLegacyAuditLogsDisabled()) {
                Map<String, Object> addAuditLogProperties = new HashMap<>();
                String id = UUID.randomUUID().toString();
                Instant recordedAt = parseDateTime(Instant.now().toString());
                String clientComponent = MDC.get(CLIENT_COMPONENT);
                String correlationId = MDC.get(CORRELATION_ID_MDC);
                AuditLog auditLog =
                        new AuditLog(id, recordedAt, clientComponent, correlationId, initiatorId, initiatorName,
                                initiatorType, evenType, targetId, targetName, targetType, dataChange);
                addAuditLogProperties.put(CarbonConstants.LogEventConstants.AUDIT_LOG, auditLog);

                IdentityEventService eventMgtService =
                        CentralLogMgtServiceComponentHolder.getInstance().getIdentityEventService();
                Event auditEvent = new Event(PUBLISH_AUDIT_LOG, addAuditLogProperties);
                eventMgtService.handleEvent(auditEvent);
            }
        } catch (IdentityEventException e) {
            String errorLog = "Error occurred when firing the event. Unable to audit the request.";
            log.error(errorLog, e);
        }
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
     */
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
     * Checks whether diagnostic logs are enabled.
     *
     * @return false if DiagnosticLogMode is NONE, true otherwise.
     */
    public static boolean isDiagnosticLogsEnabled() {

        int tenantId = IdentityTenantUtil.getTenantId(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        CarbonConstants.DiagnosticLogMode diagnosticLogMode = CarbonUtils.getDiagnosticLogMode(tenantId);

        if (CarbonConstants.DiagnosticLogMode.NONE.equals(diagnosticLogMode)) {
            return false;
        }
        return true;
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
}
