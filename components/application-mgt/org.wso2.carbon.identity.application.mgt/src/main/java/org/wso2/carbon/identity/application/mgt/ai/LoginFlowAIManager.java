package org.wso2.carbon.identity.application.mgt.ai;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AI Manager interface for the LoginFlowAI module.
 */
public interface LoginFlowAIManager {

    String generateAuthenticationSequence(String userQuery, JSONArray userClaims, JSONObject availableAuthenticators)
            throws LoginFlowAIServerException, LoginFlowAIClientException;

    Object getAuthenticationSequenceGenerationStatus(String operationId) throws LoginFlowAIServerException,
            LoginFlowAIClientException;

    Object getAuthenticationSequenceGenerationResult(String operationId) throws LoginFlowAIServerException,
            LoginFlowAIClientException;
}
