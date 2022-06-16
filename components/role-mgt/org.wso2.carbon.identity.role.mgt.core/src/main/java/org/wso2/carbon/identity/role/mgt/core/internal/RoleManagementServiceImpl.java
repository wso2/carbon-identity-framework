/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.role.mgt.core.internal;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.Role;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementEventPublisherProxy;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.dao.RoleDAO;
import org.wso2.carbon.identity.role.mgt.core.dao.RoleMgtDAOFactory;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.List;
import java.util.Set;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;

/**
 * Implementation of the {@link RoleManagementService} interface.
 */
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private final RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getRoleDAO();
    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private static final String auditMessage
            = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private final String success = "Success";

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
            List<String> permissions, String tenantDomain) throws IdentityRoleManagementException {

        /* Block the role names with the prefix 'system_' as it is used for the special roles created by the system in
        order to maintain the backward compatibility. */
        if (StringUtils.startsWithIgnoreCase(roleName, UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX)) {
            String errorMessage = String.format("Invalid role name: %s. Role names with the prefix: %s, is not allowed"
                            + " to be created from externally in the system.", roleName,
                    UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX);
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        if (isDomainSeparatorPresent(roleName)) {
            // SCIM2 API only adds roles to the internal domain.
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid character: "
                    + UserCoreConstants.DOMAIN_SEPARATOR + " contains in the role name: " + roleName + ".");
        }

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreAddRole(roleName, userList, groupList, permissions, tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.addRole(roleName, userList, groupList, permissions, tenantDomain);
        roleManagementEventPublisherProxy.publishPostAddRole(roleName, userList, groupList, permissions, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s add role of name : %s successfully.", getUser(tenantDomain), roleName));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Add Role", roleName,
                getAuditData(tenantDomain), success));
        return roleBasicInfo;
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
            String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
            String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(filter, limit, offset, sortBy,
                sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get filtered roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy =
                RoleManagementEventPublisherProxy.getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesCount(tenantDomain);
        int count = roleDAO.getRolesCount(tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRolesCount(tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles count successfully.", getUser(tenantDomain)));
        }
        return count;
    }

    @Override
    public Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRole(roleID, tenantDomain);
        Role role = roleDAO.getRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get role of id : %s successfully.", getUser(tenantDomain), roleID));
        }
        return role;
    }

    @Override
    public RoleBasicInfo updateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateRoleName(roleID, newRoleName, tenantDomain);
        if (isDomainSeparatorPresent(newRoleName)) {
            // SCIM2 API only adds roles to the internal domain.
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid character: "
                    + UserCoreConstants.DOMAIN_SEPARATOR + " contains in the role name: " + newRoleName + ".");
        }
        RoleBasicInfo roleBasicInfo = roleDAO.updateRoleName(roleID, newRoleName, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateRoleName(roleID, newRoleName, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated role name of role id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Update role name by ID", roleID,
                getAuditData(tenantDomain, newRoleName), success));
        return roleBasicInfo;
    }

    @Override
    public void deleteRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreDeleteRole(roleID, tenantDomain);
        roleDAO.deleteRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostDeleteRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s deleted role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Delete role by id", roleID,
                getAuditData(tenantDomain), success));
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetGroupListOfRole(roleID, tenantDomain);
        List<UserBasicInfo> userBasicInfoList = roleDAO.getUserListOfRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetGroupListOfRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of users of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        return userBasicInfoList;
    }

    @Override
    public RoleBasicInfo updateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
            String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateUserListOfRole(roleID, newUserIDList, deletedUserIDList,
                tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.updateUserListOfRole(roleID, newUserIDList, deletedUserIDList,
                tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateUserListOfRole(roleID, newUserIDList, deletedUserIDList,
                tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated list of users of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Update users list of role by id", roleID,
                getAuditData(tenantDomain), success));
        return roleBasicInfo;
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetUserListOfRole(roleID, tenantDomain);
        List<GroupBasicInfo> groupBasicInfoList = roleDAO.getGroupListOfRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetUserListOfRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        return groupBasicInfoList;
    }

    @Override
    public RoleBasicInfo updateGroupListOfRole(String roleID, List<String> newGroupIDList,
            List<String> deletedGroupIDList, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateGroupListOfRole(roleID, newGroupIDList, deletedGroupIDList,
                tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.updateGroupListOfRole(roleID, newGroupIDList,
                deletedGroupIDList, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateGroupListOfRole(roleID, newGroupIDList, deletedGroupIDList,
                tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated list of groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Update group list of role by id", roleID,
                getAuditData(tenantDomain), success));
        return roleBasicInfo;
    }

    @Override
    public List<String> getPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetPermissionListOfRole(roleID, tenantDomain);
        List<String> permissionListOfRole = roleDAO.getPermissionListOfRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetPermissionListOfRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of permissions of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        return permissionListOfRole;
    }

    @Override
    public RoleBasicInfo setPermissionsForRole(String roleID, List<String> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreSetPermissionsForRole(roleID, permissions, tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.setPermissionsForRole(roleID, permissions, tenantDomain);
        roleManagementEventPublisherProxy.publishPostSetPermissionsForRole(roleID, permissions, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s set list of permissions of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getUser(tenantDomain), "Set permission for role by id", roleID,
                getAuditData(tenantDomain), success));
        return roleBasicInfo;
    }

    @Override
    public boolean isExistingRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleID(roleID, tenantDomain);
    }

    @Override
    public boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException,
            NotImplementedException {

        return roleDAO.isExistingRoleName(roleName, tenantDomain);
    }

    @Override
    public Set<String> getSystemRoles() {

        return roleDAO.getSystemRoles();
    }

    /**
     * Check if the role name has a domain separator character.
     * @param roleName Role name.
     * @return True if the role name has a domain separator character.
     */
    private boolean isDomainSeparatorPresent(String roleName) {

        return roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR);
    }

    private String getUser(String tenantDomain) {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotBlank(user)) {
            user = UserCoreUtil.addTenantDomainToEntry(user, tenantDomain);
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    private String getAuditData(String tenantDomain) {

        return (String.format("Tenant Domain : %s", tenantDomain));
    }

    private String getAuditData(String tenantDomain, String newRoleName) {

        return (String.format("Tenant Domain : %s, New Role Name : %s", tenantDomain, newRoleName));
    }
}
