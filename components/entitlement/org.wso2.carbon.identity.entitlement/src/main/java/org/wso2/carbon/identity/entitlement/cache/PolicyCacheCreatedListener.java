/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;

/**
 * Created for get update when the cache entry created by any node in the cluster
 */
public class PolicyCacheCreatedListener implements CacheEntryCreatedListener<IdentityCacheKey,PolicyStatus> {

    private static Log log = LogFactory.getLog(PolicyCacheCreatedListener.class);

    /**
     *
     * @param event The entry just added.
     * @throws CacheEntryListenerException
     */
    @Override
    public void entryCreated(CacheEntryEvent<? extends IdentityCacheKey, ? extends PolicyStatus> event) throws CacheEntryListenerException {
        if(log.isDebugEnabled()){
            log.debug("ConfigCacheCreatedListener triggered for tenant: " + event.getKey().getTenantId() + " and " +
                    "key : " + event.getKey()
                    .getKey());
        }
        PolicyCache.updateLocalPolicyCacheMap(event.getKey(),event.getValue());
    }

}
