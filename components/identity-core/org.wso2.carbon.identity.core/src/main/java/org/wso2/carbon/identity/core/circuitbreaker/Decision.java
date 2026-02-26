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

    private static final Decision ALLOWED = new Decision(true, RejectReason.NONE);

    private final boolean allowed;
    private final RejectReason rejectReason;

    private Decision(boolean allowed, RejectReason rejectReason) {

        this.allowed = allowed;
        this.rejectReason = rejectReason;
    }

    public static Decision allowed() {

        return ALLOWED;
    }

    public static Decision rejected(RejectReason reason) {

        return new Decision(false, reason);
    }

    public boolean isAllowed() {

        return allowed;
    }

    public RejectReason getRejectReason() {

        return rejectReason;
    }
}
