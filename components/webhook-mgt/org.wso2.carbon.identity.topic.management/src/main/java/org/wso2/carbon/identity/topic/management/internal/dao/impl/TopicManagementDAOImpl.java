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

package org.wso2.carbon.identity.topic.management.internal.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.topic.management.api.exception.TopicManagementException;
import org.wso2.carbon.identity.topic.management.internal.constant.ErrorMessage;
import org.wso2.carbon.identity.topic.management.internal.constant.TopicSQLConstants.Column;
import org.wso2.carbon.identity.topic.management.internal.constant.TopicSQLConstants.Query;
import org.wso2.carbon.identity.topic.management.internal.dao.TopicManagementDAO;
import org.wso2.carbon.identity.topic.management.internal.util.TopicManagementExceptionHandler;

/**
 * Implementation of the TopicManagementDAO interface.
 * This class provides methods to manage topics in the database.
 */
public class TopicManagementDAOImpl implements TopicManagementDAO {

    private static final Log LOG = LogFactory.getLog(TopicManagementDAOImpl.class);

    @Override
    public void addTopic(String topic, String channelUri, String eventProfileVersion, int tenantId)
            throws TopicManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.ADD_TOPIC, statement -> {
                    statement.setString(Column.TOPIC, topic);
                    statement.setString(Column.CHANNEL_URI, channelUri);
                    statement.setString(Column.EVENT_PROFILE_VERSION, eventProfileVersion);
                    statement.setInt(Column.TENANT_ID, tenantId);
                });
                LOG.debug("Successfully added topic: " + topic + " for tenant ID: " + tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_ADD_ERROR, e, topic);
        }
    }

    @Override
    public void deleteTopic(String topic, int tenantId) throws TopicManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(Query.DELETE_TOPIC, statement -> {
                    statement.setString(Column.TOPIC, topic);
                    statement.setInt(Column.TENANT_ID, tenantId);
                });
                LOG.debug("Successfully deleted topic: " + topic + " for tenant ID: " + tenantId);
                return null;
            });
        } catch (TransactionException e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_DELETE_ERROR, e, topic);
        }
    }

    @Override
    public boolean isTopicExists(String topic, int tenantId) throws TopicManagementException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                    template.fetchSingleRecord(Query.CHECK_TOPIC_EXISTS,
                            (resultSet, rowNumber) -> true,
                            statement -> {
                                statement.setString(Column.TOPIC, topic);
                                statement.setInt(Column.TENANT_ID, tenantId);
                            })
            ) != null;
        } catch (TransactionException e) {
            throw TopicManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_CODE_TOPIC_EXISTS_CHECK_ERROR, e, topic);
        }
    }
}
