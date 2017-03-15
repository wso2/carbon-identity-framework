/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.handler.session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.context.SequenceContext;
import org.wso2.carbon.identity.gateway.context.SessionContext;
import org.wso2.carbon.identity.gateway.dao.CacheBackedSessionDAO;
import org.wso2.carbon.identity.gateway.exception.SessionHandlerException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;

import java.util.UUID;

/**
 * DefaultSessionHandler is provide by the gateway to handle the session.
 */
public class DefaultSessionHandler extends AbstractSessionHandler {

    @Override
    public boolean canHandle(MessageContext messageContext) {
        return true;
    }

    @Override
    public GatewayHandlerResponse updateSession(AuthenticationContext context) throws SessionHandlerException {

        GatewayRequest identityRequest = context.getIdentityRequest();
        if (identityRequest instanceof AuthenticationRequest) {
            String sessionKey = ((AuthenticationRequest) identityRequest).getSessionKey();
            String sessionKeyHash;
            if (StringUtils.isBlank(sessionKey)) {
                sessionKey = UUID.randomUUID().toString();
            }
            sessionKeyHash = DigestUtils.sha256Hex(sessionKey);
            if (context.getParameter(AuthenticationRequest.AuthenticationRequestConstants.SESSION_KEY) == null) {
                context.addParameter(AuthenticationRequest.AuthenticationRequestConstants.SESSION_KEY, sessionKey);
            }

            String serviceProviderName = context.getServiceProvider().getName();
            SessionContext sessionContext = context.getSessionContext();
            if (sessionContext == null) {
                sessionContext = createSession(context);
            }
            SequenceContext existingSequenceContext = sessionContext.getSequenceContext(serviceProviderName);
            SequenceContext currentSequenceContext = context.getSequenceContext();
            if (existingSequenceContext == null) {
                updateSession(context, sessionContext);
            }
            sessionContext.addSequenceContext(serviceProviderName, currentSequenceContext);
            CacheBackedSessionDAO.getInstance().put(sessionKeyHash, sessionContext);
            return new GatewayHandlerResponse(GatewayHandlerResponse.Status.CONTINUE);
        }
        throw new GatewayRuntimeException("GatewayRequest is not instance of AuthenticationRequest.");
    }

    private SessionContext createSession(AuthenticationContext authenticationContext) {
        SessionContext sessionContext = new SessionContext();
        sessionContext.addSequenceContext(authenticationContext.getServiceProvider().getName(),
                authenticationContext.getSequenceContext());
        return sessionContext;
    }

    private SessionContext updateSession(AuthenticationContext authenticationContext, SessionContext sessionContext) {
        sessionContext.addSequenceContext(authenticationContext.getServiceProvider().getName(),
                authenticationContext.getSequenceContext());
        return sessionContext;
    }
}
