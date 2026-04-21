/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.graalvm.polyglot.proxy.ProxyObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link ProxyReferenceCache}.
 * <p>
 * Covers two responsibilities:
 * <ol>
 *   <li>UUID-keyed host-function / proxy-object reference storage and path-based access</li>
 *   <li>Context-path caching with prefix resume + descendant invalidation</li>
 * </ol>
 * The two key spaces share one map; the invalidation tests explicitly verify that UUID
 * entries are not collaterally removed when a context path is invalidated.
 */
public class ProxyReferenceCacheTest {

    private ProxyReferenceCache cache;

    @BeforeMethod
    public void setUp() {

        cache = new ProxyReferenceCache();
    }

    // ===== host-function refs =====

    @Test
    public void testStoreHostReturnReferenceReturnsUuidLikeId() {

        String id = cache.storeHostReturnReference("payload");

        assertNotNull(id);
        // Verify it parses as a UUID — guards against accidental reversion to sequential IDs
        // which would collide after cache reuse across sessions.
        UUID.fromString(id);
    }

    @Test
    public void testStoreHostReturnReferenceProducesUniqueIds() {

        String a = cache.storeHostReturnReference("first");
        String b = cache.storeHostReturnReference("second");
        assertNotEquals(a, b);
    }

    @Test
    public void testGetHostRefPropertyUnknownIdReturnsNull() {

        assertNull(cache.getHostRefProperty(UUID.randomUUID().toString()));
    }

    @Test
    public void testGetHostRefPropertyNavigatesIntoStoredProxy() {

        Map<String, Object> backing = new HashMap<>();
        backing.put("status", "active");
        ProxyObject stored = ProxyObject.fromMap(backing);
        String id = cache.storeHostReturnReference(stored);

        Object result = cache.getHostRefProperty(id + RemoteEngineConstants.PATH_SEPARATOR + "status");
        assertEquals(result, "active");
    }

    @Test
    public void testSetHostRefPropertyShortPathReturnsFalse() {

        String id = cache.storeHostReturnReference(ProxyObject.fromMap(new HashMap<>()));
        // Path with only the refId — no property segment — is rejected.
        assertFalse(cache.setHostRefProperty(id, "value"));
    }

    @Test
    public void testSetHostRefPropertyUnknownIdReturnsFalse() {

        String bogus = UUID.randomUUID().toString();
        assertFalse(cache.setHostRefProperty(
                bogus + RemoteEngineConstants.PATH_SEPARATOR + "prop", "v"));
    }

    @Test
    public void testSetHostRefPropertyWritesIntoProxy() {

        Map<String, Object> backing = new HashMap<>();
        ProxyObject stored = ProxyObject.fromMap(backing);
        String id = cache.storeHostReturnReference(stored);

        boolean ok = cache.setHostRefProperty(
                id + RemoteEngineConstants.PATH_SEPARATOR + "flag", "on");
        assertTrue(ok);
        assertTrue(backing.containsKey("flag"),
                "setHostRefProperty must reach the underlying ProxyObject backing map");
    }

    // ===== proxy-object refs (UUID path) =====

    @Test
    public void testGetProxyObjectPropertyUnknownIdReturnsNull() {

        assertNull(cache.getProxyObjectProperty(UUID.randomUUID().toString() +
                RemoteEngineConstants.PATH_SEPARATOR + "anything"));
    }

    @Test
    public void testGetProxyObjectPropertyResolvesNestedPath() {

        Map<String, Object> leafBacking = new HashMap<>();
        leafBacking.put("name", "Alice");
        ProxyObject leaf = ProxyObject.fromMap(leafBacking);
        Map<String, Object> rootBacking = new HashMap<>();
        rootBacking.put("user", leaf);
        ProxyObject root = ProxyObject.fromMap(rootBacking);

        String refId = UUID.randomUUID().toString();
        cache.getCache().put(refId, root);

        Object result = cache.getProxyObjectProperty(
                refId + RemoteEngineConstants.PATH_SEPARATOR + "user"
                     + RemoteEngineConstants.PATH_SEPARATOR + "name");
        assertEquals(result, "Alice");
    }

    @Test
    public void testSetProxyObjectPropertyShortPathReturnsFalse() {

        String refId = UUID.randomUUID().toString();
        cache.getCache().put(refId, ProxyObject.fromMap(new HashMap<>()));
        assertFalse(cache.setProxyObjectProperty(refId, "v"));
    }

    @Test
    public void testSetProxyObjectPropertyUnknownIdReturnsFalse() {

        String bogus = UUID.randomUUID().toString();
        assertFalse(cache.setProxyObjectProperty(
                bogus + RemoteEngineConstants.PATH_SEPARATOR + "p", "v"));
    }

    // ===== context-path caching =====

    @Test
    public void testGetContextPathPropertyNullPathReturnsRoot() {

        Object root = new Object();
        assertSame(cache.getContextPathProperty(null, root), root);
    }

    @Test
    public void testGetContextPathPropertyEmptyPathReturnsRoot() {

        Object root = new Object();
        assertSame(cache.getContextPathProperty("", root), root);
    }

    @Test
    public void testGetContextPathPropertyFullPathCacheHitSkipsNavigation() {

        // Pre-seed a cache entry keyed by the exact full path. Root is unused because the
        // full-path branch returns directly without navigating.
        ProxyObject cached = ProxyObject.fromMap(new HashMap<>());
        cache.getCache().put("user::details", cached);

        assertSame(cache.getContextPathProperty("user::details", new Object()), cached);
    }

    @Test
    public void testGetContextPathPropertyCachesNavigableIntermediate() {

        ProxyObject leaf = ProxyObject.fromMap(new HashMap<>());
        Map<String, Object> rootBacking = new HashMap<>();
        rootBacking.put("user", leaf);
        ProxyObject root = ProxyObject.fromMap(rootBacking);

        Object first = cache.getContextPathProperty("user", root);
        // The internal cache now holds "user" -> leaf; a second call must return the same
        // cached instance (fast path) rather than re-walking the proxy.
        Object second = cache.getContextPathProperty("user", root);
        assertSame(second, first);
        assertTrue(cache.getCache().containsKey("user"));
    }

    @Test
    public void testGetContextPathPropertyDoesNotCacheLeafPrimitive() {

        // A leaf String is not a Proxy — path must not be cached.
        Map<String, Object> rootBacking = new HashMap<>();
        rootBacking.put("name", "Alice");
        ProxyObject root = ProxyObject.fromMap(rootBacking);

        Object value = cache.getContextPathProperty("name", root);
        assertEquals(value, "Alice");
        assertFalse(cache.getCache().containsKey("name"),
                "Primitive leaves must not be cached — cached live proxies only");
    }

    @Test
    public void testGetContextPathPropertyDoesNotCacheKeysTerminal() {

        // The __keys__ terminal is a snapshot of member keys — caching would return stale
        // keys after subsequent putMember writes.
        Map<String, Object> backing = new HashMap<>();
        backing.put("a", 1);
        ProxyObject root = ProxyObject.fromMap(backing);

        String path = RemoteEngineConstants.KEYS_PROPERTY;
        Object keys = cache.getContextPathProperty(path, root);
        assertNotNull(keys);
        assertFalse(cache.getCache().containsKey(path),
                "__keys__ terminal must not be cached");
    }

    @Test
    public void testGetContextPathPropertyResumesFromLongestPrefix() {

        // Seed an intermediate proxy at "user::profile" so the walk skips the first two
        // segments of "user::profile::email".
        ProxyObject profileLeaf = ProxyObject.fromMap(new HashMap<>());
        Map<String, Object> profileBacking = new HashMap<>();
        profileBacking.put("email", "alice@example.com");
        ProxyObject profile = ProxyObject.fromMap(profileBacking);
        cache.getCache().put("user::profile", profile);

        // Root unused here because the walk starts from the cached "user::profile" entry.
        Object result = cache.getContextPathProperty(
                "user::profile::email", /* root never consulted */ profileLeaf);

        assertEquals(result, "alice@example.com");
    }

    // ===== invalidation =====

    @Test
    public void testInvalidateContextPathRemovesExactAndDescendants() {

        ProxyObject dummy = ProxyObject.fromMap(new HashMap<>());
        cache.getCache().put("user", dummy);
        cache.getCache().put("user::profile", dummy);
        cache.getCache().put("user::profile::email", dummy);
        cache.getCache().put("unrelated", dummy);

        cache.invalidateContextPath("user::profile");

        assertTrue(cache.getCache().containsKey("user"),
                "Ancestor must remain — it's still a live proxy reference");
        assertFalse(cache.getCache().containsKey("user::profile"),
                "Exact invalidated path must be gone");
        assertFalse(cache.getCache().containsKey("user::profile::email"),
                "Descendants of the invalidated path must be gone");
        assertTrue(cache.getCache().containsKey("unrelated"),
                "Unrelated entries must not be touched");
    }

    @Test
    public void testInvalidateContextPathDoesNotRemoveUuidEntries() {

        // UUID entries must survive context-path invalidation — they share the same map
        // but contain no "::" so the descendant prefix cannot match.
        ProxyObject dummy = ProxyObject.fromMap(new HashMap<>());
        String uuidKey = UUID.randomUUID().toString();
        cache.getCache().put(uuidKey, dummy);
        cache.getCache().put("user", dummy);

        cache.invalidateContextPath("user");

        assertTrue(cache.getCache().containsKey(uuidKey),
                "UUID-keyed entries must not collide with context-path invalidation");
        assertFalse(cache.getCache().containsKey("user"));
    }

    @Test
    public void testInvalidateContextPathNullOrEmptyIsNoOp() {

        ProxyObject dummy = ProxyObject.fromMap(new HashMap<>());
        cache.getCache().put("user", dummy);

        cache.invalidateContextPath(null);
        cache.invalidateContextPath("");

        assertTrue(cache.getCache().containsKey("user"),
                "Null / empty invalidation must not mutate the cache");
    }

    @Test
    public void testGetCacheReturnsLiveBackingMap() {

        // getCache exposes the live map so the transport layer can set a ThreadLocal to it.
        // Mutations via the returned reference must affect subsequent lookups.
        Map<String, Object> live = cache.getCache();
        live.put("injected", "v");
        assertTrue(cache.getCache().containsKey("injected"));
    }

    // ========================================================================
    // Edge cases — concurrency & real session shapes.
    // ========================================================================

    @Test
    public void testConcurrentStoreHostReturnReferenceProducesUniqueIds() throws Exception {

        // Production: multiple gRPC callback threads on the same session may call host
        // functions that store return refs. The backing map is a ConcurrentHashMap, but
        // the UUID generator must also produce unique keys under race — this smoke test
        // would catch a regression to a sequential-id generator with weak synchronization.
        int threadCount = 16;
        int perThread = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<List<String>>> futures = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                go.await();
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < perThread; i++) {
                    ids.add(cache.storeHostReturnReference("payload-" + i));
                }
                return ids;
            }));
        }
        ready.await(5, TimeUnit.SECONDS);
        go.countDown();

        Set<String> allIds = new HashSet<>();
        for (Future<List<String>> f : futures) {
            allIds.addAll(f.get(10, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertEquals(allIds.size(), threadCount * perThread,
                "Concurrent storeHostReturnReference must yield unique UUIDs across all threads");
    }

    @Test
    public void testContextPathFullPathPutIfAbsentFirstWins() {

        // Race: two readers resolve the same context path concurrently. putIfAbsent means
        // the first caller's resolved proxy wins; subsequent callers get that same instance.
        // This test seeds the race result deterministically and verifies the visible value.
        ProxyObject firstInstance = ProxyObject.fromMap(new HashMap<>());
        ProxyObject wouldBeSecond = ProxyObject.fromMap(new HashMap<>());

        Map<String, Object> rootBacking = new HashMap<>();
        rootBacking.put("p", firstInstance);
        ProxyObject root = ProxyObject.fromMap(rootBacking);

        // First resolve — caches firstInstance under "p".
        Object first = cache.getContextPathProperty("p", root);
        assertSame(first, firstInstance);

        // Simulate a second caller whose navigation would have produced a different instance.
        // Even if we switched the backing proxy, the already-cached "p" wins.
        rootBacking.put("p", wouldBeSecond);
        Object second = cache.getContextPathProperty("p", root);
        assertSame(second, firstInstance,
                "putIfAbsent semantics: the first resolved proxy remains — cache does not re-resolve");
    }

    @Test
    public void testContextPathLongestPrefixWinsWhenMultipleAncestorsCached() {

        // Seed two ancestors — the navigator must resume from the LONGEST matching prefix,
        // not the shortest. Otherwise cache hits degrade into full re-walks.
        ProxyObject profileLeaf = ProxyObject.fromMap(mapOf("email", "a@b.com"));
        ProxyObject userLeaf = ProxyObject.fromMap(mapOf("profile", profileLeaf));
        // Use sentinel proxies so we can tell which ancestor was used to resume.
        ProxyObject shortPrefix = ProxyObject.fromMap(mapOf("profile",
                ProxyObject.fromMap(mapOf("email", "WRONG-SHORT-PREFIX"))));

        cache.getCache().put("user", shortPrefix);                  // length-1 prefix
        cache.getCache().put("user::profile", profileLeaf);         // length-2 prefix

        Object result = cache.getContextPathProperty(
                "user::profile::email", /* root never consulted */ userLeaf);

        assertEquals(result, "a@b.com",
                "Resume must start at the longest cached prefix, not the shortest");
    }

    @Test
    public void testContextPathInvalidationOnSiblingDoesNotAffectOthers() {

        // Writing to steps[1].subject should not invalidate steps[2].subject.
        ProxyObject p = ProxyObject.fromMap(new HashMap<>());
        cache.getCache().put("steps::1::subject", p);
        cache.getCache().put("steps::2::subject", p);
        cache.getCache().put("steps::1::subject::email", p);

        cache.invalidateContextPath("steps::1::subject");

        assertFalse(cache.getCache().containsKey("steps::1::subject"));
        assertFalse(cache.getCache().containsKey("steps::1::subject::email"));
        assertTrue(cache.getCache().containsKey("steps::2::subject"),
                "Sibling step entries must survive — invalidation is scoped to the written path's subtree");
    }

    @Test
    public void testContextPathInvalidationOnParentRemovesAllDescendants() {

        // Writing to "user" should blow away "user::profile", "user::profile::email" etc.
        ProxyObject p = ProxyObject.fromMap(new HashMap<>());
        cache.getCache().put("user", p);
        cache.getCache().put("user::profile", p);
        cache.getCache().put("user::profile::email", p);
        cache.getCache().put("user::profile::phone", p);

        cache.invalidateContextPath("user");

        assertFalse(cache.getCache().containsKey("user"));
        assertFalse(cache.getCache().containsKey("user::profile"));
        assertFalse(cache.getCache().containsKey("user::profile::email"));
        assertFalse(cache.getCache().containsKey("user::profile::phone"));
    }

    @Test
    public void testConcurrentContextPathGetAndInvalidateDoesNotCorruptCache() throws Exception {

        // Readers and writers interleave on the same proxy session. No assertion about
        // consistency at a given moment — only that no exception propagates and the cache
        // remains a navigable structure afterwards. ConcurrentHashMap provides the guarantees;
        // this test codifies the contract.
        ProxyObject leaf = ProxyObject.fromMap(mapOf("email", "a@b.com"));
        ProxyObject profile = ProxyObject.fromMap(mapOf("email", leaf));
        ProxyObject user = ProxyObject.fromMap(mapOf("profile", profile));
        ProxyObject root = ProxyObject.fromMap(mapOf("user", user));

        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch stop = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        // 2 readers
        for (int i = 0; i < 2; i++) {
            futures.add(pool.submit(() -> {
                while (stop.getCount() > 0) {
                    try {
                        cache.getContextPathProperty("user::profile", root);
                        cache.getContextPathProperty("user::profile::email", root);
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
                return null;
            }));
        }
        // 2 writers
        for (int i = 0; i < 2; i++) {
            futures.add(pool.submit(() -> {
                while (stop.getCount() > 0) {
                    try {
                        cache.invalidateContextPath("user::profile");
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                }
                return null;
            }));
        }

        Thread.sleep(150);
        stop.countDown();
        for (Future<?> f : futures) {
            f.get(5, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertEquals(errors.get(), 0,
                "Concurrent get/invalidate on the context cache must not throw");
    }

    @Test
    public void testProxyObjectPropertyNavigatesThroughDeeplyNestedProxy() {

        // Host function returned a nested user object, e.g. getUsersWithClaimValues → each
        // user proxy wraps `claims`, which wraps `attributes`, etc. Path navigation must
        // reach the leaf without trapping on the intermediate proxies.
        Map<String, Object> claimsBacking = new HashMap<>();
        claimsBacking.put("email", "alice@example.com");
        Map<String, Object> userBacking = new HashMap<>();
        userBacking.put("claims", ProxyObject.fromMap(claimsBacking));
        ProxyObject user = ProxyObject.fromMap(userBacking);

        String refId = UUID.randomUUID().toString();
        cache.getCache().put(refId, user);

        Object value = cache.getProxyObjectProperty(
                refId + "::claims::email");
        assertEquals(value, "alice@example.com");
    }

    @Test
    public void testSetProxyObjectPropertyWritesThrough() {

        // Host-ref write path — symmetric with the read path used by the sidecar when a
        // host function returns a mutable wrapper.
        Map<String, Object> backing = new HashMap<>();
        ProxyObject stored = ProxyObject.fromMap(backing);

        String refId = UUID.randomUUID().toString();
        cache.getCache().put(refId, stored);

        boolean ok = cache.setProxyObjectProperty(
                refId + RemoteEngineConstants.PATH_SEPARATOR + "status", "ok");
        assertTrue(ok);
        assertTrue(backing.containsKey("status"),
                "setProxyObjectProperty must reach the underlying ProxyObject backing map");
    }

    // ===== helper =====

    private static Map<String, Object> mapOf(String k, Object v) {

        Map<String, Object> m = new HashMap<>();
        m.put(k, v);
        return m;
    }
}
