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

/**
 * A single descendant organization feeding one webhook channel.
 * <p>
 * One instance corresponds to one row of IDN_WEBHOOK_CHANNEL_ORG_SUB. The tenant id drives
 * publish-time resolution and the organization id serves the REST layer. {@code topicUuid} and
 * {@code topic} are reserved for the PublisherSubscriber adapter, to route an event raised in the
 * descendant organization onto the webhook-owning organization's topic. No path writes them yet, so
 * both are currently null under either adapter.
 */
public class ChannelOrgSubscription {

    private final String channelUuid;
    private final int subscribedOrgTenantId;
    private final String subscribedOrgId;
    private final String topicUuid;
    private final String topic;
    private final String orgName;
    private final String parentOrgId;

    private ChannelOrgSubscription(Builder builder) {

        this.channelUuid = builder.channelUuid;
        this.subscribedOrgTenantId = builder.subscribedOrgTenantId;
        this.subscribedOrgId = builder.subscribedOrgId;
        this.topicUuid = builder.topicUuid;
        this.topic = builder.topic;
        this.orgName = builder.orgName;
        this.parentOrgId = builder.parentOrgId;
    }

    /**
     * UUID of the channel this subscription belongs to.
     *
     * @return Channel UUID.
     */
    public String getChannelUuid() {

        return channelUuid;
    }

    /**
     * Tenant id of the subscribed organization. The publish path matches on this, since an event is
     * raised in a tenant.
     *
     * @return Tenant id of the subscribed organization.
     */
    public int getSubscribedOrgTenantId() {

        return subscribedOrgTenantId;
    }

    /**
     * Id of the subscribed organization. Used by the REST layer, which addresses organizations by id.
     *
     * @return Organization id.
     */
    public String getSubscribedOrgId() {

        return subscribedOrgId;
    }

    /**
     * UUID of the hub topic the subscription is routed through. Reserved for the PublisherSubscriber
     * adapter; no path writes it yet, so it is currently always null.
     *
     * @return Topic UUID, or null.
     */
    public String getTopicUuid() {

        return topicUuid;
    }

    /**
     * Hub topic the subscription is routed through. Reserved for the PublisherSubscriber adapter; no path
     * writes it yet, so it is currently always null.
     *
     * @return Topic, or null.
     */
    public String getTopic() {

        return topic;
    }

    /**
     * Display name of the subscribed organization. Resolved on read only; never persisted, and null
     * when the organization could not be resolved.
     */
    public String getOrgName() {

        return orgName;
    }

    /**
     * Id of the subscribed organization's parent. Resolved on read only; never persisted, and null
     * for a root organization or when the organization could not be resolved.
     */
    public String getParentOrgId() {

        return parentOrgId;
    }

    /**
     * Create a builder for {@link ChannelOrgSubscription}.
     *
     * @return A new, empty builder.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link ChannelOrgSubscription}.
     */
    public static class Builder {

        private String channelUuid;
        private int subscribedOrgTenantId;
        private String subscribedOrgId;
        private String topicUuid;
        private String topic;
        private String orgName;
        private String parentOrgId;

        /**
         * Set the UUID of the channel.
         *
         * @param channelUuid The UUID of the channel.
         * @return This builder.
         */
        public Builder channelUuid(String channelUuid) {

            this.channelUuid = channelUuid;
            return this;
        }

        /**
         * Set the tenant id of the subscribed organization.
         *
         * @param subscribedOrgTenantId The tenant id of the subscribed organization.
         * @return This builder.
         */
        public Builder subscribedOrgTenantId(int subscribedOrgTenantId) {

            this.subscribedOrgTenantId = subscribedOrgTenantId;
            return this;
        }

        /**
         * Set the id of the subscribed organization.
         *
         * @param subscribedOrgId The id of the subscribed organization.
         * @return This builder.
         */
        public Builder subscribedOrgId(String subscribedOrgId) {

            this.subscribedOrgId = subscribedOrgId;
            return this;
        }

        /**
         * Set the UUID of the hub topic.
         *
         * @param topicUuid The UUID of the hub topic.
         * @return This builder.
         */
        public Builder topicUuid(String topicUuid) {

            this.topicUuid = topicUuid;
            return this;
        }

        /**
         * Set the hub topic.
         *
         * @param topic The hub topic.
         * @return This builder.
         */
        public Builder topic(String topic) {

            this.topic = topic;
            return this;
        }

        /**
         * Set the display name of the subscribed organization, resolved on read.
         *
         * @param orgName The display name of the subscribed organization, resolved on read.
         * @return This builder.
         */
        public Builder orgName(String orgName) {

            this.orgName = orgName;
            return this;
        }

        /**
         * Set the id of the subscribed organization's parent, resolved on read.
         *
         * @param parentOrgId The id of the subscribed organization's parent, resolved on read.
         * @return This builder.
         */
        public Builder parentOrgId(String parentOrgId) {

            this.parentOrgId = parentOrgId;
            return this;
        }

        /**
         * Build the {@link ChannelOrgSubscription} from the values set so far.
         *
         * @return A new {@link ChannelOrgSubscription}.
         */
        public ChannelOrgSubscription build() {

            return new ChannelOrgSubscription(this);
        }
    }
}
