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
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ServerSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;

/**
 * A service to terminate the sessions of federated users
 */
public class ServerSessionManagementServiceImpl implements ServerSessionManagementService {

    private static Log log = LogFactory.getLog(ServerSessionManagementServiceImpl.class);

    private static final org.apache.commons.logging.Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;

    @Override
    public boolean removeSession(String sessionId) {

        if (StringUtils.isBlank(sessionId)) {
            return false;
        }
        // Retrieve session information from cache in order to publish event
        SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionId,
                FrameworkUtils.getLoginTenantDomainFromContext());
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

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        if (FrameworkServiceDataHolder.getInstance().getAuthnDataPublisherProxy() != null && FrameworkServiceDataHolder
                .getInstance().getAuthnDataPublisherProxy().isEnabled(null) && sessionContext != null) {

            Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
            if (authenticatedUserObj != null) {
                authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            }
            FrameworkUtils.publishSessionEvent(sessionId, null, null, sessionContext, authenticatedUser,
                    FrameworkConstants.AnalyticsAttributes.SESSION_TERMINATE);
        }
        if (sessionContext == null) {
            if (log.isDebugEnabled()) {
                log.debug("The session context is not available for " + sessionId);
            }
            return;
        }
        Object tenantDomainObj = sessionContext.getProperty(FrameworkUtils.TENANT_DOMAIN);
        if (tenantDomainObj != null) {
            SessionContextCache.getInstance().clearCacheEntry(sessionId, (String) tenantDomainObj);
        } else {
            SessionContextCache.getInstance().clearCacheEntry(sessionId);
        }
        addAuditLogs(sessionId, CarbonContext.getThreadLocalCarbonContext().getUsername(),
                authenticatedUser.getUserName(), (String) tenantDomainObj, FrameworkUtils.getCorrelation(),
                System.currentTimeMillis());
    }

    private void addAuditLogs(String sessionKey, String initiator, String authenticatedUser, String userTenantDomain,
                              String traceId, Long terminatedTimestamp) {

        JSONObject auditData = new JSONObject();
        auditData.put(SessionMgtConstants.SESSION_CONTEXT_ID, sessionKey);
        auditData.put(SessionMgtConstants.AUTHENTICATED_USER, authenticatedUser);
        auditData.put(SessionMgtConstants.AUTHENTICATED_USER_TENANT_DOMAIN, userTenantDomain);
        auditData.put(SessionMgtConstants.TRACE_ID, traceId);
        auditData.put(SessionMgtConstants.SESSION_TERMINATE_TIMESTAMP, terminatedTimestamp);
        AUDIT_LOG.info(String.format(SessionMgtConstants.AUDIT_MESSAGE_TEMPLATE, initiator,
                SessionMgtConstants.TERMINATE_SESSION_ACTION, auditData, SessionMgtConstants.SUCCESS));
    }
}
