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

package org.wso2.carbon.identity.webhook.management.internal.dao;

import org.wso2.carbon.identity.webhook.management.api.model.SubscriptionPolicy;

import java.util.Map;

/**
 * An organization subscription with every organization database read already resolved, so that applying it
 * inside the identity transaction needs only the identity database.
 */
public class PlannedOrganizationSubscription {

    private final String channelUri;
    private final SubscriptionPolicy policy;
    private final String owningOrgId;
    private final Map<String, Integer> tenantIdsByOrgId;

    public PlannedOrganizationSubscription(String channelUri, SubscriptionPolicy policy, String owningOrgId,
                                           Map<String, Integer> tenantIdsByOrgId) {

        this.channelUri = channelUri;
        this.policy = policy;
        this.owningOrgId = owningOrgId;
        this.tenantIdsByOrgId = tenantIdsByOrgId;
    }

    /**
     * URI of the channel the subscription applies to.
     *
     * @return Channel URI.
     */
    public String getChannelUri() {

        return channelUri;
    }

    /**
     * Policy to apply to the channel.
     *
     * @return Subscription policy.
     */
    public SubscriptionPolicy getPolicy() {

        return policy;
    }

    /**
     * Organization owning the webhook, which holds the policy when one is written.
     *
     * @return Owning organization id.
     */
    public String getOwningOrgId() {

        return owningOrgId;
    }

    /**
     * Organizations to subscribe, each against the tenant its events are raised in.
     *
     * @return Tenant id by organization id, in the order the organizations were given.
     */
    public Map<String, Integer> getTenantIdsByOrgId() {

        return tenantIdsByOrgId;
    }
}
