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
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookChannel;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Unit tests for {@link WebhookChannel}.
 */
public class WebhookChannelTest {

    private static final String CHANNEL_ID = "2b6e5a7d-4c88-4e01-9a73-0f5cb8e3d219";
    private static final String CHANNEL_URI = "https://schemas.identity.wso2.org/events/login";

    @Test
    public void testBuilderCarriesEveryValue() {

        WebhookChannel channel = WebhookChannel.builder()
                .channelId(CHANNEL_ID)
                .channelUri(CHANNEL_URI)
                .status(SubscriptionStatus.SUBSCRIPTION_ACCEPTED)
                .policy(SubscriptionPolicy.SELECTED_ORGS_ONLY)
                .build();

        assertEquals(channel.getChannelId(), CHANNEL_ID);
        assertEquals(channel.getChannelUri(), CHANNEL_URI);
        assertEquals(channel.getStatus(), SubscriptionStatus.SUBSCRIPTION_ACCEPTED);
        assertEquals(channel.getPolicy(), SubscriptionPolicy.SELECTED_ORGS_ONLY);
    }

    /**
     * A webhook carries no status for a channel it was never subscribed to, which is reported as null rather
     * than as a status of its own.
     */
    @Test
    public void testStatusIsNullWhenTheWebhookCarriesNone() {

        WebhookChannel channel = WebhookChannel.builder()
                .channelId(CHANNEL_ID)
                .channelUri(CHANNEL_URI)
                .policy(SubscriptionPolicy.NONE)
                .build();

        assertNotNull(channel);
        assertNull(channel.getStatus());
        assertEquals(channel.getPolicy(), SubscriptionPolicy.NONE);
    }

    @Test
    public void testEmptyBuilderLeavesEveryValueUnset() {

        WebhookChannel channel = WebhookChannel.builder().build();

        assertNull(channel.getChannelId());
        assertNull(channel.getChannelUri());
        assertNull(channel.getStatus());
        assertNull(channel.getPolicy());
    }

    @Test
    public void testEveryStatusIsCarried() {

        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            WebhookChannel channel = WebhookChannel.builder().channelId(CHANNEL_ID).status(status).build();
            assertEquals(channel.getStatus(), status);
        }
    }

    @Test
    public void testEveryPolicyIsCarried() {

        for (SubscriptionPolicy policy : SubscriptionPolicy.values()) {
            WebhookChannel channel = WebhookChannel.builder().channelUri(CHANNEL_URI).policy(policy).build();
            assertEquals(channel.getPolicy(), policy);
        }
    }
}
