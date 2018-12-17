package org.wso2.carbon.identity.template.mgt.endpoint;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.GetTemplatesResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.dto.TemplateResponseDTO;
import org.wso2.carbon.identity.template.mgt.endpoint.factories.TemplatesApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/templates")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(value = "/templates", description = "the templates API")
public class TemplatesApi {

    private final TemplatesApiService delegate = TemplatesApiServiceFactory.getTemplatesApi();

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add a new template.\n", notes = "This API is used to store template information submitted by the user.\n", response = TemplateResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Created"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error")})

    public Response addTemplate(@ApiParam(value = "This represents the Template object that needs to be added to the database.", required = true) TemplateDTO template) {

        return delegate.addTemplate(template);
    }

    @DELETE
    @Path("/{templateName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Delete a template.", notes = "This API is used to delete a template.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 204, message = "No Content"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error")})

    public Response deleteTemplate(@ApiParam(value = "Name of the template to be deleted.", required = true) @PathParam("templateName") String templateName) {

        return delegate.deleteTemplate(templateName);
    }

    @GET
    @Path("/{templateName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Find template by Name.\n", notes = "This API is used to retrieve a specific template using the template name. \n", response = TemplateDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error")})

    public Response getTemplateByName(@ApiParam(value = "Name of the template to be retrieved.", required = true) @PathParam("templateName") String templateName) {

        return delegate.getTemplateByName(templateName);
    }

    @GET

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get an array of templates.", notes = "", response = GetTemplatesResponseDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error")})

    public Response getTemplates(@ApiParam(value = "Limits the number of templates listed on a page.") @QueryParam("limit") Integer limit,
                                 @ApiParam(value = "Specifies the starting point for the templates to be displayed.") @QueryParam("offset") Integer offset) {

        return delegate.getTemplates(limit, offset);
    }

    @POST
    @Path("/{templateName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an existing template. \n", notes = "This API is used to update a template. \n", response = TemplateResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found"),

            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error")})

    public Response updateTemplate(@ApiParam(value = "Name of the template to be updated.", required = true) @PathParam("templateName") String templateName,
                                   @ApiParam(value = "", required = true) TemplateDTO newTemplate) {

        return delegate.updateTemplate(templateName, newTemplate);
    }
}

