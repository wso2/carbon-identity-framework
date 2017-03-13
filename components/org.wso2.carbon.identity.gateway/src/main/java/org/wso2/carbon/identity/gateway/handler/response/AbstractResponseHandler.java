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
package org.wso2.carbon.identity.gateway.handler.response;


import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.exception.GatewayException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.common.model.sp.ResponseBuilderConfig;
import org.wso2.carbon.identity.gateway.common.model.sp.ResponseBuildingConfig;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.exception.ResponseHandlerException;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerResponse;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractResponseHandler extends AbstractGatewayHandler {

    public abstract GatewayHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext,
                                                              GatewayException exception)
            throws ResponseHandlerException;

    public abstract GatewayHandlerResponse buildErrorResponse(AuthenticationContext authenticationContext,
                                                              GatewayRuntimeException exception)
            throws ResponseHandlerException;

    public abstract GatewayHandlerResponse buildResponse(AuthenticationContext authenticationContext)
            throws ResponseHandlerException;

    public abstract boolean canHandle(MessageContext messageContext, GatewayException exception);

    public abstract boolean canHandle(MessageContext messageContext, GatewayRuntimeException exception);

    public ResponseBuilderConfig getResponseBuilderConfigs(AuthenticationContext authenticationContext) throws
            AuthenticationHandlerException {
        ResponseBuilderConfig responseBuilderConfig = null;
        if (authenticationContext.getServiceProvider() == null) {
            throw new AuthenticationHandlerException("Error while getting validator configs : No service provider " +
                    "found with uniqueId : " + authenticationContext.getUniqueId());
        }

        ResponseBuildingConfig responseBuildingConfig = authenticationContext.getServiceProvider()
                .getResponseBuildingConfig();
        List<ResponseBuilderConfig> responseBuilderConfigs = responseBuildingConfig.getResponseBuilderConfigs();

        Iterator<ResponseBuilderConfig> responseBuilderConfigIterator = responseBuilderConfigs.iterator();
        while (responseBuilderConfigIterator.hasNext()) {
            ResponseBuilderConfig responseBuilderConfigTmp = responseBuilderConfigIterator.next();
            if (getValidatorType().equalsIgnoreCase(responseBuilderConfigTmp.getType())) {
                responseBuilderConfig = responseBuilderConfigTmp;
            }
        }
        return responseBuilderConfig;
    }

    protected abstract String getValidatorType();

    protected void addSessionKey(GatewayResponse.GatewayResponseBuilder responseBuilder,
                                 AuthenticationContext context) throws ResponseHandlerException {

        responseBuilder.setSessionKey((String) context.getParameter(AuthenticationRequest.AuthenticationRequestConstants
                .SESSION_KEY));
    }
}
