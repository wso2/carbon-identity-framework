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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.wso2.carbon.identity.flow.data.provider.dfdp.analysis.DFDPAnalysisResult;
import org.wso2.carbon.identity.flow.data.provider.dfdp.analysis.DFDPTestReport;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * DFDP Response DTO.
 * Part 7: Response Generation - Main response structure for DFDP test execution results.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFDPResponse {

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("executionTime")
    private long executionTimeMs;

    @JsonProperty("testSummary")
    private DFDPTestSummary testSummary;

    @JsonProperty("claimAnalysis")
    private DFDPClaimAnalysis claimAnalysis;

    @JsonProperty("authenticatorInfo")
    private DFDPAuthenticatorInfo authenticatorInfo;

    @JsonProperty("analysisResults")
    private DFDPAnalysisResult analysisResults;

    @JsonProperty("testReport")
    private DFDPTestReport testReport;

    @JsonProperty("timeline")
    private List<DFDPTimelineEntry> timeline;

    @JsonProperty("errors")
    private List<DFDPError> errors;

    @JsonProperty("warnings")
    private List<DFDPWarning> warnings;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    /**
     * Default constructor.
     */
    public DFDPResponse() {
        this.timestamp = System.currentTimeMillis();
        this.timeline = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Constructor with request ID.
     * 
     * @param requestId Request ID
     */
    public DFDPResponse(String requestId) {
        this();
        this.requestId = requestId;
    }

    // Getters and setters

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public DFDPTestSummary getTestSummary() {
        return testSummary;
    }

    public void setTestSummary(DFDPTestSummary testSummary) {
        this.testSummary = testSummary;
    }

    public DFDPClaimAnalysis getClaimAnalysis() {
        return claimAnalysis;
    }

    public void setClaimAnalysis(DFDPClaimAnalysis claimAnalysis) {
        this.claimAnalysis = claimAnalysis;
    }

    public DFDPAuthenticatorInfo getAuthenticatorInfo() {
        return authenticatorInfo;
    }

    public void setAuthenticatorInfo(DFDPAuthenticatorInfo authenticatorInfo) {
        this.authenticatorInfo = authenticatorInfo;
    }

    public DFDPAnalysisResult getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(DFDPAnalysisResult analysisResults) {
        this.analysisResults = analysisResults;
    }

    public DFDPTestReport getTestReport() {
        return testReport;
    }

    public void setTestReport(DFDPTestReport testReport) {
        this.testReport = testReport;
    }

    public List<DFDPTimelineEntry> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<DFDPTimelineEntry> timeline) {
        this.timeline = timeline;
    }

    public void addTimelineEntry(DFDPTimelineEntry entry) {
        if (this.timeline == null) {
            this.timeline = new ArrayList<>();
        }
        this.timeline.add(entry);
    }

    public List<DFDPError> getErrors() {
        return errors;
    }

    public void setErrors(List<DFDPError> errors) {
        this.errors = errors;
    }

    public void addError(DFDPError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public void addError(String code, String message, String details) {
        addError(new DFDPError(code, message, details));
    }

    public List<DFDPWarning> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<DFDPWarning> warnings) {
        this.warnings = warnings;
    }

    public void addWarning(DFDPWarning warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addWarning(String code, String message, String details) {
        addWarning(new DFDPWarning(code, message, details));
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
    }

    // Utility methods

    /**
     * Checks if the response indicates a successful test.
     * 
     * @return true if successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status) || "COMPLETED".equals(status);
    }

    /**
     * Checks if the response has errors.
     * 
     * @return true if errors exist
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Checks if the response has warnings.
     * 
     * @return true if warnings exist
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * Gets execution time in seconds.
     * 
     * @return Execution time in seconds
     */
    public double getExecutionTimeSeconds() {
        return executionTimeMs / 1000.0;
    }

    /**
     * Gets total error count.
     * 
     * @return Error count
     */
    public int getErrorCount() {
        return errors != null ? errors.size() : 0;
    }

    /**
     * Gets total warning count.
     * 
     * @return Warning count
     */
    public int getWarningCount() {
        return warnings != null ? warnings.size() : 0;
    }
}
