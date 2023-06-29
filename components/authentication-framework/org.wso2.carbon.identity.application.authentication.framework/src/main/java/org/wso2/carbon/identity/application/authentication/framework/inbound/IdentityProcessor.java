/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Identity processor
 */
public abstract class IdentityProcessor extends AbstractIdentityHandler {

    private static Log log = LogFactory.getLog(IdentityProcessor.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    /**
     * Initialize IdentityProcessor
     *
     * @param initConfig IdentityProcessor properties
     */
    public void init(InitConfig initConfig) {

        if (initConfig != null) {
            this.initConfig = initConfig;
        }

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (IdentityProcessor.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return;
        }

        if (identityEventListenerConfig.getProperties() != null) {
            for (Map.Entry<Object, Object> property : identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String) property.getKey();
                String value = (String) property.getValue();
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                } else {
                    log.warn("Property key " + key + " already exists. Cannot add property!!");
                }
            }
        }
    }

    /**
     * Process IdentityRequest
     *
     * @param identityRequest IdentityRequest
     * @throws FrameworkException Error occurred while processing IdentityRequest
     * @return IdentityResponseBuilder
     */
    public abstract IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest)
            throws FrameworkException;

    /**
     * Get callback path
     *
     * @param context IdentityMessageContext
     * @return Callback path
     */
    public abstract String getCallbackPath(IdentityMessageContext context);

    /**
     * Get tenant qualified callback path.
     *
     * @param context IdentityMessageContext
     * @return Tenant qualified callback path
     */
    protected String getTenantQualifiedCallbackPath(IdentityMessageContext context) {

        String callbackPath = getCallbackPath(context);
        try {
            if (!isAbsoluteURI(callbackPath) && !isTenantQualifiedURI(callbackPath)) {
                if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                    callbackPath = ServiceURLBuilder.create().addPath(getCallbackPath(context)).build()
                            .getAbsolutePublicURL();
                } else {
                    String serverUrl = ServiceURLBuilder.create().build().getAbsolutePublicURL();
                    String tenantDomain = getTenantDomainFromContext();
                    if (!isSuperTenantFlow(tenantDomain)) {
                        callbackPath = serverUrl + "/t/" + tenantDomain + "/" + callbackPath;
                    } else {
                        callbackPath = serverUrl + "/" + callbackPath;
                    }
                }
            }
        } catch (URISyntaxException | URLBuilderException e) {
            throw new RuntimeException("Error while building tenant qualified Callback Path.", e);
        }
        return callbackPath;
    }

    private boolean isAbsoluteURI(String uri) throws URISyntaxException {

       return new URI(uri).isAbsolute();
    }

    private boolean isTenantQualifiedURI(String uri) {

        return uri.startsWith("/t/") || uri.startsWith("t/");
    }

    private String getTenantDomainFromContext() {

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        return tenantDomain;
    }

    private boolean isSuperTenantFlow(String tenantDomain) {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain);
    }

    /**
     * Get relying party unique ID
     *
     * @return Relying party unique ID
     */
    @Deprecated
    public abstract String getRelyingPartyId();

    /**
     * Get relying party unique ID
     *
     * @return Relying party unique ID
     */
    public abstract String getRelyingPartyId(IdentityMessageContext context);

    /**
      * Get type of inbound identity protocol supported by this processor
      *
      * @return Type of inbound identity protocol
      */
    public String getType(IdentityMessageContext context) {
        return getName();
    }

    /**
     * Tells if this processor can handle this IdentityRequest
     *
     * @param identityRequest IdentityRequest
     * @return can/not handle
     */
    public abstract boolean canHandle(IdentityRequest identityRequest);

    /**
     * Get IdentityResponseBuilder for framework login
     *
     * @param context IdentityMessageContext
     * @return IdentityResponseBuilder
     */
    protected FrameworkLoginResponse.FrameworkLoginResponseBuilder buildResponseForFrameworkLogin(
            IdentityMessageContext context) {

        IdentityRequest identityRequest = context.getRequest();
        Map<String, String[]> parameterMap = identityRequest.getParameterMap();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.appendRequestQueryParams(parameterMap);
        Set<Map.Entry<String, String>> headers = new HashMap(identityRequest.getHeaderMap()).entrySet();
        for (Map.Entry<String, String> header : headers) {
            authenticationRequest.addHeader(header.getKey(), header.getValue());
        }
        authenticationRequest.setTenantDomain(identityRequest.getTenantDomain());
        authenticationRequest.setRelyingParty(getRelyingPartyId(context));
        authenticationRequest.setType(getType(context));
        authenticationRequest.setPassiveAuth(Boolean.parseBoolean(
                String.valueOf(context.getParameter(InboundConstants.PASSIVE_AUTH))));
        authenticationRequest.setForceAuth(Boolean.parseBoolean(
                String.valueOf(context.getParameter(InboundConstants.FORCE_AUTH))));
        try {
            authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getTenantQualifiedCallbackPath(context),
                                                                            StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw FrameworkRuntimeException.error("Error occurred while URL encoding callback path " +
                    getTenantQualifiedCallbackPath(context), e);
        }

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        String sessionDataKey = UUID.randomUUID().toString();
        authRequest.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout()));
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundUtil.addContextToCache(sessionDataKey, context);

        FrameworkLoginResponse.FrameworkLoginResponseBuilder responseBuilder =
                new FrameworkLoginResponse.FrameworkLoginResponseBuilder(context);
        responseBuilder.setAuthName(getType(context));
        responseBuilder.setContextKey(sessionDataKey);
        responseBuilder.setCallbackPath(getTenantQualifiedCallbackPath(context));
        responseBuilder.setRelyingParty(getRelyingPartyId(context));
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.setAuthType(getType(context));
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        responseBuilder.setRedirectURL(commonAuthURL);
        return responseBuilder;
    }

    /**
     * Get IdentityResponseBuilder for framework logout
     *
     * @param context IdentityMessageContext
     * @return IdentityResponseBuilder
     */
    protected FrameworkLogoutResponse.FrameworkLogoutResponseBuilder buildResponseForFrameworkLogout(
            IdentityMessageContext context) {

        IdentityRequest identityRequest = context.getRequest();
        Map<String, String[]> parameterMap = identityRequest.getParameterMap();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.appendRequestQueryParams(parameterMap);
        Set<Map.Entry<String, String>> headers = new HashMap(identityRequest.getHeaderMap()).entrySet();
        for (Map.Entry<String, String> header : headers) {
            authenticationRequest.addHeader(header.getKey(), header.getValue());
        }
        authenticationRequest.setTenantDomain(identityRequest.getTenantDomain());
        authenticationRequest.setRelyingParty(getRelyingPartyId(context));
        authenticationRequest.setType(getType(context));
        try {
            authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getTenantQualifiedCallbackPath(context),
                                                                            StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw FrameworkRuntimeException.error("Error occurred while URL encoding callback path " +
                    getTenantQualifiedCallbackPath(context), e);
        }
        authenticationRequest.addRequestQueryParam(FrameworkConstants.RequestParams.LOGOUT, new String[]{"true"});

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        String sessionDataKey = UUID.randomUUID().toString();
        authRequest.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout()));
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundUtil.addContextToCache(sessionDataKey, context);

        FrameworkLogoutResponse.FrameworkLogoutResponseBuilder responseBuilder =
                new FrameworkLogoutResponse.FrameworkLogoutResponseBuilder(context);
        responseBuilder.setAuthName(getType(context));
        responseBuilder.setContextKey(sessionDataKey);
        responseBuilder.setCallbackPath(getTenantQualifiedCallbackPath(context));
        responseBuilder.setRelyingParty(getRelyingPartyId(context));
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.setAuthType(getType(context));
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        responseBuilder.setRedirectURL(commonAuthURL);
        return responseBuilder;
    }

    /**
     * Checks if previous IdentityMessageContext exists for given IdentityRequest using {@code sessionDataKey} parameter
     *
     * @param request IdentityRequest
     */
    protected boolean isContextAvailable(IdentityRequest request) {
        String sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
        // preserving backward compatibility with OAuth2 consent page
        if (StringUtils.isBlank(sessionDataKey)) {
            sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY_CONSENT);
        }
        if (StringUtils.isNotBlank(sessionDataKey)) {
            IdentityMessageContext context = InboundUtil.getContextFromCache(sessionDataKey);
            if (context != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns IdentityMessageContext if one previously existed for given IdentityRequest using {@code sessionDataKey}
     * parameter
     *
     * @param request IdentityRequest
     * @return IdentityMessageContext
     */
    protected IdentityMessageContext getContextIfAvailable(IdentityRequest request) {
        String sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
        // preserving backward compatibility with OAuth2 consent page
        if (StringUtils.isBlank(sessionDataKey)) {
            sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY_CONSENT);
        }
        IdentityMessageContext context = null;
        if (StringUtils.isNotBlank(sessionDataKey)) {
            context = InboundUtil.getContextFromCache(sessionDataKey);
        }
        return context;
    }

    /**
     * Processes the IdentityMessageContext and retrieved the using {@code sessionDataKey} parameter and sets the
     * AuthenticationResult to message context if found in AuthenticationResultCache
     *
     * @param context IdentityMessageContext
     * @param identityRequest Current IdentityRequest object
     * @return AuthenticationResult
     */
    protected AuthenticationResult processResponseFromFrameworkLogin(IdentityMessageContext context,
                                                                     IdentityRequest identityRequest) {

        String sessionDataKey = identityRequest.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
        AuthenticationResultCacheEntry entry = FrameworkUtils.getAuthenticationResultFromCache(sessionDataKey);
        AuthenticationResult authnResult = null;
        if (entry != null) {
            authnResult = entry.getResult();
        } else {
            throw FrameworkRuntimeException.error("Cannot find AuthenticationResult from the cache");
        }
        FrameworkUtils.removeAuthenticationResultFromCache(sessionDataKey);
        if (authnResult.isAuthenticated()) {
            context.addParameter(InboundConstants.RequestProcessor.AUTHENTICATION_RESULT, authnResult);
        }
        return authnResult;
    }
}
