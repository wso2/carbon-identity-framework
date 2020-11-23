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

import com.oracle.truffle.js.scriptengine.GraalJSEngineFactory;
import org.graalvm.polyglot.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.management.ExecutionListener;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
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
public class JsPolyglotGraphBuilderFactory implements JsGraphBuilderFactory {

    private static final Log LOG = LogFactory.getLog(JsPolyglotGraphBuilderFactory.class);
    private static final String JS_BINDING_CURRENT_CONTEXT = "JS_BINDING_CURRENT_CONTEXT";

    private ScriptEngine engine;

    public void init() {

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

        GraalJSEngineFactory jsEngineFactory = new GraalJSEngineFactory();

        Engine graalEngine = jsEngineFactory.getPolyglotEngine();
        ExecutionListener listener = ExecutionListener.newBuilder()
                    .onEnter((e) -> System.out.println(e.getLocation().getStartLine()+" : "+
                            e.getLocation().getCharacters()))
                    .statements(true)
                    .attach(graalEngine);

        engine = jsEngineFactory.getScriptEngine();

        return engine;
    }

    public JsPolyglotGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                                Map<Integer, StepConfig> stepConfigMap) {

        return new JsPolyglotGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext));
    }

    public JsPolyglotGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                                Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode) {

        return new JsPolyglotGraphBuilder(authenticationContext, stepConfigMap,
                createEngine(authenticationContext), currentNode);
    }
}
