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

package org.wso2.carbon.identity.rule.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
import org.wso2.carbon.identity.rule.management.api.model.Rule;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCache;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCacheEntry;
import org.wso2.carbon.identity.rule.management.core.cache.RuleCacheKey;
import org.wso2.carbon.identity.rule.management.internal.dao.RuleManagementDAO;

/**
 * Cache backed Rule Management DAO.
 * This class is used to implement the caching on top of the data layer operations.
 * This caches the Rule object.
 */
public class CacheBackedRuleManagementDAO implements RuleManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedRuleManagementDAO.class);

    private final RuleManagementDAO ruleManagementDAO;
    private final RuleCache ruleCache;

    public CacheBackedRuleManagementDAO(RuleManagementDAO ruleManagementDAO) {

        this.ruleManagementDAO = ruleManagementDAO;
        ruleCache = RuleCache.getInstance();
    }

    /**
     * Add a new Rule.
     * This method is used directly invokes the data layer operation to add the Rule.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public void addRule(Rule rule, int tenantId) throws RuleManagementException {

        ruleManagementDAO.addRule(rule, tenantId);
    }

    /**
     * Update an existing Rule.
     * This method clears the cache entry upon rule update.
     *
     * @param rule     Rule object
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public void updateRule(Rule rule, int tenantId) throws RuleManagementException {

        ruleCache.clearCacheEntry(new RuleCacheKey(rule.getId()), tenantId);
        LOG.debug("Rule cache entry is cleared for rule id: " + rule.getId() + " for rule update.");
        ruleManagementDAO.updateRule(rule, tenantId);
    }

    /**
     * Delete a Rule.
     * This method clears the cache entry upon rule deletion.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public void deleteRule(String ruleId, int tenantId) throws RuleManagementException {

        ruleCache.clearCacheEntry(new RuleCacheKey(ruleId), tenantId);
        LOG.debug("Rule cache entry is cleared for rule id: " + ruleId + " for rule deletion.");
        ruleManagementDAO.deleteRule(ruleId, tenantId);
    }

    /**
     * Get a Rule by Rule ID.
     * This method first checks the cache for the Rule object.
     * If the Rule object is not found in the cache, it invokes the data layer operation to get the Rule.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @return Rule object
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public Rule getRuleByRuleId(String ruleId, int tenantId) throws RuleManagementException {

        RuleCacheEntry ruleCacheEntry = ruleCache.getValueFromCache(new RuleCacheKey(ruleId), tenantId);
        if (ruleCacheEntry != null && ruleCacheEntry.getRule() != null) {
            LOG.debug("Rule cache hit for rule id: " + ruleId + ". Returning from cache.");
            return ruleCacheEntry.getRule();
        }

        Rule rule = ruleManagementDAO.getRuleByRuleId(ruleId, tenantId);
        if (rule != null) {
            LOG.debug("Rule cache miss for rule id: " + ruleId + ". Adding to cache.");
            ruleCache.addToCache(new RuleCacheKey(ruleId), new RuleCacheEntry(rule), tenantId);
        }
        return rule;
    }

    /**
     * Activate a Rule.
     * This method clears the cache entry upon rule activation.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public void activateRule(String ruleId, int tenantId) throws RuleManagementException {

        ruleCache.clearCacheEntry(new RuleCacheKey(ruleId), tenantId);
        LOG.debug("Rule cache entry is cleared for rule id: " + ruleId + " for rule activation.");
        ruleManagementDAO.activateRule(ruleId, tenantId);
    }

    /**
     * Deactivate a Rule.
     * This method clears the cache entry upon rule deactivation.
     *
     * @param ruleId   Rule ID
     * @param tenantId Tenant ID
     * @throws RuleManagementException Rule Management Exception
     */
    @Override
    public void deactivateRule(String ruleId, int tenantId) throws RuleManagementException {

        ruleCache.clearCacheEntry(new RuleCacheKey(ruleId), tenantId);
        LOG.debug("Rule cache entry is cleared for rule id: " + ruleId + " for rule deactivation.");
        ruleManagementDAO.deactivateRule(ruleId, tenantId);
    }
}
