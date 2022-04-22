/*
 * Copyright (c) 2010 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.user.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.common.ClaimValue;
import org.wso2.carbon.user.mgt.common.FlaggedName;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.common.UserRealmInfo;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserAdmin {

    private static final Log log = LogFactory.getLog(UserAdmin.class);

    public UserAdmin() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#listInternalUsers(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#listUsers(java.lang.String)
     */
    public String[] listUsers(String filter, int limit) throws UserAdminException {
        String[] users;
        users = getUserAdminProxy().listUsers(filter, limit);
        return users;
    }

    /**
     * @param filter
     * @param limit
     * @return
     * @throws UserAdminException
     */
    public FlaggedName[] listAllUsers(String filter, int limit) throws UserAdminException {

        FlaggedName[] names;
        names = getUserAdminProxy().listAllUsers(filter, limit);
        return names;
    }

    /**
     * Get list of users which have given permission
     *
     * @param filter     filter to check
     * @param permission permission to check
     * @param limit
     * @return
     * @throws UserAdminException
     */
    public FlaggedName[] listAllUsersWithPermission(String filter, String permission, int limit) throws
            UserAdminException {

        List<FlaggedName> permittedUsers = new ArrayList<>();
        try {
            org.wso2.carbon.user.api.UserRealm realm = UserMgtDSComponent.getRealmService().getTenantUserRealm
                    (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            AuthorizationManager authorizationManager = realm.getAuthorizationManager();


            FlaggedName[] users = getUserAdminProxy().listAllUsers(filter, limit);

            for (int i = 0; i < users.length - 1; i++) {
                if (authorizationManager.isUserAuthorized(users[i].getItemName(),
                        permission, UserMgtConstants.EXECUTE_ACTION)) {
                    permittedUsers.add(users[i]);
                }
            }
            permittedUsers.add(users[users.length - 1]);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserAdminException("Error while filtering authorized users.", e);
        }
        FlaggedName[] permittedUsersArray = new FlaggedName[permittedUsers.size()];
        return permittedUsers.toArray(permittedUsersArray);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getInternalRoles()
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#getAllRolesNames()
     */
    public FlaggedName[] getAllRolesNames(String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getAllRolesNames(filter, limit);
    }

    /**
     * Get list of roles which have given permission
     *
     * @param filter     filter to check
     * @param permission permission to check
     * @param limit
     * @return
     * @throws UserAdminException
     */
    public FlaggedName[] getAllPermittedRoleNames(String filter, String permission, int limit) throws
            UserAdminException {

        FlaggedName[] roles = getUserAdminProxy().getAllRolesNames(filter, limit);
        List<FlaggedName> permittedRoles = new ArrayList<>();
        try {
            org.wso2.carbon.user.api.UserRealm realm = UserMgtDSComponent.getRealmService().getTenantUserRealm
                    (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            AuthorizationManager authorizationManager = realm.getAuthorizationManager();
            for (int i = 0; i < roles.length - 1; i++) {
                if (authorizationManager.isRoleAuthorized(roles[i].getItemName(), permission, UserMgtConstants
                        .EXECUTE_ACTION)) {
                    permittedRoles.add(roles[i]);
                }
            }
            permittedRoles.add(roles[roles.length - 1]);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserAdminException("Error while filtering authorized roles.", e);
        }
        FlaggedName[] permittedRolesArray = new FlaggedName[permittedRoles.size()];
        return permittedRoles.toArray(permittedRolesArray);
    }


    public FlaggedName[] getAllSharedRoleNames(String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getAllRolesNames(filter, limit);
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#isWritable()
     */
    public UserRealmInfo getUserRealmInfo() throws UserAdminException {
        return getUserAdminProxy().getUserRealmInfo();
    }    ///

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addUserToInternalStore(java.lang.String
     * , java.lang.String, java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#addUser(java.lang.String,
     * java.lang.String, java.lang.String[], java.util.Map, java.lang.String)
     */
    public void addUser(String userName, String password, String[] roles, ClaimValue[] claims,
                        String profileName) throws UserAdminException {
        try {
            getUserAdminProxy().addUser(userName, password, roles, claims, profileName);
        } catch (UserAdminException e) {
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#changePassword(java.lang.String,
     * java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#changePassword(java.lang.String,
     * java.lang.String)
     */
    public void changePassword(String userName, String newPassword) throws UserAdminException {

        try {
            getUserAdminProxy().changePassword(userName, newPassword);
        } catch (UserAdminException e) {
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteUserFromInternalStore(java.lang
     * .String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) throws UserAdminException {


        try {
            getUserAdminProxy().deleteUser(userName,
                    CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_CONFIGURATION));
        } catch (UserAdminException e) {
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#addRoleToInternalStore(java.lang.String
     * , java.lang.String[], java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#addRole(java.lang.String,
     * java.lang.String[], java.util.Map)
     */
    public void addRole(String roleName, String[] userList, String[] permissions, boolean isSharedRole)
            throws UserAdminException {
        addUserRole(roleName, userList, permissions, isSharedRole, false);
    }

    private void addUserRole(String roleName, String[] userList, String[] permissions, boolean isSharedRole, boolean
            isInternalRole) throws UserAdminException {

        if (permissions == null) {
            permissions = new String[0];
        }

        UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        if (!isAllowedRoleName(roleName, realm)) {
            throw new UserAdminException("Role name is reserved by the system.");
        }
        if (isInternalRole) {
            getUserAdminProxy().addInternalRole(roleName, userList, permissions);
        } else if (!(getUserAdminProxy().isRoleAndGroupSeparationEnabled()) || ArrayUtils.isEmpty(permissions)) {
            getUserAdminProxy().addRole(roleName, userList, permissions, isSharedRole);
        } else {
            String internalSystemRoleName = getInternalSystemRoleName(roleName);
            getUserAdminProxy().addInternalRole(internalSystemRoleName, new String[0], permissions);
            permissions = new String[0];
            getUserAdminProxy().addRole(roleName, userList, permissions, isSharedRole);
            getUserAdminProxy().updateGroupListOfHybridRole(internalSystemRoleName, null,
                    new String[]{roleName});
        }
    }

    private String getInternalSystemRoleName(String roleName) {

        return UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX + UserCoreUtil.extractDomainFromName(roleName)
                .toLowerCase() + "_" + UserCoreUtil.removeDomainFromName(roleName);
    }

    /**
     * @param roleName
     * @param userList
     * @param permissions
     * @throws UserAdminException
     */
    public void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws UserAdminException {

        /* Block the role names with the prefix 'system_' as it is used for the special roles created by the system in
        order to maintain the backward compatibility. */
        if (getUserAdminProxy().isRoleAndGroupSeparationEnabled() && StringUtils
                .startsWithIgnoreCase(roleName, UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX)) {
            String errorMessage = String.format("Invalid role name: %s. Role names with the prefix: %s, is not allowed"
                            + " to be created from externally in the system.", roleName,
                    UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX);
            throw new UserAdminException(errorMessage);
        }
        addUserRole(roleName, userList, permissions, false, true);
    }

    /**
     * @param roleName
     * @param realm
     * @return
     * @throws UserAdminException
     */
    private boolean isAllowedRoleName(String roleName, UserRealm realm) throws UserAdminException {

        if (roleName == null) {
            return false;
        }

        int index;
        index = roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);

        if (index > 0) {
            roleName = roleName.substring(index + 1);
        }

        try {
            return !realm.getRealmConfiguration().isReservedRoleName(roleName);
        } catch (UserStoreException e) {
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#deleteRoleFromInternalStore(java.lang
     * .String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.TestClass#deleteRole(java.lang.String)
     */
    public void deleteRole(String roleName) throws UserAdminException {

        getUserAdminProxy().deleteRole(roleName);
        if (getUserAdminProxy().isRoleAndGroupSeparationEnabled()) {
            String internalSystemRoleName = getInternalSystemRoleName(roleName);
            if (getUserAdminProxy().isExistingHybridRole(internalSystemRoleName)) {
                getUserAdminProxy().deleteRole(appendInternalDomain(internalSystemRoleName));
            }
        }
    }

    /**
     * @param roleName
     * @param newRoleName
     * @throws UserAdminException
     */
    public void updateRoleName(String roleName, String newRoleName) throws UserAdminException {

        try {
            boolean isInternalRole = false;
            if (isInternalRole(roleName)) {
                isInternalRole = true;
            }
            String internalSystemRoleName;
            String newInternalSystemRoleName;
            if (!isInternalRole) {
                internalSystemRoleName = getInternalSystemRoleName(roleName);
                newInternalSystemRoleName = getInternalSystemRoleName(newRoleName);
                if (getUserAdminProxy().isRoleAndGroupSeparationEnabled() &&
                        getUserAdminProxy().isExistingHybridRole(internalSystemRoleName)) {
                    getUserAdminProxy().updateRoleName(appendInternalDomain(internalSystemRoleName),
                            appendInternalDomain(newInternalSystemRoleName));
                    getUserAdminProxy().updateRoleName(roleName, newRoleName);
                    getUserAdminProxy().updateGroupListOfHybridRole(newInternalSystemRoleName, new String[]{roleName},
                            new String[]{newRoleName});
                } else {
                    getUserAdminProxy().updateRoleName(roleName, newRoleName);
                }
            } else {
                getUserAdminProxy().updateRoleName(roleName, newRoleName);
            }
        } catch (UserAdminException e) {
            throw e;
        }
    }

    /**
     * @return
     * @throws UserAdminException
     */
    public boolean hasMultipleUserStores() throws UserAdminException {
        return getUserAdminProxy().hasMultipleUserStores();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#getUsersInfoOfRole(java.lang.String,
     * java.lang.String)
     */
    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getUsersOfRole(roleName, filter, limit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.UserAdmin#updateUsersOfRole(java.lang.String,
     * java.lang.String[], java.lang.String[])
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#updateUsersOfRole(java.lang.String,
     * java.lang.String[], java.lang.String[])
     */
    public void updateUsersOfRole(String roleName, FlaggedName[] userList)
            throws UserAdminException {

        try {
            getUserAdminProxy().updateUsersOfRole(roleName, userList);
        } catch (UserAdminException e) {
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.wso2.carbon.user.mgt.UserAdmin#getUsersInRole(java.lang.String)
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#getRoleInfoOfUser(java.lang.String)
     */
    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit) throws UserAdminException {
        return getUserAdminProxy().getRolesOfUser(userName, filter, limit);
    }

    // FIXME: Fix the documentation of this class including this.
    public FlaggedName[] getRolesOfCurrentUser() throws UserAdminException {
        return getRolesOfUser(CarbonContext.getThreadLocalCarbonContext().getUsername(), "*", -1);
    }   ///

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.wso2.carbon.user.mgt.TestClass#updateRolesOfUser(java.lang.String,
     * java.lang.String)
     */
    public void updateRolesOfUser(String userName, String[] newRoleList) throws UserAdminException {

        try {
            getUserAdminProxy().updateRolesOfUser(userName, newRoleList);
        } catch (UserAdminException e) {
            throw e;
        }
    }

    /**
     * @return
     * @throws UserAdminException
     */
    public UIPermissionNode getAllUIPermissions() throws UserAdminException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return getUserAdminProxy().getAllUIPermissions(tenantId);
    }

    /**
     * @param roleName
     * @return
     * @throws UserAdminException
     */
    public UIPermissionNode getRolePermissions(String roleName) throws UserAdminException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (!getUserAdminProxy().isRoleAndGroupSeparationEnabled() || isInternalRole(roleName)) {
            return getUserAdminProxy().getRolePermissions(roleName, tenantId);
        } else {
            List<String> roles = getUserAdminProxy()
                    .getHybridRoleListOfGroup(UserCoreUtil.removeDomainFromName(roleName),
                            UserCoreUtil.extractDomainFromName(roleName));
            return getUserAdminProxy().getRolePermissions(roles, tenantId);
        }
    }

    /**
     * @param roleName
     * @param rawResources
     * @throws UserAdminException
     */
    public void setRoleUIPermission(String roleName, String[] rawResources) throws UserAdminException {

        if (!getUserAdminProxy().isRoleAndGroupSeparationEnabled() || isInternalRole(roleName)) {
            getUserAdminProxy().setRoleUIPermission(roleName, rawResources);
        } else {
            String internalSystemRoleName = getInternalSystemRoleName(roleName);
            if (getUserAdminProxy().isExistingHybridRole(internalSystemRoleName)) {
                getUserAdminProxy().setRoleUIPermission(appendInternalDomain(internalSystemRoleName), rawResources);
            } else {
                getUserAdminProxy().addInternalRole(internalSystemRoleName, new String[0], rawResources);
                getUserAdminProxy().updateGroupListOfHybridRole(internalSystemRoleName, new String[]{roleName},
                        new String[]{roleName});
            }
        }
    }

    private String appendInternalDomain(String roleName) {

        if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            return UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + roleName;
        }
        return roleName;
    }

    private boolean isInternalRole(String roleName) {

        String domain = UserCoreUtil.extractDomainFromName(roleName);
        if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) || UserCoreConstants.WORKFLOW_DOMAIN
                .equalsIgnoreCase(domain) || UserCoreConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)
                || UserCoreConstants.SYSTEM_DOMAIN_NAME.equalsIgnoreCase(domain)) {
            return true;
        }
        return false;
    }

    /**
     * @param userStoreDomain
     * @param fileName
     * @param handler
     * @param defaultPassword
     * @throws UserAdminException
     */
    public void bulkImportUsers(String userStoreDomain, String fileName, DataHandler handler, String defaultPassword)
            throws UserAdminException {
        //password will no longer be used, instead the password will be taken from the file
        if (fileName == null || handler == null) {
            throw new UserAdminException("Required data not provided");
        }
        if (StringUtils.isEmpty(userStoreDomain)) {
            userStoreDomain = IdentityUtil.getPrimaryDomainName();
        }
        try {
            InputStream inStream = handler.getInputStream();
            getUserAdminProxy().bulkImportUsers(userStoreDomain, fileName, inStream, defaultPassword);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }

    }

    /**
     * @param oldPassword
     * @param newPassword
     * @throws UserAdminException
     * @throws AxisFault
     */
    public void changePasswordByUser(String userName, String oldPassword, String newPassword)
            throws UserAdminException {

        String result = null;
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            UserRealmService realmService = UserMgtDSComponent.getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            org.wso2.carbon.user.api.UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);
            boolean isAuthenticated = userRealm.getUserStoreManager().authenticate(tenantAwareUsername, oldPassword);
            if (isAuthenticated) {
                getUserAdminProxy().changePasswordByUser(userName, oldPassword, newPassword);
            } else {
                throw new UserAdminException("The current password you entered is incorrect. ");
            }
        } catch (UserAdminException e) {
            throw e;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserAdminException("Error while updating password. Please enter tenant unaware username",
                    e);
        }

    }


    /**
     * @param roleName
     * @param newUsers
     * @param deletedUsers
     * @throws UserAdminException
     */
    public void addRemoveUsersOfRole(String roleName, String[] newUsers, String[] deletedUsers)
            throws UserAdminException {

        try {
            getUserAdminProxy().updateUsersOfRole(roleName, newUsers, deletedUsers);
        } catch (UserAdminException e) {
            throw e;
        }
    }

    /**
     * @param userName
     * @param newRoles
     * @param deletedRoles
     * @throws UserAdminException
     */
    public void addRemoveRolesOfUser(String userName, String[] newRoles, String[] deletedRoles)
            throws UserAdminException {

        try {
            getUserAdminProxy().updateRolesOfUser(userName, newRoles, deletedRoles);
        } catch (UserAdminException e) {
            throw e;
        }
    }


    /**
     * @param claimValue
     * @param filter
     * @param maxLimit
     * @return
     * @throws UserAdminException
     */
    public FlaggedName[] listUserByClaim(ClaimValue claimValue, String filter, int maxLimit)
            throws UserAdminException {
        return getUserAdminProxy().listUsers(claimValue, filter, maxLimit);
    }

    /**
     * List users with given claim value and permission
     *
     * @param claimValue claim to check
     * @param filter     filter to check
     * @param permission permission to check
     * @param maxLimit
     * @return
     * @throws UserAdminException
     */
    public FlaggedName[] listUserByClaimWithPermission(ClaimValue claimValue, String filter, String permission, int
            maxLimit)
            throws UserAdminException {

        List<FlaggedName> permittedUsers = new ArrayList<>();
        try {
            org.wso2.carbon.user.api.UserRealm realm = UserMgtDSComponent.getRealmService().getTenantUserRealm
                    (PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            AuthorizationManager authorizationManager = realm.getAuthorizationManager();
            FlaggedName[] users = getUserAdminProxy().listUsers(claimValue, filter, maxLimit);
            for (int i = 0; i < users.length - 1; i++) {
                if (authorizationManager.isUserAuthorized(users[i].getItemName(),
                        permission, UserMgtConstants.EXECUTE_ACTION)) {
                    permittedUsers.add(users[i]);
                }
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserAdminException("Error while filtering authorized users.", e);
        }
        FlaggedName[] permittedUsersArray = new FlaggedName[permittedUsers.size()];
        return permittedUsers.toArray(permittedUsersArray);
    }

    /**
     * @return
     */
    private UserRealmProxy getUserAdminProxy() {
        UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
        return new UserRealmProxy(realm);
    }



    public boolean isSharedRolesEnabled() throws UserAdminException {
        return getUserAdminProxy().isSharedRolesEnabled();
    }
}
