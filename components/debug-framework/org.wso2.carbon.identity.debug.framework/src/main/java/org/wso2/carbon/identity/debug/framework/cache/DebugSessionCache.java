/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;
import org.wso2.carbon.identity.debug.framework.util.DebugSessionUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DB-backed session cache for debug contexts.
 * Used to pass data from DebugExecutor (initiator) to DebugProcessor.
 * Thread-safe singleton that delegates persistence to the framework's
 * DebugSessionDAO.
 */
public final class DebugSessionCache {

    private static final Log LOG = LogFactory.getLog(DebugSessionCache.class);
    private static final DebugSessionCache INSTANCE = new DebugSessionCache();
    private static final long SESSION_TTL_MS = DebugFrameworkConstants.CACHE_EXPIRY_MINUTES * 60 * 1000L;

    private final DebugSessionDAO debugSessionDAO = new DebugSessionDAOImpl();

    private DebugSessionCache() {

        // Singleton — prevent external instantiation.
    }

    public static DebugSessionCache getInstance() {

        return INSTANCE;
    }

    /**
     * Stores a debug context map in the session cache.
     *
     * @param key   Cache key (typically state parameter).
     * @param value Debug context map to cache.
     */
    public void put(String key, Map<String, Object> value) {

        if (key == null || value == null) {
            return;
        }
        try {
            DebugSessionData sessionData = new DebugSessionData();
            sessionData.setSessionId(key);
            sessionData.setStatus(DebugFrameworkConstants.SESSION_STATUS_PENDING);
            sessionData.setCreatedTime(System.currentTimeMillis());
            sessionData.setExpiryTime(System.currentTimeMillis() + SESSION_TTL_MS);

            Serializable serializableValue = (value instanceof Serializable)
                    ? (Serializable) value
                    : new HashMap<>(value);
            sessionData.setSessionData(DebugSessionUtil.serializeObject(serializableValue));

            debugSessionDAO.createDebugSession(sessionData);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug context cached for key: " + key);
            }
        } catch (Exception e) {
            LOG.error("Error persisting debug session to DB: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a debug context map from the session cache.
     *
     * @param key Cache key.
     * @return Debug context map or null if not found.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {

        if (key == null) {
            return null;
        }
        try {
            DebugSessionData data = debugSessionDAO.getDebugSession(key);
            if (data != null && data.getSessionData() != null) {
                return (Map<String, Object>) DebugSessionUtil.deserializeObject(data.getSessionData());
            }
        } catch (Exception e) {
            LOG.error("Error retrieving debug session from DB: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Removes a debug context from the session cache.
     *
     * @param key Cache key.
     * @return Previously cached context map or null.
     */
    public Map<String, Object> remove(String key) {

        if (key == null) {
            return null;
        }
        try {
            Map<String, Object> value = get(key);
            debugSessionDAO.deleteDebugSession(key);
            return value;
        } catch (Exception e) {
            LOG.error("Error removing debug session from DB: " + e.getMessage(), e);
        }
        return null;
    }
}
