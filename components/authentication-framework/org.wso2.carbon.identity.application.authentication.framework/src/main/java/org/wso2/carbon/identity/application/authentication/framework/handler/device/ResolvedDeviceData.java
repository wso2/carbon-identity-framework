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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Device data verified at initiation, bound to the device owner and the tenant of the device.
 */
public class ResolvedDeviceData implements Serializable {

    private static final long serialVersionUID = -2394625378140725531L;

    private final String deviceId;
    private final String ownerUserId;
    private final String tenantDomain;
    private final HashMap<String, Object> deviceData;

    /**
     * Creates resolved device data.
     *
     * @param deviceId     Device ID.
     * @param ownerUserId  User ID of the device owner.
     * @param tenantDomain Tenant domain of the device.
     * @param deviceData   Verified device data.
     */
    public ResolvedDeviceData(String deviceId, String ownerUserId, String tenantDomain,
                              Map<String, Object> deviceData) {

        this.deviceId = deviceId;
        this.ownerUserId = ownerUserId;
        this.tenantDomain = tenantDomain;
        this.deviceData = deviceData != null ? new HashMap<>(deviceData) : new HashMap<>();
    }

    /**
     * Returns the device data if the user owns the device in the tenant and the device is still valid.
     *
     * @param userId       User ID of the current user.
     * @param tenantDomain Resident tenant domain of the current user.
     * @return Read only copy of the device data, or null if not available for the user.
     */
    public Map<String, Object> getDeviceData(String userId, String tenantDomain) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(tenantDomain) || !userId.equals(ownerUserId)
                || !tenantDomain.equalsIgnoreCase(this.tenantDomain)) {
            return null;
        }
        DeviceDataResolver deviceDataResolver = FrameworkServiceDataHolder.getInstance().getDeviceDataResolver();
        if (deviceDataResolver == null || !deviceDataResolver.isValidDevice(deviceId, ownerUserId, this.tenantDomain)) {
            return null;
        }
        return Collections.unmodifiableMap(new HashMap<>(deviceData));
    }
}
