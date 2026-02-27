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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Node-local per-tenant circuit breaker manager.
 */
public class PerTenantCircuitBreakerManager {

    private final Shard[] shards;
    private final AtomicInteger entryCount = new AtomicInteger();
    private final AtomicLong requestCounter = new AtomicLong();
    private final AtomicInteger shardSweepCursor = new AtomicInteger();
    private final Object admissionLock = new Object();

    private final Policy policy;
    private final TenantBreakerObserver observer;

    public PerTenantCircuitBreakerManager(Policy policy) {

        this(policy, TenantBreakerObserver.NO_OP);
    }

    public PerTenantCircuitBreakerManager(Policy policy, TenantBreakerObserver observer) {

        this.policy = Objects.requireNonNull(policy, "policy cannot be null");
        this.observer = Objects.requireNonNull(observer, "observer cannot be null");
        int stripeCount = Math.max(1, policy.getCacheStripes());
        this.shards = new Shard[stripeCount];
        for (int i = 0; i < stripeCount; i++) {
            this.shards[i] = new Shard();
        }
    }

    public Decision tryAcquire(String tenantKey, long nowMs) {

        if (!policy.isEnabled()) {
            return Decision.allowed();
        }

        maybeCleanup(nowMs);
        TenantBreakerEntry entry = getOrCreateEntry(tenantKey, nowMs);
        if (entry == null) {
            Decision decision = Decision.rejected(RejectReason.BREAKER_CACHE_FULL);
            observer.onRejection(tenantKey, decision.getRejectReason(), null, null, null, null, null);
            return decision;
        }

        synchronized (entry) {
            CircuitState previousState = entry.getState();
            Decision decision = entry.allowRequest(nowMs);
            notifyTransitionIfRequired(tenantKey, previousState, entry);
            if (!decision.isAllowed()) {
                notifyRejection(tenantKey, decision, entry);
                return decision;
            }

            decision = entry.acquireBulkhead();
            if (!decision.isAllowed()) {
                notifyRejection(tenantKey, decision, entry);
            }
            return decision;
        }
    }

    public void onComplete(String tenantKey, boolean success, long nowMs) {

        if (!policy.isEnabled()) {
            return;
        }

        TenantBreakerEntry entry = getEntry(tenantKey);
        if (entry == null) {
            return;
        }

        synchronized (entry) {
            CircuitState previousState = entry.getState();
            entry.releaseBulkhead(nowMs);
            entry.recordResult(success, nowMs);
            notifyTransitionIfRequired(tenantKey, previousState, entry);
        }
    }

    public void cleanupIdleEntries(long nowMs) {

        if (!policy.isEnabled()) {
            return;
        }

        evictIdleEntries(nowMs, policy.getEvictionScanLimit());
    }

    public boolean isEnabled() {

        return policy.isEnabled();
    }

    Policy getPolicy() {

        return policy;
    }

    int getEntryCount() {

        return entryCount.get();
    }

    CircuitState getState(String tenantKey) {

        TenantBreakerEntry entry = getEntry(tenantKey);
        return entry == null ? null : entry.getState();
    }

    private void maybeCleanup(long nowMs) {

        if (policy.getCleanupTriggerEveryRequests() <= 0) {
            return;
        }

        long count = requestCounter.incrementAndGet();
        if (count % policy.getCleanupTriggerEveryRequests() == 0) {
            cleanupIdleEntries(nowMs);
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
            if (entryCount.get() >= policy.getMaxTenantsInCache()) {
                return null;
            }

            return putIfAbsentEntry(tenantKey, nowMs);
        }
    }

    private void ensureCapacity(long nowMs) {

        int attempts = 0;
        while (entryCount.get() >= policy.getMaxTenantsInCache() && attempts < 4) {
            attempts++;
            evictIdleEntries(nowMs, policy.getHardCapEvictionScanLimit());
            if (entryCount.get() < policy.getMaxTenantsInCache()) {
                return;
            }

            boolean evicted = evictOldestInactiveEntry(policy.getHardCapEvictionScanLimit());
            if (!evicted) {
                evicted = evictOldestEntry(policy.getHardCapEvictionScanLimit());
            }
            if (!evicted) {
                return;
            }
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
            TenantBreakerEntry existing = shard.entries.get(tenantKey);
            if (existing != null) {
                return existing;
            }

            TenantBreakerEntry created = new TenantBreakerEntry(policy, nowMs);
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
                    if (mapEntry.getValue().isEvictable(nowMs, policy.getTenantEntryIdleEvictMs())) {
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
            observer.onForcedEviction(evictedKey);
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
            observer.onForcedEviction(candidate.tenantKey);
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

    private void notifyTransitionIfRequired(String tenantKey, CircuitState previousState, TenantBreakerEntry entry) {

        CircuitState currentState = entry.getState();
        if (previousState == currentState) {
            return;
        }

        observer.onStateTransition(tenantKey, previousState, currentState, entry.getCalls(), entry.getFailures(),
                entry.getFailureRate(), policy.getFailureRateThreshold());
    }

    private void notifyRejection(String tenantKey, Decision decision, TenantBreakerEntry entry) {

        observer.onRejection(tenantKey, decision.getRejectReason(), entry.getState(), entry.getCalls(),
                entry.getFailures(), entry.getFailureRate(), entry.getInFlight());
    }

    private Shard shardFor(String tenantKey) {

        return shards[Math.floorMod(tenantKey.hashCode(), shards.length)];
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
