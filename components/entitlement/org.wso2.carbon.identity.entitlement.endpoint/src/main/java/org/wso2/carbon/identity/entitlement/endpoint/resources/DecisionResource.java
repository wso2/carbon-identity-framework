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
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.EntitledResultSetDTO;
import org.wso2.carbon.identity.entitlement.endpoint.exception.RequestParseException;
import org.wso2.carbon.identity.entitlement.endpoint.resources.models.*;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;
import org.wso2.carbon.identity.entitlement.endpoint.util.JSONRequestParser;
import org.wso2.carbon.identity.entitlement.endpoint.util.JSONResponseWriter;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.policy.search.PolicySearch;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * <p>
 *     Entry point class for the REST API end points
 * </p>
 */
@Path("/")
@Api(value = "/", description = "User REST for Integration Testing")
public class DecisionResource extends AbstractResource {
    private static Log log = LogFactory.getLog(DecisionResource.class);
    private static Gson gson = new Gson();

    @GET
    @Path("home")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    public HomeResponseModel getHome(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                          @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                          @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization){
        return new HomeResponseModel();
    }

    /**
     * API endpoint for evaluating XACML XML policies
     * @return XML Policy result String
     */
    @POST
    @Path("pdp")
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    @ApiOperation(value = "Get user details", response = String.class)
    public String getDecision(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                              @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                              @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                              @HeaderParam(EntitlementEndpointConstants.CONTENT_TYPE_HEADER) String contentType,
                              String xacmlRequest) throws Exception{

        if(log.isDebugEnabled()) {
            log.debug("recieved :" + xacmlRequest);
        }
        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        if(contentType.equals(EntitlementEndpointConstants.APPLICATION_XML)){
            return entitlementEngine.evaluate(xacmlRequest);
        }else if(contentType.equals(EntitlementEndpointConstants.APPLICATION_JSON)){
            RequestCtx requestCtx = JSONRequestParser.parse(xacmlRequest);
            ResponseCtx responseCtx = entitlementEngine.evaluateByContext(requestCtx);
            return gson.toJson(JSONResponseWriter.write(responseCtx));
        }
        return entitlementEngine.evaluate(xacmlRequest);

    }

    /**
     * API endpoint for evaluating policy by attributes as queries
     * @return XML Policy result string
     */
    @POST
    @Path("by-attrib")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public String getDecisionByAttributes(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                          @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                          @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                          DecisionRequestModel request) throws Exception{

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        return entitlementEngine.evaluate(request.getSubject(), request.getResource(),
                    request.getAction(), request.getEnvironment());


    }

    /**
     *API endpoint evaluating policy by using attributes as queries and return if true or false
     * @return Boolean
     */

    @POST
    @Path("by-attrib-boolean")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public boolean getBooleanDecision(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                      @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                      @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                      DecisionRequestModel request) throws Exception{

        EntitlementEngine entitlementEngine = EntitlementEngine.getInstance();

        String response = entitlementEngine.evaluate(request.getSubject(), request.getResource(),
                request.getAction(), null);
        return response.contains("Permit");

    }

    /**
     * API endpoint for returning entitled attributes for a give set of parameters
     * @return EntitledAttributesResponse object
     */
    @POST
    @Path("entitled-attribs")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public EntitledAttributesResponseModel getEntitledAttributes(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                                                 @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                                                 @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
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
     * @return AllEntitlementResponseModel object
     */
    @POST
    @Path("entitlements-all")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public AllEntitlementsResponseModel getAllEntitlements(@HeaderParam(EntitlementEndpointConstants.ACCEPT_HEADER) String format,
                                                           @HeaderParam(EntitlementEndpointConstants.AUTHENTICATION_TYPE_HEADER) String authMechanism,
                                                           @HeaderParam(EntitlementEndpointConstants.AUTHORIZATION_HEADER) String authorization,
                                                           AllEntitlementsRequestModel request) {

        if(log.isDebugEnabled()) {
            log.debug(request.getGivenAttributes()[0].getAttributeId());
            log.debug(request.getGivenAttributes()[0].getAttributeDataType());
            log.debug(request.getGivenAttributes()[0].getAttributeValue());
            log.debug(request.getGivenAttributes()[0].getCategory());
        }

        PolicySearch policySearch = EntitlementEngine.getInstance().getPolicySearch();

        EntitledResultSetDTO resultSet = policySearch.getEntitledAttributes(request.getIdentifier(), request.getGivenAttributes());
        AllEntitlementsResponseModel response = new AllEntitlementsResponseModel();
        response.setEntitledResultSetDTO(resultSet);
        return response;
    }

}
