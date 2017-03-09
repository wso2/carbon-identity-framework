/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.resource;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.gateway.resource.util.Utils.processParameters;

/**
 * GatewayResource is a MicroService.
 * All the request that is coming to the gateway are captured by this service.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.resource.GatewayResource",
        service = Microservice.class,
        immediate = true
)
@Path("/gateway")
public class GatewayResource implements Microservice {


    private GatewayManager gatewayManager = new GatewayManager();


    @GET
    @Path("/endpoint")
    public Response endpoint(@QueryParam("callback") String callback,
                             @QueryParam("state") String sessionDataKey) {
        if (StringUtils.isBlank(callback)) {
            return handleBadRequest("Mandatory 'callback' parameter is missing in the request parameters.");
        }

        if (StringUtils.isBlank(sessionDataKey)) {
            return handleBadRequest("Mandatory 'sessionDataKey' parameter missing in request parameters.");
        }

        String loginPage;
        try {
            loginPage = getLoginPageContent(callback, sessionDataKey);
        } catch (IOException e) {
            return Response.serverError().build();
        }

        return Response
                .ok()
                .entity(loginPage)
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .header(HttpHeaders.CONTENT_LENGTH, loginPage.getBytes().length)
                .build();
    }


    private Response handleBadRequest(String errorMessage) {

        return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
    }

    /**
     * All the GET request are come to this API and process by the GatewayManager.
     *
     * @param request
     * @return
     */
    @GET
    @Path("/")
    public Response processGet(@Context Request request) {
        processParameters(request);
        Response response = this.gatewayManager.execute(request);
        return response;
    }


    /**
     * All the POST request are come to this API and process by the GatewayManager.
     *
     * @param request
     *         is an MSF4J request.
     * @return Response
     */
    @POST
    @Path("/")
    public Response processPost(@Context Request request) {
        processParameters(request);
        Response response = this.gatewayManager.execute(request);
        return response;
    }

    private String getLoginPageContent(String callbackURL, String state) throws IOException {

        String response =  getLoginPage();
        if (StringUtils.isNotBlank(state)) {
            response = response.replace("${sessionDataKey}", state);
        }

        if (StringUtils.isNotBlank(callbackURL)) {
            response = response.replace("${callback}", callbackURL);
        }

        return response;
    }

    static String getLoginPage() throws IOException {

        InputStream inputStream = GatewayResource.class.getClassLoader()
                .getResourceAsStream(DEFAULT_LOGIN_PAGE);

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }
    private static final String DEFAULT_LOGIN_PAGE = "login.html";

}

