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

package org.wso2.carbon.identity.webhook.management.internal.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.utils.AuditLog;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * Webhook management V2 audit logger class.
 * This class is responsible for logging webhook management operations such as adding, updating, activating,
 * deactivating and deleting webhooks.
 */
public class WebhookManagementAuditLogger {

    /**
     * Print webhook audit log related to the operation.
     *
     * @param operation Operation associated with the state change.
     * @param webhook   Webhook to be logged.
     */
    public void printAuditLog(Operation operation, Webhook webhook) throws WebhookMgtException {

        JSONObject data = createAuditLogEntry(webhook);
        buildAuditLog(webhook.getId(), operation, data);
    }

    /**
     * Print webhook audit log related to the operation using webhook ID.
     *
     * @param operation Operation associated with the state change.
     * @param webhookId ID of the webhook to be logged.
     */
    public void printAuditLog(Operation operation, String webhookId) {

        JSONObject data = createAuditLogEntry(webhookId);
        buildAuditLog(webhookId, operation, data);
    }

    /**
     * Build audit log using the provided data.
     *
     * @param operation Operation to be logged.
     * @param data      data to be logged
     */
    private void buildAuditLog(String targetId, Operation operation, JSONObject data) {

        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                targetId,
                LoggerUtils.Target.Webhook.name(),
                operation.getLogAction()).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Create audit log data for the given webhook.
     *
     * @param webhook Webhook to be logged.
     * @return Audit log data as a JSONObject.
     * @throws WebhookMgtException If an error occurs while retrieving events subscribed.
     */
    private JSONObject createAuditLogEntry(Webhook webhook) throws WebhookMgtException {

        JSONObject data = new JSONObject();
        data.put(LogConstants.UUID_FIELD, webhook.getId() != null ? webhook.getId() : JSONObject.NULL);
        data.put(LogConstants.NAME_FIELD, webhook.getName() != null ? webhook.getName() : JSONObject.NULL);
        data.put(LogConstants.ENDPOINT_URI_FIELD,
                webhook.getEndpoint() != null ? webhook.getEndpoint() : JSONObject.NULL);
        data.put(LogConstants.SECRET_FIELD,
                webhook.getSecret() != null ? LoggerUtils.getMaskedContent(webhook.getSecret()) : JSONObject.NULL);
        data.put(LogConstants.EVENT_PROFILE_NAME_FIELD, webhook.getEventProfileName() != null ?
                webhook.getEventProfileName() : JSONObject.NULL);
        data.put(LogConstants.EVENT_PROFILE_URI_FIELD, webhook.getEventProfileUri() != null ?
                webhook.getEventProfileUri() : JSONObject.NULL);
        data.put(LogConstants.EVENT_PROFILE_VERSION_FIELD, webhook.getEventProfileVersion() != null ?
                webhook.getEventProfileVersion() : JSONObject.NULL);
        data.put(LogConstants.STATUS_FIELD, webhook.getStatus() != null ? webhook.getStatus() : JSONObject.NULL);
        data.put(LogConstants.CREATED_AT_FIELD,
                webhook.getCreatedAt() != null ? webhook.getCreatedAt() : JSONObject.NULL);
        data.put(LogConstants.UPDATED_AT_FIELD,
                webhook.getUpdatedAt() != null ? webhook.getUpdatedAt() : JSONObject.NULL);
        data.put(LogConstants.EVENTS_SUBSCRIBED_FIELD, webhook.getEventsSubscribed() != null ?
                webhook.getEventsSubscribed() : JSONObject.NULL);
        return data;
    }

    /**
     * Create audit log data with Webhook ID.
     *
     * @param webhookId ID of the webhook to be logged.
     * @return audit log data.
     */
    private JSONObject createAuditLogEntry(String webhookId) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.UUID_FIELD, webhookId);
        return data;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    private String getInitiatorId() {

        // Prefer IdentityContext → concrete actor userId if present
        IdentityContext idCtx = IdentityContext.getThreadLocalIdentityContext();
        if (idCtx.isUserActor() && StringUtils.isNotBlank(idCtx.getUserActor().getUserId())) {
            return idCtx.getUserActor().getUserId();
        }

        // Fallback to CarbonContext
        CarbonContext carbonCtx = CarbonContext.getThreadLocalCarbonContext();
        String username = carbonCtx.getUsername();
        String tenantDomain = carbonCtx.getTenantDomain();

        // If we still don't have a username, treat as system-initiated
        if (StringUtils.isBlank(username)) {
            return LoggerUtils.Initiator.System.name();
        }

        String initiator = null;
        if (StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }

        // Final fallback: mask the username for privacy
        return StringUtils.isNotBlank(initiator) ? initiator : LoggerUtils.getMaskedContent(username);
    }

    /**
     * Operations to be logged.
     */
    public enum Operation {
        ADD("add-webhook"),
        UPDATE("update-webhook"),
        DELETE("delete-webhook"),
        ACTIVATE("activate-webhook"),
        DEACTIVATE("deactivate-webhook");

        private final String logAction;

        Operation(String logAction) {

            this.logAction = logAction;
        }

        /**
         * Get the log action associated with the operation.
         *
         * @return Log action string.
         */
        public String getLogAction() {

            return this.logAction;
        }
    }

    /**
     * Webhook management related log constants.
     */
    private static class LogConstants {

        public static final String UUID_FIELD = "Id";
        public static final String ENDPOINT_URI_FIELD = "EndpointUri";
        public static final String NAME_FIELD = "Name";
        public static final String SECRET_FIELD = "Secret";
        public static final String EVENT_PROFILE_NAME_FIELD = "EventProfileName";
        public static final String EVENT_PROFILE_URI_FIELD = "EventProfileUri";
        public static final String EVENT_PROFILE_VERSION_FIELD = "EventProfileVersion";
        public static final String STATUS_FIELD = "Status";
        public static final String CREATED_AT_FIELD = "CreatedAt";
        public static final String UPDATED_AT_FIELD = "UpdatedAt";
        public static final String EVENTS_SUBSCRIBED_FIELD = "EventsSubscribed";
    }
}
