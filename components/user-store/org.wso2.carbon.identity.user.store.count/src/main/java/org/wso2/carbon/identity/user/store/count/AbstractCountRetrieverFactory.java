/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.identity.user.store.count;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.store.count.cache.CountRetrieverCacheEntry;
import org.wso2.carbon.identity.user.store.count.cache.CountRetrieverCacheKey;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.RealmConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create instances of user store count retrievers as required.
 */
public abstract class AbstractCountRetrieverFactory {

    private static final Log log = LogFactory.getLog(AbstractCountRetrieverFactory.class);

    public AbstractUserStoreCountRetriever getCountRetriever(String userStoreDomainName,
            RealmConfiguration realmConfiguration) throws UserStoreCounterException {

        int tenantId = -1234;

        CountRetrieverCacheKey cacheKey = new CountRetrieverCacheKey(userStoreDomainName,
                realmConfiguration.getTenantId());
        //ToDO implement cache properly
        CountRetrieverCacheEntry entry = null;
        // entry = CountRetrieverCache.getInstance().getValueFromCache(cacheKey);

        if (entry != null) {
            if (log.isDebugEnabled()) {
                log.debug("Count Retriever cache HIT for " + userStoreDomainName + " of tenant:" + tenantId);
            }
            //                return entry.getProvisioningConnector();
        }

        AbstractUserStoreCountRetriever countRetriever;

        countRetriever = buildCountRetriever(realmConfiguration);
        entry = new CountRetrieverCacheEntry();
        entry.setUserStoreCountRetriever(countRetriever);
        //ToDo
        //            CountRetrieverCache.getInstance().addToCache(cacheKey, entry);

        return countRetriever;

    }

    /**
     * @param realmConfiguration
     * @return
     * @throws UserStoreCounterException
     */
    protected abstract AbstractUserStoreCountRetriever buildCountRetriever(RealmConfiguration realmConfiguration)
            throws UserStoreCounterException;

    public void destroyCountRetriever(String userStoreDomainName, String tenantDomain)
            throws UserStoreCounterException {

        int tenantId = -1234;

        CountRetrieverCacheKey cacheKey = new CountRetrieverCacheKey(userStoreDomainName, tenantId);
        //ToDO implement cache properly
        CountRetrieverCacheEntry entry = null;
        //entry = CountRetrieverCache.getInstance().getValueFromCache(cacheKey);

        if (entry != null) {
            //                CountRetrieverCache.getInstance().clearCacheEntry(cacheKey);

            if (log.isDebugEnabled()) {
                log.debug("User store count retriever cached entry removed for " + userStoreDomainName
                        + " from the count retriever " + getCounterType());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("User store count retriever cached entry not found for " + userStoreDomainName
                        + " from the connector " + getCounterType());
            }
        }

    }

    /**
     * @return
     */
    public List<String> getConfigurationProperties() {
        return new ArrayList<>();
    }

    /**
     * @return
     */
    public abstract String getCounterType();
}
