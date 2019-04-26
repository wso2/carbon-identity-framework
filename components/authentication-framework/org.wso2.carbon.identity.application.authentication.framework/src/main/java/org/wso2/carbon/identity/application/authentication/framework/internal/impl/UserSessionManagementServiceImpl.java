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
package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

/**
 * This a service class used to manage user sessions.
 */
public class UserSessionManagementServiceImpl implements UserSessionManagementService {

    private static final Log log = LogFactory.getLog(UserSessionManagementServiceImpl.class);
    private SessionManagementService sessionManagementService = new SessionManagementService();

    @Override
    public void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException {

        validate(username, userStoreDomain, tenantDomain);
        List<String> sessionListOfUser = getSessionsOfUser(username, userStoreDomain, tenantDomain);

        if (!sessionListOfUser.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Terminating all the active sessions of user: " + username + " of userstore domain: " +
                        userStoreDomain + " in tenant: " + tenantDomain);
            }
            terminateSessionsOfUser(sessionListOfUser);
        }
    }

    private void validate(String username, String userStoreDomain, String tenantDomain) throws UserSessionException {

        if (StringUtils.isBlank(username) || StringUtils.isBlank(userStoreDomain) || StringUtils.isBlank(tenantDomain)) {
            throw new UserSessionException("Usename, userstore domain or tenant domain cannot be empty");
        }

        int tenantId = getTenantId(tenantDomain);
        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            throw new UserSessionException("Invalid tenant domain: " + tenantDomain + " provided.");
        }
    }

    private List<String> getSessionsOfUser(String username, String userStoreDomain, String tenantDomain) throws
            UserSessionException {

        String userId = UserSessionStore.getInstance().getUserId(username, getTenantId(tenantDomain),
                userStoreDomain);
        return UserSessionStore.getInstance().getSessionId(userId);
    }

    private void terminateSessionsOfUser(List<String> sessionList) {

        for (String session : sessionList) {
            sessionManagementService.removeSession(session);
        }
    }

    private int getTenantId(String tenantDomain) throws UserSessionException {

        try {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            return realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new UserSessionException("Failed to retrieve tenant id for tenant domain: " + tenantDomain);
        }
    }
}
