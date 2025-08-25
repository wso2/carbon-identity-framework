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

package org.wso2.carbon.identity.webhook.management.dao;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCacheEntry;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCacheKey;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCache;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCacheEntry;
import org.wso2.carbon.identity.webhook.management.api.core.cache.WebhookCacheKey;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class CacheBackedWebhookManagementDAOTest {

    private WebhookManagementDAO webhookManagementDAO;
    private CacheBackedWebhookManagementDAO cacheBackedWebhookManagementDAO;
    private WebhookCache webhookCache;

    public static final String WEBHOOK_ID = "webhookId";
    public static final int TENANT_ID = 1;

    @BeforeClass
    public void setUpClass() {

        webhookCache = WebhookCache.getInstance();
    }

    @BeforeMethod
    public void setUp() {

        webhookManagementDAO = mock(WebhookManagementDAO.class);
        cacheBackedWebhookManagementDAO = new CacheBackedWebhookManagementDAO(webhookManagementDAO);
    }

    @Test
    public void testAddWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        cacheBackedWebhookManagementDAO.createWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO).createWebhook(webhook, TENANT_ID);
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        cacheBackedWebhookManagementDAO.updateWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO).updateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testUpdateWebhookWhenCacheIsPopulated() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        cacheBackedWebhookManagementDAO.updateWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).updateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        cacheBackedWebhookManagementDAO.deleteWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testDeleteWebhookWhenCacheIsPopulated() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        cacheBackedWebhookManagementDAO.deleteWebhook(WEBHOOK_ID, TENANT_ID);

        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testGetWebhookByWebhookIdCacheHit() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        Webhook result = cacheBackedWebhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, webhook);
        verify(webhookManagementDAO, never()).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhookByWebhookIdCacheMiss() throws WebhookMgtException {

        webhookCache.clear(TENANT_ID);

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        Webhook result = cacheBackedWebhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID);

        assertEquals(result, webhook);
        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(webhook, webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID).getWebhook());
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);

        cacheBackedWebhookManagementDAO.activateWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).activateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testActivateWebhookWhenCacheIsPopulated() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        cacheBackedWebhookManagementDAO.activateWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).activateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);

        cacheBackedWebhookManagementDAO.deactivateWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).deactivateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testDeactivateWebhookWhenCacheIsPopulated() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        cacheBackedWebhookManagementDAO.deactivateWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).deactivateWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testGetWebhookEvents_CacheHit() throws WebhookMgtException {

        List<Subscription> events = Arrays.asList(mock(Subscription.class), mock(Subscription.class));
        Webhook webhook = mock(Webhook.class);
        when(webhook.getEventsSubscribed()).thenReturn(events);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        List<Subscription> result = cacheBackedWebhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID);

        assertEquals(result, events);
        verify(webhookManagementDAO, never()).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhookEvents_CacheMiss() throws WebhookMgtException {

        webhookCache.clear(TENANT_ID);
        List<Subscription> events = Arrays.asList(mock(Subscription.class), mock(Subscription.class));
        Webhook webhook = mock(Webhook.class);
        when(webhook.getEventsSubscribed()).thenReturn(events);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        List<Subscription> result = cacheBackedWebhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID);

        assertEquals(result, events);
        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(
                webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID).getWebhook()
                        .getEventsSubscribed(),
                events
        );
    }

    @Test
    public void testGetWebhookEvents_EmptyListWhenNoWebhook() throws WebhookMgtException {

        webhookCache.clear(TENANT_ID);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        List<Subscription> result = cacheBackedWebhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID);

        assertEquals(result, Collections.emptyList());
    }

    @Test
    public void testRetryWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getId()).thenReturn(WEBHOOK_ID);
        WebhookCacheEntry cacheEntry = new WebhookCacheEntry(webhook);
        webhookCache.addToCache(new WebhookCacheKey(WEBHOOK_ID), cacheEntry, TENANT_ID);

        cacheBackedWebhookManagementDAO.retryWebhook(webhook, TENANT_ID);

        verify(webhookManagementDAO).retryWebhook(webhook, TENANT_ID);
        assertNull(webhookCache.getValueFromCache(new WebhookCacheKey(WEBHOOK_ID), TENANT_ID));
    }

    @Test
    public void testGetWebhooksCount() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhooksCount(TENANT_ID)).thenReturn(5);

        int count = cacheBackedWebhookManagementDAO.getWebhooksCount(TENANT_ID);

        verify(webhookManagementDAO).getWebhooksCount(TENANT_ID);
        assertEquals(count, 5);
    }

    /**
     * Test getActiveWebhooks returns from cache when cache hit occurs.
     */
    @Test
    public void testGetActiveWebhooks_CacheHit() throws WebhookMgtException {

        String eventProfileName = "profile";
        String eventProfileVersion = "v1";
        String channelUri = "http://channel";
        int tenantId = TENANT_ID;
        List<Webhook> webhooks = Arrays.asList(mock(Webhook.class), mock(Webhook.class));
        ActiveWebhooksCacheKey
                cacheKey = new ActiveWebhooksCacheKey(eventProfileName, eventProfileVersion, channelUri, tenantId);
        ActiveWebhooksCacheEntry cacheEntry = mock(ActiveWebhooksCacheEntry.class);

        // Use reflection to set the private activeWebhooksCache field.
        ActiveWebhooksCache activeWebhooksCache = Mockito.mock(ActiveWebhooksCache.class);
        Field field = null;
        try {
            field = cacheBackedWebhookManagementDAO.getClass().getDeclaredField("activeWebhooksCache");
            field.setAccessible(true);
            field.set(cacheBackedWebhookManagementDAO, activeWebhooksCache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(activeWebhooksCache.getValueFromCache(cacheKey, tenantId)).thenReturn(cacheEntry);
        when(cacheEntry.getWebhooks()).thenReturn(webhooks);

        List<Webhook> result =
                cacheBackedWebhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri,
                        tenantId);

        assertEquals(result, webhooks);
        verify(activeWebhooksCache).getValueFromCache(cacheKey, tenantId);
        verify(cacheEntry, atLeastOnce()).getWebhooks();
        verify(webhookManagementDAO, never()).getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri,
                tenantId);
    }

    /**
     * Test getActiveWebhooks retrieves from DAO and updates cache when cache miss occurs.
     */
    @Test
    public void testGetActiveWebhooks_CacheMiss() throws WebhookMgtException {

        String eventProfileName = "profile";
        String eventProfileVersion = "v1";
        String channelUri = "http://channel";
        int tenantId = TENANT_ID;
        List<Webhook> webhooks = Arrays.asList(mock(Webhook.class), mock(Webhook.class));
        ActiveWebhooksCacheKey cacheKey =
                new ActiveWebhooksCacheKey(eventProfileName, eventProfileVersion, channelUri, tenantId);

        // Use reflection to set the private activeWebhooksCache field.
        ActiveWebhooksCache activeWebhooksCache = Mockito.mock(ActiveWebhooksCache.class);
        Field field = null;
        try {
            field = cacheBackedWebhookManagementDAO.getClass().getDeclaredField("activeWebhooksCache");
            field.setAccessible(true);
            field.set(cacheBackedWebhookManagementDAO, activeWebhooksCache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(activeWebhooksCache.getValueFromCache(cacheKey, tenantId)).thenReturn(null);
        when(webhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri,
                tenantId)).thenReturn(webhooks);

        List<Webhook> result =
                cacheBackedWebhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri,
                        tenantId);

        assertEquals(result, webhooks);
        verify(activeWebhooksCache).getValueFromCache(cacheKey, tenantId);
        verify(webhookManagementDAO).getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, tenantId);
        verify(activeWebhooksCache).addToCache(org.mockito.ArgumentMatchers.eq(cacheKey),
                org.mockito.ArgumentMatchers.any(ActiveWebhooksCacheEntry.class),
                org.mockito.ArgumentMatchers.eq(tenantId));
    }
}
