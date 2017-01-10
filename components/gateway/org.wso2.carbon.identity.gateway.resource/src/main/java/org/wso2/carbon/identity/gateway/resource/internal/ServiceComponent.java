/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.gateway.resource.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.gateway.processor.AbstractGatewayProcessor;
import org.wso2.carbon.identity.gateway.resource.Gateway;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayRequestFactory;
import org.wso2.carbon.identity.gateway.resource.factory.GatewayResponseFactory;

import java.util.logging.Logger;

/**
 * Service component to consume OSGi services required by
 * {@link Gateway} MicroService.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.identity.gateway.resource.internal.ServiceComponent",
        immediate = true
)
public class ServiceComponent {

    private Logger logger = Logger.getLogger(ServiceComponent.class.getName());
    private DataHolder dataHolder = DataHolder.getInstance();

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        // Register MSF4JRequestFactory instance as an OSGi service.
        bundleContext.registerService(GatewayRequestFactory.class, new GatewayRequestFactory(),
                null);
        bundleContext.registerService(GatewayResponseFactory.class, new GatewayResponseFactory(), null);

        logger.info("Service Component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are not satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {

        logger.info("Service Component is deactivated");
    }

    /**
     * This bind method will be called when {@link GatewayRequestFactory} OSGi services are registered.
     *
     * @param requestBuilderFactory {@link GatewayRequestFactory} instance registered as an OSGi service
     */
    @Reference(
            name = "msf4j.identity.request.builder.factory",
            service = GatewayRequestFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRequestBuilderFactory"
    )
    protected void setRequestBuilderFactory(GatewayRequestFactory requestBuilderFactory) {

        dataHolder.addRequestFactory(requestBuilderFactory);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link GatewayRequestFactory}
     * OSGi service.
     *
     * @param requestBuilderFactory {@link GatewayRequestFactory} OSGi service.
     */
    protected void unsetRequestBuilderFactory(GatewayRequestFactory requestBuilderFactory) {

        dataHolder.removeRequestFactory(requestBuilderFactory);
    }

    /**
     * This bind method will be called when {@link GatewayResponseFactory} OSGi services are registered.
     *
     * @param responseFactory The {@link GatewayResponseFactory} instance registered as an OSGi service
     */
    @Reference(
            name = "msf4j.response.builder.factory",
            service = GatewayResponseFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetResponseBuilderFactory"
    )
    protected void setResponseBuilderFactory(GatewayResponseFactory responseFactory) {

        dataHolder.addResponseFactory(responseFactory);
    }

    /**
     * This is the unbind method which gets called at the un-registration of a {@link GatewayResponseFactory}
     * OSGi service.
     *
     * @param responseBuilderFactory The {@link GatewayResponseFactory} instance registered as an OSGi service
     */
    protected void unsetResponseBuilderFactory(GatewayResponseFactory responseBuilderFactory) {

        dataHolder.removeResponseFactory(responseBuilderFactory);
    }

//    /**
//     * This bind method will be called when {@link IdentityProcessCoordinator} OSGi service is registered.
//     *
//     * @param processCoordinator The {@link IdentityProcessCoordinator} instance registered as an OSGi service
//     */
//    @Reference(
//            name = "identity.process.coordinator",
//            service = IdentityProcessCoordinator.class,
//            cardinality = ReferenceCardinality.MANDATORY,
//            policy = ReferencePolicy.DYNAMIC,
//            unbind = "unsetIdentityProcessCoordinator"
//    )
//    protected void setIdentityProcessCoordinator(IdentityProcessCoordinator processCoordinator) {
//
//        dataHolder.setProcessCoordinator(processCoordinator);
//    }
//
//    /**
//     * This is the unbind method which gets called at the un-registration of {@link IdentityProcessCoordinator}
//     * OSGi service.
//     *
//     * @param processCoordinator The {@link IdentityProcessCoordinator} instance registered as an OSGi service
//     */
//    protected void unsetIdentityProcessCoordinator(IdentityProcessCoordinator processCoordinator) {
//
//        dataHolder.setProcessCoordinator(null);
//    }


    /**
     * This bind method will be called when {@link AbstractGatewayProcessor} OSGi service is registered.
     *
     * @param abstractGatewayProcessor The {@link AbstractGatewayProcessor} instance registered as an OSGi service
     */
    @Reference(
            name = "gateway.processor",
            service = AbstractGatewayProcessor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetGatewayProcessor"
    )
    protected void setGatewayProcessor(AbstractGatewayProcessor abstractGatewayProcessor) {

        dataHolder.addIdentityProcessor(abstractGatewayProcessor);
    }


    protected void unsetGatewayProcessor(AbstractGatewayProcessor abstractGatewayProcessor) {

        dataHolder.addIdentityProcessor(null);
    }

}
