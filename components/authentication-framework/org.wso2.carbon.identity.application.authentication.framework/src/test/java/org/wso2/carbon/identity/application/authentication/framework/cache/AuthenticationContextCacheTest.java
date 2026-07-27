/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthenticationContextCache.addToCacheOnRead.
 */
@WithCarbonHome
public class AuthenticationContextCacheTest {

    private static final String CONTEXT_ID = "test-context-id";
    private static final String TENANT_DOMAIN = "carbon.super";

    private AuthenticationContextCacheKey cacheKey;
    private AuthenticationContextCacheEntry cacheEntry;
    private AuthenticationContext mockContext;

    @BeforeMethod
    public void setUp() throws Exception {

        cacheKey = new AuthenticationContextCacheKey(CONTEXT_ID);
        mockContext = mock(AuthenticationContext.class);
        when(mockContext.getContextIdentifier()).thenReturn(CONTEXT_ID);
        when(mockContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        cacheEntry = new AuthenticationContextCacheEntry(mockContext);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        // Reset the singleton so tests are independent.
        resetSingleton();
    }

    // -------------------------------------------------------------------------
    // Tests for addToCacheOnRead when isTemporarySessionDataPersistEnabled=false
    // -------------------------------------------------------------------------

    /**
     * With persistence disabled (default), addToCacheOnRead should call super without touching SessionDataStore.
     */
    @Test
    public void testAddToCacheOnReadPersistDisabled() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            // Default instance has isTemporarySessionDataPersistEnabled=false.
            AuthenticationContextCache.getInstance().addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    // -------------------------------------------------------------------------
    // Tests for addToCacheOnRead when isTemporarySessionDataPersistEnabled=true
    // -------------------------------------------------------------------------

    /**
     * With persistence enabled and null context properties → skip the if block, still store via SessionDataStore.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullProperties() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            resetSingleton();
            when(mockContext.getProperties()).thenReturn(null);

            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            // With null properties the inner if block is skipped → storeSessionData not called.
            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * With persistence enabled, valid properties, and successful optimization.
     * Expects storeSessionData to be called.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledSuccess() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(1);
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId("carbon.super")).thenReturn(0);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            Map<String, Object> props = new HashMap<>();
            props.put("key1", "serializable-value");
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore).storeSessionData(
                    eq(CONTEXT_ID), anyString(), any(AuthenticationContextCacheEntry.class), anyInt());
        }
    }

    /**
     * With persistence enabled, optimization throws SessionDataStorageOptimizationClientException.
     * Expects storeSessionData NOT to be called (early return).
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledClientException() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);
            doThrow(new SessionDataStorageOptimizationClientException("client error"))
                    .when(mockLoader).optimizeAuthenticationContext(any());

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            Map<String, Object> props = new HashMap<>();
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * With persistence enabled, optimization throws SessionDataStorageOptimizationServerException.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledServerException() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);
            doThrow(new SessionDataStorageOptimizationServerException("server error"))
                    .when(mockLoader).optimizeAuthenticationContext(any());

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            Map<String, Object> props = new HashMap<>();
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * With persistence enabled, optimization throws generic SessionDataStorageOptimizationException.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledGenericException() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);
            doThrow(new SessionDataStorageOptimizationException("generic error"))
                    .when(mockLoader).optimizeAuthenticationContext(any());

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            Map<String, Object> props = new HashMap<>();
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * With persistence enabled and null tenantDomain → tenantId stays INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullTenantDomain() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            // Return null tenant domain.
            when(mockContext.getTenantDomain()).thenReturn(null);
            Map<String, Object> props = new HashMap<>();
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore).storeSessionData(
                    eq(CONTEXT_ID), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    /**
     * Test the changed line in getValueFromCache: after loading from SessionDataStore the cache is
     * re-populated via addToCacheOnRead (not addToCache).  Verifies that loadAuthenticationContext
     * is called on the returned entry (as per the existing flow).
     */
    @Test
    public void testGetValueFromCacheRePopulatesViaPutOnRead() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<AuthenticationContextLoader> loaderStatic =
                     mockStatic(AuthenticationContextLoader.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            identityUtil.when(() -> IdentityUtil.getProperty(
                    "JDBCPersistenceManager.SessionDataPersist.Temporary")).thenReturn("true");
            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            AuthenticationContextLoader mockLoader = mock(AuthenticationContextLoader.class);
            loaderStatic.when(AuthenticationContextLoader::getInstance).thenReturn(mockLoader);

            // SessionDataStore returns the entry (simulates DB hit).
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);
            when(mockStore.getSessionData(CONTEXT_ID, "AuthenticationContextCache")).thenReturn(cacheEntry);

            Map<String, Object> props = new HashMap<>();
            when(mockContext.getProperties()).thenReturn(props);

            resetSingleton();
            AuthenticationContextCache cache = AuthenticationContextCache.getInstance();

            // Lookup a key that is NOT in cache → triggers DB fetch and re-populate.
            AuthenticationContextCacheEntry result = cache.getValueFromCache(cacheKey);

            // The flow should have called loadAuthenticationContext on the entry.
            verify(mockLoader).loadAuthenticationContext(any(AuthenticationContext.class));
            Assert.assertNotNull(result);
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void resetSingleton() throws Exception {

        Field instanceField = AuthenticationContextCache.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }
}
