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

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.identity.ai.service.mgt.exceptions.AIServerException;

import java.util.Map;

/**
 * AI Manager interface for the LoginFlowAI module.
 */
public interface LoginFlowAIManager {

    /**
     * Generates an authentication sequence using the LoginFlow AI service.
     *
     * @param userQuery               The user query. This is a string that contain the requested authentication
     *                                flow by the user.
     * @param userClaimsMetaData      The user claims metadata. This is a JSON array that contains the user
     *                                claims available
     *                                for that organization.
     * @param availableAuthenticators The available authenticators of the organization.
     * @return Operation ID of the generated authentication sequence.
     * @throws AIServerException When a server error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When a client error occurs while generating the authentication sequence.
     */
    String generateAuthenticationSequence(String userQuery, JSONArray userClaimsMetaData,
                                          JSONObject availableAuthenticators)
            throws AIServerException, AIClientException;

    /**
     * Retrieves the status of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return A Json representation of the status' that are completed, pending, or failed.
     * @throws AIServerException When a server error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When a client error occurs while retrieving the authentication sequence
     */
    Map<String, Object> getAuthenticationSequenceGenerationStatus(String operationId) throws AIServerException,
            AIClientException;

    /**
     * Retrieves the result of the authentication sequence generation operation.
     *
     * @param operationId The operation ID of the authentication sequence generation operation.
     * @return The result of the authentication sequence generation operation.
     * @throws AIServerException When a server error occurs while connecting to the LoginFlow AI service.
     * @throws AIClientException When a client error occurs while retrieving the authentication sequence
     */
    Map<String, Object> getAuthenticationSequenceGenerationResult(String operationId) throws AIServerException,
            AIClientException;
}
