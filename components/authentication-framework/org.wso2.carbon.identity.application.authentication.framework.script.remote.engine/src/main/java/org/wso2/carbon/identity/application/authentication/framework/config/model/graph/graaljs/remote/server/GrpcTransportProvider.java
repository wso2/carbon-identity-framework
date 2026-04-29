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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.RemoteEngineTransport;

/**
 * Provider for gRPC streaming transport singleton.
 * <p>
 * Creates and manages a single {@link GrpcStreamingTransportImpl} instance
 * that implements {@link RemoteEngineTransport}. Uses double-checked locking
 * for thread-safe lazy initialization.
 */
public class GrpcTransportProvider {

    private static final Log log = LogFactory.getLog(GrpcTransportProvider.class);

    private static volatile GrpcStreamingTransportImpl streamingInstance;
    private static final Object lock = new Object();

    /**
     * Get or create the singleton gRPC streaming transport.
     *
     * @param grpcTarget gRPC target address (host:port).
     * @return The singleton RemoteEngineTransport instance.
     * @throws IllegalArgumentException if grpcTarget is null or empty.
     */
    public static RemoteEngineTransport getTransport(String grpcTarget) {

        if (grpcTarget == null || grpcTarget.isEmpty()) {
            throw new IllegalArgumentException("gRPC target is required for gRPC transport");
        }

        if (streamingInstance == null) {
            synchronized (lock) {
                if (streamingInstance == null) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[GrpcTransportProvider] Creating GrpcStreamingTransportImpl " +
                                "for target: " + grpcTarget);
                    }
                    streamingInstance = new GrpcStreamingTransportImpl(grpcTarget);
                }
            }
        }
        return streamingInstance;
    }
}
