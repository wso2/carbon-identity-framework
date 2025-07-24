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
import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataException;
import org.wso2.carbon.identity.webhook.metadata.api.exception.WebhookMetadataServerException;
import org.wso2.carbon.identity.webhook.metadata.api.model.BinaryObject;
import org.wso2.carbon.identity.webhook.metadata.api.model.PolicyEnumWrapper;
import org.wso2.carbon.identity.webhook.metadata.api.model.WebhookMetadataProperties;
import org.wso2.carbon.identity.webhook.metadata.api.model.WebhookMetadataProperty;
import org.wso2.carbon.identity.webhook.metadata.internal.constant.WebhookMetadataSQLConstants;
import org.wso2.carbon.identity.webhook.metadata.internal.dao.WebhookMetadataDAO;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the WebhookMetadataDAO interface for managing webhook metadata properties.
 */
public class WebhookMetadataDAOImpl implements WebhookMetadataDAO {

    private static final String ORGANIZATION_POLICY_PROPERTY_NAME = "organizationPolicy";

    @Override
    public WebhookMetadataProperties getWebhookMetadataProperties(int tenantId) throws WebhookMetadataException {

        Map<String, WebhookMetadataProperty> propertiesMap = getWebhookPropertiesFromDB(tenantId);

        WebhookMetadataProperty orgPolicyProperty = propertiesMap.get(ORGANIZATION_POLICY_PROPERTY_NAME);
        PolicyEnumWrapper organizationPolicy = null;
        if (orgPolicyProperty != null && orgPolicyProperty.isPrimitive()) {
            organizationPolicy =
                    new PolicyEnumWrapper(PolicyEnum.getPolicyByPolicyName(orgPolicyProperty.getValue().toString()));
        }

        return new WebhookMetadataProperties.Builder()
                .organizationPolicy(organizationPolicy)
                .build();
    }

    @Override
    public void updateWebhookMetadataProperties(WebhookMetadataProperties webhookMetadataProperties, int tenantId)
            throws WebhookMetadataException {

        PolicyEnumWrapper updatedOrganizationPolicy = webhookMetadataProperties.getOrganizationPolicy();
        if (updatedOrganizationPolicy == null) {
            return;
        }

        try {
            updateWebhookPropertiesInDB(
                    Collections.singletonMap(ORGANIZATION_POLICY_PROPERTY_NAME, new WebhookMetadataProperty.Builder(
                            updatedOrganizationPolicy.getPolicyCode()).build()), tenantId);
        } catch (TransactionException e) {
            throw new WebhookMetadataServerException("Error while updating webhook metadata properties in the system.",
                    e);
        }
    }

    /**
     * Updates the webhook properties in the database.
     *
     * @param updatingProperties Action properties to be updated.
     * @param tenantId           Tenant ID.
     * @throws TransactionException If an error occurs while updating the Action properties in the database.
     */
    private void updateWebhookPropertiesInDB(Map<String, WebhookMetadataProperty> updatingProperties,
                                             Integer tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());

        jdbcTemplate.withTransaction(template -> {
            for (Map.Entry<String, WebhookMetadataProperty> entry : updatingProperties.entrySet()) {
                String propertyName = entry.getKey();
                WebhookMetadataProperty property = entry.getValue();
                boolean isPrimitive = property.isPrimitive();
                Object value = property.getValue();

                boolean exists = template.fetchSingleRecord(WebhookMetadataSQLConstants.Query.CHECK_PROPERTY_EXISTS,
                        (resultSet, rowNumber) -> true,
                        preparedStatement -> {
                            preparedStatement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_NAME, propertyName);
                            preparedStatement.setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId);
                        }) != null;

                if (exists) {
                    template.executeUpdate(WebhookMetadataSQLConstants.Query.UPDATE_WEBHOOK_METADATA_PROPERTY,
                            preparedStatement -> {
                                setPropertyTypeAndValue(isPrimitive, value, preparedStatement);
                                preparedStatement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_NAME,
                                        propertyName);
                                preparedStatement.setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId);
                            });
                } else {
                    template.executeInsert(WebhookMetadataSQLConstants.Query.INSERT_WEBHOOK_METADATA_PROPERTY,
                            preparedStatement -> {
                                preparedStatement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_NAME,
                                        propertyName);
                                setPropertyTypeAndValue(isPrimitive, value, preparedStatement);
                                preparedStatement.setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId);
                            }, null, false);
                }
            }
            return null;
        });
    }

    private void setPropertyTypeAndValue(boolean isPrimitive, Object value, NamedPreparedStatement preparedStatement)
            throws SQLException {

        preparedStatement.setString(WebhookMetadataSQLConstants.Column.PROPERTY_TYPE,
                isPrimitive ? WebhookMetadataProperty.Type.PRIMITIVE.name() :
                        WebhookMetadataProperty.Type.OBJECT.name());
        if (isPrimitive) {
            preparedStatement.setString(WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE,
                    value.toString());
            preparedStatement.setBinaryStream(WebhookMetadataSQLConstants.Column.OBJECT_VALUE,
                    null, 0);
        } else {
            preparedStatement.setNull(WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE,
                    java.sql.Types.VARCHAR);
            BinaryObject binaryObject = (BinaryObject) value;
            preparedStatement.setBinaryStream(WebhookMetadataSQLConstants.Column.OBJECT_VALUE,
                    binaryObject.getInputStream(), binaryObject.getLength());
        }
    }

    private Map<String, WebhookMetadataProperty> getWebhookPropertiesFromDB(int tenantId)
            throws WebhookMetadataException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, WebhookMetadataProperty> webhookMetadataProperties = new HashMap<>();
        try {
            jdbcTemplate.withTransaction(template ->
                    template.executeQuery(WebhookMetadataSQLConstants.Query.GET_WEBHOOK_METADATA_PROPERTIES_INFO_BY_ID,
                            (resultSet, rowNumber) -> {
                                if (WebhookMetadataProperty.Type.PRIMITIVE.name()
                                        .equals(resultSet.getString(
                                                WebhookMetadataSQLConstants.Column.PROPERTY_TYPE))) {
                                    webhookMetadataProperties.put(
                                            resultSet.getString(
                                                    WebhookMetadataSQLConstants.Column.PROPERTY_NAME),
                                            new WebhookMetadataProperty.Builder(
                                                    resultSet.getString(
                                                            WebhookMetadataSQLConstants.Column.PRIMITIVE_VALUE)).build());
                                } else {
                                    webhookMetadataProperties.put(
                                            resultSet.getString(
                                                    WebhookMetadataSQLConstants.Column.PROPERTY_NAME),
                                            new WebhookMetadataProperty.Builder(BinaryObject.fromInputStream(resultSet
                                                    .getBinaryStream(WebhookMetadataSQLConstants.Column
                                                            .OBJECT_VALUE))).build());
                                }
                                return null;
                            },
                            statement -> statement.setInt(WebhookMetadataSQLConstants.Column.TENANT_ID, tenantId)));
            return webhookMetadataProperties;
        } catch (TransactionException e) {
            throw new WebhookMetadataServerException("Error while retrieving webhook metadata properties from the " +
                    "system.", e);
        }
    }
}
