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

package org.wso2.carbon.identity.webhook.management.internal.dao;

import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;

import java.util.List;

/**
 * Data Access Object interface for webhook management.
 */
public interface WebhookManagementDAO {

    /**
     * Create a new webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be created.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Get a webhook subscription by ID from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return Webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook subscription.
     */
    Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Update a webhook subscription in the database.
     *
     * @param webhook  Updated webhook subscription.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Delete a webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Get all webhooks for a tenant from the database.
     *
     * @param tenantId Tenant ID.
     * @return List of webhook subscriptions.
     * @throws WebhookMgtException If an error occurs while retrieving webhook subscriptions.
     */
    List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException;

    /**
     * Get webhook events for a specific webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return List of events subscribed to the webhook.
     * @throws WebhookMgtException If an error occurs while retrieving the events.
     */
    List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException;

    /**
     * Check if a webhook endpoint exists in the database.
     *
     * @param endpoint Webhook endpoint URL.
     * @param tenantId Tenant ID.
     * @return True if the endpoint exists, false otherwise.
     * @throws WebhookMgtException If an error occurs while checking the endpoint.
     */
    boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException;

    /**
     * Enable a webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be activated.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Disable a webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be deactivated.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Retry a webhook subscription or unsubscription that has failed.
     *
     * @param webhook  Webhook subscription to be retried.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while retrying the webhook.
     */
    void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException;

    /**
     * Get the count of webhooks for a tenant.
     *
     * @param tenantId Tenant ID.
     * @return Count of webhooks.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook count.
     */
    int getWebhooksCount(int tenantId) throws WebhookMgtException;

    /**
     * Get active webhooks for a specific channel URI and tenant ID.
     *
     * @param eventProfileName    Event profile name to filter webhooks.
     * @param eventProfileVersion Event profile version to filter webhooks.
     * @param channelUri          Channel URI to filter webhooks.
     * @param tenantId            Tenant ID.
     * @return List of active webhooks for the specified channel URI and tenant ID.
     * @throws WebhookMgtException If an error occurs while retrieving the active webhooks.
     */
    List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion, String channelUri,
                                    int tenantId) throws WebhookMgtException;
}
