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

package org.wso2.carbon.identity.device.registration;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.registration.internal.DeviceRegistrationExecutorDataHolder;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;

/**
 * Persists a verified device registration to the database once the registration flow is COMPLETE.
 *
 * During a REGISTRATION flow, DeviceRegistrationExecutor verifies the ECDSA challenge-response
 * (Phase 2) but defers the DB write because UserProvisioningExecutor has not yet run and
 * FlowUser.getUserId() is null at that point. This listener fires after UserProvisioningExecutor
 * sets the real userId, reads the verified Device from the flow context, rebinds it to
 * the provisioned userId, and calls DeviceManagementService.persistDevice().
 */
public class RegistrationFlowCompletionListener extends AbstractFlowExecutionListener {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowCompletionListener.class);

    @Override
    public int getExecutionOrderId() {
        return 10;
    }

    @Override
    public int getDefaultOrderId() {
        return 10;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean doPostExecute(FlowExecutionStep step, FlowExecutionContext context)
            throws FlowEngineException {

        if (!FlowTypes.REGISTRATION.getType().equals(context.getFlowType())) {
            return true;
        }
        if (!STATUS_COMPLETE.equals(step.getFlowStatus())) {
            return true;
        }

        Object deviceObj = context.getProperty(DeviceRegistrationExecutorConstants.CTX_DEVICE_REGISTRATION);
        if (deviceObj == null) {
            return true;
        }

        Device pending = (Device) deviceObj;
        String userId = context.getFlowUser().getUserId();

        if (StringUtils.isBlank(userId)) {
            LOG.error("Cannot persist device registration: userId is blank after flow completion for user: "
                    + LoggerUtils.getMaskedContent(context.getFlowUser().getUsername())
                    + " contextId: " + context.getContextIdentifier());
            return true;
        }

        Device deviceWithUserId = new Device.Builder(pending).userId(userId).build();

        DeviceManagementService service =
                DeviceRegistrationExecutorDataHolder.getInstance().getDeviceManagementService();
        try {
            service.persistDevice(deviceWithUserId, context.getTenantDomain());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device persisted for userId: " + userId
                        + " deviceId: " + pending.getId()
                        + " tenant: " + context.getTenantDomain());
            }
        } catch (DeviceMgtException e) {
            LOG.error("Failed to persist device registration for userId: " + userId
                    + " deviceId: " + pending.getId(), e);
        }
        return true;
    }
}
