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

/**
 * DFDP Test Summary DTO.
 * Part 7: Response Generation - Summary information about the DFDP test execution.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFDPTestSummary {

    @JsonProperty("testId")
    private String testId;

    @JsonProperty("testName")
    private String testName;

    @JsonProperty("targetIdP")
    private String targetIdP;

    @JsonProperty("targetAuthenticator")
    private String targetAuthenticator;

    @JsonProperty("testStatus")
    private String testStatus;

    @JsonProperty("completionPercentage")
    private double completionPercentage;

    @JsonProperty("totalClaims")
    private int totalClaims;

    @JsonProperty("successfulMappings")
    private int successfulMappings;

    @JsonProperty("failedMappings")
    private int failedMappings;

    @JsonProperty("executionSteps")
    private int executionSteps;

    @JsonProperty("testDuration")
    private long testDurationMs;

    @JsonProperty("startTime")
    private long startTime;

    @JsonProperty("endTime")
    private long endTime;

    // Getters and setters

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTargetIdP() {
        return targetIdP;
    }

    public void setTargetIdP(String targetIdP) {
        this.targetIdP = targetIdP;
    }

    public String getTargetAuthenticator() {
        return targetAuthenticator;
    }

    public void setTargetAuthenticator(String targetAuthenticator) {
        this.targetAuthenticator = targetAuthenticator;
    }

    public String getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public int getTotalClaims() {
        return totalClaims;
    }

    public void setTotalClaims(int totalClaims) {
        this.totalClaims = totalClaims;
    }

    public int getSuccessfulMappings() {
        return successfulMappings;
    }

    public void setSuccessfulMappings(int successfulMappings) {
        this.successfulMappings = successfulMappings;
    }

    public int getFailedMappings() {
        return failedMappings;
    }

    public void setFailedMappings(int failedMappings) {
        this.failedMappings = failedMappings;
    }

    public int getExecutionSteps() {
        return executionSteps;
    }

    public void setExecutionSteps(int executionSteps) {
        this.executionSteps = executionSteps;
    }

    public long getTestDurationMs() {
        return testDurationMs;
    }

    public void setTestDurationMs(long testDurationMs) {
        this.testDurationMs = testDurationMs;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    // Utility methods

    public double getTestDurationSeconds() {
        return testDurationMs / 1000.0;
    }

    public double getMappingSuccessRate() {
        if (totalClaims == 0) {
            return 0.0;
        }
        return (double) successfulMappings / totalClaims * 100.0;
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(testStatus) || "SUCCESS".equals(testStatus) || "FAILURE".equals(testStatus);
    }
}
