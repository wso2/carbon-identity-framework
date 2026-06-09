/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.core.circuitbreaker;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * Unit tests for {@link RuntimePolicy} and its {@link RuntimePolicy.Builder}.
 */
public class RuntimePolicyTest {

    // ─────────────────────────── Builder validation — windowSize ───────────────────────────

    @Test
    public void testBuildThrowsWhenWindowSizeIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(0)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenWindowSizeIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(-1)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    // ─────────────────────────── Builder validation — minCallsToEvaluate ───────────────────────────

    @Test
    public void testBuildThrowsWhenMinCallsToEvaluateIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(0)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenMinCallsToEvaluateIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(-5)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    // ─────────────────────────── Builder validation — failureRateThreshold ───────────────────────────

    @Test
    public void testBuildThrowsWhenFailureRateThresholdIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.0)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenFailureRateThresholdIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(-0.1)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenFailureRateThresholdExceedsOne() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(1.01)
                        .setOpenDuration(1000)
                        .setMaxInFlight(1)
                        .build());
    }

    // ─────────────────────────── Builder validation — openDuration ───────────────────────────

    @Test
    public void testBuildThrowsWhenOpenDurationIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(0)
                        .setMaxInFlight(1)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenOpenDurationIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(-1000)
                        .setMaxInFlight(1)
                        .build());
    }

    // ─────────────────────────── Builder validation — maxInFlight ───────────────────────────

    @Test
    public void testBuildThrowsWhenMaxInFlightIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(0)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenMaxInFlightIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                RuntimePolicy.builder()
                        .setWindowSize(5)
                        .setMinCallsToEvaluate(1)
                        .setFailureRateThreshold(0.5)
                        .setOpenDuration(1000)
                        .setMaxInFlight(-3)
                        .build());
    }

    // ─────────────────────────── Valid boundary values ───────────────────────────

    @Test
    public void testBuildSucceedsAtMinimumValidValues() {

        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(1)
                .setMinCallsToEvaluate(1)
                .setFailureRateThreshold(0.01)
                .setOpenDuration(1)
                .setMaxInFlight(1)
                .build();

        assertEquals(policy.getWindowSize(), 1);
        assertEquals(policy.getMinCallsToEvaluate(), 1);
        assertEquals(policy.getFailureRateThreshold(), 0.01, 0.0001);
        assertEquals(policy.getOpenDuration(), 1L);
        assertEquals(policy.getMaxInFlight(), 1);
    }

    @Test
    public void testBuildSucceedsWithFailureRateThresholdOfExactlyOne() {

        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(10)
                .setMinCallsToEvaluate(5)
                .setFailureRateThreshold(1.0)
                .setOpenDuration(5000)
                .setMaxInFlight(10)
                .build();

        assertEquals(policy.getFailureRateThreshold(), 1.0, 0.0001);
    }

    // ─────────────────────────── Getters ───────────────────────────

    @Test
    public void testGettersReturnConfiguredValues() {

        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(20)
                .setMinCallsToEvaluate(10)
                .setFailureRateThreshold(0.75)
                .setOpenDuration(30000)
                .setMaxInFlight(5)
                .build();

        assertEquals(policy.getWindowSize(), 20);
        assertEquals(policy.getMinCallsToEvaluate(), 10);
        assertEquals(policy.getFailureRateThreshold(), 0.75, 0.0001);
        assertEquals(policy.getOpenDuration(), 30000L);
        assertEquals(policy.getMaxInFlight(), 5);
    }

    // ─────────────────────────── minCallsToEvaluate clamping ───────────────────────────

    @Test
    public void testMinCallsToEvaluateIsClampedToWindowSize() {

        // minCallsToEvaluate=10 > windowSize=5, so the policy stores Math.min(10, 5)=5.
        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(5)
                .setMinCallsToEvaluate(10)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(1000)
                .setMaxInFlight(2)
                .build();

        assertEquals(policy.getMinCallsToEvaluate(), 5);
    }

    @Test
    public void testMinCallsToEvaluateIsNotClampedWhenLessThanWindowSize() {

        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(10)
                .setMinCallsToEvaluate(3)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(1000)
                .setMaxInFlight(2)
                .build();

        assertEquals(policy.getMinCallsToEvaluate(), 3);
    }

    @Test
    public void testMinCallsToEvaluateEqualToWindowSizeIsUnchanged() {

        RuntimePolicy policy = RuntimePolicy.builder()
                .setWindowSize(8)
                .setMinCallsToEvaluate(8)
                .setFailureRateThreshold(0.5)
                .setOpenDuration(1000)
                .setMaxInFlight(2)
                .build();

        assertEquals(policy.getMinCallsToEvaluate(), 8);
    }

    // ─────────────────────────── Builder default values ───────────────────────────

    @Test
    public void testBuilderDefaultsProduceValidPolicy() {

        RuntimePolicy policy = RuntimePolicy.builder().build();

        assertEquals(policy.getWindowSize(), CircuitBreakerConstants.Defaults.WINDOW_SIZE);
        assertEquals(policy.getMinCallsToEvaluate(), CircuitBreakerConstants.Defaults.MIN_CALLS_TO_EVALUATE);
        assertEquals(policy.getFailureRateThreshold(),
                CircuitBreakerConstants.Defaults.FAILURE_RATE_THRESHOLD, 0.0001);
        assertEquals(policy.getOpenDuration(), CircuitBreakerConstants.Defaults.OPEN_DURATION);
        assertEquals(policy.getMaxInFlight(), CircuitBreakerConstants.Defaults.MAX_IN_FLIGHT);
    }
}
