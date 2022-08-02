/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsSerializer;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Serializer class supports Open JDK Nashorn Engine.
 */
public class JsOpenJdkNashornSerializer implements JsSerializer {

    private static final Log log = LogFactory.getLog(JsOpenJdkNashornSerializer.class);

    private static JsOpenJdkNashornSerializer jsOpenJdkNashornSerializer = new JsOpenJdkNashornSerializer();

    public static JsOpenJdkNashornSerializer getInstance() {

        return jsOpenJdkNashornSerializer;
    }

    /**
     * Serialize the object using selected serializable function.
     * @param value Object to evaluate.
     * @return Serialized Object.
     */
    @Override
    public Object toJsSerializable(Object value) {

        return toJsSerializableInternal(value);
    }

    /**
     * Serialize the object using selected serializable function.
     * @param value Object to evaluate.
     * @return Serialized Object.
     */
    public static Object toJsSerializableInternal(Object value) {

        if (value instanceof Serializable) {
            if (value instanceof HashMap) {
                Map<String, Object> map = new HashMap<>();
                ((HashMap) value).forEach((k, v) -> map.put((String) k, toJsSerializableInternal(v)));
                return map;
            } else {
                return value;
            }
        } else if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
            if (scriptObjectMirror.isFunction()) {
                return OpenJdkNashornSerializableJsFunction.toSerializableForm(scriptObjectMirror);
            } else if (scriptObjectMirror.isArray()) {
                List<Serializable> arrayItems = new ArrayList<>(scriptObjectMirror.size());
                scriptObjectMirror.values().forEach(v -> {
                    Object serializedObj = toJsSerializableInternal(v);
                    if (serializedObj instanceof Serializable) {
                        arrayItems.add((Serializable) serializedObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Serialized the value of array item as : " + serializedObj);
                        }
                    } else {
                        log.warn(String.format("Non serializable array item: %s. and will not be persisted.",
                                serializedObj));
                    }
                });
                return arrayItems;
            } else if (!scriptObjectMirror.isEmpty()) {
                Map<String, Serializable> serializedMap = new HashMap<>();
                scriptObjectMirror.forEach((k, v) -> {
                    Object serializedObj = toJsSerializableInternal(v);
                    if (serializedObj instanceof Serializable) {
                        serializedMap.put(k, (Serializable) serializedObj);
                        if (log.isDebugEnabled()) {
                            log.debug("Serialized the value for key : " + k);
                        }
                    } else {
                        log.warn(String.format("Non serializable object for key : %s, and will not be persisted.", k));
                    }

                });
                return serializedMap;
            } else {
                return Collections.EMPTY_MAP;
            }
        }
        return value;
    }

    /**
     * De-Serialize the object using selected serializable function.
     * @param value Serialized Object.
     * @param engine Js Engine.
     * @return De-Serialize object.
     * @throws FrameworkException FrameworkException.
     */
    @Override
    public Object fromJsSerializable(Object value, ScriptEngine engine) throws FrameworkException {

        return fromJsSerializableInternal(value, engine);
    }

    /**
     * De-Serialize the object using selected serializable function.
     * @param value Serialized Object.
     * @param engine Js Engine.
     * @return De-Serialize object.
     * @throws FrameworkException FrameworkException.
     */
    public static Object fromJsSerializableInternal(Object value, ScriptEngine engine) throws FrameworkException {

        if (value instanceof OpenJdkNashornSerializableJsFunction) {
            OpenJdkNashornSerializableJsFunction serializableJsFunction = (OpenJdkNashornSerializableJsFunction) value;
            try {
                return engine.eval(serializableJsFunction.getSource());
            } catch (ScriptException e) {
                throw new FrameworkException("Error in resurrecting a Javascript Function : " + serializableJsFunction);
            }

        } else if (value instanceof Map) {
            Map<String, Object> deserializedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                Object deserializedObj = fromJsSerializableInternal(entry.getValue(), engine);
                deserializedMap.put(entry.getKey(), deserializedObj);
            }
            return deserializedMap;
        }
        return value;
    }
}
