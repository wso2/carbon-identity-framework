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

package org.wso2.carbon.identity.gateway.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.gateway.message.GatewayRequest;
import org.wso2.carbon.identity.gateway.message.GatewayResponse;
import org.wso2.carbon.identity.gateway.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayFactoryManager;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayRequestFactory;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayResponseFactory;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class GatewayManager {

    private static Logger logger = LoggerFactory.getLogger(GatewayManager.class);

    private static final String ERROR_PROCESSING_REQUEST = "Error Processing Request.";
    private static final String INVALID_REQUEST = "Invalid or Malformed Request.";

    private static GatewayManager instance = new GatewayManager();

    private GatewayManager() {

    }

    public static GatewayManager getInstance() {

        return instance;
    }


    /**
     * @param msf4jRequest
     * @return
     */
    public Response processRequest(Request msf4jRequest) {

        GatewayRequestFactory requestFactory = GatewayFactoryManager.getRequestFactory(msf4jRequest);
        Response response;
        try {
            GatewayRequest gatewayRequest = requestFactory.getGatewayRequest(msf4jRequest);
            response = processGatewayRequest(gatewayRequest);
        } catch (FrameworkClientException e) {
            logger.error(INVALID_REQUEST, e);
            response = requestFactory.handleException(e, msf4jRequest);
        }

        return response;
    }


    private Response processGatewayRequest(GatewayRequest gatewayRequest) {

        // Pick the processor that can handle the GatewayRequest
        GatewayProcessor processor = GatewayFactoryManager.getGatewayProcessor(gatewayRequest);
        GatewayResponseFactory responseFactory;
        Response response;
        try {
            GatewayResponse gatewayResponse = processor.process(gatewayRequest);
            responseFactory = GatewayFactoryManager.getIdentityResponseFactory(gatewayResponse);
            response = responseFactory.create(gatewayResponse);
        } catch (FrameworkException e) {
            responseFactory = GatewayFactoryManager.getIdentityResponseFactory(e);
            response = responseFactory.handleException(e);
        }

        return response;
    }


    /**
     * We need a default ResponseBuilder to build a Response to be sent to the client in cases where we can't obtain
     * a response builder factory or something goes wrong when trying to process the Request.
     *
     * @return Response
     */

    public Response getDefaultServerErrorResponse(String message) {

        return Response.serverError().build();
    }
}
