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

package org.wso2.carbon.identity.gateway.api;

import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

public abstract class IdentityProcessor {

    protected Properties properties = new Properties();

    /**
     * Initialize IdentityProcessor
     *
     * @param properties IdentityProcessor properties
     */
    public void init(Properties properties) {
        if (properties != null) {
            this.properties = properties;
        }
    }

    /**
     * Process IdentityRequest
     *
     * @param identityRequest IdentityRequest
     * @return IdentityResponseBuilder
     * @throws FrameworkServerException Error
     *                            occurred
     *                            while
     *                            processing
     *                            IdentityRequest
     */
    public abstract IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest)
            throws FrameworkServerException;

    /**
     * Returns the unique name of the request IdentityProcessor
     *
     * @return name
     */
    public abstract String getName();

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
     * Checks if previous IdentityMessageContext exists for given IdentityRequest using {@code sessionDataKey} parameter
     *
     * @param request IdentityRequest
     */
    protected boolean isContextAvailable(IdentityRequest request) {
        String sessionDataKey = request.getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
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
        IdentityMessageContext context = null;
        if (StringUtils.isNotBlank(sessionDataKey)) {
            context = InboundUtil.getContextFromCache(sessionDataKey);
        }
        return context;
    }

    /**
     * Processes the IdentityMessageContext and retrieved the using {@code sessionDataKey} parameter and sets the
     * AuthenticationResponse to message context if found in AuthenticationResultCache
     *
     * @param context IdentityMessageContext
     * @return AuthenticationResponse
     */
    /*protected AuthenticationResult processResponseFromFrameworkLogin(IdentityMessageContext context) {

        String sessionDataKey =
                context.getIdentityRequest().getParameter(InboundConstants.RequestProcessor.CONTEXT_KEY);
        AuthenticationResultCacheEntry entry = FrameworkUtils.getAuthenticationResultFromCache(sessionDataKey);
        AuthenticationResult authnResult = null;
        if (entry != null) {
            authnResult = entry.getResult();
        } else {
            throw FrameworkRuntimeException.error("Cannot find AuthenticationResponse from the cache");
        }
        FrameworkUtils.removeAuthenticationResultFromCache(sessionDataKey);
        if (authnResult.isAuthenticated()) {
            context.addParameter(InboundConstants.RequestProcessor.AUTHENTICATION_RESULT, authnResult);
        }
        return authnResult;
    }*/
}
