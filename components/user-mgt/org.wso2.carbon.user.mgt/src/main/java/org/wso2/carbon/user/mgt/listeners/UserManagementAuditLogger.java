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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;

import java.util.List;
import java.util.Map;

/**
 * This audit logger logs the User Management success activities.
 */
public class UserManagementAuditLogger extends AbstractIdentityUserOperationEventListener {

    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String SUCCESS = "Success";
    private static final String IN_PROGRESS = "In-Progress";

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profile, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            data.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            if (ArrayUtils.isNotEmpty(roleList)) {
                data.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
            }
            data.put(ListenerUtils.PROFILE_FIELD, profile);
            audit.warn(createAuditMessage(ListenerUtils.ADD_USER_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), data, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.warn(createAuditMessage(ListenerUtils.SET_USER_CLAIM_VALUES_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claims));
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUES_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUES_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimURI);
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, IN_PROGRESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.DELETE_USER_CLAIM_VALUE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_USER_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object newCredential,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.info(createAuditMessage(ListenerUtils.CHANGE_PASSWORD_BY_ADMIN_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.DELETE_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), null, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(userList)) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
            }
            if (ArrayUtils.isNotEmpty(permissions)) {
                JSONArray permissionsArray = new JSONArray(permissions);
                dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
            }
            audit.warn(createAuditMessage(ListenerUtils.ADD_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager) {

        if (isEnable()) {
            audit.warn(createAuditMessage(ListenerUtils.UPDATE_ROLE_NAME_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), newRoleName, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdatePermissionsOfRole(String roleName, Permission[] permissions, UserStoreManager
            userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(permissions)) {
                JSONArray permissionsArray = new JSONArray(permissions);
                dataObject.put(ListenerUtils.PERMISSIONS_FIELD, permissionsArray);
            }

            audit.warn(createAuditMessage(ListenerUtils.UPDATE_PERMISSIONS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(deletedUsers)) {
                dataObject.put(ListenerUtils.DELETED_USERS, new JSONArray(deletedUsers));
            }
            if (ArrayUtils.isNotEmpty(newUsers)) {
                dataObject.put(ListenerUtils.NEW_USERS, new JSONArray(newUsers));
            }
            audit.info(createAuditMessage(ListenerUtils.UPDATE_USERS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(deletedRoles)) {
                dataObject.put(ListenerUtils.DELETED_ROLES, new JSONArray(deletedRoles));
            }
            if (ArrayUtils.isNotEmpty(newRoles)) {
                dataObject.put(ListenerUtils.NEW_ROLES, new JSONArray(newRoles));
            }
            audit.info(createAuditMessage(ListenerUtils.UPDATE_ROLES_OF_USER_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue, String profileName,
            UserStoreManager storeManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claim);
            if (CollectionUtils.isNotEmpty(claimValue)) {
                dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, new JSONArray(claimValue));
            }
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, storeManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
            Map<String, String> claimMap, UserStoreManager storeManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIMS_FIELD, new JSONObject(claimMap));
            dataObject.put(ListenerUtils.PROFILE_FIELD, profileName);
            audit.info(createAuditMessage(ListenerUtils.GET_USER_CLAIM_VALUES_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, storeManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, final List<String> returnValues,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.CLAIM_URI_FIELD, claimUri);
            dataObject.put(ListenerUtils.CLAIM_VALUE_FIELD, claimValue);
            if (CollectionUtils.isNotEmpty(returnValues)) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(returnValues));
            }
            audit.info(createAuditMessage(ListenerUtils.GET_USER_LIST_ACTION, null, dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUser(String userName, String filter, String[] roleList,
            UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            dataObject.put(ListenerUtils.FILTER_FIELD, filter);
            if (ArrayUtils.isNotEmpty(roleList)) {
                dataObject.put(ListenerUtils.ROLES_FIELD, new JSONArray(roleList));
            }
            audit.info(createAuditMessage(ListenerUtils.GET_ROLES_OF_USER_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(userName, userStoreManager), dataObject, SUCCESS));
        }
        return true;
    }

    @Override
    public boolean doPostGetUserListOfRole(String roleName, String[] userList, UserStoreManager userStoreManager) {

        if (isEnable()) {
            JSONObject dataObject = new JSONObject();
            if (ArrayUtils.isNotEmpty(userList)) {
                dataObject.put(ListenerUtils.USERS_FIELD, new JSONArray(userList));
            }
            audit.info(createAuditMessage(ListenerUtils.GET_USERS_OF_ROLE_ACTION,
                    ListenerUtils.getEntityWithUserStoreDomain(roleName, userStoreManager), dataObject, SUCCESS));
        }
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

    /**
     * To create an audit message based on provided parameters.
     *
     * @param action      Activity
     * @param target      Target affected by this activity.
     * @param data        Information passed along with the request.
     * @param resultField Result value.
     * @return Relevant audit log in Json format.
     */
    private String createAuditMessage(String action, String target, Object data, String resultField) {

        String auditMessage =
                ListenerUtils.INITIATOR + "=%s " + ListenerUtils.ACTION + "=%s " + ListenerUtils.TARGET + "=%s "
                        + ListenerUtils.DATA + "=%s " + ListenerUtils.OUTCOME + "=%s";
        return String.format(auditMessage, ListenerUtils.getUser(), action, target, data, resultField);
    }
}
