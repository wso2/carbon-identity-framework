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
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

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
                // {maxTimeoutPresent, maxTimeoutMinutes, createdTime, shouldBeValid, description}
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
            CarbonContext mockCarbonContext = mock(CarbonContext.class, withSettings().lenient());
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
            CarbonContext mockCarbonContext = mock(CarbonContext.class,
                    withSettings().lenient());
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

            // Setup IdPManagementUtil mock with max timeout enabled.
            idpManagementUtil.when(() -> IdPManagementUtil.getMaximumSessionTimeout(TENANT_DOMAIN))
                    .thenReturn(Optional.of(7200)); // 2 hours in seconds

            // Setup created timestamp as null.
            when(mockSessionContext.getProperty(FrameworkConstants.CREATED_TIMESTAMP)).thenReturn(null);

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
            CarbonContext mockCarbonContext = mock(CarbonContext.class,
                    withSettings().lenient());
            carbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn(TENANT_DOMAIN);

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
}
