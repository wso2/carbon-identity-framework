package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.*;
import org.wso2.carbon.identity.configuration.mgt.endpoint.ResourceApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.factories.ResourceApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceAddDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.AttributeDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourceFileDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/resource")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/resource", description = "the resource API")
public class ResourceApi  {

   private final ResourceApiService delegate = ResourceApiServiceFactory.getResourceApi();

    @DELETE
    @Path("/file/{file-id}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke the file\n", notes = "This API is used to revoke a file in the tenant domain given by the user.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceFileFileIdDelete(@ApiParam(value = "This represents a file id of the file to be retrieved.",required=true ) @PathParam("file-id")  String fileId)
    {
    return delegate.resourceFileFileIdDelete(fileId);
    }
    @GET
    @Path("/file/{file-id}")
    @Consumes({ "application/json" })
    @Produces({ "application/octet-stream" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve the file.\n", notes = "This API is used to retrieve a file.\n", response = File.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceFileFileIdGet(@ApiParam(value = "This represents a file id of the file to be retrieved.",required=true ) @PathParam("file-id")  String fileId)
    {
    return delegate.resourceFileFileIdGet(fileId);
    }
    @GET
    @Path("/{resource-type}/file")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve the files related to resource types.\n", notes = "This API is used to retrieve files for resource types.\n", response = File.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeFileGet(@ApiParam(value = "This represents a resource type of the files to be retrieved.",required=true ) @PathParam("resource-type")  String resourceType)
    {
    return delegate.resourceResourceTypeFileGet(resourceType);
    }
    @POST
    @Path("/{resource-type}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create resource\n", notes = "This API is used to create a new resource.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypePost(@ApiParam(value = "This represents the type of resource that is to be added.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents the name of the resource that is to be added." ,required=true ) ResourceAddDTO resource)
    {
    return delegate.resourceResourceTypePost(resourceType,resource);
    }
    @PUT
    @Path("/{resource-type}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create or replace resource\n", notes = "This API is used to create or replace a resource.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypePut(@ApiParam(value = "This represents the type of resource that is to be added/replaced.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents the resource that is to be added/replaced." ,required=true ) ResourceAddDTO resource)
    {
    return delegate.resourceResourceTypePut(resourceType,resource);
    }
    @DELETE
    @Path("/{resource-type}/{resource-name}/{attribute-key}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke attribute\n", notes = "This API is used to revoke a attribute in the tenant domain for a given resource.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameAttributeKeyDelete(@ApiParam(value = "This represents the name of the attribute that is to be revoked.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of attribute that is to be revoked.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents a key of the attribute that is to be revoked.",required=true ) @PathParam("attribute-key")  String attributeKey)
    {
    return delegate.resourceResourceTypeResourceNameAttributeKeyDelete(resourceName,resourceType,attributeKey);
    }
    @GET
    @Path("/{resource-type}/{resource-name}/{attribute-key}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve attribute\n", notes = "This API is used to retrieve an attribute.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameAttributeKeyGet(@ApiParam(value = "This represents the name of the attribute that is to be retrieved.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of attribute that is to be retrieved.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents a key of the attribute that is to be retrieved.",required=true ) @PathParam("attribute-key")  String attributeKey)
    {
    return delegate.resourceResourceTypeResourceNameAttributeKeyGet(resourceName,resourceType,attributeKey);
    }
    @DELETE
    @Path("/{resource-type}/{resource-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke resource\n", notes = "This API is used to revoke a resource in the tenant domain given by the user.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameDelete(@ApiParam(value = "This represents the name of the resource that is to be revoked.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of resource that is to be added.",required=true ) @PathParam("resource-type")  String resourceType)
    {
    return delegate.resourceResourceTypeResourceNameDelete(resourceName,resourceType);
    }
    @DELETE
    @Path("/{resource-type}/{resource-name}/file")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Revoke all the files for the resource\n", notes = "This API is used to revoke all the files for the resource.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameFileDelete(@ApiParam(value = "This represents the name of the resource to be revoked.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of the resource to be added and can either be the name or id.",required=true ) @PathParam("resource-type")  String resourceType)
    {
    return delegate.resourceResourceTypeResourceNameFileDelete(resourceName,resourceType);
    }
    @GET
    @Path("/{resource-type}/{resource-name}/file")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve all the files for the resource.\n", notes = "This API is used to retrieve all the files for the resource.\n", response = ResourceFileDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameFileGet(@ApiParam(value = "This represents the name of the resource to be retrieved.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of the resource to be added and can either be the name or id.",required=true ) @PathParam("resource-type")  String resourceType)
    {
    return delegate.resourceResourceTypeResourceNameFileGet(resourceName,resourceType);
    }
    @POST
    @Path("/{resource-type}/{resource-name}/file")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a file\n", notes = "This API is used to store a file given by the user.\n", response = ResourceFileDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameFilePost(@ApiParam(value = "This represents the name of the attribute to be added.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of the attribute to be added and can either be the name or id.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents the corresponding resource file that needs to be added.") @Multipart(value = "resourceFile", required = false) InputStream resourceFileInputStream,
    @ApiParam(value = "This represents the corresponding resource file that needs to be added. : details") @Multipart(value = "resourceFile" , required = false) Attachment resourceFileDetail)
    {
    return delegate.resourceResourceTypeResourceNameFilePost(resourceName,resourceType,resourceFileInputStream,resourceFileDetail);
    }
    @GET
    @Path("/{resource-type}/{resource-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve resource\n", notes = "This API is used to retrieve a resource.\n", response = ResourceDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNameGet(@ApiParam(value = "This represents the name of the resource that is to be retrieved.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of resource that is to be added.",required=true ) @PathParam("resource-type")  String resourceType)
    {
    return delegate.resourceResourceTypeResourceNameGet(resourceName,resourceType);
    }
    @POST
    @Path("/{resource-type}/{resource-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create attribute\n", notes = "This API is used to create a new attribute for a given resource.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Successful response"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNamePost(@ApiParam(value = "This represents the name of the attribute that is to be added.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of attribute that is to be added.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents the corresponding attribute value that is to be added." ,required=true ) AttributeDTO attribute)
    {
    return delegate.resourceResourceTypeResourceNamePost(resourceName,resourceType,attribute);
    }
    @PUT
    @Path("/{resource-type}/{resource-name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create or Replace attribute\n", notes = "This API is used to create or replace an attribute for a given resource.\n", response = AttributeDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response resourceResourceTypeResourceNamePut(@ApiParam(value = "This represents the name of the attribute that is to be added or replaced.",required=true ) @PathParam("resource-name")  String resourceName,
    @ApiParam(value = "This represents the type of attribute that is to be added or replaced.",required=true ) @PathParam("resource-type")  String resourceType,
    @ApiParam(value = "This represents the corresponding attribute value that needs to be added or replaced." ,required=true ) AttributeDTO attribute)
    {
    return delegate.resourceResourceTypeResourceNamePut(resourceName,resourceType,attribute);
    }
}

