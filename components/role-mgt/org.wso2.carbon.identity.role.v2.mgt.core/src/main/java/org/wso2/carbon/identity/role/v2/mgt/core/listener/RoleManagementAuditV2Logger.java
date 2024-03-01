/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core.listener;

import org.apache.commons.lang.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.*;
import org.wso2.carbon.user.mgt.listeners.utils.ListenerUtils;
import org.wso2.carbon.utils.AuditLog;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.central.log.mgt.utils.LogConstants.UserManagement.*;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.isEnableV2AuditLogs;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.jsonObjectToMap;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.IDP_GROUPS;

/**
 * Abstract implementation of the RoleManagementListener interface.
 */
public class RoleManagementAuditV2Logger extends AbstractRoleManagementListener {


    @Override
    public int getExecutionOrderId() {
        return 0;
    }

    @Override
    public int getDefaultOrderId() {
        return 0;
    }

    @Override
    public boolean isEnable() {
        return isEnableV2AuditLogs();
    }

    @Override
    public void postAddRole(RoleBasicInfo roleBasicInfo, String roleName, List<String> userList, List<String> groupList,
                            List<Permission> permissions, String audience, String audienceId, String tenantDomain) {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            String roleId = roleBasicInfo.getId();
            data.put(ROLE_NAME_FIELD, roleName);
            if (ArrayUtils.isNotEmpty(permissions.toArray())) {
                ArrayList<String> permissionsList = new ArrayList<>();
                for (Permission permission : permissions) {
                    permissionsList.add(permission.getName());
                }
                data.put(PERMISSIONS_FIELD, new JSONArray(permissionsList));
            }
            if (ArrayUtils.isNotEmpty(userList.toArray())) {
                data.put(USERS_FIELD, new JSONArray(userList));
            }
            if (ArrayUtils.isNotEmpty(groupList.toArray())) {
                data.put(GROUPS_FIELD, new JSONArray(groupList));
            }
            if (audience.equals(APPLICATION)) {
                AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                        ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                        roleId, LoggerUtils.TargetType.Role.name(),
                        ADD_APP_ROLE_ACTION).data(jsonObjectToMap(data));
                triggerAuditLogEvent(auditLogBuilder, true);
            }
            if (audience.equals(ORGANIZATION)) {
                AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                        ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                        roleId, LoggerUtils.TargetType.Role.name(),
                        ADD_ORG_ROLE_ACTION).data(jsonObjectToMap(data));
                triggerAuditLogEvent(auditLogBuilder, true);
            }
        }
    }


    @Override
    public void postGetRoles(List<RoleBasicInfo> roleBasicInfoList, Integer limit, Integer offset, String sortBy,
                             String sortOrder, String tenantDomain) {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roleBasicInfoList.toArray())) {
                ArrayList<String> appRoleInfoList = new ArrayList<>();
                ArrayList<String> orgRoleInfoList = new ArrayList<>();
                for (RoleBasicInfo roleInfo : roleBasicInfoList) {
                    if (roleInfo.getAudience().equals(ORGANIZATION)) {
                        orgRoleInfoList.add(roleInfo.getId());
                        //todo: confirm what information we require here; just the id or any other
                    }
                    if (roleInfo.getAudience().equals(APPLICATION)) {
                        appRoleInfoList.add(roleInfo.getId());
                    }
                }
                data.put(ORG_ROLE_FIELD, new JSONArray(orgRoleInfoList));
                data.put(APP_ROLE_FIELD, new JSONArray(appRoleInfoList));
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "role list", LoggerUtils.TargetType.Role.name(),
                    //todo : decide on the target id for the lists
                    GET_ROLE_LIST_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postGetRoles(List<Role> roles, Integer limit, Integer offset, String sortBy,
                             String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roles.toArray())) {
                ArrayList<String> appRoleInfoList = new ArrayList<>();
                ArrayList<String> orgRoleInfoList = new ArrayList<>();
                for (Role role : roles) {
                    if (role.getAudience().equals(ORGANIZATION)) {
                        orgRoleInfoList.add(role.getId());
                        //todo: confirm what information we require here; just the id or any other
                    }
                    if (role.getAudience().equals(APPLICATION)) {
                        appRoleInfoList.add(role.getId());
                    }
                }
                data.put(ORG_ROLE_FIELD, new JSONArray(orgRoleInfoList));
                data.put(APP_ROLE_FIELD, new JSONArray(appRoleInfoList));
            }
            if (ArrayUtils.isNotEmpty(requiredAttributes.toArray())) {
                data.put(REQUIRED_ATTRIBUTES_FIELD, new JSONArray(requiredAttributes));
            }

            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "role list", LoggerUtils.TargetType.Role.name(),
                    //todo : decide on the target id for the lists
                    GET_ROLE_LIST_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetRoles(List<RoleBasicInfo> roleBasicInfoList, String filter, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roleBasicInfoList.toArray())) {
                ArrayList<String> appRoleInfoList = new ArrayList<>();
                ArrayList<String> orgRoleInfoList = new ArrayList<>();
                for (RoleBasicInfo role : roleBasicInfoList) {
                    if (role.getAudience().equals(ORGANIZATION)) {
                        orgRoleInfoList.add(role.getId());
                        //todo: confirm what information we require here; just the id or any other
                    }
                    if (role.getAudience().equals(APPLICATION)) {
                        appRoleInfoList.add(role.getId());
                    }
                }
                data.put(ORG_ROLE_FIELD, new JSONArray(orgRoleInfoList));
                data.put(APP_ROLE_FIELD, new JSONArray(appRoleInfoList));
            }

            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "role list", LoggerUtils.TargetType.Role.name(),
                    //todo : decide on the target id for the lists
                    GET_ROLE_LIST_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }

    }

    @Override
    public void postGetRoles(List<Role> roleInfoList, String filter, Integer limit, Integer offset,
                             String sortBy, String sortOrder, String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roleInfoList.toArray())) {
                ArrayList<String> appRoleInfoList = new ArrayList<>();
                ArrayList<String> orgRoleInfoList = new ArrayList<>();
                for (Role role : roleInfoList) {
                    if (role.getAudience().equals(ORGANIZATION)) {
                        orgRoleInfoList.add(role.getId());
                        //todo: confirm what information we require here; just the id or any other
                    }
                    if (role.getAudience().equals(APPLICATION)) {
                        appRoleInfoList.add(role.getId());
                    }
                }
                data.put(ORG_ROLE_FIELD, new JSONArray(orgRoleInfoList));
                data.put(APP_ROLE_FIELD, new JSONArray(appRoleInfoList));
            }
            if (ArrayUtils.isNotEmpty(requiredAttributes.toArray())) {
                data.put(REQUIRED_ATTRIBUTES_FIELD, new JSONArray(requiredAttributes));
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "role list", LoggerUtils.TargetType.Role.name(),
                    //todo : decide on the target id for the lists
                    GET_ROLE_LIST_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            buildRoleData(role, data);
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    //todo : decide on the target id for the lists
                    GET_ROLE_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetRoleBasicInfo(RoleBasicInfo roleBasicInfo, String roleId, String tenantDomain) {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    GET_ROLE_BASIC_INFO_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postUpdateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        data.put(ROLE_NAME_FIELD, newRoleName);
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    UPDATE_ROLE_NAME_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postDeleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    DELETE_ROLE_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetUserListOfRole(List<UserBasicInfo> userBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(userBasicInfoList.toArray())) {
            ArrayList<String> usersList = new ArrayList<>();
            for (UserBasicInfo user : userBasicInfoList) {
                usersList.add(user.getId());
            }
            data.put(USERS_FIELD, new JSONArray(usersList));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    GET_USERS_OF_ROLE_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postUpdateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                         String tenantDomain) {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(newUserIDList.toArray())) {
            data.put(NEW_USERS, new JSONArray(newUserIDList));
        }
        if (ArrayUtils.isNotEmpty(newUserIDList.toArray())) {
            data.put(DELETED_USERS, new JSONArray(deletedUserIDList));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    UPDATE_USERS_OF_ROLE_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postGetGroupListOfRole(List<GroupBasicInfo> groupBasicInfoList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(groupBasicInfoList.toArray())) {
            ArrayList<String> groupsList = new ArrayList<>();
            for (GroupBasicInfo group : groupBasicInfoList) {
                groupsList.add(group.getId());
            }
            data.put(GROUPS_FIELD, new JSONArray(groupsList));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    GET_GROUP_LIST_OF_ROLE_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postUpdateGroupListOfRole(String roleId, List<String> newGroupIDList, List<String> deletedGroupIDList,
                                          String tenantDomain) throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(newGroupIDList.toArray())) {
            data.put(ADDED_GROUPS_FIELD, new JSONArray(newGroupIDList));
        }
        if (ArrayUtils.isNotEmpty(newGroupIDList.toArray())) {
            data.put(DELETED_GROUPS, new JSONArray(deletedGroupIDList));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    UPDATE_GROUP_LIST_OF_ROLE_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }

    }

    @Override
    public void postGetIdpGroupListOfRole(List<IdpGroup> idpGroups, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(idpGroups.toArray())) {
            ArrayList<String> idpGroupsList = new ArrayList<>();
            for (IdpGroup idpGroup : idpGroups) {
                idpGroupsList.add(idpGroup.getGroupId());
            }
            data.put(IDP_GROUPS_FIELD, new JSONArray(idpGroupsList));
            data.put(IDP_GROUPS_FIELD + "TEST", new JSONArray(idpGroups));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    GET_IDP_GROUPS_OF_ROLES).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postUpdateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupIDList,
                                             List<IdpGroup> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(newGroupIDList.toArray())) {
            ArrayList<String> newIdpGroupsList = new ArrayList<>();
            for (IdpGroup idpGroup : newGroupIDList) {
                newIdpGroupsList.add(idpGroup.getGroupId());
            }
            data.put(ADDED_IDP_GROUPS_FIELD, new JSONArray(newIdpGroupsList));
        }
        if (ArrayUtils.isNotEmpty(deletedGroupIDList.toArray())) {
            ArrayList<String> deletedIdpGroupList = new ArrayList<>();
            for (IdpGroup idpGroup : deletedGroupIDList) {
                deletedIdpGroupList.add(idpGroup.getGroupId());
            }
            data.put(DELETED_IDP_GROUPS_FIELD, new JSONArray(deletedIdpGroupList));
        }
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    UPDATE_IDP_GROUPS_OF_ROLES).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetPermissionListOfRole(List<Permission> permissionListOfRole, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(permissionListOfRole.toArray())) {
                ArrayList<String> permissionList = new ArrayList<>();
                for (Permission permission : permissionListOfRole) {
                    permissionList.add(permission.getName());
                }
                data.put(ADDED_IDP_GROUPS_FIELD, new JSONArray(permissionList));
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    GET_PERMISSIONS_OF_ROLES).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postUpdatePermissionsForRole(String roleId, List<Permission> addedPermissions,
                                             List<Permission> deletedPermissions, String audience, String audienceId,
                                             String tenantDomain) throws IdentityRoleManagementException {

        JSONObject data = new JSONObject();
        if (ArrayUtils.isNotEmpty(addedPermissions.toArray())) {
            ArrayList<String> addedPermissionsList = new ArrayList<>();
            for(Permission permission : addedPermissions) {
                addedPermissionsList.add(permission.getName());
            }
            data.put(ADDED_PERMISSIONS_FIELD, new JSONArray(addedPermissionsList));
        }
        if (ArrayUtils.isNotEmpty(deletedPermissions.toArray())) {
            ArrayList<String> deletedPermissionsList = new ArrayList<>();
            for(Permission permission : addedPermissions) {
                deletedPermissionsList.add(permission.getName());
            }
            data.put(DELETED_PERMISSIONS_FIELD, new JSONArray(deletedPermissionsList));
        }
        data.put(AUDIENCE_FIELD, audience);
        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    roleId, LoggerUtils.TargetType.Role.name(),
                    UPDATE_PERMISSIONS_OF_ROLES_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postGetRoleListOfUser(List<RoleBasicInfo> roleBasicInfoList, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roleBasicInfoList.toArray())) {
                ArrayList<String> roles = new ArrayList<>();
                for(RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
                    roles.add(roleBasicInfo.getId());
                }
                data.put(ROLES_FIELD, new JSONArray(roles));
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    userId, LoggerUtils.TargetType.User.name(),
                    GET_ROLES_OF_USER_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }


    @Override
    public void postGetRoleListOfGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                        String tenantDomain) throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            if (ArrayUtils.isNotEmpty(roleBasicInfoList.toArray())) {
                ArrayList<String> roles = new ArrayList<>();
                for(RoleBasicInfo roleBasicInfo : roleBasicInfoList) {
                    roles.add(roleBasicInfo.getId());
                }
                data.put(ROLES_FIELD, new JSONArray(roles));
            }
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "roles of groups", LoggerUtils.TargetType.User.name(),
                    GET_ROLES_OF_GROUP_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    @Override
    public void postGetRoleListOfIdpGroups(List<RoleBasicInfo> roleBasicInfoList, List<String> groupIds,
                                           String tenantDomain) throws IdentityRoleManagementException {

           //todo

    }

    @Override
    public void postGetRoleIdListOfUser(List<String> roleIds, String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            JSONObject data = new JSONObject();
            data.put(ROLES_FIELD, new JSONArray(roleIds));
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    "roles of groups", LoggerUtils.TargetType.User.name(),
                    GET_ROLES_OF_USER_ACTION).data(jsonObjectToMap(data));
            triggerAuditLogEvent(auditLogBuilder, true);
        }

    }

    @Override
    public void postGetRoleIdListOfGroups(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

         // todo
    }


    @Override
    public void postGetRoleIdListOfIdpGroups(List<String> roleIds, List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        // todo

    }


    @Override
    public void postDeleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (isEnable()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    ListenerUtils.getInitiatorId(), ListenerUtils.getInitiatorType(ListenerUtils.getInitiatorId()),
                    applicationId, LoggerUtils.TargetType.Application.name(),
                    DELETE_ROLES_BY_APP_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }

    }

    private static void buildRoleData(Role role, JSONObject data) {

        data.put(ROLE_NAME_FIELD, role.getName());
        data.put(AUDIENCE_FIELD, role.getAudience());
        if (role.getAudience().equals(APPLICATION)) {
            data.put(ASSOCIATED_APPLICATIONS_FIELD, new JSONArray(role.getAssociatedApplications()));
        }
        if (ArrayUtils.isNotEmpty(role.getPermissions().toArray())) {
            ArrayList<String> permissionsList = new ArrayList<>();
            for (Permission permission : role.getPermissions()) {
                permissionsList.add(permission.getName());
            }
            data.put(PERMISSIONS_FIELD, new JSONArray(permissionsList));
        }
        if (ArrayUtils.isNotEmpty(role.getUsers().toArray())) {
            ArrayList<String> usersList = new ArrayList<>();
            for (UserBasicInfo user : role.getUsers()) {
                usersList.add(user.getId());
            }
            data.put(USERS_FIELD, new JSONArray(usersList));
        }
        if (ArrayUtils.isNotEmpty(role.getGroups().toArray())) {
            ArrayList<String> groupsList = new ArrayList<>();
            for (GroupBasicInfo group : role.getGroups()) {
                groupsList.add(group.getId());
            }
            data.put(GROUPS_FIELD, new JSONArray(groupsList));
        }
        if (ArrayUtils.isNotEmpty(role.getIdpGroups().toArray())) {
            ArrayList<String> idpGroupsList = new ArrayList<>();
            for (IdpGroup idpGroup : role.getIdpGroups()) {
                idpGroupsList.add(idpGroup.getGroupId());
            }
            data.put(IDP_GROUPS, new JSONArray(idpGroupsList));
        }
    }

}
