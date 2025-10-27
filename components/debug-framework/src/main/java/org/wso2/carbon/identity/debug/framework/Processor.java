package org.wso2.carbon.identity.debug.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Processes debug authentication results and prepares response for client.
 * Analyzes authentication flow results and prepares structured debug information.
 */
public class Processor {

    private static final Log LOG = LogFactory.getLog(Processor.class);

    /**
     * Processes the results of the debug authentication flow.
     * Analyzes authentication results, claims, errors, and flow events to prepare response.
     *
     * @param context AuthenticationContext containing debug flow data.
     * @return Processed result object containing structured debug information.
     */
    public Object process(AuthenticationContext context) {
        if (context == null) {
            LOG.error("Authentication context is null, cannot process results");
            return createErrorResult("INVALID_CONTEXT", "Authentication context is null");
        }

        try {
            // Create comprehensive debug result.
            Map<String, Object> debugResult = new HashMap<>();

            // Add basic debug session information.
            debugResult.put("debugSessionId", context.getProperty("DEBUG_SESSION_ID"));
            debugResult.put("contextIdentifier", context.getContextIdentifier());
            debugResult.put("processedAt", System.currentTimeMillis());

            // Add step status for UI consumption
            debugResult.put("step_connection_status", context.getProperty("step_connection_status"));
            debugResult.put("step_authentication_status", context.getProperty("step_authentication_status"));
            debugResult.put("step_claim_mapping_status", context.getProperty("step_claim_mapping_status"));

            // Process authentication results.
            Map<String, Object> authResult = processAuthenticationResult(context);
            debugResult.put("authenticationResult", authResult);

            // Process claims analysis.
            Map<String, Object> claimsAnalysis = processClaimsAnalysis(context);
            debugResult.put("claimsAnalysis", claimsAnalysis);

            // Process flow events and timeline.
            List<Map<String, Object>> flowEvents = processFlowEvents(context);
            debugResult.put("flowEvents", flowEvents);

            // Process errors and issues.
            List<Map<String, Object>> errors = processErrors(context);
            debugResult.put("errors", errors);

            // Process metadata and configuration.
            Map<String, Object> metadata = processMetadata(context);
            debugResult.put("metadata", metadata);

            // Calculate overall status.
            String overallStatus = calculateOverallStatus(context, authResult, errors);
            debugResult.put("status", overallStatus);

            return debugResult;

        } catch (Exception e) {
            LOG.error("Error processing debug authentication results", e);
            return createErrorResult("PROCESSING_ERROR", "Error processing debug results: " + e.getMessage());
        }
    }

    /**
     * Processes authentication result details.
     *
     * @param context AuthenticationContext.
     * @return Map containing authentication result information.
     */
    private Map<String, Object> processAuthenticationResult(AuthenticationContext context) {
        Map<String, Object> authResult = new HashMap<>();

        try {
            // Get authentication status from simulation results (handle both String and Boolean types)
            Boolean callbackResult = getBooleanProperty(context, "DEBUG_CALLBACK_RESULT");
            Boolean authResultBool = getBooleanProperty(context, "DEBUG_AUTH_RESULT");
            String authSuccess = (String) context.getProperty("DEBUG_AUTH_SUCCESS");
            
            // Check simulation results first, then fallback to callback results
            boolean isSuccessful = "true".equals(authSuccess) || 
                                 Boolean.TRUE.equals(callbackResult) || 
                                 Boolean.TRUE.equals(authResultBool);
            authResult.put("success", isSuccessful);

            // Get authenticated user details and user existence from simulation
            Boolean debugUserExists = getBooleanProperty(context, "DEBUG_USER_EXISTS");
            String debugAuthError = (String) context.getProperty("DEBUG_AUTH_ERROR");
      
            AuthenticatedUser authenticatedUser = context.getSubject();
            if (authenticatedUser != null && isSuccessful) {
                authResult.put("userExists", true);
                authResult.put("userId", authenticatedUser.getUserId());
                authResult.put("username", authenticatedUser.getUserName());
                authResult.put("userStoreDomain", authenticatedUser.getUserStoreDomain());
                authResult.put("tenantDomain", authenticatedUser.getTenantDomain());
                authResult.put("federatedIdPName", authenticatedUser.getFederatedIdPName());
                authResult.put("userDetails", "Authentication successful");
                
                // Add OAuth token information if available
                addTokenInformation(authResult, context);
                
                // Debug: Set userExists=true (authenticated user branch).
            } else {
                // Use simulation results for user existence and error details
                boolean finalUserExists = debugUserExists != null ? debugUserExists : false;
                authResult.put("userExists", finalUserExists);
                // Debug: Set userExists=... (fallback branch).
                
                if (debugAuthError != null) {
                    authResult.put("userDetails", debugAuthError);
                } else if (!isSuccessful) {
                    authResult.put("userDetails", "Authentication failed");
                } else {
                    authResult.put("userDetails", "No authenticated user found");
                }
            }

            // Calculate response times.
            Long authTimestamp = (Long) context.getProperty("DEBUG_AUTH_TIMESTAMP");
            Long completionTimestamp = (Long) context.getProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP");
            
            if (authTimestamp != null && completionTimestamp != null) {
                long responseTime = completionTimestamp - authTimestamp;
                authResult.put("responseTime", responseTime);
            }

            // Get authenticator used.
            String authenticatorName = (String) context.getProperty("DEBUG_AUTHENTICATOR_NAME");
            authResult.put("authenticatorUsed", authenticatorName);

            // Get IdP information.
            // Note: getExternalIdP returns ExternalIdPConfig, not IdentityProvider
            // Using debug properties instead
            String idpName = (String) context.getProperty("DEBUG_IDP_NAME");
            String idpResourceId = (String) context.getProperty("DEBUG_IDP_RESOURCE_ID");
            if (idpName != null) {
                authResult.put("idpName", idpName);
                authResult.put("idpResourceId", idpResourceId);
            }

        } catch (Exception e) {
            LOG.error("Error processing authentication result: " + e.getMessage(), e);
            authResult.put("error", "Error processing authentication result: " + e.getMessage());
        }

        return authResult;
    }

    /**
     * Processes claims analysis from the authentication flow.
     *
     * @param context AuthenticationContext.
     * @return Map containing claims analysis information.
     */
    private Map<String, Object> processClaimsAnalysis(AuthenticationContext context) {
        Map<String, Object> claimsAnalysis = new HashMap<>();

        try {
            // Get claims from authenticated user.
            Map<String, String> originalRemoteClaims = new HashMap<>();
            Map<String, String> mappedLocalClaims = new HashMap<>();
            List<String> mappingErrors = new ArrayList<>();

            AuthenticatedUser authenticatedUser = context.getSubject();
            if (authenticatedUser != null) {
                // Get user attributes (mapped local claims).
                Map<ClaimMapping, String> userAttributes = authenticatedUser.getUserAttributes();
                if (userAttributes != null) {
                    for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
                        ClaimMapping claimMapping = entry.getKey();
                        String value = entry.getValue();
                        
                        if (claimMapping.getLocalClaim() != null) {
                            mappedLocalClaims.put(claimMapping.getLocalClaim().getClaimUri(), value);
                        }
                        
                        if (claimMapping.getRemoteClaim() != null) {
                            originalRemoteClaims.put(claimMapping.getRemoteClaim().getClaimUri(), value);
                        }
                    }
                }
            }

            // Check for claim mapping errors from context.
            String authError = (String) context.getProperty("DEBUG_AUTH_ERROR");
            if (authError != null && authError.toLowerCase().contains("claim")) {
                mappingErrors.add(authError);
            }

            claimsAnalysis.put("originalRemoteClaims", originalRemoteClaims);
            claimsAnalysis.put("mappedLocalClaims", mappedLocalClaims);
            claimsAnalysis.put("mappingErrors", mappingErrors);

            // Add claims processing statistics.
            claimsAnalysis.put("totalRemoteClaims", originalRemoteClaims.size());
            claimsAnalysis.put("totalMappedClaims", mappedLocalClaims.size());
            claimsAnalysis.put("totalMappingErrors", mappingErrors.size());

        } catch (Exception e) {
            LOG.error("Error processing claims analysis: " + e.getMessage(), e);
            claimsAnalysis.put("error", "Error processing claims analysis: " + e.getMessage());
        }

        return claimsAnalysis;
    }

    /**
     * Processes flow events and creates timeline.
     *
     * @param context AuthenticationContext.
     * @return List of flow events.
     */
    private List<Map<String, Object>> processFlowEvents(AuthenticationContext context) {
        List<Map<String, Object>> flowEvents = new ArrayList<>();

        try {
            // Add authentication initiation event.
            String authInitiated = (String) context.getProperty("DEBUG_AUTH_INITIATED");
            if ("true".equals(authInitiated)) {
                Map<String, Object> initEvent = new HashMap<>();
                initEvent.put("timestamp", context.getProperty("DEBUG_AUTH_TIMESTAMP"));
                initEvent.put("eventType", "AUTHENTICATION_INITIATED");
                initEvent.put("step", "1");
                initEvent.put("success", true);
                initEvent.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                initEvent.put("data", "Authentication flow initiated with IdP");
                flowEvents.add(initEvent);
            }

            // Add callback processing event.
            String callbackProcessed = (String) context.getProperty("DEBUG_CALLBACK_PROCESSED");
            if ("true".equals(callbackProcessed)) {
                Map<String, Object> callbackEvent = new HashMap<>();
                callbackEvent.put("timestamp", context.getProperty("DEBUG_CALLBACK_TIMESTAMP"));
                callbackEvent.put("eventType", "CALLBACK_PROCESSED");
                callbackEvent.put("step", "2");
                callbackEvent.put("success", context.getProperty("DEBUG_CALLBACK_RESULT"));
                callbackEvent.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                callbackEvent.put("data", "Authentication callback processed");
                flowEvents.add(callbackEvent);
            }

            // Add authentication completion event.
            String authCompleted = (String) context.getProperty("DEBUG_AUTH_COMPLETED");
            if ("true".equals(authCompleted)) {
                Map<String, Object> completionEvent = new HashMap<>();
                completionEvent.put("timestamp", context.getProperty("DEBUG_AUTH_COMPLETION_TIMESTAMP"));
                completionEvent.put("eventType", "AUTHENTICATION_COMPLETED");
                completionEvent.put("step", "3");
                completionEvent.put("success", context.getProperty("DEBUG_AUTH_RESULT"));
                completionEvent.put("authenticator", context.getProperty("DEBUG_AUTHENTICATOR_NAME"));
                completionEvent.put("data", "Authentication flow completed");
                flowEvents.add(completionEvent);
            }

        } catch (Exception e) {
            LOG.error("Error processing flow events: " + e.getMessage(), e);
        }

        return flowEvents;
    }

    /**
     * Processes errors that occurred during the debug flow.
     *
     * @param context AuthenticationContext.
     * @return List of errors.
     */
    private List<Map<String, Object>> processErrors(AuthenticationContext context) {
        List<Map<String, Object>> errors = new ArrayList<>();

        try {
            // Check for authentication errors.
            String authError = (String) context.getProperty("DEBUG_AUTH_ERROR");
            if (authError != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", "AUTHENTICATION_ERROR");
                error.put("message", authError);
                error.put("step", "Authentication");
                error.put("timestamp", context.getProperty("DEBUG_AUTH_TIMESTAMP"));
                errors.add(error);
            }

            // Check for callback errors.
            String callbackError = (String) context.getProperty("DEBUG_CALLBACK_ERROR");
            if (callbackError != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", "CALLBACK_ERROR");
                error.put("message", callbackError);
                error.put("step", "Callback Processing");
                error.put("timestamp", context.getProperty("DEBUG_CALLBACK_TIMESTAMP"));
                errors.add(error);
            }

            // Check for coordination errors.
            String coordinationError = (String) context.getProperty("DEBUG_COORDINATION_ERROR");
            if (coordinationError != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("code", "COORDINATION_ERROR");
                error.put("message", coordinationError);
                error.put("step", "Request Coordination");
                error.put("timestamp", context.getProperty("DEBUG_REQUEST_TIMESTAMP"));
                errors.add(error);
            }

        } catch (Exception e) {
            LOG.error("Error processing errors: " + e.getMessage(), e);
        }

        return errors;
    }

    /**
     * Processes metadata and configuration information.
     *
     * @param context AuthenticationContext.
     * @return Map containing metadata.
     */
    private Map<String, Object> processMetadata(AuthenticationContext context) {
        Map<String, Object> metadata = new HashMap<>();

        try {
            // Add context metadata.
            metadata.put("tenantDomain", context.getTenantDomain());
            metadata.put("serviceProvider", context.getServiceProviderName());
            metadata.put("requestType", context.getProperty("DEBUG_REQUEST_TYPE"));
            
            // Add IdP metadata.
            // Note: Using debug properties instead of getExternalIdP
            String idpName = (String) context.getProperty("DEBUG_IDP_NAME");
            String idpResourceId = (String) context.getProperty("DEBUG_IDP_RESOURCE_ID");
            String idpDescription = (String) context.getProperty("DEBUG_IDP_DESCRIPTION");
            if (idpName != null) {
                Map<String, Object> idpMetadata = new HashMap<>();
                idpMetadata.put("name", idpName);
                idpMetadata.put("resourceId", idpResourceId);
                idpMetadata.put("description", idpDescription);
                metadata.put("identityProvider", idpMetadata);
            }

            // Add authenticator metadata.
            @SuppressWarnings("unchecked")
            Map<String, String> authProperties = (Map<String, String>) context.getProperty("DEBUG_AUTHENTICATOR_PROPERTIES");
            if (authProperties != null) {
                metadata.put("authenticatorProperties", authProperties);
            }

            // Add timing metadata.
            metadata.put("debugTimestamp", context.getProperty("DEBUG_TIMESTAMP"));
            metadata.put("requestTimestamp", context.getProperty("DEBUG_REQUEST_TIMESTAMP"));
            metadata.put("callbackTimestamp", context.getProperty("DEBUG_CALLBACK_TIMESTAMP"));

        } catch (Exception e) {
            LOG.error("Error processing metadata: " + e.getMessage(), e);
            metadata.put("error", "Error processing metadata: " + e.getMessage());
        }

        return metadata;
    }

    /**
     * Calculates the overall status of the debug flow.
     *
     * @param context AuthenticationContext.
     * @param authResult Authentication result map.
     * @param errors List of errors.
     * @return Overall status string.
     */
    private String calculateOverallStatus(AuthenticationContext context, 
                                        Map<String, Object> authResult, 
                                        List<Map<String, Object>> errors) {
        try {
            // If there are errors, status is FAILURE.
            if (errors != null && !errors.isEmpty()) {
                return "FAILURE";
            }

            // Check authentication result.
            if (authResult != null) {
                Boolean success = (Boolean) authResult.get("success");
                if (Boolean.TRUE.equals(success)) {
                    return "SUCCESS";
                }
            }

            // Check if processing is complete.
             String callbackProcessed = (String) context.getProperty("DEBUG_CALLBACK_PROCESSED");
            if ("true".equals(callbackProcessed)) {
                return "COMPLETED";
            }

            // Default to in progress.
            return "IN_PROGRESS";

        } catch (Exception e) {
            LOG.error("Error calculating overall status: " + e.getMessage(), e);
            return "ERROR";
        }
    }

    /**
     * Creates an error result object.
     *
     * @param errorCode Error code.
     * @param errorMessage Error message.
     * @return Error result map.
     */
    private Map<String, Object> createErrorResult(String errorCode, String errorMessage) {
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("status", "ERROR");
        errorResult.put("error", errorCode);
        errorResult.put("message", errorMessage);
        errorResult.put("timestamp", System.currentTimeMillis());
        return errorResult;
    }

    /**
     * Safely gets a Boolean property from AuthenticationContext.
     * Handles both String ("true"/"false") and Boolean types.
     *
     * @param context AuthenticationContext to get property from.
     * @param propertyName Name of the property.
     * @return Boolean value or null if property doesn't exist or cannot be parsed.
     */
    private Boolean getBooleanProperty(AuthenticationContext context, String propertyName) {
        Object property = context.getProperty(propertyName);
        if (property == null) {
            return null;
        }
        
        if (property instanceof Boolean) {
            return (Boolean) property;
        } else if (property instanceof String) {
            String stringValue = (String) property;
            if ("true".equalsIgnoreCase(stringValue)) {
                return Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return Boolean.FALSE;
            }
        }
        
        // Cannot parse to boolean
        return null;
    }

    /**
     * Adds OAuth token information to the authentication result if tokens are available.
     *
     * @param authResult Map to add token information to.
     * @param context AuthenticationContext containing token properties.
     */
    private void addTokenInformation(Map<String, Object> authResult, AuthenticationContext context) {
        Map<String, Object> tokenInfo = new HashMap<>();
        
        // Get token information from context
        String accessToken = (String) context.getProperty("DEBUG_ACCESS_TOKEN");
        String idToken = (String) context.getProperty("DEBUG_ID_TOKEN");
        String refreshToken = (String) context.getProperty("DEBUG_REFRESH_TOKEN");
        String tokenType = (String) context.getProperty("DEBUG_TOKEN_TYPE");

        // Add token information if available
        if (accessToken != null) {
            // For security, show only first and last few characters of actual tokens
            String maskedAccessToken = maskToken(accessToken);
            tokenInfo.put("accessToken", maskedAccessToken);
            tokenInfo.put("accessTokenLength", accessToken.length());
            
            // Add full token if in debug mode (be careful with this in production!)
            if (LOG.isDebugEnabled()) {
                tokenInfo.put("fullAccessToken", accessToken);
            }
        }
        
        if (idToken != null) {
            // Show full idToken directly
            tokenInfo.put("idToken", idToken);
            tokenInfo.put("idTokenLength", idToken.length());
        }
        
        if (refreshToken != null) {
            String maskedRefreshToken = maskToken(refreshToken);
            tokenInfo.put("refreshToken", maskedRefreshToken);
            tokenInfo.put("refreshTokenLength", refreshToken.length());
        }
        
        if (tokenType != null) {
            tokenInfo.put("tokenType", tokenType);
        }

        // Add token info to auth result if any tokens were found
        if (!tokenInfo.isEmpty()) {
            authResult.put("tokenInfo", tokenInfo);
            // Step status reporting: token info added to debug response
            context.setProperty("DEBUG_STEP_TOKEN_INFO_ADDED", true);
        }
    }

    /**
     * Masks a token for secure display, showing only first and last few characters.
     *
     * @param token The token to mask.
     * @return Masked token string.
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "[TOKEN]";
        }
        
        int prefixLength = Math.min(6, token.length() / 4);
        int suffixLength = Math.min(4, token.length() / 4);
        
        String prefix = token.substring(0, prefixLength);
        String suffix = token.substring(token.length() - suffixLength);
        
        return prefix + "..." + suffix + " (" + token.length() + " chars)";
    }
}
