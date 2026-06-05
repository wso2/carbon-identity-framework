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
    private final long openDurationMs;
    private final int maxInFlight;

    private RuntimePolicy(Builder builder) {

        this.windowSize = builder.windowSize;
        this.minCallsToEvaluate = Math.min(builder.minCallsToEvaluate, builder.windowSize);
        this.failureRateThreshold = builder.failureRateThreshold;
        this.openDurationMs = builder.openDurationMs;
        this.maxInFlight = builder.maxInFlight;
    }

    public static Builder builder() {

        return new Builder();
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

    /**
     * Builder for {@link RuntimePolicy}.
     */
    public static final class Builder {

        private int windowSize = CircuitBreakerConstants.Defaults.WINDOW_SIZE;
        private int minCallsToEvaluate = CircuitBreakerConstants.Defaults.MIN_CALLS_TO_EVALUATE;
        private double failureRateThreshold = CircuitBreakerConstants.Defaults.FAILURE_RATE_THRESHOLD;
        private long openDurationMs = CircuitBreakerConstants.Defaults.OPEN_DURATION_MS;
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
            if (openDurationMs < 1) {
                throw new IllegalArgumentException("openDurationMs must be >= 1");
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

        public Builder setOpenDurationMs(long openDurationMs) {

            this.openDurationMs = openDurationMs;
            return this;
        }

        public Builder setMaxInFlight(int maxInFlight) {

            this.maxInFlight = maxInFlight;
            return this;
        }
    }
}
