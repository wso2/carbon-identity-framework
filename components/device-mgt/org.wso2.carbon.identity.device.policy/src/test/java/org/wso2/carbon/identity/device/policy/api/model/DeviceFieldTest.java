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

package org.wso2.carbon.identity.device.policy.api.model;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DeviceFieldTest {

    @Test
    public void testFromNameValid() {

        Assert.assertEquals(DeviceField.fromName("platform"), DeviceField.PLATFORM);
        Assert.assertEquals(DeviceField.fromName("androidOsVersion"), DeviceField.ANDROID_OS_VERSION);
        Assert.assertEquals(DeviceField.fromName("diskEncryption"), DeviceField.DISK_ENCRYPTION);
    }

    @Test
    public void testFromNameInvalidAndNull() {

        Assert.assertNull(DeviceField.fromName("unknown_field"));
        Assert.assertNull(DeviceField.fromName(""));
        Assert.assertNull(DeviceField.fromName("  "));
        Assert.assertNull(DeviceField.fromName(null));
    }

    @Test
    public void testAppliesTo() {

        // Matching platform
        Assert.assertTrue(DeviceField.ANDROID_OS_VERSION.appliesTo(Platform.ANDROID));

        // Non-matching platform
        Assert.assertFalse(DeviceField.ANDROID_OS_VERSION.appliesTo(Platform.IOS));

        // Universal field applies to all platforms
        Assert.assertTrue(DeviceField.PLATFORM.appliesTo(Platform.ANDROID));
        Assert.assertTrue(DeviceField.PLATFORM.appliesTo(Platform.IOS));
        Assert.assertTrue(DeviceField.PLATFORM.appliesTo(Platform.MACOS));
        Assert.assertTrue(DeviceField.PLATFORM.appliesTo(Platform.WINDOWS));

        // Null platform means all fields apply
        Assert.assertTrue(DeviceField.ANDROID_OS_VERSION.appliesTo(null));
        Assert.assertTrue(DeviceField.PLATFORM.appliesTo(null));
    }

    @Test
    public void testEveryConstantHasNonBlankNameAndNonEmptyPlatforms() {

        for (DeviceField field : DeviceField.values()) {
            Assert.assertNotNull(field.getName());
            Assert.assertFalse(field.getName().trim().isEmpty());
            Assert.assertNotNull(field.getPlatforms());
            Assert.assertFalse(field.getPlatforms().isEmpty());
        }
    }

    @Test
    public void testAllWireNamesAreUnique() {

        Set<String> names = new HashSet<>();
        for (DeviceField field : DeviceField.values()) {
            Assert.assertTrue(names.add(field.getName()), "Duplicate wire name found: " + field.getName());
        }
        Assert.assertEquals(names.size(), 22);
    }

    @Test
    public void testGetFieldNamesForAndroid() {

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "androidOsVersion", "isRooted", "usbDebugging",
                "hardwareKeystore", "biometric", "screenLockComplexity", "diskEncryption",
                "networkProxies", "wifiNetworkSecurity", "androidIntegrity"
        );
        Assert.assertEquals(DeviceField.getFieldNamesForPlatform(Platform.ANDROID), expected);
    }

    @Test
    public void testGetFieldNamesForIos() {

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "iosOsVersion", "iosIntegrity",
                "passcode", "touchIdOrFaceId", "jailbreak"
        );
        Assert.assertEquals(DeviceField.getFieldNamesForPlatform(Platform.IOS), expected);
    }

    @Test
    public void testGetFieldNamesForMacos() {

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "macosOsVersion", "diskEncryption", "secureEnclave"
        );
        Assert.assertEquals(DeviceField.getFieldNamesForPlatform(Platform.MACOS), expected);
    }

    @Test
    public void testGetFieldNamesForWindows() {

        List<String> expected = Arrays.asList(
                "platform", "lockScreen", "windowsOsVersion", "diskEncryption",
                "windowsHello", "trustedPlatformModule"
        );
        Assert.assertEquals(DeviceField.getFieldNamesForPlatform(Platform.WINDOWS), expected);
    }

    @Test
    public void testGetFieldNamesForNullPlatformReturnsEveryField() {

        List<String> fields = DeviceField.getFieldNamesForPlatform(null);

        Assert.assertEquals(fields.size(), 22);
        for (DeviceField field : DeviceField.values()) {
            Assert.assertTrue(fields.contains(field.getName()));
        }
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetFieldNamesForPlatformReturnsUnmodifiableList() {

        DeviceField.getFieldNamesForPlatform(Platform.ANDROID).add("extraField");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testGetFieldNamesForNullPlatformReturnsUnmodifiableList() {

        DeviceField.getFieldNamesForPlatform(null).add("extraField");
    }
}
