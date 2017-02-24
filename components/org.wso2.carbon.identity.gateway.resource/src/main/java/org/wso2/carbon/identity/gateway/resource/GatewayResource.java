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

import org.osgi.service.component.annotations.Component;
import static org.wso2.carbon.identity.gateway.resource.util.Utils.processParameters;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * GatewayResource is a MicroService.
 * All the request that is coming to the gateway are captured by this service.
 *
 */
@Component(
        name = "org.wso2.carbon.identity.framework.resource.GatewayResource",
        service = Microservice.class,
        immediate = true
)
@Path("/gateway")
public class GatewayResource implements Microservice {


    private GatewayManager gatewayManager = new GatewayManager();


    /**
     * All the GET request are come to this API and process by the GatewayManager.
     *
     * @param request
     * @return
     */
    @GET
    @Path("/")
    public Response processGet(@Context Request request) {
        processParameters(request);
        Response response = this.gatewayManager.execute(request);
        return response;
    }


    /**
     * All the POST request are come to this API and process by the GatewayManager.
     *
     * @param request is an MSF4J request.
     * @return Response
     */
    @POST
    @Path("/")
    public Response processPost(@Context Request request) {
        processParameters(request);
        Response response = this.gatewayManager.execute(request);
        return response;
    }
}

