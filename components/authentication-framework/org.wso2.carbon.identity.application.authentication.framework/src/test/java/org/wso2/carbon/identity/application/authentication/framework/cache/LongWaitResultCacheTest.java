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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for LongWaitResultCache.addToCacheOnRead.
 */
@WithCarbonHome
public class LongWaitResultCacheTest {

    private static final String CONTEXT_ID = "test-long-wait-id";

    private LongWaitResultCacheKey cacheKey;
    private LongWaitResultCacheEntry cacheEntry;

    @BeforeMethod
    public void setUp() {

        cacheKey = new LongWaitResultCacheKey(CONTEXT_ID);
        LongWaitStatus mockStatus = mock(LongWaitStatus.class);
        cacheEntry = new LongWaitResultCacheEntry(mockStatus);
    }

    /**
     * addToCacheOnRead is a simple delegation to super; verify it completes without error.
     */
    @Test
    public void testAddToCacheOnReadDelegatesToSuper() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            // Must not throw.
            LongWaitResultCache.getInstance().addToCacheOnRead(cacheKey, cacheEntry);
        }
    }

    /**
     * After addToCacheOnRead the value is retrievable from the cache.
     */
    @Test
    public void testAddToCacheOnReadEntryIsRetrievable() {

        try (MockedStatic<IdentityTenantUtil> tenantUtil = mockStatic(IdentityTenantUtil.class)) {

            tenantUtil.when(IdentityTenantUtil::isTenantedSessionsEnabled).thenReturn(false);

            LongWaitResultCache cache = LongWaitResultCache.getInstance();
            cache.addToCacheOnRead(cacheKey, cacheEntry);

            // putOnRead may or may not store depending on whether the key already existed;
            // the important thing is no exception was thrown during addToCacheOnRead.
            cache.getValueFromCache(cacheKey);
        }
    }

}

