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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SessionContextCache.
 */
@WithCarbonHome
@Listeners(MockitoTestNGListener.class)
public class SessionContextCacheTest {

    private static final String CONTEXT_ID = "test-context-id";
    private static final String TENANT_DOMAIN = "test.com";

    @Mock
    private SessionContext mockSessionContext;

    private SessionContextCache sessionContextCache;
    private SessionContextCacheKey cacheKey;
    private SessionContextCacheEntry cacheEntry;

    @BeforeMethod
    public void setUp() {

        sessionContextCache = SessionContextCache.getInstance();
        cacheKey = new SessionContextCacheKey(CONTEXT_ID);
        cacheEntry = new SessionContextCacheEntry();
        cacheEntry.setContext(mockSessionContext);
    }

    @DataProvider
    public Object[][] isValidMaximumSessionLifetimeData() {

        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - TimeUnit.HOURS.toMillis(1);
        long twoHoursAgo = currentTime - TimeUnit.HOURS.toMillis(2);
        long threeHoursAgo = currentTime - TimeUnit.HOURS.toMillis(3);

        return new Object[][]{
                {false, 0, oneHourAgo, true, "No max timeout configured - should be valid"},
                {true, 120, oneHourAgo, true, "Session within 2-hour limit"},
                {true, 60, oneHourAgo, false, "Session exceeds 1-hour limit"},
                {true, 120, threeHoursAgo, false, "Session exceeds 2-hour limit"},
                {true, 180, twoHoursAgo, true, "Session within 3-hour limit"},
        };
    }

    /**
     * Test isValidMaximumSessionLifetime method using reflection.
     */
    @Test(dataProvider = "isValidMaximumSessionLifetimeData")
    public void testIsValidMaximumSessionLifetime(boolean maxTimeoutPresent, int maxTimeoutMinutes,
                                                   long createdTime, boolean shouldBeValid,
                                                   String description) throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock.
            if (maxTimeoutPresent) {
                int timeoutInSeconds = maxTimeoutMinutes * 60;
                idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                        .thenReturn(Optional.of(timeoutInSeconds));
                when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP)).thenReturn(createdTime);
            } else {
                idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                        .thenReturn(Optional.empty());
            }

            // Use reflection to invoke the private method.
            Method method = SessionContextCache.class.getDeclaredMethod("isValidMaximumSessionLifetime",
                    SessionContextCacheKey.class, SessionContextCacheEntry.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(sessionContextCache, cacheKey, cacheEntry);

            Assert.assertEquals(result, shouldBeValid,
                    "Test case failed: " + description);
        }
    }

    /**
     * Test isValidMaximumSessionLifetime when cache entry is null.
     */
    @Test
    public void testIsValidMaximumSessionLifetimeWithNullCacheEntry() throws Exception {

        Method method = SessionContextCache.class.getDeclaredMethod("isValidMaximumSessionLifetime",
                SessionContextCacheKey.class, SessionContextCacheEntry.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(sessionContextCache, cacheKey, null);

        Assert.assertFalse(result, "Should return false when cache entry is null");
    }

    /**
     * Test isValidMaximumSessionLifetime when created time is null.
     */
    @Test
    public void testIsValidMaximumSessionLifetimeWithNullCreatedTime() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock with max timeout enabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(7200)); // 2 hours in seconds

            // Setup created timestamp as null.
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP)).thenReturn(null);

            // Use reflection to invoke the private method.
            Method method = SessionContextCache.class.getDeclaredMethod("isValidMaximumSessionLifetime",
                    SessionContextCacheKey.class, SessionContextCacheEntry.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(sessionContextCache, cacheKey, cacheEntry);

            Assert.assertFalse(result, "Should return false when created time is null");
        }
    }

    /**
     * Test isValidMaximumSessionLifetime when session context is null.
     */
    @Test
    public void testIsValidMaximumSessionLifetimeWithNullSessionContext() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock with max timeout enabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(7200)); // 2 hours in seconds

            // Create cache entry with null session context.
            SessionContextCacheEntry nullContextEntry = new SessionContextCacheEntry();
            nullContextEntry.setContext(null);

            // Use reflection to invoke the private method.
            Method method = SessionContextCache.class.getDeclaredMethod("isValidMaximumSessionLifetime",
                    SessionContextCacheKey.class, SessionContextCacheEntry.class);
            method.setAccessible(true);

            boolean result = (boolean) method.invoke(sessionContextCache, cacheKey, nullContextEntry);

            Assert.assertFalse(result, "Should return false when session context is null");
        }
    }

    /**
     * Test isSessionExpired when maximum session lifetime is exceeded.
     * This test verifies that even if idle session checks pass, an expired maximum lifetime will cause expiration.
     */
    @Test
    public void testIsSessionExpiredWhenMaxLifetimeExceeded() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max session timeout of 1 hour.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(3600));
            
            // Setup idle session timeout (large value so idle check passes).
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(600);

            // Setup remember me timeout (not a remember me session).
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(1800);

            // Session created 2 hours ago - exceeds maximum lifetime of 1 hour.
            long currentTime = System.currentTimeMillis();
            long twoHoursAgo = currentTime - TimeUnit.HOURS.toMillis(2);
            // Accessed 5 minutes ago (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(twoHoursAgo);
            lenient().when(mockSessionContext.getProperty("tenantDomain")).thenReturn(TENANT_DOMAIN);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            boolean result = sessionContextCache.isSessionExpired(cacheKey, cacheEntry);

            Assert.assertTrue(result,
                    "Session should be expired when maximum lifetime is exceeded even if idle is valid");
        }
    }

    /**
     * Test isSessionExpired when maximum session lifetime is valid and idle session is valid.
     */
    @Test
    public void testIsSessionExpiredWhenMaxLifetimeAndIdleSessionAreValid() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max session timeout of 2 hours.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(7200));
            
            // Setup idle session timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(1800);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(3600);

            // Session created 30 minutes ago - within maximum lifetime of 2 hours.
            long currentTime = System.currentTimeMillis();
            long thirtyMinutesAgo = currentTime - TimeUnit.MINUTES.toMillis(30);
            // Accessed 5 minutes ago (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(thirtyMinutesAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            boolean result = sessionContextCache.isSessionExpired(cacheKey, cacheEntry);

            Assert.assertFalse(result,
                    "Session should not be expired when both max lifetime and idle session are valid");
        }
    }

    /**
     * Test isSessionExpired when maximum session lifetime check fails due to null created time.
     */
    @Test
    public void testIsSessionExpiredWithNullCreatedTimeInMaxLifetimeCheck() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock with max timeout enabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(7200));
            
            // Setup idle session timeout (large value so idle check passes).
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(1800);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(3600);

            // Created timestamp is null - should cause max lifetime check to fail.
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP)).thenReturn(null);
            lenient().when(mockSessionContext.getProperty("tenantDomain")).thenReturn(TENANT_DOMAIN);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            long currentTime = System.currentTimeMillis();
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5); // Recent access.
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            boolean result = sessionContextCache.isSessionExpired(cacheKey, cacheEntry);

            Assert.assertTrue(result, "Session should be expired when created time is null, as max lifetime " +
                    "check should be failed.");
        }
    }

    /**
     * Test isSessionExpired when maximum session lifetime feature is disabled.
     */
    @Test
    public void testIsSessionExpiredWhenMaxLifetimeFeatureDisabled() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max timeout feature disabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.empty()); // Feature disabled
            
            // Setup idle session timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(1800); // 30 minutes in seconds
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(7200); // 2 hours in seconds

            // Session created 5 hours ago - would exceed any reasonable max lifetime if it were enabled.
            long currentTime = System.currentTimeMillis();
            long fiveHoursAgo = currentTime - TimeUnit.HOURS.toMillis(5);
            // Recent access (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(fiveHoursAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            boolean result = sessionContextCache.isSessionExpired(cacheKey, cacheEntry);

            Assert.assertFalse(result,
                    "Session should not be expired when max lifetime feature is disabled and idle is valid");
        }
    }

    /**
     * Test getValueFromCache when maximum session lifetime is exceeded.
     * Should return null and clear cache entry.
     */
    @Test
    public void testGetValueFromCacheWhenMaxLifetimeExceeded() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max session timeout of 1 hour.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(3600));
            
            // Setup idle session timeout (large value so idle check passes).
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(600);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(1200);

            // Session created 2 hours ago (exceeds 1 hour max lifetime).
            long currentTime = System.currentTimeMillis();
            long twoHoursAgo = currentTime - TimeUnit.HOURS.toMillis(2);
            // Recent access (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(twoHoursAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            // Add to cache so super.getValueFromCache() returns it.
            sessionContextCache.addToCache(cacheKey, cacheEntry, TENANT_DOMAIN);

            SessionContextCacheEntry result = sessionContextCache.getValueFromCache(cacheKey, TENANT_DOMAIN);

            Assert.assertNull(result, "getValueFromCache should return null when max lifetime is exceeded");
        }
    }

    /**
     * Test getValueFromCache when maximum session lifetime is not exceeded.
     * Should return the cache entry.
     */
    @Test
    public void testGetValueFromCacheWhenMaxLifetimeNotExceeded() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max session timeout of 4 hours.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(14400));
            
            // Setup idle session timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(1800);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(7200);

            // Session created 1 hour ago (within 4 hour max lifetime).
            long currentTime = System.currentTimeMillis();
            long oneHourAgo = currentTime - TimeUnit.HOURS.toMillis(1);
            // Recent access (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(oneHourAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            // Add to cache so super.getValueFromCache() returns it.
            sessionContextCache.addToCache(cacheKey, cacheEntry, TENANT_DOMAIN);

            SessionContextCacheEntry result = sessionContextCache.getValueFromCache(cacheKey, TENANT_DOMAIN);

            Assert.assertNotNull(result, "getValueFromCache should return entry when max lifetime is not exceeded");
            Assert.assertEquals(result, cacheEntry, "Should return the same cache entry");
        }
    }

    /**
     * Test getValueFromCache when maximum session lifetime feature is disabled.
     * Should return the cache entry based on idle/remember me validation only.
     */
    @Test
    public void testGetValueFromCacheWhenMaxLifetimeFeatureDisabled() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max timeout feature disabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.empty());
            
            // Setup idle session timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(1800);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(7200);

            // Session created 10 hours ago (would fail max lifetime if it were enabled).
            long currentTime = System.currentTimeMillis();
            long tenHoursAgo = currentTime - TimeUnit.HOURS.toMillis(10);
            // Recent access (idle is valid).
            long recentAccess = currentTime - TimeUnit.MINUTES.toMillis(5);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(tenHoursAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, recentAccess);

            // Add to cache so super.getValueFromCache() returns it.
            sessionContextCache.addToCache(cacheKey, cacheEntry, TENANT_DOMAIN);

            SessionContextCacheEntry result = sessionContextCache.getValueFromCache(cacheKey, TENANT_DOMAIN);

            Assert.assertNotNull(result,
                    "getValueFromCache should return entry when max lifetime feature is disabled and idle is valid");
            Assert.assertEquals(result, cacheEntry, "Should return the same cache entry");
        }
    }

    /**
     * Test getValueFromCache when both idle timeout and maximum session lifetime are exceeded.
     * Should return null and clear cache entry.
     */
    @Test
    public void testGetValueFromCacheWhenBothIdleAndMaxLifetimeExceeded() throws Exception {

        try (MockedStatic<CarbonContext> carbonContext = mockStatic(CarbonContext.class);
             MockedStatic<IdPManagementUtil> idpManagementUtil = mockStatic(IdPManagementUtil.class);
             MockedStatic<org.wso2.carbon.identity.core.util.IdentityTenantUtil> tenantUtil =
                     mockStatic(org.wso2.carbon.identity.core.util.IdentityTenantUtil.class)) {

            // Setup CarbonContext mock.
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            lenient().when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock - max session timeout of 1 hour.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(3600));
            
            // Setup idle session timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getIdleSessionTimeOut(TENANT_DOMAIN))
                    .thenReturn(600);
            
            // Setup remember me timeout.
            idpManagementUtil.when(() -> IdPManagementUtil.getRememberMeTimeout(TENANT_DOMAIN))
                    .thenReturn(1200);

            // Session created 2 hours ago (exceeds 1 hour max lifetime).
            long currentTime = System.currentTimeMillis();
            long twoHoursAgo = currentTime - TimeUnit.HOURS.toMillis(2);
            // Old access (idle also expired).
            long oldAccess = currentTime - TimeUnit.HOURS.toMillis(1);
            
            lenient().when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP))
                    .thenReturn(twoHoursAgo);
            lenient().when(mockSessionContext.isRememberMe()).thenReturn(false);
            
            // Use reflection to set accessed time.
            Method setAccessedTimeMethod = SessionContextCacheEntry.class.getDeclaredMethod("setAccessedTime",
                    long.class);
            setAccessedTimeMethod.setAccessible(true);
            setAccessedTimeMethod.invoke(cacheEntry, oldAccess);

            // Add to cache so super.getValueFromCache() returns it.
            sessionContextCache.addToCache(cacheKey, cacheEntry, TENANT_DOMAIN);

            SessionContextCacheEntry result = sessionContextCache.getValueFromCache(cacheKey, TENANT_DOMAIN);

            Assert.assertNull(result, "getValueFromCache should return null when both idle and max lifetime exceeded");
        }
    }
}

