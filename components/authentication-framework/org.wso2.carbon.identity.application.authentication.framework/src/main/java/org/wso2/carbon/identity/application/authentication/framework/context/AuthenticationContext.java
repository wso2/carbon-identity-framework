/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for holding data about the
 * authentication request sent from a servlet.
 */
public class AuthenticationContext implements Serializable {

    private static final long serialVersionUID = 6438291349985653301L;

    private String contextIdentifier;
    private String sessionIdentifier;
    private String callerPath;
    private String callerSessionKey;
    private String relyingParty;
    private String queryParams;
    private String requestType;
    private boolean isLogoutRequest;
    private int currentStep;
    private SequenceConfig sequenceConfig;
    private HttpServletRequest currentRequest;
    private ExternalIdPConfig externalIdP;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private boolean rememberMe;
    private String tenantDomain;
    private int retryCount;
    private Map<String, String> authenticatorProperties = new HashMap<String, String>();
    private String serviceProviderName;
    private String contextIdIncludedQueryParams;
    private String currentAuthenticator;

    private boolean forceAuthenticate;
    private boolean reAuthenticate;
    private boolean passiveAuthenticate;
    private AuthenticationRequest authenticationRequest;

    private Map<String, AuthenticatedIdPData> previousAuthenticatedIdPs = new HashMap<String, AuthenticatedIdPData>();
    private Map<String, AuthenticatedIdPData> currentAuthenticatedIdPs = new HashMap<String, AuthenticatedIdPData>();

    //flow controller flags
    private boolean requestAuthenticated = true;
    private boolean returning;
    private boolean retrying;
    private boolean previousSessionFound;

    //subject should be set by each authenticator
    private AuthenticatedUser subject;

    /* Holds any (state) information that would be required by the authenticator
     * for later processing.
	 * E.g. sessionIndex for SAMLSSOAuthenticator in SLO.
	 * Each authenticator should have an internal DTO that extends the
	 * AuthenticatorStateInfoDTO and set all the required state info in it.
	 */
    private AuthenticatorStateInfo stateInfo;

    public String getCallerPath() {
        return callerPath;
    }

    public void setCallerPath(String callerPath) {
        this.callerPath = callerPath;
    }

    public String getCallerSessionKey() {
        return callerSessionKey;
    }

    public void setCallerSessionKey(String callerSessionKey) {
        this.callerSessionKey = callerSessionKey;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public void setOrignalRequestQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public boolean isLogoutRequest() {
        return isLogoutRequest;
    }

    public void setLogoutRequest(boolean isLogoutRequest) {
        this.isLogoutRequest = isLogoutRequest;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public SequenceConfig getSequenceConfig() {
        return sequenceConfig;
    }

    public void setSequenceConfig(SequenceConfig sequenceConfig) {
        this.sequenceConfig = sequenceConfig;
    }

    public AuthenticatedUser getSubject() {
        return subject;
    }

    public void setSubject(AuthenticatedUser subject) {
        this.subject = subject;
    }

    public String getContextIdentifier() {
        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
    }

    public HttpServletRequest getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(HttpServletRequest currentRequest) {
        this.currentRequest = currentRequest;
    }

    public boolean isRequestAuthenticated() {
        return requestAuthenticated;
    }

    public void setRequestAuthenticated(boolean requestAuthenticated) {
        this.requestAuthenticated = requestAuthenticated;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public ExternalIdPConfig getExternalIdP() {
        return externalIdP;
    }

    public void setExternalIdP(ExternalIdPConfig externalIdP) {
        this.externalIdP = externalIdP;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public AuthenticatorStateInfo getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(AuthenticatorStateInfo stateInfo) {
        this.stateInfo = stateInfo;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Map<String, String> getAuthenticatorProperties() {
        return authenticatorProperties;
    }

    public void setAuthenticatorProperties(
            Map<String, String> authenticatorProperties) {
        this.authenticatorProperties = authenticatorProperties;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public boolean isForceAuthenticate() {
        return forceAuthenticate;
    }

    public void setForceAuthenticate(boolean forceAuthenticate) {
        this.forceAuthenticate = forceAuthenticate;
    }

    public boolean isPassiveAuthenticate() {
        return passiveAuthenticate;
    }

    public void setPassiveAuthenticate(boolean passiveAuthenticate) {
        this.passiveAuthenticate = passiveAuthenticate;
    }

    public boolean isReAuthenticate() {
        return reAuthenticate;
    }

    public void setReAuthenticate(boolean reAuthenticate) {
        this.reAuthenticate = reAuthenticate;
    }

    public String getContextIdIncludedQueryParams() {
        return contextIdIncludedQueryParams;
    }

    public void setContextIdIncludedQueryParams(String contextIdIncludedQueryParams) {
        this.contextIdIncludedQueryParams = contextIdIncludedQueryParams;
    }

    public boolean isReturning() {
        return returning;
    }

    public void setReturning(boolean returning) {
        this.returning = returning;
    }

    public Map<String, AuthenticatedIdPData> getCurrentAuthenticatedIdPs() {
        return currentAuthenticatedIdPs;
    }

    public void setCurrentAuthenticatedIdPs(Map<String, AuthenticatedIdPData> currentAuthenticatedIdPs) {
        this.currentAuthenticatedIdPs = currentAuthenticatedIdPs;
    }

    public Map<String, AuthenticatedIdPData> getPreviousAuthenticatedIdPs() {
        return previousAuthenticatedIdPs;
    }

    public void setPreviousAuthenticatedIdPs(Map<String, AuthenticatedIdPData> previousAuthenticatedIdPs) {
        this.previousAuthenticatedIdPs = previousAuthenticatedIdPs;
    }

    public boolean isRetrying() {
        return retrying;
    }

    public void setRetrying(boolean retrying) {
        this.retrying = retrying;
    }

    public String getCurrentAuthenticator() {
        return currentAuthenticator;
    }

    public void setCurrentAuthenticator(String currentAuthenticator) {
        this.currentAuthenticator = currentAuthenticator;
    }

    public boolean isPreviousSessionFound() {
        return previousSessionFound;
    }

    public void setPreviousSessionFound(boolean previousSessionFound) {
        this.previousSessionFound = previousSessionFound;
    }

    public String getRelyingParty() {
        return relyingParty;
    }

    public void setRelyingParty(String relyingParty) {
        this.relyingParty = relyingParty;
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return authenticationRequest;
    }

    public void setAuthenticationRequest(AuthenticationRequest authenticationRequest) {
        this.authenticationRequest = authenticationRequest;
    }
}
