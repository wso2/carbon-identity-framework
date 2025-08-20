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
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
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
    private static final String WEBHOOK_NAME = "Test webhook";
    private static final String WEBHOOK_SECRET = "test-secret";
    private static final String WEBHOOK_EVENT_PROFILE_NAME = "user-events";
    private static final String WEBHOOK_EVENT_PROFILE_URI = "https://schemas.org/user-events";
    Webhook testWebhook;

    public static final int TENANT_ID = 1;
    private Webhook createdWebhook;
    WebhookManagementDAOImpl webhookManagementDAOImpl = new WebhookManagementDAOImpl();

    @Test
    public void testAddWebhook() throws WebhookMgtException {

        testWebhook = createTestWebhook();
        webhookManagementDAOImpl.createWebhook(testWebhook, TENANT_ID);

        createdWebhook = webhookManagementDAOImpl.getWebhook(testWebhook.getId(), TENANT_ID);
        assertNotNull(createdWebhook);
        assertEquals(testWebhook.getId(), createdWebhook.getId());
        assertSame(createdWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test(dependsOnMethods = {"testAddWebhook"}, expectedExceptions = WebhookMgtException.class)
    public void testAddWebhookWithDuplicateEndpoint() throws WebhookMgtException {

        Webhook duplicateWebhook = createTestWebhook();
        webhookManagementDAOImpl.createWebhook(duplicateWebhook, TENANT_ID);
    }

    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testWebhookWithMultipleEvents() throws WebhookMgtException {

        List<Subscription> events = new ArrayList<>();
        events.add(
                Subscription.builder().channelUri("event1").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        events.add(
                Subscription.builder().channelUri("event2").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        events.add(
                Subscription.builder().channelUri("event3").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        testWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint(WEBHOOK_ENDPOINT2)
                .name(WEBHOOK_NAME)
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(events)
                .build();

        webhookManagementDAOImpl.createWebhook(testWebhook, TENANT_ID);

        Webhook retrievedWebhook = webhookManagementDAOImpl.getWebhook(testWebhook.getId(), TENANT_ID);
        assertNotNull(retrievedWebhook);
        assertEquals(retrievedWebhook.getId(), testWebhook.getId());
        assertNotNull(retrievedWebhook.getEventsSubscribed());
        assertEquals(retrievedWebhook.getEventsSubscribed().size(), events.size());
        for (Subscription event : events) {
            assertTrue(retrievedWebhook.getEventsSubscribed().stream()
                    .anyMatch(e -> e.getChannelUri().equals(event.getChannelUri())));
        }
    }

    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testUpdateWebhook() throws WebhookMgtException {

        testWebhook = new Webhook.Builder()
                .uuid(testWebhook.getId())
                .endpoint(testWebhook.getEndpoint())
                .name("Updated name")
                .secret(testWebhook.getSecret())
                .eventProfileName(testWebhook.getEventProfileName())
                .eventProfileUri(testWebhook.getEventProfileUri())
                .status(testWebhook.getStatus())
                .createdAt(testWebhook.getCreatedAt())
                .updatedAt(testWebhook.getUpdatedAt())
                .eventsSubscribed(testWebhook.getEventsSubscribed())
                .build();

        webhookManagementDAOImpl.updateWebhook(testWebhook, TENANT_ID);

        Webhook updatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getId(), TENANT_ID);
        assertNotNull(updatedWebhook);
        assertEquals(createdWebhook.getId(), updatedWebhook.getId());
        Assert.assertEquals(updatedWebhook.getName(), "Updated name");
    }

    @Test(dependsOnMethods = {"testUpdateWebhook"})
    public void testDeactivateWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.deactivateWebhook(createdWebhook, TENANT_ID);
        Webhook deactivatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getId(), TENANT_ID);
        assertNotNull(deactivatedWebhook);
        assertEquals(deactivatedWebhook.getId(), createdWebhook.getId());
    }

    @Test(dependsOnMethods = {"testDeactivateWebhook"})
    public void testActivateWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.activateWebhook(createdWebhook, TENANT_ID);
        Webhook activatedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getId(), TENANT_ID);
        assertNotNull(activatedWebhook);
        assertEquals(activatedWebhook.getId(), createdWebhook.getId());
        assertEquals(activatedWebhook.getStatus(), WebhookStatus.ACTIVE);
    }

    @Test(dependsOnMethods = {"testActivateWebhook"})
    public void testDeleteWebhook() throws WebhookMgtException {

        webhookManagementDAOImpl.deleteWebhook(createdWebhook.getId(), TENANT_ID);
        Webhook deletedWebhook = webhookManagementDAOImpl.getWebhook(createdWebhook.getId(), TENANT_ID);
        assertNull(deletedWebhook);
    }

    @Test(dependsOnMethods = {"testWebhookWithMultipleEvents"})
    public void testGetWebhooks() throws WebhookMgtException {

        Webhook secondWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint(WEBHOOK_ENDPOINT3)
                .name("Another test webhook")
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        webhookManagementDAOImpl.createWebhook(secondWebhook, TENANT_ID);

        List<Webhook> webhooks = webhookManagementDAOImpl.getWebhooks(TENANT_ID);

        assertNotNull(webhooks);
        assertTrue(webhooks.size() >= 2);

        boolean foundOriginal = false;
        boolean foundSecond = false;

        for (Webhook webhook : webhooks) {
            if (webhook.getId().equals(testWebhook.getId())) {
                foundOriginal = true;
            } else if (webhook.getId().equals(secondWebhook.getId())) {
                foundSecond = true;
            }
        }

        assertTrue(foundOriginal, "Original webhook not found in results");
        assertTrue(foundSecond, "Second webhook not found in results");
    }

    @Test(dependsOnMethods = {"testGetWebhooks"})
    public void testIsWebhookEndpointExists() throws WebhookMgtException {

        boolean exists = webhookManagementDAOImpl.isWebhookEndpointExists(WEBHOOK_ENDPOINT3, TENANT_ID);
        assertTrue(exists, "Webhook endpoint should exist");

        exists = webhookManagementDAOImpl.isWebhookEndpointExists(WEBHOOK_ENDPOINT2, TENANT_ID);
        assertTrue(exists, "Second webhook endpoint should exist");

        String nonExistingEndpoint = "https://example.com/nonexisting";
        exists = webhookManagementDAOImpl.isWebhookEndpointExists(nonExistingEndpoint, TENANT_ID);
        assertFalse(exists, "Non-existing webhook endpoint should not exist");
    }

    @Test(dependsOnMethods = {"testWebhookWithMultipleEvents"})
    public void testGetWebhookEvents() throws WebhookMgtException {

        List<Subscription> events = new ArrayList<>();
        events.add(
                Subscription.builder().channelUri("eventA").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        events.add(
                Subscription.builder().channelUri("eventB").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        events.add(
                Subscription.builder().channelUri("eventC").status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        Webhook webhookWithEvents = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/webhook-events")
                .name("Webhook with events")
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(events)
                .build();

        webhookManagementDAOImpl.createWebhook(webhookWithEvents, TENANT_ID);

        List<Subscription> retrievedEvents =
                webhookManagementDAOImpl.getWebhookEvents(webhookWithEvents.getId(), TENANT_ID);

        assertNotNull(retrievedEvents);
        assertEquals(retrievedEvents.size(), events.size());
        for (Subscription event : events) {
            assertTrue(retrievedEvents.stream()
                    .anyMatch(e -> e.getChannelUri().equals(event.getChannelUri())));
        }
    }

    @Test(dependsOnMethods = {"testGetWebhooks"})
    public void testGetWebhooksCount() throws WebhookMgtException {

        int count = webhookManagementDAOImpl.getWebhooksCount(TENANT_ID);
        // At this point, at least two webhooks should exist (from previous tests)
        assertTrue(count >= 2, "Expected at least 2 webhooks, but found: " + count);

        // Add another webhook and check count increases
        Webhook newWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/webhook-count")
                .name("Count Test Webhook")
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        webhookManagementDAOImpl.createWebhook(newWebhook, TENANT_ID);

        int newCount = webhookManagementDAOImpl.getWebhooksCount(TENANT_ID);
        assertEquals(newCount, count + 1, "Webhook count should increase by 1 after adding a new webhook");
    }

    @Test(dependsOnMethods = {"testGetWebhooks"})
    public void testGetActiveWebhooks() throws WebhookMgtException {

        String eventProfileName = WEBHOOK_EVENT_PROFILE_NAME;
        String eventProfileVersion = "v1";
        String channelUri = "active-channel-uri";

        // Create an active webhook
        List<Subscription> eventsSubscribed = new ArrayList<>();
        eventsSubscribed.add(
                Subscription.builder().channelUri(channelUri).status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                        .build());
        Webhook activeWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/active-webhook")
                .name("Active Webhook")
                .secret(WEBHOOK_SECRET)
                .eventProfileName(eventProfileName)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .eventProfileVersion(eventProfileVersion)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(eventsSubscribed)
                .build();
        webhookManagementDAOImpl.createWebhook(activeWebhook, TENANT_ID);

        // Create an inactive webhook (should not be returned)
        List<Subscription> eventsSubscribed1 = new ArrayList<>();
        eventsSubscribed1.add(
                Subscription.builder().channelUri(channelUri).status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                        .build());
        Webhook inactiveWebhook = new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/inactive-webhook")
                .name("Inactive Webhook")
                .secret(WEBHOOK_SECRET)
                .eventProfileName(eventProfileName)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .eventProfileVersion(eventProfileVersion)
                .status(WebhookStatus.INACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(eventsSubscribed1)
                .build();
        webhookManagementDAOImpl.createWebhook(inactiveWebhook, TENANT_ID);

        List<Webhook> activeWebhooks = webhookManagementDAOImpl.getActiveWebhooks(
                eventProfileName, eventProfileVersion, channelUri, TENANT_ID);

        assertNotNull(activeWebhooks);
        assertTrue(activeWebhooks.stream().anyMatch(w -> w.getId().equals(activeWebhook.getId())));
        assertFalse(activeWebhooks.stream().anyMatch(w -> w.getId().equals(inactiveWebhook.getId())));
    }

    private Webhook createTestWebhook() {

        return new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint(WEBHOOK_ENDPOINT)
                .name(WEBHOOK_NAME)
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
