/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.authentication.framework.services;

import org.apache.commons.lang.StringUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.authentication.framework.ServerSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

public class SessionManagementService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(SessionManagementService.class);

    public boolean removeSession(String sessionId) {

        ServerSessionManagementService serverSessionManagementService =
                FrameworkServiceDataHolder.getInstance().getServerSessionManagementService();
        return serverSessionManagementService.removeSession(sessionId);
    }

    /**
     *
     * Terminates the requested session, after validating whether the session belongs to the logged in user.
     * @deprecated Use UserSessionManagementService to terminate the session of the current user
     * {@link org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService#terminateSessionBySessionId(String, String)}
     * @param sessionId
     * @return
     */
    public boolean removeMySession(String sessionId) {

        if (StringUtils.isBlank(sessionId)) {
            return false;
        }
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId);
        // Check whether the session belongs to the logged in user.
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        String username = carbonContext.getUsername();
        // Extract the user store domain if there is any or set to 'PRIMARY'.
        String userStoreDomain = "PRIMARY";
        String[] usernameTokens = username.split("/");
        if (usernameTokens.length > 1) {
            userStoreDomain = usernameTokens[0];
            username = usernameTokens[1];
        }

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) sessionContext
                .getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (username.equals(authenticatedUser.getUserName())
                && userStoreDomain.equals(authenticatedUser.getUserStoreDomain())
                && carbonContext.getTenantDomain().equals(authenticatedUser.getTenantDomain())) {
            ServerSessionManagementService serverSessionManagementService =
                    FrameworkServiceDataHolder.getInstance().getServerSessionManagementService();
            return serverSessionManagementService.removeSession(sessionId);
        } else { // TODO : Handle federated scenario.
            log.warn(String.format("Trying to terminate a session which does not belong to logged in user (%s). " +
                    "This might be an attempt for a security breach", username));
            return false;
        }
    }
}
