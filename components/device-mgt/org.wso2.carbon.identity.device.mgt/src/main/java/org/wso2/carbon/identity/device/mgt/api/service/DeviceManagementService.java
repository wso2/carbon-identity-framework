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
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;

import java.util.List;

/**
 * Service interface for device management operations.
 */
public interface DeviceManagementService {

    /**
     * Registers a pre-verified {@link Device} in the database.
     *
     * @param device       The verified device to register.
     * @param association  The device-to-owner association; only UserDeviceAssociation is supported today.
     * @param tenantDomain Tenant domain.
     * @return Registered device.
     */
    Device registerDevice(Device device, DeviceAssociation association, String tenantDomain) throws DeviceMgtException;

    /**
     * Retrieves a device by its UUID. Returns the device regardless of its status (ACTIVE or
     * INACTIVE) — this is an admin/tenant-scoped lookup, not a filtered "my devices" view.
     *
     * @param deviceId     UUID of the device (IDN_DEVICE.ID).
     * @param tenantDomain Tenant domain.
     * @return The Device, or null if not found.
     */
    Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices registered in the tenant, ordered by registration time (newest
     * first). Returns devices of any status (ACTIVE or INACTIVE).
     *
     * @param tenantDomain Tenant domain.
     * @param offset       Number of records to skip.
     * @param limit        Maximum number of records to return.
     * @return Page of Device objects. Empty list if none found.
     */
    List<Device> getDevices(String tenantDomain, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices registered by a user, ordered by registration time (newest
     * first). Returns devices of any status (ACTIVE or INACTIVE) — this is an admin view of a
     * single user's devices.
     *
     * @param userId       WSO2 user identifier.
     * @param tenantDomain Tenant domain.
     * @param offset       Number of records to skip.
     * @param limit        Maximum number of records to return.
     * @return Page of Device objects. Empty list if none found.
     */
    List<Device> getDevicesByUserId(String userId, String tenantDomain, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Counts all devices registered in the tenant, regardless of status (ACTIVE or INACTIVE).
     *
     * @param tenantDomain Tenant domain.
     * @return Total number of devices in the tenant.
     */
    int getDeviceCount(String tenantDomain)
            throws DeviceMgtException;

    /**
     * Counts devices registered by a user, regardless of status (ACTIVE or INACTIVE) — this is
     * an admin count of a single user's devices.
     *
     * @param userId       WSO2 user identifier.
     * @param tenantDomain Tenant domain.
     * @return Total number of matching devices for the user.
     */
    int getDeviceCountByUserId(String userId, String tenantDomain)
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
     * Activates a device, setting its status to {@link Device.Status#ACTIVE}.
     *
     * @param deviceId     UUID of the device.
     * @param tenantDomain Tenant domain.
     * @return The updated Device.
     * @throws DeviceMgtException If the device does not exist or the update fails.
     */
    Device activateDevice(String deviceId, String tenantDomain) throws DeviceMgtException;

    /**
     * Deactivates a device, setting its status to {@link Device.Status#INACTIVE}.
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

    /**
     * Deletes (hard delete) all device registration records owned by a user. Intended for cleanup
     * when the user itself is deleted.
     *
     * @param userId       User identifier.
     * @param tenantDomain Tenant domain.
     * @throws DeviceMgtException If the deletion fails.
     */
    void deleteDevicesByUserId(String userId, String tenantDomain)
            throws DeviceMgtException;
}
