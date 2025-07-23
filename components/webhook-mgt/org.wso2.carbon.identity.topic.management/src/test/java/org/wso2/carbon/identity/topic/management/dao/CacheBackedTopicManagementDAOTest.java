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

package org.wso2.carbon.identity.topic.management.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCache;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCacheEntry;
import org.wso2.carbon.identity.topic.management.api.core.cache.TopicCacheKey;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.dao.impl.CacheBackedTopicManagementDAO;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedTopicManagementDAOTest {

    private TopicManagementDAO topicManagementDAO;
    private CacheBackedTopicManagementDAO cacheBackedTopicManagementDAO;
    private TopicCache topicCache;

    public static final String TOPIC = "https://example.com/events/carbon.super";
    public static final String CHANNEL_URI = "example.com/events";
    public static final String EVENT_PROFILE_VERSION = "v1";
    public static final int TENANT_ID = 1;

    @BeforeClass
    public void setUpClass() {

        topicCache = TopicCache.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        topicManagementDAO = mock(TopicManagementDAO.class);
        cacheBackedTopicManagementDAO = new CacheBackedTopicManagementDAO(topicManagementDAO);
        topicCache.clearCacheEntry(new TopicCacheKey(TOPIC), TENANT_ID);
    }

    @Test
    public void testAddTopic() throws TopicManagementException {

        cacheBackedTopicManagementDAO.addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
        verify(topicManagementDAO).addTopic(TOPIC, CHANNEL_URI, EVENT_PROFILE_VERSION, TENANT_ID);
        assertNull(topicCache.getValueFromCache(new TopicCacheKey(TOPIC), TENANT_ID));
    }

    @Test
    public void testDeleteTopic() throws TopicManagementException {

        topicCache.addToCache(new TopicCacheKey(TOPIC), new TopicCacheEntry(TOPIC), TENANT_ID);
        cacheBackedTopicManagementDAO.deleteTopic(TOPIC, TENANT_ID);
        verify(topicManagementDAO).deleteTopic(TOPIC, TENANT_ID);
        assertNull(topicCache.getValueFromCache(new TopicCacheKey(TOPIC), TENANT_ID));
    }

    @Test
    public void testDeleteTopicWhenCacheIsNotPopulated() throws TopicManagementException {

        cacheBackedTopicManagementDAO.deleteTopic(TOPIC, TENANT_ID);
        verify(topicManagementDAO).deleteTopic(TOPIC, TENANT_ID);
        assertNull(topicCache.getValueFromCache(new TopicCacheKey(TOPIC), TENANT_ID));
    }

    @Test
    public void testIsTopicExistsCacheHit() throws TopicManagementException {

        topicCache.addToCache(new TopicCacheKey(TOPIC), new TopicCacheEntry(TOPIC), TENANT_ID);
        boolean result = cacheBackedTopicManagementDAO.isTopicExists(TOPIC, TENANT_ID);
        assertTrue(result);
        verify(topicManagementDAO, never()).isTopicExists(TOPIC, TENANT_ID);
    }

    @Test
    public void testIsTopicExistsCacheMissAndExistsInDAO() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(true);
        boolean result = cacheBackedTopicManagementDAO.isTopicExists(TOPIC, TENANT_ID);
        assertTrue(result);
        verify(topicManagementDAO).isTopicExists(TOPIC, TENANT_ID);
        TopicCacheEntry cacheEntry = topicCache.getValueFromCache(new TopicCacheKey(TOPIC), TENANT_ID);
        assertNotNull(cacheEntry);
        assertEquals(cacheEntry.getTopic(), TOPIC);
    }

    @Test
    public void testIsTopicExistsCacheMissAndNotExistsInDAO() throws TopicManagementException {

        when(topicManagementDAO.isTopicExists(TOPIC, TENANT_ID)).thenReturn(false);
        boolean result = cacheBackedTopicManagementDAO.isTopicExists(TOPIC, TENANT_ID);
        assertFalse(result);
        verify(topicManagementDAO).isTopicExists(TOPIC, TENANT_ID);
        assertNull(topicCache.getValueFromCache(new TopicCacheKey(TOPIC), TENANT_ID));
    }
}
