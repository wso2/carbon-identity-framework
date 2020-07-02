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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.role.mgt.core.GroupBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.mgt.core.Role;
import org.wso2.carbon.identity.role.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.mgt.core.UserBasicInfo;
import org.wso2.carbon.identity.role.mgt.core.dao.RoleDAO;
import org.wso2.carbon.identity.role.mgt.core.dao.RoleMgtDAOFactory;

import java.util.List;

/**
 * Implementation of the {@link RoleManagementService} interface.
 */
public class RoleManagementServiceImpl implements RoleManagementService {

    private static final Log log = LogFactory.getLog(RoleManagementServiceImpl.class);
    private RoleDAO roleDAO = RoleMgtDAOFactory.getInstance().getRoleDAO();

    @Override
    public RoleBasicInfo addRole(String roleName, List<String> userList, List<String> groupList,
            List<String> permissions, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.addRole(roleName, userList, groupList, permissions, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(int limit, int offset, String sortBy, String sortOrder, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getRoles(limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public List<RoleBasicInfo> getRoles(String filter, int limit, int offset, String sortBy, String sortOrder,
            String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRoles(filter, limit, offset, sortBy, sortOrder, tenantDomain);
    }

    @Override
    public Role getRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.getRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updateRoleName(String roleID, String newRoleName, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.updateRoleName(roleID, newRoleName, tenantDomain);
    }

    @Override
    public void deleteRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        roleDAO.deleteRole(roleID, tenantDomain);
    }

    @Override
    public List<UserBasicInfo> getUserListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getUserListOfRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updateUserListOfRole(String roleID, List<String> newUserIDList, List<String> deletedUserIDList,
            String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.updateUserListOfRole(roleID, newUserIDList, deletedUserIDList, tenantDomain);
    }

    @Override
    public List<GroupBasicInfo> getGroupListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getGroupListOfRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo updateGroupListOfRole(String roleID, List<String> newGroupIDList,
            List<String> deletedGroupIDList, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.updateGroupListOfRole(roleID, newGroupIDList, deletedGroupIDList, tenantDomain);
    }

    @Override
    public List<String> getPermissionListOfRole(String roleID, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.getPermissionListOfRole(roleID, tenantDomain);
    }

    @Override
    public RoleBasicInfo setPermissionsForRole(String roleID, List<String> permissions, String tenantDomain)
            throws IdentityRoleManagementException {

        return roleDAO.setPermissionsForRole(roleID, permissions, tenantDomain);
    }

    @Override
    public boolean isExistingRole(String roleID, String tenantDomain) throws IdentityRoleManagementException {

        return roleDAO.isExistingRoleID(roleID, tenantDomain);
    }
}
