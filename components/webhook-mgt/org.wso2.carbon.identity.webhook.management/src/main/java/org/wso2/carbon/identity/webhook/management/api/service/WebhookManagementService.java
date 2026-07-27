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

import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;

import java.util.List;

/**
 * Service interface for managing webhook subscriptions.
 * This interface defines the operations for creating, retrieving, updating, and deleting webhook subscriptions.
 */
public interface WebhookManagementService {

    /**
     * Create a new webhook subscription.
     *
     * @param webhook      Webhook subscription to be created.
     * @param tenantDomain Tenant domain.
     * @return Created webhook subscription.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    Webhook createWebhook(Webhook webhook, String tenantDomain) throws WebhookMgtException;

    /**
     * Get a webhook subscription by ID.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook subscription.
     */
    Webhook getWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Update a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param webhook      Updated webhook subscription.
     * @param tenantDomain Tenant domain.
     * @return Updated webhook subscription.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    Webhook updateWebhook(String webhookId, Webhook webhook, String tenantDomain) throws WebhookMgtException;

    /**
     * Delete a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    void deleteWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Get all webhooks for a tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return List of webhook subscriptions.
     * @throws WebhookMgtException If an error occurs while retrieving webhook subscriptions.
     */
    List<Webhook> getWebhooks(String tenantDomain) throws WebhookMgtException;

    /**
     * Get webhook events by webhook ID.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return List of webhook events.
     * @throws WebhookMgtException If an error occurs while retrieving webhook events.
     */
    List<Subscription> getWebhookEvents(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Enable a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Activated webhook subscription.
     * @throws WebhookMgtException If an error occurs while enabling the webhook.
     */
    Webhook activateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Disable a webhook subscription.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Deactivated webhook subscription.
     * @throws WebhookMgtException If an error occurs while disabling the webhook.
     */
    Webhook deactivateWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Retry a webhook subscription or unsubscription that has failed.
     *
     * @param webhookId    Webhook subscription ID.
     * @param tenantDomain Tenant domain.
     * @return Retried webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    Webhook retryWebhook(String webhookId, String tenantDomain) throws WebhookMgtException;

    /**
     * Get active webhooks for a specific channel URI and tenant ID.
     *
     * @param eventProfileName    Name of the event profile.
     * @param eventProfileVersion Version of the event profile.
     * @param channelUri          Channel URI to filter webhooks.
     * @param tenantDomain        Tenant domain.
     * @return List of active webhooks for the specified channel URI and tenant ID.
     * @throws WebhookMgtException If an error occurs while retrieving the active webhooks.
     */
    List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                    String tenantDomain) throws WebhookMgtException;
}
