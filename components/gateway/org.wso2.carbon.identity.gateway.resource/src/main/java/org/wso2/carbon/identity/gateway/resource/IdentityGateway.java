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
import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.exception.FrameworkClientException;
import org.wso2.carbon.identity.framework.exception.FrameworkException;
import org.wso2.carbon.identity.framework.exception.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.request.IdentityRequest;
import org.wso2.carbon.identity.framework.response.HttpIdentityResponse;
import org.wso2.carbon.identity.framework.response.IdentityResponse;
import org.wso2.carbon.identity.framework.response.factory.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.resource.internal.DataHolder;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import java.util.Optional;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.framework.response.HttpIdentityResponse.HttpIdentityResponseBuilder;

/**
 * Identity Gateway MicroService.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.resource.IdentityGateway",
        service = Microservice.class,
        immediate = true
)
@Path("/identity")
public class IdentityGateway implements Microservice {

    private static Logger logger = LoggerFactory.getLogger(IdentityGateway.class);
    private GatewayHelper helper = GatewayHelper.getInstance();

    @POST
    @Path("/")
    public Response process(@Context Request request, String body) {
        HttpIdentityResponse response = processRequest(request);
        return buildResponse(response);
    }


    private HttpIdentityResponse processRequest(Request request) {

        // pick the correct Identity Request Factory from registered ones.
        MSF4JIdentityRequestFactory requestFactory = helper.pickRequestFactory(request);

        IdentityRequest identityRequest;
        HttpIdentityResponseBuilder responseBuilder;

        // build the canonical IdentityRequest from the MSF4J Request.
        try {
            identityRequest = requestFactory.create(request).build();
            if (identityRequest == null) {
                throw FrameworkRuntimeException.error("IdentityRequest is Null. Cannot proceed!!");
            }
        } catch (FrameworkClientException e) {
            responseBuilder = requestFactory.handleException(e, request);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }


        IdentityProcessCoordinator processCoordinator = helper.getIdentityProcessCoordinator();
        IdentityResponse identityResponse = null;
        HttpIdentityResponseFactory responseFactory = null;

        try {
            identityResponse = processCoordinator.process(identityRequest);
            if (identityResponse == null) {
                throw FrameworkRuntimeException.error("IdentityResponse is Null. Cannot proceed!!");
            }

            responseFactory = helper.pickIdentityResponseFactory(identityResponse);
            responseBuilder = responseFactory.create(identityResponse);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        } catch (FrameworkException e) {
            responseFactory = helper.pickIdentityResponseFactory(e);
            responseBuilder = responseFactory.handleException(e);
            if (responseBuilder == null) {
                throw FrameworkRuntimeException.error("HttpIdentityResponseBuilder is Null. Cannot proceed!!");
            }
            return responseBuilder.build();
        }
    }


    private Response buildResponse(HttpIdentityResponse identityResponse) {
        return Response.ok().entity(identityResponse.getBody()).build();
    }




}
