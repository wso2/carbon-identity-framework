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
import org.wso2.carbon.identity.debug.framework.registry.DebugTypeRegistry;
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

        BundleContext bundleContext = context.getBundleContext();

        DebugRequestCoordinator requestCoordinator = new DebugRequestCoordinator();
        DebugFrameworkServiceDataHolder.getInstance().setDebugRequestCoordinator(requestCoordinator);
        requestCoordinatorServiceRegistration = bundleContext.registerService(
                DebugRequestCoordinator.class, requestCoordinator, null);

        authInterceptorServiceRegistration = bundleContext.registerService(
                DebugAuthenticationInterceptor.class, new DebugCommonAuthInterceptor(), null);

        cleanupListenerServiceRegistration = bundleContext.registerService(
                DebugExecutionListener.class, new DebugSessionCleanupExecutionListener(), null);

        cleanupService = new DebugSessionCleanupService();
        cleanupService.activate();

        LOG.info("Debug Framework initialized. Waiting for protocol providers to register...");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        requestCoordinatorServiceRegistration = unregisterService(requestCoordinatorServiceRegistration);
        authInterceptorServiceRegistration = unregisterService(authInterceptorServiceRegistration);
        cleanupListenerServiceRegistration = unregisterService(cleanupListenerServiceRegistration);

        DebugFrameworkServiceDataHolder.getInstance().setDebugRequestCoordinator(null);

        if (cleanupService != null) {
            cleanupService.deactivate();
            cleanupService = null;
        }

        LOG.debug("Debug Framework OSGi component deactivated");
    }

    @Reference(name = "debug.execution.listener", service = DebugExecutionListener.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugExecutionListener")
    protected void setDebugExecutionListener(DebugExecutionListener listener) {

        DebugFrameworkServiceDataHolder.getInstance().addDebugExecutionListener(listener);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugExecutionListener registered: " + listener.getClass().getName());
        }
    }

    protected void unsetDebugExecutionListener(DebugExecutionListener listener) {

        DebugFrameworkServiceDataHolder.getInstance().removeDebugExecutionListener(listener);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugExecutionListener unregistered: " + listener.getClass().getName());
        }
    }

    @Reference(name = "debug.callback.handler", service = DebugCallbackHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugCallbackHandler")
    protected void setDebugCallbackHandler(DebugCallbackHandler handler) {

        DebugTypeRegistry.getInstance().addDebugCallbackHandler(handler);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugCallbackHandler registered: " + handler.getClass().getName());
        }
    }

    protected void unsetDebugCallbackHandler(DebugCallbackHandler handler) {

        DebugTypeRegistry.getInstance().removeDebugCallbackHandler(handler);
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugCallbackHandler unregistered: " + handler.getClass().getName());
        }
    }

    private <T> ServiceRegistration<T> unregisterService(ServiceRegistration<T> registration) {

        if (registration != null) {
            registration.unregister();
        }
        return null;
    }
}
