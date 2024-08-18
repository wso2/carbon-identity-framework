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

package org.wso2.carbon.identity.action.management;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.dao.impl.CacheBackedActionMgtDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Action management service.
 */
public class ActionManagementServiceImpl implements ActionManagementService {

    private static final Log LOG = LogFactory.getLog(ActionManagementServiceImpl.class);
    private static final ActionManagementService INSTANCE = new ActionManagementServiceImpl();
    private static final CacheBackedActionMgtDAO CACHE_BACKED_DAO =
            new CacheBackedActionMgtDAO(new ActionManagementDAOImpl());
    private static final ActionSecretProcessor ACTION_SECRET_PROCESSOR = new ActionSecretProcessor();

    private ActionManagementServiceImpl() {
    }

    public static ActionManagementService getInstance() {

        return INSTANCE;
    }

    @Override
    public Action addAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Adding Action for Action Type: %s.", actionType));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        // Check whether the maximum allowed actions per type is reached.
        validateMaxActionsPerType(resolvedActionType, tenantDomain);
        String generatedActionId = UUID.randomUUID().toString();
        return CACHE_BACKED_DAO.addAction(resolvedActionType, generatedActionId, action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving Actions for Action Type: %s.", actionType));
        }
        return CACHE_BACKED_DAO.getActionsByActionType(getActionTypeFromPath(actionType),
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action getActionByActionId(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving Actions for Action Type: %s.", actionType));
        }
        return CACHE_BACKED_DAO.getActionByActionId(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating Action for Action Type: %s and Action ID: %s.", actionType, actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        Action existingAction = checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        return CACHE_BACKED_DAO.updateAction(resolvedActionType, actionId, action, existingAction,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deleting Action for Action Type: %s and Action ID: %s", actionType, actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        Action action = checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        CACHE_BACKED_DAO.deleteAction(resolvedActionType, actionId, action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Activating Action for Action Type: %s and Action ID: %s.", actionType, actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        return CACHE_BACKED_DAO.activateAction(resolvedActionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Deactivating Action for Action Type: %s and Action ID: %s.", actionType,
                    actionId));
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        return CACHE_BACKED_DAO.deactivateAction(resolvedActionType, actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving Actions count per Type.");
        }
        return CACHE_BACKED_DAO.getActionsCountPerType(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action getActionByActionId(String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Retrieving Action of Action ID: %s", actionId));
        }
        return CACHE_BACKED_DAO.getActionByActionId(actionId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action updateActionEndpointAuthentication(String actionType, String actionId, AuthType authentication,
                                                     String tenantDomain) throws ActionMgtException {

        String resolvedActionType = getActionTypeFromPath(actionType);
        Action existingAction = checkIfActionExists(resolvedActionType, actionId, tenantDomain);
        if (existingAction.getEndpoint().getAuthentication().getType().equals(authentication.getType())) {
            // Only need to update the properties since the authType is same.
            return updateEndpointAuthenticationProperties(resolvedActionType, actionId, authentication, tenantDomain);
        } else {
            // Need to update the authentication type and properties.
            return updateEndpoint(resolvedActionType, actionId, existingAction, authentication, tenantDomain);
        }
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
                .orElseThrow(() -> ActionManagementUtil.handleClientException(
                        ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_TYPE));
    }

    /**
     * Validate the maximum actions per action type.
     *
     * @param actionType    Action Type.
     * @param tenantDomain  Tenant Domain.
     * @throws ActionMgtException If maximum actions per action type is reached.
     */
    private void validateMaxActionsPerType(String actionType, String tenantDomain) throws ActionMgtException {

        if (StringUtils.equals(Action.ActionTypes.AUTHENTICATION.getActionType(), actionType)) {
            return;
        }
        Map<String, Integer> actionsCountPerType = getActionsCountPerType(tenantDomain);
        if (actionsCountPerType.containsKey(actionType) &&
                actionsCountPerType.get(actionType) >= IdentityUtil.getMaximumActionsPerActionType()) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_MAXIMUM_ACTIONS_PER_ACTION_TYPE_REACHED);
        }
    }

    /**
     * Check if the action exists.
     *
     * @param actionType   Action Type.
     * @param actionId     Action ID.
     * @param tenantDomain Tenant Domain.
     * @throws ActionMgtException If the action does not exist.
     */
    private Action checkIfActionExists(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

        Action action = CACHE_BACKED_DAO.getActionByActionId(actionId, IdentityTenantUtil.getTenantId(tenantDomain));
        if (action == null || !actionType.equals(action.getType().name())) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_NO_ACTION_CONFIGURED_ON_GIVEN_ACTION_TYPE_AND_ID);
        }
        return action;
    }

    /**
     * Update the authentication type and properties of the action endpoint.
     *
     * @param actionType     Action Type.
     * @param actionId       Action Id.
     * @param existingAction Existing Action Information.
     * @param authentication Authentication Information to be updated.
     * @param tenantDomain   Tenant Domain.
     * @return Action response after update.
     * @throws ActionMgtException If an error occurs while updating action endpoint authentication.
     */
    private Action updateEndpoint(String actionType, String actionId, Action existingAction,
                                  AuthType authentication, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating endpoint authentication of Action Type: %s " +
                    "and Action ID: %s to AuthType: %s", actionType, actionId, authentication.getType().name()));
        }
        EndpointConfig endpoint = new EndpointConfig.EndpointConfigBuilder()
                .uri(existingAction.getEndpoint().getUri())
                .authentication(authentication).build();
        return CACHE_BACKED_DAO.updateActionEndpoint(actionType, actionId, endpoint,
                existingAction.getEndpoint().getAuthentication(), IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Update the authentication properties of the action endpoint.
     *
     * @param actionType     Action Type.
     * @param actionId       Action Id.
     * @param authentication Authentication Information to be updated.
     * @param tenantDomain   Tenant domain.
     * @return Action response after update.
     * @throws ActionMgtException If an error occurs while updating action endpoint authentication properties.
     */
    private Action updateEndpointAuthenticationProperties(String actionType, String actionId, AuthType authentication,
                                                          String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Updating endpoint authentication properties of Action Type: %s " +
                    "Action ID: %s and AuthType: %s", actionType, actionId, authentication.getType().name()));
        }
        return CACHE_BACKED_DAO.updateActionEndpointAuthProperties(actionId, authentication,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
