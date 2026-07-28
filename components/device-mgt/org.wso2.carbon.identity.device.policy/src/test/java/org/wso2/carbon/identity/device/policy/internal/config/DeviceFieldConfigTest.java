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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class DeviceFieldConfigTest {

    private DeviceFieldConfig deviceFieldConfig;

    @BeforeMethod
    public void setUp() {
        deviceFieldConfig = new DeviceFieldConfig();
    }

    @Test
    public void testGetName() {
        deviceFieldConfig.setName("testName");
        Assert.assertEquals(deviceFieldConfig.getName(), "testName");
    }

    @Test
    public void testGetApplicablePlatformsNull() {
        Assert.assertNotNull(deviceFieldConfig.getApplicablePlatforms());
        Assert.assertTrue(deviceFieldConfig.getApplicablePlatforms().isEmpty());
    }

    @Test
    public void testGetApplicablePlatforms() {
        deviceFieldConfig.setApplicablePlatforms(Arrays.asList("Android", "iOS"));
        Assert.assertEquals(deviceFieldConfig.getApplicablePlatforms().size(), 2);
        Assert.assertTrue(deviceFieldConfig.getApplicablePlatforms().contains("Android"));
    }

    @Test
    public void testIsUniversal() {
        Assert.assertTrue(deviceFieldConfig.isUniversal());
        
        deviceFieldConfig.setApplicablePlatforms(Collections.emptyList());
        Assert.assertTrue(deviceFieldConfig.isUniversal());

        deviceFieldConfig.setApplicablePlatforms(Arrays.asList("Android"));
        Assert.assertFalse(deviceFieldConfig.isUniversal());
    }

    @Test
    public void testIsApplicableTo() {
        Assert.assertTrue(deviceFieldConfig.isApplicableTo("Android"));

        deviceFieldConfig.setApplicablePlatforms(Arrays.asList("Android", "iOS"));
        Assert.assertTrue(deviceFieldConfig.isApplicableTo("Android"));
        Assert.assertTrue(deviceFieldConfig.isApplicableTo("iOS"));
        Assert.assertFalse(deviceFieldConfig.isApplicableTo("Windows"));
    }
}
