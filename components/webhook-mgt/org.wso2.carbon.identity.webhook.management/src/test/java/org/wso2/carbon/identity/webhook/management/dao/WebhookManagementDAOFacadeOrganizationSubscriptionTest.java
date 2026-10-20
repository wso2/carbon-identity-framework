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
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.secret.mgt.core.SecretManager;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManager;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.dao.PlannedOrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOFacade;
import org.wso2.carbon.identity.webhook.management.internal.dao.impl.WebhookManagementDAOImpl;
import org.wso2.carbon.identity.webhook.metadata.api.model.Adapter;
import org.wso2.carbon.identity.webhook.metadata.api.model.AdapterType;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for the facade's organization subscription unit of work, run against the real DAO on H2 so that the
 * transaction, the stored rows and the rollback are the database's own.
 */
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithCarbonHome
public class WebhookManagementDAOFacadeOrganizationSubscriptionTest {

    private static final int TENANT_ID = 3;
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String CHANNEL_CREATE = "user.create";
    private static final String CHANNEL_UPDATE = "user.update";
    private static final String MISSING_CHANNEL = "user.delete";
    private static final String OWNER_ORG_ID = "0e4a3c92-1f7b-4d58-9a06-b3e5c1d78f24";
    private static final String ORG_ID_1 = "b028ca17-8f89-449c-ae27-fa955e66465d";
    private static final String ORG_ID_2 = "a17b28ca-9f89-449c-ae27-fa955e66465f";
    private static final int ORG_TENANT_ID_1 = 11;
    private static final int ORG_TENANT_ID_2 = 12;

    private MockedStatic<IdentityTenantUtil> identityTenantUtil;
    private MockedStatic<IdentityUtil> identityUtil;
    private MockedStatic<WebhookManagementComponentServiceHolder> componentHolder;
    private ResourceSharingPolicyHandlerService sharingService;
    private WebhookManagementDAOFacade daoFacade;

    @BeforeMethod
    public void setUp() throws Exception {

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantDomain(TENANT_ID)).thenReturn(TENANT_DOMAIN);
        identityUtil = mockStatic(IdentityUtil.class, Answers.CALLS_REAL_METHODS);
        identityUtil.when(() -> IdentityUtil.getProperty(WebhookManagementDAOImpl.CHANNEL_UUID_COLUMN_AVAILABLE))
                .thenReturn("true");

        WebhookManagementComponentServiceHolder holder = mock(WebhookManagementComponentServiceHolder.class);
        componentHolder = mockStatic(WebhookManagementComponentServiceHolder.class);
        componentHolder.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

        // The Publisher adapter, since the PublisherSubscriber one does not support updating a webhook.
        Adapter adapter = mock(Adapter.class);
        when(adapter.getType()).thenReturn(AdapterType.Publisher);
        when(holder.getWebhookAdapter()).thenReturn(adapter);

        SecretManager secretManager = mock(SecretManager.class);
        SecretType secretType = mock(SecretType.class);
        when(secretType.getId()).thenReturn("WEBHOOK_SECRETS");
        when(secretManager.getSecretType(anyString())).thenReturn(secretType);
        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(true);
        SecretResolveManager secretResolveManager = mock(SecretResolveManager.class);
        ResolvedSecret resolvedSecret = mock(ResolvedSecret.class);
        when(resolvedSecret.getResolvedSecretValue()).thenReturn("test-secret");
        when(secretResolveManager.getResolvedSecret(anyString(), anyString())).thenReturn(resolvedSecret);
        when(holder.getSecretManager()).thenReturn(secretManager);
        when(holder.getSecretResolveManager()).thenReturn(secretResolveManager);

        sharingService = mock(ResourceSharingPolicyHandlerService.class);
        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(),
                anyString())).thenReturn(Optional.empty());
        when(holder.getResourceSharingPolicyHandlerService()).thenReturn(sharingService);

        daoFacade = new WebhookManagementDAOFacade(new WebhookManagementDAOImpl());
    }

    @AfterMethod
    public void tearDown() {

        identityTenantUtil.close();
        identityUtil.close();
        componentHolder.close();
    }

    @Test
    public void testCreateWebhookWithoutAPlanCreatesTheWebhookAlone() throws Exception {

        Webhook withNullPlan = newWebhook();
        Webhook withEmptyPlan = newWebhook();

        assertTrue(daoFacade.createWebhook(withNullPlan, TENANT_ID, null).isEmpty());
        assertTrue(daoFacade.createWebhook(withEmptyPlan, TENANT_ID, Collections.emptyList()).isEmpty());

        assertNotNull(daoFacade.getWebhook(withNullPlan.getId(), TENANT_ID));
        assertNotNull(daoFacade.getWebhook(withEmptyPlan.getId(), TENANT_ID));
        verify(sharingService, never()).getResourceSharingPolicyByResourceKeys(anyString(), anyString(),
                anyString(), anyString());
    }

    /**
     * The rows hold the tenants the events are raised in, and those tenants are reported as stale.
     */
    @Test
    public void testCreateWebhookStoresSelectedOrganizationsAsTenants() throws Exception {

        Webhook webhook = newWebhook();

        Set<Integer> affected = daoFacade.createWebhook(webhook, TENANT_ID, Collections.singletonList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.SELECTED_ORGS_ONLY, bothOrganizations())));

        String channelUuid = daoFacade.getChannelUuid(webhook.getId(), CHANNEL_CREATE, TENANT_ID);
        assertEquals(storedTenantIds(channelUuid), tenants(ORG_TENANT_ID_1, ORG_TENANT_ID_2));
        assertEquals(affected, tenants(ORG_TENANT_ID_1, ORG_TENANT_ID_2));
        verify(sharingService, never()).addResourceSharingPolicy(any());
    }

    /**
     * ALL stores today's organizations and leaves a policy held by the owner, which covers later ones.
     */
    @Test
    public void testCreateWebhookWritesThePolicyForAllOrganizations() throws Exception {

        Webhook webhook = newWebhook();

        daoFacade.createWebhook(webhook, TENANT_ID, Collections.singletonList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS, bothOrganizations())));

        String channelUuid = daoFacade.getChannelUuid(webhook.getId(), CHANNEL_CREATE, TENANT_ID);
        ArgumentCaptor<ResourceSharingPolicy> written = ArgumentCaptor.forClass(ResourceSharingPolicy.class);
        verify(sharingService).addResourceSharingPolicy(written.capture());
        assertEquals(written.getValue().getResourceType(), ResourceType.WEBHOOK_CHANNEL);
        assertEquals(written.getValue().getResourceId(), channelUuid);
        assertEquals(written.getValue().getPolicyHoldingOrgId(), OWNER_ORG_ID);
        assertEquals(written.getValue().getSharingPolicy(), PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS);
        assertEquals(storedTenantIds(channelUuid), tenants(ORG_TENANT_ID_1, ORG_TENANT_ID_2));
    }

    @Test
    public void testCreateWebhookWithNonePolicyStoresNothing() throws Exception {

        Webhook webhook = newWebhook();

        daoFacade.createWebhook(webhook, TENANT_ID, Collections.singletonList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.NONE, Collections.emptyMap())));

        String channelUuid = daoFacade.getChannelUuid(webhook.getId(), CHANNEL_CREATE, TENANT_ID);
        assertTrue(storedTenantIds(channelUuid).isEmpty());
        verify(sharingService, never()).addResourceSharingPolicy(any());
    }

    /**
     * Rows are replaced rather than added to, a policy the new plan does not want is cleared, and both the old
     * and the new tenants are reported as stale.
     */
    @Test
    public void testUpdateWebhookReplacesTheSelectionAndClearsAPolicyNoLongerWanted() throws Exception {

        Webhook webhook = newWebhook();
        daoFacade.createWebhook(webhook, TENANT_ID, Collections.singletonList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.SELECTED_ORGS_ONLY,
                        organization(ORG_ID_1, ORG_TENANT_ID_1))));
        String channelUuid = daoFacade.getChannelUuid(webhook.getId(), CHANNEL_CREATE, TENANT_ID);
        stubPolicyHeld(channelUuid);

        Set<Integer> affected = daoFacade.updateWebhook(webhook.getId(), webhook, TENANT_ID,
                Collections.singletonList(planned(CHANNEL_CREATE, SubscriptionPolicy.SELECTED_ORGS_ONLY,
                        organization(ORG_ID_2, ORG_TENANT_ID_2))));

        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(ResourceType.WEBHOOK_CHANNEL,
                channelUuid);
        assertEquals(storedTenantIds(channelUuid), tenants(ORG_TENANT_ID_2));
        assertEquals(affected, tenants(ORG_TENANT_ID_1, ORG_TENANT_ID_2));
    }

    @Test
    public void testUpdateWebhookWithoutAPlanUpdatesTheWebhookAlone() throws Exception {

        Webhook webhook = newWebhook();
        daoFacade.createWebhook(webhook, TENANT_ID, null);
        Webhook renamed = new Webhook.Builder()
                .uuid(webhook.getId())
                .endpoint(webhook.getEndpoint())
                .name("renamed")
                .secret("test-secret")
                .eventProfileName(webhook.getEventProfileName())
                .eventProfileUri(webhook.getEventProfileUri())
                .status(WebhookStatus.ACTIVE)
                .eventsSubscribed(webhook.getEventsSubscribed())
                .build();

        assertTrue(daoFacade.updateWebhook(webhook.getId(), renamed, TENANT_ID, null).isEmpty());
        assertEquals(daoFacade.getWebhook(webhook.getId(), TENANT_ID).getName(), "renamed");
    }

    /**
     * A failure part way through rolls the webhook row back with the transaction, while the policy already
     * written in the shared database is removed by hand.
     */
    @Test
    public void testCreateWebhookRollsBackAndUndoesTheWrittenPolicy() throws Exception {

        Webhook webhook = newWebhook();
        List<PlannedOrganizationSubscription> plan = Arrays.asList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS, bothOrganizations()),
                planned(MISSING_CHANNEL, SubscriptionPolicy.SELECTED_ORGS_ONLY, bothOrganizations()));

        assertFailsWith(() -> daoFacade.createWebhook(webhook, TENANT_ID, plan),
                ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR);

        assertNull(daoFacade.getWebhook(webhook.getId(), TENANT_ID));
        ArgumentCaptor<ResourceSharingPolicy> written = ArgumentCaptor.forClass(ResourceSharingPolicy.class);
        verify(sharingService).addResourceSharingPolicy(written.capture());
        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(ResourceType.WEBHOOK_CHANNEL,
                written.getValue().getResourceId());
    }

    /**
     * A policy cleared inside a transaction that then fails is written back, since its rows are restored too.
     */
    @Test
    public void testUpdateWebhookRestoresAClearedPolicyWhenTheTransactionFails() throws Exception {

        Webhook webhook = newWebhook();
        daoFacade.createWebhook(webhook, TENANT_ID, null);
        String channelUuid = daoFacade.getChannelUuid(webhook.getId(), CHANNEL_CREATE, TENANT_ID);
        stubPolicyHeld(channelUuid);
        List<PlannedOrganizationSubscription> plan = Arrays.asList(
                planned(CHANNEL_CREATE, SubscriptionPolicy.SELECTED_ORGS_ONLY, bothOrganizations()),
                planned(MISSING_CHANNEL, SubscriptionPolicy.SELECTED_ORGS_ONLY, bothOrganizations()));

        assertFailsWith(() -> daoFacade.updateWebhook(webhook.getId(), webhook, TENANT_ID, plan),
                ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR);

        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(ResourceType.WEBHOOK_CHANNEL,
                channelUuid);
        ArgumentCaptor<ResourceSharingPolicy> restored = ArgumentCaptor.forClass(ResourceSharingPolicy.class);
        verify(sharingService).addResourceSharingPolicy(restored.capture());
        assertEquals(restored.getValue().getResourceId(), channelUuid);
        assertEquals(restored.getValue().getPolicyHoldingOrgId(), OWNER_ORG_ID);
        assertTrue(storedTenantIds(channelUuid).isEmpty());
    }

    /**
     * A failure that is not a webhook error is reported as the operation's own server error.
     */
    @Test
    public void testCreateWebhookReportsAnUnexpectedFailureAsTheOperationError() throws Exception {

        Webhook webhook = newWebhook();
        when(sharingService.addResourceSharingPolicy(any())).thenThrow(new IllegalStateException("unavailable"));

        assertFailsWith(() -> daoFacade.createWebhook(webhook, TENANT_ID, Collections.singletonList(
                        planned(CHANNEL_UPDATE, SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS, bothOrganizations()))),
                ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR);
        assertNull(daoFacade.getWebhook(webhook.getId(), TENANT_ID));
    }

    private Webhook newWebhook() {

        return new Webhook.Builder()
                .uuid(UUID.randomUUID().toString())
                .endpoint("https://example.com/org-subscription/" + UUID.randomUUID())
                .name("Organization subscription webhook")
                .secret("test-secret")
                .eventProfileName("user-events")
                .eventProfileUri("https://schemas.org/user-events")
                .status(WebhookStatus.ACTIVE)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .eventsSubscribed(Arrays.asList(
                        Subscription.builder().channelUri(CHANNEL_CREATE)
                                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build(),
                        Subscription.builder().channelUri(CHANNEL_UPDATE)
                                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED).build()))
                .build();
    }

    private static PlannedOrganizationSubscription planned(String channelUri, SubscriptionPolicy policy,
                                                           Map<String, Integer> tenantIdsByOrgId) {

        return new PlannedOrganizationSubscription(channelUri, policy, OWNER_ORG_ID, tenantIdsByOrgId);
    }

    private static Map<String, Integer> bothOrganizations() {

        Map<String, Integer> organizations = new LinkedHashMap<>();
        organizations.put(ORG_ID_1, ORG_TENANT_ID_1);
        organizations.put(ORG_ID_2, ORG_TENANT_ID_2);
        return organizations;
    }

    private static Map<String, Integer> organization(String orgId, int tenantId) {

        return Collections.singletonMap(orgId, tenantId);
    }

    private static Set<Integer> tenants(Integer... tenantIds) {

        return new HashSet<>(Arrays.asList(tenantIds));
    }

    private Set<Integer> storedTenantIds(String channelUuid) throws WebhookMgtException {

        return daoFacade.getChannelOrgSubscriptions(channelUuid).stream()
                .map(ChannelOrgSubscription::getSubscribedOrgTenantId)
                .collect(Collectors.toSet());
    }

    private void stubPolicyHeld(String channelUuid) throws Exception {

        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), eq(channelUuid), anyString(),
                anyString())).thenReturn(Optional.of(mock(ResourceSharingPolicy.class)));
    }

    private static void assertFailsWith(FacadeCall call, ErrorMessage expected) {

        try {
            call.run();
            fail("Expected the operation to fail with: " + expected.getCode());
        } catch (WebhookMgtException e) {
            assertEquals(e.getErrorCode(), expected.getCode());
        }
    }

    @FunctionalInterface
    private interface FacadeCall {

        void run() throws WebhookMgtException;
    }
}
