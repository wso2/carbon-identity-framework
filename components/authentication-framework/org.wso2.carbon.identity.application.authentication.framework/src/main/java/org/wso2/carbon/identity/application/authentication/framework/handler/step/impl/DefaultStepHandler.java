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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationFlowHandler;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.IdentityErrorMsgContext;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreClientException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.BASIC_AUTH_MECHANISM;
import static org.wso2.carbon.identity.base.IdentityConstants.FEDERATED_IDP_SESSION_ID;
import static org.wso2.carbon.utils.CarbonUtils.isLegacyAuditLogsDisabled;

/**
 * Default implementation of the authentication step handler.
 */
public class DefaultStepHandler implements StepHandler {

    private static final Log LOG = LogFactory.getLog(DefaultStepHandler.class);
    private static volatile DefaultStepHandler instance;
    private static final String RE_CAPTCHA_USER_DOMAIN = "user-domain-recaptcha";
    Log audit = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result "
            + ": %s ";
    private static final String SUCCESS = "Success";
    private static final String FAILURE = "Failure";

    private static final String USERNAME = "username";

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

        if (context.getAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME) == null) {
            context.setAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME,
                    System.currentTimeMillis());
        }

        StepConfig stepConfig = context.getSequenceConfig().getStepMap()
                .get(context.getCurrentStep());

        List<AuthenticatorConfig> authConfigList = stepConfig.getAuthenticatorList();

        String authenticatorNames = FrameworkUtils.getAuthenticatorIdPMappingString(authConfigList);

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();

        String fidp = request.getParameter(FrameworkConstants.RequestParams.FEDERATED_IDP);

        Map<String, AuthenticatedIdPData> authenticatedIdPs = context.getCurrentAuthenticatedIdPs();

        // If there are no current authenticated IDPs, it means no authentication has been taken place yet.
        // So see whether there are previously authenticated IDPs for this session.
        // NOTE : currentAuthenticatedIdPs (if not null) always contains the previousAuthenticatedIdPs
        if (MapUtils.isEmpty(authenticatedIdPs)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No current authenticated IDPs in the authentication context. " +
                        "Continuing with the previous authenticated IDPs");
            }
            authenticatedIdPs = context.getPreviousAuthenticatedIdPs();
        }

        if (LOG.isDebugEnabled()) {
            if (MapUtils.isEmpty(authenticatedIdPs)) {
                LOG.debug("No previous authenticated IDPs found in the authentication context.");
            } else {
                LOG.debug(String.format("Found authenticated IdPs. Count: %d", authenticatedIdPs.size()));
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, context.getServiceProviderName());
                    params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain());
                    params.put(FrameworkConstants.LogConstants.COUNT, authenticatedIdPs.size());
                    Map<String, Object> idpMap = new HashMap<>();
                    authenticatedIdPs.forEach((key, value) -> idpMap.put(key, value.getAuthenticators().stream().map(
                            AuthenticatorConfig::getName).collect(Collectors.toList())));
                    params.put(FrameworkConstants.LogConstants.AUTHENTICATED_IDPS, idpMap);

                    LoggerUtils.triggerDiagnosticLogEvent(
                            FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params, LogConstants.SUCCESS,
                            "Authenticated IDPs found", FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST,
                            null);
                }
            }
        }

        if (context.isPassiveAuthenticate() && MapUtils.isNotEmpty(context.getAuthenticatedIdPsOfApp())) {
            authenticatedIdPs = context.getAuthenticatedIdPsOfApp();
        }
        Map<String, AuthenticatorConfig> authenticatedStepIdps = FrameworkUtils
                .getAuthenticatedStepIdPs(stepConfig, authenticatedIdPs);

        // check passive authentication
        if (context.isPassiveAuthenticate()) {
            if (authenticatedStepIdps.isEmpty()) {
                context.setRequestAuthenticated(false);
            } else {
                String authenticatedIdP = authenticatedStepIdps.entrySet().iterator().next().getKey();
                AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(authenticatedIdP);
                populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData, authenticatedStepIdps
                        .get(authenticatedIdP));
                request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus
                        .SUCCESS_COMPLETED);
            }

            stepConfig.setCompleted(true);
            return;
        } else {
            long authTime = 0;
            String maxAgeParam = request.getParameter(FrameworkConstants.RequestParams.MAX_AGE);
            if (StringUtils.isNotBlank(maxAgeParam) && StringUtils.isNotBlank(context.getSessionIdentifier())) {
                String loginTenantDomain = context.getLoginTenantDomain();
                long maxAge = Long.parseLong((maxAgeParam));
                if (FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier(), loginTenantDomain)
                        .getProperty(FrameworkConstants.UPDATED_TIMESTAMP) != null) {
                    authTime = Long.parseLong(FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier(),
                            loginTenantDomain).getProperty(FrameworkConstants.UPDATED_TIMESTAMP).toString());
                } else {
                    authTime = Long.parseLong(FrameworkUtils.getSessionContextFromCache(context.getSessionIdentifier(),
                            loginTenantDomain).getProperty(FrameworkConstants.CREATED_TIMESTAMP).toString());
                }
                long currentTime = System.currentTimeMillis();
                if (maxAge < (currentTime - authTime) / 1000) {
                    context.setForceAuthenticate(true);
                } else {
                    context.setPreviousAuthTime(true);
                }
            }
        }

        if (request.getParameter(FrameworkConstants.RequestParams.USER_ABORT) != null
                && Boolean.parseBoolean(request.getParameter(FrameworkConstants.RequestParams.USER_ABORT))) {
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus
                    .USER_ABORT);
            stepConfig.setCompleted(true);
            return;
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
        } else if (ConfigurationFacade.getInstance().isDumbMode() && authenticatedIdPs.isEmpty()) {
            // If dumbMode is enabled and no previous authenticated IDPs exist we redirect for Home Realm Discovery.

            if (LOG.isDebugEnabled()) {
                LOG.debug("Executing in Dumb mode");
            }

            try {
                request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
                response.sendRedirect(loginPage
                        + ("?" + context.getContextIdIncludedQueryParams()) + "&authenticators="
                        + URLEncoder.encode(authenticatorNames, "UTF-8") + "&hrd=true");
            } catch (IOException e) {
                throw new FrameworkException(e.getMessage(), e);
            }
        } else {

            if (!(context.isForceAuthenticate() || stepConfig.isForced()) && !authenticatedStepIdps.isEmpty()) {

                Map.Entry<String, AuthenticatorConfig> entry = authenticatedStepIdps.entrySet()
                        .iterator().next();
                String idp = entry.getKey();
                AuthenticatorConfig authenticatorConfig = entry.getValue();

                if (context.isReAuthenticate()) {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Re-authenticating with " + idp + " IdP");
                    }

                    try {
                        context.setExternalIdP(ConfigurationFacade.getInstance().getIdPConfigByName(
                                idp, context.getTenantDomain()));
                    } catch (IdentityProviderManagementException e) {
                        LOG.error("Exception while getting IdP by name", e);
                    }
                    doAuthentication(request, response, context, authenticatorConfig);
                    return;
                } else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Already authenticated. Skipping the step");
                    }
                    if (LoggerUtils.isDiagnosticLogsEnabled()) {
                        Map<String, Object> params = new HashMap<>();
                        params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, context.getServiceProviderName());
                        params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain());
                        params.put(FrameworkConstants.LogConstants.STEP, stepConfig.getOrder());
                        Map<String, Object> authenticatedStepIdpMap = new HashMap<>();
                        authenticatedStepIdps.forEach((key, value) ->
                                    Optional.ofNullable(value.getApplicationAuthenticator())
                                            .ifPresent(authenticator ->
                                                    authenticatedStepIdpMap.put(key, authenticator.getName()))
                                );
                        params.put(FrameworkConstants.LogConstants.AUTHENTICATED_IDPS, authenticatedStepIdpMap);
                        LoggerUtils.triggerDiagnosticLogEvent(
                                FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params, LogConstants.SUCCESS,
                                "Already authenticated. Skipping the step",
                                FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST, null);
                    }

                    // skip the step if this is a normal request
                    AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(idp);
                    populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData,
                            authenticatedStepIdps.get(idp));
                    context.getCurrentAuthenticatedIdPs().put(idp, authenticatedIdPData);
                    stepConfig.setCompleted(true);
                    request.setAttribute(
                            FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.SUCCESS_COMPLETED);
                    return;
                }
            } else {
                // Find if step contains only a single authenticator with a single
                // IdP. If yes, don't send to the multi-option page. Call directly.
                boolean sendToPage = false;
                boolean isAuthFlowHandlerOrBasicAuthInMultiOptionStep = false;
                AuthenticatorConfig authenticatorConfig = null;

                // Are there multiple authenticators?
                if (authConfigList.size() > 1) {
                    sendToPage = true;
                    // To identify whether multiple authentication options are available along with an authentication
                    // flow handler or basic authenticator. If available, doAuthentication will be executed before
                    // redirecting to the multi option page.
                    for (AuthenticatorConfig config : authConfigList) {
                        if ((config.getApplicationAuthenticator() instanceof AuthenticationFlowHandler) ||
                                (config.getApplicationAuthenticator() instanceof LocalApplicationAuthenticator &&
                                        (BASIC_AUTH_MECHANISM).equalsIgnoreCase(config.getApplicationAuthenticator()
                                                .getAuthMechanism()) && IdentityUtil.getIdentityErrorMsg() == null)) {
                            authenticatorConfig = config;
                            isAuthFlowHandlerOrBasicAuthInMultiOptionStep = true;
                            sendToPage = false;
                            break;
                        }
                    }
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

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Step contains only a single IdP. Going to call it directly");
                        }

                        // set the IdP to be called in the context
                        try {
                            context.setExternalIdP(ConfigurationFacade.getInstance()
                                                           .getIdPConfigByName(authenticatorConfig.getIdpNames().get(0),
                                                                               context.getTenantDomain()));
                        } catch (IdentityProviderManagementException e) {
                            LOG.error("Exception while getting IdP by name", e);
                        }
                    }

                    doAuthentication(request, response, context, authenticatorConfig);
                    /* If an authentication flow handler is redirected with incomplete status,
                    it will redirect to multi option page, as multi-option is available */
                    if ((request.getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS)) ==
                            AuthenticatorFlowStatus.INCOMPLETE && isAuthFlowHandlerOrBasicAuthInMultiOptionStep) {
                        sendToMultiOptionPage(stepConfig, request, context, response, authenticatorNames);
                    }
                    return;
                } else {
                    // else send to the multi option page.
                    sendToMultiOptionPage(stepConfig, request, context, response, authenticatorNames);
                    return;
                }
            }
        }
    }

    private void sendToMultiOptionPage(StepConfig stepConfig, HttpServletRequest request, AuthenticationContext context,
                                       HttpServletResponse response, String authenticatorNames)
            throws FrameworkException {

        String loginPage = ConfigurationFacade.getInstance().getAuthenticationEndpointURL();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending to the Multi Option page");
        }
        Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
        String showAuthFailureReason = null;
        if (MapUtils.isNotEmpty(parameterMap)) {
            showAuthFailureReason = parameterMap.get(FrameworkConstants.SHOW_AUTHFAILURE_RESON_CONFIG);
            if (LOG.isDebugEnabled()) {
                LOG.debug("showAuthFailureReason has been set as : " + showAuthFailureReason);
            }
        }
        String retryParam = StringUtils.EMPTY;

        if (stepConfig.isRetrying()) {
            context.setCurrentAuthenticator(null);
            retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
        }

        try {
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus
                    .INCOMPLETE);
            response.sendRedirect(getRedirectUrl(request, response, context, authenticatorNames,
                    showAuthFailureReason, retryParam, loginPage));
        } catch (IOException | URISyntaxException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }

    protected void handleHomeRealmDiscovery(HttpServletRequest request,
                                            HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request contains fidp parameter. Initiating Home Realm Discovery");
        }

        String domain = request.getParameter(FrameworkConstants.RequestParams.FEDERATED_IDP);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Received domain: " + domain);
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
                        + ("?" + context.getContextIdIncludedQueryParams()) + "&authenticators="
                        + URLEncoder.encode(authenticatorNames, "UTF-8") + "&hrd=true");
            } catch (IOException e) {
                throw new FrameworkException(e.getMessage(), e);
            }

            return;
        }
        // call home realm discovery handler to retrieve the realm
        String homeRealm = FrameworkUtils.getHomeRealmDiscoverer().discover(domain);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Home realm discovered: " + homeRealm);
        }

        // try to find an IdP with the retrieved realm
        ExternalIdPConfig externalIdPConfig = null;
        try {
             externalIdPConfig = ConfigurationFacade.getInstance()
                .getIdPConfigByRealm(homeRealm, context.getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            LOG.error("Exception while getting IdP by realm", e);
        }
        // if an IdP exists
        if (externalIdPConfig != null) {
            String idpName = externalIdPConfig.getIdPName();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Found IdP of the realm: " + idpName);
            }

            Map<String, AuthenticatedIdPData> authenticatedIdPs = context.getPreviousAuthenticatedIdPs();
            Map<String, AuthenticatorConfig> authenticatedStepIdps = FrameworkUtils
                    .getAuthenticatedStepIdPs(stepConfig, authenticatedIdPs);

            if (authenticatedStepIdps.containsKey(idpName)
                    && !(context.isForceAuthenticate() || stepConfig.isForced())
                    && !context.isReAuthenticate()) {
                // skip the step if this is a normal request
                AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(idpName);
                populateStepConfigWithAuthenticationDetails(stepConfig, authenticatedIdPData, authenticatedStepIdps
                        .get(idpName));
                stepConfig.setCompleted(true);
                //add authenticated idp data to the session wise map
                context.getCurrentAuthenticatedIdPs().put(idpName, authenticatedIdPData);
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

        if (LOG.isDebugEnabled()) {
            LOG.debug("An IdP was not found for the sent domain. Sending to the domain page");
        }

        String errorMsg = "domain.unknown";

        try {
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
            response.sendRedirect(redirectURL + ("?" + context.getContextIdIncludedQueryParams())
                    + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + "&authFailure=true"
                    + "&authFailureMsg=" + errorMsg + "&hrd=true");
        } catch (IOException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }

    protected void handleRequestFromLoginPage(HttpServletRequest request,
                                              HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Relieved a request from the multi option page");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        int currentStep = context.getCurrentStep();
        StepConfig stepConfig = sequenceConfig.getStepMap().get(currentStep);

        // if request from the login page with a selected IdP
        String selectedIdp = request.getParameter(FrameworkConstants.RequestParams.IDP);

        if (selectedIdp != null) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("User has selected IdP: " + selectedIdp);
            }

            try {
                ExternalIdPConfig externalIdPConfig = ConfigurationFacade.getInstance()
                    .getIdPConfigByName(selectedIdp, context.getTenantDomain());
                // TODO [IMPORTANT] validate the idp is inside the step.
                context.setExternalIdP(externalIdPConfig);
            } catch (IdentityProviderManagementException e) {
                LOG.error("Exception while getting IdP by name", e);
            }
        }

        for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
            ApplicationAuthenticator authenticator = authenticatorConfig
                    .getApplicationAuthenticator();
            if (authenticator != null && authenticator.getName().equalsIgnoreCase(
                    request.getParameter(FrameworkConstants.RequestParams.AUTHENTICATOR))) {
                if (StringUtils.isNotBlank(selectedIdp) && authenticatorConfig.getIdps().get(selectedIdp) == null) {
                    // If the selected idp name is not configured for the application, throw error since
                    // this is an invalid case.
                    throw new FrameworkException("Authenticators configured for application and user selected idp " +
                            "does not match. Possible tampering of parameters in login page.");
                }
                doAuthentication(request, response, context, authenticatorConfig);
                return;
            }
        }
    }

    protected void handleResponse(HttpServletRequest request, HttpServletResponse response,
                                  AuthenticationContext context) throws FrameworkException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Receive a response from the external party");
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

                if (LOG.isDebugEnabled()) {
                    LOG.debug(authenticator.getName() + " can handle the request.");
                }
                if (LoggerUtils.isDiagnosticLogsEnabled()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, context.getServiceProviderName());
                    params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain());
                    params.put(FrameworkConstants.LogConstants.AUTHENTICATOR_NAME, authenticator.getName());
                    params.put(FrameworkConstants.LogConstants.STEP, currentStep);
                    LoggerUtils.triggerDiagnosticLogEvent(
                            FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params, LogConstants.SUCCESS,
                            "Initializing authentication flow",
                            FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_STEP, null);
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
            LOG.error("Authenticator is null for AuthenticatorConfig: " + authenticatorConfig.getName());
            return;
        }

        String idpName = FrameworkConstants.LOCAL_IDP_NAME;
        if (context.getExternalIdP() != null && authenticator instanceof FederatedApplicationAuthenticator) {
            idpName = context.getExternalIdP().getIdPName();
        }

        try {
            context.setAuthenticatorProperties(FrameworkUtils.getAuthenticatorPropertyMapFromIdP(
                    context.getExternalIdP(), authenticator.getName()));
            AuthenticatorFlowStatus status = authenticator.process(request, response, context);
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, status);

            if (LOG.isDebugEnabled()) {
                LOG.debug(authenticator.getName() + " returned: " + status.toString());
            }

            if (status == AuthenticatorFlowStatus.INCOMPLETE) {
                context.setCurrentAuthenticator(authenticator.getName());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(authenticator.getName() + " is redirecting");
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
                    context.getSubject().setFederatedIdPName(idpName);
                }

                if (context.getSubject().getTenantDomain() == null) {
                    // Setting service provider's tenant domain as the default tenant for federated users
                    String tenantDomain = context.getTenantDomain();
                    context.getSubject().setTenantDomain(tenantDomain);
                }

                try {
                    // Check if the user id is available for the user. If the user id is not available or cannot be
                    // resolved, UserIdNotFoundException is thrown.
                    String userId = context.getSubject().getUserId();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("User id is available for user: " + userId);
                    }
                } catch (UserIdNotFoundException e) {
                    String tenantDomain = context.getSubject().getTenantDomain();
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    String authenticatedSubjectIdentifier = context.getSubject().getAuthenticatedSubjectIdentifier();
                    String federatedIdPName = context.getSubject().getFederatedIdPName();

                    try {
                        int idpId = UserSessionStore.getInstance().getIdPId(federatedIdPName, tenantId);
                        String userId = UserSessionStore.getInstance()
                                .getFederatedUserId(authenticatedSubjectIdentifier, tenantId, idpId);
                        try {
                            if (userId == null) {
                                userId = UUID.randomUUID().toString();
                                UserSessionStore.getInstance()
                                        .storeUserData(userId, authenticatedSubjectIdentifier, tenantId, idpId);
                            }
                        } catch (DuplicatedAuthUserException e1) {
                            String msg = "User authenticated is already persisted. Username: "
                                    + authenticatedSubjectIdentifier + " Tenant Domain:" + tenantDomain
                                    + " IdP: " + federatedIdPName;
                            LOG.warn(msg);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(msg, e1);
                            }
                            // Since duplicate entry was found, let's try to get the ID again.
                            userId = UserSessionStore.getInstance()
                                    .getFederatedUserId(authenticatedSubjectIdentifier, tenantId, idpId);
                        }
                        context.getSubject().setUserId(userId);
                    } catch (UserSessionException e2) {
                        LOG.error("Error while resolving the user id for federated user.", e2);
                    }
                }
            }

            AuthenticatedIdPData authenticatedIdPData = getAuthenticatedIdPData(context, idpName);

            // store authenticated user
            AuthenticatedUser authenticatedUser = context.getSubject();
            stepConfig.setAuthenticatedUser(authenticatedUser);
            authenticatedIdPData.setUser(authenticatedUser);

            authenticatorConfig.setAuthenticatorStateInfo(context.getStateInfo());
            stepConfig.setAuthenticatedAutenticator(authenticatorConfig);

            // store authenticated idp
            stepConfig.setAuthenticatedIdP(idpName);
            authenticatedIdPData.setIdpName(idpName);
            authenticatedIdPData.addAuthenticator(authenticatorConfig);
            //add authenticated idp data to the session wise map
            context.getCurrentAuthenticatedIdPs().put(idpName, authenticatedIdPData);

            // Add SAML federated idp session index into the authentication step history.
            String idpSessionIndex = null;
            String parameterName = FEDERATED_IDP_SESSION_ID + idpName;
            AuthHistory authHistory = new AuthHistory(authenticator.getName(), idpName);

            if (context.getParameters() != null && context.getParameters().containsKey(parameterName)) {
                Object idpSessionIndexParamValue = context.getParameter(parameterName);
                if (idpSessionIndexParamValue != null) {
                    idpSessionIndex = idpSessionIndexParamValue.toString();
                }
            }
            if (StringUtils.isNotBlank(context.getCurrentAuthenticator()) && StringUtils.isNotBlank(idpSessionIndex)) {
                authHistory.setIdpSessionIndex(idpSessionIndex);
                authHistory.setRequestType(context.getRequestType());
            }
            Serializable startTime =
                    context.getAnalyticsData(FrameworkConstants.AnalyticsData.CURRENT_AUTHENTICATOR_START_TIME);
            if (startTime instanceof Long) {
                authHistory.setDuration((long) startTime - System.currentTimeMillis());
            }
            authHistory.setSuccess(true);
            context.addAuthenticationStepHistory(authHistory);

            String initiator = null;
            if (stepConfig.getAuthenticatedUser() != null) {
                initiator = stepConfig.getAuthenticatedUser().toFullQualifiedUsername();
                if (LoggerUtils.isLogMaskingEnable) {
                    initiator = LoggerUtils.getMaskedContent(initiator);
                }
            }
            String data = "Step: " + stepConfig.getOrder() + ", IDP: " + stepConfig.getAuthenticatedIdP() +
                    ", Authenticator:" + stepConfig.getAuthenticatedAutenticator().getName();
            if (!isLegacyAuditLogsDisabled()) {
                audit.info(String.format(AUDIT_MESSAGE, initiator, "Authenticate", "ApplicationAuthenticationFramework",
                        data, SUCCESS));
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                Map<String, Object> params = new HashMap<>();
                params.put(FrameworkConstants.LogConstants.STEP, stepConfig.getOrder());
                params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, context.getServiceProviderName());
                params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain());
                params.put(FrameworkConstants.LogConstants.IDP, stepConfig.getAuthenticatedIdP());
                params.put(FrameworkConstants.LogConstants.AUTHENTICATOR_NAME,
                        stepConfig.getAuthenticatedAutenticator().getName());
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.SUCCESS, "Authentication success for step: " + stepConfig.getOrder(),
                        FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_STEP, null);
            }
        } catch (InvalidCredentialsException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("A login attempt was failed due to invalid credentials", e);
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                 Map<String, Object> params = getContextParamsForDiagnosticLogs(context, authenticatorConfig,
                        stepConfig);
                Optional.ofNullable(e.getUser()).ifPresent(user -> {
                    Optional.ofNullable(user.toFullQualifiedUsername()).ifPresent(username -> {
                        params.put(FrameworkConstants.LogConstants.USER, LoggerUtils.isLogMaskingEnable ?
                                LoggerUtils.getMaskedContent(username) : username);
                    });
                    params.put(FrameworkConstants.LogConstants.USER_STORE_DOMAIN, user.getUserStoreDomain());
                    params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, user.getTenantDomain());
                });
                params.put(FrameworkConstants.LogConstants.IDP, idpName);
                params.put(FrameworkConstants.LogConstants.AUTHENTICATOR_NAME, authenticatorConfig.getName());
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.FAILED, "Authentication failed: " + e.getMessage(),
                        FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_STEP, null);
            }
            String data = "Step: " + stepConfig.getOrder() + ", IDP: " + idpName + ", Authenticator:" +
                    authenticatorConfig.getName();
            String initiator = null;
            if (e.getUser() != null) {
                initiator = e.getUser().toFullQualifiedUsername();
            } else if (context.getSubject() != null) {
                initiator = context.getSubject().toFullQualifiedUsername();
            }
            if (LoggerUtils.isLogMaskingEnable) {
                initiator = LoggerUtils.getMaskedContent(initiator);
            }
            if (!isLegacyAuditLogsDisabled()) {
                audit.warn(String.format(AUDIT_MESSAGE, initiator, "Authenticate", "ApplicationAuthenticationFramework",
                        data, FAILURE));
            }
            handleFailedAuthentication(request, response, context, authenticatorConfig, e.getUser());
        } catch (AuthenticationFailedException e) {
            IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
            if (errorContext != null) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (!IdentityCoreConstants.ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_ERROR_CODE.
                        equals(errorContext.getErrorCode()) && !(rootCause instanceof UserStoreClientException) &&
                        !IdentityCoreConstants.USER_ACCOUNT_LOCKED_ERROR_CODE.equals(errorContext.getErrorCode()) &&
                        !IdentityCoreConstants.USER_ACCOUNT_DISABLED_ERROR_CODE.equals(errorContext.getErrorCode()) &&
                        !IdentityCoreConstants.USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE
                        .equals(errorContext.getErrorCode())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authentication failed exception!", e);
                    }
                    LOG.error("Authentication failed exception! " + e.getMessage());
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authentication failed exception!", e);
                    }
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Authentication failed exception!", e);
                }
                LOG.error("Authentication failed exception! " + e.getMessage());
            }
            String data = "Step: " + stepConfig.getOrder() + ", IDP: " + idpName + ", Authenticator:" +
                    authenticatorConfig.getName();
            String initiator = null;
            if (e.getUser() != null) {
                initiator = e.getUser().toFullQualifiedUsername();
            } else if (context.getSubject() != null) {
                initiator = context.getSubject().toFullQualifiedUsername();
            }
            if (LoggerUtils.isLogMaskingEnable) {
                initiator = LoggerUtils.getMaskedContent(initiator);
            }
            if (!isLegacyAuditLogsDisabled()) {
                audit.warn(String.format(AUDIT_MESSAGE, initiator,
                        "Authenticate", "ApplicationAuthenticationFramework", data, FAILURE));
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                Map<String, Object> params = getContextParamsForDiagnosticLogs(context, authenticatorConfig,
                        stepConfig);
                LoggerUtils.triggerDiagnosticLogEvent(FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK, params,
                        LogConstants.FAILED, "Authentication failed exception: " + e.getMessage(),
                        FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_STEP, null);
            }
            handleFailedAuthentication(request, response, context, authenticatorConfig, e.getUser());
        } catch (LogoutFailedException e) {
            throw new FrameworkException(e.getMessage(), e);
        }

        stepConfig.setCompleted(true);
    }

    private Map<String, Object> getContextParamsForDiagnosticLogs(AuthenticationContext context,
                                                                  AuthenticatorConfig authenticatorConfig,
                                                                  StepConfig stepConfig) {

        Map<String, Object> params = new HashMap<>();
        params.put(FrameworkConstants.LogConstants.STEP, stepConfig.getOrder());
        params.put(FrameworkConstants.LogConstants.SERVICE_PROVIDER, context.getServiceProviderName());
        params.put(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain());
        params.put(FrameworkConstants.LogConstants.AUTHENTICATOR_NAME, authenticatorConfig.getName());
        return params;
    }

    protected void handleFailedAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationContext context,
                                              AuthenticatorConfig authenticatorConfig,
                                              User user) {
        context.setRequestAuthenticated(false);
        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.FAIL_COMPLETED);
    }

    @Deprecated
    protected void populateStepConfigWithAuthenticationDetails(StepConfig stepConfig,
                                                               AuthenticatedIdPData authenticatedIdPData) {

        stepConfig.setAuthenticatedUser(authenticatedIdPData.getUser());
        stepConfig.setAuthenticatedIdP(authenticatedIdPData.getIdpName());
        stepConfig.setAuthenticatedAutenticator(authenticatedIdPData.getAuthenticator());
    }

    protected void populateStepConfigWithAuthenticationDetails(StepConfig stepConfig, AuthenticatedIdPData
            authenticatedIdPData, AuthenticatorConfig authenticatedStepIdp) {

        stepConfig.setAuthenticatedUser(authenticatedIdPData.getUser());
        stepConfig.setAuthenticatedIdP(authenticatedIdPData.getIdpName());
        stepConfig.setAuthenticatedAutenticator(authenticatedStepIdp);
    }

    /**
     * Returns the {@link AuthenticatedIdPData} for the given IDP name if it is in the current authenticated IDPs.
     * If the {@link AuthenticatedIdPData} is not available in the current authenticate IDPs, tries the previous ones.
     * If both checks are false, returns a newly created {@link AuthenticatedIdPData}
     *
     * @param context
     * @param idpName
     * @return
     */
    private AuthenticatedIdPData getAuthenticatedIdPData(AuthenticationContext context, String idpName) {

        AuthenticatedIdPData authenticatedIdPData = null;

        if (context.getCurrentAuthenticatedIdPs() != null && context.getCurrentAuthenticatedIdPs().get(idpName)
                != null) {

            authenticatedIdPData = context.getCurrentAuthenticatedIdPs().get(idpName);

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Authenticated IDP data of the IDP '%s' " +
                        "could be found in current authenticated IDPs", idpName));
            }
        } else {

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Authenticated IDP data of the IDP '%s' " +
                        "couldn't be found in current authenticate IDPs. Trying previous authenticated IDPs", idpName));
            }

            if (context.getPreviousAuthenticatedIdPs() != null &&
                    context.getPreviousAuthenticatedIdPs().get(idpName) != null) {

                authenticatedIdPData = context.getPreviousAuthenticatedIdPs().get(idpName);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Authenticated IDP data of the IDP '%s' " +
                            "could be found in previous authenticated IDPs", idpName));
                }
            } else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Authenticated IDP data for the IDP '%s' " +
                            "couldn't be found in previous authenticate IDPs as well. " +
                            "Using a fresh AuthenticatedIdPData object", idpName));
                }

                authenticatedIdPData = new AuthenticatedIdPData();
            }
        }

        return authenticatedIdPData;
    }

    protected String getRedirectUrl(HttpServletRequest request, HttpServletResponse response, AuthenticationContext
            context, String authenticatorNames, String showAuthFailureReason, String retryParam, String loginPage)
            throws IOException, URISyntaxException {

        IdentityErrorMsgContext errorContext = IdentityUtil.getIdentityErrorMsg();
        IdentityUtil.clearIdentityErrorMsg();

        retryParam = handleIdentifierFirstLogin(context, retryParam);
        String otp = (String) context.getProperty(FrameworkConstants.PASSWORD_PROPERTY);
        context.getProperties().remove(FrameworkConstants.PASSWORD_PROPERTY);
        String username = request.getParameter(USERNAME);

        // If recaptcha is enabled and the Basic Authenticator is in the authenticator list for this page, the recaptcha
        // params set by the Basic Authenticator need to be added to new URL generated for the multi option page.
        // Currently, there is no method to check whether recaptcha has been enabled without manually reading the
        // captcha-config.properties file. Hence, this fragment is always executed without the check, but will not
        // alter the final URL if recaptcha is not enabled. This filters out the recaptcha params from the redirect
        // URL previously set by an authenticator and generates a query string to be appended to the new redirect URL.
        StringBuilder reCaptchaParamString = new StringBuilder("");
        StringBuilder errorParamString = new StringBuilder("");
        List<NameValuePair> errorContextParams = new ArrayList<>();
        String basicAuthRedirectUrl = ((CommonAuthResponseWrapper) response).getRedirectURL();
        if (StringUtils.isNotBlank(basicAuthRedirectUrl)) {
            List<NameValuePair> queryParameters = new URIBuilder(basicAuthRedirectUrl).getQueryParams();
            List<NameValuePair> reCaptchaParameters = queryParameters.stream().filter(param ->
                    FrameworkConstants.RECAPTCHA_API_PARAM.equals(param.getName()) ||
                            FrameworkConstants.RECAPTCHA_KEY_PARAM.equals(param.getName()) ||
                            FrameworkConstants.RECAPTCHA_PARAM.equals(param.getName()) ||
                            FrameworkConstants.RECAPTCHA_RESEND_CONFIRMATION_PARAM.equals(param.getName())
            )
                    .collect(Collectors.toList());
            for (NameValuePair reCaptchaParam : reCaptchaParameters) {
                reCaptchaParamString.append("&").append(reCaptchaParam.getName()).append("=")
                        .append(reCaptchaParam.getValue());
            }

            if (errorContext == null) {
                 errorContextParams.addAll(queryParameters.stream()
                        .filter(param -> FrameworkConstants.ERROR_CODE.equals(param.getName()) ||
                                FrameworkConstants.LOCK_REASON.equals(param.getName()) ||
                                FrameworkConstants.REMAINING_ATTEMPTS.equals(param.getName()) ||
                                FrameworkConstants.FAILED_USERNAME.equals(param.getName()))
                        .collect(Collectors.toList()));
                if (!errorContextParams.isEmpty()) {
                    for (NameValuePair errorParams : errorContextParams) {
                        errorParamString.append("&").append(errorParams.getName()).append("=")
                                .append(errorParams.getValue());
                    }
                }
            }
        }
        if (StringUtils.isBlank(reCaptchaParamString.toString())) {
            String captchaParamStringFromContext = (String) context.getProperty(
                    FrameworkConstants.CAPTCHA_PARAM_STRING);
            if (captchaParamStringFromContext != null) {
                reCaptchaParamString.append(captchaParamStringFromContext);
                context.removeProperty(FrameworkConstants.CAPTCHA_PARAM_STRING);
            }
        }

        if (showAuthFailureReason != null && "true".equals(showAuthFailureReason)) {
            if (errorContext != null) {
                String errorCode = errorContext.getErrorCode();
                String reason = null;
                if (errorCode.contains(":")) {
                    String[] errorCodeReason = errorCode.split(":", 2);
                    if (errorCodeReason.length > 1) {
                        errorCode = errorCodeReason[0];
                        reason = errorCodeReason[1];
                    }
                }
                int remainingAttempts = errorContext.getMaximumLoginAttempts() - errorContext.getFailedLoginAttempts();

                if (LOG.isDebugEnabled()) {
                    String debugString = "Identity error message context is not null. Error details are as follows." +
                            "errorCode : " + errorCode + "\n" +
                            "username : " + username + "\n" +
                            "remainingAttempts : " + remainingAttempts;
                    LOG.debug(debugString);
                }

                if (UserCoreConstants.ErrorCode.INVALID_CREDENTIAL.equals(errorCode)) {
                    retryParam = String.format("%s&errorCode=%s&remainingAttempts=%d", retryParam, errorCode,
                            remainingAttempts);
                    if (username != null) {
                        retryParam = String.format("%s&failedUsername=%s", retryParam, URLEncoder.encode(username,
                                "UTF-8"));
                    }
                    return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams()))
                            + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                            reCaptchaParamString.toString();
                } else if (UserCoreConstants.ErrorCode.USER_IS_LOCKED.equals(errorCode)) {
                    String redirectURL;
                    redirectURL = response.encodeRedirectURL(loginPage
                            + ("?" + context.getContextIdIncludedQueryParams()))
                            + String.format(
                            "&errorCode=%s&authenticators=%s",
                            errorCode, URLEncoder.encode(authenticatorNames, "UTF-8"))
                            + retryParam + reCaptchaParamString;
                    if (remainingAttempts == 0) {
                        redirectURL = String.format("%s&remainingAttempts=0", redirectURL);
                    }
                    if (!StringUtils.isBlank(reason)) {
                        redirectURL = String.format("%s&lockedReason=%s", redirectURL, reason);
                    }
                    if (username != null) {
                        redirectURL = String.format("%s&failedUsername=%s", redirectURL, URLEncoder.encode(username,
                                "UTF-8"));
                    }
                    return redirectURL;
                } else if (IdentityCoreConstants.USER_ACCOUNT_NOT_CONFIRMED_ERROR_CODE.equals(errorCode)) {
                    retryParam = "&authFailure=true&authFailureMsg=account.confirmation.pending";
                    Object domain = IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN);
                    if (domain != null) {
                        username = IdentityUtil.addDomainToName(username, domain.toString());
                    }
                    retryParam = String.format("%s&errorCode=%s", retryParam, errorCode);
                    if (username != null) {
                        retryParam = String.format("%s&failedUsername=%s", retryParam, URLEncoder.encode(username,
                                "UTF-8"));
                    }
                    return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams()))
                            + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                            reCaptchaParamString.toString();
                } else if (IdentityCoreConstants.USER_INVALID_CREDENTIALS.equals(errorCode)) {
                    retryParam = "&authFailure=true&authFailureMsg=login.fail.message";
                    Object domain = IdentityUtil.threadLocalProperties.get().get(RE_CAPTCHA_USER_DOMAIN);
                    if (domain != null) {
                        username = IdentityUtil.addDomainToName(username, domain.toString());
                    }
                    retryParam = retryParam + "&errorCode=" + errorCode;
                    if (username != null) {
                        retryParam = String.format("%s&failedUsername=%s", retryParam, URLEncoder.encode(username,
                                "UTF-8"));
                    }
                    return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams()))
                            + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                            reCaptchaParamString.toString();
                } else if (IdentityCoreConstants.ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_ERROR_CODE
                        .equals(errorCode)) {
                    return getRedirectURLForcedPasswordResetOTP(request, response, context, authenticatorNames,
                            loginPage, otp, reCaptchaParamString);
                } else {
                    if (StringUtils.isNotBlank(retryParam) && StringUtils.isNotBlank(reason)) {
                        retryParam = "&authFailure=true&authFailureMsg=" + URLEncoder.encode(reason, "UTF-8");
                    }
                    retryParam = retryParam + "&errorCode=" + errorCode;
                    if (username != null) {
                        retryParam = String.format("%s&failedUsername=%s", retryParam, URLEncoder.encode(username,
                                "UTF-8"));
                    }
                    return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams()))
                            + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                            reCaptchaParamString.toString();
                }
            } else {
                if (errorContextParams.stream().anyMatch(nameValuePair ->
                        FrameworkConstants.ERROR_CODE.equals(nameValuePair.getName()) &&
                                UserCoreConstants.ErrorCode.USER_IS_LOCKED.equals(nameValuePair.getValue()))) {
                    if (isRedirectionToRetryPageOnAccountLock(context)) {
                        String retryPage = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
                        return response.encodeRedirectURL(retryPage
                                + ("?" + context.getContextIdIncludedQueryParams()))
                                + errorParamString;
                    }
                }
                return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams())) +
                        "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                        reCaptchaParamString.toString() + errorParamString;
            }
        } else {
            String errorCode = errorContext != null ? errorContext.getErrorCode() : null;
            if (UserCoreConstants.ErrorCode.USER_IS_LOCKED.equals(errorCode)) {
                String redirectURL;
                redirectURL = response.encodeRedirectURL(loginPage + ("?"
                        + context.getContextIdIncludedQueryParams()))
                        + "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8")
                        + retryParam + reCaptchaParamString.toString();
                if (username != null) {
                    redirectURL = String.format("%s&failedUsername=%s", redirectURL, URLEncoder.encode(username,
                            "UTF-8"));
                }
                return redirectURL;

            } else if (IdentityCoreConstants.ADMIN_FORCED_USER_PASSWORD_RESET_VIA_OTP_ERROR_CODE.equals(errorCode)) {
                return getRedirectURLForcedPasswordResetOTP(request, response, context, authenticatorNames,
                        loginPage, otp, reCaptchaParamString);
            } else {
                return response.encodeRedirectURL(loginPage + ("?" + context.getContextIdIncludedQueryParams())) +
                        "&authenticators=" + URLEncoder.encode(authenticatorNames, "UTF-8") + retryParam +
                        reCaptchaParamString.toString();
            }
        }
    }

    protected String handleIdentifierFirstLogin(AuthenticationContext context, String retryParam) {

        Map<String, String> runtimeParams = context
                .getAuthenticatorParams(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS);
        String promptType = null;
        String usernameFromContext = null;
        if (runtimeParams != null) {
            usernameFromContext = runtimeParams.get(FrameworkConstants.JSAttributes.JS_OPTIONS_USERNAME);
            if (usernameFromContext != null) {
                promptType = FrameworkConstants.INPUT_TYPE_IDENTIFIER_FIRST;
            }
        }

        if (promptType != null) {
            retryParam += "&" + FrameworkConstants.RequestParams.INPUT_TYPE + "=" + promptType;
            context.addEndpointParam("username", usernameFromContext);
        }
        return retryParam;
    }

    protected AuthenticatorConfig getAuthenticatorConfig() {
        AuthenticatorConfig authConfig = FileBasedConfigurationBuilder.getInstance().getAuthenticatorBean
                (FrameworkConstants.BASIC_AUTHENTICATOR_CLASS);
        if (authConfig == null) {
            authConfig = new AuthenticatorConfig();
            authConfig.setParameterMap(new HashMap());
        }
        return authConfig;
    }

    /**
     * Check whether the user should be redirected to the retry.jsp page when the user's account is locked.
     * This decision is taken based on three configuration options, redirectToMultiOptionPageOnFailure,
     showAuthFailureReasonOnLoginPage and redirectToRetryPageOnAccountLock.
     *
     * @param context  Authentication context.
     * @return boolean Whether the user should be directed to retry.jsp page or not.
     */
    protected boolean isRedirectionToRetryPageOnAccountLock(AuthenticationContext context) {

        boolean sendToMultiOptionPage = context.isSendToMultiOptionPage();
        if (sendToMultiOptionPage) {
            return false;
        }
        Map<String, String> parameterMap = getAuthenticatorConfig().getParameterMap();
        if (MapUtils.isNotEmpty(parameterMap)) {
            String showAuthFailureReasonOnLoginPage =
                    parameterMap.get(FrameworkConstants.SHOW_AUTH_FAILURE_REASON_ON_LOGIN_PAGE_CONF);
            if (Boolean.parseBoolean(showAuthFailureReasonOnLoginPage)) {
                return false;
            }
            String redirectToRetryPageOnAccountLock =
                    parameterMap.get(FrameworkConstants.REDIRECT_TO_RETRY_PAGE_ON_ACCOUNT_LOCK_CONF);
            return Boolean.parseBoolean(redirectToRetryPageOnAccountLock);
        }
        return false;
    }

    private String getRedirectURLForcedPasswordResetOTP(HttpServletRequest request, HttpServletResponse response,
                                                        AuthenticationContext context, String authenticatorNames,
                                                        String loginPage, String otp,
                                                        StringBuilder reCaptchaParamString)
            throws IOException {

        String username = request.getParameter("username");
        // Setting callback so that the user is prompted to login after a password reset.
        String callback;
        try {
            callback = ServiceURLBuilder.create().addPath(loginPage).build().getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw new IdentityRuntimeException(
                    "Error while building callback url for context: " + loginPage, e);
        }

        callback = callback + ("?" + context.getContextIdIncludedQueryParams())
                + "&authenticators=" + authenticatorNames;

        if (username == null) {
            return response.encodeRedirectURL(
                    ("accountrecoveryendpoint/confirmrecovery.do?" + context.getContextIdIncludedQueryParams()))
                    + "&confirmation=" + otp + "&callback=" + URLEncoder.encode(callback, "UTF-8")
                    + reCaptchaParamString.toString();
        }
        return response.encodeRedirectURL(
                ("accountrecoveryendpoint/confirmrecovery.do?" + context.getContextIdIncludedQueryParams()))
                + "&username=" + URLEncoder.encode(username, "UTF-8") + "&confirmation=" + otp
                + "&callback=" + URLEncoder.encode(callback, "UTF-8") + reCaptchaParamString.toString();
    }
}
