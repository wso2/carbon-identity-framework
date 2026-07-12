/*
* Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.device.mgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.CacheBackedDeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.DeviceManagementDAOImpl;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementAuditLogger;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;

import java.util.List;

/**
 * Default implementation of {@link DeviceManagementService}.
 */
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private static final Log LOG = LogFactory.getLog(DeviceManagementServiceImpl.class);
    private static final DeviceManagementServiceImpl INSTANCE = new DeviceManagementServiceImpl();
    private static final DeviceManagementAuditLogger AUDIT_LOGGER = new DeviceManagementAuditLogger();
    private final DeviceManagementDAO deviceManagementDAO;

    private DeviceManagementServiceImpl() {
        deviceManagementDAO = new CacheBackedDeviceManagementDAO(new DeviceManagementDAOImpl());
    }

    /**
     * Returns the service singleton instance.
     *
     * @return Service singleton.
     */
    public static DeviceManagementServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void persistDevice(Device device, String tenantDomain) throws DeviceMgtException {

        if (device.getUserId() == null || device.getUserId().trim().isEmpty()) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_USER_ID_REQUIRED);
        }
        deviceManagementDAO.registerDevice(device, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.REGISTER, device);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device persisted for user: " + device.getUserId() +
                    " in tenant: " + tenantDomain + " with device ID: " + device.getId());
        }
    }

    @Override
    public Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        return deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getDevicesByUserId(String userId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(userId, "userId");
        return deviceManagementDAO.getDevicesByUserId(
                userId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getDevices(String tenantDomain, int offset, int limit) throws DeviceMgtException {

        return deviceManagementDAO.getDevices(
                IdentityTenantUtil.getTenantId(tenantDomain), offset, validateLimit(limit));
    }

    @Override
    public int getDeviceCount(String tenantDomain) throws DeviceMgtException {

        return deviceManagementDAO.getDeviceCount(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Device updateDeviceName(String deviceId, String deviceName, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateRequiredField(deviceName, "deviceName");
        validateDeviceExists(deviceId, tenantDomain);

        Device updated = deviceManagementDAO.updateDeviceName(
                deviceId, deviceName, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updated == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.UPDATE, updated);
        return updated;
    }

    @Override
    public Device activateDevice(String deviceId, String tenantDomain) throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateDeviceExists(deviceId, tenantDomain);

        Device updated = deviceManagementDAO.changeDeviceStatus(
                deviceId, Device.Status.ACTIVE, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updated == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.ACTIVATE, updated);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device activated with ID: " + deviceId + " in tenant: " + tenantDomain);
        }
        return updated;
    }

    @Override
    public Device deactivateDevice(String deviceId, String tenantDomain) throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateDeviceExists(deviceId, tenantDomain);

        Device updated = deviceManagementDAO.changeDeviceStatus(
                deviceId, Device.Status.INACTIVE, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updated == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.DEACTIVATE, updated);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deactivated with ID: " + deviceId + " in tenant: " + tenantDomain);
        }
        return updated;
    }

    @Override
    public void deleteDevice(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        validateRequiredField(deviceId, "deviceId");
        validateDeviceExists(deviceId, tenantDomain);

        deviceManagementDAO.deleteDevice(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.DELETE, deviceId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deleted with ID: " + deviceId + " in tenant: " + tenantDomain);
        }
    }

    private void validateDeviceExists(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        Device existing = deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (existing == null) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }
    }

    private void validateRequiredField(String value, String fieldName)
            throws DeviceMgtClientException {

        if (value == null || value.trim().isEmpty()) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, fieldName);
        }
    }

    /**
     * Validates the requested page size against the maximum items per page configured for the server.
     * A page size larger than the configured maximum is capped, so that a caller cannot load an
     * unbounded number of devices into memory.
     *
     * @param limit Requested page size.
     * @return The page size to use, capped at the configured maximum.
     * @throws DeviceMgtClientException If the requested page size is negative.
     */
    private int validateLimit(int limit) throws DeviceMgtClientException {

        if (limit < 0) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_INVALID_DEVICE_FIELD, "limit");
        }

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();
        if (limit > maximumItemsPerPage) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Given limit: " + limit + " exceeds the maximum items per page. Using the maximum: "
                        + maximumItemsPerPage);
            }
            return maximumItemsPerPage;
        }
        return limit;
    }
}

