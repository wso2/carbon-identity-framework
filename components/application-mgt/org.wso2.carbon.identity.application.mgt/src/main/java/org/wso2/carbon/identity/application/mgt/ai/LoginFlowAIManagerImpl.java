/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.ai.service.mgt.util.AIHttpClientUtil.executeRequest;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.AUTHENTICATORS_PROPERTY;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.ErrorMessages.CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.OPERATION_ID_PROPERTY;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.USER_CLAIM_PROPERTY;
import static org.wso2.carbon.identity.application.mgt.ai.constant.LoginFlowAIConstants.USER_QUERY_PROPERTY;
import static org.wso2.carbon.registry.core.RegistryConstants.PATH_SEPARATOR;

/**
 * Implementation of the LoginFlowAIManager interface to communicate with the LoginFlowAI service.
 */
public class LoginFlowAIManagerImpl implements LoginFlowAIManager {

    private static final String LOGINFLOW_AI_ENDPOINT = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIEndpoint");
    private static final String LOGINFLOW_AI_GENERATE_PATH = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIGenerateRequestPath");
    private static final String LOGINFLOW_AI_STATUS_PATH = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIStatusRequestPath");
    private static final String LOGINFLOW_AI_RESULT_PATH = IdentityUtil.getProperty(
            "AIServices.LoginFlowAI.LoginFlowAIResultRequestPath");

    private static final Log LOG = LogFactory.getLog(LoginFlowAIManagerImpl.class);

    @Override
    public String generateAuthenticationSequence(String userQuery, JSONArray userClaimsMetaData,
                                                 JSONObject availableAuthenticators) throws AIServerException,
            AIClientException {

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(USER_QUERY_PROPERTY, userQuery);
        try {
            List<Object> userClaimsMetadataList = objectMapper.readValue(userClaimsMetaData.toString(), List.class);
            requestBody.put(USER_CLAIM_PROPERTY, userClaimsMetadataList);
            Map<String, Object> authenticatorsMap = objectMapper.readValue(availableAuthenticators.toString(),
                    Map.class);
            requestBody.put(AUTHENTICATORS_PROPERTY, authenticatorsMap);
        } catch (JsonSyntaxException | IOException e) {
            throw new AIClientException("Error occurred while parsing the user claims or available " +
                    "authenticators.", CLIENT_ERROR_WHILE_CONNECTING_TO_LOGINFLOW_AI_SERVICE.getCode(), e);
        }

        Map<String, Object> stringObjectMap = executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_GENERATE_PATH,
                HttpPost.class, requestBody);
        return (String) stringObjectMap.get(OPERATION_ID_PROPERTY);
    }

    @Override
    public Map<String, Object> getAuthenticationSequenceGenerationStatus(String operationId) throws  AIServerException,
            AIClientException {

        return executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_STATUS_PATH + PATH_SEPARATOR + operationId,
                HttpGet.class, null);
    }

    @Override
    public Map<String, Object> getAuthenticationSequenceGenerationResult(String operationId) throws AIServerException,
            AIClientException {

        return executeRequest(LOGINFLOW_AI_ENDPOINT, LOGINFLOW_AI_RESULT_PATH + PATH_SEPARATOR + operationId,
                HttpGet.class, null);
    }
}
