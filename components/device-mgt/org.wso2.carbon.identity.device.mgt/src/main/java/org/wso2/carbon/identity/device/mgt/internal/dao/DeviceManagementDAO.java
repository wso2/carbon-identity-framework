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

package org.wso2.carbon.identity.device.mgt.internal.dao;

import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;

import java.util.List;

/**
 * DAO contract for device registration.
 */
public interface DeviceManagementDAO {

    /**
     * Registers a new device.
     *
     * @param device      Device to register.
     * @param association Ownership association for the device.
     * @param tenantId    Tenant identifier.
     * @return Registered device.
     * @throws DeviceMgtException If registration fails.
     */
    Device registerDevice(Device device, DeviceAssociation association, int tenantId)
            throws DeviceMgtException;

    /**
     * Finds a device by id.
     *
     * @param deviceId Device identifier.
     * @param tenantId Tenant identifier.
     * @return Device or {@code null}.
     * @throws DeviceMgtException If retrieval fails.
     */
    Device getDeviceById(String deviceId, int tenantId)
            throws DeviceMgtException;

    /**
     * Finds a page of devices registered in the tenant, ordered by registration time (newest first).
     * Returns devices of any status (ACTIVE or INACTIVE).
     *
     * @param tenantId Tenant identifier.
     * @param offset   Number of records to skip.
     * @param limit    Maximum number of records to return.
     * @return Page of devices in the tenant.
     * @throws DeviceMgtException If retrieval fails.
     */
    List<Device> getDevices(int tenantId, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Finds a page of devices registered by a user, ordered by registration time (newest first).
     * This is an admin/tenant-scoped listing that returns devices of any status (ACTIVE or
     * INACTIVE).
     *
     * @param userId   User identifier.
     * @param tenantId Tenant identifier.
     * @param offset   Number of records to skip.
     * @param limit    Maximum number of records to return.
     * @return Page of the user's devices in the tenant.
     * @throws DeviceMgtException If retrieval fails.
     */
    List<Device> getDevicesByUserId(String userId, int tenantId, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Counts all devices registered in the tenant, regardless of status (ACTIVE or INACTIVE).
     *
     * @param tenantId Tenant identifier.
     * @return Total number of devices in the tenant.
     * @throws DeviceMgtException If the count fails.
     */
    int getDeviceCount(int tenantId)
            throws DeviceMgtException;

    /**
     * Counts devices registered by a user, regardless of status (ACTIVE or INACTIVE). This is an
     * admin/tenant-scoped count of a single user's devices.
     *
     * @param userId   User identifier.
     * @param tenantId Tenant identifier.
     * @return Total number of matching devices for the user.
     * @throws DeviceMgtException If the count fails.
     */
    int getDeviceCountByUserId(String userId, int tenantId)
            throws DeviceMgtException;

    /**
     * Updates the name of a device.
     *
     * @param deviceId Device identifier.
     * @param deviceName Device name.
     * @param tenantId Tenant identifier.
     * @return Updated device.
     * @throws DeviceMgtException If update fails.
     */
    Device updateDeviceName(String deviceId, String deviceName, int tenantId)
            throws DeviceMgtException;

    /**
     * Updates the status of a device and returns the updated record.
     *
     * @param deviceId Device identifier.
     * @param status   New status for the device.
     * @param tenantId Tenant identifier.
     * @return Updated device.
     * @throws DeviceMgtException If the status update fails.
     */
    Device changeDeviceStatus(String deviceId, Device.Status status, int tenantId)
            throws DeviceMgtException;

    /**
     * Deletes a device.
     *
     * @param deviceId Device identifier.
     * @param tenantId Tenant identifier.
     * @throws DeviceMgtException If deletion fails.
     */
    void deleteDevice(String deviceId, int tenantId)
            throws DeviceMgtException;

    /**
     * Deletes all devices registered by a user in the tenant.
     *
     * @param userId   User identifier.
     * @param tenantId Tenant identifier.
     * @throws DeviceMgtException If deletion fails.
     */
    void deleteDevicesByUserId(String userId, int tenantId)
            throws DeviceMgtException;
}
