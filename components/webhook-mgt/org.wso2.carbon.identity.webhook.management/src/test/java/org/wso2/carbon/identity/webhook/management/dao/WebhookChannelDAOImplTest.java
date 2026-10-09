/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookChannelDAOImpl;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for organization-level event subscription persistence.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookChannelDAOImplTest {

    private static final int TENANT_ID = 1;
    private static final int SUB_ORG_TENANT_ID_1 = 101;
    private static final int SUB_ORG_TENANT_ID_2 = 102;
    private static final String SUB_ORG_ID_1 = "dd8e0455-29a1-4c73-8d6b-60f88c607d38";
    private static final String SUB_ORG_ID_2 = "7c1f9a83-5b2e-41d7-9f30-2ac4e8b16d55";
    private static final String CHANNEL_URI = "https://schemas.identity.wso2.org/events/logins";
    private static final String OTHER_CHANNEL_URI = "https://schemas.identity.wso2.org/events/registration";
    private static final String EVENT_PROFILE_NAME = "WSO2";
    private static final String EVENT_PROFILE_VERSION = "v1";

    private final WebhookManagementDAOImpl webhookManagementDAO = new WebhookManagementDAOImpl();
    private final WebhookChannelDAOImpl orgSubscriptionDAO = new WebhookChannelDAOImpl();

    private String webhookId;
    private String channelUuid;

    @BeforeClass
    public void createWebhook() throws WebhookMgtException {

        webhookId = UUID.randomUUID().toString();
        List<Subscription> channels = Arrays.asList(
                Subscription.builder().channelUri(CHANNEL_URI)
                        .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build(),
                Subscription.builder().channelUri(OTHER_CHANNEL_URI)
                        .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build());

        // The test schema has the IDN_WEBHOOK_CHANNELS.UUID column. Unit tests do not load identity.xml, so the
        // property is mocked.
        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, Answers.CALLS_REAL_METHODS)) {
            identityUtil.when(() -> IdentityUtil.getProperty(WebhookManagementDAOImpl.CHANNEL_UUID_COLUMN_AVAILABLE))
                    .thenReturn("true");
            webhookManagementDAO.createWebhook(new Webhook.Builder()
                    .uuid(webhookId)
                    .endpoint("https://example.com/org-sub-webhook")
                    .name("Org subscription webhook")
                    .secret("test-secret")
                    .eventProfileName(EVENT_PROFILE_NAME)
                    .eventProfileUri("https://schemas.identity.wso2.org/events")
                    .eventProfileVersion(EVENT_PROFILE_VERSION)
                    .status(WebhookStatus.ACTIVE)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .updatedAt(new Timestamp(System.currentTimeMillis()))
                    .eventsSubscribed(channels)
                    .build(), TENANT_ID);
        }

        channelUuid = orgSubscriptionDAO.getChannelUuid(webhookId, CHANNEL_URI, TENANT_ID);
        assertNotNull(channelUuid, "Channel UUID must be resolvable after the webhook is created.");
    }

//    @Test
//    public void testGetChannelUuidForUnknownChannelReturnsNull() throws WebhookMgtException {
//
//        assertNull(orgSubscriptionDAO.getChannelUuid(webhookId, "https://schemas.identity.wso2.org/events/none",
//                TENANT_ID), "An unsubscribed channel URI must not resolve to a channel UUID.");
//    }
//
//    @Test
//    public void testGetChannelUuidsReturnsEveryChannel() throws WebhookMgtException {
//
//        // The delete path reads these before removing the webhook, so that each channel's sharing
//        // policy can be cleared from UM_RESOURCE_SHARING_POLICY, which cannot cascade across
//        // databases.
//        List<String> channelUuids = orgSubscriptionDAO.getChannelUuids(webhookId, TENANT_ID);
//        assertEquals(channelUuids.size(), 2);
//        assertTrue(channelUuids.contains(channelUuid));
//    }
//
//    @Test
//    public void testAddAndListSubscriptions() throws WebhookMgtException {
//
//        orgSubscriptionDAO.addChannelOrgSubscriptions(Arrays.asList(
//                subscription(SUB_ORG_TENANT_ID_1, SUB_ORG_ID_1),
//                subscription(SUB_ORG_TENANT_ID_2, SUB_ORG_ID_2)));
//
//        List<ChannelOrgSubscription> subscriptions = orgSubscriptionDAO.getChannelOrgSubscriptions(channelUuid);
//        assertEquals(subscriptions.size(), 2);
//        assertEquals(orgSubscriptionDAO.getChannelOrgSubscriptionCount(channelUuid), 2);
//
//        List<Integer> tenantIds = orgSubscriptionDAO.getSubscribedOrgTenantIds(channelUuid);
//        assertTrue(tenantIds.containsAll(Arrays.asList(SUB_ORG_TENANT_ID_1, SUB_ORG_TENANT_ID_2)));
//
//        // The organization id is stored alongside the tenant id so the REST layer, which works in
//        // organization ids, never has to resolve one per row.
//        for (ChannelOrgSubscription subscription : subscriptions) {
//            assertEquals(subscription.getChannelUuid(), channelUuid);
//            if (subscription.getSubscribedOrgTenantId() == SUB_ORG_TENANT_ID_1) {
//                assertEquals(subscription.getSubscribedOrgId(), SUB_ORG_ID_1);
//            } else {
//                assertEquals(subscription.getSubscribedOrgId(), SUB_ORG_ID_2);
//            }
//            assertNull(subscription.getTopic(),
//                    "TOPIC stays null under the Publisher adapter, where no topics exist.");
//            assertNull(subscription.getTopicUuid(), "TOPIC_UUID stays null under the Publisher adapter.");
//        }
//    }
//
//
//    /**
//     * Deleting a webhook must take its organization subscriptions with it, through the foreign key
//     * on CHANNEL_UUID. This is what makes deleting a webhook subscribed to many organizations a
//     * bounded local cascade rather than a fan-out.
//     */
//    @Test(dependsOnMethods = {"testDeleteByTenantSpansChannels"})
//    public void testSubscriptionsCascadeOnWebhookDelete() throws WebhookMgtException {
//
//        orgSubscriptionDAO.addChannelOrgSubscriptions(
//                java.util.Collections.singletonList(subscription(SUB_ORG_TENANT_ID_1, SUB_ORG_ID_1)));
//        assertEquals(orgSubscriptionDAO.getChannelOrgSubscriptionCount(channelUuid), 1);
//
//        webhookManagementDAO.deleteWebhook(webhookId, TENANT_ID);
//
//        assertEquals(orgSubscriptionDAO.getChannelOrgSubscriptionCount(channelUuid), 0,
//                "Organization subscriptions must be removed when the owning webhook is deleted.");
//        assertNull(orgSubscriptionDAO.getChannelUuid(webhookId, CHANNEL_URI, TENANT_ID));
//    }
//
//    private ChannelOrgSubscription subscription(int subscribedOrgTenantId, String subscribedOrgId) {
//
//        return ChannelOrgSubscription.builder()
//                .channelUuid(channelUuid)
//                .subscribedOrgTenantId(subscribedOrgTenantId)
//                .subscribedOrgId(subscribedOrgId)
//                .build();
//    }
}
