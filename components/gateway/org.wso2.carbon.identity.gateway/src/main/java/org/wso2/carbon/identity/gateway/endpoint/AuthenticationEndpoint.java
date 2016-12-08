/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.endpoint;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Authentication Endpoint MicroService.
 */
@Component(
        name = "org.wso2.carbon.identity.gateway.auth.endpoint",
        service = Microservice.class,
        immediate = true
)
@Path("/authenticate")
public class AuthenticationEndpoint implements Microservice {


    @GET
    @Path("/")
    public Response getLoginPage(@QueryParam("callback") String callback, @QueryParam("state") String sessionId) {
        String loginPage;

        try {
            loginPage = getLoginPageContent(callback, sessionId);
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


    private String getLoginPageContent(String callbackURL, String state) throws IOException {
        String response = AuthenticationEndpointUtils.getLoginPage();
        if (StringUtils.isNotBlank(state)) {
            response = response.replace("${state}", state);
        }

        if (StringUtils.isNotBlank(callbackURL)) {
            response = response.replace("${callback}", callbackURL);
        }

        return response;
    }


}
