package org.wso2.carbon.identity.api.idpmgt.endpoint;

import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.*;
import org.wso2.carbon.identity.api.idpmgt.endpoint.IdpsApiService;
import org.wso2.carbon.identity.api.idpmgt.endpoint.factories.IdpsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPListDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/idps")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/idps", description = "the idps API")
public class IdpsApi  {

   private final IdpsApiService delegate = IdpsApiServiceFactory.getIdpsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List all IdPs\n", notes = "This operation provides you a list of available IdPs\n", response = IdPListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Service provider tenant domain") @QueryParam("spTenantDomain")  String spTenantDomain)
    {
    return delegate.idpsGet(limit,offset,spTenantDomain);
    }
    @GET
    @Path("/{name}/authenticators/{authName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Listing authenticators for a given identity provider\n", notes = "This operation lists authenticators for an identity provider\n", response = FederatedAuthenticatorConfigDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nRequested authenticator for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameAuthenticatorsAuthNameGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Authenticator Name",required=true ) @PathParam("authName")  String authName)
    {
    return delegate.idpsNameAuthenticatorsAuthNameGet(name,authName);
    }
    @PUT
    @Path("/{name}/authenticators/{authName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an authenticator in a given identity provider\n", notes = "This operation updates an authenticator for a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated Authenticator successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameAuthenticatorsAuthNamePut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Authenticator Name",required=true ) @PathParam("authName")  String authName,
    @ApiParam(value = "authenticator object that needs to be updated\n" ,required=true ) FederatedAuthenticatorConfigDTO body)
    {
    return delegate.idpsNameAuthenticatorsAuthNamePut(name,authName,body);
    }
    @GET
    @Path("/{name}/authenticators/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Listing authenticators for a given identity provider\n", notes = "This operation lists authenticators for an identity provider\n", response = FederatedAuthenticatorConfigDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of authenticators for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameAuthenticatorsGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset)
    {
    return delegate.idpsNameAuthenticatorsGet(name,limit,offset);
    }
    @GET
    @Path("/{name}/claim-config/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List claim configuration for a given identity provider\n", notes = "This operation lists claim configuration for a given identity provider\n", response = ClaimConfigDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of claim configurations for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameClaimConfigGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name)
    {
    return delegate.idpsNameClaimConfigGet(name);
    }
    @PUT
    @Path("/{name}/claim-config/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing claim configuration in a given identity provider\n", notes = "This operation updates an existing claim configuration in a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated Authenticator successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameClaimConfigPut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "claim object that needs to be updated\n" ,required=true ) ClaimConfigDTO body)
    {
    return delegate.idpsNameClaimConfigPut(name,body);
    }
    @DELETE
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete specified IdP from the IS\n", notes = "This operation deletes an existing IdP\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameDelete(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name)
    {
    return delegate.idpsNameDelete(name);
    }
    @GET
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search IdPs\n", notes = "This operation provides you a list of available IdPs\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name)
    {
    return delegate.idpsNameGet(name);
    }
    @GET
    @Path("/{name}/jit-provisioning/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List jit provisioning for a given identity provider\n", notes = "This operation lists jit provisioning for a given identity provider\n", response = JustInTimeProvisioningConfigDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of jit provisioning for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameJitProvisioningGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name)
    {
    return delegate.idpsNameJitProvisioningGet(name);
    }
    @PUT
    @Path("/{name}/jit-provisioning/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update jit provisioning in a given identity provider\n", notes = "This operation updates jit provisioning in a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated Role successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameJitProvisioningPut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "role object that needs to be updated\n" ,required=true ) JustInTimeProvisioningConfigDTO body)
    {
    return delegate.idpsNameJitProvisioningPut(name,body);
    }
    @DELETE
    @Path("/{name}/outbound-provisioning-connector-configs/{connectorName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete out bound connector for a given identity provider\n", notes = "This operation delete given outbound connector for a given identity provider\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\ndelete outbound connector for the IdP.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameDelete(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Connector Name",required=true ) @PathParam("connectorName")  String connectorName)
    {
    return delegate.idpsNameOutboundProvisioningConnectorConfigsConnectorNameDelete(name,connectorName);
    }
    @GET
    @Path("/{name}/outbound-provisioning-connector-configs/{connectorName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Outbound connector for a given identity provider\n", notes = "This operation lists outbound connector for a given identity provider\n", response = ProvisioningConnectorConfigDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of outbound connectors for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNameGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Connector Name",required=true ) @PathParam("connectorName")  String connectorName)
    {
    return delegate.idpsNameOutboundProvisioningConnectorConfigsConnectorNameGet(name,connectorName);
    }
    @PUT
    @Path("/{name}/outbound-provisioning-connector-configs/{connectorName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update outbound connector in a given identity provider\n", notes = "This operation updates outbound connector for a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated Role successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameOutboundProvisioningConnectorConfigsConnectorNamePut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Connector Name",required=true ) @PathParam("connectorName")  String connectorName,
    @ApiParam(value = "outbound connector that needs to be updated\n" ,required=true ) ProvisioningConnectorConfigDTO body)
    {
    return delegate.idpsNameOutboundProvisioningConnectorConfigsConnectorNamePut(name,connectorName,body);
    }
    @GET
    @Path("/{name}/outbound-provisioning-connector-configs/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List out bound connectors for a given identity provider\n", notes = "This operation lists outbound connectors for a given identity provider\n", response = ProvisioningConnectorConfigDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of outbound connectors for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameOutboundProvisioningConnectorConfigsGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset)
    {
    return delegate.idpsNameOutboundProvisioningConnectorConfigsGet(name,limit,offset);
    }
    @POST
    @Path("/{name}/outbound-provisioning-connector-configs/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an outbound connector for a given identity provider\n", notes = "This operation adds a given outbound connector for a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nadd a new outbound connector for the IdP.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNameOutboundProvisioningConnectorConfigsPost(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "role object that needs to be added\n" ,required=true ) ProvisioningConnectorConfigDTO body)
    {
    return delegate.idpsNameOutboundProvisioningConnectorConfigsPost(name,body);
    }
    @GET
    @Path("/{name}/permission-and-role-config/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List permission-and-role-config for a given identity provider\n", notes = "This operation lists permission-and-role-config for a given identity provider\n", response = PermissionsAndRoleConfigDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nList of permission-and-role-config for the IdP returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNamePermissionAndRoleConfigGet(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name)
    {
    return delegate.idpsNamePermissionAndRoleConfigGet(name);
    }
    @PUT
    @Path("/{name}/permission-and-role-config/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update role configuration in a given identity provider\n", notes = "This operation updates role configuration in a given identity provider\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated Role successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNamePermissionAndRoleConfigPut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "role object that needs to be updated\n" ,required=true ) PermissionsAndRoleConfigDTO body)
    {
    return delegate.idpsNamePermissionAndRoleConfigPut(name,body);
    }
    @PUT
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing IdP\n", notes = "This operation updates an existing IdP\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. updated IdP successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

    public Response idpsNamePut(@ApiParam(value = "The unique identifier of a receipt",required=true ) @PathParam("name")  String name,
    @ApiParam(value = "IdP object that needs to be updated\n" ,required=true ) IdPDetailDTO body)
    {
    return delegate.idpsNamePut(name,body);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Create a new IdP", notes = "This operation can be used to create a new IdP specifying the details of the API in the payload.\n", response = IdPDetailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),
        
        @io.swagger.annotations.ApiResponse(code = 415, message = "Unsupported Media Type.\nThe entity of the request was in a not supported format.\n") })

    public Response idpsPost(@ApiParam(value = "IdP object that needs to be added\n" ,required=true ) IdPDetailDTO body)
    {
    return delegate.idpsPost(body);
    }
}

