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


        //ToDO implement to get from memory map

//        if (entry != null) {
//            if (log.isDebugEnabled()) {
//                log.debug("Count Retriever cache HIT for " + userStoreDomainName + " of tenant:" + realmConfiguration.getTenantId());
//            }
//        }

        AbstractUserStoreCountRetriever countRetriever;

        countRetriever = buildCountRetriever(realmConfiguration);
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


        //ToDO implement to read from in-memory map

//        if (entry == null) {
//
//            if (log.isDebugEnabled()) {
//                log.debug("User store count retriever in memory entry removed for " + userStoreDomainName
//                        + " from the count retriever " + getCounterType());
//            }
//        } else {
//            if (log.isDebugEnabled()) {
//                log.debug("User store count retriever in memory entry not found for " + userStoreDomainName
//                        + " from the count retriever " + getCounterType());
//            }
//        }

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
