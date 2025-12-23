/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.api.constant.ErrorMessage;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.ActionDTO;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.api.service.ActionConverter;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOFacade;
import org.wso2.carbon.identity.action.management.internal.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.internal.util.ActionDTOBuilder;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementAuditLogger;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementConfig;
import org.wso2.carbon.identity.action.management.internal.util.ActionManagementExceptionHandler;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Action management service.
 */
public class ActionManagementServiceImpl implements ActionManagementService {

    private static final Log LOG = LogFactory.getLog(ActionManagementServiceImpl.class);
    private static final ActionManagementDAOFacade DAO_FACADE =
            new ActionManagementDAOFacade(new ActionManagementDAOImpl());
    private static final ActionManagementAuditLogger auditLogger = new ActionManagementAuditLogger();

    /**
     * Create a new action of the specified type in the given tenant.
     *
     * @param actionType   Action type.
     * @param action       Action creation model.
     * @param tenantDomain Tenant domain.
     * @return Created action object.
     * @throws ActionMgtException if an error occurred when creating the action.
     */
    @Override
    public Action addAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Adding Action for Action Type: %s.", actionType));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String resolvedActionType = getActionTypeFromPath(actionType);
        Action.ActionTypes castedActionType = Action.ActionTypes.valueOf(resolvedActionType);
        ActionValidatorFactory.getActionValidator(castedActionType).doPreAddActionValidations(
                castedActionType, ActionManagementConfig.getInstance().getLatestVersion(castedActionType), action);
        // Check whether the maximum allowed actions per type is reached.
        validateMaxActionsPerType(resolvedActionType, tenantDomain);
        String generatedActionId = UUID.randomUUID().toString();
        ActionDTO creatingActionDTO = buildActionDTOForCreation(resolvedActionType, generatedActionId, action);

        DAO_FACADE.addAction(creatingActionDTO, tenantId);
        ActionDTO createdActionDTO = DAO_FACADE.getActionByActionId(resolvedActionType, generatedActionId, tenantId);
        auditLogger.printAuditLog(ActionManagementAuditLogger.Operation.ADD, creatingActionDTO,
                createdActionDTO.getCreatedAt(), createdActionDTO.getUpdatedAt());

        return buildAction(resolvedActionType, createdActionDTO);
    }

    /**
     * Retrieve actions by the type in the given tenant.
     *
     * @param actionType   Action type.
     * @param tenantDomain Tenant domain.
     * @return A list of actions of the specified type.
     * @throws ActionMgtException if an error occurred while retrieving actions.
     */
    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving Actions for Action Type: %s.", actionType));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        List<ActionDTO> actionDTOS =  DAO_FACADE.getActionsByActionType(resolvedActionType,
                IdentityTenantUtil.getTenantId(tenantDomain));

        return actionDTOS.stream()
                .map(actionDTO -> buildAction(resolvedActionType, actionDTO))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve an action by action ID.
     *
     * @param actionType   Action type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @return Action object.
     * @throws ActionMgtException if an error occurred while retrieving the action.
     */
    @Override
    public Action getActionByActionId(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving Action of Action ID: %s", actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        ActionDTO actionDTO = DAO_FACADE.getActionByActionId(resolvedActionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));

        return buildAction(resolvedActionType, actionDTO);
    }

    /**
     * Update an action of specified type in the given tenant.
     * This method performs an HTTP PATCH operation.
     * Only the non-null and non-empty fields in the provided action model will be updated.
     * Null or empty fields will be ignored.
     *
     * @param actionType   Action type.
     * @param actionId     Action ID.
     * @param action       Action update model.
     * @param tenantDomain Tenant domain.
     * @return Updated action object.
     * @throws ActionMgtException if an error occurred while updating the action.
     */
    @Override
    public Action updateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating Action for Action Type: %s and Action ID: %s.", actionType, actionId));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        String resolvedActionType = getActionTypeFromPath(actionType);
        ActionDTO existingActionDTO = checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        Action.ActionTypes castedActionType = Action.ActionTypes.valueOf(resolvedActionType);
        ActionValidatorFactory.getActionValidator(castedActionType).doPreUpdateActionValidations(
                castedActionType, resolveActionVersionAtUpdating(action, existingActionDTO), action);
        ActionDTO updatingActionDTO = buildActionDTOForUpdate(resolvedActionType, actionId, action);

        DAO_FACADE.updateAction(updatingActionDTO, existingActionDTO, tenantId);
        ActionDTO updatedActionDTO = DAO_FACADE.getActionByActionId(resolvedActionType, actionId, tenantId);
        auditLogger.printAuditLog(ActionManagementAuditLogger.Operation.UPDATE, updatingActionDTO,
                null, updatedActionDTO.getUpdatedAt());

        return buildAction(resolvedActionType, updatedActionDTO);
    }

    private String resolveActionVersionAtUpdating(Action updatingAction, ActionDTO existingActionDTO) {

        String updatingActionVersion = updatingAction.getActionVersion();
        if (StringUtils.isNotBlank(updatingActionVersion)) {
            return updatingActionVersion;
        }
        return existingActionDTO.getActionVersion();
    }

    /**
     * Delete an action of the specified type in the given tenant.
     *
     * @param actionType   Action type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @throws ActionMgtException if an error occurred while deleting the action.
     */
    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting Action for Action Type: %s and Action ID: %s", actionType, actionId));
        }
        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        ActionDTO existingActionDTO = DAO_FACADE.getActionByActionId(getActionTypeFromPath(actionType), actionId,
                tenantId);
        if (existingActionDTO != null) {
            DAO_FACADE.deleteAction(existingActionDTO, tenantId);
            auditLogger.printAuditLog(ActionManagementAuditLogger.Operation.DELETE, actionType, actionId);
        }
    }

    /**
     * Activate a created action.
     *
     * @param actionType   Action type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @return Activated action.
     * @throws ActionMgtException If an error occurred while activating the action.
     */
    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating Action for Action Type: %s and Action ID: %s.", actionType, actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        ActionDTO activatedActionDTO = DAO_FACADE.activateAction(resolvedActionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        auditLogger.printAuditLog(ActionManagementAuditLogger.Operation.ACTIVATE, actionType, actionId,
                activatedActionDTO.getUpdatedAt());
        return buildBasicAction(activatedActionDTO);
    }

    /**
     * Deactivate an action.
     *
     * @param actionType   Action type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant domain.
     * @return deactivated action.
     * @throws ActionMgtException If an error occurred while deactivating the action.
     */
    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating Action for Action Type: %s and Action ID: %s.", actionType,
                    actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        ActionDTO deactivatedActionDTO = DAO_FACADE.deactivateAction(resolvedActionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        auditLogger.printAuditLog(ActionManagementAuditLogger.Operation.DEACTIVATE, actionType, actionId,
                deactivatedActionDTO.getUpdatedAt());
        return buildBasicAction(deactivatedActionDTO);
    }

    /**
     * Retrieve number of actions per each type in a given tenant.
     *
     * @param tenantDomain Tenant domain.
     * @return A map of action count against action type.
     * @throws ActionMgtException if an error occurred while retrieving actions.
     */
    @Override
    public Map<String, Integer> getActionsCountPerType(String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving Actions count per Type.");
        }
        return DAO_FACADE.getActionsCountPerType(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Update endpoint authentication of a given action.
     *
     * @param actionType     Action type.
     * @param actionId       Action ID.
     * @param authentication Authentication Information to be updated.
     * @param tenantDomain   Tenant domain.
     * @return Updated action.
     * @throws ActionMgtException if an error occurred while updating endpoint authentication information.
     */
    @Override
    public Action updateActionEndpointAuthentication(String actionType, String actionId, Authentication authentication,
                                                     String tenantDomain) throws ActionMgtException {

        Action updatingAction = new Action.ActionRequestBuilder()
                .endpoint(new EndpointConfig.EndpointConfigBuilder()
                        .authentication(authentication)
                        .build())
                .build();

        return updateAction(actionType, actionId, updatingAction, tenantDomain);
    }

    /**
     * Get Action Type from path.
     *
     * @param actionType Action Type.
     * @return Action Type.
     * @throws ActionMgtClientException If an invalid Action Type is given.
     */
    private String getActionTypeFromPath(String actionType) throws ActionMgtClientException {

        return Arrays.stream(Action.ActionTypes.values())
                .filter(type -> type.getPathParam().equals(actionType))
                .map(Action.ActionTypes::getActionType)
                .findFirst()
                .orElseThrow(() ->
                        ActionManagementExceptionHandler.handleClientException(ErrorMessage.ERROR_INVALID_ACTION_TYPE));
    }

    /**
     * Validate the maximum actions per action type.
     *
     * @param actionType    Action Type.
     * @param tenantDomain  Tenant Domain.
     * @throws ActionMgtException If maximum actions per action type is reached.
     */
    private void validateMaxActionsPerType(String actionType, String tenantDomain) throws ActionMgtException {

        // In-flow actions are not limited by the maximum actions per action type; eg: AUTHENTICATION action type.
        if (Action.ActionTypes.Category.IN_FLOW.equals(Action.ActionTypes.valueOf(actionType).getCategory())) {
            return;
        }
        Map<String, Integer> actionsCountPerType = getActionsCountPerType(tenantDomain);
        if (actionsCountPerType.containsKey(actionType) &&
                actionsCountPerType.get(actionType) >= IdentityUtil.getMaximumActionsPerActionType()) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_MAXIMUM_ACTIONS_PER_ACTION_TYPE_REACHED);
        }
    }

    /**
     * Check if the action exists.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant Domain.
     * @return ActionDTO if the action exists.
     * @throws ActionMgtException If the action does not exist.
     */
    private ActionDTO checkIfActionExists(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

        ActionDTO actionDTO = DAO_FACADE.getActionByActionId(actionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
        if (actionDTO == null || !actionType.equals(actionDTO.getType().name())) {
            throw ActionManagementExceptionHandler.handleClientException(
                    ErrorMessage.ERROR_NO_ACTION_CONFIGURED_ON_GIVEN_ACTION_TYPE_AND_ID);
        }

        return actionDTO;
    }

    /**
     * For action creation operation, builds an `ActionDTO` object based on the provided action type, action ID, and
     * action model. This method resolves the action type and status, applies necessary transformations, and constructs
     * the `ActionDTO` object using the provided data.
     *
     * @param actionType The type of the action.
     * @param actionId The unique identifier for the action.
     * @param action The action model containing details for the action.
     * @return The constructed `ActionDTO` object.
     */
    private ActionDTO buildActionDTOForCreation(String actionType, String actionId, Action action)
            throws ActionMgtServerException {

        Action.ActionTypes resolvedActionType = Action.ActionTypes.valueOf(actionType);
        Action.Status resolvedStatus = resolvedActionType.getCategory() == Action.ActionTypes.Category.IN_FLOW ?
                Action.Status.ACTIVE : Action.Status.INACTIVE;

        String actionVersion = ActionManagementConfig.getInstance().getLatestVersion(resolvedActionType);

        ActionConverter actionConverter =
                ActionConverterFactory.getActionConverter(Action.ActionTypes.valueOf(actionType));
        if (actionConverter != null) {
            ActionDTO actionDTO = actionConverter.buildActionDTO(action);

            return new ActionDTOBuilder(actionDTO)
                    .id(actionId)
                    .type(resolvedActionType)
                    .status(resolvedStatus)
                    .actionVersion(actionVersion)
                    .build();
        }

        return new ActionDTOBuilder(action)
                .id(actionId)
                .type(resolvedActionType)
                .status(resolvedStatus)
                .actionVersion(actionVersion)
                .build();
    }

    /**
     * For action update operation, builds an `ActionDTO` object based on the provided action type, action ID, and
     * action model. This method resolves the action type and status, applies necessary transformations, and constructs
     * the `ActionDTO` object using the provided data.
     *
     * @param actionType The type of the action.
     * @param actionId The unique identifier for the action.
     * @param action The action model containing details for the action.
     * @return The constructed `ActionDTO` object.
     */
    private ActionDTO buildActionDTOForUpdate(String actionType, String actionId, Action action) {

        Action.ActionTypes resolvedActionType = Action.ActionTypes.valueOf(actionType);
        String actionVersion = action.getActionVersion();

        ActionConverter actionConverter =
                ActionConverterFactory.getActionConverter(Action.ActionTypes.valueOf(actionType));
        if (actionConverter != null) {
            ActionDTO actionDTO = actionConverter.buildActionDTO(action);

            return new ActionDTOBuilder(actionDTO)
                    .id(actionId)
                    .type(resolvedActionType)
                    .actionVersion(actionVersion)
                    .build();
        }

        return new ActionDTOBuilder(action)
                .id(actionId)
                .type(resolvedActionType)
                .actionVersion(actionVersion)
                .build();
    }

    private Action buildBasicAction(ActionDTO actionDTO) {

        if (actionDTO == null) {
            return null;
        }

        return new Action.ActionResponseBuilder()
                .id(actionDTO.getId())
                .type(actionDTO.getType())
                .name(actionDTO.getName())
                .description(actionDTO.getDescription())
                .status(actionDTO.getStatus())
                .actionVersion(actionDTO.getActionVersion())
                .createdAt(actionDTO.getCreatedAt())
                .updatedAt(actionDTO.getUpdatedAt())
                .build();
    }

    private Action buildAction(String actionType, ActionDTO actionDTO) {

        if (actionDTO == null) {
            return null;
        }

        ActionConverter actionConverter =
                ActionConverterFactory.getActionConverter(Action.ActionTypes.valueOf(actionType));
        if (actionConverter != null) {
            return actionConverter.buildAction(actionDTO);
        }

        return new Action.ActionResponseBuilder()
                .id(actionDTO.getId())
                .type(actionDTO.getType())
                .name(actionDTO.getName())
                .description(actionDTO.getDescription())
                .status(actionDTO.getStatus())
                .actionVersion(actionDTO.getActionVersion())
                .createdAt(actionDTO.getCreatedAt())
                .updatedAt(actionDTO.getUpdatedAt())
                .endpoint(actionDTO.getEndpoint())
                .rule(actionDTO.getActionRule())
                .build();
    }
}
