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
    private final int tenantServiceCacheCapacity;
    private final int tenantServiceEvictionThreshold;
    private final long tenantServiceEntryIdleTimeout;

    private StaticPolicy(Builder builder) {

        this.enabled = builder.enabled;
        this.tenantServiceCacheCapacity = builder.tenantServiceCacheCapacity;
        this.tenantServiceEvictionThreshold = Math.max(1,
                (int) (builder.tenantServiceCacheCapacity * builder.tenantServiceEvictionThreshold));
        this.tenantServiceEntryIdleTimeout = builder.tenantServiceEntryIdleTimeout;
    }

    /**
     * Returns a new {@link Builder} with default values.
     *
     * @return a new builder instance.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Returns {@code true} if the circuit breaker is enabled.
     *
     * @return {@code true} if enabled; {@code false} otherwise.
     */
    public boolean isEnabled() {

        return enabled;
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
     * Returns the entry count at which eviction is triggered before admitting a new entry.
     *
     * @return eviction threshold as an absolute entry count.
     */
    public int getTenantServiceEvictionThreshold() {

        return tenantServiceEvictionThreshold;
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
     * Builder for {@link StaticPolicy}.
     */
    public static final class Builder {

        private boolean enabled = CircuitBreakerConstants.Defaults.ENABLED;
        private int tenantServiceCacheCapacity = CircuitBreakerConstants.Defaults.TENANT_SERVICE_CACHE_CAPACITY;
        private double tenantServiceEvictionThreshold = CircuitBreakerConstants.Defaults.TENANT_SERVICE_EVICTION_THRESHOLD;
        private long tenantServiceEntryIdleTimeout = CircuitBreakerConstants.Defaults.TENANT_SERVICE_ENTRY_IDLE_TIMEOUT;

        /**
         * Builds and returns a validated {@link StaticPolicy}.
         *
         * @return the built {@link StaticPolicy}.
         * @throws IllegalArgumentException if any field is out of range.
         */
        public StaticPolicy build() {

            if (tenantServiceCacheCapacity < 1) {
                throw new IllegalArgumentException("tenantServiceCacheCapacity must be >= 1");
            }
            if (tenantServiceEntryIdleTimeout < 1) {
                throw new IllegalArgumentException("tenantServiceEntryIdleTimeout must be >= 1");
            }
            if (tenantServiceEvictionThreshold <= 0 || tenantServiceEvictionThreshold > 1) {
                throw new IllegalArgumentException("tenantServiceEvictionThreshold must be between 0 (exclusive) and 1 (inclusive)");
            }
            return new StaticPolicy(this);
        }

        /**
         * Sets whether the circuit breaker is enabled.
         *
         * @param enabled {@code true} to enable; {@code false} to disable.
         * @return this builder.
         */
        public Builder setEnabled(boolean enabled) {

            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the maximum number of (tenant, service) entries held in memory.
         *
         * @param tenantServiceCacheCapacity maximum entry count.
         * @return this builder.
         */
        public Builder setTenantServiceCacheCapacity(int tenantServiceCacheCapacity) {

            this.tenantServiceCacheCapacity = tenantServiceCacheCapacity;
            return this;
        }

        /**
         * Sets the fill fraction (0.0–1.0) of the cache at which eviction is triggered.
         *
         * @param tenantServiceEvictionThreshold eviction threshold fraction.
         * @return this builder.
         */
        public Builder setTenantServiceEvictionThreshold(double tenantServiceEvictionThreshold) {

            this.tenantServiceEvictionThreshold = tenantServiceEvictionThreshold;
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

    }
}
