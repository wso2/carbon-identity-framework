/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.inbound;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.inbound.protocol.ApplicationInboundAuthConfigHandler;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility functions related to application inbound protocols.
 */
public class InboundFunctions {
    
    private static final Log LOG = LogFactory.getLog(InboundFunctions.class);
    
    private InboundFunctions() {
    
    }
    
    /**
     * Get the inbound authentication key for the given inbound type.
     *
     * @param application Service provider.
     * @param inboundType Inbound type.
     * @return Inbound authentication key.
     */
    public static Optional<String> getInboundAuthKey(ServiceProvider application,
                                                    String inboundType) {
        
        InboundAuthenticationConfig inboundAuthConfig = application.getInboundAuthenticationConfig();
        if (inboundAuthConfig != null) {
            InboundAuthenticationRequestConfig[] inbounds = inboundAuthConfig.getInboundAuthenticationRequestConfigs();
            if (inbounds != null) {
                return Arrays.stream(inbounds)
                        .filter(inbound -> inboundType.equals(inbound.getInboundAuthType()))
                        .findAny()
                        .map(InboundAuthenticationRequestConfig::getInboundAuthKey);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Rollback the inbounds.
     *
     * @param currentlyAddedInbounds Currently added inbounds list.
     * @throws IdentityApplicationManagementException If an error occurred while rolling back the inbounds.
     */
    public static void rollbackInbounds(List<InboundAuthenticationRequestConfig> currentlyAddedInbounds)
            throws IdentityApplicationManagementException {
        
        for (InboundAuthenticationRequestConfig inbound : currentlyAddedInbounds) {
            rollbackInbound(inbound);
        }
    }
    
    /**
     * Rollback the inbound authentication request config.
     *
     * @param inbound Inbound authentication request config.
     * @throws IdentityApplicationManagementException If an error occurred while rolling back the inbound.
     */
    public static void rollbackInbound(InboundAuthenticationRequestConfig inbound)
            throws IdentityApplicationManagementException {
        
        List<ApplicationInboundAuthConfigHandler> applicationInboundAuthConfigHandlerList =
                ApplicationManagementServiceComponentHolder.getInstance().getApplicationInboundAuthConfigHandler();
        for (ApplicationInboundAuthConfigHandler applicationInboundAuthConfigHandler :
                applicationInboundAuthConfigHandlerList) {
            if (applicationInboundAuthConfigHandler.canHandle(inbound.getInboundAuthType())) {
                applicationInboundAuthConfigHandler.handleConfigDeletion(inbound.getInboundAuthKey());
                break;
            }
        }
    }
    
    /**
     * Rollback the inbound authentication request config.
     *
     * @param resourceId     Application Resource ID.
     * @param updatedInbound Updated inbound authentication request config.
     * @param tenantDomain   Tenant domain.
     * @throws IdentityApplicationManagementException If an error occurred while rolling back the inbound.
     */
    public static void doRollback(String resourceId, InboundAuthenticationRequestConfig updatedInbound,
                            String tenantDomain) throws IdentityApplicationManagementException {
        
        ServiceProvider applicationByResourceId;
        applicationByResourceId = ApplicationManagementServiceImpl.getInstance().getApplicationByResourceId(
                resourceId, tenantDomain);
        Optional<String> optionalInboundKey = getInboundAuthKey(applicationByResourceId,
                updatedInbound.getInboundAuthType());
        if (!optionalInboundKey.isPresent()) {
            // No inbound configs found for the given inbound type. Nothing to rollback.
            return;
        }
        // Current inbound key. This will give us an idea whether updatedInbound was newly added or not.
        String previousInboundKey = optionalInboundKey.get();
        String attemptedInboundKeyForUpdate = updatedInbound.getInboundAuthKey();
        if (!StringUtils.equals(previousInboundKey, attemptedInboundKeyForUpdate)) {
             /*
              * This means the application was updated with a newly created inbound. So the updated inbound details
              * could have been created before the update. Attempt to rollback by deleting any inbound configs created.
              */
            if (LOG.isDebugEnabled()) {
                String inboundType = updatedInbound.getInboundAuthType();
                LOG.debug("Removing inbound data related to inbound type: " + inboundType + " of application: "
                        + applicationByResourceId + " as part of rollback.");
            }
            rollbackInbound(updatedInbound);
        }
    }
    
    /**
     * Update or insert the inbound authentication request config to the service provider.
     *
     * @param application Service provider.
     * @param newInbound  New inbound authentication request config.
     */
    public static void updateOrInsertInbound(ServiceProvider application,
                                             InboundAuthenticationRequestConfig newInbound) {
        
        InboundAuthenticationConfig inboundAuthConfig = application.getInboundAuthenticationConfig();
        if (inboundAuthConfig != null) {
            
            InboundAuthenticationRequestConfig[] inbounds = inboundAuthConfig.getInboundAuthenticationRequestConfigs();
            if (inbounds != null) {
                Map<String, InboundAuthenticationRequestConfig> inboundAuthConfigs =
                        Arrays.stream(inbounds).collect(
                                Collectors.toMap(InboundAuthenticationRequestConfig::getInboundAuthType,
                                        Function.identity()));
                
                inboundAuthConfigs.put(newInbound.getInboundAuthType(), newInbound);
                inboundAuthConfig.setInboundAuthenticationRequestConfigs(
                        inboundAuthConfigs.values().toArray(new InboundAuthenticationRequestConfig[0]));
            } else {
                addNewInboundToSp(application, newInbound);
            }
        } else {
            // Create new inbound auth config.
            addNewInboundToSp(application, newInbound);
        }
    }
    
    private static void addNewInboundToSp(ServiceProvider application, InboundAuthenticationRequestConfig newInbound) {
        
        InboundAuthenticationConfig inboundAuth = new InboundAuthenticationConfig();
        inboundAuth.setInboundAuthenticationRequestConfigs(new InboundAuthenticationRequestConfig[]{newInbound});
        application.setInboundAuthenticationConfig(inboundAuth);
    }
}
