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
import org.wso2.carbon.identity.webhook.management.api.model.OrganizationSubscription;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for {@link OrganizationSubscription}.
 */
public class OrganizationSubscriptionTest {

    private static final String CHANNEL_URI = "https://schemas.identity.wso2.org/events/user";
    private static final String ORG_ID_1 = "10084a8d-113f-4211-a0d5-efe36b082211";
    private static final String ORG_ID_2 = "22195b9e-224g-5322-b1e6-fgf47c193322";

    @Test
    public void testBuilderCarriesEveryValue() {

        OrganizationSubscription subscription = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Arrays.asList(ORG_ID_1, ORG_ID_2))
                .build();

        assertEquals(subscription.getChannelUri(), CHANNEL_URI);
        assertEquals(subscription.getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
        assertEquals(subscription.getOrganizationIds(), Arrays.asList(ORG_ID_1, ORG_ID_2));
    }

    /**
     * A channel whose request carried no fanout is a channel with no fanout, not a channel whose policy is
     * unknown. The default keeps a caller that sets only the channel URI from having to say so.
     */
    @Test
    public void testPolicyDefaultsToNone() {

        OrganizationSubscription subscription = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .build();

        assertEquals(subscription.getPolicy(), SubscriptionPolicy.NONE);
    }

    /**
     * Organization ids are never null on the way out, so every caller can iterate the list without a guard.
     */
    @Test
    public void testOrganizationIdsDefaultToEmpty() {

        OrganizationSubscription unset = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .build();
        OrganizationSubscription explicitNull = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .organizationIds(null)
                .build();

        assertNotNull(unset.getOrganizationIds());
        assertTrue(unset.getOrganizationIds().isEmpty());
        assertNotNull(explicitNull.getOrganizationIds());
        assertTrue(explicitNull.getOrganizationIds().isEmpty());
    }

    /**
     * The list is copied on build, so a caller that keeps and mutates the list it supplied cannot change a
     * subscription that has already been handed to the service layer.
     */
    @Test
    public void testOrganizationIdsAreCopiedFromTheCaller() {

        List<String> supplied = new ArrayList<>(Collections.singletonList(ORG_ID_1));
        OrganizationSubscription subscription = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(supplied)
                .build();

        supplied.add(ORG_ID_2);

        assertEquals(subscription.getOrganizationIds().size(), 1);
        assertEquals(subscription.getOrganizationIds().get(0), ORG_ID_1);
    }

    /**
     * The returned list is unmodifiable, so a consumer cannot add an organization to a subscription in place and
     * have it reach the database without going through the service validation.
     */
    @Test
    public void testOrganizationIdsAreUnmodifiable() {

        OrganizationSubscription subscription = OrganizationSubscription.builder()
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .organizationIds(Collections.singletonList(ORG_ID_1))
                .build();

        try {
            subscription.getOrganizationIds().add(ORG_ID_2);
            fail("The organization ids of a built subscription should not be modifiable.");
        } catch (UnsupportedOperationException e) {
            // Expected.
        }
    }

    /**
     * Each builder call returns the same builder, so the chained form used throughout the service layer applies
     * every value rather than only the last.
     */
    @Test
    public void testBuilderIsChainable() {

        OrganizationSubscription.Builder builder = OrganizationSubscription.builder();

        assertEquals(builder.channelUri(CHANNEL_URI), builder);
        assertEquals(builder.policy(SubscriptionPolicy.ALL_EXISTING_AND_FUTURE_ORGS), builder);
        assertEquals(builder.organizationIds(Collections.singletonList(ORG_ID_1)), builder);
    }

    /**
     * A subscription is built from whatever the request carried, including nothing at all: the service layer, not
     * the model, decides whether a channel URI is required.
     */
    @Test
    public void testEmptyBuilderBuilds() {

        OrganizationSubscription subscription = OrganizationSubscription.builder().build();

        assertNull(subscription.getChannelUri());
        assertEquals(subscription.getPolicy(), SubscriptionPolicy.NONE);
        assertTrue(subscription.getOrganizationIds().isEmpty());
    }

    /**
     * Every policy is accepted by the model. Which of them require organization ids is enforced on write, so the
     * model carries the inconsistent combination rather than rejecting it.
     */
    @Test
    public void testEveryPolicyIsAccepted() {

        for (SubscriptionPolicy policy : SubscriptionPolicy.values()) {
            OrganizationSubscription subscription = OrganizationSubscription.builder()
                    .channelUri(CHANNEL_URI)
                    .policy(policy)
                    .build();
            assertEquals(subscription.getPolicy(), policy);
            assertEquals(SubscriptionPolicy.valueOf(policy.name()), policy);
        }
    }
}
