/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.endpoint;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourcesDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.factories.ResourceApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/resource")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(value = "/resource", description = "the resource API")
public class ResourceApi {

    private final ResourceApiService delegate = ResourceApiServiceFactory.getResourceApi();

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve resources.\n", notes = "This API is used to retrieve " +
            "resources.\n", response = ResourcesDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceGet() {

        return delegate.resourceGet();
    }

    @PATCH
    @Path("/{resource-type}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update existing resource\n", notes = "This API is used to update an" +
            " existing resource given by the user.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypePatch(@ApiParam(value = "This represents the type of the resource to be added" +
            " and can either be the name or id.", required = true) @PathParam("resource-type") String resourceType,
                                              @ApiParam(value = "This represents the resource that needs to be " +
                                                      "updated.", required = true) ResourceAddDTO resource) {

        return delegate.resourceResourceTypePatch(resourceType, resource);
    }

    @POST
    @Path("/{resource-type}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create the resource\n", notes = "This API is used to store the " +
            "resource given by the user.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypePost(@ApiParam(value = "This represents the type of the resource to be added" +
            ".", required = true) @PathParam("resource-type") String resourceType,
                                             @ApiParam(value = "This represents the resource that needs to be added."
                                                     , required = true) ResourceAddDTO resource) {

        return delegate.resourceResourceTypePost(resourceType, resource);
    }

    @PUT
    @Path("/{resource-type}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add or Replace the resource\n", notes = "This API is used to store " +
            "or replace the resource given by the user.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypePut(@ApiParam(value = "This represents the type of the resource to be added " +
            "and can either be the name or id.", required = true) @PathParam("resource-type") String resourceType,
                                            @ApiParam(value = "This represents the resource that need to be added or " +
                                                    "replaced.", required = true) ResourceAddDTO resource) {

        return delegate.resourceResourceTypePut(resourceType, resource);
    }

    @DELETE
    @Path("/{resource-type}/{resource-name}/{attribute-key}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Revoke the attribute\n", notes = "This API is used to revoke a " +
            "attribute in the tenant domain given by the user.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNameAttributeKeyDelete(@ApiParam(value = "This represents the name of" +
            " the attribute to be retrieved.", required = true) @PathParam("resource-name") String resourceName,
                                                                       @ApiParam(value = "This represents the type of" +
                                                                               " the attribute to be deleted and can " +
                                                                               "either be the name or id.", required
                                                                               = true) @PathParam("resource-type") String resourceType,
                                                                       @ApiParam(value = "This represents an " +
                                                                               "attribute key of the attribute to be " +
                                                                               "deleted.", required = true) @PathParam("attribute-key") String attributeKey) {

        return delegate.resourceResourceTypeResourceNameAttributeKeyDelete(resourceName, resourceType, attributeKey);
    }

    @GET
    @Path("/{resource-type}/{resource-name}/{attribute-key}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve the attribute.\n", notes = "This API is used to retrieve a" +
            " attribute.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNameAttributeKeyGet(@ApiParam(value = "This represents the name of " +
            "the attribute to be retrieved.", required = true) @PathParam("resource-name") String resourceName,
                                                                    @ApiParam(value = "This represents the type of " +
                                                                            "the attribute to be retrieved and can " +
                                                                            "either be the name or id.", required =
                                                                            true) @PathParam("resource-type") String resourceType,
                                                                    @ApiParam(value = "This represents an attribute " +
                                                                            "key of the attribute to be retrieved.",
                                                                            required = true) @PathParam("attribute" +
                                                                            "-key") String attributeKey) {

        return delegate.resourceResourceTypeResourceNameAttributeKeyGet(resourceName, resourceType, attributeKey);
    }

    @DELETE
    @Path("/{resource-type}/{resource-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Revoke the resource\n", notes = "This API is used to revoke a " +
            "resource in the tenant domain given by the user.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNameDelete(@ApiParam(value = "This represents the name of the " +
            "resource to be revoked.", required = true) @PathParam("resource-name") String resourceName,
                                                           @ApiParam(value = "This represents the type of the " +
                                                                   "resource to be added and can either be the name " +
                                                                   "or id.", required = true) @PathParam("resource" +
                                                                   "-type") String resourceType) {

        return delegate.resourceResourceTypeResourceNameDelete(resourceName, resourceType);
    }

    @GET
    @Path("/{resource-type}/{resource-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve the resource.\n", notes = "This API is used to retrieve a " +
            "resource.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNameGet(@ApiParam(value = "This represents the name of the resource " +
            "to be retrieved.", required = true) @PathParam("resource-name") String resourceName,
                                                        @ApiParam(value = "This represents the type of the resource " +
                                                                "to be added and can either be the name or id.",
                                                                required = true) @PathParam("resource-type") String resourceType) {

        return delegate.resourceResourceTypeResourceNameGet(resourceName, resourceType);
    }

    @PATCH
    @Path("/{resource-type}/{resource-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update existing attribute\n", notes = "This API is used to update " +
            "an existing attribute given by the user.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNamePatch(@ApiParam(value = "This represents the name of the " +
            "attribute to be added or updated.", required = true) @PathParam("resource-name") String resourceName,
                                                          @ApiParam(value = "This represents the type of the " +
                                                                  "attribute to be added or updated and can either be" +
                                                                  " the name or id.", required = true) @PathParam(
                                                                          "resource-type") String resourceType,
                                                          @ApiParam(value = "This represents the corresponding " +
                                                                  "attribute value that needs to be added or updated" +
                                                                  ".", required = true) AttributeDTO attribute) {

        return delegate.resourceResourceTypeResourceNamePatch(resourceName, resourceType, attribute);
    }

    @POST
    @Path("/{resource-type}/{resource-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Create the attribute\n", notes = "This API is used to store the " +
            "attribute given by the user.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNamePost(@ApiParam(value = "This represents the name of the attribute" +
            " to be added.", required = true) @PathParam("resource-name") String resourceName,
                                                         @ApiParam(value = "This represents the type of the attribute" +
                                                                 " to be added and can either be the name or id.",
                                                                 required = true) @PathParam("resource-type") String resourceType,
                                                         @ApiParam(value = "This represents the corresponding " +
                                                                 "attribute value that needs to be added.", required
                                                                 = true) AttributeDTO attribute) {

        return delegate.resourceResourceTypeResourceNamePost(resourceName, resourceType, attribute);
    }

    @PUT
    @Path("/{resource-type}/{resource-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add or Replace the attribute\n", notes = "This API is used to store" +
            " or replace the attribute given by the user.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceNamePut(@ApiParam(value = "This represents the name of the attribute " +
            "to be added or replaced.", required = true) @PathParam("resource-name") String resourceName,
                                                        @ApiParam(value = "This represents the type of the attribute " +
                                                                "to be added or replaced and can either be the name " +
                                                                "or id.", required = true) @PathParam("resource-type") String resourceType,
                                                        @ApiParam(value = "This represents the corresponding " +
                                                                "attribute value that needs to be added or replaced."
                                                                , required = true) AttributeDTO attribute) {

        return delegate.resourceResourceTypeResourceNamePut(resourceName, resourceType, attribute);
    }

    @GET
    @Path("/resource-type/{resource-type-name}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve all the resources for the given resource type.\n", notes =
            "This API is used to retrieve all the resources for a given resource type.\n", response =
            ResourcesDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error")})

    public Response resourceResourceTypeResourceTypeNameGet(@ApiParam(value = "This represents the name of the " +
            "resource type to be retrieved.", required = true) @PathParam("resource-type-name") String resourceTypeName) {

        return delegate.resourceResourceTypeResourceTypeNameGet(resourceTypeName);
    }
}

