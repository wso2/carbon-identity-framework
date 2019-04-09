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

package org.wso2.carbon.identity.application.authentication.framework.util;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.HomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.impl.DefaultHomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl.DefaultProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.LogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultAuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultLogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultRequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.RequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultRequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.GraphBasedStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationFrameworkWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.model.CookieBuilder;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.USER_SESSION_MAPPING_ENABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REQUEST_PARAM_SP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.TENANT_DOMAIN;

public class FrameworkUtils {

    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String UTF_8 = "UTF-8";
    private static final Log log = LogFactory.getLog(FrameworkUtils.class);
    private static int maxInactiveInterval;
    private static final String EMAIL = "email";
    private static List<String> cacheDisabledAuthenticators = Arrays
            .asList(FrameworkConstants.RequestType.CLAIM_TYPE_SAML_SSO, FrameworkConstants.OAUTH2);

    private static final String QUERY_SEPARATOR = "&";
    private static final String EQUAL = "=";
    private static final String REQUEST_PARAM_APPLICATION = "application";
    private static final String ALREADY_WRITTEN_PROPERTY = "AlreadyWritten";


    private FrameworkUtils() {
    }

    public static List<String> getCacheDisabledAuthenticators() {
        return cacheDisabledAuthenticators;
    }

    /**
     * To add authentication request cache entry to cache
     *
     * @param key          cache entry key
     * @param authReqEntry AuthenticationReqCache Entry.
     */
    public static void addAuthenticationRequestToCache(String key, AuthenticationRequestCacheEntry authReqEntry) {
        AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
        AuthenticationRequestCache.getInstance().addToCache(cacheKey, authReqEntry);
    }

    /**
     * To get authentication cache request from cache
     *
     * @param key Key of the cache entry
     * @return
     */
    public static AuthenticationRequestCacheEntry getAuthenticationRequestFromCache(String key) {

        AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
        AuthenticationRequestCacheEntry authRequest = AuthenticationRequestCache.getInstance().getValueFromCache(cacheKey);
        return authRequest;
    }

    /**
     * removes authentication request from cache.
     *
     * @param key SessionDataKey
     */
    public static void removeAuthenticationRequestFromCache(String key) {

        if (key != null) {
            AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
            AuthenticationRequestCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * Builds the wrapper, wrapping incoming request and information take from cache entry
     *
     * @param request    Original request coming to authentication framework
     * @param cacheEntry Cache entry from the cache, which is added from calling servlets
     * @return
     */
    public static HttpServletRequest getCommonAuthReqWithParams(HttpServletRequest request,
                                                                AuthenticationRequestCacheEntry cacheEntry) {

        // add this functionality as a constructor
        Map<String, String[]> modifiableParameters = new TreeMap<String, String[]>();
        if (cacheEntry != null) {
            AuthenticationRequest authenticationRequest = cacheEntry.getAuthenticationRequest();

            if (!authenticationRequest.getRequestQueryParams().isEmpty()) {
                modifiableParameters.putAll(authenticationRequest.getRequestQueryParams());
            }

            // Adding field variables to wrapper
            if (authenticationRequest.getType() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.TYPE,
                                         new String[]{authenticationRequest.getType()});
            }
            if (authenticationRequest.getCommonAuthCallerPath() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.CALLER_PATH,
                                         new String[]{authenticationRequest.getCommonAuthCallerPath()});
            }
            if (authenticationRequest.getRelyingParty() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.ISSUER,
                                         new String[]{authenticationRequest.getRelyingParty()});
            }
            if (authenticationRequest.getTenantDomain() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.TENANT_DOMAIN,
                                         new String[]{authenticationRequest.getTenantDomain()});
            }
            modifiableParameters.put(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE,
                                     new String[]{String.valueOf(authenticationRequest.getForceAuth())});
            modifiableParameters.put(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION,
                                     new String[]{String.valueOf(authenticationRequest.getPassiveAuth())});

            if (log.isDebugEnabled()) {
                StringBuilder queryStringBuilder = new StringBuilder("");

                for (Map.Entry<String, String[]> entry : modifiableParameters.entrySet()) {
                    StringBuilder paramValueBuilder = new StringBuilder("");
                    String[] paramValueArr = entry.getValue();

                    if (paramValueArr != null) {
                        for (String paramValue : paramValueArr) {
                            paramValueBuilder.append("{").append(paramValue).append("}");
                        }
                    }

                    queryStringBuilder.append("\n").append(
                            entry.getKey() + "=" + paramValueBuilder.toString());
                }

                log.debug("\nInbound Request parameters: " + queryStringBuilder.toString());
            }

            return new AuthenticationFrameworkWrapper(request, modifiableParameters,
                                                      authenticationRequest.getRequestHeaders());
        }
        return request;
    }

    /**
     * @param name
     * @return
     */
    public static ApplicationAuthenticator getAppAuthenticatorByName(String name) {

        for (ApplicationAuthenticator authenticator : FrameworkServiceComponent.getAuthenticators()) {

            if (name.equals(authenticator.getName())) {
                return authenticator;
            }
        }

        return null;
    }

    /**
     * @param request
     * @return
     */
    public static AuthenticationContext getContextData(HttpServletRequest request) {

        AuthenticationContext context = null;
        if (request.getParameter("promptResp") != null && request.getParameter("promptId") != null) {
            String promptId = request.getParameter("promptId");
            context = FrameworkUtils.getAuthenticationContextFromCache(promptId);
            if (context != null) {
                FrameworkUtils.removeAuthenticationContextFromCache(promptId);
                return context;
            }
        }
        for (ApplicationAuthenticator authenticator : FrameworkServiceComponent.getAuthenticators()) {
            try {
                String contextIdentifier = authenticator.getContextIdentifier(request);

                if (contextIdentifier != null && !contextIdentifier.isEmpty()) {
                    context = FrameworkUtils.getAuthenticationContextFromCache(contextIdentifier);
                    if (context != null) {
                        break;
                    }
                }
            } catch (UnsupportedOperationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignore UnsupportedOperationException.", e);
                }
                continue;
            }
        }

        return context;
    }

    public static RequestCoordinator getRequestCoordinator() {

        RequestCoordinator requestCoordinator = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR);

        if (obj instanceof RequestCoordinator) {
            requestCoordinator = (RequestCoordinator) obj;
        } else {
            requestCoordinator = DefaultRequestCoordinator.getInstance();
        }

        return requestCoordinator;
    }

    /**
     * @return
     */
    public static AuthenticationRequestHandler getAuthenticationRequestHandler() {

        AuthenticationRequestHandler authenticationRequestHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER);

        if (obj instanceof AuthenticationRequestHandler) {
            authenticationRequestHandler = (AuthenticationRequestHandler) obj;
        } else {
            authenticationRequestHandler = DefaultAuthenticationRequestHandler.getInstance();
        }

        return authenticationRequestHandler;
    }

    /**
     * @return
     */
    public static LogoutRequestHandler getLogoutRequestHandler() {

        LogoutRequestHandler logoutRequestHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER);

        if (obj instanceof LogoutRequestHandler) {
            logoutRequestHandler = (LogoutRequestHandler) obj;
        } else {
            logoutRequestHandler = DefaultLogoutRequestHandler.getInstance();
        }

        return logoutRequestHandler;
    }

    /**
     * Returns the step based sequence handler.
     * @return
     */
    public static StepBasedSequenceHandler getStepBasedSequenceHandler() {

        StepBasedSequenceHandler stepBasedSequenceHandler;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER);
        if (obj instanceof StepBasedSequenceHandler) {
            stepBasedSequenceHandler = (StepBasedSequenceHandler) obj;
        } else {
            stepBasedSequenceHandler = new GraphBasedSequenceHandler();
        }
        return stepBasedSequenceHandler;
    }

    /**
     * @return
     */
    public static RequestPathBasedSequenceHandler getRequestPathBasedSequenceHandler() {

        RequestPathBasedSequenceHandler reqPathBasedSeqHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER);

        if (obj instanceof RequestPathBasedSequenceHandler) {
            reqPathBasedSeqHandler = (RequestPathBasedSequenceHandler) obj;
        } else {
            reqPathBasedSeqHandler = DefaultRequestPathBasedSequenceHandler.getInstance();
        }

        return reqPathBasedSeqHandler;
    }

    /**
     * @return
     */
    public static StepHandler getStepHandler() {

        StepHandler stepHandler;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER);
        if (obj instanceof StepHandler) {
            stepHandler = (StepHandler) obj;
        } else {
            stepHandler = new GraphBasedStepHandler();
        }
        return stepHandler;
    }

    /**
     * @return
     */
    public static HomeRealmDiscoverer getHomeRealmDiscoverer() {

        HomeRealmDiscoverer homeRealmDiscoverer = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_HRD);

        if (obj instanceof HomeRealmDiscoverer) {
            homeRealmDiscoverer = (HomeRealmDiscoverer) obj;
        } else {
            homeRealmDiscoverer = DefaultHomeRealmDiscoverer.getInstance();
        }

        return homeRealmDiscoverer;
    }

    /**
     * @return
     */
    public static ClaimHandler getClaimHandler() {

        ClaimHandler claimHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER);

        if (obj instanceof ClaimHandler) {
            claimHandler = (ClaimHandler) obj;
        } else {
            claimHandler = DefaultClaimHandler.getInstance();
        }

        return claimHandler;
    }

    /**
     * @return
     */
    public static ProvisioningHandler getProvisioningHandler() {

        ProvisioningHandler provisioningHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER);

        if (obj instanceof ProvisioningHandler) {
            provisioningHandler = (ProvisioningHandler) obj;
        } else {
            provisioningHandler = DefaultProvisioningHandler.getInstance();
        }

        return provisioningHandler;
    }

    /**
     * @param request
     * @param response
     * @throws IOException
     */
    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // TODO read the URL from framework config file rather than carbon.xml
        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
        response.sendRedirect(getRedirectURL(ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL(),
                request));
    }


    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response, String status,
                                       String statusMsg) throws IOException {

        try {
            URIBuilder uriBuilder = new URIBuilder(
                    ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL());
            uriBuilder.addParameter("status", status);
            uriBuilder.addParameter("statusMsg", statusMsg);
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
            response.sendRedirect(getRedirectURL(uriBuilder.build().toString(), request));
        } catch (URISyntaxException e) {
            log.error("Error building redirect url for failure", e);
            FrameworkUtils.sendToRetryPage(request, response);
        }
    }
    /**
     * This method is used to append sp name and sp tenant domain as parameter to a given url. Those information will
     * be fetched from request parameters or referer.
     *
     * @param redirectURL Redirect URL.
     * @param request     HttpServlet Request.
     * @return sp information appended redirect URL.
     */
    public static String getRedirectURL(String redirectURL, HttpServletRequest request) {

        String spName = (String) request.getAttribute(REQUEST_PARAM_SP);
        String tenantDomain = (String) request.getAttribute(TENANT_DOMAIN);
        if (StringUtils.isBlank(spName)) {
            spName = getServiceProviderNameByReferer(request);
        }

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = getTenantDomainByReferer(request);
        }

        try {
            if (StringUtils.isNotBlank(spName)) {
                redirectURL = appendUri(redirectURL, REQUEST_PARAM_SP, spName);
            }

            if (StringUtils.isNotBlank(tenantDomain)) {
                redirectURL = appendUri(redirectURL, TENANT_DOMAIN, tenantDomain);
            }
        } catch (UnsupportedEncodingException e) {
            log.debug("Error occurred while encoding parameters: " + tenantDomain + " and/or " + spName, e);
            return redirectURL;
        }

        return redirectURL;
    }

    private static String getServiceProviderNameByReferer(HttpServletRequest request) {

        String serviceProviderName = null;
        String refererHeader = request.getHeader("referer");
        if (StringUtils.isNotBlank(refererHeader)) {
            String[] queryParams = refererHeader.split(QUERY_SEPARATOR);
            for (String queryParam : queryParams) {
                if (queryParam.contains(REQUEST_PARAM_SP + EQUAL) || queryParam.contains(REQUEST_PARAM_APPLICATION +
                        EQUAL)) {
                    serviceProviderName = queryParam.substring(queryParam.lastIndexOf(EQUAL) + 1);
                    break;
                }
            }
        }

        return serviceProviderName;
    }

    private static String getTenantDomainByReferer(HttpServletRequest request) {

        String tenantDomain = null;
        String refererHeader = request.getHeader("referer");
        if (StringUtils.isNotBlank(refererHeader)) {
            String[] queryParams = refererHeader.split(QUERY_SEPARATOR);
            for (String queryParam : queryParams) {
                if (queryParam.contains(TENANT_DOMAIN + EQUAL)) {
                    tenantDomain = queryParam.substring(queryParam.lastIndexOf(EQUAL) + 1);
                    break;
                }
            }
        }
        return tenantDomain;
    }

    private static String appendUri(String uri, String key, String value) throws UnsupportedEncodingException {

        if (StringUtils.isNotBlank(uri) && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {

            if (uri.contains("?")) {
                uri += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
            } else {
                uri += "?" + key + "=" + URLEncoder.encode(value, "UTF-8");
            }
        }
        return uri;
    }

    /**
     * Removes commonAuthCookie.
     * @param req Incoming HttpServletRequest.
     * @param resp HttpServlet response which the cookie must be written.
     */
    public static void removeAuthCookie(HttpServletRequest req, HttpServletResponse resp) {
        removeCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE);
    }

    /**
     * Removes a cookie which is already stored.
     * @param req Incoming HttpServletRequest.
     * @param resp HttpServletResponse which should be stored.
     * @param cookieName Name of the cookie which should be removed.
     */
    public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName) {

        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {

                    CookieBuilder cookieBuilder = new CookieBuilder(cookieName,
                            cookie.getValue());
                    IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig
                            (cookieName);

                    if (cookieConfig != null) {
                        updateCookieConfig(cookieBuilder, cookieConfig, 0);
                    } else {
                        cookieBuilder.setHttpOnly(true);
                        cookieBuilder.setSecure(true);
                        cookieBuilder.setPath("/");
                    }

                    cookieBuilder.setMaxAge(0);
                    resp.addCookie(cookieBuilder.build());
                    break;
                }
            }
        }
    }

    /**
     * @param req
     * @param resp
     * @param id
     */
    public static void storeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String id) {
        storeAuthCookie(req, resp, id, null);
    }

    /**
     * @param req
     * @param resp
     * @param id
     * @param age
     */
    public static void storeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String id, Integer age) {

        setCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE, id, age);
    }

    /**
     * Stores a cookie to the response taking configurations from identity.xml file.
     * @param req Incoming HttpSerletRequest.
     * @param resp Outgoing HttpServletResponse.
     * @param cookieName Name of the cookie to be stored.
     * @param id Cookie id.
     * @param age Max age of the cookie.
     */
    public static void setCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName, String id,
                                 Integer age) {

        CookieBuilder cookieBuilder = new CookieBuilder(cookieName, id);

        IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig(cookieName);

        if (cookieConfig != null) {

            updateCookieConfig(cookieBuilder, cookieConfig, age);
        } else {

            cookieBuilder.setSecure(true);
            cookieBuilder.setHttpOnly(true);
            cookieBuilder.setPath("/");

            if (age != null) {
                cookieBuilder.setMaxAge(age);
            }
        }

        resp.addCookie(cookieBuilder.build());
    }

    /**
     *
     * @param req Incoming HttpServletRequest.
     * @return CommonAuthID cookie.
     */
    public static Cookie getAuthCookie(HttpServletRequest req) {

        return getCookie(req, FrameworkConstants.COMMONAUTH_COOKIE);
    }

    /**
     * Returns the cookie with the given name.
     * @param req Incoming HttpServletRequest.
     * @param cookieName Name of the cookie.
     * @return Cookie with the given name. If it's not present null will be returned.
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName) {

        Cookie[] cookies = req.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }

        return null;
    }

    /**
     * @param contextId
     * @param context
     */
    public static void addAuthenticationContextToCache(String contextId, AuthenticationContext context) {

        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
        AuthenticationContextCacheEntry cacheEntry = new AuthenticationContextCacheEntry(context);
        cacheEntry.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getTempDataCleanUpTimeout()));
        AuthenticationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * @param key
     * @param authenticationResult
     */
    public static void addAuthenticationResultToCache(String key, AuthenticationResult authenticationResult) {

        AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(key);
        AuthenticationResultCacheEntry cacheEntry = new AuthenticationResultCacheEntry();
        cacheEntry.setResult(authenticationResult);
        cacheEntry.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout()));
        AuthenticationResultCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * To get authentication cache result from cache
     * @param key
     * @return
     */
    public static AuthenticationResultCacheEntry getAuthenticationResultFromCache(String key) {
        AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(key);
        AuthenticationResultCacheEntry authResult = AuthenticationResultCache.getInstance().getValueFromCache(cacheKey);
        return authResult;
    }

    /**
     *  Removes authentication result from cache.
     * @param autheticationResultId
     */
    public static void removeAuthenticationResultFromCache(String autheticationResultId) {
        if (autheticationResultId != null) {
            AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(autheticationResultId);
            AuthenticationResultCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * @deprecated Use the {@link #addSessionContextToCache(String, SessionContext, String)}
     *
     * @param key
     * @param sessionContext
     */
    @Deprecated
    public static void addSessionContextToCache(String key, SessionContext sessionContext) {

        SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
        SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();

        Map<String, SequenceConfig> seqData = sessionContext.getAuthenticatedSequences();
        if (seqData != null) {
            for (Entry<String, SequenceConfig> entry : seqData.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getAuthenticatedUser().setUserAttributes(null);
                    entry.getValue().setAuthenticationGraph(null);
                }
            }
        }
        Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (authenticatedUserObj instanceof AuthenticatedUser) {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            cacheEntry.setLoggedInUser(authenticatedUser.getAuthenticatedSubjectIdentifier());
        }
        cacheEntry.setContext(sessionContext);
        SessionContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    public static void addSessionContextToCache(String key, SessionContext sessionContext, String tenantDomain) {

        SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
        SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();

        Map<String, SequenceConfig> seqData = sessionContext.getAuthenticatedSequences();
        if (seqData != null) {
            for (Entry<String, SequenceConfig> entry : seqData.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getAuthenticatedUser().setUserAttributes(null);

                    // AuthenticationGraph in the SequenceConfig is used during the authentication flow and is not
                    // needed after the whole authentication flow is completed. Hense removed from the SessionContext.
                    entry.getValue().setAuthenticationGraph(null);
                }
            }
        }
        Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (authenticatedUserObj instanceof AuthenticatedUser) {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            cacheEntry.setLoggedInUser(authenticatedUser.getAuthenticatedSubjectIdentifier());
        }

        long timeoutPeriod;
        if (sessionContext.isRememberMe()) {
            timeoutPeriod = TimeUnit.SECONDS.toNanos(
                    IdPManagementUtil.getRememberMeTimeout(tenantDomain));
        } else {
            timeoutPeriod = TimeUnit.SECONDS.toNanos(
                    IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        }

        cacheEntry.setContext(sessionContext);
        cacheEntry.setValidityPeriod(timeoutPeriod);
        SessionContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * @param key
     * @return
     */
    public static SessionContext getSessionContextFromCache(String key) {

        SessionContext sessionContext = null;
        if (StringUtils.isNotBlank(key)) {
            SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
            Object cacheEntryObj = SessionContextCache.getInstance().getValueFromCache(cacheKey);

            if (cacheEntryObj != null) {
                sessionContext = ((SessionContextCacheEntry) cacheEntryObj).getContext();
            }
        }
        return sessionContext;
    }

    /**
     * @param key
     */
    public static void removeSessionContextFromCache(String key) {

        if (key != null) {
            SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
            SessionContextCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * @param contextId
     */
    public static void removeAuthenticationContextFromCache(String contextId) {

        if (contextId != null) {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
            AuthenticationContextCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * @param contextId
     * @return
     */
    public static AuthenticationContext getAuthenticationContextFromCache(String contextId) {

        AuthenticationContext authenticationContext = null;
        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
        AuthenticationContextCacheEntry authenticationContextCacheEntry = AuthenticationContextCache.getInstance().
                getValueFromCache(cacheKey);

        if (authenticationContextCacheEntry != null) {
            authenticationContext = authenticationContextCacheEntry.getContext();
        }

        if (log.isDebugEnabled() && authenticationContext == null) {
            log.debug("Authentication Context is null");
        }

        return authenticationContext;
    }

    /**
     * @param externalIdPConfig
     * @param name
     * @return
     */
    public static Map<String, String> getAuthenticatorPropertyMapFromIdP(
            ExternalIdPConfig externalIdPConfig, String name) {

        Map<String, String> propertyMap = new HashMap<String, String>();

        if (externalIdPConfig != null) {
            FederatedAuthenticatorConfig[] authenticatorConfigs = externalIdPConfig
                    .getIdentityProvider().getFederatedAuthenticatorConfigs();

            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (authenticatorConfig.getName().equals(name)) {

                    for (Property property : authenticatorConfig.getProperties()) {
                        propertyMap.put(property.getName(), property.getValue());
                    }
                    break;
                }
            }
        }

        return propertyMap;
    }

    /**
     * @param attributeValue
     * @return
     */
    public static Map<ClaimMapping, String> buildClaimMappings(Map<String, String> attributeValue) {

        Map<ClaimMapping, String> claimMap = new HashMap<ClaimMapping, String>();

        for (Iterator<Entry<String, String>> iterator = attributeValue.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, String> entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            claimMap.put(ClaimMapping.build(entry.getKey(), entry.getKey(), null, false),
                         entry.getValue());
        }

        return claimMap;

    }

    /**
     * @param attributeValues
     * @return
     */
    public static Set<String> getKeySet(Map<ClaimMapping, String> attributeValues) {

        Set<String> claimList = new HashSet<String>();

        for (Iterator<Entry<ClaimMapping, String>> iterator = attributeValues.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<ClaimMapping, String> entry = iterator.next();
            claimList.add(entry.getKey().getLocalClaim().getClaimUri());

        }

        return claimList;

    }

    /**
     * @param claimMappings
     * @return
     */
    public static Map<String, String> getClaimMappings(ClaimMapping[] claimMappings,
                                                       boolean useLocalDialectAsKey) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        for (ClaimMapping claimMapping : claimMappings) {
            if (useLocalDialectAsKey) {
                remoteToLocalClaimMap.put(claimMapping.getLocalClaim().getClaimUri(), claimMapping
                        .getRemoteClaim().getClaimUri());
            } else {
                remoteToLocalClaimMap.put(claimMapping.getRemoteClaim().getClaimUri(), claimMapping
                        .getLocalClaim().getClaimUri());
            }
        }
        return remoteToLocalClaimMap;
    }

    /**
     * @param claimMappings
     * @param useLocalDialectAsKey
     * @return
     */
    public static Map<String, String> getClaimMappings(Map<ClaimMapping, String> claimMappings,
                                                       boolean useLocalDialectAsKey) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        for (Entry<ClaimMapping, String> entry : claimMappings.entrySet()) {
            ClaimMapping claimMapping = entry.getKey();
            if (useLocalDialectAsKey) {
                remoteToLocalClaimMap.put(claimMapping.getLocalClaim().getClaimUri(), entry.getValue());
            } else {
                remoteToLocalClaimMap.put(claimMapping.getRemoteClaim().getClaimUri(), entry.getValue());
            }
        }
        return remoteToLocalClaimMap;
    }

    /**
     * @param claimMappings
     * @return
     */
    public static Map<String, String> getLocalToSPClaimMappings(Map<String, String> claimMappings) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        for (Entry<String, String> entry : claimMappings.entrySet()) {
            remoteToLocalClaimMap.put(entry.getValue(), entry.getKey());
        }
        return remoteToLocalClaimMap;
    }

    public static String getQueryStringWithFrameworkContextId(String originalQueryStr,
                                                              String callerContextId, String frameworkContextId) {

        String queryParams = originalQueryStr;

        /*
         * Upto now, query-string contained a 'sessionDataKey' of the calling servlet. At here we
         * replace it with the framework context id.
         */
        queryParams = queryParams.replace(callerContextId, frameworkContextId);

        return queryParams;
    }

    public static List<String> getStepIdPs(StepConfig stepConfig) {

        List<String> stepIdps = new ArrayList<String>();
        List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();

        for (AuthenticatorConfig authenticatorConfig : authenticatorConfigs) {
            List<String> authenticatorIdps = authenticatorConfig.getIdpNames();

            for (String authenticatorIdp : authenticatorIdps) {
                stepIdps.add(authenticatorIdp);
            }
        }

        return stepIdps;
    }

    public static List<String> getAuthenticatedStepIdPs(List<String> stepIdPs,
                                                        List<String> authenticatedIdPs) {

        List<String> idps = new ArrayList<String>();

        if (stepIdPs != null && authenticatedIdPs != null) {
            for (String stepIdP : stepIdPs) {
                if (authenticatedIdPs.contains(stepIdP)) {
                    idps.add(stepIdP);
                    break;
                }
            }
        }

        return idps;
    }

    public static Map<String, AuthenticatorConfig> getAuthenticatedStepIdPs(StepConfig stepConfig,
                                                                            Map<String, AuthenticatedIdPData> authenticatedIdPs) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Finding already authenticated IdPs of the step {order:%d}",
                    stepConfig.getOrder()));
        }

        Map<String, AuthenticatorConfig> idpAuthenticatorMap = new HashMap<String, AuthenticatorConfig>();
        List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();

        if (authenticatedIdPs != null && !authenticatedIdPs.isEmpty()) {

            for (AuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Considering the authenticator '%s'", authenticatorConfig.getName()));
                }

                String authenticatorName = authenticatorConfig.getName();
                List<String> authenticatorIdps = authenticatorConfig.getIdpNames();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%d IdP(s) found in the step.", authenticatedIdPs.size()));
                }

                for (String authenticatorIdp : authenticatorIdps) {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Considering the IDP : '%s'", authenticatorIdp));
                    }

                    AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(authenticatorIdp);

                    if (authenticatedIdPData != null && authenticatedIdPData.getIdpName() !=  null &&
                            authenticatedIdPData.getIdpName().equals(authenticatorIdp)) {

                        if (FrameworkConstants.LOCAL.equals(authenticatedIdPData.getIdpName())) {
                            if (authenticatedIdPData.isAlreadyAuthenticatedUsing(authenticatorName,
                                    authenticatorConfig.getApplicationAuthenticator().getAuthMechanism())) {
                                idpAuthenticatorMap.put(authenticatorIdp, authenticatorConfig);

                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("('%s', '%s') is an already authenticated " +
                                            "IDP - authenticator combination.",
                                            authenticatorConfig.getName(), authenticatorIdp));
                                }

                                break;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("('%s', '%s') is not an already authenticated " +
                                            "IDP - authenticator combination.",
                                            authenticatorConfig.getName(), authenticatorIdp));
                                }
                            }
                        } else {

                            if (log.isDebugEnabled()) {
                                log.debug(String.format("'%s' is an already authenticated IDP.", authenticatorIdp));
                            }

                            idpAuthenticatorMap.put(authenticatorIdp, authenticatorConfig);
                            break;
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("'%s' is NOT an already authenticated IDP.", authenticatorIdp));
                        }
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No authenticators found.");
            }
        }

        return idpAuthenticatorMap;
    }

    public static String getAuthenticatorIdPMappingString(List<AuthenticatorConfig> authConfigList) {

        StringBuilder authenticatorIdPStr = new StringBuilder("");

        for (AuthenticatorConfig authConfig : authConfigList) {
            StringBuilder idpsOfAuthenticatorStr = new StringBuilder("");

            for (String idpName : authConfig.getIdpNames()) {

                if (idpName != null) {

                    if (idpsOfAuthenticatorStr.length() != 0) {
                        idpsOfAuthenticatorStr.append(":");
                    }

                    IdentityProvider idp = authConfig.getIdps().get(idpName);

                    if (idp != null && idp.isFederationHub()) {
                        idpName += ".hub";
                    }

                    idpsOfAuthenticatorStr.append(idpName);
                }
            }

            if (authenticatorIdPStr.length() != 0) {
                authenticatorIdPStr.append(";");
            }

            authenticatorIdPStr.append(authConfig.getName()).append(":")
                    .append(idpsOfAuthenticatorStr);
        }

        return authenticatorIdPStr.toString();
    }

    /**
     * when getting query params through this, only configured params will be appended as query params
     * The required params can be configured from application-authenticators.xml
     *
     * @param request
     * @return
     */
    public static String getQueryStringWithConfiguredParams(HttpServletRequest request) {

        boolean configAvailable = FileBasedConfigurationBuilder.getInstance()
                .isAuthEndpointQueryParamsConfigAvailable();
        List<String> queryParams = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointQueryParams();
        String action = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointQueryParamsAction();

        StringBuilder queryStrBuilder = new StringBuilder("");
        Map<String, String[]> reqParamMap = request.getParameterMap();

        if (configAvailable) {
            if (action != null
                && action.equals(FrameworkConstants.AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE)) {
                if (reqParamMap != null) {
                    for (Map.Entry<String, String[]> entry : reqParamMap.entrySet()) {
                        String paramName = entry.getKey();
                        String paramValue = entry.getValue()[0];

                        //skip issuer and type and sessionDataKey parameters
                        if (SESSION_DATA_KEY.equals(paramName) || FrameworkConstants.RequestParams.ISSUER.equals
                                (paramName) || FrameworkConstants.RequestParams.TYPE.equals(paramName)) {
                            continue;
                        }

                        if (!queryParams.contains(paramName)) {
                            if (queryStrBuilder.length() > 0) {
                                queryStrBuilder.append('&');
                            }

                            try {
                                queryStrBuilder.append(URLEncoder.encode(paramName, UTF_8)).append('=')
                                        .append(URLEncoder.encode(paramValue, UTF_8));
                            } catch (UnsupportedEncodingException e) {
                                log.error(
                                        "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                        e);
                            }
                        }
                    }
                }
            } else {
                for (String param : queryParams) {
                    String paramValue = request.getParameter(param);

                    if (paramValue != null) {
                        if (queryStrBuilder.length() > 0) {
                            queryStrBuilder.append('&');
                        }
                        try {
                            queryStrBuilder.append(URLEncoder.encode(param, UTF_8)).append('=')
                                    .append(URLEncoder.encode(paramValue, UTF_8));
                        } catch (UnsupportedEncodingException e) {
                            log.error(
                                    "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                    e);
                        }
                    }
                }
            }
        } else {
            if (reqParamMap != null) {
                for (Map.Entry<String, String[]> entry : reqParamMap.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue()[0];

                    //skip issuer and type and sessionDataKey parameters
                    if (SESSION_DATA_KEY.equals(paramName) || FrameworkConstants.RequestParams.ISSUER.equals
                            (paramName) || FrameworkConstants.RequestParams.TYPE.equals(paramName)) {
                        continue;
                    }

                    if (queryStrBuilder.length() > 0) {
                        queryStrBuilder.append('&');
                    }

                    try {
                        queryStrBuilder.append(URLEncoder.encode(paramName, UTF_8)).append('=')
                                .append(URLEncoder.encode(paramValue, UTF_8));
                    } catch (UnsupportedEncodingException e) {
                        log.error(
                                "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                e);
                    }
                }
            }
        }

        return queryStrBuilder.toString();
    }

    public static String getRedirectURLWithFilteredParams(String redirectUrl, AuthenticationContext context) {

        return getRedirectURLWithFilteredParams(redirectUrl, context.getEndpointParams());
    }

    public static String getRedirectURLWithFilteredParams(String redirectUrl, Map<String, Serializable> dataStoreMap) {

        boolean configAvailable = FileBasedConfigurationBuilder.getInstance()
                .isAuthEndpointRedirectParamsConfigAvailable();

        if (!configAvailable) {
            return redirectUrl;
        }
        List<String> queryParams = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointRedirectParams();
        String action = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointRedirectParamsAction();

        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(redirectUrl);
        } catch (URISyntaxException e) {
            log.warn("Unable to filter redirect params for url." + redirectUrl, e);
            return redirectUrl;
        }

        // If the host name is not white listed then the query params will not be removed from the redirect url.
        List<String> filteringEnabledHosts = FileBasedConfigurationBuilder.getInstance().getFilteringEnabledHostNames();
        if (CollectionUtils.isNotEmpty(filteringEnabledHosts) && !filteringEnabledHosts.contains(uriBuilder.getHost())) {
            return redirectUrl;
        }

        List<NameValuePair> queryParamsList = uriBuilder.getQueryParams();

        if (action != null
                && action.equals(FrameworkConstants.AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE)) {
            if (queryParamsList != null) {
                Iterator<NameValuePair> iterator = queryParamsList.iterator();
                while (iterator.hasNext()) {
                    NameValuePair nameValuePair = iterator.next();
                    String paramName = nameValuePair.getName();
                    String paramValue = nameValuePair.getValue();

                    //skip sessionDataKey which is mandatory
                    if (SESSION_DATA_KEY.equals(paramName)) {
                        continue;
                    }

                    if (queryParams.contains(paramName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(paramName + " is in exclude list, hence removing from url and making " +
                                    "available via API");
                        }
                        dataStoreMap.put(paramName, paramValue);
                        iterator.remove();
                    }
                }
            }
        } else {
            if (queryParamsList != null) {
                Iterator<NameValuePair> iterator = queryParamsList.iterator();
                while (iterator.hasNext()) {
                    NameValuePair nameValuePair = iterator.next();
                    String paramName = nameValuePair.getName();
                    String paramValue = nameValuePair.getValue();

                    //skip sessionDataKey which is mandatory
                    if (SESSION_DATA_KEY.equals(paramName)) {
                        continue;
                    }

                    if (!queryParams.contains(paramName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(paramName + " is not in include list, hence removing from url and making " +
                                    "available via API");
                        }
                        dataStoreMap.put(paramName, paramValue);
                        iterator.remove();
                    }
                }
            }
        }
        uriBuilder.clearParameters();
        uriBuilder.setParameters(queryParamsList);
        return uriBuilder.toString();
    }

    public static boolean isRemoveAPIParamsOnConsume() {

        return FileBasedConfigurationBuilder.getInstance().isRemoveAPIParametersOnConsume();
    }

    public static int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public static void setMaxInactiveInterval(int maxInactiveInterval) {
        FrameworkUtils.maxInactiveInterval = maxInactiveInterval;
    }

    public static String prependUserStoreDomainToName(String authenticatedSubject) {

        if (authenticatedSubject == null || authenticatedSubject.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid argument. authenticatedSubject : "
                                               + authenticatedSubject);
        }
        if (!authenticatedSubject.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
            if (UserCoreUtil.getDomainFromThreadLocal() != null
                && !UserCoreUtil.getDomainFromThreadLocal().isEmpty()) {
                authenticatedSubject = UserCoreUtil.getDomainFromThreadLocal()
                                       + CarbonConstants.DOMAIN_SEPARATOR + authenticatedSubject;
            }
        } else if (authenticatedSubject.indexOf(CarbonConstants.DOMAIN_SEPARATOR) == 0) {
            throw new IllegalArgumentException("Invalid argument. authenticatedSubject : "
                                               + authenticatedSubject + " begins with \'" + CarbonConstants.DOMAIN_SEPARATOR
                                               + "\'");
        }
        return authenticatedSubject;
    }

    /*
     * Find the Subject identifier among federated claims
     */
    public static String getFederatedSubjectFromClaims(IdentityProvider identityProvider,
                                                       Map<ClaimMapping, String> claimMappings) {

        String userIdClaimURI = identityProvider.getClaimConfig().getUserClaimURI();
        ClaimMapping claimMapping = new ClaimMapping();
        Claim claim = new Claim();
        claim.setClaimUri(userIdClaimURI);
        claimMapping.setRemoteClaim(claim);
        claimMapping.setLocalClaim(claim);
        return claimMappings.get(claimMapping);
    }

    /*
     * Find the Subject identifier among federated claims
     */
    public static String getFederatedSubjectFromClaims(AuthenticationContext context, String otherDialect)
            throws FrameworkException {
        String value;
        boolean useLocalClaimDialect = context.getExternalIdP().useDefaultLocalIdpDialect();
        String userIdClaimURI = context.getExternalIdP().getUserIdClaimUri();
        Map<ClaimMapping, String> claimMappings = context.getSubject().getUserAttributes();

        if (useLocalClaimDialect) {
            Map<String, String> extAttributesValueMap = FrameworkUtils.getClaimMappings(claimMappings, false);
            Map<String, String> mappedAttrs = null;
            try {
                mappedAttrs = ClaimMetadataHandler.getInstance().getMappingsMapFromOtherDialectToCarbon(otherDialect,
                        extAttributesValueMap.keySet(), context.getTenantDomain(), true);
            } catch (ClaimMetadataException e) {
                throw new FrameworkException("Error while loading claim mappings.", e);
            }

            String spUserIdClaimURI = mappedAttrs.get(userIdClaimURI);
            value = extAttributesValueMap.get(spUserIdClaimURI);
        } else {
            ClaimMapping claimMapping = new ClaimMapping();
            Claim claim = new Claim();
            claim.setClaimUri(userIdClaimURI);
            claimMapping.setRemoteClaim(claim);
            claimMapping.setLocalClaim(claim);
            value = claimMappings.get(claimMapping);
        }
        return value;
    }

    /**
     * Starts the tenant flow for the given tenant domain
     *
     * @param tenantDomain tenant domain
     */
    public static void startTenantFlow(String tenantDomain) {
        String tenantDomainParam = tenantDomain;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;

        if (tenantDomainParam != null && !tenantDomainParam.trim().isEmpty()) {
            try {
                tenantId = FrameworkServiceComponent.getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error while getting tenantId from tenantDomain query param", e);
            }
        } else {
            tenantDomainParam = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomainParam);
    }

    /**
     * Ends the tenant flow
     */
    public static void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    /**
     * create a nano time stamp relative to Unix Epoch
     */
    public static long getCurrentStandardNano() {

        long epochTimeReference = TimeUnit.MILLISECONDS.toNanos(
                FrameworkServiceDataHolder.getInstance().getUnixTimeReference());
        long currentSystemNano = System.nanoTime();
        long currentStandardNano = epochTimeReference + (currentSystemNano - FrameworkServiceDataHolder.getInstance()
                .getNanoTimeReference());
        return currentStandardNano;
    }

    /**
     * Append a query param to the URL (URL may already contain query params)
     */
    public static String appendQueryParamsStringToUrl(String url, String queryParamString) {
        String queryAppendedUrl = url;
        // check whether param string to append is blank
        if (StringUtils.isNotEmpty(queryParamString)) {
            // check whether the URL already contains query params
            String appender;
            if (url.contains("?")) {
                appender = "&";
            } else {
                appender = "?";
            }

            // remove leading anchor or question mark in query params
            if (queryParamString.startsWith("?") || queryParamString.startsWith("&")) {
                queryParamString = queryParamString.substring(1);
            }

            queryAppendedUrl += appender + queryParamString;
        }

        return queryAppendedUrl;
    }

    /**
     * Append a query param map to the URL (URL may already contain query params)
     *
     * @param url         URL string to append the params.
     * @param queryParams Map of query params to be append.
     * @return Built URL with query params.
     * @throws UnsupportedEncodingException Throws when trying to encode the query params.
     *
     * @deprecated Use {@link #buildURLWithQueryParams(String, Map)} instead.
     */
    @Deprecated
    public static String appendQueryParamsToUrl(String url, Map<String, String> queryParams)
            throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Passed URL is empty.");
        }
        if (queryParams == null) {
            throw new IllegalArgumentException("Passed query param map is empty.");
        }

        List<String> queryParam1 = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
            queryParam1.add(entry.getKey() + "=" + encodedValue);
        }

        String queryString = StringUtils.join(queryParam1, "&");

        return appendQueryParamsStringToUrl(url, queryString);
    }

    /**
     * Append a query param map to the URL (URL may already contain query params)
     *
     * @param url         URL string to append the params.
     * @param queryParams Map of query params to be append.
     * @return Built URL with query params.
     * @throws UnsupportedEncodingException Can be thrown when trying to encode the query params.
     */
    public static String buildURLWithQueryParams(String url, Map<String, String> queryParams)
            throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Passed URL is empty.");
        }
        if (MapUtils.isEmpty(queryParams)) {
            return url;
        }

        List<String> queryParam1 = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
            queryParam1.add(entry.getKey() + "=" + encodedValue);
        }

        String queryString = StringUtils.join(queryParam1, "&");
        return appendQueryParamsStringToUrl(url, queryString);
    }

    public static void publishSessionEvent(String sessionId, HttpServletRequest request, AuthenticationContext
            context, SessionContext sessionContext, AuthenticatedUser user, String status) {
        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();
        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            paramMap.put(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionId);
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            if (FrameworkConstants.AnalyticsAttributes.SESSION_CREATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionCreation(request, context, sessionContext,
                        unmodifiableParamMap);
            } else if (FrameworkConstants.AnalyticsAttributes.SESSION_UPDATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionUpdate(request, context, sessionContext,
                        unmodifiableParamMap);
            } else if (FrameworkConstants.AnalyticsAttributes.SESSION_TERMINATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionTermination(request, context, sessionContext,
                        unmodifiableParamMap);
            }
        }
    }

    private static void updateCookieConfig(CookieBuilder cookieBuilder, IdentityCookieConfig
            cookieConfig, Integer age) {

        if (cookieConfig.getDomain() != null) {
            cookieBuilder.setDomain(cookieConfig.getDomain());
        }

        if (cookieConfig.getPath() != null) {
            cookieBuilder.setPath(cookieConfig.getPath());
        }

        if (cookieConfig.getComment() != null) {
            cookieBuilder.setComment(cookieConfig.getComment());
        }

        if (cookieConfig.getMaxAge() > 0) {
            cookieBuilder.setMaxAge(cookieConfig.getMaxAge());
        } else if (age != null) {
            cookieBuilder.setMaxAge(age);
        }

        if (cookieConfig.getVersion() > 0) {
            cookieBuilder.setVersion(cookieConfig.getVersion());
        }

        cookieBuilder.setHttpOnly(cookieConfig.isHttpOnly());

        cookieBuilder.setSecure(cookieConfig.isSecure());
    }

    public static String getMultiAttributeSeparator() {

        String multiAttributeSeparator = null;
        try {
            multiAttributeSeparator = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getRealmConfiguration().getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR);
        } catch (UserStoreException e) {
            log.warn("Error while retrieving MultiAttributeSeparator from UserRealm.");
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving MultiAttributeSeparator from UserRealm." + e);
            }
        }

        if (StringUtils.isBlank(multiAttributeSeparator)) {
            multiAttributeSeparator = IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
            if (log.isDebugEnabled()) {
                log.debug("Multi Attribute Separator is defaulting to " + multiAttributeSeparator);
            }
        }

        return multiAttributeSeparator;
    }

    public static String getPASTRCookieName (String sessionDataKey) {
        return FrameworkConstants.PASTR_COOKIE + "-" + sessionDataKey;
    }

    /**
     * Map the external IDP roles to local roles.
     * If excludeUnmapped is true exclude unmapped roles.
     * Otherwise include unmapped roles as well.
     *
     * @param externalIdPConfig     Relevant external IDP Config.
     * @param extAttributesValueMap Attributes map.
     * @param idpRoleClaimUri       IDP role claim URI.
     * @param excludeUnmapped       to indicate whether to exclude unmapped.
     * @return ArrayList<string> list of roles.
     */
    public static List<String> getIdentityProvideMappedUserRoles(ExternalIdPConfig externalIdPConfig,
            Map<String, String> extAttributesValueMap, String idpRoleClaimUri, Boolean excludeUnmapped) {

        if (idpRoleClaimUri == null) {
            // Since idpRoleCalimUri is not defined cannot do role mapping.
            if (log.isDebugEnabled()) {
                log.debug("Role claim uri is not configured for the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        String idpRoleAttrValue = null;
        if (extAttributesValueMap != null) {
            idpRoleAttrValue = extAttributesValueMap.get(idpRoleClaimUri);
        }
        String[] idpRoles;
        String federatedIDPRoleClaimAttributeSeparator;
        if (idpRoleAttrValue != null) {
            if (IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR) != null) {
                federatedIDPRoleClaimAttributeSeparator = IdentityUtil.getProperty(FrameworkConstants
                        .FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR);
                if (log.isDebugEnabled()) {
                    log.debug("The IDP side role claim value separator is configured as : " + federatedIDPRoleClaimAttributeSeparator);
                }
            } else {
                federatedIDPRoleClaimAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();
            }

            idpRoles = idpRoleAttrValue.split(federatedIDPRoleClaimAttributeSeparator);
        } else {
            // No identity provider role values found.
            if (log.isDebugEnabled()) {
                log.debug(
                        "No role attribute value has received from the external IDP: " + externalIdPConfig.getIdPName()
                                + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        Map<String, String> idpToLocalRoleMapping = externalIdPConfig.getRoleMappings();
        Set<String> idpMappedUserRoles = new HashSet<>();
        // If no role mapping is configured in the identity provider.
        if (MapUtils.isEmpty(idpToLocalRoleMapping)) {
            if (log.isDebugEnabled()) {
                log.debug("No role mapping is configured in the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            if (excludeUnmapped) {
                return new ArrayList<>();
            }
            idpMappedUserRoles.addAll(Arrays.asList(idpRoles));
            return new ArrayList<>(idpMappedUserRoles);
        }
        for (String idpRole : idpRoles) {
            if (idpToLocalRoleMapping.containsKey(idpRole)) {
                idpMappedUserRoles.add(idpToLocalRoleMapping.get(idpRole));
            } else if (!excludeUnmapped) {
                idpMappedUserRoles.add(idpRole);
            }
        }
        return new ArrayList<>(idpMappedUserRoles);
    }

    /**
     * To get the role claim uri of an IDP.
     *
     * @param externalIdPConfig Relevant external IDP Config.
     * @return idp role claim URI.
     */
    public static String getIdpRoleClaimUri(ExternalIdPConfig externalIdPConfig) {
        // get external identity provider role claim uri.
        String idpRoleClaimUri = externalIdPConfig.getRoleClaimUri();
        if (idpRoleClaimUri == null || idpRoleClaimUri.isEmpty()) {
            // no role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            ClaimMapping[] idpToLocalClaimMapping = externalIdPConfig.getClaimMappings();
            if (idpToLocalClaimMapping != null && idpToLocalClaimMapping.length > 0) {
                for (ClaimMapping mapping : idpToLocalClaimMapping) {
                    if (FrameworkConstants.LOCAL_ROLE_CLAIM_URI.equals(mapping.getLocalClaim().getClaimUri())
                            && mapping.getRemoteClaim() != null) {
                        return mapping.getRemoteClaim().getClaimUri();
                    }
                }
            }
        }
        return idpRoleClaimUri;
    }

    /**
     * Returns the local claim uri that is mapped for the IdP role claim uri configured.
     * If no role claim uri is configured for the IdP returns the local role claim 'http://wso2.org/claims/role'.
     *
     * @param externalIdPConfig IdP configurations
     * @return local claim uri mapped for the IdP role claim uri.
     */
    public static String getLocalClaimUriMappedForIdPRoleClaim(ExternalIdPConfig externalIdPConfig) {
        // get external identity provider role claim uri.
        String idpRoleClaimUri = externalIdPConfig.getRoleClaimUri();
        if (StringUtils.isNotBlank(idpRoleClaimUri)) {
            // Iterate over IdP claim mappings and check for the local claim that is mapped for the remote IdP role
            // claim uri configured.
            ClaimMapping[] idpToLocalClaimMapping = externalIdPConfig.getClaimMappings();
            if (!ArrayUtils.isEmpty(idpToLocalClaimMapping)) {
                for (ClaimMapping mapping : idpToLocalClaimMapping) {
                    if (mapping.getRemoteClaim() != null && idpRoleClaimUri
                            .equals(mapping.getRemoteClaim().getClaimUri())) {
                        return mapping.getLocalClaim().getClaimUri();
                    }
                }
            }
        }
        return FrameworkConstants.LOCAL_ROLE_CLAIM_URI;
    }

    /**
     * @deprecated This method is a temporary solution and might get changed in the future.
     * It is recommended not use this method.
     *
     * @param context AuthenticationContext.
     * @return true if the handlers need to be executed, otherwise false.
     */
    @Deprecated
    public static boolean isStepBasedSequenceHandlerExecuted(AuthenticationContext context) {

        boolean isNeeded = true;
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
        Object isDefaultStepBasedSequenceHandlerTriggered = context
                .getProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED);
        // If authenticated user is null or if step based sequence handler is not trigged, exit the flow.
        if (authenticatedUser == null || isDefaultStepBasedSequenceHandlerTriggered == null
                || !(boolean) isDefaultStepBasedSequenceHandlerTriggered) {
            isNeeded = false;
        }
        return isNeeded;
    }

    /**
     * To get the missing mandatory claims from SP side.
     *
     * @param context Authentication Context.
     * @return set of missing claims
     */
    public static String[] getMissingClaims(AuthenticationContext context) {

        StringBuilder missingClaimsString = new StringBuilder();
        StringBuilder missingClaimValuesString = new StringBuilder();

        Map<String, String> missingClaims = getMissingClaimsMap(context);

        for (Map.Entry<String, String> entry : missingClaims.entrySet()) {
            missingClaimsString.append(entry.getKey());
            missingClaimValuesString.append(entry.getValue());
            missingClaimsString.append(",");
            missingClaimValuesString.append(",");
        }

        return new String[]{missingClaimsString.toString(), missingClaimValuesString.toString()};
    }

    /**
     * Get the missing mandatory claims as a hash map.
     *
     * @param context Authentication Context.
     * @return Map of missing claims.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getMissingClaimsMap(AuthenticationContext context) {

        Map<String, String> mappedAttrs = new HashMap<>();
        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();
        Map<ClaimMapping, String> userAttributes = user.getUserAttributes();
        if (userAttributes != null) {
            Map<String, String> spToCarbonClaimMapping = new HashMap<>();
            Object object = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);

            if (object instanceof Map) {
                spToCarbonClaimMapping = (Map<String, String>) object;
            }
            for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
                String localClaimUri = entry.getKey().getLocalClaim().getClaimUri();

                //getting the carbon claim uri mapping for other claim dialects
                if (MapUtils.isNotEmpty(spToCarbonClaimMapping) && spToCarbonClaimMapping.get(localClaimUri) != null) {
                    localClaimUri = spToCarbonClaimMapping.get(localClaimUri);
                }
                mappedAttrs.put(localClaimUri, entry.getValue());
            }
        }

        Map<String, String> mandatoryClaims = context.getSequenceConfig().getApplicationConfig()
                .getMandatoryClaimMappings();
        Map<String, String> missingClaims = new HashMap<>();
        for (Map.Entry<String, String> entry : mandatoryClaims.entrySet()) {
            if (mappedAttrs.get(entry.getValue()) == null && mappedAttrs.get(entry.getKey()) == null) {
                missingClaims.put(entry.getKey(), entry.getValue());
            }
        }

        return missingClaims;
    }

    /**
     * To get the password provisioning url from the configuration file.
     *
     * @return relevant password provisioning url.
     */
    public static String getPasswordProvisioningUIUrl() {

        String passwordProvisioningUrl = IdentityUtil.getProperty("JITProvisioning.PasswordProvisioningUI");
        if (StringUtils.isEmpty(passwordProvisioningUrl)) {
            passwordProvisioningUrl = FrameworkConstants.SIGN_UP_ENDPOINT;
        }
        return passwordProvisioningUrl;
    }

    /**
     * To get the username provisioning url from the configuration file.
     *
     * @return relevant username provisioning url.
     */
    public static String getUserNameProvisioningUIUrl() {

        String userNamePrvisioningUrl = IdentityUtil.getProperty("JITProvisioning.UserNameProvisioningUI");
        if (StringUtils.isEmpty(userNamePrvisioningUrl)) {
            userNamePrvisioningUrl = FrameworkConstants.REGISTRATION_ENDPOINT;
        }
        return userNamePrvisioningUrl;
    }

    public static boolean promptOnLongWait() {

        boolean promptOnLongWait = false;
        String promptOnLongWaitString = IdentityUtil.getProperty("AdaptiveAuth.PromptOnLongWait");
        if (promptOnLongWaitString != null) {
            promptOnLongWait = Boolean.parseBoolean(promptOnLongWaitString);
        }
        return promptOnLongWait;
    }

    /**
     * This util is used to get the standard claim dialect of an Application based on the SP configuration.
     *
     * @param clientType Client Type.
     * @param appConfig  Application config.
     * @return standard dialect if exists.
     */
    public static String getStandardDialect(String clientType, ApplicationConfig appConfig) {

        Map<String, String> claimMappings = appConfig.getClaimMappings();
        if (FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(clientType)) {
            return "http://wso2.org/oidc/claim";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_STS.equals(clientType)) {
            return "http://schemas.xmlsoap.org/ws/2005/05/identity";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(clientType)) {
            return "http://axschema.org";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_SCIM.equals(clientType)) {
            return "urn:scim:schemas:core:1.0";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_WSO2.equals(clientType)) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else if (claimMappings == null || claimMappings.isEmpty()) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else {
            boolean isAtLeastOneNotEqual = false;
            for (Map.Entry<String, String> entry : claimMappings.entrySet()) {
                if (!entry.getKey().equals(entry.getValue())) {
                    isAtLeastOneNotEqual = true;
                    break;
                }
            }
            if (!isAtLeastOneNotEqual) {
                return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
            }
        }
        return null;
    }

    public static Object toJsSerializable(Object value) {

        if (value instanceof Serializable) {
            if (value instanceof HashMap) {
                Map<String, Object> map = new HashMap<>();
                ((HashMap) value).forEach((k, v) -> map.put((String) k, FrameworkUtils.toJsSerializable(v)));
                return map;
            } else {
                return value;
            }
        } else if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
            if (scriptObjectMirror.isFunction()) {
                return SerializableJsFunction.toSerializableForm(scriptObjectMirror);
            } else if (scriptObjectMirror.isArray()) {
                List<Serializable> arrayItems = new ArrayList<>(scriptObjectMirror.size());
                scriptObjectMirror.values().forEach(v -> {
                    Object serializedObj = toJsSerializable(v);
                    if (serializedObj instanceof Serializable) {
                        arrayItems.add((Serializable) serializedObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Serialized the value of array item as : " + serializedObj);
                        }
                    } else {
                        log.warn(String.format("Non serializable array item: %s. and will not be persisted.",
                                serializedObj));
                    }
                });
                return arrayItems;
            } else if (!scriptObjectMirror.isEmpty()) {
                Map<String, Serializable> serializedMap = new HashMap<>();
                scriptObjectMirror.forEach((k, v) -> {
                    Object serializedObj = toJsSerializable(v);
                    if (serializedObj instanceof Serializable) {
                        serializedMap.put(k, (Serializable) serializedObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Serialized the value for key : " + k);
                        }
                    } else {
                        log.warn(String.format("Non serializable object for key : %s, and will not be persisted.", k));
                    }

                });
                return serializedMap;
            } else {
                return Collections.EMPTY_MAP;
            }
        }
        return value;
    }

    public static Object fromJsSerializable(Object value, ScriptEngine engine) throws FrameworkException {

        if (value instanceof SerializableJsFunction) {
            SerializableJsFunction serializableJsFunction = (SerializableJsFunction) value;
            try {
                return engine.eval(serializableJsFunction.getSource());
            } catch (ScriptException e) {
                throw new FrameworkException("Error in resurrecting a Javascript Function : " + serializableJsFunction);
            }

        } else if (value instanceof Map) {
            Map<String, Object> deserializedMap = new HashMap<>();
            for (Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                Object deserializedObj = fromJsSerializable(entry.getValue(), engine);
                deserializedMap.put(entry.getKey(), deserializedObj);
            }
            return deserializedMap;
        }
        return value;
    }

    /**
     * Get the configurations of a tenant from cache or database
     *
     * @param tenantDomain Domain name of the tenant
     * @return Configurations belong to the tenant
     */
    private static Property[] getResidentIdpConfiguration(String tenantDomain) throws FrameworkException {

        IdpManager identityProviderManager = IdentityProviderManager.getInstance();
        IdentityProvider residentIdp = null;
        try {
            residentIdp = identityProviderManager.getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            String errorMsg = String.format("Error while retrieving resident Idp for %s tenant.", tenantDomain);
            throw new FrameworkException(errorMsg, e);
        }
        IdentityProviderProperty[] identityMgtProperties = residentIdp.getIdpProperties();
        Property[] configMap = new Property[identityMgtProperties.length];
        int index = 0;
        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            if (ALREADY_WRITTEN_PROPERTY.equals(identityMgtProperty.getName())) {
                continue;
            }
            Property property = new Property();
            property.setName(identityMgtProperty.getName());
            property.setValue(identityMgtProperty.getValue());
            configMap[index] = property;
            index++;
        }
        return configMap;
    }

    /**
     * This method is used to get the requested resident Idp configuration details.
     *
     * @param propertyName
     * @param tenantDomain
     * @return Property
     * @throws FrameworkException
     */
    public static Property getResidentIdpConfiguration(String propertyName, String tenantDomain) throws
            FrameworkException {

        Property requestedProperty = null;
        Property[] allProperties = getResidentIdpConfiguration(tenantDomain);
        for (int i = 0; i < allProperties.length; i++) {
            if (propertyName.equals(allProperties[i].getName())) {
                requestedProperty = allProperties[i];
                break;
            }
        }

        return requestedProperty;

    }

    /**
     * Check user session mapping enabled.
     *
     * @return Return true if UserSessionMapping configuration enabled and both IDN_AUTH_USER and
     * IDN_AUTH_USER_SESSION_MAPPING tables are available.
     */
    public static boolean isUserSessionMappingEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED)) && isTableExists(
                "IDN_AUTH_USER") && isTableExists("IDN_AUTH_USER_SESSION_MAPPING");
    }

    /**
     * Check whether the specified table exists in the Identity database.
     *
     * @param tableName name of the table.
     * @return true if table exists.
     */
    public static boolean isTableExists(String tableName) {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.storesLowerCaseIdentifiers()) {
                tableName = tableName.toLowerCase();
            }

            try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] { "TABLE" })) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table - " + tableName + " available in the Identity database.");
                    }
                    connection.commit();
                    return true;
                }
            }
            connection.commit();
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Table - " + tableName + " not available in the Identity database.");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Table - " + tableName + " not available in the Identity database.");
        }
        return false;
    }
}

