/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsSerializer;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE;

/**
 * Serializer class supports GraalJS Engine.
 */
public class GraalSerializer implements JsSerializer<Context> {

    private static final Log log = LogFactory.getLog(GraalSerializer.class);
    private static final GraalSerializer graalSerializer = new GraalSerializer();

    public static GraalSerializer getInstance() {

        return graalSerializer;
    }

    @Override
    public Object toJsSerializable(Object value) {

        return toJsSerializableInternal(value);
    }

    public static Object toJsSerializableInternal(Object value) {

        if (value instanceof Serializable) {
            if (value instanceof HashMap) {
                Map<String, Object> map = new HashMap<>();
                ((HashMap<String, Object>) value).forEach((k, v) -> map.put(k, toJsSerializableInternal(v)));
                return map;
            } else {
                return value;
            }
        } else if (value instanceof Map) {
            // Polyglot Map is not serializable. Hence, converting to HashMap.
            Map<String, Object> map = new HashMap<>();
            ((Map<String, Object>) value).forEach((k, v) -> map.put(k, toJsSerializableInternal(v)));
            return map;
        } else if (value instanceof List) {
            // Polyglot List is not serializable. Hence, converting to ArrayList.
            List<Object> list = new ArrayList<>();
            ((List<Object>) value).forEach(v -> list.add(toJsSerializableInternal(v)));
            return list;
        } else if (value instanceof Value) {
            Value valueObj = (Value) value;
            if (valueObj.canExecute()) {
                return GraalSerializableJsFunction.toSerializableForm(valueObj);
            } else if (valueObj.isProxyObject()) {
                return valueObj.asProxyObject();
            } else if (valueObj.isNumber()) {
                return valueObj.asInt();
            } else if (valueObj.isString()) {
                return valueObj.toString();
            } else if (valueObj.isDate()) {
                return valueObj.asDate();
            } else if (valueObj.isBoolean()) {
                return valueObj.asBoolean();
            } else if (valueObj.isDuration()) {
                return valueObj.asDuration();
            } else if (valueObj.isTime()) {
                return valueObj.asTime();
            } else if (valueObj.isTimeZone()) {
                return valueObj.asTimeZone();
            } else if (valueObj.isNull()) {
                return null;
            } else if (valueObj.hasArrayElements()) {
                int arraySize = (int) valueObj.getArraySize();
                List<Serializable> arrayItems = new ArrayList<>(arraySize);
                for (int key = 0; key < arraySize; key++) {
                    Object arrayObj = valueObj.getArrayElement(key);
                    Object serializedObj = toJsSerializableInternal(arrayObj);
                    arrayItems.add((Serializable) serializedObj);
                }
                return arrayItems;
            } else if (valueObj.hasMembers()) {
                Map<String, Serializable> serializedMap = new HashMap<>();
                valueObj.getMemberKeys().forEach((key) -> {
                    Object serializedObj = toJsSerializableInternal(valueObj.getMember(key));
                    if (serializedObj instanceof Serializable) {
                        serializedMap.put(key, (Serializable) serializedObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Serialized the value for key : " + key);
                        }
                    } else {
                        log.warn(
                                String.format("Non serializable object for key : %s, and will not be persisted.", key));
                    }
                });
                return serializedMap;
            } else {
                return Collections.EMPTY_MAP;
            }
        }
        return value;
    }

    @Override
    public Object fromJsSerializable(Object value, Context engine) throws FrameworkException {

        return fromJsSerializableInternal(value, engine);
    }

    public static Object fromJsSerializableInternal(Object value, Context context) throws FrameworkException {

        if (value instanceof GraalSerializableJsFunction) {
            GraalSerializableJsFunction serializableJsFunction = (GraalSerializableJsFunction) value;
            try {
                return context.eval("js", "(" + serializableJsFunction.getSource() + ")");
            } catch (Exception e) {
                log.error("Error when recreating JS Object", e);
            }
        } else if (value instanceof Map) {
            Map<String, Object> deserializedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                Object deserializedObj = fromJsSerializableInternal(entry.getValue(), context);
                deserializedMap.put(entry.getKey(), deserializedObj);
            }
            return deserializedMap;
        } else if (value instanceof List) {
            Value deserializedValue = context.eval(POLYGLOT_LANGUAGE, "[]");
            List<?> valueList = (List<?>) value;
            int listSize = valueList.size();
            for (int index = 0; index < listSize; index++) {
                Object deserializedObject = fromJsSerializableInternal(valueList.get(index), context);
                deserializedValue.setArrayElement(index, deserializedObject);
            }
            return deserializedValue;
        }
        return value;
    }
}
