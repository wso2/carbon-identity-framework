/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.temp.servlet;

import com.google.gson.Gson;
import org.wso2.carbon.identity.user.registration.engine.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.engine.temp.ConfigDataHolder;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Server api to handle the registration orchestration config data.
 */
public class RegistrationOrchestrationServlet extends HttpServlet {

    private static final long serialVersionUID = 5546734997561711495L;
    private static final String superTenantDomain = "carbon.super";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                           IOException {

        try {
            // get json data from request
            String configData = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ConfigDataHolder.getInstance().getOrchestrationConfig().put(superTenantDomain, configData);


//            UserRegistrationServiceDataHolder.getRegistrationFlowMgtService().storeRegistrationFlow(configData,
//                    superTenantDomain);

            response.setContentType("application/json");
            response.getWriter().write("{\"org\":\"" + superTenantDomain + "\", \"config\":" + configData + "}");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            log("Error while creating the registration flow.", e);
            buildStandardErrorResponse(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                          IOException {

        try {

            String configData = ConfigDataHolder.getInstance().getOrchestrationConfig().get(superTenantDomain);

            response.setContentType("application/json");
            response.getWriter().write(configData);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            log("Error while getting the registration flow.", e);
            buildStandardErrorResponse(response);
        }
    }

    private void buildStandardErrorResponse(HttpServletResponse response) throws IOException {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("error", "Error occurred while processing the request. Check terminal for logs.");

        Gson gson = new Gson();
        String jsonString = gson.toJson(data);

        response.setContentType("application/json");
        response.getWriter().write(jsonString);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }
}
