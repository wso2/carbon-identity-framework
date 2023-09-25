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

package org.wso2.carbon.identity.role.mgt.core.v2.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.v2.Permission;
import org.wso2.carbon.identity.role.mgt.core.v2.Role;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleManagementEventPublisherProxy;
import org.wso2.carbon.identity.role.mgt.core.v2.RoleManagementService;
import org.wso2.carbon.identity.role.mgt.core.v2.dao.RoleDAO;
import org.wso2.carbon.identity.role.mgt.core.v2.dao.RoleMgtDAOFactory;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.carbon.identity.role.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.mgt.core.v2.RoleConstants.ORGANIZATION;

/**
 * Implementation of the {@link RoleManagementService} interface.
 */
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private static final Log audit = CarbonConstants.AUDIT_LOG;
    private final RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getRoleDAO();
    private final org.wso2.carbon.identity.role.mgt.core.RoleManagementService roleManagementServiceV1 =
            new org.wso2.carbon.identity.role.mgt.core.internal.RoleManagementServiceImpl();
    private static final String auditMessage
            = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private final String success = "Success";

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

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

        // Validate audience.
        if (StringUtils.isNotEmpty(audience)) {
            if (!(ORGANIZATION.equalsIgnoreCase(audience) || APPLICATION.equalsIgnoreCase(audience))) {
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid role audience");
            }
        }

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreAddRoleWithException(roleName, userList, groupList, permissions,
                audience, audienceId, tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.addRole(roleName, userList, groupList, permissions, audience, audienceId,
                tenantDomain);
        roleManagementEventPublisherProxy.publishPostAddRole(roleName, userList, groupList, permissions,
                audience, audienceId, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s add role of name : %s successfully.", getUser(tenantDomain), roleName));
        }
        audit.info(String.format(auditMessage, getInitiator(tenantDomain), "Add Role", roleName,
                getAuditData(tenantDomain), success));
        return roleDAO.getRoleBasicInfoById(roleBasicInfo.getId(), tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(limit, offset, sortBy, sortOrder,
                tenantDomain);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                                                               String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(filter, limit, offset, sortBy, sortOrder,
                tenantDomain);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(filter, limit, offset, sortBy,
                sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get filtered roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRoleWithException(roleID, tenantDomain);
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

        roleManagementServiceV1.updateRoleName(roleID, newRoleName, tenantDomain);
        return roleDAO.getRoleBasicInfoById(roleID, tenantDomain);
    }

    @Override
    public void deleteRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreDeleteRoleWithException(roleID, tenantDomain);
        roleDAO.deleteRole(roleID, tenantDomain);
        roleManagementEventPublisherProxy.publishPostDeleteRole(roleID, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s deleted role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getInitiator(tenantDomain), "Delete role by id", roleID,
                getAuditData(tenantDomain), success));
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleManagementServiceV1.getUserListOfRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
                                              String tenantDomain) throws IdentityRoleManagementException {

        roleManagementServiceV1.updateGroupListOfRole(roleID, newUserIDList, deletedUserIDList, tenantDomain);
        return roleDAO.getRoleBasicInfoById(roleID, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleManagementServiceV1.getGroupListOfRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updateGroupListOfRole(String roleID, List<String> newGroupIDList,
                                               List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        roleManagementServiceV1.updateGroupListOfRole(roleID, newGroupIDList, deletedGroupIDList, tenantDomain);
        return roleDAO.getRoleBasicInfoById(roleID, tenantDomain);
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetPermissionListOfRoleWithException(roleID, tenantDomain);
        List<Permission> permissionListOfRole = roleDAO.getPermissionListOfRole(roleID, tenantDomain);
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

        roleManagementServiceV1.setPermissionsForRole(roleID, permissions, tenantDomain);
        return roleDAO.getRoleBasicInfoById(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updatePermissionListOfRole(String roleID, List<Permission> addedPermissions,
                                                       List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdatePermissionsForRoleWithException(roleID, addedPermissions,
                deletedPermissions, tenantDomain);
        removeSimilarPermissions(addedPermissions, deletedPermissions);
        RoleBasicInfo roleBasicInfo = roleDAO.updatePermissionListOfRole(roleID, addedPermissions,
                deletedPermissions, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdatePermissionsForRole(roleID, addedPermissions,
                deletedPermissions, tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s set list of permissions of role of id : %s successfully.",
                    getUser(tenantDomain), roleID));
        }
        audit.info(String.format(auditMessage, getInitiator(tenantDomain), "Set permission for role by id",
                roleID, getAuditData(tenantDomain), success));
        return roleBasicInfo;
    }

    @Override
    public boolean isExistingRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return roleManagementServiceV1.isExistingRole(roleID, tenantDomain);
    }

    @Override
    public boolean isExistingRoleName(String roleName, String tenantDomain) throws IdentityRoleManagementException {

        return roleManagementServiceV1.isExistingRoleName(roleName, tenantDomain);
    }

    @Override
    public Set<String> getSystemRoles() {

        return roleManagementServiceV1.getSystemRoles();
    }

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        return roleManagementServiceV1.getRolesCount(tenantDomain);
    }

    @Override
    public Role getRoleWithoutUsers(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        Role role = roleDAO.getRole(roleID, tenantDomain);
        role.setUsers(null);
        return role;
    }

    @Override
    public String getRoleNameByRoleId(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return roleManagementServiceV1.getRoleNameByRoleId(roleID, tenantDomain);
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

    /**
     * Check if the role name has a domain separator character.
     * @param roleName Role name.
     * @return True if the role name has a domain separator character.
     */
    private boolean isDomainSeparatorPresent(String roleName) {

        return roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR);
    }

    private String getAuditData(String tenantDomain) {

        return (String.format("Tenant Domain : %s", tenantDomain));
    }

    /**
     * Get the initiator for audit logs.
     *
     * @param tenantDomain Tenant Domain.
     * @return Initiator based on whether log masking is enabled or not.
     */
    private static String getInitiator(String tenantDomain) {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (LoggerUtils.isLogMaskingEnable) {
            if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(tenantDomain)) {
                String initiator = IdentityUtil.getInitiatorId(user, tenantDomain);
                if (StringUtils.isNotBlank(initiator)) {
                    return initiator;
                }
            }
            if (StringUtils.isNotBlank(user)) {
                return LoggerUtils.getMaskedContent(user + "@" + tenantDomain);
            }
            return LoggerUtils.getMaskedContent(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } else if (StringUtils.isNotBlank(user)) {
            return user + "@" + tenantDomain;
        }
        return CarbonConstants.REGISTRY_SYSTEM_USERNAME;
    }

    public static void removeSimilarPermissions(List<Permission> arr1, List<Permission> arr2) {
        List<Permission> toRemove = new ArrayList<>();

        for (Permission p1 : arr1) {
            for (Permission p2 : arr2) {
                if (p1.getName().equals(p2.getName())) {
                    toRemove.add(p1);
                    break;
                }
            }
        }

        arr1.removeAll(toRemove);
        arr2.removeAll(toRemove);
    }
}
