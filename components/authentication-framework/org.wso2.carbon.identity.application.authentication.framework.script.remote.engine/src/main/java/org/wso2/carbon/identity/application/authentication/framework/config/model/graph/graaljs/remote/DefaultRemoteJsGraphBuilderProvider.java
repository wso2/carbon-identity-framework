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

import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.RemoteJsGraphBuilderProvider;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.util.Map;

/**
 * Default implementation of {@link RemoteJsGraphBuilderProvider}.
 * <p>
 * Constructs {@link RemoteJsGraalGraphBuilder} instances which route adaptive
 * authentication script execution to the externalized GraalJS sidecar process
 * over gRPC. Construction is intentionally cheap — each authentication request
 * gets its own builder instance, mirroring the per-request lifetime model used
 * by the local-mode {@code JsGraalGraphBuilder}.
 */
public class DefaultRemoteJsGraphBuilderProvider implements RemoteJsGraphBuilderProvider {

    @Override
    public JsBaseGraphBuilder create(AuthenticationContext authenticationContext,
                                     Map<Integer, StepConfig> stepConfigMap) {

        return new RemoteJsGraalGraphBuilder(authenticationContext, stepConfigMap);
    }

    @Override
    public JsBaseGraphBuilder create(AuthenticationContext authenticationContext,
                                     Map<Integer, StepConfig> stepConfigMap,
                                     AuthGraphNode currentNode) {

        return new RemoteJsGraalGraphBuilder(authenticationContext, stepConfigMap, currentNode);
    }

    /**
     * HYBRID-mode per-request decision. Delegates to {@link JsGraalGraphEngineModeRouter},
     * whose HYBRID branch consults the optional {@code ScriptEngineModeResolver} OSGi
     * service bound by {@code RemoteScriptEngineComponent}.
     */
    @Override
    public boolean shouldRoute(AuthenticationContext authenticationContext) {

        return JsGraalGraphEngineModeRouter.getInstance().resolveMode(authenticationContext)
                == JsGraalGraphEngineModeRouter.ExecutionMode.REMOTE;
    }
}
