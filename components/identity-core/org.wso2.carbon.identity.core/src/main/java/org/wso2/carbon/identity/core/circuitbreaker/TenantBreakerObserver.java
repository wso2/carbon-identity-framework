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
 * Observer hook for transition and rejection events.
 */
public interface TenantBreakerObserver {

    TenantBreakerObserver NO_OP = new TenantBreakerObserver() {

    };

    default void onStateTransition(String tenantKey, CircuitState previousState, CircuitState currentState,
                                   int calls, int failures, double failureRate, double failureRateThreshold) {

    }

    default void onRejection(String tenantKey, RejectReason rejectReason, CircuitState state, Integer calls,
                             Integer failures, Double failureRate, Integer inFlight) {

    }

    default void onForcedEviction(String tenantKey) {

    }

    default void onUncachedAdmission(String tenantKey, RejectReason reason) {

    }
}
