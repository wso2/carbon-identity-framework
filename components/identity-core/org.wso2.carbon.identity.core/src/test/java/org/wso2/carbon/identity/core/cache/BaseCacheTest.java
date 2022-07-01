/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.core.cache;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.File;
import java.net.URL;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Test for the Base cache.
 */
@PrepareForTest({IdentityTenantUtil.class})
@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.*", "org.w3c.*",
        "javax.naming.*", "javax.sql.*", "org.mockito.*"})
public class BaseCacheTest {

    @BeforeMethod
    public void setUp() throws Exception {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.getAbsolutePath());
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);

        RealmService mockRealmService = mock(RealmService.class);
        TenantManager mockTenantManager = mock(TenantManager.class);
        when(mockRealmService.getTenantManager()).thenReturn(mockTenantManager);

        when(mockTenantManager.getDomain(1)).thenReturn("foo.com");
        when(mockTenantManager.getTenantId("foo.com")).thenReturn(1);
        when(mockTenantManager.getDomain(2)).thenReturn("bar.com");
        when(mockTenantManager.getTenantId("bar.com")).thenReturn(2);
        OSGiDataHolder.getInstance().setUserRealmService(mockRealmService);

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(1)).thenReturn("foo.com");
        when(IdentityTenantUtil.getTenantDomain(2)).thenReturn("bar.com");
        when(IdentityTenantUtil.getTenantId("foo.com")).thenReturn(1);
        when(IdentityTenantUtil.getTenantId("bar.com")).thenReturn(2);
    }
    @Test
    public void testAddition() {

        TestCache.getInstance().addToCache(new TestCacheKey("test"), new TestCacheEntry("value"), 1);

        TestCacheEntry entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test"), 1);
        assertEquals("value", entry.getValue());
        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test"), "foo.com");
        assertEquals("value", entry.getValue());

        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test"), "bar.com");
        assertNull(entry);
    }

    @Test
    public void testUpdate() {

        TestCache.getInstance().addToCache(new TestCacheKey("test"), new TestCacheEntry("value"), 1);

        TestCacheEntry entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test"), 1);
        assertEquals("value", entry.getValue());

        TestCache.getInstance().addToCache(new TestCacheKey("test"), new TestCacheEntry("newValue"), 1);

        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test"), "foo.com");
        assertEquals("newValue", entry.getValue());
    }

    @Test
    public void testClearCacheEntry() {

        TestCache.getInstance().addToCache(new TestCacheKey("test1"), new TestCacheEntry("value1"), 1);
        TestCache.getInstance().addToCache(new TestCacheKey("test2"), new TestCacheEntry("value2"), 1);

        TestCacheEntry entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test1"), 1);
        assertEquals("value1", entry.getValue());
        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test2"), 1);
        assertEquals("value2", entry.getValue());

        TestCache.getInstance().clearCacheEntry(new TestCacheKey("test1"), 1);

        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test1"), "foo.com");
        assertNull(entry);
        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test2"), "foo.com");
        assertEquals("value2", entry.getValue());
    }

    @Test
    public void testClear() {

        TestCache.getInstance().addToCache(new TestCacheKey("test1"), new TestCacheEntry("value1"), 1);
        TestCache.getInstance().addToCache(new TestCacheKey("test2"), new TestCacheEntry("value2"), 2);

        TestCacheEntry entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test1"), 1);
        assertEquals("value1", entry.getValue());
        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test2"), 2);
        assertEquals("value2", entry.getValue());

        TestCache.getInstance().clear(1);

        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test1"), "foo.com");
        assertNull(entry);
        entry = TestCache.getInstance().getValueFromCache(new TestCacheKey("test2"), "bar.com");
        assertEquals("value2", entry.getValue());
    }
}
