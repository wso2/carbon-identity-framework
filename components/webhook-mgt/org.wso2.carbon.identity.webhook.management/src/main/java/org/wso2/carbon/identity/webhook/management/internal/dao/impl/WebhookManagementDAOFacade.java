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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.api.service.EventSubscriberService;
import org.wso2.carbon.identity.webhook.management.internal.component.WebhookManagementComponentServiceHolder;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.util.List;

/**
 * Facade implementation for WebhookManagementDAO.
 * This class coordinates between webhook subscriber services and webhook management DAO.
 * It ensures that webhooks are first subscribed with external systems before being persisted.
 */
public class WebhookManagementDAOFacade implements WebhookManagementDAO {

    private static final Log LOG = LogFactory.getLog(WebhookManagementDAOFacade.class);

    private final WebhookManagementDAO webhookManagementDAO;

    /**
     * Constructor with WebhookManagementDAO.
     *
     * @param webhookManagementDAO WebhookManagementDAO instance.
     */
    public WebhookManagementDAOFacade(WebhookManagementDAO webhookManagementDAO) {

        this.webhookManagementDAO = webhookManagementDAO;
    }

    /**
     * Create a new webhook subscription in the database.
     * First checks if a webhook with same endpoint exists, then subscribes with the webhook subscriber service,
     * and finally persists it using the DAO.
     *
     * @param webhook  Webhook subscription to be created.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();

        try {
            jdbcTemplate.withTransaction(template -> {
                subscriberService.subscribe(webhook, tenantDomain);
                webhookManagementDAO.createWebhook(webhook, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            LOG.debug("Error creating webhook: " + webhook.getUuid() +
                    " in tenant ID: " + tenantId, e);
            // If an error occurs after subscription but before adding to DB, attempt to unsubscribe
            try {
                subscriberService.unsubscribe(webhook, tenantDomain);
            } catch (Exception ex) {
                LOG.warn("Error unsubscribing webhook during rollback: ", ex);
            }
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e);
        }
    }

    /**
     * Get all webhooks for a tenant from the database.
     *
     * @param tenantId Tenant ID.
     * @return List of webhooks.
     * @throws WebhookMgtException If an error occurs while retrieving the webhooks.
     */
    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhooks(tenantId);
    }

    /**
     * Get a webhook subscription by ID from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return Webhook subscription.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook subscription.
     */
    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhook(webhookId, tenantId);
    }

    /**
     * Get webhook events for a specific webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return List of events subscribed to the webhook.
     * @throws WebhookMgtException If an error occurs while retrieving the events.
     */
    @Override
    public List<String> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.getWebhookEvents(webhookId, tenantId);
    }

    /**
     * Update a webhook subscription in the database.
     * First updates the webhook subscription with webhook subscriber service,
     * then updates its persistent state.
     *
     * @param webhook  Updated webhook subscription.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();

        try {
            jdbcTemplate.withTransaction(template -> {

                // Get the existing webhook
                Webhook existingWebhook = webhookManagementDAO.getWebhook(webhook.getUuid(), tenantId);

                // First unsubscribe the existing webhook
                try {
                    subscriberService.unsubscribe(existingWebhook, tenantDomain);
                } catch (WebhookMgtException e) {
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_WEBHOOK_UNSUBSCRIPTION_ERROR, e, webhook.getUuid());
                }

                // Then try to subscribe with the updated webhook
                try {
                    if (WebhookStatus.ACTIVE.equals(webhook.getStatus())) {
                        subscriberService.subscribe(webhook, tenantDomain);
                    }
                } catch (WebhookMgtException e) {
                    // Try to re-subscribe with the original webhook
                    try {
                        subscriberService.subscribe(existingWebhook, tenantDomain);
                    } catch (WebhookMgtException ex) {
                        LOG.error("Error re-subscribing original webhook during rollback: ", ex);
                    }
                    String errorMsg = "Error subscribing updated webhook with subscriber service";
                    throw WebhookManagementExceptionHandler.handleServerException(
                            ErrorMessage.ERROR_CODE_UNEXPECTED_ERROR, e, errorMsg);
                }

                // Finally update the persistent state
                webhookManagementDAO.updateWebhook(webhook, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            LOG.debug("Error updating webhook: " + webhook.getUuid() +
                    " in tenant ID: " + tenantId, e);
            throw WebhookManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR,
                    e);
        }
    }

    /**
     * Delete a webhook subscription from the database.
     * First unsubscribes the webhook using the webhook subscriber service,
     * then deletes its persistent state.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        EventSubscriberService subscriberService =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();

        try {
            jdbcTemplate.withTransaction(template -> {

                // Get the existing webhook
                Webhook existingWebhook = webhookManagementDAO.getWebhook(webhookId, tenantId);

                try {
                    subscriberService.unsubscribe(existingWebhook, tenantDomain);
                } catch (WebhookMgtException e) {
                    LOG.warn("Error unsubscribing webhook during deletion: " + existingWebhook.getUuid(), e);
                }
                webhookManagementDAO.deleteWebhook(webhookId, tenantId);
                return null;
            });

        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR, e);
        }
    }

    /**
     * Check if a webhook with the specified endpoint already exists for the tenant.
     *
     * @param endpoint Endpoint URL to check.
     * @param tenantId Tenant ID.
     * @return True if webhook endpoint already exists, false otherwise.
     * @throws WebhookMgtException If an error occurs while checking for webhook existence.
     */
    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        return webhookManagementDAO.isWebhookEndpointExists(endpoint, tenantId);
    }

    /**
     * Enable a webhook subscription in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return Activated webhook subscription.
     * @throws WebhookMgtException If an error occurs while enabling the webhook.
     */
    @Override
    public Webhook activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return toggleWebhookStatus(webhookId, tenantId, true);
    }

    /**
     * Disable a webhook subscription in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @return Deactivated webhook subscription.
     * @throws WebhookMgtException If an error occurs while disabling the webhook.
     */
    @Override
    public Webhook deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        return toggleWebhookStatus(webhookId, tenantId, false);
    }

    private Webhook toggleWebhookStatus(String webhookId, int tenantId, boolean activate) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        EventSubscriberService subscriberService =
                WebhookManagementComponentServiceHolder.getInstance().getEventSubscriberService();
        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);

        try {
            return jdbcTemplate.withTransaction(template -> {
                Webhook existingWebhook = webhookManagementDAO.getWebhook(webhookId, tenantId);
                if (activate) {
                    subscriberService.subscribe(existingWebhook, tenantDomain);
                    return webhookManagementDAO.activateWebhook(webhookId, tenantId);
                } else {
                    subscriberService.unsubscribe(existingWebhook, tenantDomain);
                    return webhookManagementDAO.deactivateWebhook(webhookId, tenantId);
                }
            });
        } catch (TransactionException e) {
            String operation = activate ? "activating" : "deactivating";
            LOG.debug("Error " + operation + " webhook: " + webhookId +
                    " in tenant ID: " + tenantId, e);
            if (activate) {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_ACTIVATION_ERROR, webhookId);
            } else {
                throw WebhookManagementExceptionHandler.handleClientException(
                        ErrorMessage.ERROR_CODE_WEBHOOK_DEACTIVATION_ERROR, webhookId);
            }
        }
    }
}
