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
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;

/**
 * DFDP Authenticator Integration.
 * Part 6: Integration with Claim Processing - Provides integration points for authenticator claim processing.
 */
public class DFDPAuthenticatorIntegration {

    private static final Log log = LogFactory.getLog(DFDPAuthenticatorIntegration.class);

    /**
     * Integration point before authenticator processing.
     * Call this method before authenticator starts processing.
     * 
     * @param context Authentication context
     * @param authenticator Authenticator instance
     * @param request HTTP request
     * @param response HTTP response
     */
    public static void beforeAuthenticatorProcess(AuthenticationContext context, 
                                                ApplicationAuthenticator authenticator,
                                                HttpServletRequest request, 
                                                HttpServletResponse response) {
        if (!DFDPClaimProcessingIntegration.isIntegrationEnabled()) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            
            // Publish authenticator start event
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("authenticatorName", authenticator.getName());
            additionalData.put("authenticatorType", authenticator.getClass().getSimpleName());
            additionalData.put("stage", "AUTHENTICATOR_START");
            
            DFDPClaimEventPublisher.publishClaimEvent("AUTHENTICATOR_START", requestId, 
                                                    context, getCurrentStepConfig(context), 
                                                    null, additionalData);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP authenticator start integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point after authenticator processing.
     * Call this method after authenticator completes processing.
     * 
     * @param context Authentication context
     * @param authenticator Authenticator instance
     * @param success Whether authentication was successful
     * @param claims Claims returned by authenticator
     */
    public static void afterAuthenticatorProcess(AuthenticationContext context, 
                                               ApplicationAuthenticator authenticator,
                                               boolean success, 
                                               Map<String, String> claims) {
        if (!DFDPClaimProcessingIntegration.isIntegrationEnabled()) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            
            // Publish authenticator completion event
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("authenticatorName", authenticator.getName());
            additionalData.put("authenticatorType", authenticator.getClass().getSimpleName());
            additionalData.put("authenticationSuccess", success);
            additionalData.put("claimCount", claims != null ? claims.size() : 0);
            additionalData.put("stage", "AUTHENTICATOR_COMPLETE");
            
            String eventType = success ? "AUTHENTICATOR_SUCCESS" : "AUTHENTICATOR_FAILURE";
            
            DFDPClaimEventPublisher.publishClaimEvent(eventType, requestId, 
                                                    context, getCurrentStepConfig(context), 
                                                    claims, additionalData);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP authenticator completion integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point for IdP response processing.
     * Call this method when processing response from external IdP.
     * 
     * @param context Authentication context
     * @param authenticator Authenticator instance
     * @param rawResponse Raw response from IdP
     * @param parsedClaims Parsed claims from response
     */
    public static void onIdPResponseReceived(AuthenticationContext context, 
                                           ApplicationAuthenticator authenticator,
                                           String rawResponse, 
                                           Map<String, String> parsedClaims) {
        if (!DFDPClaimProcessingIntegration.isIntegrationEnabled()) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            
            // Publish IdP response event
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("authenticatorName", authenticator.getName());
            additionalData.put("idpName", getIdPName(context));
            additionalData.put("rawResponseLength", rawResponse != null ? rawResponse.length() : 0);
            additionalData.put("parsedClaimCount", parsedClaims != null ? parsedClaims.size() : 0);
            additionalData.put("stage", "IDP_RESPONSE_RECEIVED");
            
            DFDPClaimEventPublisher.publishClaimEvent("IDP_RESPONSE_RECEIVED", requestId, 
                                                    context, getCurrentStepConfig(context), 
                                                    parsedClaims, additionalData);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP IdP response integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Integration point for claim transformation.
     * Call this method when claims are being transformed by authenticator.
     * 
     * @param context Authentication context
     * @param authenticator Authenticator instance
     * @param originalClaims Original claims before transformation
     * @param transformedClaims Claims after transformation
     * @param transformationType Type of transformation applied
     */
    public static void onClaimTransformation(AuthenticationContext context, 
                                           ApplicationAuthenticator authenticator,
                                           Map<String, String> originalClaims,
                                           Map<String, String> transformedClaims,
                                           String transformationType) {
        if (!DFDPClaimProcessingIntegration.isIntegrationEnabled()) {
            return;
        }

        try {
            String requestId = DFDPClaimEventPublisher.getRequestId(context);
            
            // Publish claim transformation event
            Map<String, Object> additionalData = new HashMap<>();
            additionalData.put("authenticatorName", authenticator.getName());
            additionalData.put("transformationType", transformationType);
            additionalData.put("originalClaimCount", originalClaims != null ? originalClaims.size() : 0);
            additionalData.put("transformedClaimCount", transformedClaims != null ? transformedClaims.size() : 0);
            additionalData.put("stage", "CLAIM_TRANSFORMATION");
            additionalData.put("originalClaims", originalClaims);
            
            DFDPClaimEventPublisher.publishClaimEvent("CLAIM_TRANSFORMATION", requestId, 
                                                    context, getCurrentStepConfig(context), 
                                                    transformedClaims, additionalData);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error in DFDP claim transformation integration: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets current step configuration from context.
     * 
     * @param context Authentication context
     * @return Current step configuration
     */
    private static StepConfig getCurrentStepConfig(AuthenticationContext context) {
        if (context == null || context.getSequenceConfig() == null) {
            return null;
        }
        
        try {
            int currentStep = context.getCurrentStep();
            Map<Integer, StepConfig> stepMap = context.getSequenceConfig().getStepMap();
            return stepMap.get(currentStep);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not get current step config: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Gets IdP name from context.
     * 
     * @param context Authentication context
     * @return IdP name
     */
    private static String getIdPName(AuthenticationContext context) {
        if (context == null) {
            return null;
        }
        
        try {
            if (context.getExternalIdP() != null) {
                return context.getExternalIdP().getIdPName();
            }
            
            StepConfig stepConfig = getCurrentStepConfig(context);
            if (stepConfig != null && stepConfig.getAuthenticatedIdP() != null) {
                return stepConfig.getAuthenticatedIdP();
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not get IdP name: " + e.getMessage());
            }
        }
        
        return "Unknown";
    }
}
