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

package org.wso2.carbon.identity.application.authentication.framework.handler.device;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.expectThrows;

/**
 * Unit tests for {@link ResolvedDeviceData}.
 */
public class ResolvedDeviceDataTest {

    private static final String DEVICE_ID = "device-1";
    private static final String OWNER_ID = "user-1";
    private static final String TENANT_DOMAIN = "sub-org-tenant";

    private DeviceDataResolver deviceDataResolver;
    private ResolvedDeviceData resolvedDeviceData;

    @BeforeMethod
    public void setUp() {

        deviceDataResolver = mock(DeviceDataResolver.class);
        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(deviceDataResolver);
        resolvedDeviceData = new ResolvedDeviceData(DEVICE_ID, OWNER_ID, TENANT_DOMAIN,
                Collections.singletonMap("platform", "ANDROID"));
    }

    @AfterMethod
    public void tearDown() {

        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(null);
    }

    @Test
    public void testGetDeviceDataForOwnerInDeviceTenant() {

        when(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_ID, TENANT_DOMAIN)).thenReturn(true);

        Map<String, Object> deviceData = resolvedDeviceData.getDeviceData(OWNER_ID, TENANT_DOMAIN);

        assertEquals(deviceData, Collections.singletonMap("platform", "ANDROID"));
        expectThrows(UnsupportedOperationException.class, () -> deviceData.put("platform", "IOS"));
        // The returned data is a copy, so the stored data is not affected.
        assertEquals(resolvedDeviceData.getDeviceData(OWNER_ID, TENANT_DOMAIN).get("platform"), "ANDROID");
    }

    @DataProvider(name = "mismatchedUserProvider")
    public Object[][] mismatchedUserProvider() {

        return new Object[][]{
                {"user-2", TENANT_DOMAIN},
                {OWNER_ID, "carbon.super"},
                {"user-2", "carbon.super"},
                {null, TENANT_DOMAIN},
                {OWNER_ID, null},
        };
    }

    @Test(dataProvider = "mismatchedUserProvider")
    public void testGetDeviceDataForMismatchedUserOrTenant(String userId, String tenantDomain) {

        when(deviceDataResolver.isValidDevice(any(), any(), any())).thenReturn(true);

        assertNull(resolvedDeviceData.getDeviceData(userId, tenantDomain));
        verify(deviceDataResolver, never()).isValidDevice(any(), any(), any());
    }

    @Test
    public void testGetDeviceDataFollowsDeviceStatus() {

        // Deactivated after initiation, then reactivated.
        when(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_ID, TENANT_DOMAIN)).thenReturn(false, true);

        assertNull(resolvedDeviceData.getDeviceData(OWNER_ID, TENANT_DOMAIN));
        assertEquals(resolvedDeviceData.getDeviceData(OWNER_ID, TENANT_DOMAIN).get("platform"), "ANDROID");
    }

    @Test
    public void testGetDeviceDataWithoutResolver() {

        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(null);

        assertNull(resolvedDeviceData.getDeviceData(OWNER_ID, TENANT_DOMAIN));
    }

    @Test
    public void testGetDeviceDataWithoutClaims() {

        when(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_ID, TENANT_DOMAIN)).thenReturn(true);

        assertEquals(new ResolvedDeviceData(DEVICE_ID, OWNER_ID, TENANT_DOMAIN, null)
                .getDeviceData(OWNER_ID, TENANT_DOMAIN), new HashMap<>());
    }
}
