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

package org.wso2.carbon.identity.role.v2.mgt.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.model.ExpressionNode;
import org.wso2.carbon.identity.core.model.FilterTreeBuilder;
import org.wso2.carbon.identity.core.model.Node;
import org.wso2.carbon.identity.core.model.OperationNode;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
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
import org.wso2.carbon.user.core.UserCoreConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_AUDIENCE;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_PERMISSION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

/**
 * Implementation of the {@link RoleManagementService} interface.
 */
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private final RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getRoleDAO();

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
        List<RoleManagementListener> roleManagementListenerList = RoleManagementServiceComponentHolder.getInstance()
                .getRoleManagementListenerList();
        for (RoleManagementListener roleManagementListener : roleManagementListenerList) {
            if (roleManagementListener.isEnable()) {
                roleManagementListener.preAddRole(roleName, userList, groupList,
                        permissions, audience, audienceId, tenantDomain);
            }
        }

        RoleManagementEventPublisherProxy roleManagementEventPublisherProxy = RoleManagementEventPublisherProxy
                .getInstance();
        roleManagementEventPublisherProxy.publishPreAddRoleWithException(roleName, userList, groupList, permissions,
                audience, audienceId, tenantDomain);

        // Validate audience.
        if (StringUtils.isNotEmpty(audience)) {
            if (!(ORGANIZATION.equalsIgnoreCase(audience) || APPLICATION.equalsIgnoreCase(audience))) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(), "Invalid role audience");
            }
            if (ORGANIZATION.equalsIgnoreCase(audience)) {
                validateOrganizationRoleAudience(audienceId, tenantDomain);
                audience = ORGANIZATION;
            }
            if (APPLICATION.equalsIgnoreCase(audience)) {
                // audience validation done using listener.
                audience = APPLICATION;
            }
        } else {
            audience = ORGANIZATION;
            audienceId = getOrganizationIdByTenantDomain(tenantDomain);
        }
        validatePermissions(permissions, audience, audienceId, tenantDomain);
        RoleBasicInfo roleBasicInfo = roleDAO.addRole(roleName, userList, groupList, permissions, audience, audienceId,
                tenantDomain);
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
        doPreValidateRoleDeletion(roleId, tenantDomain);
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

    private void doPreValidateRoleDeletion(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleBasicInfo roleBasicInfo = getRoleBasicInfoById(roleId, tenantDomain);
        String roleAudience = roleBasicInfo.getAudience();
        if (APPLICATION.equalsIgnoreCase(roleAudience)) {
            return;
        }
        List<String> associatedApplicationByRoleId = getAssociatedApplicationByRoleId(roleId, tenantDomain);
        if (CollectionUtils.isNotEmpty(associatedApplicationByRoleId)) {
            throw new IdentityRoleManagementClientException(INVALID_REQUEST.getCode(),
                    "Unable to delete the role since it is associated with applications.");
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
        validatePermissions(addedPermissions, roleBasicInfo.getAudience(), roleBasicInfo.getAudienceId(), tenantDomain);
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

        roleDAO.addMainRoleToSharedRoleRelationship(mainRoleUUID, sharedRoleUUID, mainRoleTenantDomain,
                sharedRoleTenantDomain);
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
    public List<String> getAssociatedApplicationByRoleId(String roleId, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getAssociatedApplicationIdsByRoleId(roleId, tenantDomain);
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
     * Get organization ID by tenantDomain.
     *
     * @param tenantDomain tenantDomain.
     * @throws IdentityRoleManagementException Error occurred while retrieving organization id.
     */
    private String getOrganizationIdByTenantDomain(String tenantDomain) throws IdentityRoleManagementException {

        try {
            return RoleManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);

        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while retrieving the organization id for the given tenantDomain: "
                    + tenantDomain;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate organization role audience.
     *
     * @param audienceId               Audience ID.
     * @param roleCreationTenantDomain Role creation tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating organization role audience.
     */
    private void validateOrganizationRoleAudience(String audienceId, String roleCreationTenantDomain)
            throws IdentityRoleManagementException {

        try {
            OrganizationManager organizationManager = RoleManagementServiceComponentHolder.getInstance()
                    .getOrganizationManager();
            String orgIdOfTenantDomain = organizationManager.resolveOrganizationId(roleCreationTenantDomain);
            if (orgIdOfTenantDomain == null || !orgIdOfTenantDomain.equalsIgnoreCase(audienceId)) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Invalid audience. Given Organization id: " + audienceId + " is invalid");
            }
            if (!organizationManager.isOrganizationExistById(audienceId)) {
                throw new IdentityRoleManagementClientException(INVALID_AUDIENCE.getCode(),
                        "Invalid audience. No organization found with organization id: " + audienceId);
            }
        } catch (OrganizationManagementException e) {
            String errorMessage = "Error while checking the organization exist by id : " + audienceId;
            throw new IdentityRoleManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), errorMessage, e);
        }
    }

    /**
     * Validate permissions.
     *
     * @param permissions  Permissions.
     * @param audience     Audience.
     * @param audienceId   Audience ID.
     * @param tenantDomain Tenant domain.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    private void validatePermissions(List<Permission> permissions, String audience, String audienceId,
                                     String tenantDomain)
            throws IdentityRoleManagementException {

        if (audience.equals(ORGANIZATION)) {
            validatePermissionsForOrganization(permissions, tenantDomain);
        }
    }

    /**
     * Validate permissions for organization audience.
     *
     * @param permissions Permissions.
     * @throws IdentityRoleManagementException Error occurred while validating permissions.
     */
    private void validatePermissionsForOrganization(List<Permission> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            List<Scope> scopes = RoleManagementServiceComponentHolder.getInstance()
                    .getApiResourceManager().getScopesByTenantDomain(tenantDomain, "");
            List<String> scopeNameList = new ArrayList<>();
            for (Scope scope : scopes) {
                scopeNameList.add(scope.getName());
            }
            for (Permission permission : permissions) {

                if (!scopeNameList.contains(permission.getName())) {
                    throw new IdentityRoleManagementClientException(INVALID_PERMISSION.getCode(),
                            "Permission: " + permission.getName() + " not found");
                }
            }
        } catch (APIResourceMgtException e) {
            throw new IdentityRoleManagementException("Error while retrieving scopes", "Error while retrieving scopes "
                    + "for tenantDomain: " + tenantDomain, e);
        }
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
}
