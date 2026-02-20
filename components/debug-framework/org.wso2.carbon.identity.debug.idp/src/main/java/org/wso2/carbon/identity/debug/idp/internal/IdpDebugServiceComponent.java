package org.wso2.carbon.identity.debug.idp.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.debug.framework.extension.DebugProtocolResolver;
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

    /**
     * Activates the IDP debug handler component.
     * Registers IdpDebugResourceHandler and IdpDebugProtocolResolver with the framework.
     *
     * @param context The component context.
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

            // Register the IDP debug protocol resolver as an OSGi service.
            // The framework's DebugServiceComponent will pick this up via @Reference.
            IdpDebugProtocolResolver resolver = new IdpDebugProtocolResolver();
            context.getBundleContext().registerService(
                    DebugProtocolResolver.class, resolver, null);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Registered IdpDebugProtocolResolver service.");
            }

        } catch (Exception e) {
            LOG.error("Failed to activate IDP Debug Handler Component.", e);
            throw new RuntimeException("Failed to activate IDP Debug Handler Component.", e);
        }
    }

    /**
     * Deactivates the IDP debug handler component.
     *
     * @param context The component context.
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
