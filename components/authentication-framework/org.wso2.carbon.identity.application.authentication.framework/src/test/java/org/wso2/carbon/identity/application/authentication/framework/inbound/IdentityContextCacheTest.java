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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;
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
 * Unit tests for IdentityContextCache.addToCacheOnRead.
 */
@WithCarbonHome
public class IdentityContextCacheTest {

    private static final String CACHE_KEY = "test-inbound-key";
    private static final String TENANT_DOMAIN = "wso2.com";
    private static final int TENANT_ID = 9;

    private IdentityMessageContext<?, ?> contextWithTenant;
    private IdentityMessageContext<?, ?> contextNullTenant;

    @BeforeMethod
    public void setUp() throws Exception {

        IdentityRequest mockRequestWithTenant = mock(IdentityRequest.class);
        when(mockRequestWithTenant.getTenantDomain()).thenReturn(TENANT_DOMAIN);
        contextWithTenant = mock(IdentityMessageContext.class);
        when(contextWithTenant.getRequest()).thenReturn((IdentityRequest) mockRequestWithTenant);

        IdentityRequest mockRequestNullTenant = mock(IdentityRequest.class);
        when(mockRequestNullTenant.getTenantDomain()).thenReturn(null);
        contextNullTenant = mock(IdentityMessageContext.class);
        when(contextNullTenant.getRequest()).thenReturn((IdentityRequest) mockRequestNullTenant);

        resetSingleton();
    }

    @AfterMethod
    public void tearDown() throws Exception {

        setFlag(false);
        resetSingleton();
    }

    /**
     * Request-scope cache disabled (default) → storeSessionData must NOT be called.
     */
    @Test
    public void testAddToCacheOnReadCacheDisabled() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            IdentityContextCache.getInstance().addToCacheOnRead(CACHE_KEY, contextWithTenant);

            verify(mockStore, never()).storeSessionData(anyString(), anyString(), any(), anyInt());
        }
    }

    /**
     * Request-scope cache enabled, valid tenant domain → storeSessionData with resolved tenant ID.
     */
    @Test
    public void testAddToCacheOnReadCacheEnabledWithTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);
            tenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            IdentityContextCache.getInstance().addToCacheOnRead(CACHE_KEY, contextWithTenant);

            verify(mockStore).storeSessionData(
                    eq(CACHE_KEY), anyString(), any(IdentityMessageContext.class), eq(TENANT_ID));
        }
    }

    /**
     * Request-scope cache enabled, null tenant domain → storeSessionData with INVALID_TENANT_ID.
     */
    @Test
    public void testAddToCacheOnReadCacheEnabledNullTenantDomain() throws Exception {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class);
             MockedStatic<SessionDataStore> storeStatic = mockStatic(SessionDataStore.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            SessionDataStore mockStore = mock(SessionDataStore.class);
            storeStatic.when(SessionDataStore::getInstance).thenReturn(mockStore);

            setFlag(true);
            IdentityContextCache.getInstance().addToCacheOnRead(CACHE_KEY, contextNullTenant);

            verify(mockStore).storeSessionData(
                    eq(CACHE_KEY), anyString(), any(),
                    eq(MultitenantConstants.INVALID_TENANT_ID));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void setFlag(boolean value) throws Exception {

        Field field = IdentityContextCache.class.getDeclaredField("enableRequestScopeCache");
        field.setAccessible(true);
        field.setBoolean(IdentityContextCache.getInstance(), value);
    }

    private void resetSingleton() throws Exception {

        Field field = IdentityContextCache.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }
}
