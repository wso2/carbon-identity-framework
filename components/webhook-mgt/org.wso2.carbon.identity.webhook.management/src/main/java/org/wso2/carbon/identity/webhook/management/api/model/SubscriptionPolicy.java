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
 * Organization-level fanout policy of a webhook channel.
 * <p>
 * Determines which descendant organizations of the webhook-owning organization have their events
 * delivered to the webhook for a given channel. The policy records intent; the organizations it
 * resolves to are materialised as rows in IDN_WEBHOOK_CHANNEL_ORG_SUB.
 */
public enum SubscriptionPolicy {

    /**
     * No organization-level fanout. Only events raised in the webhook's own organization are
     * delivered. This is the default and preserves pre-existing behaviour.
     */
    NONE,

    /**
     * Events from every descendant organization are delivered, including organizations created
     * after the policy was set.
     */
    ALL_EXISTING_AND_FUTURE_ORGS,

    /**
     * Events are delivered only from an explicitly selected set of descendant organizations.
     * A selected organization does not imply its children.
     */
    SELECTED_ORGS_ONLY
}
