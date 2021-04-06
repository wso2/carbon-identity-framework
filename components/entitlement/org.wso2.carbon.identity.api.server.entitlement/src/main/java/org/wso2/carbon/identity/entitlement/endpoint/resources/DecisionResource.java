/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.resources;

import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;
import org.wso2.carbon.identity.entitlement.endpoint.exception.ExceptionBean;
import org.wso2.carbon.identity.entitlement.endpoint.exception.RequestParseException;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.AllEntitlementsRequestModel;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.AllEntitlementsResponseModel;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.DecisionRequestModel;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.EntitledAttributesRequestModel;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.EntitledAttributesResponseModel;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.HomeResponseModel;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;
import org.wso2.carbon.identity.entitlement.endpoint.util.JSONRequestParser;
import org.wso2.carbon.identity.entitlement.endpoint.util.JSONResponseWriter;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.search.PolicySearch;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Entry point class for the REST API end points
 */
@Path("/")
@Api(value = "/", description = "Evaluate XACML 3.0 Policies")
public class DecisionResource extends AbstractResource {
    private static Log log = LogFactory.getLog(DecisionResource.class);
    private static Gson gson = new Gson();

    /**
     * API endpoint for populating accessible service methods
     * Complying to XACML 3.0 REST profile
     *
     * @return <code>{@link HomeResponseModel}</code> with all necessary resource links
     */
    @GET
    @Path("home")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get API resource list according to XACML 3.0 Specification",
            httpMethod = "GET")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Method call success", response = HomeResponseModel.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class)
    })
    public HomeResponseModel getHome(@ApiParam(value = "Request Media Type", required = true)
                                     @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                     @ApiParam(value = "Authentication Type", required = true)
                                     @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                     @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                                     @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                     @ApiParam(value = "Response Media Type", required = true)
                                     @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType) {
        return new HomeResponseModel();
    }

    /**
     * API endpoint for evaluating XACML XML policies
     *
     * @return XML Policy result String
     */
    @POST
    @Path("pdp")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get response by evaluating JSON/XML XACML request", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "XACML JSON/XML Response"),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40020, message = EntitlementEndpointConstants.ERROR_REQUEST_PARSE_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_RESPONSE_READ_MESSAGE,
                    response = ExceptionBean.class)
    })
    public String getDecision(@ApiParam(value = "Request Media Type", required = true)
                              @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                              @ApiParam(value = "Authentication Type", required = true)
                              @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                              @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                              @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                              @ApiParam(value = "Response Media Type", required = true)
                              @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                              @ApiParam(value = "XACML JSON/XML Request", required = true)
                                      String xacmlRequest) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("recieved :" + xacmlRequest);
        }
        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        if (contentType.equals(EntitlementEndpointConstants.APPLICATION_JSON)) {
            RequestCtx requestCtx = JSONRequestParser.parse(xacmlRequest);
            ResponseCtx responseCtx = entitlementEngine.evaluate(requestCtx, xacmlRequest);
            return gson.toJson(JSONResponseWriter.write(responseCtx));
        } else {
            return entitlementEngine.evaluate(xacmlRequest);
        }

    }

    /**
     * API endpoint for evaluating policy by attributes as queries
     *
     * @return XML Policy result string
     */
    @POST
    @Path("by-attrib")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get response by evaluating attributes", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "XACML JSON/XML Response"),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40020, message = EntitlementEndpointConstants.ERROR_REQUEST_PARSE_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_RESPONSE_READ_MESSAGE,
                    response = ExceptionBean.class)
    })
    public String getDecisionByAttributes(@ApiParam(value = "Request Media Type", required = true)
                                          @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                          @ApiParam(value = "Authentication Type", required = true)
                                          @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                          @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                                          @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                          @ApiParam(value = "Response Media Type", required = true)
                                          @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                                          @ApiParam(value = "Decision Request Model", required = true)
                                                  DecisionRequestModel request) throws Exception {

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        return entitlementEngine.evaluate(request.getSubject(), request.getResource(),
                request.getAction(), request.getEnvironment());


    }

    /**
     * API endpoint evaluating policy by using attributes as queries and return if true or false
     *
     * @return Boolean
     */

    @POST
    @Path("by-attrib-boolean")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get boolean response by evaluating attributes", response = Boolean.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Boolean response"),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40020, message = EntitlementEndpointConstants.ERROR_REQUEST_PARSE_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_RESPONSE_READ_MESSAGE,
                    response = ExceptionBean.class)
    })
    public boolean getBooleanDecision(@ApiParam(value = "Request Media Type", required = true)
                                      @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                      @ApiParam(value = "Authentication Type", required = true)
                                      @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                      @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                                      @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                      @ApiParam(value = "Response Media Type", required = true)
                                      @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                                      @ApiParam(value = "Decision Request Model", required = true)
                                              DecisionRequestModel request) throws Exception {

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        String response = entitlementEngine.evaluate(request.getSubject(), request.getResource(),
                request.getAction(), null);
        return response.contains("Permit");

    }

    /**
     * API endpoint for returning entitled attributes for a give set of parameters
     *
     * @return EntitledAttributesResponse object
     */
    @POST
    @Path("entitled-attribs")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get entitled attributes for a given set of parameters",
            response = EntitledAttributesResponseModel.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Entitled Attributes response",
                    response = EntitledAttributesResponseModel.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40020, message = EntitlementEndpointConstants.ERROR_REQUEST_PARSE_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_RESPONSE_READ_MESSAGE,
                    response = ExceptionBean.class)
    })
    public EntitledAttributesResponseModel getEntitledAttributes(@ApiParam(value = "Request Media Type", required = true)
                                                                 @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                                                 @ApiParam(value = "Authentication Type", required = true)
                                                                 @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                                                 @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                                                                 @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                                                 @ApiParam(value = "Response Media Type", required = true)
                                                                 @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                                                                 @ApiParam(value = "Entitled Attributes Model", required = true)
                                                                         EntitledAttributesRequestModel request) throws Exception {

        if (request.getSubjectName() == null) {
            log.error("Invalid input data - either the user name or role name should be non-null");
            throw new RequestParseException(40022,
                    "Invalid input data - either the user name or role name should be non-null");
        }


        PolicySearch policySearch = EntitlementEngine.getInstance().getPolicySearch();
        EntitledResultSetDTO resultsSet = policySearch.getEntitledAttributes(request.getSubjectName(), request.getResourceName(),
                request.getSubjectId(), request.getAction(), request.isEnableChildSearch());
        EntitledAttributesResponseModel response = new EntitledAttributesResponseModel();
        response.setEntitledResultSetDTO(resultsSet);
        return response;
    }

    /**
     * API endpoint for returning all entitlements for a given set of parameters
     *
     * @return AllEntitlementResponseModel object
     */
    @POST
    @Path("entitlements-all")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get all entitlements for a given set of parameters",
            response = AllEntitlementsResponseModel.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "All Entitlements response",
                    response = AllEntitlementsResponseModel.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_UNAUTHORIZED_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40020, message = EntitlementEndpointConstants.ERROR_REQUEST_PARSE_MESSAGE,
                    response = ExceptionBean.class),
            @ApiResponse(code = 40010, message = EntitlementEndpointConstants.ERROR_RESPONSE_READ_MESSAGE,
                    response = ExceptionBean.class)
    })
    public AllEntitlementsResponseModel getAllEntitlements(@ApiParam(value = "Request Media Type", required = true)
                                                           @HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                                           @ApiParam(value = "Authentication Type", required = true)
                                                           @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                                           @ApiParam(value = "Add HTTP Basic Authorization", required = true)
                                                           @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                                           @ApiParam(value = "Response Media Type", required = true)
                                                           @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                                                           @ApiParam(value = "All Entitlements Model", required = true)
                                                                   AllEntitlementsRequestModel request) {

        PolicySearch policySearch = EntitlementEngine.getInstance().getPolicySearch();

        EntitledResultSetDTO resultSet = policySearch.getEntitledAttributes(request.getIdentifier(), request.getGivenAttributes());
        AllEntitlementsResponseModel response = new AllEntitlementsResponseModel();
        response.setEntitledResultSetDTO(resultSet);
        return response;
    }

}
