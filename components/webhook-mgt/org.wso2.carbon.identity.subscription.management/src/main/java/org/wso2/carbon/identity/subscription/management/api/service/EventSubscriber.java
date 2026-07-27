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

package org.wso2.carbon.identity.subscription.management.api.service;

import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookSubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookUnsubscriptionRequest;

import java.util.List;

/**
 * Interface for specific webhook subscriber implementations.
 * This interface defines operations for handling webhook subscriptions with external systems.
 */
public interface EventSubscriber {

    /**
     * Get the name of this subscriber.
     * This name uniquely identifies the subscriber implementation.
     *
     * @return Name of the subscriber.
     */
    String getAssociatedAdapter();

    /**
     * Subscribe a webhook to the external system.
     *
     * @param subscriptionRequest Request containing details of the subscription.
     * @param tenantDomain        Tenant domain for which the subscription is being made.
     * @return List of subscriptions created for the specified channels.
     */
    List<Subscription> subscribe(WebhookSubscriptionRequest subscriptionRequest, String tenantDomain);

    /**
     * Unsubscribe a webhook from the external system.
     *
     * @param unsubscriptionRequest Request containing details of unsubscription.
     * @param tenantDomain          Tenant domain for which the unsubscription is being made.
     * @return List of subscriptions that were successfully unsubscribed.
     */
    List<Subscription> unsubscribe(WebhookUnsubscriptionRequest unsubscriptionRequest, String tenantDomain);
}
