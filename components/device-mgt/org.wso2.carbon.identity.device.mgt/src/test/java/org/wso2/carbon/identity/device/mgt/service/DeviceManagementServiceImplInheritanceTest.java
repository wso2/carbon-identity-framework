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


package org.wso2.carbon.identity.device.mgt.service;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.constant.ErrorMessage;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtClientException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtServerException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.model.UserDeviceAssociation;
import org.wso2.carbon.identity.device.mgt.internal.component.DeviceMgtComponentServiceHolder;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.service.impl.DeviceManagementServiceImpl;
import org.wso2.carbon.identity.organization.management.organization.user.sharing.OrganizationUserSharingService;
import org.wso2.carbon.identity.organization.management.organization.user.sharing.models.UserAssociation;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementServerException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the devices that shared users inherit through {@link DeviceManagementServiceImpl}.
 */
@WithCarbonHome
public class DeviceManagementServiceImplInheritanceTest {

    private static final String ROOT_TENANT = "carbon.super";
    private static final String ROOT_ORG_ID = "root-org-id";
    private static final int ROOT_TENANT_ID = -1234;
    private static final String PARENT_TENANT = "parent-tenant";
    private static final String PARENT_ORG_ID = "parent-org-id";
    private static final int PARENT_TENANT_ID = 2;
    private static final String SUB_TENANT = "sub-tenant";
    private static final String SUB_ORG_ID = "sub-org-id";
    private static final int SUB_TENANT_ID = 3;

    private static final String SHARED_USER_ID = "shared-user";
    private static final String RESIDENT_USER_ID = "resident-user";
    private static final String SUB_ORG_USER_ID = "sub-org-user";
    private static final String DEVICE_ID = "device-1";

    private DeviceManagementServiceImpl service;
    private DeviceManagementDAO dao;
    private OrganizationManager organizationManager;
    private OrganizationUserSharingService userSharingService;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<OrganizationManagementUtil> organizationManagementUtil;
    private Field daoField;
    private DeviceManagementDAO originalDao;

    @BeforeClass
    public void setUpClass() throws Exception {

        service = DeviceManagementServiceImpl.getInstance();
        daoField = DeviceManagementServiceImpl.class.getDeclaredField("deviceManagementDAO");
        daoField.setAccessible(true);
        originalDao = (DeviceManagementDAO) daoField.get(service);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(ROOT_TENANT)).thenReturn(ROOT_TENANT_ID);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(PARENT_TENANT)).thenReturn(PARENT_TENANT_ID);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(SUB_TENANT)).thenReturn(SUB_TENANT_ID);
        organizationManagementUtil = mockStatic(OrganizationManagementUtil.class);
        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(ROOT_TENANT))
                .thenReturn(false);
        organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(SUB_TENANT))
                .thenReturn(true);
    }

    @AfterClass
    public void tearDownClass() throws Exception {

        daoField.set(service, originalDao);
        identityTenantUtil.close();
        organizationManagementUtil.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        dao = mock(DeviceManagementDAO.class);
        daoField.set(service, dao);

        organizationManager = mock(OrganizationManager.class);
        when(organizationManager.resolveOrganizationId(SUB_TENANT)).thenReturn(SUB_ORG_ID);
        when(organizationManager.resolveTenantDomain(ROOT_ORG_ID)).thenReturn(ROOT_TENANT);
        when(organizationManager.resolveTenantDomain(PARENT_ORG_ID)).thenReturn(PARENT_TENANT);
        when(organizationManager.resolveTenantDomain(SUB_ORG_ID)).thenReturn(SUB_TENANT);
        userSharingService = mock(OrganizationUserSharingService.class);

        DeviceMgtComponentServiceHolder.getInstance().setOrganizationManager(organizationManager);
        DeviceMgtComponentServiceHolder.getInstance().setOrganizationUserSharingService(userSharingService);
    }

    @AfterMethod
    public void tearDown() {

        DeviceMgtComponentServiceHolder.getInstance().setOrganizationManager(null);
        DeviceMgtComponentServiceHolder.getInstance().setOrganizationUserSharingService(null);
    }

    @Test
    public void testSharedUserGetsDevicesOfResidentUser() throws Exception {

        shareUserFrom(ROOT_ORG_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID)));
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesByUserId(SHARED_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices.size(), 1);
        Device device = devices.get(0);
        Assert.assertEquals(device.getId(), DEVICE_ID);
        Assert.assertEquals(device.getUserId(), SHARED_USER_ID);
        Assert.assertEquals(service.getDeviceCountByUserId(SHARED_USER_ID, SUB_TENANT), 1);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(SUB_TENANT_ID), anyInt(), anyInt());
        verify(dao, never()).getDeviceCountByUserId(anyString(), eq(SUB_TENANT_ID));
    }

    @Test
    public void testSharedUserGetsDevicesFromIntermediateResidentOrganization() throws Exception {

        shareUserFrom(PARENT_ORG_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, PARENT_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID)));

        List<Device> devices = service.getDevicesByUserId(SHARED_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices.size(), 1);
        Assert.assertEquals(devices.get(0).getUserId(), SHARED_USER_ID);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(ROOT_TENANT_ID), anyInt(), anyInt());
    }

    @Test
    public void testOrganizationUserGetsOwnDevices() throws Exception {

        List<Device> ownDevices = Collections.singletonList(buildDevice(DEVICE_ID, SUB_ORG_USER_ID));
        when(dao.getDevicesByUserId(SUB_ORG_USER_ID, SUB_TENANT_ID, 0, 10)).thenReturn(ownDevices);
        when(dao.getDeviceCountByUserId(SUB_ORG_USER_ID, SUB_TENANT_ID)).thenReturn(1);

        Assert.assertSame(service.getDevicesByUserId(SUB_ORG_USER_ID, SUB_TENANT, 0, 10), ownDevices);
        Assert.assertEquals(service.getDeviceCountByUserId(SUB_ORG_USER_ID, SUB_TENANT), 1);
        verify(userSharingService, never()).getUserAssociationOfAssociatedUserByOrgId(anyString(), anyString());
    }

    @Test
    public void testRootOrganizationUserGetsOwnDevices() throws Exception {

        List<Device> ownDevices = Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID));
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 0, 10)).thenReturn(ownDevices);
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID)).thenReturn(1);

        Assert.assertSame(service.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT, 0, 10), ownDevices);
        Assert.assertEquals(service.getDeviceCountByUserId(RESIDENT_USER_ID, ROOT_TENANT), 1);
        verify(userSharingService, never()).getUserAssociation(anyString(), anyString());
    }

    @Test
    public void testAssociationWithCurrentOrganizationIsNotShared() throws Exception {

        shareUserFrom(SUB_ORG_ID);
        List<Device> ownDevices = Collections.singletonList(buildDevice(DEVICE_ID, SHARED_USER_ID));
        when(dao.getDevicesByUserId(SHARED_USER_ID, SUB_TENANT_ID, 0, 10)).thenReturn(ownDevices);

        Assert.assertSame(service.getDevicesByUserId(SHARED_USER_ID, SUB_TENANT, 0, 10), ownDevices);
        verify(dao, never()).getDevicesByUserId(eq(RESIDENT_USER_ID), anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testInheritedDeviceIsNotFoundById() throws Exception {

        mockInheritedDevice();

        Assert.assertNull(service.getDeviceById(DEVICE_ID, SUB_TENANT));
    }

    @Test
    public void testSharedUserGetsInheritedDeviceByResidentUserId() throws Exception {

        mockInheritedDevice();
        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenReturn(buildAssociation(ROOT_ORG_ID));

        Device device = service.getDeviceOfResidentUser(DEVICE_ID, RESIDENT_USER_ID, SUB_TENANT);

        Assert.assertNotNull(device);
        Assert.assertEquals(device.getId(), DEVICE_ID);
        Assert.assertEquals(device.getUserId(), SHARED_USER_ID);
        verify(dao, never()).getDeviceById(anyString(), eq(SUB_TENANT_ID));
    }

    @Test
    public void testSharedUserDoesNotGetDeviceOfAnotherResidentUser() throws Exception {

        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenReturn(buildAssociation(ROOT_ORG_ID));
        when(dao.getDeviceById(DEVICE_ID, ROOT_TENANT_ID)).thenReturn(buildDevice(DEVICE_ID, "another-user"));

        Assert.assertNull(service.getDeviceOfResidentUser(DEVICE_ID, RESIDENT_USER_ID, SUB_TENANT));
    }

    @Test
    public void testOrganizationUserGetsOwnDeviceByResidentUserId() throws Exception {

        Device ownDevice = buildDevice(DEVICE_ID, SUB_ORG_USER_ID);
        when(dao.getDeviceById(DEVICE_ID, SUB_TENANT_ID)).thenReturn(ownDevice);

        Assert.assertSame(service.getDeviceOfResidentUser(DEVICE_ID, SUB_ORG_USER_ID, SUB_TENANT), ownDevice);
        Assert.assertNull(service.getDeviceOfResidentUser(DEVICE_ID, "another-user", SUB_TENANT));
        verify(dao, never()).getDeviceById(anyString(), eq(ROOT_TENANT_ID));
    }

    @Test
    public void testRootUserGetsOwnDeviceByResidentUserId() throws Exception {

        Device ownDevice = buildDevice(DEVICE_ID, RESIDENT_USER_ID);
        when(dao.getDeviceById(DEVICE_ID, ROOT_TENANT_ID)).thenReturn(ownDevice);

        Assert.assertSame(service.getDeviceOfResidentUser(DEVICE_ID, RESIDENT_USER_ID, ROOT_TENANT), ownDevice);
        Assert.assertNull(service.getDeviceOfResidentUser(DEVICE_ID, "another-user", ROOT_TENANT));
        verify(userSharingService, never()).getUserAssociationOfAssociatedUserByOrgId(anyString(), anyString());
    }

    @Test
    public void testAssociationWithCurrentOrganizationIsNotSharedForDeviceById() throws Exception {

        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenReturn(buildAssociation(SUB_ORG_ID));
        Device ownDevice = buildDevice(DEVICE_ID, RESIDENT_USER_ID);
        when(dao.getDeviceById(DEVICE_ID, SUB_TENANT_ID)).thenReturn(ownDevice);

        Assert.assertSame(service.getDeviceOfResidentUser(DEVICE_ID, RESIDENT_USER_ID, SUB_TENANT), ownDevice);
        verify(dao, never()).getDeviceById(anyString(), eq(ROOT_TENANT_ID));
    }

    @Test
    public void testOrganizationLookupFailureIsServerErrorForDeviceById() throws Exception {

        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenThrow(new OrganizationManagementServerException("error"));

        try {
            service.getDeviceOfResidentUser(DEVICE_ID, RESIDENT_USER_ID, SUB_TENANT);
            Assert.fail("Expected a server error.");
        } catch (DeviceMgtServerException e) {
            Assert.assertEquals(e.getErrorCode(),
                    ErrorMessage.ERROR_WHILE_RESOLVING_INHERITED_DEVICES.getCode());
        }
    }

    @Test
    public void testSharedUserGetsDevicesByResidentUserId() throws Exception {

        shareResidentUserFrom(ROOT_ORG_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID)));
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesOfResidentUser(RESIDENT_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices.size(), 1);
        Assert.assertEquals(devices.get(0).getId(), DEVICE_ID);
        Assert.assertEquals(devices.get(0).getUserId(), SHARED_USER_ID);
        Assert.assertEquals(service.getDeviceCountOfResidentUser(RESIDENT_USER_ID, SUB_TENANT), 1);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(SUB_TENANT_ID), anyInt(), anyInt());
        verify(dao, never()).getDeviceCountByUserId(anyString(), eq(SUB_TENANT_ID));
        verify(userSharingService, never()).getUserAssociation(anyString(), anyString());
    }

    @Test
    public void testSharedUserGetsDevicesByResidentUserIdFromIntermediateOrganization() throws Exception {

        shareResidentUserFrom(PARENT_ORG_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, PARENT_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID)));
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, PARENT_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesOfResidentUser(RESIDENT_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices.size(), 1);
        Assert.assertEquals(devices.get(0).getUserId(), SHARED_USER_ID);
        Assert.assertEquals(service.getDeviceCountOfResidentUser(RESIDENT_USER_ID, SUB_TENANT), 1);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(ROOT_TENANT_ID), anyInt(), anyInt());
        verify(dao, never()).getDevicesByUserId(anyString(), eq(SUB_TENANT_ID), anyInt(), anyInt());
    }

    @Test
    public void testOrganizationUserGetsOwnDevicesByResidentUserId() throws Exception {

        Device ownDevice = buildDevice(DEVICE_ID, SUB_ORG_USER_ID);
        when(dao.getDevicesByUserId(SUB_ORG_USER_ID, SUB_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(ownDevice));
        when(dao.getDeviceCountByUserId(SUB_ORG_USER_ID, SUB_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesOfResidentUser(SUB_ORG_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices, Collections.singletonList(ownDevice));
        Assert.assertEquals(service.getDeviceCountOfResidentUser(SUB_ORG_USER_ID, SUB_TENANT), 1);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(ROOT_TENANT_ID), anyInt(), anyInt());
    }

    @Test
    public void testRootUserGetsOwnDevicesByResidentUserId() throws Exception {

        Device ownDevice = buildDevice(DEVICE_ID, RESIDENT_USER_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(ownDevice));
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesOfResidentUser(RESIDENT_USER_ID, ROOT_TENANT, 0, 10);

        Assert.assertEquals(devices, Collections.singletonList(ownDevice));
        Assert.assertEquals(service.getDeviceCountOfResidentUser(RESIDENT_USER_ID, ROOT_TENANT), 1);
        verify(userSharingService, never()).getUserAssociationOfAssociatedUserByOrgId(anyString(), anyString());
    }

    @Test
    public void testAssociationWithCurrentOrganizationIsNotSharedForDevicesByResidentUserId() throws Exception {

        shareResidentUserFrom(SUB_ORG_ID);
        Device ownDevice = buildDevice(DEVICE_ID, RESIDENT_USER_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, SUB_TENANT_ID, 0, 10))
                .thenReturn(Collections.singletonList(ownDevice));
        when(dao.getDeviceCountByUserId(RESIDENT_USER_ID, SUB_TENANT_ID)).thenReturn(1);

        List<Device> devices = service.getDevicesOfResidentUser(RESIDENT_USER_ID, SUB_TENANT, 0, 10);

        Assert.assertEquals(devices, Collections.singletonList(ownDevice));
        Assert.assertEquals(service.getDeviceCountOfResidentUser(RESIDENT_USER_ID, SUB_TENANT), 1);
        verify(dao, never()).getDevicesByUserId(anyString(), eq(ROOT_TENANT_ID), anyInt(), anyInt());
    }

    @Test
    public void testDevicesByResidentUserIdPassesPagination() throws Exception {

        shareResidentUserFrom(ROOT_ORG_ID);
        when(dao.getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 5, 2))
                .thenReturn(Collections.singletonList(buildDevice(DEVICE_ID, RESIDENT_USER_ID)));

        List<Device> devices = service.getDevicesOfResidentUser(RESIDENT_USER_ID, SUB_TENANT, 5, 2);

        Assert.assertEquals(devices.size(), 1);
        verify(dao).getDevicesByUserId(RESIDENT_USER_ID, ROOT_TENANT_ID, 5, 2);
    }

    @DataProvider(name = "residentUserOperations")
    public Object[][] residentUserOperations() {

        return new Object[][]{
                {(DeviceOperation) () -> service.getDevicesOfResidentUser(RESIDENT_USER_ID, SUB_TENANT, 0, 10)},
                {(DeviceOperation) () -> service.getDeviceCountOfResidentUser(RESIDENT_USER_ID, SUB_TENANT)}
        };
    }

    @Test(dataProvider = "residentUserOperations")
    public void testOrganizationLookupFailureIsServerErrorForResidentUser(DeviceOperation operation)
            throws Exception {

        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenThrow(new OrganizationManagementServerException("error"));

        try {
            operation.run();
            Assert.fail("Expected a server error.");
        } catch (DeviceMgtServerException e) {
            Assert.assertEquals(e.getErrorCode(),
                    ErrorMessage.ERROR_WHILE_RESOLVING_INHERITED_DEVICES.getCode());
        }
    }

    @DataProvider(name = "updateOperations")
    public Object[][] updateOperations() {

        return new Object[][]{
                {(DeviceOperation) () -> service.updateDeviceName(DEVICE_ID, "New name", SUB_TENANT)},
                {(DeviceOperation) () -> service.activateDevice(DEVICE_ID, SUB_TENANT)},
                {(DeviceOperation) () -> service.deactivateDevice(DEVICE_ID, SUB_TENANT)}
        };
    }

    @Test(dataProvider = "updateOperations")
    public void testUpdatingInheritedDeviceIsNotFound(DeviceOperation operation) throws Exception {

        mockInheritedDevice();

        try {
            operation.run();
            Assert.fail("Expected a device not found error.");
        } catch (DeviceMgtClientException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_DEVICE_NOT_FOUND.getCode());
        }
        verify(dao, never()).updateDeviceName(anyString(), anyString(), anyInt());
        verify(dao, never()).changeDeviceStatus(anyString(), any(), anyInt());
    }

    @Test
    public void testDeletingInheritedDeviceIsNoOp() throws Exception {

        mockInheritedDevice();

        service.deleteDevice(DEVICE_ID, SUB_TENANT);
        verify(dao, never()).deleteDevice(anyString(), anyInt());
    }

    @Test
    public void testOrganizationLookupFailureIsServerError() throws Exception {

        when(organizationManager.resolveOrganizationId(SUB_TENANT))
                .thenThrow(new OrganizationManagementServerException("error"));

        try {
            service.getDevicesByUserId(SHARED_USER_ID, SUB_TENANT, 0, 10);
            Assert.fail("Expected a server error.");
        } catch (DeviceMgtServerException e) {
            Assert.assertEquals(e.getErrorCode(),
                    ErrorMessage.ERROR_WHILE_RESOLVING_INHERITED_DEVICES.getCode());
        }
    }

    @Test
    public void testDeviceCannotBeRegisteredForSharedUser() throws Exception {

        shareUserFrom(ROOT_ORG_ID);

        assertSharedUserRegistrationRejected(() -> service.registerDevice(buildDevice(DEVICE_ID, SHARED_USER_ID),
                new UserDeviceAssociation(DEVICE_ID, SHARED_USER_ID), SUB_TENANT));
        verify(dao, never()).registerDevice(any(), any(), anyInt());
    }

    @Test
    public void testDeviceCanBeRegisteredForOrganizationUser() throws Exception {

        Device device = buildDevice(DEVICE_ID, SUB_ORG_USER_ID);
        when(dao.registerDevice(any(), any(), eq(SUB_TENANT_ID))).thenReturn(device);

        Device registered = service.registerDevice(device, new UserDeviceAssociation(DEVICE_ID, SUB_ORG_USER_ID),
                SUB_TENANT);

        Assert.assertEquals(registered.getId(), DEVICE_ID);
        verify(dao).registerDevice(any(), any(), eq(SUB_TENANT_ID));
    }

    @Test
    public void testDeviceCanBeRegisteredInRootOrganization() throws Exception {

        Device device = buildDevice(DEVICE_ID, RESIDENT_USER_ID);
        when(dao.registerDevice(any(), any(), eq(ROOT_TENANT_ID))).thenReturn(device);

        service.registerDevice(device, new UserDeviceAssociation(DEVICE_ID, RESIDENT_USER_ID), ROOT_TENANT);

        verify(dao).registerDevice(any(), any(), eq(ROOT_TENANT_ID));
        verify(userSharingService, never()).getUserAssociation(anyString(), anyString());
    }

    private void assertSharedUserRegistrationRejected(DeviceOperation operation) {

        try {
            operation.run();
            Assert.fail("Expected the device registration for a shared user to be rejected.");
        } catch (DeviceMgtException e) {
            Assert.assertTrue(e instanceof DeviceMgtClientException);
            Assert.assertEquals(e.getErrorCode(), ErrorMessage.ERROR_DEVICE_REGISTRATION_FOR_SHARED_USER.getCode());
        }
    }

    private void mockInheritedDevice() throws Exception {

        shareUserFrom(ROOT_ORG_ID);
        when(dao.getDeviceById(DEVICE_ID, ROOT_TENANT_ID)).thenReturn(buildDevice(DEVICE_ID, RESIDENT_USER_ID));
    }

    private void shareUserFrom(String residentOrgId) throws Exception {

        when(userSharingService.getUserAssociation(SHARED_USER_ID, SUB_ORG_ID))
                .thenReturn(buildAssociation(residentOrgId));
    }

    private void shareResidentUserFrom(String residentOrgId) throws Exception {

        when(userSharingService.getUserAssociationOfAssociatedUserByOrgId(RESIDENT_USER_ID, SUB_ORG_ID))
                .thenReturn(buildAssociation(residentOrgId));
    }

    private static UserAssociation buildAssociation(String residentOrgId) {

        UserAssociation association = new UserAssociation();
        association.setUserId(SHARED_USER_ID);
        association.setOrganizationId(SUB_ORG_ID);
        association.setAssociatedUserId(RESIDENT_USER_ID);
        association.setUserResidentOrganizationId(residentOrgId);
        return association;
    }

    private static Device buildDevice(String id, String userId) {

        return new Device.Builder()
                .id(id)
                .userId(userId)
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .status(Device.Status.ACTIVE)
                .registeredAt(Timestamp.from(Instant.now()))
                .build();
    }

    /**
     * A device management operation under test.
     */
    @FunctionalInterface
    private interface DeviceOperation {

        void run() throws DeviceMgtException;
    }
}
