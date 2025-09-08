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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DFDP Analyzer.
 * Part 5: DFDP Analysis Components - Event listener that analyzes claim processing and compares expected vs actual results.
 */
public class DFDPAnalyzer implements DFDPEventListener {

    private static final Log log = LogFactory.getLog(DFDPAnalyzer.class);
    
    private final Map<String, DFDPAnalysisResult> analysisResults;
    private final Map<String, DFDPExpectedResults> expectedResults;
    private final boolean enabled;

    /**
     * Constructor.
     */
    public DFDPAnalyzer() {
        this.analysisResults = new ConcurrentHashMap<>();
        this.expectedResults = new ConcurrentHashMap<>();
        this.enabled = true;
    }

    @Override
    public void onDFDPClaimEvent(DFDPClaimEvent event) {
        if (!enabled || event == null || event.getRequestId() == null) {
            return;
        }

        try {
            String requestId = event.getRequestId();
            
            // Get or create analysis result for this request
            DFDPAnalysisResult result = analysisResults.computeIfAbsent(requestId, k -> new DFDPAnalysisResult(requestId));
            
            // Process the event based on type
            switch (event.getEventType()) {
                case "CLAIM_RETRIEVAL":
                    analyzeClaimRetrieval(result, event);
                    break;
                case "CLAIM_MAPPING":
                    analyzeClaimMapping(result, event);
                    break;
                case "CLAIM_COMPLETION":
                    analyzeClaimCompletion(result, event);
                    break;
                default:
                    // Handle other event types if needed
                    break;
            }

            // Update analysis metadata
            result.setLastUpdated(System.currentTimeMillis());
            result.incrementEventCount();

            if (log.isDebugEnabled()) {
                log.debug("DFDP analysis updated for request: " + requestId + 
                         ", Event: " + event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error analyzing DFDP claim event", e);
        }
    }

    /**
     * Analyzes claim retrieval event.
     * 
     * @param result Analysis result
     * @param event Claim event
     */
    private void analyzeClaimRetrieval(DFDPAnalysisResult result, DFDPClaimEvent event) {
        Map<String, String> retrievedClaims = event.getClaims();
        if (retrievedClaims != null) {
            result.setRetrievedClaims(new HashMap<>(retrievedClaims));
            result.setRetrievedClaimsCount(retrievedClaims.size());
            
            // Performance metrics
            if (result.getStartTime() == 0) {
                result.setStartTime(event.getTimestamp());
            }
            
            // Compare with expected if available
            DFDPExpectedResults expected = expectedResults.get(event.getRequestId());
            if (expected != null && expected.getExpectedClaims() != null) {
                analyzeClaimExpectations(result, retrievedClaims, expected.getExpectedClaims(), "RETRIEVAL");
            }
        }
    }

    /**
     * Analyzes claim mapping event.
     * 
     * @param result Analysis result
     * @param event Claim event
     */
    private void analyzeClaimMapping(DFDPAnalysisResult result, DFDPClaimEvent event) {
        Map<String, String> mappedClaims = event.getClaims();
        if (mappedClaims != null) {
            result.setMappedClaims(new HashMap<>(mappedClaims));
            result.setMappedClaimsCount(mappedClaims.size());
            
            // Get original claims from additional data
            if (event.getAdditionalData() != null && event.getAdditionalData().containsKey("originalClaims")) {
                @SuppressWarnings("unchecked")
                Map<String, String> originalClaims = (Map<String, String>) event.getAdditionalData().get("originalClaims");
                if (originalClaims != null) {
                    analyzeMappingEffectiveness(result, originalClaims, mappedClaims);
                }
            }
            
            // Compare with expected mapped claims
            DFDPExpectedResults expected = expectedResults.get(event.getRequestId());
            if (expected != null && expected.getExpectedMappedClaims() != null) {
                analyzeClaimExpectations(result, mappedClaims, expected.getExpectedMappedClaims(), "MAPPING");
            }
        }
    }

    /**
     * Analyzes claim completion event.
     * 
     * @param result Analysis result
     * @param event Claim event
     */
    private void analyzeClaimCompletion(DFDPAnalysisResult result, DFDPClaimEvent event) {
        Map<String, String> finalClaims = event.getClaims();
        if (finalClaims != null) {
            result.setFinalClaims(new HashMap<>(finalClaims));
            result.setFinalClaimsCount(finalClaims.size());
        }
        
        // Set completion status
        if (event.getAdditionalData() != null && event.getAdditionalData().containsKey("status")) {
            String status = (String) event.getAdditionalData().get("status");
            result.setCompletionStatus(status);
        }
        
        // Calculate total processing time
        if (result.getStartTime() > 0) {
            result.setTotalProcessingTime(event.getTimestamp() - result.getStartTime());
        }
        
        // Final analysis
        performFinalAnalysis(result);
        
        result.setCompleted(true);
    }

    /**
     * Analyzes claim expectations against actual claims.
     * 
     * @param result Analysis result
     * @param actualClaims Actual claims
     * @param expectedClaims Expected claims
     * @param stage Processing stage
     */
    private void analyzeClaimExpectations(DFDPAnalysisResult result, Map<String, String> actualClaims,
                                        Map<String, String> expectedClaims, String stage) {
        
        List<String> missingClaims = new ArrayList<>();
        List<String> unexpectedClaims = new ArrayList<>();
        List<String> mismatchedClaims = new ArrayList<>();
        
        // Check for missing expected claims
        for (String expectedClaim : expectedClaims.keySet()) {
            if (!actualClaims.containsKey(expectedClaim)) {
                missingClaims.add(expectedClaim);
            } else {
                // Check value match
                String expectedValue = expectedClaims.get(expectedClaim);
                String actualValue = actualClaims.get(expectedClaim);
                if (expectedValue != null && !expectedValue.equals(actualValue)) {
                    mismatchedClaims.add(expectedClaim + " (expected: " + expectedValue + ", actual: " + actualValue + ")");
                }
            }
        }
        
        // Check for unexpected claims
        for (String actualClaim : actualClaims.keySet()) {
            if (!expectedClaims.containsKey(actualClaim)) {
                unexpectedClaims.add(actualClaim);
            }
        }
        
        // Store analysis results
        if (stage.equals("RETRIEVAL")) {
            result.setMissingRetrievedClaims(missingClaims);
            result.setUnexpectedRetrievedClaims(unexpectedClaims);
            result.setMismatchedRetrievedClaims(mismatchedClaims);
        } else if (stage.equals("MAPPING")) {
            result.setMissingMappedClaims(missingClaims);
            result.setUnexpectedMappedClaims(unexpectedClaims);
            result.setMismatchedMappedClaims(mismatchedClaims);
        }
    }

    /**
     * Analyzes mapping effectiveness.
     * 
     * @param result Analysis result
     * @param originalClaims Original claims
     * @param mappedClaims Mapped claims
     */
    private void analyzeMappingEffectiveness(DFDPAnalysisResult result, Map<String, String> originalClaims,
                                           Map<String, String> mappedClaims) {
        
        int originalCount = originalClaims.size();
        int mappedCount = mappedClaims.size();
        
        // Calculate mapping efficiency
        double mappingEfficiency = originalCount > 0 ? (double) mappedCount / originalCount : 0.0;
        result.setMappingEfficiency(mappingEfficiency);
        
        // Identify unmapped claims
        List<String> unmappedClaims = new ArrayList<>();
        for (String originalClaim : originalClaims.keySet()) {
            boolean mapped = false;
            for (String mappedClaim : mappedClaims.keySet()) {
                // Simple check - could be enhanced with actual mapping logic
                if (mappedClaim.contains(originalClaim) || originalClaim.contains(mappedClaim)) {
                    mapped = true;
                    break;
                }
            }
            if (!mapped) {
                unmappedClaims.add(originalClaim);
            }
        }
        result.setUnmappedClaims(unmappedClaims);
    }

    /**
     * Performs final analysis on completion.
     * 
     * @param result Analysis result
     */
    private void performFinalAnalysis(DFDPAnalysisResult result) {
        // Calculate overall success rate
        int totalIssues = result.getMissingRetrievedClaims().size() + 
                         result.getMissingMappedClaims().size() +
                         result.getMismatchedRetrievedClaims().size() +
                         result.getMismatchedMappedClaims().size();
        
        boolean success = totalIssues == 0 && "SUCCESS".equals(result.getCompletionStatus());
        result.setSuccessful(success);
        
        // Calculate accuracy score
        int totalExpectedClaims = 0;
        int totalCorrectClaims = 0;
        
        DFDPExpectedResults expected = expectedResults.get(result.getRequestId());
        if (expected != null) {
            if (expected.getExpectedClaims() != null) {
                totalExpectedClaims += expected.getExpectedClaims().size();
                totalCorrectClaims += (expected.getExpectedClaims().size() - result.getMissingRetrievedClaims().size() - result.getMismatchedRetrievedClaims().size());
            }
            if (expected.getExpectedMappedClaims() != null) {
                totalExpectedClaims += expected.getExpectedMappedClaims().size();
                totalCorrectClaims += (expected.getExpectedMappedClaims().size() - result.getMissingMappedClaims().size() - result.getMismatchedMappedClaims().size());
            }
        }
        
        double accuracyScore = totalExpectedClaims > 0 ? (double) totalCorrectClaims / totalExpectedClaims : 1.0;
        result.setAccuracyScore(accuracyScore);
    }

    /**
     * Sets expected results for a request.
     * 
     * @param requestId Request ID
     * @param expectedResults Expected results
     */
    public void setExpectedResults(String requestId, DFDPExpectedResults expectedResults) {
        this.expectedResults.put(requestId, expectedResults);
        
        if (log.isDebugEnabled()) {
            log.debug("Expected results set for request: " + requestId);
        }
    }

    /**
     * Gets analysis result for a request.
     * 
     * @param requestId Request ID
     * @return Analysis result
     */
    public DFDPAnalysisResult getAnalysisResult(String requestId) {
        return analysisResults.get(requestId);
    }

    /**
     * Gets all analysis results.
     * 
     * @return Map of request ID to analysis results
     */
    public Map<String, DFDPAnalysisResult> getAllAnalysisResults() {
        return new HashMap<>(analysisResults);
    }

    /**
     * Clears analysis results for a request.
     * 
     * @param requestId Request ID
     */
    public void clearAnalysisResult(String requestId) {
        analysisResults.remove(requestId);
        expectedResults.remove(requestId);
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared analysis results for request: " + requestId);
        }
    }

    /**
     * Clears all analysis results.
     */
    public void clearAllResults() {
        analysisResults.clear();
        expectedResults.clear();
        
        if (log.isDebugEnabled()) {
            log.debug("Cleared all DFDP analysis results");
        }
    }

    @Override
    public String getListenerName() {
        return "DFDPAnalyzer";
    }

    @Override
    public int getPriority() {
        return 80; // Medium-high priority
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean supportsEventType(String eventType) {
        // Support all claim-related events
        return eventType != null && eventType.startsWith("CLAIM_");
    }
}
