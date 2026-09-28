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

package org.wso2.carbon.identity.webhook.management.internal.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.webhook.management.api.exception.WebhookMgtException;
import org.wso2.carbon.identity.webhook.management.api.model.ChannelOrgSubscription;
import org.wso2.carbon.identity.webhook.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.webhook.management.internal.constant.WebhookSQLConstants;
import org.wso2.carbon.identity.webhook.management.internal.dao.WebhookChannelDAO;
import org.wso2.carbon.identity.webhook.management.internal.util.WebhookManagementExceptionHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of {@link WebhookChannelDAO}.
 * <p>
 * Rows are removed automatically when their channel is deleted, through the foreign key on
 * CHANNEL_UUID. A webhook subscribed to a thousand organizations is therefore a bounded local
 * cascade rather than a fan-out: no hub call and no per-organization state lives outside the
 * database.
 */
public class WebhookChannelDAOImpl implements WebhookChannelDAO {

    @Override
    public void addChannelOrgSubscriptions(List<ChannelOrgSubscription> subscriptions) throws WebhookMgtException {

        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(WebhookSQLConstants.Query.ADD_CHANNEL_ORG_SUBSCRIPTION,
                        statement -> {
                            for (ChannelOrgSubscription subscription : subscriptions) {
                                statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID,
                                        subscription.getChannelUuid());
                                statement.setInt(WebhookSQLConstants.Column.SUBSCRIBED_ORG_TENANT_ID,
                                        subscription.getSubscribedOrgTenantId());
                                statement.setString(WebhookSQLConstants.Column.SUBSCRIBED_ORG_ID,
                                        subscription.getSubscribedOrgId());
                                statement.setString(WebhookSQLConstants.Column.TOPIC_UUID,
                                        subscription.getTopicUuid());
                                statement.setString(WebhookSQLConstants.Column.TOPIC, subscription.getTopic());
                                statement.addBatch();
                            }
                        }, null);
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_ADD_ERROR, e,
                    subscriptions.get(0).getChannelUuid());
        }
    }

    @Override
    public List<ChannelOrgSubscription> getChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(WebhookSQLConstants.Query.LIST_CHANNEL_ORG_SUBSCRIPTIONS,
                            (resultSet, rowNumber) -> mapResultSetToChannelOrgSubscription(resultSet),
                            statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid)));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_LIST_ERROR, e, channelUuid);
        }
    }

    @Override
    public List<Integer> getSubscribedOrgTenantIds(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(WebhookSQLConstants.Query.LIST_CHANNEL_ORG_SUBSCRIPTION_TENANTS,
                            (resultSet, rowNumber) ->
                                    resultSet.getInt(WebhookSQLConstants.Column.SUBSCRIBED_ORG_TENANT_ID),
                            statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid)));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_LIST_ERROR, e, channelUuid);
        }
    }

    @Override
    public int getChannelOrgSubscriptionCount(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Integer count = jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(WebhookSQLConstants.Query.COUNT_CHANNEL_ORG_SUBSCRIPTIONS,
                            (resultSet, rowNumber) ->
                                    resultSet.getInt(WebhookSQLConstants.Column.ORG_SUBSCRIPTION_COUNT),
                            statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid)));
            return count != null ? count : 0;
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_LIST_ERROR, e, channelUuid);
        }
    }


    @Override
    public void deleteChannelOrgSubscriptions(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(WebhookSQLConstants.Query.DELETE_CHANNEL_ORG_SUBSCRIPTIONS_BY_CHANNEL,
                        statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid));
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_DELETE_ERROR, e, channelUuid);
        }
    }

    @Override
    public List<String> getChannelUuids(String webhookId, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.executeQuery(WebhookSQLConstants.Query.LIST_CHANNEL_UUIDS_BY_WEBHOOK,
                            (resultSet, rowNumber) -> resultSet.getString(WebhookSQLConstants.Column.UUID),
                            statement -> {
                                statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                                statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                            }));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, e, "*", webhookId);
        }
    }

    @Override
    public int getOwningTenantId(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Integer tenantId = jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(WebhookSQLConstants.Query.GET_OWNING_TENANT_ID_BY_CHANNEL_UUID,
                            (resultSet, rowNumber) -> resultSet.getInt(WebhookSQLConstants.Column.TENANT_ID),
                            statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid)));
            return tenantId != null ? tenantId : -1;
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, e, channelUuid, "*");
        }
    }

    @Override
    public void deleteChannelOrgSubscriptionsByOrgId(String subscribedOrgId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(WebhookSQLConstants.Query.DELETE_CHANNEL_ORG_SUBSCRIPTIONS_BY_ORG,
                        statement -> statement.setString(
                                WebhookSQLConstants.Column.SUBSCRIBED_ORG_ID, subscribedOrgId));
                return null;
            });
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_ORG_SUBSCRIPTION_ORG_DELETE_ERROR, e, subscribedOrgId);
        }
    }

    @Override
    public String getChannelUri(String channelUuid) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(WebhookSQLConstants.Query.GET_CHANNEL_URI_BY_CHANNEL_UUID,
                            (resultSet, rowNumber) -> resultSet.getString(WebhookSQLConstants.Column.CHANNEL_URI),
                            statement -> statement.setString(WebhookSQLConstants.Column.CHANNEL_UUID, channelUuid)));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, e, channelUuid, "*");
        }
    }

    @Override
    public String getChannelUuid(String webhookId, String channelUri, int tenantId) throws WebhookMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(WebhookSQLConstants.Query.GET_CHANNEL_UUID_BY_WEBHOOK_AND_URI,
                            (resultSet, rowNumber) -> resultSet.getString(WebhookSQLConstants.Column.UUID),
                            statement -> {
                                statement.setString(WebhookSQLConstants.Column.UUID, webhookId);
                                statement.setInt(WebhookSQLConstants.Column.TENANT_ID, tenantId);
                                statement.setString(WebhookSQLConstants.Column.CHANNEL_URI, channelUri);
                            }));
        } catch (TransactionException e) {
            throw WebhookManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_CHANNEL_UUID_RESOLVE_ERROR, e, channelUri, webhookId);
        }
    }

    private ChannelOrgSubscription mapResultSetToChannelOrgSubscription(ResultSet resultSet) throws SQLException {

        return ChannelOrgSubscription.builder()
                .channelUuid(resultSet.getString(WebhookSQLConstants.Column.CHANNEL_UUID))
                .subscribedOrgTenantId(resultSet.getInt(WebhookSQLConstants.Column.SUBSCRIBED_ORG_TENANT_ID))
                .subscribedOrgId(resultSet.getString(WebhookSQLConstants.Column.SUBSCRIBED_ORG_ID))
                .topicUuid(resultSet.getString(WebhookSQLConstants.Column.TOPIC_UUID))
                .topic(resultSet.getString(WebhookSQLConstants.Column.TOPIC))
                .build();
    }
}
