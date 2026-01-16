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

package org.wso2.carbon.identity.debug.framework.core.dao;

import org.wso2.carbon.identity.debug.framework.exception.DebugFrameworkServerException;
import org.wso2.carbon.identity.debug.framework.model.DebugSessionData;

/**
 * Data Access Object interface for managing debug sessions in the database.
 */
public interface DebugSessionDAO {

    /**
     * Creates a new debug session.
     *
     * @param sessionData Session data object.
     * @throws DebugFrameworkServerException If an error occurs while creating the
     *                                       session.
     */
    void createDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException;

    /**
     * Retrieves a debug session by ID.
     *
     * @param sessionId Session ID.
     * @return DebugSessionData or null if not found.
     * @throws DebugFrameworkServerException If an error occurs while retrieving the
     *                                       session.
     */
    DebugSessionData getDebugSession(String sessionId) throws DebugFrameworkServerException;

    /**
     * Updates an existing debug session.
     *
     * @param sessionData Session data object with updated values.
     * @throws DebugFrameworkServerException If an error occurs while updating the
     *                                       session.
     */
    void updateDebugSession(DebugSessionData sessionData) throws DebugFrameworkServerException;

    /**
     * Deletes a debug session.
     *
     * @param sessionId Session ID.
     * @throws DebugFrameworkServerException If an error occurs while deleting the
     *                                       session.
     */
    void deleteDebugSession(String sessionId) throws DebugFrameworkServerException;
}
