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

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultStepHandler implements StepHandler {

    private static final Log log = LogFactory.getLog(DefaultStepHandler.class);
    private static volatile DefaultStepHandler instance;

    private static final String FAILED_USERNAME = "&failedUsername=";
    private static final String ERROR_CODE = "&errorCode=";
    private static final String AUTHENTICATORS = "&authenticators=";
    private static final String UTF_8 = "UTF-8";

    public static DefaultStepHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultStepHandler.class) {
                if (instance == null) {
                    instance = new DefaultStepHandler();
                }
            }
        }

        return instance;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        StepConfig stepConfig = context.getSequenceConfig().getStepMap()
                .get(context.getCurrentStep());

        List<AuthenticatorConfig> authConfigList = stepConfig.getAuthenticatorList();

        String authenticatorNames = FrameworkUtils.getAuthenticatorIdPMappingString(authConfigList);

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

        String fidp = request.getParameter(FrameworkConstants.RequestParams.FEDERATED_IDP);

        Map<String, AuthenticatedIdPData> authenticatedIdPs = context.getPreviousAuthenticatedIdPs();

        Map<String, AuthenticatorConfig> authenticatedStepIdps = FrameworkUtils
                .getAuthenticatedStepIdPs(stepConfig, authenticatedIdPs);

        // check passive authentication
        if (context.isPassiveAuthenticate()) {

            if (authenticatedStepIdps.isEmpty()) {
                context.setRequestAuthenticated(false);
            } else {
                String authenticatedIdP = authenticatedStepIdps.entrySet().iterator().next().getKey();
                AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(authenticatedIdP);
                populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData);
            }

            stepConfig.setCompleted(true);
            return;
        } else {
            long authTime = 0;
            String max_age = request.getParameter(FrameworkConstants.RequestParams.MAX_AGE);
            if (StringUtils.isNotBlank(max_age) && StringUtils.isNotBlank(context.getSessionIdentifier())) {
                long maxAge = Long.parseLong((max_age));
                if (FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier())
                        .getProperty(FrameworkConstants.UPDATED_TIMESTAMP) != null) {
                    authTime = Long.parseLong(FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier())
                            .getProperty(FrameworkConstants.UPDATED_TIMESTAMP).toString());
                } else {
                    authTime = Long.parseLong(FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier())
                            .getProperty(FrameworkConstants.CREATED_TIMESTAMP).toString());
                }
                long current_time = System.currentTimeMillis();
                if (maxAge < (current_time - authTime) / 1000) {
                    context.setForceAuthenticate(true);
                } else {
                    context.setPreviousAuthTime(true);
                }
            }
        }

        // if Request has fidp param and if this is the first step
        if (fidp != null && stepConfig.getOrder() == 1) {
            handleHomeRealmDiscovery(request, response, context);
            return;
        } else if (context.isReturning()) {
            // if this is a request from the multi-option page
            if (request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR) != null
                    && !request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR)
                    .isEmpty()) {
                handleRequestFromLoginPage(request, response, context);
                return;
            } else {
                // if this is a response from external parties (e.g. federated IdPs)
                handleResponse(request, response, context);
                return;
            }
        }
        // if dumbMode
        else if (ConfigurationFacade.getInstance().isDumbMode()) {

            if (log.isDebugEnabled()) {
                log.debug("Executing in Dumb mode");
            }

            try {
                request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
                response.sendRedirect(loginPage
                        + ("?" + context.getContextIdIncludedQueryParams()) + AUTHENTICATORS
                        + URLEncoder.encode(authenticatorNames, UTF_8) + "&hrd=true");
            } catch (IOException e) {
                throw new FrameworkException(e.getMessage(), e);
            }
        } else {

            if (!context.isForceAuthenticate() && !authenticatedStepIdps.isEmpty()) {

                Map.Entry<String, AuthenticatorConfig> entry = authenticatedStepIdps.entrySet()
                        .iterator().next();
                String idp = entry.getKey();
                AuthenticatorConfig authenticatorConfig = entry.getValue();

                if (context.isReAuthenticate()) {

                    if (log.isDebugEnabled()) {
                        log.debug("Re-authenticating with " + idp + " IdP");
                    }

                    try {
                        context.setExternalIdP(ConfigurationFacade.getInstance().getIdPConfigByName(
                                idp, context.getTenantDomain()));
                    } catch (IdentityProviderManagementException e) {
                        log.error("Exception while getting IdP by name", e);
                    }
                    doAuthentication(request, response, context, authenticatorConfig);
                    return;
                } else {

                    if (log.isDebugEnabled()) {
                        log.debug("Already authenticated. Skipping the step");
                    }

                    // skip the step if this is a normal request
                    AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(idp);
                    populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData);
                    stepConfig.setCompleted(true);
                    return;
                }
            } else {
                // Find if step contains only a single authenticator with a single
                // IdP. If yes, don't send to the multi-option page. Call directly.
                boolean sendToPage = false;
                AuthenticatorConfig authenticatorConfig = null;

                // Are there multiple authenticators?
                if (authConfigList.size() > 1) {
                    sendToPage = true;
                } else {
                    // Are there multiple IdPs in the single authenticator?
                    authenticatorConfig = authConfigList.get(0);
                    if (authenticatorConfig.getIdpNames().size() > 1) {
                        sendToPage = true;
                    }
                }

                if (!sendToPage) {
                    // call directly
                    if (!authenticatorConfig.getIdpNames().isEmpty()) {

                        if (log.isDebugEnabled()) {
                            log.debug("Step contains only a single IdP. Going to call it directly");
                        }

                        // set the IdP to be called in the context
                        try {
                            context.setExternalIdP(ConfigurationFacade.getInstance()
                                    .getIdPConfigByName(authenticatorConfig.getIdpNames().get(0),
                                            context.getTenantDomain()));
                        } catch (IdentityProviderManagementException e) {
                            log.error("Exception while getting IdP by name", e);
                        }
                    }

                    doAuthentication(request, response, context, authenticatorConfig);
                    return;
                } else {
                    // else send to the multi option page.
                    if (log.isDebugEnabled()) {
                        log.debug("Sending to the Multi Option page");
                    }
                    Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
                    String showAuthFailureReason = null;
                    if (parameterMap != null) {
                        showAuthFailureReason = parameterMap.get(FrameworkConstants.SHOW_AUTHFAILURE_RESON_CONFIG);
                        if (log.isDebugEnabled()) {
                            log.debug("showAuthFailureReason has been set as : " + showAuthFailureReason);
                        }
                    }
                    String retryParam = "";

                    if (stepConfig.isRetrying()) {
                        context.setCurrentAuthenticator(null);
                        retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
                    }

                    try {
                        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus
                                .INCOMPLETE);
                        response.sendRedirect(getRedirectUrl(request, response, context, authenticatorNames,
                                showAuthFailureReason, retryParam, loginPage));
                    } catch (IOException e) {
                        throw new FrameworkException(e.getMessage(), e);
                    }

                    return;
                }
            }
        }
    }

    protected void handleHomeRealmDiscovery(HttpServletRequest request,
                                            HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Request contains fidp parameter. Initiating Home Realm Discovery");
        }

        String domain = request.getParameter(FrameworkConstants.RequestParams.FEDERATED_IDP);

        if (log.isDebugEnabled()) {
            log.debug("Received domain: " + domain);
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        StepConfig stepConfig = sequenceConfig.getStepMap().get(context.getCurrentStep());
        List<AuthenticatorConfig> authConfigList = stepConfig.getAuthenticatorList();


        String authenticatorNames = FrameworkUtils.getAuthenticatorIdPMappingString(authConfigList);
        String redirectURL = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

        if (domain.trim().length() == 0) {
            //SP hasn't specified a domain. We assume it wants to get the domain from the user
            try {
                request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
                response.sendRedirect(redirectURL
                        + ("?" + context.getContextIdIncludedQueryParams()) + AUTHENTICATORS
                        + URLEncoder.encode(authenticatorNames, UTF_8) + "&hrd=true");
            } catch (IOException e) {
                throw new FrameworkException(e.getMessage(), e);
            }

            return;
        }
        // call home realm discovery handler to retrieve the realm
        String homeRealm = FrameworkUtils.getHomeRealmDiscoverer().discover(domain);

        if (log.isDebugEnabled()) {
            log.debug("Home realm discovered: " + homeRealm);
        }

        // try to find an IdP with the retrieved realm
        ExternalIdPConfig externalIdPConfig = null;
        try {
            externalIdPConfig = ConfigurationFacade.getInstance()
                    .getIdPConfigByRealm(homeRealm, context.getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            log.error("Exception while getting IdP by realm", e);
        }
        // if an IdP exists
        if (externalIdPConfig != null) {
            String idpName = externalIdPConfig.getIdPName();

            if (log.isDebugEnabled()) {
                log.debug("Found IdP of the realm: " + idpName);
            }

            Map<String, AuthenticatedIdPData> authenticatedIdPs = context.getPreviousAuthenticatedIdPs();
            Map<String, AuthenticatorConfig> authenticatedStepIdps = FrameworkUtils
                    .getAuthenticatedStepIdPs(stepConfig, authenticatedIdPs);

            if (authenticatedStepIdps.containsKey(idpName) && !context.isForceAuthenticate() && !context
                    .isReAuthenticate()) {
                // skip the step if this is a normal request
                AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(idpName);
                populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData);
                stepConfig.setCompleted(true);
                return;
            }

            // try to find an authenticator of the current step, that is mapped to the IdP
            for (AuthenticatorConfig authConfig : authConfigList) {
                // if found
                if (authConfig.getIdpNames().contains(idpName)) {
                    context.setExternalIdP(externalIdPConfig);
                    doAuthentication(request, response, context, authConfig);
                    return;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("An IdP was not found for the sent domain. Sending to the domain page");
        }

        String errorMsg = "domain.unknown";

        try {
            response.sendRedirect(redirectURL + ("?" + context.getContextIdIncludedQueryParams())
                    + AUTHENTICATORS + URLEncoder.encode(authenticatorNames, UTF_8) + "&authFailure=true"
                    + "&authFailureMsg=" + errorMsg + "&hrd=true");
        } catch (IOException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }

    protected void handleRequestFromLoginPage(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Relieved a request from the multi option page");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        int currentStep = context.getCurrentStep();
        StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);

        // if request from the login page with a selected IdP
        String selectedIdp = request.getParameter(FrameworkConstants.RequestParams.IDP);

        if (selectedIdp != null) {

            if (log.isDebugEnabled()) {
                log.debug("User has selected IdP: " + selectedIdp);
            }

            try {
                ExternalIdPConfig externalIdPConfig = ConfigurationFacade.getInstance()
                        .getIdPConfigByName(selectedIdp, context.getTenantDomain());
                // TODO [IMPORTANT] validate the idp is inside the step.
                context.setExternalIdP(externalIdPConfig);
            } catch (IdentityProviderManagementException e) {
                log.error("Exception while getting IdP by name", e);
            }
        }

        for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
            ApplicationAuthenticator authenticator = authenticatorConfig
                    .getApplicationAuthenticator();
            // TODO [IMPORTANT] validate the authenticator is inside the step.
            if (authenticator != null && authenticator.getName().equalsIgnoreCase(
                    request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR))) {
                doAuthentication(request, response, context, authenticatorConfig);
                return;
            }
        }

        // TODO handle idp null

        // TODO handle authenticator name unmatching
    }

    protected void handleResponse(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Receive a response from the external party");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        int currentStep = context.getCurrentStep();
        boolean isNoneCanHandle = true;
        StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);

        for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
            ApplicationAuthenticator authenticator = authenticatorConfig
                    .getApplicationAuthenticator();

            // Call authenticate if canHandle
            if (authenticator != null && authenticator.canHandle(request)
                    && (context.getCurrentAuthenticator() == null || authenticator.getName()
                    .equals(context.getCurrentAuthenticator()))) {
                isNoneCanHandle = false;

                if (log.isDebugEnabled()) {
                    log.debug(authenticator.getName() + " can handle the request.");
                }

                doAuthentication(request, response, context, authenticatorConfig);
                break;
            }
        }
        if (isNoneCanHandle) {
            throw new FrameworkException("No authenticator can handle the request in step :  " + currentStep);
        }
    }

    protected void doAuthentication(HttpServletRequest request, HttpServletResponse response,
                                    AuthenticationContext context, AuthenticatorConfig authenticatorConfig)
            throws FrameworkException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        int currentStep = context.getCurrentStep();
        StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);
        ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

        if (authenticator == null) {
            log.error("Authenticator is null");
            return;
        }

        try {
            context.setAuthenticatorProperties(FrameworkUtils.getAuthenticatorPropertyMapFromIdP(
                    context.getExternalIdP(), authenticator.getName()));
            AuthenticatorFlowStatus status = authenticator.process(request, response, context);
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);

            if (log.isDebugEnabled()) {
                log.debug(authenticator.getName() + " returned: " + status.toString());
            }

            if (status == AuthenticatorFlowStatus.INCOMPLETE) {
                if (log.isDebugEnabled()) {
                    log.debug(authenticator.getName() + " is redirecting");
                }
                return;
            }

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                if (context.getSubject().getUserName() == null) {
                    // Set subject identifier as the default username for federated users
                    String authenticatedSubjectIdentifier = context.getSubject().getAuthenticatedSubjectIdentifier();
                    context.getSubject().setUserName(authenticatedSubjectIdentifier);
                }

                if (context.getSubject().getFederatedIdPName() == null && context.getExternalIdP() != null) {
                    // Setting identity provider's name
                    String idpName = context.getExternalIdP().getIdPName();
                    context.getSubject().setFederatedIdPName(idpName);
                }

                if (context.getSubject().getTenantDomain() == null) {
                    // Setting service provider's tenant domain as the default tenant for federated users
                    String tenantDomain = context.getTenantDomain();
                    context.getSubject().setTenantDomain(tenantDomain);
                }
            }

            AuthenticatedIdPData authenticatedIdPData = new AuthenticatedIdPData();

            // store authenticated user
            AuthenticatedUser authenticatedUser = context.getSubject();
            stepConfig.setAuthenticatedUser(authenticatedUser);
            authenticatedIdPData.setUser(authenticatedUser);

            authenticatorConfig.setAuthenticatorStateInfo(context.getStateInfo());
            stepConfig.setAuthenticatedAutenticator(authenticatorConfig);

            String idpName = FrameworkConstants.LOCAL_IDP_NAME;

            if (context.getExternalIdP() != null) {
                idpName = context.getExternalIdP().getIdPName();
            }

            // store authenticated idp
            stepConfig.setAuthenticatedIdP(idpName);
            authenticatedIdPData.setIdpName(idpName);
            authenticatedIdPData.setAuthenticator(authenticatorConfig);
            //add authenticated idp data to the session wise map
            context.getCurrentAuthenticatedIdPs().put(idpName, authenticatedIdPData);

        } catch (InvalidCredentialsException e) {
            if (log.isDebugEnabled()) {
                log.debug("A login attempt was failed due to invalid credentials", e);
            }
            context.setRequestAuthenticated(false);
        } catch (AuthenticationFailedException e) {
            log.error(e.getMessage(), e);
            context.setRequestAuthenticated(false);
        } catch (LogoutFailedException e) {
            throw new FrameworkException(e.getMessage(), e);
        }

        stepConfig.setCompleted(true);
    }

    protected void populateStepConfigWithAuthenticationDetails(StepConfig stepConfig,
                                                               AuthenticatedIdPData authenticatedIdPData) {

        stepConfig.setAuthenticatedUser(authenticatedIdPData.getUser());
        stepConfig.setAuthenticatedIdP(authenticatedIdPData.getIdpName());
        stepConfig.setAuthenticatedAutenticator(authenticatedIdPData.getAuthenticator());
    }

    private String getRedirectUrl(HttpServletRequest request, HttpServletResponse response, AuthenticationContext
            context, String authenticatorNames, String showAuthFailureReason, String retryParam, String loginPage)
            throws IOException {

        StringBuilder url = new StringBuilder();
        url.append(loginPage);
        url.append("?" + context.getContextIdIncludedQueryParams());
        url.append(AUTHENTICATORS + URLEncoder.encode(authenticatorNames + ":" + FrameworkConstants.LOCAL, UTF_8));
        url.append(retryParam);

        IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
        IdentityUtil.clearIdentityErrorMsg();

        if (errorContext != null) {
            if (log.isDebugEnabled()) {
                log.debug("Identity error message context is not null");
            }
            String errorCode = errorContext.getErrorCode();
            StringBuilder redirectURL = new StringBuilder();


            if (showAuthFailureReason != null && "true".equals(showAuthFailureReason)) {

                int remainingAttempts = errorContext.getMaximumLoginAttempts() - errorContext.getFailedLoginAttempts();

                if (log.isDebugEnabled()) {
                    StringBuilder debugString = new StringBuilder();
                    debugString.append("Error details are as follows.");
                    debugString.append("errorCode : " + errorCode + "\n");
                    debugString.append("username : " + request.getParameter(FrameworkConstants.USERNAME) + "\n");
                    debugString.append("remainingAttempts : " + remainingAttempts);
                    log.debug(debugString.toString());
                }

                redirectURL.append(loginPage);
                redirectURL.append("?" + context.getContextIdIncludedQueryParams());
                redirectURL.append(ERROR_CODE + errorCode);
                redirectURL.append(FAILED_USERNAME + URLEncoder.encode(request.getParameter(FrameworkConstants.USERNAME), UTF_8));

                if (errorCode.equals(UserCoreConstants.ErrorCode.INVALID_CREDENTIAL)) {
                    retryParam = retryParam + "&remainingAttempts=" + remainingAttempts;
                    redirectURL.append(AUTHENTICATORS + authenticatorNames + ":" + FrameworkConstants.LOCAL);
                    redirectURL.append(retryParam);

                    return response.encodeRedirectURL(redirectURL.toString());


                } else if (errorCode.equals(UserCoreConstants.ErrorCode.USER_IS_LOCKED)) {
                    redirectURL.append(AUTHENTICATORS + URLEncoder.encode(authenticatorNames, UTF_8));
                    redirectURL.append(retryParam);
                    if (remainingAttempts == 0) {
                        redirectURL.append("&remainingAttempts=0");
                    }
                    return response.encodeRedirectURL(redirectURL.toString());

                } else if (errorCode.equals(UserCoreConstants.ErrorCode.USER_DOES_NOT_EXIST)) {

                    redirectURL.append(AUTHENTICATORS + authenticatorNames + ":" + FrameworkConstants.LOCAL);
                    redirectURL.append(retryParam);

                    return response.encodeRedirectURL(redirectURL.toString());

                } else if (errorCode.equals(IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE)) {

                    redirectURL.append(AUTHENTICATORS + authenticatorNames + ":" + FrameworkConstants.LOCAL);
                    redirectURL.append(retryParam);
                    return response.encodeRedirectURL(redirectURL.toString());
                }
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Unknown identity error code.");
                }

                if (UserCoreConstants.ErrorCode.USER_IS_LOCKED.equals(errorCode)) {
                    redirectURL.append(loginPage);
                    redirectURL.append("?" + context.getContextIdIncludedQueryParams());
                    redirectURL.append(FAILED_USERNAME + URLEncoder.encode(request.getParameter(FrameworkConstants.USERNAME), UTF_8));
                    redirectURL.append(AUTHENTICATORS + authenticatorNames + ":" + FrameworkConstants.LOCAL);
                    redirectURL.append(retryParam);
                    return response.encodeRedirectURL(redirectURL.toString());

                } else {
                    return url.toString();
                }
            }
        } else {

            if (log.isDebugEnabled()) {
                log.debug("Identity error message context is null");
            }

            return url.toString();
        }
        StringBuilder URL = new StringBuilder();
        URL.append(loginPage);
        URL.append("?" + context.getContextIdIncludedQueryParams());
        URL.append(AUTHENTICATORS + URLEncoder.encode(authenticatorNames, UTF_8));
        URL.append(retryParam);
        return URL.toString();
    }

    private AuthenticatorConfig getAuthenticatorConfig() {
        AuthenticatorConfig authConfig = FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean
                (FrameworkConstants.BASIC_AUTHENTICATOR_CLASS);
        if (authConfig == null) {
            authConfig = new AuthenticatorConfig();
            authConfig.setParameterMap(new HashMap());
        }
        return authConfig;
    }
}
