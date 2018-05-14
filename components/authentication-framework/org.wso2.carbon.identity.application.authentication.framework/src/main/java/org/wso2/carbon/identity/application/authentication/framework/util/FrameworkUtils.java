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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
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
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.DefaultStepHandler;
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
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.core.model.CookieBuilder;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FrameworkUtils {

    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String UTF_8 = "UTF-8";
    private static final Log log = LogFactory.getLog(FrameworkUtils.class);
    private static int maxInactiveInterval;
    private static final String EMAIL = "email";
    private static List<String> cacheDisabledAuthenticators = Arrays
            .asList(new String[] { FrameworkConstants.RequestType.CLAIM_TYPE_SAML_SSO, FrameworkConstants.OAUTH2 });

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

        StepBasedSequenceHandler stepBasedSequenceHandler = null;
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

        StepHandler stepHandler = null;
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
        response.sendRedirect(ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL());
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
     * @param key
     * @param sessionContext
     */
    public static void addSessionContextToCache(String key, SessionContext sessionContext) {
        SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
        SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();

        Map<String, SequenceConfig> seqData = sessionContext.getAuthenticatedSequences();
        if (seqData != null) {
            for (Entry<String, SequenceConfig> entry : seqData.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getAuthenticatedUser().setUserAttributes(null);
                }
            }
        }
        Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (authenticatedUserObj != null && authenticatedUserObj instanceof AuthenticatedUser) {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            cacheEntry.setLoggedInUser(authenticatedUser.getAuthenticatedSubjectIdentifier());
        }
        cacheEntry.setContext(sessionContext);
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
                            if (authenticatedIdPData.isAlreadyAuthenticatedUsing(authenticatorName)) {
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

        if (cookieConfig.isHttpOnly()) {
            cookieBuilder.setHttpOnly(cookieConfig.isHttpOnly());
        }

        if (cookieConfig.isSecure()) {
            cookieBuilder.setSecure(cookieConfig.isSecure());
        }
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
        if (idpRoleAttrValue != null) {
            idpRoles = idpRoleAttrValue.split(FrameworkUtils.getMultiAttributeSeparator());
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

        List<String> idpMappedUserRoles = new ArrayList<>();
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
            return idpMappedUserRoles;
        }

        for (String idpRole : idpRoles) {
            if (idpToLocalRoleMapping.containsKey(idpRole)) {
                idpMappedUserRoles.add(idpToLocalRoleMapping.get(idpRole));
            } else if (!excludeUnmapped) {
                idpMappedUserRoles.add(idpRole);
            }
        }
        return idpMappedUserRoles;
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
}

