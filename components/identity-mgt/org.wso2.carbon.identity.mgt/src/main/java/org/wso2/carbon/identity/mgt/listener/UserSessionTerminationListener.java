/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.identity.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.core.AbstractIdentityUserOperationEventListener;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

/**
 * This is an implementation of UserOperationEventListener which is responsible for termination of active sessions
 * of the user.
 */
public class UserSessionTerminationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UserSessionTerminationListener.class);

    private static final String USER_SESSION_MAPPING_ENABLED =
            "JDBCPersistenceManager.SessionDataPersist.UserSessionMapping.Enable";

    @Override
    public int getExecutionOrderId() {

        int orderId = getOrderId();
        if (orderId != IdentityCoreConstants.EVENT_LISTENER_ORDER_ID) {
            return orderId;
        }

        return 85;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String username, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the password reset user: " + username);
        }

        terminateSessionsOfUser(username, userStoreManager);
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String username, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the deleted user: " + username);
        }

        terminateSessionsOfUser(username, userStoreManager);
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValues(String username, Map<String, String> claims, String profileName, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        terminateSessionsOfLockedUserAccount(username, claims, userStoreManager);
        terminateSessionsOfDisabledUserAccount(username, claims, userStoreManager);
        return true;
    }

    @Override
    public boolean doPostSetUserClaimValue(String username, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        return true;
    }

    private void terminateSessionsOfLockedUserAccount(String username, Map<String, String> claims, UserStoreManager
            userStoreManager) throws UserStoreException {

        String errorCode = (String) IdentityUtil.threadLocalProperties.get().get(IdentityCoreConstants.USER_ACCOUNT_STATE);

        if (errorCode != null && (errorCode.equalsIgnoreCase(UserCoreConstants.ErrorCode.USER_IS_LOCKED))) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all active sessions of the locked user: " + username);
            }
            terminateSessionsOfUser(username, userStoreManager);
        }
    }

    private void terminateSessionsOfDisabledUserAccount(String username, Map<String, String> claims, UserStoreManager
            userStoreManager) throws UserStoreException {

        String errorCode = (String) IdentityUtil.threadLocalProperties.get().get(IdentityCoreConstants.USER_ACCOUNT_STATE);

        if (errorCode != null && errorCode.equalsIgnoreCase(IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE)) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all active sessions of the disabled user account of user: " + username);
            }
            terminateSessionsOfUser(username, userStoreManager);
        }
    }

    private void terminateSessionsOfUser(String username, UserStoreManager userStoreManager) throws UserStoreException {

        String userStoreDomain = userStoreManager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants
                .RealmConfig.PROPERTY_DOMAIN_NAME);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());

        try {
            IdentityMgtServiceComponent.getUserSessionManagementService().terminateSessionsOfUser(username,
                    userStoreDomain, tenantDomain);
        } catch (UserSessionException e) {
            log.error("Failed to terminate active sessions of user: " + username, e);
        }
    }
}
