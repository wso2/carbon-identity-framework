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

package org.wso2.carbon.identity.action.execution.internal.component;

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
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionRequestBuilder;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.api.service.ActionExecutorService;
import org.wso2.carbon.identity.action.execution.api.service.ActionInvocationResponseClassProvider;
import org.wso2.carbon.identity.action.execution.api.service.ActionVersioningHandler;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionRequestBuilderFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutionResponseProcessorFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionExecutorServiceImpl;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionInvocationResponseClassFactory;
import org.wso2.carbon.identity.action.execution.internal.service.impl.ActionVersioningHandlerFactory;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;

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

        LOG.debug("Registering a reference for ActionManagementService in the ActionExecutionServiceComponent.");
        ActionExecutionServiceComponentHolder.getInstance().setActionManagementService(actionManagementService);
    }

    protected void unsetActionManagementService(ActionManagementService actionManagementService) {

        LOG.debug(
                "Unregistering the reference for ActionManagementService in the ActionExecutionServiceComponent.");
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

        LOG.debug("Registering ActionExecutionRequestBuilder: " +
                actionExecutionRequestBuilder.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionExecutionRequestBuilderFactory.registerActionExecutionRequestBuilder(actionExecutionRequestBuilder);
    }

    protected void unsetActionExecutionRequestBuilder(ActionExecutionRequestBuilder actionExecutionRequestBuilder) {

        LOG.debug("Unregistering ActionExecutionRequestBuilder: " +
                actionExecutionRequestBuilder.getClass().getName() + " in the ActionExecutionServiceComponent.");
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

        LOG.debug("Registering ActionExecutionResponseProcessor: " +
                actionExecutionResponseProcessor.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionExecutionResponseProcessorFactory.registerActionExecutionResponseProcessor(
                actionExecutionResponseProcessor);
    }

    protected void unsetActionExecutionResponseProcessor(
            ActionExecutionResponseProcessor actionExecutionResponseProcessor) {

        LOG.debug("Unregistering ActionExecutionResponseProcessor: " +
                actionExecutionResponseProcessor.getClass().getName() + " in the ActionExecutionServiceComponent.");
        ActionExecutionResponseProcessorFactory.unregisterActionExecutionResponseProcessor(
                actionExecutionResponseProcessor);
    }

    @Reference(
            name = "rule.evaluation.service.component",
            service = RuleEvaluationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRuleEvaluationService"
    )
    protected void setRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        LOG.debug("Registering a reference for RuleEvaluationService in the ActionExecutionServiceComponent.");
        ActionExecutionServiceComponentHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
    }

    protected void unsetRuleEvaluationService(RuleEvaluationService ruleEvaluationService) {

        LOG.debug("Unregistering reference for RuleEvaluationService in the ActionExecutionServiceComponent.");
        ActionExecutionServiceComponentHolder.getInstance().setRuleEvaluationService(ruleEvaluationService);
    }
  
    @Reference(
            name = "action.execution.response.ActionInvocationResponseClassProvider",
            service = ActionInvocationResponseClassProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetInvocationSuccessResponseContextClass"
    )
    protected void setInvocationSuccessResponseContextClass(ActionInvocationResponseClassProvider classProvider) {

        LOG.debug("Registering ActionInvocationResponseClassProvider: " + classProvider.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionInvocationResponseClassFactory.registerActionInvocationResponseClassProvider(
                classProvider);
    }

    protected void unsetInvocationSuccessResponseContextClass(ActionInvocationResponseClassProvider classProvider) {

        LOG.debug("Unregistering ActionInvocationResponseClassProvider: " + classProvider.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionInvocationResponseClassFactory.unregisterActionInvocationResponseClassProvider(classProvider);
    }

    @Reference(
            name = "action.execution.ActionVersioningHandler",
            service = ActionVersioningHandler.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionVersioningHandler"
    )
    protected void setActionVersioningHandler(ActionVersioningHandler actionVersionHandler) {

        LOG.debug("Registering ActionVersioningHandler: " + actionVersionHandler.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionVersioningHandlerFactory.registerActionVersioningHandler(actionVersionHandler);
    }

    protected void unsetActionVersioningHandler(ActionVersioningHandler actionVersionHandler) {

        LOG.debug("Unregistering ActionVersioningHandler: " + actionVersionHandler.getClass().getName() +
                " in the ActionExecutionServiceComponent.");
        ActionVersioningHandlerFactory.unregisterActionVersioningHandler(actionVersionHandler);
    }
}
