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

import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mockStatic;
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
    private static final int SUBSCRIBED_TENANT_ID = 7;
    // createWebhook stores this rather than the webhook's own version; the lookup filters on it.
    private static final String WEBHOOK_SCHEMA_VERSION = "v1";
    private Webhook createdWebhook;
    WebhookManagementDAOImpl webhookManagementDAOImpl = new WebhookManagementDAOImpl();
    private MockedStatic<IdentityUtil> identityUtil;

    /**
     * The test schema has the IDN_WEBHOOK_CHANNELS.UUID column, so the suite declares it available. Unit tests do
     * not load identity.xml, so the property is mocked. Every other IdentityUtil method keeps its real behavior.
     */
    @BeforeClass
    public void setUpClass() {

        identityUtil = mockStatic(IdentityUtil.class, Answers.CALLS_REAL_METHODS);
        stubChannelUuidColumnAvailable("true");
    }

    @AfterMethod
    public void resetChannelUuidColumnAvailable() {

        stubChannelUuidColumnAvailable("true");
    }

    @AfterClass
    public void tearDownClass() {

        identityUtil.close();
    }

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

    /**
     * An update must not recycle channel identity. Channels present before and after the update
     * keep their UUID and subscription status, removed channels disappear, and only genuinely new
     * channels get a fresh UUID.
     * <p>
     * The UUID is the anchor for organization subscriptions and for the REST channel sub-resource,
     * so regenerating it on every update would silently detach both.
     */
    @Test(dependsOnMethods = {"testUpdateWebhook"})
    public void testUpdatePreservesChannelIdentity() throws Exception {

        List<Subscription> initialChannels = new ArrayList<>();
        initialChannels.add(Subscription.builder().channelUri("channel-keep")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        initialChannels.add(Subscription.builder().channelUri("channel-drop")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        Webhook webhook = buildWebhook(UUID.randomUUID().toString(),
                "https://example.com/webhook-identity", initialChannels);
        webhookManagementDAOImpl.createWebhook(webhook, TENANT_ID);

        Map<String, String> uuidsBefore = readChannelUuids(webhook.getId());
        assertEquals(uuidsBefore.size(), 2);
        assertNotNull(uuidsBefore.get("channel-keep"));

        // Drop one channel, retain one, add one. Statuses are intentionally absent, as they are on
        // a REST-originated update.
        List<Subscription> updatedChannels = new ArrayList<>();
        updatedChannels.add(Subscription.builder().channelUri("channel-keep").build());
        updatedChannels.add(Subscription.builder().channelUri("channel-new").build());

        webhookManagementDAOImpl.updateWebhook(
                buildWebhook(webhook.getId(), "https://example.com/webhook-identity", updatedChannels), TENANT_ID);

        Map<String, String> uuidsAfter = readChannelUuids(webhook.getId());
        assertEquals(uuidsAfter.size(), 2);
        assertEquals(uuidsAfter.get("channel-keep"), uuidsBefore.get("channel-keep"),
                "Retained channel must keep its UUID across an update.");
        assertNotNull(uuidsAfter.get("channel-new"), "Newly subscribed channel must be inserted.");
        assertNull(uuidsAfter.get("channel-drop"), "Unsubscribed channel must be removed.");

        // The retained channel must also keep the subscription status it already had, since the
        // update carried none.
        Webhook reloaded = webhookManagementDAOImpl.getWebhook(webhook.getId(), TENANT_ID);
        Subscription keptChannel = reloaded.getEventsSubscribed().stream()
                .filter(channel -> "channel-keep".equals(channel.getChannelUri()))
                .findFirst().orElse(null);
        assertNotNull(keptChannel);
        assertEquals(keptChannel.getStatus(), SubscriptionStatus.SUBSCRIPTION_ACCEPTED,
                "Retained channel must keep its subscription status across an update.");
    }

    private Webhook buildWebhook(String uuid, String endpoint, List<Subscription> channels) {

        return new Webhook.Builder()
                .uuid(uuid)
                .endpoint(endpoint)
                .name(WEBHOOK_NAME)
                .secret(WEBHOOK_SECRET)
                .eventProfileName(WEBHOOK_EVENT_PROFILE_NAME)
                .eventProfileUri(WEBHOOK_EVENT_PROFILE_URI)
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(channels)
                .build();
    }

    /**
     * Drop or restore the NOT NULL constraint on IDN_WEBHOOK_CHANNELS.UUID, so that the insert used before the
     * channel UUID migration can be tested against a schema like the one it was written for.
     */
    private void setChannelUuidNullable(boolean nullable) throws SQLException {

        String query = "ALTER TABLE IDN_WEBHOOK_CHANNELS ALTER COLUMN UUID " + (nullable ? "SET NULL" : "SET NOT NULL");
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.executeUpdate();
            connection.commit();
        }
    }

    private void stubChannelUuidColumnAvailable(String value) {

        identityUtil.when(() -> IdentityUtil.getProperty(WebhookManagementDAOImpl.CHANNEL_UUID_COLUMN_AVAILABLE))
                .thenReturn(value);
    }

    private Map<String, String> readChannelUuids(String webhookUuid) throws SQLException {

        String query = "SELECT C.CHANNEL_URI, C.UUID FROM IDN_WEBHOOK_CHANNELS C "
                + "INNER JOIN IDN_WEBHOOK W ON C.WEBHOOK_ID = W.ID WHERE W.UUID = ?";
        Map<String, String> channelUuids = new HashMap<>();
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, webhookUuid);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    channelUuids.put(resultSet.getString("CHANNEL_URI"), resultSet.getString("UUID"));
                }
            }
        }
        return channelUuids;
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

    /**
     * The channel UUID column arrives with a migration, so a deployment can declare it absent. The insert then
     * omits the column entirely and the channel is stored without an identifier.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testChannelsStoredWithoutUuidWhenColumnDeclaredUnavailable() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        List<Subscription> channels = new ArrayList<>();
        channels.add(Subscription.builder().channelUri("channel-without-uuid")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        stubChannelUuidColumnAvailable("false");
        setChannelUuidNullable(true);
        try {
            webhookManagementDAOImpl.createWebhook(
                    buildWebhook(webhookUuid, "https://example.com/webhook-no-uuid", channels), TENANT_ID);

            Map<String, String> storedUuids = readChannelUuids(webhookUuid);
            assertEquals(storedUuids.size(), 1);
            assertNull(storedUuids.get("channel-without-uuid"),
                    "A channel stored while the UUID column is declared unavailable must carry no identifier.");
        } finally {
            // Rows without a UUID must be gone before the NOT NULL constraint can be restored.
            webhookManagementDAOImpl.deleteWebhook(webhookUuid, TENANT_ID);
            setChannelUuidNullable(false);
        }
    }

    /**
     * With the column declared available, every channel is stored with a generated identifier.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testChannelsStoredWithUuidWhenColumnDeclaredAvailable() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        List<Subscription> channels = new ArrayList<>();
        channels.add(Subscription.builder().channelUri("channel-with-uuid")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        stubChannelUuidColumnAvailable("true");
        webhookManagementDAOImpl.createWebhook(
                buildWebhook(webhookUuid, "https://example.com/webhook-with-uuid", channels), TENANT_ID);

        assertNotNull(readChannelUuids(webhookUuid).get("channel-with-uuid"),
                "A channel stored while the UUID column is declared available must carry an identifier.");
    }

    /**
     * An unset property means the column is not available, so the channel is stored without an identifier and
     * webhook creation works on a schema that does not have the column.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testChannelsStoredWithoutUuidWhenConfigIsAbsent() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        List<Subscription> channels = new ArrayList<>();
        channels.add(Subscription.builder().channelUri("channel-default-config")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        stubChannelUuidColumnAvailable(null);
        setChannelUuidNullable(true);
        try {
            webhookManagementDAOImpl.createWebhook(
                    buildWebhook(webhookUuid, "https://example.com/webhook-default-config", channels), TENANT_ID);

            Map<String, String> storedUuids = readChannelUuids(webhookUuid);
            assertEquals(storedUuids.size(), 1);
            assertNull(storedUuids.get("channel-default-config"),
                    "An unset channel UUID column property must be read as unavailable.");
        } finally {
            // Rows without a UUID must be gone before the NOT NULL constraint can be restored.
            webhookManagementDAOImpl.deleteWebhook(webhookUuid, TENANT_ID);
            setChannelUuidNullable(false);
        }
    }

    /**
     * The widened lookup returns a webhook to its own tenant, and to another tenant only once that tenant holds a
     * subscription row for the channel -- the two arms of the query.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testActiveWebhookLookupReachesSubscribedTenantOnly() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        List<Subscription> channels = new ArrayList<>();
        channels.add(Subscription.builder().channelUri("lookup-channel")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        webhookManagementDAOImpl.createWebhook(
                buildWebhook(webhookUuid, "https://example.com/webhook-lookup", channels), TENANT_ID);

        assertTrue(containsWebhook(webhookManagementDAOImpl.getActiveWebhooksWithSubscribedChildOrgs(
                WEBHOOK_EVENT_PROFILE_NAME, WEBHOOK_SCHEMA_VERSION, "lookup-channel", TENANT_ID), webhookUuid));
        assertFalse(containsWebhook(webhookManagementDAOImpl.getActiveWebhooksWithSubscribedChildOrgs(
                WEBHOOK_EVENT_PROFILE_NAME, WEBHOOK_SCHEMA_VERSION, "lookup-channel", SUBSCRIBED_TENANT_ID),
                webhookUuid), "An unsubscribed tenant must not reach another tenant's webhook.");

        subscribeTenantToChannel(readChannelUuids(webhookUuid).get("lookup-channel"), SUBSCRIBED_TENANT_ID);

        assertTrue(containsWebhook(webhookManagementDAOImpl.getActiveWebhooksWithSubscribedChildOrgs(
                WEBHOOK_EVENT_PROFILE_NAME, WEBHOOK_SCHEMA_VERSION, "lookup-channel", SUBSCRIBED_TENANT_ID),
                webhookUuid), "A subscribed tenant must reach the owning tenant's webhook.");
    }

    /**
     * Null entries, entries without a URI, and repeated URIs in an update are ignored rather than stored.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testUpdateIgnoresNullAndDuplicateChannels() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        String endpoint = "https://example.com/webhook-upsert";
        List<Subscription> initial = new ArrayList<>();
        initial.add(Subscription.builder().channelUri("kept")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        webhookManagementDAOImpl.createWebhook(buildWebhook(webhookUuid, endpoint, initial), TENANT_ID);

        List<Subscription> desired = new ArrayList<>();
        desired.add(null);
        desired.add(Subscription.builder().status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        desired.add(Subscription.builder().channelUri("kept")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        desired.add(Subscription.builder().channelUri("added")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        desired.add(Subscription.builder().channelUri("added")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        webhookManagementDAOImpl.updateWebhook(buildWebhook(webhookUuid, endpoint, desired), TENANT_ID);

        assertEquals(readChannelUuids(webhookUuid).keySet(), new HashSet<>(Arrays.asList("kept", "added")));
    }

    /**
     * An update carrying no channels removes every channel the webhook had.
     */
    @Test(dependsOnMethods = {"testAddWebhook"})
    public void testUpdateWithoutChannelsRemovesAll() throws Exception {

        String webhookUuid = UUID.randomUUID().toString();
        String endpoint = "https://example.com/webhook-clear";
        List<Subscription> initial = new ArrayList<>();
        initial.add(Subscription.builder().channelUri("first")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        initial.add(Subscription.builder().channelUri("second")
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());
        webhookManagementDAOImpl.createWebhook(buildWebhook(webhookUuid, endpoint, initial), TENANT_ID);

        webhookManagementDAOImpl.updateWebhook(buildWebhook(webhookUuid, endpoint, null), TENANT_ID);

        assertTrue(readChannelUuids(webhookUuid).isEmpty());
    }

    private boolean containsWebhook(List<Webhook> webhooks, String webhookUuid) {

        return webhooks.stream().anyMatch(webhook -> webhookUuid.equals(webhook.getId()));
    }

    private void subscribeTenantToChannel(String channelUuid, int tenantId) throws SQLException {

        String query = "INSERT INTO IDN_WEBHOOK_CHANNEL_ORG_SUB "
                + "(CHANNEL_UUID, SUBSCRIBED_ORG_TENANT_ID, SUBSCRIBED_ORG_ID) VALUES (?, ?, ?)";
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, channelUuid);
            statement.setInt(2, tenantId);
            statement.setString(3, UUID.randomUUID().toString());
            statement.executeUpdate();
            connection.commit();
        }
    }
}
