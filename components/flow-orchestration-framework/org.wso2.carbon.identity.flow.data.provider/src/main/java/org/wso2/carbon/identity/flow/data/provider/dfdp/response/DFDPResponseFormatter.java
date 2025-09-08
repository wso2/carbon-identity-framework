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

package org.wso2.carbon.identity.flow.data.provider.dfdp.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * DFDP Response Formatter.
 * Part 7: Response Generation - Formats DFDP responses for different output types.
 */
public class DFDPResponseFormatter {

    private static final Log log = LogFactory.getLog(DFDPResponseFormatter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    /**
     * Formats and writes DFDP response to HTTP response.
     * 
     * @param dfdpResponse DFDP response
     * @param httpResponse HTTP servlet response
     * @param format Output format (json, xml, html, text)
     * @throws IOException if writing fails
     */
    public static void writeResponse(DFDPResponse dfdpResponse, HttpServletResponse httpResponse, 
                                   String format) throws IOException {
        if (format == null || format.trim().isEmpty()) {
            format = "json";
        }

        format = format.toLowerCase().trim();

        switch (format) {
            case "json":
                writeJsonResponse(dfdpResponse, httpResponse);
                break;
            case "html":
                writeHtmlResponse(dfdpResponse, httpResponse);
                break;
            case "text":
                writeTextResponse(dfdpResponse, httpResponse);
                break;
            case "summary":
                writeSummaryResponse(dfdpResponse, httpResponse);
                break;
            default:
                writeJsonResponse(dfdpResponse, httpResponse);
        }
    }

    /**
     * Writes JSON formatted response.
     * 
     * @param dfdpResponse DFDP response
     * @param httpResponse HTTP response
     * @throws IOException if writing fails
     */
    private static void writeJsonResponse(DFDPResponse dfdpResponse, HttpServletResponse httpResponse) 
            throws IOException {
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");

        if (dfdpResponse.isSuccessful()) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        String jsonResponse = objectMapper.writeValueAsString(dfdpResponse);
        
        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
    }

    /**
     * Writes HTML formatted response.
     * 
     * @param dfdpResponse DFDP response
     * @param httpResponse HTTP response
     * @throws IOException if writing fails
     */
    private static void writeHtmlResponse(DFDPResponse dfdpResponse, HttpServletResponse httpResponse) 
            throws IOException {
        httpResponse.setContentType("text/html");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>DFDP Test Results</title>");
            writer.println("<style>");
            writer.println(getHtmlStyles());
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writeHtmlContent(writer, dfdpResponse);
            
            writer.println("</body>");
            writer.println("</html>");
            writer.flush();
        }
    }

    /**
     * Writes text formatted response.
     * 
     * @param dfdpResponse DFDP response
     * @param httpResponse HTTP response
     * @throws IOException if writing fails
     */
    private static void writeTextResponse(DFDPResponse dfdpResponse, HttpServletResponse httpResponse) 
            throws IOException {
        httpResponse.setContentType("text/plain");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.println("DFDP Test Results");
            writer.println("=================");
            writer.println();
            
            writer.println("Request ID: " + dfdpResponse.getRequestId());
            writer.println("Status: " + dfdpResponse.getStatus());
            writer.println("Message: " + dfdpResponse.getMessage());
            writer.println("Execution Time: " + dfdpResponse.getExecutionTimeSeconds() + " seconds");
            writer.println();

            if (dfdpResponse.getTestSummary() != null) {
                writeTextTestSummary(writer, dfdpResponse.getTestSummary());
            }

            if (dfdpResponse.getClaimAnalysis() != null) {
                writeTextClaimAnalysis(writer, dfdpResponse.getClaimAnalysis());
            }

            if (dfdpResponse.hasErrors()) {
                writer.println("Errors:");
                writer.println("-------");
                for (DFDPError error : dfdpResponse.getErrors()) {
                    writer.println("- " + error.getCode() + ": " + error.getMessage());
                }
                writer.println();
            }

            if (dfdpResponse.hasWarnings()) {
                writer.println("Warnings:");
                writer.println("---------");
                for (DFDPWarning warning : dfdpResponse.getWarnings()) {
                    writer.println("- " + warning.getCode() + ": " + warning.getMessage());
                }
                writer.println();
            }

            writer.flush();
        }
    }

    /**
     * Writes summary response.
     * 
     * @param dfdpResponse DFDP response
     * @param httpResponse HTTP response
     * @throws IOException if writing fails
     */
    private static void writeSummaryResponse(DFDPResponse dfdpResponse, HttpServletResponse httpResponse) 
            throws IOException {
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setStatus(HttpServletResponse.SC_OK);

        // Create summary object
        DFDPResponseSummary summary = new DFDPResponseSummary();
        summary.setRequestId(dfdpResponse.getRequestId());
        summary.setStatus(dfdpResponse.getStatus());
        summary.setExecutionTimeSeconds(dfdpResponse.getExecutionTimeSeconds());
        summary.setSuccessful(dfdpResponse.isSuccessful());
        summary.setErrorCount(dfdpResponse.getErrorCount());
        summary.setWarningCount(dfdpResponse.getWarningCount());

        if (dfdpResponse.getTestSummary() != null) {
            summary.setTotalClaims(dfdpResponse.getTestSummary().getTotalClaims());
            summary.setSuccessfulMappings(dfdpResponse.getTestSummary().getSuccessfulMappings());
            summary.setFailedMappings(dfdpResponse.getTestSummary().getFailedMappings());
        }

        String jsonResponse = objectMapper.writeValueAsString(summary);
        
        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
    }

    /**
     * Writes HTML content.
     * 
     * @param writer Print writer
     * @param dfdpResponse DFDP response
     */
    private static void writeHtmlContent(PrintWriter writer, DFDPResponse dfdpResponse) {
        writer.println("<div class='container'>");
        writer.println("<h1>DFDP Test Results</h1>");
        
        // Status section
        String statusClass = dfdpResponse.isSuccessful() ? "success" : "error";
        writer.println("<div class='status " + statusClass + "'>");
        writer.println("<h2>Status: " + dfdpResponse.getStatus() + "</h2>");
        writer.println("<p>" + dfdpResponse.getMessage() + "</p>");
        writer.println("<p>Execution Time: " + dfdpResponse.getExecutionTimeSeconds() + " seconds</p>");
        writer.println("</div>");

        // Test summary
        if (dfdpResponse.getTestSummary() != null) {
            writeHtmlTestSummary(writer, dfdpResponse.getTestSummary());
        }

        // Claim analysis
        if (dfdpResponse.getClaimAnalysis() != null) {
            writeHtmlClaimAnalysis(writer, dfdpResponse.getClaimAnalysis());
        }

        // Errors and warnings
        writeHtmlErrorsAndWarnings(writer, dfdpResponse);

        writer.println("</div>");
    }

    /**
     * Gets HTML styles.
     * 
     * @return CSS styles
     */
    private static String getHtmlStyles() {
        return "body { font-family: Arial, sans-serif; margin: 20px; }" +
               ".container { max-width: 1200px; margin: 0 auto; }" +
               ".status { padding: 15px; border-radius: 5px; margin-bottom: 20px; }" +
               ".status.success { background-color: #d4edda; border: 1px solid #c3e6cb; color: #155724; }" +
               ".status.error { background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }" +
               ".section { margin-bottom: 30px; }" +
               ".claims-table { width: 100%; border-collapse: collapse; }" +
               ".claims-table th, .claims-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }" +
               ".claims-table th { background-color: #f2f2f2; }" +
               ".error-list, .warning-list { list-style-type: none; padding: 0; }" +
               ".error-item { background-color: #f8d7da; padding: 10px; margin: 5px 0; border-radius: 5px; }" +
               ".warning-item { background-color: #fff3cd; padding: 10px; margin: 5px 0; border-radius: 5px; }";
    }

    /**
     * Writes HTML test summary.
     * 
     * @param writer Print writer
     * @param testSummary Test summary
     */
    private static void writeHtmlTestSummary(PrintWriter writer, DFDPTestSummary testSummary) {
        writer.println("<div class='section'>");
        writer.println("<h2>Test Summary</h2>");
        writer.println("<p><strong>Target IdP:</strong> " + testSummary.getTargetIdP() + "</p>");
        writer.println("<p><strong>Total Claims:</strong> " + testSummary.getTotalClaims() + "</p>");
        writer.println("<p><strong>Successful Mappings:</strong> " + testSummary.getSuccessfulMappings() + "</p>");
        writer.println("<p><strong>Failed Mappings:</strong> " + testSummary.getFailedMappings() + "</p>");
        writer.println("<p><strong>Success Rate:</strong> " + 
                      String.format("%.2f%%", testSummary.getMappingSuccessRate()) + "</p>");
        writer.println("</div>");
    }

    /**
     * Writes HTML claim analysis.
     * 
     * @param writer Print writer
     * @param claimAnalysis Claim analysis
     */
    private static void writeHtmlClaimAnalysis(PrintWriter writer, DFDPClaimAnalysis claimAnalysis) {
        writer.println("<div class='section'>");
        writer.println("<h2>Claim Analysis</h2>");
        
        if (claimAnalysis.getOriginalClaims() != null && !claimAnalysis.getOriginalClaims().isEmpty()) {
            writer.println("<h3>Original Claims</h3>");
            writeHtmlClaimsTable(writer, claimAnalysis.getOriginalClaims());
        }

        if (claimAnalysis.getFinalClaims() != null && !claimAnalysis.getFinalClaims().isEmpty()) {
            writer.println("<h3>Final Claims</h3>");
            writeHtmlClaimsTable(writer, claimAnalysis.getFinalClaims());
        }

        writer.println("</div>");
    }

    /**
     * Writes HTML claims table.
     * 
     * @param writer Print writer
     * @param claims Claims map
     */
    private static void writeHtmlClaimsTable(PrintWriter writer, Map<String, String> claims) {
        writer.println("<table class='claims-table'>");
        writer.println("<tr><th>Claim</th><th>Value</th></tr>");
        for (Map.Entry<String, String> entry : claims.entrySet()) {
            writer.println("<tr><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>");
        }
        writer.println("</table>");
    }

    /**
     * Writes HTML errors and warnings.
     * 
     * @param writer Print writer
     * @param dfdpResponse DFDP response
     */
    private static void writeHtmlErrorsAndWarnings(PrintWriter writer, DFDPResponse dfdpResponse) {
        if (dfdpResponse.hasErrors()) {
            writer.println("<div class='section'>");
            writer.println("<h2>Errors</h2>");
            writer.println("<ul class='error-list'>");
            for (DFDPError error : dfdpResponse.getErrors()) {
                writer.println("<li class='error-item'><strong>" + error.getCode() + ":</strong> " + 
                              error.getMessage() + "</li>");
            }
            writer.println("</ul>");
            writer.println("</div>");
        }

        if (dfdpResponse.hasWarnings()) {
            writer.println("<div class='section'>");
            writer.println("<h2>Warnings</h2>");
            writer.println("<ul class='warning-list'>");
            for (DFDPWarning warning : dfdpResponse.getWarnings()) {
                writer.println("<li class='warning-item'><strong>" + warning.getCode() + ":</strong> " + 
                              warning.getMessage() + "</li>");
            }
            writer.println("</ul>");
            writer.println("</div>");
        }
    }

    /**
     * Writes text test summary.
     * 
     * @param writer Print writer
     * @param testSummary Test summary
     */
    private static void writeTextTestSummary(PrintWriter writer, DFDPTestSummary testSummary) {
        writer.println("Test Summary:");
        writer.println("-------------");
        writer.println("Target IdP: " + testSummary.getTargetIdP());
        writer.println("Total Claims: " + testSummary.getTotalClaims());
        writer.println("Successful Mappings: " + testSummary.getSuccessfulMappings());
        writer.println("Failed Mappings: " + testSummary.getFailedMappings());
        writer.println("Success Rate: " + String.format("%.2f%%", testSummary.getMappingSuccessRate()));
        writer.println();
    }

    /**
     * Writes text claim analysis.
     * 
     * @param writer Print writer
     * @param claimAnalysis Claim analysis
     */
    private static void writeTextClaimAnalysis(PrintWriter writer, DFDPClaimAnalysis claimAnalysis) {
        writer.println("Claim Analysis:");
        writer.println("---------------");
        
        if (claimAnalysis.getOriginalClaims() != null) {
            writer.println("Original Claims: " + claimAnalysis.getOriginalClaims().size());
        }
        if (claimAnalysis.getFinalClaims() != null) {
            writer.println("Final Claims: " + claimAnalysis.getFinalClaims().size());
        }
        if (claimAnalysis.getUnmappedClaims() != null) {
            writer.println("Unmapped Claims: " + claimAnalysis.getUnmappedClaims().size());
        }
        writer.println();
    }

    /**
     * DFDP Response Summary for summary format.
     */
    private static class DFDPResponseSummary {
        private String requestId;
        private String status;
        private double executionTimeSeconds;
        private boolean successful;
        private int errorCount;
        private int warningCount;
        private int totalClaims;
        private int successfulMappings;
        private int failedMappings;

        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getExecutionTimeSeconds() { return executionTimeSeconds; }
        public void setExecutionTimeSeconds(double executionTimeSeconds) { this.executionTimeSeconds = executionTimeSeconds; }
        public boolean isSuccessful() { return successful; }
        public void setSuccessful(boolean successful) { this.successful = successful; }
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        public int getTotalClaims() { return totalClaims; }
        public void setTotalClaims(int totalClaims) { this.totalClaims = totalClaims; }
        public int getSuccessfulMappings() { return successfulMappings; }
        public void setSuccessfulMappings(int successfulMappings) { this.successfulMappings = successfulMappings; }
        public int getFailedMappings() { return failedMappings; }
        public void setFailedMappings(int failedMappings) { this.failedMappings = failedMappings; }
    }
}
