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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class ActionManagementDAOImpl implements ActionManagementDAO {

    private static final Log LOG = LogFactory.getLog(ActionManagementDAOImpl.class);

    @Override
    public Action addAction(String actionType, Action action, Integer tenantId) throws ActionMgtException {

        String generatedActionId = UUID.randomUUID().toString();
        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(true);
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
            ActionMgtSQLConstants.Query.ADD_ACTION_TO_ACTION_TYPE)) {

            prepStmt.setString(1, generatedActionId);
            prepStmt.setString(2, actionType);
            prepStmt.setString(3, action.getName());
            prepStmt.setString(4, action.getDescription());
            prepStmt.setString(5, String.valueOf(Action.Status.ACTIVE));
            prepStmt.setInt(6, tenantId);
            prepStmt.executeUpdate();
            prepStmt.clearParameters();

            // Add Endpoint configuration properties.
            addEndpointProperties(dbConnection, generatedActionId, getEndpointProperties(action),
                    tenantId);

            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(generatedActionId, tenantId);
        } catch (SQLException | ActionMgtException e) {
            LOG.error("Error while creating the action in action type: " + actionType + " in tenantDomain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back created action information.");
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
             PreparedStatement prepStmt = dbConnection.prepareStatement(
                     ActionMgtSQLConstants.Query.GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE)) {

            prepStmt.setString(1, actionType);
            prepStmt.setInt(2, tenantId);

            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String actionUUID = rs.getString(ActionMgtSQLConstants.Column.ACTION_UUID);

                    actions.add(new Action.ActionResponseBuilder()
                            .id(actionUUID)
                            .type(Action.ActionTypes.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(rs.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(rs.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    rs.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .endpoint(getActionEndpointConfigById(dbConnection, actionUUID, tenantId)).build());
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
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ActionMgtSQLConstants.Query.UPDATE_ACTION_BASIC_INFO)) {

            prepStmt.setString(1, action.getName());
            prepStmt.setString(2, action.getDescription());
            prepStmt.setString(3, actionId);
            prepStmt.setString(4, actionType);
            prepStmt.setInt(5, tenantId);
            prepStmt.executeUpdate();

            // Update Endpoint Properties.
            updateActionEndpointProperties(dbConnection, actionId, getEndpointProperties(action), tenantId);
            IdentityDatabaseUtil.commitTransaction(dbConnection);

            return getActionByActionId(actionId, tenantId);
        } catch (SQLException | ActionMgtException e) {
            LOG.error("Error while updating the action in action type: " + actionType + " in tenantDomain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back updated action information.");
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
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(ActionMgtSQLConstants.Query.DELETE_ACTION)) {

            prepStmt.setString(1, actionId);
            prepStmt.setString(2, actionType);
            prepStmt.setInt(3, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            LOG.error("Error while deleting the action in action type: " + actionType + " in tenantDomain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back deleted action information.");
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_DELETING_ACTION, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }

    @Override
    public Action activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        changeActionStatus(actionType, actionId, String.valueOf(Action.Status.ACTIVE), tenantId);
        return getActionBasicInfoById(actionId, tenantId);
    }



    @Override
    public Action deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        changeActionStatus(actionType, actionId, String.valueOf(Action.Status.INACTIVE), tenantId);
        return getActionBasicInfoById(actionId, tenantId);
    }

    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        Map<String, Integer> actionTypesCountMap = new HashMap<>();
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
             PreparedStatement prepStmt = dbConnection.prepareStatement(
                     ActionMgtSQLConstants.Query.GET_ACTIONS_COUNT_PER_ACTION_TYPE)) {

            prepStmt.setInt(1, tenantId);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
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
    public Action getActionByActionId(String actionUUID, Integer tenantId) throws ActionMgtException {

        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false)) {

            Action action = getActionBasicInfoById(actionUUID, tenantId);
            if (action != null) {
                action.setEndpoint(getActionEndpointConfigById(dbConnection, actionUUID, tenantId));
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
     * @param actionUUID         UUID of the created Action.
     * @param endpointProperties Endpoint properties of the Action.
     * @param tenantId           Tenant ID.
     * @throws ActionMgtServerException If an error occurs while adding endpoint properties to the database.
     */
    private void addEndpointProperties(Connection dbConnection, String actionUUID,
                                       Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ActionMgtSQLConstants.Query.ADD_ACTION_ENDPOINT_PROPERTIES)) {

            for (Map.Entry<String, String> property : endpointProperties.entrySet()) {
                prepStmt.setString(1, actionUUID);
                prepStmt.setString(2, property.getKey());
                prepStmt.setString(3, property.getValue());
                prepStmt.setInt(4, tenantId);
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Get Action Basic Info by Action ID.
     *
     * @param actionId   UUID of the created Action.
     * @param tenantId     Tenant ID.
     * @return Action Basic Info.
     * @throws ActionMgtException If an error occurs while retrieving action basic info from the database.
     */
    private Action getActionBasicInfoById(String actionId, Integer tenantId)
            throws ActionMgtException {

        Action action = null;
        try (Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
                PreparedStatement prepStmt = dbConnection.prepareStatement(
                ActionMgtSQLConstants.Query.GET_ACTION_BASIC_INFO_BY_ID)) {

            prepStmt.setString(1, actionId);
            prepStmt.setInt(2, tenantId);

            try (ResultSet rs = prepStmt.executeQuery()) {
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

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ActionMgtSQLConstants.Query.GET_ACTION_ENDPOINT_INFO_BY_ID)) {

            prepStmt.setString(1, actionUUID);
            prepStmt.setInt(2, tenantId);

            try (ResultSet rs = prepStmt.executeQuery()) {

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
     * @param actionUUID         UUID of the created Action.
     * @param endpointProperties Endpoint Properties.
     * @param tenantId           Tenant ID.
     */
    private void updateActionEndpointProperties(Connection dbConnection, String actionUUID,
                                                Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                    ActionMgtSQLConstants.Query.DELETE_ACTION_ENDPOINT_PROPERTIES)) {
            prepStmt.setString(1, actionUUID);
            prepStmt.setInt(2, tenantId);
            prepStmt.executeUpdate();

            // Add Endpoint configuration properties.
            addEndpointProperties(dbConnection, actionUUID, endpointProperties, tenantId);
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
    private void changeActionStatus(String actionType, String actionId, String status, Integer tenantId)
            throws ActionMgtException {

        Connection dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(
                ActionMgtSQLConstants.Query.CHANGE_ACTION_STATUS)) {

            prepStmt.setString(1, status);
            prepStmt.setString(2, actionId);
            prepStmt.setString(3, actionType);
            prepStmt.setInt(4, tenantId);
            prepStmt.executeUpdate();
            IdentityDatabaseUtil.commitTransaction(dbConnection);
        } catch (SQLException e) {
            LOG.error("Error while updating the action status in action type: " + actionType + " in tenantDomain: " +
                    IdentityTenantUtil.getTenantDomain(tenantId) + ". Rolling back updated action status.");
            IdentityDatabaseUtil.rollbackTransaction(dbConnection);
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_STATUS, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(dbConnection);
        }
    }
}
