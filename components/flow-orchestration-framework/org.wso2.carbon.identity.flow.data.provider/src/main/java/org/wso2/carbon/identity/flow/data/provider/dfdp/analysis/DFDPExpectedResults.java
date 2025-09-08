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

import java.util.Map;

/**
 * DFDP Expected Results.
 * Part 5: DFDP Analysis Components - Contains expected results for comparison during DFDP analysis.
 */
public class DFDPExpectedResults {

    private String requestId;
    private Map<String, String> expectedClaims;
    private Map<String, String> expectedMappedClaims;
    private String expectedStatus;
    private long maxProcessingTime;
    private double minimumAccuracy;
    private double minimumMappingEfficiency;

    /**
     * Constructor.
     */
    public DFDPExpectedResults() {
        this.minimumAccuracy = 0.9; // 90% default
        this.minimumMappingEfficiency = 0.8; // 80% default
        this.maxProcessingTime = 5000; // 5 seconds default
        this.expectedStatus = "SUCCESS";
    }

    /**
     * Constructor with request ID.
     * 
     * @param requestId Request ID
     */
    public DFDPExpectedResults(String requestId) {
        this();
        this.requestId = requestId;
    }

    /**
     * Gets request ID.
     * 
     * @return Request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets request ID.
     * 
     * @param requestId Request ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets expected claims.
     * 
     * @return Expected claims map
     */
    public Map<String, String> getExpectedClaims() {
        return expectedClaims;
    }

    /**
     * Sets expected claims.
     * 
     * @param expectedClaims Expected claims map
     */
    public void setExpectedClaims(Map<String, String> expectedClaims) {
        this.expectedClaims = expectedClaims;
    }

    /**
     * Gets expected mapped claims.
     * 
     * @return Expected mapped claims map
     */
    public Map<String, String> getExpectedMappedClaims() {
        return expectedMappedClaims;
    }

    /**
     * Sets expected mapped claims.
     * 
     * @param expectedMappedClaims Expected mapped claims map
     */
    public void setExpectedMappedClaims(Map<String, String> expectedMappedClaims) {
        this.expectedMappedClaims = expectedMappedClaims;
    }

    /**
     * Gets expected status.
     * 
     * @return Expected status
     */
    public String getExpectedStatus() {
        return expectedStatus;
    }

    /**
     * Sets expected status.
     * 
     * @param expectedStatus Expected status
     */
    public void setExpectedStatus(String expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    /**
     * Gets maximum processing time.
     * 
     * @return Maximum processing time in milliseconds
     */
    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    /**
     * Sets maximum processing time.
     * 
     * @param maxProcessingTime Maximum processing time in milliseconds
     */
    public void setMaxProcessingTime(long maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    /**
     * Gets minimum accuracy.
     * 
     * @return Minimum accuracy (0.0 to 1.0)
     */
    public double getMinimumAccuracy() {
        return minimumAccuracy;
    }

    /**
     * Sets minimum accuracy.
     * 
     * @param minimumAccuracy Minimum accuracy (0.0 to 1.0)
     */
    public void setMinimumAccuracy(double minimumAccuracy) {
        this.minimumAccuracy = minimumAccuracy;
    }

    /**
     * Gets minimum mapping efficiency.
     * 
     * @return Minimum mapping efficiency (0.0 to 1.0)
     */
    public double getMinimumMappingEfficiency() {
        return minimumMappingEfficiency;
    }

    /**
     * Sets minimum mapping efficiency.
     * 
     * @param minimumMappingEfficiency Minimum mapping efficiency (0.0 to 1.0)
     */
    public void setMinimumMappingEfficiency(double minimumMappingEfficiency) {
        this.minimumMappingEfficiency = minimumMappingEfficiency;
    }

    /**
     * Gets expected claims count.
     * 
     * @return Expected claims count
     */
    public int getExpectedClaimsCount() {
        return expectedClaims != null ? expectedClaims.size() : 0;
    }

    /**
     * Gets expected mapped claims count.
     * 
     * @return Expected mapped claims count
     */
    public int getExpectedMappedClaimsCount() {
        return expectedMappedClaims != null ? expectedMappedClaims.size() : 0;
    }

    /**
     * Checks if has expected claims.
     * 
     * @return true if has expected claims
     */
    public boolean hasExpectedClaims() {
        return expectedClaims != null && !expectedClaims.isEmpty();
    }

    /**
     * Checks if has expected mapped claims.
     * 
     * @return true if has expected mapped claims
     */
    public boolean hasExpectedMappedClaims() {
        return expectedMappedClaims != null && !expectedMappedClaims.isEmpty();
    }

    /**
     * Gets expectations summary.
     * 
     * @return Expectations summary string
     */
    public String getExpectationsSummary() {
        return String.format("Expected: %d claims, %d mapped claims, status: %s, max time: %d ms, min accuracy: %.1f%%, min efficiency: %.1f%%",
                getExpectedClaimsCount(), getExpectedMappedClaimsCount(), expectedStatus, 
                maxProcessingTime, minimumAccuracy * 100, minimumMappingEfficiency * 100);
    }
}
