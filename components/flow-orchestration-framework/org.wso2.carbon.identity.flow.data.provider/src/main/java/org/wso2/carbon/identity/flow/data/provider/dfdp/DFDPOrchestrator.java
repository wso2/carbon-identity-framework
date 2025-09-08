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
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponse;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponseBuilder;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponseFormatter;

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

            // Step 4: Execute REAL authenticator flow with external IdP
            DFDPExecutionResult executionResult = executeRealIdPAuthentication(request, response, context);
            
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

        // Extract optional output format
        String outputFormat = request.getParameter(org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants.DFDP.OUTPUT_FORMAT);

        // Store validated parameters in context
        context.setProperty(FrameworkConstants.DFDP_TARGET_IDP, targetIdP.trim());
        if (targetAuthenticator != null && !targetAuthenticator.trim().isEmpty()) {
            context.setProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR, targetAuthenticator.trim());
        }
        if (testClaims != null && !testClaims.trim().isEmpty()) {
            context.setProperty(FrameworkConstants.DFDP_TEST_CLAIMS, testClaims.trim());
        }
        if (outputFormat != null && !outputFormat.trim().isEmpty()) {
            context.setProperty(org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants.DFDP.OUTPUT_FORMAT, outputFormat.trim().toLowerCase());
        }

        if (log.isDebugEnabled()) {
            log.debug("DFDP parameters validated - Target IdP: " + targetIdP + 
                     ", Target Authenticator: " + targetAuthenticator + 
                     ", Test Claims: " + testClaims + 
                     ", Output Format: " + outputFormat);
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

        try {
            // Get output format from request or default to JSON
            String format = (String) context.getProperty(org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants.DFDP.OUTPUT_FORMAT);
            if (format == null) {
                format = "json";
            }

            // Build comprehensive DFDP response using the response builder
            DFDPResponse dfdpResponse = DFDPResponseBuilder.buildResponse(
                executionResult, 
                context,
                "DFDP execution completed successfully"
            );

            // Use response formatter to write the response
            DFDPResponseFormatter.writeResponse(dfdpResponse, response, format);

            if (log.isDebugEnabled()) {
                log.debug("DFDP success response sent for request ID: " + executionResult.getRequestId() +
                         " in format: " + format);
            }

        } catch (Exception e) {
            log.error("Error sending DFDP response for request: " + executionResult.getRequestId(), e);
            
            // Fallback to simple error response
            sendDFDPErrorResponse(response, context, executionResult.getRequestId(), 
                                "Error generating response: " + e.getMessage());
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

        try {
            // Get output format from request or default to JSON
            String format = (String) context.getProperty(org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants.DFDP.OUTPUT_FORMAT);
            if (format == null) {
                format = "json";
            }

            // Build error response using the response builder
            DFDPResponse dfdpResponse = DFDPResponseBuilder.buildErrorResponse(
                requestId, 
                "DFDP_EXECUTION_ERROR",
                errorMessage,
                context
            );

            // Use response formatter to write the error response
            DFDPResponseFormatter.writeResponse(dfdpResponse, response, format);

            if (log.isDebugEnabled()) {
                log.debug("DFDP error response sent for request ID: " + requestId + 
                         ", error: " + errorMessage + ", format: " + format);
            }

        } catch (Exception e) {
            log.error("Error sending DFDP error response for request: " + requestId, e);
            
            // Ultimate fallback to simple JSON error
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String fallbackResponse = "{\"requestId\":\"" + requestId + "\"," +
                                    "\"status\":\"FAILED\"," +
                                    "\"error\":\"Internal error generating response\"}";
            response.getWriter().write(fallbackResponse);
            response.getWriter().flush();
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

    /**
     * Executes REAL authentication flow with external IdP for DFDP testing.
     * This method initiates an actual authentication request to the configured
     * external IdP and captures the real claims returned for testing claim mappings.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response  
     * @param context Authentication context
     * @return DFDPExecutionResult containing real IdP response data
     * @throws FrameworkException if real IdP authentication fails
     */
    public DFDPExecutionResult executeRealIdPAuthentication(HttpServletRequest request, 
                                                           HttpServletResponse response, 
                                                           AuthenticationContext context) 
                                                           throws FrameworkException {

        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);
        long startTime = System.currentTimeMillis();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting REAL IdP authentication flow for DFDP request: " + requestId);
            }

            // Create execution result to track real authentication
            DFDPExecutionResult result = new DFDPExecutionResult();
            result.setRequestId(requestId);
            result.setStatus("REAL_IDP_EXECUTION");

            // Extract DFDP parameters
            String targetIdP = request.getParameter("targetIdP");
            String targetAuthenticator = request.getParameter("targetAuthenticator");
            
            if (targetIdP == null) {
                targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
            }
            if (targetAuthenticator == null) {
                targetAuthenticator = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR);
            }

            result.setTargetIdP(targetIdP);
            result.setAuthenticatorName(targetAuthenticator);

            // Lookup REAL IdP configuration from WSO2 IS
            IdentityProvider targetIdentityProvider = lookupIdentityProvider(targetIdP, context);
            if (targetIdentityProvider == null) {
                throw new FrameworkException("Target Identity Provider not found: " + targetIdP);
            }

            // Execute REAL authenticator flow - this will make actual calls to external IdP
            Map<String, String> realIdPClaims = executeRealAuthenticatorFlow(targetIdentityProvider, 
                                                                             targetAuthenticator, 
                                                                             context, 
                                                                             request, 
                                                                             response);
            result.setRetrievedClaims(realIdPClaims);

            // Apply actual claim mappings using WSO2 IS configuration
            Map<String, String> mappedClaims = applyClaimMappings(realIdPClaims, targetIdentityProvider, context);
            result.setMappedClaims(mappedClaims);

            // Calculate execution metrics
            long executionTime = System.currentTimeMillis() - startTime;
            result.setExecutionTimeMs(executionTime);

            if (log.isDebugEnabled()) {
                log.debug("REAL IdP authentication flow completed for DFDP request: " + requestId + 
                         " in " + executionTime + "ms");
            }

            return result;

        } catch (Exception e) {
            log.error("REAL IdP authentication flow failed for DFDP request: " + requestId, e);
            throw new FrameworkException("DFDP real IdP authentication failed", e);
        }
    }

    /**
     * Executes the real authenticator flow making actual calls to external IdP.
     * This method uses the configured authenticator to make real authentication
     * requests and capture the actual claims returned by the external IdP.
     * 
     * @param targetIdentityProvider Target Identity Provider configuration
     * @param targetAuthenticator Target authenticator name
     * @param context Authentication context
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @return Map of real claims returned by external IdP
     * @throws FrameworkException if real authenticator execution fails
     */
    private Map<String, String> executeRealAuthenticatorFlow(IdentityProvider targetIdentityProvider, 
                                                            String targetAuthenticator,
                                                            AuthenticationContext context,
                                                            HttpServletRequest request, 
                                                            HttpServletResponse response) 
                                                            throws FrameworkException {
        
        if (log.isDebugEnabled()) {
            log.debug("Executing REAL authenticator flow for IdP: " + targetIdentityProvider.getIdentityProviderName() +
                     " with authenticator: " + targetAuthenticator);
        }

        try {
            // Setup step configuration for real authenticator execution
            StepConfig stepConfig = new StepConfig();
            stepConfig.setOrder(1);
            stepConfig.setSubjectStep(true);
            stepConfig.setAttributeStep(true);

            // Get the actual authenticator instance from WSO2 IS
            org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator authenticator = 
                authenticatorSetup.getConfiguredAuthenticator(targetIdentityProvider, targetAuthenticator, context);
            
            if (authenticator == null) {
                throw new FrameworkException("Authenticator not found: " + targetAuthenticator);
            }

            // Execute the REAL authenticator process method
            // This will make actual HTTP calls to the external IdP
            authenticator.process(request, response, context);

            // Extract REAL claims from the authentication context after IdP response
            Map<String, String> realClaims = extractClaimsFromAuthenticationContext(context);

            if (log.isDebugEnabled()) {
                log.debug("Successfully extracted REAL claims from IdP " + targetIdentityProvider.getIdentityProviderName() + 
                         ": " + realClaims.size() + " claims received");
            }

            return realClaims;

        } catch (Exception e) {
            log.error("Failed to execute real authenticator flow for IdP: " + targetIdentityProvider.getIdentityProviderName(), e);
            throw new FrameworkException("Real authenticator execution failed", e);
        }
    }

    /**
     * Extracts REAL claims from the authentication context after external IdP response.
     * This method retrieves the actual claims that were returned by the external IdP
     * during the real authentication process.
     * 
     * @param context Authentication context containing IdP response
     * @return Map of real claims from external IdP
     */
    private Map<String, String> extractClaimsFromAuthenticationContext(AuthenticationContext context) {
        
        Map<String, String> realClaims = new HashMap<>();
        
        try {
            // Extract claims from the authentication context
            // This gets the REAL claims returned by the external IdP
            if (context.getSequenceConfig() != null && 
                context.getSequenceConfig().getStepMap() != null) {
                
                for (StepConfig stepConfig : context.getSequenceConfig().getStepMap().values()) {
                    if (stepConfig.getAuthenticatedUser() != null && 
                        stepConfig.getAuthenticatedUser().getUserAttributes() != null) {
                        
                        // Get user attributes (claims) from the authenticated user
                        Map<org.wso2.carbon.identity.application.common.model.ClaimMapping, String> userAttributes = 
                            stepConfig.getAuthenticatedUser().getUserAttributes();
                        
                        for (Map.Entry<org.wso2.carbon.identity.application.common.model.ClaimMapping, String> entry : userAttributes.entrySet()) {
                            String claimUri = entry.getKey().getRemoteClaim().getClaimUri();
                            String claimValue = entry.getValue();
                            realClaims.put(claimUri, claimValue);
                        }
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Extracted " + realClaims.size() + " real claims from authentication context");
            }

        } catch (Exception e) {
            log.warn("Failed to extract real claims from authentication context", e);
        }

        return realClaims;
    }

