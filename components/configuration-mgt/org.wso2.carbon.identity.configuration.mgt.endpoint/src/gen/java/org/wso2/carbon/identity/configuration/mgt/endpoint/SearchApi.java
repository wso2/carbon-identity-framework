package org.wso2.carbon.identity.configuration.mgt.endpoint;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.*;
import org.wso2.carbon.identity.configuration.mgt.endpoint.SearchApiService;
import org.wso2.carbon.identity.configuration.mgt.endpoint.factories.SearchApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ResourcesDTO;
import org.wso2.carbon.identity.configuration.mgt.endpoint.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/search")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/search", description = "the search API")
public class SearchApi  {

 private final SearchApiService delegate = SearchApiServiceFactory.getSearchApi();

 @GET

 @Consumes({ "application/json" })
 @Produces({ "application/json" })
 @io.swagger.annotations.ApiOperation(value = "Retrieve tenant resources based on search parameters\n", notes = "This API is used to search resources across tenants based on search parameters given in the search query. For more information on using this API, see [Retrieving Tenant Resources Based on Search Parameters](https://docs.wso2.com/display/identity-server/Retrieving+Tenant+Resources+Based+on+Search+Parameters).\n", response = ResourcesDTO.class)
 @io.swagger.annotations.ApiResponses(value = {
         @io.swagger.annotations.ApiResponse(code = 200, message = "Ok"),

         @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request"),

         @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized"),

         @io.swagger.annotations.ApiResponse(code = 500, message = "Server Error") })

 public Response searchGet(@Context SearchContext searchContext)
 {
  return delegate.searchGet(searchContext);
 }
}
