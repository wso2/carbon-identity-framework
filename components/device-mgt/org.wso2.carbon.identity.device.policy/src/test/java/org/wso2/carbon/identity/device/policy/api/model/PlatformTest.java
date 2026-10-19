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

public class PlatformTest {

    @Test
    public void testFromValueValid() {

        Assert.assertEquals(Platform.fromValue("android"), Platform.ANDROID);
        Assert.assertEquals(Platform.fromValue("ANDROID"), Platform.ANDROID);
        Assert.assertEquals(Platform.fromValue("Android"), Platform.ANDROID);
        Assert.assertEquals(Platform.fromValue("ios"), Platform.IOS);
        Assert.assertEquals(Platform.fromValue("IOS"), Platform.IOS);
        Assert.assertEquals(Platform.fromValue("macos"), Platform.MACOS);
        Assert.assertEquals(Platform.fromValue("windows"), Platform.WINDOWS);
    }

    @Test
    public void testFromValueInvalidAndNull() {

        Assert.assertNull(Platform.fromValue("unknown"));
        Assert.assertNull(Platform.fromValue("andriod"));
        Assert.assertNull(Platform.fromValue(null));
        Assert.assertNull(Platform.fromValue("   "));
    }

    @Test
    public void testGetValue() {

        Assert.assertEquals(Platform.ANDROID.getValue(), "android");
        Assert.assertEquals(Platform.IOS.getValue(), "ios");
        Assert.assertEquals(Platform.MACOS.getValue(), "macos");
        Assert.assertEquals(Platform.WINDOWS.getValue(), "windows");
    }
}
