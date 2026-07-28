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
import org.wso2.carbon.identity.device.mgt.api.model.DeviceUser;
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
        DeviceUser owner = new DeviceUser(device.getId(), TEST_USER_ID);
        Device result = deviceManagementDAO.registerDevice(device, owner, TENANT_ID);

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
        List<String> userDeviceIds = deviceManagementDAO.getDeviceIdsByUserId(TEST_USER_ID, TENANT_ID);
        Assert.assertTrue(userDeviceIds.contains(createdDeviceId));
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
                .deviceName("Carol Phone")
                .deviceModel("Pixel 8 Pro")
                .publicKey("pk-full-" + id)
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
        DeviceUser owner = new DeviceUser(id, SECOND_USER_ID);

        deviceManagementDAO.registerDevice(device, owner, TENANT_ID);
        Device result = deviceManagementDAO.getDeviceById(id, TENANT_ID);

        Assert.assertNotNull(result);
        Assert.assertEquals(result.getId(), id);
        Assert.assertTrue(deviceManagementDAO.getDeviceIdsByUserId(SECOND_USER_ID, TENANT_ID).contains(id));
        Assert.assertEquals(result.getDeviceName(), "Carol Phone");
        Assert.assertEquals(result.getDeviceModel(), "Pixel 8 Pro");
        Assert.assertEquals(result.getPublicKey(), "pk-full-" + id);
        Assert.assertEquals(result.getStatus(), Device.Status.ACTIVE);
        Assert.assertNotNull(result.getRegisteredAt());
    }

    /**
     * Tests that a device registered under one tenant is not visible under another.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 8)
    public void testTenantIsolation() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        Device device = buildDevice(id, "Tenant Device", Device.Status.ACTIVE);
        deviceManagementDAO.registerDevice(device, new DeviceUser(id, TEST_USER_ID), TENANT_ID);

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
        Device d1 = buildDevice(id1, "Dave Phone 1", Device.Status.ACTIVE);
        Device d2 = buildDevice(id2, "Dave Phone 2", Device.Status.ACTIVE);
        deviceManagementDAO.registerDevice(d1, new DeviceUser(id1, userId), OTHER_TENANT_ID);
        deviceManagementDAO.registerDevice(d2, new DeviceUser(id2, userId), OTHER_TENANT_ID);

        List<Device> all = deviceManagementDAO.getDevices(OTHER_TENANT_ID, 0, 100);

        Assert.assertEquals(all.size(), 2);
        List<String> daveDeviceIds = deviceManagementDAO.getDeviceIdsByUserId(userId, OTHER_TENANT_ID);
        Assert.assertEquals(daveDeviceIds.size(), 2);
    }



    /**
     * Tests that updateDeviceName with a wrong tenant id does not affect the row.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 12)
    public void testUpdateDeviceNameWrongTenantNoOp() throws DeviceMgtException {

        String id = UUID.randomUUID().toString();
        Device device = buildDevice(id, "Frank Phone", Device.Status.ACTIVE);
        deviceManagementDAO.registerDevice(device, new DeviceUser(id, TEST_USER_ID), TENANT_ID);

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
        Device device = buildDevice(id, "Grace Phone", Device.Status.ACTIVE);
        deviceManagementDAO.registerDevice(device, new DeviceUser(id, TEST_USER_ID), TENANT_ID);

        deviceManagementDAO.deleteDevice(id, OTHER_TENANT_ID);

        Device result = deviceManagementDAO.getDeviceById(id, TENANT_ID);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getDeviceName(), "Grace Phone");
    }

    /**
     * Tests that changeDeviceStatus(INACTIVE) removes the device from getActiveDevicesByUserId
     * results while it remains visible via getDeviceById and getDevices.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 14)
    public void testChangeDeviceStatusDeactivateUpdatesStatus() throws DeviceMgtException {

        String userId = "heidi@example.com";
        String id = UUID.randomUUID().toString();
        Device device = buildDevice(id, "Heidi Phone", Device.Status.ACTIVE);
        deviceManagementDAO.registerDevice(device, new DeviceUser(id, userId), TENANT_ID);

        Device updated = deviceManagementDAO.changeDeviceStatus(id, Device.Status.INACTIVE, TENANT_ID);

        Assert.assertNotNull(updated);
        Assert.assertEquals(updated.getStatus(), Device.Status.INACTIVE);

        Device byId = deviceManagementDAO.getDeviceById(id, TENANT_ID);
        Assert.assertNotNull(byId);
        Assert.assertEquals(byId.getStatus(), Device.Status.INACTIVE);

        List<Device> pagedDevices = deviceManagementDAO.getDevices(TENANT_ID, 0, 100);
        long foundInPage = pagedDevices.stream().filter(d -> d.getId().equals(id)).count();
        Assert.assertEquals(foundInPage, 1);
    }

    @Test(priority = 15, dependsOnMethods = {"testChangeDeviceStatusDeactivateUpdatesStatus"})
    public void testChangeDeviceStatusActivateUpdatesStatus() throws DeviceMgtException {

        String userId = "heidi@example.com";
        List<String> userDeviceIds = deviceManagementDAO.getDeviceIdsByUserId(userId, TENANT_ID);
        String id = userDeviceIds.stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected Heidi's device to still exist"));

        Device updated = deviceManagementDAO.changeDeviceStatus(id, Device.Status.ACTIVE, TENANT_ID);

        Assert.assertEquals(updated.getStatus(), Device.Status.ACTIVE);
    }

    /**
     * Tests that getDevicesByUserId(userId, tenantId, offset, limit) returns only the given
     * user's devices, including one that is INACTIVE.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 16)
    public void testGetDevicesByUserIdIncludesInactive() throws DeviceMgtException {

        String userId = "ivan@example.com";
        String activeId = UUID.randomUUID().toString();
        String inactiveId = UUID.randomUUID().toString();
        Device activeDev = buildDevice(activeId, "Ivan Phone", Device.Status.ACTIVE);
        Device inactiveDev = buildDevice(inactiveId, "Ivan Tablet", Device.Status.INACTIVE);
        Device otherDev = buildDevice(UUID.randomUUID().toString(), "Someone Else Phone", Device.Status.ACTIVE);

        deviceManagementDAO.registerDevice(activeDev, new DeviceUser(activeId, userId), TENANT_ID);
        deviceManagementDAO.registerDevice(inactiveDev, new DeviceUser(inactiveId, userId), TENANT_ID);
        deviceManagementDAO.registerDevice(otherDev, new DeviceUser(otherDev.getId(), "other@example.com"), TENANT_ID);

        List<Device> devices = deviceManagementDAO.getDevicesByUserId(userId, TENANT_ID, 0, 100);

        Assert.assertEquals(devices.size(), 2);
        Assert.assertTrue(devices.stream().anyMatch(
                d -> d.getId().equals(inactiveId) && d.getStatus() == Device.Status.INACTIVE));
    }

    /**
     * Tests that getDeviceCountByUserId matches the number of devices returned by the
     * filtered getDevicesByUserId method.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 17, dependsOnMethods = {"testGetDevicesByUserIdIncludesInactive"})
    public void testGetDeviceCountByUserId() throws DeviceMgtException {

        int count = deviceManagementDAO.getDeviceCountByUserId("ivan@example.com", TENANT_ID);

        Assert.assertEquals(count, 2);
    }

    /**
     * Tests that getDeviceCount(tenantId) matches the number of devices returned by the
     * unfiltered getDevices method.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 18, dependsOnMethods = {"testGetDevicesReturnsAllForTenant"})
    public void testGetDeviceCountReturnsAllForTenant() throws DeviceMgtException {

        List<Device> devices = deviceManagementDAO.getDevices(OTHER_TENANT_ID, 0, 100);
        int count = deviceManagementDAO.getDeviceCount(OTHER_TENANT_ID);

        Assert.assertEquals(count, devices.size());
    }

    /**
     * Tests that deleteDevicesByUserId removes all of a user's devices (and their mappings) while
     * leaving other users' devices untouched.
     *
     * @throws DeviceMgtException If the DAO operation fails.
     */
    @Test(priority = 19)
    public void testDeleteDevicesByUserId() throws DeviceMgtException {

        String userId = "wanda@example.com";
        String otherUserId = "victor@example.com";
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String otherId = UUID.randomUUID().toString();

        Device dev1 = buildDevice(id1, "Wanda Phone", Device.Status.ACTIVE);
        Device dev2 = buildDevice(id2, "Wanda Tablet", Device.Status.INACTIVE);
        Device otherDev = buildDevice(otherId, "Victor Phone", Device.Status.ACTIVE);

        deviceManagementDAO.registerDevice(dev1, new DeviceUser(id1, userId), TENANT_ID);
        deviceManagementDAO.registerDevice(dev2, new DeviceUser(id2, userId), TENANT_ID);
        deviceManagementDAO.registerDevice(otherDev, new DeviceUser(otherId, otherUserId), TENANT_ID);

        Assert.assertEquals(deviceManagementDAO.getDeviceIdsByUserId(userId, TENANT_ID).size(), 2);

        deviceManagementDAO.deleteDevicesByUserId(userId, TENANT_ID);

        Assert.assertTrue(deviceManagementDAO.getDeviceIdsByUserId(userId, TENANT_ID).isEmpty());
        Assert.assertNull(deviceManagementDAO.getDeviceById(id1, TENANT_ID));
        Assert.assertNull(deviceManagementDAO.getDeviceById(id2, TENANT_ID));
        // The other user's device must remain.
        Assert.assertNotNull(deviceManagementDAO.getDeviceById(otherId, TENANT_ID));
    }

    private Device buildDevice(String id, String deviceName, Device.Status status) {

        return new Device.Builder()
                .id(id)
                .deviceName(deviceName)
                .deviceModel("iPhone 15 Pro")
                .publicKey("base64-public-key-" + id)
                .status(status)
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
    }
}
