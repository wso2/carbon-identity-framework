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

package org.wso2.carbon.identity.device.mgt.service;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.service.impl.DeviceManagementServiceImpl;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeviceManagementServiceImpl}.
 */
@WithCarbonHome
public class DeviceManagementServiceImplTest {

    private static final String TEST_USERNAME = "alice";
    private static final String TENANT_DOMAIN = "test.com";
    private static final int TENANT_ID = 1;

    private DeviceManagementServiceImpl service;
    private DeviceManagementDAO dao;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;
    private Field daoField;
    private DeviceManagementDAO originalDao;

    @BeforeClass
    public void setUpClass() throws Exception {

        service = DeviceManagementServiceImpl.getInstance();
        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        daoField = DeviceManagementServiceImpl.class.getDeclaredField("deviceManagementDAO");
        daoField.setAccessible(true);
        originalDao = (DeviceManagementDAO) daoField.get(service);
    }

    @AfterClass
    public void tearDownClass() throws Exception {

        daoField.set(service, originalDao);
        identityTenantUtilMocked.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        dao = mock(DeviceManagementDAO.class);
        daoField.set(service, dao);
    }

    @Test
    public void testPersistDeviceDelegatesToDao() throws Exception {

        Device device = buildDevice("d1", "alice@example.com");
        when(dao.registerDevice(any(), eq(TENANT_ID))).thenReturn(device);

        service.persistDevice(device, TENANT_DOMAIN);

        verify(dao).registerDevice(any(), eq(TENANT_ID));
    }

    @Test
    public void testPersistDeviceWithoutUserIdThrows() {

        Device device = buildDevice("d1", null);

        try {
            service.persistDevice(device, TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtServerException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_USER_ID_REQUIRED.getCode());
        }
    }

    @Test
    public void testGetDeviceByIdDelegatesToDao() throws Exception {

        Device device = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(device);

        Device result = service.getDeviceById("d1", TENANT_DOMAIN);

        Assert.assertEquals(result, device);
    }

    @Test
    public void testGetDeviceByIdWithBlankIdThrows() {

        try {
            service.getDeviceById("", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testUpdateDeviceNameWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(null);

        try {
            service.updateDeviceName("d1", "New Name", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testUpdateDeviceNameWithBlankArgsThrows() {

        try {
            service.updateDeviceName("", "New Name", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceId");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }

        try {
            service.updateDeviceName("d1", "  ", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException for blank deviceName");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testUpdateDeviceNameWhenDaoReturnsNullAfterRefetchThrowsNotNPE() throws Exception {

        Device existing = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(existing);
        when(dao.updateDeviceName("d1", "New Name", TENANT_ID)).thenReturn(null);

        try {
            service.updateDeviceName("d1", "New Name", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testDeleteDeviceWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(null);

        try {
            service.deleteDevice("d1", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testDeleteDeviceDelegatesToDao() throws Exception {

        Device device = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(device);

        service.deleteDevice("d1", TENANT_DOMAIN);

        verify(dao).deleteDevice("d1", TENANT_ID);
    }

    @Test
    public void testGetDevicesByUserIdWithBlankUserThrows() {

        try {
            service.getDevicesByUserId("", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testGetDevicesPassesValidLimitThrough() throws Exception {

        service.getDevices(TENANT_DOMAIN, 5, 20);

        verify(dao).getDevices(TENANT_ID, 5, 20);
    }

    @Test
    public void testGetDevicesCapsLimitAtMaximumItemsPerPage() throws Exception {

        int maximumItemsPerPage = IdentityUtil.getMaximumItemPerPage();

        service.getDevices(TENANT_DOMAIN, 0, maximumItemsPerPage + 5000);

        // The oversized page size must be capped, so the DAO never sees the caller's value.
        verify(dao).getDevices(TENANT_ID, 0, maximumItemsPerPage);
    }

    @Test
    public void testGetDevicesWithNegativeLimitThrows() {

        try {
            service.getDevices(TENANT_DOMAIN, 0, -1);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_INVALID_DEVICE_FIELD.getCode());
        }
    }

    @Test
    public void testDeactivateDeviceDelegatesToDao() throws Exception {

        Device existing = mock(Device.class);
        Device deactivated = buildDevice("d1", "alice@example.com", Device.Status.INACTIVE);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(existing);
        when(dao.changeDeviceStatus("d1", Device.Status.INACTIVE, TENANT_ID)).thenReturn(deactivated);

        Device result = service.deactivateDevice("d1", TENANT_DOMAIN);

        Assert.assertEquals(result.getStatus(), Device.Status.INACTIVE);
        verify(dao).changeDeviceStatus("d1", Device.Status.INACTIVE, TENANT_ID);
    }

    @Test
    public void testActivateDeviceDelegatesToDao() throws Exception {

        Device existing = mock(Device.class);
        Device activated = buildDevice("d1", "alice@example.com", Device.Status.ACTIVE);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(existing);
        when(dao.changeDeviceStatus("d1", Device.Status.ACTIVE, TENANT_ID)).thenReturn(activated);

        Device result = service.activateDevice("d1", TENANT_DOMAIN);

        Assert.assertEquals(result.getStatus(), Device.Status.ACTIVE);
        verify(dao).changeDeviceStatus("d1", Device.Status.ACTIVE, TENANT_ID);
    }

    @Test
    public void testDeactivateDeviceWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("unknown", TENANT_ID)).thenReturn(null);

        try {
            service.deactivateDevice("unknown", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testActivateDeviceWhenDeviceMissingThrows() throws Exception {

        when(dao.getDeviceById("unknown", TENANT_ID)).thenReturn(null);

        try {
            service.activateDevice("unknown", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testDeactivateDeviceWhenDaoReturnsNullAfterRefetchThrowsNotNPE() throws Exception {

        Device existing = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(existing);
        when(dao.changeDeviceStatus("d1", Device.Status.INACTIVE, TENANT_ID)).thenReturn(null);

        try {
            service.deactivateDevice("d1", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    @Test
    public void testActivateDeviceWhenDaoReturnsNullAfterRefetchThrowsNotNPE() throws Exception {

        Device existing = mock(Device.class);
        when(dao.getDeviceById("d1", TENANT_ID)).thenReturn(existing);
        when(dao.changeDeviceStatus("d1", Device.Status.ACTIVE, TENANT_ID)).thenReturn(null);

        try {
            service.activateDevice("d1", TENANT_DOMAIN);
            Assert.fail("Expected DeviceMgtClientException");
        } catch (DeviceMgtException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
    }

    private static Device buildDevice(String id, String userId) {

        return buildDevice(id, userId, Device.Status.ACTIVE);
    }

    private static Device buildDevice(String id, String userId, Device.Status status) {

        return new Device.Builder()
                .id(id)
                .userId(userId)
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .status(status)
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
    }
}
