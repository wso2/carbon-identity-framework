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
 * Count based window that tracks recent success/failure outcomes.
 */
class SlidingWindow {

    private final byte[] outcomes;
    private int index;
    private int filled;
    private int failures;

    /**
     * Creates a new window with the given capacity.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param windowSize maximum number of outcomes tracked.
     */
    public SlidingWindow(int windowSize) {

        this.outcomes = new byte[windowSize];
    }

    /**
     * Records a call outcome, evicting the oldest entry when the window is full.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @param success {@code true} if the call succeeded; {@code false} if it failed.
     */
    public void record(boolean success) {

        byte next = (byte) (success ? 1 : 0);
        if (filled == outcomes.length) {
            byte previous = outcomes[index];
            if (previous == 0) {
                failures--;
            }
        } else {
            filled++;
        }

        outcomes[index] = next;
        if (next == 0) {
            failures++;
        }

        index = (index + 1) % outcomes.length;
    }

    /**
     * Returns the number of recorded calls in the window.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return total calls recorded.
     */
    public int calls() {

        return filled;
    }

    /**
     * Returns the number of failed calls in the window.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return total failures recorded.
     */
    public int failures() {

        return failures;
    }

    /**
     * Returns the failure rate as a fraction between 0.0 and 1.0.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     *
     * @return failure rate, or {@code 0.0} if no calls have been recorded.
     */
    public double failureRate() {

        if (filled == 0) {
            return 0D;
        }
        return (double) failures / (double) filled;
    }

    /**
     * Resets the window, clearing all recorded outcomes.
     *
     * Note: Not standalone thread-safe. Must be accessed under the caller's synchronization.
     */
    public void reset() {

        index = 0;
        filled = 0;
        failures = 0;
    }
}
