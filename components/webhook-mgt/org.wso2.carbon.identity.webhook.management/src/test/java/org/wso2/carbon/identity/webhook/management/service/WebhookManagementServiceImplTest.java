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

package org.wso2.carbon.identity.webhook.management.service;

import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.exception
        .ResourceSharingPolicyMgtException;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.core.cache.ActiveWebhooksCache;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelSubscribedOrganizations;
import org.wso2.carbon.identity.webhook.management.api.model.OrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookChannel;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.PlannedOrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookManagementServiceImplTest {

    private WebhookManagementServiceImpl webhookManagementService;
    private WebhookManagementDAO webhookManagementDAO;

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private MockedStatic<WebhookManagementComponentServiceHolder> webhookComponentHolderMockedStatic;
    private WebhookMetadataService webhookMetadataService;
    private WebhookManagementComponentServiceHolder holder;

    public static final String WEBHOOK_ID = "webhookId";
    public static final String TENANT_DOMAIN = "test.com";
    public static final int TENANT_ID = 1;
    public static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";
    private static final String WEBSUBHUBADAPTER = "webSubHubAdapter";
    private static final String CHANNEL_ID = "2b6e5a7d-4c88-4e01-9a73-0f5cb8e3d219";
    private static final String CHANNEL_URI = "schemas.identity.wso2.org/events/logins";
    private static final String OTHER_CHANNEL_ID = "9f2c7b31-0a4e-4d8f-9c11-2b6e5a7d4c88";
    private static final String OTHER_CHANNEL_URI = "schemas.identity.wso2.org/events/token";
    private static final String ORG_ID_1 = "b028ca17-8f89-449c-ae27-fa955e66465d";
    private static final String ORG_ID_2 = "a17b28ca-9f89-449c-ae27-fa955e66465f";
    private static final int ORG_TENANT_ID_1 = 11;
    private static final int ORG_TENANT_ID_2 = 12;
    private static final String OWNER_ORG_ID = "0e4a3c92-1f7b-4d58-9a06-b3e5c1d78f24";
    private static final String FOREIGN_ORG_ID = "ffffffff-1f7b-4d58-9a06-b3e5c1d78f24";

    @BeforeClass
    public void setUpClass() throws WebhookMetadataException, SQLException {

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

        // Creation opens a transaction, so a datasource must be reachable. The DAO is a mock, so this connection
        // is only acquired, committed and closed; no statement ever runs on it.
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connection);
        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        identityDatabaseUtilMockedStatic.when(IdentityDatabaseUtil::getDataSource).thenReturn(dataSource);

        webhookComponentHolderMockedStatic = mockStatic(WebhookManagementComponentServiceHolder.class);
        holder = mock(WebhookManagementComponentServiceHolder.class); // Use class field
        webhookMetadataService = mock(WebhookMetadataService.class);

        webhookComponentHolderMockedStatic.when(WebhookManagementComponentServiceHolder::getInstance)
                .thenReturn(holder);
        when(holder.getWebhookMetadataService()).thenReturn(webhookMetadataService);

        Channel channel = new Channel("logins", "Logins Channel", "schemas.identity.wso2.org/events/logins",
                Collections.emptyList());
        EventProfile eventProfile = new EventProfile("profile", "uri", Collections.singletonList(channel));
        when(webhookMetadataService.getSupportedEventProfiles())
                .thenReturn(Collections.singletonList(eventProfile));
    }

    @AfterClass
    public void tearDownClass() {

        identityTenantUtilMockedStatic.close();
        identityDatabaseUtilMockedStatic.close();
        webhookComponentHolderMockedStatic.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        Adapter webhookAdapterMock = mock(Adapter.class);
        when(holder.getWebhookAdapter()).thenReturn(webhookAdapterMock);
        when(webhookAdapterMock.getName()).thenReturn(WEBSUBHUBADAPTER);

        webhookManagementDAO = mock(WebhookManagementDAO.class);
        webhookManagementService = WebhookManagementServiceImpl.getInstance();
        Field daoField = WebhookManagementServiceImpl.class.getDeclaredField("daoFACADE");
        daoField.setAccessible(true);
        daoField.set(webhookManagementService, webhookManagementDAO);
    }

    @Test
    public void testCreateWebhook() throws Exception {

        Webhook inputWebhook = mock(Webhook.class);
        when(inputWebhook.getEndpoint()).thenReturn("https://test.com/webhook");
        when(inputWebhook.getStatus()).thenReturn(null);
        when(inputWebhook.getName()).thenReturn("name");
        when(inputWebhook.getSecret()).thenReturn("aBcD1234_efGh5678~IjKl9012+MnOpQR");
        when(inputWebhook.getEventProfileName()).thenReturn("profile");
        when(inputWebhook.getEventProfileUri()).thenReturn("uri");
        when(inputWebhook.getCreatedAt()).thenReturn(null);
        when(inputWebhook.getUpdatedAt()).thenReturn(null);

        Subscription subscription =
                Subscription.builder().channelUri("schemas.identity.wso2.org/events/logins").build();
        when(inputWebhook.getEventsSubscribed()).thenReturn(Collections.singletonList(subscription));

        when(webhookManagementDAO.isWebhookEndpointExists("https://test.com/webhook", 1)).thenReturn(false);

        Webhook createdWebhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(anyString(), eq(1))).thenReturn(createdWebhook);

        WebhookManagementServiceImpl service = WebhookManagementServiceImpl.getInstance();
        Field daoField = WebhookManagementServiceImpl.class.getDeclaredField("daoFACADE");
        daoField.setAccessible(true);
        daoField.set(service, webhookManagementDAO);

        Webhook result = service.createWebhook(inputWebhook, "test.com");

        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(1), eq(Collections.emptyList()));
        verify(webhookManagementDAO).getWebhook(anyString(), eq(1));
        assertEquals(result, createdWebhook);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook endpoint already exists")
    public void testAddWebhookWithExistingEndpoint() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getEndpoint()).thenReturn(WEBHOOK_ENDPOINT);
        when(webhookManagementDAO.isWebhookEndpointExists(WEBHOOK_ENDPOINT, TENANT_ID)).thenReturn(true);
        webhookManagementService.createWebhook(webhook, TENANT_DOMAIN);
    }

    @Test
    public void testDeleteWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.INACTIVE);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhookByWebhookId() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        Webhook result = webhookManagementService.getWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, webhook);
    }

    @Test
    public void testDeactivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.ACTIVE);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.deactivateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).deactivateWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test
    public void testGetWebhooks() throws WebhookMgtException {

        List<Webhook> webhooks = new ArrayList<>();
        Webhook webhook1 = mock(Webhook.class);
        Webhook webhook2 = mock(Webhook.class);
        webhooks.add(webhook1);
        webhooks.add(webhook2);

        when(webhookManagementDAO.getWebhooks(TENANT_ID)).thenReturn(webhooks);

        List<Webhook> result = webhookManagementService.getWebhooks(TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhooks(TENANT_ID);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), webhook1);
        assertEquals(result.get(1), webhook2);
    }

    @Test
    public void testActivateWebhook() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.INACTIVE);
        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(webhook);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).activateWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp =
            "Webhook not found")
    public void testActivateWebhookNotExist() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        webhookManagementService.activateWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhookEvents() throws WebhookMgtException {

        List<Subscription> events = new ArrayList<>();
        Subscription sub1 = mock(Subscription.class);
        Subscription sub2 = mock(Subscription.class);
        events.add(sub1);
        events.add(sub2);

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(events);

        List<Subscription> result = webhookManagementService.getWebhookEvents(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).getWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO).getWebhookEvents(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, events);
    }

    @Test
    public void testRetryWebhook_PartiallyActive() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.PARTIALLY_ACTIVE);
        Webhook updatedWebhook = mock(Webhook.class);

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID))
                .thenReturn(webhook)
                .thenReturn(updatedWebhook);

        Webhook result = webhookManagementService.retryWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).retryWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, updatedWebhook);
    }

    @Test
    public void testRetryWebhook_PartiallyInactive() throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getStatus()).thenReturn(WebhookStatus.PARTIALLY_INACTIVE);
        Webhook updatedWebhook = mock(Webhook.class);

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID))
                .thenReturn(webhook)
                .thenReturn(updatedWebhook);

        Webhook result = webhookManagementService.retryWebhook(WEBHOOK_ID, TENANT_DOMAIN);

        verify(webhookManagementDAO).retryWebhook(webhook, TENANT_ID);
        verify(webhookManagementDAO, times(2)).getWebhook(WEBHOOK_ID, TENANT_ID);
        assertEquals(result, updatedWebhook);
    }

    @Test(expectedExceptions = WebhookMgtClientException.class, expectedExceptionsMessageRegExp = "Webhook not found")
    public void testRetryWebhook_NotFound() throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        webhookManagementService.retryWebhook(WEBHOOK_ID, TENANT_DOMAIN);
    }

    @Test
    public void testGetActiveWebhooks() throws WebhookMgtException {

        String eventProfileName = "profile";
        String eventProfileVersion = "v1";
        String channelUri = "schemas.identity.wso2.org/events/logins";

        List<Webhook> activeWebhooks = new ArrayList<>();
        Webhook webhook1 = mock(Webhook.class);
        Webhook webhook2 = mock(Webhook.class);
        activeWebhooks.add(webhook1);
        activeWebhooks.add(webhook2);

        when(webhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, TENANT_ID))
                .thenReturn(activeWebhooks);

        List<Webhook> result = webhookManagementService.getActiveWebhooks(
                eventProfileName, eventProfileVersion, channelUri, TENANT_DOMAIN);

        verify(webhookManagementDAO).getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, TENANT_ID);
        assertEquals(result.size(), 2);
        assertEquals(result.get(0), webhook1);
        assertEquals(result.get(1), webhook2);
    }

    @DataProvider(name = "activeWebhookLookupRouting")
    public Object[][] activeWebhookLookupRouting() {

        // childOrgSubscriptionEnabled, isSubOrganization, isSubOrganizationCheckFails, expectWidenedLookup
        return new Object[][]{
                {false, true, false, false},
                {true, false, false, false},
                {true, true, false, true},
                {true, false, true, true}
        };
    }

    /**
     * The wider lookup runs only for sub-organizations, and only when the feature is enabled. If the
     * sub-organization check fails it is used anyway, so that no webhook is missed.
     */
    @Test(dataProvider = "activeWebhookLookupRouting")
    public void testGetActiveWebhooksRouting(boolean childOrgSubscriptionEnabled, boolean isSubOrganization,
                                             boolean isSubOrganizationCheckFails, boolean expectWidenedLookup)
            throws Exception {

        String eventProfileName = "profile";
        String eventProfileVersion = "v1";
        String channelUri = "schemas.identity.wso2.org/events/logins";
        List<Webhook> tenantOnly = Collections.singletonList(mock(Webhook.class));
        List<Webhook> withChildOrgs = new ArrayList<>();
        withChildOrgs.add(mock(Webhook.class));
        withChildOrgs.add(mock(Webhook.class));
        when(webhookManagementDAO.getActiveWebhooks(eventProfileName, eventProfileVersion, channelUri, TENANT_ID))
                .thenReturn(tenantOnly);
        when(webhookManagementDAO.getActiveWebhooksForOrganization(eventProfileName, eventProfileVersion,
                channelUri, TENANT_ID)).thenReturn(withChildOrgs);

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, Answers.CALLS_REAL_METHODS);
             MockedStatic<OrganizationManagementUtil> organizationManagementUtil =
                     mockStatic(OrganizationManagementUtil.class)) {
            stubChildOrganizationSubscriptionEnabled(identityUtil, childOrgSubscriptionEnabled);
            if (isSubOrganizationCheckFails) {
                organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_ID))
                        .thenThrow(new OrganizationManagementException("error"));
            } else {
                organizationManagementUtil.when(() -> OrganizationManagementUtil.isOrganization(TENANT_ID))
                        .thenReturn(isSubOrganization);
            }

            List<Webhook> result = webhookManagementService.getActiveWebhooks(
                    eventProfileName, eventProfileVersion, channelUri, TENANT_DOMAIN);

            if (expectWidenedLookup) {
                assertEquals(result, withChildOrgs);
                verify(webhookManagementDAO, never()).getActiveWebhooks(anyString(), anyString(), anyString(),
                        anyInt());
            } else {
                assertEquals(result, tenantOnly);
                verify(webhookManagementDAO, never()).getActiveWebhooksForOrganization(anyString(),
                        anyString(), anyString(), anyInt());
            }
            if (!childOrgSubscriptionEnabled) {
                organizationManagementUtil.verify(() -> OrganizationManagementUtil.isOrganization(anyInt()),
                        never());
            }
        }
    }

    /**
     * While child organization subscriptions are disabled, reading the organizations subscribed to a channel is
     * rejected before anything is read from the subscription store.
     */
    @Test
    public void testChannelOrganizationSubscriptionOperationsRejectedWhenDisabled() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            assertRejectedAsDisabled(() -> webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID,
                    "channel-id", TENANT_DOMAIN, 0, 10));
        }
        verify(webhookManagementDAO, never()).getChannelUuids(anyString(), anyInt());
        verify(webhookManagementDAO, never()).addChannelOrgSubscriptions(any());
        verify(webhookManagementDAO, never()).deleteChannelOrgSubscriptions(anyString());
    }

    /**
     * A webhook carrying organization subscriptions is still created while the feature is disabled; the
     * subscriptions are ignored rather than applied, so the APIs behave as they did before the feature.
     */
    @Test
    public void testCreateWebhookIgnoresOrganizationSubscriptionsWhenDisabled() throws Exception {

        Webhook inputWebhook = mock(Webhook.class);
        when(inputWebhook.getEndpoint()).thenReturn("https://test.com/webhook-org-sub");
        when(inputWebhook.getName()).thenReturn("name");
        when(inputWebhook.getSecret()).thenReturn("aBcD1234_efGh5678~IjKl9012+MnOpQR");
        when(inputWebhook.getEventProfileName()).thenReturn("profile");
        when(inputWebhook.getEventProfileUri()).thenReturn("uri");
        when(inputWebhook.getEventsSubscribed()).thenReturn(Collections.singletonList(
                Subscription.builder().channelUri("schemas.identity.wso2.org/events/logins").build()));
        when(inputWebhook.getOrganizationSubscriptions())
                .thenReturn(Collections.singletonList(mock(OrganizationSubscription.class)));

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            webhookManagementService.createWebhook(inputWebhook, TENANT_DOMAIN);
        }
        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(TENANT_ID), eq(Collections.emptyList()));
        verify(webhookManagementDAO, never()).addChannelOrgSubscriptions(any());
        verify(webhookManagementDAO, never()).deleteChannelOrgSubscriptions(anyString());
    }

    /**
     * Deleting a webhook reads its channels whether or not the feature is enabled: a channel can hold a policy
     * written while it was on, and nothing cascades to that policy from the identity database.
     */
    @Test
    public void testDeleteWebhookReadsChannelsWhenDisabled() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);
        }
        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO).getChannelUuids(WEBHOOK_ID, TENANT_ID);
        verify(webhookManagementDAO, never()).getSubscribedOrgTenantIds(anyString());
    }


    @Test
    public void testGetWebhookChannelsReportsNoChannelsWhileDisabled() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            assertTrue(webhookManagementService.getWebhookChannels(WEBHOOK_ID, TENANT_DOMAIN).isEmpty());
        }
        verify(webhookManagementDAO, never()).getWebhookEvents(anyString(), anyInt());
    }

    @Test
    public void testGetWebhookChannelsBuildsOneEntryPerSubscribedChannel() throws Exception {

        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(Arrays.asList(
                Subscription.builder().channelUri(CHANNEL_URI)
                        .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build(),
                Subscription.builder().channelUri(OTHER_CHANNEL_URI)
                        .status(SubscriptionStatus.SUBSCRIPTION_PENDING).build()));
        when(webhookManagementDAO.getChannelUuid(WEBHOOK_ID, CHANNEL_URI, TENANT_ID)).thenReturn(CHANNEL_ID);
        when(webhookManagementDAO.getChannelUuid(WEBHOOK_ID, OTHER_CHANNEL_URI, TENANT_ID))
                .thenReturn(OTHER_CHANNEL_ID);
        OrganizationManager organizationManager = stubOwnerWithoutPolicy();
        when(webhookManagementDAO.getChannelOrgSubscriptionCount(CHANNEL_ID)).thenReturn(2);
        when(webhookManagementDAO.getChannelOrgSubscriptionCount(OTHER_CHANNEL_ID)).thenReturn(0);

        List<WebhookChannel> channels;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            channels = webhookManagementService.getWebhookChannels(WEBHOOK_ID, TENANT_DOMAIN);
        }

        assertEquals(channels.size(), 2);
        assertEquals(channels.get(0).getChannelId(), CHANNEL_ID);
        assertEquals(channels.get(0).getChannelUri(), CHANNEL_URI);
        assertEquals(channels.get(0).getStatus(), SubscriptionStatus.SUBSCRIPTION_ACCEPTED);
        assertEquals(channels.get(0).getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
        assertEquals(channels.get(1).getChannelId(), OTHER_CHANNEL_ID);
        assertEquals(channels.get(1).getPolicy(), SubscriptionPolicy.NONE);
        // Every channel belongs to the webhook, so its owning organization is resolved once for all of them.
        verify(organizationManager, times(1)).resolveOrganizationId(TENANT_DOMAIN);
    }

    @Test
    public void testGetWebhookChannelsWithoutChannelsSkipsTheOwnerLookup() throws Exception {

        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(Collections.emptyList());
        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            assertTrue(webhookManagementService.getWebhookChannels(WEBHOOK_ID, TENANT_DOMAIN).isEmpty());
        }
        verifyNoInteractions(organizationManager);
    }

    /**
     * Without the owning organization an ALL channel cannot be told from a SELECTED one, so the listing fails
     * rather than guess a policy that a client could save back.
     */
    @Test
    public void testGetWebhookChannelsFailsWhenTheOwningOrganizationCannotBeResolved() throws Exception {

        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(Collections.singletonList(
                Subscription.builder().channelUri(CHANNEL_URI).build()));
        when(webhookManagementDAO.getChannelUuid(WEBHOOK_ID, CHANNEL_URI, TENANT_ID)).thenReturn(CHANNEL_ID);
        stubOwnerLookupFailure();

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.getWebhookChannels(WEBHOOK_ID, TENANT_DOMAIN);
            fail("The channels must not be reported without their owning organization.");
        } catch (WebhookMgtException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR.getCode());
        }
        verify(webhookManagementDAO, never()).getChannelOrgSubscriptionCount(anyString());
    }

    /**
     * A subscription whose channel carries no identity cannot be addressed, so it is left out rather than
     * reported with a null id.
     */
    @Test
    public void testGetWebhookChannelsSkipsAChannelWithoutAnIdentity() throws Exception {

        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(Arrays.asList(
                Subscription.builder().channelUri(CHANNEL_URI).build(),
                Subscription.builder().channelUri(OTHER_CHANNEL_URI).build()));
        when(webhookManagementDAO.getChannelUuid(WEBHOOK_ID, CHANNEL_URI, TENANT_ID)).thenReturn(null);
        when(webhookManagementDAO.getChannelUuid(WEBHOOK_ID, OTHER_CHANNEL_URI, TENANT_ID))
                .thenReturn(OTHER_CHANNEL_ID);
        stubOwnerWithoutPolicy();

        List<WebhookChannel> channels;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            channels = webhookManagementService.getWebhookChannels(WEBHOOK_ID, TENANT_DOMAIN);
        }

        assertEquals(channels.size(), 1);
        assertEquals(channels.get(0).getChannelId(), OTHER_CHANNEL_ID);
    }

    /**
     * A channel is named by a column the migration adds, so while the feature is disabled a request naming one
     * is answered as a missing channel rather than attempted.
     */
    @Test
    public void testGetWebhookChannelIsNotFoundWhileDisabled() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN);
            fail("A channel must not be reported while child organization subscriptions are disabled.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND.getCode());
        }
        verify(webhookManagementDAO, never()).getChannelUri(anyString());
    }

    @Test
    public void testGetWebhookChannelCarriesStatusAndPolicy() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(WEBHOOK_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(CHANNEL_ID));
        when(webhookManagementDAO.getChannelUri(CHANNEL_ID)).thenReturn(CHANNEL_URI);
        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(
                Collections.singletonList(Subscription.builder().channelUri(CHANNEL_URI)
                        .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build()));
        stubOwnerWithoutPolicy();
        when(webhookManagementDAO.getChannelOrgSubscriptionCount(CHANNEL_ID)).thenReturn(0);

        WebhookChannel channel;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            channel = webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN);
        }

        assertEquals(channel.getChannelId(), CHANNEL_ID);
        assertEquals(channel.getChannelUri(), CHANNEL_URI);
        assertEquals(channel.getStatus(), SubscriptionStatus.SUBSCRIPTION_ACCEPTED);
        assertEquals(channel.getPolicy(), SubscriptionPolicy.NONE);
    }

    /**
     * The webhook carries no subscription for the channel, which is reported as no status rather than as a
     * status of its own.
     */
    @Test
    public void testGetWebhookChannelReportsNoStatusWhenTheWebhookCarriesNone() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(WEBHOOK_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(CHANNEL_ID));
        when(webhookManagementDAO.getChannelUri(CHANNEL_ID)).thenReturn(CHANNEL_URI);
        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(
                Collections.singletonList(Subscription.builder().channelUri(OTHER_CHANNEL_URI).build()));
        stubOwnerWithoutPolicy();

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            assertNull(webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN)
                    .getStatus());
        }
    }

    /**
     * A request naming a webhook that does not exist is told the webhook is missing, not the channel, which
     * would name the wrong resource.
     */
    @Test
    public void testGetWebhookChannelFailsWhenTheWebhookDoesNotExist() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(null);

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN);
            fail("A channel of a webhook that does not exist must not be reported.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_WEBHOOK_NOT_FOUND.getCode());
        }
        verify(webhookManagementDAO, never()).getChannelUuids(anyString(), anyInt());
    }

    @Test
    public void testGetWebhookChannelFailsWhenTheChannelBelongsToAnotherWebhook() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(WEBHOOK_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(OTHER_CHANNEL_ID));

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN);
            fail("A channel of another webhook must not be reported.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_CHANNEL_NOT_FOUND.getCode());
        }
    }

    /**
     * The page is cut from the stored rows, and the total and the SELECTED policy come from those same rows,
     * so the page runs no separate count query.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsReturnsTheRequestedPage() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptions(CHANNEL_ID)).thenReturn(Arrays.asList(
                subscriptionOf(ORG_ID_1, ORG_TENANT_ID_1), subscriptionOf(ORG_ID_2, ORG_TENANT_ID_2)));
        stubOwnerWithoutPolicy();
        stubOrganizationName(ORG_ID_2, ORG_TENANT_ID_2, "sub002", ORG_ID_1);

        ChannelSubscribedOrganizations page;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            page = webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 1, 1);
        }

        assertEquals(page.getChannelUri(), CHANNEL_URI);
        assertEquals(page.getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
        assertEquals(page.getTotalCount(), 2);
        assertEquals(page.getOrganizations().size(), 1);
        assertEquals(page.getOrganizations().get(0).getSubscribedOrgId(), ORG_ID_2);
        assertEquals(page.getOrganizations().get(0).getOrgName(), "sub002");
        assertEquals(page.getOrganizations().get(0).getParentOrgId(), ORG_ID_1);
        verify(webhookManagementDAO, never()).getChannelOrgSubscriptionCount(anyString());
    }

    /**
     * A subscription is a stored fact, so one whose organization cannot be named is returned without a name
     * rather than dropped from the page.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsResolvesNamesBestEffort() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptions(CHANNEL_ID))
                .thenReturn(Collections.singletonList(subscriptionOf(ORG_ID_1, ORG_TENANT_ID_1)));
        OrganizationManager organizationManager = stubOwnerWithoutPolicy();
        when(organizationManager.getMinimalOrganization(anyString(), any()))
                .thenThrow(new OrganizationManagementException("cannot resolve"));

        ChannelSubscribedOrganizations page;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            page = webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 0, 10);
        }

        assertEquals(page.getOrganizations().size(), 1);
        assertEquals(page.getOrganizations().get(0).getSubscribedOrgId(), ORG_ID_1);
        assertNull(page.getOrganizations().get(0).getOrgName());
    }

    /**
     * An offset past the end is an empty page rather than an error, and the window is capped by what remains
     * so a large limit cannot read past the rows.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsOffsetPastTheEndIsAnEmptyPage() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptions(CHANNEL_ID))
                .thenReturn(Collections.singletonList(subscriptionOf(ORG_ID_1, ORG_TENANT_ID_1)));
        stubOwnerWithoutPolicy();

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            assertTrue(webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 5, Integer.MAX_VALUE).getOrganizations().isEmpty());
            assertEquals(webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 0, Integer.MAX_VALUE).getOrganizations().size(), 1);
        }
    }

    @Test
    public void testGetChannelOrganizationSubscriptionsWithoutRowsReportsNone() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptions(CHANNEL_ID)).thenReturn(Collections.emptyList());
        stubOwnerWithoutPolicy();

        ChannelSubscribedOrganizations page;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            page = webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 0, 10);
        }

        assertEquals(page.getPolicy(), SubscriptionPolicy.NONE);
        assertEquals(page.getTotalCount(), 0);
        verify(webhookManagementDAO, never()).getChannelOrgSubscriptionCount(anyString());
    }

    /**
     * Paging arguments are checked as on the webhook list, before anything is read: a negative limit must not
     * mean every organization, nor a negative offset the first page.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsRejectsInvalidPagingArguments() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            assertRejectedPaging(-1, 10);
            assertRejectedPaging(0, 0);
            assertRejectedPaging(0, -1);
        }
        verify(webhookManagementDAO, never()).getChannelOrgSubscriptions(anyString());
    }

    private void assertRejectedPaging(int offset, int limit) throws WebhookMgtException {

        try {
            webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN,
                    offset, limit);
            fail("Paging with offset " + offset + " and limit " + limit + " must be rejected.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_INVALID_REQUEST.getCode());
        }
    }

    /**
     * A channel holding an organization subscription policy is reported as fanning out to every organization,
     * whatever the rows currently materialised for it say.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsReportsTheAllPolicyFromTheStoredPolicy() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptions(CHANNEL_ID)).thenReturn(Collections.emptyList());
        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(OWNER_ORG_ID);
        ResourceSharingPolicyHandlerService sharingService = mock(ResourceSharingPolicyHandlerService.class);
        when(holder.getResourceSharingPolicyHandlerService()).thenReturn(sharingService);
        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(),
                anyString())).thenReturn(Optional.of(mock(ResourceSharingPolicy.class)));

        ChannelSubscribedOrganizations page;
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            page = webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID,
                    TENANT_DOMAIN, 0, 10);
        }

        assertNotNull(page);
        assertEquals(page.getPolicy(), SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS);
        assertTrue(page.getOrganizations().isEmpty());
        verify(sharingService).getResourceSharingPolicyByResourceKeys(ResourceType.WEBHOOK_CHANNEL.name(),
                CHANNEL_ID, OWNER_ORG_ID, OWNER_ORG_ID);
    }

    /**
     * A failed owner lookup raises rather than falling back to the stored rows, which would report an ALL
     * channel as SELECTED and let a client that saves it back clear the policy.
     */
    @Test
    public void testGetChannelOrganizationSubscriptionsFailsWhenTheOwningOrganizationCannotBeResolved()
            throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getChannelOrgSubscriptionCount(CHANNEL_ID)).thenReturn(2);
        stubOwnerLookupFailure();

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN,
                    0, 10);
            fail("The policy must not be reported without the owning organization.");
        } catch (WebhookMgtException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR.getCode());
        }
    }

    @Test
    public void testGetWebhookChannelFailsWhenTheOwningOrganizationCannotBeResolved() throws Exception {

        stubChannelOf(WEBHOOK_ID, CHANNEL_ID);
        when(webhookManagementDAO.getWebhookEvents(WEBHOOK_ID, TENANT_ID)).thenReturn(Collections.singletonList(
                Subscription.builder().channelUri(CHANNEL_URI).build()));
        stubOwnerLookupFailure();

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.getWebhookChannel(WEBHOOK_ID, CHANNEL_ID, TENANT_DOMAIN);
            fail("The channel must not be reported without its owning organization.");
        } catch (WebhookMgtException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_SUB_ORG_HIERARCHY_ERROR.getCode());
        }
    }

    private void stubChannelOf(String webhookId, String channelId) throws WebhookMgtException {

        when(webhookManagementDAO.getWebhook(webhookId, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(webhookId, TENANT_ID))
                .thenReturn(Collections.singletonList(channelId));
        when(webhookManagementDAO.getChannelUri(channelId)).thenReturn(CHANNEL_URI);
    }

    private void stubOrganizationName(String orgId, int orgTenantId, String orgName, String parentOrgId)
            throws OrganizationManagementException {

        OrganizationManager organizationManager = holder.getOrganizationManager();
        if (organizationManager == null) {
            organizationManager = mock(OrganizationManager.class);
            when(holder.getOrganizationManager()).thenReturn(organizationManager);
        }
        MinimalOrganization organization = mock(MinimalOrganization.class);
        when(organization.getName()).thenReturn(orgName);
        when(organization.getParentOrganizationId()).thenReturn(parentOrgId);
        when(IdentityTenantUtil.getTenantDomain(orgTenantId)).thenReturn(orgName + ".com");
        when(organizationManager.getMinimalOrganization(eq(orgId), any())).thenReturn(organization);
    }

    private static ChannelOrgSubscription subscriptionOf(String orgId, int orgTenantId) {

        return ChannelOrgSubscription.builder()
                .channelUuid(CHANNEL_ID)
                .subscribedOrgId(orgId)
                .subscribedOrgTenantId(orgTenantId)
                .build();
    }


    /**
     * The selected organizations are planned as the tenants their events are raised in, and every tenant the
     * facade reports as affected has its cached publish-path answer cleared.
     */
    @Test
    public void testCreateWebhookPlansSelectedOrganizationsAsTenants() throws Exception {

        OrganizationManager organizationManager = stubOwnedHierarchy(ORG_ID_1, ORG_ID_2);
        when(webhookManagementDAO.createWebhook(any(Webhook.class), eq(TENANT_ID), any()))
                .thenReturn(new HashSet<>(Arrays.asList(ORG_TENANT_ID_1, ORG_TENANT_ID_2)));
        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Arrays.asList(ORG_ID_1, ORG_ID_2))
                .build());

        ActiveWebhooksCache activeWebhooksCache = mock(ActiveWebhooksCache.class);
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true);
             MockedStatic<ActiveWebhooksCache> cache = mockActiveWebhooksCache(activeWebhooksCache)) {
            webhookManagementService.createWebhook(inputWebhook, TENANT_DOMAIN);
        }

        verify(organizationManager).resolveOrganizationId(TENANT_DOMAIN);
        PlannedOrganizationSubscription planned = capturedPlannedSubscription();
        assertEquals(planned.getChannelUri(), CHANNEL_URI);
        assertEquals(planned.getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
        assertEquals(planned.getOwningOrgId(), OWNER_ORG_ID);
        assertEquals(planned.getTenantIdsByOrgId().get(ORG_ID_1), Integer.valueOf(ORG_TENANT_ID_1));
        assertEquals(planned.getTenantIdsByOrgId().get(ORG_ID_2), Integer.valueOf(ORG_TENANT_ID_2));
        verify(activeWebhooksCache).clear(ORG_TENANT_ID_1);
        verify(activeWebhooksCache).clear(ORG_TENANT_ID_2);
    }

    /**
     * ALL_EXISTING_AND_FUTURE_ORGS is planned as every current descendant; the policy for later organizations is
     * the facade's to write.
     */
    @Test
    public void testCreateWebhookPlansEveryDescendantForAllOrganizations() throws Exception {

        stubOwnedHierarchy(ORG_ID_1, ORG_ID_2);
        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS)
                .build());

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.createWebhook(inputWebhook, TENANT_DOMAIN);
        }

        PlannedOrganizationSubscription planned = capturedPlannedSubscription();
        assertEquals(planned.getPolicy(), SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS);
        assertEquals(planned.getTenantIdsByOrgId().keySet(), new HashSet<>(Arrays.asList(ORG_ID_1, ORG_ID_2)));
    }

    /**
     * NONE is planned with no organizations, which is how organization fanout is turned off.
     */
    @Test
    public void testCreateWebhookPlansNoOrganizationsForNonePolicy() throws Exception {

        stubOwnedHierarchy(ORG_ID_1);
        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI).policy(SubscriptionPolicy.NONE).build());

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.createWebhook(inputWebhook, TENANT_DOMAIN);
        }

        PlannedOrganizationSubscription planned = capturedPlannedSubscription();
        assertEquals(planned.getPolicy(), SubscriptionPolicy.NONE);
        assertTrue(planned.getTenantIdsByOrgId().isEmpty());
    }

    /**
     * Organization ids resolve globally, so an id from an unrelated hierarchy is rejected rather than written
     * as a subscriber of this webhook.
     */
    @Test
    public void testCreateWebhookRejectsAnOrganizationOutsideTheHierarchy() throws Exception {

        stubOwnedHierarchy(ORG_ID_1);
        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Collections.singletonList(FOREIGN_ORG_ID))
                .build());

        assertRejected(inputWebhook, ErrorMessage.ERROR_CODE_SUB_ORG_NOT_IN_HIERARCHY);
        verify(webhookManagementDAO, never()).addChannelOrgSubscriptions(any());
    }

    /**
     * A selection has to select something: an empty one is the same state as NONE, so it is rejected rather
     * than accepted as a second spelling of it.
     */
    @Test
    public void testCreateWebhookRejectsASelectionThatSelectsNothing() throws Exception {

        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Collections.emptyList())
                .build());

        assertRejected(inputWebhook, ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SELECTED_ORGS_REQUIRED);
    }

    /**
     * Checked before the webhook row is written, so a subscription naming a channel the webhook does not
     * subscribe to cannot leave a created webhook behind.
     */
    @Test
    public void testCreateWebhookRejectsASubscriptionForAnUnsubscribedChannel() throws Exception {

        Webhook inputWebhook = webhookSubscribing(OrganizationSubscription.builder()
                .channelUri(OTHER_CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Collections.singletonList(ORG_ID_1))
                .build());

        assertRejected(inputWebhook, ErrorMessage.ERROR_CODE_SUB_ORG_CHANNEL_NOT_SUBSCRIBED);
        verify(webhookManagementDAO, never()).createWebhook(any(), anyInt(), any());
    }

    /**
     * Two entries for one channel would have the second undo the policy the first wrote, which a rollback
     * cannot restore, so the request is rejected before anything is planned or written.
     */
    @Test
    public void testCreateWebhookRejectsTwoSubscriptionsForTheSameChannel() throws Exception {

        Webhook inputWebhook = webhookSubscribing(
                OrganizationSubscription.builder()
                        .channelUri(CHANNEL_URI)
                        .policy(SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS)
                        .build(),
                OrganizationSubscription.builder()
                        .channelUri(CHANNEL_URI)
                        .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                        .organizationIds(Collections.singletonList(ORG_ID_1))
                        .build());

        assertRejected(inputWebhook, ErrorMessage.ERROR_CODE_SUB_ORG_DUPLICATE_CHANNEL);
        verify(webhookManagementDAO, never()).createWebhook(any(), anyInt(), any());
    }

    @Test
    public void testUpdateWebhookRejectsTwoSubscriptionsForTheSameChannel() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        Webhook inputWebhook = webhookSubscribing(
                OrganizationSubscription.builder()
                        .channelUri(CHANNEL_URI)
                        .policy(SubscriptionPolicy.NONE)
                        .build(),
                OrganizationSubscription.builder()
                        .channelUri(CHANNEL_URI)
                        .policy(SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS)
                        .build());

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.updateWebhook(WEBHOOK_ID, inputWebhook, TENANT_DOMAIN);
            fail("A webhook naming one channel twice must be rejected.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_SUB_ORG_DUPLICATE_CHANNEL.getCode());
        }
        verify(webhookManagementDAO, never()).updateWebhook(anyString(), any(), anyInt(), any());
    }

    /**
     * The channel rows are gone once the webhook is deleted, so the policies they hold are removed explicitly
     * and the tenants that were subscribed have their cached publish-path answer cleared.
     */
    @Test
    public void testDeleteWebhookClearsPoliciesAndTheCacheOfSubscribedTenants() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(WEBHOOK_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(CHANNEL_ID));
        when(webhookManagementDAO.getSubscribedOrgTenantIds(CHANNEL_ID))
                .thenReturn(Arrays.asList(ORG_TENANT_ID_1, ORG_TENANT_ID_2));
        ResourceSharingPolicyHandlerService sharingService = stubNoPolicyHeld();

        ActiveWebhooksCache activeWebhooksCache = mock(ActiveWebhooksCache.class);
        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true);
             MockedStatic<ActiveWebhooksCache> cache = mockActiveWebhooksCache(activeWebhooksCache)) {
            webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);
        }

        // Every organization the channel now reaches holds a stale publish-path answer.
        verify(activeWebhooksCache).clear(ORG_TENANT_ID_1);
        verify(activeWebhooksCache).clear(ORG_TENANT_ID_2);
        verify(webhookManagementDAO).getSubscribedOrgTenantIds(CHANNEL_ID);
        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(any(), eq(CHANNEL_ID));
    }

    /**
     * Clearing a policy is best effort: the webhook is already gone, so a policy that cannot be removed is
     * left for reconciliation rather than failing the deletion.
     */
    @Test
    public void testDeleteWebhookSurvivesAPolicyThatCannotBeCleared() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));
        when(webhookManagementDAO.getChannelUuids(WEBHOOK_ID, TENANT_ID))
                .thenReturn(Collections.singletonList(CHANNEL_ID));
        ResourceSharingPolicyHandlerService sharingService = stubNoPolicyHeld();
        doThrow(new ResourceSharingPolicyMgtException("ERROR_CODE", "cannot delete", "cannot delete"))
                .when(sharingService)
                .deleteResourceSharingPolicyByResourceTypeAndId(any(), eq(CHANNEL_ID));

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);
        }

        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
    }

    /**
     * A new organization is subscribed to every live channel its ancestors hold a policy for. Its tenant is
     * resolved once however many channels match, and only that tenant's cache is cleared.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSubscribeNewOrganizationSubscribesToEveryAncestorPolicyChannel() throws Exception {

        OrganizationManager organizationManager = stubNewOrganizationUnder(OWNER_ORG_ID);
        stubPoliciesHeldBy(OWNER_ORG_ID, CHANNEL_ID, OTHER_CHANNEL_ID);
        when(webhookManagementDAO.getChannelUri(CHANNEL_ID)).thenReturn(CHANNEL_URI);
        when(webhookManagementDAO.getChannelUri(OTHER_CHANNEL_ID)).thenReturn(OTHER_CHANNEL_URI);

        ActiveWebhooksCache activeWebhooksCache = mock(ActiveWebhooksCache.class);
        try (MockedStatic<ActiveWebhooksCache> cache = mockActiveWebhooksCache(activeWebhooksCache)) {
            webhookManagementService.subscribeNewOrganization(ORG_ID_1);
        }

        ArgumentCaptor<List<ChannelOrgSubscription>> captor = ArgumentCaptor.forClass(List.class);
        verify(webhookManagementDAO).addChannelOrgSubscriptions(captor.capture());
        List<String> subscribedChannels = new ArrayList<>();
        for (ChannelOrgSubscription subscription : captor.getValue()) {
            subscribedChannels.add(subscription.getChannelUuid());
            assertEquals(subscription.getSubscribedOrgId(), ORG_ID_1);
            assertEquals(subscription.getSubscribedOrgTenantId(), ORG_TENANT_ID_1);
        }
        assertEquals(subscribedChannels, Arrays.asList(CHANNEL_ID, OTHER_CHANNEL_ID));
        verify(organizationManager, times(1)).resolveTenantDomain(ORG_ID_1);
        verify(activeWebhooksCache).clear(ORG_TENANT_ID_1);
    }

    /**
     * A policy can outlive its channel, so a channel that no longer exists is skipped while the others are
     * still subscribed.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSubscribeNewOrganizationSkipsAChannelThatNoLongerExists() throws Exception {

        stubNewOrganizationUnder(OWNER_ORG_ID);
        stubPoliciesHeldBy(OWNER_ORG_ID, CHANNEL_ID, OTHER_CHANNEL_ID);
        when(webhookManagementDAO.getChannelUri(CHANNEL_ID)).thenReturn(CHANNEL_URI);
        when(webhookManagementDAO.getChannelUri(OTHER_CHANNEL_ID)).thenReturn(null);

        try (MockedStatic<ActiveWebhooksCache> cache = mockActiveWebhooksCache(mock(ActiveWebhooksCache.class))) {
            webhookManagementService.subscribeNewOrganization(ORG_ID_1);
        }

        ArgumentCaptor<List<ChannelOrgSubscription>> captor = ArgumentCaptor.forClass(List.class);
        verify(webhookManagementDAO).addChannelOrgSubscriptions(captor.capture());
        assertEquals(captor.getValue().size(), 1);
        assertEquals(captor.getValue().get(0).getChannelUuid(), CHANNEL_ID);
    }

    @Test
    public void testSubscribeNewOrganizationWithoutAncestorPoliciesWritesNothing() throws Exception {

        OrganizationManager organizationManager = stubNewOrganizationUnder(OWNER_ORG_ID);
        stubPoliciesHeldBy(OWNER_ORG_ID);

        webhookManagementService.subscribeNewOrganization(ORG_ID_1);

        verify(organizationManager, never()).resolveTenantDomain(anyString());
        verify(webhookManagementDAO, never()).addChannelOrgSubscriptions(any());
    }

    /**
     * ORG_ID_1, of tenant ORG_TENANT_ID_1, created under the given parent organization.
     */
    private OrganizationManager stubNewOrganizationUnder(String parentOrgId) throws Exception {

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.getAncestorOrganizationIds(ORG_ID_1)).thenReturn(Arrays.asList(ORG_ID_1, parentOrgId));
        when(organizationManager.resolveTenantDomain(ORG_ID_1)).thenReturn("sub001.com");
        when(IdentityTenantUtil.getTenantId("sub001.com")).thenReturn(ORG_TENANT_ID_1);
        return organizationManager;
    }

    private void stubPoliciesHeldBy(String orgId, String... channelUuids) throws Exception {

        ResourceSharingPolicyHandlerService sharingService = mock(ResourceSharingPolicyHandlerService.class);
        when(holder.getResourceSharingPolicyHandlerService()).thenReturn(sharingService);
        List<ResourceSharingPolicy> policies = new ArrayList<>();
        for (String channelUuid : channelUuids) {
            ResourceSharingPolicy policy = mock(ResourceSharingPolicy.class);
            when(policy.getResourceId()).thenReturn(channelUuid);
            policies.add(policy);
        }
        when(sharingService.getResourceSharingPoliciesByResourceType(Collections.singletonList(orgId),
                ResourceType.WEBHOOK_CHANNEL.name())).thenReturn(policies);
    }

    /**
     * The service clears the cache through its singleton, and the real cache resolves the tenant from the
     * carbon context, which a unit test does not carry.
     */
    private static MockedStatic<ActiveWebhooksCache> mockActiveWebhooksCache(ActiveWebhooksCache cache) {

        MockedStatic<ActiveWebhooksCache> cacheMock = mockStatic(ActiveWebhooksCache.class);
        cacheMock.when(ActiveWebhooksCache::getInstance).thenReturn(cache);
        return cacheMock;
    }

    private OrganizationManager stubOwnedHierarchy(String... descendantOrgIds) throws Exception {

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(OWNER_ORG_ID);
        when(organizationManager.getChildOrganizationsIds(OWNER_ORG_ID, true))
                .thenReturn(Arrays.asList(descendantOrgIds));
        when(organizationManager.resolveTenantDomain(ORG_ID_1)).thenReturn("sub001.com");
        when(organizationManager.resolveTenantDomain(ORG_ID_2)).thenReturn("sub002.com");
        when(IdentityTenantUtil.getTenantId("sub001.com")).thenReturn(ORG_TENANT_ID_1);
        when(IdentityTenantUtil.getTenantId("sub002.com")).thenReturn(ORG_TENANT_ID_2);
        return organizationManager;
    }

    private ResourceSharingPolicyHandlerService stubNoPolicyHeld() throws Exception {

        ResourceSharingPolicyHandlerService sharingService = mock(ResourceSharingPolicyHandlerService.class);
        when(holder.getResourceSharingPolicyHandlerService()).thenReturn(sharingService);
        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(),
                anyString())).thenReturn(Optional.empty());
        return sharingService;
    }

    /**
     * The webhook's owning organization resolves from the request's tenant domain and holds no policy.
     */
    private OrganizationManager stubOwnerWithoutPolicy() throws Exception {

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN)).thenReturn(OWNER_ORG_ID);
        stubNoPolicyHeld();
        return organizationManager;
    }

    private void stubOwnerLookupFailure() throws Exception {

        OrganizationManager organizationManager = mock(OrganizationManager.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(organizationManager.resolveOrganizationId(TENANT_DOMAIN))
                .thenThrow(new OrganizationManagementException("cannot resolve"));
    }

    private Webhook webhookSubscribing(OrganizationSubscription... organizationSubscriptions)
            throws WebhookMgtException {

        Webhook webhook = mock(Webhook.class);
        when(webhook.getEndpoint())
                .thenReturn("https://test.com/webhook-" + organizationSubscriptions[0].getPolicy());
        when(webhook.getName()).thenReturn("name");
        when(webhook.getSecret()).thenReturn("aBcD1234_efGh5678~IjKl9012+MnOpQR");
        when(webhook.getEventProfileName()).thenReturn("profile");
        when(webhook.getEventProfileUri()).thenReturn("uri");
        when(webhook.getEventsSubscribed()).thenReturn(Collections.singletonList(
                Subscription.builder().channelUri(CHANNEL_URI).build()));
        when(webhook.getOrganizationSubscriptions()).thenReturn(Arrays.asList(organizationSubscriptions));
        return webhook;
    }

    private void assertRejected(Webhook webhook, ErrorMessage expected) throws WebhookMgtException {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(true)) {
            webhookManagementService.createWebhook(webhook, TENANT_DOMAIN);
            fail("The webhook must be rejected with: " + expected.getCode());
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), expected.getCode());
        }
    }

    @SuppressWarnings("unchecked")
    private PlannedOrganizationSubscription capturedPlannedSubscription() throws WebhookMgtException {

        ArgumentCaptor<List<PlannedOrganizationSubscription>> captor = ArgumentCaptor.forClass(List.class);
        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(TENANT_ID), captor.capture());
        assertEquals(captor.getValue().size(), 1);
        return captor.getValue().get(0);
    }

    /**
     * Unit tests do not load identity.xml, so the feature flag is mocked.
     */
    private static MockedStatic<IdentityUtil> mockChildOrganizationSubscriptionEnabled(boolean enabled) {

        MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class, Answers.CALLS_REAL_METHODS);
        stubChildOrganizationSubscriptionEnabled(identityUtil, enabled);
        return identityUtil;
    }

    private static void stubChildOrganizationSubscriptionEnabled(MockedStatic<IdentityUtil> identityUtil,
                                                                 boolean enabled) {

        identityUtil.when(IdentityUtil::isChildOrganizationSubscriptionEnabled).thenReturn(enabled);
    }

    private static void assertRejectedAsDisabled(ServiceCall call) throws WebhookMgtException {

        try {
            call.run();
            fail("The operation must be rejected while child organization subscriptions are disabled.");
        } catch (WebhookMgtClientException e) {
            assertEquals(e.getErrorCode(), ErrorMessage.ERROR_CODE_CHILD_ORG_SUBSCRIPTION_DISABLED.getCode());
        }
    }

    @FunctionalInterface
    private interface ServiceCall {

        void run() throws WebhookMgtException;
    }
}
