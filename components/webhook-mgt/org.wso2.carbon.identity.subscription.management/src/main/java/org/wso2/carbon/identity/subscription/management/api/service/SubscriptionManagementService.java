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

import org.wso2.carbon.identity.subscription.management.api.exception.SubscriptionManagementException;
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookSubscriptionRequest;
import org.wso2.carbon.identity.subscription.management.api.model.WebhookUnsubscriptionRequest;

import java.util.List;

/**
 * Service implementation for event subscription operations.
 * This class manages event subscribers and delegates subscription operations to appropriate subscribers.
 */
public interface SubscriptionManagementService {

    /**
     * Subscribe events from external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param subscriptionRequest Request containing details of the subscription.
     * @param adapter             Name of the adapter to be used for subscription.
     * @param tenantDomain        Tenant domain for which the subscription is being made.
     * @return List of subscriptions that were successfully subscribed.
     * @throws SubscriptionManagementException If an error occurs during subscription.
     */
    List<Subscription> subscribe(WebhookSubscriptionRequest subscriptionRequest, String adapter,
                                        String tenantDomain)
            throws SubscriptionManagementException;

    /**
     * Unsubscribe events from external systems.
     * This method delegates to appropriate event subscribers.
     *
     * @param unsubscriptionRequest Request containing details of the unsubscription.
     * @param adapter               Name of the adapter to be used for unsubscription.
     * @param tenantDomain          Tenant domain for which the unsubscription is being made.
     * @return List of subscriptions that were successfully unsubscribed.
     * @throws SubscriptionManagementException If an error occurs during unsubscription.
     */
    List<Subscription> unsubscribe(WebhookUnsubscriptionRequest unsubscriptionRequest, String adapter,
                                          String tenantDomain)
            throws SubscriptionManagementException;
}
