/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementService;
import org.wso2.carbon.identity.functions.library.mgt.FunctionLibraryManagementServiceImpl;

/**
 * OSGi declarative services component which handled activation and deactivation of
 * FunctionLibraryManagementServiceComponent.
 */
@Component(
        name = "function.library.management.service",
        immediate = true
)
public class FunctionLibraryManagementServiceComponent {

    private static final Log log = LogFactory.getLog(FunctionLibraryManagementServiceComponent.class);
    private ServiceRegistration serviceRegistration = null;

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleContext = context.getBundleContext();
        // Registering Function library management service as an OSGIService.
        serviceRegistration = bundleContext.registerService(FunctionLibraryManagementService.class,
                FunctionLibraryManagementServiceImpl.getInstance(), null);
        if (log.isDebugEnabled()) {
            log.debug("Function Library Management bundle is activated.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Function Library Management bundle is deactivated.");
        }
        // Unregistering Function library management service.
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
}
