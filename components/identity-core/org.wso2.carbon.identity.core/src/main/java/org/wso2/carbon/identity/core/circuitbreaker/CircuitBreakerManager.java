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
        TenantBreakerEntry entry = entries.get(tenantKey);
        if (entry == null || !entry.isTracking()) {
            return Decision.allowed(DecisionReason.BREAKER_SKIPPED);
        }

        long now = System.currentTimeMillis();
        CircuitState[] states = new CircuitState[2];
        Decision[] decision = new Decision[1];
        int[] intStats = new int[3];       // [0]=calls, [1]=failures, [2]=inFlight.
        double[] doubleStats = new double[2]; // [0]=failureRate, [1]=failureRateThreshold.
        entries.computeIfPresent(tenantKey, (k, e) -> {
            if (!e.isTracking()) {
                return e;
            }
            states[0] = e.getState();
            Decision d = e.allowRequest(now);
            if (d.isAllowed()) {
                d = e.acquireBulkhead();
            }
            states[1] = e.getState();
            decision[0] = d;
            intStats[0] = e.getCalls();
            intStats[1] = e.getFailures();
            intStats[2] = e.getInFlight();
            doubleStats[0] = e.getFailureRate();
            doubleStats[1] = e.getFailureRateThreshold();
            return e;
        });

        if (decision[0] == null) {
            return Decision.allowed(DecisionReason.BREAKER_SKIPPED);
        }

        notifyTransitionIfRequired(tenantDomain, service, states[0], states[1],
                intStats[0], intStats[1], doubleStats[0], doubleStats[1]);
        if (!decision[0].isAllowed()) {
            notifyRejection(tenantDomain, service, decision[0], states[1],
                    intStats[0], intStats[1], doubleStats[0], intStats[2]);
        }
        return decision[0];
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
        if (isSkipped && getOrCreateEntry(tenantKey, now) == null) {
            return;
        }

        CircuitState[] states = new CircuitState[2];
        int[] intStats = new int[2];          // [0]=calls, [1]=failures.
        double[] doubleStats = new double[2]; // [0]=failureRate, [1]=failureRateThreshold.
        entries.computeIfPresent(tenantKey, (k, e) -> {
            states[0] = e.getState();
            if (!isSkipped) {
                e.releaseBulkhead(now);
            }
            e.recordResult(success, now);
            states[1] = e.getState();
            intStats[0] = e.getCalls();
            intStats[1] = e.getFailures();
            doubleStats[0] = e.getFailureRate();
            doubleStats[1] = e.getFailureRateThreshold();
            return e;
        });

        if (states[1] == null) {
            return;
        }

        notifyTransitionIfRequired(tenantDomain, service, states[0], states[1],
                intStats[0], intStats[1], doubleStats[0], doubleStats[1]);
    }

    public void invalidateTenant(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        for (TenantService service : TenantService.values()) {
            String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
            entries.computeIfPresent(tenantKey, (k, current) -> {
                current.untrack();
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
            return null;
        });
    }

    private TenantBreakerEntry getOrCreateEntry(String tenantKey, long now) {

        TenantBreakerEntry existing = entries.get(tenantKey);
        if (existing != null) {
            return existing;
        }

        List<String> evictedKeys = new ArrayList<>();
        TenantBreakerEntry result;
        admissionLock.lock();
        try {
            existing = entries.get(tenantKey);
            if (existing != null) {
                return existing;
            }
            RuntimePolicy entryPolicy = RuntimePolicyResolver.getInstance().resolve(tenantKey, defaultRuntimePolicy);
            ensureCapacity(now, evictedKeys);
            if (entries.size() >= staticPolicy.getTenantServiceCacheCapacity()) {
                result = null;
            } else {
                result = entries.computeIfAbsent(tenantKey, k -> new TenantBreakerEntry(entryPolicy, now));
            }
        } finally {
            admissionLock.unlock();
        }

        for (String evictedKey : evictedKeys) {
            notifyForcedEviction(evictedKey);
        }
        return result;
    }

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
                entries.computeIfPresent(mapEntry.getKey(), (k, current) -> {
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
            boolean[] didEvict = {false};
            entries.computeIfPresent(oldestInactive.tenantKey, (k, current) -> {
                if (current.hasInFlightRequests()) {
                    return current;
                }
                current.untrack();
                didEvict[0] = true;
                return null;
            });
            if (didEvict[0]) {
                evictedKeys.add(oldestInactive.tenantKey);
                return;
            }
        }

        if (oldestOverall != null) {
            boolean[] didEvict = {false};
            entries.computeIfPresent(oldestOverall.tenantKey, (k, current) -> {
                current.untrack();
                didEvict[0] = true;
                return null;
            });
            if (didEvict[0]) {
                evictedKeys.add(oldestOverall.tenantKey);
            }
        }
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

    private void notifyRejection(String tenantDomain, TenantService service, Decision decision,
                                 CircuitState state, int calls, int failures, double failureRate,
                                 int inFlight) {

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
