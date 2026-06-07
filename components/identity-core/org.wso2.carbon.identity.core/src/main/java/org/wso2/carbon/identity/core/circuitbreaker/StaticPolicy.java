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
    private final int cacheShardCount;
    private final int tenantServiceCacheCapacity;
    private final long tenantServiceEntryIdleTimeout;
    private final int cleanupRequestInterval;
    private final int tenantServiceScanLimit;
    private final int tenantServiceOverflowScanLimit;

    private StaticPolicy(Builder builder) {

        this.enabled = builder.enabled;
        this.cacheShardCount = Math.max(builder.cacheShardCount, 1);
        this.tenantServiceCacheCapacity = builder.tenantServiceCacheCapacity;
        this.tenantServiceEntryIdleTimeout = builder.tenantServiceEntryIdleTimeout;
        this.cleanupRequestInterval = builder.cleanupRequestInterval;
        this.tenantServiceScanLimit = builder.tenantServiceScanLimit;
        this.tenantServiceOverflowScanLimit = builder.tenantServiceOverflowScanLimit;
    }

    public static Builder builder() {

        return new Builder();
    }

    public boolean isEnabled() {

        return enabled;
    }

    /**
     * Returns the number of independent lock stripes for the breaker entry cache.
     * Increasing this reduces lock contention under high concurrency.
     *
     * @return number of cache shards.
     */
    public int getCacheShardCount() {

        return cacheShardCount;
    }

    /**
     * Returns the maximum number of (tenant, service) breaker entries held in memory at once.
     * Each unique tenant-service pair occupies one slot.
     *
     * @return maximum tenant-service entry count.
     */
    public int getTenantServiceCacheCapacity() {

        return tenantServiceCacheCapacity;
    }

    /**
     * Returns the idle eviction timeout for tenant-service entries.
     * An entry with no active calls that has been idle longer than this value is eligible for eviction.
     *
     * @return idle timeout in milliseconds.
     */
    public long getTenantServiceEntryIdleTimeout() {

        return tenantServiceEntryIdleTimeout;
    }

    /**
     * Returns the number of requests processed between automatic idle-entry cleanup passes.
     * A value of 0 disables periodic cleanup; entries are only evicted when the cache reaches capacity.
     *
     * @return requests per cleanup interval.
     */
    public int getCleanupRequestInterval() {

        return cleanupRequestInterval;
    }

    /**
     * Returns the maximum number of entries inspected per periodic cleanup pass.
     * Bounds the per-request overhead of background eviction during normal operation.
     *
     * @return entry scan limit per cleanup cycle.
     */
    public int getTenantServiceScanLimit() {

        return tenantServiceScanLimit;
    }

    /**
     * Returns the maximum number of entries inspected when making room at full capacity.
     * A larger value increases the chance of finding an evictable entry but adds latency
     * to admission under capacity pressure.
     *
     * @return entry scan limit used during overflow eviction.
     */
    public int getTenantServiceOverflowScanLimit() {

        return tenantServiceOverflowScanLimit;
    }

    /**
     * Builder for {@link StaticPolicy}.
     */
    public static final class Builder {

        private boolean enabled = CircuitBreakerConstants.Defaults.ENABLED;
        private int cacheShardCount = CircuitBreakerConstants.Defaults.CACHE_SHARD_COUNT;
        private int tenantServiceCacheCapacity = CircuitBreakerConstants.Defaults.TENANT_SERVICE_CACHE_CAPACITY;
        private long tenantServiceEntryIdleTimeout = CircuitBreakerConstants.Defaults.TENANT_SERVICE_ENTRY_IDLE_TIMEOUT;
        private int cleanupRequestInterval = CircuitBreakerConstants.Defaults.CLEANUP_REQUEST_INTERVAL;
        private int tenantServiceScanLimit = CircuitBreakerConstants.Defaults.TENANT_SERVICE_SCAN_LIMIT;
        private int tenantServiceOverflowScanLimit = CircuitBreakerConstants.Defaults.TENANT_SERVICE_OVERFLOW_SCAN_LIMIT;

        public StaticPolicy build() {

            if (tenantServiceCacheCapacity < 1) {
                throw new IllegalArgumentException("tenantServiceCacheCapacity must be >= 1");
            }
            if (tenantServiceEntryIdleTimeout < 1) {
                throw new IllegalArgumentException("tenantServiceEntryIdleTimeout must be >= 1");
            }
            if (cleanupRequestInterval < 0) {
                throw new IllegalArgumentException("cleanupRequestInterval must be >= 0");
            }
            if (tenantServiceScanLimit < 1) {
                throw new IllegalArgumentException("tenantServiceScanLimit must be >= 1");
            }
            if (tenantServiceOverflowScanLimit < 1) {
                throw new IllegalArgumentException("tenantServiceOverflowScanLimit must be >= 1");
            }
            if (cacheShardCount < 1) {
                throw new IllegalArgumentException("cacheShardCount must be >= 1");
            }
            return new StaticPolicy(this);
        }

        public Builder setEnabled(boolean enabled) {

            this.enabled = enabled;
            return this;
        }

        public Builder setCacheShardCount(int cacheShardCount) {

            this.cacheShardCount = cacheShardCount;
            return this;
        }

        public Builder setTenantServiceCacheCapacity(int tenantServiceCacheCapacity) {

            this.tenantServiceCacheCapacity = tenantServiceCacheCapacity;
            return this;
        }

        /**
         * Set the idle eviction timeout for tenant-service entries.
         *
         * @param tenantServiceEntryIdleTimeout idle timeout in milliseconds.
         * @return this builder.
         */
        public Builder setTenantServiceEntryIdleTimeout(long tenantServiceEntryIdleTimeout) {

            this.tenantServiceEntryIdleTimeout = tenantServiceEntryIdleTimeout;
            return this;
        }

        public Builder setCleanupRequestInterval(int cleanupRequestInterval) {

            this.cleanupRequestInterval = cleanupRequestInterval;
            return this;
        }

        public Builder setTenantServiceScanLimit(int tenantServiceScanLimit) {

            this.tenantServiceScanLimit = tenantServiceScanLimit;
            return this;
        }

        public Builder setTenantServiceOverflowScanLimit(int tenantServiceOverflowScanLimit) {

            this.tenantServiceOverflowScanLimit = tenantServiceOverflowScanLimit;
            return this;
        }
    }
}
