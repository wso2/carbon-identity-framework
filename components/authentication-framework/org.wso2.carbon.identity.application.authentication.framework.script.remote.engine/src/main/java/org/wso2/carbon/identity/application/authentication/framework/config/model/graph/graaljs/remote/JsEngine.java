/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Map;

/**
 * Interface for JavaScript execution engines.
 * Abstracts the execution layer to support both in-JVM (local) and remote
 * External execution modes.
 * <p>
 * Implementations must be thread-safe as multiple authentication flows may use
 * the same engine
 * concurrently in a server environment.
 */
public interface JsEngine {

    /**
     * Evaluate a JavaScript script with the given bindings.
     *
     * @param script           The JavaScript source code to evaluate.
     * @param sourceIdentifier Identifier for the script source (for
     *                         debugging/logging).
     * @param bindings         Initial bindings to set before evaluation.
     * @return EvaluationResult containing the result and updated bindings.
     */
    EvaluationResult evaluate(String script, String sourceIdentifier, Map<String, Object> bindings);

    /**
     * Execute a serialized callback function.
     * This is used for executing event handlers (onSuccess, onFail) that were
     * serialized and need to be executed in a later authentication step.
     *
     * @param functionSource The source code of the callback function.
     * @param arguments      Arguments to pass to the callback function.
     * @param bindings       Current bindings to restore before execution.
     * @param authContext    The authentication context for this execution.
     * @return EvaluationResult containing the result and updated bindings.
     */
    EvaluationResult executeCallback(String functionSource, Object[] arguments,
            Map<String, Object> bindings, AuthenticationContext authContext);

    /**
     * Get the current bindings from the engine.
     *
     * @return Map of binding name to value.
     */
    Map<String, Object> getBindings();

    /**
     * Put a binding into the engine.
     *
     * @param name  The binding name.
     * @param value The value to bind.
     */
    void putBinding(String name, Object value);

    /**
     * Register host functions that can be called from JavaScript.
     * These include executeStep, sendError, fail, etc.
     *
     * @param hostFunctions Map of function name to function implementation.
     */
    void registerHostFunctions(Map<String, Object> hostFunctions);

    /**
     * Get the session ID associated with this engine instance.
     * Used for remote engines to track sessions.
     *
     * @return Session ID string, or null for local engines.
     */
    String getSessionId();

}
