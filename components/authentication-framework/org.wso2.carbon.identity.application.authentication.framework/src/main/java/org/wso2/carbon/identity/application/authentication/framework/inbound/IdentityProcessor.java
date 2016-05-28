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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.handler.InitConfig;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

public abstract class IdentityProcessor {

    private static Log log = LogFactory.getLog(IdentityProcessor.class);

    protected final Properties properties = new Properties();

    protected InitConfig initConfig;

    /**
     * Initialize IdentityProcessor
     *
     * @param initConfig IdentityProcessor properties
     */
    public void init(InitConfig initConfig) {

        if(initConfig != null) {
            this.initConfig = initConfig;
        }

        IdentityEventListenerConfig identityEventListenerConfig = IdentityUtil.readEventListenerProperty
                (IdentityProcessor.class.getName(), this.getClass().getName());

        if (identityEventListenerConfig == null) {
            return;
        }

        if(identityEventListenerConfig.getProperties() != null) {
            for(Map.Entry<Object,Object> property:identityEventListenerConfig.getProperties().entrySet()) {
                String key = (String)property.getKey();
                String value = (String)property.getValue();
                if(!properties.containsKey(key)) {
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
     * @throws org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException Error occurred while processing IdentityRequest
     * @return IdentityResponseBuilder
     */
    public abstract IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest)
            throws FrameworkException;

    /**
     * Returns the unique name of the request IdentityProcessor
     *
     * @return name
     */
    public abstract String getName();

    /**
     * Get callback path
     *
     * @param context IdentityMessageContext
     * @return Callback path
     */
    public abstract String getCallbackPath(IdentityMessageContext context);

    /**
     * Get relying party unique ID
     *
     * @return Relying party unique ID
     */
    public abstract String getRelyingPartyId();

    /**
     * Get priority
     *
     * @return priority
     */
    public abstract int getPriority();

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

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        IdentityRequest identityRequest = context.getRequest();

        Map<String, String[]> parameterMap = identityRequest.getParameterMap();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Object entry : identityRequest.getHeaderMap().keySet()) {
            authenticationRequest.addHeader(((Map.Entry<String,String>)entry).getKey(),
                    ((Map.Entry<String, String>)entry).getValue());
        }

        authenticationRequest.setRelyingParty(getRelyingPartyId());
        authenticationRequest.setType(getName());
        authenticationRequest.setPassiveAuth((Boolean)context.getParameter(InboundConstants.PassiveAuth));
        authenticationRequest.setForceAuth((Boolean) context.getParameter(InboundConstants.ForceAuth));
        try {
            authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getCallbackPath(context), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw FrameworkRuntimeException.error("Error occurred while URL encoding callback path " +
                    getCallbackPath(context), e);
        }

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundUtil.addContextToCache(sessionDataKey, context);

        FrameworkLoginResponse.FrameworkLoginResponseBuilder responseBuilder =
                new FrameworkLoginResponse.FrameworkLoginResponseBuilder(context);
        responseBuilder.setAuthName(getName());
        responseBuilder.setContextKey(sessionDataKey);
        responseBuilder.setCallbackPath(getCallbackPath(context));
        responseBuilder.setRelyingParty(getRelyingPartyId());
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.setAuthType(getName());
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

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        IdentityRequest identityRequest = context.getRequest();

        Map<String, String[]> parameterMap = identityRequest.getParameterMap();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Object entry : identityRequest.getHeaderMap().keySet()) {
            authenticationRequest.addHeader(((Map.Entry<String,String>)entry).getKey(),
                    ((Map.Entry<String, String>)entry).getValue());
        }

        authenticationRequest.setRelyingParty(getRelyingPartyId());
        authenticationRequest.setType(getName());
        try {
            authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getCallbackPath(context), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw FrameworkRuntimeException.error("Error occurred while URL encoding callback path " +
                    getCallbackPath(context), e);
        }
        authenticationRequest.addRequestQueryParam(FrameworkConstants.RequestParams.LOGOUT,
                new String[]{"true"});

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundUtil.addContextToCache(sessionDataKey, context);

        FrameworkLogoutResponse.FrameworkLogoutResponseBuilder responseBuilder =
                new FrameworkLogoutResponse.FrameworkLogoutResponseBuilder(context);
        responseBuilder.setAuthName(getName());
        responseBuilder.setContextKey(sessionDataKey);
        responseBuilder.setCallbackPath(getCallbackPath(context));
        responseBuilder.setRelyingParty(getRelyingPartyId());
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.setAuthType(getName());
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
        if(StringUtils.isNotBlank(sessionDataKey)) {
            IdentityMessageContext context = InboundUtil.getContextFromCache(sessionDataKey);
            if(context != null) {
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
        IdentityMessageContext context = null;
        if(StringUtils.isNotBlank(sessionDataKey)) {
            context = InboundUtil.getContextFromCache(sessionDataKey);
        }
        return context;
    }

    /**
     * Processes the IdentityMessageContext and retrieved the using {@code sessionDataKey} parameter and sets the
     * AuthenticationResult to message context if found in AuthenticationResultCache
     *
     * @param context IdentityMessageContext
     * @return AuthenticationResult
     */
    protected AuthenticationResult processResponseFromFrameworkLogin(IdentityMessageContext context) {

        String sessionDataKey = context.getRequest().getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
        AuthenticationResultCacheEntry entry = FrameworkUtils.getAuthenticationResultFromCache(sessionDataKey);
        AuthenticationResult authnResult = null;
        if(entry != null) {
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
