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
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;
import org.wso2.carbon.user.api.Tenant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Request Coordinator
 */
public class DefaultRequestCoordinator extends AbstractRequestCoordinator implements RequestCoordinator {

    private static final Log log = LogFactory.getLog(DefaultRequestCoordinator.class);
    private static volatile DefaultRequestCoordinator instance;
    private static final String ACR_VALUES_ATTRIBUTE = "acr_values";

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

        AuthenticationContext context = null;
        try {
            AuthenticationRequestCacheEntry authRequest = null;
            String sessionDataKey = request.getParameter("sessionDataKey");

            boolean returning = false;
            // Check whether this is the start of the authentication flow.
            // 'type' parameter should be present if so. This parameter contains
            // the request type (e.g. samlsso) set by the calling servlet.
            // TODO: use a different mechanism to determine the flow start.
            if (request.getParameter("type") != null) {
                // If the request is not a common auth logout request retrieve AuthenticationRequestCache entry,
                // which is stored stored from servlet against the session data key.
                if (!isCommonAuthLogoutRequest(request)) {

                    if (sessionDataKey != null) {

                        if (log.isDebugEnabled()) {
                            log.debug("retrieving authentication request from cache..");
                        }

                        authRequest = getAuthenticationRequest(request, sessionDataKey);

                        if (authRequest == null) {
                            // authRequest cannot be retrieved from cache. Cache
                            throw new FrameworkException(
                                    "Invalid authentication request. Session data key : " + sessionDataKey);
                        }
                    } else {
                        // sessionDataKey is null and not a logout request
                        if (log.isDebugEnabled()) {
                            log.debug("Session data key is null in the request and not a logout request.");
                        }
                        FrameworkUtils.sendToRetryPage(request, response);
                    }
                }

                // if there is a cache entry, wrap the original request with params in cache entry
                if (authRequest != null) {
                    request = FrameworkUtils.getCommonAuthReqWithParams(request, authRequest);
                    FrameworkUtils.removeAuthenticationRequestFromCache(sessionDataKey);
                }
                context = initializeFlow(request, response);
            } else {
                returning = true;
                context = FrameworkUtils.getContextData(request);
                associateTransientRequestData(request, response, context);
            }

            if (context != null) {
                context.setReturning(returning);

                // if this is the flow start, store the original request in the context
                if (!context.isReturning() && authRequest != null) {
                    context.setAuthenticationRequest(authRequest.getAuthenticationRequest());
                }

                if (!context.isLogoutRequest()) {
                    FrameworkUtils.getAuthenticationRequestHandler().handle(request, response, context);
                } else {
                    FrameworkUtils.getLogoutRequestHandler().handle(request, response, context);
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
                log.error("Context does not exist. Probably due to invalidated cache");
                FrameworkUtils.sendToRetryPage(request, response);
            }
        } catch (PostAuthenticationFailedException e) {
            if (log.isDebugEnabled()) {
                log.error("Error occurred while evaluating post authentication", e);
            }
            FrameworkUtils
                    .removeCookie(request, response, FrameworkUtils.getPASTRCookieName(context.getContextIdentifier()));
            publishAuthenticationFailure(request, context, context.getSequenceConfig().getAuthenticatedUser());
            try {
                URIBuilder uriBuilder = new URIBuilder(
                        ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL());
                uriBuilder.addParameter("status", "Authentication attempt failed.");
                uriBuilder.addParameter("statusMsg", e.getErrorCode());
                request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
                response.sendRedirect(uriBuilder.build().toString());
            } catch (URISyntaxException e1) {
                log.error("Error building redirect url for authz failure", e);
                FrameworkUtils.sendToRetryPage(request, response);
            }
        } catch (Throwable e) {
            log.error("Exception in Authentication Framework", e);
            FrameworkUtils.sendToRetryPage(request, response);
        }
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

        if(context == null) {
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

        // Store the request data sent by the caller
        AuthenticationContext context = new AuthenticationContext();
        context.setCallerSessionKey(callerSessionDataKey);
        context.setCallerPath(callerPath);
        context.setRequestType(requestType);
        context.setRelyingParty(relyingParty);
        context.setTenantDomain(tenantDomain);

        // generate a new key to hold the context data object
        String contextId = UUIDGenerator.generateUUID();
        context.setContextIdentifier(contextId);

        if (log.isDebugEnabled()) {
            log.debug("Framework contextId: " + contextId);
        }

        // if this a logout request from the calling servlet
        if (request.getParameter(FrameworkConstants.RequestParams.LOGOUT) != null) {

            if (log.isDebugEnabled()) {
                log.debug("Starting a logout flow");
            }

            context.setLogoutRequest(true);

            if (context.getRelyingParty() == null || context.getRelyingParty().trim().length() == 0) {

                if (log.isDebugEnabled()) {
                    log.debug("relyingParty param is null. This is a possible logout scenario.");
                }

                Cookie cookie = FrameworkUtils.getAuthCookie(request);

                if (cookie != null) {
                    String sessionContextKey = DigestUtils.sha256Hex(cookie.getValue());
                    context.setSessionIdentifier(sessionContextKey);
                }

                return context;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Starting an authentication flow");
            }
        }

        associateTransientRequestData(request, response, context);
        findPreviousAuthenticatedSession(request, context);
        buildOutboundQueryString(request, context);

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

        String tenantDomain = request.getParameter(FrameworkConstants.RequestParams.TENANT_DOMAIN);

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

    protected void findPreviousAuthenticatedSession(HttpServletRequest request, AuthenticationContext context)
            throws FrameworkException {

        List<String> acrRequested = getAcrRequested(request);
        if (acrRequested != null) {
            for (String acr : acrRequested) {
                context.addRequestedAcr(acr);
            }
        }
        // Get service provider chain
        SequenceConfig effectiveSequence = getSequenceConfig(context, request.getParameterMap());

        if (acrRequested != null) {
            for (String acr : acrRequested) {
                effectiveSequence.addRequestedAcr(acr);
            }
        }

        Cookie cookie = FrameworkUtils.getAuthCookie(request);

        // if cookie exists user has previously authenticated
        if (cookie != null) {

            if (log.isDebugEnabled()) {
                log.debug(FrameworkConstants.COMMONAUTH_COOKIE + " cookie is available with the value: " + cookie
                        .getValue());
            }

            String sessionContextKey = DigestUtils.sha256Hex(cookie.getValue());

            // get the authentication details from the cache
            SessionContext sessionContext = FrameworkUtils.getSessionContextFromCache(sessionContextKey);

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

                    if (!isReinitialize(previousAuthenticatedSeq, effectiveSequence, request, context)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Previous Sequence should be used without change");
                        }
                        try {
                            effectiveSequence = (SequenceConfig) previousAuthenticatedSeq.clone();
                        } catch (CloneNotSupportedException e) {
                            throw new FrameworkException("Exception when trying to clone the Previous Authentication "
                                    + "Sequence object of SP:" + appName, e);
                        }
                    }

                    AuthenticatedUser authenticatedUser = previousAuthenticatedSeq.getAuthenticatedUser();

                    if (authenticatedUser != null) {
                        String authenticatedUserTenantDomain = authenticatedUser.getTenantDomain();
                        // set the user for the current authentication/logout flow
                        context.setSubject(authenticatedUser);

                        if (log.isDebugEnabled()) {
                            log.debug("Already authenticated by username: " + authenticatedUser
                                    .getAuthenticatedSubjectIdentifier());
                        }

                        if (authenticatedUserTenantDomain != null) {
                            // set the user tenant domain for the current authentication/logout flow
                            context.setProperty("user-tenant-domain", authenticatedUserTenantDomain);

                            if (log.isDebugEnabled()) {
                                log.debug("Authenticated user tenant domain: " + authenticatedUserTenantDomain);
                            }
                        }
                    }
                    // This is done to reflect the changes done in SP to the sequence config. So, the requested claim updates,
                    // authentication step updates will be reflected.
                    refreshAppConfig(effectiveSequence, request.getParameter(FrameworkConstants.RequestParams.ISSUER),
                            context.getRequestType(), context.getTenantDomain());
                }

                context.setPreviousAuthenticatedIdPs(sessionContext.getAuthenticatedIdPs());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to find the SessionContext from the cache. Possible cache timeout.");
                }
            }
        }

        context.setServiceProviderName(effectiveSequence.getApplicationConfig().getApplicationName());

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
                    .append(URLEncoder.encode(context.getServiceProviderName(), "UTF-8")).append("&isSaaSApp=")
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
            ApplicationConfig appConfig = new ApplicationConfig(serviceProvider);
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
            AuthenticatedUser user) {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();

        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            authnDataPublisherProxy.publishAuthenticationFailure(request, context, unmodifiableParamMap);
        }
    }
}
