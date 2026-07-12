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

package org.wso2.carbon.identity.device.mgt.dao;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.DeviceManagementDAOImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link DeviceManagementDAOImpl}.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class DeviceManagementDAOImplTest {

    private static final int TENANT_ID = -1234;
    private static final int OTHER_TENANT_ID = -5678;
    private static final String TEST_USER_ID = "alice@example.com";
    private static final String SECOND_USER_ID = "carol@example.com";

    DeviceManagementDAOImpl deviceManagementDAO = new DeviceManagementDAOImpl();
    private String createdDeviceId;

    /**
     * Tests registering a device.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 1)
    public void testRegisterDevice() throws DeviceMgtException {

        Device device = buildDevice(UUID.randomUUID().toString(), "Alice Phone", Device.Status.ACTIVE);
        Device result = deviceManagementDAO.registerDevice(device, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), device.getId());
        Assert.assertEquals(result.getDeviceName(), "Alice Phone");
        Assert.assertEquals(result.getStatus(), Device.Status.ACTIVE);

        createdDeviceId = result.getId();
    }

    /**
     * Tests retrieving a device by id.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 2, dependsOnMethods = {"testRegisterDevice"})
    public void testGetDeviceById() throws DeviceMgtException {

        Device result = deviceManagementDAO.getDeviceById(createdDeviceId, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), createdDeviceId);
        Assert.assertEquals(result.getUserId(), TEST_USER_ID);
    }

    /**
     * Tests retrieving a non-existing device by id.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 3)
    public void testGetDeviceByIdNotFound() throws DeviceMgtException {

        Device result = deviceManagementDAO.getDeviceById(UUID.randomUUID().toString(), TENANT_ID);
        Assert.assertNull(result);
    }

    /**
     * Tests that getDevicesByUserId returns only ACTIVE devices.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 4, dependsOnMethods = {"testRegisterDevice"})
    public void testGetDevicesByUserIdOnlyActive() throws DeviceMgtException {

        Device inactiveDevice = buildDevice(UUID.randomUUID().toString(), "Old Device", Device.Status.INACTIVE);
        deviceManagementDAO.registerDevice(inactiveDevice, TENANT_ID);

        List<Device> devices = deviceManagementDAO.getDevicesByUserId(TEST_USER_ID, TENANT_ID);

        Assert.assertEquals(devices.size(), 1);
        Assert.assertEquals(devices.get(0).getStatus(), Device.Status.ACTIVE);
        Assert.assertEquals(devices.get(0).getId(), createdDeviceId);
    }

    /**
     * Tests updating a device name.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 5, dependsOnMethods = {"testRegisterDevice"})
    public void testUpdateDeviceName() throws DeviceMgtException {

        Device updated = deviceManagementDAO.updateDeviceName(createdDeviceId, "Alice Updated", TENANT_ID);

        Assert.assertNotNull(updated);
        Assert.assertEquals(updated.getDeviceName(), "Alice Updated");
    }

    /**
     * Tests deleting a device.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 6, dependsOnMethods = {"testRegisterDevice"})
    public void testDeleteDevice() throws DeviceMgtException {

        deviceManagementDAO.deleteDevice(createdDeviceId, TENANT_ID);
        Device afterDelete = deviceManagementDAO.getDeviceById(createdDeviceId, TENANT_ID);

        Assert.assertNull(afterDelete);
    }

    /**
     * Tests that all fields survive a register + getDeviceById round-trip.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 7)
    public void testRegisterDeviceFullFieldRoundTrip() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        Device device = new Device.Builder()
                .id(id)
                .userId(SECOND_USER_ID)
                .deviceName("Carol Phone")
                .deviceModel("Pixel 8 Pro")
                .publicKey("pk-full-" + id)
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()))
                .metadata("{\"env\":\"test\"}")
                .build();

        deviceManagementDAO.registerDevice(device, TENANT_ID);
        Device result = deviceManagementDAO.getDeviceById(id, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), id);
        Assert.assertEquals(result.getUserId(), SECOND_USER_ID);
        Assert.assertEquals(result.getDeviceName(), "Carol Phone");
        Assert.assertEquals(result.getDeviceModel(), "Pixel 8 Pro");
        Assert.assertEquals(result.getPublicKey(), "pk-full-" + id);
        Assert.assertEquals(result.getStatus(), Device.Status.ACTIVE);
        Assert.assertNotNull(result.getRegisteredAt());
        Assert.assertEquals(result.getMetadata(), "{\"env\":\"test\"}");
    }

    /**
     * Tests that a device registered under one tenant is not visible under another.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 8)
    public void testTenantIsolation() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(buildDevice(id, "Tenant Device", Device.Status.ACTIVE), TENANT_ID);

        Device fromOtherTenant = deviceManagementDAO.getDeviceById(id, OTHER_TENANT_ID);
        Assert.assertNull(fromOtherTenant);

        List<Device> allOtherTenant = deviceManagementDAO.getDevices(OTHER_TENANT_ID, 0, 100);
        long found = allOtherTenant.stream().filter(d -> d.getId().equals(id)).count();
        Assert.assertEquals(found, 0);
    }

    /**
     * Tests that getDevices returns every device registered under the tenant.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 9)
    public void testGetDevicesReturnsAllForTenant() throws DeviceMgtException {

        String userId = "dave@example.com";
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(
                buildDeviceForUser(id1, "Dave Phone 1", userId, Device.Status.ACTIVE), OTHER_TENANT_ID);
        deviceManagementDAO.registerDevice(
                buildDeviceForUser(id2, "Dave Phone 2", userId, Device.Status.ACTIVE), OTHER_TENANT_ID);

        List<Device> all = deviceManagementDAO.getDevices(OTHER_TENANT_ID, 0, 100);

        Assert.assertEquals(all.size(), 2);
        long count = all.stream().filter(d -> d.getUserId().equals(userId)).count();
        Assert.assertEquals(count, 2);
    }

    /**
     * Tests that getDevicesByUserId returns all active devices for a user.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 10)
    public void testGetDevicesByUserIdMultiple() throws DeviceMgtException {

        String userId = "eve@example.com";
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(
                buildDeviceForUser(id1, "Eve Phone 1", userId, Device.Status.ACTIVE), TENANT_ID);
        deviceManagementDAO.registerDevice(
                buildDeviceForUser(id2, "Eve Phone 2", userId, Device.Status.ACTIVE), TENANT_ID);

        List<Device> devices = deviceManagementDAO.getDevicesByUserId(userId, TENANT_ID);

        Assert.assertEquals(devices.size(), 2);
    }

    /**
     * Tests that getDevicesByUserId returns an empty list for an unknown user.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 11)
    public void testGetDevicesByUserIdEmpty() throws DeviceMgtException {

        List<Device> devices = deviceManagementDAO.getDevicesByUserId("unknown@example.com", TENANT_ID);

        Assert.assertNotNull(devices);
        Assert.assertTrue(devices.isEmpty());
    }

    /**
     * Tests that updateDeviceName with a wrong tenant id does not affect the row.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 12)
    public void testUpdateDeviceNameWrongTenantNoOp() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(buildDevice(id, "Frank Phone", Device.Status.ACTIVE), TENANT_ID);

        deviceManagementDAO.updateDeviceName(id, "Frank New Name", OTHER_TENANT_ID);

        Device result = deviceManagementDAO.getDeviceById(id, TENANT_ID);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getDeviceName(), "Frank Phone");
    }

    /**
     * Tests that deleteDevice with a wrong tenant id does not remove the row.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 13)
    public void testDeleteDeviceWrongTenantNoOp() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(buildDevice(id, "Grace Phone", Device.Status.ACTIVE), TENANT_ID);

        deviceManagementDAO.deleteDevice(id, OTHER_TENANT_ID);

        Device result = deviceManagementDAO.getDeviceById(id, TENANT_ID);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getDeviceName(), "Grace Phone");
    }

    /**
     * Tests that changeDeviceStatus(INACTIVE) removes the device from getDevicesByUserId results
     * while it remains visible via getDeviceById and getDevices.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 14)
    public void testChangeDeviceStatusDeactivateExcludesFromUserDeviceList() throws DeviceMgtException {

        String userId = "heidi@example.com";
        String id = UUID.randomUUID().toString();
        deviceManagementDAO.registerDevice(
                buildDeviceForUser(id, "Heidi Phone", userId, Device.Status.ACTIVE), TENANT_ID);

        Device updated = deviceManagementDAO.changeDeviceStatus(id, Device.Status.INACTIVE, TENANT_ID);

        Assert.assertNotNull(updated);
        Assert.assertEquals(updated.getStatus(), Device.Status.INACTIVE);

        List<Device> userDevices = deviceManagementDAO.getDevicesByUserId(userId, TENANT_ID);
        Assert.assertTrue(userDevices.isEmpty());

        Device byId = deviceManagementDAO.getDeviceById(id, TENANT_ID);
        Assert.assertNotNull(byId);
        Assert.assertEquals(byId.getStatus(), Device.Status.INACTIVE);

        List<Device> pagedDevices = deviceManagementDAO.getDevices(TENANT_ID, 0, 100);
        long foundInPage = pagedDevices.stream().filter(d -> d.getId().equals(id)).count();
        Assert.assertEquals(foundInPage, 1);
    }

    /**
     * Tests that changeDeviceStatus(ACTIVE) reinstates a device into getDevicesByUserId results.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 15, dependsOnMethods = {"testChangeDeviceStatusDeactivateExcludesFromUserDeviceList"})
    public void testChangeDeviceStatusActivateReincludesInUserDeviceList() throws DeviceMgtException {

        String userId = "heidi@example.com";
        List<Device> beforeActivation = deviceManagementDAO.getDevicesByUserId(userId, TENANT_ID);
        Assert.assertTrue(beforeActivation.isEmpty());

        List<Device> allDevices = deviceManagementDAO.getDevices(TENANT_ID, 0, 100);
        String id = allDevices.stream()
                .filter(d -> d.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected Heidi's device to still exist"))
                .getId();

        Device updated = deviceManagementDAO.changeDeviceStatus(id, Device.Status.ACTIVE, TENANT_ID);

        Assert.assertEquals(updated.getStatus(), Device.Status.ACTIVE);

        List<Device> userDevices = deviceManagementDAO.getDevicesByUserId(userId, TENANT_ID);
        Assert.assertEquals(userDevices.size(), 1);
        Assert.assertEquals(userDevices.get(0).getId(), id);
    }

    private Device buildDevice(String id, String deviceName, Device.Status status) {

        return new Device.Builder()
                .id(id)
                .userId(TEST_USER_ID)
                .deviceName(deviceName)
                .deviceModel("iPhone 15 Pro")
                .publicKey("base64-public-key-" + id)
                .status(status)
                .registeredAt(Timestamp.from(Instant.now()))
                .metadata("{\"label\":\"primary\"}")
                .build();
    }

    private Device buildDeviceForUser(String id, String deviceName, String userId, Device.Status status) {

        return new Device.Builder()
                .id(id)
                .userId(userId)
                .deviceName(deviceName)
                .deviceModel("Pixel 8")
                .publicKey("pk-" + id)
                .status(status)
                .registeredAt(Timestamp.from(Instant.now()))
                .metadata(null)
                .build();
    }
}
