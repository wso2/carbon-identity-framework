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

package org.wso2.carbon.identity.webhook.management.model;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelSubscribedOrganizations;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link ChannelSubscribedOrganizations}.
 */
public class ChannelSubscribedOrganizationsTest {

    private static final String CHANNEL_URI = "https://schemas.identity.wso2.org/events/token";
    private static final String CHANNEL_UUID = "2b6e5a7d-4c88-4e01-9a73-0f5cb8e3d219";
    private static final String ORG_ID_1 = "b028ca17-8f89-449c-ae27-fa955e66465d";
    private static final String ORG_ID_2 = "a17b28ca-9f89-449c-ae27-fa955e66465f";

    private static ChannelOrgSubscription subscription(String orgId, int tenantId) {

        return ChannelOrgSubscription.builder()
                .channelUuid(CHANNEL_UUID)
                .subscribedOrgId(orgId)
                .subscribedOrgTenantId(tenantId)
                .build();
    }

    @Test
    public void testBuilderCarriesEveryValue() {

        List<ChannelOrgSubscription> organizations =
                Arrays.asList(subscription(ORG_ID_1, 11), subscription(ORG_ID_2, 12));

        ChannelSubscribedOrganizations page = ChannelSubscribedOrganizations.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .totalCount(2)
                .organizations(organizations)
                .build();

        assertEquals(page.getChannelUri(), CHANNEL_URI);
        assertEquals(page.getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
        assertEquals(page.getTotalCount(), 2);
        assertEquals(page.getOrganizations().size(), 2);
        assertEquals(page.getOrganizations().get(0).getSubscribedOrgId(), ORG_ID_1);
    }

    /**
     * The page is never null, so a caller rendering it does not have to distinguish an empty page from a
     * missing one.
     */
    @Test
    public void testOrganizationsDefaultToEmptyRatherThanNull() {

        ChannelSubscribedOrganizations page = ChannelSubscribedOrganizations.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.NONE)
                .build();

        assertNotNull(page.getOrganizations());
        assertTrue(page.getOrganizations().isEmpty());
        assertEquals(page.getTotalCount(), 0);
    }

    /**
     * The page is a read, so it is handed out unmodifiable rather than letting a caller change what another
     * one is holding.
     */
    @Test
    public void testOrganizationsCannotBeModifiedByTheCaller() {

        ChannelSubscribedOrganizations page = ChannelSubscribedOrganizations.builder()
                .organizations(new ArrayList<>(Arrays.asList(subscription(ORG_ID_1, 11))))
                .build();

        try {
            page.getOrganizations().add(subscription(ORG_ID_2, 12));
            fail("Expected the returned organizations to be unmodifiable.");
        } catch (UnsupportedOperationException e) {
            assertEquals(page.getOrganizations().size(), 1);
        }
    }

    /**
     * The total is the number subscribed to the channel, not the size of the page, so a page smaller than the
     * total is the normal case rather than an inconsistency.
     */
    @Test
    public void testTotalCountIsIndependentOfThePageSize() {

        ChannelSubscribedOrganizations page = ChannelSubscribedOrganizations.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS)
                .totalCount(142)
                .organizations(Arrays.asList(subscription(ORG_ID_1, 11)))
                .build();

        assertEquals(page.getTotalCount(), 142);
        assertEquals(page.getOrganizations().size(), 1);
    }

    @Test
    public void testEmptyBuilderLeavesEveryValueUnset() {

        ChannelSubscribedOrganizations page = ChannelSubscribedOrganizations.builder().build();

        assertNull(page.getChannelUri());
        assertNull(page.getPolicy());
        assertEquals(page.getTotalCount(), 0);
        assertTrue(page.getOrganizations().isEmpty());
    }
}
