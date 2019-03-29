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
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceDataHolder;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ACCOUNT_DISABLED_CLAIM_URI;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ACCOUNT_LOCKED_CLAIM_URI;

/**
 * This is an implementation of UserOperationEventListener which is responsible for termination of active sessions
 * of the user.
 */
public class UserSessionTerminationListener extends AbstractIdentityUserOperationEventListener {

    private static final Log log = LogFactory.getLog(UserSessionTerminationListener.class);

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

        if (!IdentityMgtServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the password reset user: " + username);
        }

        terminateSessionsOfUser(username, userStoreManager);
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String username, UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!IdentityMgtServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the deleted user: " + username);
        }

        terminateSessionsOfUser(username, userStoreManager);
        return true;
    }

    @Override
    public boolean doPreSetUserClaimValues(String username, Map<String, String> claims, String profileName,
            UserStoreManager userStoreManager) throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!IdentityMgtServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
            return true;
        }

        if (isAccountLocked(claims) || isAccountDisabled(claims)) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all the active sessions of the user: " + username);
            }
            terminateSessionsOfUser(username, userStoreManager);
        }
        return true;
    }

    private boolean isAccountLocked(Map<String, String> claims) {

        return claims != null && claims.containsKey(ACCOUNT_LOCKED_CLAIM_URI) && Boolean
                .valueOf(claims.get(ACCOUNT_LOCKED_CLAIM_URI));
    }

    private boolean isAccountDisabled(Map<String, String> claims) {

        return claims != null && claims.containsKey(ACCOUNT_DISABLED_CLAIM_URI) && Boolean
                .valueOf(claims.get(ACCOUNT_DISABLED_CLAIM_URI));
    }

    private void terminateSessionsOfUser(String username, UserStoreManager userStoreManager) throws UserStoreException {

        String userStoreDomain = userStoreManager.getRealmConfiguration()
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
        String tenantDomain = IdentityTenantUtil.getTenantDomain(userStoreManager.getTenantId());

        try {
            IdentityMgtServiceComponent.getUserSessionManagementService()
                    .terminateSessionsOfUser(username, userStoreDomain, tenantDomain);
        } catch (UserSessionException e) {
            log.error("Failed to terminate active sessions of user: " + username, e);
        }
    }
}
