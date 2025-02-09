/*
 * Copyright (c) 2013-2024, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationFlowHandler;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.exception.CookieValidationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.ErrorToI18nCodeTranslator;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.I18nErrorCodeWrapper;
import org.wso2.carbon.identity.application.authentication.framework.exception.JsFailureException;
import org.wso2.carbon.identity.application.authentication.framework.exception.MisconfigurationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.LoginContextManagementUtil;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ACCOUNT_DISABLED_CLAIM_URI;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ACCOUNT_LOCKED_CLAIM_URI;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ACCOUNT_UNLOCK_TIME_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AnalyticsAttributes.SESSION_ID;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.BACK_TO_FIRST_STEP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_DESCRIPTION_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ERROR_STATUS_APP_DISABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.IS_API_BASED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ORGANIZATION_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ORGANIZATION_LOGIN_HOME_REALM_IDENTIFIER;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REDIRECT_URL;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REQUEST_PARAM_SP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.AUTH_TYPE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.IDENTIFIER_CONSENT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.IDF;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.RESTART_FLOW;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TYPE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ResidentIdpPropertyName.ACCOUNT_DISABLE_HANDLER_ENABLE_PROPERTY;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USER_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.SessionNonceCookieUtil.NONCE_ERROR_CODE;

/**
 * Request Coordinator
 */
public class DefaultRequestCoordinator extends AbstractRequestCoordinator implements RequestCoordinator {

    private static final Log log = LogFactory.getLog(DefaultRequestCoordinator.class);
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String TRUE = "true";
    private static volatile DefaultRequestCoordinator instance;
    private static final String ACR_VALUES_ATTRIBUTE = "acr_values";
    private static final String REQUESTED_ATTRIBUTES = "requested_attributes";
    private static final String SERVICE_PROVIDER_QUERY_KEY = "serviceProvider";

    public static DefaultRequestCoordinator getInstance() {

        if (instance == null) {
            synchronized (DefaultRequestCoordinator.class) {
                if (instance == null) {
                    instance = new DefaultRequestCoordinator();
                }
            }
        }

        return instance;
    }

    /**
     * Get authentication request cache entry
     *
     * @param request Http servlet request
     * @return Authentication request cache entry
     */
    private AuthenticationRequestCacheEntry getAuthenticationRequestFromRequest(HttpServletRequest request) {

        return (AuthenticationRequestCacheEntry) request.getAttribute(FrameworkConstants.RequestAttribute.AUTH_REQUEST);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {

        CommonAuthResponseWrapper responseWrapper = null;
        if (response instanceof CommonAuthResponseWrapper) {
            responseWrapper = (CommonAuthResponseWrapper) response;
        } else {
            responseWrapper = new CommonAuthResponseWrapper(response);
            responseWrapper.setWrappedByFramework(true);
        }
        AuthenticationContext context = null;
        String sessionDataKey = request.getParameter("sessionDataKey");
        try {
            IdentityUtil.threadLocalProperties.get().put(FrameworkConstants.AUTHENTICATION_FRAMEWORK_FLOW, true);
            AuthenticationRequestCacheEntry authRequest = null;
            boolean returning = false;
            // Check whether this is the start of the authentication flow.
            // 'type' parameter should be present if so. This parameter contains
            // the request type (e.g. samlsso) set by the calling servlet.
            // TODO: use a different mechanism to determine the flow start.
            if (request.getParameter("type") != null) {
                // Retrieves AuthenticationRequestCache entry, if the request contains a valid session data key and
                // handles common auth logout request.
                if (sessionDataKey != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Retrieving authentication request from cache for the sessionDataKey: " +
                                sessionDataKey);
                    }

                    authRequest = getAuthenticationRequest(request, sessionDataKey);
                    if (authRequest == null) {
                        // authRequest is not retrieved from the cache.
                        if (log.isDebugEnabled()) {
                            log.debug("No authentication request found in the cache for sessionDataKey: "
                                    + sessionDataKey);
                        }

                        if (isCommonAuthLogoutRequest(request)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Ignoring the invalid sessionDataKey: " + sessionDataKey + " in the " +
                                        "CommonAuthLogout request.");
                            }

                        } else {
                            throw new FrameworkException("Invalid authentication request with sessionDataKey: "
                                    + sessionDataKey);
                        }
                    }

                } else if (!isCommonAuthLogoutRequest(request)) {
                    // sessionDataKey is null and not a common auth logout request
                    if (log.isDebugEnabled()) {
                        log.debug("Session data key is null in the request and not a logout request.");
                    }
                    if (LoggerUtils.isDiagnosticLogsEnabled()) {
                        LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                                FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                                FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST)
                                .resultMessage(FrameworkConstants.SESSION_DATA_KEY + " is not provided in the " +
                                        "request. Sending to retry page.")
                                .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                                .resultStatus(DiagnosticLog.ResultStatus.FAILED));
                    }

                    FrameworkUtils.sendToRetryPage(request, response, context);
                }

                // if there is a cache entry, wrap the original request with params in cache entry
                if (authRequest != null) {
                    request = FrameworkUtils.getCommonAuthReqWithParams(request, authRequest);
                }
                context = initializeFlow(request, responseWrapper);

                // Set the authentication request before the context is cloned.
                if (authRequest != null) {
                    context.setAuthenticationRequest(authRequest.getAuthenticationRequest());
                }

                // We'll use the cloned context to re-initiate the login flow when the session
                // nonce cookie validation failed.
                if (request.getParameter(FrameworkConstants.RequestParams.LOGOUT) == null) {
                    context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());
                }

                context.initializeAnalyticsData();

            } else {
                context = FrameworkUtils.getContextData(request);
                if (request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW) != null &&
                        request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW).equals("true")) {
                    context = (AuthenticationContext) context.getProperty(FrameworkConstants.INITIAL_CONTEXT);
                    context.setProperty(FrameworkConstants.INITIAL_CONTEXT, context.clone());
                    context.initializeAnalyticsData();
                    String contextIdIncludedQueryParams = context.getContextIdIncludedQueryParams();
                    contextIdIncludedQueryParams += FrameworkConstants.RESTART_LOGIN_FLOW_QUERY_PARAMS;
                    context.setContextIdIncludedQueryParams(contextIdIncludedQueryParams);
                } else {
                    returning = true;
                }
                associateTransientRequestData(request, responseWrapper, context);
            }

            // Check if the application is enabled.
            if (context != null && !isApplicationEnabled(request, context)) {
                FrameworkUtils.sendToRetryPage(request, responseWrapper, null, ERROR_STATUS_APP_DISABLED,
                        ERROR_DESCRIPTION_APP_DISABLED);
                return;
            }

            if (context != null) {
                // Adding the context identifier(sessionDataKey) to the request to be used when the context
                // is not available.
                request.setAttribute(FrameworkConstants.CONTEXT_IDENTIFIER, context.getContextIdentifier());
                if (StringUtils.isNotBlank(context.getServiceProviderName())) {
                    MDC.put(SERVICE_PROVIDER_QUERY_KEY, context.getServiceProviderName());
                }
                // Remove `login.reinitiate.message` from the query params after the first step to prevent it from
                // appearing in subsequent authentication steps.
                if (context.getCurrentStep() == 1 && StringUtils.contains(context.getContextIdIncludedQueryParams(),
                        FrameworkConstants.RESTART_LOGIN_FLOW_QUERY_PARAMS)) {
                    String contextIdIncludedQueryParams = StringUtils.remove(context.getContextIdIncludedQueryParams(),
                            FrameworkConstants.RESTART_LOGIN_FLOW_QUERY_PARAMS);
                    context.setContextIdIncludedQueryParams(contextIdIncludedQueryParams);
                }
                // Monitor should be context itself as we need to synchronize only if the same context is used by two
                // different threads.
                synchronized (context) {
                    if (!context.isActiveInAThread()) {
                        // Marks this context is active in a thread. We only allow at a single instance, a context
                        // to be active in only a single thread. In other words, same context cannot active in two
                        // different threads at the same time.
                        context.setActiveInAThread(true);
                        if (log.isDebugEnabled()) {
                            log.debug("Context id: " + context.getContextIdentifier() + " is active in the thread " +
                                    "with id: " + Thread.currentThread().getId());
                        }
                    } else {
                        log.error("Same context is currently in used by a different thread. Possible double submit.");
                        if (log.isDebugEnabled()) {
                            log.debug("Same context is currently in used by a different thread. Possible double submit."
                                    +  "\n" +
                                    "Context id: " + context.getContextIdentifier() + "\n" +
                                    "Originating address: " + request.getRemoteAddr() + "\n" +
                                    "Request Headers: " + getHeaderString(request) + "\n" +
                                    "Thread Id: " + Thread.currentThread().getId());
                        }
                        if (LoggerUtils.isDiagnosticLogsEnabled()) {
                            LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                                    FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                                    FrameworkConstants.LogConstants.ActionIDs.HANDLE_AUTH_REQUEST)
                                    .inputParam(FrameworkConstants.LogConstants.CONTEXT_ID,
                                            context.getContextIdentifier())
                                    .inputParam(FrameworkConstants.LogConstants.ORIGINATING_ADDRESS,
                                            request.getRemoteAddr())
                                    .inputParam(FrameworkConstants.LogConstants.REQUEST_HEADERS,
                                            getHeaderString(request))
                                    .inputParam(FrameworkConstants.LogConstants.THREAD_ID,
                                            Thread.currentThread().getId())
                                    .resultMessage("Same authentication request is being processed by another " +
                                            "thread. Could be a possible double submit or a replay request.")
                                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                                    .resultStatus(DiagnosticLog.ResultStatus.FAILED));
                        }
                        FrameworkUtils.sendToRetryPage(request, responseWrapper, context);
                        return;
                    }
                }

                // If the Authentication context expiry validation is enabled, check the context for expiry time.
                // If the context is expired, return to retry page.
                if (FrameworkUtils.isAuthenticationContextExpiryEnabled() &&
                        (FrameworkUtils.getCurrentStandardNano() > context.getExpiryTime())) {
                    log.error("Redirecting to retry page as the authentication context has expired.");
                    FrameworkUtils.sendToRetryPage(request, responseWrapper, context,
                            FrameworkConstants.ERROR_STATUS_AUTH_FLOW_TIMEOUT,
                            FrameworkConstants.ERROR_DESCRIPTION_AUTH_FLOW_TIMEOUT);
                    return;
                }

                // Check if the Identity Framework (IDF) is initiated from authenticators like SMS OTP or Email OTP,
                // which have two procedures: 1. Identify the User, and 2. Perform authentication.
                // After the first procedure, the context's current authenticator is set to the corresponding
                // authenticator.
                // Users have the option to restart the flow from the 1st step by choosing a different option.
                // In such cases, if it's the first step in the authentication process, we need to remove
                // the context's current authenticator to reset the flow.
                // Reference: https://github.com/wso2/product-is/issues/18655
                if (FrameworkUtils.isIdfInitiatedFromAuthenticator(context)
                        && isStepHasMultiOption(context)
                        && context.getCurrentStep() == 1
                        && !isIdentifierFirstRequest(request)) {

                    // Reset the current authenticator in the context since it's the first step in the authentication.
                    context.setCurrentAuthenticator(null);
                }

                /*
                If
                 Request specify to restart the flow again from first step by passing `restart_flow`.
                 OR
                 Identifier first request received and current step does not contains any flow handler.
                    (To handle browser back with only with identifier-first and basic)
                */
                if (isBackToFirstStepRequest(request) || (isIdentifierFirstRequest(request)
                        && (!isFlowHandlerInCurrentStepCanHandleRequest(context, request)
                        && !FrameworkUtils.isIdfInitiatedFromAuthenticator(context)))) {
                    if (isCompletedStepsAreFlowHandlersOnly(context)) {
                        // If the incoming request is restart and all the completed steps have only flow handlers as the
                        // authenticated authenticator, then we reset the current step to 1.
                        if (log.isDebugEnabled()) {
                            log.debug("Restarting the authentication flow from step 1 for  " +
                                    context.getContextIdentifier());
                        }
                        context.setCurrentStep(0);
                        context.setProperty(BACK_TO_FIRST_STEP, true);
                        Map<String, String> runtimeParams =
                                context.getAuthenticatorParams(FrameworkConstants.JSAttributes.JS_COMMON_OPTIONS);
                        if (MapUtils.isNotEmpty(runtimeParams)) {
                            runtimeParams.put(FrameworkConstants.JSAttributes.JS_OPTIONS_USERNAME, null);
                        }
                        FrameworkUtils.resetAuthenticationContext(context);
                        returning = false;

                        // IDF should be the first step.
                        context.getCurrentAuthenticatedIdPs().clear();
                    } else {
                        // If the incoming request is restart and the completed steps have authenticators as the
                        // authenticated authenticator, then we redirect to retry page.
                        String msg = "Restarting the authentication flow failed because there is/are authenticator/s " +
                                "available in the completed steps for  " + context.getContextIdentifier();
                        if (log.isDebugEnabled()) {
                            log.debug(msg);
                        }
                        throw new MisconfigurationException(msg);
                    }
                }

                setSPAttributeToRequest(request, context);
                context.setReturning(returning);

                if (!context.isLogoutRequest()) {
                    FrameworkUtils.getAuthenticationRequestHandler().handle(request, responseWrapper, context);

                    // Adding spId param to the redirect URL if it is not an external system call.
                    boolean isExternalCall = Boolean.TRUE.equals(
                            request.getAttribute(FrameworkConstants.IS_EXTERNAL_CALL));
                    if (!isExternalCall) {
                        addServiceProviderIdToRedirectUrl(responseWrapper, context);
                    }
                } else {
                    FrameworkUtils.getLogoutRequestHandler().handle(request, responseWrapper, context);
                }
            } else {
                if (log.isDebugEnabled()) {
                    String key = request.getParameter("sessionDataKey");
                    if (key == null) {
                        log.debug("Session data key is null in the request");
                    } else {
                        log.debug("Session data key  :  " + key);
                    }
                }

                String userAgent = request.getHeader("User-Agent");
                String referer = request.getHeader("Referer");

                String message = "Requested client: " + request.getRemoteAddr() + ", URI :" + request.getMethod() +
                        ":" + request.getRequestURI() + ", User-Agent: " + userAgent + " , Referer: " + referer;

                log.warn("Context does not exist. Probably due to invalidated cache. " + message);
                FrameworkUtils.sendToRetryPage(request, responseWrapper, context,
                        FrameworkConstants.ERROR_STATUS_AUTH_CONTEXT_NULL,
                        FrameworkConstants.ERROR_DESCRIPTION_AUTH_CONTEXT_NULL);
            }
        } catch (JsFailureException e) {
            if (log.isDebugEnabled()) {
                log.debug("Script initiated Exception occured.", e);
            }
            publishAuthenticationFailure(request, context, context.getSequenceConfig().getAuthenticatedUser(),
                    e.getErrorCode());
            if (log.isDebugEnabled()) {
                log.debug("User will be redirected to retry page or the error page provided by script.");
            }
        } catch (MisconfigurationException e) {
            FrameworkUtils.sendToRetryPage(request, responseWrapper, context, "misconfiguration.error",
                    "something.went.wrong.contact.admin");
        } catch (PostAuthenticationFailedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while evaluating post authentication", e);
            }
            I18nErrorCodeWrapper errorWrapper = ErrorToI18nCodeTranslator.translate(e.getErrorCode());
            FrameworkUtils.removeCookie(request, responseWrapper,
                    FrameworkUtils.getPASTRCookieName(context.getContextIdentifier()));
            log.error(String.format("Error occurred while evaluating post authentication : %s : %s.",
                    errorWrapper.getStatusMsg(), e.getMessage()));
            publishAuthenticationFailure(request, context, context.getSequenceConfig().getAuthenticatedUser(),
                    e.getErrorCode());
            FrameworkUtils.sendToRetryPage(request, responseWrapper, context, errorWrapper.getStatus(),
                    errorWrapper.getStatusMsg());
        } catch (Exception e) {
            if ((e instanceof FrameworkException)
                    && (NONCE_ERROR_CODE.equals(((FrameworkException) e).getErrorCode()))) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage(), e);
                }
                request.setAttribute(FrameworkConstants.RESTART_LOGIN_FLOW, "true");
                throw new CookieValidationFailedException(NONCE_ERROR_CODE, "Session nonce cookie value is not " +
                        "matching " +
                        "for session with sessionDataKey: " + request.getParameter("sessionDataKey"));
            } else {
                log.error("Exception in Authentication Framework", e);
                FrameworkUtils.sendToRetryPage(request, responseWrapper, context);
            }
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(FrameworkConstants.AUTHENTICATION_FRAMEWORK_FLOW);
            UserCoreUtil.setDomainInThreadLocal(null);
            if (request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW) == null ||
                    request.getAttribute(FrameworkConstants.RESTART_LOGIN_FLOW).equals("false")) {
                unwrapResponse(responseWrapper, sessionDataKey, response, context);
            }

            if (context != null) {
                // Mark this context left the thread. Now another thread can use this context.
                context.setActiveInAThread(false);
                if (log.isDebugEnabled()) {
                    log.debug("Context id: " + context.getContextIdentifier() + " left the thread with id: " +
                            Thread.currentThread().getId());
                }
                // If flow is not about to conclude.
                if (!LoginContextManagementUtil.isPostAuthenticationExtensionCompleted(context) ||
                        context.isLogoutRequest()) {
                    // Persist the context.
                    FrameworkUtils.addAuthenticationContextToCache(context.getContextIdentifier(), context);
                    if (log.isDebugEnabled()) {
                        log.debug("Context with id: " + context.getContextIdentifier() + " added to the cache.");
                    }
                }
                // Clear the auto login related cookies only during none passive authentication flow.
                if (!context.isPassiveAuthenticate()) {
                    FrameworkUtils.removeALORCookie(request, response);
                }
            }
        }
    }

    private boolean isApplicationEnabled(HttpServletRequest request, AuthenticationContext context)
            throws FrameworkException {

        String type = request.getParameter(TYPE);
        String relyingParty = request.getParameter(FrameworkConstants.RequestParams.ISSUER);
        if (StringUtils.isBlank(type)) {
            type = context.getRequestType();
        }
        if (StringUtils.isBlank(relyingParty)) {
            relyingParty = context.getRelyingParty();
        }
        ServiceProvider serviceProvider = getServiceProvider(type, relyingParty, getTenantDomain(request));
        if (serviceProvider == null) {
            throw new FrameworkException("Unable to retrieve service provider for relying party : " + relyingParty);
        }
        if (!serviceProvider.isApplicationEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Application is disabled for the service provider with relying party: " + relyingParty);
            }
            return false;
        }
        return true;
    }

    protected void unwrapResponse(CommonAuthResponseWrapper responseWrapper, String sessionDataKey,
                                  HttpServletResponse response, AuthenticationContext context) throws IOException {

        if (responseWrapper.isRedirect()) {
            String redirectURL;
            if (context != null) {
                redirectURL = FrameworkUtils.getRedirectURLWithFilteredParams(responseWrapper.getRedirectURL(),
                        context);
                context.setRedirectURL(redirectURL);
            } else {
                log.warn("Authentication context is null, redirect parameter filtering will not be done for " +
                        sessionDataKey);
                redirectURL = responseWrapper.getRedirectURL();
            }
            if (responseWrapper.isWrappedByFramework()) {
                response.sendRedirect(redirectURL);
            } else {
                responseWrapper.sendRedirect(redirectURL);
            }
        } else if (responseWrapper.isWrappedByFramework()) {
            responseWrapper.write();
        }
    }

    private boolean isIdentifierFirstRequest(HttpServletRequest request) {

        String authType = request.getParameter(AUTH_TYPE);
        return IDF.equals(authType) || request.getParameter(IDENTIFIER_CONSENT) != null;
    }

    private boolean isFlowHandlerInCurrentStepCanHandleRequest(AuthenticationContext context,
                                                               HttpServletRequest request) {

        StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep());
        if (stepConfig != null) {
            List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
            for (AuthenticatorConfig config : authenticatorList) {
                if (config.getApplicationAuthenticator() instanceof AuthenticationFlowHandler &&
                        config.getApplicationAuthenticator().canHandle(request)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBackToFirstStepRequest(HttpServletRequest request) {

        String authType = request.getParameter(RESTART_FLOW);
        return Boolean.parseBoolean(authType);
    }

    private boolean isCompletedStepsAreFlowHandlersOnly(AuthenticationContext context) {

        Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
        for (int i = context.getCurrentStep() - 1; i >= 0; i--) {
            StepConfig stepConfig = stepMap.get(i);
            if (stepConfig != null) {
                AuthenticatorConfig authenticatedAuthenticator = stepConfig.getAuthenticatedAutenticator();
                if (!(authenticatedAuthenticator.getApplicationAuthenticator() instanceof AuthenticationFlowHandler)) {
                    return false;

                }
            }
        }
        return true;
    }

    /**
     * Print the request headers as a one string with header names and respective values.
     * @param request HTTP request to retrieve headers.
     * @return Headers and values as a single string.
     */
    private String getHeaderString(HttpServletRequest request) {

        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder stringBuilder = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            stringBuilder.append("Header Name: ").append(headerName).append(", ")
                    .append("Value: ").append(request.getHeader(headerName)).append(". ");
        }
        return stringBuilder.toString();
    }

    /**
     * Associates the transient request data to the Authentication Context.
     *
     * @param request
     * @param response
     * @param context
     */
    private void associateTransientRequestData(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context) {

        if (context == null) {
            return;
        }
        // set current request and response to the authentication context.
        context.setProperty(FrameworkConstants.RequestAttribute.HTTP_REQUEST, new TransientObjectWrapper(request));
        context.setProperty(FrameworkConstants.RequestAttribute.HTTP_RESPONSE, new TransientObjectWrapper(response));
    }

    /**
     * Returns true if the request is a CommonAuth logout request.
     * @param request
     * @return
     */
    private boolean isCommonAuthLogoutRequest(HttpServletRequest request) {

        return Boolean.parseBoolean(request.getParameter(FrameworkConstants.LOGOUT));
    }

    /**
     * When cache removed authentication request stored as request attribute, then taking request from request or
     * otherwise getting authentication request from cache
     *
     * @param request
     * @param sessionDataKey
     * @return
     */
    private AuthenticationRequestCacheEntry getAuthenticationRequest(HttpServletRequest request,
            String sessionDataKey) {

        AuthenticationRequestCacheEntry authRequest = getAuthenticationRequestFromRequest(request);
        if (authRequest == null) {
            authRequest = FrameworkUtils.getAuthenticationRequestFromCache(sessionDataKey);
            if (authRequest != null) {
                FrameworkUtils.removeAuthenticationRequestFromCache(sessionDataKey);
            }
        }
        return authRequest;
    }

    /**
     * Handles the initial request (from the calling servlet)
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     * @throws
     */
    protected AuthenticationContext initializeFlow(HttpServletRequest request, HttpServletResponse response)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Initializing the flow");
        }

        // "sessionDataKey" - calling servlet maintains its state information
        // using this
        String callerSessionDataKey = request.getParameter(FrameworkConstants.SESSION_DATA_KEY);

        // "commonAuthCallerPath" - path of the calling servlet. This is the url
        // response should be sent to
        String callerPath = getCallerPath(request);

        // "type" - type of the request. e.g. samlsso, openid, oauth, passivests
        String requestType = request.getParameter(FrameworkConstants.RequestParams.TYPE);

        // "relyingParty"
        String relyingParty = request.getParameter(FrameworkConstants.RequestParams.ISSUER);

        // tenant domain
        String tenantDomain = getTenantDomain(request);

        String loginDomain = request.getParameter(FrameworkConstants.RequestParams.LOGIN_TENANT_DOMAIN);
        String userDomain = request.getParameter(FrameworkConstants.RequestParams.USER_TENANT_DOMAIN_HINT);

        // Store the request data sent by the caller
        AuthenticationContext context = new AuthenticationContext();
        context.setCallerSessionKey(callerSessionDataKey);
        context.setRequestType(requestType);
        context.setRelyingParty(relyingParty);
        context.setTenantDomain(tenantDomain);
        context.setLoginTenantDomain(loginDomain);
        context.setUserTenantDomainHint(userDomain);
        context.setExpiryTime(FrameworkUtils.getCurrentStandardNano() + TimeUnit.MINUTES.toNanos(
                IdentityUtil.getAuthenticationContextValidityPeriod()));

        if (IdentityTenantUtil.isTenantedSessionsEnabled()) {
            String loginTenantDomain = context.getLoginTenantDomain();
            try {
                if (!callerPath.startsWith(FrameworkConstants.TENANT_CONTEXT_PREFIX + loginTenantDomain + "/") &&
                        !callerPath.startsWith(FrameworkConstants.ORGANIZATION_CONTEXT_PREFIX + loginTenantDomain + "/")
                        && FrameworkUtils.isURLRelative(callerPath)) {
                    callerPath = FrameworkConstants.TENANT_CONTEXT_PREFIX + loginTenantDomain + callerPath;
                }
            } catch (URISyntaxException e) {
                throw new FrameworkException(e.getMessage(), e);
            }
        }
        context.setCallerPath(callerPath);

        // generate a new key to hold the context data object
        String contextId = UUID.randomUUID().toString();
        context.setContextIdentifier(contextId);

        if (log.isDebugEnabled()) {
            log.debug("Framework contextId: " + contextId);
        }

        // if this a logout request from the calling servlet
        if (request.getParameter(FrameworkConstants.RequestParams.LOGOUT) != null) {

            if (log.isDebugEnabled()) {
                log.debug("Starting a logout flow");
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.INIT_LOGOUT_FLOW)
                        .inputParam(LogConstants.InputKeys.APPLICATION_NAME, context.getServiceProviderName())
                        .inputParam(FrameworkConstants.RequestParams.CALLER_PATH, callerPath)
                        .inputParam(FrameworkConstants.LogConstants.TENANT_DOMAIN, tenantDomain)
                        .resultMessage("Initializing logout flow.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
            }

            context.setLogoutRequest(true);

            /*
             * Set special property fedIdpId used to remove the entry in fed_auth_session_mapping table
             * in the special case of SAML SLO when same fed idp is configured twice.
             */

            if (StringUtils.isNotBlank(request.getParameter(FrameworkConstants.FED_IDP_ID))) {
                context.setProperty(FrameworkConstants.FED_IDP_ID, request.getParameter(FrameworkConstants.FED_IDP_ID));
            }

            if (context.getRelyingParty() == null || context.getRelyingParty().trim().length() == 0) {

                if (log.isDebugEnabled()) {
                    log.debug("relyingParty param is null. This is a possible logout scenario.");
                }

                Cookie cookie = FrameworkUtils.getAuthCookie(request);

                String sessionContextKey = null;
                if (cookie != null) {
                    sessionContextKey = DigestUtils.sha256Hex(cookie.getValue());
                } else {
                    sessionContextKey = request.getParameter(SESSION_ID);
                }
                context.setSessionIdentifier(sessionContextKey);
                return context;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Starting an authentication flow");
            }
            if (LoggerUtils.isDiagnosticLogsEnabled()) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.INIT_AUTH_FLOW)
                        .inputParam(LogConstants.InputKeys.APPLICATION_NAME, context.getServiceProviderName())
                        .inputParam(LogConstants.InputKeys.CLIENT_ID, relyingParty)
                        .inputParam(LogConstants.InputKeys.CALLER_PATH, callerPath)
                        .inputParam(FrameworkConstants.LogConstants.TENANT_DOMAIN, tenantDomain)
                        .inputParam(FrameworkConstants.SESSION_DATA_KEY, callerSessionDataKey)
                        .resultMessage("Initializing authentication flow.")
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
            }
        }

        List<ClaimMapping> requestedClaimsInRequest = (List<ClaimMapping>) request.getAttribute(REQUESTED_ATTRIBUTES);
        context.setProperty(FrameworkConstants.SP_REQUESTED_CLAIMS_IN_REQUEST, requestedClaimsInRequest);

        associateTransientRequestData(request, response, context);
        findPreviousAuthenticatedSession(request, context);
        buildOutboundQueryString(request, context);

        String redirectUrl = request.getParameter(REDIRECT_URI);
        if (FrameworkUtils.isAPIBasedAuthenticationFlow(request)) {
            context.setProperty(REDIRECT_URL, redirectUrl);
            context.setProperty(IS_API_BASED, TRUE);
        }

        return context;
    }

    /**
     * Sets the requested ACR values to the context if available.
     *
     * @param request
     */
    private List<String> getAcrRequested(HttpServletRequest request) {

        List<String> acrValuesList = (List<String>) request.getAttribute(ACR_VALUES_ATTRIBUTE);

        if (acrValuesList == null) {
            acrValuesList = Collections.emptyList();
        }
        return acrValuesList;
    }

    private String getCallerPath(HttpServletRequest request) throws FrameworkException {

        String callerPath = request.getParameter(FrameworkConstants.RequestParams.CALLER_PATH);
        try {
            if (callerPath != null) {
                callerPath = URLDecoder.decode(callerPath, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {
            throw new FrameworkException(e.getMessage(), e);
        }
        return callerPath;
    }

    private String getTenantDomain(HttpServletRequest request) throws FrameworkException {

        String tenantDomain = getTenantDomainFromContext();
        if (StringUtils.isNotBlank(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain resolved from the thread local context: " + tenantDomain);
            }
        } else {
            // Fall back to the tenant domain in the request param.
            tenantDomain = request.getParameter(FrameworkConstants.RequestParams.TENANT_DOMAIN);
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain resolved from request parameter: " + tenantDomain);
            }
        }

        if (tenantDomain == null || tenantDomain.isEmpty() || "null".equals(tenantDomain)) {

            String tenantId = request.getParameter(FrameworkConstants.RequestParams.TENANT_ID);

            if (tenantId != null && !"-1234".equals(tenantId)) {
                try {
                    Tenant tenant = FrameworkServiceComponent.getRealmService().getTenantManager()
                            .getTenant(Integer.parseInt(tenantId));
                    if (tenant != null) {
                        tenantDomain = tenant.getDomain();
                    }
                } catch (Exception e) {
                    throw new FrameworkException(e.getMessage(), e);
                }
            } else {
                tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
        }
        return tenantDomain;
    }

    private String getTenantDomainFromContext() {

        // We use the tenant domain set in context only in tenant qualified URL mode.
        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            return IdentityTenantUtil.getTenantDomainFromContext();
        }

        return null;
    }

    protected void findPreviousAuthenticatedSession(HttpServletRequest request, AuthenticationContext context)
            throws FrameworkException {

        List<String> acrRequested = getAcrRequested(request);
        if (acrRequested != null) {
            for (String acr : acrRequested) {
                context.addRequestedAcr(acr);
            }
            if (LoggerUtils.isDiagnosticLogsEnabled() && CollectionUtils.isNotEmpty(acrRequested)) {
                LoggerUtils.triggerDiagnosticLogEvent(new DiagnosticLog.DiagnosticLogBuilder(
                        FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                        FrameworkConstants.LogConstants.ActionIDs.PROCESS_ACR_VALUES)
                        .inputParam(LogConstants.InputKeys.APPLICATION_NAME, context.getServiceProviderName())
                        .inputParam(LogConstants.InputKeys.TENANT_DOMAIN, context.getLoginTenantDomain())
                        .inputParam(ACR_VALUES_ATTRIBUTE, acrRequested)
                        .logDetailLevel(DiagnosticLog.LogDetailLevel.INTERNAL_SYSTEM)
                        .resultStatus(DiagnosticLog.ResultStatus.SUCCESS));
            }
        }
        // Get service provider chain
        SequenceConfig effectiveSequence = getSequenceConfig(context, request.getParameterMap());
        String applicationName = effectiveSequence.getApplicationConfig().getApplicationName();
        // organization SSO IDP is added for portal apps only if requested with FIDP param.
        if (FrameworkConstants.Application.CONSOLE_APP.equals(applicationName) ||
                FrameworkConstants.Application.MY_ACCOUNT_APP.equals(applicationName)) {
            String[] fidpParam = request.getParameterMap().get(FrameworkConstants.RequestParams.FEDERATED_IDP);
            if (fidpParam == null || fidpParam.length > 0 &&
                    !ORGANIZATION_LOGIN_HOME_REALM_IDENTIFIER.equals(fidpParam[0])) {
                removeOrganizationSsoStepsForPortalApps(effectiveSequence);
            }
        }

        if (acrRequested != null) {
            for (String acr : acrRequested) {
                effectiveSequence.addRequestedAcr(acr);
            }
        }

        String sessionContextKey = null;
        Cookie cookie = FrameworkUtils.getAuthCookie(request);
        if (cookie != null) {
            if (log.isDebugEnabled()) {
                log.debug(FrameworkConstants.COMMONAUTH_COOKIE + " cookie is available with the value: " + cookie
                        .getValue());
            }
            sessionContextKey = DigestUtils.sha256Hex(cookie.getValue());
        } else if (FrameworkUtils.isAPIBasedAuthenticationFlow(request)) {
            /* If it's an API based authentication flow, the sha256 hashed value
             of the session identifier can be passed as a query param as well.*/
            String hashedSessionId = request.getParameter(FrameworkConstants.RequestParams.SESSION_ID);
            if (StringUtils.isNotBlank(hashedSessionId)) {
                if (log.isDebugEnabled()) {
                    log.debug(FrameworkConstants.RequestParams.SESSION_ID +
                            " query param is available with the value: " + hashedSessionId);
                }
                sessionContextKey = hashedSessionId;
            }
        }
        // if a value for the sessionContextKey exists user has previously authenticated
        if (sessionContextKey != null) {

            SessionContext sessionContext = null;
            // get the authentication details from the cache
            try {
                //Starting tenant-flow as tenant domain is retrieved downstream from the carbon-context to get the
                // tenant wise session expiry time
                FrameworkUtils.startTenantFlow(context.getTenantDomain());
                sessionContext = FrameworkUtils.getSessionContextFromCache(request, context, sessionContextKey);
            } finally {
                FrameworkUtils.endTenantFlow();
            }

            if (sessionContext != null) {

                context.setSessionIdentifier(sessionContextKey);
                String appName = effectiveSequence.getApplicationConfig().getApplicationName();

                if (log.isDebugEnabled()) {
                    log.debug("Service Provider is: " + appName);
                }

                SequenceConfig previousAuthenticatedSeq = sessionContext.getAuthenticatedSequences().get(appName);

                if (previousAuthenticatedSeq != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("A previously authenticated sequence found for the SP: " + appName);
                    }

                    context.setPreviousSessionFound(true);

                    effectiveSequence.setStepMap(new HashMap<>(previousAuthenticatedSeq.getStepMap()));
                    effectiveSequence.setReqPathAuthenticators(
                            new ArrayList<>(previousAuthenticatedSeq.getReqPathAuthenticators()));
                    effectiveSequence.setAuthenticatedUser(previousAuthenticatedSeq.getAuthenticatedUser());
                    effectiveSequence.setAuthenticatedIdPs(previousAuthenticatedSeq.getAuthenticatedIdPs());
                    effectiveSequence.setAuthenticatedReqPathAuthenticator(
                            previousAuthenticatedSeq.getAuthenticatedReqPathAuthenticator());

                    AuthenticatedUser authenticatedUser = previousAuthenticatedSeq.getAuthenticatedUser();

                    if (authenticatedUser != null) {
                        String authenticatedUserTenantDomain = authenticatedUser.getTenantDomain();
                        // Set the user for the current authentication/logout flow.
                        context.setSubject(authenticatedUser);

                        if (log.isDebugEnabled()) {
                            log.debug("Already authenticated by username: " +
                                    authenticatedUser.getAuthenticatedSubjectIdentifier());
                        }

                        if (authenticatedUserTenantDomain != null) {
                            // Set the user tenant domain for the current authentication/logout flow.
                            context.setProperty(USER_TENANT_DOMAIN, authenticatedUserTenantDomain);
                            if (log.isDebugEnabled()) {
                                log.debug("Authenticated user tenant domain: " + authenticatedUserTenantDomain);
                            }
                        }
                    }
                    // This is done to reflect the changes done in SP to the sequence config. So, the requested claim
                    // updates, authentication step updates will be reflected.
                    refreshAppConfig(effectiveSequence, request.getParameter(FrameworkConstants.RequestParams.ISSUER),
                            context.getRequestType(), context.getTenantDomain());
                    context.setAuthenticatedIdPsOfApp(sessionContext.getAuthenticatedIdPsOfApp(appName));
                }

                context.setPreviousAuthenticatedIdPs(sessionContext.getAuthenticatedIdPs());
                context.setProperty(FrameworkConstants.RUNTIME_CLAIMS,
                        sessionContext.getProperty(FrameworkConstants.RUNTIME_CLAIMS));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to find the SessionContext from the cache. Possible cache timeout.");
                }
            }
        }

        context.setServiceProviderName(effectiveSequence.getApplicationConfig().getApplicationName());
        if (effectiveSequence.getApplicationConfig().getServiceProvider() != null && StringUtils.isNotBlank(
                effectiveSequence.getApplicationConfig().getServiceProvider().getApplicationResourceId())) {
            context.setServiceProviderResourceId(
                    effectiveSequence.getApplicationConfig().getServiceProvider().getApplicationResourceId());
        }

        // set the sequence for the current authentication/logout flow
        context.setSequenceConfig(effectiveSequence);
    }

    /**
     * Checks whether the sequence needs re-initializing, when there is an existing user session.
     *
     * @param previousAuthenticatedSeq The previous(last) sequence-config used to authenticate.
     * @param sequenceConfig           Current sequence config, which is the candiate to be used on authentication.
     * @param request                  Incoming HTTP request.
     * @param context                  Current authentication Context.
     * @return true if there is a need to reinitialize.
     */
    private boolean isReinitialize(SequenceConfig previousAuthenticatedSeq, SequenceConfig sequenceConfig,
            HttpServletRequest request, AuthenticationContext context) {

        List<String> newAcrList = getAcrRequested(request);
        List<String> previousAcrList = previousAuthenticatedSeq.getRequestedAcr();
        if (newAcrList != null && !newAcrList.isEmpty() && isDifferent(newAcrList, previousAcrList)) {
            return true;
        }

        return false;
    }

    private boolean isDifferent(List<String> newAcrList, List<String> previousAcrList) {

        if (previousAcrList == null || previousAcrList.size() != newAcrList.size()) {
            return true;
        }
        for (int i = 0; i < previousAcrList.size(); i++) {
            if (!newAcrList.get(i).equals(previousAcrList.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void buildOutboundQueryString(HttpServletRequest request, AuthenticationContext context)
            throws FrameworkException {

        // Build the outbound query string that will be sent to the authentication endpoint and
        // federated IdPs
        StringBuilder outboundQueryStringBuilder = new StringBuilder();
        outboundQueryStringBuilder.append(FrameworkUtils.getQueryStringWithConfiguredParams(request));

        if (StringUtils.isNotEmpty(outboundQueryStringBuilder.toString())) {
            outboundQueryStringBuilder.append("&");
        }

        try {
            outboundQueryStringBuilder.append("sessionDataKey=").append(context.getContextIdentifier())
                    .append("&relyingParty=").append(URLEncoder.encode(context.getRelyingParty(), "UTF-8"))
                    .append("&type=").append(context.getRequestType()).append("&")
                    .append(FrameworkConstants.REQUEST_PARAM_SP).append("=")
                    .append(URLEncoder.encode(context.getServiceProviderName(), "UTF-8")).append("&");
            if (context.getServiceProviderResourceId() != null) {
                outboundQueryStringBuilder.append(FrameworkConstants.REQUEST_PARAM_SP_UUID).append("=")
                        .append(URLEncoder.encode(context.getServiceProviderResourceId(), "UTF-8")).append("&");
            }
            outboundQueryStringBuilder.append("&isSaaSApp=")
                    .append(context.getSequenceConfig().getApplicationConfig().isSaaSApp());
        } catch (UnsupportedEncodingException e) {
            throw new FrameworkException("Error while URL Encoding", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Outbound Query String: " + outboundQueryStringBuilder.toString());
        }

        context.setContextIdIncludedQueryParams(outboundQueryStringBuilder.toString());
        context.setOrignalRequestQueryParams(outboundQueryStringBuilder.toString());
    }

    private void refreshAppConfig(SequenceConfig sequenceConfig, String clientId, String clientType,
            String tenantDomain) throws FrameworkException {

        try {
            ServiceProvider serviceProvider = getServiceProvider(clientType, clientId, tenantDomain);
            ApplicationConfig appConfig = new ApplicationConfig(serviceProvider, tenantDomain);
            sequenceConfig.setApplicationConfig(appConfig);
            if (log.isDebugEnabled()) {
                log.debug("Refresh application config in sequence config for application id: " + sequenceConfig
                        .getApplicationId() + " in tenant: " + tenantDomain);
            }
        } catch (FrameworkException e) {
            String message =
                    "No application found for application id: " + sequenceConfig.getApplicationId() + " in tenant: "
                            + tenantDomain + " Probably, the Service Provider would have been removed.";
            throw new FrameworkException(message, e);
        }
    }

    private void publishAuthenticationFailure(HttpServletRequest request, AuthenticationContext context,
                                              AuthenticatedUser user, String errorCode) {

        Serializable authenticationStartTime =
                context.getAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_START_TIME);
        if (authenticationStartTime instanceof Long) {
            context.setAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_DURATION,
                    System.currentTimeMillis() - (long) authenticationStartTime);
        }
        context.setAnalyticsData(FrameworkConstants.AnalyticsData.AUTHENTICATION_ERROR_CODE, errorCode);
        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();

        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            authnDataPublisherProxy.publishAuthenticationFailure(request, context, unmodifiableParamMap);
        }
    }

    private void setSPAttributeToRequest(HttpServletRequest req, AuthenticationContext context) {

        req.setAttribute(REQUEST_PARAM_SP, context.getServiceProviderName());
        req.setAttribute(TENANT_DOMAIN, context.getTenantDomain());
    }

    /**
     * Checks whether AuthenticatedUser object contains a valid user for authentication.
     * Returns false if user verification is failed.
     *
     * @param user
     * @return boolean
     * @throws FrameworkException
     */
    private boolean isUserAllowedToLogin(AuthenticatedUser user) {

        if (user.isFederatedUser()) {
            return true;
        }

        int tenantId = IdentityTenantUtil.getTenantId(user.getTenantDomain());
        try {
            UserRealm userRealm = (UserRealm) FrameworkServiceComponent.getRealmService().
                    getTenantUserRealm(tenantId);
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();

            if (userStoreManager.isExistingUserWithID(user.getUserId())) {
                return !(isUserDisabled(userStoreManager, user) || isUserLocked(userStoreManager, user));
            } else {
                log.error("Trying to authenticate non existing user: " + user.getLoggableUserId());
            }
        } catch (UserStoreException e) {
            log.error("Error while checking existence of user: " + user.getLoggableUserId(), e);
        } catch (FrameworkException e) {
            log.error("Error while validating user: " + user.getLoggableUserId(), e);
        } catch (UserIdNotFoundException e) {
            log.error("User id is not available for user: " + user.getLoggableUserId(), e);
        }
        return false;
    }

    /**
     * Checks whether the given user is locked and returns true for locked users
     *
     * @param userStoreManager
     * @param user
     * @return boolean
     * @throws FrameworkException
     */
    private boolean isUserLocked(AbstractUserStoreManager userStoreManager, AuthenticatedUser user)
            throws FrameworkException, UserIdNotFoundException {

        String accountLockedClaimValue = getClaimValue(user.getUserId(), userStoreManager, ACCOUNT_LOCKED_CLAIM_URI);
        boolean accountLocked = Boolean.parseBoolean(accountLockedClaimValue);

        if (accountLocked) {
            long unlockTime = 0;
            String accountUnlockTimeClaimValue = getClaimValue(
                    user.getUserId(), userStoreManager, ACCOUNT_UNLOCK_TIME_CLAIM);

            if (NumberUtils.isNumber(accountUnlockTimeClaimValue)) {
                unlockTime = Long.parseLong(accountUnlockTimeClaimValue);
            }

            if (unlockTime != 0 && System.currentTimeMillis() >= unlockTime) {
                return false;
            }
        }
        return accountLocked;
    }

    /**
     * Checks whether the given user is disabled and returns true for disabled users
     * @param userStoreManager
     * @param user
     * @return boolean
     * @throws FrameworkException
     */
    private boolean isUserDisabled(AbstractUserStoreManager userStoreManager, AuthenticatedUser user)
            throws FrameworkException, UserIdNotFoundException {

        if (!isAccountDisablingEnabled(user.getTenantDomain())) {
            return false;
        }

        String accountDisabledClaimValue = getClaimValue(
                user.getUserId(), userStoreManager, ACCOUNT_DISABLED_CLAIM_URI);
        return Boolean.parseBoolean(accountDisabledClaimValue);
    }

    private boolean isAccountDisablingEnabled(String tenantDomain) throws FrameworkException {

        Property accountDisableConfigProperty = FrameworkUtils.getResidentIdpConfiguration(
                ACCOUNT_DISABLE_HANDLER_ENABLE_PROPERTY, tenantDomain);

        return accountDisableConfigProperty != null && Boolean.parseBoolean(accountDisableConfigProperty.getValue());
    }

    /**
     * This method retrieves requested claim value from the user store
     *
     * @param userId
     * @param userStoreManager
     * @param claimURI
     * @return claim value as a String
     * @throws FrameworkException
     */
    private String getClaimValue(String userId, AbstractUserStoreManager userStoreManager, String claimURI) throws
            FrameworkException {

        try {
            Map<String, String> values = userStoreManager.getUserClaimValuesWithID(userId, new String[]{claimURI},
                    UserCoreConstants.DEFAULT_PROFILE);
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s claim value of user %s is set to: " + values.get(claimURI),
                        claimURI, userId));
            }
            return values.get(claimURI);

        } catch (UserStoreException e) {
            throw new FrameworkException("Error occurred while retrieving claim: " + claimURI, e);
        }
    }

    /**
     * The organization SSO IDP is added for Console & MyAccount to provide sub-organization access. But the
     * organization SSO IDP should be hidden from the login sequence unless the organization SSO option is requested via
     * the fidp parameter.
     *
     * @param effectiveSequence The effective sequences.
     */
    private void removeOrganizationSsoStepsForPortalApps(SequenceConfig effectiveSequence) {

        Map<Integer, StepConfig> stepMap = effectiveSequence.getStepMap();
        if (MapUtils.isEmpty(stepMap)) {
            stepMap = effectiveSequence.getAuthenticationGraph().getStepMap();
        }
        StepConfig stepConfig = stepMap.get(1);
        if (stepConfig == null) {
            return;
        }
        // Remove the organization SSO authenticator from the step configs.
        List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
        authenticatorList = authenticatorList.stream()
                .filter(authenticatorConfig -> !authenticatorConfig.getName()
                        .equals(ORGANIZATION_AUTHENTICATOR)).collect(Collectors.toList());
        stepConfig.setAuthenticatorList(authenticatorList);

        // Remove the organization SSO authenticator from the graph nodes if already configured.
        AuthenticationGraph authenticationGraph = effectiveSequence.getAuthenticationGraph();
        if (authenticationGraph == null) {
            return;
        }
        AuthGraphNode authGraphNode = authenticationGraph.getStartNode();
        if (authGraphNode == null) {
            return;
        }
        if (authGraphNode instanceof StepConfigGraphNode) {
            StepConfigGraphNode graphNode = (StepConfigGraphNode) authGraphNode;
            if (graphNode.getStepConfig() == null ||
                    CollectionUtils.isEmpty(graphNode.getStepConfig().getAuthenticatorList())) {
                return;
            }
            authenticatorList = graphNode.getStepConfig().getAuthenticatorList();
            authenticatorList = authenticatorList.stream()
                    .filter(authenticatorConfig -> !authenticatorConfig.getName()
                            .equals(ORGANIZATION_AUTHENTICATOR)).collect(Collectors.toList());
            graphNode.getStepConfig().setAuthenticatorList(authenticatorList);
        }
    }

    private boolean isStepHasMultiOption(AuthenticationContext context) {

        SequenceConfig sequenceConfig = context.getSequenceConfig();

        if (sequenceConfig != null) {
            Map<Integer, StepConfig> stepMap = sequenceConfig.getStepMap();

            if (stepMap != null) {
                StepConfig stepConfig = stepMap.get(context.getCurrentStep());

                return stepConfig != null && stepConfig.isMultiOption();
            }
        }
        return false;
    }

    private void addServiceProviderIdToRedirectUrl(CommonAuthResponseWrapper responseWrapper,
                                                   AuthenticationContext context) {

        if (responseWrapper == null || context == null) {
            return;
        }
        try {
            String redirectURL = responseWrapper.getRedirectURL();
            String serviceProviderID = context.getServiceProviderResourceId();
            if (StringUtils.isNotBlank(redirectURL) && StringUtils.isNotBlank(serviceProviderID)) {
                URI uri = new URI(redirectURL);
                String query = uri.getRawQuery();
                if (StringUtils.isNotBlank(query)) {
                    if (!query.contains(FrameworkConstants.REQUEST_PARAM_SP_UUID + "=")) {
                        redirectURL = redirectURL + "&" + FrameworkConstants.REQUEST_PARAM_SP_UUID
                                + "=" + URLEncoder.encode(serviceProviderID,
                                StandardCharsets.UTF_8.name());
                    }
                } else {
                    redirectURL = redirectURL + "?" + FrameworkConstants.REQUEST_PARAM_SP_UUID
                            + "=" + URLEncoder.encode(serviceProviderID, StandardCharsets.UTF_8.name());
                }
                responseWrapper.sendRedirect(redirectURL);
            }
        } catch (URISyntaxException | IOException e) {
            // No need to break the flow due to this error since added spId to redirect URL is used only
            // for branding purposes.
            log.debug("Error while adding spId to redirect URL.");
        }
    }
}
