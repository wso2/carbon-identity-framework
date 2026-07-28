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

package org.wso2.carbon.identity.device.policy.internal.service.impl;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.internal.config.DeviceFieldConfig;
import org.wso2.carbon.identity.device.policy.internal.config.DeviceFieldConfigLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class DeviceFieldMetadataServiceImplTest {

    private DeviceFieldMetadataServiceImpl deviceFieldMetadataService;
    private MockedStatic<DeviceFieldConfigLoader> mockedConfigLoaderStatic;

    @BeforeMethod
    public void setUp() {

        deviceFieldMetadataService = new DeviceFieldMetadataServiceImpl();
        mockedConfigLoaderStatic = mockStatic(DeviceFieldConfigLoader.class);
    }

    @AfterMethod
    public void tearDown() {

        if (mockedConfigLoaderStatic != null) {
            mockedConfigLoaderStatic.close();
        }
    }

    @Test
    public void testGetFieldApplicablePlatforms() {

        DeviceFieldConfigLoader mockLoader = mock(DeviceFieldConfigLoader.class);
        mockedConfigLoaderStatic.when(DeviceFieldConfigLoader::getInstance).thenReturn(mockLoader);

        DeviceFieldConfig config1 = mock(DeviceFieldConfig.class);
        when(config1.isUniversal()).thenReturn(true);
        when(config1.getName()).thenReturn("universalField");

        DeviceFieldConfig config2 = mock(DeviceFieldConfig.class);
        when(config2.isUniversal()).thenReturn(false);
        when(config2.getName()).thenReturn("specificField");
        when(config2.getApplicablePlatforms()).thenReturn(Arrays.asList("Android", "iOS"));

        when(mockLoader.getFields()).thenReturn(Arrays.asList(config1, config2));

        Map<String, List<String>> result = deviceFieldMetadataService.getFieldApplicablePlatforms();

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertTrue(result.containsKey("specificField"));
        assertEquals(result.get("specificField").size(), 2);
        assertTrue(result.get("specificField").contains("Android"));
    }
}
