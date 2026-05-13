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
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthenticationResultCache.addToCacheOnRead.
 */
@WithCarbonHome
public class AuthenticationResultCacheTest {

    private static final String RESULT_ID = "test-result-id";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int TENANT_ID = 5;

    private AuthenticationResultCacheKey cacheKey;
    private AuthenticationResultCacheEntry entryWithSubject;
    private AuthenticationResultCacheEntry entryNullResult;
    private AuthenticationResultCacheEntry entryNullSubject;
    private AuthenticationResultCacheEntry entryNullTenantDomain;

    @BeforeMethod
    public void setUp() throws Exception {

        cacheKey = new AuthenticationResultCacheKey(RESULT_ID);

        // Entry with a valid subject and tenant domain.
        AuthenticatedUser mockUser = mock(AuthenticatedUser.class);
        when(mockUser.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        AuthenticationResult resultWithSubject = mock(AuthenticationResult.class);
        when(resultWithSubject.getSubject()).thenReturn(mockUser);
        entryWithSubject = new AuthenticationResultCacheEntry();
        entryWithSubject.setResult(resultWithSubject);

        // Entry with null AuthenticationResult.
        entryNullResult = new AuthenticationResultCacheEntry();
        entryNullResult.setResult(null);

        // Entry with non-null result but null subject.
        AuthenticationResult resultNullSubject = mock(AuthenticationResult.class);
        when(resultNullSubject.getSubject()).thenReturn(null);
        entryNullSubject = new AuthenticationResultCacheEntry();
        entryNullSubject.setResult(resultNullSubject);

        // Entry with subject but null tenant domain.
        AuthenticatedUser userNullTenant = mock(AuthenticatedUser.class);
        when(userNullTenant.getTenantDomain()).thenReturn(null);
        AuthenticationResult resultNullTenant = mock(AuthenticationResult.class);
        when(resultNullTenant.getSubject()).thenReturn(userNullTenant);
        entryNullTenantDomain = new AuthenticationResultCacheEntry();
        entryNullTenantDomain.setResult(resultNullTenant);

        resetSingleton();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        setFlag(false);
        resetSingleton();
    }

    /**
     * Persistence disabled → storeSessionData must NOT be called.
     */
    @Test
    public void testAddToCacheOnReadPersistDisabled() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            AuthenticationResultCache.getInstance().addToCacheOnRead(cacheKey, entryWithSubject);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * Persistence enabled, valid subject with tenant domain → storeSessionData with resolved tenant ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledWithSubjectTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationResultCache.getInstance().addToCacheOnRead(cacheKey, entryWithSubject);

            verify(mockStore).storeSessionData(
                    eq(RESULT_ID), anyString(), any(AuthenticationResultCacheEntry.class), eq(TENANT_ID));
        }
    }

    /**
     * Persistence enabled, null AuthenticationResult → storeSessionData with INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullResult() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationResultCache.getInstance().addToCacheOnRead(cacheKey, entryNullResult);

            verify(mockStore).storeSessionData(
                    eq(RESULT_ID), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    /**
     * Persistence enabled, null subject → storeSessionData with INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullSubject() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationResultCache.getInstance().addToCacheOnRead(cacheKey, entryNullSubject);

            verify(mockStore).storeSessionData(
                    eq(RESULT_ID), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    /**
     * Persistence enabled, subject has null tenant domain → storeSessionData with INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadPersistEnabledNullTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            AuthenticationResultCache.getInstance().addToCacheOnRead(cacheKey, entryNullTenantDomain);

            verify(mockStore).storeSessionData(
                    eq(RESULT_ID), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void setFlag(boolean value) throws Exception {

        Field field = AuthenticationResultCache.class
                .getDeclaredField("isTemporarySessionDataPersistEnabled");
        field.setAccessible(true);
        field.setBoolean(AuthenticationResultCache.getInstance(), value);
    }

    private void resetSingleton() throws Exception {

        Field field = AuthenticationResultCache.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }
}
