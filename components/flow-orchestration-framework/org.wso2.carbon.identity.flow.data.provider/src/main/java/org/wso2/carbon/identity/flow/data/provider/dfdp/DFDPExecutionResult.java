/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use t    /**
     * Gets claim analysis results.
     * 
     * @return Claim analysis
     */
    public DFDPClaimAnalysis getClaimAnalysis() {
        return claimAnalysis;
    }

    /**
     * Sets claim analysis results.
     * 
     * @param claimAnalysis Claim analysis
     */
    public void setClaimAnalysis(DFDPClaimAnalysis claimAnalysis) {
        this.claimAnalysis = claimAnalysis;
    }

    /**
     * Gets authenticator properties.s file except
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

import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPClaimAnalysis;

import java.util.HashMap;
import java.util.Map;

/**
 * DFDP Execution Result.
 * This class holds the results of DFDP authenticator execution including
 * retrieved claims, mapped claims, authenticator properties, and execution status.
 */
public class DFDPExecutionResult {

    private String requestId;
    private String status;
    private String targetIdP;
    private String authenticatorName;
    private String errorMessage;
    private Map<String, String> retrievedClaims;
    private Map<String, String> mappedClaims;
    private DFDPClaimAnalysis claimAnalysis;
    private Map<String, String> authenticatorProperties;
    private long executionTimeMs;
    private Map<String, Object> additionalData;

    public DFDPExecutionResult() {
        this.retrievedClaims = new HashMap<>();
        this.mappedClaims = new HashMap<>();
        this.authenticatorProperties = new HashMap<>();
        this.additionalData = new HashMap<>();
        this.executionTimeMs = 0;
    }

    /**
     * Gets the request ID.
     * 
     * @return Request ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the request ID.
     * 
     * @param requestId Request ID
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the execution status.
     * 
     * @return Execution status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the execution status.
     * 
     * @param status Execution status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the target Identity Provider name.
     * 
     * @return Target IdP name
     */
    public String getTargetIdP() {
        return targetIdP;
    }

    /**
     * Sets the target Identity Provider name.
     * 
     * @param targetIdP Target IdP name
     */
    public void setTargetIdP(String targetIdP) {
        this.targetIdP = targetIdP;
    }

    /**
     * Gets the authenticator name.
     * 
     * @return Authenticator name
     */
    public String getAuthenticatorName() {
        return authenticatorName;
    }

    /**
     * Sets the authenticator name.
     * 
     * @param authenticatorName Authenticator name
     */
    public void setAuthenticatorName(String authenticatorName) {
        this.authenticatorName = authenticatorName;
    }

    /**
     * Gets the error message if execution failed.
     * 
     * @return Error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message.
     * 
     * @param errorMessage Error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets the retrieved claims from external IdP.
     * 
     * @return Map of retrieved claims
     */
    public Map<String, String> getRetrievedClaims() {
        return retrievedClaims;
    }

    /**
     * Sets the retrieved claims from external IdP.
     * 
     * @param retrievedClaims Map of retrieved claims
     */
    public void setRetrievedClaims(Map<String, String> retrievedClaims) {
        this.retrievedClaims = retrievedClaims != null ? retrievedClaims : new HashMap<>();
    }

    /**
     * Gets the mapped claims after applying claim mappings.
     * 
     * @return Map of mapped claims
     */
    public Map<String, String> getMappedClaims() {
        return mappedClaims;
    }

    /**
     * Sets the mapped claims after applying claim mappings.
     * 
     * @param mappedClaims Map of mapped claims
     */
    public void setMappedClaims(Map<String, String> mappedClaims) {
        this.mappedClaims = mappedClaims != null ? mappedClaims : new HashMap<>();
    }

    /**
     * Gets the authenticator properties.
     * 
     * @return Map of authenticator properties
     */
    public Map<String, String> getAuthenticatorProperties() {
        return authenticatorProperties;
    }

    /**
     * Sets the authenticator properties.
     * 
     * @param authenticatorProperties Map of authenticator properties
     */
    public void setAuthenticatorProperties(Map<String, String> authenticatorProperties) {
        this.authenticatorProperties = authenticatorProperties != null ? authenticatorProperties : new HashMap<>();
    }

    /**
     * Gets the execution time in milliseconds.
     * 
     * @return Execution time in milliseconds
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Sets the execution time in milliseconds.
     * 
     * @param executionTimeMs Execution time in milliseconds
     */
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    /**
     * Gets additional data that might be useful for debugging.
     * 
     * @return Map of additional data
     */
    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    /**
     * Sets additional data that might be useful for debugging.
     * 
     * @param additionalData Map of additional data
     */
    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData != null ? additionalData : new HashMap<>();
    }

    /**
     * Adds a piece of additional data.
     * 
     * @param key Data key
     * @param value Data value
     */
    public void addAdditionalData(String key, Object value) {
        if (this.additionalData == null) {
            this.additionalData = new HashMap<>();
        }
        this.additionalData.put(key, value);
    }

    /**
     * Checks if the execution was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    /**
     * Checks if the execution failed.
     * 
     * @return true if failed, false otherwise
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Gets the total number of retrieved claims.
     * 
     * @return Number of retrieved claims
     */
    public int getRetrievedClaimsCount() {
        return retrievedClaims != null ? retrievedClaims.size() : 0;
    }

    /**
     * Gets the total number of mapped claims.
     * 
     * @return Number of mapped claims
     */
    public int getMappedClaimsCount() {
        return mappedClaims != null ? mappedClaims.size() : 0;
    }

    @Override
    public String toString() {
        return "DFDPExecutionResult{" +
                "requestId='" + requestId + '\'' +
                ", status='" + status + '\'' +
                ", targetIdP='" + targetIdP + '\'' +
                ", authenticatorName='" + authenticatorName + '\'' +
                ", retrievedClaimsCount=" + getRetrievedClaimsCount() +
                ", mappedClaimsCount=" + getMappedClaimsCount() +
                ", executionTimeMs=" + executionTimeMs +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
