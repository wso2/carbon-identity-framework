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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_EXISTENCE_CHECK_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_EVENT_LIST_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_GET_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_LIST_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR;
import static org.wso2.carbon.identity.webhook.management.api.constant.ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR;

/**
 * Implementation of WebhookManagementDAO.
 * This class handles all the database operations related to webhook management.
 */
public class WebhookManagementDAOImpl implements WebhookManagementDAO {

    private static final String WEBHOOK_SCHEMA_VERSION = "v1";
    private static final String WEBHOOK_VERSION = "1.0.0";

    @Override
    public void createWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                int webhookId = addWebhookToDB(webhook, tenantId);
                addWebhookEventsToDB(webhookId, webhook.getEventsSubscribed());
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ADD_ERROR, e);
        }
    }

    @Override
    public void updateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                updateWebhookInDB(webhook, tenantId);
                int webhookId = getInternalWebhookIdByUuid(webhook.getUuid(), tenantId);
                deleteWebhookEventsInDB(webhookId);
                addWebhookEventsToDB(webhookId, webhook.getEventsSubscribed());
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_UPDATE_ERROR, e, webhook.getUuid());
        }
    }

    @Override
    public void deleteWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(WebhookSQLConstants.Query.DELETE_WEBHOOK,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_DELETE_ERROR, e, webhookId);
        }
    }

    @Override
    public Webhook getWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                Webhook webhook = template.fetchSingleRecord(WebhookSQLConstants.Query.GET_WEBHOOK_BY_ID,
                        (resultSet, rowNumber) -> mapResultSetToWebhook(resultSet),
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        });

                if (webhook == null) {
                    return null;
                }

                // Fetch events using UUID and TENANT_ID directly
                List<String> events = getWebhookEvents(webhookId, tenantId);

                // Build the Webhook without the secret
                return new Webhook.Builder()
                        .uuid(webhook.getUuid())
                        .endpoint(webhook.getEndpoint())
                        .name(webhook.getName())
                        .tenantId(webhook.getTenantId())
                        .eventSchemaName(webhook.getEventSchemaName())
                        .eventSchemaUri(webhook.getEventSchemaUri())
                        .secret(webhook.getSecret())
                        .status(webhook.getStatus())
                        .createdAt(webhook.getCreatedAt())
                        .updatedAt(webhook.getUpdatedAt())
                        .eventsSubscribed(events)
                        .build();
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_GET_ERROR, e, webhookId);
        }
    }

    @Override
    public List<String> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(
                            WebhookSQLConstants.Query.LIST_WEBHOOK_EVENTS_BY_UUID,
                            (resultSet, rowNumber) -> resultSet.getString(WebhookSQLConstants.Column.EVENT_NAME),
                            statement -> {
                                statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                                statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                            }
                    )
            );
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_EVENT_LIST_ERROR, e);
        }
    }

    @Override
    public List<Webhook> getWebhooks(int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(
                    template -> template.executeQuery(WebhookSQLConstants.Query.GET_WEBHOOKS_BY_TENANT,
                            (resultSet, rowNumber) -> mapResultSetToWebhook(resultSet),
                            statement -> statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId)));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_LIST_ERROR, e, IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }

    @Override
    public boolean isWebhookEndpointExists(String endpoint, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(WebhookSQLConstants.Query.CHECK_WEBHOOK_ENDPOINT_EXISTS,
                            (resultSet, rowNumber) -> true,
                            statement -> {
                                statement.setString(WebhookSQLConstants.Column.ENDPOINT, endpoint);
                                statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                            }) != null
            );
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_ENDPOINT_EXISTENCE_CHECK_ERROR, e, endpoint);
        }
    }

    @Override
    public void activateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        updateWebhookStatus(webhookId, tenantId, WebhookStatus.ACTIVE.name());
    }

    @Override
    public void deactivateWebhook(String webhookId, int tenantId) throws WebhookMgtException {

        updateWebhookStatus(webhookId, tenantId, WebhookStatus.INACTIVE.name());
    }

    // --- Private helper methods ---

    private Webhook mapResultSetToWebhook(ResultSet resultSet) throws SQLException {

        return new Webhook.Builder()
                .uuid(resultSet.getString(WebhookSQLConstants.Column.UUID))
                .endpoint(resultSet.getString(WebhookSQLConstants.Column.ENDPOINT))
                .name(resultSet.getString(WebhookSQLConstants.Column.NAME))
                .eventSchemaName(resultSet.getString(WebhookSQLConstants.Column.EVENT_SCHEMA_NAME))
                .eventSchemaUri(resultSet.getString(WebhookSQLConstants.Column.EVENT_SCHEMA_URI))
                .status(WebhookStatus.valueOf(resultSet.getString(WebhookSQLConstants.Column.STATUS)))
                .secret(resultSet.getString(WebhookSQLConstants.Column.SECRET))
                .tenantId(resultSet.getInt(WebhookSQLConstants.Column.TENANT_ID))
                .createdAt(resultSet.getTimestamp(WebhookSQLConstants.Column.CREATED_AT))
                .updatedAt(resultSet.getTimestamp(WebhookSQLConstants.Column.UPDATED_AT))
                .build();
    }

    private int addWebhookToDB(Webhook webhook, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        return jdbcTemplate.withTransaction(template -> template
                .executeInsert(WebhookSQLConstants.Query.CREATE_WEBHOOK,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, webhook.getUuid());
                            statement.setString(WebhookSQLConstants.Column.ENDPOINT, webhook.getEndpoint());
                            statement.setString(WebhookSQLConstants.Column.NAME, webhook.getName());
                            statement.setString(WebhookSQLConstants.Column.SECRET, webhook.getSecret());
                            statement.setString(WebhookSQLConstants.Column.VERSION, WEBHOOK_VERSION);
                            statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_NAME,
                                    webhook.getEventSchemaName());
                            statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_URI,
                                    webhook.getEventSchemaUri());
                            statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_VERSION,
                                    WEBHOOK_SCHEMA_VERSION);
                            statement.setString(WebhookSQLConstants.Column.STATUS, String.valueOf(webhook.getStatus()));
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        }, webhook, true));
    }

    private void updateWebhookInDB(Webhook webhook, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(WebhookSQLConstants.Query.UPDATE_WEBHOOK,
                    statement -> {
                        statement.setString(WebhookSQLConstants.Column.ENDPOINT, webhook.getEndpoint());
                        statement.setString(WebhookSQLConstants.Column.NAME, webhook.getName());
                        statement.setString(WebhookSQLConstants.Column.SECRET, webhook.getSecret());
                        statement.setString(WebhookSQLConstants.Column.VERSION, WEBHOOK_VERSION);
                        statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_NAME, webhook.getEventSchemaName());
                        statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_URI, webhook.getEventSchemaUri());
                        statement.setString(WebhookSQLConstants.Column.EVENT_SCHEMA_VERSION, WEBHOOK_SCHEMA_VERSION);
                        statement.setString(WebhookSQLConstants.Column.STATUS, String.valueOf(webhook.getStatus()));
                        statement.setString(WebhookSQLConstants.Column.UUID, webhook.getUuid());
                        statement.setString(WebhookSQLConstants.Column.CREATED_AT, String.valueOf(Instant.now()));
                        statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                    });
            return null;
        });
    }

    private int getInternalWebhookIdByUuid(String uuid, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        return jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(WebhookSQLConstants.Query.GET_WEBHOOK_INTERNAL_ID_BY_ID,
                        (resultSet, rowNumber) -> resultSet.getInt(WebhookSQLConstants.Column.ID),
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, uuid);
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        }));
    }

    private void addWebhookEventsToDB(int webhookId, List<String> events) throws TransactionException {

        if (events == null || events.isEmpty()) {
            return;
        }
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeBatchInsert(WebhookSQLConstants.Query.ADD_WEBHOOK_EVENT,
                    statement -> {
                        for (String event : events) {
                            statement.setInt(WebhookSQLConstants.Column.WEBHOOK_ID, webhookId);
                            statement.setString(WebhookSQLConstants.Column.EVENT_NAME, event);
                            statement.addBatch();
                        }
                    }, null);
            return null;
        });
    }

    private void deleteWebhookEventsInDB(int webhookId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(WebhookSQLConstants.Query.DELETE_WEBHOOK_EVENTS,
                    statement -> statement.setInt(WebhookSQLConstants.Column.WEBHOOK_ID, webhookId));
            return null;
        });
    }

    private void updateWebhookStatus(String webhookId, int tenantId, String status) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(
                        status.equals(WebhookStatus.ACTIVE.name()) ?
                                WebhookSQLConstants.Query.ACTIVATE_WEBHOOK :
                                WebhookSQLConstants.Query.DEACTIVATE_WEBHOOK,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        });
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR, e, status);
        }
    }
}
