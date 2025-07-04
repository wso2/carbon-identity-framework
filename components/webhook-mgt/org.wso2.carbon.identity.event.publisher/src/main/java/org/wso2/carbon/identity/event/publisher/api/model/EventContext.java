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

package org.wso2.carbon.identity.event.publisher.api.model;

/**
 * Model Class for Event Context.
 */
public class EventContext {

    private final String tenantDomain;
    private final String eventUri;
    private final String eventProfileName;
    private final String eventProfileVersion;

    private EventContext(Builder builder) {

        this.tenantDomain = builder.tenantDomain;
        this.eventUri = builder.eventUri;
        this.eventProfileName = builder.eventProfileName;
        this.eventProfileVersion = builder.eventProfileVersion;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public String getEventUri() {

        return eventUri;
    }

    public String getEventProfileName() {

        return eventProfileName;
    }

    public String getEventProfileVersion() {

        return eventProfileVersion;
    }

    public static Builder builder() {

        return new Builder();
    }

    /**
     * Builder class to build EventContext.
     */
    public static class Builder {

        private String tenantDomain;
        private String eventUri;
        private String eventProfileName;
        private String eventProfileVersion;

        public Builder() {

        }

        public Builder tenantDomain(String tenantDomain) {

            this.tenantDomain = tenantDomain;
            return this;
        }

        public Builder eventUri(String eventUri) {

            this.eventUri = eventUri;
            return this;
        }

        public Builder eventProfileName(String eventProfileName) {

            this.eventProfileName = eventProfileName;
            return this;
        }

        public Builder eventProfileVersion(String eventProfileVersion) {

            this.eventProfileVersion = eventProfileVersion;
            return this;
        }

        public EventContext build() {

            return new EventContext(this);
        }
    }
}
