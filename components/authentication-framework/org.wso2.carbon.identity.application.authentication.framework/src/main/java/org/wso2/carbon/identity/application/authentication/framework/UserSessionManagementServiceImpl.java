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
package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.List;

/**
 * This a service class used to manage user sessions.
 */
public class UserSessionManagementServiceImpl implements UserSessionManagementService {

    private static final Log log = LogFactory.getLog(UserSessionManagementServiceImpl.class);
    private SessionManagementService sessionManagementService = new SessionManagementService();

    @Override
    public void terminateSessionsOfUser(String userName, UserStoreManager userStoreManager)
            throws UserSessionException {

        List<String> sessionIdList;
        String userId;
        if (log.isDebugEnabled()) {
            log.debug("Get session details of user: " + userName);
        }
        try {
            userId = UserSessionStore.getInstance().getUserId(userName, getTenantId(userStoreManager),
                    getUserDomain(userStoreManager));
            sessionIdList = UserSessionStore.getInstance().getSessionId(userId);
        } catch (UserSessionException e) {
            throw new UserSessionException("Error while retrieving the session data of the user.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Terminate all the active sessions of user: " + userName);
        }
        for (String sessionId : sessionIdList) {
            sessionManagementService.removeSession(sessionId);
        }
    }

    private int getTenantId(UserStoreManager userStoreManager) throws UserSessionException {

        try {
            return userStoreManager.getTenantId();
        } catch (UserStoreException e) {
            throw new UserSessionException("Error while retrieving the tenant id from the user store.", e);
        }
    }

    private String getUserDomain(UserStoreManager userStoreManager) {

        return userStoreManager.getRealmConfiguration().getUserStoreProperty("DomainName");
    }
}
