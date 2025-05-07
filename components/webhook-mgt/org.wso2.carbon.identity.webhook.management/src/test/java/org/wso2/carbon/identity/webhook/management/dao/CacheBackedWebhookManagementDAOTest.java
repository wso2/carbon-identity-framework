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

package org.wso2.carbon.identity.webhook.management.dao;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.CacheBackedWebhookManagementDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CacheBackedWebhookManagementDAOTest {

    private static final String TEST_WEBHOOK_ID = "test-webhook-id";
    private static final String TEST_ENDPOINT = "https://example.com/webhook";
    private static final String TEST_DESCRIPTION = "Test Webhook";
    private static final int TENANT_ID = 1;

    private WebhookManagementDAO webhookManagementDAOImpl;
    private CacheBackedWebhookManagementDAO cacheBackedWebhookManagementDAO;
    private Webhook testWebhook;
    private List<Webhook> testWebhookList;

    @BeforeClass
    public void setUp() {

        webhookManagementDAOImpl = mock(WebhookManagementDAO.class);
        cacheBackedWebhookManagementDAO = new CacheBackedWebhookManagementDAO(webhookManagementDAOImpl);

        // Create test webhook
        testWebhook = createTestWebhook();
        testWebhookList = new ArrayList<>();
        testWebhookList.add(testWebhook);
    }

    @BeforeMethod
    public void resetMocks() {

        reset(webhookManagementDAOImpl);
    }

    @Test(priority = 1)
    public void testCreateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAOImpl).createWebhook(any(Webhook.class), anyInt());

        cacheBackedWebhookManagementDAO.createWebhook(testWebhook, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).createWebhook(any(Webhook.class), anyInt());
    }

    @Test(priority = 2)
    public void testGetWebhookFromDB() throws WebhookMgtException {

        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);

        Webhook webhook = cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhook(anyString(), anyInt());
        Assert.assertNotNull(webhook);
        assertWebhook(webhook);
    }

    @Test(priority = 3, dependsOnMethods = "testGetWebhookFromDB")
    public void testGetWebhookFromCache() throws WebhookMgtException {
        // First call should have cached the result, so next call should use cache
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(null);

        Webhook webhook = cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, never()).getWebhook(anyString(), anyInt());
        Assert.assertNotNull(webhook);
        assertWebhook(webhook);
    }

    @Test(priority = 4)
    public void testUpdateWebhook() throws WebhookMgtException {

        doNothing().when(webhookManagementDAOImpl).updateWebhook(anyString(), any(Webhook.class), anyInt());

        cacheBackedWebhookManagementDAO.updateWebhook(TEST_WEBHOOK_ID, testWebhook, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).updateWebhook(anyString(), any(Webhook.class), anyInt());

        // Verify cache is invalidated
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhook(anyString(), anyInt());
    }

    @Test(priority = 5)
    public void testGetWebhooks() throws WebhookMgtException {

        when(webhookManagementDAOImpl.getWebhooks(anyInt())).thenReturn(testWebhookList);

        List<Webhook> webhooks = cacheBackedWebhookManagementDAO.getWebhooks(TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhooks(anyInt());
        Assert.assertEquals(webhooks.size(), testWebhookList.size());
    }

    @Test(priority = 6)
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        when(webhookManagementDAOImpl.isWebhookEndpointExists(anyString(), anyInt())).thenReturn(true);

        boolean exists = cacheBackedWebhookManagementDAO.isWebhookEndpointExists(TEST_ENDPOINT, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).isWebhookEndpointExists(anyString(), anyInt());
        Assert.assertTrue(exists);
    }

    @Test(priority = 7)
    public void testActivateWebhook() throws WebhookMgtException {
        // Cache the webhook first
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        reset(webhookManagementDAOImpl);

        doNothing().when(webhookManagementDAOImpl).activateWebhook(anyString(), anyInt());

        cacheBackedWebhookManagementDAO.activateWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).activateWebhook(anyString(), anyInt());

        // Verify cache is invalidated
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhook(anyString(), anyInt());
    }

    @Test(priority = 8)
    public void testDeactivateWebhook() throws WebhookMgtException {
        // Cache the webhook first
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        reset(webhookManagementDAOImpl);

        doNothing().when(webhookManagementDAOImpl).deactivateWebhook(anyString(), anyInt());

        cacheBackedWebhookManagementDAO.deactivateWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).deactivateWebhook(anyString(), anyInt());

        // Verify cache is invalidated
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhook(anyString(), anyInt());
    }

    @Test(priority = 9)
    public void testDeleteWebhook() throws WebhookMgtException {
        // Cache the webhook first
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        reset(webhookManagementDAOImpl);

        doNothing().when(webhookManagementDAOImpl).deleteWebhook(anyString(), anyInt());

        cacheBackedWebhookManagementDAO.deleteWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).deleteWebhook(anyString(), anyInt());

        // Verify cache is invalidated
        when(webhookManagementDAOImpl.getWebhook(anyString(), anyInt())).thenReturn(testWebhook);
        cacheBackedWebhookManagementDAO.getWebhook(TEST_WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAOImpl, times(1)).getWebhook(anyString(), anyInt());
    }

    private Webhook createTestWebhook() {

        Webhook webhook = new Webhook();
        webhook.setId(TEST_WEBHOOK_ID);
        webhook.setUuid(UUID.randomUUID().toString());
        webhook.setEndpoint(TEST_ENDPOINT);
        webhook.setDescription(TEST_DESCRIPTION);
        webhook.setSecret("test-secret");
        webhook.setTenantId(TENANT_ID);
        webhook.setEventSchemaName("user-login");
        webhook.setEventSchemaUri("https://schema.example.com/user-login");
        webhook.setStatus(WebhookStatus.ACTIVE);

        List<String> events = new ArrayList<>();
        events.add("user.login");
        events.add("user.logout");
        webhook.setEventsSubscribed(events);

        return webhook;
    }

    private void assertWebhook(Webhook webhook) {

        Assert.assertEquals(webhook.getId(), testWebhook.getId());
        Assert.assertEquals(webhook.getEndpoint(), testWebhook.getEndpoint());
        Assert.assertEquals(webhook.getDescription(), testWebhook.getDescription());
        Assert.assertEquals(webhook.getSecret(), testWebhook.getSecret());
        Assert.assertEquals(webhook.getEventSchemaName(), testWebhook.getEventSchemaName());
        Assert.assertEquals(webhook.getEventSchemaUri(), testWebhook.getEventSchemaUri());
        Assert.assertEquals(webhook.getStatus(), testWebhook.getStatus());
        Assert.assertEquals(webhook.getEventsSubscribed().size(), testWebhook.getEventsSubscribed().size());
    }
}
