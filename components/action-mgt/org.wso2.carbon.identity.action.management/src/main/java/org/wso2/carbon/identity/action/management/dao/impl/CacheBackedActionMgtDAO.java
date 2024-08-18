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
import org.wso2.carbon.identity.action.management.cache.ActionCacheByType;
import org.wso2.carbon.identity.action.management.cache.ActionCacheEntry;
import org.wso2.carbon.identity.action.management.cache.ActionTypeCacheKey;
import org.wso2.carbon.identity.action.management.dao.ActionManagementDAO;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.AuthType;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class implements the {@link ActionManagementDAO} interface.
 */
public class CacheBackedActionMgtDAO implements ActionManagementDAO {

    private static final Log LOG = LogFactory.getLog(CacheBackedActionMgtDAO.class);
    private final ActionCacheByType actionCacheByType;
    private final ActionManagementDAO actionManagementDAO;

    public CacheBackedActionMgtDAO(ActionManagementDAO actionManagementDAO) {

        this.actionManagementDAO = actionManagementDAO;
        actionCacheByType = ActionCacheByType.getInstance();
    }

    @Override
    public Action addAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.addAction(actionType, actionId, action, tenantId);
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, Integer tenantId) throws ActionMgtException {

        ActionTypeCacheKey cacheKey = new ActionTypeCacheKey(actionType);
        ActionCacheEntry entry = actionCacheByType.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for Action Type " + actionType);
            }
            return entry.getActions();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for Action Type " + actionType + ". Fetching entry from DB.");
        }

        List<Action> actions = actionManagementDAO.getActionsByActionType(actionType, tenantId);

        if (actions != null && !actions.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for Action Type " + actionType + ". Updating cache.");
            }
            actionCacheByType.addToCache(cacheKey, new ActionCacheEntry(actions), tenantId);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Action Type " + actionType + " not found in cache or DB.");
            }
        }
        return actions;
    }

    @Override
    public Action getActionByActionId(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        ActionTypeCacheKey cacheKey = new ActionTypeCacheKey(actionType);
        ActionCacheEntry entry = actionCacheByType.getValueFromCache(cacheKey, tenantId);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for Action Type " + actionType);
            }
            Action actionFromCache = entry.getActionByActionId(actionId);
            if (actionFromCache != null) return actionFromCache;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for Action Type " + actionType + ". Fetching entry from DB.");
        }

        Action action = actionManagementDAO.getActionByActionId(actionType, actionId, tenantId);

        if (action != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for Action Type " + actionType + ". Updating cache.");
            }
            if (entry != null) {
                entry.addAction(action);
            } else {
                actionCacheByType.addToCache(cacheKey, new ActionCacheEntry(
                        new ArrayList<>(Collections.singletonList(action))), tenantId);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Action Type " + actionType + " not found in cache or DB.");
            }
        }
        return action;
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action updatingAction, Action existingAction,
                               Integer tenantId) throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.updateAction(actionType, actionId, updatingAction, existingAction, tenantId);
    }

    @Override
    public void deleteAction(String actionType, String actionId, Action action, Integer tenantId)
            throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        actionManagementDAO.deleteAction(actionType, actionId, action, tenantId);
    }

    @Override
    public Action activateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.activateAction(actionType, actionId, tenantId);
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.deactivateAction(actionType, actionId, tenantId);
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(Integer tenantId) throws ActionMgtException {

        return actionManagementDAO.getActionsCountPerType(tenantId);
    }

    @Override
    public Action getActionByActionId(String actionId, Integer tenantId) throws ActionMgtException {

        return actionManagementDAO.getActionByActionId(actionId, tenantId);
    }

    @Override
    public Action updateActionEndpointAuthProperties(String actionId, AuthType authentication, int tenantId)
            throws ActionMgtException {

        return actionManagementDAO.updateActionEndpointAuthProperties(actionId, authentication, tenantId);
    }

    @Override
    public Action updateActionEndpoint(String actionType, String actionId, EndpointConfig endpoint,
                                       AuthType currentAuthentication, int tenantId)
            throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.updateActionEndpoint(actionType, actionId, endpoint, currentAuthentication,
                tenantId);
    }
}
