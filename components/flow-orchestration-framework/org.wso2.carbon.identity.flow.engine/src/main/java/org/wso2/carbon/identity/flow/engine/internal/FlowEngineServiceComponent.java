/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.engine.internal;

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
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.flow.engine.FlowService;
import org.wso2.carbon.identity.flow.engine.graph.Executor;
import org.wso2.carbon.identity.flow.engine.graph.UserOnboardingExecutor;
import org.wso2.carbon.identity.flow.engine.listener.FlowListener;
import org.wso2.carbon.identity.flow.engine.validation.InputValidationListener;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Comparator;

/**
 * OSGi declarative services component which handles flow engine service.
 */
@Component(
        name = "flow.engine.component",
        immediate = true)
public class FlowEngineServiceComponent {

    private static final Log LOG = LogFactory.getLog(FlowEngineServiceComponent.class);
    private static final Comparator<FlowListener> listenerComparator =
            new Comparator<FlowListener>() {

                @Override
                public int compare(FlowListener flowListener1,
                                   FlowListener flowListener2) {

                    if (flowListener1.getExecutionOrderId() >
                            flowListener2.getExecutionOrderId()) {
                        return 1;
                    } else if (flowListener1.getExecutionOrderId() <
                            flowListener2.getExecutionOrderId()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            };

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleContext = context.getBundleContext();
            bundleContext.registerService(FlowService.class.getName(),
                    FlowService.getInstance(), null);
            bundleContext.registerService(Executor.class.getName(), new UserOnboardingExecutor(), null);
            bundleContext.registerService(FlowListener.class.getName(), new InputValidationListener(),
                    null);
            LOG.debug("Flow Engine service successfully activated.");
        } catch (Throwable e) {
            LOG.error("Error while initiating Flow Engine service", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.ungetService(bundleCtx.getServiceReference(FlowService.class));
            LOG.debug("Flow Engine service successfully deactivated");
        } catch (Throwable e) {
            LOG.error("Error while deactivating Flow Engine service.", e);
        }
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        LOG.debug("Setting the Realm Service in the User flow service component.");
        FlowEngineDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        LOG.debug("Unsetting the Realm Service in the User Flow Service component.");
        FlowEngineDataHolder.getInstance().setRealmService(null);
    }

    @Reference(
            name = "FlowMgtService",
            service = FlowMgtService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetFlowMgtService")
    protected void setFlowMgtService(FlowMgtService flowMgtService) {

        LOG.debug("Setting the Flow Management Service in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().setFlowMgtService(flowMgtService);
    }

    protected void unsetFlowMgtService(FlowMgtService flowMgtService) {

        LOG.debug("Unsetting the Flow Management Service in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().setFlowMgtService(null);
    }

    @Reference(
            name = "Executor",
            service = Executor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetExecutors")
    protected void setExecutors(Executor executor) {

        LOG.debug("Setting executor in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().getExecutors().put(executor.getName(), executor);
    }

    protected void unsetExecutors(Executor executor) {

        LOG.debug("Unsetting executor in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().getExecutors().remove(executor.getName());
    }

    @Reference(
            name = "InputValidationManagementService",
            service = InputValidationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetInputValidationManagementService")
    protected void setInputValidationManagementService(InputValidationManagementService inputValidationManagementService) {

        LOG.debug("Setting the Input Validation Mgt Service in the Flow Engine component.");
        FlowEngineDataHolder.getInstance()
                .setInputValidationManagementService(inputValidationManagementService);
    }

    protected void unsetInputValidationManagementService(InputValidationManagementService inputValidationManagementService) {

        LOG.debug("Unsetting the Input Validation Mgt Service in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().setInputValidationManagementService(null);
    }

    @Reference(
            service = ApplicationManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetApplicationManagement"
    )
    public void setApplicationManagement(ApplicationManagementService applicationManagement) {

        FlowEngineDataHolder.getInstance().setApplicationManagementService(applicationManagement);
    }

    public void unsetApplicationManagement(ApplicationManagementService applicationManagementService) {

        FlowEngineDataHolder.getInstance().setApplicationManagementService(null);
    }

    @Reference(
            name = "FlowListener",
            service = FlowListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetFlowExecutionListener")
    protected void setFlowExecutionListener(FlowListener flowExecutionListener) {

        LOG.debug("Setting the Flow Execution Listener in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().getFlowListeners()
                .add(flowExecutionListener);
        FlowEngineDataHolder.getInstance().getFlowListeners().sort(listenerComparator);
    }

    protected void unsetFlowExecutionListener(FlowListener flowExecutionListener) {

        LOG.debug("Unsetting the Flow Execution Listener in the Flow Engine component.");
        FlowEngineDataHolder.getInstance().getFlowListeners().remove(flowExecutionListener);
    }
}
