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

import java.util.Collections;
import java.util.List;

/**
 * A page of the organizations subscribed to a channel, with the channel context that describes it.
 * <p>
 * The parts are read together so that they describe one moment. Read separately, a concurrent change to the
 * subscription policy can produce a page of organizations alongside a policy that no longer admits them.
 */
public class ChannelSubscribedOrganizations {

    private final String channelUri;
    private final SubscriptionPolicy policy;
    private final int totalCount;
    private final List<ChannelOrgSubscription> organizations;

    private ChannelSubscribedOrganizations(Builder builder) {

        this.channelUri = builder.channelUri;
        this.policy = builder.policy;
        this.totalCount = builder.totalCount;
        this.organizations = builder.organizations == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(builder.organizations);
    }

    /**
     * URI of the channel this page belongs to.
     *
     * @return Channel URI.
     */
    public String getChannelUri() {

        return channelUri;
    }

    /**
     * Subscription policy in force for the channel.
     *
     * @return Subscription policy.
     */
    public SubscriptionPolicy getPolicy() {

        return policy;
    }

    /**
     * Number of organizations subscribed to the channel, which is not the size of this page.
     *
     * @return Total number of subscribed organizations.
     */
    public int getTotalCount() {

        return totalCount;
    }

    /**
     * The requested page of subscribed organizations.
     *
     * @return Subscribed organizations, never null.
     */
    public List<ChannelOrgSubscription> getOrganizations() {

        return organizations;
    }

    /**
     * Create a builder for {@link ChannelSubscribedOrganizations}.
     *
     * @return A new, empty builder.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link ChannelSubscribedOrganizations}.
     */
    public static class Builder {

        private String channelUri;
        private SubscriptionPolicy policy;
        private int totalCount;
        private List<ChannelOrgSubscription> organizations;

        /**
         * Set the URI of the channel the page belongs to.
         *
         * @param channelUri The URI of the channel the page belongs to.
         * @return This builder.
         */
        public Builder channelUri(String channelUri) {

            this.channelUri = channelUri;
            return this;
        }

        /**
         * Set the subscription policy in force for the channel.
         *
         * @param policy The subscription policy in force for the channel.
         * @return This builder.
         */
        public Builder policy(SubscriptionPolicy policy) {

            this.policy = policy;
            return this;
        }

        /**
         * Set the total number of subscribed organizations, which is not the size of the page.
         *
         * @param totalCount The total number of subscribed organizations, which is not the size of the page.
         * @return This builder.
         */
        public Builder totalCount(int totalCount) {

            this.totalCount = totalCount;
            return this;
        }

        /**
         * Set the requested page of subscribed organizations.
         *
         * @param organizations The requested page of subscribed organizations.
         * @return This builder.
         */
        public Builder organizations(List<ChannelOrgSubscription> organizations) {

            this.organizations = organizations;
            return this;
        }

        /**
         * Build the {@link ChannelSubscribedOrganizations} from the values set so far.
         *
         * @return A new {@link ChannelSubscribedOrganizations}.
         */
        public ChannelSubscribedOrganizations build() {

            return new ChannelSubscribedOrganizations(this);
        }
    }
}
