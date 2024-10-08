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

package org.wso2.carbon.identity.action.management.listener;

import org.json.JSONObject;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.utils.AuditLog;

import static org.wso2.carbon.identity.action.management.listener.utils.ListenerUtils.getInitiatorId;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACCESS_TOKEN_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTION_DESCRIPTION_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTION_ID_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTION_NAME_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTION_STATUS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTION_TYPE_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ACTIVATE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ADD_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.API_KEY_HEADER_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.API_KEY_VALUE_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.DEACTIVATE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.DELETE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ENDPOINT_AUTHENTICATION_SCHEME_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ENDPOINT_CONFIG_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.ENDPOINT_URI_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.PASSWORD_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.UPDATE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.ActionManagement.USERNAME_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;

/**
 * This v2 audit logger logs the Action Management related operations.
 */
public class ActionManagementV2AuditLogger extends AbstractActionManagementListener {

    @Override
    public int getDefaultOrderId() {

        return 1;
    }

    @Override
    public boolean isEnable() {

        // By default audit logs v2 is enabled.
        return true;
    }

    // TODO: check params

    /**
     * Record an audit log entry when a new action is added.
     *
     * @param actionType   Type of action to be added.
     * @param action       Added action model.
     * @param tenantDomain Tenant domain in which the added action exist.
     */
    @Override
    public void postAddAction(String actionType, Action action, String tenantDomain) {

        if (!isEnable()) {
            return;
        }
        JSONObject data = generateAuditLogDataForAddAction(action);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                ADD_ACTION).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Record an audit log entry when the action is updated.
     *
     * @param actionType   Type of the action updated.
     * @param actionId     ID of the action updated.
     * @param action       Updated action model.
     * @param tenantDomain Tenant domain in which the updated action existed.
     */
    @Override
    public void postUpdateAction(String actionType, String actionId, Action action, String tenantDomain) {

        if (!isEnable()) {
            return;
        }
        JSONObject data = generateAuditLogDataForUpdateAction(action);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                UPDATE_ACTION).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Record an audit log entry when the action is deleted.
     *
     * @param actionType   Type of the action deleted.
     * @param actionId     ID of the delete action.
     * @param tenantDomain Tenant domain in which the deleted action existed.
     */
    @Override
    public void postDeleteAction(String actionType, String actionId, String tenantDomain) {

        if (!isEnable()) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put(ACTION_TYPE_FIELD, actionType);
        data.put(ACTION_ID_FIELD, actionId);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                DELETE_ACTION).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Record an audit log entry when the action is activated.
     *
     * @param actionType   Type of the action activated.
     * @param actionId     ID of the action activated.
     * @param tenantDomain Tenant domain in which the action to be activated exist.
     */
    @Override
    public void postActivateAction(String actionType, String actionId, String tenantDomain) {

        if (!isEnable()) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put(ACTION_TYPE_FIELD, actionType);
        data.put(ACTION_ID_FIELD, actionId);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                ACTIVATE_ACTION).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Record an audit log entry when the action is deactivated.
     *
     * @param actionType   Type of action deactivated.
     * @param actionId     ID of the action deactivated.
     * @param tenantDomain Tenant domain in which the action to be deactivated exist.
     */
    @Override
    public void postDeactivateAction(String actionType, String actionId, String tenantDomain) {

        if (!isEnable()) {
            return;
        }
        JSONObject data = new JSONObject();
        data.put(ACTION_TYPE_FIELD, actionType);
        data.put(ACTION_ID_FIELD, actionId);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()),
                LoggerUtils.Initiator.System.name(),
                LoggerUtils.Target.Action.name(),
                DEACTIVATE_ACTION).
                data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
    }

    /**
     * Generate audit log data for post add action.
     *
     * @param action Added action model.
     * @return audit log data.
     */
    private JSONObject generateAuditLogDataForAddAction(Action action) {

        JSONObject data = new JSONObject();
        data.put(ACTION_TYPE_FIELD, action.getType());
        data.put(ACTION_ID_FIELD, action.getId());
        data.put(ACTION_NAME_FIELD, action.getName());
        data.put(ACTION_DESCRIPTION_FIELD, action.getDescription());
        data.put(ACTION_STATUS_FIELD, action.getStatus());

        JSONObject endpointData = new JSONObject();
        endpointData.put(ENDPOINT_URI_FIELD, action.getEndpoint().getUri());
        Authentication authentication = action.getEndpoint().getAuthentication();
        endpointData.put(ENDPOINT_AUTHENTICATION_SCHEME_FIELD, authentication.getType().getName());
        switch (authentication.getType()) {
            case BASIC:
                endpointData.put(USERNAME_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.USERNAME).getValue()));
                endpointData.put(PASSWORD_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.PASSWORD).getValue()));
                break;
            case BEARER:
                endpointData.put(ACCESS_TOKEN_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.ACCESS_TOKEN).getValue()));
                break;
            case API_KEY:
                endpointData.put(API_KEY_HEADER_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.HEADER).getValue()));
                endpointData.put(API_KEY_VALUE_FIELD, LoggerUtils.getMaskedContent(authentication.
                        getProperty(Authentication.Property.VALUE).getValue()));
                break;
        }
        data.put(ENDPOINT_CONFIG_FIELD, endpointData);
        return data;
    }

    /**
     * Generate audit log data for post update action.
     *
     * @param action Updated action model.
     * @return audit log data.
     */
    private JSONObject generateAuditLogDataForUpdateAction(Action action) {

        JSONObject data = new JSONObject();
        data.put(ACTION_TYPE_FIELD, action.getType() != null ? action.getType() : JSONObject.NULL);
        data.put(ACTION_ID_FIELD, action.getId() != null ? action.getId() : JSONObject.NULL);
        data.put(ACTION_NAME_FIELD, action.getName() != null ? action.getName() : JSONObject.NULL);
        data.put(ACTION_DESCRIPTION_FIELD, action.getDescription() != null ? action.getDescription() : JSONObject.NULL);
        data.put(ACTION_STATUS_FIELD, action.getStatus() != null ? action.getStatus() : JSONObject.NULL);

        JSONObject endpointData = new JSONObject();
        endpointData.put(ENDPOINT_URI_FIELD, action.getEndpoint().getUri());
        Authentication authentication = action.getEndpoint().getAuthentication();
        endpointData.put(ENDPOINT_AUTHENTICATION_SCHEME_FIELD, authentication.getType().getName());
        switch (authentication.getType()) {
            case BASIC:
                endpointData.put(USERNAME_FIELD, LoggerUtils.getMaskedContent(
                        authentication.getProperty(Authentication.Property.USERNAME) != null
                                ? authentication.getProperty(Authentication.Property.USERNAME).getValue() : ""));
                endpointData.put(PASSWORD_FIELD, LoggerUtils.getMaskedContent(
                        authentication.getProperty(Authentication.Property.PASSWORD) != null
                                ? authentication.getProperty(Authentication.Property.PASSWORD).getValue() : ""));
                break;
            case BEARER:
                endpointData.put(ACCESS_TOKEN_FIELD, LoggerUtils.getMaskedContent(
                        authentication.getProperty(Authentication.Property.ACCESS_TOKEN) != null
                                ? authentication.getProperty(Authentication.Property.ACCESS_TOKEN).getValue() : ""));
                break;
            case API_KEY:
                endpointData.put(API_KEY_HEADER_FIELD, LoggerUtils.getMaskedContent(
                        authentication.getProperty(Authentication.Property.HEADER) != null
                                ? authentication.getProperty(Authentication.Property.HEADER).getValue() : ""));
                endpointData.put(API_KEY_VALUE_FIELD, LoggerUtils.getMaskedContent(
                        authentication.getProperty(Authentication.Property.VALUE) != null
                                ? authentication.getProperty(Authentication.Property.VALUE).getValue() : ""));
                break;
        }
        data.put(ENDPOINT_CONFIG_FIELD, endpointData);
        return data;
    }
}
