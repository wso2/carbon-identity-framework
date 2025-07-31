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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.TransactionException;
import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionDTOModelResolverException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.ActionRule;
import org.wso2.carbon.identity.action.management.api.model.AuthProperty;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionDTOModelResolver;
import org.wso2.carbon.identity.action.management.internal.component.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.internal.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementExceptionHandler;
import org.wso2.carbon.identity.action.management.internal.util.ActionSecretProcessor;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.rule.management.api.exception.RuleManagementException;
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
                // Add action rule
                addActionRule(actionDTOBuilder, IdentityTenantUtil.getTenantDomain(tenantId));
                // Resolve action properties
                ActionDTO resolvedActionDTO = getResolvedActionDTOForAddOperation(actionDTOBuilder.build(),
                        tenantId);
                actionManagementDAO.addAction(resolvedActionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
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
            return getResolvedActionDTOsForGetOperation(actionType, actionDTOS, tenantId);
        } catch (ActionMgtException | ActionDTOModelResolverException e) {
            throw ActionManagementExceptionHandler.handleServerException(
                    ErrorMessage.ERROR_WHILE_RETRIEVING_ACTIONS_BY_ACTION_TYPE, e);
        }
    }

    @Override
    public ActionDTO getActionByActionId(String actionType, String actionId, Integer tenantId)
            throws ActionMgtException {

        NamedJdbcTemplate jdbcTemplate = new NamedJdbcTemplate(IdentityDatabaseUtil.getDataSource());
        try {
            return jdbcTemplate.withTransaction(template -> {
                ActionDTO actionDTO = actionManagementDAO.getActionByActionId(actionType, actionId, tenantId);
                if (actionDTO == null) {
                    return null;
                }

                ActionDTOBuilder actionDTOBuilder = new ActionDTOBuilder(actionDTO);
                // Load action rule
                loadActionRule(actionDTOBuilder, IdentityTenantUtil.getTenantDomain(tenantId));
                // Populate action properties
                return getResolvedActionDTOForGetOperation(actionDTOBuilder.build(), tenantId);
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
            handleActionPropertyResolverClientException(e.getCause());
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
                // Update action rule
                updateActionRule(updatingActionDTOBuilder, existingActionDTO,
                        IdentityTenantUtil.getTenantDomain(tenantId));
                // Resolve action properties
                ActionDTO resolvedUpdatingActionDTO =
                        getResolvedActionDTOForUpdateOperation(updatingActionDTOBuilder.build(), existingActionDTO,
                                tenantId);

                actionManagementDAO.updateAction(resolvedUpdatingActionDTO, existingActionDTO, tenantId);
                return null;
            });
        } catch (TransactionException e) {
            // Since exceptions thrown are wrapped with TransactionException, extracting the actual cause.
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
                deleteAuthenticationSecrets(deletingActionDTO);
                deleteActionRule(deletingActionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
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
                        .allowedHeaders(actionDTOBuilder.getEndpoint().getAllowedHeaders())
                        .allowedParameters(actionDTOBuilder.getEndpoint().getAllowedParameters())
                        .build());
    }

    private void addActionRule(ActionDTOBuilder actionDTOBuilder, String tenantDomain) throws ActionMgtException {

        if (actionDTOBuilder.getActionRule() == null || actionDTOBuilder.getActionRule().getRule() == null) {
            return;
        }

        try {
            ActionMgtServiceComponentHolder.getInstance()
                    .getRuleManagementService()
                    .addRule(actionDTOBuilder.getActionRule().getRule(), tenantDomain);
        } catch (RuleManagementException e) {
            throw new ActionMgtServerException("Error while adding the Rule associated with the Action.", e);
        }

    }

    private void loadActionRule(ActionDTOBuilder actionDTOBuilder, String tenantDomain)
            throws ActionMgtServerException {

        if (actionDTOBuilder.getActionRule() == null) {
            return;
        }

        try {
            ActionRule actionRule = ActionRule.create(ActionMgtServiceComponentHolder.getInstance()
                    .getRuleManagementService()
                    .getRuleByRuleId(actionDTOBuilder.getActionRule().getId(), tenantDomain));
            actionDTOBuilder.rule(actionRule);
        } catch (RuleManagementException e) {
            throw new ActionMgtServerException("Error while retrieving the Rule associated with the Action.", e);
        }
    }

    private void updateActionRule(ActionDTOBuilder updatingActionDTOBuilder, ActionDTO existingActionDTO,
                                  String tenantDomain) throws ActionMgtException {

         /*
             When updating an action, the action rule can be added, removed or updated.
             When action rule is added, the Rule object is added to the ActionRule of the ActionDTO.
             When action rule is removed, the Rule object is set as null in the ActionRule of the ActionDTO.
             This happens as the API accepts the removal of the rule in an action update via an empty rule JSON object.
             e.g., rule: {}. If rule is not present in the payload that means rule is not updated.
             When action rule is updated, the Rule object is updated in the ActionRule of the ActionDTO.
        */
        if (isAddingNewActionRule(updatingActionDTOBuilder, existingActionDTO)) {
            addActionRule(updatingActionDTOBuilder, tenantDomain);
        } else if (isRemovingExistingActionRule(updatingActionDTOBuilder, existingActionDTO)) {
            deleteActionRule(existingActionDTO, tenantDomain);
        } else if (isUpdatingExistingActionRule(updatingActionDTOBuilder, existingActionDTO)) {
            updateExistingActionRule(updatingActionDTOBuilder, existingActionDTO, tenantDomain);
        }
    }

    private boolean isAddingNewActionRule(ActionDTOBuilder updatingActionDTOBuilder, ActionDTO existingActionDTO)
            throws ActionMgtException {

        return existingActionDTO.getActionRule() == null && updatingActionDTOBuilder.getActionRule() != null &&
                updatingActionDTOBuilder.getActionRule().getRule() != null;
    }

    private boolean isRemovingExistingActionRule(ActionDTOBuilder updatingActionDTOBuilder,
                                                 ActionDTO existingActionDTO) throws ActionMgtException {

        return existingActionDTO.getActionRule() != null && updatingActionDTOBuilder.getActionRule() != null &&
                updatingActionDTOBuilder.getActionRule().getRule() == null;
    }

    private boolean isUpdatingExistingActionRule(ActionDTOBuilder updatingActionDTOBuilder,
                                                 ActionDTO existingActionDTO) throws ActionMgtException {

        return existingActionDTO.getActionRule() != null && updatingActionDTOBuilder.getActionRule() != null &&
                updatingActionDTOBuilder.getActionRule().getRule() != null;
    }

    private void updateExistingActionRule(ActionDTOBuilder updatingActionDTOBuilder, ActionDTO existingActionDTO,
                                          String tenantDomain) throws ActionMgtException {

        try {
            updatingActionDTOBuilder.getActionRule().getRule().setId(existingActionDTO.getActionRule().getId());
            ActionMgtServiceComponentHolder.getInstance()
                    .getRuleManagementService()
                    .updateRule(updatingActionDTOBuilder.getActionRule().getRule(), tenantDomain);
        } catch (RuleManagementException e) {
            throw new ActionMgtServerException("Error while updating the Rule associated with the Action.", e);
        }
    }

    private void deleteActionRule(ActionDTO actionDTO, String tenantDomain) throws ActionMgtServerException {

        if (actionDTO.getActionRule() == null) {
            return;
        }

        try {
            ActionMgtServiceComponentHolder.getInstance()
                    .getRuleManagementService()
                    .deleteRule(actionDTO.getActionRule().getId(), tenantDomain);
        } catch (RuleManagementException e) {
            throw new ActionMgtServerException("Error while deleting the Rule associated with the Action.", e);
        }
    }

    /**
     * Get the ActionDTO with resolved adding properties that needs to be added in the Action Management Service.
     *
     * @param actionDTO ActionDTO object.
     * @param tenantId  Tenant ID.
     * @return ActionDTO object with resolved adding properties.
     * @throws ActionDTOModelResolverException If an error occurs while resolving the adding properties.
     */
    private ActionDTO getResolvedActionDTOForAddOperation(ActionDTO actionDTO, Integer tenantId)
            throws ActionDTOModelResolverException {

        ActionDTOModelResolver actionDTOModelResolver =
                ActionDTOModelResolverFactory.getActionDTOModelResolver(actionDTO.getType());
        if (actionDTOModelResolver == null) {
            return actionDTO;
        }

        return actionDTOModelResolver.resolveForAddOperation(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO list with populated properties according to the references stored in the Action Management
     * Service.
     *
     * @param actionType Action type.
     * @param actionDTOs List of ActionDTO objects.
     * @param tenantId   Tenant ID.
     * @return List of ActionDTO objects with populated properties.
     * @throws ActionDTOModelResolverException If an error occurs while populating the properties.
     */
    private List<ActionDTO> getResolvedActionDTOsForGetOperation(String actionType, List<ActionDTO> actionDTOs,
                                                                 Integer tenantId)
            throws ActionDTOModelResolverException {

        ActionDTOModelResolver actionDTOModelResolver =
                ActionDTOModelResolverFactory.getActionDTOModelResolver(Action.ActionTypes.valueOf(actionType));
        if (actionDTOModelResolver == null) {
            return actionDTOs;
        }

        return actionDTOModelResolver.resolveForGetOperation(actionDTOs, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO with populated properties according to the references stored in the Action Management Service.
     *
     * @param actionDTO ActionDTO object.
     * @param tenantId  Tenant ID.
     * @return ActionDTO object with populated properties.
     * @throws ActionDTOModelResolverException If an error occurs while populating the properties.
     */
    private ActionDTO getResolvedActionDTOForGetOperation(ActionDTO actionDTO, Integer tenantId)
            throws ActionDTOModelResolverException {

        ActionDTOModelResolver actionDTOModelResolver =
                ActionDTOModelResolverFactory.getActionDTOModelResolver(actionDTO.getType());
        if (actionDTOModelResolver == null) {
            return actionDTO;
        }

        return actionDTOModelResolver.resolveForGetOperation(actionDTO, IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Get the ActionDTO with resolved updating properties that needs to be updated in the Action Management Service.
     *
     * @param updatingActionDTO Updating ActionDTO object.
     * @param existingActionDTO Existing ActionDTO object.
     * @param tenantId          Tenant ID.
     * @return ActionDTO object with resolved updating properties.
     * @throws ActionDTOModelResolverException If an error occurs while resolving the updating properties.
     */
    private ActionDTO getResolvedActionDTOForUpdateOperation(ActionDTO updatingActionDTO,
                                                             ActionDTO existingActionDTO, Integer tenantId)
            throws ActionDTOModelResolverException {

        ActionDTOModelResolver actionDTOModelResolver =
                ActionDTOModelResolverFactory.getActionDTOModelResolver(updatingActionDTO.getType());
        if (actionDTOModelResolver == null) {
            return updatingActionDTO;
        }

        return actionDTOModelResolver.resolveForUpdateOperation(updatingActionDTO, existingActionDTO,
                IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Delete the properties that are deleted in the Action Management Service.
     *
     * @param deletingActionDTO Deleting ActionDTO object.
     * @param tenantId          Tenant ID.
     * @throws ActionDTOModelResolverException If an error occurs while deleting the properties.
     */
    private void deleteProperties(ActionDTO deletingActionDTO, Integer tenantId)
            throws ActionDTOModelResolverException {

        ActionDTOModelResolver actionDTOModelResolver =
                ActionDTOModelResolverFactory.getActionDTOModelResolver(deletingActionDTO.getType());
        if (actionDTOModelResolver == null) {
            return;
        }

        actionDTOModelResolver.resolveForDeleteOperation(deletingActionDTO,
                IdentityTenantUtil.getTenantDomain(tenantId));
    }

    /**
     * Handle the ActionPropertyResolverClientException and throw the relevant ActionMgtClientException.
     *
     * @param throwable Throwable object.
     * @throws ActionMgtClientException If an error occurs while handling the ActionPropertyResolverClientException.
     */
    private static void handleActionPropertyResolverClientException(Throwable throwable)
            throws ActionMgtClientException {

        if (throwable instanceof ActionDTOModelResolverClientException) {
            ActionDTOModelResolverClientException clientException = (ActionDTOModelResolverClientException) throwable;
            throw new ActionMgtClientException(clientException.getMessage(), clientException.getDescription(),
                    ErrorMessage.ERROR_INVALID_ACTION_PROPERTIES.getCode());
        }
    }
}
