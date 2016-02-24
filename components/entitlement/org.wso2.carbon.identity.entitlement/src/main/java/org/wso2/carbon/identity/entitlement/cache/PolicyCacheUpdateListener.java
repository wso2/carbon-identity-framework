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

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * Created for get update when the cache entry update by any node in the cluster
 */
public class PolicyCacheUpdateListener implements CacheEntryUpdatedListener<IdentityCacheKey,PolicyStatus>{

    private static Log log = LogFactory.getLog(PolicyCacheUpdateListener.class);


    /**
     *
     * @param event The event just updated.
     * @throws CacheEntryListenerException
     */
    @Override
    public void entryUpdated(CacheEntryEvent<? extends IdentityCacheKey, ? extends PolicyStatus> event) throws CacheEntryListenerException {
        if(event!=null) {
            PolicyCache.updateLocalPolicyCacheMap(event.getKey(), event.getValue());
        }
    }


}
