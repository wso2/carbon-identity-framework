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

package org.wso2.carbon.identity.application.authentication.framework.handler.device;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Extension point for resolving verified device data from an app native authentication initiation request.
 * An invalid device token must be returned as an empty optional; throwing fails the authentication flow.
 */
public interface DeviceDataResolver {

    /**
     * Resolves the device data of the device token on the request, in the tenant of the device.
     *
     * @param request The initial authentication request carrying the device token.
     * @return Verified device data, or empty if there is no valid device token.
     */
    Optional<ResolvedDeviceData> resolve(HttpServletRequest request);

    /**
     * Checks whether the device is still active and owned by the user in the tenant.
     *
     * @param deviceId     Device ID.
     * @param userId       User ID of the owner.
     * @param tenantDomain Tenant domain of the device.
     * @return True if the device data can still be used for the user.
     */
    boolean isValidDevice(String deviceId, String userId, String tenantDomain);
}
