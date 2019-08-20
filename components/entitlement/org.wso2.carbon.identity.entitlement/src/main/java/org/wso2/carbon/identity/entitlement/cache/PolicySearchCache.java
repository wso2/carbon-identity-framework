/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.identity.entitlement.cache;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;
import org.wso2.carbon.identity.entitlement.policy.search.PolicySearchCacheInvalidationClusteringMessage;
import org.wso2.carbon.identity.entitlement.policy.search.SearchResult;

/**
 *
 */
public class PolicySearchCache extends EntitlementBaseCache<IdentityCacheKey, SearchResult> {

    private static final Log log = LogFactory.getLog(PolicySearchCache.class);

    public PolicySearchCache(int timeOut) {
        super(CachingConstants.LOCAL_CACHE_PREFIX + PDPConstants.POLICY_SEARCH_CACHE, timeOut);
    }


    public void addToCache(String key, SearchResult result) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        addToCache(cacheKey, result);
    }

    public SearchResult getFromCache(String key) {

        SearchResult searchResult = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        Object entry = getValueFromCache(cacheKey);
        if (entry != null) {
            searchResult = (SearchResult) entry;
        }

        return searchResult;
    }

    public void clearCache() {
        clear();
    }

    /**
     * Invalidate {@link PolicySearchCache}. It will send the cluster message to clean the {@link PolicySearchCache}
     * in all the nodes.
     */
    public void invalidateCache() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Trigger invalidate policy search cache to tenant :  " + IdentityTenantUtil.getTenantDomain(tenantId));
        }

        // Update local policy search cache of this node.
        clearCache();

        // Send out a cluster message to notify other nodes.
        if (isClusteringEnabled()) {
            sendClusterMessage(new PolicySearchCacheInvalidationClusteringMessage(tenantId), true);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Clustering not enabled. Not sending cluster message to other nodes.");
            }
        }
    }

    /**
     * Send out policy status change notification to other nodes.
     *
     * @param clusterMessage
     * @param isSync
     */
    private void sendClusterMessage(ClusteringMessage clusterMessage, boolean isSync) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Sending cluster message to all other nodes");
            }
            ClusteringAgent clusteringAgent = EntitlementConfigHolder.getInstance().getConfigurationContextService()
                    .getServerConfigContext().getAxisConfiguration().getClusteringAgent();
            if (clusteringAgent != null) {
                clusteringAgent.sendMessage(clusterMessage, isSync);
            } else {
                log.error("Clustering Agent not available.");
            }
        } catch (ClusteringFault clusteringFault) {
            log.error("Error while sending cluster message", clusteringFault);
        }
    }

    /**
     * Check whether clustering is enabled.
     *
     * @return boolean returns true if clustering enabled, false otherwise.
     */
    private boolean isClusteringEnabled() {

        return EntitlementConfigHolder.getInstance().getConfigurationContextService()
                .getServerConfigContext().getAxisConfiguration().getClusteringAgent() != null;

    }
}
