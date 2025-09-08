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

import java.util.Map;
import java.util.List;

/**
 * DFDP Claim Analysis DTO.
 * Part 7: Response Generation - Detailed claim analysis information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DFDPClaimAnalysis {

    @JsonProperty("originalClaims")
    private Map<String, String> originalClaims;

    @JsonProperty("mappedClaims")
    private Map<String, String> mappedClaims;

    @JsonProperty("finalClaims")
    private Map<String, String> finalClaims;

    @JsonProperty("claimMappings")
    private Map<String, String> claimMappings;

    @JsonProperty("unmappedClaims")
    private List<String> unmappedClaims;

    @JsonProperty("missingExpectedClaims")
    private List<String> missingExpectedClaims;

    @JsonProperty("unexpectedClaims")
    private List<String> unexpectedClaims;

    @JsonProperty("claimTransformations")
    private List<DFDPClaimTransformation> claimTransformations;

    @JsonProperty("mappingStatistics")
    private DFDPMappingStatistics mappingStatistics;

    // Getters and setters

    public Map<String, String> getOriginalClaims() {
        return originalClaims;
    }

    public void setOriginalClaims(Map<String, String> originalClaims) {
        this.originalClaims = originalClaims;
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

    public Map<String, String> getClaimMappings() {
        return claimMappings;
    }

    public void setClaimMappings(Map<String, String> claimMappings) {
        this.claimMappings = claimMappings;
    }

    public List<String> getUnmappedClaims() {
        return unmappedClaims;
    }

    public void setUnmappedClaims(List<String> unmappedClaims) {
        this.unmappedClaims = unmappedClaims;
    }

    public List<String> getMissingExpectedClaims() {
        return missingExpectedClaims;
    }

    public void setMissingExpectedClaims(List<String> missingExpectedClaims) {
        this.missingExpectedClaims = missingExpectedClaims;
    }

    public List<String> getUnexpectedClaims() {
        return unexpectedClaims;
    }

    public void setUnexpectedClaims(List<String> unexpectedClaims) {
        this.unexpectedClaims = unexpectedClaims;
    }

    public List<DFDPClaimTransformation> getClaimTransformations() {
        return claimTransformations;
    }

    public void setClaimTransformations(List<DFDPClaimTransformation> claimTransformations) {
        this.claimTransformations = claimTransformations;
    }

    public DFDPMappingStatistics getMappingStatistics() {
        return mappingStatistics;
    }

    public void setMappingStatistics(DFDPMappingStatistics mappingStatistics) {
        this.mappingStatistics = mappingStatistics;
    }

    /**
     * DFDP Claim Transformation DTO.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DFDPClaimTransformation {

        @JsonProperty("fromClaim")
        private String fromClaim;

        @JsonProperty("toClaim")
        private String toClaim;

        @JsonProperty("originalValue")
        private String originalValue;

        @JsonProperty("transformedValue")
        private String transformedValue;

        @JsonProperty("transformationType")
        private String transformationType;

        @JsonProperty("successful")
        private boolean successful;

        @JsonProperty("errorMessage")
        private String errorMessage;

        // Getters and setters

        public String getFromClaim() {
            return fromClaim;
        }

        public void setFromClaim(String fromClaim) {
            this.fromClaim = fromClaim;
        }

        public String getToClaim() {
            return toClaim;
        }

        public void setToClaim(String toClaim) {
            this.toClaim = toClaim;
        }

        public String getOriginalValue() {
            return originalValue;
        }

        public void setOriginalValue(String originalValue) {
            this.originalValue = originalValue;
        }

        public String getTransformedValue() {
            return transformedValue;
        }

        public void setTransformedValue(String transformedValue) {
            this.transformedValue = transformedValue;
        }

        public String getTransformationType() {
            return transformationType;
        }

        public void setTransformationType(String transformationType) {
            this.transformationType = transformationType;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * DFDP Mapping Statistics DTO.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DFDPMappingStatistics {

        @JsonProperty("totalOriginalClaims")
        private int totalOriginalClaims;

        @JsonProperty("totalMappedClaims")
        private int totalMappedClaims;

        @JsonProperty("totalFinalClaims")
        private int totalFinalClaims;

        @JsonProperty("successfulMappings")
        private int successfulMappings;

        @JsonProperty("failedMappings")
        private int failedMappings;

        @JsonProperty("mappingSuccessRate")
        private double mappingSuccessRate;

        @JsonProperty("transformationsApplied")
        private int transformationsApplied;

        @JsonProperty("claimsDropped")
        private int claimsDropped;

        @JsonProperty("claimsAdded")
        private int claimsAdded;

        // Getters and setters

        public int getTotalOriginalClaims() {
            return totalOriginalClaims;
        }

        public void setTotalOriginalClaims(int totalOriginalClaims) {
            this.totalOriginalClaims = totalOriginalClaims;
        }

        public int getTotalMappedClaims() {
            return totalMappedClaims;
        }

        public void setTotalMappedClaims(int totalMappedClaims) {
            this.totalMappedClaims = totalMappedClaims;
        }

        public int getTotalFinalClaims() {
            return totalFinalClaims;
        }

        public void setTotalFinalClaims(int totalFinalClaims) {
            this.totalFinalClaims = totalFinalClaims;
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

        public double getMappingSuccessRate() {
            return mappingSuccessRate;
        }

        public void setMappingSuccessRate(double mappingSuccessRate) {
            this.mappingSuccessRate = mappingSuccessRate;
        }

        public int getTransformationsApplied() {
            return transformationsApplied;
        }

        public void setTransformationsApplied(int transformationsApplied) {
            this.transformationsApplied = transformationsApplied;
        }

        public int getClaimsDropped() {
            return claimsDropped;
        }

        public void setClaimsDropped(int claimsDropped) {
            this.claimsDropped = claimsDropped;
        }

        public int getClaimsAdded() {
            return claimsAdded;
        }

        public void setClaimsAdded(int claimsAdded) {
            this.claimsAdded = claimsAdded;
        }
    }
}
