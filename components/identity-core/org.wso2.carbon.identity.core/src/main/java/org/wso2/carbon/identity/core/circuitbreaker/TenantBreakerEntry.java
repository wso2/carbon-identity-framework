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
 * Tenant-local state holder used by the breaker manager.
 */
class TenantBreakerEntry {

    private final long openDurationMs;
    private final double failureRateThreshold;
    private final int minCallsToEvaluate;
    private final int maxInFlight;
    private final SlidingWindow window;

    private CircuitState state = CircuitState.CLOSED;
    private long stateSinceMs;
    private long lastAccessMs;

    private int inFlight;

    TenantBreakerEntry(Policy policy, long nowMs) {

        this.openDurationMs = policy.getOpenDurationMs();
        this.failureRateThreshold = policy.getFailureRateThreshold();
        this.minCallsToEvaluate = policy.getMinCallsToEvaluate();
        this.maxInFlight = policy.getMaxInFlight();
        this.window = new SlidingWindow(policy.getWindowSize());
        this.stateSinceMs = nowMs;
        this.lastAccessMs = nowMs;
    }

    synchronized Decision allowRequest(long nowMs) {

        lastAccessMs = nowMs;
        if (state == CircuitState.OPEN) {
            if ((nowMs - stateSinceMs) < openDurationMs) {
                return Decision.rejected(RejectReason.CIRCUIT_OPEN);
            }
            state = CircuitState.HALF_OPEN;
            stateSinceMs = nowMs;
        }

        if (state == CircuitState.HALF_OPEN) {
            if (inFlight > 0) {
                return Decision.rejected(RejectReason.CIRCUIT_OPEN);
            }
        }

        return Decision.allowed();
    }

    synchronized Decision acquireBulkhead() {

        if (inFlight >= maxInFlight) {
            return Decision.rejected(RejectReason.BULKHEAD_FULL);
        }

        inFlight++;
        return Decision.allowed();
    }

    synchronized void releaseBulkhead(long nowMs) {

        lastAccessMs = nowMs;
        if (inFlight > 0) {
            inFlight--;
        }
    }

    synchronized void recordResult(boolean success, long nowMs) {

        lastAccessMs = nowMs;

        if (state == CircuitState.HALF_OPEN) {
            if (success) {
                state = CircuitState.CLOSED;
                stateSinceMs = nowMs;
                window.reset();
            } else {
                state = CircuitState.OPEN;
                stateSinceMs = nowMs;
            }
            return;
        }

        if (state != CircuitState.CLOSED) {
            return;
        }

        window.record(success);
        if (window.calls() < minCallsToEvaluate) {
            return;
        }

        if (window.failureRate() >= failureRateThreshold) {
            state = CircuitState.OPEN;
            stateSinceMs = nowMs;
        }
    }

    synchronized boolean isEvictable(long nowMs, long idleEvictMs) {

        return (nowMs - lastAccessMs) > idleEvictMs && inFlight == 0;
    }

    synchronized CircuitState getState() {

        return state;
    }

    synchronized int getCalls() {

        return window.calls();
    }

    synchronized int getFailures() {

        return window.failures();
    }

    synchronized double getFailureRate() {

        return window.failureRate();
    }

    synchronized int getInFlight() {

        return inFlight;
    }

    synchronized boolean hasInFlightRequests() {

        return inFlight > 0;
    }

    synchronized long getLastAccessMs() {

        return lastAccessMs;
    }
}
