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

import com.google.protobuf.ByteString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.GraalSerializer;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.JsGraalGraphEngineModeRouter;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedArray;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedMap;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.remote.proto.SerializedValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializer for converting Java objects to/from Protocol Buffers
 * SerializedValue.
 * Leverages existing GraalSerializer patterns for JavaScript value conversion.
 */
class Serializer {

    private static final Log log = LogFactory.getLog(Serializer.class);

    // Thread-local session proxy cache for storing complex objects
    // This is set per-session by RemoteJsEngine and used during serialization
    private static final ThreadLocal<Map<String, Object>> sessionProxyCache = new ThreadLocal<>();

    private Serializer() {
        // Utility class
    }

    /**
     * Set the session proxy cache for the current thread.
     * This should be called by RemoteJsEngine before serialization.
     *
     * @param cache The proxy object cache for this session.
     */
    public static void setSessionProxyCache(Map<String, Object> cache) {
        sessionProxyCache.set(cache);
    }

    /**
     * Clear the session proxy cache for the current thread.
     */
    public static void clearSessionProxyCache() {
        sessionProxyCache.remove();
    }

    /**
     * Get the session proxy cache for the current thread.
     *
     * @return The proxy cache, or null if not set.
     */
    private static Map<String, Object> getSessionProxyCache() {
        return sessionProxyCache.get();
    }

    /**
     * Convert a Java object to Protocol Buffers SerializedValue.
     *
     * @param value The Java object to serialize.
     * @return SerializedValue protobuf message.
     */
    public static SerializedValue toProto(Object value) {
        if (value == null) {
            return SerializedValue.newBuilder()
                    .setNullValue(ByteString.EMPTY)
                    .build();
        }

        // First convert using GraalSerializer if needed
        Object serializable = GraalSerializer.toJsSerializableInternal(value);

        if (serializable == null) {
            return SerializedValue.newBuilder()
                    .setNullValue(ByteString.EMPTY)
                    .build();
        }

        if (serializable instanceof String) {
            return SerializedValue.newBuilder()
                    .setStringValue((String) serializable)
                    .build();
        }

        if (serializable instanceof Integer) {
            return SerializedValue.newBuilder()
                    .setIntValue(((Integer) serializable).longValue())
                    .build();
        }

        if (serializable instanceof Long) {
            return SerializedValue.newBuilder()
                    .setIntValue((Long) serializable)
                    .build();
        }

        if (serializable instanceof Double) {
            return SerializedValue.newBuilder()
                    .setDoubleValue((Double) serializable)
                    .build();
        }

        if (serializable instanceof Float) {
            return SerializedValue.newBuilder()
                    .setDoubleValue(((Float) serializable).doubleValue())
                    .build();
        }

        if (serializable instanceof Boolean) {
            return SerializedValue.newBuilder()
                    .setBoolValue((Boolean) serializable)
                    .build();
        }

        if (serializable instanceof GraalSerializableJsFunction) {
            GraalSerializableJsFunction jsFunc = (GraalSerializableJsFunction) serializable;
            return SerializedValue.newBuilder()
                    .setFunctionValue(SerializedFunction.newBuilder()
                            .setSource(jsFunc.getSource())
                            .build())
                    .build();
        }

        if (serializable instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapValue = (Map<String, Object>) serializable;
            SerializedMap.Builder mapBuilder = SerializedMap.newBuilder();
            for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
                mapBuilder.putEntries(entry.getKey(), toProto(entry.getValue()));
            }
            return SerializedValue.newBuilder()
                    .setMapValue(mapBuilder.build())
                    .build();
        }

        if (serializable instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> listValue = (List<Object>) serializable;
            if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                log.debug("[Serializer] Serializing List with " + listValue.size() + " elements");
            }
            SerializedArray.Builder arrayBuilder = SerializedArray.newBuilder();
            for (int i = 0; i < listValue.size(); i++) {
                Object element = listValue.get(i);
                if (JsGraalGraphEngineModeRouter.isTracingEnabled()) {
                    log.debug("[Serializer] List element[" + i + "] type: " +
                            (element != null ? element.getClass().getName() : "null"));
                }
                arrayBuilder.addElements(toProto(element));
            }
            return SerializedValue.newBuilder()
                    .setArrayValue(arrayBuilder.build())
                    .build();
        }

        // Handle Java arrays (e.g., String[] from request params)
        if (serializable.getClass().isArray()) {
            Object[] arrayValue;
            if (serializable instanceof Object[]) {
                arrayValue = (Object[]) serializable;
            } else {
                // Primitive arrays - convert to Object[]
                int length = java.lang.reflect.Array.getLength(serializable);
                arrayValue = new Object[length];
                for (int i = 0; i < length; i++) {
                    arrayValue[i] = java.lang.reflect.Array.get(serializable, i);
                }
            }
            SerializedArray.Builder arrayBuilder = SerializedArray.newBuilder();
            for (Object element : arrayValue) {
                arrayBuilder.addElements(toProto(element));
            }
            return SerializedValue.newBuilder()
                    .setArrayValue(arrayBuilder.build())
                    .build();
        }

        // Handle ProxyArray (e.g., from JsGraalParameters.processParameterMember)
        if (serializable instanceof org.graalvm.polyglot.proxy.ProxyArray) {
            org.graalvm.polyglot.proxy.ProxyArray proxyArray =
                    (org.graalvm.polyglot.proxy.ProxyArray) serializable;
            SerializedArray.Builder arrayBuilder = SerializedArray.newBuilder();
            long size = proxyArray.getSize();
            for (long i = 0; i < size; i++) {
                arrayBuilder.addElements(toProto(proxyArray.get(i)));
            }
            return SerializedValue.newBuilder()
                    .setArrayValue(arrayBuilder.build())
                    .build();
        }

        // Handle JsGraal* proxy types (ProxyObject/ProxyArray implementations).
        // Two sub-cases:
        // 1. JsGraalParameters/JsGraalWritableParameters: unwrap the inner Map and serialize
        //    it directly. Critical for httpGet/httpPost response data.
        // 2. All other proxies (JsGraalAuthenticatedUser, JsGraalStep, etc.): store in the
        //    session proxy cache for lazy property loading via __proxyref__ path.
        if (ProxyTypeResolver.isJsWrapperProxy(serializable)) {
            if (serializable instanceof org.wso2.carbon.identity.application.authentication.framework
                    .config.model.graph.js.JsParameters) {
                return toProto(((org.wso2.carbon.identity.application.authentication.framework
                        .config.model.graph.js.JsParameters) serializable).getWrapped());
            }

            Map<String, Object> cache = getSessionProxyCache();
            if (cache != null) {
                String referenceId = java.util.UUID.randomUUID().toString();
                cache.put(referenceId, serializable);

                String proxyType = ProxyTypeResolver.getJsWrapperProxyType(serializable);
                if (JsGraalGraphEngineModeRouter.isTracingEnabled() && log.isDebugEnabled()) {
                    log.debug("Created explicit proxy marker for nested JS Wrapper: " +
                            serializable.getClass().getName() + " with referenceId: " + referenceId);
                }

                return SerializedValue.newBuilder()
                        .setProxyObject(SerializedProxyObject.newBuilder()
                                .setType(proxyType)
                                .setReferenceId(referenceId)
                                .build())
                        .build();
            } else {
                log.warn("Proxy cache not set, cannot create proxy marker for JS Wrapper: " +
                        serializable.getClass().getName());
            }
        }

        Map<String, Object> cache = getSessionProxyCache();

        throw new IllegalArgumentException(
                "Unhandled type in Serializer.toProto(): " + serializable.getClass().getName() +
                ". Add an explicit serialization handler for this type. " +
                "Proxy cache was: " + (cache != null ? "available" : "NULL"));
    }

    /**
     * Convert a Protocol Buffers SerializedValue to Java object.
     *
     * @param sv The SerializedValue to deserialize.
     * @return Java object.
     */
    public static Object fromProto(SerializedValue sv) {
        if (sv == null) {
            return null;
        }

        switch (sv.getValueCase()) {
            case STRING_VALUE:
                String s = sv.getStringValue();
                if (s != null && (s.trim().startsWith("function") || s.contains("=>"))) {
                    log.warn("[typing-probe] STRING_VALUE looks function-like: key context unknown, len="
                            + s.length());
                }
                return s;

            case INT_VALUE:
                return sv.getIntValue();

            case DOUBLE_VALUE:
                return sv.getDoubleValue();

            case BOOL_VALUE:
                return sv.getBoolValue();

            case NULL_VALUE:
                return null;

            case MAP_VALUE:
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<String, SerializedValue> entry : sv.getMapValue().getEntriesMap().entrySet()) {
                    map.put(entry.getKey(), fromProto(entry.getValue()));
                }
                return map;

            case ARRAY_VALUE:
                List<Object> list = new ArrayList<>();
                for (SerializedValue element : sv.getArrayValue().getElementsList()) {
                    list.add(fromProto(element));
                }
                return list;

            case FUNCTION_VALUE:
                SerializedFunction func = sv.getFunctionValue();
                return new GraalSerializableJsFunction(func.getSource(), true);

            case PROXY_OBJECT:
                // For proxy objects, return a placeholder map with type info
                SerializedProxyObject proxy = sv.getProxyObject();
                Map<String, Object> proxyMap = new HashMap<>();
                proxyMap.put(RemoteEngineConstants.PROXY_TYPE_FIELD, proxy.getType());
                proxyMap.put(RemoteEngineConstants.REFERENCE_ID_FIELD, proxy.getReferenceId());
                return proxyMap;

            default:
                log.error("Unknown SerializedValue case: " + sv.getValueCase() +
                        ". This may indicate a version mismatch between IS and sidecar.");
                throw new IllegalArgumentException("Unknown SerializedValue case: " + sv.getValueCase());
        }
    }

    /**
     * Serialize a map of bindings to protobuf map.
     *
     * @param bindings The bindings map.
     * @return Map of string to SerializedValue.
     */
    public static Map<String, SerializedValue> toProtoMap(Map<String, Object> bindings) {
        Map<String, SerializedValue> result = new HashMap<>();
        if (bindings != null) {
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                result.put(entry.getKey(), toProto(entry.getValue()));
            }
        }
        return result;
    }

    /**
     * Deserialize a protobuf map to Java bindings map.
     *
     * @param protoMap The protobuf map.
     * @return Java bindings map.
     */
    public static Map<String, Object> fromProtoMap(Map<String, SerializedValue> protoMap) {
        Map<String, Object> result = new HashMap<>();
        if (protoMap != null) {
            for (Map.Entry<String, SerializedValue> entry : protoMap.entrySet()) {
                result.put(entry.getKey(), fromProto(entry.getValue()));
            }
        }
        return result;
    }
}
