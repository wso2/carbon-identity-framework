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

package org.wso2.carbon.identity.debug.framework.core;

import org.wso2.carbon.identity.debug.framework.exception.DebugExecutionException;
import org.wso2.carbon.identity.debug.framework.model.DebugContext;
import org.wso2.carbon.identity.debug.framework.model.DebugResult;

/**
 * Abstract base class for executing debug flows.
 * Extensions should implement specific execution logic for different authentication protocols.
 */
public abstract class DebugExecutor {

    /**
     * Executes a debug flow step and generates the result.
     * Implementations handle protocol-specific logic like URL generation, token exchange, claim extraction.
     *
     * @param context DebugContext containing debug configuration and state.
     * @return DebugResult containing the outcome of the execution.
     * @throws DebugExecutionException If execution fails.
     */
    public abstract DebugResult execute(DebugContext context) throws DebugExecutionException;

    /**
     * Validates if this executor can handle the given context.
     * Used to determine which executor to use for a specific resource configuration.
     *
     * @param context DebugContext to validate against.
     * @return true if this executor can handle the context, false otherwise.
     */
    public abstract boolean canExecute(DebugContext context);

    /**
     * Gets the name of the executor.
     * Used for logging and identification purposes.
     *
     * @return Executor name.
     */
    public abstract String getExecutorName();

    /**
     * Performs any cleanup or resource release.
     * Called when the debug operation completes.
     * Implementations must explicitly handle cleanup to ensure resources are not leaked.
     */
    public abstract void cleanup();
}
