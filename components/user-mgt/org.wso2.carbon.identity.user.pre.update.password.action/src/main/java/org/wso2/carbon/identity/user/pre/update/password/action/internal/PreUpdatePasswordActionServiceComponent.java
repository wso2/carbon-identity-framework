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

package org.wso2.carbon.identity.user.pre.update.password.action.internal;

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
import org.wso2.carbon.identity.action.execution.ActionExecutionResponseProcessor;
import org.wso2.carbon.identity.action.execution.ActionExecutorService;
import org.wso2.carbon.identity.action.management.service.ActionConverter;
import org.wso2.carbon.identity.action.management.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.rule.evaluation.provider.RuleEvaluationDataProvider;
import org.wso2.carbon.identity.user.action.service.UserActionExecutor;
import org.wso2.carbon.identity.user.pre.update.password.action.core.execution.PreUpdatePasswordActionRequestBuilder;
import org.wso2.carbon.identity.user.pre.update.password.action.core.execution.PreUpdatePasswordResponseProcessor;
import org.wso2.carbon.identity.user.pre.update.password.action.core.execution.UserPreUpdatePasswordActionExecutor;
import org.wso2.carbon.identity.user.pre.update.password.action.core.management.PreUpdatePasswordActionConverter;
import org.wso2.carbon.identity.user.pre.update.password.action.core.management.PreUpdatePasswordActionDTOModelResolver;
import org.wso2.carbon.identity.user.pre.update.password.action.core.rule.PreUpdatePasswordActionRuleEvaluationDataProvider;

/**
 * Service component for the Pre Update Password Action.
 */
@Component(
        name = "pre.update.password.action.service.component",
        immediate = true
)
public class PreUpdatePasswordActionServiceComponent {

    private static final Log LOG = LogFactory.getLog(PreUpdatePasswordActionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();

            bundleCtx.registerService(ActionConverter.class, new PreUpdatePasswordActionConverter(), null);
            LOG.debug("Pre Update Password Action Converter is enabled");

            bundleCtx.registerService(ActionDTOModelResolver.class, new PreUpdatePasswordActionDTOModelResolver(),
                    null);
            LOG.debug("Pre Update Password Action DTO Model Resolver is enabled");

            bundleCtx.registerService(ActionExecutionRequestBuilder.class, new PreUpdatePasswordActionRequestBuilder(),
                    null);
            LOG.debug("Pre Update Password Action Request Builder is enabled");

            bundleCtx.registerService(ActionExecutionResponseProcessor.class, new PreUpdatePasswordResponseProcessor(),
                    null);
            LOG.debug("Pre Update Password Action Response Processor is enabled");

            bundleCtx.registerService(UserActionExecutor.class, new UserPreUpdatePasswordActionExecutor(), null);
            LOG.debug("User Pre Update Password Action Executor is enabled");

            bundleCtx.registerService(RuleEvaluationDataProvider.class,
                    new PreUpdatePasswordActionRuleEvaluationDataProvider(), null);
            LOG.debug("User Pre Update Password Action Rule Evaluation Data Provider is enabled");

            LOG.debug("Pre Update Password Action bundle is activated");
        } catch (Throwable e) {
            LOG.error("Error while initializing Pre Update Password Action service component.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Pre Update Password Action bundle is deactivated");
    }

    @Reference(
            name = "org.wso2.carbon.identity.certificate.management",
            service = CertificateManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCertificateManagementService"
    )
    private void setCertificateManagementService(CertificateManagementService certificateManagementService) {

        PreUpdatePasswordActionServiceComponentHolder.getInstance()
                .setCertificateManagementService(certificateManagementService);
        LOG.debug("CertificateManagementService set in Pre Update Password Action bundle.");
    }

    private void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setCertificateManagementService(null);
        LOG.debug("CertificateManagementService unset in PreUpdatePasswordActionServiceComponentHolder bundle.");
    }

    @Reference(
            name = "action.execution.service",
            service = ActionExecutorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutorService"
    )
    protected void setActionExecutionService(ActionExecutorService actionExecutorService) {

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setActionExecutorService(actionExecutorService);
        LOG.debug("ActionExecutorService set in PreUpdatePasswordActionServiceComponentHolder bundle.");
    }

    protected void unsetActionExecutorService(ActionExecutorService actionExecutorService) {

        PreUpdatePasswordActionServiceComponentHolder.getInstance().setActionExecutorService(null);
        LOG.debug("ActionExecutorService unset in PreUpdatePasswordActionServiceComponentHolder bundle.");
    }
}
