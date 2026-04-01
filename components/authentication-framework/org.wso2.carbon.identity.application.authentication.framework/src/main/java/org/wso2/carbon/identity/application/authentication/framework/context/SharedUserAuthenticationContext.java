/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.OrganizationLoginData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Decorator for {@link AuthenticationContext} that overrides tenant domain resolution
 * for shared user login scenarios. When the authenticated user is a shared user, this wrapper
 * returns the tenant domain of the user's resident organization with getUserResidentTenantDomain().
 * <p>
 * All other methods are delegated to the wrapped {@link AuthenticationContext} instance.
 */
public class SharedUserAuthenticationContext extends AuthenticationContext {

    private final AuthenticationContext wrappedContext;

    /**
     * Creates a new SharedUserAuthenticationContext decorating the given context.
     *
     * @param wrappedContext the original authentication context to wrap.
     */
    public SharedUserAuthenticationContext(AuthenticationContext wrappedContext) {

        this.wrappedContext = wrappedContext;
    }

    /**
     * Returns the wrapped (original) authentication context.
     *
     * @return the original authentication context.
     */
    public AuthenticationContext getWrappedContext() {

        return wrappedContext;
    }

    /**
     * Returns the tenant domain of the user's resident organization if the authenticated
     * user is a shared user. Otherwise, delegates to the wrapped context in which the return value is equal to
     * getTenantDomain().
     *
     * @return the tenant domain.
     */
    @Override
    public String getUserResidentTenantDomain() {

        Map<Integer, StepConfig> stepMap = wrappedContext.getSequenceConfig().getStepMap();
        for (StepConfig stepConfig : stepMap.values()) {
            if (stepConfig.getAuthenticatedUser() != null && stepConfig.getAuthenticatedUser().isSharedUser() &&
                    FrameworkConstants.SHARED_USER_IDENTIFIER_HANDLER.equals(
                            stepConfig.getAuthenticatedAutenticator().getName())) {
                return stepConfig.getAuthenticatedUser().getTenantDomain();
            }
        }

        return wrappedContext.getUserResidentTenantDomain();
    }

    // Delegated methods from AuthenticationContext.

    @Override
    public String getTenantDomain() {

        return wrappedContext.getTenantDomain();
    }

    @Override
    public String getCallerPath() {

        return wrappedContext.getCallerPath();
    }

    @Override
    public void setCallerPath(String callerPath) {

        wrappedContext.setCallerPath(callerPath);
    }

    @Override
    public String getCallerSessionKey() {

        return wrappedContext.getCallerSessionKey();
    }

    @Override
    public void setCallerSessionKey(String callerSessionKey) {

        wrappedContext.setCallerSessionKey(callerSessionKey);
    }

    @Override
    public String getQueryParams() {

        return wrappedContext.getQueryParams();
    }

    @Override
    public void setQueryParams(String queryParams) {

        wrappedContext.setQueryParams(queryParams);
    }

    @Override
    public void setOrignalRequestQueryParams(String queryParams) {

        wrappedContext.setOrignalRequestQueryParams(queryParams);
    }

    @Override
    public String getRequestType() {

        return wrappedContext.getRequestType();
    }

    @Override
    public void setRequestType(String requestType) {

        wrappedContext.setRequestType(requestType);
    }

    @Override
    public boolean isLogoutRequest() {

        return wrappedContext.isLogoutRequest();
    }

    @Override
    public void setLogoutRequest(boolean isLogoutRequest) {

        wrappedContext.setLogoutRequest(isLogoutRequest);
    }

    @Override
    public int getCurrentStep() {

        return wrappedContext.getCurrentStep();
    }

    @Override
    public void setCurrentStep(int currentStep) {

        wrappedContext.setCurrentStep(currentStep);
    }

    @Override
    public SequenceConfig getSequenceConfig() {

        return wrappedContext.getSequenceConfig();
    }

    @Override
    public void setSequenceConfig(SequenceConfig sequenceConfig) {

        wrappedContext.setSequenceConfig(sequenceConfig);
    }

    @Override
    public AuthenticatedUser getSubject() {

        return wrappedContext.getSubject();
    }

    @Override
    public void setSubject(AuthenticatedUser subject) {

        wrappedContext.setSubject(subject);
    }

    @Override
    public String getContextIdentifier() {

        return wrappedContext.getContextIdentifier();
    }

    @Override
    public void setContextIdentifier(String contextIdentifier) {

        wrappedContext.setContextIdentifier(contextIdentifier);
    }

    @Override
    public boolean isRequestAuthenticated() {

        return wrappedContext.isRequestAuthenticated();
    }

    @Override
    public void setRequestAuthenticated(boolean requestAuthenticated) {

        wrappedContext.setRequestAuthenticated(requestAuthenticated);
    }

    @Override
    public boolean isRememberMe() {

        return wrappedContext.isRememberMe();
    }

    @Override
    public void setRememberMe(boolean rememberMe) {

        wrappedContext.setRememberMe(rememberMe);
    }

    @Override
    public String getSessionIdentifier() {

        return wrappedContext.getSessionIdentifier();
    }

    @Override
    public void setSessionIdentifier(String sessionIdentifier) {

        wrappedContext.setSessionIdentifier(sessionIdentifier);
    }

    @Override
    public Map<String, Object> getProperties() {

        return wrappedContext.getProperties();
    }

    @Override
    public void setProperties(Map<String, Object> properties) {

        wrappedContext.setProperties(properties);
    }

    @Override
    public void setProperty(String key, Object value) {

        wrappedContext.setProperty(key, value);
    }

    @Override
    public void removeProperty(String key) {

        wrappedContext.removeProperty(key);
    }

    @Override
    public Object getProperty(String key) {

        return wrappedContext.getProperty(key);
    }

    @Override
    public ExternalIdPConfig getExternalIdP() {

        return wrappedContext.getExternalIdP();
    }

    @Override
    public void setExternalIdP(ExternalIdPConfig externalIdP) {

        wrappedContext.setExternalIdP(externalIdP);
    }

    @Override
    public void setTenantDomain(String tenantDomain) {

        wrappedContext.setTenantDomain(tenantDomain);
    }

    @Override
    public AuthenticatorStateInfo getStateInfo() {

        return wrappedContext.getStateInfo();
    }

    @Override
    public void setStateInfo(AuthenticatorStateInfo stateInfo) {

        wrappedContext.setStateInfo(stateInfo);
    }

    @Override
    public int getRetryCount() {

        return wrappedContext.getRetryCount();
    }

    @Override
    public void setRetryCount(int retryCount) {

        wrappedContext.setRetryCount(retryCount);
    }

    @Override
    public Map<String, String> getAuthenticatorProperties() {

        return wrappedContext.getAuthenticatorProperties();
    }

    @Override
    public void setAuthenticatorProperties(Map<String, String> authenticatorProperties) {

        wrappedContext.setAuthenticatorProperties(authenticatorProperties);
    }

    @Override
    public String getServiceProviderName() {

        return wrappedContext.getServiceProviderName();
    }

    @Override
    public void setServiceProviderName(String serviceProviderName) {

        wrappedContext.setServiceProviderName(serviceProviderName);
    }

    @Override
    public String getServiceProviderResourceId() {

        return wrappedContext.getServiceProviderResourceId();
    }

    @Override
    public void setServiceProviderResourceId(String serviceProviderResourceId) {

        wrappedContext.setServiceProviderResourceId(serviceProviderResourceId);
    }

    @Override
    public boolean isForceAuthenticate() {

        return wrappedContext.isForceAuthenticate();
    }

    @Override
    public void setForceAuthenticate(boolean forceAuthenticate) {

        wrappedContext.setForceAuthenticate(forceAuthenticate);
    }

    @Override
    public boolean isPassiveAuthenticate() {

        return wrappedContext.isPassiveAuthenticate();
    }

    @Override
    public void setPassiveAuthenticate(boolean passiveAuthenticate) {

        wrappedContext.setPassiveAuthenticate(passiveAuthenticate);
    }

    @Override
    public boolean isReAuthenticate() {

        return wrappedContext.isReAuthenticate();
    }

    @Override
    public void setReAuthenticate(boolean reAuthenticate) {

        wrappedContext.setReAuthenticate(reAuthenticate);
    }

    @Override
    public String getContextIdIncludedQueryParams() {

        return wrappedContext.getContextIdIncludedQueryParams();
    }

    @Override
    public void setContextIdIncludedQueryParams(String contextIdIncludedQueryParams) {

        wrappedContext.setContextIdIncludedQueryParams(contextIdIncludedQueryParams);
    }

    @Override
    public boolean isReturning() {

        return wrappedContext.isReturning();
    }

    @Override
    public void setReturning(boolean returning) {

        wrappedContext.setReturning(returning);
    }

    @Override
    public Map<String, AuthenticatedIdPData> getCurrentAuthenticatedIdPs() {

        return wrappedContext.getCurrentAuthenticatedIdPs();
    }

    @Override
    public void setCurrentAuthenticatedIdPs(Map<String, AuthenticatedIdPData> currentAuthenticatedIdPs) {

        wrappedContext.setCurrentAuthenticatedIdPs(currentAuthenticatedIdPs);
    }

    @Override
    public Map<String, AuthenticatedIdPData> getPreviousAuthenticatedIdPs() {

        return wrappedContext.getPreviousAuthenticatedIdPs();
    }

    @Override
    public void setPreviousAuthenticatedIdPs(Map<String, AuthenticatedIdPData> previousAuthenticatedIdPs) {

        wrappedContext.setPreviousAuthenticatedIdPs(previousAuthenticatedIdPs);
    }

    @Override
    public void setAuthenticatedIdPsOfApp(Map<String, AuthenticatedIdPData> authenticatedIdPsOfApp) {

        wrappedContext.setAuthenticatedIdPsOfApp(authenticatedIdPsOfApp);
    }

    @Override
    public Map<String, AuthenticatedIdPData> getAuthenticatedIdPsOfApp() {

        return wrappedContext.getAuthenticatedIdPsOfApp();
    }

    @Override
    public boolean isRetrying() {

        return wrappedContext.isRetrying();
    }

    @Override
    public void setRetrying(boolean retrying) {

        wrappedContext.setRetrying(retrying);
    }

    @Override
    public String getCurrentAuthenticator() {

        return wrappedContext.getCurrentAuthenticator();
    }

    @Override
    public void setCurrentAuthenticator(String currentAuthenticator) {

        wrappedContext.setCurrentAuthenticator(currentAuthenticator);
    }

    @Override
    public String getRedirectURL() {

        return wrappedContext.getRedirectURL();
    }

    @Override
    public void setRedirectURL(String redirectURL) {

        wrappedContext.setRedirectURL(redirectURL);
    }

    @Override
    public boolean isPreviousSessionFound() {

        return wrappedContext.isPreviousSessionFound();
    }

    @Override
    public void setPreviousSessionFound(boolean previousSessionFound) {

        wrappedContext.setPreviousSessionFound(previousSessionFound);
    }

    @Override
    public String getRelyingParty() {

        return wrappedContext.getRelyingParty();
    }

    @Override
    public void setRelyingParty(String relyingParty) {

        wrappedContext.setRelyingParty(relyingParty);
    }

    @Override
    public AuthenticationRequest getAuthenticationRequest() {

        return wrappedContext.getAuthenticationRequest();
    }

    @Override
    public void setAuthenticationRequest(AuthenticationRequest authenticationRequest) {

        wrappedContext.setAuthenticationRequest(authenticationRequest);
    }

    @Override
    public boolean isPreviousAuthTime() {

        return wrappedContext.isPreviousAuthTime();
    }

    @Override
    public void setPreviousAuthTime(boolean previousAuthTime) {

        wrappedContext.setPreviousAuthTime(previousAuthTime);
    }

    @Override
    public void addAuthenticationStepHistory(AuthHistory history) {

        wrappedContext.addAuthenticationStepHistory(history);
    }

    @Override
    public List<AuthHistory> getAuthenticationStepHistory() {

        return wrappedContext.getAuthenticationStepHistory();
    }

    @Override
    public AcrRule getAcrRule() {

        return wrappedContext.getAcrRule();
    }

    @Override
    public void setAcrRule(AcrRule acrRule) {

        wrappedContext.setAcrRule(acrRule);
    }

    @Override
    public String getSelectedAcr() {

        return wrappedContext.getSelectedAcr();
    }

    @Override
    public void setSelectedAcr(String selectedAcr) {

        wrappedContext.setSelectedAcr(selectedAcr);
    }

    @Override
    public List<String> getRequestedAcr() {

        return wrappedContext.getRequestedAcr();
    }

    @Override
    public void addRequestedAcr(String acr) {

        wrappedContext.addRequestedAcr(acr);
    }

    @Override
    public boolean isSendToMultiOptionPage() {

        return wrappedContext.isSendToMultiOptionPage();
    }

    @Override
    public void setSendToMultiOptionPage(boolean sendToMultiOptionPage) {

        wrappedContext.setSendToMultiOptionPage(sendToMultiOptionPage);
    }

    @Override
    public AuthenticatedUser getLastAuthenticatedUser() {

        return wrappedContext.getLastAuthenticatedUser();
    }

    @Override
    public int getCurrentPostAuthHandlerIndex() {

        return wrappedContext.getCurrentPostAuthHandlerIndex();
    }

    @Override
    public List<String> getExecutedPostAuthHandlers() {

        return wrappedContext.getExecutedPostAuthHandlers();
    }

    @Override
    public void setExecutedPostAuthHandler(String postAuthHandler) {

        wrappedContext.setExecutedPostAuthHandler(postAuthHandler);
    }

    @Override
    public void addAuthenticatorParams(Map<String, Map<String, String>> authenticatorParams) {

        wrappedContext.addAuthenticatorParams(authenticatorParams);
    }

    @Override
    public Map<String, String> getAuthenticatorParams(String authenticatorName) {

        return wrappedContext.getAuthenticatorParams(authenticatorName);
    }

    @Override
    public void addEndpointParam(String key, Serializable value) {

        wrappedContext.addEndpointParam(key, value);
    }

    @Override
    public void addEndpointParams(Map<String, Serializable> params) {

        wrappedContext.addEndpointParams(params);
    }

    @Override
    public Map<String, Serializable> getEndpointParams() {

        return wrappedContext.getEndpointParams();
    }

    @Override
    public boolean isActiveInAThread() {

        return wrappedContext.isActiveInAThread();
    }

    @Override
    public void setActiveInAThread(boolean activeInAThread) {

        wrappedContext.setActiveInAThread(activeInAThread);
    }

    @Override
    public void initializeAnalyticsData() {

        wrappedContext.initializeAnalyticsData();
    }

    @Override
    public void setAnalyticsData(String key, Serializable value) {

        wrappedContext.setAnalyticsData(key, value);
    }

    @Override
    public Serializable getAnalyticsData(String key) {

        return wrappedContext.getAnalyticsData(key);
    }

    @Override
    public String getRuntimeClaim(String claimUri) {

        return wrappedContext.getRuntimeClaim(claimUri);
    }

    @Override
    public void addRuntimeClaim(String claimUri, String claimValue) {

        wrappedContext.addRuntimeClaim(claimUri, claimValue);
    }

    @Override
    public Map<String, String> getRuntimeClaims() {

        return wrappedContext.getRuntimeClaims();
    }

    @Override
    public String getUserTenantDomain() {

        return wrappedContext.getUserTenantDomain();
    }

    @Override
    public void setUserTenantDomainHint(String userTenantDomainHint) {

        wrappedContext.setUserTenantDomainHint(userTenantDomainHint);
    }

    @Override
    public String getLoginTenantDomain() {

        return wrappedContext.getLoginTenantDomain();
    }

    @Override
    public void setLoginTenantDomain(String loginTenantDomain) {

        wrappedContext.setLoginTenantDomain(loginTenantDomain);
    }

    @Override
    public void addLoggedOutAuthenticator(String idpName, String authenticatorName) {

        wrappedContext.addLoggedOutAuthenticator(idpName, authenticatorName);
    }

    @Override
    public boolean isLoggedOutAuthenticator(String idpName, String authenticatorName) {

        return wrappedContext.isLoggedOutAuthenticator(idpName, authenticatorName);
    }

    @Override
    public void clearLoggedOutAuthenticators() {

        wrappedContext.clearLoggedOutAuthenticators();
    }

    @Override
    public void setExternalIdPResourceId(String resourceId) {

        wrappedContext.setExternalIdPResourceId(resourceId);
    }

    @Override
    public String getExternalIdPResourceId() {

        return wrappedContext.getExternalIdPResourceId();
    }

    @Override
    public long getExpiryTime() {

        return wrappedContext.getExpiryTime();
    }

    @Override
    public void setExpiryTime(long expiryTimeNano) {

        wrappedContext.setExpiryTime(expiryTimeNano);
    }

    /**
     * Returns a clone of the wrapped context, not the wrapper itself.
     * This ensures serialization and caching operate on the original context.
     *
     * @return clone of the wrapped authentication context.
     */
    @Override
    public Object clone() {

        return wrappedContext.clone();
    }

    @Override
    public boolean isSharedAppLoginContextUpdateRequired() {

        return wrappedContext.isSharedAppLoginContextUpdateRequired();
    }

    @Override
    public void setSharedAppLoginContextUpdateRequired(boolean sharedAppLoginContextUpdateRequired) {

        wrappedContext.setSharedAppLoginContextUpdateRequired(sharedAppLoginContextUpdateRequired);
    }

    @Override
    public boolean isOrgApplicationLogin() {

        return wrappedContext.isOrgApplicationLogin();
    }

    @Override
    public void setOrgApplicationLogin(boolean orgApplicationLogin) {

        wrappedContext.setOrgApplicationLogin(orgApplicationLogin);
    }

    @Override
    public boolean isSharedAppLogin() {

        return wrappedContext.isSharedAppLogin();
    }

    @Override
    public void setSharedAppLogin(boolean sharedAppLogin) {

        wrappedContext.setSharedAppLogin(sharedAppLogin);
    }

    @Override
    public OrganizationLoginData getOrganizationLoginData() {

        return wrappedContext.getOrganizationLoginData();
    }

    @Override
    public void setOrganizationLoginData(OrganizationLoginData organizationLoginData) {

        wrappedContext.setOrganizationLoginData(organizationLoginData);
    }

    @Override
    public boolean isPasswordResetComplete() {

        return wrappedContext.isPasswordResetComplete();
    }

    @Override
    public void setPasswordResetComplete(boolean passwordResetComplete) {

        wrappedContext.setPasswordResetComplete(passwordResetComplete);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addParameter(Object key, Object value) {

        wrappedContext.addParameter(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addParameters(Map parameters) {

        wrappedContext.addParameters(parameters);
    }

    @Override
    public Map getParameters() {

        return wrappedContext.getParameters();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getParameter(Object key) {

        return wrappedContext.getParameter(key);
    }
}
