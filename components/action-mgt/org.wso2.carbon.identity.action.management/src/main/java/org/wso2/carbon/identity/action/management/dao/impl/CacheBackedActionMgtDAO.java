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
import org.wso2.carbon.identity.action.management.model.Authentication;
import org.wso2.carbon.identity.action.management.model.EndpointConfig;

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
    public Action getActionByActionId(String actionType, String actionId, Integer tenantId) throws ActionMgtException {

        ActionTypeCacheKey cacheKey = new ActionTypeCacheKey(actionType);
        ActionCacheEntry entry = actionCacheByType.getValueFromCache(cacheKey, tenantId);

        /* If the entry for the given action type is not null, get the action list from cache and iterate to get the
         action by matching action id. */
        if (entry != null) {
            for (Action action: entry.getActions()) {
                if (StringUtils.equals(action.getId(), actionId)) {
                    LOG.debug("Action is found from the cache with action Id " + actionId);
                    return action;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Action is not found from the cache with action Id " + actionId + ". Fetching entry from DB.");
        }

        Action action = actionManagementDAO.getActionByActionId(actionType, actionId, tenantId);
        if (action != null) {
            updateCache(action, entry, cacheKey, tenantId);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action with action Id " + actionId + " is not found in cache or DB.");
            }
        }

        return action;
    }

    @Override
    public Action updateActionEndpointAuthProperties(String actionType, String actionId, Authentication authentication,
                                                     int tenantId) throws ActionMgtException {

        return actionManagementDAO.updateActionEndpointAuthProperties(actionType, actionId, authentication, tenantId);
    }

    @Override
    public Action updateActionEndpoint(String actionType, String actionId, EndpointConfig endpoint,
                                       Authentication currentAuthentication, int tenantId)
            throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantId);
        return actionManagementDAO.updateActionEndpoint(actionType, actionId, endpoint, currentAuthentication,
                tenantId);
    }

    private void updateCache(Action action, ActionCacheEntry entry, ActionTypeCacheKey cacheKey, int tenantId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Entry fetched from DB for Action Id " + action.getId() + ". Updating cache.");
        }
        /* If the entry for the given action type is not null, add the fetched action to the entry. Then, clear the
         cache and add the updated entry to the cache. If the entry is null, create a new cache entry.*/
        if (entry != null) {
            List<Action> actionsFromCache = entry.getActions();
            actionsFromCache.add(action);
            actionCacheByType.clearCacheEntry(cacheKey, tenantId);
            actionCacheByType.addToCache(cacheKey, new ActionCacheEntry(actionsFromCache), tenantId);
        }
    }
}
