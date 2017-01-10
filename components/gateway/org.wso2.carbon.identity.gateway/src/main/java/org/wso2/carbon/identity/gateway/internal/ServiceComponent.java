package org.wso2.carbon.identity.gateway.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.framework.handler.AbstractHandler;
import org.wso2.carbon.identity.gateway.GatewayProcessor;
import org.wso2.carbon.identity.gateway.artifact.ArtifactReader;
import org.wso2.carbon.identity.gateway.artifact.ArtifactStore;
import org.wso2.carbon.identity.gateway.artifact.model.ServiceProvider;
import org.wso2.carbon.identity.gateway.element.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.element.authentication.handler.BasicAuthenticationHandler;
import org.wso2.carbon.identity.gateway.element.authentication.handler.RequestPathAuthenticator;
import org.wso2.carbon.identity.gateway.element.callback.BasicAuthCallbackHandler;
import org.wso2.carbon.identity.gateway.element.callback.GatewayCallbackHandler;
import org.wso2.carbon.identity.gateway.element.custom.CustomRequestValidator;
import org.wso2.carbon.identity.gateway.element.custom.CustomResponseBuilder;
import org.wso2.carbon.identity.gateway.element.response.SAMLResponseHandler;
import org.wso2.carbon.identity.gateway.element.validation.SAMLValidationHandler;
import org.wso2.carbon.identity.gateway.processor.CallbackProcessor;
import org.wso2.carbon.identity.gateway.processor.RequestProcessor;


/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
 *
 * @since 1.0.0-SNAPSHOT
 */
@Component(
        name = "org.wso2.carbon.identity.gateway.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private Logger logger = LoggerFactory.getLogger(ServiceComponent.class);

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        //HandlerChain handlerChain = ArtifactReader.loadChain();
        //ArtifactStore.getInstance().addHandlerChain(handlerChain);

        ServiceProvider serviceProvider = ArtifactReader.loadServiceProvider();
        ArtifactStore.getInstance().addServiceProvider(serviceProvider);



        // register processors
        bundleContext.registerService(GatewayProcessor.class, new RequestProcessor(), null);
        bundleContext.registerService(GatewayProcessor.class, new CallbackProcessor(), null);

        // registering callback handlers
        bundleContext.registerService(GatewayCallbackHandler.class, new BasicAuthCallbackHandler(), null);

        //Registering generic handlers list
        bundleContext.registerService(AbstractGatewayHandler.class, new CustomRequestValidator(), null);
        bundleContext.registerService(AbstractGatewayHandler.class, new CustomResponseBuilder(), null);
        bundleContext.registerService(AbstractGatewayHandler.class, new RequestPathAuthenticator(), null);
        bundleContext.registerService(AbstractGatewayHandler.class, new SAMLValidationHandler(), null);
        bundleContext.registerService(AbstractGatewayHandler.class, new SAMLResponseHandler(), null);
        bundleContext.registerService(AbstractGatewayHandler.class, new BasicAuthenticationHandler(), null);

        logger.info("Service Component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {

        logger.info("Service Component is deactivated");
    }


    /**
     * This bind method will be called when {@link GatewayCallbackHandler} OSGi services are registered
     *
     * @param callbackHandler {@link GatewayCallbackHandler} OSGi service instance.
     */
    @Reference(
            name = "gateway.callback.handler",
            service = GatewayCallbackHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetGatewayCallbackHandler"
    )
    protected void setGatewayCallbackHandler(GatewayCallbackHandler callbackHandler) {

        DataHolder.getInstance().addGatewayCallbackHandler(callbackHandler);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link GatewayCallbackHandler}
     * OSGi service.
     *
     * @param gatewayCallbackHandler The {@link GatewayCallbackHandler} instance registered by Carbon Kernel as
     *                               an OSGi service
     */
    protected void unsetGatewayCallbackHandler(GatewayCallbackHandler gatewayCallbackHandler) {

        DataHolder.getInstance().addGatewayCallbackHandler(null);
    }


    @Reference(
            name = "gateway.handler",
            service = AbstractGatewayHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHandler"
    )
    protected void setHandler(AbstractGatewayHandler handler) {

        DataHolder.getInstance().addHandler(handler);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link AbstractHandler}
     * OSGi service.
     *
     * @param handler The {@link AbstractHandler} instance registered by Carbon Kernel as
     *                               an OSGi service
     */
    protected void unsetHandler(AbstractHandler handler) {

        DataHolder.getInstance().addHandler(null);
    }




}
