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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.wso2.carbon.identity.user.registration.engine.UserRegistrationFlowService;
import org.wso2.carbon.identity.user.registration.engine.model.ExecutionState;
import org.wso2.carbon.identity.user.registration.engine.model.InputData;
import org.wso2.carbon.identity.user.registration.engine.util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Client servlet to handle the registration flow data.
 */
public class RegistrationPortalServlet extends HttpServlet {

    private static final long serialVersionUID = 5546734997561711495L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        try {
            // get request uri
            String uri = request.getRequestURI();
            if (uri.contains("/initiate")) {
                initiateFlow(request, response);
            } else {
                continueFlow(request, response);
            }
        } catch (Throwable e) {
            log("Error while processing the request.", e);
            buildStandardErrorResponse(response);
        }
    }

    private void initiateFlow(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            ExecutionState state = UserRegistrationFlowService.getInstance().initiateFlow("carbon.super");
            buildResponse(response, state);
        } catch (Throwable e) {
            log("Error while initiating the registration flow", e);
            buildStandardErrorResponse(response);
        }
    }

    private void continueFlow(HttpServletRequest request, HttpServletResponse response) throws IOException {

        /*
            { "flowId": "d13ec8d2-2d1e-11ee-be56-0242ac120002", "action": "EmailOTPVerifier", "inputs": { "username":
             "johndoe", "firstname": "John", "lastname": "Doe", "dob": "30/06/1995" } }
        */
        String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        JSONObject json = new JSONObject(requestBody);
        String flowId = json.getString("flowId");
        String action = json.getString("action");
        JSONObject inputs = json.getJSONObject("inputs");
        inputs.put("user-choice", action);

        // loop through the inputs and convert them to a map without using streams
        Map<String, String> inputMap = new HashMap<>();
        for (Object key : inputs.keySet()) {
            inputMap.put((String) key, inputs.getString((String) key));
        }
        InputData inputData = new InputData();
        inputData.setUserInput(inputMap);

        try {
            ExecutionState state = UserRegistrationFlowService.getInstance().continueFlow(flowId, inputData);
            buildResponse(response, state);
        } catch (Throwable e) {
            log("Error while continuing the registration flow", e);
            buildStandardErrorResponse(response);
        }
    }

    private void buildResponse(HttpServletResponse response, ExecutionState state) throws IOException {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        Gson gson = new Gson();

        data.put("flowId", state.getFlowId());
        if ("COMPLETE".equals(state.getResponse().getStatus())) {
            data.put("status", "COMPLETE");
            data.put("userAssertion", state.getResponse().getUserAssertion());
            String jsonString = gson.toJson(data);

            response.setContentType("application/json");
            response.getWriter().write(jsonString);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else if (Constants.STATUS_EXTERNAL_REDIRECTION.equals(state.getResponse().getStatus())) {
            data.put("status", Constants.STATUS_EXTERNAL_REDIRECTION);
            // Iterate the Map<String, String> returned from state.getResponse().getAdditionalProperties().
            // Convert it to a Map<String, Object> and add it to the data map.
            data.putAll(state.getResponse().getAdditionalInfo());
            data.put("requiredData", state.getResponse().getRequiredData());
            String jsonString = gson.toJson(data);

            response.setContentType("application/json");
            response.getWriter().write(jsonString);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            String pageContent = state.getResponse().getPage();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(pageContent);

            // Create a new ObjectNode and add flowID first
            ObjectNode modifiedJson = objectMapper.createObjectNode();
            modifiedJson.put("flowID", state.getFlowId());
            modifiedJson.put("status", state.getResponse().getStatus());
            modifiedJson.put("type", "registration");

            // Copy existing fields into the new object
            modifiedJson.set("elements", rootNode.get("elements"));
            modifiedJson.set("blocks", rootNode.get("blocks"));
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(modifiedJson));
            response.setStatus(HttpServletResponse.SC_OK);
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
