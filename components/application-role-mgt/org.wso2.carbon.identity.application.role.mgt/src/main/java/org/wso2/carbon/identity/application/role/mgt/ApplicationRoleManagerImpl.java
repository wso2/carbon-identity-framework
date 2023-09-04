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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.role.mgt.dao.ApplicationRoleMgtDAO;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.ApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.dao.impl.CacheBackedApplicationRoleMgtDAOImpl;
import org.wso2.carbon.identity.application.role.mgt.exceptions.ApplicationRoleManagementException;
import org.wso2.carbon.identity.application.role.mgt.internal.ApplicationRoleMgtServiceComponentHolder;
import org.wso2.carbon.identity.application.role.mgt.model.ApplicationRole;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_DUPLICATE_ROLE;
import static org.wso2.carbon.identity.application.role.mgt.constants.ApplicationRoleMgtConstants.ErrorMessages.ERROR_CODE_IDP_NOT_FOUND;
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

        String tenantDomain = getTenantDomain();
        boolean existingRole =
                applicationRoleMgtDAO.isExistingRole(applicationRole.getApplicationId(), applicationRole.getRoleName(),
                        tenantDomain);
        if (existingRole) {
            throw handleClientException(ERROR_CODE_DUPLICATE_ROLE, applicationRole.getRoleName(),
                    applicationRole.getApplicationId());
        }
        return applicationRoleMgtDAO.addApplicationRole(applicationRole, tenantDomain);
    }

    @Override
    public ApplicationRole updateApplicationRole(String applicationId, String roleId, String newName,
                                                 List<String> addedScopes, List<String> removedScopes)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        // TODO: Check authorized scopes for the app and filter out added permissions
        return applicationRoleMgtDAO.updateApplicationRole(roleId, newName, addedScopes, removedScopes,
                getTenantDomain());
    }

    @Override
    public ApplicationRole getApplicationRoleById(String roleId) throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        return applicationRoleMgtDAO.getApplicationRoleById(roleId, getTenantDomain());
    }

    @Override
    public List<ApplicationRole> getApplicationRoles(String applicationId) throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRoles(applicationId);
    }

    @Override
    public void deleteApplicationRole(String roleId) throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        applicationRoleMgtDAO.deleteApplicationRole(roleId, getTenantDomain());
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedUsers(String roleId, List<String> addedUsers,
                                                              List<String> removedUsers)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        removeCommonValues(addedUsers, removedUsers);
        return applicationRoleMgtDAO.updateApplicationRoleAssignedUsers(roleId, addedUsers, removedUsers,
                getTenantDomain());
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedUsers(String roleId)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        return applicationRoleMgtDAO.getApplicationRoleAssignedUsers(roleId, getTenantDomain());
    }

    @Override
    public ApplicationRole updateApplicationRoleAssignedGroups(String roleId, String idpId, List<String> addedGroups,
                                                    List<String> removedGroups)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        try {
            IdentityProvider identityProvider;
            if (LOCAL_IDP.equals(idpId)) {

                identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                        .getIdentityProviderManager().getResidentIdP(getTenantDomain());
            } else {
                identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                        .getIdentityProviderManager().getIdPByResourceId(idpId, getTenantDomain(), true);
            }
            if (identityProvider == null) {
                throw handleClientException(ERROR_CODE_IDP_NOT_FOUND, idpId);
            }
            removeCommonValues(addedGroups, removedGroups);
            return applicationRoleMgtDAO.updateApplicationRoleAssignedGroups(roleId, identityProvider,
                    addedGroups, removedGroups, getTenantDomain());
        }  catch (IdentityProviderManagementException e) {
            throw new ApplicationRoleManagementException("Error while retrieving idp",
                    "Error while retrieving idp for idpId: " + idpId, e);
        }
    }

    @Override
    public ApplicationRole getApplicationRoleAssignedGroups(String roleId, String idpId)
            throws ApplicationRoleManagementException {

        validateAppRoleId(roleId);
        try {
            IdentityProvider identityProvider;
            if (LOCAL_IDP.equalsIgnoreCase(idpId)) {
                identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                        .getIdentityProviderManager().getResidentIdP(getTenantDomain());
            } else {
                identityProvider = ApplicationRoleMgtServiceComponentHolder.getInstance()
                        .getIdentityProviderManager().getIdPByResourceId(idpId, getTenantDomain(), true);
            }
            if (identityProvider == null) {
                throw handleClientException(ERROR_CODE_IDP_NOT_FOUND, idpId);
            }
            return applicationRoleMgtDAO.getApplicationRoleAssignedGroups(roleId, identityProvider, getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            throw new ApplicationRoleManagementException("Error while retrieving idp", "Error while retrieving idp " +
                    "for idpId: " + idpId, e);
        }
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByUserId(String userId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByUserId(userId, tenantDomain);
    }

    @Override
    public List<ApplicationRole> getApplicationRolesByGroupId(String groupId, String tenantDomain)
            throws ApplicationRoleManagementException {

        return applicationRoleMgtDAO.getApplicationRolesByGroupId(groupId, tenantDomain);
    }

    /**
     * Validate application role id.
     *
     * @param roleId Role ID.
     * @throws  ApplicationRoleManagementException Error occurred while validating roleId.
     */
    private void validateAppRoleId(String roleId) throws ApplicationRoleManagementException {

        boolean isExists = applicationRoleMgtDAO.checkRoleExists(roleId, getTenantDomain());

        if (!isExists) {
            throw handleClientException(ERROR_CODE_ROLE_NOT_FOUND, roleId);
        }
    }

    /**
     * Get tenant domain.
     *
     */
    private static String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    /**
     * Remove common values in given two lists.
     *
     * @param list1 List 1.
     * @param list2 List 2.
     */
    private void removeCommonValues(List<String> list1, List<String> list2) {
        HashSet<String> set = new HashSet<>(list1);

        Iterator<String> iterator = list2.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (set.contains(value)) {
                iterator.remove();
                list1.remove(value);
            }
        }
    }
}
