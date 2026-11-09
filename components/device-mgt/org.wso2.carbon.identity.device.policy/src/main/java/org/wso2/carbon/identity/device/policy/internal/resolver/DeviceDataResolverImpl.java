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

package org.wso2.carbon.identity.device.policy.internal.resolver;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.handler.device.DeviceDataResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.device.ResolvedDeviceData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves verified device data from the device token of the initiation request.
 * The token is read from the {@code X-Device-Token} header, then the {@code device_token} parameter, and its
 * single use {@code jti} is consumed here, so callers must mint a fresh token on every initiation attempt.
 * The {@code x-client-attestation} header, if present, is added to the device data for {@code IntegrityDataEnricher}.
 */
public class DeviceDataResolverImpl implements DeviceDataResolver {

    private static final Log LOG = LogFactory.getLog(DeviceDataResolverImpl.class);

    @Override
    public Optional<ResolvedDeviceData> resolve(HttpServletRequest request) {

        if (request == null) {
            return Optional.empty();
        }

        // The framework gates the resolution on the same transport, hence the shared constants.
        String deviceToken = request.getHeader(FrameworkConstants.DEVICE_TOKEN_HEADER);
        if (StringUtils.isBlank(deviceToken)) {
            deviceToken = request.getParameter(FrameworkConstants.DEVICE_TOKEN_PARAM);
        }
        if (StringUtils.isBlank(deviceToken)) {
            return Optional.empty();
        }

        try {
            DeviceTokenExtractor.VerifiedDeviceToken verifiedToken = new DeviceTokenExtractor().extract(deviceToken);
            Map<String, Object> deviceData = verifiedToken.getDeviceData();

            String attestationHeader = request.getHeader(Constants.ATTESTATION_HEADER);
            if (StringUtils.isNotBlank(attestationHeader)) {
                deviceData.put(DeviceTokenConstants.ATTESTATION_TOKEN_KEY, attestationHeader);
            }

            return Optional.of(new ResolvedDeviceData(verifiedToken.getDeviceId(), verifiedToken.getOwnerUserId(),
                    verifiedToken.getTenantDomain(), deviceData));
        } catch (DevicePolicyClientException e) {
            return Optional.empty();
        } catch (DevicePolicyException e) {
            LOG.error("Error while verifying device token at initiation.");
            return Optional.empty();
        }
    }

    @Override
    public boolean isValidDevice(String deviceId, String userId, String tenantDomain) {

        if (StringUtils.isBlank(deviceId) || StringUtils.isBlank(userId) || StringUtils.isBlank(tenantDomain)) {
            return false;
        }
        try {
            // Cache backed lookup, so activation changes made at any time are honoured.
            Device device = DevicePolicyComponentServiceHolder.getInstance().getDeviceManagementService()
                    .getDeviceById(deviceId, tenantDomain);
            boolean valid = device != null && userId.equals(device.getUserId())
                    && device.getStatus() == Device.Status.ACTIVE;
            if (!valid && LOG.isDebugEnabled()) {
                LOG.debug("Device: " + deviceId + " is not active or not owned by the user in the tenant: "
                        + tenantDomain);
            }
            return valid;
        } catch (DeviceMgtException e) {
            LOG.error("Error while validating the device: " + deviceId, e);
            return false;
        }
    }
}
