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

package org.wso2.carbon.identity.policy.management.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCache;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheEntry;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheKey;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;
import org.wso2.carbon.identity.policy.management.internal.dao.impl.CacheBackedPolicyManagementDAO;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for CacheBackedPolicyManagementDAO.
 * Verifies name-only caching, pass-through for ID lookups, and precise cache invalidation on writes.
 */
@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedPolicyManagementDAOTest {

    private static final String POLICY_ID = "policyId";
    private static final String POLICY_NAME = "TestPolicy";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = 1;

    private PolicyManagementDAO policyManagementDAO;
    private CacheBackedPolicyManagementDAO cacheBackedPolicyManagementDAO;
    private PolicyCache policyCache;

    @BeforeClass
    public void setUpClass() {

        policyCache = PolicyCache.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        policyManagementDAO = mock(PolicyManagementDAO.class);
        cacheBackedPolicyManagementDAO = new CacheBackedPolicyManagementDAO(policyManagementDAO);
        policyCache.clear(TENANT_ID);
    }

    private Policy policy() {

        return new Policy(POLICY_ID, POLICY_NAME, TENANT_DOMAIN, Collections.emptyList());
    }

    @Test
    public void testAddPolicyDoesNotCache() throws PolicyManagementException {

        Policy policy = policy();

        cacheBackedPolicyManagementDAO.addPolicy(policy, TENANT_ID);

        verify(policyManagementDAO).addPolicy(policy, TENANT_ID);
        assertNull(policyCache.getValueFromCache(new PolicyCacheKey(POLICY_NAME), TENANT_ID));
    }

    @Test
    public void testGetPolicyByNameCacheMissPopulatesNameEntry() throws PolicyManagementException {

        Policy policy = policy();
        when(policyManagementDAO.getPolicyByName(POLICY_NAME, TENANT_ID)).thenReturn(policy);

        Policy result = cacheBackedPolicyManagementDAO.getPolicyByName(POLICY_NAME, TENANT_ID);

        assertEquals(result, policy);
        verify(policyManagementDAO).getPolicyByName(POLICY_NAME, TENANT_ID);
        assertEquals(policyCache.getValueFromCache(new PolicyCacheKey(POLICY_NAME), TENANT_ID).getPolicy(),
                policy);
    }

    @Test
    public void testGetPolicyByNameCacheHit() throws PolicyManagementException {

        Policy policy = policy();
        policyCache.addToCacheOnRead(new PolicyCacheKey(POLICY_NAME), new PolicyCacheEntry(policy), TENANT_ID);

        Policy result = cacheBackedPolicyManagementDAO.getPolicyByName(POLICY_NAME, TENANT_ID);

        assertEquals(result, policy);
        verify(policyManagementDAO, never()).getPolicyByName(POLICY_NAME, TENANT_ID);
    }

    @Test
    public void testGetPolicyByIdAlwaysDelegates() throws PolicyManagementException {

        Policy policy = policy();
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(policy);

        Policy first = cacheBackedPolicyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID);
        Policy second = cacheBackedPolicyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID);

        assertEquals(first, policy);
        assertEquals(second, policy);
        // Both calls must go to the underlying DAO — getPolicyById is never cached.
        verify(policyManagementDAO, times(2)).getPolicyById(POLICY_ID, TENANT_ID);
    }

    @Test
    public void testUpdatePolicyClearsNameEntry() throws PolicyManagementException {

        Policy policy = policy();
        policyCache.addToCacheOnRead(new PolicyCacheKey(POLICY_NAME), new PolicyCacheEntry(policy), TENANT_ID);
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(policy);
        when(policyManagementDAO.updatePolicy(policy, TENANT_ID)).thenReturn(policy);

        cacheBackedPolicyManagementDAO.updatePolicy(policy, TENANT_ID);

        verify(policyManagementDAO).updatePolicy(policy, TENANT_ID);
        assertNull(policyCache.getValueFromCache(new PolicyCacheKey(POLICY_NAME), TENANT_ID));
    }

    @Test
    public void testUpdatePolicyRenamesClearsBothOldAndNewName() throws PolicyManagementException {

        String oldName = "OldPolicy";
        Policy existing = new Policy(POLICY_ID, oldName, TENANT_DOMAIN, Collections.emptyList());
        Policy renamed = new Policy(POLICY_ID, POLICY_NAME, TENANT_DOMAIN, Collections.emptyList());
        policyCache.addToCacheOnRead(new PolicyCacheKey(oldName), new PolicyCacheEntry(existing), TENANT_ID);
        policyCache.addToCacheOnRead(new PolicyCacheKey(POLICY_NAME), new PolicyCacheEntry(existing), TENANT_ID);
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(existing);
        when(policyManagementDAO.updatePolicy(renamed, TENANT_ID)).thenReturn(renamed);

        cacheBackedPolicyManagementDAO.updatePolicy(renamed, TENANT_ID);

        assertNull(policyCache.getValueFromCache(new PolicyCacheKey(oldName), TENANT_ID));
        assertNull(policyCache.getValueFromCache(new PolicyCacheKey(POLICY_NAME), TENANT_ID));
    }

    @Test
    public void testDeletePolicyClearsNameEntry() throws PolicyManagementException {

        Policy policy = policy();
        policyCache.addToCacheOnRead(new PolicyCacheKey(POLICY_NAME), new PolicyCacheEntry(policy), TENANT_ID);
        when(policyManagementDAO.getPolicyById(POLICY_ID, TENANT_ID)).thenReturn(policy);

        cacheBackedPolicyManagementDAO.deletePolicy(POLICY_ID, TENANT_ID);

        verify(policyManagementDAO).deletePolicy(POLICY_ID, TENANT_ID);
        assertNull(policyCache.getValueFromCache(new PolicyCacheKey(POLICY_NAME), TENANT_ID));
    }
}
