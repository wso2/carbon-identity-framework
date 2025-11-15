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

package org.wso2.carbon.identity.action.management.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.action.management.api.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.api.model.Action;
import org.wso2.carbon.identity.action.management.api.model.Authentication;
import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.action.management.core.cache.ActionCacheByType;
import org.wso2.carbon.identity.action.management.core.cache.ActionCacheEntry;
import org.wso2.carbon.identity.action.management.core.cache.ActionTypeCacheKey;

import java.util.List;
import java.util.Map;

/**
 * CacheBackedActionManagementService act as the caching layer for the Action Management Service.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.action.management.api.service.ActionManagementService",
                "service.scope=singleton"
        }
)
public class CacheBackedActionManagementService implements ActionManagementService {

    private static final CacheBackedActionManagementService INSTANCE = new CacheBackedActionManagementService();
    private static final Log LOG = LogFactory.getLog(CacheBackedActionManagementService.class);
    private static final ActionManagementServiceImpl ACTION_MGT_SERVICE = new ActionManagementServiceImpl();
    private final ActionCacheByType actionCacheByType;

    private CacheBackedActionManagementService() {

        actionCacheByType = ActionCacheByType.getInstance();
    }

    public static CacheBackedActionManagementService getInstance() {

        return INSTANCE;
    }

    @Override
    public Action addAction(String actionType, Action action, String tenantDomain) throws ActionMgtException {

        Action createdAction = ACTION_MGT_SERVICE.addAction(actionType, action, tenantDomain);
        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        return createdAction;
    }

    @Override
    public List<Action> getActionsByActionType(String actionType, String tenantDomain) throws ActionMgtException {

        ActionTypeCacheKey cacheKey = new ActionTypeCacheKey(actionType);
        ActionCacheEntry entry = actionCacheByType.getValueFromCache(cacheKey, tenantDomain);

        if (entry != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache entry found for Action Type " + actionType);
            }
            return entry.getActions();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cache entry not found for Action Type " + actionType + ". Fetching entry from DB.");
        }

        List<Action> actions = ACTION_MGT_SERVICE.getActionsByActionType(actionType, tenantDomain);

        if (actions != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry fetched from DB for Action Type " + actionType + ". Updating cache.");
            }
            actionCacheByType.addToCache(cacheKey, new ActionCacheEntry(actions), tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Entry for Action Type " + actionType + " not found in cache or DB.");
            }
        }

        return actions;
    }

    @Override
    public Action getActionByActionId(String actionType, String actionId, String tenantDomain)
            throws ActionMgtException {

        ActionTypeCacheKey cacheKey = new ActionTypeCacheKey(actionType);
        ActionCacheEntry entry = actionCacheByType.getValueFromCache(cacheKey, tenantDomain);

        /* If the entry for the given action type is not null, get the action list from cache and iterate to get the
         action by matching action id. */
        if (entry != null) {
            for (Action action : entry.getActions()) {
                if (StringUtils.equals(action.getId(), actionId)) {
                    LOG.debug("Action is found from the cache with action Id " + actionId);
                    return action;
                }
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Action is not found from the cache with action Id " + actionId + ". Fetching entry from DB.");
        }

        Action action = ACTION_MGT_SERVICE.getActionByActionId(actionType, actionId, tenantDomain);
        if (action != null) {
            updateCache(action, entry, cacheKey, tenantDomain);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action with action Id " + actionId + " is not found in cache or DB.");
            }
        }

        return action;
    }

    @Override
    public Action updateAction(String actionType, String actionId, Action action, String tenantDomain)
            throws ActionMgtException {

        Action updatedAction = ACTION_MGT_SERVICE.updateAction(actionType, actionId, action, tenantDomain);
        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        return updatedAction;
    }

    @Override
    public void deleteAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        ACTION_MGT_SERVICE.deleteAction(actionType, actionId, tenantDomain);
    }

    @Override
    public Action activateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        Action activatedAction = ACTION_MGT_SERVICE.activateAction(actionType, actionId, tenantDomain);
        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        return activatedAction;
    }

    @Override
    public Action deactivateAction(String actionType, String actionId, String tenantDomain) throws ActionMgtException {

        Action deactivatedAction = ACTION_MGT_SERVICE.deactivateAction(actionType, actionId, tenantDomain);
        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        return deactivatedAction;
    }

    @Override
    public Map<String, Integer> getActionsCountPerType(String tenantDomain) throws ActionMgtException {

        return ACTION_MGT_SERVICE.getActionsCountPerType(tenantDomain);
    }

    @Override
    public Action updateActionEndpointAuthentication(String actionType, String actionId, Authentication authentication,
                                                     String tenantDomain) throws ActionMgtException {

        Action updatedAction = ACTION_MGT_SERVICE.updateActionEndpointAuthentication(actionType, actionId,
                authentication, tenantDomain);
        actionCacheByType.clearCacheEntry(new ActionTypeCacheKey(actionType), tenantDomain);
        return updatedAction;
    }

    private void updateCache(Action action, ActionCacheEntry entry, ActionTypeCacheKey cacheKey, String tenantDomain) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Entry fetched from DB for Action Id " + action.getId() + ". Updating cache.");
        }
        /* If the entry for the given action type is not null, add the fetched action to the entry. Then, clear the
         cache and add the updated entry to the cache. If the entry is null, create a new cache entry.*/
        if (entry != null) {
            List<Action> actionsFromCache = entry.getActions();
            actionsFromCache.add(action);
            actionCacheByType.clearCacheEntry(cacheKey, tenantDomain);
            actionCacheByType.addToCache(cacheKey, new ActionCacheEntry(actionsFromCache), tenantDomain);
        }
    }
}
