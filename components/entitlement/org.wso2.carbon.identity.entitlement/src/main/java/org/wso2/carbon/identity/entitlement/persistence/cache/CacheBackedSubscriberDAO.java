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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.cache.SubscriberCache;
import org.wso2.carbon.identity.entitlement.cache.SubscriberIdListCache;
import org.wso2.carbon.identity.entitlement.dto.PublisherDataHolder;
import org.wso2.carbon.identity.entitlement.dto.PublisherPropertyDTO;
import org.wso2.carbon.identity.entitlement.persistence.dao.SubscriberDAO;

import java.util.ArrayList;
import java.util.List;

public class CacheBackedSubscriberDAO extends SubscriberDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedSubscriberDAO.class);
    private final SubscriberCache subscriberCache = SubscriberCache.getInstance();
    private final SubscriberIdListCache subscriberIdListCache = SubscriberIdListCache.getInstance();
    private static final String SUBSCRIBER_ID_LIST_CACHE_KEY = "SUBSCRIBER_ID_LIST_CACHE_KEY";
    private static final CacheBackedSubscriberDAO instance = new CacheBackedSubscriberDAO();

    private CacheBackedSubscriberDAO() {

    }

    public static CacheBackedSubscriberDAO getInstance() {

        return instance;
    }

    @Override
    public PublisherDataHolder getSubscriber(String subscriberId, int tenantId) throws EntitlementException {

        PublisherDataHolder subscriber = subscriberCache.getValueFromCache(subscriberId, tenantId);
        if (subscriber != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in SubscriberCache for subscriber: %s for tenant: %s",
                        subscriberId, tenantId));
            }
            return subscriber;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in SubscriberCache for subscriber: %s for tenant: %s",
                    subscriberId, tenantId));
        }
        subscriber = super.getSubscriber(subscriberId, tenantId);
        subscriberCache.addToCache(subscriberId, subscriber, tenantId);
        return subscriber;
    }

    @Override
    public List<String> getSubscriberIds(int tenantId) throws EntitlementException {

        List<String> subscriberIds = subscriberIdListCache.getValueFromCache(SUBSCRIBER_ID_LIST_CACHE_KEY, tenantId);
        if (subscriberIds != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Cache hit in SubscriberIdListCache for subscriber ids for tenant: %s",
                        tenantId));
            }
            return subscriberIds;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Cache miss in SubscriberIdListCache for subscriber ids for tenant: %s", tenantId));
        }
        subscriberIds = super.getSubscriberIds(tenantId);
        subscriberIdListCache.addToCache(SUBSCRIBER_ID_LIST_CACHE_KEY, (ArrayList<String>) subscriberIds, tenantId);
        return subscriberIds;
    }

    @Override
    public void insertSubscriber(String subscriberId, PublisherDataHolder holder, int tenantId)
            throws EntitlementException {

        super.insertSubscriber(subscriberId, holder, tenantId);
        subscriberCache.addToCache(subscriberId, holder, tenantId);
        subscriberIdListCache.clearCacheEntry(SUBSCRIBER_ID_LIST_CACHE_KEY, tenantId);
    }

    @Override
    public void updateSubscriber(String subscriberId, String updatedModuleName,
                                 PublisherPropertyDTO[] updatedPropertyDTOS, int tenantId)
            throws EntitlementException {

        super.updateSubscriber(subscriberId, updatedModuleName, updatedPropertyDTOS, tenantId);
        subscriberCache.clearCacheEntry(subscriberId, tenantId);
    }

    @Override
    public void deleteSubscriber(String subscriberId, int tenantId) throws EntitlementException {

        super.deleteSubscriber(subscriberId, tenantId);
        subscriberCache.clearCacheEntry(subscriberId, tenantId);
        subscriberIdListCache.clearCacheEntry(SUBSCRIBER_ID_LIST_CACHE_KEY, tenantId);
    }
}
