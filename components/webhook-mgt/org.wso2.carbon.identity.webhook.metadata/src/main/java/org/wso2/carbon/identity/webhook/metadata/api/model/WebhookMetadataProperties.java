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

package org.wso2.carbon.identity.webhook.metadata.api.model;

import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;

import java.util.Objects;

/**
 * Represents metadata properties for a webhook.
 */
public class WebhookMetadataProperties {

    private final PolicyEnum organizationPolicy;

    private WebhookMetadataProperties(Builder builder) {

        this.organizationPolicy = builder.organizationPolicy;
    }

    public PolicyEnum getOrganizationPolicy() {

        return organizationPolicy;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (!(o instanceof WebhookMetadataProperties)) {
            return false;
        }
        WebhookMetadataProperties that = (WebhookMetadataProperties) o;
        return organizationPolicy == that.organizationPolicy;
    }

    @Override
    public int hashCode() {

        return Objects.hash(organizationPolicy);
    }

    @Override
    public String toString() {

        return "WebhookMetadataProperties{" +
                "organizationPolicy=" + organizationPolicy +
                '}';
    }

    /**
     * Builder class for WebhookMetadataProperties.
     */
    public static class Builder {

        private PolicyEnum organizationPolicy;

        public Builder organizationPolicy(PolicyEnum organizationPolicy) {

            this.organizationPolicy = organizationPolicy;
            return this;
        }

        public WebhookMetadataProperties build() {

            return new WebhookMetadataProperties(this);
        }
    }
}
