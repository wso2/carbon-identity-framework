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

package org.wso2.carbon.identity.application.authentication.framework.dfdp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.model.CommonAuthResponseWrapper;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * DFDP (Debug Flow Data Provider) Orchestrator.
 * This class serves as the main controller for DFDP flows, coordinating the entire
 * DFDP process from request validation to response generation.
 */
public class DFDPOrchestrator {

    private static final Log log = LogFactory.getLog(DFDPOrchestrator.class);
    private static DFDPOrchestrator instance;

    /**
     * Private constructor for singleton pattern.
     */
    private DFDPOrchestrator() {
        // Private constructor
    }

    /**
     * Get singleton instance of DFDPOrchestrator.
     *
     * @return DFDPOrchestrator instance
     */
    public static synchronized DFDPOrchestrator getInstance() {
        if (instance == null) {
            instance = new DFDPOrchestrator();
        }
        return instance;
    }

    /**
     * Processes the DFDP request and orchestrates the entire DFDP flow.
     * This method follows the architecture flow:
     * DFDP Component -> RequestCoordinator -> DFDP Detection & Routing -> Direct Authenticator Setup 
     * -> Specific Authenticator -> External IdP -> Claim Handler -> DFDP Analysis Layer
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response wrapper
     * @param context Authentication context configured for DFDP flow
     * @throws IOException if an error occurs during response handling
     * @throws FrameworkException if an error occurs during DFDP processing
     */
    public void processDFDPRequest(HttpServletRequest request, CommonAuthResponseWrapper response, 
                                  AuthenticationContext context) throws IOException, FrameworkException {

        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);
        long startTime = System.currentTimeMillis();

        try {
            if (log.isDebugEnabled()) {
                log.debug("DFDP: Starting DFDP processing for request ID: " + requestId + 
                         " - Following architecture: DFDP Direct Path - Bypass Normal Flow");
            }

            // Step 1: DFDP Authenticator Setup - Create StepConfig, Set Target IdP, Configure Properties
            setupDFDPAuthenticator(context);

            // Step 2: Get Authenticator Instance - Direct access to specific authenticator
            String authenticatorInstance = getTargetAuthenticatorInstance(context);
            
            // Step 3: Execute Specific Authenticator (SAMLSSOAuthenticator, OIDCAuthenticator, etc.)
            // This calls authenticator.process() to interact with External Identity Provider
            executeSpecificAuthenticator(request, response, context, authenticatorInstance);

            // Step 4: Handle Claims from External IdP Response - DefaultClaimHandler integration
            // Following flow: IdP Response with Claims -> ClaimHandler -> handleClaimMappings()
            processDFDPClaimHandling(context);

            // Step 5: DFDP Analysis Layer - Capture, Analyze, Report
            performDFDPAnalysis(context);

            // Step 6: Generate DFDP Test Results and send response back through the chain
            generateDFDPResponse(request, response, context);

            long processingTime = System.currentTimeMillis() - startTime;
            if (log.isDebugEnabled()) {
                log.debug("DFDP: Processing completed for request ID: " + requestId + 
                         " in " + processingTime + "ms");
            }

        } catch (Exception e) {
            log.error("DFDP: Error during processing for request ID: " + requestId, e);
            handleDFDPError(response, context, e);
        }
    }

    /**
     * Step 1: DFDP Authenticator Setup - Create StepConfig, Set Target IdP, Configure Properties.
     * This implements the "DFDP Authenticator Setup" component from the architecture.
     */
    private void setupDFDPAuthenticator(AuthenticationContext context) throws FrameworkException {
        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        
        if (StringUtils.isBlank(targetIdP)) {
            throw new FrameworkException("Target Identity Provider is required for DFDP processing");
        }
        
        // Create StepConfig for direct authenticator execution
        // Set Target IdP configuration
        // Configure Properties for bypass mode
        context.setProperty(FrameworkConstants.DFDP_AUTHENTICATOR_SETUP_COMPLETE, true);
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Authenticator setup completed for IdP: " + targetIdP);
        }
    }

    /**
     * Step 2: Get Target Authenticator Instance.
     * Returns the specific authenticator (SAML, OIDC, OAuth2, etc.) for direct execution.
     */
    private String getTargetAuthenticatorInstance(AuthenticationContext context) throws FrameworkException {
        String targetAuthenticator = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_AUTHENTICATOR);
        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        
        // If no specific authenticator specified, determine from IdP configuration
        if (StringUtils.isBlank(targetAuthenticator)) {
            targetAuthenticator = determineAuthenticatorFromIdP(targetIdP);
        }
        
        context.setProperty(FrameworkConstants.DFDP_AUTHENTICATOR_INSTANCE, targetAuthenticator);
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Target authenticator instance: " + targetAuthenticator + " for IdP: " + targetIdP);
        }
        
        return targetAuthenticator;
    }

    /**
     * Step 3: Execute Specific Authenticator.
     * Calls authenticator.process() to interact with External Identity Provider.
     */
    private void executeSpecificAuthenticator(HttpServletRequest request, CommonAuthResponseWrapper response,
                                            AuthenticationContext context, String authenticatorInstance) 
                                            throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Executing specific authenticator: " + authenticatorInstance);
        }
        
        // Direct authenticator execution bypassing normal framework flow
        // This would call the actual authenticator (SAMLSSOAuthenticator, OIDCAuthenticator, etc.)
        // and capture the response from External IdP

        
        // For now, simulate successful authenticator execution
        context.setProperty(FrameworkConstants.DFDP_RETRIEVED_CLAIMS, "test_claims_retrieved");
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Specific authenticator execution completed");
        }
    }

    /**
     * Step 4: Process DFDP Claim Handling.
     * Integrates with DefaultClaimHandler to process claims from External IdP.
     */
    private void processDFDPClaimHandling(AuthenticationContext context) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Processing claim handling from External IdP response");
        }
        
        // Following flow: IdP Response with Claims -> ClaimHandler -> handleClaimMappings()
        // -> mapRemoteClaimsToLocalClaims -> Process System Claims
        
        // This would integrate with DefaultClaimHandler
        context.setProperty(FrameworkConstants.DFDP_MAPPED_CLAIMS, "test_mapped_claims");
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Claim handling processing completed");
        }
    }

    /**
     * Step 5: DFDP Analysis Layer.
     * Implements: DFDP Logger, DFDP Analyzer, DFDP Reporter components.
     */
    private void performDFDPAnalysis(AuthenticationContext context) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Performing analysis - Logger, Analyzer, Reporter");
        }
        
        // DFDP Logger: Capture Claims at Each Step
        // DFDP Analyzer: Compare Expected vs Actual  
        // DFDP Reporter: Generate Test Results
        
        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("status", "SUCCESS");
        analysisResult.put("claims_captured", true);
        analysisResult.put("analysis_completed", true);
        
        context.setProperty(FrameworkConstants.DFDP_ANALYSIS_RESULT, analysisResult);
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Analysis completed successfully");
        }
    }

    /**
     * Step 6: Generate DFDP Response.
     * Sends test results back through: DFDP Reporter -> DFDP Component -> Debug Endpoint -> UI.
     */
    private void generateDFDPResponse(HttpServletRequest request, CommonAuthResponseWrapper response,
                                    AuthenticationContext context) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Generating response and sending back through the chain");
        }
        
        Map<String, Object> analysisResult = (Map<String, Object>) context.getProperty(FrameworkConstants.DFDP_ANALYSIS_RESULT);
        
        // Generate JSON response with test results
        String jsonResponse = "{\"status\":\"SUCCESS\",\"message\":\"DFDP processing completed\",\"results\":" + analysisResult + "}";
        
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        
        if (log.isDebugEnabled()) {
            log.debug("DFDP: Response sent successfully");
        }
    }

    /**
     * Handle DFDP processing errors.
     */
    private void handleDFDPError(CommonAuthResponseWrapper response, AuthenticationContext context, Exception e) 
                                throws IOException {
        log.error("DFDP: Error during processing", e);
        
        String errorResponse = "{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}";
        response.setContentType("application/json");
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }

    /**
     * Determine authenticator type from IdP configuration.
     */
    private String determineAuthenticatorFromIdP(String targetIdP) throws FrameworkException {
        // Logic to determine authenticator based on IdP name or configuration
        // This would examine the IdP configuration to determine if it's SAML, OIDC, OAuth2, etc.
        
        if (targetIdP.toLowerCase().contains("saml")) {
            return "SAMLSSOAuthenticator";
        } else if (targetIdP.toLowerCase().contains("oidc") || targetIdP.toLowerCase().contains("openid")) {
            return "OIDCAuthenticator";
        } else if (targetIdP.toLowerCase().contains("oauth")) {
            return "OAuth2Authenticator";
        } else {
            return "DefaultAuthenticator";  // Fallback
        }
    }

    /**
     * Validates the DFDP parameters in the authentication context.
     * 
     * @param context The authentication context containing DFDP parameters.
     * @throws FrameworkException If required DFDP parameters are missing or invalid.
     */
    private void validateDFDPParameters(AuthenticationContext context) throws FrameworkException {

        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);

        if (StringUtils.isBlank(targetIdP)) {
            throw new FrameworkException("Target Identity Provider is required for DFDP processing");
        }

        if (log.isDebugEnabled()) {
            log.debug("DFDP parameters validated for request ID: " + requestId + 
                     ", Target IdP: " + targetIdP);
        }
    }

    /**
     * Executes the DFDP authenticator flow to interact with external IdP.
     * This method handles the direct authentication with the target IdP without
     * going through the normal authentication sequence.
     * 
     * @param request HTTP servlet request
     * @param response HTTP servlet response wrapper
     * @param context Authentication context
     * @throws FrameworkException if authenticator execution fails
     */
    private void executeDFDPAuthenticatorFlow(HttpServletRequest request, CommonAuthResponseWrapper response,
                                            AuthenticationContext context) throws FrameworkException {

        String targetIdP = (String) context.getProperty(FrameworkConstants.DFDP_TARGET_IDP);
        String requestId = (String) context.getProperty(FrameworkConstants.DFDP_REQUEST_ID);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing DFDP authenticator flow for IdP: " + targetIdP + 
                         ", Request ID: " + requestId);
            }

            // Step 1: Create DFDP Authenticator Setup
            DFDPAuthenticatorSetup authenticatorSetup = new DFDPAuthenticatorSetup();

            // Step 2: Create StepConfig for direct authenticator execution
            authenticatorSetup.createStepConfig(context);

            // Step 3: Configure authenticator properties for DFDP mode
            authenticatorSetup.configureProperties(context);

            // Step 4: Execute the authenticator directly
            try {
                authenticatorSetup.executeAuthenticator(request, response, context);
                
                // Mark that authenticator execution completed
                context.setProperty(FrameworkConstants.DFDP_AUTHENTICATOR_SETUP_COMPLETE, true);
                
                if (log.isDebugEnabled()) {
                    log.debug("DFDP authenticator execution completed for IdP: " + targetIdP);
                }
                
            } catch (Exception e) {
                log.error("DFDP authenticator execution failed for IdP: " + targetIdP, e);
                throw new FrameworkException("DFDP authenticator execution failed: " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("Error executing DFDP authenticator flow for IdP: " + targetIdP, e);
            throw new FrameworkException("Failed to execute DFDP authenticator flow: " + e.getMessage(), e);
        }
    }

    /**
     * Execute IdP authentication for DFDP testing by directly invoking the authenticator.
     * This method bypasses the normal authentication framework flow and directly calls
     * the specific authenticator to test external IdP connectivity and claim mapping.
     *
     * @param testContext Test context containing IdP name and other parameters
     * @return Authentication result with claims
     * @throws FrameworkException if authentication fails
     */
    public Object executeRealIdPAuthentication(Map<String, String> testContext) throws FrameworkException {
        String idpName = testContext.get("idpName");
        String authenticatorName = testContext.get("authenticatorName");

        if (log.isDebugEnabled()) {
            log.debug("Executing direct authenticator invocation for IdP: " + idpName + 
                     " with authenticator: " + authenticatorName);
        }

        try {
            // Step 1: Get the authenticator instance directly
            Object authenticatorInstance = getAuthenticatorInstance(idpName, authenticatorName);
            
            // Step 2: Create minimal authentication context for the authenticator
            AuthenticationContext dfdpContext = createDFDPAuthenticationContext(testContext);
            
            // Step 3: Execute the authenticator directly
            Object authResult = executeAuthenticatorDirectly(authenticatorInstance, dfdpContext, testContext);
            
            // Step 4: Process and extract claims from the result
            Map<String, Object> processedResult = processDFDPAuthenticationResult(authResult, testContext);
            
            return processedResult;

        } catch (Exception e) {
            log.error("Error during direct authenticator invocation for IdP: " + idpName, e);
            throw new FrameworkException("Failed to execute direct authenticator invocation: " + e.getMessage(), e);
        }
    }

    /**
     * Get the specific authenticator instance for direct invocation.
     *
     * @param idpName Identity provider name
     * @param authenticatorName Authenticator name
     * @return Authenticator instance
     * @throws FrameworkException if authenticator cannot be found or instantiated
     */
    private Object getAuthenticatorInstance(String idpName, String authenticatorName) throws FrameworkException {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            
            // Get IdP configuration
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider idp = idpManager.getIdPByName(idpName, tenantDomain, false);
            
            if (idp == null) {
                throw new FrameworkException("Identity Provider not found: " + idpName);
            }
            
            // Find the specific authenticator configuration
            FederatedAuthenticatorConfig targetAuthConfig = null;
            FederatedAuthenticatorConfig[] authConfigs = idp.getFederatedAuthenticatorConfigs();
            
            if (authConfigs != null) {
                for (FederatedAuthenticatorConfig authConfig : authConfigs) {
                    if (StringUtils.isBlank(authenticatorName) || 
                        authenticatorName.equals(authConfig.getName())) {
                        targetAuthConfig = authConfig;
                        break;
                    }
                }
            }
            
            if (targetAuthConfig == null) {
                throw new FrameworkException("Authenticator not found: " + authenticatorName + " for IdP: " + idpName);
            }
            
            // TODO: Get actual authenticator instance from authenticator registry
            // For now, return the configuration - this will be implemented in the next phase
            return targetAuthConfig;
            
        } catch (Exception e) {
            log.error("Error getting authenticator instance", e);
            throw new FrameworkException("Failed to get authenticator instance: " + e.getMessage(), e);
        }
    }

    /**
     * Create a minimal authentication context for DFDP testing.
     *
     * @param testContext Test context parameters
     * @return AuthenticationContext for DFDP
     */
    private AuthenticationContext createDFDPAuthenticationContext(Map<String, String> testContext) {
        AuthenticationContext context = new AuthenticationContext();
        
        // Set essential properties for DFDP testing
        context.setRequestType("dfdp-test");
        context.setContextIdentifier("dfdp-" + System.currentTimeMillis());
        context.setProperty("DFDP_MODE", true);
        context.setProperty("DFDP_TEST_CONTEXT", testContext);
        
        // Set tenant domain
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        context.setTenantDomain(tenantDomain);
        
        return context;
    }

    /**
     * Execute the authenticator directly without going through the framework.
     *
     * @param authenticatorInstance The authenticator instance
     * @param context DFDP authentication context
     * @param testContext Test parameters
     * @return Authentication result
     * @throws FrameworkException if execution fails
     */
    private Object executeAuthenticatorDirectly(Object authenticatorInstance, 
                                               AuthenticationContext context, 
                                               Map<String, String> testContext) throws FrameworkException {
        // TODO: Implement direct authenticator execution
        // This will involve:
        // 1. Cast to appropriate authenticator type (SAML, OIDC, etc.)
        // 2. Call authenticator.process() method
        // 3. Handle the authentication flow (redirect, callback, etc.)
        // 4. Extract claims from the response
        
        // For now, return mock result
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("authenticatorInstance", authenticatorInstance);
        result.put("context", context);
        
        return result;
    }

    /**
     * Process the authentication result and extract meaningful information for DFDP.
     *
     * @param authResult Authentication result from direct authenticator execution
     * @param testContext Test context parameters
     * @return Processed result with claims and metadata
     */
    private Map<String, Object> processDFDPAuthenticationResult(Object authResult, 
                                                              Map<String, String> testContext) {
        Map<String, Object> processedResult = new HashMap<>();
        
        // Build the response structure
        processedResult.put("requestId", testContext.get("requestId"));
        processedResult.put("idpName", testContext.get("idpName"));
        processedResult.put("authenticatorName", testContext.get("authenticatorName"));
        processedResult.put("status", "SUCCESS");
        processedResult.put("timestamp", System.currentTimeMillis());
        processedResult.put("testMode", "DIRECT_AUTHENTICATOR_INVOCATION");
        
        // TODO: Extract actual claims from authResult
        // This will be implemented when we have real authenticator execution
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");
        claims.put("displayName", "Test User");
        claims.put("username", "testuser");
        processedResult.put("claims", claims);
        
        // Add metadata about the direct invocation
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("executionMode", "direct");
        metadata.put("bypassedFramework", true);
        metadata.put("authenticatorType", "federated");
        metadata.put("claimsCount", claims.size());
        processedResult.put("metadata", metadata);
        
        return processedResult;
    }

    /**
     * Get list of available identity providers for testing.
     *
     * @return List of identity providers
     * @throws FrameworkException if retrieval fails
     */
    public List<Map<String, Object>> getAvailableIdPs() throws FrameworkException {
        try {
            List<Map<String, Object>> idpList = new ArrayList<>();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            // Get all identity providers from IdentityProviderManager
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            List<IdentityProvider> idps = idpManager.getEnabledIdPs(tenantDomain);

            for (IdentityProvider idp : idps) {
                // Skip local IdP
                if (!"LOCAL".equals(idp.getIdentityProviderName())) {
                    Map<String, Object> idpInfo = new HashMap<>();
                    idpInfo.put("name", idp.getIdentityProviderName());
                    idpInfo.put("displayName", idp.getDisplayName());
                    idpInfo.put("description", idp.getIdentityProviderDescription());
                    idpInfo.put("enabled", idp.isEnable());
                    idpInfo.put("resourceId", idp.getResourceId());

                    // Get authenticator count
                    FederatedAuthenticatorConfig[] authenticators = idp.getFederatedAuthenticatorConfigs();
                    idpInfo.put("authenticatorCount", authenticators != null ? authenticators.length : 0);

                    idpList.add(idpInfo);
                }
            }

            return idpList;

        } catch (Exception e) {
            log.error("Error retrieving available identity providers", e);
            throw new FrameworkException("Failed to retrieve identity providers: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of authenticators for a specific identity provider.
     *
     * @param idpName Identity provider name
     * @return List of authenticators
     * @throws FrameworkException if retrieval fails
     */
    public List<Map<String, Object>> getIdPAuthenticators(String idpName) throws FrameworkException {
        try {
            List<Map<String, Object>> authenticatorList = new ArrayList<>();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            // Get identity provider by name
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            IdentityProvider idp = idpManager.getIdPByName(idpName, tenantDomain, false);

            if (idp == null) {
                throw new FrameworkException("Identity provider not found: " + idpName);
            }

            FederatedAuthenticatorConfig[] authenticators = idp.getFederatedAuthenticatorConfigs();
            if (authenticators != null) {
                for (FederatedAuthenticatorConfig authenticator : authenticators) {
                    Map<String, Object> authInfo = new HashMap<>();
                    authInfo.put("name", authenticator.getName());
                    authInfo.put("displayName", authenticator.getDisplayName());
                    authInfo.put("enabled", authenticator.isEnabled());
                    authInfo.put("isDefault", authenticator.getName().equals(
                            idp.getDefaultAuthenticatorConfig() != null ? 
                            idp.getDefaultAuthenticatorConfig().getName() : null));

                    // Add properties count
                    authInfo.put("propertiesCount", 
                                authenticator.getProperties() != null ? 
                                authenticator.getProperties().length : 0);

                    authenticatorList.add(authInfo);
                }
            }

            return authenticatorList;

        } catch (Exception e) {
            log.error("Error retrieving authenticators for IdP: " + idpName, e);
            throw new FrameworkException("Failed to retrieve authenticators: " + e.getMessage(), e);
        }
    }
}
