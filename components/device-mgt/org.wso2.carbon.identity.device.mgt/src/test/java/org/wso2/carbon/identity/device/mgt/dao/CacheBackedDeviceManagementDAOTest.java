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

import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCache;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCacheEntry;
import org.wso2.carbon.identity.device.mgt.internal.cache.DeviceCacheKey;
import org.wso2.carbon.identity.device.mgt.internal.dao.DeviceManagementDAO;
import org.wso2.carbon.identity.device.mgt.internal.dao.impl.CacheBackedDeviceManagementDAO;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link CacheBackedDeviceManagementDAO}.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedDeviceManagementDAOTest {

    private static final String DEVICE_ID = "deviceId";
    private static final int TENANT_ID = 1;
    private static final int OTHER_TENANT_ID = 2;
    private static final String TENANT_DOMAIN = "tenant1.example.com";
    private static final String OTHER_TENANT_DOMAIN = "tenant2.example.com";

    private DeviceManagementDAO deviceManagementDAO;
    private CacheBackedDeviceManagementDAO cacheBackedDeviceManagementDAO;
    private DeviceCache deviceCache;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;

    @BeforeClass
    public void setUpClass() {

        // BaseCache resolves a tenant domain per tenantId internally (for its own OSGi cache
        // ownership checks); stub distinct domains so the two tenants used below are genuinely
        // isolated instead of both falling back to the same default domain.
        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class, CALLS_REAL_METHODS);
        when(IdentityTenantUtil.getTenantDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
        when(IdentityTenantUtil.getTenantDomain(OTHER_TENANT_ID)).thenReturn(OTHER_TENANT_DOMAIN);

        deviceCache = DeviceCache.getInstance();
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMocked.close();
    }

    @BeforeMethod
    public void setUp() {

        deviceManagementDAO = mock(DeviceManagementDAO.class);
        cacheBackedDeviceManagementDAO = new CacheBackedDeviceManagementDAO(deviceManagementDAO);

        // Reset cache state so tests don't leak into each other.
        deviceCache.clear(TENANT_ID);
        deviceCache.clear(OTHER_TENANT_ID);
    }

    @Test
    public void testRegisterDeviceDoesNotTouchCache() throws DeviceMgtException {

        Device device = mock(Device.class);
        when(deviceManagementDAO.registerDevice(device, TENANT_ID)).thenReturn(device);

        cacheBackedDeviceManagementDAO.registerDevice(device, TENANT_ID);

        verify(deviceManagementDAO).registerDevice(device, TENANT_ID);
        assertNull(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID));
    }

    @Test
    public void testGetDeviceByIdCacheMissThenCacheHit() throws DeviceMgtException {

        Device device = mock(Device.class);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(device);

        Device firstResult = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(firstResult, device);
        verify(deviceManagementDAO, times(1)).getDeviceById(DEVICE_ID, TENANT_ID);
        assertEquals(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID).getDevice(), device);

        Device secondResult = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(secondResult, device);
        // The inner DAO must not be invoked again — the second read is served from the cache.
        verify(deviceManagementDAO, times(1)).getDeviceById(DEVICE_ID, TENANT_ID);
    }

    @Test
    public void testGetDeviceByIdCacheHitWhenPrePopulated() throws DeviceMgtException {

        Device device = mock(Device.class);
        deviceCache.addToCache(new DeviceCacheKey(DEVICE_ID), new DeviceCacheEntry(device), TENANT_ID);

        Device result = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(result, device);
        verify(deviceManagementDAO, never()).getDeviceById(DEVICE_ID, TENANT_ID);
    }

    @Test
    public void testGetDeviceByIdNullResultIsNotCached() throws DeviceMgtException {

        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(null);

        Device result = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertNull(result);
        assertNull(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID));
    }

    @Test
    public void testUpdateDeviceNameInvalidatesCache() throws DeviceMgtException {

        Device cached = mock(Device.class);
        deviceCache.addToCache(new DeviceCacheKey(DEVICE_ID), new DeviceCacheEntry(cached), TENANT_ID);

        cacheBackedDeviceManagementDAO.updateDeviceName(DEVICE_ID, "New Name", TENANT_ID);

        verify(deviceManagementDAO).updateDeviceName(DEVICE_ID, "New Name", TENANT_ID);
        assertNull(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID));

        // The next read must miss the cache and hit the inner DAO again.
        Device fresh = mock(Device.class);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(fresh);
        Device result = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(result, fresh);
        verify(deviceManagementDAO).getDeviceById(DEVICE_ID, TENANT_ID);
    }

    @Test
    public void testChangeDeviceStatusInvalidatesCache() throws DeviceMgtException {

        Device cached = mock(Device.class);
        deviceCache.addToCache(new DeviceCacheKey(DEVICE_ID), new DeviceCacheEntry(cached), TENANT_ID);

        cacheBackedDeviceManagementDAO.changeDeviceStatus(DEVICE_ID, Device.Status.INACTIVE, TENANT_ID);

        verify(deviceManagementDAO).changeDeviceStatus(DEVICE_ID, Device.Status.INACTIVE, TENANT_ID);
        assertNull(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID));

        Device fresh = mock(Device.class);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(fresh);
        Device result = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(result, fresh);
        verify(deviceManagementDAO).getDeviceById(DEVICE_ID, TENANT_ID);
    }

    @Test
    public void testDeleteDeviceInvalidatesCache() throws DeviceMgtException {

        Device cached = mock(Device.class);
        deviceCache.addToCache(new DeviceCacheKey(DEVICE_ID), new DeviceCacheEntry(cached), TENANT_ID);

        cacheBackedDeviceManagementDAO.deleteDevice(DEVICE_ID, TENANT_ID);

        verify(deviceManagementDAO).deleteDevice(DEVICE_ID, TENANT_ID);
        assertNull(deviceCache.getValueFromCache(new DeviceCacheKey(DEVICE_ID), TENANT_ID));

        Device fresh = mock(Device.class);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(fresh);
        Device result = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);

        assertEquals(result, fresh);
        verify(deviceManagementDAO).getDeviceById(DEVICE_ID, TENANT_ID);
    }

    @Test
    public void testGetDevicesFilteredByUserDelegatesToDao() throws DeviceMgtException {

        Device device = mock(Device.class);
        when(deviceManagementDAO.getDevices(TENANT_ID, 0, 100, "alice@example.com"))
                .thenReturn(Collections.singletonList(device));

        List<Device> result = cacheBackedDeviceManagementDAO.getDevices(TENANT_ID, 0, 100, "alice@example.com");

        assertEquals(result, Collections.singletonList(device));
        verify(deviceManagementDAO).getDevices(TENANT_ID, 0, 100, "alice@example.com");
    }

    @Test
    public void testGetDeviceCountFilteredByUserDelegatesToDao() throws DeviceMgtException {

        when(deviceManagementDAO.getDeviceCount(TENANT_ID, "alice@example.com")).thenReturn(2);

        int result = cacheBackedDeviceManagementDAO.getDeviceCount(TENANT_ID, "alice@example.com");

        assertEquals(result, 2);
        verify(deviceManagementDAO).getDeviceCount(TENANT_ID, "alice@example.com");
    }

    @Test
    public void testTenantIsolation() throws DeviceMgtException {

        Device tenantDevice = mock(Device.class);
        Device otherTenantDevice = mock(Device.class);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID)).thenReturn(tenantDevice);
        when(deviceManagementDAO.getDeviceById(DEVICE_ID, OTHER_TENANT_ID)).thenReturn(otherTenantDevice);

        Device resultTenant = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);
        Device resultOtherTenant = cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, OTHER_TENANT_ID);

        assertEquals(resultTenant, tenantDevice);
        assertEquals(resultOtherTenant, otherTenantDevice);

        // Each tenant's cache entry is independent — re-reading each still hits the cache, not the DAO.
        cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, TENANT_ID);
        cacheBackedDeviceManagementDAO.getDeviceById(DEVICE_ID, OTHER_TENANT_ID);

        verify(deviceManagementDAO, times(1)).getDeviceById(DEVICE_ID, TENANT_ID);
        verify(deviceManagementDAO, times(1)).getDeviceById(DEVICE_ID, OTHER_TENANT_ID);
    }
}
