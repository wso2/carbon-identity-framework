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

package org.wso2.carbon.identity.application.authentication.framework;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationData;
import org.wso2.carbon.identity.application.authentication.framework.model.SessionData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.handler.AbstractIdentityMessageHandler;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAuthenticationDataPublisher extends AbstractIdentityMessageHandler {

    private static final Log log = LogFactory.getLog(AbstractAuthenticationDataPublisher.class);
    public static final String UNKNOWN = "unknown";
    // HTTP headers which may contain IP address of the client in the order of priority
    private static final String[] HEADERS_WITH_IP = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    /**
     * Publish authentication success
     *
     * @param request Request which comes to the framework for authentication
     * @param context Authentication context
     * @param params  Other parameters which are need to be passed
     */
    public void publishAuthenticationStepSuccess(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing authentication step success");
        }
        AuthenticationData authenticationData = new AuthenticationData();
        int step = context.getCurrentStep();
        if (context.getExternalIdP() == null) {
            authenticationData.setIdentityProvider(FrameworkConstants.LOCAL_IDP_NAME);
        } else {
            authenticationData.setIdentityProvider(context.getExternalIdP().getIdPName());
        }
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            authenticationData.setTenantDomain(user.getTenantDomain());
            authenticationData.setUserStoreDomain(user.getUserStoreDomain());
            authenticationData.setUsername(user.getUserName());
        }
        Object isFederatedObj = params.get(FrameworkConstants.PublisherParamNames.IS_FEDERATED);
        if (isFederatedObj != null) {
            authenticationData.setFederated((Boolean) isFederatedObj);
        }
        authenticationData.setContextId(context.getContextIdentifier());
        authenticationData.setEventId(UUID.randomUUID().toString());
        authenticationData.setAuthnSuccess(false);
        authenticationData.setRemoteIp(getClientIpAddress(request));
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        authenticationData.setAuthenticator(context.getCurrentAuthenticator());
        authenticationData.setSuccess(true);
        authenticationData.setStepNo(step);
        doPublishAuthenticationStepSuccess(authenticationData);
    }

    /**
     * Published authentication step failure
     *
     * @param request Incoming Http request to framework for authentication
     * @param context Authentication Context
     * @param params  Other relevant parameters which needs to be published
     */
    public void publishAuthenticationStepFailure(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing authentication step failure");
        }
        AuthenticationData authenticationData = new AuthenticationData();
        int step = context.getCurrentStep();
        if (context.getExternalIdP() == null) {
            authenticationData.setIdentityProvider(FrameworkConstants.LOCAL_IDP_NAME);
        } else {
            authenticationData.setIdentityProvider(context.getExternalIdP().getIdPName());
        }
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            authenticationData.setTenantDomain(user.getTenantDomain());
            authenticationData.setUserStoreDomain(user.getUserStoreDomain());
            authenticationData.setUsername(user.getUserName());
        }
        Object isFederatedObj = params.get(FrameworkConstants.PublisherParamNames.IS_FEDERATED);
        if (isFederatedObj != null) {
            authenticationData.setFederated((Boolean) isFederatedObj);
        }
        authenticationData.setContextId(context.getContextIdentifier());
        authenticationData.setEventId(UUID.randomUUID().toString());
        authenticationData.setAuthnSuccess(false);
        authenticationData.setRemoteIp(getClientIpAddress(request));
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        authenticationData.setAuthenticator(context.getCurrentAuthenticator());
        authenticationData.setSuccess(false);
        authenticationData.setStepNo(step);
        doPublishAuthenticationStepFailure(authenticationData);
    }

    /**
     * Publishes authentication success
     *
     * @param request Incoming request for authentication
     * @param context Authentication context
     * @param params  Other relevant parameters which needs to be published
     */
    public void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing authentication success");
        }
        AuthenticationData authenticationData = new AuthenticationData();
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            authenticationData.setTenantDomain(user.getTenantDomain());
            authenticationData.setUserStoreDomain(user.getUserStoreDomain());
            authenticationData.setUsername(user.getUserName());
        }
        Object isFederatedObj = params.get(FrameworkConstants.PublisherParamNames.IS_FEDERATED);
        if (isFederatedObj != null) {
            authenticationData.setFederated((Boolean) isFederatedObj);
        }

        authenticationData.setSuccess(true);
        authenticationData.setContextId(context.getContextIdentifier());
        authenticationData.setEventId(UUID.randomUUID().toString());
        authenticationData.setAuthnSuccess(true);
        authenticationData.setRemoteIp(getClientIpAddress(request));
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        doPublishAuthenticationSuccess(authenticationData);
    }

    /**
     * Publishes authentication failure
     *
     * @param request Incoming authentication request
     * @param context Authentication context
     * @param params  Other relevant parameters which needs to be published
     */
    public void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing authentication failure");
        }
        AuthenticationData authenticationData = new AuthenticationData();
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            authenticationData.setTenantDomain(user.getTenantDomain());
            authenticationData.setUserStoreDomain(user.getUserStoreDomain());
            authenticationData.setUsername(user.getUserName());
        }

        authenticationData.setContextId(context.getContextIdentifier());
        authenticationData.setEventId(UUID.randomUUID().toString());
        authenticationData.setAuthnSuccess(false);
        authenticationData.setRemoteIp(getClientIpAddress(request));
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        doPublishAuthenticationFailure(authenticationData);
    }

    /**
     * Publishes session creation information
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication Context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing session creation");
        }
        SessionData sessionData = new SessionData();
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        String sessionId = (String) params.get(FrameworkConstants.PublisherParamNames.SESSION_ID);
        String userName = null;
        String userStoreDomain = null;
        String tenantDomain = null;
        Long terminationTime = null;
        Long createdTime = null;
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            userName = user.getUserName();
            userStoreDomain = user.getUserStoreDomain();
            tenantDomain = user.getTenantDomain();
        }
        if (sessionContext != null) {
            Object createdTimeObj = sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
            createdTime = (Long) createdTimeObj;
            terminationTime = getSessionExpirationTime(createdTime, createdTime, tenantDomain, sessionContext.isRememberMe());
            sessionData.setIsRememberMe(sessionContext.isRememberMe());
        }
        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(createdTime);
        sessionData.setTerminationTimestamp(terminationTime);
        sessionData.setRemoteIP(getClientIpAddress(request));

        doPublishSessionCreation(sessionData);
    }

    /**
     * Publishes session update
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionUpdate(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing session update");
        }

        SessionData sessionData = new SessionData();
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        String sessionId = (String) params.get(FrameworkConstants.PublisherParamNames.SESSION_ID);
        String userName = null;
        String userStoreDomain = null;
        String tenantDomain = null;
        Long terminationTime = null;
        Long createdTime = null;
        Long currentTime = System.currentTimeMillis();

        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            userName = user.getUserName();
            userStoreDomain = user.getUserStoreDomain();
            tenantDomain = user.getTenantDomain();
        }

        if (sessionContext != null) {
            Object createdTimeObj = sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
            createdTime = (Long) createdTimeObj;
            terminationTime = getSessionExpirationTime(createdTime, currentTime, tenantDomain, sessionContext.isRememberMe());
            sessionData.setIsRememberMe(sessionContext.isRememberMe());
        }

        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(currentTime);
        sessionData.setTerminationTimestamp(terminationTime);
        sessionData.setRemoteIP(getClientIpAddress(request));

        doPublishSessionUpdate(sessionData);
    }

    /**
     * Publishes session termination
     *
     * @param request        Incoming request for authentication
     * @param context        Authentication context
     * @param sessionContext Session context
     * @param params         Other relevant parameters which needs to be published
     */
    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
                                          SessionContext sessionContext, Map<String, Object> params) {

        if (log.isDebugEnabled()) {
            log.debug("Publishing session termination");
        }
        SessionData sessionData = new SessionData();
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        String sessionId = (String) params.get(FrameworkConstants.PublisherParamNames.SESSION_ID);
        String userName = null;
        String userStoreDomain = null;
        String tenantDomain = null;
        Long createdTime = null;
        Long currentTime = System.currentTimeMillis();
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            userName = user.getUserName();
            userStoreDomain = user.getUserStoreDomain();
            tenantDomain = user.getTenantDomain();
        }

        if (sessionContext != null) {
            Object createdTimeObj = sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
            createdTime = (Long) createdTimeObj;
            sessionData.setIsRememberMe(sessionContext.isRememberMe());
        }

        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(currentTime);
        sessionData.setTerminationTimestamp(currentTime);
        sessionData.setRemoteIP(getClientIpAddress(request));
        doPublishSessionTermination(sessionData);
    }

    /**
     * Does the publishing part of authentication step success
     *
     * @param authenticationData Bean with authentication information
     */
    public abstract void doPublishAuthenticationStepSuccess(AuthenticationData authenticationData);

    /**
     * Does the publishing part of authentication step failure
     *
     * @param authenticationData Bean with authentication information
     */
    public abstract void doPublishAuthenticationStepFailure(AuthenticationData authenticationData);

    /**
     * Does the publishing part of authentication success
     *
     * @param authenticationData Bean with authentication information
     */
    public abstract void doPublishAuthenticationSuccess(AuthenticationData authenticationData);

    /**
     * Does the publishing part of authentication step failure
     *
     * @param authenticationData Bean with authentication information
     */
    public abstract void doPublishAuthenticationFailure(AuthenticationData authenticationData);

    /**
     * Does the publishing part of session creation
     *
     * @param sessionData Bean with session information
     */
    public abstract void doPublishSessionCreation(SessionData sessionData);

    /**
     * Does the publishing part of session update
     *
     * @param sessionData Bean with session information
     */
    public abstract void doPublishSessionUpdate(SessionData sessionData);

    /**
     * Does the publishing part of session termination
     *
     * @param sessionData Bean with session information
     */
    public abstract void doPublishSessionTermination(SessionData sessionData);

    /**
     * Get the expiration time of the session
     *
     * @param createdTime  Created time of the session
     * @param updatedTime  Updated time of the session
     * @param tenantDomain Tenant Domain
     * @param isRememberMe Whether remember me is enabled
     * @return Session expiration time
     */
    protected long getSessionExpirationTime(long createdTime, long updatedTime, String tenantDomain,
                                            boolean isRememberMe) {
        // If remember me is enabled, Session termination time will be fixed
        if (isRememberMe) {
            long rememberMeTimeout = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getRememberMeTimeout(tenantDomain));
            return createdTime + rememberMeTimeout;
        }
        long idleSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        return idleSessionTimeOut + updatedTime;
    }

    /**
     * Get client IP address from the http request
     *
     * @param request http servlet request
     * @return IP address of the initial client
     */
    protected String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_WITH_IP) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !UNKNOWN.equalsIgnoreCase(ip)) {
                return getFirstIP(ip);
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Get the first IP from a comma separated list of IPs
     *
     * @param commaSeparatedIPs String which contains comma+space separated IPs
     * @return First IP
     */
    protected String getFirstIP(String commaSeparatedIPs) {
        if (StringUtils.isNotEmpty(commaSeparatedIPs) && commaSeparatedIPs.contains(",")) {
            return commaSeparatedIPs.split(",")[0];
        }
        return commaSeparatedIPs;
    }

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }
}
