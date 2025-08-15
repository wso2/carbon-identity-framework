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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
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
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.listener.SessionContextMgtListener;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationContextProperty;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.FederatedToken;
import org.wso2.carbon.identity.application.authentication.framework.services.PostAuthenticationMgtService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.LoginContextManagementUtil;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;
import org.wso2.carbon.user.core.model.UserMgtContext;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.nonNull;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ALLOW_SESSION_CREATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ORGANIZATION_USER_PROPERTIES;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_MISMATCHING_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_CONCLUDING_AUTHENTICATION_SUBJECT_ID_NULL;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_ERROR_CODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.addNonceCookie;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.getNonceCookieName;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.isNonceCookieEnabled;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.removeNonceCookie;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.validateNonceCookie;

/**
 * Default authentication request handler.
 */
public class DefaultAuthenticationRequestHandler implements AuthenticationRequestHandler {

    public static final String AUTHZ_FAIL_REASON = "AUTHZ_FAIL_REASON";
    private static final Log log = LogFactory.getLog(DefaultAuthenticationRequestHandler.class);
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String COMMA = ",";
    private static volatile DefaultAuthenticationRequestHandler instance;

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

        boolean addOrUpdateNonceCookie = false;
        try {
            UserStorePreferenceOrderSupplier<List<String>> userStorePreferenceOrderSupplier =
                    FrameworkUtils.getUserStorePreferenceOrderSupplier(context, null);
            if (userStorePreferenceOrderSupplier != null) {
                // Add the user store preference supplier to the container UserMgtContext.
                UserMgtContext userMgtContext = new UserMgtContext();
                userMgtContext.setUserStorePreferenceOrderSupplier(userStorePreferenceOrderSupplier);
                UserCoreUtil.setUserMgtContextInThreadLocal(userMgtContext);
            }

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

                // Add or Validate session nonce cookie.
                if (isNonceCookieEnabled()) {
                    String nonceCookieName = getNonceCookieName(context);
                    if (context.isReturning()) {
                        if (validateNonceCookie(request, context)) {
                            addOrUpdateNonceCookie = true;
                        } else {
                            throw new FrameworkException(NONCE_ERROR_CODE, "Session nonce cookie value is not " +
                                    "matching " +
                                    "for session with sessionDataKey: " + request.getParameter("sessionDataKey"));
                        }
                    } else if (context.getProperty(nonceCookieName) == null) {
                        addOrUpdateNonceCookie = true;
                        // Remove restartLoginFlow flag once we added session nonce cookie in the new browser session.
                        if (request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW) != null &&
                                request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW).equals("true")) {
                            request.removeAttribute(FrameworkConstants.RESTART_LOGIN_FLOW);
                        }
                    }
                }

                // call step based sequence handler
                FrameworkUtils.getStepBasedSequenceHandler().handle(request, response, context);
            }
        } catch (FrameworkException e) {
            // Remove nonce cookie after authentication failure.
            removeNonceCookie(request, response, context);
            throw e;
        } finally {
            UserCoreUtil.removeUserMgtContextInThreadLocal();
        }

        // handle post authentication
        try {
            handlePostAuthentication(request, response, context);
        } catch (FrameworkException e) {
            // Remove nonce cookie after post authentication failure.
            removeNonceCookie(request, response, context);
            throw e;
        }
        // if flow completed, send response back
        if (canConcludeFlow(context)) {
            // Remove nonce cookie after authentication completion.
            if (addOrUpdateNonceCookie) {
                removeNonceCookie(request, response, context);
            }
            concludeFlow(request, response, context);
        } else if (addOrUpdateNonceCookie) {
            // Update nonce cookie value.
            addNonceCookie(request, response, context);
        }
    }

    protected boolean canConcludeFlow(AuthenticationContext context) {

        if (context.isPassiveAuthenticate()) {
            if (log.isDebugEnabled()) {
                log.debug("Can conclude the authentication flow for passive authentication request from the " +
                        "application: " + context.getServiceProviderName());
            }
            return true;
        }
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
                if (log.isDebugEnabled()) {
                    log.debug("No post authentication service found. Hence not evaluating post authentication.");
                }
                LoginContextManagementUtil.markPostAuthenticationCompleted(context);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Sequence is not completed yet. Hence skipping post authentication");
            }
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

        // If the remember me flag is already set in one step, we don't need to override.
        if (context.isRememberMe()) {
            return;
        }

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
        request.setAttribute(FrameworkConstants.IS_AUTH_FLOW_CONCLUDED, true);

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
                        throw new FrameworkException(ERROR_MISMATCHING_TENANT_DOMAIN.getCode(),
                                ERROR_MISMATCHING_TENANT_DOMAIN.getMessage());
                    }
                }
            }

            DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = null;
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST)
                        .inputParam(LogConstants.InputKeys.APPLICATION_NAME, context.getServiceProviderName())
                        .inputParam(FrameworkConstants.LogConstants.TENANT_DOMAIN, context.getTenantDomain())
                        .inputParam(FrameworkConstants.LogConstants.USER, LoggerUtils.isLogMaskingEnable
                                ? LoggerUtils.getMaskedContent(sequenceConfig.getAuthenticatedUser().getUserName())
                                : sequenceConfig.getAuthenticatedUser().getUserName())
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.FAILED);
            }
            // Break the flow if the authenticated subject identifier or user id is null.
            if (StringUtils.isBlank(sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier())) {
                if (diagnosticLogBuilder != null) {
                    // diagnosticLogBuilder is null when diagnostic logs are disabled.
                    diagnosticLogBuilder.resultMessage(
                            ERROR_WHILE_CONCLUDING_AUTHENTICATION_SUBJECT_ID_NULL.getMessage());
                    LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                }
                throw new PostAuthenticationFailedException(
                        ERROR_WHILE_CONCLUDING_AUTHENTICATION_SUBJECT_ID_NULL.getCode(),
                        ERROR_WHILE_CONCLUDING_AUTHENTICATION_SUBJECT_ID_NULL.getMessage());
            }
            try {
                if (StringUtils.isBlank(sequenceConfig.getAuthenticatedUser().getUserId())) {
                    if (diagnosticLogBuilder != null) {
                        // diagnosticLogBuilder is null when diagnostic logs are disabled.
                        diagnosticLogBuilder.resultMessage(
                                ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getMessage());
                        LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                    }
                    throw new PostAuthenticationFailedException(
                            ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getCode(),
                            ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getMessage());
                }
            } catch (UserIdNotFoundException e) {
                if (diagnosticLogBuilder != null) {
                    // diagnosticLogBuilder is null when diagnostic logs are disabled.
                    diagnosticLogBuilder.resultMessage(
                            ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getMessage());
                    LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
                }
                throw new PostAuthenticationFailedException(
                        ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getCode(),
                        ERROR_WHILE_CONCLUDING_AUTHENTICATION_USER_ID_NULL.getMessage(), e);
            }

            authenticationResult.setSubject(new AuthenticatedUser(sequenceConfig.getAuthenticatedUser()));
            ApplicationConfig appConfig = sequenceConfig.getApplicationConfig();

            // Adding user organization properties to the authentication result, such as scopes.
            if (nonNull(context.getProperty(ORGANIZATION_USER_PROPERTIES))) {
                authenticationResult.addProperty(ORGANIZATION_USER_PROPERTIES,
                        context.getProperty(ORGANIZATION_USER_PROPERTIES));
            }

            if (appConfig.getServiceProvider().getLocalAndOutBoundAuthenticationConfig()
                    .isAlwaysSendBackAuthenticatedListOfIdPs()) {
                authenticationResult.setAuthenticatedIdPs(sequenceConfig.getAuthenticatedIdPs());
            }

            // SessionContext is retained across different SP requests in the same browser session.
            // it is tracked by a cookie

            SessionContext sessionContext = null;
            String commonAuthCookie = null;
            String sessionContextKey = null;
            String analyticsSessionAction = null;

            //When getting the cookie, it will not give the path. When paths are tenant qualified, it will only give
            // the cookies matching that path.
            Cookie authCookie = FrameworkUtils.getAuthCookie(request);
            // Force authentication requires the creation of a new session. Therefore skip using the existing session
            if (authCookie != null && !context.isForceAuthenticate()) {

                commonAuthCookie = authCookie.getValue();

                if (commonAuthCookie != null) {
                    sessionContextKey = DigestUtils.sha256Hex(commonAuthCookie);
                    sessionContext = FrameworkUtils.getSessionContextFromCache(sessionContextKey,
                            context.getLoginTenantDomain());
                }
            }

            String applicationTenantDomain = getApplicationTenantDomain(context);
            // session context may be null when cache expires therefore creating new cookie as well.
            if (sessionContext != null) {
                analyticsSessionAction = FrameworkConstants.AnalyticsAttributes.SESSION_UPDATE;
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(), sequenceConfig);
                sessionContext.getAuthenticatedIdPs().putAll(context.getCurrentAuthenticatedIdPs());
                if (!context.isPassiveAuthenticate()) {
                    setAuthenticatedIDPsOfApp(sessionContext, context.getCurrentAuthenticatedIdPs(),
                            appConfig.getApplicationName());
                }
                sessionContext.getSessionAuthHistory().resetHistory(AuthHistory
                        .merge(sessionContext.getSessionAuthHistory().getHistory(),
                                context.getAuthenticationStepHistory()));
                populateAuthenticationContextHistory(authenticationResult, context, sessionContext);
                long updatedSessionTime = System.currentTimeMillis();
                if (!context.isPreviousAuthTime()) {
                    sessionContext.addProperty(FrameworkConstants.UPDATED_TIMESTAMP, updatedSessionTime);
                }

                authenticationResult.addProperty(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionContextKey);
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

                Long createdTime = (Long) sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
                if (createdTime != null) {
                    authenticationResult.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTime);
                }

                Long updatedTime = (Long) sessionContext.getProperty(FrameworkConstants.UPDATED_TIMESTAMP);
                if (updatedTime != null) {
                    authenticationResult.addProperty(FrameworkConstants.UPDATED_TIMESTAMP, updatedTime);
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

                FrameworkUtils.updateSessionLastAccessTimeMetadata(sessionContextKey, updatedSessionTime);

                /*
                 * In the default configuration, the expiry time of the commonAuthCookie is fixed when rememberMe
                 * option is selected. With this config, the expiry time will increase at every authentication.
                 */
                if (sessionContext.isRememberMe() &&
                        Boolean.parseBoolean(IdentityUtil.getProperty(
                                IdentityConstants.ServerConfig.EXTEND_REMEMBER_ME_SESSION_ON_AUTH))) {
                    context.setRememberMe(sessionContext.isRememberMe());
                    setAuthCookie(request, response, context, commonAuthCookie, applicationTenantDomain);
                }

                if (context.getRuntimeClaims().size() > 0) {
                    sessionContext.addProperty(FrameworkConstants.RUNTIME_CLAIMS, context.getRuntimeClaims());
                }
                handleSessionContextUpdate(context.getRequestType(), sessionContextKey, sessionContext,
                        request, response, context);
                // TODO add to cache?
                // store again. when replicate  cache is used. this may be needed.
                FrameworkUtils.addSessionContextToCache(sessionContextKey, sessionContext, applicationTenantDomain,
                        context.getLoginTenantDomain());
                // Since the session context is already available, audit log will be added with updated details.
                addAuditLogs(SessionMgtConstants.UPDATE_SESSION_ACTION, authenticationResult.getSubject(),
                        sessionContextKey, FrameworkUtils.getCorrelation(),
                        updatedSessionTime, sessionContext.isRememberMe());
            } else {
                analyticsSessionAction = FrameworkConstants.AnalyticsAttributes.SESSION_CREATE;
                sessionContext = new SessionContext();
                // To identify first login
                context.setProperty(FrameworkConstants.AnalyticsAttributes.IS_INITIAL_LOGIN, true);
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(),
                        sequenceConfig);
                sessionContext.setAuthenticatedIdPs(context.getCurrentAuthenticatedIdPs());
                setAuthenticatedIDPsOfApp(sessionContext, context.getCurrentAuthenticatedIdPs(),
                        appConfig.getApplicationName());
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
                String sessionKey = UUID.randomUUID().toString();
                sessionContextKey = DigestUtils.sha256Hex(sessionKey);
                sessionContext.addProperty(FrameworkConstants.AUTHENTICATED_USER, authenticationResult.getSubject());
                sessionContext.addProperty(FrameworkUtils.TENANT_DOMAIN, context.getLoginTenantDomain());
                Long createdTimeMillis = System.currentTimeMillis();
                sessionContext.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTimeMillis);
                authenticationResult.addProperty(FrameworkConstants.CREATED_TIMESTAMP, createdTimeMillis);
                authenticationResult.addProperty(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionContextKey);
                sessionContext.getSessionAuthHistory().resetHistory(
                        AuthHistory.merge(sessionContext.getSessionAuthHistory().getHistory(),
                                context.getAuthenticationStepHistory()));
                populateAuthenticationContextHistory(authenticationResult, context, sessionContext);

                if (context.getRuntimeClaims().size() > 0) {
                    sessionContext.addProperty(FrameworkConstants.RUNTIME_CLAIMS, context.getRuntimeClaims());
                }

                handleInboundSessionCreate(context.getRequestType(), sessionContextKey, sessionContext,
                        request, response, context);
                FrameworkUtils.addSessionContextToCache(sessionContextKey, sessionContext, applicationTenantDomain,
                        context.getLoginTenantDomain());
                // The session context will be stored from here. Since the audit log will be logged as a storing
                // operation.
                addAuditLogs(SessionMgtConstants.STORE_SESSION_ACTION,
                        authenticationResult.getSubject(), sessionContextKey, FrameworkUtils.getCorrelation(),
                        createdTimeMillis, sessionContext.isRememberMe());
                if (request.getAttribute(ALLOW_SESSION_CREATION) == null
                        || Boolean.parseBoolean(request.getAttribute(ALLOW_SESSION_CREATION).toString())) {
                    setAuthCookie(request, response, context, sessionKey, applicationTenantDomain);
                }
                if (FrameworkServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
                    try {
                        storeSessionMetaData(sessionContextKey, request);
                    } catch (UserSessionException e) {
                        log.error("Storing session meta data failed.", e);
                    }
                }
            }

            if (authenticatedUserTenantDomain == null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }

            if (FrameworkServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
                try {
                    storeSessionData(context, sessionContextKey);
                } catch (UserSessionException e) {
                    throw new FrameworkException("Error while storing session details of the authenticated user to " +
                            "the database", e);
                }
            }
            // Check whether the authentication flow includes a SAML federated IdP and
            // store the saml index with the session context key for the single logout.
            if (context.getAuthenticationStepHistory() != null) {
                for (AuthHistory authHistory : context.getAuthenticationStepHistory()) {
                    if (StringUtils.isNotBlank(authHistory.getIdpSessionIndex()) &&
                            StringUtils.isNotBlank(authHistory.getIdpName())) {
                        try {
                            if (FrameworkUtils.isIdpIdColumnAvailableInFedAuthTable()) {
                                int idpId = Integer.parseInt(IdentityProviderManager.getInstance()
                                        .getIdPByName(authHistory.getIdpName(), context.getTenantDomain()).getId());
                                if (FrameworkUtils.isTenantIdColumnAvailableInFedAuthTable()) {
                                    storeFedAuthSessionWithTenantIdAndIdpId(context.getTenantDomain(),
                                            sessionContextKey, authHistory, idpId);
                                } else {
                                    storeFedAuthSessionWithIdpId(context.getTenantDomain(), sessionContextKey,
                                            authHistory);
                                }
                            } else {
                                if (FrameworkUtils.isTenantIdColumnAvailableInFedAuthTable()) {
                                    storeFedAuthSessionWithTenantId(context.getTenantDomain(), sessionContextKey,
                                            authHistory);
                                } else {
                                    storeFedAuthSessionMapping(sessionContextKey, authHistory);
                                }
                            }
                        } catch (UserSessionException | IdentityProviderManagementException e) {
                            throw new FrameworkException("Error while storing federated authentication session details "
                                    + "of the authenticated user to the database", e);
                        }
                    }
                }
            }
            FrameworkUtils.publishSessionEvent(sessionContextKey, request, context, sessionContext, sequenceConfig
                        .getAuthenticatedUser(), analyticsSessionAction);
            publishAuthenticationSuccess(request, context, sequenceConfig.getAuthenticatedUser());
        }

        // Passing the federated tokens to the authentication result.
        if (context.getProperty(FrameworkConstants.FEDERATED_TOKENS) instanceof List) {
            authenticationResult.addProperty(FrameworkConstants.FEDERATED_TOKENS,
                    context.getProperty(FrameworkConstants.FEDERATED_TOKENS));

            if (log.isDebugEnabled()) {
                List<String> federatedAuthenticatorNames = getFederatedAuthenticatorName(
                        (List<FederatedToken>) context.getProperty(FrameworkConstants.FEDERATED_TOKENS));
                log.debug("Federated tokens are available in the authentication context for the IDP: " +
                        StringUtils.join(federatedAuthenticatorNames, COMMA) +
                        " and added to the authentication result");
            }
        }

        // Adding locally mapped remote claims to authentication results.
        if (context.getProperty(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES) instanceof Map) {
            Map<String, String> mappedRemoteClaims =
                    (Map<String, String>) context.getProperty(FrameworkConstants.UNFILTERED_SP_CLAIM_VALUES);
            authenticationResult.setMappedRemoteClaims(mappedRemoteClaims);
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

    private void storeFedAuthSessionMapping(String sessionContextKey, AuthHistory authHistory)
            throws UserSessionException {

        UserSessionStore userSessionStore = UserSessionStore.getInstance();
        if (!userSessionStore.hasExistingFederatedAuthSession(
                authHistory.getIdpSessionIndex())) {
            userSessionStore.storeFederatedAuthSessionInfo(sessionContextKey, authHistory);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Federated auth session with the id: " + authHistory.getIdpSessionIndex() + " already " +
                        "exists.");
            }
            userSessionStore.updateFederatedAuthSessionInfo(sessionContextKey, authHistory);
        }
    }

    private void storeFedAuthSessionWithTenantId(String tenantDomain, String sessionContextKey,
             AuthHistory authHistory) throws UserSessionException {

        UserSessionStore userSessionStore = UserSessionStore.getInstance();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (!userSessionStore.isExistingFederatedAuthSessionAvailable(authHistory.getIdpSessionIndex(), tenantId)) {
            userSessionStore.storeFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Federated auth session with the id: " + authHistory.getIdpSessionIndex() + " already " +
                        "exists.");
            }
            userSessionStore.updateFederatedAuthSessionInfo(sessionContextKey, authHistory,
                    tenantId);
        }
    }

    private void storeFedAuthSessionWithIdpId(String tenantDomain, String sessionContextKey,
            AuthHistory authHistory) throws UserSessionException, IdentityProviderManagementException {

        UserSessionStore userSessionStore = UserSessionStore.getInstance();
        int idpId = Integer.parseInt(IdentityProviderManager.getInstance()
                .getIdPByName(authHistory.getIdpName(), tenantDomain).getId());
        if (!userSessionStore.hasExistingFederatedAuthSessionWithIdpId(authHistory.getIdpSessionIndex(), idpId)) {
            userSessionStore.storeFederatedAuthSessionInfoWithIdpId(sessionContextKey, authHistory, idpId);
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Federated auth session with the session id: %s and idp id: %s already exists.",
                        authHistory.getIdpSessionIndex(), idpId));
            }
            userSessionStore.updateFederatedAuthSessionInfoWithIdpId(sessionContextKey, authHistory, idpId);
        }
    }

    private void storeFedAuthSessionWithTenantIdAndIdpId(String tenantDomain, String sessionContextKey,
             AuthHistory authHistory, int idpId) throws UserSessionException {

        UserSessionStore userSessionStore = UserSessionStore.getInstance();
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (userSessionStore.hasExistingFederatedAuthSession(authHistory.getIdpSessionIndex(), tenantId, idpId)) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Federated auth session with the session id: %s and idp id: %s already exists.",
                        authHistory.getIdpSessionIndex(), idpId));
            }
            userSessionStore.updateFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId, idpId);
        } else {
            userSessionStore.storeFederatedAuthSessionInfo(sessionContextKey, authHistory, tenantId, idpId);
        }
    }

    private void handleSessionContextUpdate(String requestType, String sessionContextKey, SessionContext sessionContext,
                                            HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationContext context) {

        SessionContextMgtListener sessionContextMgtListener = FrameworkServiceDataHolder.getInstance()
                .getSessionContextMgtListener(requestType);
        if (sessionContextMgtListener == null) {
            return;
        }
        Map<String, String> inboundProperties = sessionContextMgtListener.onPreUpdateSession(sessionContextKey, request,
                response, context);
        inboundProperties.forEach(sessionContext::addProperty);
    }

    private void handleInboundSessionCreate(String requestType, String sessionContextKey, SessionContext sessionContext,
                                            HttpServletRequest request, HttpServletResponse response,
                                            AuthenticationContext context) {

        SessionContextMgtListener sessionContextMgtListener = FrameworkServiceDataHolder.getInstance()
                .getSessionContextMgtListener(requestType);
        if (sessionContextMgtListener == null) {
            return;
        }
        Map<String, String> inboundProperties = sessionContextMgtListener.onPreCreateSession(sessionContextKey, request,
                response, context);
        inboundProperties.forEach(sessionContext::addProperty);
    }

    /**
     * Polulates the authentication history information and sets it as a request attribute.
     * The inbound protocol
     *
     * @param authenticationResult
     * @param context
     * @param sessionContext
     */
    private void populateAuthenticationContextHistory(AuthenticationResult authenticationResult,
                                                      AuthenticationContext context,
                                                      SessionContext sessionContext) {

        if (context.getSelectedAcr() != null) {
            sessionContext.getSessionAuthHistory().setSelectedAcrValue(context.getSelectedAcr());
            sessionContext.getSessionAuthHistory().setSessionCreatedTime(calculateCreatedTime(sessionContext));
        }

        authenticationResult.addProperty(FrameworkConstants.SESSION_AUTH_HISTORY,
                sessionContext.getSessionAuthHistory());
    }

    /**
     * Calculates the session creted time from the available information.
     *
     * @param sessionContext
     * @return DateTime object of the SSO session created. Will return current time if it can not be inferred.
     */
    private DateTime calculateCreatedTime(SessionContext sessionContext) {
        Object createdTsObject = sessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP);
        if (createdTsObject != null) {
            long createdTimeLong = Long.parseLong(createdTsObject.toString());
            return new DateTime(createdTimeLong);
        }
        return DateTime.now();
    }

    /**
     * Method used to store user and session related data to the database.
     *
     * @param context           {@link AuthenticationContext} object with the authentication request related data
     * @param sessionContextKey of the authenticated session
     */
    private void storeSessionData(AuthenticationContext context, String sessionContextKey)
            throws UserSessionException, PostAuthenticationFailedException {

        String subject = context.getSequenceConfig().getAuthenticatedUser().getAuthenticatedSubjectIdentifier();
        String inboundAuth = context.getCallerPath().substring(1);
        int appId = context.getSequenceConfig().getApplicationConfig().getApplicationID();

        for (AuthenticatedIdPData authenticatedIdPData : context.getCurrentAuthenticatedIdPs().values()) {
            AuthenticatedUser user = authenticatedIdPData.getUser();

            try {
                if (FrameworkServiceDataHolder.getInstance()
                        .isSkipLocalUserSearchForAuthenticationFlowHandlersEnabled() &&
                        FrameworkUtils.isAllFlowHandlers(authenticatedIdPData.getAuthenticators()) &&
                        !FrameworkUtils.isJITProvisioningEnabled(context) && !user.isUserIdExists()) {
                    continue;
                }
                String userId = user.getUserId();

                try {
                    if (!UserSessionStore.getInstance().isExistingMapping(userId, sessionContextKey)) {
                        UserSessionStore.getInstance().storeUserSessionData(userId, sessionContextKey);
                    }
                    /*
                For JIT provisioned users, if AssertIdentity Using Mapped Local Subject Identifier config is enabled in
                the app level, add an entry in the IDN_AUTH_USER_SESSION_MAPPING table with local userId.
                 */
                    if (user.isFederatedUser() &&
                            context.getSequenceConfig().getApplicationConfig().isMappedSubjectIDSelected()) {
                        String localUserId =
                                FrameworkUtils.resolveUserIdFromUsername(
                                        IdentityTenantUtil.getTenantId(user.getTenantDomain()),
                                        user.getUserStoreDomain(), user.getUserName());
                        if (StringUtils.isNotEmpty(localUserId) &&
                                !UserSessionStore.getInstance().isExistingMapping(localUserId, sessionContextKey)) {
                            try {
                                UserSessionStore.getInstance().storeUserSessionData(localUserId, sessionContextKey);
                            } catch (DuplicatedAuthUserException e) {
                                // If isExistingMapping return false due to a database write latency issue,
                                // the same user to session mapping will be persisted from the same node handling the
                                // request. Thus, persisting the user to session mapping can be gracefully ignored here.
                                if (log.isDebugEnabled()) {
                                    log.debug("Mapping between session Id: " + sessionContextKey + " and user Id: "
                                            + userId + " is already persisted.");
                                }
                            }
                        }
                    }
                } catch (UserSessionException e) {
                    throw new UserSessionException("Error while storing session data for user: "
                            + user.getLoggableUserId(), e);
                }
            } catch (UserIdNotFoundException e) {
                // If the user id is not available in the user object, or data is not sufficient to resolve it. Hence
                // the mapping is not stored.
                if (log.isDebugEnabled()) {
                    log.debug("A unique user id is not set for the user: " + user.getLoggableUserId()
                            + ". Hence the session information of the user is not stored.");
                }
            }
        }
        if (appId > 0) {
            storeAppSessionData(sessionContextKey, subject, appId, inboundAuth);
        }
    }

    /**
     * Method to store app session data. If an error occurs, it tries maximum times and throws an error.
     *
     * @param sessionContextKey Context of the authenticated session.
     * @param subject           Username in application
     * @param appId             ID of the application.
     * @param inboundAuth       Protocol used in app.
     * @throws UserSessionException If storing app session data fails.
     */
    private void storeAppSessionData(String sessionContextKey, String subject, int appId, String inboundAuth)
            throws UserSessionException {

        storeAppSessionData(sessionContextKey, subject, appId, inboundAuth, 0);
    }

    /**
     * Method to store app session data. If an error occurs, it tries maximum times and throws an error.
     *
     * @param sessionContextKey   Context of the authenticated session.
     * @param subject             Username in application
     * @param appId               ID of the application.
     * @param inboundAuth         Protocol used in app.
     * @param retryAttemptCounter The retry attempt number.
     * @throws UserSessionException If storing app session data fails.
     */
    private void storeAppSessionData(String sessionContextKey, String subject, int appId, String inboundAuth,
                                     int retryAttemptCounter) throws UserSessionException {

        try {
            UserSessionStore.getInstance().storeAppSessionData(sessionContextKey, subject, appId, inboundAuth);
        } catch (DataAccessException e) {
            if (retryAttemptCounter >= FrameworkConstants.MAX_RETRY_TIME) {
                throw new UserSessionException("Error while storing Application session data in the database for " +
                        "subject: " + subject + ", app Id: " + appId + ", protocol: " + inboundAuth + ".", e);
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error while storing Application session data in the database. Retrying to " +
                        "store the data for the %d time.", retryAttemptCounter + 1), e);
            }
            storeAppSessionData(sessionContextKey, subject, appId, inboundAuth,
                    retryAttemptCounter + 1);
        }
    }

    /**
     * Method to store session meta data.
     *
     * @param sessionId Id of the authenticated session
     * @param request   HttpServletRequest
     * @throws UserSessionException if storing session meta data fails
     */
    private void storeSessionMetaData(String sessionId, HttpServletRequest request)
            throws UserSessionException {
        String userAgent = request.getHeader(javax.ws.rs.core.HttpHeaders.USER_AGENT);
        String ip = IdentityUtil.getClientIpAddress(request);
        String time = Long.toString(System.currentTimeMillis());

        Map<String, String> metaDataMap = new HashMap<>();
        if (StringUtils.isNotEmpty(userAgent)) {
            metaDataMap.put(SessionMgtConstants.USER_AGENT, userAgent);
        }
        metaDataMap.put(SessionMgtConstants.IP_ADDRESS, ip);
        metaDataMap.put(SessionMgtConstants.LOGIN_TIME, time);
        metaDataMap.put(SessionMgtConstants.LAST_ACCESS_TIME, time);
        UserSessionStore.getInstance().storeSessionMetaData(sessionId, metaDataMap);
    }

    private String getApplicationTenantDomain(AuthenticationContext context) {

        return (StringUtils.isNotEmpty(context.getTenantDomain()) ?
                context.getTenantDomain() : MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

    }

    private void publishAuthenticationSuccess(HttpServletRequest request, AuthenticationContext context,
                                              AuthenticatedUser user) {

        Serializable authenticationStartTime =
                context.getAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_START_TIME);
        if (authenticationStartTime instanceof Long) {
            context.setAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_DURATION,
                    System.currentTimeMillis() - (long) authenticationStartTime);
        }
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
        String path = null;
        if (IdentityTenantUtil.isTenantedSessionsEnabled()) {
            if (FrameworkUtils.isOrganizationQualifiedRequest()) {
                // Handling the cookie path for requests coming with the path `/o/<org-id>`.
                String organizationId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId();
                path = FrameworkConstants.ORGANIZATION_CONTEXT_PREFIX + organizationId + "/";
            } else {
                if (!IdentityTenantUtil.isSuperTenantAppendInCookiePath() &&
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(context.getLoginTenantDomain())) {
                    path = "/";
                } else {
                    path = FrameworkConstants.TENANT_CONTEXT_PREFIX + context.getLoginTenantDomain() + "/";
                }
            }
        }
        FrameworkUtils.storeAuthCookie(request, response, sessionKey, authCookieAge, path);
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
                debugMessage.append(context.getSequenceConfig().getAuthenticatedUser()
                        .getAuthenticatedSubjectIdentifier()).append("\n");
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
        try {
            String queryParamsString = "";
            String redirectURL = FrameworkUtils.buildCallerPathRedirectURL(context.getCallerPath(), context);
            if (context.getCallerSessionKey() != null) {
                queryParamsString = FrameworkConstants.SESSION_DATA_KEY + "=" +
                        URLEncoder.encode(context.getCallerSessionKey(), "UTF-8");
            }

            if (StringUtils.isNotEmpty(rememberMeParam)) {
                queryParamsString += "&" + rememberMeParam;
            }
            redirectURL = FrameworkUtils.appendQueryParamsStringToUrl(redirectURL, queryParamsString);
            response.sendRedirect(redirectURL);
        } catch (IOException | URLBuilderException e) {
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

    private void setAuthenticatedIDPsOfApp(SessionContext sessionContext,
                                           Map<String, AuthenticatedIdPData> authenticatedIdPs,
                                           String applicationName) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Getting current authenticatedIDPs of the application from authentication context and setting "
                    + "it into session context for application: " + applicationName);
        }
        Map<String, AuthenticatedIdPData> authenticatedIdPDataMap = new HashMap<>();
        for (Map.Entry<String, AuthenticatedIdPData> entry : authenticatedIdPs.entrySet()) {
            try {
                AuthenticatedIdPData authenticatedIdpData = (AuthenticatedIdPData) entry.getValue().clone();
                authenticatedIdPDataMap.put(authenticatedIdpData.getIdpName(), authenticatedIdpData);
            } catch (CloneNotSupportedException e) {
                String errorMsg = "Error while cloning AuthenticatedIdPData object.";
                throw new FrameworkException(errorMsg, e);
            }
        }
        sessionContext.setAuthenticatedIdPsOfApp(applicationName, authenticatedIdPDataMap);
    }

    private void addAuditLogs(String sessionAction, AuthenticatedUser authenticatedUser, String sessionKey,
                              String traceId, Long lastAccessedTimestamp, boolean isRememberMe) {

        if (LoggerUtils.isEnableV2AuditLogs()) {
            return;
        }
        String userTenantDomain = authenticatedUser.getTenantDomain();
        boolean isFederated = authenticatedUser.isFederatedUser();
        String username = authenticatedUser.getUserName();
        if (!isFederated) {
            username = authenticatedUser.getUserStoreDomain() + UserCoreConstants.DOMAIN_SEPARATOR + username;
        }

        JSONObject auditData = new JSONObject();
        auditData.put(SessionMgtConstants.SESSION_CONTEXT_ID, sessionKey);
        auditData.put(SessionMgtConstants.REMEMBER_ME, isRememberMe);
        auditData.put(SessionMgtConstants.AUTHENTICATED_USER_TENANT_DOMAIN, userTenantDomain);
        auditData.put(SessionMgtConstants.TRACE_ID, traceId);

        String initiator = null;
        if (LoggerUtils.isLogMaskingEnable) {
            String maskedUsername = LoggerUtils.getMaskedContent(username);
            auditData.put(SessionMgtConstants.AUTHENTICATED_USER, maskedUsername);
            /* Not resolving the initiatorId for federated users since the subject returned from the external IDP
             * can be set to any identifier based on the IdP. Authenticated user's subject identifier is sent as the
             * username and initiatorId, and It could contain PII information. Therefore, the initiatorId will be set
             * as the masked username for federated users.
             */
            if (!isFederated && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(userTenantDomain)) {
                initiator = IdentityUtil.getInitiatorId(username, userTenantDomain);
            }
            if (StringUtils.isBlank(initiator)) {
                initiator = maskedUsername;
            }
        } else {
            auditData.put(SessionMgtConstants.AUTHENTICATED_USER, username);
            initiator = username;
        }
        /* When the action is StoreSession, the LastAccessedTimestamp means the session created timestamp. If the
         action is UpdateSession, the LastAccessedTimestamp means the session's last accessed timestamp. */
        auditData.put(SessionMgtConstants.SESSION_LAST_ACCESSED_TIMESTAMP, lastAccessedTimestamp);
        AUDIT_LOG.info(String.format(SessionMgtConstants.AUDIT_MESSAGE_TEMPLATE, initiator,
                sessionAction, auditData, SessionMgtConstants.SUCCESS));
    }

    /**
     * This method returns the list of federated authenticator names bounded to the
     * federated_tokens property in the authentication context.
     *
     * @param federatedTokens The list of federated tokens.
     * @return List of the federated authenticator names.
     */
    private List<String> getFederatedAuthenticatorName(List<FederatedToken> federatedTokens) {

        if (CollectionUtils.isEmpty(federatedTokens)) {
            return null;
        }
        return federatedTokens.stream().map(FederatedToken::getIdp).collect(Collectors.toList());
    }
}
