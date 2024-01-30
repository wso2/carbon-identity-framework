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

package org.wso2.carbon.identity.application.authentication.framework.handler.approles.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.util.RoleResolverUtils;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant.AppRolesConstants.ErrorMessages.ERROR_CODE_RETRIEVING_APP_ROLES;
import static org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant.AppRolesConstants.ErrorMessages.ERROR_CODE_RETRIEVING_IDENTITY_PROVIDER;
import static org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant.AppRolesConstants.ErrorMessages.ERROR_CODE_RETRIEVING_LOCAL_USER_GROUPS;
import static org.wso2.carbon.identity.application.authentication.framework.handler.approles.constant.AppRolesConstants.ErrorMessages.ERROR_CODE_USER_NULL;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.user.mgt.UserMgtConstants.INTERNAL_ROLE;

/**
 * Application associated roles resolver implementation.
 */
public class AppAssociatedRolesResolverImpl implements ApplicationRolesResolver {

    @Override
    public int getPriority() {

        return 200;
    }

    @Override
    public String[] getRoles(AuthenticatedUser authenticatedUser, String applicationId) throws
            ApplicationRolesException {

        if (authenticatedUser == null) {
            throw RoleResolverUtils.handleClientException(ERROR_CODE_USER_NULL);
        }
        if (authenticatedUser.isFederatedUser()) {
            return getAppAssociatedRolesForFederatedUser(authenticatedUser, applicationId, null);
        }
        return getAppAssociatedRolesForLocalUser(authenticatedUser, applicationId);
    }

    @Override
    public String[] getAppAssociatedRolesOfFederatedUser(AuthenticatedUser authenticatedUser, String applicationId,
                                                         String idpGroupClaimURI) throws ApplicationRolesException {

        if (authenticatedUser == null) {
            throw RoleResolverUtils.handleClientException(ERROR_CODE_USER_NULL);
        }
        return getAppAssociatedRolesForFederatedUser(authenticatedUser, applicationId, idpGroupClaimURI);
    }

    @Override
    public String[] getAppAssociatedRolesOfLocalUser(AuthenticatedUser authenticatedUser, String applicationId)
            throws ApplicationRolesException {

        if (authenticatedUser == null) {
            throw RoleResolverUtils.handleClientException(ERROR_CODE_USER_NULL);
        }
        return getAppAssociatedRolesForLocalUser(authenticatedUser, applicationId);
    }


    /**
     * Get app associated roles for local user for given app.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID.
     * @return App associated roles for local user.
     * @throws ApplicationRolesException If an error occurred while getting app associated roles for local user.
     */
    private String[] getAppAssociatedRolesForLocalUser(AuthenticatedUser authenticatedUser, String applicationId)
            throws ApplicationRolesException {

        Set<String> userRoleIds = getAllRolesOfLocalUser(authenticatedUser);
        List<RoleV2> rolesAssociatedWithApp = getRolesAssociatedWithApplication(applicationId,
                authenticatedUser.getTenantDomain());

        return rolesAssociatedWithApp.stream()
                .filter(role -> userRoleIds.contains(role.getId()))
                .map(role -> appendInternalDomain(role.getName()))
                .toArray(String[]::new);
    }


    /**
     * Get app associated roles for federated user for given app.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID.
     * @param idpGroupClaimURI  IDP group claim URI.
     * @return App associated roles for federated user.
     * @throws ApplicationRolesException If an error occurred while getting app associated roles for federated user.
     */
    private String[] getAppAssociatedRolesForFederatedUser(AuthenticatedUser authenticatedUser, String applicationId,
                                                           String idpGroupClaimURI) throws ApplicationRolesException {

        Set<String> federatedUserRoleIds = getAllRolesOfFederatedUser(authenticatedUser, idpGroupClaimURI);
        List<RoleV2> rolesAssociatedWithApp = getRolesAssociatedWithApplication(applicationId,
                authenticatedUser.getTenantDomain());

        return rolesAssociatedWithApp.stream()
                .filter(role -> federatedUserRoleIds.contains(role.getId()))
                .map(role -> appendInternalDomain(role.getName()))
                .toArray(String[]::new);
    }

    /**
     * Get all roles of the local user.
     *
     * @param authenticatedUser Authenticated user.
     * @return All the roles assigned to the local user.
     * @throws ApplicationRolesException If an error occurred while getting all roles of a local user.
     */
    private Set<String> getAllRolesOfLocalUser(AuthenticatedUser authenticatedUser)
            throws ApplicationRolesException {

        try {
            List<String> userGroups = getUserGroups(authenticatedUser);
            List<String> roleIdsFromUserGroups = getRoleIdsOfGroups(userGroups, authenticatedUser.getTenantDomain());
            List<String> roleIdsFromUser =
                    getRoleIdsOfUser(authenticatedUser.getUserId(), authenticatedUser.getTenantDomain());

            return new HashSet<>(CollectionUtils.union(roleIdsFromUserGroups, roleIdsFromUser));
        } catch (IdentityRoleManagementException | UserIdNotFoundException e) {
            throw RoleResolverUtils.handleServerException(ERROR_CODE_RETRIEVING_APP_ROLES, e);
        }
    }

    /**
     * Get all roles of the federated user.
     *
     * @param authenticatedUser Authenticated user.
     * @return All the roles assigned to the federated user.
     * @throws ApplicationRolesException If an error occurred while getting all roles of a federated user.
     */
    private Set<String> getAllRolesOfFederatedUser(AuthenticatedUser authenticatedUser, String idpGroupClaimURI)
            throws ApplicationRolesException {

        String tenantDomain = authenticatedUser.getTenantDomain();
        String idpName = authenticatedUser.getFederatedIdPName();
        IdentityProvider identityProvider = getIDP(idpName, tenantDomain);
        if (identityProvider == null) {
            return Collections.emptySet();
        }
        IdPGroup[] idpGroups = identityProvider.getIdPGroupConfig();
        List<String> idpGroupNamesOfUser = getFederatedIdPGroupNamesOfUser(authenticatedUser, identityProvider,
                idpGroupClaimURI);
        if (CollectionUtils.isEmpty(idpGroupNamesOfUser)) {
            return Collections.emptySet();
        }
        List<String> idpGroupIdsOfUser = getIdPGroupIdListOfUser(idpGroupNamesOfUser, idpGroups);
        List<String> roleIdsFromIdPGroups = getRoleIdsOfIdpGroups(idpGroupIdsOfUser, tenantDomain);
        return new HashSet<>(roleIdsFromIdPGroups);
    }

    /**
     * Get IDP by IDP name and tenant domain.
     *
     * @param idpName      IDP name.
     * @param tenantDomain Tenant domain.
     * @return Identity provider.
     * @throws ApplicationRolesException If an error occurred while getting IDP.
     */
    private IdentityProvider getIDP(String idpName, String tenantDomain) throws ApplicationRolesException {

        try {
            return FrameworkServiceDataHolder.getInstance().getIdentityProviderManager()
                    .getIdPByName(idpName, tenantDomain, true);
        } catch (IdentityProviderManagementException e) {
            throw RoleResolverUtils.handleServerException(
                    ERROR_CODE_RETRIEVING_IDENTITY_PROVIDER, e, idpName, tenantDomain);
        }
    }

    /**
     * Get user IDP group ids.
     *
     * @param userIdPGroups User IDP group names.
     * @param idpGroups     IDP groups.
     * @return User IDP group ids.
     */
    private List<String> getIdPGroupIdListOfUser(List<String> userIdPGroups, IdPGroup[] idpGroups) {

        List<String> idpGroupIds = new ArrayList<>();
        for (IdPGroup group : idpGroups) {
            if (group.getIdpGroupId() != null && userIdPGroups.contains(group.getIdpGroupName())) {
                idpGroupIds.add(group.getIdpGroupId());
            }
        }
        return idpGroupIds;
    }

    /**
     * Get Role IDs assigned to user through groups.
     *
     * @param userGroups   User groups.
     * @param tenantDomain Tenant domain.
     * @return Role IDs assigned to user through groups.
     * @throws IdentityRoleManagementException If an error occurred while getting role IDs assigned through groups.
     */
    private List<String> getRoleIdsOfGroups(List<String> userGroups, String tenantDomain)
            throws IdentityRoleManagementException {

        return FrameworkServiceDataHolder.getInstance().getRoleManagementServiceV2()
                .getRoleIdListOfGroups(userGroups, tenantDomain);
    }

    /**
     * Get Role IDs assigned to user directly.
     *
     * @param userId       User ID.
     * @param tenantDomain Tenant domain.
     * @return Role IDs assigned to user directly.
     * @throws IdentityRoleManagementException If an error occurred while getting role IDs assigned directly.
     */
    private List<String> getRoleIdsOfUser(String userId, String tenantDomain)
            throws IdentityRoleManagementException {

        return FrameworkServiceDataHolder.getInstance().getRoleManagementServiceV2()
                .getRoleIdListOfUser(userId, tenantDomain);
    }

    /**
     * Get roles associated with the application.
     *
     * @param applicationId Application ID.
     * @param tenantDomain  Tenant domain.
     * @return Roles associated with the application.
     * @throws ApplicationRolesException If an error occurred while getting roles associated with the application.
     */
    private List<RoleV2> getRolesAssociatedWithApplication(String applicationId, String tenantDomain)
            throws ApplicationRolesException {

        try {
            return FrameworkServiceDataHolder.getInstance().getApplicationManagementService()
                    .getAssociatedRolesOfApplication(applicationId, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw RoleResolverUtils.handleServerException(ERROR_CODE_RETRIEVING_APP_ROLES, e);
        }
    }

    /**
     * Get federated user IDP groups.
     *
     * @param authenticatedUser Authenticated user.
     * @param federatedIdP      Federated IDP.
     * @return Federated user IDP groups.
     */
    private List<String> getFederatedIdPGroupNamesOfUser(AuthenticatedUser authenticatedUser,
                                                         IdentityProvider federatedIdP, String idpGroupClaimURI) {

        if (federatedIdP != null) {
            if (StringUtils.isEmpty(idpGroupClaimURI)) {
                idpGroupClaimURI = FrameworkUtils.getIdpGroupClaimUri(federatedIdP.getClaimConfig().getClaimMappings());
            }
            if (idpGroupClaimURI != null) {
                String[] idpGroups = getIdPUserGroups(authenticatedUser, idpGroupClaimURI);
                if (idpGroups != null && idpGroups.length > 0) {
                    return Arrays.asList(idpGroups);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get the groups of the local authenticated user.
     *
     * @param authenticatedUser Authenticated user.
     * @return Groups of the local user.
     * @throws ApplicationRolesException If an error occurred while getting groups of the local user.
     */
    private List<String> getUserGroups(AuthenticatedUser authenticatedUser) throws ApplicationRolesException {

        List<String> userGroups = new ArrayList<>();

        RealmService realmService = UserCoreUtil.getRealmService();
        try {
            int tenantId = IdentityTenantUtil.getTenantId(authenticatedUser.getTenantDomain());
            UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            List<Group> groups = ((AbstractUserStoreManager) userStoreManager)
                    .getGroupListOfUser(authenticatedUser.getUserId(), null, null);
            // Exclude internal and application groups from the list.
            for (Group group : groups) {
                String groupName = group.getGroupName();
                if (!StringUtils.containsIgnoreCase(groupName, INTERNAL_ROLE) &&
                        !StringUtils.containsIgnoreCase(groupName, APPLICATION_DOMAIN)) {
                    userGroups.add(group.getGroupID());
                }
            }
        } catch (UserIdNotFoundException e) {
            throw RoleResolverUtils.handleServerException(ERROR_CODE_RETRIEVING_LOCAL_USER_GROUPS,
                    e);
        } catch (UserStoreException e) {
            if (isDoGetGroupListOfUserNotImplemented(e)) {
                return userGroups;
            }
            throw RoleResolverUtils.handleServerException(ERROR_CODE_RETRIEVING_LOCAL_USER_GROUPS,
                    e);
        }
        return userGroups;
    }

    /**
     * Check if the UserStoreException occurred due to the doGetGroupListOfUser method not being implemented.
     *
     * @param e UserStoreException.
     * @return true if the UserStoreException was caused by the doGetGroupListOfUser method not being implemented,
     * false otherwise.
     */
    private boolean isDoGetGroupListOfUserNotImplemented(UserStoreException e) {

        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof NotImplementedException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    /**
     * Get the IdP groups of the federated authenticated user.
     *
     * @param authenticatedUser Authenticated federated user.
     * @param idpGroupClaimUri  IDP group claim URI.
     * @return IdP groups of the authenticated user.
     */
    private String[] getIdPUserGroups(AuthenticatedUser authenticatedUser, String idpGroupClaimUri) {

        String idpGroupClaimValueSeparator = FrameworkUtils.getIdpGroupClaimValueSeparator();
        Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();
        for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
            ClaimMapping claimMapping = entry.getKey();
            if (idpGroupClaimUri.equals(claimMapping.getRemoteClaim().getClaimUri())) {
                String idPGroupsClaim = entry.getValue();
                if (StringUtils.isNotBlank(idPGroupsClaim)) {
                    return idPGroupsClaim.split(Pattern.quote(idpGroupClaimValueSeparator));
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Get the role ids of idp groups.
     *
     * @param groups       IDP Groups.
     * @param tenantDomain Tenant domain.
     * @return Role ids of idp groups.
     * @throws ApplicationRolesException if an error occurs while retrieving role id list of idp groups.
     */
    private static List<String> getRoleIdsOfIdpGroups(List<String> groups, String tenantDomain)
            throws ApplicationRolesException {

        try {
            return FrameworkServiceDataHolder.getInstance().getRoleManagementServiceV2()
                    .getRoleIdListOfIdpGroups(groups, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw RoleResolverUtils.handleServerException(ERROR_CODE_RETRIEVING_APP_ROLES, e);
        }
    }

    /**
     * Append internal domain if there is no domain appended already.
     *
     * @param roleName Role name.
     * @return Domain appended role name.
     */
    private String appendInternalDomain(String roleName) {

        if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            return UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + roleName;
        }
        return roleName;
    }
}
