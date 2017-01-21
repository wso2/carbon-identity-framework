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

import java.util.logging.Logger;

/**
 * Service Activator for Gateway Resource Bundle.
 *
 * @since 1.0.0
 */
@Component(
        name = "org.wso2.carbon.identity.gateway.resource.internal.GatewayResourceServiceComponent",
        immediate = true
)
public class GatewayResourceServiceComponent {

    private Logger logger = Logger.getLogger(GatewayResourceServiceComponent.class.getName());

    /**
     * This is the activation method of GatewayResourceServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {

        // Register MSF4JRequestFactory instance as an OSGi service.
        /*bundleContext.registerService(GatewayRequestFactory.class, new GatewayRequestFactory(),
                null);
        bundleContext.registerService(GatewayResponseFactory.class, new GatewayResponseFactory(), null);
*/
        logger.info("Service Component is activated");
    }

    /**
     * This is the deactivation method of GatewayResourceServiceComponent. This will be called when this component
     * is being stopped or references are not satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {

        logger.info("Service Component is deactivated");
    }


}
