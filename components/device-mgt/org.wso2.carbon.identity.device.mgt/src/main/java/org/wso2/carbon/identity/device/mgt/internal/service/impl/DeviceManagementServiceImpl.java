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
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.model.TenantDevice;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.mgt.internal.component.DeviceMgtComponentServiceHolder;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.CacheBackedDeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.DeviceManagementDAOImpl;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementAuditLogger;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceManagementExceptionHandler;
import org.wso2.carbon.identity.device.mgt.internal.util.DeviceValidator;
import org.wso2.carbon.identity.organization.management.organization.user.sharing.models.UserAssociation;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link DeviceManagementService}.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService",
                "service.scope=singleton"
        }
)
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
    public String generateDeviceId() {

        return UUID.randomUUID().toString();
    }

    @Override
    public Device registerDevice(Device device, DeviceAssociation association, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateDeviceForRegistration(device);
        DEVICE_VALIDATOR.validateAssociation(association);
        validateDeviceRegistration(((UserDeviceAssociation) association).getUserId(), tenantDomain);

        String deviceId = device.getId();
        Timestamp registeredAt = device.getRegisteredAt() != null
                ? device.getRegisteredAt() : Timestamp.from(Instant.now());
        device = new Device.Builder(device)
                .id(deviceId)
                .registeredAt(registeredAt)
                .build();

        UserDeviceAssociation userDeviceAssociation = (UserDeviceAssociation) association;
        association = new UserDeviceAssociation(deviceId, userDeviceAssociation.getUserId());

        Device registeredDevice = deviceManagementDAO.registerDevice(
                device, association, IdentityTenantUtil.getTenantId(tenantDomain));

        String userId = userDeviceAssociation.getUserId();
        AUDIT_LOGGER.printAuditLog(DeviceManagementAuditLogger.Operation.REGISTER,
                registeredDevice != null ? registeredDevice : device, userId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Device registered for user: " + userId +
                    LOG_IN_TENANT + tenantDomain + " with device ID: " + device.getId());
        }
        return registeredDevice != null ? registeredDevice : device;
    }

    private void validateDeviceRegistration(String userId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(userId, FIELD_USER_ID);
        // Shared users inherit devices from their resident organization.
        if (!userId.equals(resolveDeviceOwner(userId, tenantDomain).userId)) {
            throw DeviceManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_DEVICE_REGISTRATION_FOR_SHARED_USER, tenantDomain);
        }
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
    public Device getDeviceOfResidentUser(String deviceId, String residentUserId, String tenantDomain)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);
        DEVICE_VALIDATOR.validateRequiredField(residentUserId, FIELD_USER_ID);
        DeviceOwner owner = resolveDeviceOwnerOfResidentUser(residentUserId, tenantDomain);
        Device device = getDeviceOfOwner(deviceId, owner.userId, owner.tenantDomain);
        return device != null ? toDisplayedDevice(device, owner) : null;
    }

    private Device getDeviceOfOwner(String deviceId, String userId, String tenantDomain) throws DeviceMgtException {

        Device device = getDeviceById(deviceId, tenantDomain);
        return device != null && userId.equals(device.getUserId()) ? device : null;
    }

    @Override
    public TenantDevice getTenantDeviceById(String deviceId) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(deviceId, FIELD_DEVICE_ID);
        return deviceManagementDAO.getTenantDeviceById(deviceId);
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
        int validatedLimit = DEVICE_VALIDATOR.validateLimit(limit);
        DeviceOwner owner = resolveDeviceOwner(userId, tenantDomain);
        List<Device> devices = deviceManagementDAO.getDevicesByUserId(
                owner.userId, IdentityTenantUtil.getTenantId(owner.tenantDomain), offset, validatedLimit);
        if (userId.equals(owner.userId)) {
            return devices;
        }
        return devices.stream()
                .map(device -> toDisplayedDevice(device, owner))
                .collect(Collectors.toList());
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
        DeviceOwner owner = resolveDeviceOwner(userId, tenantDomain);
        return deviceManagementDAO.getDeviceCountByUserId(
                owner.userId, IdentityTenantUtil.getTenantId(owner.tenantDomain));
    }

    @Override
    public List<Device> getDevicesOfResidentUser(String residentUserId, String tenantDomain, int offset, int limit)
            throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(residentUserId, FIELD_USER_ID);
        int validatedLimit = DEVICE_VALIDATOR.validateLimit(limit);
        DeviceOwner owner = resolveDeviceOwnerOfResidentUser(residentUserId, tenantDomain);
        List<Device> devices = deviceManagementDAO.getDevicesByUserId(
                owner.userId, IdentityTenantUtil.getTenantId(owner.tenantDomain), offset, validatedLimit);
        return devices.stream()
                .map(device -> toDisplayedDevice(device, owner))
                .collect(Collectors.toList());
    }

    @Override
    public int getDeviceCountOfResidentUser(String residentUserId, String tenantDomain) throws DeviceMgtException {

        DEVICE_VALIDATOR.validateRequiredField(tenantDomain, FIELD_TENANT_DOMAIN);
        DEVICE_VALIDATOR.validateRequiredField(residentUserId, FIELD_USER_ID);
        DeviceOwner owner = resolveDeviceOwnerOfResidentUser(residentUserId, tenantDomain);
        return deviceManagementDAO.getDeviceCountByUserId(
                owner.userId, IdentityTenantUtil.getTenantId(owner.tenantDomain));
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

    /**
     * Resolves the user and tenant that own the devices of the given user.
     *
     * @param userId       User ID.
     * @param tenantDomain Tenant domain of the user.
     * @return The resident user and its tenant for a shared user, otherwise the given user and tenant.
     * @throws DeviceMgtException If the user association cannot be resolved.
     */
    private DeviceOwner resolveDeviceOwner(String userId, String tenantDomain) throws DeviceMgtException {

        DeviceMgtComponentServiceHolder holder = DeviceMgtComponentServiceHolder.getInstance();
        OrganizationManager organizationManager = holder.getOrganizationManager();
        try {
            if (!OrganizationManagementUtil.isOrganization(tenantDomain)) {
                return new DeviceOwner(userId, tenantDomain, userId);
            }
            String orgId = organizationManager.resolveOrganizationId(tenantDomain);
            UserAssociation association = holder.getOrganizationUserSharingService().getUserAssociation(userId, orgId);
            if (!isSharedUserAssociation(association, orgId)) {
                return new DeviceOwner(userId, tenantDomain, userId);
            }
            // Devices are only in the resident organization; shared users can't register devices in sub-orgs.
            return new DeviceOwner(association.getAssociatedUserId(),
                    organizationManager.resolveTenantDomain(association.getUserResidentOrganizationId()), userId);
        } catch (OrganizationManagementException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RESOLVING_INHERITED_DEVICES, e, tenantDomain);
        }
    }

    /**
     * Resolves the user and tenant that own the devices of the given resident user in the request tenant.
     *
     * @param residentUserId User ID of the user in its resident organization.
     * @param tenantDomain   Tenant domain of the request.
     * @return The resident user and its tenant, with the shared user ID to display for a shared user.
     * @throws DeviceMgtException If the user association cannot be resolved.
     */
    private DeviceOwner resolveDeviceOwnerOfResidentUser(String residentUserId, String tenantDomain)
            throws DeviceMgtException {

        DeviceMgtComponentServiceHolder holder = DeviceMgtComponentServiceHolder.getInstance();
        OrganizationManager organizationManager = holder.getOrganizationManager();
        try {
            if (!OrganizationManagementUtil.isOrganization(tenantDomain)) {
                return new DeviceOwner(residentUserId, tenantDomain, residentUserId);
            }
            String orgId = organizationManager.resolveOrganizationId(tenantDomain);
            UserAssociation association = holder.getOrganizationUserSharingService()
                    .getUserAssociationOfAssociatedUserByOrgId(residentUserId, orgId);
            if (!isSharedUserAssociation(association, orgId)) {
                return new DeviceOwner(residentUserId, tenantDomain, residentUserId);
            }
            // Devices are only in the resident organization; shared users can't register devices in sub-orgs.
            return new DeviceOwner(residentUserId,
                    organizationManager.resolveTenantDomain(association.getUserResidentOrganizationId()),
                    association.getUserId());
        } catch (OrganizationManagementException e) {
            throw DeviceManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RESOLVING_INHERITED_DEVICES, e, tenantDomain);
        }
    }

    private boolean isSharedUserAssociation(UserAssociation association, String orgId) {

        return association != null && StringUtils.isNotBlank(association.getUserId())
                && StringUtils.isNotBlank(association.getAssociatedUserId())
                && StringUtils.isNotBlank(association.getUserResidentOrganizationId())
                && !orgId.equals(association.getUserResidentOrganizationId());
    }

    private Device toDisplayedDevice(Device device, DeviceOwner owner) {

        if (owner.displayUserId.equals(device.getUserId())) {
            return device;
        }
        return new Device.Builder(device)
                .userId(owner.displayUserId)
                .build();
    }

    /**
     * The user and tenant that own a user's devices, and the user ID to show the devices with.
     */
    private static final class DeviceOwner {

        private final String userId;
        private final String tenantDomain;
        private final String displayUserId;

        private DeviceOwner(String userId, String tenantDomain, String displayUserId) {

            this.userId = userId;
            this.tenantDomain = tenantDomain;
            this.displayUserId = displayUserId;
        }
    }
}
