/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.device.registration.internal.cache;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeviceRegistrationCache}, {@link DeviceRegistrationCacheKey} and
 * {@link DeviceRegistrationCacheEntry}.
 */
@WithCarbonHome
public class DeviceRegistrationCacheTest {

    private static final String TENANT_DOMAIN_1 = "tenant1.example.com";
    private static final String TENANT_DOMAIN_2 = "tenant2.example.com";
    private static final int TENANT_ID_1 = 100;
    private static final int TENANT_ID_2 = 200;
    private static final String REGISTRATION_ID = "reg-cache-001";

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMocked;

    @BeforeClass
    public void setUpClass() {

        identityTenantUtilMocked = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN_1)).thenReturn(TENANT_ID_1);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN_2)).thenReturn(TENANT_ID_2);
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMocked.close();
    }

    @BeforeMethod
    public void setUp() {

        DeviceRegistrationCache.getInstance().clear(TENANT_DOMAIN_1);
        DeviceRegistrationCache.getInstance().clear(TENANT_DOMAIN_2);
    }

    @Test
    public void testCacheKeyEqualsAndHashCodeForEqualIds() {

        DeviceRegistrationCacheKey key1 = new DeviceRegistrationCacheKey(REGISTRATION_ID);
        DeviceRegistrationCacheKey key2 = new DeviceRegistrationCacheKey(REGISTRATION_ID);

        Assert.assertEquals(key1, key2);
        Assert.assertEquals(key1.hashCode(), key2.hashCode());
        Assert.assertEquals(key1, key1);
    }

    @Test
    public void testCacheKeyEqualsForDifferingIds() {

        DeviceRegistrationCacheKey key1 = new DeviceRegistrationCacheKey("reg-a");
        DeviceRegistrationCacheKey key2 = new DeviceRegistrationCacheKey("reg-b");

        Assert.assertNotEquals(key1, key2);
    }

    @Test
    public void testCacheKeyEqualsAgainstNullAndDifferentType() {

        DeviceRegistrationCacheKey key = new DeviceRegistrationCacheKey(REGISTRATION_ID);

        Assert.assertNotEquals(key, null);
        Assert.assertNotEquals(key, "not-a-cache-key");
    }

    @Test
    public void testAddThenGetReturnsCachedContext() {

        DeviceRegistrationCacheKey key = new DeviceRegistrationCacheKey(REGISTRATION_ID);
        DeviceRegistrationContext context = new DeviceRegistrationContext("alice", "challenge-b64", TENANT_DOMAIN_1);

        DeviceRegistrationCache.getInstance().addToCache(key, new DeviceRegistrationCacheEntry(context),
                TENANT_DOMAIN_1);

        DeviceRegistrationCacheEntry result = DeviceRegistrationCache.getInstance()
                .getValueFromCache(key, TENANT_DOMAIN_1);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getContext());
        Assert.assertEquals(result.getContext().getUsername(), "alice");
        Assert.assertEquals(result.getContext().getChallenge(), "challenge-b64");
        Assert.assertEquals(result.getContext().getTenantDomain(), TENANT_DOMAIN_1);
    }

    @Test
    public void testClearCacheEntryRemovesIt() {

        DeviceRegistrationCacheKey key = new DeviceRegistrationCacheKey(REGISTRATION_ID);
        DeviceRegistrationContext context = new DeviceRegistrationContext("bob", "challenge-b64", TENANT_DOMAIN_1);
        DeviceRegistrationCache.getInstance().addToCache(key, new DeviceRegistrationCacheEntry(context),
                TENANT_DOMAIN_1);

        DeviceRegistrationCache.getInstance().clearCacheEntry(key, TENANT_DOMAIN_1);

        Assert.assertNull(DeviceRegistrationCache.getInstance().getValueFromCache(key, TENANT_DOMAIN_1));
    }

    @Test
    public void testSameRegistrationIdInDifferentTenantsDoesNotCollide() {

        DeviceRegistrationCacheKey key = new DeviceRegistrationCacheKey(REGISTRATION_ID);
        DeviceRegistrationContext contextTenant1 =
                new DeviceRegistrationContext("carol", "challenge-tenant-1", TENANT_DOMAIN_1);
        DeviceRegistrationContext contextTenant2 =
                new DeviceRegistrationContext("dave", "challenge-tenant-2", TENANT_DOMAIN_2);

        DeviceRegistrationCache.getInstance().addToCache(key, new DeviceRegistrationCacheEntry(contextTenant1),
                TENANT_DOMAIN_1);
        DeviceRegistrationCache.getInstance().addToCache(key, new DeviceRegistrationCacheEntry(contextTenant2),
                TENANT_DOMAIN_2);

        DeviceRegistrationCacheEntry resultTenant1 = DeviceRegistrationCache.getInstance()
                .getValueFromCache(key, TENANT_DOMAIN_1);
        DeviceRegistrationCacheEntry resultTenant2 = DeviceRegistrationCache.getInstance()
                .getValueFromCache(key, TENANT_DOMAIN_2);

        Assert.assertNotNull(resultTenant1);
        Assert.assertNotNull(resultTenant2);
        Assert.assertEquals(resultTenant1.getContext().getUsername(), "carol");
        Assert.assertEquals(resultTenant2.getContext().getUsername(), "dave");
    }
}
