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
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

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
    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the password reset user: " + userName);
        }

        try {
            IdentityMgtServiceComponent.getUserSessionManagementService()
                    .terminateSessionsOfUser(userName);
        } catch (UserSessionException e) {
            throw new UserStoreException("Error while terminating the sessions of the user: " + userName +
                    " while user password reset.", e);
        }
        return true;
    }

    @Override
    public boolean doPreDeleteUser(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (!isEnable()) {
            return true;
        }

        if (!Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED))) {
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminating all the active sessions of the deleted user: " + userName);
        }

        try {
            IdentityMgtServiceComponent.getUserSessionManagementService()
                    .terminateSessionsOfUser(userName);
        } catch (UserSessionException e) {
            throw new UserStoreException("Error while terminating the active sessions of the user: " + userName +
                    " while user deletion.", e);
        }
        return true;
    }
}
