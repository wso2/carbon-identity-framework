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

package org.wso2.carbon.identity.webhook.metadata.internal.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.BinaryObject;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataSQLConstants;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;
import org.wso2.carbon.identity.webhook.metadata.internal.model.WebhookMetadataProperty;
import org.wso2.carbon.identity.webhook.metadata.internal.util.WebhookMetadataExceptionHandler;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static org.wso2.carbon.identity.webhook.metadata.internal.constant.ErrorMessage.ERROR_CODE_WEBHOOK_METADATA_UPDATE_ERROR;

/**
 * Implementation of WebhookMetadataDAO.
 * This class handles the database operations related to webhook metadata properties.
 */
public class WebhookMetadataDAOImpl implements WebhookMetadataDAO {

    @Override
    public Map<String, WebhookMetadataProperty> getWebhookMetadataProperties(int tenantId)
            throws WebhookMetadataException {

        return getWebhookPropertiesFromDB(tenantId);
    }

    @Override
    public void updateWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                                int tenantId) throws WebhookMetadataException {

        try {
            batchProcessWebhookProperties(webhookMetadataProperties, tenantId,
                    WebhookMetadataSQLConstants.Query.UPDATE_WEBHOOK_METADATA_PROPERTY);
        } catch (TransactionException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(ERROR_CODE_WEBHOOK_METADATA_UPDATE_ERROR);
        }
    }

    @Override
    public void addWebhookMetadataProperties(Map<String, WebhookMetadataProperty> webhookMetadataProperties,
                                             int tenantId) throws WebhookMetadataException {

        try {
            batchProcessWebhookProperties(webhookMetadataProperties, tenantId,
                    WebhookMetadataSQLConstants.Query.INSERT_WEBHOOK_METADATA_PROPERTY);
        } catch (TransactionException e) {
            throw WebhookMetadataExceptionHandler.handleServerException(ERROR_CODE_WEBHOOK_METADATA_UPDATE_ERROR);
        }
    }

    private void batchProcessWebhookProperties(Map<String, WebhookMetadataProperty> properties, int tenantId,
                                               String query)
            throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template ->
                template.executeBatchInsert(query, statement -> {
                    for (Map.Entry<String, WebhookMetadataProperty> entry : properties.entrySet()) {
                        setStatementProperties(statement, entry.getKey(), entry.getValue(), tenantId);
                        statement.addBatch();
                    }
                }, null));
    }

    private void setStatementProperties(NamedPreparedStatement statement,
                                        String propertyName, WebhookMetadataProperty property, int tenantId)
            throws java.sql.SQLException {

        boolean isPrimitive = property.isPrimitive();
        statement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_NAME, propertyName);
        statement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_TYPE,
                isPrimitive ? WebhookMetadataProperty.Type.PRIMITIVE.name() :
                        WebhookMetadataProperty.Type.OBJECT.name());
        if (isPrimitive) {
            statement.setString(WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE, property.getValue().toString());
            statement.setBinaryStream(WebhookMetadataSQLConstants.Column.OBJECT_VALUE, null, 0);
        } else {
            statement.setNull(WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE, Types.VARCHAR);
            BinaryObject binaryObject = (BinaryObject) property.getValue();
            statement.setBinaryStream(WebhookMetadataSQLConstants.Column.OBJECT_VALUE,
                    binaryObject.getInputStream(), binaryObject.getLength());
        }
        statement.setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId);
    }

    private Map<String, WebhookMetadataProperty> getWebhookPropertiesFromDB(int tenantId)
            throws WebhookMetadataException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, WebhookMetadataProperty> webhookMetadataProperties = new HashMap<>();
        try {
            jdbcTemplate.withTransaction(template ->
                    template.executeQuery(WebhookMetadataSQLConstants.Query.GET_WEBHOOK_METADATA_PROPERTIES_INFO_BY_ID,
                            (resultSet, rowNumber) -> {
                                String propertyType =
                                        resultSet.getString(WebhookMetadataSQLConstants.Column.PROPERTY_TYPE);
                                String propertyName =
                                        resultSet.getString(WebhookMetadataSQLConstants.Column.PROPERTY_NAME);
                                if (WebhookMetadataProperty.Type.PRIMITIVE.name().equals(propertyType)) {
                                    webhookMetadataProperties.put(propertyName,
                                            new WebhookMetadataProperty.Builder(resultSet.getString(
                                                    WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE)).build());
                                } else {
                                    webhookMetadataProperties.put(propertyName,
                                            new WebhookMetadataProperty.Builder(
                                                    BinaryObject.fromInputStream(resultSet.getBinaryStream(
                                                            WebhookMetadataSQLConstants.Column.OBJECT_VALUE))).build());
                                }
                                return null;
                            },
                            statement -> statement
                                    .setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId)));
            return webhookMetadataProperties;
        } catch (TransactionException e) {
            throw new WebhookMetadataServerException(
                    "Error while retrieving webhook metadata properties from the system.", e);
        }
    }
}
