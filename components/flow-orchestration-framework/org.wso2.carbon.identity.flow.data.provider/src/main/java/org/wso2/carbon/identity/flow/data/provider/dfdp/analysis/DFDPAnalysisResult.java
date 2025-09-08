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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DFDP Analysis Result.
 * Part 5: DFDP Analysis Components - Contains comprehensive analysis results for a DFDP request.
 */
public class DFDPAnalysisResult {

    private String requestId;
    private long startTime;
    private long lastUpdated;
    private long totalProcessingTime;
    private int eventCount;
    private boolean completed;
    private boolean successful;
    private String completionStatus;
    private double accuracyScore;
    private double mappingEfficiency;

    // Claim data
    private Map<String, String> retrievedClaims;
    private Map<String, String> mappedClaims;
    private Map<String, String> finalClaims;
    private int retrievedClaimsCount;
    private int mappedClaimsCount;
    private int finalClaimsCount;

    // Analysis results
    private List<String> missingRetrievedClaims;
    private List<String> unexpectedRetrievedClaims;
    private List<String> mismatchedRetrievedClaims;
    private List<String> missingMappedClaims;
    private List<String> unexpectedMappedClaims;
    private List<String> mismatchedMappedClaims;
    private List<String> unmappedClaims;

    /**
     * Constructor.
     * 
     * @param requestId Request ID
     */
    public DFDPAnalysisResult(String requestId) {
        this.requestId = requestId;
        this.startTime = 0;
        this.lastUpdated = System.currentTimeMillis();
        this.eventCount = 0;
        this.completed = false;
        this.successful = false;
        this.accuracyScore = 0.0;
        this.mappingEfficiency = 0.0;
        
        // Initialize lists
        this.missingRetrievedClaims = new ArrayList<>();
        this.unexpectedRetrievedClaims = new ArrayList<>();
        this.mismatchedRetrievedClaims = new ArrayList<>();
        this.missingMappedClaims = new ArrayList<>();
        this.unexpectedMappedClaims = new ArrayList<>();
        this.mismatchedMappedClaims = new ArrayList<>();
        this.unmappedClaims = new ArrayList<>();
        
        // Initialize maps
        this.retrievedClaims = new HashMap<>();
        this.mappedClaims = new HashMap<>();
        this.finalClaims = new HashMap<>();
    }

    // Getters and setters

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public void setTotalProcessingTime(long totalProcessingTime) {
        this.totalProcessingTime = totalProcessingTime;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public void incrementEventCount() {
        this.eventCount++;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getCompletionStatus() {
        return completionStatus;
    }

    public void setCompletionStatus(String completionStatus) {
        this.completionStatus = completionStatus;
    }

    public double getAccuracyScore() {
        return accuracyScore;
    }

    public void setAccuracyScore(double accuracyScore) {
        this.accuracyScore = accuracyScore;
    }

    public double getMappingEfficiency() {
        return mappingEfficiency;
    }

    public void setMappingEfficiency(double mappingEfficiency) {
        this.mappingEfficiency = mappingEfficiency;
    }

    // Claim data getters and setters

    public Map<String, String> getRetrievedClaims() {
        return retrievedClaims;
    }

    public void setRetrievedClaims(Map<String, String> retrievedClaims) {
        this.retrievedClaims = retrievedClaims;
    }

    public Map<String, String> getMappedClaims() {
        return mappedClaims;
    }

    public void setMappedClaims(Map<String, String> mappedClaims) {
        this.mappedClaims = mappedClaims;
    }

    public Map<String, String> getFinalClaims() {
        return finalClaims;
    }

    public void setFinalClaims(Map<String, String> finalClaims) {
        this.finalClaims = finalClaims;
    }

    public int getRetrievedClaimsCount() {
        return retrievedClaimsCount;
    }

    public void setRetrievedClaimsCount(int retrievedClaimsCount) {
        this.retrievedClaimsCount = retrievedClaimsCount;
    }

    public int getMappedClaimsCount() {
        return mappedClaimsCount;
    }

    public void setMappedClaimsCount(int mappedClaimsCount) {
        this.mappedClaimsCount = mappedClaimsCount;
    }

    public int getFinalClaimsCount() {
        return finalClaimsCount;
    }

    public void setFinalClaimsCount(int finalClaimsCount) {
        this.finalClaimsCount = finalClaimsCount;
    }

    // Analysis results getters and setters

    public List<String> getMissingRetrievedClaims() {
        return missingRetrievedClaims;
    }

    public void setMissingRetrievedClaims(List<String> missingRetrievedClaims) {
        this.missingRetrievedClaims = missingRetrievedClaims;
    }

    public List<String> getUnexpectedRetrievedClaims() {
        return unexpectedRetrievedClaims;
    }

    public void setUnexpectedRetrievedClaims(List<String> unexpectedRetrievedClaims) {
        this.unexpectedRetrievedClaims = unexpectedRetrievedClaims;
    }

    public List<String> getMismatchedRetrievedClaims() {
        return mismatchedRetrievedClaims;
    }

    public void setMismatchedRetrievedClaims(List<String> mismatchedRetrievedClaims) {
        this.mismatchedRetrievedClaims = mismatchedRetrievedClaims;
    }

    public List<String> getMissingMappedClaims() {
        return missingMappedClaims;
    }

    public void setMissingMappedClaims(List<String> missingMappedClaims) {
        this.missingMappedClaims = missingMappedClaims;
    }

    public List<String> getUnexpectedMappedClaims() {
        return unexpectedMappedClaims;
    }

    public void setUnexpectedMappedClaims(List<String> unexpectedMappedClaims) {
        this.unexpectedMappedClaims = unexpectedMappedClaims;
    }

    public List<String> getMismatchedMappedClaims() {
        return mismatchedMappedClaims;
    }

    public void setMismatchedMappedClaims(List<String> mismatchedMappedClaims) {
        this.mismatchedMappedClaims = mismatchedMappedClaims;
    }

    public List<String> getUnmappedClaims() {
        return unmappedClaims;
    }

    public void setUnmappedClaims(List<String> unmappedClaims) {
        this.unmappedClaims = unmappedClaims;
    }

    // Utility methods

    /**
     * Gets total issue count.
     * 
     * @return Total issues found
     */
    public int getTotalIssueCount() {
        return missingRetrievedClaims.size() + unexpectedRetrievedClaims.size() + mismatchedRetrievedClaims.size() +
               missingMappedClaims.size() + unexpectedMappedClaims.size() + mismatchedMappedClaims.size() +
               unmappedClaims.size();
    }

    /**
     * Gets processing time in milliseconds.
     * 
     * @return Processing time in ms
     */
    public long getProcessingTimeMs() {
        return totalProcessingTime;
    }

    /**
     * Gets processing time in seconds.
     * 
     * @return Processing time in seconds
     */
    public double getProcessingTimeSeconds() {
        return totalProcessingTime / 1000.0;
    }

    /**
     * Gets accuracy percentage.
     * 
     * @return Accuracy as percentage
     */
    public double getAccuracyPercentage() {
        return accuracyScore * 100.0;
    }

    /**
     * Gets mapping efficiency percentage.
     * 
     * @return Mapping efficiency as percentage
     */
    public double getMappingEfficiencyPercentage() {
        return mappingEfficiency * 100.0;
    }

    /**
     * Gets analysis summary.
     * 
     * @return Analysis summary string
     */
    public String getAnalysisSummary() {
        return String.format("Request: %s - %s (%.1f%% accurate, %.1f%% mapping efficiency, %d issues, %d ms)",
                requestId, successful ? "SUCCESS" : "FAILED", getAccuracyPercentage(), 
                getMappingEfficiencyPercentage(), getTotalIssueCount(), totalProcessingTime);
    }

    /**
     * Checks if analysis has issues.
     * 
     * @return true if has issues
     */
    public boolean hasIssues() {
        return getTotalIssueCount() > 0;
    }

    /**
     * Gets quality grade based on accuracy and efficiency.
     * 
     * @return Quality grade (A, B, C, D, F)
     */
    public String getQualityGrade() {
        double score = (accuracyScore + mappingEfficiency) / 2.0 * 100.0;
        
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }
}
