/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.internal;

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
import org.wso2.carbon.identity.action.execution.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.ActionExecutionRequestBuilderFactory;
import org.wso2.carbon.identity.action.execution.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.ActionExecutionResponseProcessorFactory;
import org.wso2.carbon.identity.action.execution.ActionExecutorService;
import org.wso2.carbon.identity.action.execution.ActionExecutorServiceImpl;
import org.wso2.carbon.identity.action.management.ActionManagementService;

/**
 * OSGI service component for the Action execution.
 */
@Component(
        name = "action.execution.service.component",
        immediate = true
)
public class ActionExecutionServiceComponent {

    private static final Log LOG = LogFactory.getLog(ActionExecutionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(ActionExecutorService.class.getName(), ActionExecutorServiceImpl.getInstance(),
                    null);
            LOG.debug("Action execution bundle is activated.");
        } catch (Throwable e) {
            LOG.error("Error while initializing Action execution service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(ActionExecutorService.class));
            LOG.debug("Action execution bundle is deactivated.");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Action execution service component.", e);
        }
    }

    @Reference(
            name = "action.management.service",
            service = ActionManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionManagementService"
    )
    protected void setActionManagementService(ActionManagementService actionManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering a reference for ActionManagementService in the ActionExecutionServiceComponent.");
        }
        ActionExecutionServiceComponentHolder.getInstance().setActionManagementService(actionManagementService);
    }

    protected void unsetActionManagementService(ActionManagementService actionManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Unregistering the reference for ActionManagementService in the ActionExecutionServiceComponent.");
        }
        if (ActionExecutionServiceComponentHolder.getInstance().getActionManagementService()
                .equals(actionManagementService)) {
            ActionExecutionServiceComponentHolder.getInstance().setActionManagementService(null);
        }
    }

    @Reference(
            name = "action.execution.request.builder",
            service = ActionExecutionRequestBuilder.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutionRequestBuilder"
    )
    protected void setActionExecutionRequestBuilder(ActionExecutionRequestBuilder actionExecutionRequestBuilder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering ActionExecutionRequestBuilder: " +
                            actionExecutionRequestBuilder.getClass().getName() +
                            " in the ActionExecutionServiceComponent.");
        }
        ActionExecutionRequestBuilderFactory.registerActionExecutionRequestBuilder(actionExecutionRequestBuilder);
    }

    protected void unsetActionExecutionRequestBuilder(ActionExecutionRequestBuilder actionExecutionRequestBuilder) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering ActionExecutionRequestBuilder: " +
                    actionExecutionRequestBuilder.getClass().getName() + " in the ActionExecutionServiceComponent.");
        }
        ActionExecutionRequestBuilderFactory.unregisterActionExecutionRequestBuilder(actionExecutionRequestBuilder);
    }

    @Reference(
            name = "action.execution.response.processor",
            service = ActionExecutionResponseProcessor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutionResponseProcessor"
    )
    protected void setActionExecutionResponseProcessor(
            ActionExecutionResponseProcessor actionExecutionResponseProcessor) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Registering ActionExecutionResponseProcessor: " +
                            actionExecutionResponseProcessor.getClass().getName() +
                            " in the ActionExecutionServiceComponent.");
        }
        ActionExecutionResponseProcessorFactory.registerActionExecutionResponseProcessor(
                actionExecutionResponseProcessor);
    }

    protected void unsetActionExecutionResponseProcessor(
            ActionExecutionResponseProcessor actionExecutionResponseProcessor) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unregistering ActionExecutionResponseProcessor: " +
                    actionExecutionResponseProcessor.getClass().getName() + " in the ActionExecutionServiceComponent.");
        }
        ActionExecutionResponseProcessorFactory.unregisterActionExecutionResponseProcessor(
                actionExecutionResponseProcessor);
    }
}
