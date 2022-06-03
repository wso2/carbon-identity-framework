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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used for holding data about the
 * authentication request sent from a servlet.
 */
public class AuthenticationContext extends MessageContext implements Serializable {

    private static final long serialVersionUID = 6438291349985653402L;

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
    private ExternalIdPConfig externalIdP;
    private boolean rememberMe;
    private String tenantDomain;
    private int retryCount;
    private int currentPostAuthHandlerIndex = 0;
    private Map<String, String> authenticatorProperties = new HashMap<>();
    private String serviceProviderName;
    private String contextIdIncludedQueryParams;
    private String currentAuthenticator;
    private Map<String, Serializable> endpointParams = new HashMap<>();

    private boolean forceAuthenticate;
    private boolean reAuthenticate;
    private boolean passiveAuthenticate;
    private boolean previousAuthTime;
    private AuthenticationRequest authenticationRequest;

    private Map<String, AuthenticatedIdPData> previousAuthenticatedIdPs = new HashMap<>();
    private Map<String, AuthenticatedIdPData> currentAuthenticatedIdPs = new HashMap<>();

    // Authentication context thread status flag.
    private volatile boolean activeInAThread;

    //flow controller flags
    private boolean requestAuthenticated = true;
    private boolean returning;
    private boolean retrying;
    private boolean previousSessionFound;

    //Adaptive Authentication control and status
    private List<AuthHistory> authenticationStepHistory = new ArrayList<>();
    private List<String> requestedAcr;
    private AcrRule acrRule = AcrRule.EXACT;
    private String selectedAcr;
    private Map<String,  AuthenticatedIdPData> authenticatedIdPsOfApp = new HashMap<>();

    /** The user/subject known at the latest authentication step */
    private AuthenticatedUser lastAuthenticatedUser;

    /** subject should be set by each authenticator */
    private AuthenticatedUser subject;

    /* Holds any (state) information that would be required by the authenticator
     * for later processing.
     * E.g. sessionIndex for SAMLSSOAuthenticator in SLO.
     * Each authenticator should have an internal DTO that extends the
     * AuthenticatorStateInfoDTO and set all the required state info in it.
     */
    private AuthenticatorStateInfo stateInfo;

    private String userTenantDomainHint;

    private String loginTenantDomain;

    private List<String> executedPostAuthHandlers = new ArrayList<>();

    private final Map<String, List<String>> loggedOutAuthenticators = new HashMap<>();

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
        if (subject != null) {
            lastAuthenticatedUser = subject;
        }
    }

    public String getContextIdentifier() {
        return contextIdentifier;
    }

    public void setContextIdentifier(String contextIdentifier) {
        this.contextIdentifier = contextIdentifier;
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
        return parameters;
    }

    public void setProperties(Map<String, Object> properties) {
        this.parameters = properties;
    }

    public void setProperty(String key, Object value) {
        parameters.put(key, value);
    }

    public void removeProperty(String key) {
        parameters.remove(key);
    }

    public Object getProperty(String key) {
        return parameters.get(key);
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

    public void setAuthenticatedIdPsOfApp(Map<String, AuthenticatedIdPData> authenticatedIdPsOfApp) {

        this.authenticatedIdPsOfApp = authenticatedIdPsOfApp;
    }

    public Map<String, AuthenticatedIdPData> getAuthenticatedIdPsOfApp() {

        return authenticatedIdPsOfApp;
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

    public boolean isPreviousAuthTime() {
        return previousAuthTime;
    }

    public void setPreviousAuthTime(boolean previousAuthTime) {
        this.previousAuthTime = previousAuthTime;
    }

    public void addAuthenticationStepHistory(AuthHistory history) {
        authenticationStepHistory.add(history);
    }

    public List<AuthHistory> getAuthenticationStepHistory() {
        return Collections.unmodifiableList(authenticationStepHistory);
    }

    public AcrRule getAcrRule() {
        return acrRule;
    }

    public void setAcrRule(AcrRule acrRule) {
        this.acrRule = acrRule;
    }

    public String getSelectedAcr() {
        return selectedAcr;
    }

    public void setSelectedAcr(String selectedAcr) {
        this.selectedAcr = selectedAcr;
    }

    public List<String> getRequestedAcr() {
        if (requestedAcr == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(requestedAcr);
    }

    public void addRequestedAcr(String acr) {
        if (requestedAcr == null) {
            requestedAcr = new ArrayList<>();
        }
        requestedAcr.add(acr);
    }

    /**
     * Returns the Authenticated user who is known as at the moment.
     * Use this to get the user details for any multi-factor authenticator which depends on previously known subject.
     *
     * @return AuthenticatedUser which is assigned to the context last. Null if no previous step could find a user.
     */
    public AuthenticatedUser getLastAuthenticatedUser() {
        return lastAuthenticatedUser;
    }

    /**
     * Returns current post authentication handler index which is in execution.
     *
     * @return Post handler index which is currently in execution.
     */
    public int getCurrentPostAuthHandlerIndex() {

        return currentPostAuthHandlerIndex;
    }

    /**
     * List of post authentication handlers already executed.
     * @return List of post authentication handlers already executed.
     */
    public List<String> getExecutedPostAuthHandlers() {

        return executedPostAuthHandlers;
    }

    /**
     * Sets a post authentication handler.
     * @param postAuthHandler Post Authentication Handler.
     */
    public void setExecutedPostAuthHandler(String postAuthHandler) {

        this.executedPostAuthHandlers.add(postAuthHandler);
        currentPostAuthHandlerIndex++;
    }

    /**
     * Add authentication params to the message context parameters Map.
     *
     * @param authenticatorParams Map of authenticator and params.
     */
    public void addAuthenticatorParams(Map<String, Map<String, String>> authenticatorParams) {

        if (MapUtils.isEmpty(authenticatorParams)) {
            return;
        }
        Object runtimeParamsObj = getParameter(FrameworkConstants.RUNTIME_PARAMS);
        if (runtimeParamsObj == null) {
            addParameter(FrameworkConstants.RUNTIME_PARAMS, authenticatorParams);
            return;
        }
        if (runtimeParamsObj instanceof Map) {
            Map<String, Map<String, String>> runtimeParams = (Map<String, Map<String, String>>) runtimeParamsObj;
            for (Map.Entry<String, Map<String, String>> params : authenticatorParams.entrySet()) {
                if (runtimeParams.get(params.getKey()) != null) {
                    runtimeParams.get(params.getKey()).putAll(params.getValue());
                } else {
                    runtimeParams.put(params.getKey(), params.getValue());
                }
            }
        } else {
            throw IdentityRuntimeException.error("There is already a object set with RUNTIME_PARAMS key in the " +
                    "message context.");
        }
    }

    /**
     * Get parameter map for a specific authenticator
     *
     * @param authenticatorName Authenticator name
     * @return Parameter map
     */
    public Map<String, String> getAuthenticatorParams(String authenticatorName) {

        Object parameter = getParameter(FrameworkConstants.RUNTIME_PARAMS);
        if (parameters != null && (parameter instanceof Map)) {
            Map<String, Map<String, String>> runtimeParams = (Map<String, Map<String, String>>) parameter;
            Map<String, String> authenticatorParams = runtimeParams.get(authenticatorName);
            if (MapUtils.isNotEmpty(authenticatorParams)) {
                return authenticatorParams;
            }
        }
        return Collections.emptyMap();
    }

    /**
     * Add an API parameter to the context. This can be used to pass sensitive values to the endpoints, without
     * sending them as query parameters.
     * @param key parameter key
     * @param value parameter value
     */
    public void addEndpointParam(String key, Serializable value) {

        endpointParams.put(key, value);
    }

    /**
     * Similar to {@link #addEndpointParam(String, Serializable)}. Provide the ability to add multiple parameters
     * at once.
     * @param params Map of parameters to add
     */
    public void addEndpointParams(Map<String, Serializable> params) {

        endpointParams.putAll(params);
    }

    /**
     * Get the endpoint parameters in the context. Refer {@link #addEndpointParam(String, Serializable)}
     * for more details.
     * @return
     */
    public Map<String, Serializable> getEndpointParams() {

        return endpointParams;
    }

    /**
     * Checks whether this context is in use in a active flow.
     * @return True if this context is being used by an active thread.
     */
    public boolean isActiveInAThread() {
        return activeInAThread;
    }

    /**
     * This flag is used to mark when this authentication context is used by an active thread. This is to prevent same
     * context is used by two different threads at the same time.
     *
     * @param activeInAThread True when this context started to being used by a thread.
     */
    public void setActiveInAThread(boolean activeInAThread) {

        this.activeInAThread = activeInAThread;
    }

    /**
     * Initialize the authentication time related parameter maps so that in later we don't need to
     * check whether it is initialized.
     */
    public void initializeAnalyticsData() {

        Map<String, Serializable> analyticsData = new HashMap<>();
        this.addParameter(FrameworkConstants.AnalyticsData.DATA_MAP, analyticsData);
        this.setAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_START_TIME,
                System.currentTimeMillis());
    }

    /**
     * Set analytics related params for Authentication.
     *
     * @param value the authentication related param.
     */
    public void setAnalyticsData(String key, Serializable value) {

        Map<String, Serializable> analyticsData = (HashMap<String, Serializable>)
                this.getParameter(FrameworkConstants.AnalyticsData.DATA_MAP);
        analyticsData.put(key, value);
    }

    /**
     * Get analytics related params for Authentication.
     *
     * @return the analytics related params.
     */
    public Serializable getAnalyticsData(String key) {

        if (this.getParameters().containsKey(FrameworkConstants.AnalyticsData.DATA_MAP)) {
            Map<String, Serializable> analyticsData =
                    (HashMap<String, Serializable>) this.getParameter(FrameworkConstants.AnalyticsData.DATA_MAP);
            return analyticsData.get(key);
        } else {
            return null;
        }
    }

    /**
     * The Runtime claims in the the context.
     *
     * @param claimUri  Claim URI
     * @return Claim value
     */
    public String getRuntimeClaim(String claimUri) {

        Object parameter = getProperty(FrameworkConstants.RUNTIME_CLAIMS);
        if (parameter instanceof Map) {
            Map<String, String> tempClaims = (Map<String, String>) parameter;
            return tempClaims.get(claimUri);
        }
        return null;
    }

    /**
     * Set Runtime claims to the context.  In the the claim handler the priority will be given to these values.
     *
     * @param claimUri  Claim URI
     * @param claimValue    Claim value
     */
    public void addRuntimeClaim(String claimUri, String claimValue) {

        Object parameter = getProperty(FrameworkConstants.RUNTIME_CLAIMS);
        if (parameter instanceof Map) {
            Map<String, String> tempClaims = (Map<String, String>) parameter;
            tempClaims.put(claimUri, claimValue);
        } else {
            Map<String, String> tempClaims = new HashMap<>();
            tempClaims.put(claimUri, claimValue);
            setProperty(FrameworkConstants.RUNTIME_CLAIMS, tempClaims);
        }
    }

    /**
     * Get the Runtime claims map.
     *
     * @return Map of Claim URI and value
     */
    public Map<String, String> getRuntimeClaims() {

        Object parameter = getProperty(FrameworkConstants.RUNTIME_CLAIMS);
        if (parameter instanceof Map) {
            return (Map<String, String>) parameter;
        }
        return Collections.emptyMap();
    }

    /**
     * Retrieves the potential tenant domain of the user who is going to login. This will return the first non-empty
     * value of userTenantDomainHint, loginTenantDomain or tenant domain in the respective order.
     *
     * This will be used to populate the FQN of the user (if user/client didn't provide explicitly) when logging in.
     * This should ideally be the tenant domain user is going to log into (the one where the session will be created)
     * , but may be overridden for any special call applications with the domain hint.
     *
     * @return The most possible tenant domain of the user who will be logging in
     */
    public String getUserTenantDomain() {

        if (!IdentityTenantUtil.isTenantedSessionsEnabled()) {
            return tenantDomain;
        }
        if (StringUtils.isNotBlank(userTenantDomainHint)) {
            return userTenantDomainHint;
        }
        if (StringUtils.isNotBlank(loginTenantDomain)) {
            return loginTenantDomain;
        }
        return tenantDomain;
    }

    /**
     * Set the user's tenant domain hint. Should only be used if different from the tenant domain where the session
     * would be created
     *
     * @param userTenantDomainHint The possible tenant domain of the user
     */
    public void setUserTenantDomainHint(String userTenantDomainHint) {

        this.userTenantDomainHint = userTenantDomainHint;
    }

    /**
     * Gets the tenant domain to which the user should get logged into and the session should get created. For a
     * non-saas application this should be the user's and application's tenant domain. For a saas application, this
     * will be the user's tenant domain for most cases.
     *
     * @return the tenant domain the user's session should be created
     */
    public String getLoginTenantDomain() {

        if (!IdentityTenantUtil.isTenantedSessionsEnabled()) {
            return tenantDomain;
        }
        if (StringUtils.isNotBlank(loginTenantDomain)) {
            return loginTenantDomain;
        }
        return tenantDomain;
    }

    /**
     * Sets the tenant domain where the user's session should be created
     *
     * @param loginTenantDomain the tenant domain where the user's session is created
     */
    public void setLoginTenantDomain(String loginTenantDomain) {

        this.loginTenantDomain = loginTenantDomain;
    }

    /**
     * Add a logged out authenticator providing the IDP name. Method creates a new list and appends the
     * authenticator if no entry for the IDP.
     *
     * @param idpName Identity provider name.
     * @param  authenticatorName Authenticator name.
     */
    public void addLoggedOutAuthenticator(String idpName, String authenticatorName) {

        if (loggedOutAuthenticators.containsKey(idpName)) {
            loggedOutAuthenticators.get(idpName).add(authenticatorName);
        } else {
            List<String> authenticators = new ArrayList<>();
            authenticators.add(authenticatorName);
            loggedOutAuthenticators.put(idpName, authenticators);
        }
    }

    /**
     * Check whether the authenticator is already logged out.
     *
     * @param idpName Identity provider name.
     * @param authenticatorName Authenticator name.
     * @return true if the authenticator already logged out. False otherwise.
     */
    public boolean isLoggedOutAuthenticator(String idpName, String authenticatorName) {

        if (loggedOutAuthenticators.containsKey(idpName)) {
            return loggedOutAuthenticators.get(idpName).contains(authenticatorName);
        }
        return false;
    }

    /**
     * Clears all currently logged out authenticators from the context.
     */
    public void clearLoggedOutAuthenticators() {

        loggedOutAuthenticators.clear();
    }
}
