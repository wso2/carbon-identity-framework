/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
import org.wso2.carbon.identity.debug.framework.core.event.DebugSessionCleanupListener;
import org.wso2.carbon.identity.debug.framework.core.event.DebugSessionEventManager;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;

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

    private DebugSessionCleanupListener cleanupListener;

    @Activate
    protected void activate(ComponentContext context) throws DebugFrameworkException {

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug Framework OSGi component activating");
            }

            // Register DebugRequestCoordinator as an OSGi service for backward
            // compatibility.
            // This service is deprecated and delegates to DebugFlowOrchestrator internally.
            @SuppressWarnings("deprecation")
            DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
            context.getBundleContext().registerService(
                    DebugRequestCoordinator.class.getName(),
                    requestCoordinator,
                    null);

            // Register the cleanup listener to delete database records after flow
            // completion
            cleanupListener = new DebugSessionCleanupListener();
            DebugSessionEventManager.getInstance().registerListener(cleanupListener);

            LOG.info("Debug Framework initialized. Waiting for protocol providers to register...");
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugRequestCoordinator registered as OSGi service (deprecated, use DebugFlowOrchestrator)");
                LOG.debug("DebugSessionCleanupListener registered for automatic database cleanup on flow completion");
            }

        } catch (Exception e) {
            LOG.error("Debug Framework activation failed", e);
            throw new DebugFrameworkException("Debug Framework activation failed: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        // Unregister the cleanup listener
        if (cleanupListener != null) {
            DebugSessionEventManager.getInstance().unregisterListener(cleanupListener);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugSessionCleanupListener unregistered");
            }
        }

        LOG.info("Debug Framework OSGi component deactivated");
    }

    /**
     * Sets the DebugProtocolProvider.
     * Called by OSGi when a protocol module registers a DebugProtocolProvider.
     * Multiple providers can be registered (one per protocol: OIDC, Google, SAML,
     * etc.).
     * 
     * Uses 0..* cardinality (MULTIPLE is optional by default) so the framework
     * can activate even if no providers are registered initially. Providers are
     * discovered dynamically at runtime via OSGi service lookups.
     *
     * @param provider the DebugProtocolProvider instance
     */
    @Reference(name = "debug.protocol.provider", service = org.wso2.carbon.identity.debug.framework.core.extension.DebugProtocolProvider.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unsetDebugProtocolProvider")
    protected void setDebugProtocolProvider(
            org.wso2.carbon.identity.debug.framework.core.extension.DebugProtocolProvider provider) {

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
     * Called by OSGi when a protocol module deactivates or unregisters its
     * provider.
     *
     * @param provider the DebugProtocolProvider instance
     */
    protected void unsetDebugProtocolProvider(
            org.wso2.carbon.identity.debug.framework.core.extension.DebugProtocolProvider provider) {

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
     * @param service the ClaimMetadataManagementService instance
     */
    @Reference(name = "claimMetadataManagementService", service = ClaimMetadataManagementService.class, cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, unbind = "unsetClaimMetadataManagementService")
    protected void setClaimMetadataManagementService(ClaimMetadataManagementService service) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService set in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(service);
    }

    /**
     * Unsets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService
     *                                       instance
     */
    protected void unsetClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("ClaimMetadataManagementService unset in DebugServiceComponent");
        }
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
