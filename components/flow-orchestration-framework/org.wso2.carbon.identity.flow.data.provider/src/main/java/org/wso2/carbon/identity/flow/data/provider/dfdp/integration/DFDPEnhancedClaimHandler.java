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
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * DFDP Enhanced Claim Handler.
 * Part 6: Integration with Claim Processing - Enhanced claim handler that publishes DFDP events.
 */
public class DFDPEnhancedClaimHandler extends DefaultClaimHandler {

    private static final Log log = LogFactory.getLog(DFDPEnhancedClaimHandler.class);

    @Override
    public Map<String, String> handleClaimMappings(StepConfig stepConfig, AuthenticationContext context,
                                                   Map<String, String> remoteClaims, boolean isFederatedClaims)
            throws FrameworkException {

        String requestId = DFDPClaimEventPublisher.getRequestId(context);
        
        try {
            // Publish event for remote claims received (if federated)
            if (isFederatedClaims && remoteClaims != null) {
                DFDPClaimEventPublisher.publishRemoteClaimsReceived(requestId, context, stepConfig, remoteClaims);
            }

            // Call parent implementation to get processed claims
            Map<String, String> processedClaims = super.handleClaimMappings(stepConfig, context, remoteClaims, isFederatedClaims);

            // Publish event for final processed claims
            if (processedClaims != null) {
                Map<String, Object> processingDetails = new HashMap<>();
                processingDetails.put("isFederatedClaims", isFederatedClaims);
                processingDetails.put("originalClaimCount", remoteClaims != null ? remoteClaims.size() : 0);
                processingDetails.put("processedClaimCount", processedClaims.size());
                
                DFDPClaimEventPublisher.publishFinalClaimsProcessed(requestId, context, stepConfig, 
                                                                  processedClaims, processingDetails);
            }

            return processedClaims;

        } catch (FrameworkException e) {
            // Publish error event
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("errorMessage", e.getMessage());
            errorDetails.put("errorCode", e.getErrorCode());
            errorDetails.put("isFederatedClaims", isFederatedClaims);
            
            DFDPClaimEventPublisher.publishClaimProcessingError(requestId, context, stepConfig, errorDetails);
            throw e;
        }
    }

    @Override
    protected Map<String, String> handleFederatedClaims(Map<String, String> remoteClaims, String spStandardDialect,
                                                        StepConfig stepConfig, AuthenticationContext context)
            throws FrameworkException {

        String requestId = DFDPClaimEventPublisher.getRequestId(context);

        try {
            // Call parent implementation
            Map<String, String> localClaims = super.handleFederatedClaims(remoteClaims, spStandardDialect, stepConfig, context);

            // Publish local claim mapping event
            if (localClaims != null) {
                // Get claim mappings from context if available
                Map<String, String> claimMappings = getClaimMappingsFromContext(context, stepConfig);
                DFDPClaimEventPublisher.publishLocalClaimMapping(requestId, context, stepConfig, 
                                                                localClaims, claimMappings);
            }

            return localClaims;

        } catch (FrameworkException e) {
            // Publish error event for federated claim handling
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("errorMessage", e.getMessage());
            errorDetails.put("errorCode", e.getErrorCode());
            errorDetails.put("stage", "FEDERATED_CLAIM_HANDLING");
            errorDetails.put("spStandardDialect", spStandardDialect);
            
            DFDPClaimEventPublisher.publishClaimProcessingError(requestId, context, stepConfig, errorDetails);
            throw e;
        }
    }

    /**
     * Extracts claim mappings from context for event publishing.
     * 
     * @param context Authentication context
     * @param stepConfig Step configuration
     * @return Claim mappings
     */
    private Map<String, String> getClaimMappingsFromContext(AuthenticationContext context, StepConfig stepConfig) {
        Map<String, String> claimMappings = new HashMap<>();
        
        try {
            // Try to extract claim mappings from external IdP configuration
            if (context.getExternalIdP() != null && context.getExternalIdP().getClaimMappings() != null) {
                for (org.wso2.carbon.identity.application.common.model.ClaimMapping claimMapping : 
                     context.getExternalIdP().getClaimMappings()) {
                    if (claimMapping.getLocalClaim() != null && claimMapping.getRemoteClaim() != null) {
                        claimMappings.put(claimMapping.getLocalClaim().getClaimUri(), 
                                        claimMapping.getRemoteClaim().getClaimUri());
                    }
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not extract claim mappings from context: " + e.getMessage());
            }
        }
        
        return claimMappings;
    }
}
