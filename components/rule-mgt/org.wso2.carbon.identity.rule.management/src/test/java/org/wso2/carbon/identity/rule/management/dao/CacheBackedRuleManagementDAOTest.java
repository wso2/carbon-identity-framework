/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.rule.management.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCache;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCacheEntry;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCacheKey;
import org.wso2.carbon.identity.rule.management.internal.dao.RuleManagementDAO;
import org.wso2.carbon.identity.rule.management.internal.dao.impl.CacheBackedRuleManagementDAO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedRuleManagementDAOTest {

    private RuleManagementDAO ruleManagementDAO;
    private CacheBackedRuleManagementDAO cacheBackedRuleManagementDAO;
    private RuleCache ruleCache;

    public static final String RULE_ID = "ruleId";
    public static final int TENANT_ID = 1;

    @BeforeClass
    public void setUpClass() {

        ruleCache = RuleCache.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        ruleManagementDAO = mock(RuleManagementDAO.class);
        cacheBackedRuleManagementDAO = new CacheBackedRuleManagementDAO(ruleManagementDAO);
    }

    @Test
    public void testAddRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);

        cacheBackedRuleManagementDAO.addRule(rule, TENANT_ID);

        verify(ruleManagementDAO).addRule(rule, TENANT_ID);
    }

    @Test
    public void testUpdateRule() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);

        cacheBackedRuleManagementDAO.updateRule(rule, TENANT_ID);

        verify(ruleManagementDAO).updateRule(rule, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testUpdateRuleWhenCacheIsPopulated() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        RuleCacheEntry cacheEntry = new RuleCacheEntry(rule);
        ruleCache.addToCache(new RuleCacheKey(RULE_ID), cacheEntry, TENANT_ID);

        cacheBackedRuleManagementDAO.updateRule(rule, TENANT_ID);

        verify(ruleManagementDAO).updateRule(rule, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testDeleteRule() throws RuleManagementException {

        cacheBackedRuleManagementDAO.deleteRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).deleteRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testDeleteRuleWhenCacheIsPopulated() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        RuleCacheEntry cacheEntry = new RuleCacheEntry(rule);
        ruleCache.addToCache(new RuleCacheKey(RULE_ID), cacheEntry, TENANT_ID);

        cacheBackedRuleManagementDAO.deleteRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).deleteRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testGetRuleByRuleIdCacheHit() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        RuleCacheEntry cacheEntry = new RuleCacheEntry(rule);
        ruleCache.addToCache(new RuleCacheKey(RULE_ID), cacheEntry, TENANT_ID);

        Rule result = cacheBackedRuleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID);

        assertEquals(result, rule);
        verify(ruleManagementDAO, never()).getRuleByRuleId(RULE_ID, TENANT_ID);
    }

    @Test
    public void testGetRuleByRuleIdCacheMiss() throws RuleManagementException {

        ruleCache.clear(TENANT_ID);

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        when(ruleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID)).thenReturn(rule);

        Rule result = cacheBackedRuleManagementDAO.getRuleByRuleId(RULE_ID, TENANT_ID);

        assertEquals(result, rule);
        verify(ruleManagementDAO).getRuleByRuleId(RULE_ID, TENANT_ID);
        // Verify that the rule is added to the cache
        assertEquals(rule, ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID).getRule());
    }

    @Test
    public void testActivateRule() throws RuleManagementException {

        cacheBackedRuleManagementDAO.activateRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).activateRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testActivateRuleWhenCacheIsPopulated() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        RuleCacheEntry cacheEntry = new RuleCacheEntry(rule);
        ruleCache.addToCache(new RuleCacheKey(RULE_ID), cacheEntry, TENANT_ID);

        cacheBackedRuleManagementDAO.activateRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).activateRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testDeactivateRule() throws RuleManagementException {

        cacheBackedRuleManagementDAO.deactivateRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).deactivateRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }

    @Test
    public void testDeactivateRuleWhenCacheIsPopulated() throws RuleManagementException {

        Rule rule = mock(Rule.class);
        when(rule.getId()).thenReturn(RULE_ID);
        RuleCacheEntry cacheEntry = new RuleCacheEntry(rule);
        ruleCache.addToCache(new RuleCacheKey(RULE_ID), cacheEntry, TENANT_ID);

        cacheBackedRuleManagementDAO.deactivateRule(RULE_ID, TENANT_ID);

        verify(ruleManagementDAO).deactivateRule(RULE_ID, TENANT_ID);
        assertNull(ruleCache.getValueFromCache(new RuleCacheKey(RULE_ID), TENANT_ID));
    }
}
