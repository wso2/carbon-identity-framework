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

package org.wso2.carbon.identity.webhook.management.api.service;

import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;

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
    String getName();

    /**
     * Subscribe a webhook to the external system.
     *
     * @param events       List of events to subscribe to.
     * @param endpoint     The endpoint URL to which the webhook will send notifications.
     * @param tenantDomain Tenant domain.
     * @return True if subscription is successful, false otherwise.
     * @throws WebhookMgtException If an error occurs during subscription.
     */
    boolean subscribe(List<String> events, String endpoint, String tenantDomain) throws WebhookMgtException;

    /**
     * Unsubscribe a webhook from the external system.
     *
     * @param events       List of events to unsubscribe from.
     * @param endpoint     The endpoint URL from which the webhook will stop sending notifications.
     * @param tenantDomain Tenant domain.
     * @return True if unsubscription is successful, false otherwise.
     * @throws WebhookMgtException If an error occurs during unsubscription.
     */
    boolean unsubscribe(List<String> events, String endpoint, String tenantDomain) throws WebhookMgtException;
}
