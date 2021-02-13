/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ServerSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

/**
 * A service to terminate the sessions of federated users
 */
public class ServerSessionManagementServiceImpl implements ServerSessionManagementService {

    private static Log log = LogFactory.getLog(ServerSessionManagementServiceImpl.class);

    @Override
    public boolean removeSession(String sessionId) {

        if (StringUtils.isBlank(sessionId)) {
            return false;
        }
        // Retrieve session information from cache in order to publish event
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId);
        terminateSession(sessionContext, sessionId);
        return true;
    }

    /**
     * Terminate the session by sessionId
     *
     * @param sessionContext - session context for the sessionId
     * @param sessionId - Session id of the federated user
     */
    private void terminateSession(SessionContext sessionContext, String sessionId) {

        if (FrameworkServiceDataHolder.getInstance().getAuthnDataPublisherProxy() != null && FrameworkServiceDataHolder
                .getInstance().getAuthnDataPublisherProxy().isEnabled(null) && sessionContext != null) {

            Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            if (authenticatedUserObj != null) {
                authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            }
            FrameworkUtils.publishSessionEvent(sessionId, null, null, sessionContext, authenticatedUser,
                    FrameworkConstants.AnalyticsAttributes.SESSION_TERMINATE);
        }
        SessionContextCache.getInstance().clearCacheEntry(sessionId);
    }
}
