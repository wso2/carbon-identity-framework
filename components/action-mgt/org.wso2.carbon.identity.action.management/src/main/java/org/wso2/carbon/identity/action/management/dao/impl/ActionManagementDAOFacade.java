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

    private void encryptAddingAuthSecrets(ActionDTOBuilder actionDTOBuilder) throws ActionMgtException {

        try {
            List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(
                    actionDTOBuilder.getEndpoint().getAuthentication(), actionDTOBuilder.getId());

            addEncryptedAuthSecretsToBuilder(actionDTOBuilder, encryptedProperties);
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while encrypting Action Endpoint Authentication Secrets.", e);
        }
    }

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

    private void deleteAuthenticationSecrets(ActionDTO deletingActionDTO) throws ActionMgtServerException {

        try {
            actionSecretProcessor.deleteAssociatedSecrets(deletingActionDTO.getEndpoint().getAuthentication(),
                    deletingActionDTO.getId());
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while deleting Action Endpoint Authentication Secrets.", e);
        }
    }

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

    private ActionDTO getActionDTOWithResolvedAddingProperties(ActionDTO actionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());
        if (actionPropertyResolver == null) {
            return actionDTO;
        }

        return actionPropertyResolver.resolveAddingProperties(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    private List<ActionDTO> getActionDTOsWithPopulatedProperties(String actionType, List<ActionDTO> actionDTOS,
                                                                 Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(Action.ActionTypes.valueOf(actionType));
        if (actionPropertyResolver == null) {
            return actionDTOS;
        }

        return actionPropertyResolver.populateProperties(actionDTOS, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    private ActionDTO getActionDTOWithPopulatedProperties(ActionDTO actionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());
        if (actionPropertyResolver == null) {
            return actionDTO;
        }

        return actionPropertyResolver.populateProperties(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

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

    private void deleteProperties(ActionDTO deletingActionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(deletingActionDTO.getType());
        if (actionPropertyResolver == null) {
            return;
        }

        actionPropertyResolver.deleteProperties(deletingActionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    private static void handleActionPropertyResolverClientException(Throwable throwable)
            throws ActionMgtClientException {

        if (throwable instanceof ActionPropertyResolverClientException) {
            throw ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_INVALID_ACTION_PROPERTIES,
                    throwable.getMessage());
        }
    }
}
