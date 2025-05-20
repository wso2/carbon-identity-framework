/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.api.service.AsyncOperationStatusMgtService;
import org.wso2.carbon.identity.framework.async.operation.status.mgt.internal.service.impl.AsyncOperationStatusMgtServiceImpl;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;

/**
 * OSGi service component for asynchronous operation status management bundle.
 */
@Component(
        name = "async.operation.status.mgt.service.component",
        immediate = true
)
public class AsyncOperationStatusMgtServiceComponent {

    private static final Log LOG = LogFactory.getLog(AsyncOperationStatusMgtServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(
                    AsyncOperationStatusMgtService.class, AsyncOperationStatusMgtServiceImpl.getInstance(), null);
            LOG.debug("Async Operation status mgt bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Async Operation status management component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(AsyncOperationStatusMgtService.class));
            LOG.debug("Async operation status mgt bundle is deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Async operation status management component.", e);
        }
    }

    @Reference(
            name = "organization.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager"
    )
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        AsyncOperationStatusMgtDataHolder.getInstance().setOrganizationManager(organizationManager);
        LOG.debug("OrganizationManager set in Async Operation Status Management bundle.");
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        AsyncOperationStatusMgtDataHolder.getInstance().setOrganizationManager(null);
        LOG.debug("OrganizationManager unset in Async Operation Status Management bundle.");
    }
}
