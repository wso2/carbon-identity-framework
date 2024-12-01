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
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.action.management.constant.error.ErrorMessage;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.exception.ActionPropertyResolverClientException;
import org.wso2.carbon.identity.action.management.exception.ActionPropertyResolverException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.ActionDTO;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.service.ActionPropertyResolver;
import org.wso2.carbon.identity.action.management.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.util.ActionManagementExceptionHandler;
import org.wso2.carbon.identity.action.management.util.ActionSecretProcessor;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Facade class for Action Management DAO.
 * ActionManagementDAOFacade is responsible for handling external service integrations.
 */
public class ActionManagementDAOFacade implements ActionManagementDAO {

    private static final Log LOG = LogFactory.getLog(ActionManagementDAOFacade.class);

    private final ActionManagementDAO actionManagementDAO;
    private final ActionSecretProcessor actionSecretProcessor;

    public ActionManagementDAOFacade(ActionManagementDAO actionManagementDAO) {

        this.actionManagementDAO = actionManagementDAO;
        this.actionSecretProcessor = new ActionSecretProcessor();
    }

    @Override
    public void addAction(ActionDTO actionDTO, Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                ActionDTOBuilder actionDTOBuilder = new ActionDTOBuilder(actionDTO);
                // Encrypt authentication secrets
                encryptAddingAuthSecrets(actionDTOBuilder);
                // Resolve action properties
                ActionDTO resolvedActionDTO = getActionDTOWithResolvedAddingProperties(actionDTOBuilder.build(),
                        tenantId);

                actionManagementDAO.addAction(resolvedActionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            handleActionPropertyResolverClientException(e.getCause());
            LOG.debug("Error while creating the Action of Action Type: " + actionDTO.getType().getDisplayName() +
                            " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                            ". Rolling back created action information, authentication secrets and action properties.");
            throw ActionManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_WHILE_ADDING_ACTION, e);
        }
    }

    @Override
    public List<ActionDTO> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        try {
            List<ActionDTO> actionDTOS = actionManagementDAO.getActionsByActionType(actionType, tenantId);

            return getActionDTOsWithPopulatedProperties(actionType, actionDTOS, tenantId);
        } catch (ActionMgtException | ActionPropertyResolverException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE, e);
        }
    }

    @Override
    public ActionDTO getActionByActionId(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        try {
            ActionDTO actionDTO = actionManagementDAO.getActionByActionId(actionType, actionId, tenantId);
            if (actionDTO == null) {
                return null;
            }

            // Populate action properties
            return getActionDTOWithPopulatedProperties(actionDTO, tenantId);
        } catch (ActionMgtException | ActionPropertyResolverException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_ACTION_BY_ID, e);
        }
    }

    @Override
    public void updateAction(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                ActionDTOBuilder updatingActionDTOBuilder = new ActionDTOBuilder(updatingActionDTO);
                // Encrypt authentication secrets
                encryptUpdatingAuthSecrets(updatingActionDTOBuilder, existingActionDTO);
                // Resolve action properties
                ActionDTO resolvedUpdatingActionDTO =
                        getActionDTOWithResolvedUpdatingProperties(updatingActionDTOBuilder.build(), existingActionDTO,
                                tenantId);

                actionManagementDAO.updateAction(resolvedUpdatingActionDTO, existingActionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            handleActionPropertyResolverClientException(e.getCause());
            LOG.debug("Error while updating the Action of Action Type: " +
                    updatingActionDTO.getType().getDisplayName() + " and Action ID: " + updatingActionDTO.getId() +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back updated action information");
            throw ActionManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_WHILE_UPDATING_ACTION, e);
        }
    }

    @Override
    public void deleteAction(ActionDTO deletingActionDTO, Integer tenantId) throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                actionManagementDAO.deleteAction(deletingActionDTO, tenantId);
                // Encrypt authentication secrets
                deleteAuthenticationSecrets(deletingActionDTO);
                // Resolve action properties
                deleteProperties(deletingActionDTO, tenantId);

                return null;
            });
        } catch (TransactionException e) {
            LOG.debug("Error while deleting the Action of Action Type: " +
                    deletingActionDTO.getType().getDisplayName() + " and Action ID: " + deletingActionDTO.getId() +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back deleted action information");
            throw ActionManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_WHILE_DELETING_ACTION, e);
        }
    }

    @Override
    public ActionDTO activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        try {
            return actionManagementDAO.activateAction(actionType, actionId, tenantId);
        } catch (ActionMgtException e) {
            throw ActionManagementExceptionHandler.handleServerException(ErrorMessage.ERROR_WHILE_ACTIVATING_ACTION, e);
        }
    }

    @Override
    public ActionDTO deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        try {
            return actionManagementDAO.deactivateAction(actionType, actionId, tenantId);
        } catch (ActionMgtException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_DEACTIVATING_ACTION, e);
        }
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        try {
            return actionManagementDAO.getActionsCountPerType(tenantId);
        } catch (ActionMgtException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_ACTIONS_COUNT_PER_TYPE, e);
        }
    }

    /**
     * Encrypt and store the authentication secrets of the Action Endpoint Authentication.
     *
     * @param actionDTOBuilder ActionDTOBuilder object.
     * @throws ActionMgtException If an error occurs while encrypting the authentication secrets.
     */
    private void encryptAddingAuthSecrets(ActionDTOBuilder actionDTOBuilder) throws ActionMgtException {

        try {
            List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(
                    actionDTOBuilder.getEndpoint().getAuthentication(), actionDTOBuilder.getId());

            addEncryptedAuthSecretsToBuilder(actionDTOBuilder, encryptedProperties);
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while encrypting Action Endpoint Authentication Secrets.", e);
        }
    }

    /**
     * Encrypt and update the authentication secrets of the Action Endpoint Authentication.
     * If the authentication type is changed, delete the existing authentication secrets and add new secrets.
     * If the authentication properties are updated, update the existing authentication secrets.
     *
     * @param updatingActionDTOBuilder ActionDTOBuilder object.
     * @param existingActionDTO        Existing ActionDTO object.
     * @throws ActionMgtException If an error occurs while encrypting the authentication secrets.
     */
    private void encryptUpdatingAuthSecrets(ActionDTOBuilder updatingActionDTOBuilder,
                                            ActionDTO existingActionDTO)
            throws ActionMgtException {

        if (updatingActionDTOBuilder.getEndpoint() == null ||
                updatingActionDTOBuilder.getEndpoint().getAuthentication() == null) {
            return;
        }

        Authentication updatingAuthentication = updatingActionDTOBuilder.getEndpoint().getAuthentication();
        Authentication existingAuthentication = existingActionDTO.getEndpoint().getAuthentication();

        try {
            if (updatingAuthentication.getType() != existingAuthentication.getType()) {
                actionSecretProcessor.deleteAssociatedSecrets(existingAuthentication, existingActionDTO.getId());
            }
            List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(
                    updatingAuthentication, updatingActionDTOBuilder.getId());

            addEncryptedAuthSecretsToBuilder(updatingActionDTOBuilder, encryptedProperties);
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while updating Action Endpoint Authentication Secrets.", e);
        }
    }

    /**
     * Delete the authentication secrets of the Action Endpoint Authentication.
     *
     * @param deletingActionDTO ActionDTO object.
     * @throws ActionMgtException If an error occurs while deleting the authentication secrets.
     */
    private void deleteAuthenticationSecrets(ActionDTO deletingActionDTO) throws ActionMgtException {

        try {
            actionSecretProcessor.deleteAssociatedSecrets(deletingActionDTO.getEndpoint().getAuthentication(),
                    deletingActionDTO.getId());
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while deleting Action Endpoint Authentication Secrets.", e);
        }
    }

    /**
     * Add the encrypted authentication secrets and replace the input authentication properties in the ActionDTOBuilder
     * object.
     *
     * @param actionDTOBuilder     ActionDTOBuilder object.
     * @param encryptedProperties  List of encrypted AuthProperty objects.
     */
    private void addEncryptedAuthSecretsToBuilder(ActionDTOBuilder actionDTOBuilder,
                                                  List<AuthProperty> encryptedProperties) {

        Map<String, String> encryptedPropertyMap = encryptedProperties.stream()
                .collect(Collectors.toMap(AuthProperty::getName, AuthProperty::getValue));

        actionDTOBuilder.endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .uri(actionDTOBuilder.getEndpoint().getUri())
                        .authentication(new Authentication.AuthenticationBuilder()
                                .type(actionDTOBuilder.getEndpoint().getAuthentication().getType())
                                .properties(encryptedPropertyMap)
                                .build())
                        .build());
    }

    /**
     * Get the ActionDTO with resolved adding properties that needs to be added in the Action Management Service.
     *
     * @param actionDTO ActionDTO object.
     * @param tenantId  Tenant ID.
     * @return ActionDTO object with resolved adding properties.
     * @throws ActionPropertyResolverException If an error occurs while resolving the adding properties.
     */
    private ActionDTO getActionDTOWithResolvedAddingProperties(ActionDTO actionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());
        if (actionPropertyResolver == null) {
            return actionDTO;
        }

        return actionPropertyResolver.resolveAddingProperties(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO list with populated properties according to the references stored in the Action Management
     * Service.
     *
     * @param actionType Action type.
     * @param actionDTOs List of ActionDTO objects.
     * @param tenantId   Tenant ID.
     * @return List of ActionDTO objects with populated properties.
     * @throws ActionPropertyResolverException If an error occurs while populating the properties.
     */
    private List<ActionDTO> getActionDTOsWithPopulatedProperties(String actionType, List<ActionDTO> actionDTOs,
                                                                 Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(Action.ActionTypes.valueOf(actionType));
        if (actionPropertyResolver == null) {
            return actionDTOs;
        }

        return actionPropertyResolver.populateProperties(actionDTOs, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO with populated properties according to the references stored in the Action Management Service.
     *
     * @param actionDTO ActionDTO object.
     * @param tenantId  Tenant ID.
     * @return ActionDTO object with populated properties.
     * @throws ActionPropertyResolverException If an error occurs while populating the properties.
     */
    private ActionDTO getActionDTOWithPopulatedProperties(ActionDTO actionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());
        if (actionPropertyResolver == null) {
            return actionDTO;
        }

        return actionPropertyResolver.populateProperties(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO with resolved updating properties that needs to be updated in the Action Management Service.
     *
     * @param updatingActionDTO Updating ActionDTO object.
     * @param existingActionDTO Existing ActionDTO object.
     * @param tenantId          Tenant ID.
     * @return ActionDTO object with resolved updating properties.
     * @throws ActionPropertyResolverException If an error occurs while resolving the updating properties.
     */
    private ActionDTO getActionDTOWithResolvedUpdatingProperties(ActionDTO updatingActionDTO,
                                                                 ActionDTO existingActionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(updatingActionDTO.getType());
        if (actionPropertyResolver == null) {
            return updatingActionDTO;
        }

        return actionPropertyResolver.resolveUpdatingProperties(updatingActionDTO, existingActionDTO,
                IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Delete the properties that are deleted in the Action Management Service.
     *
     * @param deletingActionDTO Deleting ActionDTO object.
     * @param tenantId          Tenant ID.
     * @throws ActionPropertyResolverException If an error occurs while deleting the properties.
     */
    private void deleteProperties(ActionDTO deletingActionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(deletingActionDTO.getType());
        if (actionPropertyResolver == null) {
            return;
        }

        actionPropertyResolver.deleteProperties(deletingActionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Handle the ActionPropertyResolverClientException and throw the relevant ActionMgtClientException.
     *
     * @param throwable Throwable object.
     * @throws ActionMgtClientException If an error occurs while handling the ActionPropertyResolverClientException.
     */
    private static void handleActionPropertyResolverClientException(Throwable throwable)
            throws ActionMgtClientException {

        if (throwable instanceof ActionPropertyResolverClientException) {
            throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_INVALID_ACTION_PROPERTIES,
                    throwable.getMessage());
        }
    }
}
