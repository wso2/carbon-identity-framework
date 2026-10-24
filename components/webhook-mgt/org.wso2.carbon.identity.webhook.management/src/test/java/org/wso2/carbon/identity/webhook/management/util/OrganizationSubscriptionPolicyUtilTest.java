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

package org.wso2.carbon.identity.webhook.management.util;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.ResourceSharingPolicyHandlerService;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.ResourceType;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.exception
        .ResourceSharingPolicyMgtException;
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.model.ResourceSharingPolicy;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.util.OrganizationSubscriptionPolicyUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link OrganizationSubscriptionPolicyUtil}.
 */
public class OrganizationSubscriptionPolicyUtilTest {

    private static final String CHANNEL_UUID = "c7f0b1de-0000-4000-8000-000000000001";
    private static final String OTHER_CHANNEL_UUID = "c7f0b1de-0000-4000-8000-000000000002";
    private static final String OWNING_ORG_ID = "owning-org-id";
    private static final String WEBHOOK_CHANNEL = ResourceType.WEBHOOK_CHANNEL.name();

    private ResourceSharingPolicyHandlerService sharingService;
    private ResourceSharingPolicyHandlerService originalSharingService;

    @BeforeMethod
    public void setUp() {

        WebhookManagementComponentServiceHolder holder = WebhookManagementComponentServiceHolder.getInstance();
        originalSharingService = holder.getResourceSharingPolicyHandlerService();
        sharingService = mock(ResourceSharingPolicyHandlerService.class);
        holder.setResourceSharingPolicyHandlerService(sharingService);
    }

    @AfterMethod
    public void tearDown() {

        WebhookManagementComponentServiceHolder.getInstance()
                .setResourceSharingPolicyHandlerService(originalSharingService);
    }

    @Test
    public void testIsPolicyHeldWhenTheOwningOrganizationHoldsIt() throws Exception {

        when(sharingService.getResourceSharingPolicyByResourceKeys(
                WEBHOOK_CHANNEL, CHANNEL_UUID, OWNING_ORG_ID, OWNING_ORG_ID))
                .thenReturn(Optional.of(mock(ResourceSharingPolicy.class)));

        assertTrue(OrganizationSubscriptionPolicyUtil.isPolicyHeld(CHANNEL_UUID, OWNING_ORG_ID));
    }

    @Test
    public void testIsPolicyHeldWhenNoPolicyExists() throws Exception {

        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(),
                anyString())).thenReturn(Optional.empty());

        assertFalse(OrganizationSubscriptionPolicyUtil.isPolicyHeld(CHANNEL_UUID, OWNING_ORG_ID));
    }

    @Test
    public void testIsPolicyHeldReportsAReadFailure() throws Exception {

        when(sharingService.getResourceSharingPolicyByResourceKeys(anyString(), anyString(), anyString(),
                anyString())).thenThrow(sharingFailure());

        assertFailsWith(() -> OrganizationSubscriptionPolicyUtil.isPolicyHeld(CHANNEL_UUID, OWNING_ORG_ID),
                ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR);
    }

    @Test
    public void testGetPoliciesHeldByReturnsTheWebhookChannelPoliciesOfTheOrganization() throws Exception {

        List<ResourceSharingPolicy> policies = Collections.singletonList(mock(ResourceSharingPolicy.class));
        when(sharingService.getResourceSharingPoliciesByResourceType(
                Collections.singletonList(OWNING_ORG_ID), WEBHOOK_CHANNEL)).thenReturn(policies);

        assertSame(OrganizationSubscriptionPolicyUtil.getPoliciesHeldBy(OWNING_ORG_ID), policies);
    }

    @Test
    public void testGetPoliciesHeldByReportsAReadFailure() throws Exception {

        when(sharingService.getResourceSharingPoliciesByResourceType(any(), anyString()))
                .thenThrow(sharingFailure());

        assertFailsWith(() -> OrganizationSubscriptionPolicyUtil.getPoliciesHeldBy(OWNING_ORG_ID),
                ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_GET_ERROR);
    }

    @Test
    public void testWritePolicySubscribesExistingAndFutureOrganizations() throws Exception {

        OrganizationSubscriptionPolicyUtil.writePolicy(CHANNEL_UUID, OWNING_ORG_ID);

        ArgumentCaptor<ResourceSharingPolicy> captor = ArgumentCaptor.forClass(ResourceSharingPolicy.class);
        verify(sharingService).addResourceSharingPolicy(captor.capture());
        ResourceSharingPolicy policy = captor.getValue();
        assertEquals(policy.getResourceType(), ResourceType.WEBHOOK_CHANNEL);
        assertEquals(policy.getResourceId(), CHANNEL_UUID);
        assertEquals(policy.getInitiatingOrgId(), OWNING_ORG_ID);
        assertEquals(policy.getPolicyHoldingOrgId(), OWNING_ORG_ID);
        assertEquals(policy.getSharingPolicy(), PolicyEnum.ALL_EXISTING_AND_FUTURE_ORGS);
    }

    @Test
    public void testWritePolicyReportsAWriteFailure() throws Exception {

        when(sharingService.addResourceSharingPolicy(any())).thenThrow(sharingFailure());

        assertFailsWith(() -> {
            OrganizationSubscriptionPolicyUtil.writePolicy(CHANNEL_UUID, OWNING_ORG_ID);
            return null;
        }, ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR);
    }

    @Test
    public void testClearPolicyDeletesThePolicyOfTheChannel() throws Exception {

        OrganizationSubscriptionPolicyUtil.clearPolicy(CHANNEL_UUID);

        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(
                ResourceType.WEBHOOK_CHANNEL, CHANNEL_UUID);
    }

    @Test
    public void testClearPolicyReportsADeleteFailure() throws Exception {

        doThrow(sharingFailure()).when(sharingService)
                .deleteResourceSharingPolicyByResourceTypeAndId(any(ResourceType.class), anyString());

        assertFailsWith(() -> {
            OrganizationSubscriptionPolicyUtil.clearPolicy(CHANNEL_UUID);
            return null;
        }, ErrorMessage.ERROR_CODE_SUB_ORG_POLICY_SET_ERROR);
    }

    @Test
    public void testClearPoliciesQuietlyIgnoresAMissingOrEmptyList() {

        OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(null);
        OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(Collections.emptyList());

        verifyNoInteractions(sharingService);
    }

    @Test
    public void testClearPoliciesQuietlyContinuesPastAFailedDelete() throws Exception {

        doThrow(sharingFailure()).when(sharingService)
                .deleteResourceSharingPolicyByResourceTypeAndId(ResourceType.WEBHOOK_CHANNEL, CHANNEL_UUID);

        OrganizationSubscriptionPolicyUtil.clearPoliciesQuietly(Arrays.asList(CHANNEL_UUID, OTHER_CHANNEL_UUID));

        verify(sharingService).deleteResourceSharingPolicyByResourceTypeAndId(
                ResourceType.WEBHOOK_CHANNEL, OTHER_CHANNEL_UUID);
    }

    @Test
    public void testRestorePoliciesQuietlyIgnoresAMissingOrEmptyMap() {

        OrganizationSubscriptionPolicyUtil.restorePoliciesQuietly(null);
        OrganizationSubscriptionPolicyUtil.restorePoliciesQuietly(Collections.emptyMap());

        verifyNoInteractions(sharingService);
    }

    @Test
    public void testRestorePoliciesQuietlyContinuesPastAFailedWrite() throws Exception {

        when(sharingService.addResourceSharingPolicy(any())).thenThrow(sharingFailure()).thenReturn(1);
        Map<String, String> cleared = new LinkedHashMap<>();
        cleared.put(CHANNEL_UUID, OWNING_ORG_ID);
        cleared.put(OTHER_CHANNEL_UUID, OWNING_ORG_ID);

        OrganizationSubscriptionPolicyUtil.restorePoliciesQuietly(cleared);

        ArgumentCaptor<ResourceSharingPolicy> captor = ArgumentCaptor.forClass(ResourceSharingPolicy.class);
        verify(sharingService, times(2)).addResourceSharingPolicy(captor.capture());
        assertEquals(captor.getAllValues().get(1).getResourceId(), OTHER_CHANNEL_UUID);
    }

    private static ResourceSharingPolicyMgtException sharingFailure() {

        return new ResourceSharingPolicyMgtException("RSP-00000", "Sharing failure", "Simulated failure.");
    }

    private static void assertFailsWith(UtilCall call, ErrorMessage expected) {

        try {
            call.run();
            fail("Expected " + expected.getCode());
        } catch (WebhookMgtException e) {
            assertEquals(e.getErrorCode(), expected.getCode());
            assertTrue(e.getCause() instanceof ResourceSharingPolicyMgtException);
        }
    }

    @FunctionalInterface
    private interface UtilCall {

        Object run() throws WebhookMgtException;
    }
}
