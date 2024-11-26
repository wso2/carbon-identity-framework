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
import org.wso2.carbon.identity.action.management.ActionPropertyResolver;
import org.wso2.carbon.identity.action.management.constant.error.ErrorMessage;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.dao.model.ActionDTO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.exception.ActionPropertyResolverException;
import org.wso2.carbon.identity.action.management.factory.ActionPropertyResolverFactory;
import org.wso2.carbon.identity.action.management.model.AuthProperty;
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
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
                // Encrypt authentication secrets
                encryptAuthenticationSecrets(actionDTO);
                // Resolve action properties
                addProperties(actionDTO, tenantId);

                actionManagementDAO.addAction(actionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            LOG.debug("Error while creating the Action of Action Type: " + actionDTO.getType().getDisplayName() +
                            " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                            ". Rolling back created action information, authentication secrets and action properties.");
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_ADDING_ACTION, e);
        }
    }

    @Override
    public List<ActionDTO> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        try {
            List<ActionDTO> actionDTOS = actionManagementDAO.getActionsByActionType(actionType, tenantId);
            getPropertiesOfActionDTOs(actionType, actionDTOS, tenantId);

            return actionDTOS;
        } catch (ActionMgtException | ActionPropertyResolverException e) {
            throw ActionManagementUtil.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE, e);
        }
    }

    @Override
    public ActionDTO getActionByActionId(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        try {
            ActionDTO actionDTO = actionManagementDAO.getActionByActionId(actionType, actionId, tenantId);
            if (actionDTO != null) {
                // Resolve action properties
                getProperties(actionDTO, tenantId);
            }

            return actionDTO;
        } catch (ActionMgtException | ActionPropertyResolverException e) {
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_RETRIEVING_ACTION_BY_ID, e);
        }
    }

    @Override
    public void updateAction(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            jdbcTemplate.withTransaction(template -> {
                // Encrypt authentication secrets
                updateAuthenticationSecrets(updatingActionDTO, existingActionDTO);
                // Resolve action properties
                updateProperties(updatingActionDTO, existingActionDTO, tenantId);

                actionManagementDAO.updateAction(updatingActionDTO, existingActionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            LOG.debug("Error while updating the Action of Action Type: " +
                    updatingActionDTO.getType().getDisplayName() + " and Action ID: " + updatingActionDTO.getId() +
                    " in Tenant Domain: " + IdentityTenantUtil.getTenantDomain(tenantId) +
                    ". Rolling back updated action information");
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_UPDATING_ACTION, e);
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
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_DELETING_ACTION, e);
        }
    }

    @Override
    public ActionDTO activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        try {
            return actionManagementDAO.activateAction(actionType, actionId, tenantId);
        } catch (ActionMgtException e) {
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_ACTIVATING_ACTION, e);
        }
    }

    @Override
    public ActionDTO deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        try {
            return actionManagementDAO.deactivateAction(actionType, actionId, tenantId);
        } catch (ActionMgtException e) {
            throw ActionManagementUtil.handleServerException(ErrorMessage.ERROR_WHILE_DEACTIVATING_ACTION, e);
        }
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        return actionManagementDAO.getActionsCountPerType(tenantId);
    }

    private void encryptAuthenticationSecrets(ActionDTO actionDTO) throws ActionMgtException {

        try {
            List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(
                    actionDTO.getEndpoint().getAuthentication(), actionDTO.getId());
            actionDTO.setAuthenticationProperties(encryptedProperties);
        } catch (SecretManagementException e) {
            throw new ActionMgtServerException("Error while encrypting Action Endpoint Authentication Secrets.", e);
        }
    }

    private void updateAuthenticationSecrets(ActionDTO updatingActionDTO, ActionDTO existingActionDTO)
            throws ActionMgtException {

        if (updatingActionDTO.getEndpoint() == null || updatingActionDTO.getEndpoint().getAuthentication() == null) {
            return;
        }

        Authentication updatingAuthentication = updatingActionDTO.getEndpoint().getAuthentication();
        Authentication existingAuthentication = existingActionDTO.getEndpoint().getAuthentication();

        try {
            if (updatingAuthentication.getType() != existingAuthentication.getType()) {
                actionSecretProcessor.deleteAssociatedSecrets(existingAuthentication, existingActionDTO.getId());
            }
            List<AuthProperty> encryptedProperties = actionSecretProcessor.encryptAssociatedSecrets(
                    updatingAuthentication, updatingActionDTO.getId());
            updatingActionDTO.setAuthenticationProperties(encryptedProperties);
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

    private void addProperties(ActionDTO actionDTO, Integer tenantId) throws ActionPropertyResolverException {

        Map<String, String> properties = null;
        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());

        if (actionPropertyResolver != null) {
            properties = actionPropertyResolver.addProperties(actionDTO,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
        if (properties != null) {
            actionDTO.setProperties(properties.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }

    private void getPropertiesOfActionDTOs(String actionType, List<ActionDTO> actionDTOS, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(
                        org.wso2.carbon.identity.action.management.model.Action.ActionTypes.valueOf(actionType));
        if (actionPropertyResolver == null) {
            return;
        }

        for (ActionDTO actionDTO : actionDTOS) {
            actionDTO.setProperties(actionPropertyResolver.getProperties(actionDTO,
                    IdentityTenantUtil.getTenantDomain(tenantId)));
        }
    }

    private void getProperties(ActionDTO actionDTO, Integer tenantId) throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(actionDTO.getType());

        if (actionPropertyResolver != null) {
            actionDTO.setProperties(actionPropertyResolver.getProperties(actionDTO,
                    IdentityTenantUtil.getTenantDomain(tenantId)));
        }
    }

    private void updateProperties(ActionDTO updatingActionDTO, ActionDTO existingActionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(updatingActionDTO.getType());

        if (actionPropertyResolver != null) {
            Map<String, String> properties = actionPropertyResolver.updateProperties(updatingActionDTO,
                    existingActionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
            updatingActionDTO.setProperties(properties.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }

    private void deleteProperties(ActionDTO deletingActionDTO, Integer tenantId)
            throws ActionPropertyResolverException {

        ActionPropertyResolver actionPropertyResolver =
                ActionPropertyResolverFactory.getActionPropertyResolver(deletingActionDTO.getType());

        if (actionPropertyResolver != null) {
            actionPropertyResolver.deleteProperties(deletingActionDTO,
                    IdentityTenantUtil.getTenantDomain(tenantId));
        }
    }
}
