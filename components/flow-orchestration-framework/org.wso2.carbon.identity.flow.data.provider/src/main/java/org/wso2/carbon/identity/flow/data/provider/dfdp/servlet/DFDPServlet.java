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

package org.wso2.carbon.identity.flow.data.provider.dfdp.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.flow.data.provider.dfdp.DFDPOrchestrator;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponse;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponseBuilder;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPResponseFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * DFDP Servlet.
 * Part 7: Response Generation - HTTP endpoint for DFDP operations.
 * 
 * This servlet provides a REST API endpoint for external applications to 
 * trigger DFDP (Debug Flow Data Provider) tests and receive formatted responses.
 * 
 * Usage:
 * POST /debug
 * GET /debug (for service info)
 * Parameters:
 * - targetIdP: Required. Name of the target Identity Provider
 * - targetAuthenticator: Optional. Specific authenticator to test
 * - testClaims: Optional. JSON string of test claims to use
 * - outputFormat: Optional. Response format (json, html, text, summary)
 * - action: Optional. Action to perform (test, validate, info, health)
 */
public class DFDPServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(DFDPServlet.class);
    private static final long serialVersionUID = 1L;

    private DFDPOrchestrator dfdpOrchestrator;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dfdpOrchestrator = new DFDPOrchestrator();
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP Servlet initialized");
        }
    }

    /**
     * Handles GET requests - returns service information by default.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O fails
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String action = request.getParameter("action");
        
        if ("health".equals(action)) {
            sendHealthCheck(response);
        } else {
            // Default to service info for GET requests
            sendServiceInfo(response);
        }
    }

    /**
     * Handles POST requests - processes DFDP requests based on action parameter.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet processing fails
     * @throws IOException if I/O fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String action = request.getParameter("action");
        
        if ("validate".equals(action)) {
            validateDFDPRequest(request, response);
        } else {
            // Default to test action for POST requests
            processDFDPTestRequest(request, response);
        }
    }

    /**
     * Processes DFDP test requests.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if I/O fails
     */
    private void processDFDPTestRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {

        String requestId = generateRequestId();
        
        try {
            if (log.isDebugEnabled()) {
                log.debug("Processing DFDP test request with ID: " + requestId);
            }

            // Create authentication context for DFDP processing
            AuthenticationContext context = new AuthenticationContext();
            context.setContextId(requestId);
            context.setProperty(FrameworkConstants.DFDP_REQUEST_ID, requestId);

            // Add DFDP parameter to mark this as a DFDP request
            request.setAttribute(FrameworkConstants.DFDP_PARAM, "true");

            // Process DFDP request using orchestrator
            dfdpOrchestrator.processDFDPRequest(request, response, context);

        } catch (FrameworkException e) {
            log.error("DFDP test request failed for ID: " + requestId, e);
            sendErrorResponse(response, requestId, "DFDP_TEST_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing DFDP test request: " + requestId, e);
            sendErrorResponse(response, requestId, "INTERNAL_ERROR", "Internal server error occurred");
        }
    }

    /**
     * Validates DFDP request parameters without executing.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if I/O fails
     */
    private void validateDFDPRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {

        String requestId = generateRequestId();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Validating DFDP request with ID: " + requestId);
            }

            // Validate using orchestrator
            dfdpOrchestrator.validateDFDPRequest(request);

            // Build validation success response
            DFDPResponse validationResponse = DFDPResponseBuilder.buildValidationResponse(
                requestId, 
                "DFDP request parameters are valid",
                extractRequestParameters(request)
            );

            // Send response
            String format = request.getParameter("outputFormat");
            DFDPResponseFormatter.writeResponse(validationResponse, response, format);

        } catch (FrameworkException e) {
            log.warn("DFDP request validation failed for ID: " + requestId + " - " + e.getMessage());
            sendErrorResponse(response, requestId, "VALIDATION_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error validating DFDP request: " + requestId, e);
            sendErrorResponse(response, requestId, "INTERNAL_ERROR", "Internal server error occurred");
        }
    }

    /**
     * Sends service information.
     * 
     * @param response HTTP response
     * @throws IOException if I/O fails
     */
    private void sendServiceInfo(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String serviceInfo = "{\n" +
                "  \"service\": \"DFDP (Debug Flow Data Provider)\",\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"description\": \"External IdP claim mapping testing service\",\n" +
                "  \"endpoint\": \"/debug\",\n" +
                "  \"methods\": {\n" +
                "    \"GET /debug\": \"Service information (default)\",\n" +
                "    \"GET /debug?action=health\": \"Health check\",\n" +
                "    \"POST /debug\": \"Execute DFDP test (default)\",\n" +
                "    \"POST /debug?action=validate\": \"Validate DFDP request parameters\"\n" +
                "  },\n" +
                "  \"parameters\": {\n" +
                "    \"targetIdP\": \"Required. Target Identity Provider name\",\n" +
                "    \"targetAuthenticator\": \"Optional. Specific authenticator to test\",\n" +
                "    \"testClaims\": \"Optional. JSON string of test claims\",\n" +
                "    \"outputFormat\": \"Optional. Response format (json, html, text, summary)\",\n" +
                "    \"action\": \"Optional. Action to perform (test, validate, health)\"\n" +
                "  }\n" +
                "}";

        response.getWriter().write(serviceInfo);
        response.getWriter().flush();
    }

    /**
     * Sends health check response.
     * 
     * @param response HTTP response
     * @throws IOException if I/O fails
     */
    private void sendHealthCheck(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        String healthInfo = "{\n" +
                "  \"status\": \"UP\",\n" +
                "  \"timestamp\": \"" + System.currentTimeMillis() + "\",\n" +
                "  \"checks\": {\n" +
                "    \"orchestrator\": \"" + (dfdpOrchestrator != null ? "OK" : "NOT_INITIALIZED") + "\"\n" +
                "  }\n" +
                "}";

        response.getWriter().write(healthInfo);
        response.getWriter().flush();
    }

    /**
     * Sends error response.
     * 
     * @param response HTTP response
     * @param requestId Request ID
     * @param errorCode Error code
     * @param errorMessage Error message
     * @throws IOException if I/O fails
     */
    private void sendErrorResponse(HttpServletResponse response, String requestId, 
                                 String errorCode, String errorMessage) throws IOException {
        try {
            AuthenticationContext context = new AuthenticationContext();
            context.setContextId(requestId);

            DFDPResponse errorResponse = DFDPResponseBuilder.buildErrorResponse(
                requestId, 
                errorCode, 
                errorMessage,
                context
            );

            DFDPResponseFormatter.writeResponse(errorResponse, response, "json");

        } catch (Exception e) {
            log.error("Error sending error response for request: " + requestId, e);
            
            // Fallback to simple JSON error
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            String fallbackResponse = "{\"requestId\":\"" + requestId + "\"," +
                                    "\"status\":\"FAILED\"," +
                                    "\"error\":\"" + escapeJson(errorMessage) + "\"}";
            response.getWriter().write(fallbackResponse);
            response.getWriter().flush();
        }
    }

    /**
     * Extracts request parameters for validation response.
     * 
     * @param request HTTP request
     * @return Parameters string
     */
    private String extractRequestParameters(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        params.append("targetIdP=").append(request.getParameter("targetIdP"));
        
        String targetAuth = request.getParameter("targetAuthenticator");
        if (targetAuth != null) {
            params.append(", targetAuthenticator=").append(targetAuth);
        }
        
        String testClaims = request.getParameter("testClaims");
        if (testClaims != null) {
            params.append(", testClaims=[provided]");
        }
        
        String outputFormat = request.getParameter("outputFormat");
        if (outputFormat != null) {
            params.append(", outputFormat=").append(outputFormat);
        }
        
        return params.toString();
    }

    /**
     * Generates a unique request ID.
     * 
     * @return Unique request ID
     */
    private String generateRequestId() {
        return "DFDP-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
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
}
