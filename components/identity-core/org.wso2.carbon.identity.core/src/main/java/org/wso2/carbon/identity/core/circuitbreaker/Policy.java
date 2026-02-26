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
 * Policy configuration for the per-tenant breaker.
 */
public final class Policy {

    private final boolean enabled;
    private final int windowSize;
    private final int minCallsToEvaluate;
    private final double failureRateThreshold;
    private final long openDurationMs;
    private final int maxInFlight;
    private final long tenantEntryIdleEvictMs;
    private final int maxTenantsInCache;
    private final int cleanupTriggerEveryRequests;
    private final int evictionScanLimit;
    private final int hardCapEvictionScanLimit;
    private final int cacheStripes;

    private Policy(Builder builder) {

        this.enabled = builder.enabled;
        this.windowSize = builder.windowSize;
        this.minCallsToEvaluate = Math.min(builder.minCallsToEvaluate, builder.windowSize);
        this.failureRateThreshold = builder.failureRateThreshold;
        this.openDurationMs = builder.openDurationMs;
        this.maxInFlight = builder.maxInFlight;
        this.tenantEntryIdleEvictMs = builder.tenantEntryIdleEvictMs;
        this.maxTenantsInCache = builder.maxTenantsInCache;
        this.cleanupTriggerEveryRequests = builder.cleanupTriggerEveryRequests;
        this.evictionScanLimit = builder.evictionScanLimit;
        this.hardCapEvictionScanLimit = builder.hardCapEvictionScanLimit;
        this.cacheStripes = Math.max(builder.cacheStripes, 1);
    }

    public static Builder builder() {

        return new Builder();
    }

    public boolean isEnabled() {

        return enabled;
    }

    public int getWindowSize() {

        return windowSize;
    }

    public int getMinCallsToEvaluate() {

        return minCallsToEvaluate;
    }

    public double getFailureRateThreshold() {

        return failureRateThreshold;
    }

    public long getOpenDurationMs() {

        return openDurationMs;
    }

    public int getMaxInFlight() {

        return maxInFlight;
    }

    public long getTenantEntryIdleEvictMs() {

        return tenantEntryIdleEvictMs;
    }

    public int getMaxTenantsInCache() {

        return maxTenantsInCache;
    }

    public int getCleanupTriggerEveryRequests() {

        return cleanupTriggerEveryRequests;
    }

    public int getEvictionScanLimit() {

        return evictionScanLimit;
    }

    public int getHardCapEvictionScanLimit() {

        return hardCapEvictionScanLimit;
    }

    public int getCacheStripes() {

        return cacheStripes;
    }

    /**
     * Builder for policy values.
     */
    public static final class Builder {

        private boolean enabled = true;
        private int windowSize = 2;
        private int minCallsToEvaluate = 2;
        private double failureRateThreshold = 0.50D;
        private long openDurationMs = 600000L;
        private int maxInFlight = 2;
        private long tenantEntryIdleEvictMs = 600000L;
        private int maxTenantsInCache = 2;
        private int cleanupTriggerEveryRequests = 5;
        private int evictionScanLimit = 2;
        private int hardCapEvictionScanLimit = 2;
        private int cacheStripes = 1;

        public Policy build() {

            return new Policy(this);
        }

        public Builder setEnabled(boolean enabled) {

            this.enabled = enabled;
            return this;
        }

        public Builder setWindowSize(int windowSize) {

            this.windowSize = windowSize;
            return this;
        }

        public Builder setMinCallsToEvaluate(int minCallsToEvaluate) {

            this.minCallsToEvaluate = minCallsToEvaluate;
            return this;
        }

        public Builder setFailureRateThreshold(double failureRateThreshold) {

            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        public Builder setOpenDurationMs(long openDurationMs) {

            this.openDurationMs = openDurationMs;
            return this;
        }

        public Builder setMaxInFlight(int maxInFlight) {

            this.maxInFlight = maxInFlight;
            return this;
        }

        public Builder setTenantEntryIdleEvictMs(long tenantEntryIdleEvictMs) {

            this.tenantEntryIdleEvictMs = tenantEntryIdleEvictMs;
            return this;
        }

        public Builder setMaxTenantsInCache(int maxTenantsInCache) {

            this.maxTenantsInCache = maxTenantsInCache;
            return this;
        }

        public Builder setCleanupTriggerEveryRequests(int cleanupTriggerEveryRequests) {

            this.cleanupTriggerEveryRequests = cleanupTriggerEveryRequests;
            return this;
        }

        public Builder setEvictionScanLimit(int evictionScanLimit) {

            this.evictionScanLimit = evictionScanLimit;
            return this;
        }

        public Builder setHardCapEvictionScanLimit(int hardCapEvictionScanLimit) {

            this.hardCapEvictionScanLimit = hardCapEvictionScanLimit;
            return this;
        }

        public Builder setCacheStripes(int cacheStripes) {

            this.cacheStripes = cacheStripes;
            return this;
        }
    }
}
