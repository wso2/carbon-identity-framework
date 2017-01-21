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
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
     * @return Response
     */
    @POST
    @Path("/")
    public Response processPost(@Context Request request) {

        return null;
    }


    @GET
    @Path("/")
    public Response processGet(@Context Request request) {

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
    public Response callback(@Context Request request) {

        return null;
    }


    @GET
    @Path("/callback")
    public Response callbackGet(@Context Request request) {

        return callback(request);
    }


}
