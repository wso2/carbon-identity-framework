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
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.JsGraalGraphEngineModeRouter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pre-computed dispatch table for host function invocation.
 * <p>
 * Replaces the per-invocation reflection scan in RemoteJsEngine.handleHostFunctionCallback()
 * with O(1) HashMap lookup. Methods are resolved once at registration time via
 * {@link #register(Map)}, and each invocation goes through
 * {@link #invoke(String, ArgumentAdapter, Object[])}.
 * <p>
 * Method resolution strategy (same priority order as the original code):
 * 1. First method annotated with {@code @org.graalvm.polyglot.HostAccess.Export}
 * 2. Fallback: first non-default interface method
 * <p>
 * Fails fast at registration time if no callable method is found for a host function,
 * rather than failing silently during a user's authentication flow.
 */
class HostFunctionRegistry {

    private static final Log log = LogFactory.getLog(HostFunctionRegistry.class);

    private final Map<String, MethodInvoker> dispatchTable = new ConcurrentHashMap<>();

    /**
     * Register host functions by scanning each object for its callable method.
     * This builds the dispatch table — one entry per function name.
     *
     * @param hostFunctions Map of function name to host function implementation object.
     * @throws IllegalStateException if a host function object has no callable method.
     */
    void register(Map<String, Object> hostFunctions) {

        for (Map.Entry<String, Object> entry : hostFunctions.entrySet()) {
            String functionName = entry.getKey();
            Object hostFuncInstance = entry.getValue();

            Method targetMethod = findCallableMethod(hostFuncInstance);
            if (targetMethod == null) {
                throw new IllegalStateException(
                        "No callable method found for host function: " + functionName +
                                " (class: " + hostFuncInstance.getClass().getName() +
                                "). Expected either @HostAccess.Export annotation or a non-default interface method.");
            }

            MethodInvoker invoker = new MethodInvoker(hostFuncInstance, targetMethod);
            dispatchTable.put(functionName, invoker);

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[HostFunctionRegistry] Registered '" + functionName + "' -> " +
                        targetMethod.getDeclaringClass().getSimpleName() + "." + targetMethod.getName() +
                        ", params=" + targetMethod.getParameterCount() +
                        ", varArgs=" + targetMethod.isVarArgs());
            }
        }
    }

    /**
     * Invoke a host function by name. O(1) dispatch table lookup.
     *
     * @param functionName    The function name as called from JavaScript.
     * @param argumentAdapter The adapter to coerce arguments to match the method signature.
     * @param args            Raw arguments from the External (protobuf-deserialized).
     * @return The host function return value.
     * @throws Exception if the host function throws or if the function name is unknown.
     */
    Object invoke(String functionName, ArgumentAdapter argumentAdapter, Object[] args) throws Exception {

        MethodInvoker invoker = dispatchTable.get(functionName);
        if (invoker == null) {
            log.error("[HostFunctionRegistry] Unknown host function: " + functionName +
                    ", registered: " + dispatchTable.keySet());
            throw new IllegalArgumentException("Unknown host function: " + functionName);
        }

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[HostFunctionRegistry] Dispatching '" + functionName + "' -> " +
                    invoker.getMethod().getName() + ", params=" + invoker.getParameterTypes().length +
                    ", args=" + (args != null ? args.length : 0));
        }

        Object[] adaptedArgs = argumentAdapter.adaptArgumentsForMethod(invoker.getMethod(), args);

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            for (int i = 0; i < adaptedArgs.length; i++) {
                log.debug("[HostFunctionRegistry] Adapted arg[" + i + "]: type=" +
                        (adaptedArgs[i] != null ? adaptedArgs[i].getClass().getName() : "null"));
            }
        }

        try {
            Object result = invoker.invoke(adaptedArgs);
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[HostFunctionRegistry] '" + functionName + "' returned: " +
                        (result != null ? result.getClass().getName() + "=" + result : "null"));
            }
            return result;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            log.error("[HostFunctionRegistry] Host function '" + functionName + "' threw exception: " +
                    (cause != null ? cause.getClass().getName() + ": " + cause.getMessage() : e.getMessage()));
            if (cause != null) {
                log.error("[HostFunctionRegistry] Root cause stack trace:", cause);
            }
            throw e;
        }
    }

    /**
     * Check if a function name is registered.
     */
    boolean contains(String functionName) {

        return dispatchTable.containsKey(functionName);
    }

    /**
     * Get all registered function names (for request building).
     */
    Iterable<String> getFunctionNames() {

        return dispatchTable.keySet();
    }

    /**
     * Find the callable method on a host function object.
     * Same priority order as the original host function dispatch:
     * 1. First @HostAccess.Export annotated method
     * 2. First non-default interface method
     */
    private static Method findCallableMethod(Object instance) {

        // Look for @HostAccess.Export annotation
        for (Method method : instance.getClass().getMethods()) {
            if (method.isAnnotationPresent(org.graalvm.polyglot.HostAccess.Export.class)) {
                return method;
            }
        }
        return null;
    }

    /**
     * Cached wrapper around a reflective method invocation.
     * Pre-computes parameter types and varargs flag to avoid repeated Method introspection.
     */
    static class MethodInvoker {

        private final Object targetInstance;
        private final Method method;
        private final Class<?>[] parameterTypes;
        private final boolean varArgs;

        MethodInvoker(Object targetInstance, Method method) {

            this.targetInstance = targetInstance;
            this.method = method;
            this.parameterTypes = method.getParameterTypes();
            this.varArgs = method.isVarArgs();
        }

        Object invoke(Object[] args) throws Exception {

            return method.invoke(targetInstance, args);
        }

        Method getMethod() {

            return method;
        }

        Class<?>[] getParameterTypes() {

            return parameterTypes;
        }

        boolean isVarArgs() {

            return varArgs;
        }
    }
}
