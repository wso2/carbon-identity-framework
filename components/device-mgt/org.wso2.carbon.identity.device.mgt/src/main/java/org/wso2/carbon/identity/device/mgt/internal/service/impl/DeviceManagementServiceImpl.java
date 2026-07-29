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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.CacheBackedDeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.DeviceManagementDAOImpl;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementAuditLogger;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceValidator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * Default implementation of {@link DeviceManagementService}.
 */
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private static final Log LOG = LogFactory.getLog(DeviceManagementServiceImpl.class);
    private static final DeviceManagementServiceImpl INSTANCE = new DeviceManagementServiceImpl();
    private static final DeviceManagementAuditLogger AUDIT_LOGGER = new DeviceManagementAuditLogger();
    private static final DeviceValidator DEVICE_VALIDATOR = new DeviceValidator();
    private static final String FIELD_TENANT_DOMAIN = "tenantDomain";
    private static final String FIELD_DEVICE_ID = "deviceId";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_DEVICE_NAME = "deviceName";
    private static final String LOG_IN_TENANT = " in tenant: ";

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
    public Device registerDevice(Device device, DeviceAssociation association, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateDeviceForRegistration(device);
        DEVICE_VALIDATOR.validateAssociation(association);
        if (!StringUtils.equals(association.getDeviceId(), device.getId())) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_INVALID_DEVICE_ASSOCIATION);
        }
        if (device.getRegisteredAt() == null) {
            device = new Device.Builder(device).registeredAt(Timestamp.from(Instant.now())).build();
        }
        Device registeredDevice = deviceManagementDAO.registerDevice(
                device, association, IdentityTenantUtil.getTenantId(tenantDomain));

        String userId = ((UserDeviceAssociation) association).getUserId();
        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.REGISTER,
                registeredDevice != null ? registeredDevice : device, userId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device registered for user: " + userId +
                    LOG_IN_TENANT + tenantDomain + " with device ID: " + device.getId());
        }
        return registeredDevice != null ? registeredDevice : device;
    }

    @Override
    public Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);
        return deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Device> getDevices(String tenantDomain, int offset, int limit) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        return deviceManagementDAO.getDevices(
                IdentityTenantUtil.getTenantId(tenantDomain), offset, DEVICE_VALIDATOR.validateLimit(limit));
    }

    @Override
    public List<Device> getDevicesByUserId(String userId, String tenantDomain, int offset, int limit)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(userId, FIELD_USER_ID);
        return deviceManagementDAO.getDevicesByUserId(
                userId, IdentityTenantUtil.getTenantId(tenantDomain), offset, DEVICE_VALIDATOR.validateLimit(limit));
    }

    @Override
    public int getDeviceCount(String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        return deviceManagementDAO.getDeviceCount(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public int getDeviceCountByUserId(String userId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(userId, FIELD_USER_ID);
        return deviceManagementDAO.getDeviceCountByUserId(userId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Device updateDeviceName(String deviceId, String deviceName, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);
        DEVICE_VALIDATOR.validateRequiredField(deviceName, FIELD_DEVICE_NAME);

        Device existing = getDeviceById(deviceId, tenantDomain);
        DEVICE_VALIDATOR.validateDeviceExists(existing, deviceId);

        Device updatedDevice = deviceManagementDAO.updateDeviceName(
                deviceId, deviceName, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updatedDevice == null) {
            throw DeviceManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.UPDATE, updatedDevice);
        return updatedDevice;
    }

    @Override
    public Device activateDevice(String deviceId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);

        Device existing = getDeviceById(deviceId, tenantDomain);
        DEVICE_VALIDATOR.validateDeviceExists(existing, deviceId);

        Device updated = deviceManagementDAO.changeDeviceStatus(
                deviceId, Device.Status.ACTIVE, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updated == null) {
            throw DeviceManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.ACTIVATE, updated);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device activated with ID: " + deviceId + LOG_IN_TENANT + tenantDomain);
        }
        return updated;
    }

    @Override
    public Device deactivateDevice(String deviceId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);

        Device existing = getDeviceById(deviceId, tenantDomain);
        DEVICE_VALIDATOR.validateDeviceExists(existing, deviceId);

        Device updated = deviceManagementDAO.changeDeviceStatus(
                deviceId, Device.Status.INACTIVE, IdentityTenantUtil.getTenantId(tenantDomain));

        if (updated == null) {
            throw DeviceManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_DEVICE_NOT_FOUND, deviceId);
        }

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.DEACTIVATE, updated);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deactivated with ID: " + deviceId + LOG_IN_TENANT + tenantDomain);
        }
        return updated;
    }

    @Override
    public void deleteDevice(String deviceId, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);

        Device existing = deviceManagementDAO.getDeviceById(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (existing == null) {
            return;
        }

        deviceManagementDAO.deleteDevice(
                deviceId, IdentityTenantUtil.getTenantId(tenantDomain));

        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.DELETE, deviceId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device deleted with ID: " + deviceId + LOG_IN_TENANT + tenantDomain);
        }
    }

    @Override
    public void deleteDevicesByUserId(String userId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(userId, FIELD_USER_ID);
        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);

        deviceManagementDAO.deleteDevicesByUserId(userId, IdentityTenantUtil.getTenantId(tenantDomain));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Devices deleted for user id: " + userId + LOG_IN_TENANT + tenantDomain);
        }
    }
}
