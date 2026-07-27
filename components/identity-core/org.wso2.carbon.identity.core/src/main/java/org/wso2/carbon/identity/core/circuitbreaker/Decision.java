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
 * Admission decision produced by the breaker manager.
 */
public final class Decision {

    private static final Decision ALLOWED = new Decision(AllowReason.NONE);
    private static final Decision ALLOWED_SKIP = new Decision(AllowReason.SKIPPED);
    private static final Decision REJECTED_INVALID_DATA = new Decision(RejectReason.INVALID_DATA);
    private static final Decision REJECTED_CIRCUIT_OPEN = new Decision(RejectReason.CIRCUIT_OPEN);
    private static final Decision REJECTED_BULKHEAD_FULL = new Decision(RejectReason.BULKHEAD_FULL);

    private final AllowReason allowReason;
    private final RejectReason rejectReason;

    private Decision(AllowReason allowReason) {

        this.allowReason = allowReason;
        this.rejectReason = null;
    }

    private Decision(RejectReason rejectReason) {

        this.allowReason = null;
        this.rejectReason = rejectReason;
    }

    /**
     * Returns the shared allowed decision.
     *
     * @return a {@link Decision} that permits the request.
     */
    public static Decision allowed() {

        return ALLOWED;
    }

    /**
     * Returns the shared skip decision.
     *
     * @return a {@link Decision} that permits the request without tracking.
     */
    public static Decision skip() {

        return ALLOWED_SKIP;
    }

    /**
     * Returns the shared rejected decision for the given reason.
     *
     * @param reason The reason for rejection.
     * @return a {@link Decision} that denies the request.
     */
    public static Decision rejected(RejectReason reason) {

        switch (reason) {
            case RejectReason.CIRCUIT_OPEN:
                return REJECTED_CIRCUIT_OPEN;
            case RejectReason.BULKHEAD_FULL:
                return REJECTED_BULKHEAD_FULL;
            default:
                return REJECTED_INVALID_DATA;
        }
    }

    /**
     * Returns {@code true} if the request is permitted (including skipped decisions).
     *
     * @return {@code true} if allowed or skipped; {@code false} if rejected.
     */
    public boolean isAllowed() {

        return allowReason != null;
    }

    /**
     * Returns {@code true} if this decision was produced when no tracking entry existed for the tenant and service.
     *
     * @return {@code true} if the decision is a skip; {@code false} otherwise.
     */
    public boolean isSkip() {

        return allowReason == AllowReason.SKIPPED;
    }

    /**
     * Returns the reason this request was rejected, or {@code null} if the request was allowed.
     *
     * @return the {@link RejectReason}, or {@code null} if allowed.
     */
    public RejectReason getRejectReason() {

        return rejectReason;
    }
}
