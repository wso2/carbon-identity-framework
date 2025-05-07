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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookManagementDAOImplTest {

    private static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";
    private static final String WEBHOOK_DESCRIPTION = "Test webhook description";
    private static final String WEBHOOK_SECRET = "test-secret";
    private static final String WEBHOOK_EVENT_SCHEMA_NAME = "user-events";
    private static final String WEBHOOK_EVENT_SCHEMA_URI = "https://schemas.org/user-events";
    private static final int TENANT_ID = 1;

    private WebhookManagementDAO webhookManagementDAO;
    private String webhookId;

    @BeforeClass
    public void setUpClass() {
        // Initialize DAO
        webhookManagementDAO = new WebhookManagementDAOImpl();

        // Create test tables if needed
        // This would normally be handled by @WithH2Database annotation
    }

    @BeforeMethod
    public void setUp() {

        webhookId = UUID.randomUUID().toString();
    }

    @AfterClass
    public void tearDown() {
        // Clean up resources if needed
    }

    @Test
    public void testCreateWebhook() throws WebhookMgtException {

        Webhook webhook = createTestWebhook();
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Verify webhook was created by retrieving it
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getId(), webhookId);
        assertEquals(retrievedWebhook.getEndpoint(), WEBHOOK_ENDPOINT);
        assertEquals(retrievedWebhook.getDescription(), WEBHOOK_DESCRIPTION);
        assertEquals(retrievedWebhook.getStatus(), WebhookStatus.INACTIVE);
    }

    @Test
    public void testGetWebhook() throws WebhookMgtException {
        // Create a webhook first
        Webhook webhook = createTestWebhook();
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Get the webhook and verify
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getId(), webhookId);
        assertEquals(retrievedWebhook.getEndpoint(), WEBHOOK_ENDPOINT);
        assertEquals(retrievedWebhook.getDescription(), WEBHOOK_DESCRIPTION);
    }

    @Test
    public void testGetNonExistentWebhook() throws WebhookMgtException {

        String nonExistentId = UUID.randomUUID().toString();
        Webhook webhook = webhookManagementDAO.getWebhook(nonExistentId, TENANT_ID);
        assertNull(webhook);
    }

    @Test
    public void testUpdateWebhook() throws WebhookMgtException {
        // Create a webhook first
        Webhook webhook = createTestWebhook();
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Update webhook
        String updatedEndpoint = "https://updated-example.com/webhook";
        String updatedDescription = "Updated description";

        Webhook updatedWebhook = new Webhook();
        updatedWebhook.setId(webhookId);
        updatedWebhook.setEndpoint(updatedEndpoint);
        updatedWebhook.setDescription(updatedDescription);
        updatedWebhook.setStatus(WebhookStatus.ACTIVE);

        webhookManagementDAO.updateWebhook(webhookId, updatedWebhook, TENANT_ID);

        // Get updated webhook and verify
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getEndpoint(), updatedEndpoint);
        assertEquals(retrievedWebhook.getDescription(), updatedDescription);
        assertEquals(retrievedWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {
        // Create a webhook first
        Webhook webhook = createTestWebhook();
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Verify webhook exists
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);

        // Delete webhook
        webhookManagementDAO.deleteWebhook(webhookId, TENANT_ID);

        // Verify webhook no longer exists
        Webhook deletedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNull(deletedWebhook);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {
        // Create multiple webhooks
        for (int i = 0; i < 3; i++) {
            Webhook webhook = createTestWebhook();
            webhook.setId(UUID.randomUUID().toString());
            webhookManagementDAO.createWebhook(webhook, TENANT_ID);
        }

        // Get all webhooks
        List<Webhook> webhooks = webhookManagementDAO.getWebhooks(TENANT_ID);
        assertNotNull(webhooks);
        assertTrue(webhooks.size() >= 3);
    }

    @Test
    public void testIsWebhookEndpointExists() throws WebhookMgtException {
        // Create a webhook first
        Webhook webhook = createTestWebhook();
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Check if endpoint exists
        boolean exists = webhookManagementDAO.isWebhookEndpointExists(WEBHOOK_ENDPOINT, TENANT_ID);
        assertTrue(exists);

        // Check non-existent endpoint
        String nonExistentEndpoint = "https://non-existent-endpoint.com/webhook";
        boolean notExists = webhookManagementDAO.isWebhookEndpointExists(nonExistentEndpoint, TENANT_ID);
        assertFalse(notExists);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {
        // Create a webhook with inactive status
        Webhook webhook = createTestWebhook();
        webhook.setStatus(WebhookStatus.INACTIVE);
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Activate webhook
        webhookManagementDAO.activateWebhook(webhookId, TENANT_ID);

        // Verify webhook is active
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {
        // Create a webhook with active status
        Webhook webhook = createTestWebhook();
        webhook.setStatus(WebhookStatus.ACTIVE);
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Deactivate webhook
        webhookManagementDAO.deactivateWebhook(webhookId, TENANT_ID);

        // Verify webhook is inactive
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getStatus(), WebhookStatus.INACTIVE);
    }

    @Test
    public void testCascadeDeleteWebhookWithEvents() throws WebhookMgtException {
        // Create a webhook
        Webhook webhook = createTestWebhook();
        webhook.getEventsSubscribed().add("user.login");
        webhook.getEventsSubscribed().add("user.logout");
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Verify webhook has events
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getEventsSubscribed().size(), 2);

        // Delete webhook
        webhookManagementDAO.deleteWebhook(webhookId, TENANT_ID);

        // Verify webhook and its events are deleted
        Webhook deletedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNull(deletedWebhook);
    }

    @Test
    public void testUpdateWebhookEvents() throws WebhookMgtException {
        // Create a webhook with events
        Webhook webhook = createTestWebhook();
        webhook.getEventsSubscribed().add("user.login");
        webhookManagementDAO.createWebhook(webhook, TENANT_ID);

        // Update webhook with different events
        Webhook updatedWebhook = new Webhook();
        updatedWebhook.setId(webhookId);
        updatedWebhook.setEndpoint(WEBHOOK_ENDPOINT);
        updatedWebhook.getEventsSubscribed().add("user.created");
        updatedWebhook.getEventsSubscribed().add("user.updated");

        webhookManagementDAO.updateWebhook(webhookId, updatedWebhook, TENANT_ID);

        // Verify updated events
        Webhook retrievedWebhook = webhookManagementDAO.getWebhook(webhookId, TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getEventsSubscribed().size(), 2);
        assertTrue(retrievedWebhook.getEventsSubscribed().contains("user.created"));
        assertTrue(retrievedWebhook.getEventsSubscribed().contains("user.updated"));
        assertFalse(retrievedWebhook.getEventsSubscribed().contains("user.login"));
    }

    private Webhook createTestWebhook() {

        Webhook webhook = new Webhook();
        webhook.setId(webhookId);
        webhook.setEndpoint(WEBHOOK_ENDPOINT);
        webhook.setDescription(WEBHOOK_DESCRIPTION);
        webhook.setSecret(WEBHOOK_SECRET);
        webhook.setEventSchemaName(WEBHOOK_EVENT_SCHEMA_NAME);
        webhook.setEventSchemaUri(WEBHOOK_EVENT_SCHEMA_URI);
        webhook.setStatus(WebhookStatus.INACTIVE);
        webhook.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        webhook.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return webhook;
    }
}
