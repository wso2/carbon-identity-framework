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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;

/**
 * DAO interface for managing FlowExecutionContext store.
 */
public interface FlowContextStoreDAO {

    /**
     * Store a FlowExecutionContext with a specified TTL.
     *
     * @param context FlowExecutionContext to store.
     * @param ttlSeconds Time to live in seconds.
     * @throws FlowEngineException if an error occurs while storing the context.
     */
    void storeContext(FlowExecutionContext context, long ttlSeconds) throws FlowEngineException;

    /**
     * Store a FlowExecutionContext with a specified TTL.
     *
     * @param contextIdentifier Unique identifier for the context.
     * @param context FlowExecutionContext to store.
     * @param ttlSeconds Time to live in seconds.
     * @throws FlowEngineException if an error occurs while storing the context.
     */
    void storeContext(String contextIdentifier, FlowExecutionContext context, long ttlSeconds)
            throws FlowEngineException;

    /**
     * Retrieve a FlowExecutionContext by its context ID.
     *
     * @param contextId The unique identifier of the FlowExecutionContext.
     * @return The FlowExecutionContext if found, otherwise null.
     * @throws FlowEngineException if an error occurs while retrieving the context.
     */
    FlowExecutionContext getContext(String contextId) throws FlowEngineException;

    /**
     * Delete a FlowExecutionContext by its context ID.
     *
     * @param contextId The unique identifier of the FlowExecutionContext to delete.
     * @throws FlowEngineException if an error occurs while deleting the context.
     */
    void deleteContext(String contextId) throws FlowEngineException;
}

