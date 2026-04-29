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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ContextPropertyRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ContextPropertyResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ContextPropertySetRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ContextPropertySetResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.EvaluateResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.ExecuteCallbackResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.HostFunctionDefinition;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.HostFunctionRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.HostFunctionResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedValue;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.StreamMessage;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remote JavaScript engine that communicates with a GraalJS External via
 * pluggable transport.
 * Each instance represents a session with the External.
 * <p>
 * Host function calls from the External are routed back to IS via the callback
 * server.
 * <p>
 * This implementation is decoupled from specific transport mechanisms
 * through the RemoteEngineTransport abstraction.
 * <p>
 * Responsibilities are delegated to focused collaborators:
 * <ul>
 *   <li>{@link ArgumentAdapter} — External argument adaptation and type conversion</li>
 *   <li>{@link ProxyReferenceCache} — proxy object and host function return reference caching</li>
 * </ul>
 * <p>
 * Thread model: With the same-thread callback architecture, host function invocations
 * (handleHostFunctionCallback) run on the IS HTTP thread (Thread A) which already has the correct
 * ThreadLocal context (contextForJs, dynamicallyBuiltBaseNode, currentBuilder, CarbonContext).
 * No thread-local setup/teardown is needed.
 */
public class RemoteJsEngine implements JsEngine {

    private static final Log log = LogFactory.getLog(RemoteJsEngine.class);

    private final RemoteEngineTransport transport;
    private final String sessionId;
    private final AuthenticationContext authContext;

    private final Map<String, Object> bindings = new ConcurrentHashMap<>();
    private final Map<String, Object> hostFunctions = new ConcurrentHashMap<>();

    // Collaborators — each handles a single responsibility
    private final ArgumentAdapter argumentAdapter;
    private final ProxyReferenceCache proxyReferenceCache;
    private final HostFunctionRegistry hostFunctionRegistry;

    /**
     * Create a new remote JavaScript engine.
     *
     * @param transport   The transport layer for communicating with the remote engine.
     * @param authContext The authentication context for this session.
     */
    public RemoteJsEngine(RemoteEngineTransport transport, AuthenticationContext authContext) {
        this.transport = transport;
        this.authContext = authContext;
        this.sessionId = UUID.randomUUID().toString();
        this.argumentAdapter = new ArgumentAdapter(authContext);
        this.proxyReferenceCache = new ProxyReferenceCache();
        this.hostFunctionRegistry = new HostFunctionRegistry();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] Created with session: " + sessionId +
                    ", transport: " + transport.getClass().getSimpleName() +
                    ", SP: " + (authContext != null ? authContext.getServiceProviderName() : "null"));
        }
    }

    @Override
    public EvaluationResult evaluate(String script, String sourceIdentifier, Map<String, Object> initialBindings) {
        long startTime = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] evaluate() called, session: " + sessionId + ", sourceId: " + sourceIdentifier);
        }

        try {
            // Phase 1: Connect and setup
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Ensuring connection to remote engine");
            }
            ensureConnected();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Connection established");
            }
            long tConnectDone = System.currentTimeMillis();

            // Phase 2: Build request (protobuf serialization)
            // Build the request
            EvaluateRequest.Builder requestBuilder = EvaluateRequest.newBuilder()
                    .setSessionId(sessionId)
                    .setScript(script)
                    .setSourceIdentifier(sourceIdentifier != null ? sourceIdentifier : "script");

            // Serialize bindings
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Serializing " + bindings.size() + " bindings, " +
                        hostFunctions.size() + " host functions");
            }

            // Add host function definitions so External knows to call back
            for (String funcName : hostFunctions.keySet()) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Registering host function: " + funcName);
                }
                requestBuilder.addHostFunctions(
                        HostFunctionDefinition.newBuilder()
                                .setName(funcName)
                                .build());
            }

            long tRequestBuilt = System.currentTimeMillis();

            // Phase 3: Transport round-trip
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Sending evaluate request to remote engine...");
            }
            EvaluateResponse response = transport.sendEvaluate(requestBuilder.build(), this);
            long tResponseReceived = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Received response, success: " + response.getSuccess());
            }

            // Phase 4: Response processing
            EvaluationResult evalResult;
            if (response.getSuccess()) {
                // Update bindings from response
                Map<String, Object> updatedBindings = new HashMap<>();
                for (Map.Entry<String, SerializedValue> entry : response.getUpdatedBindingsMap().entrySet()) {
                    Object deserialized = Serializer.fromProto(entry.getValue());
                    updatedBindings.put(entry.getKey(), deserialized);
                    bindings.put(entry.getKey(), deserialized);
                }

                Object result = Serializer.fromProto(response.getResult());
                long tResponseProcessed = System.currentTimeMillis();

                long isElapsed = tResponseProcessed - startTime;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Phase timing: connectSetup=" + (tConnectDone - startTime) +
                            "ms, requestBuild=" + (tRequestBuilt - tConnectDone) +
                            "ms, transportRoundTrip=" + (tResponseReceived - tRequestBuilt) +
                            "ms, responseProcess=" + (tResponseProcessed - tResponseReceived) +
                            "ms, total=" + isElapsed + "ms" +
                            ", ExternalReported=" + response.getElapsedMs() + "ms");
                }

                evalResult = EvaluationResult.builder()
                        .success(true)
                        .result(result)
                        .updatedBindings(updatedBindings)
                        .elapsedMs(isElapsed)
                        .build();
            } else {
                long tResponseProcessed = System.currentTimeMillis();
                long isElapsed = tResponseProcessed - startTime;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Phase timing (error): connectSetup=" + (tConnectDone - startTime) +
                            "ms, requestBuild=" + (tRequestBuilt - tConnectDone) +
                            "ms, transportRoundTrip=" + (tResponseReceived - tRequestBuilt) +
                            "ms, responseProcess=" + (tResponseProcessed - tResponseReceived) +
                            "ms, total=" + isElapsed + "ms");
                }
                evalResult = EvaluationResult.failure(
                        response.getErrorMessage(),
                        response.getErrorType(),
                        isElapsed);
            }
            return evalResult;

        } catch (IOException e) {
            long isElapsed = System.currentTimeMillis() - startTime;
            log.error("IOException during remote evaluation", e);
            return EvaluationResult.failure("Remote engine communication failed: " + e.getMessage(),
                    "IOException", isElapsed);
        }
    }

    @Override
    public EvaluationResult executeCallback(String functionSource, Object[] arguments,
            Map<String, Object> callbackBindings, AuthenticationContext context) {
        long startTime = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] executeCallback() called, session: " + sessionId +
                    ", function length: " + (functionSource != null ? functionSource.length() : 0) +
                    ", args: " + (arguments != null ? arguments.length : 0));
        }

        try {
            // Phase 1: Connect and setup
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] executeCallback - ensuring connection to remote engine");
            }
            ensureConnected();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] executeCallback - connection established");
            }
            long tConnectDone = System.currentTimeMillis();

            // Phase 2: Build request (protobuf serialization)
            // Apply callback bindings
            if (callbackBindings != null && !callbackBindings.isEmpty()) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Applying " + callbackBindings.size() + " callback bindings: " +
                            callbackBindings.keySet());
                }
                for (Map.Entry<String, Object> entry : callbackBindings.entrySet()) {
                    Object value = entry.getValue();
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[RemoteJsEngine] Callback binding: " + entry.getKey() + " = " +
                                (value != null ? value.getClass().getSimpleName() + ": " + value : "null"));
                    }
                    bindings.put(entry.getKey(), value);
                }
            } else {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] No callback bindings provided (null or empty). " +
                            "callbackBindings=" + callbackBindings);
                }
            }

            // Build the request
            ExecuteCallbackRequest.Builder requestBuilder = ExecuteCallbackRequest.newBuilder()
                    .setSessionId(sessionId)
                    .setFunctionSource(functionSource);

            // Serialize arguments
            if (arguments != null) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Serializing " + arguments.length + " arguments");
                }
                for (int i = 0; i < arguments.length; i++) {
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[RemoteJsEngine] Arg[" + i + "] type: " +
                                (arguments[i] != null ? arguments[i].getClass().getName() : "null"));
                    }
                    // Replace JsGraalAuthenticationContext with a marker string instead of
                    // serializing the full object (which is not Serializer-compatible)
                    // The External detects
                    // this marker via sv.getStringValue().contains("JsGraalAuthenticationContext")
                    // and substitutes its local DynamicContextProxy. We must preserve the
                    // argument position — skipping it would shift subsequent args (e.g.,
                    // httpGet's onSuccess(context, data) would receive (data, undefined)).
                    if (arguments[i] instanceof JsGraalAuthenticationContext) {
                        requestBuilder.addArguments(
                                Serializer.toProto(RemoteEngineConstants.CONTEXT_PLACEHOLDER));
                        continue;
                    }
                    requestBuilder.addArguments(Serializer.toProto(arguments[i]));
                }
            }

            // Serialize bindings (excluding host functions)
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Total bindings to serialize: " + bindings.size() +
                        ", keys: " + bindings.keySet());
            }
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Host functions (excluded from bindings): " + hostFunctions.keySet());
            }
            int bindingsAdded = 0;
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                // Skip "context" -- JsGraalAuthenticationContext is not Serializer-compatible.
                // The External attaches its DynamicContextProxy when handling the request and
                // reads context state live via callbacks back to IS. Serializing the proxy here
                // would fall back to toString() with a WARN. If this binding is ever needed,
                // implement a proper toProto() conversion for JsGraalAuthenticationContext first.
                if (!RemoteEngineConstants.CONTEXT_BINDING_KEY.equals(entry.getKey()) && !hostFunctions.containsKey(entry.getKey())) {
                    Object value = entry.getValue();
                    if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                        log.debug("[RemoteJsEngine] Serializing binding: " + entry.getKey() + " = " +
                                (value != null ? value.getClass().getSimpleName() + ": " + value : "null"));
                    }
                    requestBuilder.putBindings(entry.getKey(), Serializer.toProto(value));
                    bindingsAdded++;
                }
            }
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Bindings serialized: " + bindingsAdded);
            }

            // Add host function definitions so External knows to create stubs for callbacks
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Adding " + hostFunctions.size() + " host function definitions");
            }
            for (String funcName : hostFunctions.keySet()) {
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Adding host function: " + funcName);
                }
                requestBuilder.addHostFunctions(
                        HostFunctionDefinition.newBuilder()
                                .setName(funcName)
                                .build());
            }
            long tRequestBuilt = System.currentTimeMillis();

            // Phase 3: Transport round-trip
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Sending executeCallback request to remote engine...");
            }
            ExecuteCallbackResponse response = transport.sendExecuteCallback(requestBuilder.build(), this);
            long tResponseReceived = System.currentTimeMillis();

            // Phase 4: Response processing
            EvaluationResult evalResult;
            if (response.getSuccess()) {
                // Update bindings from response
                Map<String, Object> updatedBindings = new HashMap<>();
                for (Map.Entry<String, SerializedValue> entry : response.getUpdatedBindingsMap().entrySet()) {
                    Object deserialized = Serializer.fromProto(entry.getValue());
                    updatedBindings.put(entry.getKey(), deserialized);
                    bindings.put(entry.getKey(), deserialized);
                }

                Object result = Serializer.fromProto(response.getResult());
                long tResponseProcessed = System.currentTimeMillis();

                long isElapsed = tResponseProcessed - startTime;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Phase timing: connectSetup=" + (tConnectDone - startTime) +
                            "ms, requestBuild=" + (tRequestBuilt - tConnectDone) +
                            "ms, transportRoundTrip=" + (tResponseReceived - tRequestBuilt) +
                            "ms, responseProcess=" + (tResponseProcessed - tResponseReceived) +
                            "ms, total=" + isElapsed + "ms" +
                            ", ExternalReported=" + response.getElapsedMs() + "ms");
                }

                evalResult = EvaluationResult.builder()
                        .success(true)
                        .result(result)
                        .updatedBindings(updatedBindings)
                        .elapsedMs(isElapsed)
                        .build();
            } else {
                long tResponseProcessed = System.currentTimeMillis();
                long isElapsed = tResponseProcessed - startTime;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Phase timing (error): connectSetup=" + (tConnectDone - startTime) +
                            "ms, requestBuild=" + (tRequestBuilt - tConnectDone) +
                            "ms, transportRoundTrip=" + (tResponseReceived - tRequestBuilt) +
                            "ms, responseProcess=" + (tResponseProcessed - tResponseReceived) +
                            "ms, total=" + isElapsed + "ms");
                }
                evalResult = EvaluationResult.failure(
                        response.getErrorMessage(),
                        "ExecutionError",
                        isElapsed);
            }
            return evalResult;

        } catch (IOException e) {
            long isElapsed = System.currentTimeMillis() - startTime;
            log.error("IOException during remote callback execution", e);
            return EvaluationResult.failure("Remote callback execution failed: " + e.getMessage(),
                    "IOException", isElapsed);
        }
    }

    @Override
    public Map<String, Object> getBindings() {
        return new HashMap<>(bindings);
    }

    @Override
    public void putBinding(String name, Object value) {
        bindings.put(name, value);
    }

    @Override
    public void registerHostFunctions(Map<String, Object> functions) {
        if (functions != null) {
            hostFunctions.putAll(functions);
            hostFunctionRegistry.register(functions);
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    // ======================== Callback Handling ========================

    /**
     * Handle a host function callback request from the External.
     * Deserializes arguments, invokes the host function via the registry,
     * serializes the result, and returns the response as a StreamMessage.
     * <p>
     * CRITICAL: Preserves the proxy cache ThreadLocal set-before/clear-after pattern
     * for lazy-loading complex objects (e.g., User arrays).
     *
     * @param callbackSessionId The session identifier for the stream message.
     * @param request           The host function request from the External.
     * @return StreamMessage containing the HostFunctionResponse.
     */
    public StreamMessage handleHostFunctionCallback(String callbackSessionId, HostFunctionRequest request) {

        String functionName = request.getFunctionName();
        long hfStart = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
            log.debug("[PERF] [" + hfStart + "] IS HOST_FN_HANDLE_START session=" + callbackSessionId +
                    " fn=" + functionName + " handleStartTs=" + hfStart);
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[GrpcStreaming] handleHostFunction: " + functionName + ", session: " + callbackSessionId);
        }

        try {
            // Deserialize arguments
            List<Object> args = new ArrayList<>();
            for (SerializedValue sv : request.getArgumentsList()) {
                args.add(Serializer.fromProto(sv));
            }

            // Invoke host function via registry dispatch
            long hfExecStart = System.currentTimeMillis();
            Object[] argsArray = args.toArray();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Invoking host function: " + functionName + " with " +
                        argsArray.length + " args, session: " + sessionId);
                for (int i = 0; i < argsArray.length; i++) {
                    log.debug("[RemoteJsEngine] Raw arg[" + i + "]: type=" +
                            (argsArray[i] != null ? argsArray[i].getClass().getName() : "null") +
                            ", value=" + (argsArray[i] != null ? truncateForLog(argsArray[i].toString()) : "null"));
                }
            }
            Object result = hostFunctionRegistry.invoke(functionName, argumentAdapter, argsArray);
            long hfExecEnd = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                log.debug("[PERF] [" + hfExecEnd + "] IS HOST_FN_EXECUTED session=" + callbackSessionId +
                        " fn=" + functionName +
                        " handleStartTs=" + hfStart + " deserEndTs=" + hfExecStart +
                        " execStartTs=" + hfExecStart + " execEndTs=" + hfExecEnd +
                        " deserMs=" + (hfExecStart - hfStart) +
                        " execMs=" + (hfExecEnd - hfExecStart) +
                        " totalMs=" + (hfExecEnd - hfStart));
            }
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[GrpcStreaming] Host function " + functionName + " returned: " +
                        (result != null ? result.getClass().getSimpleName() : "null"));
            }

            // Serialize result: use proxy object for complex types, primitive serialization otherwise
            SerializedValue serializedResult;
            if (result != null && ProxyTypeResolver.isJsWrapperProxy(result)) {
                String refId = proxyReferenceCache.storeHostReturnReference(result);
                String proxyType = ProxyTypeResolver.getJsWrapperProxyType(result);
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[GrpcStreaming] Serializing complex result as proxy: type=" + proxyType +
                            ", refId=" + refId);
                }
                serializedResult = SerializedValue.newBuilder()
                        .setProxyObject(SerializedProxyObject.newBuilder()
                                .setType(proxyType)
                                .setReferenceId(refId != null ? refId : "")
                                .build())
                        .build();
            } else {
                Map<String, Object> proxyCache = proxyReferenceCache.getCache();
                if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                    log.debug("[GrpcStreaming] Setting proxy cache ThreadLocal - cache size: " +
                            (proxyCache != null ? proxyCache.size() : "NULL"));
                }
                Serializer.setSessionProxyCache(proxyCache);
                try {
                    serializedResult = Serializer.toProto(result);
                } finally {
                    Serializer.clearSessionProxyCache();
                }
            }

            long hfSerEnd = System.currentTimeMillis();
            StreamMessage response = StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setHostFunctionResponse(HostFunctionResponse.newBuilder()
                            .setSuccess(true)
                            .setResult(serializedResult)
                            .build())
                    .build();
            long hfSentTs = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                log.debug("[PERF] [" + hfSentTs + "] IS HOST_FN_RESPONSE_BUILT session=" +
                        callbackSessionId + " fn=" + functionName +
                        " handleStartTs=" + hfStart + " execEndTs=" + hfExecEnd +
                        " serEndTs=" + hfSerEnd + " builtTs=" + hfSentTs +
                        " serMs=" + (hfSerEnd - hfExecEnd) + " buildMs=" + (hfSentTs - hfSerEnd) +
                        " totalHandleMs=" + (hfSentTs - hfStart));
            }
            return response;

        } catch (Exception e) {
            log.error("[GrpcStreaming] Error in host function " + functionName, e);
            return StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setHostFunctionResponse(HostFunctionResponse.newBuilder()
                            .setSuccess(false)
                            .setErrorMessage(formatErrorForWire(e))
                            .build())
                    .build();
        }
    }

    /**
     * Handle a context property read callback request from the External.
     * Resolves the property path, determines if the value is a proxy type,
     * and returns the appropriate response (proxy metadata with member keys, or serialized value).
     * <p>
     * Handles the special {@code __keys__} path for member key enumeration.
     *
     * @param callbackSessionId The session identifier for the stream message.
     * @param request           The context property request from the External.
     * @return StreamMessage containing the ContextPropertyResponse.
     */
    public StreamMessage handleContextPropertyCallback(String callbackSessionId, ContextPropertyRequest request) {

        String propertyPath = request.getPropertyPath();
        long cpStart = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
            log.debug("[PERF] [" + cpStart + "] IS CTX_PROP_HANDLE_START session=" + callbackSessionId +
                    " path=" + propertyPath + " handleStartTs=" + cpStart);
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[GrpcStreaming] handleContextProperty: " + propertyPath + ", session: " + callbackSessionId);
        }

        try {
            long cpExecStart = System.currentTimeMillis();

            // Resolve property value based on path prefix routing
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Resolving context property: " + propertyPath +
                        ", session: " + sessionId);
            }

            Object value;
            // Handle proxy object property access: "__proxyref__::<referenceId>::<property>"
            // This enables lazy loading of complex objects (e.g., User objects from getUsersWithClaimValues)
            if (propertyPath.startsWith(RemoteEngineConstants.PROXY_REF_PREFIX)) {
                value = proxyReferenceCache.getProxyObjectProperty(
                        propertyPath.substring(RemoteEngineConstants.PROXY_REF_PREFIX.length()));
            // Handle host function return references: "__hostref__::<refId>::<property>"
            } else if (propertyPath.startsWith(RemoteEngineConstants.HOST_REF_PREFIX)) {
                value = proxyReferenceCache.getHostRefProperty(
                        propertyPath.substring(RemoteEngineConstants.HOST_REF_PREFIX.length()));
            } else if (authContext == null) {
                log.warn("[RemoteJsEngine] No authContext available for property access");
                value = null;
            } else {
                // Navigate via the cache-aware resolver so repeated accesses can resume
                // from the longest previously cached intermediate in proxyObjectCache
                // instead of rebuilding from JsGraalAuthenticationContext on every callback.
                // Existing __proxyref__ / __hostref__ UUID-keyed entries are untouched —
                // context-path keys share the same map but never collide with UUIDs.
                JsGraalAuthenticationContext jsContext = new JsGraalAuthenticationContext(authContext);
                value = proxyReferenceCache.getContextPathProperty(propertyPath, jsContext);
            }

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Resolved context property '" + propertyPath + "' = " +
                        (value != null ? value.getClass().getSimpleName() : "null"));
            }

            long cpExecEnd = System.currentTimeMillis();

            ContextPropertyResponse.Builder responseBuilder = ContextPropertyResponse.newBuilder()
                    .setSuccess(true);

            // Handle __keys__ path - value is the member keys array
            if (propertyPath.endsWith(RemoteEngineConstants.PATH_SEPARATOR +
                    RemoteEngineConstants.KEYS_PROPERTY) ||
                    RemoteEngineConstants.KEYS_PROPERTY.equals(propertyPath)) {
                if (value != null) {
                    MemberKeyExtractor.extractTo(value, responseBuilder);
                }
                return StreamMessage.newBuilder()
                        .setSessionId(callbackSessionId)
                        .setContextPropertyResponse(responseBuilder.build())
                        .build();
            }

            if (value != null) {
                boolean isProxy = ProxyTypeResolver.isJsWrapperProxy(value);
                responseBuilder.setIsProxy(isProxy);

                if (isProxy) {
                    responseBuilder.setProxyType(ProxyTypeResolver.getJsWrapperProxyType(value));
                } else {
                    responseBuilder.setValue(Serializer.toProto(value));
                }
            }

            StreamMessage response = StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setContextPropertyResponse(responseBuilder.build())
                    .build();
            long cpSentTs = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                log.debug("[PERF] [" + cpSentTs + "] IS CTX_PROP_RESPONSE_BUILT session=" +
                        callbackSessionId + " path=" + propertyPath +
                        " handleStartTs=" + cpStart + " execStartTs=" + cpExecStart +
                        " execEndTs=" + cpExecEnd + " builtTs=" + cpSentTs +
                        " execMs=" + (cpExecEnd - cpExecStart) +
                        " serMs=" + (cpSentTs - cpExecEnd) +
                        " totalMs=" + (cpSentTs - cpStart));
            }
            return response;

        } catch (Exception e) {
            log.error("[GrpcStreaming] Error getting context property: " + propertyPath, e);
            return StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setContextPropertyResponse(ContextPropertyResponse.newBuilder()
                            .setSuccess(false)
                            .setErrorMessage(formatErrorForWire(e))
                            .build())
                    .build();
        }
    }

    /**
     * Handle a context property write callback request from the External.
     * Deserializes the value from protobuf and delegates to property setting.
     *
     * @param callbackSessionId The session identifier for the stream message.
     * @param request           The context property set request from the External.
     * @return StreamMessage containing the ContextPropertySetResponse.
     */
    public StreamMessage handleContextPropertySetCallback(String callbackSessionId,
                                                          ContextPropertySetRequest request) {

        String propertyPath = request.getPropertyPath();
        long cpsStart = System.currentTimeMillis();
        if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
            log.debug("[PERF] [" + cpsStart + "] IS CTX_PROP_SET_HANDLE_START session=" +
                    callbackSessionId + " path=" + propertyPath + " handleStartTs=" + cpsStart);
        }
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[GrpcStreaming] handleContextPropertySet: " + propertyPath +
                    ", session: " + callbackSessionId);
        }

        try {
            long cpsDeserStart = System.currentTimeMillis();
            Object javaValue = Serializer.fromProto(request.getValue());
            long cpsExecStart = System.currentTimeMillis();
            // Resolve property write based on path prefix routing
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Setting context property: " + propertyPath + " = " +
                        (javaValue != null ? javaValue.getClass().getSimpleName() : "null") +
                        ", session: " + sessionId);
            }

            boolean success;
            // Handle proxy object references (list elements from host function returns)
            if (propertyPath.startsWith(RemoteEngineConstants.PROXY_REF_PREFIX)) {
                success = proxyReferenceCache.setProxyObjectProperty(
                        propertyPath.substring(RemoteEngineConstants.PROXY_REF_PREFIX.length()), javaValue);
            // Handle host function return references
            } else if (propertyPath.startsWith(RemoteEngineConstants.HOST_REF_PREFIX)) {
                success = proxyReferenceCache.setHostRefProperty(
                        propertyPath.substring(RemoteEngineConstants.HOST_REF_PREFIX.length()), javaValue);
            } else if (authContext == null) {
                log.warn("[RemoteJsEngine] No authContext available for property write");
                success = false;
            } else {
                // Navigate to the parent object and set the final property via PropertyPathNavigator
                String[] parts = propertyPath.split(RemoteEngineConstants.PATH_SEPARATOR);
                if (parts.length == 0) {
                    success = false;
                } else {
                    JsGraalAuthenticationContext jsContext = new JsGraalAuthenticationContext(authContext);
                    success = PropertyPathNavigator.setProperty(parts, 0, jsContext, javaValue);
                    if (success) {
                        // Evict any cached navigation entry the write may have invalidated
                        // (exact path + descendants only). Ancestor entries remain valid
                        // live proxy references into the current authentication context.
                        proxyReferenceCache.invalidateContextPath(propertyPath);
                    }
                }
            }
            long cpsExecEnd = System.currentTimeMillis();

            StreamMessage response = StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setContextPropertySetResponse(ContextPropertySetResponse.newBuilder()
                            .setSuccess(success)
                            .build())
                    .build();
            long cpsSentTs = System.currentTimeMillis();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                log.debug("[PERF] [" + cpsSentTs + "] IS CTX_PROP_SET_RESPONSE_BUILT session=" +
                        callbackSessionId + " path=" + propertyPath +
                        " handleStartTs=" + cpsStart + " deserStartTs=" + cpsDeserStart +
                        " execStartTs=" + cpsExecStart + " execEndTs=" + cpsExecEnd +
                        " builtTs=" + cpsSentTs +
                        " deserMs=" + (cpsExecStart - cpsDeserStart) +
                        " execMs=" + (cpsExecEnd - cpsExecStart) +
                        " buildMs=" + (cpsSentTs - cpsExecEnd) +
                        " totalMs=" + (cpsSentTs - cpsStart));
            }
            return response;

        } catch (Exception e) {
            log.error("[GrpcStreaming] Error setting context property: " + propertyPath, e);
            return StreamMessage.newBuilder()
                    .setSessionId(callbackSessionId)
                    .setContextPropertySetResponse(ContextPropertySetResponse.newBuilder()
                            .setSuccess(false)
                            .setErrorMessage(formatErrorForWire(e))
                            .build())
                    .build();
        }
    }

    /**
     * Builds a concise error string for transmission over gRPC.
     * Includes the exception type, message (if any), and the top stack frame
     * so the sidecar log shows WHERE the failure happened without leaking the full trace.
     */
    private String formatErrorForWire(Exception e) {

        StringBuilder sb = new StringBuilder(e.toString());
        StackTraceElement[] stack = e.getStackTrace();
        if (stack.length > 0) {
            sb.append(" at ").append(stack[0]);
        }
        return sb.toString();
    }

    // ======================== Private Helpers ========================

    /**
     * Truncate a string for logging to avoid excessively long log messages.
     */
    private String truncateForLog(String value) {
        if (value == null) {
            return "null";
        }
        if (value.length() > 200) {
            return value.substring(0, 200) + "...[truncated]";
        }
        return value;
    }

    private void ensureConnected() throws IOException {
        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] ensureConnected - transport: " + transport.getClass().getSimpleName() +
                    ", connected: " + transport.isConnected());
        }
        if (!transport.isConnected()) {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Connecting transport");
            }
            transport.connect();
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Connected to remote engine successfully");
            }
        }
    }
}
