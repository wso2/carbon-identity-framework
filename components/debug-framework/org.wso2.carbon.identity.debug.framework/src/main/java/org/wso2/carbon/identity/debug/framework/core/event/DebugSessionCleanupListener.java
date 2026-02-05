/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.debug.framework.core.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.core.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.core.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;

/**
 * Event listener that cleans up debug session records from the database after
 * flow completion.
 * This listener deletes the session record from IDN_DEBUG_SESSION table when
 * the debug flow completes.
 */
public class DebugSessionCleanupListener implements DebugSessionEventListener {

    private static final Log LOG = LogFactory.getLog(DebugSessionCleanupListener.class);

    private static final String LISTENER_NAME = "DebugSessionCleanupListener";
    private static final int LISTENER_ORDER = 1000; // Execute last, after all other listeners

    private final DebugSessionDAO debugSessionDAO;

    /**
     * Constructs a new DebugSessionCleanupListener with default DAO implementation.
     */
    public DebugSessionCleanupListener() {

        this.debugSessionDAO = new DebugSessionDAOImpl();
    }

    /**
     * Constructs a new DebugSessionCleanupListener with custom DAO.
     * Useful for testing purposes.
     *
     * @param debugSessionDAO Custom DAO implementation.
     */
    public DebugSessionCleanupListener(DebugSessionDAO debugSessionDAO) {

        this.debugSessionDAO = debugSessionDAO;
    }

    @Override
    public void onCompletion(DebugSessionEventContext context) {

        String sessionId = context.getSessionId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session completed. Initiating cleanup for session: " + sessionId
                    + ", successful: " + context.isSuccessful());
        }

        // Skipping immediate cleanup to allow the result to be retrieved by the Debug
        // API.
        // If we delete here, the result is gone before the UI can fetch it.
        // deleteSessionRecord(sessionId);
    }

    @Override
    public void onRetrieved(DebugSessionEventContext context) {

        String sessionId = context.getSessionId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session retrieved. Initiating cleanup for session: " + sessionId);
        }

        // Delete the session record after it has been retrieved (read-once)
        deleteSessionRecord(sessionId);
    }

    @Override
    public void onError(DebugSessionEventContext context) {

        String sessionId = context.getSessionId();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Debug session encountered error. Initiating cleanup for session: " + sessionId
                    + ", error: " + context.getErrorMessage());
        }

        // Also clean up on error to prevent orphaned records
        deleteSessionRecord(sessionId);
    }

    /**
     * Deletes the debug session record from the database.
     *
     * @param sessionId The session ID to delete.
     */
    private void deleteSessionRecord(String sessionId) {

        if (sessionId == null || sessionId.isEmpty()) {
            LOG.warn("Cannot delete debug session: session ID is null or empty");
            return;
        }

        try {
            debugSessionDAO.deleteDebugSession(sessionId);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully deleted debug session record from database: " + sessionId);
            }
        } catch (DebugFrameworkServerException e) {
            // Log the error but don't throw - cleanup should not affect the main flow
            LOG.error("Failed to delete debug session record from database: " + sessionId, e);
        }
    }

    @Override
    public String getListenerName() {

        return LISTENER_NAME;
    }

    @Override
    public int getOrder() {

        return LISTENER_ORDER;
    }
}
