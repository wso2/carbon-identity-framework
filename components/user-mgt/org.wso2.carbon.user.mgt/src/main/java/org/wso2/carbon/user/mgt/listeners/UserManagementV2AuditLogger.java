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
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.utils.AuditLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.ADD_USER_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.CLAIMS_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.CLAIM_URI_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.CLAIM_VALUE_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.CREDENTIAL_UPDATE_BY_ADMIN_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.CREDENTIAL_UPDATE_BY_USER_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.DELETE_USER_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.DELETE_USER_CLAIM_VALUES_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.DELETE_USER_CLAIM_VALUE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.GET_USER_CLAIM_VALUES_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.GET_USER_CLAIM_VALUE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.PROFILE_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.ROLE_NAME_FIELD;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.SET_USER_CLAIM_VALUES_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.SET_USER_CLAIM_VALUE_ACTION;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.isEnableV2AuditLogs;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.getInitiatorId;

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

        if (!isEnable()) {
            return true;
        }
        JSONObject data = new JSONObject();
        data.put(PROFILE_FIELD, profile);
        if (ArrayUtils.isNotEmpty(roleList)) {
            data.put(ROLE_NAME_FIELD, new JSONArray(roleList));
        }
        maskClaimsInAuditLog(claims, data);
        String userId = user.getUserID();
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(), ADD_USER_ACTION)
                .data(jsonObjectToMap(data));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostDeleteUserWithID(String userId, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                DELETE_USER_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userId, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                SET_USER_CLAIM_VALUE_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    public boolean doPostSetUserClaimValuesWithID(String userId, Map<String, String> claims, String profileName,
                                                  UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        dataObject.put(PROFILE_FIELD, profileName);
        maskClaimsInAuditLog(claims, dataObject);
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                SET_USER_CLAIM_VALUES_ACTION).data(jsonObjectToMap(dataObject));
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String userId, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                getInitiatorId(), LoggerUtils.getInitiatorType(getInitiatorId()), userId,
                LoggerUtils.Target.User.name(), DELETE_USER_CLAIM_VALUE_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String userId, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                DELETE_USER_CLAIM_VALUES_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialWithID(String userId, Object credential, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(getInitiatorId(),
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                CREDENTIAL_UPDATE_BY_USER_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdminWithID(String userId, Object credential,
                                                       UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        String initiatorId = getInitiatorId();
        // In most cases even if the password reset is initiated by the user him self, it is being treated as
        // admin initiated. Hence,
        if (StringUtils.isNotBlank(initiatorId) && initiatorId.equals(userId)) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(initiatorId,
                    LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                    CREDENTIAL_UPDATE_BY_USER_ACTION);
            triggerAuditLogEvent(auditLogBuilder);
            return true;
        }
        AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(initiatorId,
                LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                CREDENTIAL_UPDATE_BY_ADMIN_ACTION);
        triggerAuditLogEvent(auditLogBuilder);
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValueWithID(String userId, String claim, List<String> claimValue,
                                                 String profileName, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        dataObject.put(CLAIM_URI_FIELD, claim);
        if (LoggerUtils.isLogMaskingEnable) {
            List<String> maskedClaimValues = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(claimValue)) {
                for (String claimVal : claimValue) {
                    String maskedClaimValue = LoggerUtils.getMaskedClaimValue(claim, claimVal);
                    maskedClaimValues.add(maskedClaimValue);
                }
                dataObject.put(CLAIM_VALUE_FIELD, new JSONArray(maskedClaimValues));
            }
        } else if (CollectionUtils.isNotEmpty(claimValue)) {
            dataObject.put(CLAIM_VALUE_FIELD, new JSONArray(claimValue));
        }
        dataObject.put(PROFILE_FIELD, profileName);
        String initiatorId = getInitiatorId();
        if (!LoggerUtils.Initiator.System.name().equalsIgnoreCase(initiatorId)) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(initiatorId,
                    LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                    GET_USER_CLAIM_VALUE_ACTION).data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder);
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String userId, String[] claims, String profileName,
                                                  Map<String, String> claimMap, UserStoreManager userStoreManager) {

        if (!isEnable()) {
            return true;
        }
        JSONObject dataObject = new JSONObject();
        if (claimMap != null && !claimMap.isEmpty()) {
            maskClaimsInAuditLog(claimMap, dataObject);
        }
        dataObject.put(PROFILE_FIELD, profileName);
        String initiatorId = getInitiatorId();
        if (!LoggerUtils.Initiator.System.name().equalsIgnoreCase(initiatorId)) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(initiatorId,
                    LoggerUtils.getInitiatorType(getInitiatorId()), userId, LoggerUtils.Target.User.name(),
                    GET_USER_CLAIM_VALUES_ACTION).data(jsonObjectToMap(dataObject));
            triggerAuditLogEvent(auditLogBuilder);
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
            data.put(CLAIMS_FIELD, new JSONObject(maskedClaims));
        } else {
            data.put(CLAIMS_FIELD, new JSONObject(claims));
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
