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

package org.wso2.carbon.identity.framework.context;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.framework.handler.GatewayEventHandler;
import org.wso2.carbon.identity.framework.handler.GatewayHandlerStatus;
import org.wso2.carbon.identity.framework.message.IdentityRequest;
import org.wso2.carbon.identity.framework.message.IdentityResponse;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public class IdentityMessageContext<T1 extends Serializable, T2 extends Serializable, T3 extends IdentityRequest,
        T4 extends IdentityResponse> extends MessageContext implements Serializable {

    private static final long serialVersionUID = -3000397944848547943L;

    protected T3 initialIdentityRequest;

    protected T3 currentIdentityRequest;
    protected T4 identityResponse;

    protected GatewayEventHandler currentHandler;
    protected GatewayHandlerStatus currentHandlerStatus;

    public IdentityMessageContext(T3 identityRequest, T4 identityResponseMessage) {
        this.currentIdentityRequest = identityRequest;
        this.identityResponse = identityResponseMessage;
    }

    public IdentityMessageContext(T3 identityRequest, T4 identityResponseMessage, Map<T1, T2> parameters) {
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


    public GatewayEventHandler getCurrentHandler() {
        return currentHandler;
    }

    public void setCurrentHandler(GatewayEventHandler currentHandler) {
        this.currentHandler = currentHandler;
    }

    public GatewayHandlerStatus getCurrentHandlerStatus() {
        return currentHandlerStatus;
    }

    public void setCurrentHandlerStatus(GatewayHandlerStatus currentHandlerStatus) {
        this.currentHandlerStatus = currentHandlerStatus;
    }

    public T3 getInitialIdentityRequest() {
        return initialIdentityRequest;
    }

    public void setInitialIdentityRequest(T3 initialIdentityRequest) {
        this.initialIdentityRequest = initialIdentityRequest;
    }
}
