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

package org.wso2.carbon.identity.debug.framework.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic DB-backed store for debug data.
 * Unified persistence layer for:
 * - Intermediate debug context (passed from initiator to processor via state parameter)
 * - Final debug results (persisted for API retrieval)
 */
public final class DebugSessionStore {

    private static final Log LOG = LogFactory.getLog(DebugSessionStore.class);
    private static final DebugSessionStore INSTANCE = new DebugSessionStore();
    private static final long SESSION_TTL_MS = DebugFrameworkConstants.CACHE_EXPIRY_MINUTES * 60 * 1000L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() { };

    private final DebugSessionDAO debugSessionDAO = new DebugSessionDAOImpl();

    private DebugSessionStore() {

        // Singleton — prevent external instantiation.
    }

    public static DebugSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Stores a debug context map in the session store.
     *
     * @param key   Store key (typically state parameter).
     * @param value Debug context map to persist.
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

            byte[] serializedValue = OBJECT_MAPPER.writeValueAsBytes(value);
            sessionData.setSessionData(serializedValue);

            debugSessionDAO.createDebugSession(sessionData);

        } catch (IOException e) {
            String errorMsg = "Error persisting debug session to DB";
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
    }

    /**
     * Stores a DebugContext in the session store.
     *
     * @param key     Store key (typically state parameter).
     * @param context DebugContext to persist.
     */
    public void put(String key, DebugContext context) throws DebugFrameworkServerException {

        if (key == null || context == null) {
            return;
        }
        
        // Convert DebugContext to Map for storage.
        Map<String, Object> contextMap = new HashMap<>(context.getProperties());
        
        // Add metadata fields.
        Object connectionId = context.getProperty("connectionId");
        if (connectionId != null) {
            contextMap.put("connectionId", connectionId);
        }
        if (context.getResourceType() != null) {
            contextMap.put("resourceType", context.getResourceType());
        }

        put(key, contextMap);
    }

    /**
     * Retrieves a debug context map from the session store.
     *
     * @param key Store key.
     * @return Debug context map or empty map if not found.
     */
    public Map<String, Object> get(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return new HashMap<>();
        }
        try {
            DebugSessionData data = debugSessionDAO.getDebugSession(key);
            if (data != null && data.getSessionData() != null) {
                return OBJECT_MAPPER.readValue(data.getSessionData(), MAP_TYPE);
            }
        } catch (IOException e) {
            String errorMsg = "Error retrieving debug session from DB";
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
        return new HashMap<>();
    }

    /**
     * Atomically removes a debug context from the session store.
     * Fetches the record first, then deletes. If deletion fails, the fetched value
     * is not returned to prevent state inconsistency. This ensures that if the caller
     * receives data, it has been deleted from the store.
     *
     * @param key Store key.
     * @return Previously cached context map or empty map if not found or deletion failed.
     */
    public Map<String, Object> remove(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return new HashMap<>();
        }
        try {
            DebugSessionData data = debugSessionDAO.deleteAndReturnDebugSession(key);
            if (data != null && data.getSessionData() != null) {
                return OBJECT_MAPPER.readValue(data.getSessionData(), MAP_TYPE);
            }
        } catch (IOException e) {
            String errorMsg = "Error retrieving debug session for removal";
            LOG.error(errorMsg, e);
            throw new DebugFrameworkServerException(errorMsg, e);
        }
        return new HashMap<>();
    }

    /**
     * Stores a debug result as JSON string for final results.
     *
     * @param key    Store key (typically state parameter).
     * @param result JSON-serialized result to persist.
     */
    public void putResult(String key, String result) throws DebugFrameworkServerException {

        if (key == null || result == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Store.putResult: key and result cannot be null");
            }
            return;
        }

        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(key);
        sessionData.setResultJson(result);
        sessionData.setStatus(DebugFrameworkConstants.SESSION_STATUS_COMPLETED);
        sessionData.setCreatedTime(System.currentTimeMillis());
        sessionData.setExpiryTime(System.currentTimeMillis() + SESSION_TTL_MS);

        debugSessionDAO.upsertDebugSession(sessionData);
    }

    /**
     * Retrieves a stored debug result as JSON string.
     *
     * @param key Store key.
     * @return JSON result or null if not found.
     * @throws DebugFrameworkServerException If database access fails.
     */
    public String getResult(String key) throws DebugFrameworkServerException {

        if (key == null) {
            return null;
        }
        DebugSessionData data = debugSessionDAO.getDebugSession(key);
        if (data != null && data.getResultJson() != null) {
            return data.getResultJson();
        }
        return null;
    }

    /**
     * Removes a stored debug result.
     *
     * @param key Store key.
     */
    public void removeResult(String key) throws DebugFrameworkServerException {

        if (key != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing debug result for key: " + key);
            }
            debugSessionDAO.deleteDebugSession(key);
        }
    }
}
