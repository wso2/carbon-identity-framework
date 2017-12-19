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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.PDPConstants;


/**
 * Decision cache to handle request against response cache within the cluster.
 */
public class DecisionCache extends EntitlementBaseCache<IdentityCacheKey, Object> {

    private static Log log = LogFactory.getLog(DecisionCache.class);

    public DecisionCache(int timeOut) {
        super(PDPConstants.PDP_DECISION_CACHE, timeOut);
    }

    /**
     * Can add decision to the cluster with key
     *
     * @param key
     * @param decision
     */
    public void addToCache(String key, Object decision) {
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            log.debug("DecisionCache is added for tenant : " + tenantDomain + "  tenantId : " + tenantId + " " +
                      "cache key : " + key + " cache value : " + decision);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);
        addToCache(cacheKey, decision);

    }

    /**
     * Can get the decision for the request if it available in the cache.
     *
     * @param key
     * @return
     */
    public Object getFromCache(String key) {

        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            log.debug("DecisionCache is get for tenant : " + tenantDomain + "  tenantId : " + tenantId + " " +
                      "cache key : " + key);
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, key);


        Object entry = getValueFromCache(cacheKey);
        if (entry != null) {
            return entry;
        }

        return null;
    }

    /**
     * Clear all decision cache
     */
    public void clearCache() {
        if (log.isDebugEnabled()) {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            log.debug("DecisionCache clear all cache from the cluster and tenant domain " + tenantDomain);
        }
        clear();
    }

}
