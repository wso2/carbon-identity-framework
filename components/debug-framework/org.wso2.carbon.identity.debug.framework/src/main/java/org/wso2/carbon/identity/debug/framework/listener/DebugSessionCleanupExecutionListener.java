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

package org.wso2.carbon.identity.debug.framework.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.debug.framework.dao.DebugSessionDAO;
import org.wso2.carbon.identity.debug.framework.dao.impl.DebugSessionDAOImpl;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkException;
import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugRequest;
import org.wso2.carbon.identity.debug.framework.model.DebugResponse;

/**
 * Debug execution listener that cleans up session records from the database
 * after successful debug result retrieval.
 * Executes as a post-execution listener with high order (last to run).
 */
public class DebugSessionCleanupExecutionListener implements DebugExecutionListener {

    private static final Log LOG = LogFactory.getLog(DebugSessionCleanupExecutionListener.class);
    private static final int DEFAULT_ORDER = 1000;

    private final DebugSessionDAO debugSessionDAO;

    /**
     * Constructs a new listener with the default DAO implementation.
     */
    public DebugSessionCleanupExecutionListener() {

        this.debugSessionDAO = new DebugSessionDAOImpl();
    }

    /**
     * Constructs a new listener with a custom DAO.
     * Useful for testing purposes.
     *
     * @param debugSessionDAO Custom DAO implementation.
     */
    public DebugSessionCleanupExecutionListener(DebugSessionDAO debugSessionDAO) {

        this.debugSessionDAO = debugSessionDAO;
    }

    @Override
    public int getExecutionOrderId() {

        return DEFAULT_ORDER;
    }

    @Override
    public int getDefaultOrderId() {

        return DEFAULT_ORDER;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    /**
     * No-op for pre-execution. Cleanup only happens after execution.
     *
     * @param debugRequest The debug request about to be executed.
     * @return true to allow execution to proceed.
     * @throws DebugFrameworkException If an error occurs.
     */
    @Override
    public boolean doPreExecute(DebugRequest debugRequest) throws DebugFrameworkException {

        return true;
    }

    /**
     * Post-execute cleanup: deletes the debug session record from the database
     * after a successful response has been retrieved.
     *
     * @param debugResponse The debug response from execution.
     * @param debugRequest  The original debug request.
     * @return true to allow execution to proceed.
     * @throws DebugFrameworkException If an error occurs.
     */
    @Override
    public boolean doPostExecute(DebugResponse debugResponse, DebugRequest debugRequest)
            throws DebugFrameworkException {

        if (debugResponse == null || !debugResponse.isSuccess()) {
            return true;
        }

        String sessionId = debugRequest != null ? debugRequest.getEffectiveResourceId() : null;
        if (sessionId == null || sessionId.isEmpty()) {
            return true;
        }

        deleteSessionRecord(sessionId);
        return true;
    }

    /**
     * Deletes the debug session record from the database.
     *
     * @param sessionId The session ID to delete.
     */
    private void deleteSessionRecord(String sessionId) {

        try {
            debugSessionDAO.deleteDebugSession(sessionId);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Successfully deleted debug session record from database: " + sessionId);
            }
        } catch (DebugFrameworkServerException e) {
            // Log the error. Cleanup should not affect the main flow.
            LOG.error("Failed to delete debug session record from database: " + sessionId, e);
        }
    }
}
