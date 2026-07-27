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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationError;
import org.wso2.carbon.identity.application.authentication.framework.store.SessionDataStore;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for AuthenticationErrorCache.addToCacheOnRead.
 */
@WithCarbonHome
public class AuthenticationErrorCacheTest {

    private static final String ERROR_KEY = "test-error-key";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int TENANT_ID = 42;

    private AuthenticationErrorCacheKey cacheKey;
    private AuthenticationErrorCacheEntry cacheEntry;
    private AuthenticationErrorCacheEntry cacheEntryNullTenant;

    @BeforeMethod
    public void setUp() {

        cacheKey = new AuthenticationErrorCacheKey(ERROR_KEY);
        AuthenticationError mockError = mock(AuthenticationError.class);
        cacheEntry = new AuthenticationErrorCacheEntry(mockError, TENANT_DOMAIN);
        cacheEntryNullTenant = new AuthenticationErrorCacheEntry(mockError, null);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        // Reset the isTemporarySessionDataPersistEnabled flag back to false.
        setFlag(false);
    }

    /**
     * Persistence disabled (default) → storeSessionData must NOT be called.
     */
    @Test
    public void testAddToCacheOnReadPersistDisabled() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            AuthenticationErrorCache.getInstance().addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * Persistence enabled, entry has a valid tenant domain → storeSessionData with resolved tenant ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledWithTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationErrorCache.getInstance().addToCacheOnRead(cacheKey, cacheEntry);

            verify(mockStore).storeSessionData(
                    eq(ERROR_KEY), anyString(), any(AuthenticationErrorCacheEntry.class), eq(TENANT_ID));
        }
    }

    /**
     * Persistence enabled, entry has null tenant domain → storeSessionData with INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationErrorCache.getInstance().addToCacheOnRead(cacheKey, cacheEntryNullTenant);

            verify(mockStore).storeSessionData(
                    eq(ERROR_KEY), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void setFlag(boolean value) throws Exception {

        Field field = AuthenticationErrorCache.class
                .getDeclaredField("isTemporarySessionDataPersistEnabled");
        field.setAccessible(true);
        field.setBoolean(AuthenticationErrorCache.getInstance(), value);
    }
}
