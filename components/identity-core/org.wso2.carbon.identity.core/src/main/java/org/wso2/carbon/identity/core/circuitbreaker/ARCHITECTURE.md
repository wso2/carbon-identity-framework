# Circuit Breaker — Architecture Diagrams

---

## 1. High-Level Component Overview

```mermaid
graph TB
    subgraph config["Configuration (identity.xml)"]
        XML["identity.xml"]
        DPCL["DefaultPolicyConfigurationLoader<br/>(static Holder — lazy loaded once)"]
        SP["StaticPolicy<br/>───────────────────<br/>enabled · cacheStripes<br/>maxTenantsInCache<br/>tenantEntryIdleEvictMs<br/>cleanupTriggerEveryRequests<br/>evictionScanLimit"]
        RP_DEF["RuntimePolicy (default)<br/>───────────────────<br/>windowSize · minCallsToEvaluate<br/>failureRateThreshold<br/>openDurationMs · maxInFlight"]
        XML --> DPCL
        DPCL --> SP
        DPCL --> RP_DEF
    end

    subgraph engine["Core Engine"]
        CBM["CircuitBreakerManager<br/>(Singleton)<br/>───────────────────<br/>tryAcquire()<br/>onComplete()<br/>cleanupIdleEntries()"]
        SHARDS["Shards[N]<br/>(striped LinkedHashMap<br/>+ ReentrantLock each)"]
        TBE["TenantBreakerEntry<br/>(per tenant × service)<br/>───────────────────<br/>state · stateSinceMs<br/>inFlight · lastAccessMs"]
        SW["SlidingWindow<br/>(count-based ring buffer)<br/>───────────────────<br/>record() · calls()<br/>failures() · failureRate()"]
        TKU["TenantKeyUtil<br/>key = 'tenant:SERVICE'"]
    end

    subgraph resolution["Policy Resolution"]
        RPR["RuntimePolicyResolver<br/>(Singleton)<br/>resolve(tenantKey, defaultPolicy)"]
        RPL["«interface»<br/>RuntimePolicyLoader<br/>───────────────────<br/>getService()<br/>load(tenant, service, policy)"]
        RPE["«interface»<br/>RuntimePolicyExtender<br/>───────────────────<br/>extend(tenant, service, policy)"]
    end

    subgraph ext["Extension / Observation"]
        TSBO["«interface»<br/>TenantServiceBreakerObserver<br/>───────────────────<br/>onStateTransition()<br/>onRejection()<br/>onForcedEviction()"]
        TS["TenantService<br/>«enum»<br/>───────────────────<br/>SMS_OTP<br/>EMAIL_OTP"]
    end

    subgraph osgi["OSGi Service Registry"]
        DSH["IdentityCoreServiceDataHolder<br/>───────────────────<br/>RuntimePolicyLoader (per service)<br/>RuntimePolicyExtender (global)<br/>TenantServiceBreakerObserver (per service)"]
    end

    SP --> CBM
    RP_DEF --> CBM
    CBM --> SHARDS
    SHARDS -->|"shardFor(key)"| TBE
    TBE --> SW
    TBE -->|"reads volatile fields"| RP_DEF
    CBM --> RPR
    RPR -->|"1. clone default"| RP_DEF
    RPR -->|"2. service loader"| RPL
    RPR -->|"3. global extender"| RPE
    RPR --> DSH
    CBM --> DSH
    DSH --> TSBO
    DSH --> RPL
    DSH --> RPE
    TS -.->|"keyed by"| DSH
    TKU --> CBM
```

---

## 2. How a Service Uses the Circuit Breaker

```mermaid
sequenceDiagram
    participant Caller as Caller (e.g. SmsOtpProvider)
    participant CBM as CircuitBreakerManager
    participant Provider as External Provider

    Caller->>CBM: tryAcquire(tenantDomain, SMS_OTP)
    CBM-->>Caller: Decision

    alt Decision.isAllowed() == true
        Caller->>Provider: send OTP (HTTP / SDK call)

        alt Provider responds successfully
            Provider-->>Caller: 200 OK
            Caller->>CBM: onComplete(tenantDomain, SMS_OTP, success=true)
        else Provider fails / times out
            Provider-->>Caller: 5xx / timeout
            Caller->>CBM: onComplete(tenantDomain, SMS_OTP, success=false)
        end

        Caller-->>Caller: return result to user

    else Decision.isAllowed() == false
        Note over Caller: Skip provider call entirely
        Caller-->>Caller: throw / return error (CIRCUIT_OPEN, BULKHEAD_FULL, or BREAKER_CACHE_FULL)
    end
```

> **Key contract:** Every allowed `tryAcquire` MUST be paired with exactly one `onComplete`.
> The circuit breaker tracks in-flight concurrency via the bulkhead; leaking a call skews the count permanently.

---

## 3. Policy Loading — Static and Runtime

### 3a. Startup: Loading Defaults from identity.xml

```mermaid
flowchart LR
    XML["identity.xml<br/>CircuitBreaker.*"]

    subgraph static_load["Loaded once at startup — requires restart to change"]
        SP_B["StaticPolicy.Builder"]
        SP["StaticPolicy<br/>• enabled<br/>• cacheStripes<br/>• maxTenantsInCache<br/>• tenantEntryIdleEvictMs<br/>• cleanupTriggerEveryRequests<br/>• evictionScanLimit<br/>• hardCapEvictionScanLimit"]
    end

    subgraph runtime_load["Loaded once at startup — can be updated at runtime without restart"]
        RP_B["RuntimePolicy.Builder"]
        RP["RuntimePolicy<br/>• windowSize<br/>• minCallsToEvaluate<br/>• failureRateThreshold<br/>• openDurationMs<br/>• maxInFlight"]
    end

    CBM["CircuitBreakerManager<br/>(constructor)"]

    XML -->|"IdentityUtil.getProperty()"| SP_B --> SP --> CBM
    XML -->|"IdentityUtil.getProperty()"| RP_B --> RP --> CBM
```

### 3b. Per-Entry: Runtime Policy Resolution Chain

Called once when a new `TenantBreakerEntry` is created (first request for a tenant × service pair).

```mermaid
flowchart TD
    TRIGGER(["New entry needed for 'wso2.com:SMS_OTP'"])
    CLONE["1. Clone server default RuntimePolicy<br/>(all fields copied)"]
    LOADER{"2. RuntimePolicyLoader<br/>registered for SMS_OTP?"}
    LOADER_APPLY["Call loader.load(tenant, SMS_OTP, clonedPolicy)<br/>Returns overridden policy or null"]
    EXTENDER{"3. RuntimePolicyExtender<br/>registered (global)?"}
    EXTENDER_APPLY["Call extender.extend(tenant, SMS_OTP, currentPolicy)<br/>Returns final policy or null"]
    ENTRY["TenantBreakerEntry created<br/>with resolved RuntimePolicy"]

    TRIGGER --> CLONE --> LOADER
    LOADER -->|"yes"| LOADER_APPLY --> EXTENDER
    LOADER -->|"no"| EXTENDER
    EXTENDER -->|"yes"| EXTENDER_APPLY --> ENTRY
    EXTENDER -->|"no"| ENTRY
```

> **Precedence:** `RuntimePolicyExtender` always runs last and wins over `RuntimePolicyLoader`.
> Returning `null` from either hook keeps the current policy unchanged.

---

## 4. Observer Loading and Event Dispatch

### 4a. OSGi Registration (startup)

```mermaid
sequenceDiagram
    participant Bundle as Component Bundle
    participant OSGi as OSGi Runtime
    participant DSH as IdentityCoreServiceDataHolder

    Note over Bundle: Activator / @Component
    Bundle->>OSGi: register TenantServiceBreakerObserver (getService = SMS_OTP)
    OSGi->>DSH: setTenantServiceBreakerObserver(observer)
    DSH->>DSH: observers.put(SMS_OTP, observer)

    Note over Bundle: Same pattern for RuntimePolicyLoader and RuntimePolicyExtender
```

### 4b. Event Dispatch During Request Lifecycle

```mermaid
sequenceDiagram
    participant CBM as CircuitBreakerManager
    participant DSH as IdentityCoreServiceDataHolder
    participant OBS as TenantServiceBreakerObserver

    Note over CBM: On state change (CLOSED → OPEN etc.)
    CBM->>DSH: getTenantServiceBreakerObserver(SMS_OTP)
    DSH-->>CBM: observer
    CBM->>OBS: onStateTransition(tenant, service, prevState, newState, calls, failures, failureRate, threshold)

    Note over CBM: On request rejected
    CBM->>OBS: onRejection(tenant, service, CIRCUIT_OPEN | BULKHEAD_FULL | BREAKER_CACHE_FULL, ...)

    Note over CBM: On idle entry evicted from cache
    CBM->>OBS: onForcedEviction(tenant, service)
```

---

## 5. Circuit Breaker Internal Flows

### 5a. State Machine

```mermaid
stateDiagram-v2
    [*] --> CLOSED : Entry created

    CLOSED --> OPEN : failureRate >= threshold AND calls >= minCalls

    OPEN --> HALF_OPEN : openDurationMs elapsed (on next tryAcquire)

    HALF_OPEN --> CLOSED : probe request succeeds (window reset)

    HALF_OPEN --> OPEN : probe request fails

    note right of CLOSED
        Requests flow freely.
        SlidingWindow accumulates
        success/failure outcomes.
    end note

    note right of OPEN
        All requests rejected
        with CIRCUIT_OPEN.
        No in-flight allowed.
    end note

    note right of HALF_OPEN
        One probe allowed at a time
        (inFlight == 0 gate).
        Subsequent requests rejected
        with CIRCUIT_OPEN.
    end note
```

---

### 5b. `tryAcquire()` Flow

```mermaid
flowchart TD
    START(["tryAcquire(tenantDomain, service)"])
    CHK_EN{"staticPolicy<br/>.isEnabled()?"}
    CHK_TENANT{"tenantDomain<br/>blank?"}
    CLEANUP["maybeCleanup()<br/>(evict idle entries every N requests)"]
    GET_ENTRY["getOrCreateEntry(tenantKey)"]
    CHK_ENTRY{"entry == null?<br/>(cache full)"}
    REJ_CACHE["reject(BREAKER_CACHE_FULL)<br/>+ observer.onRejection()"]

    SYNC["synchronized(entry) — save previousState"]
    CHK_OPEN{"state == OPEN?"}
    CHK_DUR{"openDuration<br/>elapsed?"}
    TRANS_HO["state → HALF_OPEN"]
    CHK_HO{"state == HALF_OPEN<br/>&& inFlight > 0?"}
    NOTIFY_T["notifyTransitionIfRequired()"]
    CHK_AR{"allowRequest<br/>allowed?"}
    NOTIFY_R["observer.onRejection()"]
    BULKHEAD["entry.acquireBulkhead()"]
    CHK_BH{"inFlight >=<br/>maxInFlight?"}
    REJ_BH["reject(BULKHEAD_FULL)<br/>+ observer.onRejection()"]
    INC["inFlight++"]
    ALLOWED(["return Decision.allowed()"])
    REJECTED(["return Decision.rejected(reason)"])

    START --> CHK_EN
    CHK_EN -->|"no"| ALLOWED
    CHK_EN -->|"yes"| CHK_TENANT
    CHK_TENANT -->|"yes"| REJECTED
    CHK_TENANT -->|"no"| CLEANUP
    CLEANUP --> GET_ENTRY --> CHK_ENTRY
    CHK_ENTRY -->|"yes"| REJ_CACHE --> REJECTED
    CHK_ENTRY -->|"no"| SYNC

    SYNC --> CHK_OPEN
    CHK_OPEN -->|"yes"| CHK_DUR
    CHK_DUR -->|"not yet — CIRCUIT_OPEN"| NOTIFY_T
    CHK_DUR -->|"elapsed"| TRANS_HO --> CHK_HO
    CHK_OPEN -->|"no"| CHK_HO
    CHK_HO -->|"yes — CIRCUIT_OPEN"| NOTIFY_T
    CHK_HO -->|"no — ALLOWED"| NOTIFY_T

    NOTIFY_T --> CHK_AR
    CHK_AR -->|"no"| NOTIFY_R --> REJECTED
    CHK_AR -->|"yes"| BULKHEAD
    BULKHEAD --> CHK_BH
    CHK_BH -->|"yes"| REJ_BH --> REJECTED
    CHK_BH -->|"no"| INC --> ALLOWED
```

---

### 5c. `onComplete()` Flow

```mermaid
flowchart TD
    START(["onComplete(tenantDomain, service, success)"])
    CHK_TENANT{"tenantDomain<br/>blank?"}
    CHK_EN{"staticPolicy<br/>.isEnabled()?"}
    GET_E["getEntry(tenantKey)<br/>(no creation)"]
    CHK_NULL{"entry == null?"}
    SYNC["synchronized(entry)"]
    RELEASE["entry.releaseBulkhead()<br/>inFlight--"]
    RECORD["entry.recordResult(success, nowMs)"]
    CHK_HO{"state ==<br/>HALF_OPEN?"}
    CHK_SUC{"success?"}
    TO_CLOSED["state → CLOSED<br/>window.reset()"]
    TO_OPEN["state → OPEN"]
    CHK_CLOSED{"state ==<br/>CLOSED?"}
    WIN_RECORD["window.record(success)"]
    CHK_MIN{"calls >=<br/>minCallsToEvaluate?"}
    CHK_RATE{"failureRate >=<br/>threshold?"}
    TRIP["state → OPEN"]
    NOTIFY["notifyTransitionIfRequired()"]
    END(["return"])

    START --> CHK_TENANT
    CHK_TENANT -->|"yes"| END
    CHK_TENANT -->|"no"| CHK_EN
    CHK_EN -->|"no"| END
    CHK_EN -->|"yes"| GET_E --> CHK_NULL
    CHK_NULL -->|"yes"| END
    CHK_NULL -->|"no"| SYNC

    SYNC --> RELEASE --> RECORD --> CHK_HO
    CHK_HO -->|"yes"| CHK_SUC
    CHK_SUC -->|"yes"| TO_CLOSED --> NOTIFY
    CHK_SUC -->|"no"| TO_OPEN --> NOTIFY
    CHK_HO -->|"no"| CHK_CLOSED
    CHK_CLOSED -->|"no"| END
    CHK_CLOSED -->|"yes"| WIN_RECORD --> CHK_MIN
    CHK_MIN -->|"no"| END
    CHK_MIN -->|"yes"| CHK_RATE
    CHK_RATE -->|"no"| END
    CHK_RATE -->|"yes"| TRIP --> NOTIFY --> END
```

---

### 5d. Sliding Window (Count-Based Ring Buffer)

```mermaid
graph LR
    subgraph window["SlidingWindow — windowSize = 5, current: 3 failures in 5 calls"]
        direction LR
        S1["✗ fail"]
        S2["✓ ok"]
        S3["✗ fail"]
        S4["✓ ok"]
        S5["✗ fail"]
        IDX["▲ next index"]
        S5 --> IDX
    end

    RATE["failureRate = 3/5 = 0.60<br/>threshold = 0.50<br/>→ TRIP to OPEN"]

    window --> RATE
```

> The window is a fixed-size `byte[]` ring buffer. When full, the oldest outcome is overwritten and its failure/success contribution is subtracted before adding the new one. `reset()` clears the window on `HALF_OPEN → CLOSED` transition.

---

### 5e. `maybeCleanup()` Flow

Called on every `tryAcquire` to periodically trigger idle-entry eviction without a dedicated background thread.

```mermaid
flowchart TD
    START(["maybeCleanup(nowMs)"])
    CHK_DISABLED{"cleanupTriggerEveryRequests<br/><= 0?"}
    INC["requestCounter.incrementAndGet()"]
    CHK_MOD{"count %<br/>cleanupTriggerEveryRequests == 0?"}
    EVICT["evictIdleEntries(nowMs, evictionScanLimit)"]
    END(["return"])

    START --> CHK_DISABLED
    CHK_DISABLED -->|"yes — disabled"| END
    CHK_DISABLED -->|"no"| INC --> CHK_MOD
    CHK_MOD -->|"no"| END
    CHK_MOD -->|"yes"| EVICT --> END
```

---

### 5f. `evictIdleEntries()` Flow

Scans up to `scanLimit` entries across shards in rotating order and removes any that have been idle longer than `tenantEntryIdleEvictMs` and have no in-flight requests.

```mermaid
flowchart TD
    START(["evictIdleEntries(nowMs, scanLimit)"])
    CHK_EMPTY{"scanLimit <= 0<br/>or entryCount == 0?"}
    INIT["evictedKeys = []<br/>startShard = shardSweepCursor++<br/>scanned = 0"]
    LOOP_SHARD{"more shards<br/>and scanned < scanLimit?"}
    LOCK["shard.lock.lock()"]
    LOOP_ENTRY{"iterator.hasNext()<br/>and scanned < scanLimit?"}
    INC_SCAN["scanned++"]
    CHK_EVICT{"entry.isEvictable(nowMs,<br/>idleEvictMs)?<br/>(idle > threshold and inFlight == 0)"}
    REMOVE["iterator.remove()<br/>entryCount--<br/>evictedKeys.add(key)"]
    UNLOCK["shard.lock.unlock()"]
    NEXT_KEY{"more evicted keys?"}
    NOTIFY["observer.onForcedEviction(tenantDomain, service)"]
    END(["return"])

    START --> CHK_EMPTY
    CHK_EMPTY -->|"yes"| END
    CHK_EMPTY -->|"no"| INIT --> LOOP_SHARD
    LOOP_SHARD -->|"yes"| LOCK --> LOOP_ENTRY
    LOOP_ENTRY -->|"yes"| INC_SCAN --> CHK_EVICT
    CHK_EVICT -->|"yes"| REMOVE --> LOOP_ENTRY
    CHK_EVICT -->|"no"| LOOP_ENTRY
    LOOP_ENTRY -->|"no"| UNLOCK --> LOOP_SHARD
    LOOP_SHARD -->|"no"| NEXT_KEY
    NEXT_KEY -->|"yes"| NOTIFY --> NEXT_KEY
    NEXT_KEY -->|"no"| END
```

> Observer notifications are fired **after all shard locks are released** — evicted keys are collected inside the locked section and dispatched in a separate pass to avoid holding the lock during observer callbacks.

---

### 5g. `getOrCreateEntry()` + `ensureCapacity()` + `putIfAbsentEntry()` Flow

Called from `tryAcquire` to look up or create the `TenantBreakerEntry` for a tenant × service pair. Uses a double-checked lock pattern to minimise contention on the common (entry already exists) path.

```mermaid
flowchart TD
    START(["getOrCreateEntry(tenantKey, nowMs)"])
    SL1["shard.lock.lock()"]
    GET1["shard.entries.get(tenantKey)"]
    SUL1["shard.lock.unlock()"]
    CHK1{"entry found?"}
    ADM["synchronized(admissionLock)"]
    SL2["shard.lock.lock()"]
    GET2["shard.entries.get(tenantKey)<br/>(double-check)"]
    SUL2["shard.lock.unlock()"]
    CHK2{"entry found?"}
    CAP["ensureCapacity(nowMs)"]
    CHK_FULL{"entryCount >= maxTenantsInCache?"}
    NULL(["return null — cache full"])
    PUT["putIfAbsentEntry(tenantKey, nowMs)"]
    RETURN(["return entry"])

    START --> SL1 --> GET1 --> SUL1 --> CHK1
    CHK1 -->|"yes — fast path"| RETURN
    CHK1 -->|"no"| ADM --> SL2 --> GET2 --> SUL2 --> CHK2
    CHK2 -->|"yes"| RETURN
    CHK2 -->|"no"| CAP --> CHK_FULL
    CHK_FULL -->|"yes"| NULL
    CHK_FULL -->|"no"| PUT --> RETURN

    subgraph ensureCapacity["ensureCapacity(nowMs)"]
        EC_CHK{"entryCount < max?"}
        EC_IDLE["evictIdleEntries(nowMs,<br/>hardCapEvictionScanLimit)<br/>(uses shard locks internally)"]
        EC_CHK2{"entryCount < max?"}
        EC_INACTIVE["evictOldestInactiveEntry()<br/>(uses shard locks internally)"]
        EC_CHK3{"evicted?"}
        EC_OLDEST["evictOldestEntry()<br/>(uses shard locks internally)"]
        EC_END(["return"])

        EC_CHK -->|"yes — has room"| EC_END
        EC_CHK -->|"no"| EC_IDLE --> EC_CHK2
        EC_CHK2 -->|"yes — freed"| EC_END
        EC_CHK2 -->|"no"| EC_INACTIVE --> EC_CHK3
        EC_CHK3 -->|"yes"| EC_END
        EC_CHK3 -->|"no"| EC_OLDEST --> EC_END
    end

    subgraph putIfAbsentEntry["putIfAbsentEntry(tenantKey, nowMs)"]
        PA_LOCK["shard.lock.lock()"]
        PA_RESOLVE["RuntimePolicyResolver.resolve(<br/>tenantKey, defaultPolicy)"]
        PA_CREATE["new TenantBreakerEntry(resolvedPolicy, nowMs)"]
        PA_PUT["shard.entries.put(tenantKey, entry)<br/>entryCount++"]
        PA_UNLOCK["shard.lock.unlock()"]
        PA_RET(["return entry"])

        PA_LOCK --> PA_RESOLVE --> PA_CREATE --> PA_PUT --> PA_UNLOCK --> PA_RET
    end

    CAP --> ensureCapacity
    PUT --> putIfAbsentEntry
```

---

### 5h. `evictOldestInactiveEntry()` and `evictOldestEntry()` Flow

Both methods share the same internal chain — `findShardEldestCandidate` → `evictCandidate` → `removeEntry` — differing only in the `inactiveOnly` flag.

```mermaid
flowchart TD
    START_I(["evictOldestInactiveEntry(scanLimit)"])
    START_A(["evictOldestEntry(scanLimit)"])
    NOTE["inactiveOnly=true — skips entries that still have in-flight requests<br/>inactiveOnly=false — considers any entry regardless of in-flight, used as last resort"]

    subgraph find["findShardEldestCandidate(scanLimit, inactiveOnly)"]
        FC_CHK{"scanLimit <= 0<br/>or entryCount == 0?"}
        FC_INIT["candidate = null<br/>startShard = shardSweepCursor++"]
        FC_LOOP{"more shards and<br/>scanned < scanLimit?"}
        FC_LOCK["shard.lock.lock()"]
        FC_EMPTY{"shard empty?"}
        FC_UL1["shard.lock.unlock()"]
        FC_SCAN["scanned++<br/>peek eldest entry (LRU order)"]
        FC_INACTIVE{"inactiveOnly AND<br/>hasInFlightRequests()?"}
        FC_UL2["shard.lock.unlock()"]
        FC_CMP{"entry.lastAccessMs < candidate<br/>(or candidate == null)?"}
        FC_UPDATE["update candidate"]
        FC_UL3["shard.lock.unlock()"]

        FC_CHK -->|"no"| FC_INIT --> FC_LOOP
        FC_LOOP -->|"yes"| FC_LOCK --> FC_EMPTY
        FC_EMPTY -->|"yes"| FC_UL1 --> FC_LOOP
        FC_EMPTY -->|"no"| FC_SCAN --> FC_INACTIVE
        FC_INACTIVE -->|"yes — skip shard"| FC_UL2 --> FC_LOOP
        FC_INACTIVE -->|"no"| FC_CMP
        FC_CMP -->|"yes"| FC_UPDATE --> FC_UL3 --> FC_LOOP
        FC_CMP -->|"no"| FC_UL3
    end

    subgraph evict["evictCandidate(candidate) + removeEntry()"]
        EC_CHK{"candidate == null?"}
        EC_FALSE1(["return false"])
        RM_LOCK["shard.lock.lock()"]
        RM_ID{"current entry ==<br/>expected? (reference check)"}
        RM_UL_MM["shard.lock.unlock()"]
        EC_FALSE2(["return false"])
        RM_REMOVE["shard.entries.remove(key)<br/>entryCount--"]
        RM_UL["shard.lock.unlock()"]
        EC_NOTIFY["observer.onForcedEviction(tenantDomain, service)"]
        EC_TRUE(["return true"])

        EC_CHK -->|"yes"| EC_FALSE1
        EC_CHK -->|"no"| RM_LOCK --> RM_ID
        RM_ID -->|"no — entry replaced since scan"| RM_UL_MM --> EC_FALSE2
        RM_ID -->|"yes"| RM_REMOVE --> RM_UL --> EC_NOTIFY --> EC_TRUE
    end

    START_I -->|"inactiveOnly=true"| NOTE
    START_A -->|"inactiveOnly=false"| NOTE
    NOTE --> FC_CHK
    FC_CHK -->|"yes — return null"| EC_CHK
    FC_LOOP -->|"no — return candidate"| EC_CHK
```

> The reference check inside `removeEntry` (`current entry == expected`) guards against a race where the same shard entry was replaced between the scan and the removal — in that case eviction is safely skipped.

---

## Summary: Component Responsibilities

| Component | Role |
|---|---|
| `CircuitBreakerManager` | Singleton entry point; shard routing; eviction; observer dispatch |
| `TenantBreakerEntry` | Per-tenant-service state: CLOSED/OPEN/HALF_OPEN, bulkhead counter, sliding window |
| `SlidingWindow` | Tracks last N outcomes to compute failure rate |
| `StaticPolicy` | Immutable cache/infrastructure settings from `identity.xml` |
| `RuntimePolicy` | Mutable breaker-behavior thresholds; volatile fields for live updates |
| `DefaultPolicyConfigurationLoader` | Reads `identity.xml` once via lazy Holder; produces both policies |
| `RuntimePolicyResolver` | Applies loader → extender chain to produce per-entry policy |
| `RuntimePolicyLoader` | OSGi extension point: override policy per service at entry creation time |
| `RuntimePolicyExtender` | OSGi extension point: global final override after service-level loader |
| `TenantServiceBreakerObserver` | OSGi extension point: react to state transitions, rejections, evictions |
| `TenantKeyUtil` | Builds/parses composite key `"tenantDomain:SERVICE"` |
| `TenantService` | Enum of supported services (`SMS_OTP`, `EMAIL_OTP`) |
