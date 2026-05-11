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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * SPI for resolving the per-request adaptive-authentication script execution mode.
 * <p>
 * Consulted by {@code JsGraalGraphBuilderFactory} only when the configured engine
 * mode is {@code HYBRID}; for {@code LOCAL} the framework never asks, and for
 * {@code REMOTE} the framework always routes remote without asking.
 * <p>
 * Implementations are registered as OSGi services. A custom implementation can
 * be supplied by dropping a bundle into the server's dropins folder, allowing
 * routing logic to be tailored without modifying the core framework or the
 * externalized GraalJS runtime bundle.
 */
public interface ScriptEngineModeResolver {

    /**
     * Per-request execution mode resolved by {@link #resolve(AuthenticationContext)}.
     */
    enum ExecutionMode {
        /**
         * Run the adaptive script in-JVM via the local GraalJS engine.
         */
        LOCAL,
        /**
         * Run the adaptive script in the externalized GraalJS runtime over the
         * remote-engine wire protocol.
         */
        REMOTE
    }

    /**
     * Resolve the execution mode for the given authentication context.
     *
     * @param authenticationContext the current authentication context (may be {@code null}).
     * @return the resolved {@link ExecutionMode} for this request.
     */
    ExecutionMode resolve(AuthenticationContext authenticationContext);
}
