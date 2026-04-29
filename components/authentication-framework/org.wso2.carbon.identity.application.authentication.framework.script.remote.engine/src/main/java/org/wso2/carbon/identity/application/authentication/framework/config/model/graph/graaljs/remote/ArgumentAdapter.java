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
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperFactoryProvider;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts arguments from External (protobuf-deserialized) format to Java types
 * expected by host function method signatures.
 * <p>
 * Handles:
 * <ul>
 *   <li>Context proxy marker reconstruction (External sends Map with __isContextProxy__ flag)</li>
 *   <li>Authenticated user direct reconstruction (bypasses proxy navigation for steps::N::subject)</li>
 *   <li>Primitive type coercion (protobuf Double to Integer/Long/Boolean/String)</li>
 *   <li>VarArgs method adaptation with null filtering</li>
 *   <li>Map number type coercion (protobuf deserializes all numbers as Double)</li>
 * </ul>
 */
class ArgumentAdapter {

    private static final Log log = LogFactory.getLog(ArgumentAdapter.class);

    private final AuthenticationContext authContext;

    ArgumentAdapter(AuthenticationContext authContext) {
        this.authContext = authContext;
    }

    /**
     * Adapt arguments to match the method's parameter types.
     * This handles reconstruction of JsAuthenticationContext and type conversions.
     *
     * @param method The method to adapt arguments for.
     * @param args   The raw arguments from the External.
     * @return Adapted arguments matching the method's parameter types.
     */
    Object[] adaptArgumentsForMethod(Method method, Object[] args) {

        Class<?>[] paramTypes = method.getParameterTypes();
        boolean isVarArgs = method.isVarArgs();

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] adaptArgumentsForMethod: paramCount=" + paramTypes.length +
                    ", argsCount=" + (args != null ? args.length : 0) +
                    ", isVarArgs=" + isVarArgs);
        }

        if (args == null) {
            args = new Object[0];
        }

        Object[] adapted = new Object[paramTypes.length];

        int fixedCount = isVarArgs ? paramTypes.length - 1 : paramTypes.length;

        adaptFixedArguments(method, args, paramTypes, adapted, fixedCount);

        if (isVarArgs) {
            adapted[fixedCount] = adaptVarArgsTail(method, args, paramTypes[fixedCount], fixedCount);
        }

        return adapted;
    }

    private void adaptFixedArguments(Method method, Object[] args, Class<?>[] paramTypes,
                                     Object[] adapted, int fixedCount) {

        for (int i = 0; i < fixedCount; i++) {

            if (i >= args.length) {
                throw new IllegalArgumentException(
                        "Missing argument at index " + i + " for method " + method.getName()
                );
            }

            Object arg = args[i];
            Class<?> paramType = paramTypes[i];

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Adapting arg[" + i + "] from " +
                        (arg != null ? arg.getClass().getSimpleName() : "null") +
                        " to " + paramType.getSimpleName());
            }

            adapted[i] = adaptSingleArgument(arg, paramType);
        }
    }

    private Object adaptVarArgsTail(Method method, Object[] args,
                                    Class<?> varArgArrayType, int startIndex) {

        Class<?> componentType = varArgArrayType.getComponentType();

        List<Object> varArgsList = new ArrayList<>();

        for (int i = startIndex; i < args.length; i++) {

            Object raw = args[i];

            if (raw == null) {
                throw new IllegalArgumentException(
                        "Null value not allowed in varargs at index " + i +
                                " for method " + method.getName()
                );
            }

            Object adapted = adaptSingleArgument(raw, componentType);
            varArgsList.add(adapted);

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Adapting vararg[" + (i - startIndex) + "] from " +
                        raw.getClass().getSimpleName() + " to " + componentType.getSimpleName());
            }
        }

        Object array = Array.newInstance(componentType, varArgsList.size());
        for (int i = 0; i < varArgsList.size(); i++) {
            Array.set(array, i, varArgsList.get(i));
        }

        return array;
    }

    /**
     * Adapt a single argument to the target parameter type.
     */
    @SuppressWarnings("unchecked")
    Object adaptSingleArgument(Object arg, Class<?> paramType) {
        if (arg == null) {
            return null;
        }

        if (arg instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) arg;
            if (Boolean.TRUE.equals(map.get(RemoteEngineConstants.IS_CONTEXT_PROXY))) {
                String proxyType = (String) map.get(RemoteEngineConstants.PROXY_TYPE_FIELD);
                String basePath = (String) map.get(RemoteEngineConstants.BASE_PATH_FIELD);
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Received context proxy marker: type=" + proxyType +
                            ", basePath=" + basePath);
                }

                // Reconstruct the actual object based on proxyType and basePath
                Object reconstructed = reconstructFromProxy( basePath );
                if (reconstructed == null) {
                    throw new IllegalStateException(
                            "Failed to reconstruct context proxy: type=" + proxyType +
                                    ", basePath=" + basePath
                    );
                }
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Reconstructed " + reconstructed.getClass().getSimpleName() +
                            " from context proxy marker");
                }
                return reconstructed;
            }
        }

        // Handle Integer conversion.
        if (paramType == Integer.class || paramType == int.class) {
            if (arg instanceof Number) {
                return ((Number) arg).intValue();
            } else if (arg instanceof String) {
                try {
                    return Integer.parseInt((String) arg);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid integer value: " + arg);
                }
            }
        }

        // Handle Long conversion.
        if (paramType == Long.class || paramType == long.class) {
            if (arg instanceof Number) {
                return ((Number) arg).longValue();
            }
        }

        // Handle Double conversion.
        if (paramType == Double.class || paramType == double.class) {
            if (arg instanceof Number) {
                return ((Number) arg).doubleValue();
            }
        }

        // Handle Boolean conversion.
        if (paramType == Boolean.class || paramType == boolean.class) {

            if (arg instanceof Boolean) {return arg;}
            if (arg instanceof String) {
                String value = ((String) arg).trim();

                if ("true".equalsIgnoreCase(value)) {return true;}
                if ("false".equalsIgnoreCase(value)) {return false;}
                throw new IllegalArgumentException("Invalid boolean string value: '" + arg + "'");
            }
            throw new IllegalArgumentException("Cannot convert type " + arg.getClass().getName() +
                            " to boolean for value: " + arg);
        }

        // Handle String conversion.
        if (paramType == String.class) {
            return arg.toString();
        }

        // Handle List type conversion.
        if (List.class.isAssignableFrom(paramType)) {
            if (arg instanceof List) {
                return arg;
            } else if (arg instanceof Object[]) {
                return Arrays.asList((Object[]) arg);
            }
        }

        // Handle array type conversion.
        if (paramType.isArray()) {
            if (arg instanceof List) {
                List<?> list = (List<?>) arg;
                Class<?> componentType = paramType.getComponentType();
                if (componentType == String.class) {
                    return list.toArray(new String[0]);
                } else {
                    return list.toArray();
                }
            }
        }

        if (paramType == Object.class) {
            if (arg instanceof Map) {
                Map<String, Object> mapArg = (Map<String, Object>) arg;
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("[RemoteJsEngine] Coercing number types in Map with " + mapArg.size() + " entries");
                }
                return coerceMapNumberTypes(mapArg);
            }
            return arg;
        }

        // Direct assignment for compatible types.
        return arg;
    }

    /**
     * Coerce whole-number Double values inside a Map to Integer.
     * Creates a new mutable HashMap to avoid issues with immutable/protobuf maps.
     * Protobuf deserializes all JS numbers as Double, but Java host functions
     * expect Integer for integer-valued options (e.g., max-age in setCookie).
     */
    @SuppressWarnings("unchecked")
    Map<String, Object> coerceMapNumberTypes(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>(map);
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Double) {
                double d = (Double) val;
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    // Whole number — convert to Integer (or Long if out of int range)
                    if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
                        entry.setValue((int) d);
                        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                            log.debug("[RemoteJsEngine] Coerced " + entry.getKey() + ": " + d + " -> " + (int) d);
                        }
                    } else {
                        entry.setValue((long) d);
                    }
                }
            } else if (val instanceof Map) {
                entry.setValue(coerceMapNumberTypes((Map<String, Object>) val));
            }
        }
        return result;
    }

    /**
     * Reconstruct a context object from a proxy marker sent by the External.
     * This handles nested properties like context.currentKnownSubject,
     * context.steps[1], etc.
     *
     * @param basePath  The path to the property (e.g., "", "currentKnownSubject",
     *                  "steps::1")
     * @return The reconstructed object, or null if reconstruction fails
     */
    Object reconstructFromProxy(String basePath) {

        // If basePath is empty or null, return the full context
        if (basePath == null || basePath.isEmpty()) {
            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Reconstructing root context");
            }
            return new JsGraalAuthenticationContext(authContext);
        }

        if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
            log.debug("[RemoteJsEngine] Navigating to nested property: " + basePath);
        }

        try {
            String[] pathParts = basePath.split(RemoteEngineConstants.PATH_SEPARATOR);
            Object root = new JsGraalAuthenticationContext(authContext);
            Object result = PropertyPathNavigator.navigatePath(pathParts, 0, root);

            if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                log.debug("[RemoteJsEngine] Successfully navigated to: " + basePath +
                        ", result type: " + (result != null ? result.getClass().getSimpleName() : "null"));
            }
            return result;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to reconstruct proxy for path: " + basePath, e
            );
        }
    }
}
