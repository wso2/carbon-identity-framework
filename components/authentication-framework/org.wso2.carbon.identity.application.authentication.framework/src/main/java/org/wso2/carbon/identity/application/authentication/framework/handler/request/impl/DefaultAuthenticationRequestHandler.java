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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.DuplicatedAuthUserException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationContextProperty;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.LoginContextManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultAuthenticationRequestHandler implements AuthenticationRequestHandler {

    private static final Log log = LogFactory.getLog(DefaultAuthenticationRequestHandler.class);
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static volatile DefaultAuthenticationRequestHandler instance;

    public static final String AUTHZ_FAIL_REASON = "AUTHZ_FAIL_REASON";

    public static DefaultAuthenticationRequestHandler getInstance() {

        if (instance == null) {
            synchronized (DefaultAuthenticationRequestHandler.class) {
                if (instance == null) {
                    instance = new DefaultAuthenticationRequestHandler();
                }
            }
        }

        return instance;
    }

    /**
     * Executes the authentication flow
     *
     * @param request
     * @param response
     * @throws FrameworkException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("In authentication flow");
        }

        if (context.isReturning()) {
            // if "Deny" or "Cancel" pressed on the login page.
            if (request.getParameter(FrameworkConstants.RequestParams.DENY) != null) {
                handleDenyFromLoginPage(request, response, context);
                return;
            }

            // handle remember-me option from the login page
            handleRememberMeOptionFromLoginPage(request, context);
        }

        int currentStep = context.getCurrentStep();

        // if this is the start of the authentication flow
        if (currentStep == 0) {
            handleSequenceStart(request, response, context);
        }

        SequenceConfig seqConfig = context.getSequenceConfig();
        List<AuthenticatorConfig> reqPathAuthenticators = seqConfig.getReqPathAuthenticators();

        // if SP has request path authenticators configured and this is start of
        // the flow
        if (reqPathAuthenticators != null && !reqPathAuthenticators.isEmpty() && currentStep == 0) {
            // call request path sequence handler
            FrameworkUtils.getRequestPathBasedSequenceHandler().handle(request, response, context);
        }

        // if no request path authenticators or handler returned cannot handle
        if (!context.getSequenceConfig().isCompleted()
                || (reqPathAuthenticators == null || reqPathAuthenticators.isEmpty())) {
            // To keep track of whether particular request goes through the step based sequence handler.
            context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

            // call step based sequence handler
            FrameworkUtils.getStepBasedSequenceHandler().handle(request, response, context);
        }
        // handle post authentication
        handlePostAuthentication(request, response, context);
        // if flow completed, send response back
        if (canConcludeFlow(context)) {
            concludeFlow(request, response, context);
        }
    }

    protected boolean canConcludeFlow(AuthenticationContext context) {

        return LoginContextManagementUtil.isPostAuthenticationExtensionCompleted(context);
    }

    protected void handlePostAuthentication(HttpServletRequest request, HttpServletResponse response,
                                          AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Handling post authentication");
        }
        PostAuthenticationMgtService postAuthenticationMgtService =
                FrameworkServiceDataHolder.getInstance().getPostAuthenticationMgtService();

        if (context.getSequenceConfig().isCompleted()) {
            if (postAuthenticationMgtService != null) {
                postAuthenticationMgtService.handlePostAuthentication(request, response, context);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("No post authentication service found. Hence not evaluating post authentication.");
                }
                LoginContextManagementUtil.markPostAuthenticationCompleted(context);
            }
        } else {
            log.debug("Sequence is not completed yet. Hence skipping post authentication");
        }
    }

    private void handleDenyFromLoginPage(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("User has pressed Deny or Cancel in the login page. Terminating the authentication flow");
        }

        context.getSequenceConfig().setCompleted(true);
        context.setRequestAuthenticated(false);
        //No need to handle authorization, because the authentication is not completed
        concludeFlow(request, response, context);
    }

    private void handleRememberMeOptionFromLoginPage(HttpServletRequest request, AuthenticationContext context) {

        String rememberMe = request.getParameter(FrameworkConstants.RequestParams.REMEMBER_ME);

        if (FrameworkConstants.REMEMBER_ME_OPT_ON.equalsIgnoreCase(rememberMe)) {
            context.setRememberMe(true);
        } else {
            context.setRememberMe(false);
        }
    }

    /**
     * Handle the start of a Sequence
     *
     * @param request
     * @param response
     * @param context
     * @return
     * @throws ServletException
     * @throws IOException
     * @throws FrameworkException
     */
    protected boolean handleSequenceStart(HttpServletRequest request,
                                          HttpServletResponse response,
                                          AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Starting the sequence");
        }

        // "forceAuthenticate" - go in the full authentication flow even if user
        // is already logged in.
        boolean forceAuthenticate = request.getParameter(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE) != null ?
                Boolean.valueOf(request.getParameter(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE)) : false;

        context.setForceAuthenticate(forceAuthenticate);

        if (log.isDebugEnabled()) {
            log.debug("Force Authenticate : " + forceAuthenticate);
        }

        // "reAuthenticate" - authenticate again with the same IdPs as before.
        boolean reAuthenticate = request.getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE) != null ?
                Boolean.valueOf(request.getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE)) : false;

        if (log.isDebugEnabled()) {
            log.debug("Re-Authenticate : " + reAuthenticate);
        }

        context.setReAuthenticate(reAuthenticate);

        // "checkAuthentication" - passive mode. just send back whether user is
        // *already* authenticated or not.
        String passiveAuthReqParam = request.getParameter(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION);
        boolean passiveAuthenticate = passiveAuthReqParam != null ? Boolean.valueOf(passiveAuthReqParam) : false;

        if (log.isDebugEnabled()) {
            log.debug("Passive Authenticate : " + passiveAuthenticate);
        }

        context.setPassiveAuthenticate(passiveAuthenticate);

        return false;
    }

    /**
     * Sends the response to the servlet that initiated the authentication flow
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void concludeFlow(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Concluding the Authentication Flow");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        sequenceConfig.setCompleted(false);

        AuthenticationResult authenticationResult = new AuthenticationResult();
        boolean isAuthenticated = context.isRequestAuthenticated();
        authenticationResult.setAuthenticated(isAuthenticated);

        String authenticatedUserTenantDomain = getAuthenticatedUserTenantDomain(context, authenticationResult);

        authenticationResult.setSaaSApp(sequenceConfig.getApplicationConfig().isSaaSApp());

        if (isAuthenticated) {

            if (!sequenceConfig.getApplicationConfig().isSaaSApp()) {
                String spTenantDomain = context.getTenantDomain();
                String userTenantDomain = sequenceConfig.getAuthenticatedUser().getTenantDomain();
                if (StringUtils.isNotEmpty(userTenantDomain)) {
                    if (StringUtils.isNotEmpty(spTenantDomain) && !spTenantDomain.equals
                            (userTenantDomain)) {
                        throw new FrameworkException("Service Provider tenant domain must be equal to user tenant " +
                                "domain for non-SaaS applications");
                    }
                }
            }

            authenticationResult.setSubject(new AuthenticatedUser(sequenceConfig.getAuthenticatedUser()));
            ApplicationConfig appConfig = sequenceConfig.getApplicationConfig();

            if (appConfig.getServiceProvider().getLocalAndOutBoundAuthenticationConfig()
                    .isAlwaysSendBackAuthenticatedListOfIdPs()) {
                authenticationResult.setAuthenticatedIdPs(sequenceConfig.getAuthenticatedIdPs());
            }

            // SessionContext is retained across different SP requests in the same browser session.
            // it is tracked by a cookie

            SessionContext sessionContext = null;
            String commonAuthCookie = null;
            String sessionContextKey = null;
            // Force authentication requires the creation of a new session. Therefore skip using the existing session
            if (FrameworkUtils.getAuthCookie(request) != null && !context.isForceAuthenticate()) {

                commonAuthCookie = FrameworkUtils.getAuthCookie(request).getValue();

                if (commonAuthCookie != null) {
                    sessionContextKey = DigestUtils.sha256Hex(commonAuthCookie);
                    sessionContext = FrameworkUtils.getSessionContextFromCache(sessionContextKey);
                }
            }

            String applicationTenantDomain = getApplicationTenantDomain(context);
            // session context may be null when cache expires therefore creating new cookie as well.
            if (sessionContext != null) {
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(),
                        sequenceConfig);
                sessionContext.getAuthenticatedIdPs().putAll(context.getCurrentAuthenticatedIdPs());
                sessionContext.getSessionAuthHistory().resetHistory(AuthHistory
                        .merge(sessionContext.getSessionAuthHistory().getHistory(),
                                context.getAuthenticationStepHistory()));
                if (context.getSelectedAcr() != null) {
                    sessionContext.getSessionAuthHistory().setSelectedAcrValue(context.getSelectedAcr());
                }
                long updatedSessionTime = System.currentTimeMillis();
                if (!context.isPreviousAuthTime()) {
                    sessionContext.addProperty(FrameworkConstants.UPDATED_TIMESTAMP, updatedSessionTime);
                }

                List<AuthenticationContextProperty> authenticationContextProperties = new ArrayList<>();

                // Authentication context properties from already authenticated IdPs
                if (sessionContext.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES) != null) {
                    List<AuthenticationContextProperty> existingAuthenticationContextProperties =
                            (List<AuthenticationContextProperty>) sessionContext.getProperty(FrameworkConstants
                                    .AUTHENTICATION_CONTEXT_PROPERTIES);
                    for (AuthenticationContextProperty contextProperty : existingAuthenticationContextProperties) {
                        for (StepConfig stepConfig : context.getSequenceConfig().getStepMap().values()) {
                            if (stepConfig.getAuthenticatedIdP().equals(contextProperty.getIdPName())) {
                                authenticationContextProperties.add(contextProperty);
                                break;
                            }
                        }
                    }
                }

                Long createdTime = (Long)sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
                if (createdTime != null) {
                    authenticationResult.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTime);
                }

                // Authentication context properties received from newly authenticated IdPs
                if (context.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES) != null) {
                    authenticationContextProperties.addAll((List<AuthenticationContextProperty>) context
                            .getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES));

                    if (sessionContext.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES) == null) {
                        sessionContext.addProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES,
                                authenticationContextProperties);
                    } else {
                        List<AuthenticationContextProperty> existingAuthenticationContextProperties =
                                (List<AuthenticationContextProperty>) sessionContext.getProperty(FrameworkConstants
                                        .AUTHENTICATION_CONTEXT_PROPERTIES);
                        existingAuthenticationContextProperties.addAll((List<AuthenticationContextProperty>)
                                context.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES));

                    }
                }

                if (!authenticationContextProperties.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("AuthenticationContextProperties are available.");
                    }
                    authenticationResult.addProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES,
                            authenticationContextProperties);
                }

                // TODO add to cache?
                // store again. when replicate  cache is used. this may be needed.
                FrameworkUtils.addSessionContextToCache(sessionContextKey, sessionContext, applicationTenantDomain);
                FrameworkUtils.publishSessionEvent(sessionContextKey, request, context, sessionContext, sequenceConfig
                        .getAuthenticatedUser(), FrameworkConstants.AnalyticsAttributes.SESSION_UPDATE);

            } else {
                sessionContext = new SessionContext();
                // To identify first login
                context.setProperty(FrameworkConstants.AnalyticsAttributes.IS_INITIAL_LOGIN, true);
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(),
                        sequenceConfig);
                sessionContext.setAuthenticatedIdPs(context.getCurrentAuthenticatedIdPs());
                sessionContext.setRememberMe(context.isRememberMe());
                if (context.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("AuthenticationContextProperties are available.");
                    }
                    authenticationResult.addProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES,
                            context.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES));
                    // Add to session context
                    sessionContext.addProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES,
                            context.getProperty(FrameworkConstants.AUTHENTICATION_CONTEXT_PROPERTIES));
                }
                String sessionKey = UUIDGenerator.generateUUID();
                sessionContextKey = DigestUtils.sha256Hex(sessionKey);
                sessionContext.addProperty(FrameworkConstants.AUTHENTICATED_USER, authenticationResult.getSubject());
                Long createdTimeMillis = System.currentTimeMillis();
                sessionContext.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTimeMillis);
                authenticationResult.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTimeMillis);
                sessionContext.getSessionAuthHistory().resetHistory(
                        AuthHistory.merge(sessionContext.getSessionAuthHistory().getHistory(),
                                context.getAuthenticationStepHistory()));
                if (context.getSelectedAcr() != null) {
                    sessionContext.getSessionAuthHistory().setSelectedAcrValue(context.getSelectedAcr());
                }

                FrameworkUtils.addSessionContextToCache(sessionContextKey, sessionContext, applicationTenantDomain);
                setAuthCookie(request, response, context, sessionKey, applicationTenantDomain);
                FrameworkUtils.publishSessionEvent(sessionContextKey, request, context, sessionContext, sequenceConfig
                        .getAuthenticatedUser(), FrameworkConstants.AnalyticsAttributes.SESSION_CREATE);
            }

            if (authenticatedUserTenantDomain == null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }
            publishAuthenticationSuccess(request, context, sequenceConfig.getAuthenticatedUser());

            if (FrameworkServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
                try {
                    storeSessionData(context, sessionContextKey);
                } catch (UserSessionException e) {
                    throw new FrameworkException("Error while storing session details of the authenticated user to " +
                            "the database", e);
                }
            }
        }

        // Checking weather inbound protocol is an already cache removed one, request come from federated or other
        // authenticator in multi steps scenario. Ex. Fido
        if (FrameworkUtils.getCacheDisabledAuthenticators().contains(context.getRequestType())
                && (response instanceof CommonAuthResponseWrapper) &&
                !((CommonAuthResponseWrapper) response).isWrappedByFramework()) {
            //Set the result as request attribute
            request.setAttribute("sessionDataKey", context.getCallerSessionKey());
            addAuthenticationResultToRequest(request, authenticationResult);
        } else {
            FrameworkUtils.addAuthenticationResultToCache(context.getCallerSessionKey(), authenticationResult);
        }
        /*
         * TODO Cache retaining is a temporary fix. Remove after Google fixes
         * http://code.google.com/p/gdata-issues/issues/detail?id=6628
         */
        String retainCache = System.getProperty("retainCache");

        if (retainCache == null) {
            FrameworkUtils.removeAuthenticationContextFromCache(context.getContextIdentifier());
        }

        sendResponse(request, response, context);
    }

    /**
     * Method used to store user and session related data to the database.
     *
     * @param context           {@link AuthenticationContext} object with the authentication request related data
     * @param sessionContextKey of the authenticated session
     */
    private void storeSessionData(AuthenticationContext context, String sessionContextKey)
            throws UserSessionException {

        for (AuthenticatedIdPData authenticatedIdPData : context.getCurrentAuthenticatedIdPs().values()) {
            String userName = authenticatedIdPData.getUser().getUserName();
            String tenantDomain = getAuthenticatedUserTenantDomain(context, null);
            int tenantId = (tenantDomain == null) ? MultitenantConstants.INVALID_TENANT_ID : IdentityTenantUtil
                    .getTenantId(tenantDomain);
            String userStoreDomain = authenticatedIdPData.getUser().getUserStoreDomain();
            String idpName = authenticatedIdPData.getIdpName();
            String userId;
            try {
                int idpId = UserSessionStore.getInstance().getIdPId(idpName);
                userId = UserSessionStore.getInstance().getUserId(userName, tenantId, userStoreDomain, idpId);

                boolean persistUserToSessionMapping = true;
                try {
                    if (userId == null) {
                        userId = UUIDGenerator.generateUUID();
                        UserSessionStore.getInstance().storeUserData(userId, userName, tenantId, userStoreDomain, idpId);
                    }
                } catch (DuplicatedAuthUserException e) {
                    // When the authenticated user is already persisted the respective user to session mapping will
                    // be persisted from the same node handling the request.
                    // Thus, persisting the user to session mapping can be gracefully ignored here.
                    persistUserToSessionMapping = false;
                    String msg = "User authenticated is already persisted. Username: " + userName + " Tenant Domain:" +
                            " " + tenantDomain + " User Store Domain: " + userStoreDomain + " IdP: " + idpName;
                    log.warn(msg);
                    if (log.isDebugEnabled()) {
                        log.debug(msg, e);
                    }
                }

                if (persistUserToSessionMapping && !UserSessionStore.getInstance().isExistingMapping(userId,
                        sessionContextKey)) {
                    UserSessionStore.getInstance().storeUserSessionData(userId, sessionContextKey);
                }
            } catch (UserSessionException e) {
                throw new UserSessionException("Error while storing session data for user: " + userName + " of " +
                        "user store domain: " + userStoreDomain + " in tenant domain: " + tenantDomain , e);
            }
        }
    }

    private String getApplicationTenantDomain(AuthenticationContext context) {

        return (StringUtils.isNotEmpty(context.getTenantDomain()) ?
                context.getTenantDomain() : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

    }

    private void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                              AuthenticatedUser user) {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            authnDataPublisherProxy.publishAuthenticationSuccess(request, context,
                    unmodifiableParamMap);

        }
    }

    /**
     * Add authentication request as request attribute
     *
     * @param request
     * @param authenticationResult
     */
    private void addAuthenticationResultToRequest(HttpServletRequest request,
                                                  AuthenticationResult authenticationResult) {

        request.setAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT, authenticationResult);
    }

    private void setAuthCookie(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
                               String sessionKey, String tenantDomain) throws FrameworkException {

        Integer authCookieAge = null;

        if (context.isRememberMe()) {
            authCookieAge = IdPManagementUtil.getRememberMeTimeout(tenantDomain);
        }

        FrameworkUtils.storeAuthCookie(request, response, sessionKey, authCookieAge);
    }

    private String getAuthenticatedUserTenantDomain(AuthenticationContext context,
                                                    AuthenticationResult authenticationResult) {

        String authenticatedUserTenantDomain = null;
        if (context.getProperties() != null) {
            authenticatedUserTenantDomain = (String) context.getProperties()
                    .get("user-tenant-domain");
        }
        return authenticatedUserTenantDomain;
    }

    protected void sendResponse(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            StringBuilder debugMessage = new StringBuilder();
            debugMessage.append("Sending response back to: ");
            debugMessage.append(context.getCallerPath()).append("...\n");
            debugMessage.append(FrameworkConstants.ResponseParams.AUTHENTICATED).append(": ");
            debugMessage.append(String.valueOf(context.isRequestAuthenticated())).append("\n");
            debugMessage.append(FrameworkConstants.ResponseParams.AUTHENTICATED_USER).append(": ");
            if (context.getSequenceConfig().getAuthenticatedUser() != null) {
                debugMessage.append(context.getSequenceConfig().getAuthenticatedUser().getAuthenticatedSubjectIdentifier()).append("\n");
            } else {
                debugMessage.append("No Authenticated User").append("\n");
            }
            debugMessage.append(FrameworkConstants.ResponseParams.AUTHENTICATED_IDPS).append(": ");
            debugMessage.append(context.getSequenceConfig().getAuthenticatedIdPs()).append("\n");
            debugMessage.append(FrameworkConstants.SESSION_DATA_KEY).append(": ");
            debugMessage.append(context.getCallerSessionKey());

            log.debug(debugMessage);
        }

        // TODO rememberMe should be handled by a cookie authenticator. For now rememberMe flag that
        // was set in the login page will be sent as a query param to the calling servlet so it will
        // handle rememberMe as usual.
        String rememberMeParam = "";

        if (context.isRequestAuthenticated() && context.isRememberMe()) {
            rememberMeParam = rememberMeParam + "chkRemember=on";
        }

        // if request is not authenticated populate error information sent from authenticators/handlers
        if (!context.isRequestAuthenticated()) {
            populateErrorInformation(request, response, context);
        }

        // redirect to the caller
        String redirectURL;
        String commonauthCallerPath = context.getCallerPath();

        try {
            String queryParamsString = "";
            if (context.getCallerSessionKey() != null) {
                queryParamsString = FrameworkConstants.SESSION_DATA_KEY + "=" +
                        URLEncoder.encode(context.getCallerSessionKey(), "UTF-8");
            }

            if (StringUtils.isNotEmpty(rememberMeParam)) {
                queryParamsString += "&" + rememberMeParam;
            }
            redirectURL = FrameworkUtils.appendQueryParamsStringToUrl(commonauthCallerPath, queryParamsString);

            response.sendRedirect(redirectURL);
        } catch (IOException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }


    /**
     * Populate any error information sent from Authenticators to be sent in the Response from the authentication
     * framework. By default we retrieve the error information from the AuthenticationContext and populate the error
     * it within the AuthenticationResult as properties.
     *
     * @param request
     * @param response
     * @param context
     */
    protected void populateErrorInformation(HttpServletRequest request,
                                            HttpServletResponse response,
                                            AuthenticationContext context) {

        // get the authentication result
        AuthenticationResult authenticationResult = getAuthenticationResult(request, response, context);

        String errorCode = String.valueOf(context.getProperty(FrameworkConstants.AUTH_ERROR_CODE));
        String errorMessage = String.valueOf(context.getProperty(FrameworkConstants.AUTH_ERROR_MSG));
        String errorUri = String.valueOf(context.getProperty(FrameworkConstants.AUTH_ERROR_URI));

        if (authenticationResult != null) {

            if (IdentityUtil.isNotBlank(errorCode)) {
                // set the custom error code
                authenticationResult.addProperty(FrameworkConstants.AUTH_ERROR_CODE, errorCode);
            }

            if (IdentityUtil.isNotBlank(errorMessage)) {
                // set the custom error message
                authenticationResult.addProperty(FrameworkConstants.AUTH_ERROR_MSG, errorMessage);
            }

            if (IdentityUtil.isNotBlank(errorUri)) {
                // set the custom error uri
                authenticationResult.addProperty(FrameworkConstants.AUTH_ERROR_URI, errorUri);
            }

            if (log.isDebugEnabled()) {
                log.debug("Populated errorCode=" + errorCode + ", errorMessage=" + errorMessage + ", errorUri=" +
                        errorUri + " to the AuthenticationResult.");
            }

            // set the updated authentication result to request
            request.setAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT, authenticationResult);
        }
    }

    private AuthenticationResult getAuthenticationResult(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         AuthenticationContext context) {

        AuthenticationResult authenticationResult = null;
        if (FrameworkUtils.getCacheDisabledAuthenticators().contains(context.getRequestType())
                && (response instanceof CommonAuthResponseWrapper) &&
                !((CommonAuthResponseWrapper) response).isWrappedByFramework()) {
            // Get the authentication result from the request
            authenticationResult =
                    (AuthenticationResult) request.getAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT);
        } else {
            // Retrieve the authentication result from cache
            AuthenticationResultCacheEntry authenticationResultCacheEntry =
                    FrameworkUtils.getAuthenticationResultFromCache(context.getCallerSessionKey());
            if (authenticationResultCacheEntry != null) {
                authenticationResult = authenticationResultCacheEntry.getResult();
            }
        }
        return authenticationResult;
    }

}