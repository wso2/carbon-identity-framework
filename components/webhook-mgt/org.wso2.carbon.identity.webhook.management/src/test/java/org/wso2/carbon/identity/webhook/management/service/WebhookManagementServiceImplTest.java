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
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.component.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.OrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.WebhookChannelService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookManagementServiceImpl;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.Channel;
import org.wso2.carbon.identity.webhook.metadata.api.model.EventProfile;
import org.wso2.carbon.identity.webhook.metadata.api.service.WebhookMetadataService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

@WithCarbonHome
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class WebhookManagementServiceImplTest {

    private WebhookManagementServiceImpl webhookManagementService;
    private WebhookManagementDAO webhookManagementDAO;
    private WebhookChannelService webhookChannelService;

    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;
    private MockedStatic<WebhookManagementComponentServiceHolder> webhookComponentHolderMockedStatic;
    private WebhookMetadataService webhookMetadataService;
    private WebhookManagementComponentServiceHolder holder;

    public static final String WEBHOOK_ID = "webhookId";
    public static final String TENANT_DOMAIN = "test.com";
    public static final int TENANT_ID = 1;
    public static final String WEBHOOK_ENDPOINT = "https://example.com/webhook";
    private static final String WEBSUBHUBADAPTER = "webSubHubAdapter";

    @BeforeClass
    public void setUpClass() throws WebhookMetadataException {

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(TENANT_ID);

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

        // The service resolves a webhook's channels through this collaborator, which it constructs itself.
        // Left real, it reaches a datasource that does not exist under test.
        webhookChannelService = mock(WebhookChannelService.class);
        Field channelServiceField =
                WebhookManagementServiceImpl.class.getDeclaredField("webhookChannelService");
        channelServiceField.setAccessible(true);
        channelServiceField.set(webhookManagementService, webhookChannelService);
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

        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(1));
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
     * Only a sub-organization can be subscribed to an ancestor's webhook, so the lookup that includes
     * subscribed child organizations runs only for sub-organizations, and only when the feature is enabled.
     * If the sub-organization check fails, the wider lookup is used so that no webhook is missed.
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
        when(webhookManagementDAO.getActiveWebhooksWithSubscribedChildOrgs(eventProfileName, eventProfileVersion,
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
                verify(webhookManagementDAO, never()).getActiveWebhooksWithSubscribedChildOrgs(anyString(),
                        anyString(), anyString(), anyInt());
            }
            if (!childOrgSubscriptionEnabled) {
                organizationManagementUtil.verify(() -> OrganizationManagementUtil.isOrganization(anyInt()),
                        never());
            }
        }
    }

    /**
     * While child organization subscriptions are disabled, the channel-level organization subscription operations
     * are rejected before anything is read from or written to the subscription store.
     */
    @Test
    public void testChannelOrganizationSubscriptionOperationsRejectedWhenDisabled() throws Exception {

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            assertRejectedAsDisabled(() -> webhookManagementService.updateChannelSubscription(WEBHOOK_ID,
                    "channel-id", SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS, null, TENANT_DOMAIN));
            assertRejectedAsDisabled(() -> webhookManagementService.getChannelOrganizationSubscriptions(WEBHOOK_ID,
                    "channel-id", TENANT_DOMAIN, 0, 10));
        }
        verify(webhookChannelService, never()).getChannelUuids(anyString(), anyInt());
        verify(webhookChannelService, never()).setSubscriptionPolicy(any(), any(), any(), any());
    }

    /**
     * A webhook that carries organization subscriptions is still created while the feature is disabled. The
     * subscriptions are ignored rather than applied, so that the webhook APIs keep working as they did before the
     * feature existed.
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
        verify(webhookManagementDAO).createWebhook(any(Webhook.class), eq(TENANT_ID));
        verify(webhookChannelService, never()).setSubscriptionPolicy(any(), any(), any(), any());
    }

    /**
     * Deleting a webhook while the feature is disabled does not touch the channel UUIDs or the subscription store,
     * which may not exist yet.
     */
    @Test
    public void testDeleteWebhookSkipsOrganizationSubscriptionsWhenDisabled() throws Exception {

        when(webhookManagementDAO.getWebhook(WEBHOOK_ID, TENANT_ID)).thenReturn(mock(Webhook.class));

        try (MockedStatic<IdentityUtil> identityUtil = mockChildOrganizationSubscriptionEnabled(false)) {
            webhookManagementService.deleteWebhook(WEBHOOK_ID, TENANT_DOMAIN);
        }
        verify(webhookManagementDAO).deleteWebhook(WEBHOOK_ID, TENANT_ID);
        verify(webhookChannelService, never()).getChannelUuids(anyString(), anyInt());
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
