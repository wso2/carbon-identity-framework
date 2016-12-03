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
import org.wso2.carbon.identity.framework.IdentityProcessCoordinator;
import org.wso2.carbon.identity.framework.response.factory.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.gateway.resource.MSF4JIdentityRequestFactory;
import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.logging.Logger;

/**
 * Service component to consume CarbonRuntime instance which has been registered as an OSGi service
 * by Carbon Kernel.
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
        logger.info("Service Component is activated");

        // Register MSF4JRequestFactory instance as an OSGi service.
        bundleContext.registerService(MSF4JIdentityRequestFactory.class, new MSF4JIdentityRequestFactory(), null);
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
     * This bind method will be called when CarbonRuntime OSGi service is registered.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    @Reference(
            name = "carbon.runtime.service",
            service = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCarbonRuntime"
    )
    protected void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        dataHolder.setCarbonRuntime(carbonRuntime);
    }

    /**
     * This is the unbind method which gets called at the un-registration of CarbonRuntime OSGi service.
     *
     * @param carbonRuntime The CarbonRuntime instance registered by Carbon Kernel as an OSGi service
     */
    protected void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        DataHolder.getInstance().setCarbonRuntime(null);
    }

    @Reference(
            name = "msf4j.http.request.factory",
            service = MSF4JIdentityRequestFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRequestFactory"
    )
    protected void setRequestFactory(MSF4JIdentityRequestFactory requestFactory) {
        dataHolder.addRequestFactory(requestFactory);
    }

    protected void unsetRequestFactory(MSF4JIdentityRequestFactory requestFactory) {
        dataHolder.removeRequestFactory(requestFactory);
    }

    @Reference(
            name = "http.response.factory",
            service = HttpIdentityResponseFactory.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetResponseFactory"
    )
    protected void setResponseFactory(HttpIdentityResponseFactory httpIdentityResponseFactory) {
        dataHolder.addResponseFactory(httpIdentityResponseFactory);
    }

    protected void unsetResponseFactory(HttpIdentityResponseFactory httpIdentityResponseFactory) {
        dataHolder.removeResponseFactory(httpIdentityResponseFactory);
    }

    @Reference(
            name = "identity.process.coordinator",
            service = IdentityProcessCoordinator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdentityProcessCoordinator"
    )
    protected void setIdentityProcessCoordinator(IdentityProcessCoordinator processCoordinator) {
        dataHolder.setProcessCoordinator(processCoordinator);
    }

    protected void unsetIdentityProcessCoordinator(IdentityProcessCoordinator processCoordinator) {
        dataHolder.setProcessCoordinator(null);
    }


}
