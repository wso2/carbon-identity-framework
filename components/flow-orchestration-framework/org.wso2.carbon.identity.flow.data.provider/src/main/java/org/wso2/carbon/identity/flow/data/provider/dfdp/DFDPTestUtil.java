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

package org.wso2.carbon.identity.flow.data.provider.dfdp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPClaimAnalysis;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * DFDP Test Utility.
 * This class provides utility functions for testing DFDP functionality.
 */
public class DFDPTestUtil {

    private static final Log log = LogFactory.getLog(DFDPTestUtil.class);

    /**
     * Creates a test authentication context for DFDP testing.
     * 
     * @param request HTTP servlet request
     * @return Test authentication context
     */
    public static AuthenticationContext createTestContext(HttpServletRequest request) {

        AuthenticationContext context = new AuthenticationContext();
        
        // Set basic context properties
        context.setContextIdentifier("dfdp-test-" + System.currentTimeMillis());
        context.setRequestType("dfdp");
        context.setRememberMe(false);
        context.setForceAuthenticate(false);
        context.setPassiveAuthenticate(false);
        
        // Set DFDP specific properties
        context.setProperty(FrameworkConstants.DFDP_EXECUTION_MODE, "true");
        
        // Extract DFDP parameters from request
        String targetIdP = request.getParameter("targetIdP");
        String testClaims = request.getParameter("testClaims");
        String simulateErrors = request.getParameter("simulateErrors");
        
        if (targetIdP != null) {
            context.setProperty(FrameworkConstants.DFDP_TARGET_IDP, targetIdP);
        }
        
        if (testClaims != null) {
            context.setProperty(FrameworkConstants.DFDP_TEST_CLAIMS, testClaims);
        }
        
        if ("true".equals(simulateErrors)) {
            context.setProperty(FrameworkConstants.DFDP_SIMULATE_ERRORS, "true");
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Created DFDP test context with ID: " + context.getContextIdentifier());
        }
        
        return context;
    }

    /**
     * Creates a test Identity Provider configuration.
     * 
     * @param idpName Identity Provider name
     * @param idpType Identity Provider type (SAML, OIDC, OAuth2, etc.)
     * @return Test Identity Provider
     */
    public static IdentityProvider createTestIdentityProvider(String idpName, String idpType) {

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setIdentityProviderName(idpName);
        identityProvider.setDisplayName(idpName + " Test IdP");
        identityProvider.setIdentityProviderDescription("Test Identity Provider for DFDP testing");
        identityProvider.setEnable(true);
        identityProvider.setPrimary(false);
        
        // Set up basic claim configuration
        setupTestClaimConfiguration(identityProvider, idpType);
        
        if (log.isDebugEnabled()) {
            log.debug("Created test Identity Provider: " + idpName + " (Type: " + idpType + ")");
        }
        
        return identityProvider;
    }

    /**
     * Sets up test claim configuration for an Identity Provider.
     * 
     * @param identityProvider Identity Provider to configure
     * @param idpType Identity Provider type
     */
    private static void setupTestClaimConfiguration(IdentityProvider identityProvider, String idpType) {

        // This would typically set up claim mappings based on the IdP type
        // For now, we'll leave this as a placeholder for specific implementations
        
        if (log.isDebugEnabled()) {
            log.debug("Setting up test claim configuration for IdP type: " + idpType);
        }
    }

    /**
     * Creates test claims based on claim type.
     * 
     * @param claimType Type of claims to create (standard, custom, etc.)
     * @return Map of test claims
     */
    public static Map<String, String> createTestClaims(String claimType) {

        Map<String, String> claims = new HashMap<>();
        
        switch (claimType.toLowerCase()) {
            case "standard":
                claims.put("http://wso2.org/claims/emailaddress", "test@example.com");
                claims.put("http://wso2.org/claims/fullname", "Test User");
                claims.put("http://wso2.org/claims/givenname", "Test");
                claims.put("http://wso2.org/claims/lastname", "User");
                break;
                
            case "oidc":
                claims.put("email", "test@example.com");
                claims.put("given_name", "Test");
                claims.put("family_name", "User");
                claims.put("preferred_username", "testuser");
                claims.put("email_verified", "true");
                break;
                
            case "saml":
                claims.put("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress", "test@example.com");
                claims.put("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname", "Test");
                claims.put("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname", "User");
                claims.put("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name", "testuser");
                break;
                
            case "custom":
                claims.put("custom_email", "test@example.com");
                claims.put("custom_name", "Test User");
                claims.put("custom_department", "Engineering");
                claims.put("custom_role", "Developer");
                break;
                
            default:
                claims.put("email", "test@example.com");
                claims.put("name", "Test User");
                break;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Created " + claims.size() + " test claims for type: " + claimType);
        }
        
        return claims;
    }

    /**
     * Validates DFDP execution result.
     * 
     * @param result DFDP execution result
     * @return Validation report
     */
    public static DFDPValidationReport validateExecutionResult(DFDPExecutionResult result) {

        DFDPValidationReport report = new DFDPValidationReport();
        report.setValid(true);
        
        // Check basic result properties
        if (result.getRequestId() == null) {
            report.addIssue("MISSING_REQUEST_ID", "Request ID is missing");
            report.setValid(false);
        }
        
        if (result.getStatus() == null) {
            report.addIssue("MISSING_STATUS", "Status is missing");
            report.setValid(false);
        }
        
        if (result.getAuthenticatorName() == null) {
            report.addIssue("MISSING_AUTHENTICATOR_NAME", "Authenticator name is missing");
            report.setValid(false);
        }
        
        // Check claims
        if (result.getRetrievedClaims() == null || result.getRetrievedClaims().isEmpty()) {
            report.addIssue("NO_RETRIEVED_CLAIMS", "No claims were retrieved");
        }
        
        if (result.getMappedClaims() == null || result.getMappedClaims().isEmpty()) {
            report.addIssue("NO_MAPPED_CLAIMS", "No claims were mapped");
        }
        
        // Check claim analysis if available
        if (result.getClaimAnalysis() != null) {
            DFDPClaimAnalysis analysis = result.getClaimAnalysis();
            
            if (!analysis.isSuccessful()) {
                report.addIssue("CLAIM_ANALYSIS_FAILED", "Claim analysis failed: " + analysis.getErrorMessage());
            }
            
            if (analysis.getErrorValidations() > 0) {
                report.addIssue("CLAIM_VALIDATION_ERRORS", 
                               "Found " + analysis.getErrorValidations() + " claim validation errors");
            }
        }
        
        // Check execution time
        if (result.getExecutionTimeMs() <= 0) {
            report.addIssue("INVALID_EXECUTION_TIME", "Invalid execution time: " + result.getExecutionTimeMs());
        }
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP result validation completed. Valid: " + report.isValid() + 
                     ", Issues: " + report.getIssues().size());
        }
        
        return report;
    }

    /**
     * Generates a comprehensive test report for DFDP execution.
     * 
     * @param result DFDP execution result
     * @return Test report string
     */
    public static String generateTestReport(DFDPExecutionResult result) {

        StringBuilder report = new StringBuilder();
        report.append("DFDP Test Execution Report\n");
        report.append("=========================\n\n");
        
        report.append("Request ID: ").append(result.getRequestId()).append("\n");
        report.append("Status: ").append(result.getStatus()).append("\n");
        report.append("Target IdP: ").append(result.getTargetIdP()).append("\n");
        report.append("Authenticator: ").append(result.getAuthenticatorName()).append(" (").append(result.getAuthenticatorType()).append(")\n");
        report.append("Execution Time: ").append(result.getExecutionTimeMs()).append(" ms\n");
        report.append("Successful: ").append(result.isSuccessful()).append("\n\n");
        
        // Claims summary
        report.append("Claims Summary:\n");
        report.append("- Retrieved Claims: ").append(result.getRetrievedClaimsCount()).append("\n");
        report.append("- Mapped Claims: ").append(result.getMappedClaimsCount()).append("\n\n");
        
        // Claim analysis
        if (result.getClaimAnalysis() != null) {
            DFDPClaimAnalysis analysis = result.getClaimAnalysis();
            report.append("Claim Analysis:\n");
            report.append("- Processing Status: ").append(analysis.getProcessingStatus()).append("\n");
            report.append("- Total Validations: ").append(analysis.getTotalValidations()).append("\n");
            report.append("- Error Validations: ").append(analysis.getErrorValidations()).append("\n");
            report.append("- Warning Validations: ").append(analysis.getWarningValidations()).append("\n");
            
            if (analysis.getCoverage() != null) {
                report.append("- Coverage: ").append(analysis.getCoverage().getCoverageSummary()).append("\n");
            }
            
            if (analysis.getCategories() != null) {
                report.append("- Categories: ").append(analysis.getCategories().getCategorySummary()).append("\n");
            }
            report.append("\n");
        }
        
        // Error details
        if (result.getErrorMessage() != null) {
            report.append("Error Details:\n");
            report.append(result.getErrorMessage()).append("\n\n");
        }
        
        report.append("Report generated at: ").append(new java.util.Date()).append("\n");
        
        return report.toString();
    }
}
