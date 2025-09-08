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
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.core.ApplicationAuthenticatorManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.data.provider.dfdp.response.DFDPClaimAnalysis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DFDP Authenticator Executor.
 * This class handles the actual execution of authenticators for DFDP flows,
 * including claim retrieval, mapping, and result processing.
 */
public class DFDPAuthenticatorExecutor {

    private static final Log log = LogFactory.getLog(DFDPAuthenticatorExecutor.class);

    /**
     * Executes the configured authenticator and processes the results.
     * 
     * @param context Authentication context containing DFDP configuration
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @return DFDPExecutionResult containing the execution results
     * @throws FrameworkException if execution fails
     */
    public DFDPExecutionResult executeAuthenticator(AuthenticationContext context, 
                                                   HttpServletRequest request, 
                                                   HttpServletResponse response) throws FrameworkException {

        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);
        StepConfig stepConfig = (StepConfig) context.getProperty(FrameworkConstants.DFDP_STEP_CONFIG);
        IdentityProvider identityProvider = (IdentityProvider) context.getProperty(FrameworkConstants.DFDP_IDENTITY_PROVIDER);

        if (stepConfig == null || identityProvider == null) {
            throw new FrameworkException("DFDP configuration not properly setup");
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting DFDP authenticator execution for request ID: " + requestId + 
                         ", IdP: " + identityProvider.getIdentityProviderName());
            }

            // Get the authenticator configuration
            List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();
            if (authenticatorConfigs == null || authenticatorConfigs.isEmpty()) {
                throw new FrameworkException("No authenticator configuration found for DFDP execution");
            }

            AuthenticatorConfig authenticatorConfig = authenticatorConfigs.get(0);
            
            // Get the actual authenticator instance
            ApplicationAuthenticator authenticator = getAuthenticatorInstance(authenticatorConfig, context.getTenantDomain());
            if (authenticator == null) {
                throw new FrameworkException("Authenticator instance not found: " + authenticatorConfig.getName());
            }

            // Execute the authenticator in DFDP mode
            DFDPExecutionResult result = executeAuthenticatorInDFDPMode(
                authenticator, authenticatorConfig, context, request, response, identityProvider);

            if (log.isDebugEnabled()) {
                log.debug("DFDP authenticator execution completed for request ID: " + requestId + 
                         ", Status: " + result.getStatus());
            }

            return result;

        } catch (Exception e) {
            log.error("Error executing DFDP authenticator for request ID: " + requestId, e);
            throw new FrameworkException("DFDP authenticator execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the authenticator instance for the given configuration.
     * 
     * @param authenticatorConfig Authenticator configuration
     * @param tenantDomain Tenant domain
     * @return ApplicationAuthenticator instance
     * @throws FrameworkException if authenticator cannot be found
     */
    private ApplicationAuthenticator getAuthenticatorInstance(AuthenticatorConfig authenticatorConfig, 
                                                            String tenantDomain) throws FrameworkException {

        try {
            return ApplicationAuthenticatorManager.getInstance()
                    .getApplicationAuthenticatorByName(authenticatorConfig.getName(), tenantDomain);
        } catch (Exception e) {
            throw new FrameworkException("Error retrieving authenticator: " + authenticatorConfig.getName(), e);
        }
    }

    /**
     * Executes the authenticator in DFDP mode.
     * 
     * @param authenticator Application authenticator instance
     * @param authenticatorConfig Authenticator configuration
     * @param context Authentication context
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param identityProvider Identity Provider configuration
     * @return DFDPExecutionResult containing execution results
     * @throws FrameworkException if execution fails
     */
    private DFDPExecutionResult executeAuthenticatorInDFDPMode(ApplicationAuthenticator authenticator,
                                                              AuthenticatorConfig authenticatorConfig,
                                                              AuthenticationContext context,
                                                              HttpServletRequest request,
                                                              HttpServletResponse response,
                                                              IdentityProvider identityProvider) throws FrameworkException {

        DFDPExecutionResult result = new DFDPExecutionResult();
        result.setRequestId((String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID));
        result.setTargetIdP(identityProvider.getIdentityProviderName());
        result.setAuthenticatorName(authenticator.getName());

        try {
            // Check if this is a federated authenticator
            if (authenticator instanceof FederatedApplicationAuthenticator) {
                result = executeFederatedAuthenticator(
                    (FederatedApplicationAuthenticator) authenticator, 
                    authenticatorConfig, context, request, response, identityProvider, result);
            } else {
                // For local authenticators, we'll simulate the flow
                result = simulateLocalAuthenticatorExecution(
                    authenticator, authenticatorConfig, context, identityProvider, result);
            }

            // Process claim mappings
            processClaimMappings(result, identityProvider, context);

            result.setStatus(FrameworkConstants.DFDPStatus.SUCCESS);

        } catch (Exception e) {
            log.error("Error in DFDP authenticator execution", e);
            result.setStatus(FrameworkConstants.DFDPStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    /**
     * Executes a federated authenticator for DFDP testing.
     * 
     * @param authenticator Federated application authenticator
     * @param authenticatorConfig Authenticator configuration
     * @param context Authentication context
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     * @param identityProvider Identity Provider configuration
     * @param result DFDP execution result to populate
     * @return Updated DFDPExecutionResult
     * @throws FrameworkException if execution fails
     */
    private DFDPExecutionResult executeFederatedAuthenticator(FederatedApplicationAuthenticator authenticator,
                                                             AuthenticatorConfig authenticatorConfig,
                                                             AuthenticationContext context,
                                                             HttpServletRequest request,
                                                             HttpServletResponse response,
                                                             IdentityProvider identityProvider,
                                                             DFDPExecutionResult result) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing federated authenticator in DFDP mode: " + authenticator.getName());
        }

        try {
            // Mark this as a DFDP flow to modify authenticator behavior
            context.setProperty(FrameworkConstants.DFDP_EXECUTION_MODE, true);
            
            // For DFDP mode, we'll simulate the authentication process
            // In a real implementation, this would initiate the actual authentication flow
            // but capture the results for testing purposes
            
            // Simulate user authentication result
            AuthenticatedUser authenticatedUser = createSimulatedUser(context, identityProvider);
            context.setSubject(authenticatedUser);

            // Simulate claims retrieval
            Map<String, String> retrievedClaims = simulateClaimsRetrieval(context, identityProvider, authenticator);
            
            // Process claims through DFDP claim processor
            DFDPClaimProcessor claimProcessor = new DFDPClaimProcessor();
            DFDPClaimAnalysis claimAnalysis = claimProcessor.processAndAnalyzeClaims(retrievedClaims, identityProvider);
            
            // Set claims and analysis in the result
            result.setRetrievedClaims(retrievedClaims);
            result.setMappedClaims(claimAnalysis.getMappedClaims());
            result.setClaimAnalysis(claimAnalysis);

            // Get authenticator properties for debugging
            Map<String, String> authenticatorProperties = getAuthenticatorProperties(authenticatorConfig, identityProvider);
            result.setAuthenticatorProperties(authenticatorProperties);

            if (log.isDebugEnabled()) {
                log.debug("DFDP federated authenticator execution completed. Retrieved " + 
                         retrievedClaims.size() + " claims");
            }

        } catch (Exception e) {
            throw new FrameworkException("Error executing federated authenticator in DFDP mode: " + e.getMessage(), e);
        }

        return result;
    }

    /**
     * Simulates local authenticator execution for DFDP testing.
     * 
     * @param authenticator Application authenticator
     * @param authenticatorConfig Authenticator configuration
     * @param context Authentication context
     * @param identityProvider Identity Provider configuration
     * @param result DFDP execution result to populate
     * @return Updated DFDPExecutionResult
     * @throws FrameworkException if simulation fails
     */
    private DFDPExecutionResult simulateLocalAuthenticatorExecution(ApplicationAuthenticator authenticator,
                                                                   AuthenticatorConfig authenticatorConfig,
                                                                   AuthenticationContext context,
                                                                   IdentityProvider identityProvider,
                                                                   DFDPExecutionResult result) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Simulating local authenticator execution in DFDP mode: " + authenticator.getName());
        }

        // For local authenticators, we simulate basic user and claims
        AuthenticatedUser authenticatedUser = createSimulatedUser(context, identityProvider);
        context.setSubject(authenticatedUser);

        // Simulate basic claims for local authentication
        Map<String, String> retrievedClaims = new HashMap<>();
        retrievedClaims.put("http://wso2.org/claims/username", "dfdp-test-user");
        retrievedClaims.put("http://wso2.org/claims/emailaddress", "dfdp.test@example.com");
        retrievedClaims.put("http://wso2.org/claims/fullname", "DFDP Test User");

        result.setRetrievedClaims(retrievedClaims);

        // Get authenticator properties
        Map<String, String> authenticatorProperties = new HashMap<>();
        if (authenticatorConfig.getParameterMap() != null) {
            authenticatorProperties.putAll(authenticatorConfig.getParameterMap());
        }
        result.setAuthenticatorProperties(authenticatorProperties);

        return result;
    }

    /**
     * Creates a simulated authenticated user for DFDP testing.
     * 
     * @param context Authentication context
     * @param identityProvider Identity Provider configuration
     * @return AuthenticatedUser instance
     */
    private AuthenticatedUser createSimulatedUser(AuthenticationContext context, 
                                                 IdentityProvider identityProvider) {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        authenticatedUser.setUserName("dfdp-test-user");
        authenticatedUser.setTenantDomain(context.getTenantDomain());
        authenticatedUser.setUserStoreDomain("PRIMARY");
        authenticatedUser.setAuthenticatedSubjectIdentifier("dfdp-test-user@" + identityProvider.getIdentityProviderName());

        return authenticatedUser;
    }

    /**
     * Simulates claims retrieval from external IdP.
     * 
     * @param context Authentication context
     * @param identityProvider Identity Provider configuration
     * @param authenticator Application authenticator
     * @return Map of retrieved claims
     * @throws FrameworkException if claims retrieval fails
     */
    private Map<String, String> simulateClaimsRetrieval(AuthenticationContext context,
                                                       IdentityProvider identityProvider,
                                                       ApplicationAuthenticator authenticator) throws FrameworkException {

        Map<String, String> claims = new HashMap<>();
        
        // Add some standard claims that would typically be retrieved from external IdP
        claims.put("http://wso2.org/claims/emailaddress", "test.user@external-idp.com");
        claims.put("http://wso2.org/claims/fullname", "External IdP Test User");
        claims.put("http://wso2.org/claims/givenname", "External");
        claims.put("http://wso2.org/claims/lastname", "User");
        claims.put("http://wso2.org/claims/organization", "External Organization");
        claims.put("http://wso2.org/claims/department", "Engineering");
        claims.put("http://wso2.org/claims/mobile", "+1234567890");

        // Add any test claims specified in the request
        String testClaims = (String) context.getProperty(FrameworkConstants.DFDP_TEST_CLAIMS);
        if (StringUtils.isNotBlank(testClaims)) {
            parseAndAddTestClaims(claims, testClaims);
        }

        // Add IdP-specific claims based on authenticator type
        addAuthenticatorSpecificClaims(claims, authenticator, identityProvider);

        if (log.isDebugEnabled()) {
            log.debug("Simulated claims retrieval completed. Total claims: " + claims.size());
        }

        return claims;
    }

    /**
     * Parses test claims from the request and adds them to the claims map.
     * 
     * @param claims Claims map to update
     * @param testClaims Test claims string in format "claim1=value1,claim2=value2"
     */
    private void parseAndAddTestClaims(Map<String, String> claims, String testClaims) {

        try {
            String[] claimPairs = testClaims.split(",");
            for (String claimPair : claimPairs) {
                String[] parts = claimPair.split("=", 2);
                if (parts.length == 2) {
                    String claimUri = parts[0].trim();
                    String claimValue = parts[1].trim();
                    claims.put(claimUri, claimValue);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Added test claim: " + claimUri + " = " + claimValue);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing test claims: " + testClaims, e);
        }
    }

    /**
     * Adds authenticator-specific claims based on the authenticator type.
     * 
     * @param claims Claims map to update
     * @param authenticator Application authenticator
     * @param identityProvider Identity Provider configuration
     */
    private void addAuthenticatorSpecificClaims(Map<String, String> claims,
                                               ApplicationAuthenticator authenticator,
                                               IdentityProvider identityProvider) {

        String authenticatorName = authenticator.getName();
        
        // Add claims specific to different authenticator types
        if (authenticatorName.contains("SAML")) {
            claims.put("http://wso2.org/claims/saml/issuer", identityProvider.getIdentityProviderName());
            claims.put("http://wso2.org/claims/saml/nameformat", "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        } else if (authenticatorName.contains("OIDC") || authenticatorName.contains("OpenIDConnect")) {
            claims.put("http://wso2.org/claims/oidc/sub", "external-user-12345");
            claims.put("http://wso2.org/claims/oidc/iss", identityProvider.getIdentityProviderName());
            claims.put("http://wso2.org/claims/oidc/aud", "dfdp-client");
        } else if (authenticatorName.contains("OAuth")) {
            claims.put("http://wso2.org/claims/oauth/scope", "openid profile email");
            claims.put("http://wso2.org/claims/oauth/client_id", "dfdp-oauth-client");
        }
    }

    /**
     * Gets authenticator properties for debugging purposes.
     * 
     * @param authenticatorConfig Authenticator configuration
     * @param identityProvider Identity Provider configuration
     * @return Map of authenticator properties
     */
    private Map<String, String> getAuthenticatorProperties(AuthenticatorConfig authenticatorConfig,
                                                          IdentityProvider identityProvider) {

        Map<String, String> properties = new HashMap<>();
        
        // Add authenticator config parameters
        if (authenticatorConfig.getParameterMap() != null) {
            properties.putAll(authenticatorConfig.getParameterMap());
        }

        // Add IdP-specific properties
        FederatedAuthenticatorConfig[] federatedConfigs = identityProvider.getFederatedAuthenticatorConfigs();
        if (federatedConfigs != null) {
            for (FederatedAuthenticatorConfig config : federatedConfigs) {
                if (config.getName().equals(authenticatorConfig.getName())) {
                    if (config.getProperties() != null) {
                        for (org.wso2.carbon.identity.application.common.model.Property property : config.getProperties()) {
                            properties.put(property.getName(), property.getValue());
                        }
                    }
                    break;
                }
            }
        }

        return properties;
    }

    /**
     * Processes claim mappings for the retrieved claims.
     * 
     * @param result DFDP execution result
     * @param identityProvider Identity Provider configuration
     * @param context Authentication context
     * @throws FrameworkException if claim mapping fails
     */
    private void processClaimMappings(DFDPExecutionResult result,
                                    IdentityProvider identityProvider,
                                    AuthenticationContext context) throws FrameworkException {

        Map<String, String> retrievedClaims = result.getRetrievedClaims();
        if (retrievedClaims == null || retrievedClaims.isEmpty()) {
            result.setMappedClaims(new HashMap<>());
            return;
        }

        try {
            Map<String, String> mappedClaims = applyClaimMappings(retrievedClaims, identityProvider);
            result.setMappedClaims(mappedClaims);

            if (log.isDebugEnabled()) {
                log.debug("Claim mapping completed. Input claims: " + retrievedClaims.size() + 
                         ", Mapped claims: " + mappedClaims.size());
            }

        } catch (Exception e) {
            log.error("Error processing claim mappings", e);
            throw new FrameworkException("Claim mapping failed: " + e.getMessage(), e);
        }
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
                    mappedClaims.put(localClaimUri, claimValue);
                    
                    if (log.isDebugEnabled()) {
                        log.debug("Mapped claim: " + remoteClaimUri + " -> " + localClaimUri + " = " + claimValue);
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
}
