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

package org.wso2.carbon.identity.debug.idp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
import org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry;
import org.wso2.carbon.identity.debug.idp.core.handler.IdpDebugResourceHandler;
import org.wso2.carbon.identity.debug.idp.resolver.IdpDebugProtocolResolver;

/**
 * OSGi service component for the IDP debug handler module.
 * Registers the IDP-specific debug resource handler and protocol resolver with the core framework.
 *
 * Protocol providers (e.g., OIDC, Google) are bound by the framework's DebugServiceComponent
 * into DebugFrameworkServiceDataHolder. This component only registers IDP-specific services.
 */
@Component(name = "identity.debug.idp.component", immediate = true)
public class IdpDebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(IdpDebugServiceComponent.class);
    private static final String IDP_HANDLER_TYPE = "idp";
    private ServiceRegistration<DebugProtocolResolver> resolverServiceRegistration;

    /**
     * Activates the IDP debug handler component.
     * Registers IdpDebugResourceHandler and IdpDebugProtocolResolver with the framework.
     *
     * @param context The component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        try {
            LOG.debug("Activating IDP Debug Handler Component.");

            // Register the IDP debug resource handler with the framework.
            IdpDebugResourceHandler idpHandler = new IdpDebugResourceHandler();
            DebugHandlerRegistry.getInstance().register(IDP_HANDLER_TYPE, idpHandler);

            LOG.debug("Registered IdpDebugResourceHandler with DebugHandlerRegistry.");

            // Register the IDP debug protocol resolver as an OSGi service.
            // The framework's DebugServiceComponent will pick this up via @Reference.
            IdpDebugProtocolResolver resolver = new IdpDebugProtocolResolver();
            resolverServiceRegistration = context.getBundleContext().registerService(
                    DebugProtocolResolver.class, resolver, null);

            LOG.debug("Registered IdpDebugProtocolResolver service.");
        } catch (Throwable e) {
            LOG.error("Error while activating IDP Debug Handler Component.", e);
        }
    }

    /**
     * Deactivates the IDP debug handler component.
     *
     * @param context The component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            LOG.debug("Deactivating IDP Debug Handler Component.");

            // Clear the IDP handler registration.
            DebugHandlerRegistry.getInstance().unregister(IDP_HANDLER_TYPE);

            if (resolverServiceRegistration != null) {
                resolverServiceRegistration.unregister();
                resolverServiceRegistration = null;
            }

            LOG.debug("IDP Debug Handler Component deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating IDP Debug Handler Component.", e);
        }
    }
}
