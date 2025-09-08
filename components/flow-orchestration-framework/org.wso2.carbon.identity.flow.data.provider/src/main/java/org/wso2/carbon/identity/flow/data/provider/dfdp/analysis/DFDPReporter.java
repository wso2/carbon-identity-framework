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

package org.wso2.carbon.identity.flow.data.provider.dfdp.analysis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPClaimEvent;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DFDP Reporter.
 * Part 5: DFDP Analysis Components - Event listener that generates comprehensive test reports.
 */
public class DFDPReporter implements DFDPEventListener {

    private static final Log log = LogFactory.getLog(DFDPReporter.class);
    
    private final Map<String, DFDPTestReport> testReports;
    private final boolean enabled;
    private final SimpleDateFormat dateFormat;

    /**
     * Constructor.
     */
    public DFDPReporter() {
        this.testReports = new ConcurrentHashMap<>();
        this.enabled = true;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }

    @Override
    public void onDFDPClaimEvent(DFDPClaimEvent event) {
        if (!enabled || event == null || event.getRequestId() == null) {
            return;
        }

        try {
            String requestId = event.getRequestId();
            
            // Get or create test report for this request
            DFDPTestReport report = testReports.computeIfAbsent(requestId, k -> new DFDPTestReport(requestId));
            
            // Update report based on event type
            updateReport(report, event);

            if (log.isDebugEnabled()) {
                log.debug("DFDP test report updated for request: " + requestId + 
                         ", Event: " + event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error updating DFDP test report", e);
        }
    }

    /**
     * Updates test report with event information.
     * 
     * @param report Test report
     * @param event DFDP claim event
     */
    private void updateReport(DFDPTestReport report, DFDPClaimEvent event) {
        
        // Update basic information
        if (report.getStartTime() == 0) {
            report.setStartTime(event.getTimestamp());
        }
        report.setLastEventTime(event.getTimestamp());
        
        if (event.getAuthenticatorName() != null) {
            report.setAuthenticatorName(event.getAuthenticatorName());
        }
        if (event.getIdentityProviderName() != null) {
            report.setIdentityProviderName(event.getIdentityProviderName());
        }
        
        // Add event to timeline
        String timelineEntry = formatTimelineEntry(event);
        report.addTimelineEntry(timelineEntry);
        
        // Handle specific event types
        switch (event.getEventType()) {
            case "CLAIM_RETRIEVAL":
                handleClaimRetrievalEvent(report, event);
                break;
            case "CLAIM_MAPPING":
                handleClaimMappingEvent(report, event);
                break;
            case "CLAIM_COMPLETION":
                handleClaimCompletionEvent(report, event);
                break;
            default:
                // Handle other events
                report.incrementEventCount();
                break;
        }
    }

    /**
     * Handles claim retrieval event.
     * 
     * @param report Test report
     * @param event DFDP claim event
     */
    private void handleClaimRetrievalEvent(DFDPTestReport report, DFDPClaimEvent event) {
        report.setClaimsRetrieved(true);
        report.setRetrievedClaimsCount(event.getClaimCount());
        report.incrementEventCount();
    }

    /**
     * Handles claim mapping event.
     * 
     * @param report Test report
     * @param event DFDP claim event
     */
    private void handleClaimMappingEvent(DFDPTestReport report, DFDPClaimEvent event) {
        report.setClaimsMapped(true);
        report.setMappedClaimsCount(event.getClaimCount());
        report.incrementEventCount();
    }

    /**
     * Handles claim completion event.
     * 
     * @param report Test report
     * @param event DFDP claim event
     */
    private void handleClaimCompletionEvent(DFDPTestReport report, DFDPClaimEvent event) {
        report.setCompleted(true);
        report.setFinalClaimsCount(event.getClaimCount());
        
        // Set completion status
        if (event.getAdditionalData() != null && event.getAdditionalData().containsKey("status")) {
            String status = (String) event.getAdditionalData().get("status");
            report.setCompletionStatus(status);
            report.setSuccessful("SUCCESS".equals(status));
        }
        
        // Calculate total execution time
        if (report.getStartTime() > 0) {
            report.setTotalExecutionTime(event.getTimestamp() - report.getStartTime());
        }
        
        report.incrementEventCount();
    }

    /**
     * Formats timeline entry for an event.
     * 
     * @param event DFDP claim event
     * @return Formatted timeline entry
     */
    private String formatTimelineEntry(DFDPClaimEvent event) {
        return String.format("[%s] %s - %s (%d claims)",
                dateFormat.format(new Date(event.getTimestamp())),
                event.getEventType(),
                event.getProcessingStage(),
                event.getClaimCount());
    }

    /**
     * Generates comprehensive test report for a request.
     * 
     * @param requestId Request ID
     * @param analysisResult Analysis result (optional)
     * @param logEntries Log entries (optional)
     * @return Comprehensive test report
     */
    public String generateComprehensiveReport(String requestId, DFDPAnalysisResult analysisResult, 
                                            List<DFDPLogger.DFDPClaimLogEntry> logEntries) {
        
        DFDPTestReport baseReport = testReports.get(requestId);
        if (baseReport == null) {
            return "No test report found for request: " + requestId;
        }

        StringBuilder report = new StringBuilder();
        
        // Header
        report.append("DFDP Test Report\n");
        report.append("================\n\n");
        
        // Basic Information
        report.append("Test Information:\n");
        report.append("-----------------\n");
        report.append("Request ID: ").append(requestId).append("\n");
        report.append("Start Time: ").append(dateFormat.format(new Date(baseReport.getStartTime()))).append("\n");
        report.append("Completion Time: ").append(dateFormat.format(new Date(baseReport.getLastEventTime()))).append("\n");
        report.append("Total Execution Time: ").append(baseReport.getTotalExecutionTime()).append(" ms\n");
        report.append("Authenticator: ").append(baseReport.getAuthenticatorName()).append("\n");
        report.append("Identity Provider: ").append(baseReport.getIdentityProviderName()).append("\n");
        report.append("Status: ").append(baseReport.isSuccessful() ? "SUCCESS" : "FAILED").append("\n");
        report.append("Completion Status: ").append(baseReport.getCompletionStatus()).append("\n\n");
        
        // Claims Summary
        report.append("Claims Summary:\n");
        report.append("---------------\n");
        report.append("Retrieved Claims: ").append(baseReport.getRetrievedClaimsCount()).append("\n");
        report.append("Mapped Claims: ").append(baseReport.getMappedClaimsCount()).append("\n");
        report.append("Final Claims: ").append(baseReport.getFinalClaimsCount()).append("\n");
        report.append("Claims Retrieved: ").append(baseReport.isClaimsRetrieved() ? "YES" : "NO").append("\n");
        report.append("Claims Mapped: ").append(baseReport.isClaimsMapped() ? "YES" : "NO").append("\n\n");
        
        // Analysis Results
        if (analysisResult != null) {
            report.append("Analysis Results:\n");
            report.append("-----------------\n");
            report.append("Accuracy Score: ").append(String.format("%.1f%%", analysisResult.getAccuracyPercentage())).append("\n");
            report.append("Mapping Efficiency: ").append(String.format("%.1f%%", analysisResult.getMappingEfficiencyPercentage())).append("\n");
            report.append("Quality Grade: ").append(analysisResult.getQualityGrade()).append("\n");
            report.append("Total Issues: ").append(analysisResult.getTotalIssueCount()).append("\n");
            
            if (analysisResult.hasIssues()) {
                report.append("\nIssues Found:\n");
                appendIssueList(report, "Missing Retrieved Claims", analysisResult.getMissingRetrievedClaims());
                appendIssueList(report, "Unexpected Retrieved Claims", analysisResult.getUnexpectedRetrievedClaims());
                appendIssueList(report, "Mismatched Retrieved Claims", analysisResult.getMismatchedRetrievedClaims());
                appendIssueList(report, "Missing Mapped Claims", analysisResult.getMissingMappedClaims());
                appendIssueList(report, "Unexpected Mapped Claims", analysisResult.getUnexpectedMappedClaims());
                appendIssueList(report, "Mismatched Mapped Claims", analysisResult.getMismatchedMappedClaims());
                appendIssueList(report, "Unmapped Claims", analysisResult.getUnmappedClaims());
            }
            report.append("\n");
        }
        
        // Event Timeline
        report.append("Event Timeline:\n");
        report.append("---------------\n");
        for (String timelineEntry : baseReport.getTimeline()) {
            report.append(timelineEntry).append("\n");
        }
        report.append("\n");
        
        // Detailed Logs
        if (logEntries != null && !logEntries.isEmpty()) {
            report.append("Detailed Processing Log:\n");
            report.append("------------------------\n");
            for (DFDPLogger.DFDPClaimLogEntry logEntry : logEntries) {
                report.append(logEntry.getSummary()).append("\n");
            }
            report.append("\n");
        }
        
        // Performance Metrics
        report.append("Performance Metrics:\n");
        report.append("--------------------\n");
        report.append("Total Events Processed: ").append(baseReport.getEventCount()).append("\n");
        report.append("Average Event Processing Time: ");
        if (baseReport.getEventCount() > 0) {
            report.append(String.format("%.2f ms", (double) baseReport.getTotalExecutionTime() / baseReport.getEventCount()));
        } else {
            report.append("N/A");
        }
        report.append("\n\n");
        
        // Footer
        report.append("Report Generated: ").append(dateFormat.format(new Date())).append("\n");
        report.append("=".repeat(50)).append("\n");
        
        return report.toString();
    }

    /**
     * Appends issue list to report.
     * 
     * @param report Report builder
     * @param title Issue category title
     * @param issues List of issues
     */
    private void appendIssueList(StringBuilder report, String title, List<String> issues) {
        if (issues != null && !issues.isEmpty()) {
            report.append("  ").append(title).append(":\n");
            for (String issue : issues) {
                report.append("    - ").append(issue).append("\n");
            }
        }
    }

    /**
     * Gets test report for a request.
     * 
     * @param requestId Request ID
     * @return Test report
     */
    public DFDPTestReport getTestReport(String requestId) {
        return testReports.get(requestId);
    }

    /**
     * Clears test report for a request.
     * 
     * @param requestId Request ID
     */
    public void clearTestReport(String requestId) {
        testReports.remove(requestId);
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared DFDP test report for request: " + requestId);
        }
    }

    /**
     * Clears all test reports.
     */
    public void clearAllReports() {
        testReports.clear();
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared all DFDP test reports");
        }
    }

    /**
     * Gets active report count.
     * 
     * @return Number of active reports
     */
    public int getActiveReportCount() {
        return testReports.size();
    }

    @Override
    public String getListenerName() {
        return "DFDPReporter";
    }

    @Override
    public int getPriority() {
        return 60; // Medium priority
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean supportsEventType(String eventType) {
        // Support all claim-related events
        return eventType != null && (
                eventType.startsWith("CLAIM_") ||
                eventType.equals("DFDP_START") ||
                eventType.equals("DFDP_END")
        );
    }
}
