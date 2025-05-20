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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookManagementDAOImplTest {

    private static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";
    private static final String WEBHOOK_ENDPOINT2 = "https://example.com/webhook2";
    private static final String WEBHOOK_ENDPOINT3 = "https://example.com/webhook3";
    private static final String WEBHOOK_DESCRIPTION = "Test webhook description";
    private static final String WEBHOOK_SECRET = "test-secret";
    private static final String WEBHOOK_EVENT_SCHEMA_NAME = "user-events";
    private static final String WEBHOOK_EVENT_SCHEMA_URI = "https://schemas.org/user-events";
    Webhook testWebhook;

    public static final int TENANT_ID = 1;
    private Webhook createdWebhook;
    WebhookManagementDAOImpl webhookManagementDAOImpl = new WebhookManagementDAOImpl();

    @Test
    public void testAddWebhook() throws WebhookMgtException {

        testWebhook = createTestWebhook();
        webhookManagementDAOImpl.createWebhook(testWebhook, TENANT_ID);

        createdWebhook = webhookManagementDAOImpl.getWebhook(testWebhook.getUuid(), TENANT_ID);
        assertNotNull(createdWebhook);
        assertEquals(testWebhook.getUuid(), createdWebhook.getUuid());
        assertSame(createdWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test(dependsOnMethods = {"testAddWebhook"}, expectedExceptions = WebhookMgtException.class)
    public void testAddWebhookWithDuplicateEndpoint() throws WebhookMgtException {

        // Create a webhook with the same endpoint as the existing one
        Webhook duplicateWebhook = createTestWebhook();
        webhookManagementDAOImpl.createWebhook(duplicateWebhook, TENANT_ID);
    }

    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testWebhookWithMultipleEvents() throws WebhookMgtException {

        // Create a webhook with multiple events
        testWebhook = createTestWebhook();
        testWebhook.setEndpoint(WEBHOOK_ENDPOINT2);
        List<String> events = new ArrayList<>();
        events.add("event1");
        events.add("event2");
        events.add("event3");
        testWebhook.setEventsSubscribed(events);
        webhookManagementDAOImpl.createWebhook(testWebhook, TENANT_ID);

        // Retrieve the webhook and verify the events
        Webhook retrievedWebhook = webhookManagementDAOImpl.getWebhook(testWebhook.getUuid(), TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getUuid(), testWebhook.getUuid());
        assertNotNull(retrievedWebhook.getEventsSubscribed());
        assertEquals(retrievedWebhook.getEventsSubscribed().size(), events.size());
        assertTrue(retrievedWebhook.getEventsSubscribed().containsAll(events));
    }

    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testUpdateWebhook() throws WebhookMgtException {

        testWebhook.setDescription("Updated description");
        webhookManagementDAOImpl.updateWebhook(testWebhook, TENANT_ID);

        Webhook updatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getUuid(), TENANT_ID);
        assertNotNull(updatedWebhook);
        assertEquals(createdWebhook.getUuid(), updatedWebhook.getUuid());
        Assert.assertEquals(updatedWebhook.getDescription(), "Updated description");
    }

    @Test(dependsOnMethods = {"testUpdateWebhook"})
    public void testDeactivateWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.deactivateWebhook(createdWebhook.getUuid(), TENANT_ID);
        Webhook deactivatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getUuid(), TENANT_ID);
        assertNotNull(deactivatedWebhook);
        assertEquals(deactivatedWebhook.getUuid(), createdWebhook.getUuid());
        assertEquals(deactivatedWebhook.getStatus(), WebhookStatus.INACTIVE);
    }

    @Test(dependsOnMethods = {"testDeactivateWebhook"})
    public void testActivateWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.activateWebhook(createdWebhook.getUuid(), TENANT_ID);
        Webhook activatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getUuid(), TENANT_ID);
        assertNotNull(activatedWebhook);
        assertEquals(activatedWebhook.getUuid(), createdWebhook.getUuid());
        assertEquals(activatedWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test(dependsOnMethods = {"testActivateWebhook"})
    public void testDeleteWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.deleteWebhook(createdWebhook.getUuid(), TENANT_ID);
        Webhook deletedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getUuid(), TENANT_ID);
        assertNull(deletedWebhook);
    }

    @Test(dependsOnMethods = {"testWebhookWithMultipleEvents"})
    public void testGetWebhooks() throws WebhookMgtException {
        // Create another webhook to ensure we have multiple webhooks
        Webhook secondWebhook = createTestWebhook();
        secondWebhook.setEndpoint(WEBHOOK_ENDPOINT3);
        secondWebhook.setDescription("Another test webhook");
        webhookManagementDAOImpl.createWebhook(secondWebhook, TENANT_ID);

        // Get all webhooks for the tenant
        List<Webhook> webhooks = webhookManagementDAOImpl.getWebhooks(TENANT_ID);

        // Verify the results
        assertNotNull(webhooks);
        assertTrue(webhooks.size() >= 2);

        // Verify that our created webhooks are in the list
        boolean foundOriginal = false;
        boolean foundSecond = false;

        for (Webhook webhook : webhooks) {
            if (webhook.getUuid().equals(testWebhook.getUuid())) {
                foundOriginal = true;
            } else if (webhook.getUuid().equals(secondWebhook.getUuid())) {
                foundSecond = true;
            }
        }

        assertTrue(foundOriginal, "Original webhook not found in results");
        assertTrue(foundSecond, "Second webhook not found in results");
    }

    @Test(dependsOnMethods = {"testGetWebhooks"})
    public void testIsWebhookEndpointExists() throws WebhookMgtException {
        // Test with an existing endpoint
        boolean exists = webhookManagementDAOImpl.isWebhookEndpointExists(WEBHOOK_ENDPOINT3, TENANT_ID);
        assertTrue(exists, "Webhook endpoint should exist");

        // Test with another existing endpoint
        exists = webhookManagementDAOImpl.isWebhookEndpointExists(WEBHOOK_ENDPOINT2, TENANT_ID);
        assertTrue(exists, "Second webhook endpoint should exist");

        // Test with a non-existing endpoint
        String nonExistingEndpoint = "https://example.com/nonexisting";
        exists = webhookManagementDAOImpl.isWebhookEndpointExists(nonExistingEndpoint, TENANT_ID);
        assertFalse(exists, "Non-existing webhook endpoint should not exist");
    }

    private Webhook createTestWebhook() {

        Webhook webhook = new Webhook();
        webhook.setUuid(UUID.randomUUID().toString());
        webhook.setEndpoint(WEBHOOK_ENDPOINT);
        webhook.setDescription(WEBHOOK_DESCRIPTION);
        webhook.setSecret(WEBHOOK_SECRET);
        webhook.setEventSchemaName(WEBHOOK_EVENT_SCHEMA_NAME);
        webhook.setEventSchemaUri(WEBHOOK_EVENT_SCHEMA_URI);
        webhook.setStatus(WebhookStatus.ACTIVE);
        webhook.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        webhook.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return webhook;
    }
}
