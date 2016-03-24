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
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

public abstract class InboundProcessor {

    protected Properties properties = new Properties();

    /**
     * Initialize InboundRequestProcessor
     *
     * @param properties InboundRequestProcessor properties
     */
    public void init(Properties properties) {
        if(properties != null){
            this.properties = properties;
        }
    }

    /**
     * Process inbound authentication request
     *
     * @param inboundRequest InboundRequest
     * @return InboundResponse
     */
    public abstract InboundResponse process(InboundRequest inboundRequest);

    /**
     * Returns the unique name of the request InboundRequestProcessor
     * @return name
     */
    public abstract String getName();

    /**
     * Get callback path
     *
     * @param context InboundMessageContext
     * @return Callback path
     */
    public abstract String getCallbackPath(InboundMessageContext context);

    /**
     * Get relying party unique ID
     * @return Relying party unique ID
     */
    public abstract String getRelyingPartyId();

    /**
     * Get execution order priority
     * @return priority
     */
    public abstract int getPriority();

    /**
     * Can handle
     * @param inboundRequest InboundRequest
     * @return can/not handle
     */
    public abstract boolean canHandle(InboundRequest inboundRequest);

    /**
     * Get InboundResponseBuilder for framework login
     *
     * @param context InboundMessageContext
     * @return InboundResponseBuilder
     */
    protected InboundResponse.InboundResponseBuilder buildResponseForFrameworkLogin(InboundMessageContext context) {

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        InboundRequest inboundRequest = context.getRequest();

        Map<String, String[]> parameterMap = inboundRequest.getParameterMap();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Object entry : inboundRequest.getHeaderMap().keySet()) {
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

        InboundResponse.InboundResponseBuilder responseBuilder =
                new InboundResponse.InboundResponseBuilder();
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_NAME,
                new String[]{getName()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.SESSION_DATA_KEY,
                new String[]{sessionDataKey});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.CALL_BACK_PATH,
                new String[]{getCallbackPath(context)});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.RELYING_PARTY,
                new String[]{getRelyingPartyId()});
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_TYPE,
                new String[]{getName()});
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        responseBuilder.setRedirectURL(commonAuthURL);
        return responseBuilder;
    }

    /**
     * Get InboundResponseBuilder for framework logout
     *
     * @param context InboundContext
     * @return InboundResponseBuilder
     */
    protected InboundResponse.InboundResponseBuilder buildResponseForFrameworkLogout(InboundMessageContext context) {

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        InboundRequest inboundRequest = context.getRequest();

        Map<String, String[]> parameterMap = inboundRequest.getParameterMap();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Object entry : inboundRequest.getHeaderMap().keySet()) {
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

        InboundResponse.InboundResponseBuilder responseBuilder =
                new InboundResponse.InboundResponseBuilder();
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_NAME, new String[]{getName()});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.SESSION_DATA_KEY, new String[]{sessionDataKey});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.CALL_BACK_PATH,
                new String[]{getCallbackPath(context)});
        responseBuilder.addParameter(InboundConstants.RequestProcessor.RELYING_PARTY,
                new String[]{getRelyingPartyId()});
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        responseBuilder.addParameter(InboundConstants.RequestProcessor.AUTH_TYPE, new String[]{getName()});
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        responseBuilder.setRedirectURL(commonAuthURL);
        return responseBuilder;
    }

    /**
     * Checks if previous InboundMessageContext exists for given InboundRequest using {@code sessionDataKey} parameter
     *
     * @param request InboundRequest
     * @return InboundResponseBuilder
     */
    protected boolean isContextAvailable(InboundRequest request) {
        String sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.SESSION_DATA_KEY);
        if(StringUtils.isNotBlank(sessionDataKey)){
            InboundMessageContext context = InboundContextCache.getInstance().getValueFromCache(sessionDataKey);
            if(context != null){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns InboundMessageContext if one previously existed for given InboundRequest using {@code sessionDataKey}
     * parameter
     *
     * @param request InboundRequest
     * @return InboundResponseBuilder
     */
    protected InboundMessageContext getContextIfAvailable(InboundRequest request) {
        String sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.SESSION_DATA_KEY);
        InboundMessageContext context = null;
        if(StringUtils.isNotBlank(sessionDataKey)){
            context = InboundContextCache.getInstance().getValueFromCache(sessionDataKey);
        }
        return context;
    }

    /**
     * Processes the InboundMessageContext and retrieved the using {@code sessionDataKey} parameter and sets the
     * AuthenticationResult to message context if found in AuthenticationResultCache
     *
     * @param context InboundMessageContext
     * @return InboundResponseBuilder
     */
    protected AuthenticationResult processResponseFromFrameworkLogin(InboundMessageContext context) {

        String sessionDataKey = context.getRequest().getParameter(InboundConstants.RequestProcessor.SESSION_DATA_KEY);
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
