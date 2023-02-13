/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserMgtFailureEventListener;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;

import java.util.Map;

import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.getInitiator;
import static org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils.getTargetForAuditLog;

/**
 * This class is responsible for logging the failure events while doing User Management Tasks.
 */
public class UserMgtFailureAuditLogger extends AbstractIdentityUserMgtFailureEventListener {
    private static final Log audit = CarbonConstants.AUDIT_LOG;

    @Override
    public boolean onAuthenticateFailure(String errorCode, String errorMessage, String userName, Object credential,
            UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.AUTHENTICATION_ACTION, getTargetForAuditLog(userName,
                userStoreManager), null, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onAddUserFailure(String errorCode, String errorMessage, String userName, Object credential,
            String[] roleList, Map<String, String> claims, String profile, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(roleList)) {
            dataObject.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
        }
        if (LoggerUtils.isLogMaskingEnable) {
            Map<String, String> maskedClaimsMap = LoggerUtils.getMaskedClaimsMap(claims);
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(maskedClaimsMap));
        } else {
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
        }
        dataObject.put(ListenerUtils.PROFILE_FIELD, profile);
        audit.warn(createAuditMessage(ListenerUtils.ADD_USER_ACTION, getTargetForAuditLog(userName, userStoreManager),
                profile, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onUpdateCredentialFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, Object oldCredential, UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_USER_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), null, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onUpdateCredentialByAdminFailure(String errorCode, String errorMessage, String userName,
            Object newCredential, UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_ADMIN_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), null, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onDeleteUserFailure(String errorCode, String errorMessage, String userName,
            UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_ACTION, getTargetForAuditLog(userName,
                userStoreManager), null, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onSetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claimURI,
            String claimValue, String profileName, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (LoggerUtils.isLogMaskingEnable) {
            String maskedClaimValue = LoggerUtils.getMaskedClaimValue(claimURI, claimValue);
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, maskedClaimValue);
        } else {
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
        }
        dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
        audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUE_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), dataObject, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onSetUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            Map<String, String> claims, String profileName, UserStoreManager userStoreManager) {

        if (LoggerUtils.isLogMaskingEnable) {
            Map<String, String> maskedClaimsMap = LoggerUtils.getMaskedClaimsMap(claims);
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUES_ACTION, getTargetForAuditLog(userName,
                            userStoreManager), new JSONObject(maskedClaimsMap), errorCode, errorMessage));
        } else {
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUES_ACTION, getTargetForAuditLog(userName,
                            userStoreManager), new JSONObject(claims), errorCode, errorMessage));
        }
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValuesFailure(String errorCode, String errorMessage, String userName,
            String[] claims, String profileName, UserStoreManager userStoreManager) {

        JSONArray data = new JSONArray();
        if (ArrayUtils.isNotEmpty(claims)) {
            data = new JSONArray(claims);
        }
        audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUES_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), data, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onDeleteUserClaimValueFailure(String errorCode, String errorMessage, String userName,
            String claimURI, String profileName, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
        dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
        audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUE_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), dataObject, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onAddRoleFailure(String errorCode, String errorMessage, String roleName, String[] userList,
            Permission[] permissions, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(userList)) {
            if (LoggerUtils.isLogMaskingEnable) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(LoggerUtils.getMaskedArraysOfValues(userList)));
            } else {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
            }
        }
        if (ArrayUtils.isNotEmpty(permissions)) {
            JSONArray permissionsArray = new JSONArray(permissions);
            dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
        }
        audit.warn(createAuditMessage(ListenerUtils.ADD_ROLE_ACTION, ListenerUtils.getEntityWithUserStoreDomain(
                roleName, userStoreManager), dataObject, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onAddRoleFailureWithID(String errorCode, String errorMessage, String roleName, String[] userList,
                                          org.wso2.carbon.user.api.Permission[] permissions,
                                          UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(userList)) {
            if (LoggerUtils.isLogMaskingEnable) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(LoggerUtils.getMaskedArraysOfValues(userList)));
            } else {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
            }
        }
        if (ArrayUtils.isNotEmpty(permissions)) {
            JSONArray permissionsArray = new JSONArray(permissions);
            dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
        }
        audit.warn(createAuditMessage(ListenerUtils.ADD_ROLE_ACTION,
                ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, errorCode,
                errorMessage));
        return true;
    }

    @Override
    public boolean onDeleteRoleFailure(String errorCode, String errorMessage, String roleName,
            UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.DELETE_ROLE_ACTION,
                ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), null, errorCode, errorMessage));
        return true;
    }

    @Override
    public boolean onUpdateRoleNameFailure(String errorCode, String errorMessage, String roleName, String newRoleName,
            UserStoreManager userStoreManager) {

        audit.warn(createAuditMessage(ListenerUtils.UPDATE_ROLE_NAME_ACTION,
                ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), newRoleName, errorCode,
                errorMessage));
        return true;
    }

    @Override
    public boolean onUpdateUserListOfRoleFailure(String errorCode, String errorMessage, String roleName,
            String[] deletedUsers, String[] newUsers, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(deletedUsers)) {
            dataObject.put(ListenerUtils.DELETED_USERS, new JSONArray(deletedUsers));
        }
        if (ArrayUtils.isNotEmpty(newUsers)) {
            dataObject.put(ListenerUtils.NEW_USERS, new JSONArray(newUsers));
        }
        audit.warn(createAuditMessage(ListenerUtils.UPDATE_USERS_OF_ROLE_ACTION,
                ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, errorCode,
                errorMessage));

        return true;
    }

    @Override
    public boolean onUpdateRoleListOfUserFailure(String errorCode, String errorMessage, String userName,
            String[] deletedRoles, String[] newRoles, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(deletedRoles)) {
            dataObject.put(ListenerUtils.DELETED_ROLES, new JSONArray(deletedRoles));
        }
        if (ArrayUtils.isNotEmpty(newRoles)) {
            dataObject.put(ListenerUtils.NEW_ROLES, new JSONArray(newRoles));
        }
        audit.warn(createAuditMessage(ListenerUtils.UPDATE_ROLES_OF_USER_ACTION, getTargetForAuditLog(userName,
                userStoreManager), dataObject, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onGetUserClaimValueFailure(String errorCode, String errorMessage, String userName, String claim,
            String profileName, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
        dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claim);
        audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUE_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), dataObject, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onGetUserClaimValuesFailure(String errorCode, String errorMessage, String userName, String[] claims,
            String profile, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        dataObject.put(ListenerUtils.PROFILE_FIELD, profile);
        if (ArrayUtils.isNotEmpty(claims)) {
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONArray(claims));
        }
        audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUES_ACTION, getTargetForAuditLog(userName,
                        userStoreManager), dataObject, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onGetUserListFailure(String errorCode, String errorMessage, String claim, String claimValue,
            String profileName, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claim);
        if (LoggerUtils.isLogMaskingEnable) {
            String maskedClaimValue = LoggerUtils.getMaskedClaimValue(claim, claimValue);
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, maskedClaimValue);
        } else {
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
        }
        dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
        audit.info(createAuditMessage(ListenerUtils.GET_USER_LIST_ACTION, null, dataObject, errorCode, errorMessage));

        return true;
    }

    @Override
    public boolean onUpdatePermissionsOfRoleFailure(String errorCode, String errorMessage, String roleName,
            Permission[] permissions, UserStoreManager userStoreManager) {

        JSONObject dataObject = new JSONObject();
        if (ArrayUtils.isNotEmpty(permissions)) {
            JSONArray permissionsArray = new JSONArray(permissions);
            dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
        }

        audit.warn(createAuditMessage(ListenerUtils.UPDATE_PERMISSIONS_OF_ROLE_ACTION,
                ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, errorCode,
                errorMessage));
        return true;
    }

    /**
     * To create an audit message based on provided parameters.
     *
     * @param action       Activity
     * @param target       Target affected by this activity.
     * @param data         Information passed along with the request.
     * @param errorCode    Error Code.
     * @param errorMessage Error Message.
     * @return Relevant audit log in Json format.
     */
    private String createAuditMessage(String action, String target, Object data, String errorCode,
            String errorMessage) {

        String errorCodeField = "Error Code";
        String errorMessageField = "Error Message";
        JSONObject error = new JSONObject();
        error.put(errorCodeField, errorCode);
        error.put(errorMessageField, errorMessage);

        String auditMessage =
                ListenerUtils.INITIATOR + "=%s " + ListenerUtils.ACTION + "=%s " + ListenerUtils.TARGET + "=%s "
                        + ListenerUtils.DATA + "=%s " + ListenerUtils.OUTCOME + "=Failure " + ListenerUtils.ERROR
                        + "=%s";
        return String.format(auditMessage, getInitiator(), action, target, data, error);
    }
}
