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

package org.wso2.carbon.identity.device.registration.internal.component;

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
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.service.DevicePolicyEvaluator;
import org.wso2.carbon.identity.device.policy.api.service.DeviceTokenVerifier;
import org.wso2.carbon.identity.device.registration.executor.DeviceRegistrationExecutor;
import org.wso2.carbon.identity.device.registration.listener.RegistrationFlowCompletionListener;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;

/**
 * OSGi DS component for the device registration executor bundle.
 *
 * On activation it registers {@link DeviceRegistrationExecutor} as an {@link Executor} OSGi
 * service. The flow execution engine's ServiceComponent picks it up automatically via its
 * MULTIPLE/DYNAMIC @Reference binding for Executor.class and adds it to the executor registry
 * keyed by the executor name defined in DeviceRegistrationConstants.
 */
@Component(
        name = "device.registration.component",
        immediate = true
)
public class DeviceRegistrationServiceComponent {

    private static final Log LOG = LogFactory.getLog(DeviceRegistrationServiceComponent.class);

    @Activate
    protected void activate(ComponentContext context) {

        try {
            BundleContext bundleCtx = context.getBundleContext();
            bundleCtx.registerService(
                    Executor.class.getName(),
                    new DeviceRegistrationExecutor(),
                    null);
            bundleCtx.registerService(
                    FlowExecutionListener.class.getName(),
                    new RegistrationFlowCompletionListener(),
                    null);
            LOG.debug("Device registration executor bundle activated.");
        } catch (Throwable e) {
            LOG.error("Error while activating device registration executor bundle.", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        LOG.debug("Device registration executor bundle deactivated.");
    }

    @Reference(
            name = "DeviceManagementService",
            service = DeviceManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementService"
    )
    protected void setDeviceManagementService(DeviceManagementService deviceManagementService) {

        LOG.debug("Setting DeviceManagementService in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance()
                .setDeviceManagementService(deviceManagementService);
    }

    protected void unsetDeviceManagementService(DeviceManagementService deviceManagementService) {

        LOG.debug("Unsetting DeviceManagementService in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance().setDeviceManagementService(null);
    }

    @Reference(
            name = "DevicePolicyEvaluator",
            service = DevicePolicyEvaluator.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDevicePolicyEvaluator"
    )
    protected void setDevicePolicyEvaluator(DevicePolicyEvaluator devicePolicyEvaluator) {

        LOG.debug("Setting DevicePolicyEvaluator in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance()
                .setDevicePolicyEvaluator(devicePolicyEvaluator);
    }

    protected void unsetDevicePolicyEvaluator(DevicePolicyEvaluator devicePolicyEvaluator) {

        LOG.debug("Unsetting DevicePolicyEvaluator in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance().setDevicePolicyEvaluator(null);
    }

    @Reference(
            name = "DeviceTokenVerifier",
            service = DeviceTokenVerifier.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceTokenVerifier"
    )
    protected void setDeviceTokenVerifier(DeviceTokenVerifier deviceTokenVerifier) {

        LOG.debug("Setting DeviceTokenVerifier in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance().setDeviceTokenVerifier(deviceTokenVerifier);
    }

    protected void unsetDeviceTokenVerifier(DeviceTokenVerifier deviceTokenVerifier) {

        LOG.debug("Unsetting DeviceTokenVerifier in the device registration executor.");
        DeviceRegistrationComponentServiceHolder.getInstance().setDeviceTokenVerifier(null);
    }
}
