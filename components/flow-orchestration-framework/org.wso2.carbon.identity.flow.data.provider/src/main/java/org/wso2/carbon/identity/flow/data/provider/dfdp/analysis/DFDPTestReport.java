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
import java.util.List;

/**
 * DFDP Test Report.
 * Part 5: DFDP Analysis Components - Contains test execution report for a DFDP request.
 */
public class DFDPTestReport {

    private String requestId;
    private long startTime;
    private long lastEventTime;
    private long totalExecutionTime;
    private String authenticatorName;
    private String identityProviderName;
    private boolean completed;
    private boolean successful;
    private String completionStatus;
    private int eventCount;

    // Claims information
    private boolean claimsRetrieved;
    private boolean claimsMapped;
    private int retrievedClaimsCount;
    private int mappedClaimsCount;
    private int finalClaimsCount;

    // Timeline and logs
    private List<String> timeline;

    /**
     * Constructor.
     * 
     * @param requestId Request ID
     */
    public DFDPTestReport(String requestId) {
        this.requestId = requestId;
        this.startTime = 0;
        this.lastEventTime = 0;
        this.totalExecutionTime = 0;
        this.completed = false;
        this.successful = false;
        this.eventCount = 0;
        this.claimsRetrieved = false;
        this.claimsMapped = false;
        this.retrievedClaimsCount = 0;
        this.mappedClaimsCount = 0;
        this.finalClaimsCount = 0;
        this.timeline = new ArrayList<>();
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

    public long getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(long lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public void setTotalExecutionTime(long totalExecutionTime) {
        this.totalExecutionTime = totalExecutionTime;
    }

    public String getAuthenticatorName() {
        return authenticatorName;
    }

    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    public String getIdentityProviderName() {
        return identityProviderName;
    }

    public void setIdentityProviderName(String identityProviderName) {
        this.identityProviderName = identityProviderName;
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

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public void incrementEventCount() {
        this.eventCount++;
    }

    public boolean isClaimsRetrieved() {
        return claimsRetrieved;
    }

    public void setClaimsRetrieved(boolean claimsRetrieved) {
        this.claimsRetrieved = claimsRetrieved;
    }

    public boolean isClaimsMapped() {
        return claimsMapped;
    }

    public void setClaimsMapped(boolean claimsMapped) {
        this.claimsMapped = claimsMapped;
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

    public List<String> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<String> timeline) {
        this.timeline = timeline;
    }

    public void addTimelineEntry(String entry) {
        this.timeline.add(entry);
    }

    // Utility methods

    /**
     * Gets execution time in seconds.
     * 
     * @return Execution time in seconds
     */
    public double getExecutionTimeSeconds() {
        return totalExecutionTime / 1000.0;
    }

    /**
     * Gets test summary.
     * 
     * @return Test summary string
     */
    public String getTestSummary() {
        return String.format("Request: %s - %s (%s) - %.2f sec, %d events, %d claims retrieved, %d claims mapped",
                requestId, successful ? "SUCCESS" : "FAILED", completionStatus,
                getExecutionTimeSeconds(), eventCount, retrievedClaimsCount, mappedClaimsCount);
    }

    /**
     * Checks if test was executed completely.
     * 
     * @return true if completed
     */
    public boolean isTestComplete() {
        return completed && claimsRetrieved;
    }

    /**
     * Gets completion percentage.
     * 
     * @return Completion percentage
     */
    public double getCompletionPercentage() {
        int totalSteps = 3; // retrieval, mapping, completion
        int completedSteps = 0;
        
        if (claimsRetrieved) completedSteps++;
        if (claimsMapped) completedSteps++;
        if (completed) completedSteps++;
        
        return (double) completedSteps / totalSteps * 100.0;
    }

    /**
     * Gets test status description.
     * 
     * @return Status description
     */
    public String getStatusDescription() {
        if (!completed) {
            return "IN_PROGRESS";
        } else if (successful) {
            return "COMPLETED_SUCCESS";
        } else {
            return "COMPLETED_FAILED";
        }
    }
}
