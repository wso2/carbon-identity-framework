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
import org.wso2.carbon.identity.framework.handler.HandlerResponseStatus;
import org.wso2.carbon.identity.gateway.artifact.model.HandlerConfig;
import org.wso2.carbon.identity.gateway.artifact.model.ServiceProvider;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;
import org.wso2.carbon.identity.gateway.message.GatewayResponse.GatewayResponseBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GatewayMessageContext<T1 extends Serializable, T2 extends Serializable> extends MessageContext implements Serializable {

    private static final long serialVersionUID = -3000397944848547943L;

    private ServiceProvider serviceProvider = null ;
    private HandlerConfig currentHandler = null ;

    protected String sessionDataKey;

    protected GatewayRequest initialIdentityRequest;
    protected GatewayResponseBuilder finalResponseBuilder;

    protected GatewayRequest currentIdentityRequest;
    protected GatewayResponse currentIdentityResponse;

    protected HandlerResponseStatus handlerResponseStatus;

    protected Map<String, ? extends GatewayMessageContext> messageContexts = new HashMap<>();


    public GatewayMessageContext(GatewayRequest identityRequest) {

        this.currentIdentityRequest = identityRequest;
        this.currentIdentityResponse = new GatewayResponseBuilder().build();
        this.finalResponseBuilder = new GatewayResponseBuilder();
        sessionDataKey = UUID.randomUUID().toString();
    }

    public GatewayMessageContext(GatewayRequest identityRequest, Map<T1, T2> parameters) {

        this(identityRequest);
        Optional.ofNullable(parameters).ifPresent(x -> {
            this.parameters.putAll(x);
        });
    }

    public GatewayRequest getCurrentIdentityRequest() {

        return currentIdentityRequest;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public void setCurrentIdentityRequest(GatewayRequest currentIdentityRequest) {

        this.currentIdentityRequest = currentIdentityRequest;
    }

    public void setCurrentIdentityResponse(GatewayResponse currentIdentityResponse) {

        this.currentIdentityResponse = currentIdentityResponse;
    }

    public GatewayResponse getCurrentIdentityResponse() {

        return currentIdentityResponse;
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


    public HandlerConfig getCurrentHandler() {
        return currentHandler;
    }

    public void setCurrentHandler(HandlerConfig currentHandler) {
        this.currentHandler = currentHandler;
    }

    public HandlerResponseStatus getHandlerResponseStatus() {

        return handlerResponseStatus;
    }

    public void setHandlerResponseStatus(HandlerResponseStatus handlerResponseStatus) {

        this.handlerResponseStatus = handlerResponseStatus;
    }

    public GatewayRequest getInitialIdentityRequest() {

        return initialIdentityRequest;
    }

    public void setInitialIdentityRequest(GatewayRequest initialIdentityRequest) {

        this.initialIdentityRequest = initialIdentityRequest;
    }


    public String getSessionDataKey() {

        return sessionDataKey;
    }

    public void setSessionDataKey(String sessionDataKey) {

        this.sessionDataKey = sessionDataKey;
    }

    public GatewayResponseBuilder getFinalResponseBuilder() {

        return finalResponseBuilder;
    }

    public void setFinalResponseBuilder(GatewayResponseBuilder finalResponseBuilder) {

        this.finalResponseBuilder = finalResponseBuilder;
    }
}
