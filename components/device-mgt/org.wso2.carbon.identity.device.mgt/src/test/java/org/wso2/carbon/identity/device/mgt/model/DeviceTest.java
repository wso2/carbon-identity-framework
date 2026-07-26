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

package org.wso2.carbon.identity.device.mgt.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.mgt.api.model.Device;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Unit tests for {@link Device} model and builder validations.
 */
public class DeviceTest {

    @Test
    public void testBuildValidDeviceSucceeds() {

        Timestamp now = Timestamp.from(Instant.now());
        Device device = new Device.Builder()
                .id("d1")
                .userId("alice@example.com")
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .status(Device.Status.ACTIVE)
                .registeredAt(now)
                .metadata("{\"os\":\"iOS\"}")
                .build();

        Assert.assertEquals(device.getId(), "d1");
        Assert.assertEquals(device.getUserId(), "alice@example.com");
        Assert.assertEquals(device.getDeviceName(), "Alice's iPhone");
        Assert.assertEquals(device.getDeviceModel(), "iPhone 15");
        Assert.assertEquals(device.getPublicKey(), "dummy-public-key");
        Assert.assertEquals(device.getStatus(), Device.Status.ACTIVE);
        Assert.assertEquals(device.getRegisteredAt(), now);
        Assert.assertEquals(device.getMetadata(), "{\"os\":\"iOS\"}");
    }

    @Test
    public void testBuildDeviceDefaultsStatusAndRegisteredAt() {

        Device device = new Device.Builder()
                .id("d1")
                .userId("alice@example.com")
                .deviceName("Alice's iPhone")
                .publicKey("dummy-public-key")
                .build();

        Assert.assertEquals(device.getStatus(), Device.Status.ACTIVE);
        Assert.assertNotNull(device.getRegisteredAt());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildDeviceWithoutIdThrows() {

        new Device.Builder()
                .userId("alice@example.com")
                .deviceName("Alice's iPhone")
                .publicKey("dummy-public-key")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildDeviceWithoutUserIdThrows() {

        new Device.Builder()
                .id("d1")
                .deviceName("Alice's iPhone")
                .publicKey("dummy-public-key")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildDeviceWithoutDeviceNameThrows() {

        new Device.Builder()
                .id("d1")
                .userId("alice@example.com")
                .publicKey("dummy-public-key")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildDeviceWithoutPublicKeyThrows() {

        new Device.Builder()
                .id("d1")
                .userId("alice@example.com")
                .deviceName("Alice's iPhone")
                .build();
    }
}
