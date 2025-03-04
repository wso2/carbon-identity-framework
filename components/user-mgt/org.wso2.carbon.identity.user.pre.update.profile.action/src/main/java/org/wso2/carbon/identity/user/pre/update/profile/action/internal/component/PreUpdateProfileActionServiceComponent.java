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

package org.wso2.carbon.identity.user.pre.update.profile.action.internal.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.user.pre.update.profile.action.internal.management.PreUpdateProfileActionConverter;

/**
 * OSGI Service component for the Pre Update Profile Action.
 */
@Component(
        name = "pre.update.profile.action.service.component",
        immediate = true
)
public class PreUpdateProfileActionServiceComponent {

    private static final Log LOG = LogFactory.getLog(PreUpdateProfileActionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            bundleCtx.registerService(ActionConverter.class, new PreUpdateProfileActionConverter(), null);
            LOG.debug("Pre Update Profile Action Converter is enabled");

            LOG.debug("Pre Update Profile Action bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Pre Update Profile Action service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Pre Update Profile Action bundle is deactivated");
    }

    @Reference(
            name = "action.execution.service",
            service = ActionExecutorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutorService"
    )
    protected void setActionExecutionService(ActionExecutorService actionExecutorService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setActionExecutorService(actionExecutorService);
        LOG.debug("ActionExecutorService set in PreUpdateProfileActionServiceComponentHolder bundle.");
    }

    protected void unsetActionExecutorService(ActionExecutorService actionExecutorService) {

        PreUpdateProfileActionServiceComponentHolder.getInstance().setActionExecutorService(null);
        LOG.debug("ActionExecutorService unset in PreUpdateProfileActionServiceComponentHolder bundle.");
    }
}
