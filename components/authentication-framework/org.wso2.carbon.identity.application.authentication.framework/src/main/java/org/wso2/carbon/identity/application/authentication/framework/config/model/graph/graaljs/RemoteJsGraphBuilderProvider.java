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

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Map;

/**
 * SPI for constructing remote-execution graph builders for adaptive authentication.
 * <p>
 * This is the integration seam between the authentication framework and the
 * externalized GraalJS script execution runtime. The framework's
 * {@code JsGraalGraphBuilderFactory} consumes this provider to obtain a builder
 * that routes script evaluation and callback execution to a remote engine via
 * the wire protocol owned by the remote engine bundle.
 * <p>
 * An implementation is expected to be registered as an OSGi service by the
 * {@code script.remote.engine} bundle. When the engine mode resolves to
 * {@code REMOTE} (or {@code HYBRID} → REMOTE via {@link ScriptEngineModeResolver})
 * and no provider is bound, the factory will fail loudly rather than silently
 * degrading.
 * <p>
 * Per-request mode routing is owned by the separate {@link ScriptEngineModeResolver}
 * SPI; this interface deals only with builder construction.
 */
public interface RemoteJsGraphBuilderProvider {

    /**
     * Create a builder for the initial script evaluation path.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         the step map from the service provider configuration.
     * @return a new graph builder backed by the remote engine.
     */
    JsBaseGraphBuilder create(AuthenticationContext authenticationContext,
                              Map<Integer, StepConfig> stepConfigMap);

    /**
     * Create a builder for the callback evaluation path.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         the step map from the service provider configuration.
     * @param currentNode           the current authentication graph node.
     * @return a new graph builder backed by the remote engine.
     */
    JsBaseGraphBuilder create(AuthenticationContext authenticationContext,
                              Map<Integer, StepConfig> stepConfigMap,
                              AuthGraphNode currentNode);
}
