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
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtRuntimeException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.action.management.constant.ActionMgtConstants.AUTHN_TYPE_PROPERTY;
import static org.wso2.carbon.identity.action.management.constant.ActionMgtConstants.CERTIFICATE_ID_PROPERTY;
import static org.wso2.carbon.identity.action.management.constant.ActionMgtConstants.PASSWORD_SHARING_FORMAT_PROPERTY;
import static org.wso2.carbon.identity.action.management.constant.ActionMgtConstants.URI_PROPERTY;

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

                // Add action properties.
                addActionProperties(actionType, actionId, action, tenantId);

                return null;
            });

            return getActionByActionId(actionType, actionId, tenantId);
        } catch (TransactionException e) {
            if (e.getCause() instanceof ActionMgtClientException) {
                throw (ActionMgtClientException) e.getCause();
            }
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
        List<Action> actions = new ArrayList<>();
        try {
            jdbcTemplate.executeQuery(ActionMgtSQLConstants.Query.GET_ACTIONS_BASIC_INFO_BY_ACTION_TYPE,
                (resultSet, rowNumber) -> {
                    String actionId = resultSet.getString(ActionMgtSQLConstants.Column.ACTION_UUID);
                    Action actionBasicInfo = new Action.ActionResponseBuilder()
                            .id(actionId)
                            .type(Action.ActionTypes
                                    .valueOf(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_TYPE)))
                            .name(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_NAME))
                            .description(resultSet.getString(ActionMgtSQLConstants.Column.ACTION_DESCRIPTION))
                            .status(Action.Status.valueOf(
                                    resultSet.getString(ActionMgtSQLConstants.Column.ACTION_STATUS)))
                            .build();

                    Map<String, String> actionProperties = getActionPropertiesById(actionId, tenantId);
                    actions.add(buildActionResponse(actionType, actionBasicInfo, actionProperties, tenantId));
                    return null;
                },
                statement -> {
                    statement.setString(ActionMgtSQLConstants.Column.ACTION_TYPE, actionType);
                    statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                });

            return actions;
        } catch (ActionMgtRuntimeException | DataAccessException e) {
            /**
             * Handling {@link ActionMgtRuntimeException}, which is intentionally thrown to represent underlying
             * exceptions from the {@link #buildActionResponse(String, Action, Map, Integer)} method.
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
                // Update Action Properties.
                updateActionProperties(actionType, actionId, updatingAction, existingAction, tenantId);

                return null;
            });

            return getActionByActionId(actionType, actionId, tenantId);
        } catch (TransactionException e) {
            if (e.getCause() instanceof ActionMgtClientException) {
                throw (ActionMgtClientException) e.getCause();
            }
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
                // Delete action type specific properties.
                deleteActionTypeSpecificProperties(actionType, action, tenantId);

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
                Map<String, String> actionProperties = getActionPropertiesById(actionId, tenantId);
                action = buildActionResponse(actionType, action, actionProperties, tenantId);
            }

            return action;
        } catch (ActionMgtException | ActionMgtRuntimeException | SQLException e) {
            /**
             * Handling {@link ActionMgtRuntimeException}, which is intentionally thrown to represent underlying
             * exceptions from the {@link #buildActionResponse(String, Action, Map, Integer)} method.
             */
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BY_ID, e);
        }
    }

    /**
     * Add Action properties.
     *
     * @param actionType Type of the Action.
     * @param actionId   UUID of the created Action.
     * @param action     Properties of the Action.
     * @param tenantId   Tenant ID.
     * @throws ActionMgtException If an error occurs while adding action properties to the database.
     */
    private void addActionProperties(String actionType, String actionId, Action action,
                                     Integer tenantId) throws ActionMgtException {

        try {
            Map<String, String> actionProperties =
                    resolveActionTypeSpecificProperties(actionType, actionId, action, null, tenantId);

            EndpointConfig endpoint = action.getEndpoint();
            // Encrypt the authentication secrets.
            List<AuthProperty> authProperties =
                    actionSecretProcessor.encryptAssociatedSecrets(endpoint.getAuthentication(), actionId);

            actionProperties.put(URI_PROPERTY, endpoint.getUri());
            actionProperties.put(AUTHN_TYPE_PROPERTY, endpoint.getAuthentication().getType().name());
            authProperties.forEach(authProperty -> actionProperties.put(authProperty.getName(),
                    authProperty.getValue()));

            addActionPropertiesToDB(actionId, actionProperties, tenantId);
        } catch (ActionMgtClientException e) {
            throw e;
        } catch (ActionMgtException | SecretManagementException | TransactionException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION_PROPERTIES, e);
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
        jdbcTemplate.withTransaction(template -> {
            template.executeBatchInsert(ActionMgtSQLConstants.Query.ADD_ACTION_ENDPOINT_PROPERTIES,
                    statement -> {
                        for (Map.Entry<String, String> property : actionProperties.entrySet()) {
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
    }

    /**
     * Update the properties of an {@link Action} by given Action ID.
     *
     * @param actionId       Action ID.
     * @param updatingAction Information to be updated.
     * @param existingAction Existing Action information.
     * @param tenantId       Tenant ID.
     * @throws ActionMgtException If an error occurs while updating the Action properties.
     */
    private void updateActionProperties(String actionType, String actionId, Action updatingAction,
                                        Action existingAction, Integer tenantId) throws ActionMgtException {

        try {
            Map<String, String> actionProperties = resolveEndpointProperties(actionId, updatingAction, existingAction);
            actionProperties.putAll(resolveActionTypeSpecificProperties(actionType, actionId, updatingAction,
                    existingAction, tenantId));

            updateActionPropertiesInDB(actionId, actionProperties, tenantId);
        } catch (ActionMgtClientException e) {
            throw e;
        } catch (ActionMgtException | SecretManagementException | TransactionException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_PROPERTIES, e);
        }
    }

    /**
     * Update the basic information of an {@link Action} by given Action ID.
     *
     * @param actionId           UUID of the created Action.
     * @param updatingProperties Action properties to be updated.
     * @param tenantId           Tenant ID.
     * @throws TransactionException If an error occurs while updating the Action properties.
     */
    private void updateActionPropertiesInDB(String actionId, Map<String, String> updatingProperties,
                                            Integer tenantId) throws TransactionException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        jdbcTemplate.withTransaction(template -> {
            template.executeUpdate(ActionMgtSQLConstants.Query.DELETE_ACTION_ENDPOINT_PROPERTIES,
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });

            // Add updated action properties.
            addActionPropertiesToDB(actionId, updatingProperties, tenantId);
            return null;
        });
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
     * Get Action properties by ID.
     *
     * @param actionId UUID of the created Action.
     * @param tenantId Tenant ID.
     * @return A map of action properties, including any additional data based on action type.
     * @throws SQLException If an error occurs while retrieving action properties from the database.
     */
    private Map<String, String> getActionPropertiesById(String actionId, Integer tenantId)
            throws SQLException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        Map<String, String> actionEndpointProperties = new HashMap<>();
        try {
            jdbcTemplate.executeQuery(ActionMgtSQLConstants.Query.GET_ACTION_ENDPOINT_INFO_BY_ID,
                    (resultSet, rowNumber) -> {
                        actionEndpointProperties.put(
                                resultSet.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_NAME),
                                resultSet.getString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_PROPERTY_VALUE));
                        return null;
                    },
                    statement -> {
                        statement.setString(ActionMgtSQLConstants.Column.ACTION_ENDPOINT_UUID, actionId);
                        statement.setInt(ActionMgtSQLConstants.Column.TENANT_ID, tenantId);
                    });

            return actionEndpointProperties;
        } catch (DataAccessException e) {
            throw new SQLException(ActionMgtConstants.ErrorMessages
                    .ERROR_WHILE_RETRIEVING_ACTION_PROPERTIES.getMessage(), e);
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
     * @throws ActionMgtException If an error occurs while updating the Action basic information.
     */
    private void updateBasicInfo(String actionType, String actionId, Action updatingAction, Action existingAction,
                                 Integer tenantId) throws ActionMgtException {

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
     * Resolves the endpoint properties for an action, supporting both addAction and updateAction scenarios.
     * This method ensures that authentication secrets are handled appropriately, and the URI is resolved
     * based on the provided or existing endpoint configurations.
     * When the updating action does not contain endpoint configuration, it uses the existing endpoint's properties.
     *
     * @param actionId       Action ID.
     * @param updatingAction Action to update.
     * @param existingAction Existing Action.
     * @return A map containing the resolved endpoint properties to be stored.
     * @throws SecretManagementException If an error occurs while updating the authentication secrets.
     */
    private Map<String, String> resolveEndpointProperties(String actionId, Action updatingAction, Action existingAction)
            throws SecretManagementException {

        EndpointConfig updatingEndpoint = updatingAction.getEndpoint();
        EndpointConfig existingEndpoint = existingAction.getEndpoint();

        Map<String, String> resolvedEndpointProperties =
                resolveEndpointAuthenticationProperties(actionId, updatingEndpoint, existingEndpoint);

        if (updatingEndpoint != null && updatingEndpoint.getUri() != null) {
            resolvedEndpointProperties.put(URI_PROPERTY, updatingEndpoint.getUri());
        } else {
            resolvedEndpointProperties.put(URI_PROPERTY, existingEndpoint.getUri());
        }

        return resolvedEndpointProperties;
    }

    /**
     * Resolves the authentication properties for an endpoint, handling both addAction and updateAction scenarios.
     * In addAction, the method generates new secrets based on the provided endpoint configuration.
     * In updateAction, it deletes existing secrets and updates them with new properties as necessary.
     * When the updating endpoint does not contain authentication, it uses the existing endpoint's properties.
     *
     * @param actionId         Action ID.
     * @param updatingEndpoint Endpoint configurations to be updated.
     * @param existingEndpoint Existing Endpoint configurations.
     * @return A map containing the resolved endpoint authentication properties to be stored.
     * @throws SecretManagementException If an error occurs while updating the authentication secrets.
     */
    private Map<String, String> resolveEndpointAuthenticationProperties(String actionId,
                                                                        EndpointConfig updatingEndpoint,
                                                                        EndpointConfig existingEndpoint)
            throws SecretManagementException {

        Authentication updatingAuthentication = updatingEndpoint != null ? updatingEndpoint.getAuthentication() : null;
        Authentication existingAuthentication = existingEndpoint.getAuthentication();

        Map<String, String> authentication = new HashMap<>();
        Authentication.Type resolvedAuthType = existingAuthentication.getType();;
        List<AuthProperty> resolvedAuthProperties = existingAuthentication.getProperties();;

        if (updatingAuthentication != null) {
            // Delete existing secrets.
            actionSecretProcessor.deleteAssociatedSecrets(existingAuthentication, actionId);
            // Add new secrets.
            resolvedAuthProperties = actionSecretProcessor.encryptAssociatedSecrets(updatingAuthentication, actionId);
            resolvedAuthType = updatingAuthentication.getType();
        }

        authentication.put(AUTHN_TYPE_PROPERTY, resolvedAuthType.getName());
        resolvedAuthProperties.forEach(property -> authentication.put(property.getName(), property.getValue()));

        return authentication;
    }

    /**
     * Resolve the action type specific properties for creating or updating an action.
     *
     * @param actionType     Action Type.
     * @param actionId       Action ID.
     * @param inputAction    A map containing the properties for the new or updated action.
     * @param existingAction A map containing the existing properties.
     * @param tenantId       Tenant ID.
     * @return A map containing the resolved action type specific properties.
     * @throws ActionMgtException If an error occurs while handling action type specific properties.
     */
    private Map<String, String> resolveActionTypeSpecificProperties(String actionType, String actionId,
                                                                    Action inputAction,
                                                                    Action existingAction,
                                                                    Integer tenantId) throws ActionMgtException {

        Map<String, String> actionTypeSpecificProperties = new HashMap<>();
        switch (Action.ActionTypes.valueOf(actionType)) {
            case PRE_UPDATE_PASSWORD:
                PreUpdatePasswordAction inputPreUpdatePasswordAction = (PreUpdatePasswordAction) inputAction;
                PreUpdatePasswordAction existingPreUpdatePasswordAction = (PreUpdatePasswordAction) existingAction;

                if (inputPreUpdatePasswordAction.getPasswordSharingFormat() != null) {
                    actionTypeSpecificProperties.put(PASSWORD_SHARING_FORMAT_PROPERTY,
                            inputPreUpdatePasswordAction.getPasswordSharingFormat().name());
                } else {
                    actionTypeSpecificProperties.put(PASSWORD_SHARING_FORMAT_PROPERTY,
                            existingPreUpdatePasswordAction.getPasswordSharingFormat().name());
                }

                // Handle certificate changes.
                String certId = handleCertificateChanges(actionId, inputPreUpdatePasswordAction,
                        existingPreUpdatePasswordAction, tenantId);
                if (StringUtils.isNotEmpty(certId)) {
                    actionTypeSpecificProperties.put(CERTIFICATE_ID_PROPERTY, certId);
                }

                break;
            case PRE_ISSUE_ACCESS_TOKEN:
            default:
                break;
        }

        return actionTypeSpecificProperties;
    }

    /**
     * Deletes action type-specific properties associated with the provided action.
     *
     * @param actionType Type of the Action.
     * @param action     Action information.
     * @param tenantId   Tenant Id.
     * @throws ActionMgtException If an error occurs while deleting action type specific properties.
     */
    private void deleteActionTypeSpecificProperties(String actionType, Action action, Integer tenantId)
            throws ActionMgtException {

        switch (Action.ActionTypes.valueOf(actionType)) {
            case PRE_UPDATE_PASSWORD:
                Certificate certificate = ((PreUpdatePasswordAction) action).getCertificate();
                if (certificate != null) {
                    deleteCertificate(certificate.getId(), tenantId);
                }
                break;
            case PRE_ISSUE_ACCESS_TOKEN:
            default:
                break;
        }
    }

    /**
     * Updates the certificate associated with an action based on the provided updating properties.
     * If a new certificate is provided, it persists the certificate and returns its ID.
     * If the existing certificate is being removed (empty value), it deletes the certificate and returns null.
     * If the existing certificate is being updated, it updates the certificate and returns its existing ID.
     *
     * @param actionId       Action ID.
     * @param inputAction    A map containing the properties to update, including the certificate.
     * @param existingAction A map containing the existing properties, including the current certificate ID.
     * @param tenantId       Tenant ID.
     * @return The updated certificate ID, or null if the certificate was deleted.
     * @throws ActionMgtException If an error occurs while updating the certificate.
     */
    private String handleCertificateChanges(String actionId, PreUpdatePasswordAction inputAction,
                                            PreUpdatePasswordAction existingAction, Integer tenantId)
            throws ActionMgtException {

        String updatingCertificate = inputAction.getCertificate() != null ?
                inputAction.getCertificate().getCertificateContent() : null;
        String updatingCertificateId = existingAction != null && existingAction.getCertificate() != null
                ? existingAction.getCertificate().getId() : null;

        if (updatingCertificate != null) {
            if (updatingCertificateId == null) {
                // Add the new certificate.
                updatingCertificateId = addCertificate(actionId, updatingCertificate, tenantId);
            } else if (updatingCertificate.isEmpty()) {
                // Delete the existing certificate.
                deleteCertificate(updatingCertificateId, tenantId);
                updatingCertificateId = null;
            } else {
                // Update the existing certificate.
                updateCertificate(updatingCertificateId, updatingCertificate, tenantId);
            }
        }

        return updatingCertificateId;
    }

    /**
     * Add the certificate in the database.
     *
     * @param actionId           UUID of the created Action.
     * @param certificateContent Certificate to be added.
     * @param tenantId           Tenant ID.
     * @throws ActionMgtException If an error occurs while adding the certificate.
     * @returns Certificate ID.
     */
    private String addCertificate(String actionId, String certificateContent, Integer tenantId)
            throws ActionMgtException {
        try {
            Certificate certificate = new Certificate.Builder()
                    .name("ACTIONS:" + actionId)
                    .certificateContent(certificateContent)
                    .build();
            return ActionMgtServiceComponentHolder.getInstance().getCertificateManagementService()
                    .addCertificate(certificate, IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (CertificateMgtClientException e) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_CERTIFICATE, e);
        } catch (CertificateMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION_CERTIFICATE, e);
        }
    }

    /**
     * Get the certificate content by certificate ID.
     *
     * @param certificateId Certificate ID.
     * @param tenantId      Tenant ID.
     * @return Certificate information.
     * @throws ActionMgtException If an error occurs while retrieving the certificate from the database.
     */
    private Certificate getCertificate(String certificateId, Integer tenantId)
            throws ActionMgtException {

        try {
            return ActionMgtServiceComponentHolder.getInstance().getCertificateManagementService()
                    .getCertificate(certificateId, IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (CertificateMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_CERTIFICATE, e);
        }
    }

    /**
     * Update the certificate by certificate ID.
     *
     * @param certificateId       Certificate ID.
     * @param updatingCertificate Certificate to be updated.
     * @param tenantId            Tenant ID.
     * @throws ActionMgtException If an error occurs while updating the certificate in the database.
     */
    private void updateCertificate(String certificateId, String updatingCertificate, Integer tenantId)
            throws ActionMgtException {

        try {
            ActionMgtServiceComponentHolder.getInstance().getCertificateManagementService()
                    .updateCertificateContent(certificateId, updatingCertificate,
                            IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (CertificateMgtClientException e) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_CERTIFICATE, e);
        } catch (CertificateMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION_CERTIFICATE, e);
        }
    }

    /**
     * Delete the certificate by certificate ID.
     *
     * @param certificateId Certificate ID.
     * @param tenantId      Tenant ID.
     * @throws ActionMgtException If an error occurs while deleting the certificate in the database.
     */
    private void deleteCertificate(String certificateId, Integer tenantId) throws ActionMgtException {

        try {
            ActionMgtServiceComponentHolder.getInstance().getCertificateManagementService()
                    .deleteCertificate(certificateId, IdentityTenantUtil.getTenantDomain(tenantId));
        } catch (CertificateMgtException e) {
            throw ActionManagementUtil.handleServerException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_DELETING_ACTION_CERTIFICATE, e);
        }
    }

    /**
     * Build the Action Response Object according to the actionType.
     *
     * @param actionType       Action Type.
     * @param action           Action basic information.
     * @param actionProperties Action Properties.
     * @param tenantId          Tenant Id.
     * @return Action Response.
     * @throws ActionMgtRuntimeException If an error occurs while retrieving the certificate.
     */
    private Action buildActionResponse(String actionType, Action action, Map<String, String> actionProperties,
                                       Integer tenantId) {

        Action.ActionResponseBuilder actionResponseBuilder;
        try {
            switch (Action.ActionTypes.valueOf(actionType)) {
                case PRE_UPDATE_PASSWORD:
                    Certificate certificate = actionProperties.get(CERTIFICATE_ID_PROPERTY) != null ?
                            getCertificate(actionProperties.get(CERTIFICATE_ID_PROPERTY), tenantId) : null;

                    actionResponseBuilder = new PreUpdatePasswordAction.ResponseBuilder()
                            .certificate(certificate)
                            .passwordSharingFormat(PreUpdatePasswordAction.PasswordFormat.valueOf(
                                    actionProperties.get(PASSWORD_SHARING_FORMAT_PROPERTY)));
                    break;
                case PRE_ISSUE_ACCESS_TOKEN:
                default:
                    actionResponseBuilder = new Action.ActionResponseBuilder();
                    break;
            }

            Authentication authentication = null;
            Authentication.Type authnType =
                    Authentication.Type.valueOf(actionProperties.get(ActionMgtConstants.AUTHN_TYPE_PROPERTY));
            switch (authnType) {
                case BASIC:
                    authentication = new Authentication.BasicAuthBuilder(
                            actionProperties.get(Authentication.Property.USERNAME.getName()),
                            actionProperties.get(Authentication.Property.PASSWORD.getName())).build();
                    break;
                case BEARER:
                    authentication = new Authentication.BearerAuthBuilder(
                            actionProperties.get(Authentication.Property.ACCESS_TOKEN.getName())).build();
                    break;
                case API_KEY:
                    authentication = new Authentication.APIKeyAuthBuilder(
                            actionProperties.get(Authentication.Property.HEADER.getName()),
                            actionProperties.get(Authentication.Property.VALUE.getName())).build();
                    break;
                case NONE:
                    authentication = new Authentication.NoneAuthBuilder().build();
                    break;
                default:
                    break;
            }

            EndpointConfig endpointConfig = new EndpointConfig.EndpointConfigBuilder()
                        .uri(actionProperties.get(ActionMgtConstants.URI_PROPERTY))
                        .authentication(authentication)
                        .build();

            return actionResponseBuilder
                    .id(action.getId())
                    .type(Action.ActionTypes.valueOf(actionType))
                    .name(action.getName())
                    .description(action.getDescription())
                    .status(action.getStatus())
                    .endpoint(endpointConfig)
                    .build();
        } catch (ActionMgtException e) {
            /**
             * Throwing a runtime exception because {@link ActionMgtException} is not handled in
             * {@link org.wso2.carbon.database.utils.jdbc.RowMapper} of {@link NamedJdbcTemplate#executeQuery(String,
             * org.wso2.carbon.database.utils.jdbc.RowMapper,org.wso2.carbon.database.utils.jdbc.NamedQueryFilter)}
             * in {@link #getActionsByActionType(String, Integer)}
             */
            throw ActionManagementUtil.handleRuntimeException(
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_BUILDING_ACTION_RESPONSE.getMessage(), e);
        }
    }
}
