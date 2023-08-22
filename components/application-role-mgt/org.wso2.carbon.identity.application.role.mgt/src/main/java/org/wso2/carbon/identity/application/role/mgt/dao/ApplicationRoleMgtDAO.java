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

package org.wso2.carbon.identity.application.role.mgt.dao;

import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementServerException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;

import java.util.List;

/**
 * Application role DAO interface.
 */
public interface ApplicationRoleMgtDAO {

    ApplicationRole addApplicationRole(ApplicationRole applicationRole, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    ApplicationRole getApplicationRoleById(String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementServerException;

    void updateApplicationRole(String applicationId, String roleId, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    void deleteApplicationRole(String roleId, String tenantDomain) throws ApplicationRoleManagementServerException;

    boolean isExistingRole(String applicationId, String roleName, String tenantDomain)
            throws ApplicationRoleManagementServerException;

    void updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers, List<String> removedUsers,
                                            String tenantDomain) throws
            ApplicationRoleManagementServerException;

    ApplicationRole getApplicationRoleAssignedUsers(String roleId, String tenantDomain)
            throws ApplicationRoleManagementException;

    void updateApplicationRoleAssignedGroups(String roleId, String idpId, List<String> addedGroups,
                                             List<String> removedGroups, String tenantDomain)
            throws ApplicationRoleManagementException;

    ApplicationRole getApplicationRoleAssignedGroups(String roleId, String tenantDomain)
            throws ApplicationRoleManagementException;

    ApplicationRole getApplicationRoleAssignedGroups(String roleId, IdentityProvider identityProvider,
                                                     String tenantDomain) throws ApplicationRoleManagementException;
}
