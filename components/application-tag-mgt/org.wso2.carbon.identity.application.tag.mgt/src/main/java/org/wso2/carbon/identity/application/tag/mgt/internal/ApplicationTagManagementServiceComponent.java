/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.tag.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagManager;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagManagerImpl;

/**
 * Service component for the Application Tag management.
 */
@Component(
        name = "application.tag.mgt.service.component",
        immediate = true
)
public class ApplicationTagManagementServiceComponent {

    private static final Log LOG = LogFactory.getLog(ApplicationTagManagementServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.registerService(ApplicationTagManager.class, ApplicationTagManagerImpl.getInstance(),
                    null);
            LOG.info("Application Tag management bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Application Tag management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        try {
            BundleContext bundleCtx = ctxt.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(ApplicationTagManager.class));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Application Tag management bundle is deactivated");
            }
        } catch (Throwable e) {
            LOG.error("Error while deactivating Application Tag management component.", e);
        }
    }
}
