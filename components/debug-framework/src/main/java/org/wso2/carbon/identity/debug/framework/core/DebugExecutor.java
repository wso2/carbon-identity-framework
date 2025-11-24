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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.exception.ExecutionException;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

import java.util.Map;

/**
 * Abstract base class for executing debug flows.
 * Extensions should implement specific execution logic for different authentication protocols.
 * Examples: OAuth2Executor, SAMLExecutor, etc.
 */
public abstract class DebugExecutor {

    /**
     * Executes a debug flow step and generates the result.
     * Implementations handle protocol-specific logic like URL generation, token exchange, claim extraction.
     *
     * @param context Map containing debug configuration and state.
     * @return DebugResult containing the outcome of the execution.
     * @throws ExecutionException If execution fails.
     */
    public abstract DebugResult execute(Map<String, Object> context) throws ExecutionException;

    /**
     * Validates if this executor can handle the given context.
     * Used to determine which executor to use for a specific resource configuration.
     *
     * @param context Map to validate against.
     * @return true if this executor can handle the context, false otherwise.
     */
    public abstract boolean canExecute(Map<String, Object> context);

    /**
     * Gets the name/type of this executor.
     * Used for logging and identification purposes.
     *
     * @return Executor name string.
     */
    public abstract String getExecutorName();

    /**
     * Performs any cleanup or resource release.
     * Called when the debug operation completes.
     */
    public void cleanup() {
        
        // Default: no cleanup needed. Override if necessary.
    }
}
