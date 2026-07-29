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
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.exception.DevicePolicyClientException;
import org.wso2.carbon.identity.device.policy.internal.util.DeviceTokenExtractor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeviceDataResolverImplTest {

    private DeviceDataResolverImpl deviceDataResolver;
    private MockedStatic<LogFactory> mockedLogFactory;

    @BeforeMethod
    public void setUp() {
        Log mockLog = mock(Log.class);
        when(mockLog.isDebugEnabled()).thenReturn(false);
        mockedLogFactory = mockStatic(LogFactory.class);
        mockedLogFactory.when(() -> LogFactory.getLog(DeviceDataResolverImpl.class)).thenReturn(mockLog);
        
        deviceDataResolver = new DeviceDataResolverImpl();
    }

    @AfterMethod
    public void tearDown() {
        if (mockedLogFactory != null) {
            mockedLogFactory.close();
        }
    }

    @Test
    public void testResolveDeviceDataNullRequest() {

        Optional<Map<String, Object>> result = deviceDataResolver.resolveDeviceData(null, "carbon.super");
        assertFalse(result.isPresent());
    }

    @Test
    public void testResolveDeviceDataNoToken() {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn(null);
        when(request.getHeader("X-Device-Token")).thenReturn(null);

        Optional<Map<String, Object>> result = deviceDataResolver.resolveDeviceData(request, "carbon.super");
        assertFalse(result.isPresent());
    }

    @Test
    public void testResolveDeviceDataSuccessFromParam() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("valid-token");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("deviceId", "device-123");

        try (MockedConstruction<DeviceTokenExtractor> mockedConstruction = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> {
                    when(mock.extractFromToken(anyString(), anyString())).thenReturn(mockData);
                })) {

            Optional<Map<String, Object>> result = deviceDataResolver.resolveDeviceData(request, "carbon.super");
            assertTrue(result.isPresent());
            assertEquals(result.get().get("deviceId"), "device-123");
        }
    }

    @Test
    public void testResolveDeviceDataSuccessFromHeader() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn(null);
        when(request.getHeader("X-Device-Token")).thenReturn("valid-token-header");

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("deviceId", "device-456");

        try (MockedConstruction<DeviceTokenExtractor> mockedConstruction = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> {
                    when(mock.extractFromToken(anyString(), anyString())).thenReturn(mockData);
                })) {

            Optional<Map<String, Object>> result = deviceDataResolver.resolveDeviceData(request, "carbon.super");
            assertTrue(result.isPresent());
            assertEquals(result.get().get("deviceId"), "device-456");
        }
    }

    @Test
    public void testResolveDeviceDataClientException() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("device_token")).thenReturn("invalid-token");

        try (MockedConstruction<DeviceTokenExtractor> mockedConstruction = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> {
                    when(mock.extractFromToken(anyString(), anyString())).thenThrow(
                            new DevicePolicyClientException("error", "invalid token", "description"));
                })) {

            Optional<Map<String, Object>> result = deviceDataResolver.resolveDeviceData(request, "carbon.super");
            assertFalse(result.isPresent());
        }
    }
}
