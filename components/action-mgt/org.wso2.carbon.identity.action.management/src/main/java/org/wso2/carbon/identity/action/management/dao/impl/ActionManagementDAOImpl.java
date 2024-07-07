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
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.constant.ActionMgtSQLConstants;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class ActionManagementDAOImpl implements ActionManagementDAO {

    private static final Log LOG = LogFactory.getLog(ActionManagementDAOImpl.class);

    @Override
    public Action addAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.ADD_ACTION_TO_ACTION_TYPE)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME, action.getName());
            statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION, action.getDescription());
            statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, String.valueOf(Action.Status.ACTIVE));
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Add Endpoint configuration properties.
            addEndpointProperties(dbConnection, actionId, getEndpointProperties(action), tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (SQLException | ActionMgtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while creating the action in action type: " + actionType + " in tenantDomain: " +
                        IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back created action information.");
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
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
            updateActionEndpointProperties(dbConnection, actionId, getEndpointProperties(action), tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (SQLException | ActionMgtException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while updating the action in action type: " + actionType + " in tenantDomain: " +
                        IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back updated action information.");
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public void deleteAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.DELETE_ACTION)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
            statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while deleting the action in action type: " + actionType + " in tenantDomain: " +
                        IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back deleted action information.");
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

                EndpointConfig endpointConfig = new EndpointConfig();
                AuthType authentication = new AuthType();
                Map<String, Object> authnProperties = new HashMap<>();

                while (rs.next()) {
                    String propName = rs.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME);
                    String propValue = rs.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE);

                    if (propName.equals(ActionMgtConstants.URI_ATTRIBUTE)) {
                        endpointConfig.setUri(propValue);
                    } else if (propName.equals(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE)) {
                        authentication.setType(AuthType.AuthenticationType.valueOf(propValue));
                    } else {
                        // Authentication properties.
                        authnProperties.put(propName, propValue);
                    }
                }
                authentication.setProperties(authnProperties);
                endpointConfig.setAuthentication(authentication);

                return endpointConfig;
            }
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Get Action Endpoint properties Map.
     *
     * @param action Action Object.
     * @return Endpoint Properties Map.
     */
    private Map<String, String> getEndpointProperties(Action action) {

        Map<String, String> endpointProperties = new HashMap<>();
        endpointProperties.put(ActionMgtConstants.URI_ATTRIBUTE, action.getEndpoint().getUri());
        endpointProperties.put(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE,
                String.valueOf(action.getEndpoint().getAuthentication().getType()));
        for (Map.Entry<String, Object> property : action.getEndpoint().getAuthentication().getProperties().entrySet()) {
            endpointProperties.put(property.getKey(), String.valueOf(property.getValue()));
        }

        return endpointProperties;
    }

    /**
     * Update Action Endpoint properties.
     *
     * @param dbConnection       DB Connection.
     * @param actionId           UUID of the created Action.
     * @param endpointProperties Endpoint Properties.
     * @param tenantId           Tenant ID.
     */
    private void updateActionEndpointProperties(Connection dbConnection, String actionId,
                                                Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(dbConnection,
                ActionMgtSQLConstants.Query.DELETE_ACTION_ENDPOINT_PROPERTIES)) {

            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
            statement.executeUpdate();

            // Add Endpoint configuration properties.
            addEndpointProperties(dbConnection, actionId, endpointProperties, tenantId);
        } catch (SQLException | ActionMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Update Action Endpoint properties.
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
                LOG.debug("Error while updating the action status in action type: " + actionType +
                        " in tenantDomain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                        ". Rolling back updated action status.");
            }
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_STATUS, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }
}
