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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackResponse;

import java.io.IOException;

/**
 * Abstraction for remote JavaScript engine transport layer.
 * Provides request/response communication between the Identity Server and the remote JavaScript execution engine.
 * <p>
 * Implementations can use different transport mechanisms (gRPC, HTTP, etc.)
 * while maintaining the same protocol for script evaluation and callback execution.
 * <p>
 * This interface decouples the RemoteJsEngine from specific transport implementations,
 * allowing per-instance transport selection and future extensibility.
 */
public interface RemoteEngineTransport {

    EvaluateResponse sendEvaluate(EvaluateRequest request,
                                  RemoteJsEngine engine) throws IOException;

    ExecuteCallbackResponse sendExecuteCallback(ExecuteCallbackRequest request,
                                                RemoteJsEngine engine) throws IOException;

    void connect() throws IOException;

    boolean isConnected();
}
