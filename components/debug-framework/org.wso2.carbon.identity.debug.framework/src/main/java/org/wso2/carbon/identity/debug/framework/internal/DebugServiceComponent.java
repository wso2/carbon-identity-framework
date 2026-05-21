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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.application.authentication.framework.DebugAuthenticationInterceptor;
import org.wso2.carbon.identity.debug.framework.core.DebugCommonAuthInterceptor;
import org.wso2.carbon.identity.debug.framework.core.DebugRequestCoordinator;
import org.wso2.carbon.identity.debug.framework.extension.DebugCallbackHandler;
import org.wso2.carbon.identity.debug.framework.listener.DebugExecutionListener;
import org.wso2.carbon.identity.debug.framework.listener.DebugSessionCleanupExecutionListener;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;
import org.wso2.carbon.identity.debug.framework.store.DebugSessionCleanupService;

/**
 * OSGi service component for Debug Framework.
 * This component provides the framework infrastructure for debug operations.
*/
@Component(name = "identity.debug.service.component", immediate = true)
public class DebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(DebugServiceComponent.class);

    private ServiceRegistration<DebugExecutionListener> cleanupListenerServiceRegistration;
    private ServiceRegistration<DebugRequestCoordinator> requestCoordinatorServiceRegistration;
    private ServiceRegistration<DebugAuthenticationInterceptor> authInterceptorServiceRegistration;
    private DebugSessionCleanupService cleanupService;

    @Activate
    protected void activate(ComponentContext context) {

        LOG.debug("Debug Framework OSGi component activating");
        BundleContext bundleContext = context.getBundleContext();

        try {
            // Register DebugRequestCoordinator as the API-facing debug service.
            DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
            requestCoordinatorServiceRegistration = bundleContext.registerService(
                    DebugRequestCoordinator.class, requestCoordinator, null);

            // Register a dedicated auth interceptor that delegates callbacks to the coordinator.
            authInterceptorServiceRegistration = bundleContext.registerService(
                    DebugAuthenticationInterceptor.class,
                    new DebugCommonAuthInterceptor(requestCoordinator), null);

            // Register the cleanup listener as an OSGi service.
            cleanupListenerServiceRegistration = bundleContext.registerService(
                    DebugExecutionListener.class, new DebugSessionCleanupExecutionListener(), null);

            // Start the periodic cleanup service for expired sessions.
            cleanupService = new DebugSessionCleanupService();
            cleanupService.activate();

            LOG.info("Debug Framework initialized. Waiting for protocol providers to register...");
            LOG.debug("DebugRequestCoordinator and DebugCommonAuthInterceptor services registered");
        } catch (Exception e) {
            LOG.error("Error during debug framework component activation", e);
        }

        LOG.debug("Debug Framework OSGi component activated successfully");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Debug Framework OSGi component deactivating");

        try {
            // Unregister request coordinator service.
            requestCoordinatorServiceRegistration = unregisterService(requestCoordinatorServiceRegistration,
                    "DebugRequestCoordinator service unregistered");

            // Unregister auth interceptor service.
            authInterceptorServiceRegistration = unregisterService(authInterceptorServiceRegistration,
                    "DebugAuthenticationInterceptor service unregistered");

            // Unregister cleanup listener service.
            cleanupListenerServiceRegistration = unregisterService(cleanupListenerServiceRegistration,
                    "DebugSessionCleanupExecutionListener service unregistered");

            // Shutdown the cleanup service.
            if (cleanupService != null) {
                cleanupService.deactivate();
                cleanupService = null;
            }

            LOG.debug("Debug Framework OSGi component deactivated");
        } catch (Exception e) {
            LOG.error("Error during debug framework component deactivation", e);
        }
    }

    @Reference(name = "debug.execution.listener", service = DebugExecutionListener.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugExecutionListener")
    protected void setDebugExecutionListener(DebugExecutionListener listener) {

        bindExecutionListener(listener, true);
    }

    protected void unsetDebugExecutionListener(DebugExecutionListener listener) {

        bindExecutionListener(listener, false);
    }

    @Reference(name = "debug.callback.handler", service = DebugCallbackHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugCallbackHandler")
    protected void setDebugCallbackHandler(DebugCallbackHandler handler) {

        bindCallbackHandler(handler, true);
    }

    protected void unsetDebugCallbackHandler(DebugCallbackHandler handler) {

        bindCallbackHandler(handler, false);
    }

    private void bindExecutionListener(DebugExecutionListener listener, boolean isBind) {

        if (listener == null) {
            return;
        }

        if (isBind) {
            DebugFrameworkServiceDataHolder.getInstance().addDebugExecutionListener(listener);
        } else {
            DebugFrameworkServiceDataHolder.getInstance().removeDebugExecutionListener(listener);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugExecutionListener " + getLifecycleAction(isBind) + ": " + listener.getClass().getName());
        }
    }

    private void bindCallbackHandler(DebugCallbackHandler handler, boolean isBind) {

        if (handler == null) {
            return;
        }

        if (isBind) {
            DebugProtocolRegistry.getInstance().addDebugCallbackHandler(handler);
        } else {
            DebugProtocolRegistry.getInstance().removeDebugCallbackHandler(handler);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugCallbackHandler " + getLifecycleAction(isBind) + ": " + handler.getClass().getName());
        }
    }

    private <T> ServiceRegistration<T> unregisterService(ServiceRegistration<T> registration, String debugMessage) {

        if (registration == null) {
            return null;
        }

        registration.unregister();
        LOG.debug(debugMessage);
        return null;
    }

    private String getLifecycleAction(boolean isBind) {

        return isBind ? "registered" : "unregistered";
    }
}
