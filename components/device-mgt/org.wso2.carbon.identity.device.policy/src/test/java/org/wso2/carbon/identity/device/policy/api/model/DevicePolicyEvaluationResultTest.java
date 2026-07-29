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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DevicePolicyEvaluationResultTest {

    private static final String POLICY_NAME = "TestPolicy";

    @Test
    public void testCompliantFactory() {

        DevicePolicyEvaluationResult result = DevicePolicyEvaluationResult.compliant(POLICY_NAME);

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.COMPLIANT);
        Assert.assertEquals(result.getPolicyName(), POLICY_NAME);
        Assert.assertTrue(result.isCompliant());
        Assert.assertTrue(result.getFailedFields().isEmpty());
        Assert.assertTrue(result.getMissingFields().isEmpty());
    }

    @Test
    public void testNonCompliantFactory() {

        List<String> failed = new ArrayList<>(Arrays.asList("isRooted", "diskEncryption"));
        DevicePolicyEvaluationResult result = DevicePolicyEvaluationResult.nonCompliant(POLICY_NAME, failed);

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.NON_COMPLIANT);
        Assert.assertEquals(result.getPolicyName(), POLICY_NAME);
        Assert.assertFalse(result.isCompliant());
        Assert.assertEquals(result.getFailedFields(), Arrays.asList("isRooted", "diskEncryption"));
        Assert.assertTrue(result.getMissingFields().isEmpty());

        // Defensive copy check: modifying input list should not affect result
        failed.add("usbDebugging");
        Assert.assertEquals(result.getFailedFields().size(), 2);

        // Unmodifiable list check
        try {
            result.getFailedFields().add("extra");
            Assert.fail("Expected UnsupportedOperationException on modifying returned failed fields");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testIncompleteDeviceDataFactory() {

        List<String> missing = new ArrayList<>(Arrays.asList("platform"));
        DevicePolicyEvaluationResult result = DevicePolicyEvaluationResult.incompleteDeviceData(POLICY_NAME, missing);

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.INCOMPLETE_DEVICE_DATA);
        Assert.assertEquals(result.getPolicyName(), POLICY_NAME);
        Assert.assertFalse(result.isCompliant());
        Assert.assertEquals(result.getMissingFields(), Arrays.asList("platform"));
        Assert.assertTrue(result.getFailedFields().isEmpty());

        // Defensive copy check
        missing.add("lockScreen");
        Assert.assertEquals(result.getMissingFields().size(), 1);

        // Unmodifiable list check
        try {
            result.getMissingFields().add("extra");
            Assert.fail("Expected UnsupportedOperationException on modifying returned missing fields");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testPolicyNotFoundFactory() {

        DevicePolicyEvaluationResult result = DevicePolicyEvaluationResult.policyNotFound(POLICY_NAME);

        Assert.assertEquals(result.getStatus(), DevicePolicyEvaluationResult.Status.POLICY_NOT_FOUND);
        Assert.assertEquals(result.getPolicyName(), POLICY_NAME);
        Assert.assertFalse(result.isCompliant());
        Assert.assertTrue(result.getFailedFields().isEmpty());
        Assert.assertTrue(result.getMissingFields().isEmpty());
    }
}
