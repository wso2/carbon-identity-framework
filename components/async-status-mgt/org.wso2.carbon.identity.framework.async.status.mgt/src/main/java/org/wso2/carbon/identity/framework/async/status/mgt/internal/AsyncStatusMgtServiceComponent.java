/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtService;
import org.wso2.carbon.identity.framework.async.status.mgt.AsyncStatusMgtServiceImpl;

/**
 * OSGi service component for asynchronous operation status management bundle.
 */
@Component(
        name = "org.wso2.carbon.identity.framework.internal."
                + "AsyncStatusMgtServiceComponent",
        immediate = true
)
public class AsyncStatusMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(AsyncStatusMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        BundleContext bundleCtx = context.getBundleContext();
        bundleCtx.registerService(AsyncStatusMgtService.class.getName(), AsyncStatusMgtServiceImpl.getInstance(), null);
        LOG.debug("Async status mgt bundle is activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        BundleContext bundleCtx = context.getBundleContext();
        bundleCtx.ungetService(bundleCtx.getServiceReference(AsyncStatusMgtService.class));
        LOG.debug("Async status mgt bundle is deactivated");
    }
}
