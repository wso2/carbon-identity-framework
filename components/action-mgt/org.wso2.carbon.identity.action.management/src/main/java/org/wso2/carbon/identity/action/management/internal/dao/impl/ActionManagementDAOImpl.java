/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.internal.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionProperty;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.BinaryObject;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtSQLConstants;
import org.wso2.carbon.identity.action.management.internal.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementDAOUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class ActionManagementDAOImpl implements ActionManagementDAO {

    private static final String V1 = "1.0.0";

    private static final ActionManagementDAOUtil actionMgtDAOUtil = new ActionManagementDAOUtil();

    @Override
    public void addAction(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        // Add action basic information.
        addBasicInfo(actionDTO, tenantId);
        // Add action endpoint.
        addEndpoint(actionDTO, tenantId);
        // Add action rule reference.
        addRuleReference(actionDTO, tenantId);
        // Add action properties.
        addProperties(actionDTO, tenantId);
    }

    @Override
    public List<ActionDTO> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        List<ActionDTO> actionDTOS = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                     ActionMgtSQLConstants.Query.GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String actionId = rs.getString(ActionMgtSQLConstants.Column.ACTION_UUID);
                    Map<String, ActionProperty> properties = getActionPropertiesFromDB(actionId, tenantId);
                    ActionDTO actionDTO = new ActionDTOBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(rs.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(rs.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .createdAt(rs.getTimestamp(ActionMgtSQLConstants.Column.CREATED_AT))
                            .updatedAt(rs.getTimestamp(ActionMgtSQLConstants.Column.UPDATED_AT))
                            .actionVersion(rs.getString(ActionMgtSQLConstants.Column.ACTION_VERSION))
                            .endpoint(populateEndpoint(properties))
                            .rule(populateRule(properties, tenantId))
                            .properties(properties.entrySet().stream()
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                            .build();

                    actionDTOS.add(actionDTO);
                }
            }

            return actionDTOS;
        } catch (SQLException e) {
            throw new ActionMgtServerException("Error while retrieving Actions information by Action Type from " +
                    "the system.", e);
        }
    }

    @Override
    public ActionDTO getActionByActionId(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        ActionDTOBuilder actionBuilder = getBasicInfo(actionType, actionId, tenantId);
        if (actionBuilder == null) {
            return null;
        }

        Map<String, ActionProperty> actionProperties = getActionPropertiesFromDB(actionId, tenantId);
        actionBuilder.endpoint(populateEndpoint(actionProperties));
        actionBuilder.rule(populateRule(actionProperties, tenantId));
        actionBuilder.properties(actionProperties.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return actionBuilder.build();
    }

    @Override
    public void updateAction(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        // Update action basic information.
        updateBasicInfo(updatingActionDTO, existingActionDTO, tenantId);
        // Update Action Endpoint.
        updateEndpoint(updatingActionDTO, existingActionDTO, tenantId);
        // Update Rule Reference.
        updateRuleReference(updatingActionDTO, existingActionDTO, tenantId);
        // Update Action Properties.
        updateProperties(updatingActionDTO, existingActionDTO, tenantId);
    }

    @Override
    public void deleteAction(ActionDTO deletingActionDTO, Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ActionMgtSQLConstants.Query.DELETE_ACTION,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, deletingActionDTO.getId());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE,
                                deletingActionDTO.getType().getActionType());
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });

                return null;
            });
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while deleting Action information in the system.", e);
        }
    }

    @Override
    public ActionDTO activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        return changeActionStatus(actionType, actionId, String.valueOf(Action.Status.ACTIVE), tenantId);
    }

    @Override
    public ActionDTO deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        return changeActionStatus(actionType, actionId, String.valueOf(Action.Status.INACTIVE), tenantId);
    }

    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        Map<String, Integer> actionTypesCountMap = new HashMap<>();
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template ->
                template.executeQuery(ActionMgtSQLConstants.Query.GET_ACTIONS_COUNT_PER_ACTION_TYPE,
                    (resultSet, rowNumber) -> {
                        actionTypesCountMap.put(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE),
                                resultSet.getInt(ActionMgtSQLConstants.Column.ACTION_COUNT));
                        return null;
                    }, statement -> statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId)));
            return actionTypesCountMap;
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while retrieving Actions count per Action Type from the system.",
                    e);
        }
    }

    /**
     * Add Basic Information of an {@link ActionDTO} to the Database.
     *
     * @param actionDTO ActionDTO object with basic information.
     * @param tenantId  Tenant ID.
     * @throws ActionMgtException If an error occurs while adding action basic information in the database.
     */
    private void addBasicInfo(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template ->
                template.executeInsert(ActionMgtSQLConstants.Query.ADD_ACTION_TO_ACTION_TYPE,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionDTO.getId());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE,
                                actionDTO.getType().getActionType());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME, actionDTO.getName());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION,
                                actionDTO.getDescription());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, actionDTO.getStatus().name());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_VERSION, actionDTO.getActionVersion());
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                        statement.setTimeStamp(ActionMgtSQLConstants.Column.CREATED_AT, currentTimestamp, null);
                        statement.setTimeStamp(ActionMgtSQLConstants.Column.UPDATED_AT, currentTimestamp, null);
                        statement.setString(ActionMgtSQLConstants.Column.SCHEMA_VERSION, V1);
                    }, actionDTO, false));
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while adding Action Basic information in the system.", e);
        }
    }

    /**
     * Update the basic information of an {@link ActionDTO} by given Action ID.
     *
     * @param updatingActionDTO Information to be updated.
     * @param existingActionDTO Existing Action information.
     * @param tenantId          Tenant ID.
     * @throws ActionMgtException If an error occurs while updating the Action basic information in the database.
     */
    private void updateBasicInfo(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ActionMgtSQLConstants.Query.UPDATE_ACTION_BASIC_INFO,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME,
                                updatingActionDTO.getName() == null ? existingActionDTO.getName()
                                        : updatingActionDTO.getName());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION,
                                updatingActionDTO.getDescription() == null ? existingActionDTO.getDescription()
                                        : updatingActionDTO.getDescription());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_VERSION,
                                updatingActionDTO.getActionVersion() == null ? existingActionDTO.getActionVersion()
                                        : updatingActionDTO.getActionVersion());
                        statement.setTimeStamp(ActionMgtSQLConstants.Column.UPDATED_AT,
                                new Timestamp(new Date().getTime()), null);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, updatingActionDTO.getId());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE,
                                updatingActionDTO.getType().getActionType());
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });

                return null;
            });
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while updating Action Basic information in the system.", e);
        }
    }

    /**
     * Get Action Basic Info by Action ID.
     *
     * @param actionId UUID of the created Action.
     * @param tenantId Tenant ID.
     * @return ActionDTO Builder with action basic information.
     * @throws ActionMgtException If an error occurs while retrieving action basic info from the database.
     */
    private ActionDTOBuilder getBasicInfo(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template ->
                template.fetchSingleRecord(ActionMgtSQLConstants.Query.GET_ACTION_BASIC_INFO_BY_ID,
                    (resultSet, rowNumber) -> new ActionDTOBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes.valueOf(
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .createdAt(resultSet.getTimestamp(ActionMgtSQLConstants.Column.CREATED_AT))
                            .updatedAt(resultSet.getTimestamp(ActionMgtSQLConstants.Column.UPDATED_AT))
                            .actionVersion(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_VERSION)),
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                }));
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while retrieving Action Basic information from the system.", e);
        }
    }

    /**
     * Add Action Endpoint Configurations.
     *
     * @param actionDTO ActionDTO object with endpoint information.
     * @param tenantId  Tenant ID.
     * @throws ActionMgtException If an error occurs while adding action endpoint.
     */
    private void addEndpoint(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        EndpointConfig endpoint = actionDTO.getEndpoint();
        Map<String, ActionProperty> endpointProperties = new HashMap<>();
        try {
            endpointProperties.put(ActionMgtConstants.URI_PROPERTY, new ActionProperty.BuilderForDAO(
                    endpoint.getUri()).build());
            endpointProperties.put(ActionMgtConstants.AUTHN_TYPE_PROPERTY, new ActionProperty.BuilderForDAO(
                    endpoint.getAuthentication().getType().name()).build());

            endpoint.getAuthentication().getProperties().forEach(
                    authProperty -> endpointProperties.put(authProperty.getName(),
                            new ActionProperty.BuilderForDAO(authProperty.getValue()).build()));
            // Allowed headers and parameters are optional properties.
            if (endpoint.getAllowedHeaders() != null) {
                endpointProperties.put(ActionMgtConstants.ALLOWED_HEADERS_PROPERTY,
                        actionMgtDAOUtil.buildActionPropertyFromList(endpoint.getAllowedHeaders()));
            }
            if (endpoint.getAllowedParameters() != null) {
                endpointProperties.put(ActionMgtConstants.ALLOWED_PARAMETERS_PROPERTY,
                        actionMgtDAOUtil.buildActionPropertyFromList(endpoint.getAllowedParameters()));
            }

            addActionPropertiesToDB(actionDTO.getId(), endpointProperties, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while adding Action Endpoint configurations in the system.", e);
        }
    }

    /**
     * Update Action Endpoint Configurations.
     * If the endpoint configuration is updated, the existing configuration is deleted
     * and re added with updating values.
     *
     * @param updatingActionDTO Updating ActionDTO object with endpoint information.
     * @param existingActionDTO Existing ActionDTO object with endpoint information.
     * @param tenantId          Tenant ID.
     * @throws ActionMgtException If an error occurs while updating action endpoint.
     */
    private void updateEndpoint(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        if (updatingActionDTO.getEndpoint() == null) {
            return;
        }

        try {
            deleteExistingEndpoint(existingActionDTO.getId(), existingActionDTO.getEndpoint(), tenantId);

            EndpointConfig updatingEndpoint = actionMgtDAOUtil.buildUpdatingEndpointConfig(
                    updatingActionDTO.getEndpoint(),
                    existingActionDTO.getEndpoint());
            Map<String, ActionProperty> updatingEndpointProperties = actionMgtDAOUtil.getUpdatingEndpointProperties(
                    updatingEndpoint);
            try {
                addActionPropertiesToDB(existingActionDTO.getId(), updatingEndpointProperties, tenantId);
            } catch (TransactionException e) {
                throw new ActionMgtServerException(
                        "Error while adding Action Endpoint configurations in the system.", e);
            }
        } catch (ActionMgtException e) {
            throw new ActionMgtServerException("Error while updating Action Endpoint information in the system.", e);
        }
    }

    /**
     * Deletes the existing endpoint configuration for a given action.
     *
     * @param actionId         UUID of the Action whose endpoint is being deleted.
     * @param deletingEndpoint The endpoint configuration to be deleted.
     * @param tenantId         Tenant ID.
     * @throws ActionMgtServerException If an error occurs while deleting the endpoint configuration.
     */
    private void deleteExistingEndpoint(String actionId, EndpointConfig deletingEndpoint, Integer tenantId)
            throws ActionMgtServerException {

        List<String> deletingProperties = new ArrayList<>();
        deletingProperties.add(ActionMgtConstants.URI_PROPERTY);

        if (deletingEndpoint.getAllowedHeaders() != null) {
            deletingProperties.add(ActionMgtConstants.ALLOWED_HEADERS_PROPERTY);
        }
        if (deletingEndpoint.getAllowedParameters() != null) {
            deletingProperties.add(ActionMgtConstants.ALLOWED_PARAMETERS_PROPERTY);
        }

        List<String> deletingAuthProperties = deletingEndpoint.getAuthentication().getProperties().stream()
                .map(AuthProperty::getName)
                .collect(Collectors.toList());

        deletingProperties.addAll(deletingAuthProperties);
        deletingProperties.add(ActionMgtConstants.AUTHN_TYPE_PROPERTY);

        try {
            deleteActionPropertiesInDB(actionId, deletingProperties, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while deleting Action Endpoint information in the system.", e);
        }
    }

    private EndpointConfig populateEndpoint(Map<String, ActionProperty> propertiesFromDB)
            throws ActionMgtException {

        Authentication authentication;
        Authentication.Type authnType = Authentication.Type.valueOfName(propertiesFromDB.remove(
                ActionMgtConstants.AUTHN_TYPE_PROPERTY).getValue().toString());
        switch (authnType) {
            case BASIC:
                authentication = new Authentication.BasicAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.USERNAME.getName()).getValue().toString(),
                        propertiesFromDB.remove(Authentication.Property.PASSWORD.getName()).getValue().toString())
                        .build();
                break;
            case BEARER:
                authentication = new Authentication.BearerAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.ACCESS_TOKEN.getName()).getValue().toString())
                        .build();
                break;
            case API_KEY:
                authentication = new Authentication.APIKeyAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.HEADER.getName()).getValue().toString(),
                        propertiesFromDB.remove(Authentication.Property.VALUE.getName()).getValue().toString()).build();
                break;
            case NONE:
                authentication = new Authentication.NoneAuthBuilder().build();
                break;
            default:
                throw new ActionMgtServerException("Authentication type is not defined for the Action Endpoint.");
        }

        EndpointConfig.EndpointConfigBuilder endpointBuilder = new EndpointConfig.EndpointConfigBuilder();

        List<String> allowedHeaders = actionMgtDAOUtil
                .readDBListProperty(propertiesFromDB, ActionMgtConstants.ALLOWED_HEADERS_PROPERTY);
        List<String> allowedParameters = actionMgtDAOUtil
                .readDBListProperty(propertiesFromDB, ActionMgtConstants.ALLOWED_PARAMETERS_PROPERTY);
        if (!allowedHeaders.isEmpty()) {
            endpointBuilder = endpointBuilder.allowedHeaders(allowedHeaders);
        }
        if (!allowedParameters.isEmpty()) {
            endpointBuilder = endpointBuilder.allowedParameters(allowedParameters);
        }

        return endpointBuilder
                .uri(propertiesFromDB.remove(ActionMgtConstants.URI_PROPERTY).getValue().toString())
                .authentication(authentication)
                .build();
    }

    private void addRuleReference(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        if (actionDTO.getActionRule() == null || actionDTO.getActionRule().getRule() == null) {
            return;
        }

        Map<String, ActionProperty> propertiesMap = Collections.singletonMap(ActionMgtConstants.RULE_PROPERTY,
                new ActionProperty.BuilderForDAO(actionDTO.getActionRule().getId()).build());
        try {
            addActionPropertiesToDB(actionDTO.getId(), propertiesMap, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while adding the reference for the Rule in Action.", e);
        }
    }

    private void updateRuleReference(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtServerException {

        try {
            if (existingActionDTO.getActionRule() == null && updatingActionDTO.getActionRule() != null &&
                    updatingActionDTO.getActionRule().getRule() != null) {
                // This means a new action rule is added when updating the action. Add the rule reference.
                addRuleReference(updatingActionDTO, tenantId);
            } else if (existingActionDTO.getActionRule() != null && updatingActionDTO.getActionRule() != null &&
                    updatingActionDTO.getActionRule().getRule() == null) {
                // This means the existing action rule is removed when updating the action. Remove the rule reference.
                deleteRuleReference(updatingActionDTO, tenantId);
            }
        } catch (ActionMgtException e) {
            throw new ActionMgtServerException("Error while updating the reference for the Rule in Action.", e);
        }
    }

    private void deleteRuleReference(ActionDTO actionDTO, Integer tenantId) throws ActionMgtServerException {

        try {
            deleteActionPropertiesInDB(actionDTO.getId(),
                    Collections.singletonList(ActionMgtConstants.RULE_PROPERTY), tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while removing the reference for the Rule in Action.", e);
        }
    }

    private ActionRule populateRule(Map<String, ActionProperty> propertiesFromDB, Integer tenantId) {

        if (!propertiesFromDB.containsKey(ActionMgtConstants.RULE_PROPERTY)) {
            return null;
        }

        return ActionRule.create(propertiesFromDB.remove(ActionMgtConstants.RULE_PROPERTY).getValue().toString(),
                IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Add Action properties.
     *
     * @param actionDTO ActionDTO object with properties.
     * @param tenantId  Tenant ID.
     * @throws ActionMgtException If an error occurs while adding action properties.
     */
    private void addProperties(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        Map<String, ActionProperty> actionProperties = actionDTO.getProperties();
        if (actionProperties == null || actionProperties.isEmpty()) {
            return;
        }
        try {
            addActionPropertiesToDB(actionDTO.getId(), actionProperties, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while adding Action Properties in the system.", e);
        }
    }

    /**
     * Update Action properties.
     *
     * @param updatingActionDTO Updating ActionDTO object with properties.
     * @param existingActionDTO Existing ActionDTO object with properties.
     * @param tenantId          Tenant ID.
     * @throws ActionMgtException If an error occurs while updating action properties.
     */
    private void updateProperties(ActionDTO updatingActionDTO, ActionDTO existingActionDTO,
                                  Integer tenantId) throws ActionMgtException {

        Map<String, ActionProperty> updatingProperties = updatingActionDTO.getProperties();
        if (updatingProperties == null) {
            return;
        }
        try {
            // Delete existing properties.
            deleteActionPropertiesInDB(updatingActionDTO.getId(),
                    new ArrayList<>(existingActionDTO.getProperties().keySet()), tenantId);
            // Add updated properties.
            addActionPropertiesToDB(updatingActionDTO.getId(), updatingProperties, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while updating Action Properties in the system.", e);
        }
    }

    /**
     * Add Action properties to the Database.
     *
     * @param actionId         UUID of the created Action.
     * @param actionProperties Properties of the Action.
     * @param tenantId         Tenant ID.
     * @throws TransactionException If an error occurs while persisting action properties to the database.
     */
    private void addActionPropertiesToDB(String actionId, Map<String, ActionProperty> actionProperties,
                                         Integer tenantId) throws TransactionException {


        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeBatchInsert(ActionMgtSQLConstants.Query.ADD_ACTION_PROPERTIES,
                    statement -> {
                        for (Map.Entry<String, ActionProperty> property : actionProperties.entrySet()) {
                            boolean isPrimitive = property.getValue().isPrimitive();
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME,
                                    property.getKey());
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_TYPE,
                                    property.getValue().getType().toString());
                            if (isPrimitive) {
                                statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE,
                                        property.getValue().getValue().toString());
                                statement.setBinaryStream(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_OBJECT_VALUE,
                                        null, 0);
                            } else {
                                BinaryObject binaryObject = (BinaryObject) property.getValue().getValue();
                                statement.setNull(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE,
                                        java.sql.Types.VARCHAR);
                                statement.setBinaryStream(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_OBJECT_VALUE,
                                        binaryObject.getInputStream(), binaryObject.getLength());
                            }
                            statement.addBatch();
                        }
                    }, null);
            return null;
        });
    }

    /**
     * Get Action properties by ID.
     *
     * @param actionId UUID of the created Action.
     * @param tenantId Tenant ID.
     * @return A map of action properties, including any additional data based on action type.
     * @throws ActionMgtException If an error occurs while retrieving action properties from the database.
     */
    private Map<String, ActionProperty> getActionPropertiesFromDB(String actionId, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, ActionProperty> actionEndpointProperties = new HashMap<>();
        try {
            jdbcTemplate.withTransaction(template ->
                template.executeQuery(ActionMgtSQLConstants.Query.GET_ACTION_PROPERTIES_INFO_BY_ID,
                    (resultSet, rowNumber) -> {
                        if (ActionProperty.Type.PRIMITIVE.name().equals(resultSet.getString(ActionMgtSQLConstants
                                .Column.ACTION_PROPERTIES_PROPERTY_TYPE))) {
                            actionEndpointProperties.put(
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME),
                                    new ActionProperty.BuilderForDAO(resultSet.getString(
                                            ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE)).build());
                        } else {
                            actionEndpointProperties.put(
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME),
                                    new ActionProperty.BuilderForDAO(BinaryObject.fromInputStream(resultSet
                                            .getBinaryStream(ActionMgtSQLConstants.Column
                                                    .ACTION_PROPERTIES_OBJECT_VALUE))).build());
                        }
                        return null;
                    },
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                }));
            return actionEndpointProperties;
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while retrieving Action Properties from the system.", e);
        }
    }

    /**
     * Delete the given properties of an {@link ActionDTO} by given Action ID.
     *
     * @param actionId           UUID of the created Action.
     * @param deletingProperties Action properties to be deleted.
     * @param tenantId           Tenant ID.
     * @throws TransactionException If an error occurs while deleting the Action properties in the database.
     */
    private void deleteActionPropertiesInDB(String actionId, List<String> deletingProperties, Integer tenantId)
            throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template ->
            template.executeBatchInsert(ActionMgtSQLConstants.Query.DELETE_ACTION_PROPERTY,
                statement -> {
                    for (String property : deletingProperties) {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME,
                                property);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                        statement.addBatch();
                    }
                }, null));
    }

    /**
     * Update Action Status.
     *
     * @param actionType Action Type.
     * @param actionId   UUID of the Action.
     * @param status     Action status to be updated.
     * @param tenantId   Tenant ID.
     * @return Updated ActionDTO with basic information.
     * @throws ActionMgtException If an error occurs while updating the Action status.
     */
    private ActionDTO changeActionStatus(String actionType, String actionId, String status, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ActionMgtSQLConstants.Query.CHANGE_ACTION_STATUS,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, status);
                        statement.setTimeStamp(ActionMgtSQLConstants.Column.UPDATED_AT,
                                new Timestamp(new Date().getTime()), null);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });

                return null;
            });

            return getBasicInfo(actionType, actionId, tenantId).build();
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while updating Action Status to " + status, e);
        }
    }
}
