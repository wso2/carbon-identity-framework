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

package org.wso2.carbon.identity.webhook.metadata.dao;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCache;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCacheEntry;
import org.wso2.carbon.identity.webhook.metadata.api.core.cache.WebhookMetadataCacheKey;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.impl.CacheBackedWebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class CacheBackedWebhookMetadataDAOTest {

    private WebhookMetadataDAO webhookMetadataDAO;
    private CacheBackedWebhookMetadataDAO cacheBackedWebhookMetadataDAO;
    private WebhookMetadataCache webhookMetadataCache;

    private static final int TENANT_ID = 1;

    @BeforeClass
    public void setUpClass() {

        webhookMetadataCache = WebhookMetadataCache.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        webhookMetadataDAO = mock(WebhookMetadataDAO.class);
        cacheBackedWebhookMetadataDAO = new CacheBackedWebhookMetadataDAO(webhookMetadataDAO);
        webhookMetadataCache.clear(TENANT_ID);
    }

    @Test
    public void testGetWebhookMetadataProperties_CacheHit() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        properties.put("prop", mock(WebhookMetadataProperty.class));
        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(TENANT_ID);
        webhookMetadataCache.addToCache(cacheKey, new WebhookMetadataCacheEntry(properties), TENANT_ID);

        Map<String, WebhookMetadataProperty> result =
                cacheBackedWebhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID);

        assertEquals(result, properties);
        verify(webhookMetadataDAO, never()).getWebhookMetadataProperties(TENANT_ID);
    }

    @Test
    public void testGetWebhookMetadataProperties_CacheMiss() throws WebhookMetadataException {

        webhookMetadataCache.clear(TENANT_ID);
        Map<String, WebhookMetadataProperty> properties = new HashMap<>();
        properties.put("prop", mock(WebhookMetadataProperty.class));
        when(webhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID)).thenReturn(properties);

        Map<String, WebhookMetadataProperty> result =
                cacheBackedWebhookMetadataDAO.getWebhookMetadataProperties(TENANT_ID);

        assertEquals(result, properties);
        verify(webhookMetadataDAO).getWebhookMetadataProperties(TENANT_ID);
        assertEquals(webhookMetadataCache.getValueFromCache(new WebhookMetadataCacheKey(TENANT_ID), TENANT_ID)
                .getWebhookMetadataProperties(), properties);
    }

    @Test
    public void testUpdateWebhookMetadataProperties_ClearsCache() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = Collections.emptyMap();
        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(TENANT_ID);
        webhookMetadataCache.addToCache(cacheKey, new WebhookMetadataCacheEntry(properties), TENANT_ID);

        cacheBackedWebhookMetadataDAO.updateWebhookMetadataProperties(properties, TENANT_ID);

        verify(webhookMetadataDAO).updateWebhookMetadataProperties(properties, TENANT_ID);
        assertNull(webhookMetadataCache.getValueFromCache(cacheKey, TENANT_ID));
    }

    @Test
    public void testAddWebhookMetadataProperties_ClearsCache() throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> properties = Collections.emptyMap();
        WebhookMetadataCacheKey cacheKey = new WebhookMetadataCacheKey(TENANT_ID);
        webhookMetadataCache.addToCache(cacheKey, new WebhookMetadataCacheEntry(properties), TENANT_ID);

        cacheBackedWebhookMetadataDAO.addWebhookMetadataProperties(properties, TENANT_ID);

        verify(webhookMetadataDAO).addWebhookMetadataProperties(properties, TENANT_ID);
        assertNull(webhookMetadataCache.getValueFromCache(cacheKey, TENANT_ID));
    }
}
