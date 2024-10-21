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

package org.wso2.carbon.identity.action.management.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.AuditLog;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * Action management V2 audit logger class.
 */
public class ActionManagementAuditLogger {

    /**
     * Print action audit log related to the operation.
     *
     * @param operation Operation associated with the state change.
     * @param action    Action object to be logged.
     */
    public void printAuditLog(Operation operation, Action action) {

        if (!LoggerUtils.isEnableV2AuditLogs()) {
            return;
        }
        JSONObject data = createAuditLogEntry(action);
        buildAuditLog(operation, data);
    }

    /**
     * Print action audit log related to the operation by action ID.
     *
     * @param operation Operation associated with the state change.
     * @param actionId  ID of the action to be logged.
     * @param action    Action object to be logged.
     */
    public void printAuditLog(Operation operation, String actionId, Action action) {

        if (!LoggerUtils.isEnableV2AuditLogs()) {
            return;
        }
        JSONObject data = createAuditLogEntry(actionId, action);
        buildAuditLog(operation, data);
    }

    /**
     * Print action audit log related to the operation by the action type and action ID.
     *
     * @param operation  Operation associated with the state change.
     * @param actionType Type of the action to be logged.
     * @param actionId   ID of the action to be logged.
     */
    public void printAuditLog(Operation operation, String actionType, String actionId) {

        if (!LoggerUtils.isEnableV2AuditLogs()) {
            return;
        }
        JSONObject data = createAuditLogEntry(actionType, actionId);
        buildAuditLog(operation, data);
    }

    /**
     * Build audit log using the provided data.
     *
     * @param operation Operation to be logged.
     * @param data      data to be logged
     */
    private void buildAuditLog(Operation operation, JSONObject data) {

        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                operation.getLogAction()).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Create audit log data with action.
     * This method expects all the action fields to be non-null/non-empty.
     *
     * @param action Action to be logged.
     * @return audit log data.
     */
    private JSONObject createAuditLogEntry(Action action) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ACTION_TYPE_FIELD, action.getType());
        data.put(LogConstants.ACTION_ID_FIELD, action.getId());
        data.put(LogConstants.ACTION_NAME_FIELD, action.getName());
        data.put(LogConstants.ACTION_DESCRIPTION_FIELD, action.getDescription());
        data.put(LogConstants.ACTION_STATUS_FIELD, action.getStatus());
        data.put(LogConstants.ENDPOINT_CONFIG_FIELD, getAllEndpointData(action.getEndpoint()));
        return data;
    }

    /**
     * Create audit log data with action and ID.
     * This method expects null/empty action fields.
     *
     * @param actionId ID of the action to be logged.
     * @param action   Action to be logged.
     * @return audit log data.
     */
    private JSONObject createAuditLogEntry(String actionId, Action action) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ACTION_TYPE_FIELD, action.getType() != null ? action.getType() : JSONObject.NULL);
        data.put(LogConstants.ACTION_ID_FIELD, actionId);
        data.put(LogConstants.ACTION_NAME_FIELD, action.getName() != null ? action.getName() : JSONObject.NULL);
        data.put(LogConstants.ACTION_DESCRIPTION_FIELD,
                action.getDescription() != null ? action.getDescription() : JSONObject.NULL);
        data.put(LogConstants.ACTION_STATUS_FIELD, action.getStatus() != null ? action.getStatus() : JSONObject.NULL);
        if (action.getEndpoint() != null) {
            data.put(LogConstants.ENDPOINT_CONFIG_FIELD, getEndpointData(action.getEndpoint()));
        }
        return data;
    }

    /**
     * Create audit log data with action type and ID.
     *
     * @param actionType Type of action to be logged.
     * @param actionId   ID of action to be logged.
     * @return audit log data.
     */
    private JSONObject createAuditLogEntry(String actionType, String actionId) {

        JSONObject data = new JSONObject();
        data.put(LogConstants.ACTION_TYPE_FIELD, actionType);
        data.put(LogConstants.ACTION_ID_FIELD, actionId);
        return data;
    }

    /**
     * Retrieve complete endpoint configuration data to be logged.
     *
     * @param endpointConfig Endpoint data to be logged.
     * @return endpoint config data.
     */
    private JSONObject getAllEndpointData(EndpointConfig endpointConfig) {

        JSONObject endpointData = new JSONObject();
        endpointData.put(LogConstants.ENDPOINT_URI_FIELD, endpointConfig.getUri());
        Authentication authentication = endpointConfig.getAuthentication();
        endpointData.put(LogConstants.AUTHENTICATION_SCHEME_FIELD, authentication.getType().getName());
        switch (authentication.getType()) {
            case BASIC:
                endpointData.put(LogConstants.USERNAME_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.USERNAME).getValue()));
                endpointData.put(LogConstants.PASSWORD_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.PASSWORD).getValue()));
                break;
            case BEARER:
                endpointData.put(LogConstants.ACCESS_TOKEN_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.ACCESS_TOKEN).getValue()));
                break;
            case API_KEY:
                endpointData.put(LogConstants.API_KEY_HEADER_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.HEADER).getValue()));
                endpointData.put(LogConstants.API_KEY_VALUE_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.VALUE).getValue()));
                break;
        }
        return endpointData;
    }

    /**
     * Retrieve endpoint configuration data to be logged.
     * This method expects null/empty endpoint config fields.
     *
     * @param endpointConfig Endpoint data to be logged.
     * @return endpoint config data.
     */
    private JSONObject getEndpointData(EndpointConfig endpointConfig) {

        JSONObject endpointData = new JSONObject();
        endpointData.put(LogConstants.ENDPOINT_URI_FIELD, endpointConfig.getUri() != null ? endpointConfig.getUri() :
                JSONObject.NULL);
        if (endpointConfig.getAuthentication() != null) {
            Authentication authentication = endpointConfig.getAuthentication();
            endpointData.put(LogConstants.AUTHENTICATION_SCHEME_FIELD, authentication.getType());
            switch (authentication.getType()) {
                case BASIC:
                    endpointData.put(LogConstants.USERNAME_FIELD, LoggerUtils.getMaskedContent(
                            authentication.getProperty(Authentication.Property.USERNAME) != null
                                    ? authentication.getProperty(Authentication.Property.USERNAME).getValue() : ""));
                    endpointData.put(LogConstants.PASSWORD_FIELD, LoggerUtils.getMaskedContent(
                            authentication.getProperty(Authentication.Property.PASSWORD) != null
                                    ? authentication.getProperty(Authentication.Property.PASSWORD).getValue() : ""));
                    break;
                case BEARER:
                    endpointData.put(LogConstants.ACCESS_TOKEN_FIELD, LoggerUtils.getMaskedContent(
                            authentication.getProperty(Authentication.Property.ACCESS_TOKEN) != null
                                    ? authentication.getProperty(Authentication.Property.ACCESS_TOKEN).
                                    getValue() : ""));
                    break;
                case API_KEY:
                    endpointData.put(LogConstants.API_KEY_HEADER_FIELD, LoggerUtils.getMaskedContent(
                            authentication.getProperty(Authentication.Property.HEADER) != null
                                    ? authentication.getProperty(Authentication.Property.HEADER).getValue() : ""));
                    endpointData.put(LogConstants.API_KEY_VALUE_FIELD, LoggerUtils.getMaskedContent(
                            authentication.getProperty(Authentication.Property.VALUE) != null
                                    ? authentication.getProperty(Authentication.Property.VALUE).getValue() : ""));
                    break;
            }
        }
        return endpointData;
    }

    /**
     * To get the current user, who is doing the current task.
     *
     * @return current logged-in user.
     */
    private String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(user)) {
            user = UserCoreUtil
                    .addTenantDomainToEntry(user, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    private String getInitiatorId() {

        String initiator = null;
        String username = MultitenantUtils.getTenantAwareUsername(getUser());
        String tenantDomain = MultitenantUtils.getTenantDomain(getUser());
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        if (StringUtils.isBlank(initiator)) {
            if (username.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                // If the initiator is wso2.system, the username need not be masked.
                return LoggerUtils.Initiator.System.name();
            }
            initiator = LoggerUtils.getMaskedContent(getUser());
        }
        return initiator;
    }

    /**
     * Operations to be logged.
     */
    public enum Operation {
        ADD("add-action"),
        UPDATE("update-action"),
        DELETE("delete-action"),
        ACTIVATE("activate-action"),
        DEACTIVATE("deactivate-action");

        private final String logAction;

        Operation(String logAction) {

            this.logAction = logAction;
        }

        public String getLogAction() {

            return this.logAction;
        }
    }

    /**
     * Action management related log constants.
     */
    private static class LogConstants {

        public static final String ACTION_TYPE_FIELD = "ActionType";
        public static final String ACTION_NAME_FIELD = "ActionName";
        public static final String ACTION_ID_FIELD = "ActionId";
        public static final String ACTION_DESCRIPTION_FIELD = "ActionDescription";
        public static final String ACTION_STATUS_FIELD = "ActionStatus";
        public static final String ENDPOINT_CONFIG_FIELD = "EndpointConfiguration";
        public static final String ENDPOINT_URI_FIELD = "EndpointUri";
        public static final String AUTHENTICATION_SCHEME_FIELD = "AuthenticationScheme";
        public static final String USERNAME_FIELD = "Username";
        public static final String PASSWORD_FIELD = "Password";
        public static final String ACCESS_TOKEN_FIELD = "AccessToken";
        public static final String API_KEY_HEADER_FIELD = "ApiKeyHeader";
        public static final String API_KEY_VALUE_FIELD = "ApiKeyValue";
    }
}


