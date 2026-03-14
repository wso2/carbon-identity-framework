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
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;
import org.wso2.carbon.identity.debug.framework.util.DebugSessionUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic DB-backed cache for debug data.
 * Unified caching layer for:
 * - Intermediate debug context (passed from initiator to processor via state parameter)
 * - Final debug results (persisted for API retrieval)
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
    public void put(String key, Map<String, Object> value) throws DebugFrameworkServerException {

        if (key == null || value == null) {
            return;
        }
        try {
            DebugSessionData sessionData = new DebugSessionData();
            sessionData.setDebugId(key);
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
        } catch (DebugFrameworkServerException e) {
            throw e;
        } catch (IOException e) {
            LOG.error("Error persisting debug session to DB: " + e.getMessage(), e);
            throw new DebugFrameworkServerException("Error persisting debug session to DB", e);
        }
    }

    /**
     * Stores a DebugContext in the session cache.
     *
     * @param key     Cache key (typically state parameter).
     * @param context DebugContext to cache.
     */
    public void put(String key, DebugContext context) throws DebugFrameworkServerException {

        if (key == null || context == null) {
            return;
        }
        
        // Convert DebugContext to Map for storage.
        Map<String, Object> contextMap = new HashMap<>(context.getProperties());
        
        // Add metadata fields.
        if (context.getConnectionId() != null) {
            contextMap.put("connectionId", context.getConnectionId());
        }
        if (context.getResourceType() != null) {
            contextMap.put("resourceType", context.getResourceType());
        }

        put(key, contextMap);
    }

    /**
     * Retrieves a debug context map from the session cache.
     *
     * @param key Cache key.
     * @return Debug context map or empty map if not found.
     */
    public Map<String, Object> get(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return new HashMap<>();
        }
        try {
            DebugSessionData data = debugSessionDAO.getDebugSession(key);
            if (data != null && data.getSessionData() != null) {
                Object deserializedObject = DebugSessionUtil.deserializeObject(data.getSessionData());
                if (deserializedObject instanceof Map<?, ?>) {
                    Map<String, Object> contextData = new HashMap<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) deserializedObject).entrySet()) {
                        if (entry.getKey() instanceof String) {
                            contextData.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    return contextData;
                }
                LOG.warn("Unexpected debug session payload type for key: " + key);
            }
        } catch (DebugFrameworkServerException e) {
            throw e;
        } catch (IOException e) {
            LOG.error("Error retrieving debug session from DB: " + e.getMessage(), e);
            throw new DebugFrameworkServerException("Error retrieving debug session from DB", e);
        } catch (ClassNotFoundException e) {
            LOG.error("Error deserializing debug session: " + e.getMessage(), e);
            throw new DebugFrameworkServerException("Error deserializing debug session", e);
        }
        return new HashMap<>();
    }

    /**
     * Removes a debug context from the session cache.
     *
     * @param key Cache key.
     * @return Previously cached context map or empty map if not found.
     */
    public Map<String, Object> remove(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return new HashMap<>();
        }
        Map<String, Object> value = get(key);
        debugSessionDAO.deleteDebugSession(key);
        return value;
    }

    /**
     * Caches a debug result as JSON string (for final results).
     * Generic method supporting both intermediate context and final results.
     *
     * @param key    Cache key (typically state parameter).
     * @param result JSON-serialized result to cache.
     */
    public void putResult(String key, String result) throws DebugFrameworkServerException {

        putResult(key, result, null, null);
    }

    /**
     * Caches a debug result with metadata.
     *
     * @param key          Cache key.
     * @param result       JSON-serialized result.
     * @param resourceType Type of debug resource (IDP, FRAUD_DETECTION).
     * @param connectionId   Identifier of the resource.
     */
    public void putResult(String key, String result, String resourceType, String connectionId)
            throws DebugFrameworkServerException {

        if (key == null || result == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cache.putResult: key and result cannot be null");
            }
            return;
        }

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(key);
        sessionData.setResultJson(result);
        sessionData.setStatus(DebugFrameworkConstants.SESSION_STATUS_COMPLETED);
        sessionData.setCreatedTime(System.currentTimeMillis());
        sessionData.setExpiryTime(System.currentTimeMillis() + SESSION_TTL_MS);
        sessionData.setResourceType(resourceType);
        sessionData.setConnectionId(connectionId);

        debugSessionDAO.upsertDebugSession(sessionData);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug result cached for key: " + key);
        }
    }

    /**
     * Retrieves a cached debug result as JSON string.
     *
     * @param key Cache key.
     * @return JSON result or null if not found.
     */
    public String getResult(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return null;
        }
        try {
            DebugSessionData data = debugSessionDAO.getDebugSession(key);
            if (data != null && data.getResultJson() != null) {
                return data.getResultJson();
            }
        } catch (DebugFrameworkServerException e) {
            throw e;
        }
        return null;
    }

    /**
     * Removes a cached debug result.
     *
     * @param key Cache key.
     */
    public void removeResult(String key) throws DebugFrameworkServerException {

        if (key != null) {
            debugSessionDAO.deleteDebugSession(key);
        }
    }
}
