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

package org.wso2.carbon.identity.device.mgt.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCache;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCacheEntry;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCacheKey;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;

import java.util.List;

/**
 * Cache backed Device Management DAO.
 * This class implements the caching on top of the data layer operations.
 * This caches the Device object, keyed by device ID and tenant. List/aggregate reads
 * (getDevicesByUserId, getDevices, getDeviceCount) are intentionally not cached since
 * invalidating a list on every write is a different, harder problem.
 */
public class CacheBackedDeviceManagementDAO implements DeviceManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedDeviceManagementDAO.class);

    private final DeviceManagementDAO deviceManagementDAO;
    private final DeviceCache deviceCache;

    /**
     * Wraps the given DAO with a read-through, write-invalidate cache.
     *
     * @param deviceManagementDAO The underlying DAO to delegate to.
     */
    public CacheBackedDeviceManagementDAO(DeviceManagementDAO deviceManagementDAO) {

        this.deviceManagementDAO = deviceManagementDAO;
        deviceCache = DeviceCache.getInstance();
    }

    /**
     * Persists a new device.
     * This method directly invokes the data layer operation — a brand-new device has nothing
     * to invalidate in the cache.
     *
     * @param device   Device to persist.
     * @param tenantId Tenant identifier.
     * @return Persisted device.
     * @throws DeviceMgtException If persistence fails.
     */
    @Override
    public Device registerDevice(Device device, int tenantId) throws DeviceMgtException {

        return deviceManagementDAO.registerDevice(device, tenantId);
    }

    /**
     * Finds a device by id.
     * This method first checks the cache for the Device object. If the Device object is not
     * found in the cache, it invokes the data layer operation to get the Device and, if found,
     * adds it to the cache.
     *
     * @param deviceId Device identifier.
     * @param tenantId Tenant identifier.
     * @return Device or {@code null}.
     * @throws DeviceMgtException If retrieval fails.
     */
    @Override
    public Device getDeviceById(String deviceId, int tenantId) throws DeviceMgtException {

        DeviceCacheKey cacheKey = new DeviceCacheKey(deviceId);
        DeviceCacheEntry cacheEntry = deviceCache.getValueFromCache(cacheKey, tenantId);
        if (cacheEntry != null && cacheEntry.getDevice() != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device cache hit for device id: " + deviceId + ". Returning from cache.");
            }
            return cacheEntry.getDevice();
        }

        Device device = deviceManagementDAO.getDeviceById(deviceId, tenantId);
        if (device != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Device cache miss for device id: " + deviceId + ". Adding to cache.");
            }
            deviceCache.addToCacheOnRead(cacheKey, new DeviceCacheEntry(device), tenantId);
        }
        return device;
    }

    /**
     * Finds all active devices by user id.
     * This method is not cached; list results are not invalidated entry-by-entry.
     *
     * @param userId   User identifier.
     * @param tenantId Tenant identifier.
     * @return Active devices.
     * @throws DeviceMgtException If retrieval fails.
     */
    @Override
    public List<Device> getDevicesByUserId(String userId, int tenantId) throws DeviceMgtException {

        return deviceManagementDAO.getDevicesByUserId(userId, tenantId);
    }

    /**
     * Finds a page of devices registered in the tenant, ordered by registration time (newest first).
     * This method is not cached; list results are not invalidated entry-by-entry.
     *
     * @param tenantId Tenant identifier.
     * @param offset   Number of records to skip.
     * @param limit    Maximum number of records to return.
     * @return Page of devices in the tenant.
     * @throws DeviceMgtException If retrieval fails.
     */
    @Override
    public List<Device> getDevices(int tenantId, int offset, int limit) throws DeviceMgtException {

        return deviceManagementDAO.getDevices(tenantId, offset, limit);
    }

    /**
     * Finds a page of devices registered in the tenant, optionally filtered to a single user.
     * This method is not cached; list results are not invalidated entry-by-entry.
     *
     * @param tenantId Tenant identifier.
     * @param offset   Number of records to skip.
     * @param limit    Maximum number of records to return.
     * @param userId   User identifier to filter by, or {@code null}/blank for no filtering.
     * @return Page of devices in the tenant.
     * @throws DeviceMgtException If retrieval fails.
     */
    @Override
    public List<Device> getDevices(int tenantId, int offset, int limit, String userId) throws DeviceMgtException {

        return deviceManagementDAO.getDevices(tenantId, offset, limit, userId);
    }

    /**
     * Counts all devices registered in the tenant.
     * This method is not cached.
     *
     * @param tenantId Tenant identifier.
     * @return Total number of devices in the tenant.
     * @throws DeviceMgtException If the count fails.
     */
    @Override
    public int getDeviceCount(int tenantId) throws DeviceMgtException {

        return deviceManagementDAO.getDeviceCount(tenantId);
    }

    /**
     * Counts devices registered in the tenant, optionally filtered to a single user.
     * This method is not cached.
     *
     * @param tenantId Tenant identifier.
     * @param userId   User identifier to filter by, or {@code null}/blank for no filtering.
     * @return Total number of matching devices in the tenant.
     * @throws DeviceMgtException If the count fails.
     */
    @Override
    public int getDeviceCount(int tenantId, String userId) throws DeviceMgtException {

        return deviceManagementDAO.getDeviceCount(tenantId, userId);
    }

    /**
     * Updates the name of a device.
     * This method clears the cache entry upon device name update.
     *
     * @param deviceId   Device identifier.
     * @param deviceName Device name.
     * @param tenantId   Tenant identifier.
     * @return Updated device.
     * @throws DeviceMgtException If update fails.
     */
    @Override
    public Device updateDeviceName(String deviceId, String deviceName, int tenantId) throws DeviceMgtException {

        deviceCache.clearCacheEntry(new DeviceCacheKey(deviceId), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device cache entry is cleared for device id: " + deviceId + " for device name update.");
        }
        return deviceManagementDAO.updateDeviceName(deviceId, deviceName, tenantId);
    }

    /**
     * Updates the status of a device and returns the updated record.
     * This method clears the cache entry upon status change.
     *
     * @param deviceId Device identifier.
     * @param status   New status for the device.
     * @param tenantId Tenant identifier.
     * @return Updated device.
     * @throws DeviceMgtException If the status update fails.
     */
    @Override
    public Device changeDeviceStatus(String deviceId, Device.Status status, int tenantId)
            throws DeviceMgtException {

        deviceCache.clearCacheEntry(new DeviceCacheKey(deviceId), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device cache entry is cleared for device id: " + deviceId + " for status change.");
        }
        return deviceManagementDAO.changeDeviceStatus(deviceId, status, tenantId);
    }

    /**
     * Deletes a device.
     * This method clears the cache entry upon device deletion.
     *
     * @param deviceId Device identifier.
     * @param tenantId Tenant identifier.
     * @throws DeviceMgtException If deletion fails.
     */
    @Override
    public void deleteDevice(String deviceId, int tenantId) throws DeviceMgtException {

        deviceCache.clearCacheEntry(new DeviceCacheKey(deviceId), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Device cache entry is cleared for device id: " + deviceId + " for device deletion.");
        }
        deviceManagementDAO.deleteDevice(deviceId, tenantId);
    }
}
