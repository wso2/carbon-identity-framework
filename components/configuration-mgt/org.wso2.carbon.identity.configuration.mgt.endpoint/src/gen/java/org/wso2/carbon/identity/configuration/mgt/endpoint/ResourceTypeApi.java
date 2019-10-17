package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.*;
import org.wso2.carbon.identity.configuration.mgt.endpoint.ResourceTypeApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.factories.ResourceTypeApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceTypeAddDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/resource-type")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/resource-type", description = "the resource-type API")
public class ResourceTypeApi  {

   private final ResourceTypeApiService delegate = ResourceTypeApiServiceFactory.getResourceTypeApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create resource type\n", notes = "This API is used to create a new resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceTypePost(@ApiParam(value = "This represents the name of the resource type that is to be added." ,required=true ) ResourceTypeAddDTO type)
    {
    return delegate.resourceTypePost(type);
    }
    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create or replace resource type\n", notes = "This API is used to create or replace a resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceTypePut(@ApiParam(value = "This represents the name of the resource type that is to be added/replaced." ,required=true ) ResourceTypeAddDTO type)
    {
    return delegate.resourceTypePut(type);
    }
    @DELETE
    @Path("/{resource-type-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke resource type\n", notes = "This API is used to delete an existing resource type.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceTypeResourceTypeNameDelete(@ApiParam(value = "This represents the name of the resource type that is to be revoked.",required=true ) @PathParam("resource-type-name")  String resourceTypeName)
    {
    return delegate.resourceTypeResourceTypeNameDelete(resourceTypeName);
    }
    @GET
    @Path("/{resource-type-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve resource type\n", notes = "This API is used to retrieve an existing resource type.\n", response = ResourceTypeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceTypeResourceTypeNameGet(@ApiParam(value = "This represents the name of the resource type that is to be retrieved.",required=true ) @PathParam("resource-type-name")  String resourceTypeName)
    {
    return delegate.resourceTypeResourceTypeNameGet(resourceTypeName);
    }
}

