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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InMemoryIdentityDataStore#storeOnRead}.
 */
public class InMemoryIdentityDataStoreTest {

    private static final String DOMAIN_NAME = "PRIMARY";
    private static final int TENANT_ID = -1234;
    private static final String USER_NAME = "testUser";

    private Cache<String, UserIdentityClaimsDO> mockCache;
    private InMemoryIdentityDataStore store;
    private UserStoreManager userStoreManager;
    private RealmConfiguration realmConfiguration;
    private MockedStatic<IdentityUtil> identityUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());

        mockCache = mock(Cache.class);

        // Create a subclass that returns the mock cache to avoid OSGI/CachingFactory dependency.
        store = new InMemoryIdentityDataStore() {
            @Override
            protected Cache<String, UserIdentityClaimsDO> getCache() {
                return mockCache;
            }
        };

        userStoreManager = mock(UserStoreManager.class);
        realmConfiguration = mock(RealmConfiguration.class);
        when(((org.wso2.carbon.user.core.UserStoreManager) userStoreManager).getRealmConfiguration())
                .thenReturn(realmConfiguration);
        when(realmConfiguration.getUserStoreProperty(
                UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME)).thenReturn(DOMAIN_NAME);
        when(userStoreManager.getTenantId()).thenReturn(TENANT_ID);

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
    public void testStoreOnReadPutsValueInCacheWhenUserNameIsPresent()
            throws IdentityException, UserStoreException {

        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(UserIdentityDataStore.ACCOUNT_LOCK, "false");
        UserIdentityClaimsDO userIdentityDTO = new UserIdentityClaimsDO(USER_NAME, claimsMap);

        store.storeOnRead(userIdentityDTO, userStoreManager);

        String expectedKey = DOMAIN_NAME + TENANT_ID + USER_NAME;
        verify(mockCache).putOnRead(eq(expectedKey), eq(userIdentityDTO));
    }

    @Test
    public void testStoreOnReadWithNullUserNameDoesNotInteractWithCache()
            throws IdentityException, UserStoreException {

        UserIdentityClaimsDO userIdentityDTO = mock(UserIdentityClaimsDO.class);
        when(userIdentityDTO.getUserName()).thenReturn(null);

        store.storeOnRead(userIdentityDTO, userStoreManager);

        verify(mockCache, never()).putOnRead(any(), any());
    }

    @Test
    public void testStoreOnReadWithNullDTODoesNotInteractWithCache()
            throws IdentityException, UserStoreException {

        store.storeOnRead(null, userStoreManager);

        verify(mockCache, never()).putOnRead(any(), any());
    }

    @Test
    public void testStoreOnReadLowercasesUserNameForCaseInsensitiveStore()
            throws IdentityException, UserStoreException {

        // Override: report the store as case-insensitive.
        identityUtil.when(() -> IdentityUtil.isUserStoreCaseSensitive(
                (org.wso2.carbon.user.core.UserStoreManager) any())).thenReturn(false);

        String mixedCaseUser = "TestUser";
        Map<String, String> claimsMap = new HashMap<>();
        UserIdentityClaimsDO userIdentityDTO = new UserIdentityClaimsDO(mixedCaseUser, claimsMap);

        store.storeOnRead(userIdentityDTO, userStoreManager);

        // The cache key should use the lower-cased username.
        String expectedKey = DOMAIN_NAME + TENANT_ID + mixedCaseUser.toLowerCase();
        verify(mockCache).putOnRead(eq(expectedKey), eq(userIdentityDTO));
    }

    @Test
    public void testStoreOnReadWithNullCacheDoesNotThrow() throws IdentityException, UserStoreException {

        InMemoryIdentityDataStore storeWithNullCache = new InMemoryIdentityDataStore() {
            @Override
            protected Cache<String, UserIdentityClaimsDO> getCache() {
                return null;
            }
        };

        Map<String, String> claimsMap = new HashMap<>();
        UserIdentityClaimsDO userIdentityDTO = new UserIdentityClaimsDO(USER_NAME, claimsMap);

        // Should not throw even when cache is null.
        storeWithNullCache.storeOnRead(userIdentityDTO, userStoreManager);
    }
}
