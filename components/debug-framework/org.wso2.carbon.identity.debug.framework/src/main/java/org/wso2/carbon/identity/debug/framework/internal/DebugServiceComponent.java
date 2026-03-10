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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.DebugAuthenticationInterceptor;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.debug.framework.core.DebugRequestCoordinator;
import org.wso2.carbon.identity.debug.framework.core.store.DebugSessionCleanupService;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;
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

    private ServiceRegistration<DebugExecutionListener> cleanupListenerServiceRegistration;
    private DebugSessionCleanupService cleanupService;

    @Activate
    protected void activate(ComponentContext context) {

        try {
            LOG.debug("Debug Framework OSGi component activating");

            // Register DebugRequestCoordinator as an OSGi interceptor service.
            DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
            context.getBundleContext().registerService(
                    new String[] {
                            DebugAuthenticationInterceptor.class.getName(),
                            DebugRequestCoordinator.class.getName()
                    },
                    requestCoordinator,
                    null);

            // Register the cleanup listener as an OSGi service.
            cleanupListenerServiceRegistration = context.getBundleContext().registerService(
                    DebugExecutionListener.class, new DebugSessionCleanupExecutionListener(), null);

            // Start the periodic cleanup service for expired sessions.
            cleanupService = new DebugSessionCleanupService();
            cleanupService.activate();

            LOG.info("Debug Framework initialized. Waiting for protocol providers to register...");
            LOG.debug("DebugRequestCoordinator registered as DebugAuthenticationInterceptor service");
        } catch (Throwable e) {
            LOG.error("Error while activating debug framework component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            // Unregister cleanup listener service.
            if (cleanupListenerServiceRegistration != null) {
                cleanupListenerServiceRegistration.unregister();
                cleanupListenerServiceRegistration = null;
                LOG.debug("DebugSessionCleanupExecutionListener service unregistered");
            }

            // Shutdown the cleanup service.
            if (cleanupService != null) {
                cleanupService.deactivate();
            }

            LOG.info("Debug Framework OSGi component deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating debug framework component.", e);
        }
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
    @Reference(name = "debug.protocol.provider", service = DebugProtocolProvider.class, cardinality 
        = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "unsetDebugProtocolProvider")

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
     * Called by OSGi when a protocol module deactivates or unregisters its
     * provider.
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
     * Sets a debug protocol resolver.
     *
     * @param resolver Debug protocol resolver implementation.
     */
    @Reference(name = "debug.protocol.resolver", service = DebugProtocolResolver.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugProtocolResolver")
    protected void setDebugProtocolResolver(DebugProtocolResolver resolver) {

        if (resolver != null) {
            DebugFrameworkServiceDataHolder.getInstance().addDebugProtocolResolver(resolver);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugProtocolResolver registered: " + resolver.getClass().getName());
            }
        }
    }

    /**
     * Unsets a debug protocol resolver.
     *
     * @param resolver Debug protocol resolver implementation.
     */
    protected void unsetDebugProtocolResolver(DebugProtocolResolver resolver) {

        if (resolver != null) {
            DebugFrameworkServiceDataHolder.getInstance().removeDebugProtocolResolver(resolver);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugProtocolResolver unregistered: " + resolver.getClass().getName());
            }
        }
    }

    @Reference(name = "debug.execution.listener", service = DebugExecutionListener.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugExecutionListener")
    protected void setDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            DebugFrameworkServiceDataHolder.getInstance().addDebugExecutionListener(listener);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugExecutionListener registered: " + listener.getClass().getName());
            }
        }
    }

    protected void unsetDebugExecutionListener(DebugExecutionListener listener) {

        if (listener != null) {
            DebugFrameworkServiceDataHolder.getInstance().removeDebugExecutionListener(listener);
            if (LOG.isDebugEnabled()) {
                LOG.debug("DebugExecutionListener unregistered: " + listener.getClass().getName());
            }
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
        policy = ReferencePolicy.DYNAMIC, unbind = "unsetClaimMetadataManagementService")

    protected void setClaimMetadataManagementService(ClaimMetadataManagementService service) {

        LOG.debug("ClaimMetadataManagementService set in DebugServiceComponent");
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(service);
    }

    /**
     * Unsets the ClaimMetadataManagementService.
     *
     * @param claimMetadataManagementService the ClaimMetadataManagementService
     *                                       instance.
     */
    protected void unsetClaimMetadataManagementService(ClaimMetadataManagementService claimMetadataManagementService) {

        LOG.debug("ClaimMetadataManagementService unset in DebugServiceComponent");
        DebugFrameworkServiceDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
