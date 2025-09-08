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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPClaimAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DFDP Claim Processor.
 * This class handles advanced claim processing operations for DFDP including
 * claim mapping validation, transformation, filtering, and analysis.
 */
public class DFDPClaimProcessor {

    private static final Log log = LogFactory.getLog(DFDPClaimProcessor.class);

    // Common claim URI patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".*email.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile(".*name.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern ROLE_PATTERN = Pattern.compile(".*role.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern GROUP_PATTERN = Pattern.compile(".*group.*", Pattern.CASE_INSENSITIVE);

    /**
     * Processes and analyzes claims for DFDP execution.
     * 
     * @param retrievedClaims Claims retrieved from external IdP
     * @param identityProvider Identity Provider configuration
     * @return DFDPClaimAnalysis containing processed results
     */
    public DFDPClaimAnalysis processAndAnalyzeClaims(Map<String, String> retrievedClaims,
                                                    IdentityProvider identityProvider) {

        DFDPClaimAnalysis analysis = new DFDPClaimAnalysis();
        analysis.setOriginalClaims(retrievedClaims);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting DFDP claim processing for IdP: " + identityProvider.getIdentityProviderName() +
                         ", Input claims: " + retrievedClaims.size());
            }

            // Step 1: Apply claim mappings
            Map<String, String> mappedClaims = applyClaimMappings(retrievedClaims, identityProvider);
            analysis.setMappedClaims(mappedClaims);

            // Step 2: Process role mappings
            Map<String, String> roleMappedClaims = processRoleMappings(mappedClaims, identityProvider);
            analysis.setRoleMappedClaims(roleMappedClaims);

            // Step 3: Validate claim mappings
            List<DFDPClaimValidation> validations = validateClaimMappings(retrievedClaims, mappedClaims, identityProvider);
            analysis.setValidations(validations);

            // Step 4: Categorize claims
            DFDPClaimCategories categories = categorizeClaims(mappedClaims);
            analysis.setCategories(categories);

            // Step 5: Analyze claim coverage
            DFDPClaimCoverage coverage = analyzeClaimCoverage(retrievedClaims, identityProvider);
            analysis.setCoverage(coverage);

            analysis.setProcessingStatus("SUCCESS");

            if (log.isDebugEnabled()) {
                log.debug("DFDP claim processing completed. Mapped: " + mappedClaims.size() +
                         ", Validations: " + validations.size() + ", Status: " + analysis.getProcessingStatus());
            }

        } catch (Exception e) {
            log.error("Error processing DFDP claims", e);
            analysis.setProcessingStatus("FAILED");
            analysis.setErrorMessage(e.getMessage());
        }

        return analysis;
    }

    /**
     * Applies claim mappings based on Identity Provider configuration.
     * 
     * @param retrievedClaims Claims retrieved from external IdP
     * @param identityProvider Identity Provider configuration
     * @return Map of mapped claims
     */
    private Map<String, String> applyClaimMappings(Map<String, String> retrievedClaims,
                                                  IdentityProvider identityProvider) {

        Map<String, String> mappedClaims = new HashMap<>();
        
        // Get claim mappings from IdP configuration
        ClaimMapping[] claimMappings = identityProvider.getClaimConfig() != null ? 
                                     identityProvider.getClaimConfig().getClaimMappings() : null;

        if (claimMappings != null && claimMappings.length > 0) {
            // Apply configured claim mappings
            for (ClaimMapping claimMapping : claimMappings) {
                String remoteClaimUri = claimMapping.getRemoteClaim().getClaimUri();
                String localClaimUri = claimMapping.getLocalClaim().getClaimUri();
                
                if (retrievedClaims.containsKey(remoteClaimUri)) {
                    String claimValue = retrievedClaims.get(remoteClaimUri);
                    
                    // Apply any transformation if needed
                    String transformedValue = transformClaimValue(claimValue, claimMapping);
                    mappedClaims.put(localClaimUri, transformedValue);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Mapped claim: " + remoteClaimUri + " -> " + localClaimUri + " = " + transformedValue);
                    }
                }
            }
        } else {
            // No explicit mappings, use direct mapping
            mappedClaims.putAll(retrievedClaims);
            
            if (log.isDebugEnabled()) {
                log.debug("No claim mappings configured, using direct mapping");
            }
        }

        return mappedClaims;
    }

    /**
     * Processes role mappings for the claims.
     * 
     * @param mappedClaims Mapped claims
     * @param identityProvider Identity Provider configuration
     * @return Map of claims with role mappings applied
     */
    private Map<String, String> processRoleMappings(Map<String, String> mappedClaims,
                                                   IdentityProvider identityProvider) {

        Map<String, String> roleMappedClaims = new HashMap<>(mappedClaims);

        PermissionsAndRoleConfig roleConfig = identityProvider.getPermissionAndRoleConfig();
        if (roleConfig != null && roleConfig.getRoleMappings() != null) {
            
            RoleMapping[] roleMappings = roleConfig.getRoleMappings();
            for (RoleMapping roleMapping : roleMappings) {
                String remoteRole = roleMapping.getRemoteRole();
                LocalRole localRole = roleMapping.getLocalRole();
                
                // Check if any claim contains this remote role
                for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
                    String claimUri = entry.getKey();
                    String claimValue = entry.getValue();
                    
                    if (ROLE_PATTERN.matcher(claimUri).matches() && claimValue.contains(remoteRole)) {
                        // Replace remote role with local role
                        String updatedValue = claimValue.replace(remoteRole, localRole.getLocalRoleName());
                        roleMappedClaims.put(claimUri, updatedValue);
                        
                        if (log.isDebugEnabled()) {
                            log.debug("Applied role mapping: " + remoteRole + " -> " + localRole.getLocalRoleName());
                        }
                    }
                }
            }
        }

        return roleMappedClaims;
    }

    /**
     * Validates claim mappings and identifies potential issues.
     * 
     * @param retrievedClaims Original retrieved claims
     * @param mappedClaims Mapped claims
     * @param identityProvider Identity Provider configuration
     * @return List of validation results
     */
    private List<DFDPClaimValidation> validateClaimMappings(Map<String, String> retrievedClaims,
                                                           Map<String, String> mappedClaims,
                                                           IdentityProvider identityProvider) {

        List<DFDPClaimValidation> validations = new ArrayList<>();

        // Check for unmapped claims
        for (String retrievedClaim : retrievedClaims.keySet()) {
            boolean mapped = false;
            ClaimMapping[] claimMappings = identityProvider.getClaimConfig() != null ? 
                                         identityProvider.getClaimConfig().getClaimMappings() : null;
            
            if (claimMappings != null) {
                for (ClaimMapping claimMapping : claimMappings) {
                    if (retrievedClaim.equals(claimMapping.getRemoteClaim().getClaimUri())) {
                        mapped = true;
                        break;
                    }
                }
            }
            
            if (!mapped) {
                DFDPClaimValidation validation = new DFDPClaimValidation();
                validation.setType("UNMAPPED_CLAIM");
                validation.setSeverity("WARNING");
                validation.setClaimUri(retrievedClaim);
                validation.setMessage("Claim '" + retrievedClaim + "' is not mapped to any local claim");
                validation.setSuggestion("Consider adding a claim mapping for this claim");
                validations.add(validation);
            }
        }

        // Check for missing essential claims
        validateEssentialClaims(mappedClaims, validations);

        // Check for claim value format issues
        validateClaimValueFormats(mappedClaims, validations);

        return validations;
    }

    /**
     * Validates essential claims presence.
     * 
     * @param mappedClaims Mapped claims
     * @param validations List to add validations to
     */
    private void validateEssentialClaims(Map<String, String> mappedClaims, List<DFDPClaimValidation> validations) {

        String[] essentialClaims = {
            "http://wso2.org/claims/username",
            "http://wso2.org/claims/emailaddress"
        };

        for (String essentialClaim : essentialClaims) {
            if (!mappedClaims.containsKey(essentialClaim) || 
                StringUtils.isBlank(mappedClaims.get(essentialClaim))) {
                
                DFDPClaimValidation validation = new DFDPClaimValidation();
                validation.setType("MISSING_ESSENTIAL_CLAIM");
                validation.setSeverity("ERROR");
                validation.setClaimUri(essentialClaim);
                validation.setMessage("Essential claim '" + essentialClaim + "' is missing or empty");
                validation.setSuggestion("Ensure this claim is mapped and has a value from the external IdP");
                validations.add(validation);
            }
        }
    }

    /**
     * Validates claim value formats.
     * 
     * @param mappedClaims Mapped claims
     * @param validations List to add validations to
     */
    private void validateClaimValueFormats(Map<String, String> mappedClaims, List<DFDPClaimValidation> validations) {

        for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
            String claimUri = entry.getKey();
            String claimValue = entry.getValue();
            
            // Validate email format
            if (EMAIL_PATTERN.matcher(claimUri).matches() && !isValidEmail(claimValue)) {
                DFDPClaimValidation validation = new DFDPClaimValidation();
                validation.setType("INVALID_CLAIM_FORMAT");
                validation.setSeverity("WARNING");
                validation.setClaimUri(claimUri);
                validation.setMessage("Claim '" + claimUri + "' has invalid email format: " + claimValue);
                validation.setSuggestion("Verify the email format from the external IdP");
                validations.add(validation);
            }
        }
    }

    /**
     * Categorizes claims into different types.
     * 
     * @param mappedClaims Mapped claims
     * @return DFDPClaimCategories
     */
    private DFDPClaimCategories categorizeClaims(Map<String, String> mappedClaims) {

        DFDPClaimCategories categories = new DFDPClaimCategories();

        for (Map.Entry<String, String> entry : mappedClaims.entrySet()) {
            String claimUri = entry.getKey();
            String claimValue = entry.getValue();
            
            if (EMAIL_PATTERN.matcher(claimUri).matches()) {
                categories.addEmailClaim(claimUri, claimValue);
            } else if (NAME_PATTERN.matcher(claimUri).matches()) {
                categories.addNameClaim(claimUri, claimValue);
            } else if (ROLE_PATTERN.matcher(claimUri).matches()) {
                categories.addRoleClaim(claimUri, claimValue);
            } else if (GROUP_PATTERN.matcher(claimUri).matches()) {
                categories.addGroupClaim(claimUri, claimValue);
            } else {
                categories.addOtherClaim(claimUri, claimValue);
            }
        }

        return categories;
    }

    /**
     * Analyzes claim coverage and completeness.
     * 
     * @param retrievedClaims Retrieved claims
     * @param identityProvider Identity Provider configuration
     * @return DFDPClaimCoverage
     */
    private DFDPClaimCoverage analyzeClaimCoverage(Map<String, String> retrievedClaims,
                                                  IdentityProvider identityProvider) {

        DFDPClaimCoverage coverage = new DFDPClaimCoverage();
        coverage.setTotalRetrievedClaims(retrievedClaims.size());

        ClaimMapping[] claimMappings = identityProvider.getClaimConfig() != null ? 
                                     identityProvider.getClaimConfig().getClaimMappings() : null;

        if (claimMappings != null) {
            coverage.setTotalConfiguredMappings(claimMappings.length);
            
            int mappedCount = 0;
            for (ClaimMapping claimMapping : claimMappings) {
                if (retrievedClaims.containsKey(claimMapping.getRemoteClaim().getClaimUri())) {
                    mappedCount++;
                }
            }
            coverage.setMappedClaimsCount(mappedCount);
            coverage.setUnmappedClaimsCount(claimMappings.length - mappedCount);
            
            double coveragePercentage = claimMappings.length > 0 ? 
                                      (double) mappedCount / claimMappings.length * 100 : 0;
            coverage.setCoveragePercentage(coveragePercentage);
        }

        return coverage;
    }

    /**
     * Transforms claim value if needed.
     * 
     * @param claimValue Original claim value
     * @param claimMapping Claim mapping configuration
     * @return Transformed claim value
     */
    private String transformClaimValue(String claimValue, ClaimMapping claimMapping) {

        // For now, just return the original value
        // In the future, this can include transformations like:
        // - Case conversion
        // - Format standardization
        // - Value mapping
        return claimValue;
    }

    /**
     * Validates email format.
     * 
     * @param email Email string to validate
     * @return true if valid email format
     */
    private boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        return email.contains("@") && email.contains(".") && !email.startsWith("@") && !email.endsWith("@");
    }
}
