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
 * Organization subscription configured for one channel of a webhook.
 * <p>
 * The policy discriminates which of the remaining fields carry meaning:
 * <ul>
 *   <li>{@link SubscriptionPolicy#NONE} — {@code organizationIds} is ignored and dropped;
 *       no organizations receive events.</li>
 *   <li>{@link SubscriptionPolicy#ALL_EXISTING_AND_FUTURE_ORGS} — {@code organizationIds} is ignored and
 *       dropped, since the subscription follows the hierarchy rather than a list.</li>
 *   <li>{@link SubscriptionPolicy#SELECTED_ORGS_ONLY} — {@code organizationIds} is required and must
 *       be non-empty.</li>
 * </ul>
 * {@code organizationIds} is supplied on write and is not populated on read: the organizations are
 * read back from the channel organizations sub-resource, which resolves names and parents and is
 * paginated. That resource reports the count too, so it is not repeated here.
 */
public class OrganizationSubscription {

    private final String channelUri;
    private final SubscriptionPolicy policy;
    private final List<String> organizationIds;

    private OrganizationSubscription(Builder builder) {

        this.channelUri = builder.channelUri;
        this.policy = builder.policy;
        this.organizationIds = builder.organizationIds == null
                ? Collections.emptyList()
                : List.copyOf(builder.organizationIds);
    }

    /**
     * Return channel URI of a webhook channel.
     *
     * @return channel URI.
     */
    public String getChannelUri() {

        return channelUri;
    }

    /**
     * Return subscription policy for the channel.
     *
     * @return subscription policy.
     */
    public SubscriptionPolicy getPolicy() {

        return policy;
    }

    /**
     * Organizations to subscribe. Meaningful only when the policy is
     * {@link SubscriptionPolicy#SELECTED_ORGS_ONLY}; empty otherwise.
     */
    public List<String> getOrganizationIds() {

        return organizationIds;
    }

    /**
     * Create a builder for {@link OrganizationSubscription}.
     *
     * @return A new, empty builder.
     */
    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder for {@link OrganizationSubscription}.
     */
    public static class Builder {

        private String channelUri;
        private SubscriptionPolicy policy = SubscriptionPolicy.NONE;
        private List<String> organizationIds;

        /**
         * Set the URI of the channel the subscription applies to.
         *
         * @param channelUri The URI of the channel the subscription applies to.
         * @return This builder.
         */
        public Builder channelUri(String channelUri) {

            this.channelUri = channelUri;
            return this;
        }

        /**
         * Set the subscription policy to apply.
         *
         * @param policy The subscription policy to apply.
         * @return This builder.
         */
        public Builder policy(SubscriptionPolicy policy) {

            this.policy = policy;
            return this;
        }

        /**
         * Set the organizations to subscribe. Meaningful only when the policy is
         * {@link SubscriptionPolicy#SELECTED_ORGS_ONLY}; ignored and dropped for any other policy.
         *
         * @param organizationIds Ids of the organizations to subscribe.
         * @return This builder.
         */
        public Builder organizationIds(List<String> organizationIds) {

            this.organizationIds = organizationIds;
            return this;
        }

        /**
         * Build the {@link OrganizationSubscription} from the values set so far.
         *
         * @return A new {@link OrganizationSubscription}.
         */
        public OrganizationSubscription build() {

            return new OrganizationSubscription(this);
        }
    }
}
