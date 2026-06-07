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

    private final RuntimePolicy runtimePolicy;
    private final SlidingWindow window;

    private CircuitState state = CircuitState.CLOSED;
    private long stateSince;
    private long lastAccess;

    private int inFlight;

    public TenantBreakerEntry(RuntimePolicy runtimePolicy, long now) {

        this.runtimePolicy = runtimePolicy;
        this.window = new SlidingWindow(runtimePolicy.getWindowSize());
        this.stateSince = now;
        this.lastAccess = now;
    }

    public synchronized Decision allowRequest(long now) {

        lastAccess = now;
        if (state == CircuitState.OPEN) {
            if ((now - stateSince) < runtimePolicy.getOpenDuration()) {
                return Decision.rejected(RejectReason.CIRCUIT_OPEN);
            }
            state = CircuitState.HALF_OPEN;
            stateSince = now;
        }

        if (state == CircuitState.HALF_OPEN) {
            if (inFlight > 0) {
                return Decision.rejected(RejectReason.CIRCUIT_OPEN);
            }
        }

        return Decision.allowed();
    }

    public synchronized Decision acquireBulkhead() {

        if (inFlight >= runtimePolicy.getMaxInFlight()) {
            return Decision.rejected(RejectReason.BULKHEAD_FULL);
        }

        inFlight++;
        return Decision.allowed();
    }

    public synchronized void releaseBulkhead(long now) {

        lastAccess = now;
        if (inFlight > 0) {
            inFlight--;
        }
    }

    public synchronized void recordResult(boolean success, long now) {

        lastAccess = now;

        if (state == CircuitState.HALF_OPEN) {
            if (success) {
                state = CircuitState.CLOSED;
                stateSince = now;
                window.reset();
            } else {
                state = CircuitState.OPEN;
                stateSince = now;
            }
            return;
        }

        if (state != CircuitState.CLOSED) {
            return;
        }

        window.record(success);
        if (window.calls() < runtimePolicy.getMinCallsToEvaluate()) {
            return;
        }

        if (window.failureRate() >= runtimePolicy.getFailureRateThreshold()) {
            state = CircuitState.OPEN;
            stateSince = now;
        }
    }

    public synchronized boolean isEvictable(long now, long idleEvict) {

        return (now - lastAccess) > idleEvict && inFlight == 0;
    }

    public synchronized CircuitState getState() {

        return state;
    }

    public synchronized int getCalls() {

        return window.calls();
    }

    public synchronized int getFailures() {

        return window.failures();
    }

    public synchronized double getFailureRate() {

        return window.failureRate();
    }

    public synchronized int getInFlight() {

        return inFlight;
    }

    public synchronized boolean hasInFlightRequests() {

        return inFlight > 0;
    }

    public synchronized long getLastAccess() {

        return lastAccess;
    }
}
