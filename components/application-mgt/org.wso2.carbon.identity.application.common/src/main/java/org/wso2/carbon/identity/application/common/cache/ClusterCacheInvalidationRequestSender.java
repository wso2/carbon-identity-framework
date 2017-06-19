/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.identity.application.common.cache;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.internal.DataHolder;
import org.wso2.carbon.identity.application.common.listener.AbstractCacheListener;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * Listens for cache entry removals and updates and sends a cache invalidation request
 * to the other members in the cluster.
 *
 * This is feature intended only when separate local caches are maintained by each node
 * in the cluster.
 *
 */
public class ClusterCacheInvalidationRequestSender extends AbstractCacheListener implements
        CacheEntryRemovedListener, CacheEntryUpdatedListener {

    private static final Log log = LogFactory.getLog(ClusterCacheInvalidationRequestSender.class);

    @Override
    public void entryRemoved(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {
        execute(cacheEntryEvent);
    }

    @Override
    public void entryUpdated(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {
        execute(cacheEntryEvent);
    }

    /**
     * We will invalidate the particular cache in other nodes whenever
     * there is an remove/update of the local cache in the current node.
     */
    private void execute(CacheEntryEvent cacheEntryEvent) {
        if (getClusteringAgent() != null) {
            int numberOfRetries = 0;
            boolean isOriginatedFromAnotherNode = CacheInvalidationThreadLocal
                    .isOriginatedFromAnotherNode();
            if (!isOriginatedFromAnotherNode) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Sending cache invalidation message to other cluster nodes for " + cacheEntryEvent.getKey()
                                    + " of the cache " + cacheEntryEvent.getSource().getName()
                                    + " of the cache manager " + cacheEntryEvent.getSource().getCacheManager()
                                    .getName());
                }

                //Send the cluster message
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
                ClusterCacheInvalidationRequest.CacheInfo cacheInfo = new ClusterCacheInvalidationRequest.CacheInfo(
                        cacheEntryEvent.getSource().getCacheManager().getName(),
                        cacheEntryEvent.getSource().getName(),
                        cacheEntryEvent.getKey());

                ClusterCacheInvalidationRequest clusterCacheInvalidationRequest = new ClusterCacheInvalidationRequest(
                        cacheInfo, tenantDomain, tenantId);

                while (numberOfRetries < 60) {
                    try {
                        getClusteringAgent().sendMessage(clusterCacheInvalidationRequest, true);
                        log.debug("Sent [" + clusterCacheInvalidationRequest + "]");
                        break;
                    } catch (ClusteringFault e) {
                        numberOfRetries++;
                        if (numberOfRetries < 60) {
                            log.warn("Could not send CacheInvalidationMessage for tenant " +
                                    tenantId + ". Retry will be attempted in 2s. Request: " +
                                    clusterCacheInvalidationRequest, e);
                        } else {
                            log.error("Could not send CacheInvalidationMessage for tenant " +
                                    tenantId + ". Several retries failed. Request:" + clusterCacheInvalidationRequest, e);
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
    }

    private ClusteringAgent getClusteringAgent() {
        return DataHolder.getInstance().getClusteringAgent();
    }

}
