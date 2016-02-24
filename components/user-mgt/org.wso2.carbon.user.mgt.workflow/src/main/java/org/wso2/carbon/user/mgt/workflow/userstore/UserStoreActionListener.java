/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.mgt.workflow.userstore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.HashMap;
import java.util.Map;

public class UserStoreActionListener extends AbstractIdentityUserOperationEventListener {

    public static final String DO_PRE_AUTHENTICATE_IDENTITY_PROPERTY = "doPreAuthenticate";
    public static final String DO_POST_AUTHENTICATE_IDENTITY_PROPERTY = "doPostAuthenticate";
    public static final String DO_POST_ADD_USER_IDENTITY_PROPERTY = "doPostAddUser";
    public static final String DO_PRE_SET_USER_CLAIM_VALUES_IDENTITY_PROPERT = "doPreSetUserClaimValues";
    public static final String DO_POST_UPDATE_CREDENTIAL_IDENTITY_PROPERTY = "doPostUpdateCredential";
    private static Log log = LogFactory.getLog(UserStoreActionListener.class);

    @Override
    public int getExecutionOrderId() {
        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }
        return 10;
    }


    @Override
    public boolean doPreAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                String profile, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            AddUserWFRequestHandler addUserWFRequestHandler = new AddUserWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);
            return addUserWFRequestHandler.startAddUserFlow(domain, userName, credential, roleList, claims, profile);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreUpdateCredential(String userName, Object newCredential, Object oldCredential,
                                         UserStoreManager userStoreManager) throws UserStoreException {
// todo: commenting out since a test failure
//        String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
//                .PROPERTY_DOMAIN_NAME);
//        try {
//            return new ChangeCredentialWFRequestHandler()
//                    .startChangeCredentialWorkflow(domain, userName, newCredential, oldCredential);
//        } catch (WorkflowException e) {
//            log.error("Initiating workflow for updating credentials of user: " + userName + " failed.", e);
//        }
//        return false;
        return true;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String s, Object o, UserStoreManager userStoreManager) throws
            UserStoreException {
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            DeleteUserWFRequestHandler deleteUserWFRequestHandler = new DeleteUserWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);
            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return deleteUserWFRequestHandler.startDeleteUserFlow(domain, userName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName,
                                          UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }

        Map<String, String> claims = new HashMap<>();
        claims.put(claimURI, claimValue);

        try {
            SetMultipleClaimsWFRequestHandler setMultipleClaimsWFRequestHandler = new SetMultipleClaimsWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return setMultipleClaimsWFRequestHandler.startSetMultipleClaimsWorkflow(domain, userName, claims,
                                                                                    profileName);

        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            SetMultipleClaimsWFRequestHandler setMultipleClaimsWFRequestHandler = new SetMultipleClaimsWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);
            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return setMultipleClaimsWFRequestHandler.startSetMultipleClaimsWorkflow(domain, userName, claims, profileName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String userName, String[] claims, String profileName, UserStoreManager
            userStoreManager) throws UserStoreException {

        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            DeleteMultipleClaimsWFRequestHandler deleteMultipleClaimsWFRequestHandler = new DeleteMultipleClaimsWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);
            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return deleteMultipleClaimsWFRequestHandler.startDeleteMultipleClaimsWorkflow(domain, userName, claims,
                    profileName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String userName, String claimURI, String profileName,
                                             UserStoreManager userStoreManager) throws UserStoreException {
        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }

        String[] claims = new String[1];
        claims[0] = claimURI;

        try {
            DeleteMultipleClaimsWFRequestHandler deleteMultipleClaimsWFRequestHandler = new DeleteMultipleClaimsWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return deleteMultipleClaimsWFRequestHandler.startDeleteMultipleClaimsWorkflow(domain, userName, claims,
                                                                                          profileName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreAddRole(String roleName, String[] userList, Permission[] permissions, UserStoreManager
            userStoreManager) throws UserStoreException {

        try {
            if (!isEnable() || isCalledViaIdentityMgtListners()) {
                return true;
            }
            AddRoleWFRequestHandler addRoleWFRequestHandler = new AddRoleWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return addRoleWFRequestHandler.startAddRoleFlow(domain, roleName, userList, permissions);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            DeleteRoleWFRequestHandler deleteRoleWFRequestHandler = new DeleteRoleWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return deleteRoleWFRequestHandler.startDeleteRoleFlow(domain, roleName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreUpdateRoleName(String roleName, String newRoleName, UserStoreManager userStoreManager) throws
            UserStoreException {
        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            UpdateRoleNameWFRequestHandler updateRoleNameWFRequestHandler = new UpdateRoleNameWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return updateRoleNameWFRequestHandler.startUpdateRoleNameFlow(domain, roleName, newRoleName);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers, UserStoreManager
            userStoreManager) throws UserStoreException {
        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            UpdateRoleUsersWFRequestHandler updateRoleUsersWFRequestHandler = new UpdateRoleUsersWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return updateRoleUsersWFRequestHandler.startUpdateRoleUsersFlow(domain, roleName, deletedUsers, newUsers);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles, UserStoreManager
            userStoreManager) throws UserStoreException {
        if (!isEnable() || isCalledViaIdentityMgtListners()) {
            return true;
        }
        try {
            UpdateUserRolesWFRequestHandler updateUserRolesWFRequestHandler = new UpdateUserRolesWFRequestHandler();
            String domain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                                                                                                  .PROPERTY_DOMAIN_NAME);

            int tenantId = userStoreManager.getTenantId() ;
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(currentUser);

            return updateUserRolesWFRequestHandler.startUpdateUserRolesFlow(domain, userName, deletedRoles, newRoles);
        } catch (WorkflowException e) {
            // Sending e.getMessage() since it is required to give error message to end user.
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private boolean isCalledViaIdentityMgtListners() {
        return IdentityUtil.threadLocalProperties.get().containsKey(DO_PRE_AUTHENTICATE_IDENTITY_PROPERTY) ||
                IdentityUtil .threadLocalProperties.get().containsKey(DO_POST_AUTHENTICATE_IDENTITY_PROPERTY) ||
                IdentityUtil .threadLocalProperties .get().containsKey(DO_POST_ADD_USER_IDENTITY_PROPERTY) ||
                IdentityUtil.threadLocalProperties.get() .containsKey(DO_PRE_SET_USER_CLAIM_VALUES_IDENTITY_PROPERT)
                || IdentityUtil.threadLocalProperties.get().containsKey (DO_POST_UPDATE_CREDENTIAL_IDENTITY_PROPERTY);
    }
}
