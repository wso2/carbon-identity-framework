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

package org.wso2.carbon.identity.device.registration.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.registration.internal.component.DeviceRegistrationComponentServiceHolder;
import org.wso2.carbon.identity.device.registration.internal.constant.DeviceRegistrationConstants;
import org.wso2.carbon.identity.device.registration.internal.util.DeviceRegistrationDiagnosticLogger;
import org.wso2.carbon.identity.device.registration.model.VerifiedDevice;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.listener.AbstractFlowExecutionListener;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;

import java.sql.Timestamp;
import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;

/**
 * Persists a verified device registration once the whole flow reaches STATUS_COMPLETE, for every
 * flow type. Deferred here because a userId is not guaranteed to be available on FlowUser until
 * the flow completes (e.g. a brand-new user is only provisioned later in a REGISTRATION flow) —
 * this also means a later step failing never leaves a device that needs rolling back.
 */
public class RegistrationFlowCompletionListener extends AbstractFlowExecutionListener {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowCompletionListener.class);

    private final DeviceRegistrationDiagnosticLogger diagnosticLogger = new DeviceRegistrationDiagnosticLogger();

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

        if (!STATUS_COMPLETE.equals(step.getFlowStatus())) {
            return true;
        }

        Object deviceObj = context.getProperty(DeviceRegistrationConstants.CTX_DEVICE_REGISTRATION);
        if (deviceObj == null) {
            return true;
        }

        VerifiedDevice pending = toVerifiedDevice(deviceObj);
        if (pending == null) {
            LOG.error("Cannot register device: the verified device in the flow context is invalid. contextId: "
                    + context.getContextIdentifier());
            return true;
        }
        String userId = context.getFlowUser().getUserId();

        if (StringUtils.isBlank(userId)) {
            // Some executors, such as UserResolveExecutor, never populate the dedicated userId
            // field and only add claims to FlowUser. Fall back to the userid claim before giving up.
            Object userIdClaim = context.getFlowUser().getClaim(DeviceRegistrationConstants.CLAIM_USER_ID);
            if (userIdClaim != null) {
                userId = String.valueOf(userIdClaim);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resolved userId from userid claim for contextId: "
                            + context.getContextIdentifier());
                }
            }
        }

        if (StringUtils.isBlank(userId)) {
            LOG.error("Cannot register device: userId is blank after flow completion for user: "
                    + LoggerUtils.getMaskedContent(context.getFlowUser().getUsername())
                    + " contextId: " + context.getContextIdentifier());
            return true;
        }

        DeviceManagementService service =
                DeviceRegistrationComponentServiceHolder.getInstance().getDeviceManagementService();
        try {
            Device registeredDevice = service.registerDevice(pending.bindTo(userId),
                    new UserDeviceAssociation(pending.getId(), userId), context.getTenantDomain());
            String registeredDeviceId = registeredDevice != null ? registeredDevice.getId() : pending.getId();
            diagnosticLogger.logRegistrationCompleted(registeredDeviceId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device registered for userId: " + userId
                        + " deviceId: " + registeredDeviceId
                        + " tenant: " + context.getTenantDomain());
            }
        } catch (DeviceMgtException e) {
            diagnosticLogger.logRegistrationFailure("Failed to register device: " + e.getMessage());
            LOG.error("Failed to register device for userId: " + userId
                    + " deviceId: " + pending.getId(), e);
        }
        return true;
    }

    /**
     * Read the verified device from the flow context, which holds a map instead once the context is stored.
     *
     * @param deviceObj Value of the verified device in the flow context.
     * @return The verified device, or null if invalid.
     */
    private VerifiedDevice toVerifiedDevice(Object deviceObj) {

        if (deviceObj instanceof VerifiedDevice) {
            return (VerifiedDevice) deviceObj;
        }
        if (!(deviceObj instanceof Map)) {
            return null;
        }
        Map<?, ?> fields = (Map<?, ?>) deviceObj;
        Object registeredAt = fields.get("registeredAt");
        try {
            return new VerifiedDevice.Builder()
                    .id(asString(fields.get("id")))
                    .deviceName(asString(fields.get("deviceName")))
                    .deviceModel(asString(fields.get("deviceModel")))
                    .publicKey(asString(fields.get("publicKey")))
                    .registeredAt(registeredAt instanceof Number ?
                            new Timestamp(((Number) registeredAt).longValue()) : null)
                    .build();
        } catch (IllegalArgumentException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The verified device in the flow context is missing a required field.", e);
            }
            return null;
        }
    }

    private String asString(Object value) {

        return value == null ? null : String.valueOf(value);
    }
}
