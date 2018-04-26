/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of Nashorn engine and any related expnsive objects.
 */
public class JsGraphBuilderFactory {

    private static final String JS_BINDING_CURRENT_CONTEXT = "JS_BINDING_CURRENT_CONTEXT";
    private JsFunctionRegistry jsFunctionRegistry;
    private NashornScriptEngineFactory factory;

    private static final Log jsLog = LogFactory
            .getLog(JsGraphBuilder.class.getPackage().getName() + ".JsBasedSequence");

    public void init() {

        factory = new NashornScriptEngineFactory();
    }

    public ScriptEngine createEngine(AuthenticationContext authenticationContext) {

        ScriptEngine engine = factory.getScriptEngine("--no-java");

        Bindings bindings = engine.createBindings();
        engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        engine.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
                (SelectOneFunction) selectAcrFromFunction::evaluate);

        JsLogger jsLogger = new JsLogger();
        bindings.put(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);
        return engine;
    }

    public static void persistCurrentContext(AuthenticationContext context, ScriptEngine engine) {

        Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Map<String, Object> persistableMap = new HashMap<>();
        engineBindings.forEach((key, value) -> persistableMap.put(key, toJsSerializable(key, value)));
        context.setProperty(JS_BINDING_CURRENT_CONTEXT, persistableMap);
    }

    private static Object toJsSerializable(String name, Object value) {

        if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
            if (scriptObjectMirror.isFunction()) {
                return SerializableJsFunction.toSerializableForm(name, scriptObjectMirror);
            } else {
                return scriptObjectMirror;
            }
        }
        return value;
    }

    private static Object fromJsSerializable(Object value, ScriptEngine engine) throws FrameworkException {

        if (value instanceof SerializableJsFunction) {
            SerializableJsFunction serializableJsFunction = (SerializableJsFunction) value;
            try {
                Object fn = engine.eval(serializableJsFunction.getSource());
                return fn;
            } catch (ScriptException e) {
                throw new FrameworkException("Error in resurrecting a Javascript Function : " + serializableJsFunction);
            }

        }
        return value;
    }

    public static void restoreCurrentContext(AuthenticationContext context, ScriptEngine engine)
            throws FrameworkException {

        Map<String, Object> map = (Map<String, Object>) context.getProperty(JS_BINDING_CURRENT_CONTEXT);
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                bindings.put(entry.getKey(), fromJsSerializable(entry.getValue(), engine));
            }
        }
    }

    public JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
            Map<Integer, StepConfig> stepConfigMap) {

        JsGraphBuilder result = new JsGraphBuilder(authenticationContext, stepConfigMap,
                createEngine(authenticationContext));
        return result;
    }

    public JsFunctionRegistry getJsFunctionRegistry() {

        return jsFunctionRegistry;
    }

    public void setJsFunctionRegistry(JsFunctionRegistry jsFunctionRegistry) {

        this.jsFunctionRegistry = jsFunctionRegistry;
    }
}
