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

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityGroupOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.Claim;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.utils.AuditLog;

import java.util.List;

import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.ADDED_USERS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.ADD_GROUP_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.DELETED_USERS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.DELETE_GROUP_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.GET_GROUPS_OF_USERS_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.GROUPS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.GROUP_NAME_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.UPDATE_GROUP_NAME_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.UPDATE_USERS_OF_GROUP_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.USERS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.Target;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.isEnableV2AuditLogs;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.getInitiatorId;

/**
 * This v2 audit logger logs the Group Management success activities.
 */
public class GroupManagementV2AuditLogger extends AbstractIdentityGroupOperationEventListener {

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            return isEnableV2AuditLogs();
        }
        return false;
    }

    @Override
    public boolean postAddGroup(String groupName, String groupId, List<String> userIds, List<Claim> claims,
                                UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        if (CollectionUtils.isNotEmpty(userIds)) {
            dataObject.put(USERS_FIELD, new JSONArray(userIds));
        }
        dataObject.put(GROUP_NAME_FIELD, groupName);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), groupId, Target.Group.name(), ADD_GROUP_ACTION)
                .data(jsonObjectToMap(dataObject));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean postDeleteGroup(String groupId, String groupName, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), groupId, Target.Group.name(), DELETE_GROUP_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean postRenameGroup(String groupId, String newGroupName, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        dataObject.put(GROUP_NAME_FIELD, newGroupName);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), groupId, Target.Group.name(), UPDATE_GROUP_NAME_ACTION)
                .data(jsonObjectToMap(dataObject));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean postUpdateUserListOfGroup(String groupId, List<String> deletedUserIds, List<String> newUserIds,
                                             UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        if (CollectionUtils.isNotEmpty(deletedUserIds)) {
            dataObject.put(DELETED_USERS_FIELD, new JSONArray(deletedUserIds));
        }
        if (CollectionUtils.isNotEmpty(newUserIds)) {
            dataObject.put(ADDED_USERS_FIELD, new JSONArray(deletedUserIds));
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), groupId, Target.Group.name(),
                UPDATE_USERS_OF_GROUP_ACTION).data(jsonObjectToMap(dataObject));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean postGetGroupsListOfUserByUserId(String userId, List<Group> groupList,
                                                   UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        if (CollectionUtils.isNotEmpty(groupList)) {
            dataObject.put(GROUPS_FIELD, new JSONArray(groupList));
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, Target.User.name(), GET_GROUPS_OF_USERS_ACTION)
                .data(jsonObjectToMap(dataObject));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 1;
    }
}
