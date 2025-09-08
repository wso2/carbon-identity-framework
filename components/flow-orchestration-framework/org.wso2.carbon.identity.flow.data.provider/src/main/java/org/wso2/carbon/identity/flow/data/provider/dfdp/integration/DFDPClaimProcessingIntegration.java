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

package org.wso2.carbon.identity.flow.data.provider.dfdp.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants;

import java.util.Map;

/**
 * DFDP Claim Processing Integration.
 * Part 6: Integration with Claim Processing - Provides static integration points for claim processing events.
 */
public class DFDPClaimProcessingIntegration {

    private static final Log log = LogFactory.getLog(DFDPClaimProcessingIntegration.class);
    private static boolean integrationEnabled = true;

    /**
     * Enables or disables DFDP integration.
     * 
     * @param enabled Integration status
     */
    public static void setIntegrationEnabled(boolean enabled) {
        integrationEnabled = enabled;
    }

    /**
     * Checks if DFDP integration is enabled.
     * 
     * @return true if enabled
     */
    public static boolean isIntegrationEnabled() {
        return integrationEnabled;
    }

    /**
     * Integration point for remote claims received from external IdP.
     * Call this method when remote claims are received from external IdP.
     * 
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param remoteClaims Remote claims received from IdP
     */
    public static void onRemoteClaimsReceived(AuthenticationContext context, StepConfig stepConfig,
                                            Map<String, String> remoteClaims) {
        if (!integrationEnabled || !isDFDPRequest(context)) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            DFDPClaimEventPublisher.publishRemoteClaimsReceived(requestId, context, stepConfig, remoteClaims);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP remote claims received integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point for local claim mapping.
     * Call this method when remote claims are mapped to local claims.
     * 
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param localClaims Mapped local claims
     * @param claimMappings Claim mappings used
     */
    public static void onLocalClaimMapping(AuthenticationContext context, StepConfig stepConfig,
                                         Map<String, String> localClaims, Map<String, String> claimMappings) {
        if (!integrationEnabled || !isDFDPRequest(context)) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            DFDPClaimEventPublisher.publishLocalClaimMapping(requestId, context, stepConfig, 
                                                            localClaims, claimMappings);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP local claim mapping integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point for final claim processing.
     * Call this method when final claims are processed and ready to be returned.
     * 
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param finalClaims Final processed claims
     * @param processingDetails Processing details
     */
    public static void onFinalClaimsProcessed(AuthenticationContext context, StepConfig stepConfig,
                                            Map<String, String> finalClaims, Map<String, Object> processingDetails) {
        if (!integrationEnabled || !isDFDPRequest(context)) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            DFDPClaimEventPublisher.publishFinalClaimsProcessed(requestId, context, stepConfig, 
                                                              finalClaims, processingDetails);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP final claims processed integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point for claim processing errors.
     * Call this method when an error occurs during claim processing.
     * 
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param error Error that occurred
     * @param stage Processing stage where error occurred
     */
    public static void onClaimProcessingError(AuthenticationContext context, StepConfig stepConfig,
                                            Exception error, String stage) {
        if (!integrationEnabled || !isDFDPRequest(context)) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            
            java.util.Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("errorMessage", error.getMessage());
            errorDetails.put("errorType", error.getClass().getSimpleName());
            errorDetails.put("stage", stage);
            
            DFDPClaimEventPublisher.publishClaimProcessingError(requestId, context, stepConfig, errorDetails);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP claim processing error integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the appropriate claim handler for DFDP requests.
     * 
     * @param context Authentication context
     * @param defaultHandler Default claim handler
     * @return Claim handler to use
     */
    public static ClaimHandler getClaimHandler(AuthenticationContext context, ClaimHandler defaultHandler) {
        if (!integrationEnabled || !isDFDPRequest(context)) {
            return defaultHandler;
        }

        // Return DFDP enhanced claim handler for DFDP requests
        return new DFDPEnhancedClaimHandler();
    }

    /**
     * Checks if the current request is a DFDP request.
     * 
     * @param context Authentication context
     * @return true if DFDP request
     */
    private static boolean isDFDPRequest(AuthenticationContext context) {
        if (context == null) {
            return false;
        }
        
        Object dfdpFlag = context.getProperty(FrameworkConstants.DFDP.IS_DFDP_REQUEST);
        return Boolean.TRUE.equals(dfdpFlag);
    }

    /**
     * Sets up DFDP context for claim processing.
     * 
     * @param context Authentication context
     * @param requestId DFDP request ID
     */
    public static void setupDFDPContext(AuthenticationContext context, String requestId) {
        if (context == null) {
            return;
        }

        context.setProperty(FrameworkConstants.DFDP.IS_DFDP_REQUEST, Boolean.TRUE);
        context.setProperty(FrameworkConstants.DFDP.REQUEST_ID, requestId);
        context.setProperty(FrameworkConstants.DFDP.INTEGRATION_ENABLED, Boolean.TRUE);
    }

    /**
     * Cleans up DFDP context after processing.
     * 
     * @param context Authentication context
     */
    public static void cleanupDFDPContext(AuthenticationContext context) {
        if (context == null) {
            return;
        }

        context.removeProperty(FrameworkConstants.DFDP.IS_DFDP_REQUEST);
        context.removeProperty(FrameworkConstants.DFDP.REQUEST_ID);
        context.removeProperty(FrameworkConstants.DFDP.INTEGRATION_ENABLED);
    }
}
