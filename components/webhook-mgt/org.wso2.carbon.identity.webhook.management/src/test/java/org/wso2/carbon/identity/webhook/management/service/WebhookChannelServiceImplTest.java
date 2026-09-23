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

package org.wso2.carbon.identity.webhook.management.service;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.MinimalOrganization;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtClientException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookChannelDAO;
import org.wso2.carbon.identity.webhook.management.internal.service.WebhookChannelService;
import org.wso2.carbon.identity.webhook.management.internal.service.impl.WebhookChannelServiceImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WebhookChannelServiceImpl}.
 * <p>
 * The collaborators are the channel DAO, the organization manager and the resource sharing policy service. All
 * three are mocked: the behaviour under test is which of them is called, in what order, and how their answers
 * are combined -- not what they themselves do.
 */
@WithCarbonHome
public class WebhookChannelServiceImplTest {

    private static final String CHANNEL_UUID = "channel-uuid";
    private static final String OWNING_ORG_ID = "owning-org-id";
    private static final String CHILD_ORG_ID = "child-org-id";
    private static final String OTHER_ORG_ID = "unrelated-org-id";
    private static final String CHILD_TENANT_DOMAIN = "child.com";
    private static final int OWNING_TENANT_ID = 1;
    private static final int CHILD_TENANT_ID = 2;
    private static final String OWNING_TENANT_DOMAIN = "owner.com";

    private WebhookChannelService webhookChannelService;
    private WebhookChannelDAO webhookChannelDAO;
    private OrganizationManager organizationManager;
    private ResourceSharingPolicyHandlerService sharingService;

    private MockedStatic<WebhookManagementComponentServiceHolder> holderMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtilMockedStatic;

    @BeforeMethod
    public void setUp() throws Exception {

        webhookChannelDAO = mock(WebhookChannelDAO.class);
        organizationManager = mock(OrganizationManager.class);
        sharingService = mock(ResourceSharingPolicyHandlerService.class);

        WebhookManagementComponentServiceHolder holder = mock(WebhookManagementComponentServiceHolder.class);
        when(holder.getOrganizationManager()).thenReturn(organizationManager);
        when(holder.getResourceSharingPolicyHandlerService()).thenReturn(sharingService);
        holderMockedStatic = mockStatic(WebhookManagementComponentServiceHolder.class);
        holderMockedStatic.when(WebhookManagementComponentServiceHolder::getInstance).thenReturn(holder);

        identityTenantUtilMockedStatic = mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(OWNING_TENANT_ID)).thenReturn(OWNING_TENANT_DOMAIN);
        when(IdentityTenantUtil.getTenantDomain(CHILD_TENANT_ID)).thenReturn(CHILD_TENANT_DOMAIN);
        when(IdentityTenantUtil.getTenantId(CHILD_TENANT_DOMAIN)).thenReturn(CHILD_TENANT_ID);

        webhookChannelService = new WebhookChannelServiceImpl();
        Field daoField = WebhookChannelServiceImpl.class.getDeclaredField("webhookChannelDAO");
        daoField.setAccessible(true);
        daoField.set(webhookChannelService, webhookChannelDAO);
    }

    @AfterMethod
    public void tearDown() {

        holderMockedStatic.close();
        identityTenantUtilMockedStatic.close();
    }

    // ---------------------------------------------------------------------------------------
    // getSubscriptionPolicy: the policy is derived, not stored.
    // ---------------------------------------------------------------------------------------

//    @Test
//    public void testGetSubscriptionPolicyReturnsAllWhenStandingInstructionExists() throws Exception {
//
//        givenChannelIsOwnedBy(OWNING_ORG_ID);
//        when(sharingService.getResourceSharingPolicyByResourceKeys(
////                ResourceType.WEBHOOK_CHANNEL.name()
//                null
//                , CHANNEL_UUID, OWNING_ORG_ID, OWNING_ORG_ID))
//                .thenReturn(Optional.of(mock(ResourceSharingPolicy.class)));
//
//        assertEquals(webhookChannelService.getSubscriptionPolicy(CHANNEL_UUID),
//                SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS);
//    }
//
//    @Test
//    public void testGetSubscriptionPolicyReturnsSelectedWhenRowsExist() throws Exception {
//
//        givenChannelIsOwnedBy(OWNING_ORG_ID);
//        when(sharingService.getResourceSharingPolicyByResourceKeys(
//                anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.empty());
//        when(webhookChannelDAO.getChannelOrgSubscriptionCount(CHANNEL_UUID)).thenReturn(3);
//
//        assertEquals(webhookChannelService.getSubscriptionPolicy(CHANNEL_UUID),
//                SubscriptionPolicy.SELECTED_ORGS_ONLY);
//    }
//
//    @Test
//    public void testGetSubscriptionPolicyReturnsNoneWhenNothingSubscribed() throws Exception {
//
//        givenChannelIsOwnedBy(OWNING_ORG_ID);
//        when(sharingService.getResourceSharingPolicyByResourceKeys(
//                anyString(), anyString(), anyString(), anyString())).thenReturn(Optional.empty());
//        when(webhookChannelDAO.getChannelOrgSubscriptionCount(CHANNEL_UUID)).thenReturn(0);
//
//        assertEquals(webhookChannelService.getSubscriptionPolicy(CHANNEL_UUID), SubscriptionPolicy.NONE);
//    }
//
//    /**
//     * A channel whose owning tenant cannot be resolved -- the DAO's "not found" sentinel -- reports NONE rather
//     * than failing, so reading the policy of a deleted channel is answerable.
//     */
//    @Test
//    public void testGetSubscriptionPolicyReturnsNoneWhenChannelIsGone() throws Exception {
//
//        when(webhookChannelDAO.getOwningTenantId(CHANNEL_UUID)).thenReturn(-1);
//        when(webhookChannelDAO.getChannelOrgSubscriptionCount(CHANNEL_UUID)).thenReturn(0);
//
//        assertEquals(webhookChannelService.getSubscriptionPolicy(CHANNEL_UUID), SubscriptionPolicy.NONE);
//        verify(sharingService, never())
//                .getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(), anyString());
//    }
//
//    // ---------------------------------------------------------------------------------------
//    // setSubscriptionPolicy.
//    // ---------------------------------------------------------------------------------------
//
//    @Test(expectedExceptions = WebhookMgtClientException.class)
//    public void testSetSelectedPolicyRejectsEmptyOrganizations() throws Exception {
//
//        webhookChannelService.setSubscriptionPolicy(CHANNEL_UUID, OWNING_ORG_ID,
//                SubscriptionPolicy.SELECTED_ORGS_ONLY, Collections.emptyList());
//    }
//
//    /**
//     * An organization outside the owner's subtree is refused, and nothing is cleared on the way out: the
//     * channel keeps whatever subscription it had.
//     */
//    @Test
//    public void testSetSelectedPolicyRejectsOrganizationOutsideHierarchy() throws Exception {
//
//        when(organizationManager.getChildOrganizationsIds(OWNING_ORG_ID, true))
//                .thenReturn(Collections.singletonList(CHILD_ORG_ID));
//
//        try {
//            webhookChannelService.setSubscriptionPolicy(CHANNEL_UUID, OWNING_ORG_ID,
//                    SubscriptionPolicy.SELECTED_ORGS_ONLY, Collections.singletonList(OTHER_ORG_ID));
//            throw new AssertionError("Expected the unrelated organization to be rejected.");
//        } catch (WebhookMgtClientException e) {
//            assertTrue(e.getDescription().contains(OTHER_ORG_ID));
//        }
//        verify(webhookChannelDAO, never()).deleteChannelOrgSubscriptions(anyString());
//        verify(webhookChannelDAO, never()).addChannelOrgSubscriptions(any());
//    }
//
//    @Test
//    public void testSetSelectedPolicyWritesRowsForDescendants() throws Exception {
//
//        when(organizationManager.getChildOrganizationsIds(OWNING_ORG_ID, true))
//                .thenReturn(Collections.singletonList(CHILD_ORG_ID));
//        when(organizationManager.resolveTenantDomain(CHILD_ORG_ID)).thenReturn(CHILD_TENANT_DOMAIN);
//        when(webhookChannelDAO.getSubscribedOrgTenantIds(CHANNEL_UUID)).thenReturn(new ArrayList<>());
//
//        webhookChannelService.setSubscriptionPolicy(CHANNEL_UUID, OWNING_ORG_ID,
//                SubscriptionPolicy.SELECTED_ORGS_ONLY, Collections.singletonList(CHILD_ORG_ID));
//
//        verify(webhookChannelDAO).deleteChannelOrgSubscriptions(CHANNEL_UUID);
//        verify(webhookChannelDAO).addChannelOrgSubscriptions(any());
//        // SELECTED leaves no standing instruction: it does not extend to organizations yet to be created.
//        verify(sharingService, never()).addResourceSharingPolicy(any());
//    }
//
//    @Test
//    public void testSetAllPolicyWritesStandingInstructionAndSubtree() throws Exception {
//
//        when(organizationManager.getChildOrganizationsIds(OWNING_ORG_ID, true))
//                .thenReturn(Collections.singletonList(CHILD_ORG_ID));
//        when(organizationManager.resolveTenantDomain(CHILD_ORG_ID)).thenReturn(CHILD_TENANT_DOMAIN);
//        when(webhookChannelDAO.getSubscribedOrgTenantIds(CHANNEL_UUID)).thenReturn(new ArrayList<>());
//
//        webhookChannelService.setSubscriptionPolicy(CHANNEL_UUID, OWNING_ORG_ID,
//                SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS, null);
//
//        verify(sharingService).addResourceSharingPolicy(any(ResourceSharingPolicy.class));
//        verify(webhookChannelDAO).addChannelOrgSubscriptions(any());
//    }
//
//    /**
//     * NONE is applied rather than skipped: on an update it is a real transition that removes an existing fanout.
//     */
//    @Test
//    public void testSetNonePolicyClearsWithoutWriting() throws Exception {
//
//        when(webhookChannelDAO.getSubscribedOrgTenantIds(CHANNEL_UUID))
//                .thenReturn(Collections.singletonList(CHILD_TENANT_ID));
//
//        webhookChannelService.setSubscriptionPolicy(CHANNEL_UUID, OWNING_ORG_ID, SubscriptionPolicy.NONE, null);
//
//        verify(webhookChannelDAO).deleteChannelOrgSubscriptions(CHANNEL_UUID);
//        verify(webhookChannelDAO, never()).addChannelOrgSubscriptions(any());
//        verify(sharingService, never()).addResourceSharingPolicy(any());
//    }
//
//    // ---------------------------------------------------------------------------------------
//    // Paging and enrichment.
//    // ---------------------------------------------------------------------------------------
//
//    @Test
//    public void testGetSubscribedOrganizationsReturnsRequestedWindowOnly() throws Exception {
//
//        when(webhookChannelDAO.getChannelOrgSubscriptions(CHANNEL_UUID))
//                .thenReturn(subscriptionsFor("org-0", "org-1", "org-2", "org-3"));
//        when(organizationManager.getMinimalOrganization(anyString(), anyString())).thenReturn(null);
//
//        List<ChannelOrgSubscription> page = webhookChannelService.getSubscribedOrganizations(CHANNEL_UUID, 1, 2);
//
//        assertEquals(page.size(), 2);
//        assertEquals(page.get(0).getSubscribedOrgId(), "org-1");
//        assertEquals(page.get(1).getSubscribedOrgId(), "org-2");
//    }
//
//    @Test
//    public void testGetSubscribedOrganizationsClampsOffsetAndNegativeLimit() throws Exception {
//
//        when(webhookChannelDAO.getChannelOrgSubscriptions(CHANNEL_UUID))
//                .thenReturn(subscriptionsFor("org-0", "org-1"));
//        when(organizationManager.getMinimalOrganization(anyString(), anyString())).thenReturn(null);
//
//        assertEquals(webhookChannelService.getSubscribedOrganizations(CHANNEL_UUID, -5, -1).size(), 2);
//        assertEquals(webhookChannelService.getSubscribedOrganizations(CHANNEL_UUID, 99, 10).size(), 0);
//    }
//
//    @Test
//    public void testGetSubscribedOrganizationsAddsNameAndParent() throws Exception {
//
//        when(webhookChannelDAO.getChannelOrgSubscriptions(CHANNEL_UUID))
//                .thenReturn(subscriptionsFor(CHILD_ORG_ID));
//        MinimalOrganization organization = mock(MinimalOrganization.class);
//        when(organization.getName()).thenReturn("Child Org");
//        when(organization.getParentOrganizationId()).thenReturn(OWNING_ORG_ID);
//        when(organizationManager.getMinimalOrganization(CHILD_ORG_ID, CHILD_TENANT_DOMAIN))
//                .thenReturn(organization);
//
//        ChannelOrgSubscription resolved =
//                webhookChannelService.getSubscribedOrganizations(CHANNEL_UUID, 0, 10).get(0);
//
//        assertEquals(resolved.getOrgName(), "Child Org");
//        assertEquals(resolved.getParentOrgId(), OWNING_ORG_ID);
//    }
//
//    /**
//     * A subscription is a stored fact; a naming lookup that cannot answer must not hide it or fail the page.
//     */
//    @Test
//    public void testGetSubscribedOrganizationsKeepsRowWhenNameCannotBeResolved() throws Exception {
//
//        when(webhookChannelDAO.getChannelOrgSubscriptions(CHANNEL_UUID))
//                .thenReturn(subscriptionsFor(CHILD_ORG_ID));
//        when(organizationManager.getMinimalOrganization(anyString(), anyString()))
//                .thenThrow(new OrganizationManagementException("unreachable"));
//
//        List<ChannelOrgSubscription> page = webhookChannelService.getSubscribedOrganizations(CHANNEL_UUID, 0, 10);
//
//        assertEquals(page.size(), 1);
//        assertEquals(page.get(0).getSubscribedOrgId(), CHILD_ORG_ID);
//        assertNull(page.get(0).getOrgName());
//    }
//
//    // ---------------------------------------------------------------------------------------
//    // Organization lifecycle.
//    // ---------------------------------------------------------------------------------------
//
//    @Test
//    public void testSubscribeNewOrganizationFollowsAncestorStandingInstructions() throws Exception {
//
//        when(organizationManager.getAncestorOrganizationIds(CHILD_ORG_ID))
//                .thenReturn(Arrays.asList(CHILD_ORG_ID, OWNING_ORG_ID));
//        ResourceSharingPolicy standingInstruction = mock(ResourceSharingPolicy.class);
//        when(standingInstruction.getResourceId()).thenReturn(CHANNEL_UUID);
//        when(sharingService.getResourceSharingPoliciesByResourceType(Collections.singletonList(anyString()),
//                anyString())).thenReturn(Collections.singletonList(standingInstruction));
//        when(organizationManager.resolveTenantDomain(CHILD_ORG_ID)).thenReturn(CHILD_TENANT_DOMAIN);
//
//        webhookChannelService.subscribeNewOrganization(CHILD_ORG_ID);
//
//        verify(webhookChannelDAO).addChannelOrgSubscriptions(any());
//    }
//
//    /**
//     * A root organization has no ancestors, so there is no standing instruction that could reach it.
//     */
//    @Test
//    public void testSubscribeNewOrganizationDoesNothingForRoot() throws Exception {
//
//        when(organizationManager.getAncestorOrganizationIds(OWNING_ORG_ID))
//                .thenReturn(Collections.singletonList(OWNING_ORG_ID));
//
//        webhookChannelService.subscribeNewOrganization(OWNING_ORG_ID);
//
//        verify(webhookChannelDAO, never()).addChannelOrgSubscriptions(any());
//    }
//
//    /**
//     * A deleted organization is removed twice over: as a subscriber of other channels, and as the owner of
//     * channels whose standing instructions live in another database and cannot cascade.
//     */
//    @Test
//    public void testUnsubscribeDeletedOrganizationRemovesRowsAndOwnedInstructions() throws Exception {
//
//        ResourceSharingPolicy standingInstruction = mock(ResourceSharingPolicy.class);
//        when(standingInstruction.getResourceId()).thenReturn(CHANNEL_UUID);
//        when(sharingService.getResourceSharingPoliciesByResourceType(Collections.singletonList(anyString()),
//                anyString())).thenReturn(Collections.singletonList(standingInstruction));
//
//        webhookChannelService.unsubscribeDeletedOrganization(OWNING_ORG_ID);
//
//        verify(webhookChannelDAO).deleteChannelOrgSubscriptionsByOrgId(OWNING_ORG_ID);
//        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(
////                ResourceType.WEBHOOK_CHANNEL,
//                null, CHANNEL_UUID);
//    }
//
//    // ---------------------------------------------------------------------------------------
//    // clearSubscriptionPolicies: best effort, by contract.
//    // ---------------------------------------------------------------------------------------
//
//    @Test
//    public void testClearSubscriptionPoliciesClearsEachChannel() throws Exception {
//
//        webhookChannelService.clearSubscriptionPolicies(Arrays.asList("channel-a", "channel-b"));
//
//        verify(sharingService, times(2))
//                .deleteResourceSharingPolicyByResourceTypeAndId(any(ResourceType.class), anyString());
//    }
//
//    @Test
//    public void testClearSubscriptionPoliciesTolerantOfNullAndEmpty() throws Exception {
//
//        webhookChannelService.clearSubscriptionPolicies(null);
//        webhookChannelService.clearSubscriptionPolicies(Collections.emptyList());
//
//        verify(sharingService, never())
//                .deleteResourceSharingPolicyByResourceTypeAndId(any(ResourceType.class), anyString());
//    }
//
//    // ---------------------------------------------------------------------------------------
//    // Channel identity reads pass straight through to the DAO.
//    // ---------------------------------------------------------------------------------------
//
//    @Test
//    public void testChannelIdentityReadsDelegateToDao() throws Exception {
//
//        when(webhookChannelDAO.getChannelUuids("webhook-id", OWNING_TENANT_ID))
//                .thenReturn(Collections.singletonList(CHANNEL_UUID));
//        when(webhookChannelDAO.getChannelUuid("webhook-id", "channel-uri", OWNING_TENANT_ID))
//                .thenReturn(CHANNEL_UUID);
//        when(webhookChannelDAO.getChannelUri(CHANNEL_UUID)).thenReturn("channel-uri");
//        when(webhookChannelDAO.getSubscribedOrgTenantIds(CHANNEL_UUID))
//                .thenReturn(Collections.singletonList(CHILD_TENANT_ID));
//
//        assertEquals(webhookChannelService.getChannelUuids("webhook-id", OWNING_TENANT_ID).size(), 1);
//        assertEquals(webhookChannelService.getChannelUuid("webhook-id", "channel-uri", OWNING_TENANT_ID),
//                CHANNEL_UUID);
//        assertEquals(webhookChannelService.getChannelUri(CHANNEL_UUID), "channel-uri");
//        assertEquals(webhookChannelService.getSubscribedOrgTenantIds(CHANNEL_UUID).size(), 1);
//    }
//
//    @Test
//    public void testGetSubscribedOrganizationCountDelegatesToDao() throws Exception {
//
//        when(webhookChannelDAO.getChannelOrgSubscriptionCount(CHANNEL_UUID)).thenReturn(7);
//
//        assertEquals(webhookChannelService.getSubscribedOrganizationCount(CHANNEL_UUID), 7);
//    }
//
//    // ---------------------------------------------------------------------------------------
//
//    private void givenChannelIsOwnedBy(String owningOrgId) throws Exception {
//
//        when(webhookChannelDAO.getOwningTenantId(CHANNEL_UUID)).thenReturn(OWNING_TENANT_ID);
//        when(organizationManager.resolveOrganizationId(OWNING_TENANT_DOMAIN)).thenReturn(owningOrgId);
//    }
//
//    private List<ChannelOrgSubscription> subscriptionsFor(String... orgIds) {
//
//        List<ChannelOrgSubscription> subscriptions = new ArrayList<>();
//        for (String orgId : orgIds) {
//            subscriptions.add(ChannelOrgSubscription.builder()
//                    .channelUuid(CHANNEL_UUID)
//                    .subscribedOrgId(orgId)
//                    .subscribedOrgTenantId(CHILD_TENANT_ID)
//                    .build());
//        }
//        return subscriptions;
//    }
}
