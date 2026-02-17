/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.debug.framework.core.DebugRequestCoordinator;
import org.wso2.carbon.identity.debug.framework.core.store.DebugSessionCleanupService;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.listener.DebugSessionCleanupExecutionListener;

/**
 * OSGi service component for Debug Framework.
 * This component provides the framework infrastructure for debug operations.
 * Protocol-specific implementations (e.g., OAuth2ContextResolver,
 * OAuth2Executor) are provided
 * by protocol modules and automatically discovered via OSGi service lookups.
 */
@Component(name = "identity.debug.service.component", immediate = true)
public class DebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(DebugServiceComponent.class);

    private DebugSessionCleanupExecutionListener cleanupListener;
    private DebugSessionCleanupService cleanupService;

    @Activate
    protected void activate(ComponentContext context) throws DebugFrameworkException {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug Framework OSGi component activating");
            }

            // Register DebugRequestCoordinator as an OSGi service for backward compatibility.
            // This service is deprecated and delegates to DebugFlowOrchestrator internally.
            @SuppressWarnings("deprecation")
            DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
            context.getBundleContext().registerService(
                    DebugRequestCoordinator.class.getName(),
                    requestCoordinator,
                    null);

            // Register the cleanup listener for post-execution database cleanup.
            cleanupListener = new DebugSessionCleanupExecutionListener();
            DebugFrameworkServiceDataHolder.getInstance().addDebugExecutionListener(cleanupListener);

            // Start the periodic cleanup service for expired sessions.
            cleanupService = new DebugSessionCleanupService();
            cleanupService.activate();

            LOG.info("Debug Framework initialized. Waiting for protocol providers to register...");
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugRequestCoordinator registered as OSGi service (deprecated, use DebugFlowOrchestrator)");
                LOG.debug("DebugSessionCleanupExecutionListener registered for automatic database cleanup");
            }

        } catch (Exception e) {
            LOG.error("Debug Framework activation failed", e);
            throw new DebugFrameworkException("Debug Framework activation failed: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        // Unregister the cleanup listener.
        if (cleanupListener != null) {
            DebugFrameworkServiceDataHolder.getInstance().removeDebugExecutionListener(cleanupListener);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugSessionCleanupExecutionListener unregistered");
            }
        }

        // Shutdown the cleanup service.
        if (cleanupService != null) {
            cleanupService.deactivate();
        }

        LOG.info("Debug Framework OSGi component deactivated");
    }

    /**
     * Sets the DebugProtocolProvider.
     * Called by OSGi when a protocol module registers a DebugProtocolProvider.
     * Multiple providers can be registered.
     * 
     * 
     * Uses 0..* cardinality (MULTIPLE is optional by default) so the framework
     * can activate even if no providers are registered initially. Providers are
     * discovered dynamically at runtime via OSGi service lookups.
     *
     * @param provider the DebugProtocolProvider instance.
     */
    @Reference(name = "debug.protocol.provider", 
            service = DebugProtocolProvider.class, 
            cardinality = ReferenceCardinality.MULTIPLE, 
            policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetDebugProtocolProvider")

    protected void setDebugProtocolProvider(DebugProtocolProvider provider) {

        if (provider != null) {
            String protocolType = provider.getProtocolType();
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugProtocolProvider registered for protocol: " + protocolType);
            }
            DebugFrameworkServiceDataHolder.getInstance().addDebugProtocolProvider(provider);
            LOG.info("Successfully registered DebugProtocolProvider for protocol: " + protocolType);
        }
    }

    /**
     * Unsets the DebugProtocolProvider.
     * Called by OSGi when a protocol module deactivates or unregisters its provider.
     *
     * @param provider the DebugProtocolProvider instance.
     */
    protected void unsetDebugProtocolProvider(DebugProtocolProvider provider) {

        if (provider != null) {
            String protocolType = provider.getProtocolType();
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugProtocolProvider unregistered for protocol: " + protocolType);
            }
            DebugFrameworkServiceDataHolder.getInstance().removeDebugProtocolProvider(provider);
            LOG.info("Unregistered DebugProtocolProvider for protocol: " + protocolType);
        }
    }

    /**
     * Sets the ClaimMetadataManagementService.
     *
     * @param service the ClaimMetadataManagementService instance.
     */
    @Reference(name = "claimMetadataManagementService", 
            service = ClaimMetadataManagementService.class, 
            cardinality = ReferenceCardinality.OPTIONAL, 
            policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetClaimMetadataManagementService")

    protected void setClaimMetadataManagementService(ClaimMetadataManagementService service) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService set in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(service);
    }

    /**
     * Unsets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService instance.                  
     */
    protected void unsetClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService unset in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
