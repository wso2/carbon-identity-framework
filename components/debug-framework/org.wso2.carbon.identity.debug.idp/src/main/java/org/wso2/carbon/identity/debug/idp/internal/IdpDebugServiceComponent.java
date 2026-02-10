/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.idp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.debug.idp.core.handler.IdpDebugResourceHandler;

/**
 * OSGi service component for the IDP debug handler module.
 * Registers the IDP-specific debug resource handlers with the core framework.
 * 
 * Note: Protocol providers (e.g., OIDCDebugProtocolProvider,
 * GoogleDebugProtocolProvider)
 * should register themselves from their own OSGi modules to maintain proper
 * separation of concerns. This module only handles IDP resource-level
 * debugging.
 */
@Component(name = "identity.debug.idp.component", immediate = true)
public class IdpDebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(IdpDebugServiceComponent.class);

    /**
     * Activates the IDP debug handler component.
     * Registers the IdpDebugResourceHandler with the framework's debug handler
     * registry.
     * 
     * Protocol-specific providers (OAuth2, SAML, etc.) are registered by their
     * respective modules, not by this IDP handler component.
     *
     * @param context The OSGi component context.
     */
    @Activate
    protected void activate(ComponentContext context) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Activating IDP Debug Handler Component.");
        }

        try {
            // Register the IDP debug resource handler with the framework.
            IdpDebugResourceHandler idpHandler = new IdpDebugResourceHandler();
            org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry.getInstance()
                    .register("idp", idpHandler);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered IdpDebugResourceHandler with DebugHandlerRegistry.");
            }

            LOG.info("IDP Debug Handler Component activated successfully.");

        } catch (Exception e) {
            LOG.error("Failed to activate IDP Debug Handler Component.", e);
            throw new RuntimeException("Failed to activate IDP Debug Handler Component.", e);
        }
    }

    /**
     * Deactivates the IDP debug handler component.
     * Unregisters the handlers and providers from the framework.
     *
     * @param context The OSGi component context.
     */
    @Deactivate
    protected void deactivate(ComponentContext context) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deactivating IDP Debug Handler Component.");
        }

        // Clear the IDP handler registration.
        org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry.getInstance()
                .unregister("idp");

        if (LOG.isDebugEnabled()) {
            LOG.debug("IDP Debug Handler Component deactivated.");
        }
    }
}
