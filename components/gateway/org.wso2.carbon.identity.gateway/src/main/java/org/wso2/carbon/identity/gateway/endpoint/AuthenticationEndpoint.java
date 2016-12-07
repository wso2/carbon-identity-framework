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

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.Microservice;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder.build;

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
        String loginPage = getLoginPageContent(callback, sessionId);

        return Response
                .ok()
                .entity(loginPage)
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .header(HttpHeaders.CONTENT_LENGTH, loginPage.getBytes().length)
                .build();
    }


    private String getLoginPageContent(String callbackURL, String state) {
        String response = AuthenticationEndpointUtils.getLoginPage();
        response = response.replace("${state}", state);
        response = response.replace("${callbackURL}", callbackURL);
        return response;
    }


}
