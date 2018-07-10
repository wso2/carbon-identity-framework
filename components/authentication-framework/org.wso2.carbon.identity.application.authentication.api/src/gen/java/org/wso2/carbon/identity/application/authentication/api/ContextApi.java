/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.api;

import org.wso2.carbon.identity.application.authentication.api.dto.*;
import org.wso2.carbon.identity.application.authentication.api.ContextApiService;
import org.wso2.carbon.identity.application.authentication.api.factories.ContextApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.application.authentication.api.dto.ErrorDTO;
import org.wso2.carbon.identity.application.authentication.api.dto.ParametersDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/context")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/context", description = "the context API")
public class ContextApi  {

   private final ContextApiService delegate = ContextApiServiceFactory.getContextApi();

    @GET
    @Path("/{sessionKey}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve a authentication context parameter.\n", notes = "This API is used to retrieve parameters set by authentication framework to be accessed by authentication endpoint\n", response = ParametersDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response getContextParameters(@ApiParam(value = "This represents the Revoke Receipt ID.",required=true ) @PathParam("sessionKey")  String sessionKey,
    @ApiParam(value = "Comma separated list of parameters to filter. If none provided all available parameters will be sent.") @QueryParam("parameters")  String parameters)
    {
    return delegate.getContextParameters(sessionKey,parameters);
    }
}

