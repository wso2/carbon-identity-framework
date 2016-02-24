/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

public abstract class InboundAuthenticationRequestProcessor {

    /**
     * Process inbound authentication request
     *
     * @param authenticationRequest Inbound authentication request
     * @return Inbound authentication response
     * @throws FrameworkException
     */
    public abstract InboundAuthenticationResponse process(InboundAuthenticationRequest authenticationRequest)
            throws FrameworkException;

    /**
     * Get Name
     * @return Name
     */
    public abstract String getName();

    /**
     * Get callback path
     *
     * @param context Inbound authentication context
     * @return Callback path
     * @throws FrameworkException
     */
    public abstract String getCallbackPath(InboundAuthenticationContext context) throws FrameworkException;

    /**
     * Get relying party id
     * @return Relying party id
     */
    public abstract String getRelyingPartyId();

    /**
     * Get Priority
     * @return Priority
     */
    public abstract int getPriority();

    /**
     * Can handle
     * @param authenticationRequest Inbound authentication request
     * @return boolean
     * @throws FrameworkException
     */
    public abstract boolean canHandle(InboundAuthenticationRequest authenticationRequest) throws FrameworkException;

    /**
     * Build response for framework login
     *
     * @param context Inbound authentication context
     * @return
     * @throws IOException
     * @throws IdentityApplicationManagementException
     * @throws FrameworkException
     */
    protected InboundAuthenticationResponse buildResponseForFrameworkLogin(InboundAuthenticationContext context)
            throws IOException, IdentityApplicationManagementException, FrameworkException {

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        InboundAuthenticationRequest inboundAuthenticationRequest = context.getInboundAuthenticationRequest();

        Map<String, String[]> parameterMap = inboundAuthenticationRequest.getParameters();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Map.Entry<String, String> entry : inboundAuthenticationRequest.getHeaders().entrySet()) {
            authenticationRequest.addHeader(entry.getKey(), entry.getValue());
        }

        authenticationRequest.setRelyingParty(getRelyingPartyId());
        authenticationRequest.setType(getName());
        authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getCallbackPath(context), "UTF-8"));

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundAuthenticationContextCacheEntry contextCacheEntry = new InboundAuthenticationContextCacheEntry(context);
        InboundAuthenticationUtil.addInboundAuthenticationContextToCache(sessionDataKey, contextCacheEntry);

        InboundAuthenticationResponse response = new InboundAuthenticationResponse();
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.AUTH_NAME, getName());
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.SESSION_DATA_KEY, sessionDataKey);
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.CALL_BACK_PATH,
                getCallbackPath(context));
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.RELYING_PARTY, getRelyingPartyId());
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.AUTH_TYPE, getName());
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        response.setRedirectURL(commonAuthURL);
        return response;
    }

    /**
     * Build response for framework logout
     *
     * @param context Inbound authentication context
     * @return
     * @throws IOException
     * @throws IdentityApplicationManagementException
     * @throws FrameworkException
     */
    protected InboundAuthenticationResponse buildResponseForFrameworkLogout(InboundAuthenticationContext context)
            throws IOException, IdentityApplicationManagementException, FrameworkException {

        String sessionDataKey = UUIDGenerator.generateUUID();

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        InboundAuthenticationRequest inboundAuthenticationRequest = context.getInboundAuthenticationRequest();

        Map<String, String[]> parameterMap = inboundAuthenticationRequest.getParameters();

        parameterMap.put(FrameworkConstants.SESSION_DATA_KEY, new String[] { sessionDataKey });
        parameterMap.put(FrameworkConstants.RequestParams.TYPE, new String[] { getName() });

        authenticationRequest.appendRequestQueryParams(parameterMap);

        for (Map.Entry<String, String> entry : inboundAuthenticationRequest.getHeaders().entrySet()) {
            authenticationRequest.addHeader(entry.getKey(), entry.getValue());
        }

        authenticationRequest.setRelyingParty(getRelyingPartyId());
        authenticationRequest.setType(getName());
        authenticationRequest.setCommonAuthCallerPath(URLEncoder.encode(getCallbackPath(context), "UTF-8"));
        authenticationRequest.addRequestQueryParam(FrameworkConstants.RequestParams.LOGOUT,
                new String[]{"true"});

        AuthenticationRequestCacheEntry authRequest = new AuthenticationRequestCacheEntry(authenticationRequest);
        FrameworkUtils.addAuthenticationRequestToCache(sessionDataKey, authRequest);

        InboundAuthenticationContextCacheEntry contextCacheEntry = new InboundAuthenticationContextCacheEntry(context);
        InboundAuthenticationUtil.addInboundAuthenticationContextToCache(sessionDataKey, contextCacheEntry);

        InboundAuthenticationResponse response = new InboundAuthenticationResponse();
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.AUTH_NAME, getName());
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.SESSION_DATA_KEY, sessionDataKey);
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.CALL_BACK_PATH,
                getCallbackPath(context));
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.RELYING_PARTY, getRelyingPartyId());
        //type parameter is using since framework checking it, but future it'll use AUTH_NAME
        response.addParameters(InboundAuthenticationConstants.RequestProcessor.AUTH_TYPE, getName());
        String commonAuthURL = IdentityUtil.getServerURL(FrameworkConstants.COMMONAUTH, true, true);
        response.setRedirectURL(commonAuthURL);
        return response;
    }
}
