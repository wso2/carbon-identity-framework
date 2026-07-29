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

import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.api.model.Platform;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DeviceFieldMetadataServiceImplTest {

    @Test
    public void testGetFieldsForAndroid() throws Exception {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        List<String> fields = service.getFieldsForPlatform(Platform.ANDROID);

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "androidOsVersion", "isRooted", "usbDebugging",
                "hardwareKeystore", "biometric", "screenLockComplexity", "diskEncryption",
                "networkProxies", "wifiNetworkSecurity", "androidIntegrity"
        );
        assertEquals(fields, expected);
    }

    @Test
    public void testGetFieldsForIos() throws Exception {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        List<String> fields = service.getFieldsForPlatform(Platform.IOS);

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "iosOsVersion", "iosIntegrity",
                "passcode", "touchIdOrFaceId", "jailbreak"
        );
        assertEquals(fields, expected);
    }

    @Test
    public void testGetFieldsForMacos() throws Exception {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        List<String> fields = service.getFieldsForPlatform(Platform.MACOS);

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "macosOsVersion", "diskEncryption", "secureEnclave"
        );
        assertEquals(fields, expected);
    }

    @Test
    public void testGetFieldsForWindows() throws Exception {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        List<String> fields = service.getFieldsForPlatform(Platform.WINDOWS);

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "windowsOsVersion", "diskEncryption",
                "windowsHello", "trustedPlatformModule"
        );
        assertEquals(fields, expected);
    }

    @Test
    public void testGetFieldsForNullPlatform() throws Exception {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        List<String> fields = service.getFieldsForPlatform(null);

        assertEquals(fields.size(), 22);
    }

    @Test
    public void testGetSupportedPlatforms() {

        DeviceFieldMetadataServiceImpl service = new DeviceFieldMetadataServiceImpl();
        Set<Platform> supportedPlatforms = service.getSupportedPlatforms();

        assertNotNull(supportedPlatforms);
        assertEquals(supportedPlatforms, EnumSet.allOf(Platform.class));
    }
}
