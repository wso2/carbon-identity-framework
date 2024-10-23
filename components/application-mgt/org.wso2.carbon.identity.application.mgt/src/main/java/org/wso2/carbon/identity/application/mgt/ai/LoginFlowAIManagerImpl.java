/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.mgt.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.ai.service.mgt.util.AIHttpClientUtil.executeRequest;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE;

/**
 * Implementation of the LoginFlowAIManager interface to communicate with the LoginFlowAI service.
 */
public class LoginFlowAIManagerImpl implements LoginFlowAIManager {

    private static final String LOGINFLOW_AI_ENDPOINT = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIEndpoint");
    private static final String LOGINFLOW_AI_GENERATE_PATH = "/api/server/v1/applications/loginflow/generate";
    private static final String LOGINFLOW_AI_STATUS_PATH = "/api/server/v1/applications/loginflow/status";
    private static final String LOGINFLOW_AI_RESULT_PATH = "/api/server/v1/applications/loginflow/result";

    private static final Log LOG = LogFactory.getLog(LoginFlowAIManagerImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates an authentication sequence using the LoginFlow AI service.
     *
     * @param userQuery               The user query. This is a string that contain the requested authentication
     *                                flow by the user.
     * @param userClaims              The user claims. This is a JSON array that contains the user claims available
     *                                for that organization.
     * @param availableAuthenticators The available authenticators of the organization.
     * @return Operation ID of the generated authentication sequence.
     * @throws AIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When an error occurs while generating the authentication sequence.
     */
    @Override
    public String generateAuthenticationSequence(String userQuery, JSONArray userClaims,
                                                 JSONObject availableAuthenticators) throws AIServerException,
            AIClientException {

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_query", userQuery);
        try {
            // Convert JSONArray to List.
            List<Object> userClaimsList = objectMapper.readValue(userClaims.toString(), List.class);
            requestBody.put("user_claims", userClaimsList);

            // Convert JSONObject to Map.
            Map<String, Object> authenticatorsMap = objectMapper.readValue(availableAuthenticators.toString(),
                    Map.class);
            requestBody.put("available_authenticators", authenticatorsMap);
        } catch (JsonSyntaxException | IOException e) {
            throw new AIClientException("Error occurred while parsing the user claims or available " +
                    "authenticators.", SERVER_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getCode(), e);
        }

        Map<String, Object> stringObjectMap = executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_GENERATE_PATH,
                HttpPost.class, requestBody);
        return (String) stringObjectMap.get("operation_id");
    }

    /**
     * Retrieves the status of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return A Json representation of the status' that are completed, pending, or failed.
     * @throws AIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When an error occurs while retrieving the authentication sequence
     *                                    generation status.
     */
    @Override
    public Map<String, Object> getAuthenticationSequenceGenerationStatus(String operationId) throws AIServerException,
            AIClientException {

        return executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_STATUS_PATH + "/" + operationId, HttpGet.class, null);
    }

    /**
     * Retrieves the result of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return The result of the authentication sequence generation operation.
     * @throws AIServerException When an error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When an error occurs while retrieving the authentication sequence
     *                                    generation result.
     */
    @Override
    public Map<String, Object> getAuthenticationSequenceGenerationResult(String operationId) throws AIServerException,
            AIClientException {

        return executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_RESULT_PATH + "/" + operationId, HttpGet.class, null);
    }
}
