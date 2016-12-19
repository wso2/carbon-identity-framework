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

package org.wso2.carbon.identity.gateway.resource.factory;

import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.message.Response;
import org.wso2.carbon.identity.gateway.GatewayProcessor;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;
import org.wso2.carbon.identity.gateway.resource.Gateway;
import org.wso2.carbon.identity.gateway.resource.internal.DataHolder;
import org.wso2.msf4j.Request;

/**
 * Utility class used by {@link Gateway}
 */
public class GatewayFactoryManager {

    private static DataHolder dataHolder = DataHolder.getInstance();

    /**
     * Pick a @{@link GatewayRequestFactory} that can return a canonical {@link GatewayRequest} object based on the
     * {@link Request} received by the {@link Gateway} micro service.
     *
     * @param request {@link Request} object.
     * @return GatewayRequestFactory instance
     */
    public static GatewayRequestFactory getRequestFactory(Request request) {

        return dataHolder.getRequestFactoryList().stream()
                .filter(x -> x.canHandle(request))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a request factory to build the " +
                        "canonical Identity Request."));
    }


    /**
     * Pick a @{@link GatewayResponseFactory} that can return a @{@link javax.ws.rs.core.Response.ResponseBuilder}
     * based on the @{@link Response} sent from the Framework.
     *
     * @param gatewayResponse {@link Response} object sent from framework
     * @return GatewayRequestFactory instance or null if none of the available services can handle the
     * response
     */
    public static GatewayResponseFactory getIdentityResponseFactory(GatewayResponse gatewayResponse) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(gatewayResponse))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a response factory to handle the " +
                        "Response."));
    }


    /**
     * Pick a {@link GatewayResponseFactory} that can handle when a {@link FrameworkException}
     * is encountered while processing an {@link org.wso2.carbon.identity.framework.message.Request}
     *
     * @param ex {@link FrameworkException} encountered.
     * @return GatewayResponseFactory instance that can handle the exception, null if no registered response
     * factory can handle the exception.
     */
    public static GatewayResponseFactory getIdentityResponseFactory(FrameworkException ex) {

        return dataHolder.getResponseFactoryList().stream()
                .filter(x -> x.canHandle(ex))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find a response factory to handle the " +
                        "FrameworkException."));
    }


    public static GatewayProcessor getGatewayProcessor(GatewayRequest gatewayRequest) {

        return dataHolder.getGatewayProcessors().stream()
                .filter(x -> x.canHandle(gatewayRequest))
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find the gateway processor to handle the " +
                        "request"));
    }
}
