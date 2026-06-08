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
 * Observer hook for transition and rejection events for a specific tenant service.
 */
public interface TenantServiceBreakerObserver {

    /**
     * Returns the tenant service this observer is registered for.
     *
     * @return The {@link TenantService} handled by this observer.
     */
    public TenantService getService();

    /**
     * Invoked when the circuit breaker transitions between states for a tenant-service pair.
     *
     * @param tenantDomain         The tenant domain.
     * @param service              The tenant service.
     * @param previousState        The state before the transition.
     * @param currentState         The state after the transition.
     * @param calls                Total calls recorded in the current window.
     * @param failures             Failed calls recorded in the current window.
     * @param failureRate          Current failure rate.
     * @param failureRateThreshold Configured threshold that triggered the transition.
     */
    public void onStateTransition(String tenantDomain, TenantService service, CircuitState previousState,
                                  CircuitState currentState, int calls, int failures, double failureRate,
                                  double failureRateThreshold);

    /**
     * Invoked when a request is rejected by the circuit breaker for a tenant-service pair.
     *
     * @param tenantDomain The tenant domain.
     * @param service      The tenant service.
     * @param rejectReason The reason the request was rejected.
     * @param state        The current circuit state at the time of rejection.
     * @param calls        Total calls recorded in the current window.
     * @param failures     Failed calls recorded in the current window.
     * @param failureRate  Current failure rate.
     * @param inFlight     Number of in-flight requests at the time of rejection.
     */
    public void onRejection(String tenantDomain, TenantService service, DecisionReason rejectReason, CircuitState state,
                            Integer calls, Integer failures, Double failureRate, Integer inFlight);

    /**
     * Invoked when the entry for a tenant-service pair is forcibly evicted from the breaker cache.
     *
     * @param tenantDomain The tenant domain.
     * @param service      The tenant service whose entry was evicted.
     */
    public void onForcedEviction(String tenantDomain, TenantService service);
}
