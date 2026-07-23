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

package org.wso2.carbon.identity.policy.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.policy.management.api.exception.PolicyManagementException;
import org.wso2.carbon.identity.policy.management.api.model.Policy;
import org.wso2.carbon.identity.policy.management.api.model.PolicyBasicInfo;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCache;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheEntry;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheKey;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyIdByNameCache;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyIdCacheEntry;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyNameCacheKey;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;

import java.util.List;
import java.util.Objects;

/**
 * Cache-backed Policy Management DAO.
 * Wraps a PolicyManagementDAO with an in-process cache to reduce DB hits on the hot path.
 * Hot path: getPolicyByName (called on every policy evaluation) is served from cache after first read.
 * Caching is split into a canonical cache (policy ID to full Policy) and an index cache
 * (policy name to policy ID). A name-based lookup resolves the ID via the index cache and then
 * fetches the full Policy from the canonical cache, so the full Policy is never duplicated.
 */
public class CacheBackedPolicyManagementDAO implements PolicyManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedPolicyManagementDAO.class);

    private final PolicyManagementDAO policyManagementDAO;
    private final PolicyCache policyCache;
    private final PolicyIdByNameCache policyIdByNameCache;

    public CacheBackedPolicyManagementDAO(PolicyManagementDAO policyManagementDAO) {

        this.policyManagementDAO = policyManagementDAO;
        policyCache = PolicyCache.getInstance();
        policyIdByNameCache = PolicyIdByNameCache.getInstance();
    }

    /**
     * Add a new Policy.
     * This method directly invokes the data layer operation without caching the result.
     *
     * @param policy   Policy object.
     * @param tenantId Tenant ID.
     * @return Created Policy object.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy addPolicy(Policy policy, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.addPolicy(policy, tenantId);
    }

    /**
     * Update an existing Policy.
     * Reads the persisted policy first to detect renames, then delegates the update, and finally
     * clears the canonical cache entry for the policy ID. If the policy name has changed, the old
     * name's index cache entry is also cleared; a plain content update leaves the index untouched.
     *
     * @param policy   Policy object with updated state.
     * @param tenantId Tenant ID.
     * @return Updated Policy object.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy updatePolicy(Policy policy, int tenantId) throws PolicyManagementException {

        Policy existingPolicy = policyManagementDAO.getPolicyById(policy.getId(), tenantId);
        Policy updatedPolicy = policyManagementDAO.updatePolicy(policy, tenantId);

        if (existingPolicy != null && !Objects.equals(existingPolicy.getName(), policy.getName())) {
            policyIdByNameCache.clearCacheEntry(new PolicyNameCacheKey(existingPolicy.getName()), tenantId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy name index cache cleared for old name: " + existingPolicy.getName()
                        + " on update.");
            }
        }
        policyCache.clearCacheEntry(new PolicyCacheKey(policy.getId()), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy cache cleared for ID: " + policy.getId() + " on update.");
        }
        return updatedPolicy;
    }

    /**
     * Delete a Policy.
     * Reads the persisted policy first to resolve its name, delegates the delete, then clears
     * both the canonical cache entry for the policy ID and the index cache entry for its name.
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public void deletePolicy(String policyId, int tenantId) throws PolicyManagementException {

        Policy existingPolicy = policyManagementDAO.getPolicyById(policyId, tenantId);
        policyManagementDAO.deletePolicy(policyId, tenantId);
        if (existingPolicy != null) {
            policyIdByNameCache.clearCacheEntry(new PolicyNameCacheKey(existingPolicy.getName()), tenantId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy name index cache cleared for name: " + existingPolicy.getName() + " on delete.");
            }
        }
        policyCache.clearCacheEntry(new PolicyCacheKey(policyId), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy cache cleared for ID: " + policyId + " on delete.");
        }
    }

    /**
     * Get a Policy by Policy ID.
     * This method first checks the canonical cache for the Policy object.
     * If the Policy object is not found in the cache, it invokes the data layer operation
     * and populates the canonical cache.
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @return Policy object, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy getPolicyById(String policyId, int tenantId) throws PolicyManagementException {

        PolicyCacheEntry cacheEntry = policyCache.getValueFromCache(new PolicyCacheKey(policyId), tenantId);
        if (cacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy cache hit for ID: " + policyId);
            }
            return cacheEntry.getPolicy();
        }
        Policy policy = policyManagementDAO.getPolicyById(policyId, tenantId);
        if (policy != null) {
            policyCache.addToCacheOnRead(new PolicyCacheKey(policyId), new PolicyCacheEntry(policy), tenantId);
        }
        return policy;
    }

    /**
     * Get a Policy by Policy name.
     * This method first checks the index cache for the policy ID mapped to the given name.
     * On an index hit, the full Policy is resolved through the canonical ID-based lookup.
     * On an index miss, it invokes the data layer operation and populates both the canonical
     * cache (by ID) and the index cache (name to ID).
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy object, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy getPolicyByName(String policyName, int tenantId) throws PolicyManagementException {

        PolicyIdCacheEntry idCacheEntry = policyIdByNameCache.getValueFromCache(
                new PolicyNameCacheKey(policyName), tenantId);
        if (idCacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy name index cache hit for name: " + policyName);
            }
            return getPolicyById(idCacheEntry.getPolicyId(), tenantId);
        }
        Policy policy = policyManagementDAO.getPolicyByName(policyName, tenantId);
        if (policy != null) {
            policyCache.addToCacheOnRead(new PolicyCacheKey(policy.getId()), new PolicyCacheEntry(policy), tenantId);
            policyIdByNameCache.addToCacheOnRead(
                    new PolicyNameCacheKey(policyName), new PolicyIdCacheEntry(policy.getId()), tenantId);
        }
        return policy;
    }

    /**
     * Get a page of Policy summaries for a tenant, optionally filtered by name.
     * Paginated lists are not cached; this directly invokes the data layer operation.
     *
     * @param tenantId Tenant ID.
     * @param filter   Name filter; {@code null} or blank means no filter.
     * @param offset   Zero-based start index.
     * @param limit    Maximum number of results.
     * @return List of policy summaries. Never {@code null}.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public List<PolicyBasicInfo> getPolicies(int tenantId, String filter, int offset, int limit)
            throws PolicyManagementException {

        return policyManagementDAO.getPolicies(tenantId, filter, offset, limit);
    }

    /**
     * Count Policies matching the given filter. Not cached; directly invokes the data layer.
     *
     * @param tenantId Tenant ID.
     * @param filter   Name filter; {@code null} or blank means count all.
     * @return Total number of matching policies.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public int getPolicyCount(int tenantId, String filter) throws PolicyManagementException {

        return policyManagementDAO.getPolicyCount(tenantId, filter);
    }

    /**
     * Get a Policy ID by Policy name.
     * Checks the index cache first and returns the cached ID on a hit, avoiding the DB.
     * On a miss, delegates to the underlying data layer's lightweight ID lookup and warms
     * the index cache with the result.
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy ID, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException {

        PolicyIdCacheEntry idCacheEntry = policyIdByNameCache.getValueFromCache(
                new PolicyNameCacheKey(policyName), tenantId);
        if (idCacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy name index cache hit for name: " + policyName + " while resolving policy ID.");
            }
            return idCacheEntry.getPolicyId();
        }
        String policyId = policyManagementDAO.getPolicyIdByName(policyName, tenantId);
        if (policyId != null) {
            policyIdByNameCache.addToCacheOnRead(
                    new PolicyNameCacheKey(policyName), new PolicyIdCacheEntry(policyId), tenantId);
        }
        return policyId;
    }
}
