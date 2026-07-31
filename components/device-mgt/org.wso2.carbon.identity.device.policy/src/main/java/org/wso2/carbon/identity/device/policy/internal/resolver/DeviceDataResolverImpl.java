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
import org.wso2.carbon.identity.client.attestation.mgt.utils.Constants;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyException;
import org.wso2.carbon.identity.device.policy.api.service.DeviceDataResolver;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves verified device data from the initiation request.
 * The device token is read from the {@code device_token} query parameter first (redirect flows),
 * then from the {@code X-Device-Token} header (app native / headless flows). The token is verified
 * via {@link DeviceTokenExtractor} and the resulting claims are returned as the device payload.
 *
 * <p>If the request carries the {@code x-client-attestation} header (the same header already
 * required elsewhere in the login path for client attestation), its value is copied into the
 * resolved device data under the attestation token key so {@code IntegrityDataEnricher} can use
 * it — the caller does not need to also embed the attestation token a second time inside the
 * device-data JWT payload.
 *
 * <p>The token's {@code jti} is single-use and consumed here at initiation (see
 * {@link DeviceTokenExtractor}'s replay protection). A caller that re-submits the same
 * {@code device_token} on a retry or page refresh will have it rejected as a replay and this
 * method silently returns {@link Optional#empty()} — callers MUST mint a fresh device token on
 * every initiation attempt, including retries and refreshes; the same token cannot be reused.
 */
public class DeviceDataResolverImpl implements DeviceDataResolver {

    private static final Log LOG = LogFactory.getLog(DeviceDataResolverImpl.class);
    private static final String DEVICE_TOKEN_PARAM = "device_token";
    private static final String DEVICE_TOKEN_HEADER = "X-Device-Token";

    @Override
    public Optional<Map<String, Object>> resolveDeviceData(HttpServletRequest request, String tenantDomain) {

        if (request == null) {
            return Optional.empty();
        }

        String deviceToken = request.getParameter(DEVICE_TOKEN_PARAM);
        if (StringUtils.isBlank(deviceToken)) {
            deviceToken = request.getHeader(DEVICE_TOKEN_HEADER);
        }
        if (StringUtils.isBlank(deviceToken)) {
            return Optional.empty();
        }

        try {
            Map<String, Object> deviceData = new DeviceTokenExtractor().extractFromToken(deviceToken, tenantDomain);

            String attestationHeader = request.getHeader(Constants.ATTESTATION_HEADER);
            if (StringUtils.isNotBlank(attestationHeader)) {
                deviceData.put(DeviceTokenConstants.ATTESTATION_TOKEN_KEY, attestationHeader);
            }

            return Optional.of(deviceData);
        } catch (DevicePolicyClientException e) {
            return Optional.empty();
        } catch (DevicePolicyException e) {
            LOG.error("Error while verifying device token at initiation.");
            return Optional.empty();
        }
    }
}
