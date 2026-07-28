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

package org.wso2.carbon.identity.device.policy.api.constant;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DevicePolicyErrorMessageTest {

    @Test
    public void testGetCode() {
        Assert.assertEquals(DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED.getCode(), "DPM-60008");
    }

    @Test
    public void testGetMessage() {
        Assert.assertEquals(DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED.getMessage(),
                "Device token invalid.");
    }

    @Test
    public void testGetDescription() {
        Assert.assertEquals(DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED.getDescription(),
                "Failed to parse X-Device-Token JWT.");
    }

    @Test
    public void testEnumValues() {
        DevicePolicyErrorMessage[] values = DevicePolicyErrorMessage.values();
        Assert.assertNotNull(values);
        Assert.assertTrue(values.length > 0);
        
        DevicePolicyErrorMessage valueOf = DevicePolicyErrorMessage.valueOf("ERROR_DEVICE_TOKEN_PARSE_FAILED");
        Assert.assertEquals(valueOf, DevicePolicyErrorMessage.ERROR_DEVICE_TOKEN_PARSE_FAILED);
    }
}
