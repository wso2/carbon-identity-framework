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

package org.wso2.carbon.identity.flow.execution.engine.poll;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCache;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCacheEntry;
import org.wso2.carbon.identity.flow.execution.engine.cache.FlowPollingCacheKey;
import org.wso2.carbon.identity.flow.execution.engine.dao.FlowPollingDAO;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.POLL_VALIDITY_SECONDS;

/**
 * Unit tests for FlowPollingService, with an in memory polling DAO and polling cache.
 */
public class FlowPollingServiceTest {

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String FLOW_ID = "flow-id";

    private InMemoryStore store;
    private Map<String, FlowPollingCacheEntry> cacheEntries;
    private FlowPollingCache cache;
    private FlowPollingService pollingService;
    private MockedStatic<FlowExecutionEngineUtils> engineUtils;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        store = new InMemoryStore();
        cacheEntries = new HashMap<>();
        cache = mock(FlowPollingCache.class);
        doAnswer(invocation -> cacheEntries.get(((FlowPollingCacheKey) invocation.getArgument(0)).getEntryId()))
                .when(cache).getValueFromCache(any());
        doAnswer(invocation -> cacheEntries.putIfAbsent(((FlowPollingCacheKey) invocation.getArgument(0))
                .getEntryId(), invocation.getArgument(1))).when(cache).addToCacheOnRead(any(), any());
        doAnswer(invocation -> cacheEntries.remove(((FlowPollingCacheKey) invocation.getArgument(0)).getEntryId()))
                .when(cache).clearCacheEntry(any());
        pollingService = new FlowPollingService(store, () -> cache);
        engineUtils = mockStatic(FlowExecutionEngineUtils.class);
        engineUtils.when(FlowExecutionEngineUtils::resolveTenantDomain).thenReturn(TENANT_DOMAIN);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);
    }

    @AfterMethod
    public void tearDown() {

        engineUtils.close();
        identityTenantUtil.close();
    }

    @Test
    public void testCreateStoresOnlyTheHashOfTheToken() throws Exception {

        String token = pollingService.create(FLOW_ID);

        assertEquals(store.entries.size(), 1);
        String entryId = store.entries.keySet().iterator().next();
        assertTrue(entryId.startsWith("poll_"));
        assertFalse(entryId.contains(token));
        assertEquals(store.entries.get(entryId).getFlowId(), FLOW_ID);
        assertTrue(store.entries.get(entryId).getExpiresAt() > System.currentTimeMillis());
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        // The cache is keyed by the same hash, never by the token.
        assertEquals(cacheEntries.keySet().iterator().next(), entryId);
    }

    @Test
    public void testCreateCachesTheWaitingEntry() throws Exception {

        long before = System.currentTimeMillis();
        String token = pollingService.create(FLOW_ID);

        FlowPollingCacheEntry cached = cacheEntries.get(entryIdOf(token));
        assertNotNull(cached);
        assertEquals(cached.getStatus(), FlowPollingService.Status.WAITING.name());
        long validity = POLL_VALIDITY_SECONDS * 1000L;
        assertTrue(cached.getExpiresAt() >= before + validity);
        assertTrue(cached.getExpiresAt() <= System.currentTimeMillis() + validity);
    }

    @Test
    public void testTokensAreUnique() throws Exception {

        assertNotEquals(pollingService.create(FLOW_ID), pollingService.create(FLOW_ID));
        assertEquals(store.entries.size(), 2);
    }

    @Test
    public void testCacheHitDoesNotReadTheStore() throws Exception {

        String token = pollingService.create(FLOW_ID);
        for (int i = 0; i < 5; i++) {
            assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        }
        assertEquals(store.reads, 0);
    }

    @Test
    public void testCacheMissReadsTheStoreAndPopulatesTheCache() throws Exception {

        String token = pollingService.create(FLOW_ID);
        // As on another node, which has not cached the entry.
        cacheEntries.clear();

        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        int readsOfMiss = store.reads;
        assertTrue(readsOfMiss > 0);
        assertEquals(cacheEntries.get(entryIdOf(token)).getStatus(), FlowPollingService.Status.WAITING.name());

        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        assertEquals(store.reads, readsOfMiss);
    }

    @Test
    public void testMissesAreNotCached() throws Exception {

        pollingService.create(FLOW_ID);
        cacheEntries.clear();
        clearInvocations(cache);

        assertEquals(pollingService.getStatus("unknown"), FlowPollingService.Status.FAILED);
        assertEquals(pollingService.getStatus("unknown"), FlowPollingService.Status.FAILED);
        assertTrue(cacheEntries.isEmpty());
        assertEquals(store.reads, 2);
        verify(cache, never()).addToCacheOnRead(any(), any());
    }

    @Test
    public void testEntryWithoutExpiryIsNotCached() throws Exception {

        String token = pollingService.create(FLOW_ID);
        cacheEntries.clear();
        store.entries.put(entryIdOf(token), new FlowPollingEntry(FLOW_ID, FlowPollingService.Status.WAITING.name(), 0));

        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        assertTrue(cacheEntries.isEmpty());
    }

    @Test
    public void testMarkSuccess() throws Exception {

        String token = pollingService.create(FLOW_ID);

        assertTrue(pollingService.markSuccess(token));
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.SUCCESS);
        assertEquals(store.inserts, 1);
    }

    @Test
    public void testMarkSuccessOfBlankOrUnknownTokenIsNotApplied() throws Exception {

        pollingService.create(FLOW_ID);

        assertFalse(pollingService.markSuccess(null));
        assertFalse(pollingService.markSuccess(""));
        assertFalse(pollingService.markSuccess("unknown"));
        assertEquals(store.writes, 1);
    }

    @Test
    public void testMarkSuccessClearsTheCacheAndTheNextReadSeesSuccess() throws Exception {

        String token = pollingService.create(FLOW_ID);
        String entryId = entryIdOf(token);
        long expiresAt = cacheEntries.get(entryId).getExpiresAt();
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);

        pollingService.markSuccess(token);

        assertNull(cacheEntries.get(entryId));
        verify(cache, never()).addToCache(any(), any(), any(String.class));
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.SUCCESS);
        FlowPollingCacheEntry cached = cacheEntries.get(entryId);
        assertEquals(cached.getStatus(), FlowPollingService.Status.SUCCESS.name());
        // The expiry time is kept when the entry is marked as successful.
        assertEquals(cached.getExpiresAt(), expiresAt);
    }

    @Test
    public void testUnknownOrBlankTokenIsFailed() throws Exception {

        pollingService.create(FLOW_ID);
        assertEquals(pollingService.getStatus("unknown"), FlowPollingService.Status.FAILED);
        assertEquals(pollingService.getStatus(""), FlowPollingService.Status.FAILED);
        assertEquals(pollingService.getStatus(null), FlowPollingService.Status.FAILED);
    }

    @Test
    public void testDeleteClearsTheCache() throws Exception {

        String token = pollingService.create(FLOW_ID);
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.WAITING);
        pollingService.delete(token);

        assertTrue(cacheEntries.isEmpty());
        assertTrue(store.entries.isEmpty());
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.FAILED);
        assertTrue(cacheEntries.isEmpty());
    }

    @Test
    public void testExpiredCachedEntryIsFailed() throws Exception {

        String token = pollingService.create(FLOW_ID);
        String entryId = entryIdOf(token);
        // The cache still has the entry, for example when its timeout exceeds the validity.
        cacheEntries.put(entryId, new FlowPollingCacheEntry(FlowPollingService.Status.WAITING.name(),
                System.currentTimeMillis() - 1));

        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.FAILED);
        assertNull(cacheEntries.get(entryId));
        assertEquals(store.reads, 0);
    }

    @Test
    public void testMarkSuccessAfterDeleteCreatesNothing() throws Exception {

        String token = pollingService.create(FLOW_ID);
        pollingService.delete(token);

        assertFalse(pollingService.markSuccess(token));
        assertTrue(store.entries.isEmpty());
        assertTrue(cacheEntries.isEmpty());
        assertEquals(store.inserts, 1);
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.FAILED);
        assertTrue(cacheEntries.isEmpty());
    }

    @Test
    public void testMarkSuccessWhenEntryIsDeletedWhileMarking() throws Exception {

        String token = pollingService.create(FLOW_ID);
        // The entry is read, then deleted by the failing flow before it is updated.
        store.deleteBeforeNextUpdate = true;

        assertFalse(pollingService.markSuccess(token));

        assertTrue(store.entries.isEmpty());
        assertTrue(cacheEntries.isEmpty());
        assertEquals(store.inserts, 1);
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.FAILED);
    }

    @Test
    public void testMarkSuccessBetweenReadAndCachingIsNotHidden() throws Exception {

        String token = pollingService.create(FLOW_ID);
        cacheEntries.clear();
        // The flow marks success between the poll's read of WAITING and its caching.
        store.afterNextGet = () -> {
            try {
                pollingService.markSuccess(token);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        FlowPollingService.Status status = pollingService.getStatus(token);

        assertEquals(status, FlowPollingService.Status.SUCCESS);
        assertTrue(cacheEntries.isEmpty());
        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.SUCCESS);
    }

    @Test
    public void testDeleteBetweenReadAndCachingIsNotHidden() throws Exception {

        String token = pollingService.create(FLOW_ID);
        cacheEntries.clear();
        store.afterNextGet = () -> {
            try {
                pollingService.delete(token);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        assertEquals(pollingService.getStatus(token), FlowPollingService.Status.FAILED);
        assertTrue(cacheEntries.isEmpty());
    }

    @Test
    public void testPollingDoesNotWrite() throws Exception {

        String token = pollingService.create(FLOW_ID);
        int writes = store.writes;
        for (int i = 0; i < 5; i++) {
            pollingService.getStatus(token);
        }
        cacheEntries.clear();
        for (int i = 0; i < 5; i++) {
            pollingService.getStatus(token);
        }
        assertEquals(store.writes, writes);
    }

    private String entryIdOf(String token) throws Exception {

        byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        return "poll_" + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    /**
     * Polling DAO that keeps entries in memory.
     */
    private static class InMemoryStore implements FlowPollingDAO {

        private final Map<String, FlowPollingEntry> entries = new HashMap<>();
        private int inserts;
        private int writes;
        private boolean deleteBeforeNextUpdate;
        private int reads;
        private Runnable afterNextGet;

        @Override
        public void addPollingEntry(String entryId, int tenantId, FlowPollingEntry entry) {

            writes++;
            inserts++;
            entries.put(entryId, entry);
        }

        @Override
        public FlowPollingEntry getPollingEntry(String entryId, int tenantId) {

            reads++;
            FlowPollingEntry entry = entries.get(entryId);
            if (afterNextGet != null) {
                Runnable hook = afterNextGet;
                afterNextGet = null;
                hook.run();
            }
            return entry;
        }

        @Override
        public boolean updatePollingEntry(String entryId, int tenantId, FlowPollingEntry entry) {

            writes++;
            if (deleteBeforeNextUpdate) {
                deleteBeforeNextUpdate = false;
                entries.remove(entryId);
            }
            if (!entries.containsKey(entryId)) {
                return false;
            }
            entries.put(entryId, entry);
            return true;
        }

        @Override
        public void deletePollingEntry(String entryId, int tenantId) {

            writes++;
            entries.remove(entryId);
        }
    }
}
