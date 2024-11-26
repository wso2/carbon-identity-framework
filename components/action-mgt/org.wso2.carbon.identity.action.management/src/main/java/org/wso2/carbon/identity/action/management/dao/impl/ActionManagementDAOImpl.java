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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.action.management.ActionSecretProcessor;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.constant.ActionMgtSQLConstants;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtRuntimeException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

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

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeInsert(ActionMgtSQLConstants.Query.ADD_ACTION_TO_ACTION_TYPE,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME, action.getName());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION, action.getDescription());
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS,
                                String.valueOf(Action.Status.ACTIVE));
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    }, action, false);

                // Encrypt secrets.
                List<AuthProperty> encryptedAuthProperties = actionSecretProcessor
                        .encryptAssociatedSecrets(action.getEndpoint().getAuthentication(), actionId);

                // Add Endpoint configuration properties.
                addEndpointProperties(actionId, getEndpointProperties(action.getEndpoint().getUri(),
                        action.getEndpoint().getAuthentication().getType().name(), encryptedAuthProperties), tenantId);

                return null;
            });

            return getActionByActionId(actionType, actionId, tenantId);
        } catch (TransactionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while creating the Action of Action Type: %s in Tenant Domain: %s." +
                                " Rolling back created action information and deleting added secrets.", actionType,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION, e);
        }
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.executeQuery(ActionMgtSQLConstants.Query.GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE,
                (resultSet, rowNumber) -> new Action.ActionResponseBuilder()
                        .id(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_UUID))
                        .type(Action.ActionTypes
                                .valueOf(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                        .name(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                        .description(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                        .status(Action.Status
                                .valueOf(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                        .endpoint(getActionEndpointConfigById(
                                resultSet.getString(ActionMgtSQLConstants.Column.ACTION_UUID), tenantId))
                        .build(),
                statement -> {
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                    statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });
        } catch (ActionMgtRuntimeException | DataAccessException e) {
            /**
             * Handling {@link ActionMgtRuntimeException}, which is intentionally thrown to represent underlying
             * exceptions from the {@link #getActionEndpointConfigById(String, Integer)} method.
             */
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE, e);
        }
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action updatingAction, Action existingAction,
                               Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                // Update Basic Info.
                updateBasicInfo(actionType, actionId, updatingAction, existingAction, tenantId);
                // Update Endpoint URI and Authentication.
                updateEndpointUriAndAuthentication(actionId, updatingAction, existingAction, tenantId);

                return null;
            });

            return getActionByActionId(actionType, actionId, tenantId);
        } catch (TransactionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action of Action Type: %s and Action ID: %s in" +
                                " Tenant Domain: %s. Rolling back updated action information.", actionType, actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION, e);
        }
    }

    @Override
    public void deleteAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ActionMgtSQLConstants.Query.DELETE_ACTION,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });
                // Delete action endpoint authentication related secrets.
                actionSecretProcessor.deleteAssociatedSecrets(action.getEndpoint().getAuthentication(), actionId);

                return null;
            });
        } catch (TransactionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while deleting the Action of Action Type: %s and Action ID: %s in" +
                                " Tenant Domain: %s. Rolling back deleted action information.", actionType, actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_DELETING_ACTION, e);
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
        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.executeQuery(ActionMgtSQLConstants.Query.GET_ACTIONS_COUNT_PER_ACTION_TYPE,
                (resultSet, rowNumber) -> {
                    actionTypesCountMap.put(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE),
                            resultSet.getInt(ActionMgtSQLConstants.Column.ACTION_COUNT));
                    return null;
                }, statement -> statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId));

            return actionTypesCountMap;
        } catch (DataAccessException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTIONS_COUNT_PER_TYPE, e);
        }
    }

    @Override
    public Action getActionByActionId(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        try {
            Action action = getActionBasicInfoById(actionType, actionId, tenantId);
            if (action != null) {
                action = new Action.ActionResponseBuilder()
                        .id(actionId)
                        .type(Action.ActionTypes.valueOf(actionType))
                        .name(action.getName())
                        .description(action.getDescription())
                        .status(action.getStatus())
                        .endpoint(getActionEndpointConfigById(actionId, tenantId))
                        .build();
            }

            return action;
        } catch (ActionMgtException | ActionMgtRuntimeException e) {
            /**
             * Handling {@link ActionMgtRuntimeException}, which is intentionally thrown to represent underlying
             * exceptions from the {@link #getActionEndpointConfigById(String, Integer)} method.
             */
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BY_ID, e);
        }
    }

    @Override
    public Action updateActionEndpointAuthProperties(String actionType, String actionId, Authentication authentication,
                                                     int tenantId) throws ActionMgtException {

        updateActionEndpointAuthProperties(actionId, authentication, tenantId);
        return getActionByActionId(actionType, actionId, tenantId);
    }

    @Override
    public Action updateActionEndpoint(String actionType, String actionId, EndpointConfig endpoint,
                                       Authentication currentAuthentication, int tenantId)
            throws ActionMgtException {

        updateActionEndpoint(actionId, endpoint, currentAuthentication, tenantId);
        return getActionByActionId(actionType, actionId, tenantId);
    }

    /**
     * Update the endpoint authentication properties of an {@link Action} by given Action ID.
     *
     * @param actionId       Action ID.
     * @param authentication Authentication information to be updated.
     * @param tenantId       Tenant Id.
     * @throws ActionMgtServerException If an error occurs while updating the Action endpoint authentication properties.
     */
    private void updateActionEndpointAuthProperties(String actionId, Authentication authentication, int tenantId)
            throws ActionMgtServerException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Map<String, String> nonSecretEndpointProperties = authentication.getProperties().stream()
                    .filter(property -> !property.getIsConfidential())
                    .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));

            jdbcTemplate.withTransaction(template -> {
                // Update non-secret endpoint properties.
                updateActionEndpointProperties(actionId, nonSecretEndpointProperties, tenantId);
                // Encrypt and update secret endpoint properties.
                actionSecretProcessor.encryptAssociatedSecrets(authentication, actionId);
                return null;
            });
        } catch (TransactionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action Endpoint Authentication Properties of " +
                                "Auth type: %s and Action ID: %s in Tenant Domain: %s. Rolling back updated action" +
                                " endpoint authentication properties.", authentication.getType(), actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Update the endpoint information of an {@link Action} by given Action ID.
     *
     * @param actionId              Action ID.
     * @param endpoint              Endpoint information to be updated.
     * @param currentAuthentication Current Action endpoint authentication information.
     * @param tenantId              Tenant Id.
     * @throws ActionMgtServerException If an error occurs while updating the Action endpoint.
     */
    private void updateActionEndpoint(String actionId, EndpointConfig endpoint, Authentication currentAuthentication,
                                      int tenantId) throws ActionMgtServerException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeUpdate(ActionMgtSQLConstants.Query.DELETE_ACTION_ENDPOINT_PROPERTIES,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });

                // Add new Endpoint configuration properties.
                Map<String, String> propertiesMap = getEndpointProperties(endpoint.getUri(),
                        endpoint.getAuthentication().getType().name(),
                        endpoint.getAuthentication().getPropertiesWithSecretReferences(actionId));
                addEndpointProperties(actionId, propertiesMap, tenantId);
                // Encrypt and add new endpoint properties secrets.
                actionSecretProcessor.encryptAssociatedSecrets(endpoint.getAuthentication(), actionId);

                // Delete old secrets.
                actionSecretProcessor.deleteAssociatedSecrets(currentAuthentication, actionId);
                return null;
            });
        } catch (TransactionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Error while updating the Action Endpoint Authentication from Auth type: %s" +
                                " to Auth type: %s of Action ID: %s in Tenant Domain: %s. Rolling back updated" +
                                " action endpoint authentication.", currentAuthentication.getType(),
                        endpoint.getAuthentication().getType(), actionId,
                        IdentityTenantUtil.getTenantDomain(tenantId)));
            }
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Add Action Endpoint properties to the Database.
     *
     * @param actionId           UUID of the created Action.
     * @param endpointProperties Endpoint properties of the Action.
     * @param tenantId           Tenant ID.
     * @throws ActionMgtServerException If an error occurs while adding endpoint properties to the database.
     */
    private void addEndpointProperties(String actionId, Map<String, String> endpointProperties, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(ActionMgtSQLConstants.Query.ADD_ACTION_ENDPOINT_PROPERTIES,
                    statement -> {
                        for (Map.Entry<String, String> property : endpointProperties.entrySet()) {
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME,
                                    property.getKey());
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE,
                                    property.getValue());
                            statement.addBatch();
                        }
                    }, null);
                return null;
            });
        } catch (TransactionException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ENDPOINT_PROPERTIES, e);
        }
    }

    /**
     * Get Action Basic Info by Action ID.
     *
     * @param actionId     UUID of the created Action.
     * @param tenantId     Tenant ID.
     * @return Action Basic Info.
     * @throws ActionMgtException If an error occurs while retrieving action basic info from the database.
     */
    private Action getActionBasicInfoById(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.fetchSingleRecord(ActionMgtSQLConstants.Query.GET_ACTION_BASIC_INFO_BY_ID,
                (resultSet, rowNumber) -> new Action.ActionResponseBuilder()
                        .id(actionId)
                        .type(Action.ActionTypes.valueOf(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                        .name(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                        .description(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                        .status(Action.Status.valueOf(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                        .build(),
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });
        } catch (DataAccessException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BASIC_INFO, e);
        }
    }

    /**
     * Get Action Endpoint properties by ID.
     *
     * @param actionUUID   UUID of the created Action.
     * @param tenantId     Tenant ID.
     * @return Endpoint Configuration.
     * @throws ActionMgtRuntimeException If an error occurs while retrieving endpoint properties from the database.
     */
    private EndpointConfig getActionEndpointConfigById(String actionUUID, Integer tenantId)
            throws ActionMgtRuntimeException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            Map<String, String> actionEndpointProperties = new HashMap<>();
            jdbcTemplate.executeQuery(ActionMgtSQLConstants.Query.GET_ACTION_ENDPOINT_INFO_BY_ID,
                (resultSet, rowNumber) -> {
                    actionEndpointProperties.put(
                            resultSet.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME),
                            resultSet.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE));
                    return null;
                },
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionUUID);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });

            Authentication authentication = null;
            if (actionEndpointProperties.containsKey(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE)) {
                authentication = new Authentication.AuthenticationBuilder()
                        .type(Authentication.Type.valueOf(
                                actionEndpointProperties.get(ActionMgtConstants.AUTHN_TYPE_ATTRIBUTE)))
                        .properties(actionEndpointProperties)
                        .build();
            } else {
                throw ActionManagementUtil.handleServerException(
                        ActionMgtConstants.ErrorMessages.ERROR_NO_AUTHENTICATION_TYPE, null);
            }

            return new EndpointConfig.EndpointConfigBuilder()
                    .uri(actionEndpointProperties.get(ActionMgtConstants.URI_ATTRIBUTE))
                    .authentication(authentication).build();
        } catch (ActionMgtServerException | DataAccessException e) {
            /**
             * Throwing a runtime exception because {@link ActionMgtServerException} and {@link DataAccessException}
             * is not handled in {@link org.wso2.carbon.database.utils.jdbc.RowMapper} of
             * {@link NamedJdbcTemplate#executeQuery(String, org.wso2.carbon.database.utils.jdbc.RowMapper,
             * org.wso2.carbon.database.utils.jdbc.NamedQueryFilter)}
             */
            throw ActionManagementUtil.handleRuntimeException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_ENDPOINT_PROPERTIES.getMessage(), e);
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
     * @param actionId           UUID of the created Action.
     * @param endpointProperties Endpoint Properties to be updated.
     * @param tenantId           Tenant ID.
     */
    private void updateActionEndpointProperties(String actionId, Map<String, String> endpointProperties,
                                                Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                template.executeBatchInsert(ActionMgtSQLConstants.Query.UPDATE_ACTION_ENDPOINT_PROPERTIES,
                    statement -> {
                        for (Map.Entry<String, String> property : endpointProperties.entrySet()) {
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE,
                                    property.getValue());
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME,
                                    property.getKey());
                            statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                            statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                            statement.addBatch();
                        }
                    }, null);
                return null;
            });
        } catch (TransactionException e) {
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

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.executeUpdate(ActionMgtSQLConstants.Query.CHANGE_ACTION_STATUS,
                statement -> {
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_STATUS, status);
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                    statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });

            return getActionBasicInfoById(actionType, actionId, tenantId);
        } catch (DataAccessException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_STATUS, e);
        }
    }

    /**
     * Update the basic information of an {@link Action} by given Action ID.
     *
     * @param actionType     Action Type.
     * @param actionId       Action ID.
     * @param updatingAction Information to be updated.
     * @param existingAction Existing Action information.
     * @param tenantId       Tenant ID.
     * @throws ActionMgtServerException If an error occurs while updating the Action basic information.
     */
    private void updateBasicInfo(String actionType, String actionId, Action updatingAction, Action existingAction,
                                 Integer tenantId) throws ActionMgtServerException {

        if (updatingAction.getName() == null && updatingAction.getDescription() == null) {
            return;
        }

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.executeUpdate(ActionMgtSQLConstants.Query.UPDATE_ACTION_BASIC_INFO,
                statement -> {
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_NAME,
                            updatingAction.getName() == null ? existingAction.getName() : updatingAction.getName());
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION,
                            updatingAction.getDescription() == null ? existingAction.getDescription()
                                    : updatingAction.getDescription());
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_UUID, actionId);
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                    statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });
        } catch (DataAccessException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_BASIC_INFO, e);
        }
    }

    /**
     * Update the endpoint URI and authentication properties of an {@link Action} by given Action ID.
     *
     * @param actionId       Action ID.
     * @param updatingAction Information to be updated.
     * @param existingAction Existing Action information.
     * @param tenantId       Tenant ID.
     * @throws ActionMgtException If an error occurs while updating the Action endpoint.
     */
    private void updateEndpointUriAndAuthentication(String actionId, Action updatingAction, Action existingAction,
                                                    Integer tenantId) throws ActionMgtException {

        EndpointConfig updatingEndpoint = updatingAction.getEndpoint();
        if (updatingEndpoint == null) {
            // No update needed if there's no endpoint configuration in the updating action.
            return;
        }

        Authentication updatingAuthentication = updatingEndpoint.getAuthentication();
        if (updatingAuthentication == null) {
            // When updating action, updates the URI only.
            updateActionEndpointProperties(actionId, getEndpointProperties(updatingEndpoint.getUri(), null,
                    null), tenantId);
            return;
        }

        Authentication existingAuthentication = existingAction.getEndpoint().getAuthentication();
        if (updatingAuthentication.getType().equals(existingAuthentication.getType())) {
            // When updating action, updates the URI and the authentication properties only.
            if (updatingEndpoint.getUri() != null) {
                updateActionEndpointProperties(actionId, getEndpointProperties(updatingEndpoint.getUri(), null,
                        null), tenantId);
            }
            updateActionEndpointAuthProperties(actionId, updatingAuthentication, tenantId);
            return;
        }

        // When updating action, updates the entire endpoint.
        updatingEndpoint = StringUtils.isNotEmpty(updatingEndpoint.getUri()) ? updatingEndpoint :
                new EndpointConfig.EndpointConfigBuilder()
                        .uri(existingAction.getEndpoint().getUri())
                        .authentication(updatingAuthentication)
                        .build();
        updateActionEndpoint(actionId, updatingEndpoint, existingAuthentication, tenantId);
    }
}
