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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Node-local per-tenant, per-service circuit breaker manager.
 */
public class CircuitBreakerManager {

    private static final CircuitBreakerManager INSTANCE = new CircuitBreakerManager();

    private final ConcurrentHashMap<String, TenantBreakerEntry> entries = new ConcurrentHashMap<>();
    private final AtomicInteger entryCount = new AtomicInteger();
    private final Object admissionLock = new Object();

    private final StaticPolicy staticPolicy;
    private final RuntimePolicy defaultRuntimePolicy;

    public static CircuitBreakerManager getInstance() {

        return INSTANCE;
    }

    private CircuitBreakerManager() {

        this.staticPolicy = DefaultPolicyConfigurationLoader.getStaticPolicy();
        this.defaultRuntimePolicy = DefaultPolicyConfigurationLoader.getRuntimePolicy();
    }

    public Decision tryAcquire(String tenantDomain, TenantService service) {

        if (!staticPolicy.isEnabled()) {
            return Decision.allowed();
        }
        
        if (StringUtils.isBlank(tenantDomain)) {
            return Decision.rejected(DecisionReason.INVALID_DATA);
        }

        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        if (!entries.containsKey(tenantKey)) {
            return Decision.allowed(DecisionReason.BREAKER_SKIPPED);
        }

        long now = System.currentTimeMillis();
        TenantBreakerEntry entry = getOrCreateEntry(tenantKey, now);
        if (entry == null) {
            Decision decision = Decision.rejected(DecisionReason.BREAKER_CACHE_FULL);
            notifyRejection(tenantDomain, service, decision, null, null, null, null, null);
            return decision;
        }

        if (!entry.isTracking()) {
            return Decision.allowed(DecisionReason.BREAKER_SKIPPED);
        }

        CircuitState previousState;
        CircuitState currentState;
        Decision decision;
        int calls, failures, inFlight;
        double failureRate;

        synchronized (entry) {
            if (!entry.isTracking()) {
                return Decision.allowed(DecisionReason.BREAKER_SKIPPED);
            }
            previousState = entry.getState();
            decision = entry.allowRequest(now);
            if (decision.isAllowed()) {
                decision = entry.acquireBulkhead();
            }
            currentState = entry.getState();
            calls = entry.getCalls();
            failures = entry.getFailures();
            failureRate = entry.getFailureRate();
            inFlight = entry.getInFlight();
        }

        notifyTransitionIfRequired(tenantDomain, service, previousState, currentState, calls, failures, failureRate);
        if (!decision.isAllowed()) {
            notifyRejection(tenantDomain, service, decision, currentState, calls, failures, failureRate, inFlight);
        }
        return decision;
    }

    public void onComplete(String tenantDomain, TenantService service, Decision acquireDecision, boolean success) {

        if (StringUtils.isBlank(tenantDomain) || !staticPolicy.isEnabled() || !acquireDecision.isAllowed()) {
            return;
        }

        boolean isSkipped = acquireDecision.getReason() == DecisionReason.BREAKER_SKIPPED;

        if (isSkipped && success) {
            return;
        }

        long now = System.currentTimeMillis();
        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        TenantBreakerEntry entry = isSkipped ? getOrCreateEntry(tenantKey, now) : entries.get(tenantKey);
        if (entry == null) {
            return;
        }

        CircuitState previousState;
        CircuitState currentState;
        int calls, failures;
        double failureRate;

        synchronized (entry) {
            previousState = entry.getState();
            if (!isSkipped) {
                entry.releaseBulkhead(now);
            }
            entry.recordResult(success, now);
            currentState = entry.getState();
            calls = entry.getCalls();
            failures = entry.getFailures();
            failureRate = entry.getFailureRate();
        }

        notifyTransitionIfRequired(tenantDomain, service, previousState, currentState, calls, failures, failureRate);
    }

    public void invalidateTenant(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        for (TenantService service : TenantService.values()) {
            String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
            entries.computeIfPresent(tenantKey, (k, current) -> {
                current.untrack();
                entryCount.decrementAndGet();
                return null;
            });
        }
    }

    public void invalidateTenantService(String tenantDomain, TenantService service) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        entries.computeIfPresent(tenantKey, (k, current) -> {
            current.untrack();
            entryCount.decrementAndGet();
            return null;
        });
    }

    private TenantBreakerEntry getOrCreateEntry(String tenantKey, long now) {

        TenantBreakerEntry existingEntry = entries.get(tenantKey);
        if (existingEntry != null) {
            return existingEntry;
        }

        RuntimePolicy entryPolicy = RuntimePolicyResolver.getInstance().resolve(tenantKey, defaultRuntimePolicy);

        List<String> evictedKeys = new ArrayList<>();
        TenantBreakerEntry result;
        synchronized (admissionLock) {
            existingEntry = entries.get(tenantKey);
            if (existingEntry != null) {
                return existingEntry;
            }

            ensureCapacity(now, evictedKeys);
            if (entryCount.get() >= staticPolicy.getTenantServiceCacheCapacity()) {
                result = null;
            } else {
                result = putIfAbsentEntry(tenantKey, entryPolicy, now);
            }
        }

        for (String evictedKey : evictedKeys) {
            notifyForcedEviction(evictedKey);
        }
        return result;
    }

    private void ensureCapacity(long now, List<String> evictedKeys) {

        if (entryCount.get() < staticPolicy.getTenantServiceEvictionThreshold()) {
            return;
        }

        EvictionCandidate oldestInactive = null;
        EvictionCandidate oldestOverall = null;
        long idleTimeout = staticPolicy.getTenantServiceEntryIdleTimeout();
        boolean anyIdleEvicted = false;

        for (Map.Entry<String, TenantBreakerEntry> mapEntry : entries.entrySet()) {
            TenantBreakerEntry entry = mapEntry.getValue();
            if (entry.isEvictable(now, idleTimeout)) {
                TenantBreakerEntry result = entries.computeIfPresent(mapEntry.getKey(), (k, current) -> {
                    if (!current.isEvictable(now, idleTimeout)) {
                        return current;
                    }
                    current.untrack();
                    entryCount.decrementAndGet();
                    return null;
                });
                if (result == null) {
                    anyIdleEvicted = true;
                    evictedKeys.add(mapEntry.getKey());
                    continue;
                }
            }
            long lastAccess = entry.getLastAccess();
            if (!entry.hasInFlightRequests() && (oldestInactive == null || lastAccess < oldestInactive.lastAccess)) {
                oldestInactive = new EvictionCandidate(mapEntry.getKey(), lastAccess);
            }
            if (oldestOverall == null || lastAccess < oldestOverall.lastAccess) {
                oldestOverall = new EvictionCandidate(mapEntry.getKey(), lastAccess);
            }
        }

        if (anyIdleEvicted) {
            return;
        }

        if (oldestInactive != null) {
            TenantBreakerEntry result = entries.computeIfPresent(oldestInactive.tenantKey, (k, current) -> {
                if (current.hasInFlightRequests()) {
                    return current;
                }
                current.untrack();
                entryCount.decrementAndGet();
                return null;
            });
            if (result == null) {
                evictedKeys.add(oldestInactive.tenantKey);
                return;
            }
        }
        
        entries.computeIfPresent(oldestOverall.tenantKey, (k, current) -> {
            current.untrack();
            entryCount.decrementAndGet();
            return null;
        });
        evictedKeys.add(oldestOverall.tenantKey);
    }

    private TenantBreakerEntry putIfAbsentEntry(String tenantKey, RuntimePolicy entryPolicy, long now) {

        TenantBreakerEntry created = new TenantBreakerEntry(entryPolicy, now);
        entries.computeIfAbsent(tenantKey, (k) -> {
            entryCount.incrementAndGet();
            return created;
        });
        return created;
    }

    private void notifyForcedEviction(String tenantKey) {

        TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(tenantKey);
        TenantService service = TenantService.valueOf(parts.serviceName());
        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onForcedEviction(parts.tenantDomain(), service);
        }
    }

    private void notifyTransitionIfRequired(String tenantDomain, TenantService service, CircuitState previousState,
                                            CircuitState currentState, int calls, int failures, double failureRate) {

        if (previousState == currentState) {
            return;
        }

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onStateTransition(tenantDomain, service, previousState, currentState,
                    calls, failures, failureRate, defaultRuntimePolicy.getFailureRateThreshold());
        }
    }

    private void notifyRejection(String tenantDomain, TenantService service, Decision decision,
                                 CircuitState state, Integer calls, Integer failures, Double failureRate,
                                 Integer inFlight) {

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onRejection(tenantDomain, service, decision.getReason(),
                    state, calls, failures, failureRate, inFlight);
        }
    }

    private TenantServiceBreakerObserver observerFor(TenantService service) {

        return IdentityCoreServiceDataHolder.getInstance().getTenantServiceBreakerObserver(service);
    }

    private static final class EvictionCandidate {

        private final String tenantKey;
        private final long lastAccess;

        private EvictionCandidate(String tenantKey, long lastAccess) {

            this.tenantKey = tenantKey;
            this.lastAccess = lastAccess;
        }
    }
}
