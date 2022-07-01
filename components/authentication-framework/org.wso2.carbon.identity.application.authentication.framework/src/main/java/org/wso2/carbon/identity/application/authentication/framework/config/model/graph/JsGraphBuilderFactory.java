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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openjdk.nashorn.api.scripting.ClassFilter;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of Nashorn engine and any related expnsive objects.
 */
public class JsGraphBuilderFactory {

    private static final Log LOG = LogFactory.getLog(JsGraphBuilderFactory.class);
    private static final String JS_BINDING_CURRENT_CONTEXT = "JS_BINDING_CURRENT_CONTEXT";
    private static final String[] NASHORN_ARGS = {"--no-java"};
    private ClassFilter classFilter;

    // Suppress the Nashorn deprecation warnings in jdk 11
    @SuppressWarnings("removal")
    private NashornScriptEngineFactory factory;


    public void init() {

        factory = new NashornScriptEngineFactory();
        classFilter = new RestrictedClassFilter();
    }

    public static void restoreCurrentContext(AuthenticationContext context, ScriptEngine engine)
        throws FrameworkException {

        Map<String, Object> map = (Map<String, Object>) context.getProperty(JS_BINDING_CURRENT_CONTEXT);
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object deserializedValue = FrameworkUtils.fromJsSerializable(entry.getValue(), engine);
                if (deserializedValue instanceof AbstractJSObjectWrapper) {
                    ((AbstractJSObjectWrapper) deserializedValue).initializeContext(context);
                }
                bindings.put(entry.getKey(), deserializedValue);
            }
        }
    }

    public static void persistCurrentContext(AuthenticationContext context, ScriptEngine engine) {

        Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        Map<String, Object> persistableMap = new HashMap<>();
        engineBindings.forEach((key, value) -> persistableMap.put(key, FrameworkUtils.toJsSerializable(value)));
        context.setProperty(JS_BINDING_CURRENT_CONTEXT, persistableMap);
    }

    public ScriptEngine createEngine(AuthenticationContext authenticationContext) {

        ScriptEngine engine = factory.getScriptEngine(NASHORN_ARGS, getClassLoader(), classFilter);
        Bindings bindings = engine.createBindings();
        engine.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
        engine.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
//        todo move to functions registry
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
            (SelectOneFunction) selectAcrFromFunction::evaluate);

        JsLogger jsLogger = new JsLogger();
        bindings.put(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);
        return engine;
    }

    private ClassLoader getClassLoader() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? NashornScriptEngineFactory.class.getClassLoader() : classLoader;
    }

    public JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
            Map<Integer, StepConfig> stepConfigMap) {

        return new JsGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext));
    }

    public JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                        Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode) {

        return new JsGraphBuilder(authenticationContext, stepConfigMap,
                createEngine(authenticationContext), currentNode);
    }
}
