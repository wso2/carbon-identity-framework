/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of UniqueIDUserOperationEventListener. This will call the
 * relevant UserOperationEventListener methods when the user ID related listeners are being called. This has been
 * implemented to preserve the backwards compatibility.
 */
public class IdentityUserNameResolverListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(IdentityUserNameResolverListener.class);
    private static final String DO_PRE_DELETE_USER_USER_NAME = "doPreDeleteUserUserName";

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 14;
    }

    @Override
    public boolean doPreAuthenticateWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAuthenticate(userName, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String userID, AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName;
        boolean authenticated =
                authenticationResult.getAuthenticationStatus() == AuthenticationResult.AuthenticationStatus.SUCCESS;

        if (authenticated) {
            userName = authenticationResult.getAuthenticatedUser().get().getUsername();
        } else {
            userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAuthenticate(userName, authenticated, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                      String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAddUser(userName, credential, roleList, claims, profile, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAddUserWithID(User user, Object credential, String[] roleList, Map<String, String> claims,
                                       String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(user.getUserID(), (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(user.getUserID(), userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAddUser(userName, credential, roleList, claims, profile, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential,
                                               UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateCredential(userName, newCredential, oldCredential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialWithID(String userID, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostUpdateCredential(userName, credential, userStoreManager)) {
                    return false;
                }

            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String userID, Object newCredential,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateCredentialByAdmin(userName, newCredential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdminWithID(String userID, Object credential,
                                                       UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostUpdateCredentialByAdmin(userName, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        // Setting the thread-local to keep userName for doPostDeleteUserWithID listener.
        IdentityUtil.threadLocalProperties.get().put(DO_PRE_DELETE_USER_USER_NAME, userName);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreDeleteUser(userName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUserWithID(String userID, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        try {
            // Getting the userName from thread-local which has been set from doPreDeleteUserWithID.
            String userName = (String) IdentityUtil.threadLocalProperties.get().get(DO_PRE_DELETE_USER_USER_NAME);

            for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
                if (isNotAResolverListener(listener)) {
                    if (!listener.doPostDeleteUser(userName, userStoreManager)) {
                        return false;
                    }
                }
            }
        } finally {
            // Remove thread local variable.
            IdentityUtil.threadLocalProperties.get().remove(DO_PRE_DELETE_USER_USER_NAME);
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreSetUserClaimValue(userName, claimURI, claimValue, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostSetUserClaimValue(userName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreSetUserClaimValues(userName, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostSetUserClaimValues(userName, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreDeleteUserClaimValues(userName, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostDeleteUserClaimValues(userName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValueWithID(String userID, String claimURI, String profileName,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreDeleteUserClaimValue(userName, claimURI, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String userID, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostDeleteUserClaimValue(userName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddRoleWithID(String roleName, String[] userIDList, Permission[] permissions,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userNames = getUserNamesFromUserIDs(userIDList, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAddRole(roleName, userNames, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAddRoleWithID(String roleName, String[] userIDList, Permission[] permissions,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userNames = getUserNamesFromUserIDs(userIDList, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAddRole(roleName, userNames, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddInternalRoleWithID(String roleName, String[] userIDList, Permission[] permissions,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userNames = getUserNamesFromUserIDs(userIDList, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAddInternalRole(roleName, userNames, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAddInternalRoleWithID(String roleName, String[] userIDList, Permission[] permissions,
                                               UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userNames = getUserNamesFromUserIDs(userIDList, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAddInternalRole(roleName, userNames, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfInternalRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserNames = getUserNamesFromUserIDs(deletedUserIDs,
                (AbstractUserStoreManager) userStoreManager);
        String[] newUserNames = getUserNamesFromUserIDs(newUserIDs, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateUserListOfInternalRole(roleName, deletedUserNames, newUserNames,
                        userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserNames = getUserNamesFromUserIDs(deletedUserIDs,
                (AbstractUserStoreManager) userStoreManager);
        String[] newUserNames = getUserNamesFromUserIDs(newUserIDs, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateUserListOfRole(roleName, deletedUserNames, newUserNames, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserNames = getUserNamesFromUserIDs(deletedUserIDs,
                (AbstractUserStoreManager) userStoreManager);
        String[] newUserNames = getUserNamesFromUserIDs(newUserIDs, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostUpdateUserListOfRole(roleName, deletedUserNames, newUserNames, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateInternalRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateInternalRoleListOfUser(userName, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfInternalRoleWithID(String roleName, String[] deletedUserIDs,
                                                            String[] newUserIDs, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserNames = getUserNamesFromUserIDs(deletedUserIDs,
                (AbstractUserStoreManager) userStoreManager);
        String[] newUserNames = getUserNamesFromUserIDs(newUserIDs, (AbstractUserStoreManager) userStoreManager);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostUpdateUserListOfInternalRole(roleName, deletedUserNames, newUserNames, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreUpdateRoleListOfUser(userName, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValueWithID(String userID, String claim, String profileName,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreGetUserClaimValue(userName, claim, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
                                                 Map<String, String> claimMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreGetUserClaimValues(userName, claims, profileName, claimMap, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValueWithID(String userID, String claim, List<String> claimValue,
                                                 String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetUserClaimValue(userName, claim, claimValue, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String userID, String[] claims, String profileName,
                                                  Map<String, String> claimMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetUserClaimValues(userName, claims, profileName, claimMap, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, List<User> returnUsersList,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());
        Set<String> returnInitialUserNamesList = new HashSet<>(returnUserNamesList);
        Set<String> tempUserNamesList = new HashSet<>();

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreGetUserList(claimUri, claimValue, returnUserNamesList, userStoreManager)) {
                    return false;
                }
            }
        }

        // Reflect newly removed users by listeners in returnUsersList
        if (CollectionUtils.isNotEmpty(returnUserNamesList)) {
            tempUserNamesList.addAll(returnInitialUserNamesList);
            tempUserNamesList.removeAll(returnUserNamesList);
            for (User user : returnUsersList) {
                if (tempUserNamesList.contains(user.getUsername())) {
                    returnUsersList.remove(user);
                }
            }
            tempUserNamesList.clear();
        }

        // Reflect newly add users by listeners in returnUsersList
        if (CollectionUtils.isNotEmpty(returnUserNamesList)) {
            tempUserNamesList.addAll(returnUserNamesList);
            tempUserNamesList.removeAll(returnInitialUserNamesList);
            for (String username : tempUserNamesList) {
                User newUser = new User();
                newUser.setUsername(username);
                try {
                    newUser.setUserID(FrameworkUtils.resolveUserIdFromUsername(userStoreManager, username));
                } catch (UserSessionException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error occurred while resolving Id for the user: " + username, e);
                    }
                }
                returnUsersList.add(newUser);
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(String claimUri, String claimValue, int limit, int offset,
                                          List<User> returnUsersList, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                return listener
                        .doPreGetUserList(claimUri, claimValue, limit, offset, returnUserNamesList, userStoreManager);
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, List<User> returnUsersList,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetUserList(claimUri, claimValue, returnUserNamesList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(String claimUri, String claimValue, List<User> returnUsersList, int limit,
                                           int offset, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                return listener
                        .doPostGetUserList(claimUri, claimValue, returnUserNamesList, limit, offset, userStoreManager);
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetPaginatedUserListWithID(String claimUri, String claimValue, List<User> returnUsersList,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetPaginatedUserList(claimUri, claimValue, returnUserNamesList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostListUsersWithID(String filter, int limit, int offset, List<User> returnUsersList,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = returnUsersList.stream().map(User::getUsername).collect(Collectors.toList());

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostListUsers(filter, limit, offset, returnUserNamesList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUserWithID(String userID, String filter, String[] roleList,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName = getUserNameFromUserID(userID, (AbstractUserStoreManager) userStoreManager);
        if (userName == null) {
            return handleUserNameResolveFailure(userID, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetRoleListOfUser(userName, filter, roleList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserListWithID(Condition condition, String domain, String profileName, int limit, int offset,
                                          String sortBy, String sortOrder, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder,
                        userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserListWithID(Condition condition, String domain, String profileName, int limit,
                                           int offset, String sortBy, String sortOrder, List<User> users,
                                           UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> userNamesList = users.stream().map(User::getUsername).collect(Collectors.toList());
        String[] userNames = userNamesList.toArray(new String[0]);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                return listener
                        .doPostGetUserList(condition, domain, profileName, limit, offset, sortBy, sortOrder, userNames,
                                userStoreManager);
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserListOfRoleWithID(String roleName, List<User> userList,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> returnUserNamesList = userList.stream().map(User::getUsername).collect(Collectors.toList());
        String[] returnUserNames = returnUserNamesList.toArray(new String[0]);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetUserListOfRole(roleName, returnUserNames, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUsersClaimValuesWithID(List<String> userIDs, List<String> claims, String profileName,
                                                   List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntries,
                                                   UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> userNamesList = ((AbstractUserStoreManager) userStoreManager).getUserNamesFromUserIDs(userIDs);
        String[] userNames = userNamesList.toArray(new String[0]);
        List<UserClaimSearchEntry> userClaimSearchEntries = ((AbstractUserStoreManager) userStoreManager)
                .getUserClaimSearchEntries(uniqueIDUserClaimSearchEntries);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetUsersClaimValues(userNames, claims.toArray(new String[0]), profileName,
                        userClaimSearchEntries.toArray(new UserClaimSearchEntry[0]))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
                                           Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName;
        String userStoreDomainName = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        preferredUserNameValue = userStoreDomainName + UserCoreConstants.DOMAIN_SEPARATOR + preferredUserNameValue;
        String[] users = userStoreManager.getUserList(preferredUserNameClaim, preferredUserNameValue, null);
        if (users.length == 1) {
            userName = UserCoreUtil.removeDomainFromName(users[0]);
        } else {
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAuthenticate(userName, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(String preferredUserNameClaim, String preferredUserNameValue,
                                            AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName;
        boolean authenticated =
                authenticationResult.getAuthenticationStatus() == AuthenticationResult.AuthenticationStatus.SUCCESS;
        if (authenticated) {
            userName = authenticationResult.getAuthenticatedUser().get().getUsername();
        } else {
            String[] users = userStoreManager.getUserList(preferredUserNameClaim, preferredUserNameValue, null);
            if (users.length == 1) {
                userName = UserCoreUtil.removeDomainFromName(users[0]);
            } else {
                return true;
            }
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAuthenticate(userName, authenticated, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String username = ((AbstractUserStoreManager) userStoreManager).getUsernameByClaims(loginIdentifiers);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPreAuthenticate(username, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticateWithID(List<LoginIdentifier> loginIdentifiers,
                                            AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userName;
        boolean authenticated =
                authenticationResult.getAuthenticationStatus() == AuthenticationResult.AuthenticationStatus.SUCCESS;
        if (authenticated) {
            userName = authenticationResult.getAuthenticatedUser().get().getUsername();
        } else {
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostAuthenticate(userName, authenticated, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUsersWithID(List<String> userIDs, Map<String, List<String>> rolesOfUsersMap,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> userNamesList = ((AbstractUserStoreManager) userStoreManager).getUserNamesFromUserIDs(userIDs);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!listener.doPostGetRoleListOfUsers(userNamesList.toArray(new String[0]), rolesOfUsersMap)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean handleUserNameResolveFailure(String userID, UserStoreManager userStoreManager) {

        if (log.isDebugEnabled()) {
            log.debug("A userID cannot be found in the userStoreManager" + getUserStoreDomainName(userStoreManager)
                    + "for the given userID: " + userID);
        }
        return true;
    }

    private String getUserStoreDomainName(UserStoreManager userStoreManager) {

        String domainNameProperty;
        domainNameProperty = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        if (StringUtils.isBlank(domainNameProperty)) {
            domainNameProperty = IdentityUtil.getPrimaryDomainName();
        }
        return domainNameProperty;
    }

    private String[] getUserNamesFromUserIDs(String[] userIDList, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        if (userIDList == null) {
            return new String[0];
        }
        List<String> userNamesList =
                userStoreManager.getUserNamesFromUserIDs(getDomainLessNamesAsList(userIDList));
        return userNamesList.toArray(new String[0]);
    }

    private Collection<UserOperationEventListener> getUserStoreManagerListeners() {

        Map<Integer, UserOperationEventListener> userOperationEventListeners =
                IdentityMgtServiceDataHolder.getInstance().getUserOperationEventListeners();
        return userOperationEventListeners.values();
    }

    private List<String> getDomainLessNamesAsList(String[] names) {

        if (names == null) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(names).map(UserCoreUtil::removeDomainFromName).collect(Collectors.toList());
    }

    private boolean isNotAResolverListener(UserOperationEventListener listener) {

        return !(listener instanceof IdentityUserIdResolverListener)
                && !(listener instanceof IdentityUserNameResolverListener);
    }

    private String getUserNameFromUserID(String userID, AbstractUserStoreManager userStoreManager) throws
            UserStoreException {

        return UserCoreUtil.removeDomainFromName(userStoreManager.getUserNameFromUserID(userID));
    }
}
