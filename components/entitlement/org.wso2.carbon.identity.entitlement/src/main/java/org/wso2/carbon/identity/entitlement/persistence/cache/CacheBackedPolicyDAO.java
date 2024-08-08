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

package org.wso2.carbon.identity.entitlement.persistence.cache;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.cache.PapPolicyCache;
import org.wso2.carbon.identity.entitlement.cache.PapPolicyListCache;
import org.wso2.carbon.identity.entitlement.cache.PdpPolicyCache;
import org.wso2.carbon.identity.entitlement.cache.PdpPolicyListCache;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.dto.PolicyStoreDTO;
import org.wso2.carbon.identity.entitlement.persistence.dao.PolicyDAO;

import java.util.ArrayList;
import java.util.List;

public class CacheBackedPolicyDAO extends PolicyDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedPolicyDAO.class);
    private final PapPolicyCache papPolicyCache = PapPolicyCache.getInstance();
    private final PapPolicyListCache papPolicyListCache = PapPolicyListCache.getInstance();
    private final PdpPolicyCache pdpPolicyCache = PdpPolicyCache.getInstance();
    private final PdpPolicyListCache pdpPolicyListCache = PdpPolicyListCache.getInstance();
    private static final String PAP_POLICY_LIST_CACHE_KEY = "PAP_POLICY_LIST_CACHE_KEY";
    private static final String PDP_POLICY_LIST_CACHE_KEY = "PDP_POLICY_LIST_CACHE_KEY";

    @Override
    public void insertPolicy(PolicyDTO policy, int tenantId) throws EntitlementException {

        super.insertPolicy(policy, tenantId);
        papPolicyCache.addToCache(policy.getPolicyId(), policy, tenantId);
        papPolicyListCache.clearCacheEntry(PAP_POLICY_LIST_CACHE_KEY, tenantId);
    }

    @Override
    public PolicyDTO getPAPPolicy(String policyId, int tenantId) throws EntitlementException {

        PolicyDTO policy = papPolicyCache.getValueFromCache(policyId, tenantId);
        if (policy != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in PapPolicyCache for policy: %s for tenant: %s",
                        policyId, tenantId));
            }
            return policy;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in PapPolicyCache for policy: %s for tenant: %s", policyId, tenantId));
        }
        policy = super.getPAPPolicy(policyId, tenantId);
        papPolicyCache.addToCache(policyId, policy, tenantId);
        return policy;
    }

    @Override
    public List<PolicyDTO> getAllPAPPolicies(int tenantId) throws EntitlementException {

        List<PolicyDTO> policies = papPolicyListCache.getValueFromCache(PAP_POLICY_LIST_CACHE_KEY, tenantId);
        if (policies != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in PapPolicyListCache for policies for tenant: %s", tenantId));
            }
            return policies;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in PapPolicyListCache for policies for tenant: %s", tenantId));
        }
        policies = super.getAllPAPPolicies(tenantId);
        papPolicyListCache.addToCache(PAP_POLICY_LIST_CACHE_KEY, (ArrayList<PolicyDTO>) policies, tenantId);
        return policies;
    }

    @Override
    public void deletePAPPolicy(String policyId, int tenantId) throws EntitlementException {

        super.deletePAPPolicy(policyId, tenantId);
        papPolicyCache.clearCacheEntry(policyId, tenantId);
        papPolicyListCache.clearCacheEntry(PAP_POLICY_LIST_CACHE_KEY, tenantId);
    }

    @Override
    public PolicyStoreDTO getPDPPolicy(String policyId, int tenantId) {

        PolicyStoreDTO policy = pdpPolicyCache.getValueFromCache(policyId, tenantId);
        if (policy != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in PdpPolicyCache for policy: %s for tenant: %s",
                        policyId, tenantId));
            }
            return policy;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in PdpPolicyCache for policy: %s for tenant: %s", policyId, tenantId));
        }
        policy = super.getPDPPolicy(policyId, tenantId);
        pdpPolicyCache.addToCache(policyId, policy, tenantId);
        return policy;
    }

    @Override
    public PolicyStoreDTO[] getAllPDPPolicies(int tenantId) throws EntitlementException {

        PolicyStoreDTO[] policies = pdpPolicyListCache.getValueFromCache(PDP_POLICY_LIST_CACHE_KEY, tenantId);
        if (policies != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in PdpPolicyListCache for policies for tenant: %s", tenantId));
            }
            return policies;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in PdpPolicyListCache for policies for tenant: %s", tenantId));
        }
        policies = super.getAllPDPPolicies(tenantId);
        pdpPolicyListCache.addToCache(PDP_POLICY_LIST_CACHE_KEY, policies, tenantId);
        return policies;
    }

    @Override
    public void insertOrUpdatePolicy(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

        super.insertOrUpdatePolicy(policy, tenantId);
        pdpPolicyCache.addToCache(policy.getPolicyId(), policy, tenantId);
        pdpPolicyListCache.clearCacheEntry(PDP_POLICY_LIST_CACHE_KEY, tenantId);
    }

    @Override
    public void updateActiveStatusAndOrder(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

        super.updateActiveStatusAndOrder(policy, tenantId);
        pdpPolicyCache.clearCacheEntry(policy.getPolicyId(), tenantId);
        pdpPolicyListCache.clearCacheEntry(PDP_POLICY_LIST_CACHE_KEY, tenantId);
        papPolicyCache.clearCacheEntry(policy.getPolicyId(), tenantId);
        papPolicyListCache.clearCacheEntry(PAP_POLICY_LIST_CACHE_KEY, tenantId);
    }

    @Override
    public int getPublishedVersion(PolicyStoreDTO policy, int tenantId) throws EntitlementException {

        String policyId = policy.getPolicyId();
        PolicyStoreDTO cachedPolicy = pdpPolicyCache.getValueFromCache(policyId, tenantId);
        if (cachedPolicy != null && StringUtils.isNotBlank(cachedPolicy.getVersion())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in PdpPolicyCache for policy: %s for tenant: %s",
                        policyId, tenantId));
            }
            return Integer.parseInt(cachedPolicy.getVersion());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in PdpPolicyCache for policy: %s for tenant: %s", policyId, tenantId));
        }
        return super.getPublishedVersion(policy, tenantId);
    }

    @Override
    public boolean unpublishPolicy(String policyId, int tenantId) {

        boolean isSuccess = super.unpublishPolicy(policyId, tenantId);
        pdpPolicyCache.clearCacheEntry(policyId, tenantId);
        pdpPolicyListCache.clearCacheEntry(PDP_POLICY_LIST_CACHE_KEY, tenantId);
        return isSuccess;
    }
}
