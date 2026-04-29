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

/**
 * OSGi service interface for resolving the script engine execution mode in HYBRID mode.
 * <p>
 * When the engine mode is set to HYBRID, the {@link JsGraalGraphEngineModeRouter} delegates to an
 * implementation of this interface to determine whether a given authentication request
 * should use the LOCAL (in-JVM GraalJS) or REMOTE (External via gRPC) engine.
 * <p>
 * Implementations can be dropped into the server's dropins folder as OSGi bundles,
 * allowing custom routing logic without modifying the core framework.
 */
public interface ScriptEngineModeResolver {

    /**
     * Resolve the execution mode for a given authentication context.
     *
     * @param authenticationContext The authentication context for the current request.
     * @return The resolved {@link JsGraalGraphEngineModeRouter.ExecutionMode} (LOCAL or REMOTE).
     */
    JsGraalGraphEngineModeRouter.ExecutionMode resolve(AuthenticationContext authenticationContext);
}
