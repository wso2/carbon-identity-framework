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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Node-local per-tenant, per-service circuit breaker manager.
 */
public class CircuitBreakerManager {

    private static final CircuitBreakerManager INSTANCE = new CircuitBreakerManager();

    private final Shard[] shards;
    private final AtomicInteger entryCount = new AtomicInteger();
    private final AtomicLong requestCounter = new AtomicLong();
    private final AtomicInteger shardSweepCursor = new AtomicInteger();
    private final Object admissionLock = new Object();

    private final StaticPolicy staticPolicy;
    private final RuntimePolicy defaultRuntimPolicy;

    public static CircuitBreakerManager getInstance() {

        return INSTANCE;
    }

    private CircuitBreakerManager() {

        this.staticPolicy = DefaultPolicyConfigurationLoader.getStaticPolicy();
        this.defaultRuntimPolicy = DefaultPolicyConfigurationLoader.getRuntimePolicy();
        int stripeCount = Math.max(1, staticPolicy.getCacheStripes());
        this.shards = new Shard[stripeCount];
        for (int i = 0; i < stripeCount; i++) {
            this.shards[i] = new Shard();
        }
    }

    public Decision tryAcquire(String tenantDomain, TenantService service) {

        if (!staticPolicy.isEnabled()) {
            return Decision.allowed();
        }
        if (StringUtils.isBlank(tenantDomain)) {
            return Decision.rejected(RejectReason.NONE);
        }

        long nowMs = System.currentTimeMillis();
        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        maybeCleanup(nowMs);
        TenantBreakerEntry entry = getOrCreateEntry(tenantKey, nowMs);
        if (entry == null) {
            Decision decision = Decision.rejected(RejectReason.BREAKER_CACHE_FULL);
            TenantServiceBreakerObserver obs = observerFor(service);
            if (obs != null) {
                obs.onRejection(tenantDomain, service, decision.getRejectReason(), null, null, null, null, null);
            }
            return decision;
        }

        synchronized (entry) {
            CircuitState previousState = entry.getState();
            Decision decision = entry.allowRequest(nowMs);
            notifyTransitionIfRequired(tenantDomain, service, previousState, entry);
            if (!decision.isAllowed()) {
                notifyRejection(tenantDomain, service, decision, entry);
                return decision;
            }

            decision = entry.acquireBulkhead();
            if (!decision.isAllowed()) {
                notifyRejection(tenantDomain, service, decision, entry);
            }
            return decision;
        }
    }

    public void onComplete(String tenantDomain, TenantService service, boolean success) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }
        if (!staticPolicy.isEnabled()) {
            return;
        }

        long nowMs = System.currentTimeMillis();
        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        TenantBreakerEntry entry = getEntry(tenantKey);
        if (entry == null) {
            return;
        }

        synchronized (entry) {
            CircuitState previousState = entry.getState();
            entry.releaseBulkhead(nowMs);
            entry.recordResult(success, nowMs);
            notifyTransitionIfRequired(tenantDomain, service, previousState, entry);
        }
    }

    public void cleanupIdleEntries() {

        if (!staticPolicy.isEnabled()) {
            return;
        }

        evictIdleEntries(System.currentTimeMillis(), staticPolicy.getEvictionScanLimit());
    }

    public void invalidateTenant(String tenantDomain) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        for (TenantService service : TenantService.values()) {
            String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
            Shard shard = shardFor(tenantKey);
            shard.lock.lock();
            try {
                if (shard.entries.remove(tenantKey) != null) {
                    entryCount.decrementAndGet();
                }
            } finally {
                shard.lock.unlock();
            }
        }
    }

    public void invalidateTenantService(String tenantDomain, TenantService service) {

        if (StringUtils.isBlank(tenantDomain)) {
            return;
        }

        String tenantKey = TenantKeyUtil.buildTenantServiceKey(tenantDomain, service.name());
        Shard shard = shardFor(tenantKey);
        shard.lock.lock();
        try {
            if (shard.entries.remove(tenantKey) != null) {
                entryCount.decrementAndGet();
            }
        } finally {
            shard.lock.unlock();
        }
    }

    private void maybeCleanup(long nowMs) {

        if (staticPolicy.getCleanupTriggerEveryRequests() <= 0) {
            return;
        }

        long count = requestCounter.incrementAndGet();
        if (count % staticPolicy.getCleanupTriggerEveryRequests() == 0) {
            evictIdleEntries(nowMs, staticPolicy.getEvictionScanLimit());
        }
    }

    private TenantBreakerEntry getOrCreateEntry(String tenantKey, long nowMs) {

        TenantBreakerEntry existingEntry = getEntry(tenantKey);
        if (existingEntry != null) {
            return existingEntry;
        }

        synchronized (admissionLock) {
            existingEntry = getEntry(tenantKey);
            if (existingEntry != null) {
                return existingEntry;
            }

            ensureCapacity(nowMs);
            if (entryCount.get() >= staticPolicy.getMaxTenantsInCache()) {
                return null;
            }

            return putIfAbsentEntry(tenantKey, nowMs);
        }
    }

    private void ensureCapacity(long nowMs) {

        if (entryCount.get() < staticPolicy.getMaxTenantsInCache()) {
            return;
        }
        evictIdleEntries(nowMs, staticPolicy.getHardCapEvictionScanLimit());
        if (entryCount.get() < staticPolicy.getMaxTenantsInCache()) {
            return;
        }
        boolean evicted = evictOldestInactiveEntry(staticPolicy.getHardCapEvictionScanLimit());
        if (!evicted) {
            evictOldestEntry(staticPolicy.getHardCapEvictionScanLimit());
        }
    }

    private TenantBreakerEntry getEntry(String tenantKey) {

        Shard shard = shardFor(tenantKey);
        shard.lock.lock();
        try {
            return shard.entries.get(tenantKey);
        } finally {
            shard.lock.unlock();
        }
    }

    private TenantBreakerEntry putIfAbsentEntry(String tenantKey, long nowMs) {

        Shard shard = shardFor(tenantKey);
        shard.lock.lock();
        try {
            RuntimePolicy entryPolicy = RuntimePolicyResolver.getInstance().resolve(tenantKey, defaultRuntimPolicy);
            TenantBreakerEntry created = new TenantBreakerEntry(entryPolicy, nowMs);
            shard.entries.put(tenantKey, created);
            entryCount.incrementAndGet();
            return created;
        } finally {
            shard.lock.unlock();
        }
    }

    private boolean removeEntry(String tenantKey, TenantBreakerEntry expectedEntry) {

        Shard shard = shardFor(tenantKey);
        shard.lock.lock();
        try {
            TenantBreakerEntry current = shard.entries.get(tenantKey);
            if (current != expectedEntry) {
                return false;
            }

            shard.entries.remove(tenantKey);
            entryCount.decrementAndGet();
            return true;
        } finally {
            shard.lock.unlock();
        }
    }

    private void evictIdleEntries(long nowMs, int scanLimit) {

        if (scanLimit <= 0 || entryCount.get() == 0) {
            return;
        }

        int scanned = 0;
        List<String> evictedKeys = new ArrayList<>();

        int startShard = Math.floorMod(shardSweepCursor.getAndIncrement(), shards.length);
        for (int offset = 0; offset < shards.length && scanned < scanLimit; offset++) {
            Shard shard = shards[(startShard + offset) % shards.length];
            shard.lock.lock();
            try {
                Iterator<Map.Entry<String, TenantBreakerEntry>> iterator = shard.entries.entrySet().iterator();
                while (iterator.hasNext() && scanned < scanLimit) {
                    scanned++;
                    Map.Entry<String, TenantBreakerEntry> mapEntry = iterator.next();
                    if (mapEntry.getValue().isEvictable(nowMs, staticPolicy.getTenantEntryIdleEvictMs())) {
                        iterator.remove();
                        entryCount.decrementAndGet();
                        evictedKeys.add(mapEntry.getKey());
                    }
                }
            } finally {
                shard.lock.unlock();
            }
        }

        for (String evictedKey : evictedKeys) {
            TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(evictedKey);
            TenantService service = TenantService.valueOf(parts.serviceName());
            TenantServiceBreakerObserver obs = observerFor(service);
            if (obs != null) {
                obs.onForcedEviction(parts.tenantDomain(), service);
            }
        }
    }

    private boolean evictOldestInactiveEntry(int scanLimit) {

        return evictCandidate(findShardEldestCandidate(scanLimit, true));
    }

    private boolean evictOldestEntry(int scanLimit) {

        return evictCandidate(findShardEldestCandidate(scanLimit, false));
    }

    private boolean evictCandidate(EvictionCandidate candidate) {

        if (candidate == null) {
            return false;
        }

        if (removeEntry(candidate.tenantKey, candidate.entry)) {
            TenantKeyUtil.TenantKeyParts parts = TenantKeyUtil.parse(candidate.tenantKey);
            TenantService service = TenantService.valueOf(parts.serviceName());
            TenantServiceBreakerObserver obs = observerFor(service);
            if (obs != null) {
                obs.onForcedEviction(parts.tenantDomain(), service);
            }
            return true;
        }
        return false;
    }

    private EvictionCandidate findShardEldestCandidate(int scanLimit, boolean inactiveOnly) {

        if (scanLimit <= 0 || entryCount.get() == 0) {
            return null;
        }

        int scanned = 0;
        EvictionCandidate candidate = null;

        int startShard = Math.floorMod(shardSweepCursor.getAndIncrement(), shards.length);
        for (int offset = 0; offset < shards.length && scanned < scanLimit; offset++) {
            Shard shard = shards[(startShard + offset) % shards.length];
            shard.lock.lock();
            try {
                Iterator<Map.Entry<String, TenantBreakerEntry>> iterator = shard.entries.entrySet().iterator();
                if (!iterator.hasNext()) {
                    continue;
                }

                scanned++;
                Map.Entry<String, TenantBreakerEntry> eldestEntry = iterator.next();
                TenantBreakerEntry entry = eldestEntry.getValue();
                if (inactiveOnly && entry.hasInFlightRequests()) {
                    continue;
                }

                long lastAccessMs = entry.getLastAccessMs();
                if (candidate == null || lastAccessMs < candidate.lastAccessMs) {
                    candidate = new EvictionCandidate(eldestEntry.getKey(), entry, lastAccessMs);
                }
            } finally {
                shard.lock.unlock();
            }
        }

        return candidate;
    }

    private void notifyTransitionIfRequired(String tenantDomain, TenantService service, CircuitState previousState,
                                            TenantBreakerEntry entry) {

        CircuitState currentState = entry.getState();
        if (previousState == currentState) {
            return;
        }

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onStateTransition(tenantDomain, service, previousState, currentState,
                    entry.getCalls(), entry.getFailures(), entry.getFailureRate(),
                    defaultRuntimPolicy.getFailureRateThreshold());
        }
    }

    private void notifyRejection(String tenantDomain, TenantService service, Decision decision,
                                 TenantBreakerEntry entry) {

        TenantServiceBreakerObserver obs = observerFor(service);
        if (obs != null) {
            obs.onRejection(tenantDomain, service, decision.getRejectReason(), entry.getState(),
                    entry.getCalls(), entry.getFailures(), entry.getFailureRate(), entry.getInFlight());
        }
    }

    private Shard shardFor(String tenantKey) {

        return shards[Math.floorMod(tenantKey.hashCode(), shards.length)];
    }

    private TenantServiceBreakerObserver observerFor(TenantService service) {

        return IdentityCoreServiceDataHolder.getInstance().getTenantServiceBreakerObserver(service);
    }

    private static final class Shard {

        private final ReentrantLock lock = new ReentrantLock();
        private final LinkedHashMap<String, TenantBreakerEntry> entries = new LinkedHashMap<>(16, 0.75f, true);
    }

    private static final class EvictionCandidate {

        private final String tenantKey;
        private final TenantBreakerEntry entry;
        private final long lastAccessMs;

        private EvictionCandidate(String tenantKey, TenantBreakerEntry entry, long lastAccessMs) {

            this.tenantKey = tenantKey;
            this.entry = entry;
            this.lastAccessMs = lastAccessMs;
        }
    }
}
