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

import org.mockito.MockedConstruction;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.device.policy.internal.jwt.DeviceTokenExtractor;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class DeviceTokenVerifierImplTest {

    @Test
    public void testVerifyWithPublicKey() throws PolicyManagementException {

        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("deviceId", "test-device-id");

        try (MockedConstruction<DeviceTokenExtractor> mockedExtractor = mockConstruction(DeviceTokenExtractor.class,
                (mock, context) -> {
                    when(mock.extractWithPublicKey(anyString(), anyString(), anyString(), anyString()))
                            .thenReturn(mockResult);
                })) {

            DeviceTokenVerifierImpl verifier = new DeviceTokenVerifierImpl();
            Map<String, Object> result = verifier.verifyWithPublicKey("dummyToken", "dummyKey", "corrId",
                    "carbon.super");

            assertNotNull(result);
            assertEquals(result.get("deviceId"), "test-device-id");
        }
    }
}
