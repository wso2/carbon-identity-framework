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

package org.wso2.carbon.identity.application.role.mgt;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.ApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.CacheBackedApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.identity.application.role.mgt.model.Group;
import org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ROLE_NAME;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_ROLE_NOT_FOUND;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.LOCAL_IDP;
import static org.wso2.carbon.identity.application.role.mgt.util.ApplicationRoleMgtUtils.handleClientException;

/**
 * Application role management service implementation.
 */
public class ApplicationRoleManagerImpl implements ApplicationRoleManager {

    private static final ApplicationRoleManager instance = new ApplicationRoleManagerImpl();

    private ApplicationRoleManagerImpl() {

    }

    public static ApplicationRoleManager getInstance() {

        return instance;
    }

    private final ApplicationRoleMgtDAO applicationRoleMgtDAO =
            new CacheBackedApplicationRoleMgtDAOImpl(new ApplicationRoleMgtDAOImpl());

    @Override
    public ApplicationRole addApplicationRole(ApplicationRole applicationRole)
            throws ApplicationRoleManagementException {

        String tenantDomain = ApplicationRoleMgtUtils.getTenantDomain();
        if (StringUtils.isBlank(applicationRole.getRoleName())) {
            throw handleClientException(ERROR_CODE_INVALID_ROLE_NAME);
        }
        boolean existingRole =
                applicationRoleMgtDAO.isExistingRole(applicationRole.getApplicationId(), applicationRole.getRoleName(),
                        tenantDomain);
        if (existingRole) {
            throw handleClientException(ERROR_CODE_DUPLICATE_ROLE, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
        // Validate scopes are authorized to given application.
        ApplicationRoleMgtUtils.validateAuthorizedScopes(applicationRole.getApplicationId(),
                Arrays.asList(applicationRole.getPermissions()));
        return applicationRoleMgtDAO.addApplicationRole(applicationRole, tenantDomain);
    }

    @Override
    public ApplicationRole updateApplicationRole(String applicationId, String roleId, String newName,
                                                 List<String> addedScopes, List<String> removedScopes)
            throws ApplicationRoleManagementException {

        String tenantDomain = ApplicationRoleMgtUtils.getTenantDomain();
        validateAppRoleId(roleId);
        if (newName != null) {
            // Check whether new role name is valid.
            if (StringUtils.isBlank(newName)) {
                throw handleClientException(ERROR_CODE_INVALID_ROLE_NAME);
            }
            // Check whether new role name is already exists.
            boolean existingRole =
                    applicationRoleMgtDAO.isExistingRole(applicationId, newName, tenantDomain);
            if (existingRole) {
                throw handleClientException(ERROR_CODE_DUPLICATE_ROLE, newName,
                        applicationId);
            }
        }
        if (addedScopes != null && removedScopes != null) {
            // Remove common scopes in both added and removed scopes.
            ApplicationRoleMgtUtils.removeCommonValues(addedScopes, removedScopes);
        }
        // Validate scopes are authorized to given application.
        ApplicationRoleMgtUtils.validateAuthorizedScopes(applicationId, addedScopes);
        return applicationRoleMgtDAO.updateApplicationRole(roleId, newName, addedScopes, removedScopes, tenantDomain);
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        return applicationRoleMgtDAO.getApplicationRoleById(roleId, ApplicationRoleMgtUtils.getTenantDomain());
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoles(applicationId);
    }

    @Override
    public void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        applicationRoleMgtDAO.deleteApplicationRole(roleId, ApplicationRoleMgtUtils.getTenantDomain());
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers,
                                                              List<String> removedUsers)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        ApplicationRoleMgtUtils.removeCommonValues(addedUsers, removedUsers);
        return applicationRoleMgtDAO.updateApplicationRoleAssignedUsers(roleId, addedUsers, removedUsers,
                ApplicationRoleMgtUtils.getTenantDomain());
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedUsers(String roleId)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        return applicationRoleMgtDAO.getApplicationRoleAssignedUsers(roleId, ApplicationRoleMgtUtils.getTenantDomain());
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedGroups(String roleId, List<Group> addedGroups,
                                                    List<String> removedGroups)
            throws ApplicationRoleManagementException {

        String tenantDomain = ApplicationRoleMgtUtils.getTenantDomain();
        validateAppRoleId(roleId);
        ApplicationRoleMgtUtils.removeCommonGroupValues(addedGroups, removedGroups);
        ApplicationRoleMgtUtils.validateGroupIds(addedGroups);
        ApplicationRole applicationRole = applicationRoleMgtDAO.updateApplicationRoleAssignedGroups(roleId, addedGroups,
                removedGroups, tenantDomain);
        resolveAssignedGroupsOfRole(applicationRole.getAssignedGroups(), tenantDomain);
        return  applicationRole;
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, String idpId)
            throws ApplicationRoleManagementException {

        String tenantDomain = ApplicationRoleMgtUtils.getTenantDomain();
        validateAppRoleId(roleId);
        ApplicationRole applicationRole;
        if (StringUtils.isNotBlank(idpId)) {
            if (LOCAL_IDP.equalsIgnoreCase(idpId)) {
                IdentityProvider identityProvider = ApplicationRoleMgtUtils.getResidentIdp();
                applicationRole = applicationRoleMgtDAO.getApplicationRoleAssignedGroups(roleId,
                        identityProvider.getResourceId(), tenantDomain);
            } else {
                applicationRole = applicationRoleMgtDAO.getApplicationRoleAssignedGroups(roleId, idpId, tenantDomain);
            }

        } else {
            applicationRole = applicationRoleMgtDAO.getApplicationRoleAssignedGroups(roleId, tenantDomain);
        }
        resolveAssignedGroupsOfRole(applicationRole.getAssignedGroups(), tenantDomain);
        return applicationRole;
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByUserId(userId, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByUserId(userId, appId, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByGroupId(groupId, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByGroupId(groupId, appId, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupIds(List<String> groupIds, String appId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByGroupIds(groupIds, appId, tenantDomain);
    }

    @Override
    public List<String> getScopesByRoleIds(List<String> roleIds, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getScopesByRoleIds(roleIds, tenantDomain);
    }

    /**
     * Validate application role id.
     *
     * @param roleId Role ID.
     * @throws  ApplicationRoleManagementException Error occurred while validating roleId.
     */
    private void validateAppRoleId(String roleId) throws ApplicationRoleManagementException {

        boolean isExists = applicationRoleMgtDAO.checkRoleExists(roleId, ApplicationRoleMgtUtils.getTenantDomain());

        if (!isExists) {
            throw handleClientException(ERROR_CODE_ROLE_NOT_FOUND, roleId);
        }
    }

    /**
     * Resolve assigned groups of a role.
     *
     * @param assignedGroups Role ID.
     * @throws  ApplicationRoleManagementException Error occurred while validating roleId.
     */
    private void resolveAssignedGroupsOfRole(List<Group> assignedGroups, String tenantDomain)
            throws ApplicationRoleManagementException {

        for (Group group : assignedGroups) {

            IdentityProvider identityProvider = ApplicationRoleMgtUtils.getIdpById(group.getIdpId());
            if (LOCAL_IDP.equalsIgnoreCase(identityProvider.getIdentityProviderName())) {
                    group.setGroupName(ApplicationRoleMgtUtils.getGroupNameByID(group.getGroupId(), tenantDomain));
                    group.setIdpName(identityProvider.getIdentityProviderName());
            } else {
                IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
                Map<String, String> idToNameMap = new HashMap<>();
                for (IdPGroup idpGroup : idpGroups) {
                    idToNameMap.put(idpGroup.getIdpGroupId(), idpGroup.getIdpGroupName());
                }
                if (idToNameMap.containsKey(group.getGroupId())) {
                    group.setGroupName(idToNameMap.get(group.getGroupId()));
                    group.setIdpName(identityProvider.getIdentityProviderName());
                }
            }
        }
    }
}
