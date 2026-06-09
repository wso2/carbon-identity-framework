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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Node-local per-tenant, per-service circuit breaker manager.
 */
public class CircuitBreakerManager {

    private static final CircuitBreakerManager INSTANCE = new CircuitBreakerManager();

    private final ConcurrentHashMap<String, TenantBreakerEntry> entries = new ConcurrentHashMap<>();
    private final Lock admissionLock = new ReentrantLock();

    private final StaticPolicy staticPolicy;
    private final RuntimePolicy defaultRuntimePolicy;

    /**
     * Returns the singleton instance.
     *
     * @return the {@link CircuitBreakerManager} instance
     */
    public static CircuitBreakerManager getInstance() {

        return INSTANCE;
    }

    private CircuitBreakerManager() {

        this.staticPolicy = DefaultPolicyConfigurationLoader.getStaticPolicy();
        this.defaultRuntimePolicy = DefaultPolicyConfigurationLoader.getRuntimePolicy();
    }

    /**
     * Attempts to acquire a permit for the given tenant and service.
     *
     * @param tenantDomain The tenant domain.
     * @param service      The service being called.
     * @return The acquire decision.
     */
    public Decision tryAcquire(String tenantDomain, TenantService service) {

        if (!staticPolicy.isEnabled()) {
            return Decision.allowed();
        }

        if (StringUtils.isBlank(tenantDomain)) {
            return Decision.rejected(RejectReason.INVALID_DATA);
        }

        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        TenantBreakerEntry entry = entries.get(tenantKey);
        if (entry == null || !entry.isTracking()) {
            return Decision.skip();
        }

        long now = System.currentTimeMillis();
        AcquireSnapshot[] snap = new AcquireSnapshot[1];
        entries.computeIfPresent(tenantKey, (key, currentEntry) -> {
            if (!currentEntry.isTracking()) {
                return currentEntry;
            }
            CircuitState prev = currentEntry.getState();
            Decision decision = currentEntry.allowRequest(now);
            if (decision.isAllowed()) {
                decision = currentEntry.acquireBulkhead();
            }
            snap[0] = new AcquireSnapshot(prev, currentEntry.getState(), decision,
                    currentEntry.getCalls(), currentEntry.getFailures(), currentEntry.getInFlight(),
                    currentEntry.getFailureRate(), currentEntry.getFailureRateThreshold());
            return currentEntry;
        });

        if (snap[0] == null) {
            return Decision.skip();
        }

        AcquireSnapshot snapshot = snap[0];
        notifyTransitionIfRequired(tenantDomain, service, snapshot.previousState(), snapshot.currentState(),
                snapshot.calls(), snapshot.failures(), snapshot.failureRate(), snapshot.failureRateThreshold());
        if (!snapshot.decision().isAllowed()) {
            notifyRejection(tenantDomain, service, snapshot.decision(), snapshot.currentState(),
                    snapshot.calls(), snapshot.failures(), snapshot.failureRate(), snapshot.inFlight());
        }
        return snapshot.decision();
    }

    /**
     * Records the outcome of a previously acquired call. Must be called with the {@link Decision} returned by
     * {@link #tryAcquire}.
     *
     * @param tenantDomain    The tenant domain.
     * @param service         The service that was called.
     * @param acquireDecision The decision returned by {@link #tryAcquire}.
     * @param success         {@code true} if the call succeeded.
     */
    public void onComplete(String tenantDomain, TenantService service, Decision acquireDecision, boolean success) {

        if (StringUtils.isBlank(tenantDomain) || !staticPolicy.isEnabled() || !acquireDecision.isAllowed()) {
            return;
        }

        if (acquireDecision.isSkip() && success) {
            return;
        }

        long now = System.currentTimeMillis();
        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        if (acquireDecision.isSkip() && getOrCreateEntry(tenantKey, now) == null) {
            return;
        }

        CompleteSnapshot[] snap = new CompleteSnapshot[1];
        entries.computeIfPresent(tenantKey, (key, entry) -> {
            if (!acquireDecision.isSkip()) {
                entry.releaseBulkhead(now);
            }
            if (!entry.isTracking()) {
                return entry;
            }
            CircuitState prev = entry.getState();
            entry.recordResult(success, now);
            snap[0] = new CompleteSnapshot(prev, entry.getState(),
                    entry.getCalls(), entry.getFailures(), entry.getFailureRate(), entry.getFailureRateThreshold());
            return entry;
        });

        if (snap[0] == null) {
            return;
        }

        CompleteSnapshot snapshot = snap[0];
        notifyTransitionIfRequired(tenantDomain, service, snapshot.previousState(), snapshot.currentState(),
                snapshot.calls(), snapshot.failures(), snapshot.failureRate(), snapshot.failureRateThreshold());
    }

    /**
     * Removes all circuit-breaker entries for the given tenant across every {@link TenantService}.
     *
     * @param tenantDomain The tenant domain to invalidate.
     */
    public void invalidateTenant(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        for (TenantService service : TenantService.values()) {
            String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
            entries.computeIfPresent(tenantKey, (key, current) -> {
                current.untrack();
                return null;
            });
        }
    }

    /**
     * Removes the circuit-breaker entry for the given tenant and service.
     *
     * @param tenantDomain The tenant domain.
     * @param service      The service whose entry should be removed.
     */
    public void invalidateTenantService(String tenantDomain, TenantService service) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        entries.computeIfPresent(tenantKey, (key, current) -> {
            current.untrack();
            return null;
        });
    }

    /**
     * Returns the existing entry for {@code tenantKey}, or creates and registers a new one.
     * Returns {@code null} when the cache is at full capacity and no eviction could free a slot.
     *
     * @param tenantKey Composite key built from tenant domain and service name.
     * @param now       Current epoch-millis timestamp.
     * @return The entry, or {@code null} if the cache is full.
     */
    private TenantBreakerEntry getOrCreateEntry(String tenantKey, long now) {

        TenantBreakerEntry existing = entries.get(tenantKey);
        if (existing != null) {
            return existing;
        }

        List<String> evictedKeys = new ArrayList<>();
        TenantBreakerEntry result;
        RuntimePolicy entryPolicy = RuntimePolicyResolver.getInstance().resolve(tenantKey, defaultRuntimePolicy);
        admissionLock.lock();
        try {
            existing = entries.get(tenantKey);
            if (existing != null) {
                return existing;
            }
            ensureCapacity(now, evictedKeys);
            if (entries.size() >= staticPolicy.getTenantServiceCacheCapacity()) {
                result = null;
            } else {
                result = new TenantBreakerEntry(entryPolicy, now);
                entries.put(tenantKey, result);
            }
        } finally {
            admissionLock.unlock();
        }

        for (String evictedKey : evictedKeys) {
            notifyForcedEviction(evictedKey);
        }
        if (result == null) {
            notifyCacheFull(tenantKey);
        }
        return result;
    }

    /**
     * Evicts one or more entries to bring the cache below the eviction threshold.
     * Note: Not standalone thread-safe. Must be called while holding {@link #admissionLock}.
     *
     * @param now         Current epoch-millis timestamp.
     * @param evictedKeys List to collect keys of evicted entries.
     */
    private void ensureCapacity(long now, List<String> evictedKeys) {

        if (entries.size() < staticPolicy.getTenantServiceEvictionThreshold()) {
            return;
        }

        EvictionCandidate oldestInactive = null;
        EvictionCandidate oldestOverall = null;
        long idleTimeout = staticPolicy.getTenantServiceEntryIdleTimeout();
        boolean anyIdleEvicted = false;

        for (Map.Entry<String, TenantBreakerEntry> mapEntry : entries.entrySet()) {
            TenantBreakerEntry entry = mapEntry.getValue();
            if (entry.isEvictable(now, idleTimeout)) {
                boolean[] didEvict = {false};
                entries.computeIfPresent(mapEntry.getKey(), (key, current) -> {
                    if (!current.isEvictable(now, idleTimeout)) {
                        return current;
                    }
                    current.untrack();
                    didEvict[0] = true;
                    return null;
                });
                if (didEvict[0]) {
                    anyIdleEvicted = true;
                    evictedKeys.add(mapEntry.getKey());
                    continue;
                }
            }
            long lastAccess = entry.getLastAccess();
            if (!entry.hasInFlightRequests() && (oldestInactive == null || lastAccess < oldestInactive.lastAccess())) {
                oldestInactive = new EvictionCandidate(mapEntry.getKey(), lastAccess);
            }
            if (oldestOverall == null || lastAccess < oldestOverall.lastAccess()) {
                oldestOverall = new EvictionCandidate(mapEntry.getKey(), lastAccess);
            }
        }

        if (anyIdleEvicted) {
            return;
        }

        if (oldestInactive != null) {
            boolean[] didEvict = {false};
            entries.computeIfPresent(oldestInactive.tenantKey(), (key, current) -> {
                if (current.hasInFlightRequests()) {
                    return current;
                }
                current.untrack();
                didEvict[0] = true;
                return null;
            });
            if (didEvict[0]) {
                evictedKeys.add(oldestInactive.tenantKey());
                return;
            }
        }

        if (oldestOverall != null) {
            boolean[] didEvict = {false};
            entries.computeIfPresent(oldestOverall.tenantKey(), (key, current) -> {
                current.untrack();
                didEvict[0] = true;
                return null;
            });
            if (didEvict[0]) {
                evictedKeys.add(oldestOverall.tenantKey());
            }
        }
    }

    /**
     * Dispatches an {@link TenantServiceBreakerObserver#onForcedEviction} event for the given key.
     *
     * @param tenantKey Composite key of the evicted entry.
     */
    private void notifyForcedEviction(String tenantKey) {

        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantKey);
        TenantService service = TenantService.valueOf(parts.serviceName());
        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onForcedEviction(parts.tenantDomain(), service);
        }
    }

    /**
     * Dispatches an {@link TenantServiceBreakerObserver#onCacheFull} event for the given key.
     *
     * @param tenantKey Composite key of the entry that could not be admitted.
     */
    private void notifyCacheFull(String tenantKey) {

        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantKey);
        TenantService service = TenantService.valueOf(parts.serviceName());
        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onCacheFull(parts.tenantDomain(), service);
        }
    }

    /**
     * Dispatches a {@link TenantServiceBreakerObserver#onStateTransition} event only when the circuit state changed.
     *
     * @param tenantDomain         The tenant domain.
     * @param service              The service.
     * @param previousState        State before the last operation.
     * @param currentState         State after the last operation.
     * @param calls                Total calls in the current window.
     * @param failures             Failed calls in the current window.
     * @param failureRate          Current failure rate.
     * @param failureRateThreshold Configured threshold that triggered the transition.
     */
    private void notifyTransitionIfRequired(String tenantDomain, TenantService service, CircuitState previousState,
                                            CircuitState currentState, int calls, int failures, double failureRate,
                                            double failureRateThreshold) {

        if (previousState == currentState) {
            return;
        }

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onStateTransition(tenantDomain, service, previousState, currentState,
                    calls, failures, failureRate, failureRateThreshold);
        }
    }

    /**
     * Dispatches a {@link TenantServiceBreakerObserver#onRejection} event for a denied acquire decision.
     *
     * @param tenantDomain The tenant domain.
     * @param service      The service.
     * @param decision     The rejected decision carrying the {@link RejectReason}.
     * @param state        Current circuit state at the time of rejection.
     * @param calls        Total calls in the current window.
     * @param failures     Failed calls in the current window.
     * @param failureRate  Current failure rate.
     * @param inFlight     Number of in-flight requests at the time of rejection.
     */
    private void notifyRejection(String tenantDomain, TenantService service, Decision decision,
                                 CircuitState state, int calls, int failures, double failureRate,
                                 int inFlight) {

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onRejection(tenantDomain, service, decision.getRejectReason(),
                    state, calls, failures, failureRate, inFlight);
        }
    }

    /**
     * Returns the registered observer for the given service, or {@code null} if none is registered.
     *
     * @param service The service.
     * @return The observer, or {@code null}.
     */
    private TenantServiceBreakerObserver observerFor(TenantService service) {

        return IdentityCoreServiceDataHolder.getInstance().getTenantServiceBreakerObserver(service);
    }

    private record AcquireSnapshot(
            CircuitState previousState,
            CircuitState currentState,
            Decision decision,
            int calls,
            int failures,
            int inFlight,
            double failureRate,
            double failureRateThreshold) {

    }

    private record CompleteSnapshot(
            CircuitState previousState,
            CircuitState currentState,
            int calls,
            int failures,
            double failureRate,
            double failureRateThreshold) {

    }

    private record EvictionCandidate(String tenantKey, long lastAccess) {

    }
}
