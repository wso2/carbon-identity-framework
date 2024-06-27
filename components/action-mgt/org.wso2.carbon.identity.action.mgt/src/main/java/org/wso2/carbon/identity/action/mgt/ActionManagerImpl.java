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

package org.wso2.carbon.identity.action.mgt;

import org.wso2.carbon.identity.action.mgt.dao.impl.ActionManagementDAOImpl;
import org.wso2.carbon.identity.action.mgt.dao.impl.CacheBackedActionMgtDAO;
import org.wso2.carbon.identity.action.mgt.model.Action;
import org.wso2.carbon.identity.action.mgt.model.ActionType;
import org.wso2.carbon.identity.action.mgt.util.ActionManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;

import java.util.List;

/**
 * Action management service.
 */
public class ActionManagerImpl implements ActionManager {

    private static final ActionManager INSTANCE = new ActionManagerImpl();

    private static final CacheBackedActionMgtDAO CACHE_BACKED_DAO =
            new CacheBackedActionMgtDAO(new ActionManagementDAOImpl());

    private ActionManagerImpl() {
    }

    public static ActionManager getInstance() {

        return INSTANCE;
    }

    @Override
    public Action addAction(String actionType, Action action, String tenantDomain)
            throws ActionMgtException {

        return CACHE_BACKED_DAO.addAction(ActionManagementUtil.getvalidatedActionType(actionType), action,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.getActionsByActionType(ActionManagementUtil.getvalidatedActionType(actionType),
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action actionUpdateModel,
                               String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.updateAction(ActionManagementUtil.getvalidatedActionType(actionType), actionId,
                actionUpdateModel, IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        CACHE_BACKED_DAO.deleteAction(ActionManagementUtil.getvalidatedActionType(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.activateAction(ActionManagementUtil.getvalidatedActionType(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.deactivateAction(ActionManagementUtil.getvalidatedActionType(actionType), actionId,
                IdentityTenantUtil.getTenantId(tenantDomain));
    }

    @Override
    public List<ActionType> getActionTypes(String tenantDomain) throws ActionMgtException {

        return CACHE_BACKED_DAO.getActionTypes(IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
