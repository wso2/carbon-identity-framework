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

/**
 * Constants for the circuit breaker.
 */
final class CircuitBreakerConstants {

    private CircuitBreakerConstants() {

    }

    /**
     * identity.xml property keys, all rooted under the {@code CircuitBreaker} prefix.
     */
    public static final class PropertyKeys {

        public static final String PREFIX = "CircuitBreaker.";

        // StaticPolicy keys — require a server restart to take effect.
        public static final String ENABLED = PREFIX + "Enabled";
        public static final String CACHE_STRIPES = PREFIX + "CacheStripes";
        public static final String MAX_TENANTS_IN_CACHE = PREFIX + "MaxTenantsInCache";
        public static final String TENANT_ENTRY_IDLE_EVICT_MS = PREFIX + "TenantEntryIdleEvictMs";
        public static final String CLEANUP_EVERY_REQUESTS = PREFIX + "CleanupEveryRequests";
        public static final String EVICTION_SCAN_LIMIT = PREFIX + "EvictionScanLimit";
        public static final String HARD_CAP_EVICTION_SCAN_LIMIT = PREFIX + "HardCapEvictionScanLimit";

        // RuntimePolicy keys — can be overridden at runtime.
        public static final String WINDOW_SIZE = PREFIX + "WindowSize";
        public static final String MIN_CALLS_TO_EVALUATE = PREFIX + "MinCallsToEvaluate";
        public static final String FAILURE_RATE_THRESHOLD = PREFIX + "FailureRateThreshold";
        public static final String OPEN_DURATION_MS = PREFIX + "OpenDurationMs";
        public static final String MAX_IN_FLIGHT = PREFIX + "MaxInFlight";

        private PropertyKeys() {

        }
    }

    /**
     * Default policy values used when a property is absent or invalid in identity.xml.
     */
    public static final class Defaults {

        // StaticPolicy defaults.
        public static final boolean ENABLED = true;
        public static final int CACHE_STRIPES = 1;
        public static final int MAX_TENANTS_IN_CACHE = 2;
        public static final long TENANT_ENTRY_IDLE_EVICT_MS = 600000L;
        public static final int CLEANUP_EVERY_REQUESTS = 5;
        public static final int EVICTION_SCAN_LIMIT = 2;
        public static final int HARD_CAP_EVICTION_SCAN_LIMIT = 2;

        // RuntimePolicy defaults.
        public static final int WINDOW_SIZE = 2;
        public static final int MIN_CALLS_TO_EVALUATE = 2;
        public static final double FAILURE_RATE_THRESHOLD = 0.50D;
        public static final long OPEN_DURATION_MS = 600000L;
        public static final int MAX_IN_FLIGHT = 2;

        private Defaults() {

        }
    }
}
