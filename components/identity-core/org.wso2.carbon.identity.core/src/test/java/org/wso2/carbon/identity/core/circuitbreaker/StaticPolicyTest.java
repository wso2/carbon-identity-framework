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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Unit tests for {@link StaticPolicy} and its {@link StaticPolicy.Builder}.
 */
public class StaticPolicyTest {

    // ─────────────────────────── Builder validation — tenantServiceCacheCapacity ───────────────────────────

    @Test
    public void testBuildThrowsWhenCacheCapacityIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(0)
                        .setTenantServiceEvictionThreshold(0.5)
                        .setTenantServiceEntryIdleTimeout(1000)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenCacheCapacityIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(-1)
                        .setTenantServiceEvictionThreshold(0.5)
                        .setTenantServiceEntryIdleTimeout(1000)
                        .build());
    }

    // ─────────────────────────── Builder validation — tenantServiceEntryIdleTimeout ───────────────────────────

    @Test
    public void testBuildThrowsWhenIdleTimeoutIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(10)
                        .setTenantServiceEvictionThreshold(0.5)
                        .setTenantServiceEntryIdleTimeout(0)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenIdleTimeoutIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(10)
                        .setTenantServiceEvictionThreshold(0.5)
                        .setTenantServiceEntryIdleTimeout(-1000)
                        .build());
    }

    // ─────────────────────────── Builder validation — tenantServiceEvictionThreshold ───────────────────────────

    @Test
    public void testBuildThrowsWhenEvictionThresholdIsZero() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(10)
                        .setTenantServiceEvictionThreshold(0.0)
                        .setTenantServiceEntryIdleTimeout(1000)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenEvictionThresholdIsNegative() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(10)
                        .setTenantServiceEvictionThreshold(-0.1)
                        .setTenantServiceEntryIdleTimeout(1000)
                        .build());
    }

    @Test
    public void testBuildThrowsWhenEvictionThresholdExceedsOne() {

        expectThrows(IllegalArgumentException.class, () ->
                StaticPolicy.builder()
                        .setTenantServiceCacheCapacity(10)
                        .setTenantServiceEvictionThreshold(1.01)
                        .setTenantServiceEntryIdleTimeout(1000)
                        .build());
    }

    // ─────────────────────────── Valid boundary values ───────────────────────────

    @Test
    public void testBuildSucceedsAtMinimumValidValues() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(1)
                .setTenantServiceEvictionThreshold(0.01)
                .setTenantServiceEntryIdleTimeout(1)
                .build();

        assertEquals(policy.getTenantServiceCacheCapacity(), 1);
        assertEquals(policy.getTenantServiceEntryIdleTimeout(), 1L);
    }

    @Test
    public void testBuildSucceedsWithEvictionThresholdOfExactlyOne() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(5)
                .setTenantServiceEvictionThreshold(1.0)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertEquals(policy.getTenantServiceEvictionThreshold(), 5);
    }

    // ─────────────────────────── Getters ───────────────────────────

    @Test
    public void testGettersReturnConfiguredValues() {

        StaticPolicy policy = StaticPolicy.builder()
                .setEnabled(true)
                .setTenantServiceCacheCapacity(100)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(60000)
                .build();

        assertTrue(policy.isEnabled());
        assertEquals(policy.getTenantServiceCacheCapacity(), 100);
        assertEquals(policy.getTenantServiceEntryIdleTimeout(), 60000L);
    }

    @Test
    public void testIsEnabledReturnsFalseWhenDisabled() {

        StaticPolicy policy = StaticPolicy.builder()
                .setEnabled(false)
                .setTenantServiceCacheCapacity(10)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertFalse(policy.isEnabled());
    }

    // ─────────────────────────── eviction threshold conversion ───────────────────────────

    @Test
    public void testEvictionThresholdIsComputedFromCapacityAndFraction() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(10)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertEquals(policy.getTenantServiceEvictionThreshold(), 5);
    }

    @Test
    public void testEvictionThresholdIsTruncatedNotRounded() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(3)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertEquals(policy.getTenantServiceEvictionThreshold(), 1);
    }

    @Test
    public void testEvictionThresholdIsAtLeastOne() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(1)
                .setTenantServiceEvictionThreshold(0.5)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertEquals(policy.getTenantServiceEvictionThreshold(), 1);
    }

    @Test
    public void testEvictionThresholdScalesWithCapacity() {

        StaticPolicy policy = StaticPolicy.builder()
                .setTenantServiceCacheCapacity(100)
                .setTenantServiceEvictionThreshold(0.8)
                .setTenantServiceEntryIdleTimeout(1000)
                .build();

        assertEquals(policy.getTenantServiceEvictionThreshold(), 80);
    }

    // ─────────────────────────── Builder default values ───────────────────────────

    @Test
    public void testBuilderDefaultsProduceValidPolicy() {

        StaticPolicy policy = StaticPolicy.builder().build();

        assertTrue(policy.isEnabled());
        assertEquals(policy.getTenantServiceCacheCapacity(),
                CircuitBreakerConstants.Defaults.TENANT_SERVICE_CACHE_CAPACITY);
        assertEquals(policy.getTenantServiceEntryIdleTimeout(),
                CircuitBreakerConstants.Defaults.TENANT_SERVICE_ENTRY_IDLE_TIMEOUT);
        int expectedThreshold = Math.max(1, (int) (CircuitBreakerConstants.Defaults.TENANT_SERVICE_CACHE_CAPACITY
                * CircuitBreakerConstants.Defaults.TENANT_SERVICE_EVICTION_THRESHOLD));
        assertEquals(policy.getTenantServiceEvictionThreshold(), expectedThreshold);
    }
}
