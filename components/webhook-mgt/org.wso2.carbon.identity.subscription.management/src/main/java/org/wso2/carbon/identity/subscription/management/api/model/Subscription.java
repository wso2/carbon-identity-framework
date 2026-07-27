/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.subscription.management.api.model;

/**
 * Represents a subscription to a webhook channel.
 */
public class Subscription {

    private SubscriptionStatus status;
    private String channelUri;

    private Subscription(Builder builder) {

        this.status = builder.status;
        this.channelUri = builder.channelUri;
    }

    public SubscriptionStatus getStatus() {

        return status;
    }

    public String getChannelUri() {

        return channelUri;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder class for Subscription.
     * This class is used to build Subscription objects.
     */
    public static class Builder {

        private SubscriptionStatus status;
        private String channelUri;

        public Builder status(SubscriptionStatus status) {

            this.status = status;
            return this;
        }

        public Builder channelUri(String channelUri) {

            this.channelUri = channelUri;
            return this;
        }

        public Subscription build() {

            return new Subscription(this);
        }
    }
}
