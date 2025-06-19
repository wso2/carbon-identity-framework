/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.topic.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCache;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCacheEntry;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCacheKey;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;

/**
 * Cache backed Topic Management DAO.
 * This class is used to implement the caching on top of the data layer operations.
 * This caches the Topic string.
 */
public class CacheBackedTopicManagementDAO implements TopicManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedTopicManagementDAO.class);

    private final TopicManagementDAO topicManagementDAO;
    private final TopicCache topicCache;

    public CacheBackedTopicManagementDAO(TopicManagementDAO topicManagementDAO) {

        this.topicManagementDAO = topicManagementDAO;
        this.topicCache = TopicCache.getInstance();
    }

    /**
     * Add a new topic.
     * This method directly invokes the data layer operation to add the topic.
     *
     * @param topic               Topic to be added.
     * @param channelUri          The channel URI associated with the topic.
     * @param eventProfileVersion The version of the event profile.
     * @param tenantId            Tenant ID.
     * @throws TopicManagementException If an error occurs while adding the topic.
     */
    @Override
    public void addTopic(String topic, String channelUri, String eventProfileVersion, int tenantId)
            throws TopicManagementException {

        topicManagementDAO.addTopic(topic, channelUri, eventProfileVersion, tenantId);
    }

    /**
     * Delete a topic.
     * This method clears the cache entry upon topic deletion.
     *
     * @param topic    Topic to be deleted.
     * @param tenantId Tenant ID.
     * @throws TopicManagementException If an error occurs while deleting the topic.
     */
    @Override
    public void deleteTopic(String topic, int tenantId) throws TopicManagementException {

        topicCache.clearCacheEntry(new TopicCacheKey(topic), tenantId);
        LOG.debug("Topic cache entry is cleared for topic: " + topic + " for topic deletion.");
        topicManagementDAO.deleteTopic(topic, tenantId);
    }

    /**
     * Check if a topic exists.
     * This method first checks the cache for the topic.
     * If the topic is not found in the cache, it invokes the data layer operation to check if the topic exists.
     *
     * @param topic    Topic to check.
     * @param tenantId Tenant ID.
     * @return True if the topic exists, false otherwise.
     * @throws TopicManagementException If an error occurs while checking if the topic exists.
     */
    @Override
    public boolean isTopicExists(String topic, int tenantId) throws TopicManagementException {

        TopicCacheKey cacheKey = new TopicCacheKey(topic);
        TopicCacheEntry cacheEntry = topicCache.getValueFromCache(cacheKey, tenantId);

        if (cacheEntry != null) {
            LOG.debug(
                    "Topic cache hit for topic: " + topic + " for tenant ID: " + tenantId +
                            ". Returning from cache.");
            return true;
        }

        LOG.debug("Topic cache miss for topic: " + topic + " for tenant ID: " + tenantId + ". Checking DB.");
        boolean exists = topicManagementDAO.isTopicExists(topic, tenantId);

        if (exists) {
            LOG.debug("Topic added to cache after checking existence: " + topic + " for tenant ID: " + tenantId);
            topicCache.addToCache(cacheKey, new TopicCacheEntry(topic), tenantId);
        }

        return exists;
    }
}
