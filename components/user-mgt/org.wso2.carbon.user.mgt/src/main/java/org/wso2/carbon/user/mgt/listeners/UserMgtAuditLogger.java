/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt.listeners;

import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;

import java.util.Arrays;
import java.util.Map;

public class UserMgtAuditLogger extends AbstractIdentityUserOperationEventListener {


    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String SUCCESS = "Success";

    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }

        return 8;
    }

    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                 String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        StringBuilder builder = new StringBuilder();
        if (roleList != null) {
            for (int i = 0; i < roleList.length; i++) {
                builder.append(roleList[i] + ",");
            }
        }
        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Add User",
                LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(userName) : userName, "Roles :" +
                        builder.toString(), SUCCESS));
        return true;
    }

    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Delete User",
                LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(userName) : userName, "", SUCCESS));
        return true;
    }

    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager) throws
            UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Change Password by User",
                LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(userName) : userName, "", SUCCESS));
        return true;
    }

    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential, UserStoreManager
            userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(),
                "Change Password by Administrator", LoggerUtils.isLogMaskingEnable ? LoggerUtils
                        .getMaskedContent(userName) : userName, "", SUCCESS));
        return true;
    }

    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Delete Role", roleName, "",
                SUCCESS));
        return true;
    }

    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions, UserStoreManager
            userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        StringBuilder stringBuilder = new StringBuilder();
        String permissionsString;
        String usersString;
        String format = "ResourceID : %s Action : %s\n";
        for (Permission permission : permissions) {
            stringBuilder.append(String.format(format, permission.getResourceId(), permission.getAction()));
        }
        permissionsString = stringBuilder.toString();
        // Clear buffer for reuse.
        stringBuilder.setLength(0);
        format = "%s\n";
        for (String user : userList) {
            stringBuilder.append(String.format(format, LoggerUtils.isLogMaskingEnable ?
                    LoggerUtils.getMaskedContent(user) : user));
        }
        usersString = stringBuilder.toString();
        format = "\nUsers :\n%sPermissions :\n%s";
        String data = String.format(format, usersString, permissionsString);
        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Add Role", roleName, data,
                SUCCESS));
        return true;
    }

    public boolean doPostUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Update Role Name", roleName,
                "Old : " + roleName + " New : " + newRoleName, SUCCESS));
        return true;
    }

    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (LoggerUtils.isLogMaskingEnable) {
            audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Update Users of Role",
                    roleName, "UsersAdded : " + Arrays.toString(LoggerUtils.getMaskedArraysOfValues(newUsers)) +
                            ", UsersRemoved : " + Arrays.toString(LoggerUtils.getMaskedArraysOfValues(deletedUsers)),
                    SUCCESS));
        } else {
            audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Update Users of Role",
                    roleName, "UsersAdded : " + Arrays.toString(newUsers) + ", UsersRemoved : " +
                            Arrays.toString(deletedUsers), SUCCESS));
        }
        return true;
    }

    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        audit.info(String.format(AUDIT_MESSAGE, ListenerUtils.getInitiatorFromContext(), "Update Roles of User",
                LoggerUtils.isLogMaskingEnable ? LoggerUtils.getMaskedContent(userName) : userName, "RolesAdded : " +
                        Arrays.toString(newRoles) + ", RolesRemoved : " + Arrays.toString(deletedRoles), SUCCESS));
        return true;
    }
}
