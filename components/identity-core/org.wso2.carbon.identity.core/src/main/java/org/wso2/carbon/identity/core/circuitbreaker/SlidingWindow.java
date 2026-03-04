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

    SlidingWindow(int windowSize) {

        this.outcomes = new byte[windowSize];
    }

    void record(boolean success) {

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

    int calls() {

        return filled;
    }

    int failures() {

        return failures;
    }

    double failureRate() {

        if (filled == 0) {
            return 0D;
        }
        return (double) failures / (double) filled;
    }

    void reset() {

        index = 0;
        filled = 0;
        failures = 0;
    }
}
