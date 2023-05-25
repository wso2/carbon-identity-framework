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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.listener.UniqueIDUserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;
import org.wso2.carbon.user.core.model.UserClaimSearchEntry;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is an implementation of UserOperationEventListener. This will call the
 * relevant UniqueIDUserOperationEventListener methods when the user name related listeners are being called. This has
 * been implemented to preserve the backwards compatibility.
 */
public class IdentityUserIdResolverListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(IdentityUserIdResolverListener.class);
    private static final String DO_PRE_DELETE_USER_USER_ID = "doPreDeleteUserUserID";

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 15;
    }

    @Override
    public boolean doPreAuthenticate(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreAuthenticateWithID(userID, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAuthenticate(String userName, boolean authenticated, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        AuthenticationResult authenticationResult;
        if (authenticated) {
            authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.SUCCESS);
            User user = ((AbstractUserStoreManager) userStoreManager).getUser(userID, userName);
            authenticationResult.setAuthenticatedUser(user);
        } else {
            authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostAuthenticateWithID(userID, authenticationResult, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }
        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener) || isNotClaimValueEncryptionListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreAddUserWithID(userName, credential, roleList, claims, profile, userStoreManager)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                 String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        User user = ((AbstractUserStoreManager) userStoreManager).getUser(null, userName);
        if (user == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostAddUserWithID(user, credential, roleList, claims, profile,
                                userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateCredentialWithID(userID, newCredential, oldCredential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostUpdateCredentialWithID(userID, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String userName, Object newCredential,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateCredentialByAdminWithID(userID, newCredential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostUpdateCredentialByAdminWithID(userID, credential, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }
        // Setting the thread-local to keep userID for doPostDeleteUserWithID listener.
        IdentityUtil.threadLocalProperties.get().put(DO_PRE_DELETE_USER_USER_ID, userID);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener).doPreDeleteUserWithID(userID, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        try {
            // Getting the userName from thread-local which has been set from doPreDeleteUserWithID.
            String userID = (String) IdentityUtil.threadLocalProperties.get().get(DO_PRE_DELETE_USER_USER_ID);

            for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
                if (isNotAResolverListener(listener)) {
                    if (!((UniqueIDUserOperationEventListener) listener)
                            .doPostDeleteUserWithID(userID, userStoreManager)) {
                        return false;
                    }
                }
            }
        } finally {
            // Remove thread local variable.
            IdentityUtil.threadLocalProperties.get().remove(DO_PRE_DELETE_USER_USER_ID);
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener) || isNotClaimValueEncryptionListener(listener)) {
                return ((UniqueIDUserOperationEventListener) listener)
                        .doPreSetUserClaimValueWithID(userID, claimURI, claimValue, profileName, userStoreManager);
            }
        }

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener) || isNotClaimValueEncryptionListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostSetUserClaimValueWithID(userID, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener) || isNotClaimValueEncryptionListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreSetUserClaimValuesWithID(userID, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostSetUserClaimValuesWithID(userID, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreDeleteUserClaimValuesWithID(userID, claims, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostDeleteUserClaimValuesWithID(userID, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreDeleteUserClaimValueWithID(userID, claimURI, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);

        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostDeleteUserClaimValueWithID(userID, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions,
                                UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userIDs;
        try {
            userIDs = getUserIdsFromUserNames(userList, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {

            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreAddRoleWithID(roleName, userIDs, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userIDs;
        try {
            userIDs = getUserIdsFromUserNames(userList, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostAddRoleWithID(roleName, userIDs, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreAddInternalRole(String roleName, String[] userList, Permission[] permissions,
                                        UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userIDs;
        try {
            userIDs = getUserIdsFromUserNames(userList, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreAddInternalRoleWithID(roleName, userIDs, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostAddInternalRole(String roleName, String[] userList, Permission[] permissions,
                                         UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] userIDs;
        try {
            userIDs = getUserIdsFromUserNames(userList, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostAddInternalRoleWithID(roleName, userIDs, permissions, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfInternalRole(String roleName, String deletedUsers[], String[] newUsers,
                                                     UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserIDs;
        try {
            deletedUserIDs = getUserIdsFromUserNames(deletedUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        String[] newUserIDs;
        try {
            newUserIDs = getUserIdsFromUserNames(newUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateUserListOfInternalRoleWithID(roleName, deletedUserIDs, newUserIDs, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserIDs;
        try {
            deletedUserIDs = getUserIdsFromUserNames(deletedUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        String[] newUserIDs;
        try {
            newUserIDs = getUserIdsFromUserNames(newUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfInternalRole(String roleName, String[] deletedUsers, String[] newUsers,
                                                      UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserIDs;
        try {
            deletedUserIDs = getUserIdsFromUserNames(deletedUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        String[] newUserIDs;
        try {
            newUserIDs = getUserIdsFromUserNames(newUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostUpdateUserListOfInternalRoleWithID(roleName, deletedUserIDs, newUserIDs, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String[] deletedUserIDs;
        try {
            deletedUserIDs = getUserIdsFromUserNames(deletedUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        String[] newUserIDs;
        try {
            newUserIDs = getUserIdsFromUserNames(newUsers, (AbstractUserStoreManager) userStoreManager);
        } catch (UserStoreException e) {
            // User ID cannot get for the user names. This is probably the user store manager is not an unique id
            // supported user store.
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostUpdateUserListOfRoleWithID(roleName, deletedUserIDs, newUserIDs, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateInternalRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                                     UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateInternalRoleListOfUserWithID(userID, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostUpdateRoleListOfUserWithID(userID, deletedRoles, newRoles, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValue(String userName, String claim, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreGetUserClaimValueWithID(userID, claim, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserClaimValues(String userName, String[] claims, String profileName,
                                           Map<String, String> claimMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreGetUserClaimValuesWithID(userID, claims, profileName, claimMap, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValue(String userName, String claim, List<String> claimValue, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserClaimValueWithID(userID, claim, claimValue, profileName, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserClaimValues(String userName, String[] claims, String profileName,
                                            Map<String, String> claimMap, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserClaimValuesWithID(userID, claims, profileName, claimMap, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserList(String claimUri, String claimValue, List<String> returnUserNameList,
                                    UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreGetUserListWithID(claimUri, claimValue, returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                    String sortBy, String sortOrder, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder,
                                userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPreGetUserList(String claimUri, String claimValue, int limit, int offset,
                                    List<String> returnUserNameList, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPreGetUserListWithID(claimUri, claimValue, limit, offset, returnUsersList,
                                userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, List<String> returnUserNameList,
                                     UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserListWithID(claimUri, claimValue, returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserList(String claimUri, String claimValue, List<String> returnUserNameList, int limit,
                                     int offset, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserListWithID(claimUri, claimValue, returnUsersList, limit, offset,
                                userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserList(Condition condition, String domain, String profileName, int limit, int offset,
                                     String sortBy, String sortOrder, String[] returnUserNameList,
                                     UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager,
                Arrays.asList(returnUserNameList));

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserListWithID(condition, domain, profileName, limit, offset, sortBy, sortOrder,
                                returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    private List<User> getUsersFromNames(AbstractUserStoreManager userStoreManager, List<String> userList)
            throws UserStoreException {

        return userStoreManager.getUsersFromUserNames(getDomainLessNamesFromList(userList));
    }

    private List<String> getDomainLessNamesFromList(List<String> userList) {

        if (userList == null) {
            return new ArrayList<>();
        }
        return userList.stream().map(UserCoreUtil::removeDomainFromName).collect(Collectors.toList());
    }

    private String[] getDomainLessNames(String[] names) {

        if (names == null) {
            return new String[0];
        }
        return Arrays.stream(names).map(UserCoreUtil::removeDomainFromName).toArray(String[]::new);
    }

    @Override
    public boolean doPostGetPaginatedUserList(String claimUri, String claimValue, List<String> returnUserNameList,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetPaginatedUserListWithID(claimUri, claimValue, returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostListUsers(String filter, int limit, int offset, List<String> returnUserNameList,
                                   UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager, returnUserNameList);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostListUsersWithID(filter, limit, offset, returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUser(String userName, String filter, String[] roleList,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        String userID = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(userName);
        if (userID == null) {
            return handleUserIDResolveFailure(userName, userStoreManager);
        }

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetRoleListOfUserWithID(userID, filter, roleList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUserListOfRole(String roleName, String[] returnUserNameList,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<User> returnUsersList = getUsersFromNames((AbstractUserStoreManager) userStoreManager,
                Arrays.asList(getDomainLessNames(returnUserNameList)));

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUserListOfRoleWithID(roleName, returnUsersList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetRoleListOfUsers(String[] userNames, Map<String, List<String>> rolesOfUsersMap,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> userIDsList = ((AbstractUserStoreManager) userStoreManager)
                .getUserIDsFromUserNames(Arrays.asList(userNames));

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetRoleListOfUsersWithID(userIDsList, rolesOfUsersMap, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean doPostGetUsersClaimValues(String[] userNames, String[] claims, String profileName,
                                             UserClaimSearchEntry[] userClaimSearchEntries,
                                             UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        List<String> userIDsList = ((AbstractUserStoreManager) userStoreManager)
                .getUserIDsFromUserNames(Arrays.asList(getDomainLessNames(userNames)));
        List<String> claimsList = Arrays.asList(claims);
        List<UniqueIDUserClaimSearchEntry> uniqueIDUserClaimSearchEntriesList =
                ((AbstractUserStoreManager) userStoreManager)
                        .getUniqueIDUserClaimSearchEntries(userClaimSearchEntries);

        for (UserOperationEventListener listener : getUserStoreManagerListeners()) {
            if (isNotAResolverListener(listener)) {
                if (!((UniqueIDUserOperationEventListener) listener)
                        .doPostGetUsersClaimValuesWithID(userIDsList, claimsList, profileName,
                                uniqueIDUserClaimSearchEntriesList, userStoreManager)) {
                    return false;
                }
            }
        }

        return true;
    }

    private String[] getUserIdsFromUserNames(String[] userNames, AbstractUserStoreManager userStoreManager)
            throws UserStoreException {

        if (ArrayUtils.isEmpty(userNames)) {
            return new String[0];
        }

        List<String> userIDsList = userStoreManager.getUserIDsFromUserNames(Arrays.asList(userNames));

        return userIDsList.toArray(new String[0]);
    }

    private boolean handleUserIDResolveFailure(String userName, UserStoreManager userStoreManager) {

        if (log.isDebugEnabled()) {
            log.debug("A userID cannot be found in the userStoreManager" + getUserStoreDomainName(userStoreManager)
                    + "for the given userName: " + userName);
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

    private Collection<UserOperationEventListener> getUserStoreManagerListeners() {

        Map<Integer, UserOperationEventListener> userOperationEventListeners =
                IdentityMgtServiceDataHolder.getInstance().getUserOperationEventListeners();
        return userOperationEventListeners.values();
    }

    private boolean isNotAResolverListener(UserOperationEventListener listener) {

        return !(listener instanceof IdentityUserNameResolverListener)
                && !(listener instanceof IdentityUserIdResolverListener);
    }

    private boolean isNotClaimValueEncryptionListener(UserOperationEventListener listener) {

        return !(listener instanceof IdentityClaimValueEncryptionListener);
    }
}
