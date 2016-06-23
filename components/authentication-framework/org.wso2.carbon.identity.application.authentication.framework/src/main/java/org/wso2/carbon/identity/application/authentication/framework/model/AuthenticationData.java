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

package org.wso2.carbon.identity.application.authentication.framework.model;

public class AuthenticationData {

    private String eventId;
    private String contextId;
    private boolean federated;
    private boolean authnSuccess;
    private String username;
    private String userStoreDomain;
    private String tenantDomain;
    private String remoteIp;
    private String serviceProvider;
    private String inboundProtocol;
    private boolean rememberMe;
    private boolean forcedAuthn;
    private boolean passive;
    private boolean initialLogin;

    private int stepNo;
    private String identityProvider;
    private String authenticator;
    private boolean success;


    public String getEventId() {

        return eventId;
    }

    public void setEventId(String eventId) {

        this.eventId = eventId;
    }

    public String getContextId() {

        return contextId;
    }

    public void setContextId(String contextId) {

        this.contextId = contextId;
    }

    public boolean isFederated() {

        return federated;
    }

    public void setFederated(boolean federated) {

        this.federated = federated;
    }

    public boolean isAuthnSuccess() {

        return authnSuccess;
    }

    public void setAuthnSuccess(boolean authnSuccess) {

        this.authnSuccess = authnSuccess;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getUserStoreDomain() {

        return userStoreDomain;
    }

    public void setUserStoreDomain(String userStoreDomain) {

        this.userStoreDomain = userStoreDomain;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public String getRemoteIp() {

        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {

        this.remoteIp = remoteIp;
    }

    public String getServiceProvider() {

        return serviceProvider;
    }

    public void setServiceProvider(String serviceProvider) {

        this.serviceProvider = serviceProvider;
    }

    public String getInboundProtocol() {

        return inboundProtocol;
    }

    public void setInboundProtocol(String inboundProtocol) {

        this.inboundProtocol = inboundProtocol;
    }

    public boolean isRememberMe() {

        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {

        this.rememberMe = rememberMe;
    }

    public boolean isForcedAuthn() {

        return forcedAuthn;
    }

    public void setForcedAuthn(boolean forcedAuthn) {

        this.forcedAuthn = forcedAuthn;
    }

    public boolean isPassive() {

        return passive;
    }

    public void setPassive(boolean passive) {

        this.passive = passive;
    }

    public boolean isInitialLogin() {

        return initialLogin;
    }

    public void setInitialLogin(boolean initialLogin) {

        this.initialLogin = initialLogin;
    }

    public int getStepNo() {

        return stepNo;
    }

    public void setStepNo(int stepNo) {

        this.stepNo = stepNo;
    }

    public String getIdentityProvider() {

        return identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {

        this.identityProvider = identityProvider;
    }

    public boolean isSuccess() {

        return success;
    }

    public void setSuccess(boolean success) {

        this.success = success;
    }

    public String getAuthenticator() {

        return authenticator;
    }

    public void setAuthenticator(String authenticator) {

        this.authenticator = authenticator;
    }
}
