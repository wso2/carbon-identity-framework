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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.DebugFrameworkConstants;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;
import org.wso2.carbon.identity.debug.framework.util.DebugFrameworkUtils;

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
    private static final String SESSION_TTL_MINUTES_PROPERTY = "debug.session.ttl.minutes";
    private static final long SESSION_TTL_MS = resolveSessionTtlMs();
    private static final DebugSessionStore INSTANCE = new DebugSessionStore();

    private final DebugSessionDAO debugSessionDAO = new DebugSessionDAOImpl();

    private DebugSessionStore() {

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
            long now = System.currentTimeMillis();
            DebugSessionData sessionData = new DebugSessionData();
            sessionData.setDebugId(key);
            sessionData.setStatus(DebugSessionData.SessionStatus.PENDING);
            sessionData.setCreatedTime(now);
            sessionData.setExpiryTime(now + SESSION_TTL_MS);
            sessionData.setSessionData(DebugFrameworkUtils.getObjectMapper().writeValueAsBytes(value));

            debugSessionDAO.createDebugSession(sessionData);

        } catch (IOException e) {
            String errorMsg = "Error persisting debug session: " + key + " to DB";
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

        Map<String, Object> contextMap = new HashMap<>(context.getProperties());
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
                return DebugFrameworkUtils.getObjectMapper()
                        .readValue(data.getSessionData(), DebugFrameworkUtils.getMapTypeReference());
            }
        } catch (IOException e) {
            String errorMsg = "Error retrieving debug session: " + key + " from DB";
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

        long now = System.currentTimeMillis();
        DebugSessionData sessionData = new DebugSessionData();
        sessionData.setDebugId(key);
        sessionData.setResultJson(result);
        sessionData.setStatus(DebugSessionData.SessionStatus.COMPLETED);
        sessionData.setCreatedTime(now);
        sessionData.setExpiryTime(now + SESSION_TTL_MS);

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
     * Resolves the session TTL in milliseconds. Reads {@code debug.session.ttl.minutes} from system
     * properties or the {@code DEBUG_SESSION_TTL_MINUTES} environment variable, falling back to
     * {@link DebugFrameworkConstants#CACHE_EXPIRY_MINUTES} when unset or invalid.
     */
    private static long resolveSessionTtlMs() {

        String configured = System.getProperty(SESSION_TTL_MINUTES_PROPERTY,
                System.getenv("DEBUG_SESSION_TTL_MINUTES"));
        int minutes = DebugFrameworkConstants.CACHE_EXPIRY_MINUTES;
        if (configured != null && !configured.trim().isEmpty()) {
            try {
                int parsed = Integer.parseInt(configured.trim());
                if (parsed > 0) {
                    minutes = parsed;
                } else {
                    LOG.warn("Invalid debug session TTL configured: " + parsed + ". Using default: " + minutes);
                }
            } catch (NumberFormatException e) {
                LOG.warn("Failed to parse debug session TTL: " + configured + ". Using default: " + minutes);
            }
        }
        return minutes * 60_000L;
    }
}
