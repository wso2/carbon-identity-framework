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

package org.wso2.carbon.identity.action.management.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.constant.ActionMgtSQLConstants;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class ActionManagementDAOImpl implements ActionManagementDAO {

    private static final Log LOG = LogFactory.getLog(ActionManagementDAOImpl.class);
    private final ActionSecretProcessor actionSecretProcessor;

    public ActionManagementDAOImpl() {

        this.actionSecretProcessor = new ActionSecretProcessor();
    }

    @Override
    public Action addAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                    ActionMgtSQLConstants.Query.ADD_ACTION_TO_ACTION_TYPE)) {

                statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME, action.getName());
                statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION, action.getDescription());
                statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, String.valueOf(Action.Status.ACTIVE));
                statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                statement.executeUpdate();

                // Encrypt secrets.
                List<AuthProperty> encryptedAuthProperties = actionSecretProcessor
                        .encryptAssociatedSecrets(action.getEndpoint().getAuthentication(), actionId);
                // Add Endpoint configuration properties.
                addEndpointProperties(dbConnection, actionId, getEndpointProperties(action.getEndpoint().getUri(),
                        action.getEndpoint().getAuthentication().getType().name(), encryptedAuthProperties), tenantId);
                IdentityDatabaseUtil.commitTransaction(dbConnection);

                return getActionByActionId(actionId, tenantId);
            } catch (SQLException | ActionMgtException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Error while creating the Action of Action Type: %s in Tenant Domain: %s." +
                            " Rolling back created action information and deleting added secrets.", actionType,
                            IdentityTenantUtil.getTenantDomain(tenantId)));
                }
                actionSecretProcessor.deleteAssociatedSecrets(action.getEndpoint().getAuthentication(), actionId);
                IdentityDatabaseUtil.rollbackTransaction(dbConnection);
                throw ActionManagementUtil.handleServerException(
                        ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION, e);
            }
        } catch (SecretManagementException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        List<Action> actions = new ArrayList<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                     ActionMgtSQLConstants.Query.GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    String actionId = rs.getString(ActionMgtSQLConstants.Column.ACTION_UUID);

                    actions.add(new Action.ActionResponseBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(rs.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(rs.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .endpoint(getActionEndpointConfigById(dbConnection, actionId, tenantId)).build());
                }
            }
            return actions;
        } catch (SQLException | ActionMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE, e);
        }
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.UPDATE_ACTION_BASIC_INFO)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME, action.getName());
            statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION, action.getDescription());
            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Update Endpoint Properties.
            updateActionEndpointProperties(dbConnection, actionId, getEndpointProperties(action.getEndpoint().getUri(),
                    null, null), tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (SQLException | ActionMgtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action of Action Type: %s and Action ID: %s in" +
                                " Tenant Domain: %s. Rolling back updated action information.", actionType, actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public void deleteAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.DELETE_ACTION)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Delete action endpoint authentication related secrets.
            actionSecretProcessor.deleteAssociatedSecrets(action.getEndpoint().getAuthentication(), actionId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException | SecretManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the Action of Action Type: %s and Action ID: %s in" +
                                " Tenant Domain: %s. Rolling back deleted action information.", actionType, actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_DELETING_ACTION, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public Action activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        return changeActionStatus(actionType, actionId, String.valueOf(Action.Status.ACTIVE), tenantId);

    }

    @Override
    public Action deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        return changeActionStatus(actionType, actionId, String.valueOf(Action.Status.INACTIVE), tenantId);
    }

    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        Map<String, Integer> actionTypesCountMap = new HashMap<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                     ActionMgtSQLConstants.Query.GET_ACTIONS_COUNT_PER_ACTION_TYPE)) {

            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {

                    actionTypesCountMap.put(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE),
                            resultSet.getInt(ActionMgtSQLConstants.Column.ACTION_COUNT));
                }
            }
            return actionTypesCountMap;
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTIONS_COUNT_PER_TYPE, e);
        }
    }

    @Override
    public Action getActionByActionId(String actionId, Integer tenantId) throws ActionMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {
            Action action = getActionBasicInfoById(dbConnection, actionId, tenantId);
            if (action != null) {
                action.setEndpoint(getActionEndpointConfigById(dbConnection, actionId, tenantId));
            }

            return action;
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BY_ID, e);
        }
    }

    @Override
    public Action updateActionEndpointAuthProperties(String actionId, AuthType authentication, int tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            Map<String, String> nonSecretEndpointProperties = authentication.getProperties().stream()
                    .filter(property -> !property.getIsConfidential())
                    .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));
            // Update non-secret endpoint properties.
            updateActionEndpointProperties(dbConnection, actionId, nonSecretEndpointProperties, tenantId);
            // Encrypt and update non-secret endpoint properties.
            actionSecretProcessor.encryptAssociatedSecrets(authentication, actionId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (ActionMgtException | SecretManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action Endpoint Authentication Properties of " +
                                "Auth type: %s and Action ID: %s in Tenant Domain: %s. Rolling back updated action" +
                                " endpoint authentication properties.", authentication.getType(), actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public Action updateActionEndpoint(String actionType, String actionId, EndpointConfig endpoint,
                                       AuthType currentAuthentication, int tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.DELETE_ACTION_ENDPOINT_PROPERTIES)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Add new Endpoint configuration properties.
            addEndpointProperties(dbConnection, actionId, getEndpointProperties(endpoint.getUri(),
                    endpoint.getAuthentication().getType().name(),
                    endpoint.getAuthentication().getPropertiesWithSecretReferences(actionId)), tenantId);
            // Encrypt and add new endpoint properties secrets.
            actionSecretProcessor.encryptAssociatedSecrets(endpoint.getAuthentication(), actionId);

            // Delete old secrets.
            actionSecretProcessor.deleteAssociatedSecrets(currentAuthentication, actionId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (SQLException | ActionMgtException | SecretManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action Endpoint Authentication from Auth type: %s" +
                                " to Auth type: %s of Action ID: %s in Tenant Domain: %s. Rolling back updated" +
                                " action endpoint authentication.", currentAuthentication.getType(),
                        endpoint.getAuthentication().getType(), actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    /**
     * Add Action Endpoint properties to the Database.
     *
     * @param dbConnection       DB Connection.
     * @param actionId           UUID of the created Action.
     * @param endpointProperties Endpoint properties of the Action.
     * @param tenantId           Tenant ID.
     * @throws ActionMgtServerException If an error occurs while adding endpoint properties to the database.
     */
    private void addEndpointProperties(Connection dbConnection, String actionId,
                                       Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.ADD_ACTION_ENDPOINT_PROPERTIES)) {

            for (Map.Entry<String, String> property : endpointProperties.entrySet()) {
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME, property.getKey());
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE, property.getValue());
                statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Get Action Basic Info by Action ID.
     *
     * @param dbConnection DB Connection.
     * @param actionId     UUID of the created Action.
     * @param tenantId     Tenant ID.
     * @return Action Basic Info.
     * @throws ActionMgtException If an error occurs while retrieving action basic info from the database.
     */
    private Action getActionBasicInfoById(Connection dbConnection, String actionId, Integer tenantId)
            throws ActionMgtException {

        Action action = null;
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.GET_ACTION_BASIC_INFO_BY_ID)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    action = new Action.ActionResponseBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes.valueOf(rs.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(rs.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(rs.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(rs.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .build();
                }
            }
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BASIC_INFO, e);
        }
        return action;
    }

    /**
     * Get Action Endpoint properties by ID.
     *
     * @param dbConnection DB Connection.
     * @param actionUUID   UUID of the created Action.
     * @param tenantId     Tenant ID.
     * @return Endpoint Configuration.
     * @throws ActionMgtServerException If an error occurs while retrieving endpoint properties from the database.
     */
    private EndpointConfig getActionEndpointConfigById(Connection dbConnection, String actionUUID, Integer tenantId)
            throws ActionMgtException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.GET_ACTION_ENDPOINT_INFO_BY_ID)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionUUID);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);

            try (ResultSet rs = statement.executeQuery()) {

                String endpointUri = null;
                AuthType.AuthenticationType authnType = null;
                Map<String, String> authnPropertiesMap = new HashMap<>();
                List<AuthProperty> authnProperties = new ArrayList<>();

                while (rs.next()) {
                    String propName = rs.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME);
                    String propValue = rs.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE);

                    if (propName.equals(ActionMgtConstants.URI_ATTRIBUTE)) {
                        endpointUri = propValue;
                    } else if (propName.equals(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE)) {
                        authnType = AuthType.AuthenticationType.valueOf(propValue);
                    } else {
                        // Authentication properties.
                        authnPropertiesMap.put(propName, propValue);
                    }
                }

                if (authnType != null) {
                    for (AuthType.AuthenticationType.AuthenticationProperty property : authnType.getProperties()) {
                        if (authnPropertiesMap.containsKey(property.getName())) {
                            authnProperties.add(new AuthProperty.AuthPropertyBuilder()
                                    .name(property.getName())
                                    .value(authnPropertiesMap.get(property.getName()))
                                    .isConfidential(property.getIsConfidential())
                                    .build());
                        }
                    }
                }

                return new EndpointConfig.EndpointConfigBuilder()
                        .uri(endpointUri)
                        .authentication(new AuthType.AuthTypeBuilder()
                                .type(authnType)
                                .properties(authnProperties).build()).build();
            }
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Get Action Endpoint properties Map.
     *
     * @param endpointUri    Endpoint URI of the Action.
     * @param authType       Authentication Type of the Action.
     * @param authProperties Authentication Properties of the Endpoint.
     * @return Endpoint Properties Map.
     */
    private Map<String, String> getEndpointProperties(String endpointUri, String authType,
                                                      List<AuthProperty> authProperties) {

        Map<String, String> endpointProperties = new HashMap<>();
        if (endpointUri != null) {
            endpointProperties.put(ActionMgtConstants.URI_ATTRIBUTE, endpointUri);
        }
        if (authType != null) {
            endpointProperties.put(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE, authType);
        }
        if (authProperties != null) {
            for (AuthProperty property : authProperties) {
                endpointProperties.put(property.getName(), property.getValue());
            }
        }

        return endpointProperties;
    }

    /**
     * Update Action Endpoint properties.
     *
     * @param dbConnection       DB Connection.
     * @param actionId           UUID of the created Action.
     * @param endpointProperties Endpoint Properties to be updated.
     * @param tenantId           Tenant ID.
     */
    private void updateActionEndpointProperties(Connection dbConnection, String actionId,
                                                Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.UPDATE_ACTION_ENDPOINT_PROPERTIES)) {

            for (Map.Entry<String, String> property : endpointProperties.entrySet()) {
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE, property.getValue());
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME, property.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Update Action Status.
     *
     * @param actionType Action Type.
     * @param actionId   UUID of the Action.
     * @param status     Action status to be updated.
     * @param tenantId   Tenant ID.
     * @throws ActionMgtException If an error occurs while updating the Action status.
     */
    private Action changeActionStatus(String actionType, String actionId, String status, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.CHANGE_ACTION_STATUS)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, status);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionBasicInfoById(dbConnection, actionId, tenantId);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action Status to %s of Action type: %s in " +
                                "Tenant Domain: %s. Rolling back updated action status.", status, actionType,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_STATUS, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }
}
