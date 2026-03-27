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

package org.wso2.carbon.identity.mgt.store;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.dto.UserIdentityClaimsDO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfiguration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserStoreBasedIdentityDataStore#storeOnRead}.
 */
public class UserStoreBasedIdentityDataStoreTest {

    private static final String DOMAIN_NAME = "PRIMARY";
    private static final int TENANT_ID = -1234;
    private static final String USER_NAME = "testUser";

    private Cache<String, UserIdentityClaimsDO> mockCache;
    private UserStoreBasedIdentityDataStore store;
    private UserStoreManager userStoreManager;
    private RealmConfiguration realmConfiguration;
    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        mockCache = mock(Cache.class);

        // Override getCache() to avoid OSGI/CachingFactory dependency.
        store = new UserStoreBasedIdentityDataStore() {
            @Override
            protected Cache<String, UserIdentityClaimsDO> getCache() {
                return mockCache;
            }

            @Override
            protected void setUserClaimsValuesInUserStore(org.wso2.carbon.user.api.UserStoreManager userStoreManager,
                    String username, Map<String, String> claims, String profile) throws IdentityException {
                // No-op: avoid real user-store I/O in unit tests.
            }
        };

        userStoreManager = mock(UserStoreManager.class);
        realmConfiguration = mock(RealmConfiguration.class);
        when(((org.wso2.carbon.user.core.UserStoreManager) userStoreManager).getRealmConfiguration())
                .thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)).thenReturn(DOMAIN_NAME);
        when(userStoreManager.getTenantId()).thenReturn(TENANT_ID);
        when(userStoreManager.isReadOnly()).thenReturn(false);

        identityUtil = mockStatic(IdentityUtil.class);
        identityUtil.when(() -> IdentityUtil.isUserStoreCaseSensitive(
                (org.wso2.carbon.user.core.UserStoreManager) any())).thenReturn(true);
    }

    @AfterMethod
    public void tearDown() {

        identityUtil.close();
        try {
            PrivilegedCarbonContext.endTenantFlow();
        } catch (Exception e) {
            // Ignore if no tenant flow was started.
        }
    }

    @Test
    public void testStoreOnReadPutsValueInCacheForWritableStore()
            throws IdentityException, UserStoreException {

        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(UserIdentityDataStore.ACCOUNT_LOCK, "false");
        UserIdentityClaimsDO dto = new UserIdentityClaimsDO(USER_NAME, claimsMap);
        dto.setTenantId(TENANT_ID);

        store.storeOnRead(dto, userStoreManager);

        String expectedKey = DOMAIN_NAME + TENANT_ID + USER_NAME;
        verify(mockCache).putOnRead(eq(expectedKey), any(UserIdentityClaimsDO.class));
    }

    @Test
    public void testStoreOnReadWithReadOnlyStoreDoesNotCallSetUserClaims()
            throws IdentityException, UserStoreException {

        when(userStoreManager.isReadOnly()).thenReturn(true);

        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(UserIdentityDataStore.ACCOUNT_LOCK, "false");
        UserIdentityClaimsDO dto = new UserIdentityClaimsDO(USER_NAME, claimsMap);
        dto.setTenantId(TENANT_ID);

        // The method should return after logging a warning and still put to cache via super.storeOnRead.
        store.storeOnRead(dto, userStoreManager);

        // putOnRead should still be called (super.storeOnRead is called before the read-only guard).
        String expectedKey = DOMAIN_NAME + TENANT_ID + USER_NAME;
        verify(mockCache).putOnRead(eq(expectedKey), any(UserIdentityClaimsDO.class));
    }

    @Test
    public void testStoreOnReadWithNullUserNameSkipsCacheWrite()
            throws IdentityException, UserStoreException {

        UserIdentityClaimsDO dto = new UserIdentityClaimsDO(null, new HashMap<>());
        dto.setTenantId(TENANT_ID);

        store.storeOnRead(dto, userStoreManager);

        verify(mockCache, never()).putOnRead(any(), any());
    }

    @Test
    public void testStoreOnReadWithNullCacheDoesNotThrow() throws IdentityException, UserStoreException {

        UserStoreBasedIdentityDataStore storeWithNullCache = new UserStoreBasedIdentityDataStore() {
            @Override
            protected Cache<String, UserIdentityClaimsDO> getCache() {
                return null;
            }

            @Override
            protected void setUserClaimsValuesInUserStore(org.wso2.carbon.user.api.UserStoreManager userStoreManager,
                    String username, Map<String, String> claims, String profile) {
                // No-op.
            }
        };

        Map<String, String> claimsMap = new HashMap<>();
        UserIdentityClaimsDO dto = new UserIdentityClaimsDO(USER_NAME, claimsMap);

        storeWithNullCache.storeOnRead(dto, userStoreManager);
    }

    @Test
    public void testStoreOnReadTenantIdSetOnNewDTOFromUserStoreManager()
            throws IdentityException, UserStoreException {

        int expectedTenantId = 5;
        when(userStoreManager.getTenantId()).thenReturn(expectedTenantId);
        when(realmConfiguration.getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)).thenReturn(DOMAIN_NAME);

        Map<String, String> claimsMap = new HashMap<>();
        UserIdentityClaimsDO dto = new UserIdentityClaimsDO(USER_NAME, claimsMap);

        store.storeOnRead(dto, userStoreManager);

        String expectedKey = DOMAIN_NAME + expectedTenantId + USER_NAME;
        verify(mockCache).putOnRead(eq(expectedKey), any(UserIdentityClaimsDO.class));
    }
}
