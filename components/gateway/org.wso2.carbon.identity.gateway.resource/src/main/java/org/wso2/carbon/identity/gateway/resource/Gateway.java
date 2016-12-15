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

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.FrameworkClientException;
import org.wso2.carbon.identity.framework.FrameworkException;
import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.message.Request;
import org.wso2.carbon.identity.framework.message.Response;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.ResponseBuilder;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * Identity Gateway MicroService. This serves as the endpoint for all requests that come into the Identity Gateway.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.resource.Gateway",
        service = Microservice.class,
        immediate = true
)
@Path("/gateway")
public class Gateway implements Microservice {

    private static Logger logger = LoggerFactory.getLogger(Gateway.class);
    private static final String ERROR_PROCESSING_REQUEST = "Error Processing Request.";
    private static final String INVALID_REQUEST = "Invalid or Malformed Request.";

    /**
     * Entry point for all initial Identity Request coming into the Gateway.
     *
     * @param request
     * @return
     */
    @POST
    @Path("/")
    public javax.ws.rs.core.Response processPost(@Context org.wso2.msf4j.Request request) {

        try {
            return processRequest(request);
        } catch (FrameworkException ex) {
            logger.error("Error processing the initial request.", ex);
            return getDefaultServerErrorResponseBuilder(ERROR_PROCESSING_REQUEST).build();
        }
    }


    @GET
    @Path("/")
    public javax.ws.rs.core.Response processGet(@Context org.wso2.msf4j.Request request) {

        return processPost(request);
    }

    /**
     * Entry point for all subsequent requests/callbacks coming into the gateway after redirection/federation etc.
     *
     * @param request
     * @return
     */
    @POST
    @Path("/callback")
    public javax.ws.rs.core.Response callback(@Context org.wso2.msf4j.Request request) {

        try {
            return processRequest(request);
        } catch (FrameworkException ex) {
            logger.error("Error processing the callback request.", ex);
            return getDefaultServerErrorResponseBuilder(ERROR_PROCESSING_REQUEST).build();
        }
    }


    @GET
    @Path("/callback")
    public javax.ws.rs.core.Response callbackGet(@Context org.wso2.msf4j.Request request) {

        return callback(request);
    }


    /**
     * Process the {@link org.wso2.msf4j.Request} received by Identity Gateway endpoint.
     *
     * @param request {@link org.wso2.msf4j.Request} received by the Identity Gateway.
     * @return @{@link javax.ws.rs.core.Response} to be sent to the client.
     */
    private javax.ws.rs.core.Response processRequest(org.wso2.msf4j.Request request) throws FrameworkException {

        /*
            Pick a registered GatewayRequestFactory that can handle the request that came into the Identity
            Gateway.
         */
        GatewayRequestFactory requestFactory = GatewayFactoryManager.pickRequestFactory(request);


        Request identityRequest;
        ResponseBuilder responseBuilder;

        /*
            We use the IdentityRequestBuilder returned by the factory to create an Request which is the
            canonical representation of the request received by the Identity Gateway. This canonical representation
            will be used in request to the Identity Framework.
         */
        try {
            identityRequest = requestFactory.getGatewayRequest(request);
            if (identityRequest == null) {
                logger.error("Could not build the Identity Request.");
                throw new FrameworkRuntimeException(ERROR_PROCESSING_REQUEST);
            }
        } catch (FrameworkClientException e) {
            logger.error(INVALID_REQUEST, e);
            responseBuilder = requestFactory.handleException(e, request);
            if (responseBuilder == null) {
                logger.error("Unable to find a builder to build a response for client. Building with default error " +
                        "response builder");
                responseBuilder = javax.ws.rs.core.Response.status(BAD_REQUEST);
            }
            return responseBuilder.build();
        }

        IdentityProcessCoordinator processCoordinator = GatewayFactoryManager.getIdentityProcessCoordinator();

        Response response;
        GatewayResponseBuilderFactory responseFactory;
        /*
            Process the Request by handing it over to the IdentityProcessCoordinator.
            IdentityProcessCoordinator will send the Request through the framework and return an
            Response.
         */
        try {
            response = processCoordinator.process(identityRequest);
            if (response == null) {
                logger.error("Framework returned a null response. We can't proceed further.");
                return getDefaultServerErrorResponseBuilder(ERROR_PROCESSING_REQUEST).build();
            }

            /*
               Pick the GatewayResponseBuilderFactory that can provide us with a JAX-RS response builder to build
               the Response to be sent to the client.
             */
            responseFactory = GatewayFactoryManager.pickIdentityResponseFactory(response);
            responseBuilder = responseFactory.create(response);
            return responseBuilder.build();

        } catch (FrameworkException e) {
            responseFactory = GatewayFactoryManager.pickIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                logger.error("No response builder to build a response to be sent to the client.");
                return getDefaultServerErrorResponseBuilder(ERROR_PROCESSING_REQUEST).build();
            }
            return responseBuilder.build();
        }
    }

    /**
     * We need a default ResponseBuilder to build a Response to be sent to the client in cases where we can't obtain
     * a response builder factory or something goes wrong when trying to process the Request.
     *
     * @return
     */
    private ResponseBuilder getDefaultServerErrorResponseBuilder(String message) {

        return javax.ws.rs.core.Response.serverError();
    }
}
