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
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolProvider;
import org.wso2.carbon.identity.debug.framework.registry.DebugHandlerRegistry;
import org.wso2.carbon.identity.debug.framework.registry.DebugProtocolRegistry;
import org.wso2.carbon.identity.debug.idp.core.handler.IdpDebugResourceHandler;
import org.wso2.carbon.identity.debug.idp.registry.IdpDebugProviderRegistry;

/**
 * OSGi service component for the IDP debug handler module.
 */
@Component(name = "identity.debug.idp.component", immediate = true)
public class IdpDebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(IdpDebugServiceComponent.class);
    private static final String IDP_HANDLER_TYPE = "idp";

    @Activate
    protected void activate(ComponentContext context) {

        LOG.debug("Activating IDP Debug Handler Component.");
        DebugHandlerRegistry.getInstance().register(IDP_HANDLER_TYPE, new IdpDebugResourceHandler());
        LOG.debug("Registered IdpDebugResourceHandler with DebugHandlerRegistry.");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Deactivating IDP Debug Handler Component.");
        DebugHandlerRegistry.getInstance().unregister(IDP_HANDLER_TYPE);
        LOG.debug("IDP Debug Handler Component deactivated.");
    }

    @Reference(name = "debug.protocol.provider", service = DebugProtocolProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDebugProtocolProvider")
    protected void setDebugProtocolProvider(DebugProtocolProvider provider) {

        IdpDebugProviderRegistry.getInstance().addProvider(provider);
        if (provider.getCallbackHandler() != null) {
            DebugProtocolRegistry.getInstance().addDebugCallbackHandler(provider.getCallbackHandler());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugProtocolProvider registered for protocol: " + provider.getProtocolType());
        }
    }

    protected void unsetDebugProtocolProvider(DebugProtocolProvider provider) {

        IdpDebugProviderRegistry.getInstance().removeProvider(provider);
        if (provider.getCallbackHandler() != null) {
            DebugProtocolRegistry.getInstance().removeDebugCallbackHandler(provider.getCallbackHandler());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("DebugProtocolProvider unregistered for protocol: " + provider.getProtocolType());
        }
    }

}
