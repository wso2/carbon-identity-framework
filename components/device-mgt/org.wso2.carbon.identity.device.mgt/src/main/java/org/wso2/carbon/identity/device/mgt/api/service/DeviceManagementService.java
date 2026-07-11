package org.wso2.carbon.identity.device.mgt.api.service;

import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.DeviceRegistrationInitiation;

import java.util.List;

/**
 * Service interface for device management operations.
 */
public interface DeviceManagementService {

    /**
     * Phase 1 of the two-phase registration protocol.
     * Generates a cryptographically random challenge and stores it in the distributed cache
     * keyed by the returned registrationId. The client SDK must sign the challenge with its
     * private key and return the signature in Phase 2.
     *
     * @param username     Username of the registering user.
     * @param tenantDomain Tenant domain.
     * @return DeviceRegistrationInitiation containing registrationId and challenge (base64url).
     */
    DeviceRegistrationInitiation initiateDeviceRegistration(String username, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Verifies the device registration challenge-response without persisting to the database.
     * Used during registration flows where the user does not yet have a provisioned userId —
     * the caller stores the returned object in the flow context and defers the DB write to
     * {@link #persistDevice(Device, String)} once UserProvisioningExecutor has run.
     *
     * @param registrationId Opaque token returned by initiateDeviceRegistration.
     * @param publicKey      Base64-encoded EC public key (X.509/SubjectPublicKeyInfo DER).
     * @param signature      Base64-encoded ECDSA signature over the challenge bytes.
     * @param deviceName     Human-readable name for the device.
     * @param deviceModel    Hardware model string (nullable).
     * @param metadata       Optional JSON string for extensible attributes (nullable).
     * @param tenantDomain   Tenant domain.
     * @return A Device whose userId is unset — caller must set the real userId before
     *         calling {@link #persistDevice(Device, String)}.
     */
    Device verifyDeviceRegistration(
            String registrationId,
            String publicKey,
            String signature,
            String deviceName,
            String deviceModel,
            String metadata,
            String tenantDomain) throws DeviceMgtException;

    /**
     * Persists a pre-verified {@link Device} to the database.
     * Counterpart to {@link #verifyDeviceRegistration}: call this after replacing the placeholder
     * userId with the real provisioned userId.
     *
     * @param device       The verified device to persist.
     * @param tenantDomain Tenant domain.
     */
    void persistDevice(Device device, String tenantDomain) throws DeviceMgtException;

    /**
     * Retrieves a device by its UUID.
     *
     * @param deviceId     UUID of the device (IDN_DEVICE.ID).
     * @param tenantDomain Tenant domain.
     * @return The Device, or null if not found.
     */
    Device getDeviceById(String deviceId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves all ACTIVE devices registered by a user.
     *
     * @param userId       WSO2 user identifier.
     * @param tenantDomain Tenant domain.
     * @return List of active Device objects. Empty list if none found.
     */
    List<Device> getDevicesByUserId(String userId, String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves all devices registered in the tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return List of all Device objects. Empty list if none found.
     */
    List<Device> getAllDevices(String tenantDomain)
            throws DeviceMgtException;

    /**
     * Retrieves a page of devices registered in the tenant, ordered by registration time (newest first).
     *
     * @param tenantDomain Tenant domain.
     * @param offset       Number of records to skip.
     * @param limit        Maximum number of records to return.
     * @return Page of Device objects. Empty list if none found.
     */
    List<Device> getDevices(String tenantDomain, int offset, int limit)
            throws DeviceMgtException;

    /**
     * Counts all devices registered in the tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return Total number of devices in the tenant.
     */
    int getDeviceCount(String tenantDomain)
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
     * Deletes (hard delete) a device registration record.
     *
     * @param deviceId     UUID of the device.
     * @param tenantDomain Tenant domain.
     */
    void deleteDevice(String deviceId, String tenantDomain)
            throws DeviceMgtException;
}
