/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.flow.extension.internal;

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
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.extension.executor.FlowExtensionExecutor;
import org.wso2.carbon.identity.flow.extension.executor.FlowExtensionRequestBuilder;
import org.wso2.carbon.identity.flow.extension.executor.FlowExtensionResponseProcessor;
import org.wso2.carbon.identity.flow.extension.management.FlowExtensionActionConverter;
import org.wso2.carbon.identity.flow.extension.management.FlowExtensionActionDTOModelResolver;

/**
 * OSGi declarative services component which registers the In-Flow Extension services.
 */
@Component(
        name = "flow.extension.component",
        immediate = true)
public class FlowExtensionServiceComponent {

    private static final Log LOG = LogFactory.getLog(FlowExtensionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();

            bundleContext.registerService(Executor.class.getName(), new FlowExtensionExecutor(), null);
            bundleContext.registerService(ActionExecutionRequestBuilder.class.getName(),
                    new FlowExtensionRequestBuilder(), null);
            bundleContext.registerService(ActionExecutionResponseProcessor.class.getName(),
                    new FlowExtensionResponseProcessor(), null);

            bundleContext.registerService(ActionConverter.class.getName(),
                    new FlowExtensionActionConverter(), null);
            bundleContext.registerService(ActionDTOModelResolver.class.getName(),
                    new FlowExtensionActionDTOModelResolver(
                            FlowExtensionDataHolder.getInstance().getCertificateManagementService()),
                    null);

            LOG.debug("In-Flow Extension service successfully activated.");
        } catch (Throwable e) {
            LOG.error("Error while initiating In-Flow Extension service", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("In-Flow Extension service successfully deactivated.");
    }

    @Reference(
            name = "ActionManagementService",
            service = ActionManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionManagementService"
    )
    protected void setActionManagementService(ActionManagementService actionManagementService) {

        LOG.debug("Setting the ActionManagementService in the In-Flow Extension component.");
        FlowExtensionDataHolder.getInstance().setActionManagementService(actionManagementService);
    }

    protected void unsetActionManagementService(ActionManagementService actionManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the ActionManagementService in the In-Flow Extension component. Service: "
                    + actionManagementService);
        }
        FlowExtensionDataHolder.getInstance().setActionManagementService(null);
    }

    @Reference(
            name = "ActionExecutorService",
            service = ActionExecutorService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetActionExecutorService"
    )
    protected void setActionExecutorService(ActionExecutorService actionExecutorService) {

        LOG.debug("Setting the ActionExecutorService in the In-Flow Extension component.");
        InFlowExtensionDataHolder.getInstance().setActionExecutorService(actionExecutorService);
    }

    protected void unsetActionExecutorService(ActionExecutorService actionExecutorService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the ActionExecutorService in the In-Flow Extension component. Service: "
                    + actionExecutorService);
        }
        FlowExtensionDataHolder.getInstance().setActionExecutorService(null);
    }

    @Reference(
            name = "CertificateManagementService",
            service = CertificateManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetCertificateManagementService"
    )
    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {

        LOG.debug("Setting the CertificateManagementService in the In-Flow Extension component.");
        FlowExtensionDataHolder.getInstance()
                .setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(
            CertificateManagementService certificateManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the CertificateManagementService in the In-Flow Extension component. Service: "
                    + certificateManagementService);
        }
        FlowExtensionDataHolder.getInstance().setCertificateManagementService(null);
    }

    @Reference(
            name = "ClaimMetadataManagementService",
            service = ClaimMetadataManagementService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetClaimMetadataManagementService"
    )
    protected void setClaimMetadataManagementService(
            ClaimMetadataManagementService claimMetadataManagementService) {

        LOG.debug("Setting the ClaimMetadataManagementService in the In-Flow Extension component.");
        FlowExtensionDataHolder.getInstance()
                .setClaimMetadataManagementService(claimMetadataManagementService);
    }

    protected void unsetClaimMetadataManagementService(
            ClaimMetadataManagementService claimMetadataManagementService) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsetting the ClaimMetadataManagementService in the In-Flow Extension component. Service: "
                    + claimMetadataManagementService);
        }
        FlowExtensionDataHolder.getInstance().setClaimMetadataManagementService(null);
    }
}
