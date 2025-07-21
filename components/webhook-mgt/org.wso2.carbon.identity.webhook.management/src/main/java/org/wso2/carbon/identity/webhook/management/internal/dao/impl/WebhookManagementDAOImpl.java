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
import org.wso2.carbon.identity.subscription.management.api.model.Subscription;
import org.wso2.carbon.identity.subscription.management.api.model.SubscriptionStatus;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.Webhook;
import org.wso2.carbon.identity.webhook.management.api.model.WebhookStatus;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookManagementDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ADD_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_DELETE_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_ENDPOINT_EXISTENCE_CHECK_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_EVENT_LIST_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_GET_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_LIST_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_RETRY_STATUS_UPDATE_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_UPDATE_ERROR;
import static org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants.Column.WEBHOOK_COUNT;

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
                int webhookId = getInternalWebhookIdByUuid(webhook.getId(), tenantId);
                deleteWebhookEventsInDB(webhookId);
                addWebhookEventsToDB(webhookId, webhook.getEventsSubscribed());
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ERROR_CODE_WEBHOOK_UPDATE_ERROR, e, webhook.getId());
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
                List<Subscription> events = getWebhookEvents(webhookId, tenantId);

                // Build the Webhook without the secret
                return new Webhook.Builder()
                        .uuid(webhook.getId())
                        .endpoint(webhook.getEndpoint())
                        .name(webhook.getName())
                        .eventProfileName(webhook.getEventProfileName())
                        .eventProfileUri(webhook.getEventProfileUri())
                        .eventProfileVersion(webhook.getEventProfileVersion())
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
    public List<Subscription> getWebhookEvents(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(
                            WebhookSQLConstants.Query.LIST_WEBHOOK_EVENTS_BY_UUID,
                            (resultSet, rowNumber) -> {
                                String statusStr =
                                        resultSet.getString(WebhookSQLConstants.Column.CHANNEL_SUBSCRIPTION_STATUS);
                                SubscriptionStatus status =
                                        statusStr != null ? SubscriptionStatus.valueOf(statusStr) : null;
                                return Subscription.builder()
                                        .channelUri(resultSet.getString(WebhookSQLConstants.Column.CHANNEL_URI))
                                        .status(status)
                                        .build();
                            },
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
    public void activateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        processWebhookStatusUpdate(webhook.getId(), tenantId, webhook.getEventsSubscribed(), webhook.getStatus(),
                ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR);
    }

    @Override
    public void deactivateWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        processWebhookStatusUpdate(webhook.getId(), tenantId, webhook.getEventsSubscribed(), webhook.getStatus(),
                ERROR_CODE_WEBHOOK_STATUS_UPDATE_ERROR);
    }

    @Override
    public void retryWebhook(Webhook webhook, int tenantId) throws WebhookMgtException {

        processWebhookStatusUpdate(webhook.getId(), tenantId, webhook.getEventsSubscribed(), webhook.getStatus(),
                ERROR_CODE_WEBHOOK_RETRY_STATUS_UPDATE_ERROR);
    }

    @Override
    public int getWebhooksCount(int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(
                            WebhookSQLConstants.Query.COUNT_WEBHOOKS_BY_TENANT,
                            (resultSet, rowNumber) -> resultSet.getInt(WEBHOOK_COUNT),
                            statement -> statement.setInt
                                    (WebhookSQLConstants.Column.TENANT_ID, tenantId)
                    )
            );
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_WEBHOOKS_COUNT, e,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }

    @Override
    public List<Webhook> getActiveWebhooks(String eventProfileName, String eventProfileVersion,
                                           String channelUri, int tenantId)
            throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(
                            WebhookSQLConstants.Query.GET_ACTIVE_WEBHOOKS_BY_PROFILE_CHANNEL,
                            (resultSet, rowNumber) -> mapResultSetToWebhook(resultSet),
                            statement -> {
                                statement.setString(WebhookSQLConstants.Column.CHANNEL_URI, channelUri);
                                statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                                statement.setString(WebhookSQLConstants.Column.STATUS, WebhookStatus.ACTIVE.name());
                                statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_NAME, eventProfileName);
                                statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_VERSION,
                                        eventProfileVersion);
                            }
                    )
            );
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_ACTIVE_WEBHOOKS_BY_PROFILE_CHANNEL_ERROR, e, channelUri,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }

    // --- Private helper methods ---

    private Webhook mapResultSetToWebhook(ResultSet resultSet) throws SQLException {

        return new Webhook.Builder()
                .uuid(resultSet.getString(WebhookSQLConstants.Column.UUID))
                .endpoint(resultSet.getString(WebhookSQLConstants.Column.ENDPOINT))
                .name(resultSet.getString(WebhookSQLConstants.Column.NAME))
                .eventProfileName(resultSet.getString(WebhookSQLConstants.Column.EVENT_PROFILE_NAME))
                .eventProfileUri(resultSet.getString(WebhookSQLConstants.Column.EVENT_PROFILE_URI))
                .eventProfileVersion(resultSet.getString(WebhookSQLConstants.Column.EVENT_PROFILE_VERSION))
                .status(WebhookStatus.valueOf(resultSet.getString(WebhookSQLConstants.Column.STATUS)))
                .secret(resultSet.getString(WebhookSQLConstants.Column.SECRET_ALIAS))
                .createdAt(resultSet.getTimestamp(WebhookSQLConstants.Column.CREATED_AT))
                .updatedAt(resultSet.getTimestamp(WebhookSQLConstants.Column.UPDATED_AT))
                .build();
    }

    private int addWebhookToDB(Webhook webhook, int tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        return jdbcTemplate.withTransaction(template -> template
                .executeInsert(WebhookSQLConstants.Query.CREATE_WEBHOOK,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.UUID, webhook.getId());
                            statement.setString(WebhookSQLConstants.Column.ENDPOINT, webhook.getEndpoint());
                            statement.setString(WebhookSQLConstants.Column.NAME, webhook.getName());
                            statement.setString(WebhookSQLConstants.Column.SECRET_ALIAS, webhook.getSecret());
                            statement.setString(WebhookSQLConstants.Column.VERSION, WEBHOOK_VERSION);
                            statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_NAME,
                                    webhook.getEventProfileName());
                            statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_URI,
                                    webhook.getEventProfileUri());
                            statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_VERSION,
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
                        statement.setString(WebhookSQLConstants.Column.SECRET_ALIAS, webhook.getSecret());
                        statement.setString(WebhookSQLConstants.Column.VERSION, WEBHOOK_VERSION);
                        statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_NAME,
                                webhook.getEventProfileName());
                        statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_URI, webhook.getEventProfileUri());
                        statement.setString(WebhookSQLConstants.Column.EVENT_PROFILE_VERSION, WEBHOOK_SCHEMA_VERSION);
                        statement.setString(WebhookSQLConstants.Column.STATUS, String.valueOf(webhook.getStatus()));
                        statement.setString(WebhookSQLConstants.Column.UUID, webhook.getId());
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

    private void addWebhookEventsToDB(int webhookId, List<Subscription> events) throws TransactionException {

        if (events == null || events.isEmpty()) {
            return;
        }
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeBatchInsert(WebhookSQLConstants.Query.ADD_WEBHOOK_EVENT,
                    statement -> {
                        statement.setInt(WebhookSQLConstants.Column.WEBHOOK_ID, webhookId);
                        for (Subscription event : events) {
                            statement.setString(WebhookSQLConstants.Column.CHANNEL_URI, event.getChannelUri());
                            String status = event.getStatus() != null ? event.getStatus().name() : null;
                            statement.setString(WebhookSQLConstants.Column.CHANNEL_SUBSCRIPTION_STATUS, status);
                            statement.addBatch();
                        }
                    }, null);
            return null;
        });
    }

    private void updateWebhookEventStatusInDB(int webhookId, List<Subscription> channels) throws TransactionException {

        if (channels == null || channels.isEmpty()) {
            return;
        }
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            for (Subscription event : channels) {
                if (event.getStatus() == null) {
                    continue;
                }
                template.executeUpdate(WebhookSQLConstants.Query.UPDATE_WEBHOOK_EVENT_STATUS,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.CHANNEL_SUBSCRIPTION_STATUS,
                                    event.getStatus().name());
                            statement.setInt(WebhookSQLConstants.Column.WEBHOOK_ID, webhookId);
                            statement.setString(WebhookSQLConstants.Column.CHANNEL_URI, event.getChannelUri());
                        });
            }
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

    private void processWebhookStatusUpdate(String webhookId, int tenantId, List<Subscription> channels,
                                            WebhookStatus status, ErrorMessage errorMessage)
            throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(
                        WebhookSQLConstants.Query.UPDATE_WEBHOOK_STATUS,
                        statement -> {
                            statement.setString(WebhookSQLConstants.Column.STATUS, status.name());
                            statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                            statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                        }
                );

                int internalWebhookId = getInternalWebhookIdByUuid(webhookId, tenantId);
                updateWebhookEventStatusInDB(internalWebhookId, channels);

                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    errorMessage, e, webhookId);
        }
    }
}
