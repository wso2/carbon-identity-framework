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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;
import org.wso2.carbon.utils.AuditLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.*;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.isEnableV2AuditLogs;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.CLAIM_URI_FIELD;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.CLAIM_VALUE_FIELD;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.COUNT;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.DELETED_USERS;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.NEW_USERS;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.USERS_FIELD;

/**
 * This v2 audit logger logs the User Management success activities.
 */
public class UserManagementV2AuditLogger extends AbstractIdentityUserOperationEventListener {

    @Override
    public boolean isEnable() {

        if (super.isEnable()) {
            return isEnableV2AuditLogs();
        }
        return false;
    }

    @Override
    public boolean doPostAddUserWithID(User user, Object credential, String[]
            roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            data.put(ListenerUtils.PROFILE_FIELD, profile);
            if (ArrayUtils.isNotEmpty(roleList)) {
                data.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
            }
            maskClaimsInAuditLog(claims, data);
            String userId = user.getUserID();
            if (isEnableV2AuditLogs()) {
                AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                        ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                        userId, LoggerUtils.TargetType.User.name(), ADD_USER_ACTION).data(jsonObjectToMap(data));
                triggerAuditLogEvent(auditLogBuilder, true);
            }
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserWithID(String userId, UserStoreManager userStoreManager) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), DELETE_USER_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userId, UserStoreManager userStoreManager) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), SET_USER_CLAIM_VALUE_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    public boolean doPostSetUserClaimValuesWithID(String userId, Map<String, String> claims, String profileName,
                                                  UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            maskClaimsInAuditLog(claims, dataObject);
            String initiatorId = ListenerUtils.getInitiatorId();
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    initiatorId, ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()), userId,
                    LoggerUtils.TargetType.User.name(), SET_USER_CLAIM_VALUES_ACTION)
                    .data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String userId, UserStoreManager userStoreManager) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), DELETE_USER_CLAIM_VALUE_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String userId, UserStoreManager userStoreManager) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), DELETE_USER_CLAIM_VALUES_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialWithID(String userId, Object credential, UserStoreManager userStoreManager) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), CHANGE_PASSWORD_BY_USER_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdminWithID(String userId, Object credential,
                                                       UserStoreManager userStoreManager) {

        if (isEnable()) {
            String initiatorId = ListenerUtils.getInitiatorId();
            // In most cases even if the password reset is initiated by the user him self, it is being treated as
            // admin initiated. Hence,
            if (StringUtils.isNotBlank(initiatorId) && initiatorId.equals(userId)) {
                AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                        ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()), userId, LoggerUtils.TargetType.User.name(),
                        CHANGE_PASSWORD_BY_USER_ACTION);
                triggerAuditLogEvent(auditLogBuilder, true);
                return true;
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()), userId, LoggerUtils.TargetType.User.name(),
                    CHANGE_PASSWORD_BY_ADMIN_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }


    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, List<User> users,
                                           UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            List<String> userList = new ArrayList<>();

            for (User user : users) {
                userList.add(user.getUserID());
            }

            dataObject.put(USERS_FIELD, new JSONArray(userList));
            dataObject.put(COUNT, users.size());
            String maskedClaimValue = LoggerUtils.getMaskedClaimValue(claimUri, claimValue);
            dataObject.put(CLAIM_VALUE_FIELD, maskedClaimValue);
            dataObject.put(CLAIM_URI_FIELD, claimUri);
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()), "User List", LoggerUtils.TargetType.User.name(),
                    GET_USER_LIST_ACTION).data(jsonObjectToMap(dataObject));
            // todo: what will be the target here? and why we dont have getUserListWithID for a single get user?
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValueWithID(String userId, String claim, List<String> claimValue,
                                                 String profileName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claim);
            if (LoggerUtils.isLogMaskingEnable) {
                List<String> maskedClaimValues = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(claimValue)) {
                    for (String claimVal : claimValue) {
                        String maskedClaimValue = LoggerUtils.getMaskedClaimValue(claim, claimVal);
                        maskedClaimValues.add(maskedClaimValue);
                    }
                    dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, new JSONArray(maskedClaimValues));
                }
            } else if (CollectionUtils.isNotEmpty(claimValue)) {
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, new JSONArray(claimValue));
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), GET_USER_CLAIM_VALUE_ACTION)
                    .data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String userId, String[] claims, String profileName,
                                                  Map<String, String> claimMap, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (claimMap != null && !claimMap.isEmpty()) {
                maskClaimsInAuditLog(claimMap, dataObject);
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), GET_USER_CLAIM_VALUES_ACTION)
                    .data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String userId, AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) {

        if (isEnable()) {
            if (authenticationResult.getAuthenticationStatus() == AuthenticationResult.AuthenticationStatus.SUCCESS) {
                AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                        ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                        userId, LoggerUtils.TargetType.User.name(), LOGIN);
                triggerAuditLogEvent(auditLogBuilder, true);
            }
        }
        return true;
    }
    @Override
    public boolean doPostUpdateRoleListOfUserWithID(String userId, String[] deletedRoles, String[] newRoles,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(deletedRoles)) {
                dataObject.put(ListenerUtils.DELETED_ROLES, new JSONArray(deletedRoles));
            }
            if (ArrayUtils.isNotEmpty(newRoles)) {
                dataObject.put(ListenerUtils.NEW_ROLES, new JSONArray(newRoles));
            }

            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(), UPDATE_GROUPS_OF_USER_ACTION)
                    .data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return true;
    }

    /**
     * Mask claims in audit logs based on the masking config.
     *
     * @param claims Claims map.
     * @param data   JSON Object which will be added to audit log.
     */
    private void maskClaimsInAuditLog(Map<String, String> claims, JSONObject data) {

        if (LoggerUtils.isLogMaskingEnable) {
            Map<String, String> maskedClaims = LoggerUtils.getMaskedClaimsMap(claims);
            data.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(maskedClaims));
        } else {
            data.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
        }
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
