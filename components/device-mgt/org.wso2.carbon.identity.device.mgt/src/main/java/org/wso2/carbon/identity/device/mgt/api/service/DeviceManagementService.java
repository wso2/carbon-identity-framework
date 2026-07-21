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

package org.wso2.carbon.identity.device.mgt.api.service;

import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;

import java.util.List;

/**
 * Service interface for device management operations.
 */
public interface DeviceManagementService {

    /**
     * Registers a pre-verified {@link Device} in the database.
     *
     * @param device       The verified device to register.
     * @param tenantDomain Tenant domain.
     */
    void registerDevice(Device device, String tenantDomain) throws DeviceMgtException;

    /**
     * Retrieves a device by its UUID. Returns the device regardless of its status (ACTIVE or
     * INACTIVE) — this is an admin/tenant-scoped lookup, not a filtered "my devices" view.
     * Do not use this to decide whether a device should be trusted for authentication — a
     * deactivated (revoked) device is still returned. Use {@link #getActiveDeviceById} for that.
     *
     * @param deviceId     UUID of the device (IDN_DEVICE.ID).
     * @param tenantDomain Tenant domain.
     * @return The Device, or null if not found.
     */
    Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a device by its UUID, but only if its status is {@link Device.Status#ACTIVE}.
     * Returns {@code null} both when the device does not exist and when it exists but has been
     * deactivated (revoked) — callers that use the result to decide whether to trust a device
     * (e.g. token/signature validation) must not be able to distinguish the two cases from the
     * return value alone, since doing so would leak whether a given device id was ever registered.
     * This is the method authentication/authorization paths must use; {@link #getDeviceById} is
     * for management/admin views where an inactive device should still be visible.
     *
     * @param deviceId     UUID of the device (IDN_DEVICE.ID).
     * @param tenantDomain Tenant domain.
     * @return The Device if it exists and is ACTIVE; {@code null} otherwise.
     */
    Device getActiveDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves all ACTIVE devices registered by a user. This is the user-facing "my devices"
     * list: devices deactivated via {@link #deactivateDevice(String, String)} are excluded.
     *
     * @param userId       WSO2 user identifier.
     * @param tenantDomain Tenant domain.
     * @return List of active Device objects. Empty list if none found.
     */
    List<Device> getDevicesByUserId(String userId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices registered in the tenant, ordered by registration time (newest
     * first). Returns devices of any status (ACTIVE or INACTIVE) — this is an admin/tenant-wide
     * view, not filtered like {@link #getDevicesByUserId}.
     *
     * @param tenantDomain Tenant domain.
     * @param offset       Number of records to skip.
     * @param limit        Maximum number of records to return.
     * @return Page of Device objects. Empty list if none found.
     */
    List<Device> getDevices(String tenantDomain, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices registered in the tenant, ordered by registration time (newest
     * first), optionally filtered to a single user's devices. This is an admin view: it returns
     * devices of any status (ACTIVE or INACTIVE), unlike {@link #getDevicesByUserId}, which is the
     * user-facing "my devices" list restricted to ACTIVE devices. Do not use
     * {@link #getDevicesByUserId} in place of this method for admin views — it silently drops
     * INACTIVE devices.
     *
     * @param tenantDomain Tenant domain.
     * @param offset       Number of records to skip.
     * @param limit        Maximum number of records to return.
     * @param userId       WSO2 user identifier to filter by, or {@code null}/blank for no filtering.
     * @return Page of Device objects. Empty list if none found.
     */
    List<Device> getDevices(String tenantDomain, int offset, int limit, String userId)
            throws DeviceMgtException;

    /**
     * Counts all devices registered in the tenant, regardless of status (ACTIVE or INACTIVE) —
     * this is an admin/tenant-wide count, not filtered like {@link #getDevicesByUserId}.
     *
     * @param tenantDomain Tenant domain.
     * @return Total number of devices in the tenant.
     */
    int getDeviceCount(String tenantDomain)
            throws DeviceMgtException;

    /**
     * Counts devices registered in the tenant, optionally filtered to a single user, regardless of
     * status (ACTIVE or INACTIVE). This is an admin/tenant-wide count, not filtered like
     * {@link #getDevicesByUserId}, which only counts ACTIVE devices for the user-facing view.
     *
     * @param tenantDomain Tenant domain.
     * @param userId       WSO2 user identifier to filter by, or {@code null}/blank for no filtering.
     * @return Total number of matching devices in the tenant.
     */
    int getDeviceCount(String tenantDomain, String userId)
            throws DeviceMgtException;

    /**
     * Updates the display name of a device.
     *
     * @param deviceId     UUID of the device.
     * @param deviceName   New name for the device.
     * @param tenantDomain Tenant domain.
     * @return The updated Device.
     */
    Device updateDeviceName(String deviceId, String deviceName, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Activates a device, setting its status to {@link Device.Status#ACTIVE}. An activated device
     * reappears in {@link #getDevicesByUserId(String, String)} results.
     *
     * @param deviceId     UUID of the device.
     * @param tenantDomain Tenant domain.
     * @return The updated Device.
     * @throws DeviceMgtException If the device does not exist or the update fails.
     */
    Device activateDevice(String deviceId, String tenantDomain) throws DeviceMgtException;

    /**
     * Deactivates a device, setting its status to {@link Device.Status#INACTIVE}. A deactivated
     * device is excluded from {@link #getDevicesByUserId(String, String)} results, but remains
     * visible via {@link #getDeviceById} and {@link #getDevices}.
     *
     * @param deviceId     UUID of the device.
     * @param tenantDomain Tenant domain.
     * @return The updated Device.
     * @throws DeviceMgtException If the device does not exist or the update fails.
     */
    Device deactivateDevice(String deviceId, String tenantDomain) throws DeviceMgtException;

    /**
     * Deletes (hard delete) a device registration record.
     *
     * @param deviceId     UUID of the device.
     * @param tenantDomain Tenant domain.
     */
    void deleteDevice(String deviceId, String tenantDomain)
            throws DeviceMgtException;
}
