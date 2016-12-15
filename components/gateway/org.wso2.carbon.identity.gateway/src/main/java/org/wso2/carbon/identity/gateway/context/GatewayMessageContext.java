/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.context;

import org.wso2.carbon.identity.framework.context.MessageContext;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GatewayMessageContext<T1 extends Serializable, T2 extends Serializable, T3 extends GatewayRequest,
        T4 extends GatewayResponse> extends MessageContext implements Serializable {

    private static final long serialVersionUID = -3000397944848547943L;

    protected String sessionDataKey;

    protected T3 initialIdentityRequest;

    protected T3 currentIdentityRequest;
    protected T4 identityResponse;

    protected AbstractHandler currentHandler;
    protected HandlerResponseStatus handlerResponseStatus;

    protected Map<String, ? extends GatewayMessageContext> messageContexts = new HashMap<>();


    public GatewayMessageContext(T3 identityRequest, T4 identityResponseMessage) {

        this.currentIdentityRequest = identityRequest;
        this.identityResponse = identityResponseMessage;
        sessionDataKey = UUID.randomUUID().toString();
    }

    public GatewayMessageContext(T3 identityRequest, T4 identityResponseMessage, Map<T1, T2> parameters) {

        this(identityRequest, identityResponseMessage);
        Optional.ofNullable(parameters).ifPresent(x -> {
            this.parameters.putAll(x);
        });
    }

    public T3 getCurrentIdentityRequest() {

        return currentIdentityRequest;
    }

    public void setCurrentIdentityRequest(T3 currentIdentityRequest) {

        this.currentIdentityRequest = currentIdentityRequest;
    }

    public void setIdentityResponse(T4 identityResponse) {

        this.identityResponse = identityResponse;
    }

    public T4 getIdentityResponse() {

        return identityResponse;
    }

    @Override
    public Map<T1, T2> getParameters() {

        return parameters;
    }

    @Override
    public void addParameter(Object key, Object value) {

        super.addParameter(key, value);
    }

    @Override
    public void addParameters(Map paramMap) {

        Optional.of(paramMap).ifPresent(x -> this.parameters.putAll(x));
    }


    @Override
    public Object getParameter(Object key) {

        return super.getParameter(key);
    }

    public void addParameter(T1 key, T2 value) {

        parameters.putIfAbsent(key, value);
    }


    public AbstractHandler getCurrentHandler() {

        return currentHandler;
    }

    public void setCurrentHandler(AbstractHandler currentHandler) {

        this.currentHandler = currentHandler;
    }

    public HandlerResponseStatus getHandlerResponseStatus() {

        return handlerResponseStatus;
    }

    public void setHandlerResponseStatus(HandlerResponseStatus handlerResponseStatus) {

        this.handlerResponseStatus = handlerResponseStatus;
    }

    public T3 getInitialIdentityRequest() {

        return initialIdentityRequest;
    }

    public void setInitialIdentityRequest(T3 initialIdentityRequest) {

        this.initialIdentityRequest = initialIdentityRequest;
    }


    public String getSessionDataKey() {

        return sessionDataKey;
    }

    public void setSessionDataKey(String sessionDataKey) {

        this.sessionDataKey = sessionDataKey;
    }
}
