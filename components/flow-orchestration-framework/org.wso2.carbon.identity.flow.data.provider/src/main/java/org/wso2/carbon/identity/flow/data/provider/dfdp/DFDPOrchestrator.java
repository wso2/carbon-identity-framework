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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DFDP Orchestrator.
 * This class coordinates the entire DFDP (Debug Flow Data Provider) flow, from initial request
 * processing to final response generation. It serves as the central coordinator for testing
 * external IdP claim mappings and authentication flows.
 */
public class DFDPOrchestrator implements DFDPService {

    private static final Log log = LogFactory.getLog(DFDPOrchestrator.class);
    
    private DFDPAuthenticatorSetup authenticatorSetup;

    public DFDPOrchestrator() {
        this.authenticatorSetup = new DFDPAuthenticatorSetup();
    }

    /**
     * Main entry point for DFDP request processing.
     * This method orchestrates the complete DFDP flow including parameter validation,
     * authenticator setup, execution, and response generation.
     * 
     * @param request HTTP servlet request containing DFDP parameters
     * @param response HTTP servlet response for sending results
     * @param context Authentication context for maintaining state
     * @throws FrameworkException if DFDP processing fails
     */
    @Override
    public void processDFDPRequest(HttpServletRequest request, HttpServletResponse response, 
                                  AuthenticationContext context) throws FrameworkException {

        String requestId = generateRequestId();
        context.setProperty(FrameworkConstants.DFDP_REQUEST_ID, requestId);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting DFDP request processing with ID: " + requestId);
            }

            // Step 1: Validate DFDP parameters
            validateDFDPParameters(request, context);

            // Step 2: Setup authenticator configuration
            authenticatorSetup.setupAuthenticatorForDFDP(context);

            // Step 3: Execute the authenticator flow directly
            executeDFDPAuthenticatorFlow(context);

            // Step 4: Send success response with claim data
            sendDFDPResponse(response, context, requestId);

            if (log.isDebugEnabled()) {
                log.debug("DFDP request processing completed successfully for ID: " + requestId);
            }

        } catch (Exception e) {
            log.error("Error processing DFDP request with ID: " + requestId, e);
            sendDFDPErrorResponse(response, context, requestId, e.getMessage());
            throw new FrameworkException("DFDP processing failed: " + e.getMessage(), e);
        }
    }

    /**
     * Validates required DFDP parameters from the request.
     * 
     * @param request HTTP servlet request
     * @param context Authentication context
     * @throws FrameworkException if validation fails
     */
    private void validateDFDPParameters(HttpServletRequest request, AuthenticationContext context) 
            throws FrameworkException {

        // Extract and validate target IdP
        String targetIdP = request.getParameter(FrameworkConstants.DFDP_TARGET_IDP);
        if (targetIdP == null || targetIdP.trim().isEmpty()) {
            throw new FrameworkException("Missing required parameter: " + FrameworkConstants.DFDP_TARGET_IDP);
        }

        // Extract optional target authenticator
        String targetAuthenticator = request.getParameter(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR);

        // Extract optional test claims
        String testClaims = request.getParameter(FrameworkConstants.DFDP_TEST_CLAIMS);

        // Store validated parameters in context
        context.setProperty(FrameworkConstants.DFDP_TARGET_IDP, targetIdP.trim());
        if (targetAuthenticator != null && !targetAuthenticator.trim().isEmpty()) {
            context.setProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR, targetAuthenticator.trim());
        }
        if (testClaims != null && !testClaims.trim().isEmpty()) {
            context.setProperty(FrameworkConstants.DFDP_TEST_CLAIMS, testClaims.trim());
        }

        if (log.isDebugEnabled()) {
            log.debug("DFDP parameters validated - Target IdP: " + targetIdP + 
                     ", Target Authenticator: " + targetAuthenticator + 
                     ", Test Claims: " + testClaims);
        }
    }

    /**
     * Executes the DFDP authenticator flow directly without going through normal authentication sequence.
     * This method simulates the authentication process to test claim mappings.
     * 
     * @param context Authentication context containing DFDP configuration
     * @throws FrameworkException if execution fails
     */
    private void executeDFDPAuthenticatorFlow(AuthenticationContext context) throws FrameworkException {

        StepConfig stepConfig = (StepConfig) context.getProperty(FrameworkConstants.DFDP_STEP_CONFIG);
        IdentityProvider identityProvider = (IdentityProvider) context.getProperty(FrameworkConstants.DFDP_IDENTITY_PROVIDER);

        if (stepConfig == null || identityProvider == null) {
            throw new FrameworkException("DFDP configuration not properly setup");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing DFDP authenticator flow for IdP: " + identityProvider.getIdentityProviderName());
            }

            // Simulate authentication execution and claim retrieval
            // In a real implementation, this would:
            // 1. Execute the configured authenticator
            // 2. Retrieve claims from the external IdP
            // 3. Apply claim mappings
            // 4. Store results for response

            // For now, we'll prepare mock data to demonstrate the concept
            Map<String, String> retrievedClaims = simulateClaimRetrieval(context);
            Map<String, String> mappedClaims = simulateClaimMapping(retrievedClaims, identityProvider);

            // Store results in context
            context.setProperty(FrameworkConstants.DFDP_RETRIEVED_CLAIMS, retrievedClaims);
            context.setProperty(FrameworkConstants.DFDP_MAPPED_CLAIMS, mappedClaims);
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_STATUS, FrameworkConstants.DFDPStatus.SUCCESS);

            if (log.isDebugEnabled()) {
                log.debug("DFDP authenticator flow executed successfully. Retrieved " + 
                         retrievedClaims.size() + " claims, mapped to " + mappedClaims.size() + " claims");
            }

        } catch (Exception e) {
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_STATUS, FrameworkConstants.DFDPStatus.FAILED);
            context.setProperty(FrameworkConstants.DFDP_ERROR_MESSAGE, e.getMessage());
            throw new FrameworkException("Failed to execute DFDP authenticator flow: " + e.getMessage(), e);
        }
    }

    /**
     * Simulates claim retrieval from external IdP.
     * In a real implementation, this would interact with the actual authenticator.
     * 
     * @param context Authentication context
     * @return Map of retrieved claims
     */
    private Map<String, String> simulateClaimRetrieval(AuthenticationContext context) {

        Map<String, String> claims = new HashMap<>();
        
        // Simulate some common claims that would be retrieved from external IdP
        claims.put("http://wso2.org/claims/emailaddress", "test.user@example.com");
        claims.put("http://wso2.org/claims/fullname", "Test User");
        claims.put("http://wso2.org/claims/givenname", "Test");
        claims.put("http://wso2.org/claims/lastname", "User");
        claims.put("http://wso2.org/claims/organization", "WSO2");

        // Add any test claims specified in the request
        String testClaims = (String) context.getProperty(FrameworkConstants.DFDP_TEST_CLAIMS);
        if (testClaims != null) {
            // Parse test claims format: "claim1=value1,claim2=value2"
            String[] claimPairs = testClaims.split(",");
            for (String claimPair : claimPairs) {
                String[] parts = claimPair.split("=", 2);
                if (parts.length == 2) {
                    claims.put(parts[0].trim(), parts[1].trim());
                }
            }
        }

        return claims;
    }

    /**
     * Simulates claim mapping process.
     * In a real implementation, this would use the actual claim mapping configuration.
     * 
     * @param retrievedClaims Claims retrieved from external IdP
     * @param identityProvider Identity Provider configuration
     * @return Map of mapped claims
     */
    private Map<String, String> simulateClaimMapping(Map<String, String> retrievedClaims, 
                                                    IdentityProvider identityProvider) {

        Map<String, String> mappedClaims = new HashMap<>();

        // Simulate claim mapping logic
        // In reality, this would use the IdP's claim mapping configuration
        for (Map.Entry<String, String> entry : retrievedClaims.entrySet()) {
            String claimUri = entry.getKey();
            String claimValue = entry.getValue();
            
            // Simple mapping simulation - in reality this would be configurable
            String mappedUri = claimUri; // Default to same URI
            if (claimUri.contains("emailaddress")) {
                mappedUri = "http://wso2.org/claims/username";
            }
            
            mappedClaims.put(mappedUri, claimValue);
        }

        return mappedClaims;
    }

    /**
     * Sends successful DFDP response with claim data.
     * 
     * @param response HTTP servlet response
     * @param context Authentication context containing results
     * @param requestId DFDP request ID
     * @throws IOException if response writing fails
     */
    private void sendDFDPResponse(HttpServletResponse response, AuthenticationContext context, 
                                 String requestId) throws IOException {

        Map<String, String> retrievedClaims = (Map<String, String>) context.getProperty(FrameworkConstants.DFDP_RETRIEVED_CLAIMS);
        Map<String, String> mappedClaims = (Map<String, String>) context.getProperty(FrameworkConstants.DFDP_MAPPED_CLAIMS);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{");
        jsonResponse.append("\"requestId\":\"").append(requestId).append("\",");
        jsonResponse.append("\"status\":\"").append(FrameworkConstants.DFDPStatus.SUCCESS).append("\",");
        jsonResponse.append("\"targetIdP\":\"").append(context.getProperty(FrameworkConstants.DFDP_TARGET_IDP)).append("\",");
        
        // Add retrieved claims
        jsonResponse.append("\"retrievedClaims\":{");
        if (retrievedClaims != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : retrievedClaims.entrySet()) {
                if (!first) jsonResponse.append(",");
                jsonResponse.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
        }
        jsonResponse.append("},");

        // Add mapped claims
        jsonResponse.append("\"mappedClaims\":{");
        if (mappedClaims != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
                if (!first) jsonResponse.append(",");
                jsonResponse.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
        }
        jsonResponse.append("}");
        jsonResponse.append("}");

        response.getWriter().write(jsonResponse.toString());
        response.getWriter().flush();

        if (log.isDebugEnabled()) {
            log.debug("DFDP success response sent for request ID: " + requestId);
        }
    }

    /**
     * Sends error response for DFDP failures.
     * 
     * @param response HTTP servlet response
     * @param context Authentication context
     * @param requestId DFDP request ID
     * @param errorMessage Error message
     * @throws IOException if response writing fails
     */
    private void sendDFDPErrorResponse(HttpServletResponse response, AuthenticationContext context, 
                                      String requestId, String errorMessage) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{");
        jsonResponse.append("\"requestId\":\"").append(requestId).append("\",");
        jsonResponse.append("\"status\":\"").append(FrameworkConstants.DFDPStatus.FAILED).append("\",");
        jsonResponse.append("\"error\":\"").append(errorMessage.replace("\"", "\\\"")).append("\"");
        jsonResponse.append("}");

        response.getWriter().write(jsonResponse.toString());
        response.getWriter().flush();

        if (log.isDebugEnabled()) {
            log.debug("DFDP error response sent for request ID: " + requestId + ", error: " + errorMessage);
        }
    }

    /**
     * Generates a unique request ID for DFDP operations.
     * 
     * @return Unique request ID
     */
    private String generateRequestId() {
        return "DFDP-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * Checks if the given request is a DFDP request.
     * 
     * @param request HTTP servlet request
     * @return true if this is a DFDP request, false otherwise
     */
    @Override
    public boolean isDFDPRequest(HttpServletRequest request) {
        String dfdpParam = request.getParameter(FrameworkConstants.DFDP_PARAM);
        return "true".equalsIgnoreCase(dfdpParam);
    }

    /**
     * Validates DFDP parameters in the request.
     * 
     * @param request HTTP servlet request
     * @throws FrameworkException if validation fails
     */
    @Override
    public void validateDFDPRequest(HttpServletRequest request) throws FrameworkException {
        String targetIdP = request.getParameter(FrameworkConstants.DFDP_TARGET_IDP);
        if (targetIdP == null || targetIdP.trim().isEmpty()) {
            throw new FrameworkException("Missing required parameter: " + FrameworkConstants.DFDP_TARGET_IDP);
        }
    }
}
