/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.endpoint;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.factories.ResourceTypeApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/resource-type")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(value = "/resource-type", description = "the resource-type API")
public class ResourceTypeApi {

    private final ResourceTypeApiService delegate = ResourceTypeApiServiceFactory.getResourceTypeApi();

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create the resource type.\n", notes = "This API is used to create a" +
            " new resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceTypePost(@ApiParam(value = "This represents the resource type to be added.", required =
            true) ResourceTypeAddDTO type) {

        return delegate.resourceTypePost(type);
    }

    @PUT

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create or replace the resource type.\n", notes = "This API is used " +
            "to create or replace a new resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceTypePut(@ApiParam(value = "This represents the resource type to be added.", required =
            true) ResourceTypeAddDTO type) {

        return delegate.resourceTypePut(type);
    }

    @DELETE
    @Path("/{resource-type-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Revoke resource type.\n", notes = "This API is used to delete an " +
            "existing resource type.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceTypeResourceTypeNameDelete(@ApiParam(value = "This represents the resource type to be " +
            "revoked.", required = true) @PathParam("resource-type-name") String resourceTypeName) {

        return delegate.resourceTypeResourceTypeNameDelete(resourceTypeName);
    }

    @GET
    @Path("/{resource-type-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the resource type.\n", notes = "This API is used to get an " +
            "existing resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceTypeResourceTypeNameGet(@ApiParam(value = "This represents the resource type to be " +
            "retrieved.", required = true) @PathParam("resource-type-name") String resourceTypeName) {

        return delegate.resourceTypeResourceTypeNameGet(resourceTypeName);
    }
}

