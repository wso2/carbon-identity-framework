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
    private volatile long lastAccess;

    private volatile int inFlight;
    private volatile boolean tracking;

    /**
     * Creates a new entry with the given policy, initializing state to CLOSED.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param runtimePolicy the policy governing this entry's breaker behaviour.
     * @param now           current epoch-millis timestamp.
     */
    public TenantBreakerEntry(RuntimePolicy runtimePolicy, long now) {

        this.runtimePolicy = runtimePolicy;
        this.window = new SlidingWindow(runtimePolicy.getWindowSize());
        this.stateSince = now;
        this.lastAccess = now;
        this.tracking = true;
    }

    /**
     * Checks whether the current circuit state permits a new request.
     * Transitions OPEN to HALF_OPEN when the open duration has elapsed.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param now current epoch-millis timestamp.
     * @return an allowed {@link Decision}, or a rejected one if the circuit is open.
     */
    public Decision allowRequest(long now) {

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

    /**
     * Attempts to acquire a bulkhead slot for an in-flight request.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return an allowed {@link Decision}, or rejected with BULKHEAD_FULL if the limit is reached.
     */
    public Decision acquireBulkhead() {

        if (inFlight >= runtimePolicy.getMaxInFlight()) {
            return Decision.rejected(RejectReason.BULKHEAD_FULL);
        }

        inFlight++;
        return Decision.allowed();
    }

    /**
     * Releases a previously acquired bulkhead slot.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param now current epoch-millis timestamp.
     */
    public void releaseBulkhead(long now) {

        lastAccess = now;
        if (inFlight > 0) {
            inFlight--;
        }
    }

    /**
     * Records the outcome of a completed call and updates the circuit state if required.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param success {@code true} if the call succeeded; {@code false} if it failed.
     * @param now     current epoch-millis timestamp.
     */
    public void recordResult(boolean success, long now) {

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
        } else if (state == CircuitState.CLOSED) {
            window.record(success);
            if (window.calls() >= runtimePolicy.getMinCallsToEvaluate()
                    && window.failureRate() >= runtimePolicy.getFailureRateThreshold()) {
                state = CircuitState.OPEN;
                stateSince = now;
            }
        }

        tracking = !(state == CircuitState.CLOSED
                && window.calls() >= runtimePolicy.getMinCallsToEvaluate()
                && window.failures() == 0);
    }

    /**
     * Returns {@code true} if this entry is still being tracked by the breaker manager.
     *
     * @return {@code true} if tracking; {@code false} if evicted or untracked.
     */
    public boolean isTracking() {

        return tracking;
    }

    /**
     * Marks this entry as no longer tracked, signalling that it should be removed from the cache.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     */
    public void untrack() {

        tracking = false;
    }

    /**
     * Returns {@code true} if this entry is idle and eligible for eviction.
     *
     * @param now        current epoch-millis timestamp.
     * @param idleEvict  idle timeout in milliseconds after which the entry is evictable.
     * @return {@code true} if the entry has been idle longer than {@code idleEvict} with no in-flight requests.
     */
    public boolean isEvictable(long now, long idleEvict) {

        return (now - lastAccess) > idleEvict && inFlight == 0;
    }

    /**
     * Returns the current circuit state.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return the current {@link CircuitState}.
     */
    public CircuitState getState() {

        return state;
    }

    /**
     * Returns the total number of calls recorded in the current window.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return total call count.
     */
    public int getCalls() {

        return window.calls();
    }

    /**
     * Returns the number of failed calls in the current window.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return failure count.
     */
    public int getFailures() {

        return window.failures();
    }

    /**
     * Returns the current failure rate as a fraction between 0.0 and 1.0.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return failure rate.
     */
    public double getFailureRate() {

        return window.failureRate();
    }

    /**
     * Returns the number of currently in-flight requests.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return in-flight request count.
     */
    public int getInFlight() {

        return inFlight;
    }

    /**
     * Returns {@code true} if there is at least one in-flight request.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return {@code true} if any requests are in flight.
     */
    public boolean hasInFlightRequests() {

        return inFlight > 0;
    }

    /**
     * Returns the timestamp of the last access to this entry.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return last access time in epoch-millis.
     */
    public long getLastAccess() {

        return lastAccess;
    }

    /**
     * Returns the failure rate threshold configured in the runtime policy.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return failure rate threshold.
     */
    public double getFailureRateThreshold() {

        return runtimePolicy.getFailureRateThreshold();
    }
}
