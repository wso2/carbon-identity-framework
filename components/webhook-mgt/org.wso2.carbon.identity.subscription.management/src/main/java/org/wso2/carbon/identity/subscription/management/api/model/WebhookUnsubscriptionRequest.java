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

/**
 * Represents a request to unsubscribe from webhook events.
 */
public class WebhookUnsubscriptionRequest {

    private final List<String> channelToUnsubscribe;
    private final String eventProfileVersion;
    private final String eventProfileName;
    private final String endpoint;

    private WebhookUnsubscriptionRequest(Builder builder) {

        this.channelToUnsubscribe = builder.channelsToUnsubscribe;
        this.eventProfileVersion = builder.eventProfileVersion;
        this.eventProfileName = builder.eventProfileName;
        this.endpoint = builder.endpoint;
    }

    public List<String> getChannelToUnsubscribe() {

        return channelToUnsubscribe;
    }

    public String getEventProfileVersion() {

        return eventProfileVersion;
    }

    public String getEventProfileName() {

        return eventProfileName;
    }

    public String getEndpoint() {

        return endpoint;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder class for WebhookUnsubscriptionRequest.
     */
    public static class Builder {

        private List<String> channelsToUnsubscribe;
        private String eventProfileVersion;
        private String eventProfileName;
        private String endpoint;

        public Builder channelsToUnsubscribe(List<String> channelsToUnsubscribe) {

            this.channelsToUnsubscribe = channelsToUnsubscribe;
            return this;
        }

        public Builder eventProfileVersion(String eventProfileVersion) {

            this.eventProfileVersion = eventProfileVersion;
            return this;
        }

        public Builder eventProfileName(String eventProfileName) {

            this.eventProfileName = eventProfileName;
            return this;
        }

        public Builder endpoint(String endpoint) {

            this.endpoint = endpoint;
            return this;
        }

        public WebhookUnsubscriptionRequest build() {

            return new WebhookUnsubscriptionRequest(this);
        }
    }
}
