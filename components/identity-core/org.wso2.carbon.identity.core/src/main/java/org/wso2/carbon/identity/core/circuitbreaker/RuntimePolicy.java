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
 * Immutable circuit breaker policy for breaker-behaviour settings.
 * Defaults are loaded from identity.xml at server startup. To apply new values,
 * construct a new instance via {@link Builder}.
 */
public final class RuntimePolicy {

    private final int windowSize;
    private final int minCallsToEvaluate;
    private final double failureRateThreshold;
    private final long openDuration;
    private final int maxInFlight;

    private RuntimePolicy(Builder builder) {

        this.windowSize = builder.windowSize;
        this.minCallsToEvaluate = Math.min(builder.minCallsToEvaluate, builder.windowSize);
        this.failureRateThreshold = builder.failureRateThreshold;
        this.openDuration = builder.openDuration;
        this.maxInFlight = builder.maxInFlight;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Returns the size of the sliding call window per (tenant, service) entry.
     * The breaker tracks the last N calls to compute the live failure rate.
     *
     * @return sliding window size.
     */
    public int getWindowSize() {

        return windowSize;
    }

    /**
     * Returns the minimum number of recorded calls required in the window before the failure rate
     * is acted on. Prevents premature tripping on a statistically insignificant sample.
     *
     * @return minimum call count before evaluation.
     */
    public int getMinCallsToEvaluate() {

        return minCallsToEvaluate;
    }

    /**
     * Returns the fraction of failures (0.0–1.0) within the window that triggers the circuit to open.
     * For example, 0.5 means 50% failure rate opens the circuit.
     *
     * @return failure rate threshold.
     */
    public double getFailureRateThreshold() {

        return failureRateThreshold;
    }

    /**
     * Returns the duration the circuit stays OPEN before transitioning to HALF_OPEN
     * and allowing one probe call to test service recovery.
     *
     * @return open-state duration in milliseconds.
     */
    public long getOpenDuration() {

        return openDuration;
    }

    /**
     * Returns the maximum number of concurrent in-flight calls allowed per (tenant, service) entry.
     * Excess calls are rejected with BULKHEAD_FULL before reaching the downstream service.
     *
     * @return maximum concurrent calls per entry.
     */
    public int getMaxInFlight() {

        return maxInFlight;
    }

    /**
     * Builder for {@link RuntimePolicy}.
     */
    public static final class Builder {

        private int windowSize = CircuitBreakerConstants.Defaults.WINDOW_SIZE;
        private int minCallsToEvaluate = CircuitBreakerConstants.Defaults.MIN_CALLS_TO_EVALUATE;
        private double failureRateThreshold = CircuitBreakerConstants.Defaults.FAILURE_RATE_THRESHOLD;
        private long openDuration = CircuitBreakerConstants.Defaults.OPEN_DURATION;
        private int maxInFlight = CircuitBreakerConstants.Defaults.MAX_IN_FLIGHT;

        public RuntimePolicy build() {

            if (windowSize < 1) {
                throw new IllegalArgumentException("windowSize must be >= 1");
            }
            if (minCallsToEvaluate < 1) {
                throw new IllegalArgumentException("minCallsToEvaluate must be >= 1");
            }
            if (failureRateThreshold <= 0.0 || failureRateThreshold > 1.0) {
                throw new IllegalArgumentException("failureRateThreshold must be > 0.0 and <= 1.0");
            }
            if (openDuration < 1) {
                throw new IllegalArgumentException("openDuration must be >= 1");
            }
            if (maxInFlight < 1) {
                throw new IllegalArgumentException("maxInFlight must be >= 1");
            }
            return new RuntimePolicy(this);
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

        /**
         * Set the open-state duration.
         *
         * @param openDuration open-state duration in milliseconds.
         * @return this builder.
         */
        public Builder setOpenDuration(long openDuration) {

            this.openDuration = openDuration;
            return this;
        }

        public Builder setMaxInFlight(int maxInFlight) {

            this.maxInFlight = maxInFlight;
            return this;
        }
    }
}
