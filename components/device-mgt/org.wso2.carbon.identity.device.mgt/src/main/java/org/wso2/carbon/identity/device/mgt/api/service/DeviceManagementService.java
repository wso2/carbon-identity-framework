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
import org.wso2.carbon.identity.device.mgt.api.model.TenantDevice;

import java.util.List;

/**
 * Service interface for device management operations.
 */
public interface DeviceManagementService {

    /**
     * Generates a new unique device identifier. Callers that need to construct a {@link Device}
     * (e.g. the device registration executor) must obtain the id from here rather than minting
     * their own, so device.mgt remains the single source of truth for the id format.
     *
     * @return A new unique device identifier.
     */
    String generateDeviceId();

    /**
     * Registers a pre-verified {@link Device} in the database.
     *
     * @param device       The verified device to register.
     * @param association  The device-to-owner association; only UserDeviceAssociation is supported today.
     * @param tenantDomain Tenant domain.
     * @return Registered device.
     * @throws DeviceMgtException If registration fails or the user is a shared user.
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
     * Retrieves a device by its UUID if it is owned by the user, giving a shared user its inherited device.
     *
     * @param deviceId       UUID of the device (IDN_DEVICE.ID).
     * @param residentUserId User ID of the user in its resident organization.
     * @param tenantDomain   Tenant domain of the request.
     * @return The device with the user's ID in the tenant, or null if the user has no such device.
     * @throws DeviceMgtException If the device or the user association cannot be retrieved.
     */
    Device getDeviceOfResidentUser(String deviceId, String residentUserId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a device by its UUID from the tenant it is registered in, regardless of its status.
     *
     * @param deviceId UUID of the device (IDN_DEVICE.ID).
     * @return The device with its tenant domain, or null if not found.
     * @throws DeviceMgtException If the device cannot be retrieved.
     */
    TenantDevice getTenantDeviceById(String deviceId) throws DeviceMgtException;

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
     * single user's devices. A shared user gets the devices of its resident user, with its own user ID.
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
     * an admin count of a single user's devices. A shared user's count is that of its resident user.
     *
     * @param userId       WSO2 user identifier.
     * @param tenantDomain Tenant domain.
     * @return Total number of matching devices for the user.
     */
    int getDeviceCountByUserId(String userId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices of the user, ordered by registration time (newest first), giving a shared user
     * its inherited devices.
     *
     * @param residentUserId User ID of the user in its resident organization.
     * @param tenantDomain   Tenant domain of the request.
     * @param offset         Number of records to skip.
     * @param limit          Maximum number of records to return.
     * @return Page of devices with the user's ID in the tenant. Empty list if none found.
     * @throws DeviceMgtException If the devices or the user association cannot be retrieved.
     */
    List<Device> getDevicesOfResidentUser(String residentUserId, String tenantDomain, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Counts the devices of the user, counting the inherited devices of a shared user.
     *
     * @param residentUserId User ID of the user in its resident organization.
     * @param tenantDomain   Tenant domain of the request.
     * @return Total number of devices of the user.
     * @throws DeviceMgtException If the devices or the user association cannot be retrieved.
     */
    int getDeviceCountOfResidentUser(String residentUserId, String tenantDomain)
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
