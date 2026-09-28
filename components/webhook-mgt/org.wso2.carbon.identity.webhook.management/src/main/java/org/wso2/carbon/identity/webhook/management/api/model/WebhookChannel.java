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

package org.wso2.carbon.identity.webhook.management.api.model;

import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;

/**
 * A single channel of a webhook: what it is, whether the webhook is subscribed to it, and how far its events
 * fan out across the organization hierarchy.
 * <p>
 * The parts are read together so that they describe one moment. Read separately, a concurrent change can pair a
 * subscription status with an organization policy that no longer belongs beside it.
 */
public class WebhookChannel {

    private final String channelId;
    private final String channelUri;
    private final SubscriptionStatus status;
    private final SubscriptionPolicy policy;

    private WebhookChannel(Builder builder) {

        this.channelId = builder.channelId;
        this.channelUri = builder.channelUri;
        this.status = builder.status;
        this.policy = builder.policy;
    }

    /**
     * UUID of the channel.
     *
     * @return Channel UUID.
     */
    public String getChannelId() {

        return channelId;
    }

    /**
     * URI of the channel.
     *
     * @return Channel URI.
     */
    public String getChannelUri() {

        return channelUri;
    }

    /**
     * Subscription status the webhook holds for this channel.
     *
     * @return Subscription status, null when the webhook carries no status for it.
     */
    public SubscriptionStatus getStatus() {

        return status;
    }

    /**
     * Organization subscription policy in force for the channel.
     *
     * @return Subscription policy.
     */
    public SubscriptionPolicy getPolicy() {

        return policy;
    }

    /**
     * Create a builder for {@link WebhookChannel}.
     *
     * @return A new, empty builder.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link WebhookChannel}.
     */
    public static class Builder {

        private String channelId;
        private String channelUri;
        private SubscriptionStatus status;
        private SubscriptionPolicy policy;

        /**
         * Set the UUID of the channel.
         *
         * @param channelId The UUID of the channel.
         * @return This builder.
         */
        public Builder channelId(String channelId) {

            this.channelId = channelId;
            return this;
        }

        /**
         * Set the URI of the channel.
         *
         * @param channelUri The URI of the channel.
         * @return This builder.
         */
        public Builder channelUri(String channelUri) {

            this.channelUri = channelUri;
            return this;
        }

        /**
         * Set the subscription status the webhook holds for the channel.
         *
         * @param status The subscription status the webhook holds for the channel.
         * @return This builder.
         */
        public Builder status(SubscriptionStatus status) {

            this.status = status;
            return this;
        }

        /**
         * Set the organization subscription policy in force for the channel.
         *
         * @param policy The organization subscription policy in force for the channel.
         * @return This builder.
         */
        public Builder policy(SubscriptionPolicy policy) {

            this.policy = policy;
            return this;
        }

        /**
         * Build the {@link WebhookChannel} from the values set so far.
         *
         * @return A new {@link WebhookChannel}.
         */
        public WebhookChannel build() {

            return new WebhookChannel(this);
        }
    }
}
