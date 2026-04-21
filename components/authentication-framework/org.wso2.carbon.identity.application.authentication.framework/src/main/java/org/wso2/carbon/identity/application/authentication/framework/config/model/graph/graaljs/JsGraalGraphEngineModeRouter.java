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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.ResourceLimits;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.ScriptEngineModeResolver;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Arrays;
import java.util.Locale;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.DEFAULT_ENGINE_MODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.DEFAULT_GRAALJS_SCRIPT_STATEMENTS_LIMIT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.DEFAULT_GRPC_TARGET;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.DEFAULT_REMOTE_ENGINE_TRACING;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.GRAALJS_ENGINE_MODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.GRAALJS_GRPC_TARGET;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.GRAALJS_REMOTE_ENGINE_TRACING;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.GRAALJS_SCRIPT_STATEMENTS_LIMIT;
/**
 * Factory for creating JavaScript engines.
 * Supports LOCAL (in-JVM), REMOTE (External via gRPC), and HYBRID (per-request routing) modes.
 * <p>
 * Engine mode can be configured in deployment.toml:
 * <pre>
 * [authentication.adaptive.graaljs]
 * engine_mode = "REMOTE"              # "LOCAL", "REMOTE", or "HYBRID"
 * grpc_target = "localhost:50051"
 * </pre>
 * <p>
 * In HYBRID mode, the engine selection is delegated to a {@link ScriptEngineModeResolver}
 * OSGi service, which can be customized by dropping a bundle into the dropins folder.
 */
public class JsGraalGraphEngineModeRouter {

    private static final Log log = LogFactory.getLog(JsGraalGraphEngineModeRouter.class);

    /**
     * Execution mode for JavaScript engine.
     */
    public enum ExecutionMode {
        /**
         * Execute JavaScript locally in the same JVM using GraalVM Polyglot.
         */
        LOCAL,
        /**
         * Execute JavaScript in a remote External process via gRPC.
         */
        REMOTE
    }

    /**
     * Engine mode configuration value.
     * Each constant encapsulates its own resolution strategy, eliminating the need for switch-case dispatch.
     */
    public enum EngineMode {
        /**
         * All requests use the local in-JVM GraalJS engine.
         */
        LOCAL {
            @Override
            public ExecutionMode resolve(AuthenticationContext context) {

                return ExecutionMode.LOCAL;
            }
        },
        /**
         * All requests use the remote External engine via gRPC.
         */
        REMOTE {
            @Override
            public ExecutionMode resolve(AuthenticationContext context) {

                return ExecutionMode.REMOTE;
            }
        },
        /**
         * Per-request routing delegated to a {@link ScriptEngineModeResolver} OSGi service.
         */
        HYBRID {
            @Override
            public ExecutionMode resolve(AuthenticationContext context) {

                ScriptEngineModeResolver resolver =
                        FrameworkServiceDataHolder.getInstance().getScriptEngineModeResolver();

                if (resolver == null) {
                    log.warn("[JsGraalGraphEngineModeRouter] HYBRID mode configured but no ScriptEngineModeResolver " +
                            "OSGi service found. Falling back to LOCAL.");
                    return ExecutionMode.LOCAL;
                }

                ExecutionMode resolved = resolver.resolve(context);
                if (isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[JsGraalGraphEngineModeRouter] HYBRID mode resolved to: " + resolved +
                            " for SP: " + (context != null ?
                            context.getServiceProviderName() : "null"));
                }
                return resolved;
            }
        };

        /**
         * Resolve the execution mode for a given authentication context.
         *
         * @param context The authentication context (may be null).
         * @return The resolved ExecutionMode for this request.
         */
        public abstract ExecutionMode resolve(AuthenticationContext context);
    }

    // Configured engine mode.
    private EngineMode engineMode = EngineMode.REMOTE;

    // Default gRPC target for remote engine (host:port).
    private String grpcTarget = DEFAULT_GRPC_TARGET;

    // Statement limit for local engine.
    private int javascriptResourceLimit = DEFAULT_GRAALJS_SCRIPT_STATEMENTS_LIMIT;

    // Remote engine tracing toggle — guards debug/perf logs in remote package.
    private boolean tracingEnabled = DEFAULT_REMOTE_ENGINE_TRACING;

    // singleton holder
    private static volatile JsGraalGraphEngineModeRouter instance;

    private JsGraalGraphEngineModeRouter() {

        initializeFromConfig();
    }

    /**
     * Get the singleton instance. Uses lazy initialization to ensure config is available.
     *
     * @return JsGraalGraphEngineModeRouter instance.
     */
    public static JsGraalGraphEngineModeRouter getInstance() {

        if (instance == null) {
            synchronized (JsGraalGraphEngineModeRouter.class) {
                if (instance == null) {
                    instance = new JsGraalGraphEngineModeRouter();
                }
            }
        }
        return instance;
    }



    /**
     * Get the configured engine mode.
     *
     * @return Current EngineMode.
     */
    public EngineMode getEngineMode() {

        return engineMode;
    }

    /**
     * Get the gRPC target address.
     *
     * @return gRPC target string (host:port).
     */
    public static String getGrpcTarget() {

        return getInstance().grpcTarget;
    }

    /**
     * Check if remote engine tracing is enabled.
     * When enabled, debug/performance log statements in the remote package are active.
     * When disabled (default), all such statements are suppressed regardless of log level.
     *
     * @return true if remote engine tracing is enabled.
     */
    public static boolean isTracingEnabled() {

        return getInstance().tracingEnabled;
    }

    /**
     * Set the JavaScript statement limit.
     *
     * @param limit The statement limit.
     */
    public void setStatementLimit(int limit) {

        javascriptResourceLimit = limit;
    }

    /**
     * Resolve the execution mode for a given authentication context.
     * For LOCAL/REMOTE modes, returns the configured mode directly.
     * For HYBRID mode, delegates to the {@link ScriptEngineModeResolver} OSGi service.
     * This is the public API that callers (e.g., JsGraalGraphBuilder) should use
     * to determine which code path (local vs remote) to follow.
     *
     * @param authenticationContext The authentication context (may be null).
     * @return The resolved ExecutionMode for this request.
     */
    public ExecutionMode resolveMode(AuthenticationContext authenticationContext) {

        return engineMode.resolve(authenticationContext);
    }

    /**
     * Get the resource limits for local engine execution.
     *
     * @return ResourceLimits instance.
     */
    public ResourceLimits getResourceLimits() {

        ResourceLimits.Builder resourceLimitsBuilder = ResourceLimits.newBuilder();
        resourceLimitsBuilder.statementLimit(javascriptResourceLimit, null);
        return resourceLimitsBuilder.build();
    }


    private void initializeFromConfig() {

        // Read statement limit from config
        String statementLimit = IdentityUtil.getProperty(GRAALJS_SCRIPT_STATEMENTS_LIMIT);
        if (statementLimit != null) {
            try {
                javascriptResourceLimit = Integer.parseInt(statementLimit);
            } catch (NumberFormatException e) {
                log.warn("Error parsing script statement limit. Defaulting to " +
                        DEFAULT_GRAALJS_SCRIPT_STATEMENTS_LIMIT, e);
                javascriptResourceLimit = DEFAULT_GRAALJS_SCRIPT_STATEMENTS_LIMIT;
            }
        }

        // Read engine mode (LOCAL, REMOTE, or HYBRID)
        String mode = IdentityUtil.getProperty(GRAALJS_ENGINE_MODE);
        if (mode != null && !mode.isEmpty()) {
            try {
                engineMode = EngineMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown engine mode: '" + mode + "'. Defaulting to " + DEFAULT_ENGINE_MODE);
                engineMode = EngineMode.valueOf(DEFAULT_ENGINE_MODE);
            }
        } else {
            engineMode = EngineMode.valueOf(DEFAULT_ENGINE_MODE);
        }

        // Read gRPC target
        String target = IdentityUtil.getProperty(GRAALJS_GRPC_TARGET);
        if (target != null && !target.isEmpty()) {
            grpcTarget = target.trim();
        }

        // Read remote engine tracing toggle
        String tracingStr = IdentityUtil.getProperty(GRAALJS_REMOTE_ENGINE_TRACING);
        if (tracingStr != null && !tracingStr.isEmpty()) {
            tracingEnabled = Boolean.parseBoolean(tracingStr.trim());
        }

        log.info("JsGraalGraphEngineModeRouter initialized. EngineMode: " + engineMode +
                ", gRPC Target: " + grpcTarget +
                ", RemoteEngineTracing: " + tracingEnabled);
    }
}
