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
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCache;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheEntry;
import org.wso2.carbon.identity.policy.management.internal.cache.PolicyCacheKey;
import org.wso2.carbon.identity.policy.management.internal.dao.PolicyManagementDAO;

import java.util.List;

/**
 * Cache-backed Policy Management DAO.
 * Wraps PolicyManagementDAOFacade with an in-process cache to reduce DB hits on the hot path.
 * Hot path: getPolicyByName (called on every device evaluation) is served from cache after first read.
 * Cache is invalidated on add, update, and delete.
 * Both name-based and ID-based entries are maintained so either lookup type can benefit.
 */
public class CacheBackedPolicyManagementDAO implements PolicyManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedPolicyManagementDAO.class);

    private final PolicyManagementDAO policyManagementDAO;
    private final PolicyCache policyCache;

    public CacheBackedPolicyManagementDAO(PolicyManagementDAO policyManagementDAO) {

        this.policyManagementDAO = policyManagementDAO;
        policyCache = PolicyCache.getInstance();
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
     * This method clears both the ID-based and name-based cache entries upon policy update.
     * If the policy name has changed, the old name's cache entry is also cleared.
     *
     * @param policy   Policy object with updated state.
     * @param tenantId Tenant ID.
     * @return Updated Policy object.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy updatePolicy(Policy policy, int tenantId) throws PolicyManagementException {

        // Read old name from cache before clearing so the name-based entry can also be invalidated.
        PolicyCacheEntry existingEntry = policyCache.getValueFromCache(
                PolicyCacheKey.forId(policy.getId()), tenantId);
        if (existingEntry != null) {
            String oldName = existingEntry.getPolicy().getName();
            if (!oldName.equals(policy.getName())) {
                policyCache.clearCacheEntry(PolicyCacheKey.forName(oldName), tenantId);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Policy cache cleared for old name: " + oldName + " on update.");
                }
            }
        }
        policyCache.clearCacheEntry(PolicyCacheKey.forId(policy.getId()), tenantId);
        policyCache.clearCacheEntry(PolicyCacheKey.forName(policy.getName()), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy cache cleared for ID: " + policy.getId()
                    + " and name: " + policy.getName() + " on update.");
        }
        return policyManagementDAO.updatePolicy(policy, tenantId);
    }

    /**
     * Delete a Policy.
     * This method clears both the ID-based and name-based cache entries upon policy deletion.
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public void deletePolicy(String policyId, int tenantId) throws PolicyManagementException {

        // Read name from cache before clearing so the name-based entry can also be invalidated.
        PolicyCacheEntry existingEntry = policyCache.getValueFromCache(
                PolicyCacheKey.forId(policyId), tenantId);
        if (existingEntry != null) {
            policyCache.clearCacheEntry(
                    PolicyCacheKey.forName(existingEntry.getPolicy().getName()), tenantId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy cache cleared for name: "
                        + existingEntry.getPolicy().getName() + " on delete.");
            }
        }
        policyCache.clearCacheEntry(PolicyCacheKey.forId(policyId), tenantId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Policy cache cleared for ID: " + policyId + " on delete.");
        }
        policyManagementDAO.deletePolicy(policyId, tenantId);
    }

    /**
     * Get a Policy by Policy ID.
     * This method first checks the cache for the Policy object.
     * If the Policy object is not found in the cache, it invokes the data layer operation to get the Policy.
     *
     * @param policyId Policy ID.
     * @param tenantId Tenant ID.
     * @return Policy object, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy getPolicyById(String policyId, int tenantId) throws PolicyManagementException {

        PolicyCacheEntry cacheEntry = policyCache.getValueFromCache(
                PolicyCacheKey.forId(policyId), tenantId);
        if (cacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy cache hit for ID: " + policyId);
            }
            return cacheEntry.getPolicy();
        }
        Policy policy = policyManagementDAO.getPolicyById(policyId, tenantId);
        if (policy != null) {
            policyCache.addToCacheOnRead(
                    PolicyCacheKey.forId(policyId), new PolicyCacheEntry(policy), tenantId);
        }
        return policy;
    }

    /**
     * Get a Policy by Policy name.
     * This method first checks the cache for the Policy object.
     * If the Policy object is not found in the cache, it invokes the data layer operation to get the Policy.
     * On a cache miss, both the name-based and ID-based cache entries are populated.
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy object, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public Policy getPolicyByName(String policyName, int tenantId) throws PolicyManagementException {

        PolicyCacheEntry cacheEntry = policyCache.getValueFromCache(
                PolicyCacheKey.forName(policyName), tenantId);
        if (cacheEntry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Policy cache hit for name: " + policyName);
            }
            return cacheEntry.getPolicy();
        }
        Policy policy = policyManagementDAO.getPolicyByName(policyName, tenantId);
        if (policy != null) {
            // Populate both name-based and ID-based entries so either lookup type benefits.
            policyCache.addToCacheOnRead(
                    PolicyCacheKey.forName(policyName), new PolicyCacheEntry(policy), tenantId);
            policyCache.addToCacheOnRead(
                    PolicyCacheKey.forId(policy.getId()), new PolicyCacheEntry(policy), tenantId);
        }
        return policy;
    }

    /**
     * Get all Policies for a tenant.
     * This method directly invokes the data layer operation without caching.
     *
     * @param tenantId Tenant ID.
     * @return List of Policy objects. Never {@code null}.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public List<Policy> getPolicies(int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicies(tenantId);
    }

    /**
     * Get a Policy ID by Policy name.
     * This method directly invokes the data layer operation without caching.
     *
     * @param policyName Policy name.
     * @param tenantId   Tenant ID.
     * @return Policy ID, or {@code null} if not found.
     * @throws PolicyManagementException Policy Management Exception.
     */
    @Override
    public String getPolicyIdByName(String policyName, int tenantId) throws PolicyManagementException {

        return policyManagementDAO.getPolicyIdByName(policyName, tenantId);
    }
}
