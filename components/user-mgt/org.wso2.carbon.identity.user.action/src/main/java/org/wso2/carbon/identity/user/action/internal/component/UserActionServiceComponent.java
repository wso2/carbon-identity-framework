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

package org.wso2.carbon.identity.user.action.internal.component;

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
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.user.action.api.service.UserActionExecutor;
import org.wso2.carbon.identity.user.action.internal.factory.UserActionExecutorFactory;
import org.wso2.carbon.identity.user.action.internal.listener.ActionUserOperationEventListener;
import org.wso2.carbon.user.core.listener.UserOperationEventListener;

/**
 * Service component for the Pre Update Password Action.
 */
@Component(
        name = "user.action.service.component",
        immediate = true
)
public class UserActionServiceComponent {

    private static final Log LOG = LogFactory.getLog(UserActionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(UserOperationEventListener.class, new ActionUserOperationEventListener(), null);
            LOG.debug("Action UserOperation Event Listener is enabled");
            LOG.debug("User Action bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing User Action service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("User Action bundle is deactivated");
    }

    @Reference(
            name = "user.action.executor",
            service = UserActionExecutor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetUserActionExecutor"
    )
    protected void setUserActionExecutor(UserActionExecutor userActionExecutor) {

        LOG.debug("Registering UserActionExecutor: " + userActionExecutor.getClass().getName() +
                    " in the UserActionServiceComponent.");
        UserActionExecutorFactory.registerUserActionExecutor(userActionExecutor);
    }

    protected void unsetUserActionExecutor(UserActionExecutor userActionExecutor) {

        LOG.debug("Unregistering UserActionExecutor: " + userActionExecutor.getClass().getName() +
                " in the UserActionServiceComponent.");
        UserActionExecutorFactory.unregisterUserActionExecutor(userActionExecutor);
    }

    @Reference(
            name = "organization.service",
            service = OrganizationManager.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOrganizationManager"
    )
    protected void setOrganizationManager(OrganizationManager organizationManager) {

        UserActionServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
        LOG.debug("Organization management service set in UserActionServiceComponentHolder bundle.");
    }

    protected void unsetOrganizationManager(OrganizationManager organizationManager) {

        UserActionServiceComponentHolder.getInstance().setOrganizationManager(null);
        LOG.debug("Organization management service unset in UserActionServiceComponentHolder bundle.");
    }
}
