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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.flow.data.provider.dfdp.analysis.DFDPAnalysisResult;
import org.wso2.carbon.identity.flow.data.provider.dfdp.analysis.DFDPTestReport;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPEventManager;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.Instant;

/**
 * DFDP Response Builder.
 * Part 7: Response Generation - Builds comprehensive DFDP responses from analysis results.
 */
public class DFDPResponseBuilder {

    private static final Log log = LogFactory.getLog(DFDPResponseBuilder.class);

    /**
     * Builds a complete DFDP response from analysis results.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param analysisResult Analysis results
     * @param testReport Test report
     * @param executionTimeMs Execution time in milliseconds
     * @return Complete DFDP response
     */
    public static DFDPResponse buildResponse(String requestId, AuthenticationContext context,
                                           DFDPAnalysisResult analysisResult, DFDPTestReport testReport,
                                           long executionTimeMs) {
        try {
            DFDPResponse response = new DFDPResponse(requestId);
            response.setExecutionTimeMs(executionTimeMs);

            // Set basic status
            if (analysisResult != null && analysisResult.isSuccessful()) {
                response.setStatus("SUCCESS");
                response.setMessage("DFDP test execution completed successfully");
            } else {
                response.setStatus("FAILURE");
                response.setMessage("DFDP test execution completed with issues");
            }

            // Build test summary
            response.setTestSummary(buildTestSummary(requestId, context, analysisResult, testReport, executionTimeMs));

            // Build claim analysis
            response.setClaimAnalysis(buildClaimAnalysis(analysisResult));

            // Build authenticator info
            response.setAuthenticatorInfo(buildAuthenticatorInfo(context, analysisResult));

            // Set analysis results
            response.setAnalysisResults(analysisResult);

            // Set test report
            response.setTestReport(testReport);

            // Build timeline
            response.setTimeline(buildTimeline(requestId));

            // Build errors and warnings
            buildErrorsAndWarnings(response, analysisResult, testReport);

            // Add metadata
            response.setMetadata(buildMetadata(context, analysisResult));

            return response;

        } catch (Exception e) {
            log.error("Error building DFDP response for request " + requestId, e);
            return buildErrorResponse(requestId, "RESPONSE_BUILD_ERROR", e.getMessage(), executionTimeMs);
        }
    }

    /**
     * Builds an error response for DFDP failures.
     * 
     * @param requestId Request ID
     * @param errorCode Error code
     * @param errorMessage Error message
     * @param executionTimeMs Execution time
     * @return Error response
     */
    public static DFDPResponse buildErrorResponse(String requestId, String errorCode, 
                                                String errorMessage, long executionTimeMs) {
        DFDPResponse response = new DFDPResponse(requestId);
        response.setStatus("ERROR");
        response.setMessage("DFDP test execution failed");
        response.setExecutionTimeMs(executionTimeMs);

        // Add error details
        response.addError(errorCode, errorMessage, "DFDP execution encountered an error");

        return response;
    }

    /**
     * Builds a validation response for DFDP parameter validation.
     * 
     * @param requestId Request ID
     * @param message Validation message
     * @param parameters Validated parameters
     * @return DFDP validation response
     */
    public static DFDPResponse buildValidationResponse(String requestId, String message, String parameters) {
        DFDPResponse response = new DFDPResponse();
        response.setRequestId(requestId);
        response.setStatus("VALIDATION_SUCCESS");
        response.setMessage(message);
        response.setTimestamp(Instant.now().toString());
        response.setExecutionTimeMs(0); // No execution for validation
        
        // Add validation metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("validationType", "PARAMETER_VALIDATION");
        metadata.put("validatedParameters", parameters);
        metadata.put("validationTimestamp", Instant.now().toString());
        response.setMetadata(metadata);
        
        return response;
    }

    /**
     * Builds test summary from analysis results.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param analysisResult Analysis results
     * @param testReport Test report
     * @param executionTimeMs Execution time
     * @return Test summary
     */
    private static DFDPTestSummary buildTestSummary(String requestId, AuthenticationContext context,
                                                   DFDPAnalysisResult analysisResult, DFDPTestReport testReport,
                                                   long executionTimeMs) {
        DFDPTestSummary summary = new DFDPTestSummary();
        summary.setTestId(requestId);
        summary.setTestName("DFDP Claim Mapping Test");
        summary.setTestDurationMs(executionTimeMs);
        summary.setStartTime(System.currentTimeMillis() - executionTimeMs);
        summary.setEndTime(System.currentTimeMillis());

        if (context != null) {
            summary.setTargetIdP(getStringProperty(context, "dfdpTargetIdp"));
            summary.setTargetAuthenticator(getStringProperty(context, "dfdpTargetAuthenticator"));
        }

        if (analysisResult != null) {
            summary.setTestStatus(analysisResult.isSuccessful() ? "SUCCESS" : "FAILURE");
            summary.setTotalClaims(analysisResult.getTotalClaims());
            summary.setSuccessfulMappings(analysisResult.getSuccessfulMappings());
            summary.setFailedMappings(analysisResult.getTotalClaims() - analysisResult.getSuccessfulMappings());
        }

        if (testReport != null) {
            summary.setCompletionPercentage(testReport.getCompletionPercentage());
            summary.setExecutionSteps(testReport.getEventCount());
        }

        return summary;
    }

    /**
     * Builds claim analysis from results.
     * 
     * @param analysisResult Analysis results
     * @return Claim analysis
     */
    private static DFDPClaimAnalysis buildClaimAnalysis(DFDPAnalysisResult analysisResult) {
        DFDPClaimAnalysis claimAnalysis = new DFDPClaimAnalysis();

        if (analysisResult != null) {
            claimAnalysis.setOriginalClaims(analysisResult.getOriginalClaims());
            claimAnalysis.setMappedClaims(analysisResult.getMappedClaims());
            claimAnalysis.setFinalClaims(analysisResult.getFinalClaims());
            claimAnalysis.setUnmappedClaims(new ArrayList<>(analysisResult.getUnmappedClaims()));
            claimAnalysis.setMissingExpectedClaims(new ArrayList<>(analysisResult.getMissingExpectedClaims()));
            claimAnalysis.setUnexpectedClaims(new ArrayList<>(analysisResult.getUnexpectedClaims()));

            // Build mapping statistics
            DFDPClaimAnalysis.DFDPMappingStatistics stats = new DFDPClaimAnalysis.DFDPMappingStatistics();
            stats.setTotalOriginalClaims(analysisResult.getOriginalClaims() != null ? 
                                        analysisResult.getOriginalClaims().size() : 0);
            stats.setTotalMappedClaims(analysisResult.getMappedClaims() != null ? 
                                      analysisResult.getMappedClaims().size() : 0);
            stats.setTotalFinalClaims(analysisResult.getFinalClaims() != null ? 
                                     analysisResult.getFinalClaims().size() : 0);
            stats.setSuccessfulMappings(analysisResult.getSuccessfulMappings());
            stats.setFailedMappings(analysisResult.getTotalClaims() - analysisResult.getSuccessfulMappings());
            stats.setMappingSuccessRate(analysisResult.getSuccessRate());

            claimAnalysis.setMappingStatistics(stats);
        }

        return claimAnalysis;
    }

    /**
     * Builds authenticator information.
     * 
     * @param context Authentication context
     * @param analysisResult Analysis results
     * @return Authenticator info
     */
    private static DFDPAuthenticatorInfo buildAuthenticatorInfo(AuthenticationContext context,
                                                              DFDPAnalysisResult analysisResult) {
        DFDPAuthenticatorInfo info = new DFDPAuthenticatorInfo();

        if (context != null) {
            info.setAuthenticatorName(getStringProperty(context, "dfdpTargetAuthenticator"));
            info.setIdentityProviderName(getStringProperty(context, "dfdpTargetIdp"));
            
            if (context.getExternalIdP() != null) {
                info.setIdentityProviderName(context.getExternalIdP().getIdPName());
            }
        }

        if (analysisResult != null) {
            info.setAuthenticationStatus(analysisResult.isSuccessful() ? "SUCCESS" : "FAILURE");
            info.setResponseReceived(analysisResult.getOriginalClaims() != null && 
                                   !analysisResult.getOriginalClaims().isEmpty());
            info.setClaimsExtracted(info.isResponseReceived());
        }

        return info;
    }

    /**
     * Builds timeline from event manager.
     * 
     * @param requestId Request ID
     * @return Timeline entries
     */
    private static List<DFDPTimelineEntry> buildTimeline(String requestId) {
        List<DFDPTimelineEntry> timeline = new ArrayList<>();

        try {
            // This would integrate with the event manager to get timeline data
            // For now, we'll create a basic timeline structure
            timeline.add(new DFDPTimelineEntry("INITIALIZATION", "REQUEST_RECEIVED", 
                                             "DFDP request received and validated"));
            timeline.add(new DFDPTimelineEntry("SETUP", "AUTHENTICATOR_CONFIGURED", 
                                             "Authenticator setup completed"));
            timeline.add(new DFDPTimelineEntry("EXECUTION", "CLAIMS_PROCESSED", 
                                             "Claim processing completed"));
            timeline.add(new DFDPTimelineEntry("ANALYSIS", "RESULTS_GENERATED", 
                                             "Analysis results generated"));

        } catch (Exception e) {
            log.debug("Error building timeline for request " + requestId, e);
        }

        return timeline;
    }

    /**
     * Builds errors and warnings from analysis results.
     * 
     * @param response Response to populate
     * @param analysisResult Analysis results
     * @param testReport Test report
     */
    private static void buildErrorsAndWarnings(DFDPResponse response, DFDPAnalysisResult analysisResult,
                                             DFDPTestReport testReport) {
        if (analysisResult != null) {
            // Add errors for failed mappings
            if (!analysisResult.isSuccessful()) {
                response.addError("MAPPING_FAILURE", "Some claim mappings failed", 
                                "Check claim configuration and IdP response");
            }

            // Add warnings for unmapped claims
            if (analysisResult.getUnmappedClaims() != null && !analysisResult.getUnmappedClaims().isEmpty()) {
                response.addWarning("UNMAPPED_CLAIMS", "Some claims were not mapped", 
                                  "Consider adding claim mappings for: " + 
                                  String.join(", ", analysisResult.getUnmappedClaims()));
            }

            // Add warnings for missing expected claims
            if (analysisResult.getMissingExpectedClaims() != null && 
                !analysisResult.getMissingExpectedClaims().isEmpty()) {
                response.addWarning("MISSING_EXPECTED_CLAIMS", "Expected claims not received", 
                                  "Missing claims: " + 
                                  String.join(", ", analysisResult.getMissingExpectedClaims()));
            }
        }
    }

    /**
     * Builds metadata from context and results.
     * 
     * @param context Authentication context
     * @param analysisResult Analysis results
     * @return Metadata map
     */
    private static Map<String, Object> buildMetadata(AuthenticationContext context, 
                                                   DFDPAnalysisResult analysisResult) {
        Map<String, Object> metadata = new HashMap<>();

        if (context != null) {
            metadata.put("tenantDomain", context.getTenantDomain());
            metadata.put("serviceProvider", context.getServiceProviderName());
        }

        metadata.put("dfdpVersion", "1.0.0");
        metadata.put("analysisEngine", "DFDP Analysis Engine");
        metadata.put("generatedAt", System.currentTimeMillis());

        return metadata;
    }

    /**
     * Gets string property from context.
     * 
     * @param context Authentication context
     * @param propertyName Property name
     * @return Property value or null
     */
    private static String getStringProperty(AuthenticationContext context, String propertyName) {
        Object value = context.getProperty(propertyName);
        return value != null ? value.toString() : null;
    }
}
