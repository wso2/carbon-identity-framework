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

package org.wso2.carbon.identity.dfdp.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.dfdp.core.DFDPEventListener;
import org.wso2.carbon.identity.dfdp.core.DebugRequest;
import org.wso2.carbon.identity.dfdp.core.DebugResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core service for DFDP (Debug Flow Data Provider) functionality.
 * Handles debug authentication flows using event listeners for claim processing.
 */
public class DFDPDebugService {

    private static final Log LOG = LogFactory.getLog(DFDPDebugService.class);
    
    // Session storage for debug sessions (in-memory for now)
    private static final Map<String, DebugResponse> debugSessions = new ConcurrentHashMap<>();
    
    // Event listener for capturing authentication flow events
    private final DFDPEventListener eventListener;

    public DFDPDebugService() {
        this.eventListener = new DFDPEventListener();
    }

    /**
     * Process a debug authentication request using event listeners for claim capture.
     * 
     * @param sessionId Debug session ID
     * @param debugRequest Debug request parameters
     * @return DebugResponse with authentication flow analysis
     */
    public DebugResponse processDebugAuthentication(String sessionId, DebugRequest debugRequest) {
        DebugResponse debugResponse = new DebugResponse();
        debugResponse.setSessionId(sessionId);
        debugResponse.setTargetIdp(debugRequest.getTargetIdp());
        debugResponse.setAuthenticatorUsed(debugRequest.getTargetAuthenticator());
        
        List<DebugResponse.FlowEvent> flowEvents = new ArrayList<>();
        List<DebugResponse.DebugError> errors = new ArrayList<>();

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing DFDP debug authentication for session: " + sessionId);
            }

            // Initialize event listener for this session
            eventListener.initializeSession(sessionId);

            // Step 1: Validate target Identity Provider
            DebugResponse.FlowEvent validationEvent = new DebugResponse.FlowEvent();
            validationEvent.setTimestamp(System.currentTimeMillis());
            validationEvent.setEventType("IDP_VALIDATION");
            validationEvent.setStep("Identity Provider Validation");

            IdentityProvider targetIdp = validateIdentityProvider(debugRequest.getTargetIdp());
            if (targetIdp == null) {
                validationEvent.setSuccess(false);
                validationEvent.setData(Map.of("error", "Identity Provider not found: " + debugRequest.getTargetIdp()));
                flowEvents.add(validationEvent);
                
                DebugResponse.DebugError error = new DebugResponse.DebugError();
                error.setCode("IDP_NOT_FOUND");
                error.setMessage("Target Identity Provider not found: " + debugRequest.getTargetIdp());
                error.setStep("Identity Provider Validation");
                errors.add(error);
                
                debugResponse.setStatus("FAILURE");
                debugResponse.setFlowEvents(flowEvents);
                debugResponse.setErrors(errors);
                return debugResponse;
            }

            validationEvent.setSuccess(true);
            validationEvent.setData(Map.of("idpName", targetIdp.getIdentityProviderName(), 
                                          "enabled", targetIdp.isEnable()));
            flowEvents.add(validationEvent);

            // Step 2: Validate and test authenticator
            DebugResponse.FlowEvent authEvent = new DebugResponse.FlowEvent();
            authEvent.setTimestamp(System.currentTimeMillis());
            authEvent.setEventType("AUTHENTICATOR_VALIDATION");
            authEvent.setStep("Authenticator Configuration Test");

            FederatedAuthenticatorConfig authenticator = validateAuthenticator(targetIdp, 
                debugRequest.getTargetAuthenticator());
            if (authenticator == null) {
                authEvent.setSuccess(false);
                authEvent.setData(Map.of("error", "Authenticator not found: " + debugRequest.getTargetAuthenticator()));
                flowEvents.add(authEvent);
                
                DebugResponse.DebugError error = new DebugResponse.DebugError();
                error.setCode("AUTHENTICATOR_NOT_FOUND");
                error.setMessage("Target authenticator not found: " + debugRequest.getTargetAuthenticator());
                error.setStep("Authenticator Configuration Test");
                errors.add(error);
                
                debugResponse.setStatus("FAILURE");
                debugResponse.setFlowEvents(flowEvents);
                debugResponse.setErrors(errors);
                return debugResponse;
            }

            authEvent.setSuccess(true);
            authEvent.setAuthenticator(authenticator.getName());
            authEvent.setData(Map.of("authenticatorName", authenticator.getName(), 
                                   "enabled", authenticator.isEnabled()));
            flowEvents.add(authEvent);

            // Step 3: Create debug authentication context and simulate flow
            DebugResponse.FlowEvent contextEvent = new DebugResponse.FlowEvent();
            contextEvent.setTimestamp(System.currentTimeMillis());
            contextEvent.setEventType("DEBUG_AUTHENTICATION");
            contextEvent.setStep("Debug Authentication Flow");

            // Create debug authentication context for this session
            createDebugAuthenticationContext(debugRequest, sessionId);
            
            // Step 4: Process claims using event listeners (instead of direct logging)
            DebugResponse.ClaimsAnalysis claimsAnalysis = processClaimsWithEventListeners(
                debugRequest, targetIdp, authenticator, sessionId);

            // Step 5: Create authentication result
            DebugResponse.AuthenticationResult authResult = new DebugResponse.AuthenticationResult();
            authResult.setSuccess(true);
            authResult.setUserExists(debugRequest.getTestUser() != null);
            authResult.setResponseTime(System.currentTimeMillis() - contextEvent.getTimestamp());

            if (debugRequest.getTestUser() != null) {
                Map<String, String> userDetails = new HashMap<>();
                userDetails.put("username", debugRequest.getTestUser());
                userDetails.put("source", "debug_test");
                authResult.setUserDetails(userDetails);
            }

            contextEvent.setSuccess(true);
            contextEvent.setData(Map.of("authenticationResult", "success", 
                                      "claimsProcessed", claimsAnalysis.getOriginalRemoteClaims().size()));
            flowEvents.add(contextEvent);

            // Compile final response
            debugResponse.setStatus("SUCCESS");
            debugResponse.setAuthenticationResult(authResult);
            debugResponse.setClaimsAnalysis(claimsAnalysis);
            debugResponse.setFlowEvents(flowEvents);
            debugResponse.setErrors(errors);

            // Additional metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("processingTime", System.currentTimeMillis() - flowEvents.get(0).getTimestamp());
            metadata.put("eventListenerEnabled", debugRequest.getEnableEventCapture());
            metadata.put("debugMode", debugRequest.getDebugMode());
            debugResponse.setMetadata(metadata);

            // Store session for later retrieval
            debugSessions.put(sessionId, debugResponse);

            if (LOG.isDebugEnabled()) {
                LOG.debug("DFDP debug authentication completed successfully for session: " + sessionId);
            }

        } catch (Exception e) {
            LOG.error("Error during DFDP debug authentication processing", e);
            
            DebugResponse.DebugError error = new DebugResponse.DebugError();
            error.setCode("PROCESSING_ERROR");
            error.setMessage("Error during debug authentication: " + e.getMessage());
            error.setStep("Debug Authentication Processing");
            errors.add(error);
            
            debugResponse.setStatus("FAILURE");
            debugResponse.setFlowEvents(flowEvents);
            debugResponse.setErrors(errors);
        }

        return debugResponse;
    }

    /**
     * Test authenticator configuration and claim mapping.
     * 
     * @param sessionId Debug session ID
     * @param idpId Identity Provider ID
     * @param authenticatorName Authenticator name
     * @param testClaims Test claims JSON
     * @return DebugResponse with authenticator test results
     */
    public DebugResponse testAuthenticatorConfiguration(String sessionId, String idpId, 
                                                       String authenticatorName, String testClaims) {
        DebugResponse debugResponse = new DebugResponse();
        debugResponse.setSessionId(sessionId);
        debugResponse.setTargetIdp(idpId);
        debugResponse.setAuthenticatorUsed(authenticatorName);
        
        // Placeholder implementation - will be enhanced based on requirements
        debugResponse.setStatus("SUCCESS");
        
        DebugResponse.AuthenticationResult result = new DebugResponse.AuthenticationResult();
        result.setSuccess(true);
        result.setResponseTime(100L);
        debugResponse.setAuthenticationResult(result);
        
        // Store session
        debugSessions.put(sessionId, debugResponse);
        
        return debugResponse;
    }

    /**
     * Get debug session data.
     * 
     * @param sessionId Debug session ID
     * @return DebugResponse with session data or null if not found
     */
    public DebugResponse getDebugSessionData(String sessionId) {
        return debugSessions.get(sessionId);
    }

    /**
     * Validate that the specified Identity Provider exists and is accessible.
     * 
     * @param idpName Identity Provider name
     * @return IdentityProvider if found, null otherwise
     */
    private IdentityProvider validateIdentityProvider(String idpName) {
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
            return idpManager.getIdPByName(idpName, tenantDomain);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error validating Identity Provider: " + idpName, e);
            }
            return null;
        }
    }

    /**
     * Validate that the specified authenticator exists for the Identity Provider.
     * 
     * @param idp Identity Provider
     * @param authenticatorName Authenticator name
     * @return FederatedAuthenticatorConfig if found, null otherwise
     */
    private FederatedAuthenticatorConfig validateAuthenticator(IdentityProvider idp, String authenticatorName) {
        if (idp.getFederatedAuthenticatorConfigs() != null) {
            for (FederatedAuthenticatorConfig config : idp.getFederatedAuthenticatorConfigs()) {
                if (authenticatorName.equals(config.getName())) {
                    return config;
                }
            }
        }
        return null;
    }

    /**
     * Create a debug authentication context for testing.
     * 
     * @param debugRequest Debug request
     * @param sessionId Session ID
     * @return AuthenticationContext for debug flow
     */
    private AuthenticationContext createDebugAuthenticationContext(DebugRequest debugRequest, String sessionId) {
        AuthenticationContext context = new AuthenticationContext();
        context.setContextIdentifier(sessionId);
        context.setRequestType(FrameworkConstants.RequestType.CLAIM_TYPE_OIDC);
        
        // Set debug-specific properties using string constants
        context.setProperty("dfdp.enabled", "true");
        context.setProperty("dfdp.target.idp", debugRequest.getTargetIdp());
        context.setProperty("dfdp.target.authenticator", debugRequest.getTargetAuthenticator());
        
        return context;
    }

    /**
     * Process claims using event listeners instead of direct logging.
     * This method captures claim processing events for analysis.
     * 
     * @param debugRequest Debug request
     * @param idp Identity Provider
     * @param authenticator Authenticator config
     * @param sessionId Session ID
     * @return ClaimsAnalysis with event listener captured data
     */
    private DebugResponse.ClaimsAnalysis processClaimsWithEventListeners(DebugRequest debugRequest, 
                                                                        IdentityProvider idp,
                                                                        FederatedAuthenticatorConfig authenticator,
                                                                        String sessionId) {
        DebugResponse.ClaimsAnalysis claimsAnalysis = new DebugResponse.ClaimsAnalysis();
        
        // Simulate claim processing with event listener capture
        Map<String, String> originalClaims = new HashMap<>();
        Map<String, String> mappedClaims = new HashMap<>();
        Map<String, String> filteredClaims = new HashMap<>();
        List<String> mappingErrors = new ArrayList<>();

        // Use test claims if provided, otherwise create sample claims
        if (debugRequest.getTestClaims() != null && !debugRequest.getTestClaims().isEmpty()) {
            originalClaims.putAll(debugRequest.getTestClaims());
        } else {
            // Create sample claims for testing
            originalClaims.put("sub", "debug-user-123");
            originalClaims.put("email", "debug@example.com");
            originalClaims.put("name", "Debug User");
            originalClaims.put("given_name", "Debug");
            originalClaims.put("family_name", "User");
        }

        // Simulate claim mapping process (captured via event listeners)
        eventListener.captureClaimMappingEvent(sessionId, "ORIGINAL_CLAIMS", originalClaims);
        
        // Map remote claims to local claims
        for (Map.Entry<String, String> claim : originalClaims.entrySet()) {
            String localClaimUri = mapRemoteClaimToLocal(claim.getKey());
            if (localClaimUri != null) {
                mappedClaims.put(localClaimUri, claim.getValue());
                filteredClaims.put(localClaimUri, claim.getValue());
            } else {
                mappingErrors.add("No mapping found for remote claim: " + claim.getKey());
            }
        }

        eventListener.captureClaimMappingEvent(sessionId, "MAPPED_CLAIMS", mappedClaims);
        eventListener.captureClaimMappingEvent(sessionId, "FILTERED_CLAIMS", filteredClaims);

        claimsAnalysis.setOriginalRemoteClaims(originalClaims);
        claimsAnalysis.setMappedLocalClaims(mappedClaims);
        claimsAnalysis.setFilteredClaims(filteredClaims);
        claimsAnalysis.setMappingErrors(mappingErrors);

        return claimsAnalysis;
    }

    /**
     * Map remote claim to local claim URI.
     * This is a simplified mapping for debug purposes.
     * 
     * @param remoteClaim Remote claim name
     * @return Local claim URI or null if no mapping exists
     */
    private String mapRemoteClaimToLocal(String remoteClaim) {
        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("sub", "http://wso2.org/claims/userid");
        claimMappings.put("email", "http://wso2.org/claims/emailaddress");
        claimMappings.put("name", "http://wso2.org/claims/fullname");
        claimMappings.put("given_name", "http://wso2.org/claims/givenname");
        claimMappings.put("family_name", "http://wso2.org/claims/lastname");
        
        return claimMappings.get(remoteClaim);
    }
}
