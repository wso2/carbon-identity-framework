/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.internal.constant.ActionMgtSQLConstants;
import org.wso2.carbon.identity.action.management.internal.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class ActionManagementDAOImpl implements ActionManagementDAO {

    private static final String V1 = "1.0.0";
    private static final String URI_PROPERTY = "uri";
    private static final String AUTHN_TYPE_PROPERTY = "authnType";
    private static final String RULE_PROPERTY = "rule";

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
                    Map<String, String> properties = getActionPropertiesFromDB(actionId, tenantId);
                    ActionDTO actionDTO = new ActionDTOBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(rs.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(rs.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
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

        Map<String, String> actionProperties = getActionPropertiesFromDB(actionId, tenantId);
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
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
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

        if (updatingActionDTO.getName() == null && updatingActionDTO.getDescription() == null) {
            return;
        }

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
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_STATUS))),
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
        Map<String, String> endpointProperties = new HashMap<>();
        try {
            endpointProperties.put(URI_PROPERTY, endpoint.getUri());
            endpointProperties.put(AUTHN_TYPE_PROPERTY, endpoint.getAuthentication().getType().name());
            endpoint.getAuthentication().getProperties().forEach(
                    authProperty -> endpointProperties.put(authProperty.getName(), authProperty.getValue()));

            addActionPropertiesToDB(actionDTO.getId(), endpointProperties, tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while adding Action Endpoint configurations in the system.", e);
        }
    }

    /**
     * Update Action Endpoint Configurations.
     *
     * @param updatingActionDTO Updating ActionDTO object with endpoint information.
     * @param existingActionDTO Existing ActionDTO object with endpoint information.
     * @param tenantId          Tenant ID.
     * @throws ActionMgtException If an error occurs while updating action endpoint.
     */
    private void updateEndpoint(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        EndpointConfig updatingEndpoint = updatingActionDTO.getEndpoint();
        if (updatingEndpoint == null) {
            return;
        }

        try {
            if (updatingEndpoint.getUri() != null) {
                updateActionPropertiesInDB(updatingActionDTO.getId(),
                        Collections.singletonMap(URI_PROPERTY, updatingEndpoint.getUri()), tenantId);
            }

            updateEndpointAuthentication(updatingActionDTO.getId(), updatingEndpoint.getAuthentication(),
                    existingActionDTO.getEndpoint().getAuthentication(), tenantId);
        } catch (ActionMgtException | TransactionException e) {
            throw new ActionMgtServerException("Error while updating Action Endpoint information in the system.", e);
        }
    }

    private EndpointConfig populateEndpoint(Map<String, String> propertiesFromDB) throws ActionMgtException {

        Authentication authentication;
        Authentication.Type authnType =
                Authentication.Type.valueOfName(propertiesFromDB.remove(AUTHN_TYPE_PROPERTY));
        switch (authnType) {
            case BASIC:
                authentication = new Authentication.BasicAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.USERNAME.getName()),
                        propertiesFromDB.remove(Authentication.Property.PASSWORD.getName())).build();
                break;
            case BEARER:
                authentication = new Authentication.BearerAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.ACCESS_TOKEN.getName())).build();
                break;
            case API_KEY:
                authentication = new Authentication.APIKeyAuthBuilder(
                        propertiesFromDB.remove(Authentication.Property.HEADER.getName()),
                        propertiesFromDB.remove(Authentication.Property.VALUE.getName())).build();
                break;
            case NONE:
                authentication = new Authentication.NoneAuthBuilder().build();
                break;
            default:
                throw new ActionMgtServerException("Authentication type is not defined for the Action Endpoint.");
        }

        return new EndpointConfig.EndpointConfigBuilder()
                .uri(propertiesFromDB.remove(URI_PROPERTY))
                .authentication(authentication)
                .build();
    }

    /**
     * Update Action Endpoint Authentication.
     *
     * @param actionId               UUID of the created Action.
     * @param updatingAuthentication Authentication object with updated configurations.
     * @param existingAuthentication Existing Authentication object.
     * @param tenantId               Tenant ID.
     * @throws ActionMgtException If an error occurs while updating action endpoint authentication.
     */
    private void updateEndpointAuthentication(String actionId, Authentication updatingAuthentication,
                                              Authentication existingAuthentication, Integer tenantId)
            throws ActionMgtException {

        if (updatingAuthentication == null) {
            return;
        }

        try {
            if (updatingAuthentication.getType() == existingAuthentication.getType()) {
                updateAuthentication(actionId, updatingAuthentication, tenantId);
            } else {
                // Delete existing authentication configurations.
                deleteAuthentication(actionId, existingAuthentication, tenantId);
                // Add new authentication configurations.
                addAuthentication(actionId, updatingAuthentication, tenantId);
            }
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while updating Action Endpoint Authentication.", e);
        }
    }

    /**
     * Add Authentication Configurations of a new Authentication type.
     *
     * @param actionId               UUID of the created Action.
     * @param updatingAuthentication Authentication object with updated configurations.
     * @param tenantId               Tenant ID.
     * @throws TransactionException If an error occurs while adding action authentication.
     */
    private void addAuthentication(String actionId, Authentication updatingAuthentication, Integer tenantId)
            throws TransactionException {

        Map<String, String> authenticationProperties = updatingAuthentication.getProperties().stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
        authenticationProperties.put(AUTHN_TYPE_PROPERTY, updatingAuthentication.getType().name());

        addActionPropertiesToDB(actionId, authenticationProperties, tenantId);
    }

    /**
     * Delete Authentication Configurations of an existing Authentication type.
     *
     * @param actionId               UUID of the created Action.
     * @param existingAuthentication Existing Authentication object.
     * @param tenantId               Tenant ID.
     * @throws TransactionException If an error occurs while deleting action authentication.
     */
    private void deleteAuthentication(String actionId, Authentication existingAuthentication, Integer tenantId)
            throws TransactionException {

        List<String> deletingProperties = existingAuthentication.getProperties().stream()
                .map(AuthProperty::getName)
                .collect(Collectors.toList());
        deletingProperties.add(AUTHN_TYPE_PROPERTY);

        deleteActionPropertiesInDB(actionId, deletingProperties, tenantId);
    }

    /**
     * Update Authentication Configurations of an existing Authentication type.
     *
     * @param actionId               UUID of the created Action.
     * @param updatingAuthentication Authentication object with updated configurations.
     * @param tenantId               Tenant ID.
     * @throws TransactionException If an error occurs while updating action authentication.
     */
    private void updateAuthentication(String actionId, Authentication updatingAuthentication, Integer tenantId)
            throws TransactionException {

        Map<String, String> nonSecretAuthenticationProperties = updatingAuthentication.getProperties().stream()
                .filter(property -> !property.getIsConfidential())
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
        // Update non-secret endpoint properties.
        updateActionPropertiesInDB(actionId, nonSecretAuthenticationProperties, tenantId);
    }

    private void addRuleReference(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        if (actionDTO.getActionRule() == null || actionDTO.getActionRule().getRule() == null) {
            return;
        }

        Map<String, String> propertiesMap =
                Collections.singletonMap(RULE_PROPERTY, actionDTO.getActionRule().getId());
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
                    Collections.singletonList(RULE_PROPERTY), tenantId);
        } catch (TransactionException e) {
            throw new ActionMgtServerException("Error while removing the reference for the Rule in Action.", e);
        }
    }

    private ActionRule populateRule(Map<String, String> propertiesFromDB, Integer tenantId) {

        if (!propertiesFromDB.containsKey(RULE_PROPERTY)) {
            return null;
        }

        return ActionRule.create(propertiesFromDB.remove(RULE_PROPERTY),
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

        Map<String, Object> propertiesMap = actionDTO.getProperties();
        if (propertiesMap == null || propertiesMap.isEmpty()) {
            return;
        }

        Map<String, String> actionProperties = propertiesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
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

        Map<String, Object> propertiesMap = updatingActionDTO.getProperties();
        if (propertiesMap == null) {
            return;
        }

        Map<String, String> updatingProperties = propertiesMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
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
    private void addActionPropertiesToDB(String actionId, Map<String, String> actionProperties, Integer tenantId)
            throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            boolean primitiveValueColumnExists  = isPrimitiveValueColumnExists();
            String query = primitiveValueColumnExists ? ActionMgtSQLConstants.Query.ADD_ACTION_PROPERTIES
                    : ActionMgtSQLConstants.Query.ADD_ACTION_PROPERTIES_WITH_PROPERTY_VALUE_COLUMN;
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(query,
                        statement -> {
                            for (Map.Entry<String, String> property : actionProperties.entrySet()) {
                                statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                                statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                                statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME,
                                        property.getKey());
                                statement.setString(primitiveValueColumnExists ?
                                                ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PRIMITIVE_VALUE :
                                                ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE,
                                        property.getValue());
                                statement.addBatch();
                            }
                        }, null);
                return null;
            });
        } catch (SQLException e) {
            throw new TransactionException("Error while adding Action Properties in the system.", e);
        }
    }

    /**
     * Get Action properties by ID.
     *
     * @param actionId UUID of the created Action.
     * @param tenantId Tenant ID.
     * @return A map of action properties, including any additional data based on action type.
     * @throws ActionMgtException If an error occurs while retrieving action properties from the database.
     */
    private Map<String, String> getActionPropertiesFromDB(String actionId, Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, String> actionEndpointProperties = new HashMap<>();

        try {
            boolean primitiveValueColumnExists = isPrimitiveValueColumnExists();
            String query = primitiveValueColumnExists ? ActionMgtSQLConstants.Query.GET_ACTION_PROPERTIES_INFO_BY_ID
                    : ActionMgtSQLConstants.Query.GET_ACTION_PROPERTIES_INFO_BY_ID_WITH_PROPERTY_VALUE_COLUMN;
            jdbcTemplate.withTransaction(template ->
                template.executeQuery(query,
                    (resultSet, rowNumber) -> {
                        actionEndpointProperties.put(
                                resultSet.getString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME),
                                resultSet.getString(primitiveValueColumnExists ?
                                        ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PRIMITIVE_VALUE :
                                        ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE));
                        return null;
                    },
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                }));

            return actionEndpointProperties;
        } catch (TransactionException | SQLException e) {
            throw new ActionMgtServerException("Error while retrieving Action Properties from the system.", e);
        }
    }

    /**
     * Update the given property of an {@link ActionDTO} by given Action ID.
     *
     * @param actionId           UUID of the created Action.
     * @param updatingProperties Action properties to be updated.
     * @param tenantId           Tenant ID.
     * @throws TransactionException If an error occurs while updating the Action properties in the database.
     */
    private void updateActionPropertiesInDB(String actionId, Map<String, String> updatingProperties,
                                            Integer tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            boolean primitiveValueColumnExists = isPrimitiveValueColumnExists();
            String query = primitiveValueColumnExists ? ActionMgtSQLConstants.Query.UPDATE_ACTION_PROPERTY
                    : ActionMgtSQLConstants.Query.UPDATE_ACTION_PROPERTY_WITH_PROPERTY_VALUE_COLUMN;
            jdbcTemplate.withTransaction(template ->
                    template.executeBatchInsert(query,
                            statement -> {
                                for (Map.Entry<String, String> property : updatingProperties.entrySet()) {
                                    statement.setString(primitiveValueColumnExists ?
                                                    ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PRIMITIVE_VALUE :
                                                    ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_VALUE,
                                            property.getValue());
                                    statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_PROPERTY_NAME,
                                            property.getKey());
                                    statement.setString(ActionMgtSQLConstants.Column.ACTION_PROPERTIES_UUID, actionId);
                                    statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                                    statement.addBatch();
                                }
                            }, null));
        } catch (SQLException  e) {
            throw new TransactionException("Error while updating Action Properties in the system.", e);
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

    /**
     * Check whether the PRITMITIVE_VALUE column exists in IDN_ACTION_PROPERTIES table.
     * TODO: Remove this temporary method once the column name is changed.
     *
     * @return True if the column exists, False otherwise.
     * @throws SQLException If an error occurs while checking the table existence.
     */
    private boolean isPrimitiveValueColumnExists() throws SQLException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false);
             ResultSet resultSet = connection.getMetaData().getColumns(null, null,
                     "IDN_ACTION_PROPERTIES", "PRIMITIVE_VALUE")) {
            return resultSet.next();
        }
    }

}
