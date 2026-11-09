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

package org.wso2.carbon.identity.device.policy.internal.resolver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.handler.device.DeviceDataResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.device.ResolvedDeviceData;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.device.mgt.api.exception.DeviceMgtException;
import org.wso2.carbon.identity.device.mgt.api.model.Device;
import org.wso2.carbon.identity.device.mgt.api.service.DeviceManagementService;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyServerException;
import org.wso2.carbon.identity.device.policy.internal.component.DevicePolicyComponentServiceHolder;
import org.wso2.carbon.identity.device.policy.internal.constant.DeviceTokenConstants;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class DeviceDataResolverImplTest {

    private static final String DEVICE_ID = "device-id";
    private static final String OWNER_USER_ID = "owner-user";
    private static final String DEVICE_TENANT = "sub-org-tenant";

    private DeviceDataResolverImpl deviceDataResolver;
    private DeviceManagementService deviceManagementService;
    private MockedStatic<LogFactory> mockedLogFactory;

    @BeforeMethod
    public void setUp() {

        Log mockLog = mock(Log.class);
        when(mockLog.isDebugEnabled()).thenReturn(false);
        mockedLogFactory = mockStatic(LogFactory.class);
        mockedLogFactory.when(() -> LogFactory.getLog(DeviceDataResolverImpl.class)).thenReturn(mockLog);

        deviceDataResolver = new DeviceDataResolverImpl();
        deviceManagementService = mock(DeviceManagementService.class);
        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(deviceManagementService);
        // Claims are read through the framework, which validates the device with the registered resolver.
        DeviceDataResolver frameworkResolver = mock(DeviceDataResolver.class);
        when(frameworkResolver.isValidDevice(anyString(), anyString(), anyString())).thenReturn(true);
        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(frameworkResolver);
    }

    @AfterMethod
    public void tearDown() {

        if (mockedLogFactory != null) {
            mockedLogFactory.close();
        }
        DevicePolicyComponentServiceHolder.getInstance().setDeviceManagementService(null);
        FrameworkServiceDataHolder.getInstance().setDeviceDataResolver(null);
    }

    @Test
    public void testResolveNullRequest() {

        assertFalse(deviceDataResolver.resolve(null).isPresent());
    }

    @Test
    public void testResolveNoToken() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        assertFalse(deviceDataResolver.resolve(request).isPresent());
    }

    @Test
    public void testResolveSuccessFromParam() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("valid-token");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockExtractor(claims("platform", "android"))) {
            Optional<ResolvedDeviceData> result = deviceDataResolver.resolve(request);

            assertTrue(result.isPresent());
            assertEquals(ownerClaims(result.get()).get("platform"), "android");
        }
    }

    @Test
    public void testResolvePrefersTheHeaderOverTheParameter() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Device-Token")).thenReturn("header-token");
        when(request.getParameter("device_token")).thenReturn("param-token");

        try (MockedConstruction<DeviceTokenExtractor> extractor = mockExtractor(new HashMap<>())) {
            assertTrue(deviceDataResolver.resolve(request).isPresent());
            verify(extractor.constructed().get(0)).extract("header-token");
        }
    }

    @Test
    public void testResolveBindsTheDataToTheOwnerInTheDeviceTenant() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Device-Token")).thenReturn("token");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockExtractor(claims("platform", "ios"))) {
            ResolvedDeviceData result = deviceDataResolver.resolve(request).orElse(null);

            assertNotNull(result);
            assertEquals(ownerClaims(result).get("platform"), "ios");
            assertNull(result.getDeviceData("other-user", DEVICE_TENANT));
            assertNull(result.getDeviceData(OWNER_USER_ID, "carbon.super"));
        }
    }

    @Test
    public void testResolveInjectsAttestationHeaderIntoDeviceData() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("valid-token");
        when(request.getHeader("x-client-attestation")).thenReturn("attestation-header-value");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockExtractor(new HashMap<>())) {
            ResolvedDeviceData result = deviceDataResolver.resolve(request).orElse(null);

            assertNotNull(result);
            assertEquals(ownerClaims(result).get(DeviceTokenConstants.ATTESTATION_TOKEN_KEY),
                    "attestation-header-value");
        }
    }

    @Test
    public void testResolveWithoutAttestationHeaderDoesNotInjectKey() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("valid-token");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockExtractor(new HashMap<>())) {
            ResolvedDeviceData result = deviceDataResolver.resolve(request).orElse(null);

            assertNotNull(result);
            assertFalse(ownerClaims(result).containsKey(DeviceTokenConstants.ATTESTATION_TOKEN_KEY));
        }
    }

    @Test
    public void testResolveClientException() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("invalid-token");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> when(mock.extract(anyString())).thenThrow(
                        new DevicePolicyClientException("error", "invalid token", "description")))) {
            assertFalse(deviceDataResolver.resolve(request).isPresent());
        }
    }

    @Test
    public void testResolveServerException() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("token");

        try (MockedConstruction<DeviceTokenExtractor> ignored = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> when(mock.extract(anyString())).thenThrow(
                        new DevicePolicyServerException("error", "lookup failed", "description")))) {
            assertFalse(deviceDataResolver.resolve(request).isPresent());
        }
    }

    @Test
    public void testIsValidDeviceForActiveDeviceOfTheUser() throws Exception {

        Device device = device(OWNER_USER_ID, Device.Status.ACTIVE);
        when(deviceManagementService.getDeviceById(DEVICE_ID, DEVICE_TENANT)).thenReturn(device);

        assertTrue(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT));
    }

    @Test
    public void testIsValidDeviceFollowsStatusChanges() throws Exception {

        // Deactivated after initiation, then reactivated.
        Device active = device(OWNER_USER_ID, Device.Status.ACTIVE);
        Device inactive = device(OWNER_USER_ID, Device.Status.INACTIVE);
        when(deviceManagementService.getDeviceById(DEVICE_ID, DEVICE_TENANT)).thenReturn(inactive, active);

        assertFalse(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT));
        assertTrue(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT));
        verify(deviceManagementService, times(2)).getDeviceById(DEVICE_ID, DEVICE_TENANT);
    }

    @DataProvider(name = "invalidDeviceProvider")
    public Object[][] invalidDeviceProvider() {

        return new Object[][]{
                {null},
                {device("other-user", Device.Status.ACTIVE)},
                {device(OWNER_USER_ID, Device.Status.INACTIVE)},
        };
    }

    @Test(dataProvider = "invalidDeviceProvider")
    public void testIsValidDeviceForInvalidDevice(Device device) throws Exception {

        when(deviceManagementService.getDeviceById(DEVICE_ID, DEVICE_TENANT)).thenReturn(device);

        assertFalse(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT));
    }

    @Test
    public void testIsValidDeviceWhenLookupFails() throws Exception {

        when(deviceManagementService.getDeviceById(DEVICE_ID, DEVICE_TENANT))
                .thenThrow(new DeviceMgtException("lookup failed", "desc", "DM-65000"));

        assertFalse(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT));
    }

    @Test
    public void testIsValidDeviceWithBlankArguments() throws Exception {

        assertFalse(deviceDataResolver.isValidDevice(null, OWNER_USER_ID, DEVICE_TENANT));
        assertFalse(deviceDataResolver.isValidDevice(DEVICE_ID, "", DEVICE_TENANT));
        assertFalse(deviceDataResolver.isValidDevice(DEVICE_ID, OWNER_USER_ID, null));
        verify(deviceManagementService, times(0)).getDeviceById(any(), any());
    }

    private MockedConstruction<DeviceTokenExtractor> mockExtractor(Map<String, Object> claims) {

        return mockConstruction(DeviceTokenExtractor.class, (mock, context) -> when(mock.extract(anyString()))
                .thenReturn(new DeviceTokenExtractor.VerifiedDeviceToken(DEVICE_ID, OWNER_USER_ID, DEVICE_TENANT,
                        claims)));
    }

    private Map<String, Object> ownerClaims(ResolvedDeviceData deviceData) {

        return deviceData.getDeviceData(OWNER_USER_ID, DEVICE_TENANT);
    }

    private static Map<String, Object> claims(String key, Object value) {

        Map<String, Object> claims = new HashMap<>();
        claims.put(key, value);
        return claims;
    }

    private static Device device(String userId, Device.Status status) {

        Device device = mock(Device.class);
        when(device.getUserId()).thenReturn(userId);
        when(device.getStatus()).thenReturn(status);
        return device;
    }
}
