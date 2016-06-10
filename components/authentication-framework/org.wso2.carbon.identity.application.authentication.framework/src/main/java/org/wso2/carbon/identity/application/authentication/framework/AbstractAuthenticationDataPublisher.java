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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

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

    public void publishSessionCreation(HttpServletRequest request, AuthenticationContext context,
                                       Map<String, Object> params) {

        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        String sessionId = (String) params.get(FrameworkConstants.PublisherParamNames.SESSION_ID);
        String userName = null;
        String userStoreDomain = null;
        String tenantDomain = null;
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            userName = user.getUserName();
            userStoreDomain = user.getUserStoreDomain();
            tenantDomain = user.getTenantDomain();
        }
//        doPublishSessionCreation(userName, userStoreDomain, tenantDomain, sessionId,
//                System.currentTimeMillis(), context.isRememberMe());
    }

    public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
                                          Map<String, Object> params) {
        Object userObj = params.get(FrameworkConstants.PublisherParamNames.USER);
        String sessionId = (String) params.get(FrameworkConstants.PublisherParamNames.SESSION_ID);
        String userName = null;
        String userStoreDomain = null;
        String tenantDomain = null;
        if (userObj != null && userObj instanceof AuthenticatedUser) {
            AuthenticatedUser user = (AuthenticatedUser) userObj;
            userName = user.getUserName();
            userStoreDomain = user.getUserStoreDomain();
            tenantDomain = user.getTenantDomain();
        }
//        doPublishSessionTermination(userName, userStoreDomain, tenantDomain, sessionId,
//                System.currentTimeMillis(), context.isRememberMe());
    }

    public abstract void doPublishAuthenticationStepSuccess(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationStepFailure(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationSuccess(AuthenticationData authenticationData);

    public abstract void doPublishAuthenticationFailure(AuthenticationData authenticationData);

    public abstract void doPublishSessionCreation(String user, String userStoreDomain, String tenantDomain,
                                                  String sessionId, long createdTimestamp, long updatedTimestamp,
                                                  long terminationTimestamp, boolean isRememberMe);

    public abstract void doPublishSessionUpdate(String user, String userStoreDomain, String tenantDomain,
                                                     String sessionId, long createdTimestamp, long updatedTimestamp,
                                                     long terminationTimestamp, boolean isRememberMe);

    public abstract void doPublishSessionTermination(String user, String userStoreDomain, String tenantDomain,
                                                     String sessionId, long createdTimestamp, long updatedTimestamp,
                                                     long terminationTimestamp, boolean isRememberMe);
}
