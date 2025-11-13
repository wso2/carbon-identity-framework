/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.role.v2.mgt.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleDAO;
import org.wso2.carbon.identity.role.v2.mgt.core.dao.RoleMgtDAOFactory;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementClientException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementServerException;
import org.wso2.carbon.identity.role.v2.mgt.core.internal.RoleManagementServiceComponentHolder;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.RoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.GroupBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.IdpGroup;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleDTO;
import org.wso2.carbon.identity.role.v2.mgt.core.model.UserBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.util.RoleManagementUtils;
import org.wso2.carbon.identity.role.v2.mgt.core.util.UserIDResolver;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_AUDIENCE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

/**
 * Implementation of the {@link RoleManagementService} interface.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService",
                "service.scope=singleton"
        }
)
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private final RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getCacheBackedRoleDAO();
    private final UserIDResolver userIDResolver = new UserIDResolver();
    private static final String IS_FRAGMENT_APP = "isFragmentApp";

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
                                 List<Permission> permissions, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            if (!RoleManagementUtils.isAllowSystemPrefixForRole() &&
                    StringUtils.startsWithIgnoreCase(roleName, UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX)) {
                String errorMessage = String.format("Invalid role name: %s. Role names with the prefix: %s, is not " +
                                "allowed to be created from externally in the system.", roleName,
                        UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX);
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
            }
            if (roleName == null || roleName.isEmpty()) {
                String errorMessage = "Role name cannot be empty.";
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
            }
            if (roleName.length() > 255) {
                String errorMessage = "Provided role name exceeds the maximum length of 255 characters.";
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
            }
            if (isDomainSeparatorPresent(roleName)) {
                // SCIM2 API only adds roles to the internal domain.
                throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid character: "
                        + UserCoreConstants.DOMAIN_SEPARATOR + " contains in the role name: " + roleName + ".");
            }
            List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.
                    getInstance().getRoleManagementListenerList();
            for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
                if (roleManagementListener.isEnable()) {
                    roleManagementListener.preAddRole(roleName, userList, groupList,
                            permissions, audience, audienceId, tenantDomain);
                }
            }

            RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                    .getInstance();
            roleManagementEventPublisherProxy.publishPreAddRoleWithException(roleName, userList, groupList,
                    permissions, audience, audienceId, tenantDomain);

            // Validate audience.
            if (StringUtils.isNotEmpty(audience)) {
                if (!(ORGANIZATION.equalsIgnoreCase(audience) || APPLICATION.equalsIgnoreCase(audience))) {
                    throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                            "Invalid role audience");
                }
                if (ORGANIZATION.equalsIgnoreCase(audience)) {
                    RoleManagementUtils.validateOrganizationRoleAudience(audienceId, tenantDomain);
                    audience = ORGANIZATION;
                }
                if (APPLICATION.equalsIgnoreCase(audience)) {
                    // audience validation done using listener.
                    audience = APPLICATION;
                }
            } else {
                audience = ORGANIZATION;
                audienceId = RoleManagementUtils.getOrganizationIdByTenantDomain(tenantDomain);
            }
            RoleManagementUtils.validatePermissions(permissions, audience, tenantDomain);
            RoleBasicInfo roleBasicInfo = roleDAO.addRole(roleName, userList, groupList, permissions, audience,
                    audienceId, tenantDomain);
            roleManagementEventPublisherProxy.publishPostAddRole(roleBasicInfo.getId(), roleName, userList, groupList,
                    permissions, audience, audienceId, tenantDomain);
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s added role of name : %s successfully.", getUser(tenantDomain), roleName));
            }
            RoleBasicInfo role = roleDAO.getRoleBasicInfoById(roleBasicInfo.getId(), tenantDomain);
            for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
                if (roleManagementListener.isEnable()) {
                    roleManagementListener.postAddRole(role, roleName, userList,
                            groupList, permissions, audience, audienceId, tenantDomain);
                }
            }
            return role;
        } finally {
            if (IdentityUtil.threadLocalProperties.get().get(IS_FRAGMENT_APP) != null) {
                IdentityUtil.threadLocalProperties.get().remove(IS_FRAGMENT_APP);
            }
        }
    }

    @Override
    public List<RoleBasicInfo> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoles(limit, offset, sortBy, sortOrder, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(limit, offset, sortBy, sortOrder,
                tenantDomain);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(limit, offset, sortBy, sortOrder, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoles(roleBasicInfoList, limit, offset, sortBy, sortOrder,
                        tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public List<Role> getRoles(Integer limit, Integer offset, String sortBy, String sortOrder, String tenantDomain,
                               List<String> requiredAttributes) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoles(limit, offset, sortBy, sortOrder, tenantDomain, requiredAttributes);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(limit, offset, sortBy, sortOrder,
                tenantDomain, requiredAttributes);
        List<Role> rolesList = roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain, requiredAttributes);
        roleManagementEventPublisherProxy.publishPostGetRoles(limit, offset, sortBy, sortOrder, tenantDomain,
                requiredAttributes);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoles(rolesList, limit, offset, sortBy, sortOrder, tenantDomain,
                        requiredAttributes);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles successfully.", getUser(tenantDomain)));
        }
        return rolesList;
    }

    @Override
    public List<RoleBasicInfo> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                                        String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(filter, limit, offset, sortBy, sortOrder,
                tenantDomain);
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
        List<RoleBasicInfo> roleBasicInfoList = roleDAO.getRoles(expressionNodes, limit, offset, sortBy,
                sortOrder, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoles(roleBasicInfoList, filter, limit, offset, sortBy, sortOrder,
                        tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get filtered roles successfully.", getUser(tenantDomain)));
        }
        return roleBasicInfoList;
    }

    @Override
    public List<Role> getRoles(String filter, Integer limit, Integer offset, String sortBy, String sortOrder,
                               String tenantDomain, List<String> requiredAttributes)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain,
                        requiredAttributes);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesWithException(filter, limit, offset, sortBy, sortOrder,
                tenantDomain, requiredAttributes);
        List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
        List<Role> rolesList = roleDAO.getRoles(expressionNodes, limit, offset, sortBy,
                sortOrder, tenantDomain, requiredAttributes);
        roleManagementEventPublisherProxy.publishPostGetRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain,
                requiredAttributes);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoles(rolesList, filter, limit, offset, sortBy, sortOrder, tenantDomain,
                        requiredAttributes);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get filtered roles successfully.", getUser(tenantDomain)));
        }
        return rolesList;
    }

    @Override
    public Role getRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRoleWithException(roleId, tenantDomain);
        Role role = roleDAO.getRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRole(role, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get role of id : %s successfully.", getUser(tenantDomain), roleId));
        }
        return role;
    }

    @Override
    public Role getRole(String roleId) throws IdentityRoleManagementException {

        return roleDAO.getRole(roleId);
    }

    @Override
    public RoleBasicInfo getRoleBasicInfoById(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleBasicInfo(roleId, tenantDomain);
            }
        }
        RoleBasicInfo role = roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleBasicInfo(role, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s executed get role basic info by id : %s successfully.", getUser(tenantDomain),
                    roleId));
        }
        return role;
    }

    @Override
    public RoleBasicInfo updateRoleName(String roleId, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preUpdateRoleName(roleId, newRoleName, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateRoleNameWithException(roleId, newRoleName, tenantDomain);

        if (newRoleName == null || newRoleName.isEmpty()) {
            String errorMessage = "Role name cannot be empty.";
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        if (newRoleName.length() > 255) {
            String errorMessage = "Provided role name exceeds the maximum length of 255 characters.";
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), errorMessage);
        }
        if (isDomainSeparatorPresent(newRoleName)) {
            // SCIM2 API only adds roles to the internal domain.
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid character: "
                    + UserCoreConstants.DOMAIN_SEPARATOR + " contains in the role name: " + newRoleName + ".");
        }
        roleDAO.updateRoleName(roleId, newRoleName, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateRoleName(roleId, newRoleName, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postUpdateRoleName(roleId, newRoleName, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated role name of role id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public void deleteRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preDeleteRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreDeleteRoleWithException(roleId, tenantDomain);
        roleDAO.deleteRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostDeleteRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postDeleteRole(roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s deleted role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetUserListOfRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetUserListOfRoleWithException(roleId, tenantDomain);
        List<UserBasicInfo> userBasicInfoList = roleDAO.getUserListOfRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetUserListOfRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetUserListOfRole(userBasicInfoList, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of users of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return userBasicInfoList;
    }

    @Override
    public List<UserBasicInfo> getUserListOfRoles(String filter, Integer limit, Integer offset,
                                                  String sortBy, String sortOrder, String tenantDomain,
                                                  String userStoreDomain) throws IdentityRoleManagementException {

        List<ExpressionNode> expressionNodes = getExpressionNodes(filter);
        return roleDAO.getUserListOfRoles(expressionNodes, limit, offset, sortBy, sortOrder, tenantDomain,
                userStoreDomain);
    }

    @Override
    public RoleBasicInfo updateUserListOfRole(String roleId, List<String> newUserIDList, List<String> deletedUserIDList,
                                              String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preUpdateUserListOfRole(roleId, newUserIDList, deletedUserIDList, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateUserListOfRoleWithException(roleId, newUserIDList,
                deletedUserIDList,
                tenantDomain);
        // Validate the user removal operation based on the default system roles.
        validateUserRemovalFromRole(deletedUserIDList, roleId, tenantDomain);
        roleDAO.updateUserListOfRole(roleId, newUserIDList, deletedUserIDList, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateUserListOfRole(roleId, newUserIDList, deletedUserIDList,
                tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postUpdateUserListOfRole(roleId, newUserIDList, deletedUserIDList, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated list of users of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetGroupListOfRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetGroupListOfRoleWithException(roleId, tenantDomain);
        List<GroupBasicInfo> groupBasicInfoList = roleDAO.getGroupListOfRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetGroupListOfRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetGroupListOfRole(groupBasicInfoList, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return groupBasicInfoList;
    }

    @Override
    public RoleBasicInfo updateGroupListOfRole(String roleId, List<String> newGroupIDList,
                                               List<String> deletedGroupIDList, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preUpdateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList,
                        tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateGroupListOfRoleWithException(roleId, newGroupIDList,
                deletedGroupIDList, tenantDomain);
        roleDAO.updateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList,
                tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postUpdateGroupListOfRole(roleId, newGroupIDList, deletedGroupIDList,
                        tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated list of groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public List<IdpGroup> getIdpGroupListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetIdpGroupListOfRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetIdpGroupListOfRoleWithException(roleId, tenantDomain);
        List<IdpGroup> idpGroups = roleDAO.getIdpGroupListOfRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostIdpGetGroupListOfRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetIdpGroupListOfRole(idpGroups, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of idp groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return idpGroups;
    }

    @Override
    public RoleBasicInfo updateIdpGroupListOfRole(String roleId, List<IdpGroup> newGroupList,
                                                  List<IdpGroup> deletedGroupList, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preUpdateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList,
                        tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdateIdpGroupListOfRoleWithException(roleId, newGroupList,
                deletedGroupList, tenantDomain);
        removeSimilarIdpGroups(newGroupList, deletedGroupList);
        roleDAO.updateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList,
                tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postUpdateIdpGroupListOfRole(roleId, newGroupList, deletedGroupList,
                        tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s updated list of idp groups of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public List<Permission> getPermissionListOfRole(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetPermissionListOfRole(roleId, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetPermissionListOfRoleWithException(roleId, tenantDomain);
        List<Permission> permissionListOfRole = roleDAO.getPermissionListOfRole(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetPermissionListOfRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetPermissionListOfRole(permissionListOfRole, roleId, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get list of permissions of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return permissionListOfRole;
    }

    @Override
    public List<String> getPermissionListOfRoles(List<String> roleIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetPermissionListOfRoles(roleIds, tenantDomain);
            }
        }
        List<String> permissionListOfRoles = roleDAO.getPermissionListOfRoles(roleIds, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetPermissionListOfRoles(permissionListOfRoles, roleIds, tenantDomain);
            }
        }
        return permissionListOfRoles;
    }

    @Override
    public RoleBasicInfo updatePermissionListOfRole(String roleId, List<Permission> addedPermissions,
                                                    List<Permission> deletedPermissions, String tenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreUpdatePermissionsForRoleWithException(roleId, addedPermissions,
                deletedPermissions, tenantDomain);
        removeSimilarPermissions(addedPermissions, deletedPermissions);
        RoleBasicInfo roleBasicInfo = roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preUpdatePermissionsForRole(roleId, addedPermissions, deletedPermissions,
                        roleBasicInfo.getAudience(), roleBasicInfo.getAudienceId(), tenantDomain);
            }
        }
        RoleManagementUtils.validatePermissions(addedPermissions, roleBasicInfo.getAudience(), tenantDomain);
        roleDAO.updatePermissionListOfRole(roleId, addedPermissions,
                deletedPermissions, tenantDomain);
        roleManagementEventPublisherProxy.publishPostUpdatePermissionsForRole(roleId, addedPermissions,
                deletedPermissions, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postUpdatePermissionsForRole(roleId, addedPermissions, deletedPermissions,
                        roleBasicInfo.getAudience(), roleBasicInfo.getAudienceId(), tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s set list of permissions of role of id : %s successfully.",
                    getUser(tenantDomain), roleId));
        }
        return roleDAO.getRoleBasicInfoById(roleId, tenantDomain);
    }

    @Override
    public boolean isExistingRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleID(roleId, tenantDomain);
    }

    @Override
    public boolean isExistingRoleName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleName(roleName, audience, audienceId, tenantDomain);
    }

    @Override
    public Set<String> getSystemRoles() {

        return roleDAO.getSystemRoles();
    }

    @Override
    public int getRolesCount(String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRolesCount(tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy =
                RoleManagementEventPublisherProxy.getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesCountWithException(tenantDomain);
        int count = roleDAO.getRolesCount(tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRolesCount(tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRolesCount(count, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s get roles count successfully.", getUser(tenantDomain)));
        }
        return count;
    }

    @Override
    public int getRolesCount(String searchFilter, String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRolesCount(searchFilter, tenantDomain);
            }
        }
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy =
                RoleManagementEventPublisherProxy.getInstance();
        roleManagementEventPublisherProxy.publishPreGetRolesCountWithException(searchFilter, tenantDomain);
        List<ExpressionNode> expressionNodes = getExpressionNodes(searchFilter);
        int count = roleDAO.getRolesCount(expressionNodes, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRolesCount(searchFilter, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRolesCount(count, searchFilter, tenantDomain);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Get roles count for the filter %s & tenant domain %s is successful.",
                    searchFilter, tenantDomain));
        }
        return count;
    }

    @Override
    public Role getRoleWithoutUsers(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreGetRoleWithException(roleId, tenantDomain);
        Role role = roleDAO.getRoleWithoutUsers(roleId, tenantDomain);
        roleManagementEventPublisherProxy.publishPostGetRole(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRole(role, roleId, tenantDomain);
            }
        }
        return role;
    }

    @Override
    public String getRoleNameByRoleId(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoleNameByID(roleId, tenantDomain);
    }

    @Override
    public String getRoleIdByName(String roleName, String audience, String audienceId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoleIdByName(roleName, audience, audienceId, tenantDomain);
    }

    @Override
    public void addMainRoleToSharedRoleRelationship(String mainRoleUUID, String sharedRoleUUID,
                                                    String mainRoleTenantDomain, String sharedRoleTenantDomain)
            throws IdentityRoleManagementException {

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreAddMainRoleToSharedRoleRelationshipWithException(mainRoleUUID,
                sharedRoleUUID, mainRoleTenantDomain, sharedRoleTenantDomain);
        roleDAO.addMainRoleToSharedRoleRelationship(mainRoleUUID, sharedRoleUUID, mainRoleTenantDomain,
                sharedRoleTenantDomain);
        roleManagementEventPublisherProxy.publishPostAddMainRoleToSharedRoleRelationship(mainRoleUUID, sharedRoleUUID,
                mainRoleTenantDomain, sharedRoleTenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfUser(String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleListOfUser(userId, tenantDomain);
            }
        }
        List<RoleBasicInfo> roles = roleDAO.getRoleListOfUser(userId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleListOfUser(roles, userId, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleListOfGroups(groupIds, tenantDomain);
            }
        }
        List<RoleBasicInfo> roles = roleDAO.getRoleListOfGroups(groupIds, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleListOfGroups(roles, groupIds, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public List<RoleBasicInfo> getRoleListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleListOfIdpGroups(groupIds, tenantDomain);
            }
        }
        List<RoleBasicInfo> roles = roleDAO.getRoleListOfIdpGroups(groupIds, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleListOfIdpGroups(roles, groupIds, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public List<String> getRoleIdListOfUser(String userId, String tenantDomain) throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleIdListOfUser(userId, tenantDomain);
            }
        }
        List<String> roles = roleDAO.getRoleIdListOfUser(userId, tenantDomain);
        addEveryoneRoleToRoleList(roles, tenantDomain);

        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleIdListOfUser(roles, userId, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public List<String> getRoleIdListOfGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleIdListOfGroups(groupIds, tenantDomain);
            }
        }
        List<String> roles = roleDAO.getRoleIdListOfGroups(groupIds, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleIdListOfGroups(groupIds, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public List<String> getRoleIdListOfIdpGroups(List<String> groupIds, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetRoleIdListOfIdpGroups(groupIds, tenantDomain);
            }
        }
        List<String> roles = roleDAO.getRoleIdListOfIdpGroups(groupIds, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetRoleIdListOfIdpGroups(roles, groupIds, tenantDomain);
            }
        }
        return roles;
    }

    @Override
    public void deleteRolesByApplication(String applicationId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preDeleteRolesByApplication(applicationId, tenantDomain);
            }
        }
        roleDAO.deleteRolesByApplication(applicationId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postDeleteRolesByApplication(applicationId, tenantDomain);
            }
        }
    }

    @Override
    public Map<String, String> getMainRoleToSharedRoleMappingsBySubOrg(List<String> roleIds,
                                                                       String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getMainRoleToSharedRoleMappingsBySubOrg(roleIds, subOrgTenantDomain);
    }

    @Override
    public Map<String, String> getSharedRoleToMainRoleMappingsBySubOrg(List<String> roleIds, String subOrgTenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getSharedRoleToMainRoleMappingsBySubOrg(roleIds, subOrgTenantDomain);
    }

    @Override
    public List<String> getAssociatedApplicationByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        List<RoleManagementListener> roleManagementListenerList =
                RoleManagementServiceComponentHolder.getInstance().getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preGetAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
            }
        }
        List<String> associatedApplicationIds = roleDAO.getAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.postGetAssociatedApplicationIdsByRoleId(associatedApplicationIds,
                        roleId, tenantDomain);
            }
        }
        return associatedApplicationIds;
    }

    @Override
    public List<RoleDTO> getSharedHybridRoles(String roleId, int mainTenantId) throws IdentityRoleManagementException {

        return roleDAO.getSharedHybridRoles(roleId, mainTenantId);
    }

    /**
     * Get user from tenant domain.
     *
     * @param tenantDomain tenantDomain.
     * @return user.
     */
    private String getUser(String tenantDomain) {

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



    /**
     * Check if the role name has a domain separator character.
     *
     * @param roleName Role name.
     * @return True if the role name has a domain separator character.
     */
    private boolean isDomainSeparatorPresent(String roleName) {

        return roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR);
    }

    /**
     * Remove similar permissions.
     *
     * @param arr1 Array of permissions.
     * @param arr2 Array of permissions.
     */
    private void removeSimilarPermissions(List<Permission> arr1, List<Permission> arr2) {

        if (arr1 == null || arr2 == null) {
            return;
        }

        List<Permission> common = new ArrayList<>(arr1);
        common.retainAll(arr2);

        arr1.removeAll(common);
        arr2.removeAll(common);
    }

    /**
     * Remove similar idp groups.
     *
     * @param arr1 Array of idp groups.
     * @param arr2 Array of idp groups.
     */
    private void removeSimilarIdpGroups(List<IdpGroup> arr1, List<IdpGroup> arr2) {

        List<IdpGroup> common = new ArrayList<>(arr1);
        common.retainAll(arr2);

        arr1.removeAll(common);
        arr2.removeAll(common);
    }

    /**
     * Get the filter node as a list.
     *
     * @param filter Filter string.
     * @throws IdentityRoleManagementException Error when validate filters.
     */
    private List<ExpressionNode> getExpressionNodes(String filter) throws IdentityRoleManagementException {

        List<ExpressionNode> expressionNodes = new ArrayList<>();
        filter = StringUtils.isBlank(filter) ? StringUtils.EMPTY : filter;
        try {
            if (StringUtils.isNotBlank(filter)) {
                FilterTreeBuilder filterTreeBuilder = new FilterTreeBuilder(filter);
                Node rootNode = filterTreeBuilder.buildTree();
                setExpressionNodeList(rootNode, expressionNodes);
            }
            return expressionNodes;
        } catch (IOException | IdentityException e) {
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(), "Invalid filter");
        }
    }

    /**
     * Set the node values as list of expression.
     *
     * @param node       filter node.
     * @param expression list of expression.
     */
    private void setExpressionNodeList(Node node, List<ExpressionNode> expression) {

        if (node instanceof ExpressionNode) {
            if (StringUtils.isNotBlank(((ExpressionNode) node).getAttributeValue())) {
                expression.add((ExpressionNode) node);
            }
        } else if (node instanceof OperationNode) {
            setExpressionNodeList(node.getLeftNode(), expression);
            setExpressionNodeList(node.getRightNode(), expression);
        }
    }

    /**
     * Validate user removal from role.
     *
     * @param deletedUserIDList Deleted user ID list.
     * @param roleId            Role ID.
     * @param tenantDomain      Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating user removal from role.
     */
    private void validateUserRemovalFromRole(List<String> deletedUserIDList, String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        if (!IdentityUtil.isSystemRolesEnabled() || deletedUserIDList.isEmpty()) {
            return;
        }
        try {
            boolean isOrganization = OrganizationManagementUtil.isOrganization(tenantDomain);
            // No restriction when removing access from an organization user.
            if (isOrganization) {
                return;
            }
        } catch (OrganizationManagementException e) {
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format("Error while checking the tenant domain: %s is an organization.", tenantDomain), e);
        }

        try {
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String adminUserName = userRealm.getRealmConfiguration().getAdminUserName();
            String adminRoleName = userRealm.getRealmConfiguration().getAdminRoleName();

            org.wso2.carbon.user.core.UserStoreManager userStoreManager =
                    (org.wso2.carbon.user.core.UserStoreManager) userRealm
                            .getUserStoreManager();
            boolean isUseCaseSensitiveUsernameForCacheKeys = IdentityUtil
                    .isUseCaseSensitiveUsernameForCacheKeys(userStoreManager);

            Role role = getRole(roleId, tenantDomain);
            // Only the tenant owner can remove users from Administrator role.
            if ((StringUtils.equals(adminRoleName, role.getName())
                    && RoleConstants.ORGANIZATION.equals(role.getAudience()))
                    || (RoleConstants.ADMINISTRATOR.equals(role.getName()) &&
                            RoleConstants.CONSOLE_APP_AUDIENCE_NAME.equals(role.getAudienceName()))) {
                if ((isUseCaseSensitiveUsernameForCacheKeys && !StringUtils.equals(username, adminUserName)) || (
                        !isUseCaseSensitiveUsernameForCacheKeys && !StringUtils
                                .equalsIgnoreCase(username, adminUserName))) {
                    String errorMessage = "Invalid operation. Only the tenant owner can remove users from the role: %s";
                    throw new IdentityRoleManagementClientException(RoleConstants.Error.OPERATION_FORBIDDEN.getCode(),
                            String.format(errorMessage, RoleConstants.ADMINISTRATOR));
                } else {
                    List<String> deletedUserNamesList = getUserNamesByIDs(deletedUserIDList, tenantDomain);
                    // Tenant owner cannot be removed from Administrator role.
                    if (deletedUserNamesList.contains(adminUserName)) {
                        String errorMessage = "Invalid operation. Tenant owner cannot be removed from the role: %s";
                        throw new IdentityRoleManagementClientException(RoleConstants.Error.OPERATION_FORBIDDEN
                                .getCode(),
                                String.format(errorMessage, RoleConstants.ADMINISTRATOR));
                    }
                }
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while validating user removal from the role: %s in the tenantDomain: %s";
            throw new IdentityRoleManagementServerException(RoleConstants.Error.UNEXPECTED_SERVER_ERROR.getCode(),
                    String.format(errorMessage, roleId, tenantDomain), e);
        }
    }

    /**
     * Get usernames by IDs.
     *
     * @param userIDs      User IDs.
     * @param tenantDomain Tenant Domain.
     * @return List of usernames.
     * @throws IdentityRoleManagementException IdentityRoleManagementException.
     */
    private List<String> getUserNamesByIDs(List<String> userIDs, String tenantDomain)
            throws IdentityRoleManagementException {

        return userIDResolver.getNamesByIDs(userIDs, tenantDomain);
    }

    /**
     * Get everyone role id by tenant domain.
     *
     * @param tenantDomain Tenant domain.
     * @return every one role id.
     * @throws IdentityRoleManagementException if error occurred while retrieving everyone role id.
     */
    private String getEveryoneRoleId(String tenantDomain) throws IdentityRoleManagementException {

        String everyOneRoleName = RoleManagementUtils.getEveryOneRoleName(tenantDomain);
        String orgId = RoleManagementUtils.getOrganizationId(tenantDomain);
        return getRoleIdByName(everyOneRoleName, ORGANIZATION, orgId, tenantDomain);
    }

    private void addEveryoneRoleToRoleList(List<String> roles, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            if (!OrganizationManagementUtil.isOrganization(tenantDomain)) {
                log.debug("Adding everyone role of tenant to the user role list.");
                roles.add(getEveryoneRoleId(tenantDomain));
            }
        } catch (OrganizationManagementException e) {
            throw new IdentityRoleManagementException(String.format("Error while checking whether the tenant domain: " +
                    "%s is an organization.", tenantDomain), e);
        }
    }
}
