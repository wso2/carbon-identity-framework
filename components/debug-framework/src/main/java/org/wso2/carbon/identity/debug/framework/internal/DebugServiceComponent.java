/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.debug.framework.DebugService;
import org.wso2.carbon.identity.debug.framework.RequestCoordinator;

/**
 * OSGi service component for Debug Framework.
 */
@Component(
        name = "identity.debug.service.component",
        immediate = true
)
public class DebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(DebugServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            BundleContext bundleContext = context.getBundleContext();
            
            if (bundleContext == null) {
                throw new RuntimeException("BundleContext is null - cannot register services");
            }
            
            // Create and register RequestCoordinator instance
            RequestCoordinator debugCoordinator = new RequestCoordinator();
            
            // Register RequestCoordinator as DebugService (it implements DebugService interface)  
            bundleContext.registerService(DebugService.class.getName(), debugCoordinator, null);
            
            // Register RequestCoordinator itself for direct access
            bundleContext.registerService(RequestCoordinator.class.getName(), debugCoordinator, null);
            
            // Store services in data holder for authentication framework access
            DebugFrameworkServiceDataHolder.getInstance().setDebugService(debugCoordinator);
            DebugFrameworkServiceDataHolder.getInstance().setRequestCoordinator(debugCoordinator);
            
            LOG.info("Debug Framework OSGi component activated successfully");
            
        } catch (Throwable e) {
            LOG.error("Debug Framework activation failed", e);
            throw new RuntimeException("Debug Framework activation failed: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOG.info("Debug Framework OSGi component deactivated");
    }
}
