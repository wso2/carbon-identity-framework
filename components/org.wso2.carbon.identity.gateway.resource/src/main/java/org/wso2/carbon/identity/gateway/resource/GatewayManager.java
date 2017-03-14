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
package org.wso2.carbon.identity.gateway.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.api.processor.GatewayProcessor;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponse;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.gateway.resource.internal.GatewayResourceDataHolder;
import org.wso2.msf4j.Request;
import java.util.List;
import javax.ws.rs.core.Response;


/**
 * GatewayManager manage the MSF4J request to call the gateway.
 * <p>
 * 1. Find the relevant RequestFactory
 * 2. Build the request
 * 3. Find the relevant Processor
 * 4. Process the request
 * 5. Find the Response factory for either success or error
 * <p>
 * Except the above flow, this will catch the Runtime exception to handle the request that is not possible handle by
 * framework and return 500.
 */
public class GatewayManager {
    private Logger log = LoggerFactory.getLogger(GatewayManager.class);

    /**
     * Main execution point of the gateway resource manager.
     *
     * @param request
     * @return MSF4J response
     */
    public Response execute(Request request) {

        GatewayRequestBuilderFactory factory = null;

        try {

            factory = getIdentityRequestFactory(request);
            GatewayRequest gatewayRequest = factory.create(request).build();
            GatewayProcessor processor = getIdentityProcessor(gatewayRequest);
            GatewayResponse gatewayResponse = processor.process(gatewayRequest).build();
            GatewayResponseBuilderFactory responseFactory = getIdentityResponseFactory(gatewayResponse);
            Response.ResponseBuilder builder = responseFactory.createBuilder(gatewayResponse);
            return builder.build();
        } catch (GatewayClientException exception) {

            log.error("Error occurred while building the request, " + exception);
            return factory.handleException(exception).build();
        } catch (RuntimeException exception) {

            log.error("Error occurred while processing the request" + exception);
            if (factory == null) {
                log.error("Error occurred while finding a request factory for the request : " + exception);
                Response.ResponseBuilder builder = handleException(exception);
                return builder.build();
            }
            Response.ResponseBuilder builder = factory.handleException(exception);
            return builder.build();
        }
    }

    /**
     * Handler the exception that is not handle by the gateway and throw 500 status.
     *
     * @param exception
     * @return
     */
    public Response.ResponseBuilder handleException(RuntimeException exception) {

        Response.ResponseBuilder builder = Response.noContent();
        builder.status(500);
        builder.entity("Server Error: Something went wrong.");
        return builder;
    }


    /**
     * Find the Processor Implementation based on the GatewayRequest. If it is not there, throw GatewayRuntimeException
     * exception.
     *
     * @param gatewayRequest
     * @return
     */
    private GatewayProcessor getIdentityProcessor(GatewayRequest gatewayRequest) {
        List<GatewayProcessor> processors = GatewayResourceDataHolder.getInstance().getGatewayProcessors();

        for (GatewayProcessor requestProcessor : processors) {
            try {
                if (requestProcessor.canHandle(gatewayRequest)) {
                    return requestProcessor;
                }
            } catch (Exception e) {
                log.error("Error occurred while checking if " + requestProcessor.getName() + " can handle " +
                        gatewayRequest.toString());
            }
        }
        throw new GatewayRuntimeException("No GatewayProcessor found to process the request.");
    }

    /**
     * Find the RequestFactory based on Request. If not find any extended version of GatewayRequestBuilderFactory,
     * then it will pick up the GatewayRequestBuilderFactory as the default one.
     *
     * @param request
     * @return
     */
    private GatewayRequestBuilderFactory getIdentityRequestFactory(Request request) {

        List<GatewayRequestBuilderFactory> factories =
                GatewayResourceDataHolder.getInstance().getHttpIdentityRequestFactories();

        for (GatewayRequestBuilderFactory requestBuilder : factories) {
            try {
                if (requestBuilder.canHandle(request)) {
                    return requestBuilder;
                }
            } catch (GatewayException e) {
                log.error("Error occurred while checking the can handle in GatewayRequestBuilderFactory, " + e
                        .getMessage(), e);
            }
        }

        throw new GatewayRuntimeException("No GatewayResponseBuilderFactory found to create the request");
    }


    /**
     * Find the ResponseFactory based on GatewayResponse. If not find any extended version of
     * GatewayResponseBuilderFactory, then it will pick up the GatewayResponseBuilderFactory as the default one.
     *
     * @param gatewayResponse
     * @return
     */
    private GatewayResponseBuilderFactory getIdentityResponseFactory(GatewayResponse gatewayResponse) {

        List<GatewayResponseBuilderFactory> factories = GatewayResourceDataHolder.getInstance()
                .getHttpIdentityResponseFactories();

        for (GatewayResponseBuilderFactory responseFactory : factories) {
            if (responseFactory.canHandle(gatewayResponse)) {
                return responseFactory;
            }
        }
        throw new GatewayRuntimeException("No GatewayResponseBuilderFactory found to create the response.");
    }
}
