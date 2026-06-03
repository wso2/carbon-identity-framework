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
 * Immutable circuit breaker policy loaded once from identity.xml at server startup.
 * Covers structural and cache-management settings that require a server restart to change.
 */
public final class StaticPolicy {

    private final boolean enabled;
    private final int cacheStripes;
    private final int maxTenantsInCache;
    private final long tenantEntryIdleEvictMs;
    private final int cleanupTriggerEveryRequests;
    private final int evictionScanLimit;
    private final int hardCapEvictionScanLimit;

    private StaticPolicy(Builder builder) {

        this.enabled = builder.enabled;
        this.cacheStripes = Math.max(builder.cacheStripes, 1);
        this.maxTenantsInCache = builder.maxTenantsInCache;
        this.tenantEntryIdleEvictMs = builder.tenantEntryIdleEvictMs;
        this.cleanupTriggerEveryRequests = builder.cleanupTriggerEveryRequests;
        this.evictionScanLimit = builder.evictionScanLimit;
        this.hardCapEvictionScanLimit = builder.hardCapEvictionScanLimit;
    }

    public static Builder builder() {

        return new Builder();
    }

    public boolean isEnabled() {

        return enabled;
    }

    public int getCacheStripes() {

        return cacheStripes;
    }

    public int getMaxTenantsInCache() {

        return maxTenantsInCache;
    }

    public long getTenantEntryIdleEvictMs() {

        return tenantEntryIdleEvictMs;
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

    /**
     * Builder for {@link StaticPolicy}.
     */
    public static final class Builder {

        private boolean enabled = CircuitBreakerConstants.Defaults.ENABLED;
        private int cacheStripes = CircuitBreakerConstants.Defaults.CACHE_STRIPES;
        private int maxTenantsInCache = CircuitBreakerConstants.Defaults.MAX_TENANTS_IN_CACHE;
        private long tenantEntryIdleEvictMs = CircuitBreakerConstants.Defaults.TENANT_ENTRY_IDLE_EVICT_MS;
        private int cleanupTriggerEveryRequests = CircuitBreakerConstants.Defaults.CLEANUP_EVERY_REQUESTS;
        private int evictionScanLimit = CircuitBreakerConstants.Defaults.EVICTION_SCAN_LIMIT;
        private int hardCapEvictionScanLimit = CircuitBreakerConstants.Defaults.HARD_CAP_EVICTION_SCAN_LIMIT;

        public StaticPolicy build() {

            if (maxTenantsInCache < 1) {
                throw new IllegalArgumentException("maxTenantsInCache must be >= 1");
            }
            if (tenantEntryIdleEvictMs < 1) {
                throw new IllegalArgumentException("tenantEntryIdleEvictMs must be >= 1");
            }
            if (cleanupTriggerEveryRequests < 0) {
                throw new IllegalArgumentException("cleanupTriggerEveryRequests must be >= 0");
            }
            if (evictionScanLimit < 1) {
                throw new IllegalArgumentException("evictionScanLimit must be >= 1");
            }
            if (hardCapEvictionScanLimit < 1) {
                throw new IllegalArgumentException("hardCapEvictionScanLimit must be >= 1");
            }
            if (cacheStripes < 1) {
                throw new IllegalArgumentException("cacheStripes must be >= 1");
            }
            return new StaticPolicy(this);
        }

        public Builder setEnabled(boolean enabled) {

            this.enabled = enabled;
            return this;
        }

        public Builder setCacheStripes(int cacheStripes) {

            this.cacheStripes = cacheStripes;
            return this;
        }

        public Builder setMaxTenantsInCache(int maxTenantsInCache) {

            this.maxTenantsInCache = maxTenantsInCache;
            return this;
        }

        public Builder setTenantEntryIdleEvictMs(long tenantEntryIdleEvictMs) {

            this.tenantEntryIdleEvictMs = tenantEntryIdleEvictMs;
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
    }
}
