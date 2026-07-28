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
import org.wso2.carbon.identity.application.authentication.framework.device.DeviceDataResolver;
import org.wso2.carbon.identity.device.policy.internal.jwt.DeviceTokenExtractor;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementClientException;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Resolves verified device data from the initiation request.
 * The device token is read from the {@code device_token} query parameter first (redirect flows),
 * then from the {@code X-Device-Token} header (app native / headless flows). The token is verified
 * via {@link DeviceTokenExtractor} and the resulting claims are returned as the device payload.
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
            return Optional.of(deviceData);
        } catch (PolicyManagementClientException e) {
            return Optional.empty();
        } catch (PolicyManagementException e) {
            LOG.error("Error while verifying device token at initiation.", e);
            return Optional.empty();
        }
    }
}
