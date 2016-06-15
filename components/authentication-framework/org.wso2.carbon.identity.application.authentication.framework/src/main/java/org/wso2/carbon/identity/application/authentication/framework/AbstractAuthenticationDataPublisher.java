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

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationData;
import org.wso2.carbon.identity.application.authentication.framework.model.SessionData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAuthenticationDataPublisher extends AbstractIdentityHandler {

    public void publishAuthenticationStepSuccess(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> params) {

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
        authenticationData.setRemoteIp(request.getRemoteAddr());
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

    public void publishAuthenticationStepFailure(HttpServletRequest request, AuthenticationContext context,
                                                 Map<String, Object> params) {

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
        authenticationData.setRemoteIp(request.getRemoteAddr());
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

    public void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params) {

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
        authenticationData.setRemoteIp(request.getRemoteAddr());
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        doPublishAuthenticationSuccess(authenticationData);
    }

    public void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context,
                                             Map<String, Object> params) {

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
        authenticationData.setRemoteIp(request.getRemoteAddr());
        authenticationData.setServiceProvider(context.getServiceProviderName());
        authenticationData.setInboundProtocol(context.getRequestType());
        authenticationData.setRememberMe(context.isRememberMe());
        authenticationData.setForcedAuthn(context.isForceAuthenticate());
        authenticationData.setPassive(context.isPassiveAuthenticate());
        authenticationData.setInitialLogin(true);
        doPublishAuthenticationFailure(authenticationData);
    }

    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params) {

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
        }
        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(createdTime);
        sessionData.setTerminationTimestamp(terminationTime);
        if (context != null) {
            sessionData.setIsRememberMe(context.isRememberMe());
        }

        doPublishSessionCreation(sessionData);
    }

    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
                                          SessionContext sessionContext, Map<String, Object> params) {

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
        }

        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(currentTime);
        sessionData.setTerminationTimestamp(currentTime);
        if (context != null) {
            sessionData.setIsRememberMe(context.isRememberMe());
        }
        doPublishSessionTermination(sessionData);

    }

    public void publishSessionUpdate(HttpServletRequest request, AuthenticationContext context, SessionContext
            sessionContext, Map<String, Object> params) {

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
        }

        sessionData.setUser(userName);
        sessionData.setUserStoreDomain(userStoreDomain);
        sessionData.setTenantDomain(tenantDomain);
        sessionData.setSessionId(sessionId);
        sessionData.setCreatedTimestamp(createdTime);
        sessionData.setUpdatedTimestamp(currentTime);
        sessionData.setTerminationTimestamp(terminationTime);
        if (context != null) {
            sessionData.setIsRememberMe(context.isRememberMe());
        }
        doPublishSessionUpdate(sessionData);
    }

    public abstract void doPublishAuthenticationStepSuccess(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationStepFailure(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationSuccess(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationFailure(AuthenticationData authenticationData);

    public abstract void doPublishSessionCreation(SessionData sessionData);

    public abstract void doPublishSessionUpdate(SessionData sessionData);

    public abstract void doPublishSessionTermination(SessionData sessionData);

    protected long getSessionExpirationTime(long createdTime, long updatedTime, String tenantDomain,
                                            boolean isRememberMe) {
        if (isRememberMe) {
            long rememberMeTimeout = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getRememberMeTimeout(tenantDomain));
            return createdTime + rememberMeTimeout;
        }
        long idleSessionTimeOut = TimeUnit.SECONDS.toMillis(IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        return idleSessionTimeOut + updatedTime;
    }
}
