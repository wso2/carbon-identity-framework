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

package org.wso2.carbon.identity.device.policy.internal.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementServerException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DeviceFieldConfigLoaderTest {

    @BeforeMethod
    public void setUp() {
        System.setProperty("carbon.home", System.getProperty("user.dir") + "/src/test/resources");
    }

    @Test
    public void testLoadAndGetInstance() throws PolicyManagementServerException {
        Log mockLog = mock(Log.class);
        when(mockLog.isDebugEnabled()).thenReturn(false);
        try (MockedStatic<LogFactory> mockedLogFactory = mockStatic(LogFactory.class)) {
            mockedLogFactory.when(() -> LogFactory.getLog(DeviceFieldConfigLoader.class)).thenReturn(mockLog);
            DeviceFieldConfigLoader.load();
        }
        DeviceFieldConfigLoader loader = DeviceFieldConfigLoader.getInstance();
        
        Assert.assertNotNull(loader);
        
        List<DeviceFieldConfig> fields = loader.getFields();
        Assert.assertNotNull(fields);
        Assert.assertEquals(fields.size(), 2);
        
        DeviceFieldConfig field1 = fields.get(0);
        Assert.assertEquals(field1.getName(), "device_model");
        Assert.assertEquals(field1.getApplicablePlatforms().size(), 2);
        
        DeviceFieldConfig field2 = fields.get(1);
        Assert.assertEquals(field2.getName(), "os_version");
        Assert.assertEquals(field2.getApplicablePlatforms().size(), 0);
        Assert.assertTrue(field2.isUniversal());
    }
}
