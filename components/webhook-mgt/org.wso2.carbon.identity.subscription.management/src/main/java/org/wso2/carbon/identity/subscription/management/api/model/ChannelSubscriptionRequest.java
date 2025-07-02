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

import java.util.List;

public class ChannelSubscriptionRequest {

    private final List<Subscription> eventsSubscribed;
    private final String eventProfileVersion;
    private final String endpoint;
    private final String secret;

    private ChannelSubscriptionRequest(SubscribeBuilder builder) {

        this.eventsSubscribed = builder.eventsSubscribed;
        this.eventProfileVersion = builder.eventProfileVersion;
        this.endpoint = builder.endpoint;
        this.secret = builder.secret;
    }

    private ChannelSubscriptionRequest(UnsubscribeBuilder builder) {

        this.eventsSubscribed = builder.eventsSubscribed;
        this.eventProfileVersion = builder.eventProfileVersion;
        this.endpoint = builder.endpoint;
        this.secret = null;
    }

    public List<Subscription> getEventsSubscribed() {

        return eventsSubscribed;
    }

    public String getEventProfileVersion() {

        return eventProfileVersion;
    }

    public String getEndpoint() {

        return endpoint;
    }

    public String getSecret() {

        return secret;
    }

    public static SubscribeBuilder subscribe() {

        return new SubscribeBuilder();
    }

    public static UnsubscribeBuilder unsubscribe() {

        return new UnsubscribeBuilder();
    }

    public static class SubscribeBuilder {

        private List<Subscription> eventsSubscribed;
        private String eventProfileVersion;
        private String endpoint;
        private String secret;

        public SubscribeBuilder eventsSubscribed(List<Subscription> eventsSubscribed) {

            this.eventsSubscribed = eventsSubscribed;
            return this;
        }

        public SubscribeBuilder eventProfileVersion(String eventProfileVersion) {

            this.eventProfileVersion = eventProfileVersion;
            return this;
        }

        public SubscribeBuilder endpoint(String endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        public SubscribeBuilder secret(String secret) {

            this.secret = secret;
            return this;
        }

        public ChannelSubscriptionRequest build() {

            return new ChannelSubscriptionRequest(this);
        }
    }

    public static class UnsubscribeBuilder {

        private List<Subscription> eventsSubscribed;
        private String eventProfileVersion;
        private String endpoint;

        public UnsubscribeBuilder eventsSubscribed(List<Subscription> eventsSubscribed) {

            this.eventsSubscribed = eventsSubscribed;
            return this;
        }

        public UnsubscribeBuilder eventProfileVersion(String eventProfileVersion) {

            this.eventProfileVersion = eventProfileVersion;
            return this;
        }

        public UnsubscribeBuilder endpoint(String endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        public ChannelSubscriptionRequest build() {

            return new ChannelSubscriptionRequest(this);
        }
    }
}
