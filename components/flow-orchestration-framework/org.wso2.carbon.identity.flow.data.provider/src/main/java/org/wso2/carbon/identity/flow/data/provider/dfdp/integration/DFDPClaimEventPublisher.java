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
import org.wso2.carbon.identity.flow.data.provider.dfdp.util.FrameworkConstants;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPClaimEvent;
import org.wso2.carbon.identity.flow.data.provider.dfdp.event.DFDPEventManager;

import java.util.Map;
import java.util.HashMap;

/**
 * DFDP Claim Event Publisher.
 * Part 6: Integration with Claim Processing - Publishes claim processing events for DFDP analysis.
 */
public class DFDPClaimEventPublisher {

    private static final Log log = LogFactory.getLog(DFDPClaimEventPublisher.class);

    /**
     * Publishes claim processing event for DFDP analysis.
     * 
     * @param eventType Event type
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param claims Claims data
     * @param additionalData Additional event data
     */
    public static void publishClaimEvent(String eventType, String requestId, 
                                       AuthenticationContext context, StepConfig stepConfig,
                                       Map<String, String> claims, Map<String, Object> additionalData) {
        // Only publish events for DFDP requests
        if (!isDFDPRequest(context)) {
            return;
        }

        try {
            DFDPClaimEvent event = createClaimEvent(eventType, requestId, context, stepConfig, 
                                                  claims, additionalData);
            DFDPEventManager.getInstance().publishEvent(event);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error publishing DFDP claim event: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Publishes remote claims received event.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param remoteClaims Remote claims from IdP
     */
    public static void publishRemoteClaimsReceived(String requestId, AuthenticationContext context,
                                                 StepConfig stepConfig, Map<String, String> remoteClaims) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("claimCount", remoteClaims != null ? remoteClaims.size() : 0);
        additionalData.put("stage", "REMOTE_CLAIMS_RECEIVED");
        
        publishClaimEvent(FrameworkConstants.DFDP.CLAIM_EVENT_REMOTE_RECEIVED, requestId, 
                         context, stepConfig, remoteClaims, additionalData);
    }

    /**
     * Publishes local claim mapping event.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param localClaims Mapped local claims
     * @param claimMappings Claim mappings used
     */
    public static void publishLocalClaimMapping(String requestId, AuthenticationContext context,
                                              StepConfig stepConfig, Map<String, String> localClaims,
                                              Map<String, String> claimMappings) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("claimCount", localClaims != null ? localClaims.size() : 0);
        additionalData.put("mappingCount", claimMappings != null ? claimMappings.size() : 0);
        additionalData.put("stage", "LOCAL_CLAIM_MAPPING");
        additionalData.put("claimMappings", claimMappings);
        
        publishClaimEvent(FrameworkConstants.DFDP.CLAIM_EVENT_LOCAL_MAPPED, requestId, 
                         context, stepConfig, localClaims, additionalData);
    }

    /**
     * Publishes final claims processed event.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param finalClaims Final processed claims
     * @param processingDetails Processing details
     */
    public static void publishFinalClaimsProcessed(String requestId, AuthenticationContext context,
                                                 StepConfig stepConfig, Map<String, String> finalClaims,
                                                 Map<String, Object> processingDetails) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("claimCount", finalClaims != null ? finalClaims.size() : 0);
        additionalData.put("stage", "FINAL_CLAIMS_PROCESSED");
        additionalData.put("processingDetails", processingDetails);
        
        publishClaimEvent(FrameworkConstants.DFDP.CLAIM_EVENT_FINAL_PROCESSED, requestId, 
                         context, stepConfig, finalClaims, additionalData);
    }

    /**
     * Publishes claim processing error event.
     * 
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param errorDetails Error details
     */
    public static void publishClaimProcessingError(String requestId, AuthenticationContext context,
                                                  StepConfig stepConfig, Map<String, Object> errorDetails) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("stage", "CLAIM_PROCESSING_ERROR");
        additionalData.put("errorDetails", errorDetails);
        
        publishClaimEvent(FrameworkConstants.DFDP.CLAIM_EVENT_ERROR, requestId, 
                         context, stepConfig, null, additionalData);
    }

    /**
     * Creates a DFDP claim event.
     * 
     * @param eventType Event type
     * @param requestId Request ID
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @param claims Claims data
     * @param additionalData Additional data
     * @return DFDP claim event
     */
    private static DFDPClaimEvent createClaimEvent(String eventType, String requestId,
                                                 AuthenticationContext context, StepConfig stepConfig,
                                                 Map<String, String> claims, Map<String, Object> additionalData) {
        DFDPClaimEvent event = new DFDPClaimEvent(requestId, eventType, System.currentTimeMillis());
        
        // Set basic information
        if (context != null) {
            event.setTenantDomain(context.getTenantDomain());
            event.setServiceProvider(context.getServiceProviderName());
            if (context.getExternalIdP() != null) {
                event.setIdentityProviderName(context.getExternalIdP().getIdPName());
            }
        }
        
        if (stepConfig != null && stepConfig.getAuthenticatedIdP() != null) {
            event.setAuthenticatorName(stepConfig.getAuthenticatedIdP());
        }

        // Set claims data
        if (claims != null) {
            event.setClaims(new HashMap<>(claims));
        }

        // Set additional data
        if (additionalData != null) {
            for (Map.Entry<String, Object> entry : additionalData.entrySet()) {
                event.setAdditionalData(entry.getKey(), entry.getValue());
            }
        }

        return event;
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
     * Gets request ID from context.
     * 
     * @param context Authentication context
     * @return Request ID
     */
    public static String getRequestId(AuthenticationContext context) {
        if (context == null) {
            return null;
        }
        
        Object requestId = context.getProperty(FrameworkConstants.DFDP.REQUEST_ID);
        return requestId != null ? requestId.toString() : context.getContextIdentifier();
    }
}
