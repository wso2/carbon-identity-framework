/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.core.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache for debug callback results.
 * Stores JSON-serialized debug results keyed by state parameter.
 * Results are cached temporarily during the debug flow and retrieved
 * by the debug endpoint to display to users.
 * Uses ConcurrentHashMap for better performance and thread-safety.
 * Results are automatically cleaned up after TTL expiry (default: 15 minutes).
 */
/**
 * Persistence wrapper for debug callback results.
 * Delegates to DebugSessionDAO for DB storage.
 * Replaces the previous in-memory cache.
 */
public final class DebugResultCache {

    private static final Log LOG = LogFactory.getLog(DebugResultCache.class);
    // Use the DAO implementation directly (in a real OSGi env, this should be a
    // service reference)
    private static final org.wso2.carbon.identity.debug.framework.core.dao.DebugSessionDAO DAO = new org.wso2.carbon.identity.debug.framework.core.dao.impl.DebugSessionDAOImpl();

    public static void add(String state, String result) {

        if (state == null || result == null) {
            LOG.warn("Cache.add: state and result cannot be null");
            return;
        }

        try {
            org.wso2.carbon.identity.debug.framework.model.DebugSessionData sessionData = new org.wso2.carbon.identity.debug.framework.model.DebugSessionData();
            sessionData.setSessionId(state);
            sessionData.setResultJson(result);
            sessionData.setStatus("COMPLETED"); // Mark as completed

            // We update the existing session with the partial result data
            DAO.updateDebugSession(sessionData);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug result persisted to DB for state: " + state);
            }
        } catch (Exception e) {
            LOG.error("Error persisting debug result: " + e.getMessage(), e);
        }
    }

    public static String get(String state) {

        if (state == null) {
            return null;
        }
        try {
            org.wso2.carbon.identity.debug.framework.model.DebugSessionData data = DAO.getDebugSession(state);
            if (data != null) {
                return data.getResultJson();
            }
        } catch (Exception e) {
            LOG.error("Error retrieving debug result from DB: " + e.getMessage(), e);
        }
        return null;
    }

    public static void remove(String state) {

        if (state == null) {
            return;
        }
        try {
            // For remove, we might actually want to DELETE the session row or just clear
            // result?
            // Assuming delete for cleanup.
            DAO.deleteDebugSession(state);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug session deleted from DB: " + state);
            }
        } catch (Exception e) {
            LOG.error("Error deleting debug result from DB: " + e.getMessage(), e);
        }
    }

    public static void clear() {
        // Clearing DB table not supported/recommended via this API
        LOG.warn("Clear all cache not supported in DB mode");
    }

    public static int size() {
        // Not efficiently supported
        return 0;
    }

    private DebugResultCache() {
        // Prevent instantiation.
    }
}
