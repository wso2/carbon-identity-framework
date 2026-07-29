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

package org.wso2.carbon.identity.device.registration.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.mgt.api.model.Device;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Unit tests for {@link VerifiedDevice} model and builder validations.
 */
public class VerifiedDeviceTest {

    @Test
    public void testBuildValidVerifiedDeviceSucceeds() {

        Timestamp now = Timestamp.from(Instant.now());
        VerifiedDevice verifiedDevice = new VerifiedDevice.Builder()
                .id("d1")
                .deviceName("Alice's iPhone")
                .deviceModel("iPhone 15")
                .publicKey("dummy-public-key")
                .registeredAt(now)
                .build();

        Assert.assertEquals(verifiedDevice.getId(), "d1");
        Assert.assertEquals(verifiedDevice.getDeviceName(), "Alice's iPhone");
        Assert.assertEquals(verifiedDevice.getDeviceModel(), "iPhone 15");
        Assert.assertEquals(verifiedDevice.getPublicKey(), "dummy-public-key");
        Assert.assertEquals(verifiedDevice.getRegisteredAt(), now);
    }

    @Test
    public void testBindToUserIdProducesValidDevice() {

        VerifiedDevice verifiedDevice = new VerifiedDevice.Builder()
                .id("d1")
                .deviceName("Alice's iPhone")
                .publicKey("dummy-public-key")
                .build();

        Device device = verifiedDevice.bindTo("alice@example.com");

        Assert.assertEquals(device.getId(), "d1");
        Assert.assertEquals(device.getDeviceName(), "Alice's iPhone");
        Assert.assertEquals(device.getPublicKey(), "dummy-public-key");
        Assert.assertEquals(device.getStatus(), Device.Status.ACTIVE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildVerifiedDeviceWithoutIdThrows() {

        new VerifiedDevice.Builder()
                .deviceName("Alice's iPhone")
                .publicKey("dummy-public-key")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildVerifiedDeviceWithoutDeviceNameThrows() {

        new VerifiedDevice.Builder()
                .id("d1")
                .publicKey("dummy-public-key")
                .build();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBuildVerifiedDeviceWithoutPublicKeyThrows() {

        new VerifiedDevice.Builder()
                .id("d1")
                .deviceName("Alice's iPhone")
                .build();
    }
}
