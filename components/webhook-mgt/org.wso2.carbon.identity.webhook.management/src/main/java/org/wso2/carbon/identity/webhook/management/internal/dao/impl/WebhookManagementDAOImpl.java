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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.Column;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.Query;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of WebhookManagementDAO interface.
 */
public class WebhookManagementDAOImpl implements WebhookManagementDAO {

    private static final String WEBHOOK_SCHEMA_VERSION = "v1";
    private static final String WEBHOOK_VERSION = "1.0.0";

    /**
     * Create a new webhook subscription in the database.
     *
     * @param webhook  Webhook subscription to be created.
     * @param tenantId Tenant ID.
     * @throws WebhookMgtException If an error occurs while creating the webhook subscription.
     */
    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                Integer webhookInternalId = addWebhookToDB(webhook, tenantId);
                if (webhook.getEventsSubscribed() != null && !webhook.getEventsSubscribed().isEmpty() &&
                        webhookInternalId != null) {
                    addWebhookToEventsDB(webhookInternalId, webhook.getEventsSubscribed());
                }
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR, e, webhook.getId());
        }
    }

    /**
     * Update a webhook subscription in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param webhook   Updated webhook subscription.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while updating the webhook subscription.
     */
    @Override
    public void updateWebhook(String webhookId, Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                updateWebhookInDB(webhookId, webhook, tenantId);
                Integer webhookInternalId = getWebhookInternalId(webhookId, tenantId);
                if (webhookInternalId != null && webhook.getEventsSubscribed() != null) {
                    // Delete existing events
                    deleteAllWebhookEvents(webhookInternalId);
                    // Add new events
                    addWebhookToEventsDB(webhookInternalId, webhook.getEventsSubscribed());
                }
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR, e, webhook.getId());
        }
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

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Webhook webhookData = new Webhook();
        try {
            jdbcTemplate.withTransaction(
                    template -> template.fetchSingleRecord(Query.GET_WEBHOOK_BY_ID,
                            (resultSet, rowNumber) -> {
                                Webhook mappedWebhook = mapWebhook(resultSet);
                                webhookData.setId(mappedWebhook.getId());
                                webhookData.setEndpoint(mappedWebhook.getEndpoint());
                                webhookData.setDescription(mappedWebhook.getDescription());
                                webhookData.setSecret(mappedWebhook.getSecret());
                                webhookData.setEventSchemaName(mappedWebhook.getEventSchemaName());
                                webhookData.setEventSchemaUri(mappedWebhook.getEventSchemaUri());
                                webhookData.setStatus(mappedWebhook.getStatus());
                                webhookData.setCreatedAt(mappedWebhook.getCreatedAt());
                                webhookData.setUpdatedAt(mappedWebhook.getUpdatedAt());
                                return null;
                            },
                            preparedStatement -> {
                                preparedStatement.setString(Column.UUID, webhookId);
                                preparedStatement.setInt(Column.TENANT_ID, tenantId);
                            }));

            if (StringUtils.isEmpty(webhookData.getId())) {
                return null;
            }
            Integer webhookInternalId = getWebhookInternalId(webhookId, tenantId);
            if (webhookInternalId != null) {
                webhookData.setEventsSubscribed(listWebhookEvents(webhookInternalId));
            }
            return webhookData;
        } catch (TransactionException | DataAccessException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_GET_ERROR, e, webhookId);
        }
    }

    /**
     * Delete a webhook subscription from the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while deleting the webhook subscription.
     */
    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.DELETE_WEBHOOK,
                        preparedStatement -> {
                            preparedStatement.setString(Column.UUID, webhookId);
                            preparedStatement.setInt(Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR, e, webhookId);
        }
    }

    /**
     * Get all webhooks for a tenant from the database.
     *
     * @param tenantId Tenant ID.
     * @return List of webhook subscriptions.
     * @throws WebhookMgtException If an error occurs while retrieving webhook subscriptions.
     */
    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        List<Webhook> webhooks;

        try {
            webhooks = jdbcTemplate.executeQuery(Query.LIST_ALL_WEBHOOKS,
                    (resultSet, rowNumber) -> mapWebhook(resultSet),
                    preparedStatement -> {
                        preparedStatement.setInt(Column.TENANT_ID, tenantId);
                    });

            // Fetch events for each webhook
            for (Webhook webhook : webhooks) {
                Integer webhookInternalId = getWebhookInternalId(webhook.getId(), tenantId);
                if (webhookInternalId != null) {
                    webhook.setEventsSubscribed(listWebhookEvents(webhookInternalId));
                }
            }

            return webhooks;
        } catch (DataAccessException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_LIST_ERROR, e, IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }

    /**
     * Check if a webhook endpoint already exists for a tenant.
     *
     * @param endpoint Webhook endpoint URL.
     * @param tenantId Tenant ID.
     * @return True if the webhook endpoint exists, false otherwise.
     * @throws WebhookMgtException If an error occurs while checking for webhook existence.
     */
    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.fetchSingleRecord(Query.CHECK_WEBHOOK_ENDPOINT_EXISTS,
                    (resultSet, rowNumber) -> resultSet.getInt(1) > 0,
                    preparedStatement -> {
                        preparedStatement.setString(Column.ENDPOINT, endpoint);
                        preparedStatement.setInt(Column.TENANT_ID, tenantId);
                    });
        } catch (DataAccessException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_GET_ERROR, e, endpoint);
        }
    }

    /**
     * Enable a webhook subscription in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while enabling the webhook.
     */
    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.ACTIVATE_WEBHOOK,
                        preparedStatement -> {
                            preparedStatement.setString(Column.UUID, webhookId);
                            preparedStatement.setInt(Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR, e, webhookId);
        }
    }

    /**
     * Disable a webhook subscription in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param tenantId  Tenant ID.
     * @throws WebhookMgtException If an error occurs while disabling the webhook.
     */
    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.DEACTIVATE_WEBHOOK,
                        preparedStatement -> {
                            preparedStatement.setString(Column.UUID, webhookId);
                            preparedStatement.setInt(Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR, e, webhookId);
        }
    }

    /**
     * Get the internal ID of a webhook subscription.
     *
     * @param webhookId Webhook subscription ID (UUID).
     * @param tenantId  Tenant ID.
     * @return Internal ID of the webhook.
     * @throws WebhookMgtException If an error occurs while retrieving the webhook ID.
     */
    private Integer getWebhookInternalId(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.fetchSingleRecord(Query.GET_WEBHOOK_BY_ID,
                    (resultSet, rowNumber) -> resultSet.getInt(Column.ID),
                    preparedStatement -> {
                        preparedStatement.setString(Column.UUID, webhookId);
                        preparedStatement.setInt(Column.TENANT_ID, tenantId);
                    });
        } catch (DataAccessException e) {
            if (e.getMessage().contains("No result")) {
                return null;
            }
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_DATABASE_ERROR, e);
        }
    }

    /**
     * Add events to a webhook subscription.
     *
     * @param webhookId Internal ID of the webhook.
     * @param events    List of events to add.
     * @throws DataAccessException If an error occurs while adding events.
     */
    private void addWebhookToEventsDB(Integer webhookId, List<String> events) throws DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        for (String event : events) {
            jdbcTemplate.executeUpdate(Query.ADD_WEBHOOK_EVENT,
                    preparedStatement -> {
                        preparedStatement.setInt(Column.WEBHOOK_ID, webhookId);
                        preparedStatement.setString(Column.EVENT_NAME, event);
                    });
        }
    }

    /**
     * List events associated with a webhook subscription.
     *
     * @param webhookId Internal ID of the webhook.
     * @return List of events.
     * @throws DataAccessException If an error occurs while retrieving events.
     */
    private List<String> listWebhookEvents(Integer webhookId) throws DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        return jdbcTemplate.executeQuery(
                Query.LIST_WEBHOOK_EVENTS,
                (resultSet, rowNumber) -> resultSet.getString(Column.EVENT_NAME),
                preparedStatement -> preparedStatement.setInt(Column.WEBHOOK_ID, webhookId)
        );
    }

    /**
     * Delete all events associated with a webhook subscription.
     *
     * @param webhookId Internal ID of the webhook.
     * @throws DataAccessException If an error occurs while deleting events.
     */
    private void deleteAllWebhookEvents(Integer webhookId) throws DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.executeUpdate(Query.DELETE_WEBHOOK_EVENTS,
                preparedStatement -> {
                    preparedStatement.setInt(Column.WEBHOOK_ID, webhookId);
                });
    }

    /**
     * Map a database result set to a Webhook object.
     *
     * @param resultSet Database result set.
     * @return Webhook object.
     * @throws SQLException If an error occurs while accessing the result set.
     */
    private Webhook mapWebhook(ResultSet resultSet) throws SQLException {

        Webhook webhook = new Webhook();
        webhook.setId(resultSet.getString(Column.UUID));
        webhook.setEndpoint(resultSet.getString(Column.ENDPOINT));
        webhook.setDescription(resultSet.getString(Column.DESCRIPTION));
        webhook.setSecret(resultSet.getString(Column.SECRET));
        webhook.setEventSchemaName(resultSet.getString(Column.EVENT_SCHEMA_NAME));
        webhook.setEventSchemaUri(resultSet.getString(Column.EVENT_SCHEMA_URI));
        webhook.setStatus(resultSet.getString(Column.STATUS));
        webhook.setCreatedAt(resultSet.getTimestamp(Column.CREATED_AT));
        webhook.setUpdatedAt(resultSet.getTimestamp(Column.UPDATED_AT));
        return webhook;
    }

    /**
     * Add webhook to the database.
     *
     * @param webhook  Webhook subscription to be created.
     * @param tenantId Tenant ID.
     * @return Internal ID of the created webhook.
     * @throws TransactionException If an error occurs during the transaction.
     */
    private Integer addWebhookToDB(Webhook webhook, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        return jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(Query.CREATE_WEBHOOK,
                    preparedStatement -> {
                        preparedStatement.setString(Column.UUID, webhook.getId());
                        preparedStatement.setString(Column.ENDPOINT, webhook.getEndpoint());
                        preparedStatement.setString(Column.DESCRIPTION, webhook.getDescription());
                        preparedStatement.setString(Column.SECRET, webhook.getSecret());
                        preparedStatement.setString(Column.VERSION, WEBHOOK_VERSION);
                        preparedStatement.setString(Column.EVENT_SCHEMA_NAME, webhook.getEventSchemaName());
                        preparedStatement.setString(Column.EVENT_SCHEMA_URI, webhook.getEventSchemaUri());
                        preparedStatement.setString(Column.EVENT_SCHEMA_VERSION, WEBHOOK_SCHEMA_VERSION);
                        preparedStatement.setString(Column.STATUS,
                                webhook.getStatusString());
                        preparedStatement.setInt(Column.TENANT_ID, tenantId);
                    });

            return getWebhookInternalId(webhook.getId(), tenantId);
        });
    }

    /**
     * Update webhook in the database.
     *
     * @param webhookId Webhook subscription ID.
     * @param webhook   Updated webhook subscription.
     * @param tenantId  Tenant ID.
     * @throws DataAccessException If an error occurs while updating the webhook.
     */
    private void updateWebhookInDB(String webhookId, Webhook webhook, int tenantId)
            throws DataAccessException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.executeUpdate(Query.UPDATE_WEBHOOK,
                preparedStatement -> {
                    preparedStatement.setString(Column.ENDPOINT, webhook.getEndpoint());
                    preparedStatement.setString(Column.DESCRIPTION, webhook.getDescription());
                    preparedStatement.setString(Column.SECRET, webhook.getSecret());
                    preparedStatement.setString(Column.VERSION, WEBHOOK_VERSION);
                    preparedStatement.setString(Column.EVENT_SCHEMA_NAME, webhook.getEventSchemaName());
                    preparedStatement.setString(Column.EVENT_SCHEMA_URI, webhook.getEventSchemaUri());
                    preparedStatement.setString(Column.EVENT_SCHEMA_VERSION, WEBHOOK_SCHEMA_VERSION);
                    preparedStatement.setString(Column.STATUS,
                            webhook.getStatusString());
                    preparedStatement.setString(Column.UUID, webhookId);
                    preparedStatement.setInt(Column.TENANT_ID, tenantId);
                });
    }
}
