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

import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.management.dao.impl.CacheBackedActionMgtDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.List;
import java.util.Map;


/**
 * Action management service.
 */
public class ActionManagementServiceImpl implements ActionManagementService {

    private static final ActionManagementService INSTANCE = new ActionManagementServiceImpl();

    private static final CacheBackedActionMgtDAO CACHE_BACKED_DAO =
            new CacheBackedActionMgtDAO(new ActionManagementDAOImpl());

    private ActionManagementServiceImpl() {
    }

    public static ActionManagementService getInstance() {

        return INSTANCE;
    }

    @Override
    public Action addAction(String actionType, Action action, String tenantDomain)
            throws ActionMgtException {

        String resolvedActionType = getActionTypeFromPath(actionType);
        // Check whether the maximum allowed actions per type is reached.
        validateMaxActionsPerType(resolvedActionType, tenantDomain);
        return CACHE_BACKED_DAO.addAction(resolvedActionType, action, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.getActionsByActionType(getActionTypeFromPath(actionType),
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action action,
                               String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.updateAction(getActionTypeFromPath(actionType), actionId, action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        CACHE_BACKED_DAO.deleteAction(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.activateAction(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.deactivateAction(getActionTypeFromPath(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.getActionsCountPerType(IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action getActionByActionId(String actionId, String tenantDomain) throws ActionMgtException {

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

        for (Action.ActionTypes type: Action.ActionTypes.values()) {

            if (type.getPathParam().equals(actionType)) {
                return type.getActionType();
            }
        }
        throw ActionManagementUtil.handleClientException(ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_TYPE);
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
        if (actionsCountPerType.get(actionType) != null &&
                actionsCountPerType.get(actionType) >= IdentityUtil.getMaximumActionsPerActionType()) {
            throw ActionManagementUtil.handleClientException(
                    ActionMgtConstants.ErrorMessages.ERROR_MAXIMUM_ACTIONS_PER_ACTION_TYPE_REACHED);
        }
    }
}
