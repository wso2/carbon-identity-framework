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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.dao.impl.CacheBackedActionMgtDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
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

    private ActionManagementServiceImpl() {
    }

    public static ActionManagementService getInstance() {

        return INSTANCE;
    }

    @Override
    public Action addAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding Action for Action Type: " + actionType);
        }
        String resolvedActionType = getActionTypeFromPath(actionType);
        String generatedActionId = UUID.randomUUID().toString();
        // Check whether the maximum allowed actions per type is reached.
        validateMaxActionsPerType(resolvedActionType, tenantDomain);
        return CACHE_BACKED_DAO.addAction(resolvedActionType, generatedActionId, action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving Actions for Action Type: " + actionType);
        }
        return CACHE_BACKED_DAO.getActionsByActionType(getActionTypeFromPath(actionType),
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating Action for Action Type: " + actionType + " and Action Id: " + actionId);
        }
        checkIfActionExists(actionId, tenantDomain);
        return CACHE_BACKED_DAO.updateAction(getActionTypeFromPath(actionType), actionId, action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting Action for Action Type: " + actionType + " and Action Id: " + actionId);
        }
        checkIfActionExists(actionId, tenantDomain);
        CACHE_BACKED_DAO.deleteAction(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Activating Action for Action Type: " + actionType + " and Action Id: " + actionId);
        }
        checkIfActionExists(actionId, tenantDomain);
        return CACHE_BACKED_DAO.activateAction(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deactivating Action for Action Type: " + actionType + " and Action Id: " + actionId);
        }
        checkIfActionExists(actionId, tenantDomain);
        return CACHE_BACKED_DAO.deactivateAction(getActionTypeFromPath(actionType), actionId,
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
            LOG.debug("Retrieving Action of Action Id: " + actionId);
        }
        return CACHE_BACKED_DAO.getActionByActionId(actionId, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    /**
     * Get Action Type from path.
     *
     * @param actionType Action Type.
     * @return Action Type.
     * @throws ActionMgtClientException If an invalid Action Type is given.
     */
    private static String getActionTypeFromPath(String actionType) throws ActionMgtClientException {

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
     * @param actionId     Action ID.
     * @param tenantDomain Tenant Domain.
     * @throws ActionMgtException If the action does not exist.
     */
    private void checkIfActionExists(String actionId, String tenantDomain) throws ActionMgtException {

        if (CACHE_BACKED_DAO.getActionByActionId(actionId, IdentityTenantUtil.getTenantId(tenantDomain)) == null) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_NO_ACTION_CONFIGURED_ON_GIVEN_ID);
        }
    }
}
