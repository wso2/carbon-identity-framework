package org.wso2.carbon.identity.application.mgt.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.ai.service.mgt.exceptions.AIClientException;
import org.wso2.carbon.ai.service.mgt.exceptions.AIServerException;

import java.util.Map;

/**
 * AI Manager interface for the LoginFlowAI module.
 */
public interface LoginFlowAIManager {

    String generateAuthenticationSequence(String userQuery, JSONArray userClaims, JSONObject availableAuthenticators)
            throws AIServerException, AIClientException;

    Map<String, Object> getAuthenticationSequenceGenerationStatus(String operationId) throws AIServerException,
            AIClientException;

    Map<String, Object> getAuthenticationSequenceGenerationResult(String operationId) throws AIServerException,
            AIClientException;
}
