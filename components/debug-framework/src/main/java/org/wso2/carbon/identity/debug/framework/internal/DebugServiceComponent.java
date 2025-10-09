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
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.debug.framework.RequestCoordinator;
import org.wso2.carbon.identity.debug.framework.DebugService;
import org.wso2.carbon.identity.debug.framework.DebugFlowService;

/**
 * OSGi service component for Debug Framework.
 */
@Component(
        name = "identity.debug.service.component",
        immediate = true
)
public class DebugServiceComponent {

    private static final Log LOG = LogFactory.getLog(DebugServiceComponent.class);
    
    // Static initialization logging to verify class loading
    static {
        try {
            Log log = LogFactory.getLog(DebugServiceComponent.class);
            log.info("=== STATIC INIT: DebugServiceComponent class is being loaded ===");
        } catch (Exception e) {
            System.err.println("ERROR in DebugServiceComponent static init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Activate
    protected void activate(ComponentContext context) {
        LOG.info("=== DEBUG FRAMEWORK: OSGi component activation STARTED ===");
        
        try {
            BundleContext bundleContext = context.getBundleContext();
            LOG.info("Debug Framework: Got bundle context: " + bundleContext);
            
            if (bundleContext == null) {
                throw new RuntimeException("BundleContext is null - cannot register services");
            }
            
            // Create RequestCoordinator instance
            RequestCoordinator debugCoordinator = new RequestCoordinator();
            LOG.info("Debug Framework: Created RequestCoordinator instance: " + debugCoordinator);
            
            // Register RequestCoordinator as DebugService (it implements DebugService interface)  
            ServiceRegistration<?> debugServiceReg = bundleContext.registerService(
                DebugService.class.getName(), debugCoordinator, null);
            LOG.info("Debug Framework: Registered DebugService - Registration: " + debugServiceReg);
            
            // Register RequestCoordinator itself for direct access
            ServiceRegistration<?> coordinatorReg = bundleContext.registerService(
                RequestCoordinator.class.getName(), debugCoordinator, null);
            LOG.info("Debug Framework: Registered RequestCoordinator - Registration: " + coordinatorReg);
            
            // Create and register DebugFlowService (for backward compatibility)
            DebugFlowService debugFlowService = new DebugFlowService();
            ServiceRegistration<?> flowServiceReg = bundleContext.registerService(
                DebugFlowService.class.getName(), debugFlowService, null);
            LOG.info("Debug Framework: Registered DebugFlowService - Registration: " + flowServiceReg);
            
            // Store services in data holder for authentication framework access
            DebugFrameworkServiceDataHolder.getInstance().setDebugService(debugCoordinator);
            DebugFrameworkServiceDataHolder.getInstance().setRequestCoordinator(debugCoordinator);
            LOG.info("Debug Framework: Stored services in data holder");
            
            LOG.info("=== DEBUG FRAMEWORK: OSGi component activated SUCCESSFULLY ===");
            
        } catch (Throwable e) {
            LOG.error("=== DEBUG FRAMEWORK: FATAL ERROR during OSGi component activation ===", e);
            LOG.error("Debug Framework activation failed with: " + e.getClass().getName() + ": " + e.getMessage());
            if (e.getCause() != null) {
                LOG.error("Caused by: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
            }
            // Re-throw to ensure OSGi framework knows about the failure
            throw new RuntimeException("Debug Framework activation failed: " + e.getMessage(), e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        LOG.info("=== DEBUG FRAMEWORK: OSGi component deactivated ===");
    }
}
