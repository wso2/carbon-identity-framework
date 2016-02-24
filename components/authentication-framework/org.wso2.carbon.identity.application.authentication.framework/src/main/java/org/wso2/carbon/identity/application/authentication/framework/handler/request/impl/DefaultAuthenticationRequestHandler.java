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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultAuthenticationRequestHandler implements AuthenticationRequestHandler {

    private static final Log log = LogFactory.getLog(DefaultAuthenticationRequestHandler.class);
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
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

        // if SP has request path authenticators configured and this is start of
        // the flow
        if (reqPathAuthenticators != null && !reqPathAuthenticators.isEmpty() && currentStep == 0) {
            // call request path sequence handler
            FrameworkUtils.getRequestPathBasedSequenceHandler().handle(request, response, context);
        }

        // if no request path authenticators or handler returned cannot handle
        if (!context.getSequenceConfig().isCompleted()
            || (reqPathAuthenticators == null || reqPathAuthenticators.isEmpty())) {
            // call step based sequence handler
            FrameworkUtils.getStepBasedSequenceHandler().handle(request, response, context);
        }

        // if flow completed, send response back
        if (context.getSequenceConfig().isCompleted()) {
            concludeFlow(request, response, context);
        } else { // redirecting outside
            FrameworkUtils.addAuthenticationContextToCache(context.getContextIdentifier(), context);
        }
    }

    private void handleDenyFromLoginPage(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("User has pressed Deny or Cancel in the login page. Terminating the authentication flow");
        }

        context.getSequenceConfig().setCompleted(true);
        context.setRequestAuthenticated(false);
        concludeFlow(request, response, context);
    }

    private void handleRememberMeOptionFromLoginPage(HttpServletRequest request, AuthenticationContext context) {
        String rememberMe = request.getParameter("chkRemember");

        if (rememberMe != null && "on".equalsIgnoreCase(rememberMe)) {
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
    protected boolean handleSequenceStart(HttpServletRequest request, HttpServletResponse response,
                                          AuthenticationContext context) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Starting the sequence");
        }

        // "forceAuthenticate" - go in the full authentication flow even if user
        // is already logged in.
        boolean forceAuthenticate = request
                                            .getParameter(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE) != null ? Boolean
                                            .valueOf(request.getParameter(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE))
                                                                                                                       : false;

        context.setForceAuthenticate(forceAuthenticate);

        if (log.isDebugEnabled()) {
            log.debug("Force Authenticate : " + forceAuthenticate);
        }

        // "reAuthenticate" - authenticate again with the same IdPs as before.
        boolean reAuthenticate = request
                                         .getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE) != null ? Boolean
                                         .valueOf(request.getParameter(FrameworkConstants.RequestParams.RE_AUTHENTICATE))
                                                                                                                 : false;

        if (log.isDebugEnabled()) {
            log.debug("Re-Authenticate : " + reAuthenticate);
        }

        context.setReAuthenticate(reAuthenticate);

        // "checkAuthentication" - passive mode. just send back whether user is
        // *already* authenticated or not.
        boolean passiveAuthenticate = request
                                              .getParameter(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION) != null ? Boolean
                                              .valueOf(request
                                                               .getParameter(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION))
                                                                                                                             : false;

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
            if (FrameworkUtils.getAuthCookie(request) != null) {
                commonAuthCookie = FrameworkUtils.getAuthCookie(request).getValue();
                if (commonAuthCookie != null) {
                    sessionContext = FrameworkUtils.getSessionContextFromCache(commonAuthCookie);
                }
            }

            // session context may be null when cache expires therefore creating new cookie as well.
            if (sessionContext != null) {
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(),
                                                               sequenceConfig);
                sessionContext.getAuthenticatedIdPs().putAll(context.getCurrentAuthenticatedIdPs());
                // TODO add to cache?
                // store again. when replicate  cache is used. this may be needed.
                FrameworkUtils.addSessionContextToCache(commonAuthCookie, sessionContext);
            } else {
                sessionContext = new SessionContext();
                sessionContext.getAuthenticatedSequences().put(appConfig.getApplicationName(),
                                                               sequenceConfig);
                sessionContext.setAuthenticatedIdPs(context.getCurrentAuthenticatedIdPs());
                sessionContext.setRememberMe(context.isRememberMe());
                String sessionKey = UUIDGenerator.generateUUID();
                FrameworkUtils.addSessionContextToCache(sessionKey, sessionContext);

                setAuthCookie(request, response, context, sessionKey, authenticatedUserTenantDomain);
            }

            if (authenticatedUserTenantDomain == null) {
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }

            String auditData = "\"" + "ContextIdentifier" + "\" : \"" + context.getContextIdentifier()
                               + "\",\"" + "AuthenticatedUser" + "\" : \"" + sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier()
                               + "\",\"" + "AuthenticatedUserTenantDomain" + "\" : \"" + authenticatedUserTenantDomain
                               + "\",\"" + "ServiceProviderName" + "\" : \"" + context.getServiceProviderName()
                               + "\",\"" + "RequestType" + "\" : \"" + context.getRequestType()
                               + "\",\"" + "RelyingParty" + "\" : \"" + context.getRelyingParty()
                               + "\",\"" + "AuthenticatedIdPs" + "\" : \"" + sequenceConfig.getAuthenticatedIdPs()
                               + "\"";

            AUDIT_LOG.info(String.format(
                    FrameworkConstants.AUDIT_MESSAGE,
                    sequenceConfig.getAuthenticatedUser().getAuthenticatedSubjectIdentifier(),
                    "Login",
                    "ApplicationAuthenticationFramework", auditData, FrameworkConstants.AUDIT_SUCCESS));
        }

        // Checking weather inbound protocol is an already cache removed one, request come from federated or other
        // authenticator in multi steps scenario. Ex. Fido
        if (FrameworkUtils.getCacheDisabledAuthenticators().contains(context.getRequestType())
                && (response instanceof CommonAuthResponseWrapper)) {
            //Set the result as request attribute
            request.setAttribute("sessionDataKey", context.getCallerSessionKey());
            addAuthenticationResultToRequest(request, authenticationResult);
        }else{
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
     * Add authentication request as request attribute
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
            rememberMeParam = rememberMeParam + "&chkRemember=on";
        }

        // redirect to the caller
        String redirectURL = context.getCallerPath() + "?sessionDataKey="
                + context.getCallerSessionKey() + rememberMeParam;
        try {
            response.sendRedirect(redirectURL);
        } catch (IOException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
    }
}
