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
import org.wso2.carbon.identity.flow.data.provider.dfdp.integration.DFDPClaimProcessingIntegration;

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
    private DFDPAuthenticatorExecutor authenticatorExecutor;

    public DFDPOrchestrator() {
        this.authenticatorSetup = new DFDPAuthenticatorSetup();
        this.authenticatorExecutor = new DFDPAuthenticatorExecutor();
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
        long startTime = System.currentTimeMillis();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting DFDP request processing with ID: " + requestId);
            }

            // Step 1: Validate DFDP parameters
            validateDFDPParameters(request, context);

            // Step 2: Setup DFDP integration for claim processing
            DFDPClaimProcessingIntegration.setupDFDPContext(context, requestId);

            // Step 3: Setup authenticator configuration
            authenticatorSetup.setupAuthenticatorForDFDP(context);

            // Step 4: Execute the authenticator flow directly
            DFDPExecutionResult executionResult = authenticatorExecutor.executeAuthenticator(context, request, response);
            
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            executionResult.setExecutionTimeMs(executionTime);

            // Step 5: Send response with execution results
            sendDFDPResponseWithResults(response, context, executionResult);

            if (log.isDebugEnabled()) {
                log.debug("DFDP request processing completed successfully for ID: " + requestId + 
                         " in " + executionTime + "ms");
            }

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Error processing DFDP request with ID: " + requestId + " after " + executionTime + "ms", e);
            sendDFDPErrorResponse(response, context, requestId, e.getMessage());
            throw new FrameworkException("DFDP processing failed: " + e.getMessage(), e);
        } finally {
            // Cleanup DFDP integration context
            DFDPClaimProcessingIntegration.cleanupDFDPContext(context);
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
     * Sends successful DFDP response with execution results.
     * 
     * @param response HTTP servlet response
     * @param context Authentication context containing results
     * @param executionResult DFDP execution result
     * @throws IOException if response writing fails
     */
    private void sendDFDPResponseWithResults(HttpServletResponse response, AuthenticationContext context, 
                                           DFDPExecutionResult executionResult) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{");
        jsonResponse.append("\"requestId\":\"").append(executionResult.getRequestId()).append("\",");
        jsonResponse.append("\"status\":\"").append(executionResult.getStatus()).append("\",");
        jsonResponse.append("\"targetIdP\":\"").append(executionResult.getTargetIdP()).append("\",");
        jsonResponse.append("\"authenticatorName\":\"").append(executionResult.getAuthenticatorName()).append("\",");
        jsonResponse.append("\"executionTimeMs\":").append(executionResult.getExecutionTimeMs()).append(",");
        
        // Add retrieved claims
        jsonResponse.append("\"retrievedClaims\":{");
        Map<String, String> retrievedClaims = executionResult.getRetrievedClaims();
        if (retrievedClaims != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : retrievedClaims.entrySet()) {
                if (!first) jsonResponse.append(",");
                jsonResponse.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                          .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
        }
        jsonResponse.append("},");

        // Add mapped claims
        jsonResponse.append("\"mappedClaims\":{");
        Map<String, String> mappedClaims = executionResult.getMappedClaims();
        if (mappedClaims != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
                if (!first) jsonResponse.append(",");
                jsonResponse.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                          .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
        }
        jsonResponse.append("},");

        // Add authenticator properties
        jsonResponse.append("\"authenticatorProperties\":{");
        Map<String, String> authenticatorProperties = executionResult.getAuthenticatorProperties();
        if (authenticatorProperties != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : authenticatorProperties.entrySet()) {
                if (!first) jsonResponse.append(",");
                jsonResponse.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                          .append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
        }
        jsonResponse.append("},");

        // Add summary information
        jsonResponse.append("\"summary\":{");
        jsonResponse.append("\"retrievedClaimsCount\":").append(executionResult.getRetrievedClaimsCount()).append(",");
        jsonResponse.append("\"mappedClaimsCount\":").append(executionResult.getMappedClaimsCount()).append(",");
        jsonResponse.append("\"successful\":").append(executionResult.isSuccessful());
        
        // Add claim analysis summary if available
        DFDPClaimAnalysis claimAnalysis = executionResult.getClaimAnalysis();
        if (claimAnalysis != null) {
            jsonResponse.append(",\"claimAnalysis\":{");
            jsonResponse.append("\"processingStatus\":\"").append(escapeJson(claimAnalysis.getProcessingStatus())).append("\",");
            jsonResponse.append("\"totalValidations\":").append(claimAnalysis.getTotalValidations()).append(",");
            jsonResponse.append("\"errorValidations\":").append(claimAnalysis.getErrorValidations()).append(",");
            jsonResponse.append("\"warningValidations\":").append(claimAnalysis.getWarningValidations());
            
            if (claimAnalysis.getCoverage() != null) {
                jsonResponse.append(",\"coverage\":{");
                jsonResponse.append("\"percentage\":").append(claimAnalysis.getCoverage().getCoveragePercentage()).append(",");
                jsonResponse.append("\"level\":\"").append(escapeJson(claimAnalysis.getCoverage().getCoverageLevel())).append("\"");
                jsonResponse.append("}");
            }
            
            if (claimAnalysis.getCategories() != null) {
                jsonResponse.append(",\"categories\":\"").append(escapeJson(claimAnalysis.getCategories().getCategorySummary())).append("\"");
            }
            
            jsonResponse.append("}");
        }
        
        jsonResponse.append("}");
        
        jsonResponse.append("}");

        response.getWriter().write(jsonResponse.toString());
        response.getWriter().flush();

        if (log.isDebugEnabled()) {
            log.debug("DFDP success response sent for request ID: " + executionResult.getRequestId());
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
        jsonResponse.append("\"error\":\"").append(escapeJson(errorMessage)).append("\"");
        jsonResponse.append("}");

        response.getWriter().write(jsonResponse.toString());
        response.getWriter().flush();

        if (log.isDebugEnabled()) {
            log.debug("DFDP error response sent for request ID: " + requestId + ", error: " + errorMessage);
        }
    }

    /**
     * Escapes special characters in JSON strings.
     * 
     * @param input Input string
     * @return Escaped string
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
