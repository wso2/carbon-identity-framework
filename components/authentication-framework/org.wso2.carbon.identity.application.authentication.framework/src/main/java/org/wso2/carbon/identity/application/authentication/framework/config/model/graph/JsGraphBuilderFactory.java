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
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.PersistableBindings;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of Nashorn engine and any related expnsive objects.
 */
public class JsGraphBuilderFactory {

    private static final String JS_BINDING_GLOBAL_SCOPE = "JS_BINDING_GLOBAL_SCOPE";
    private JsFunctionRegistryImpl jsFunctionRegistry;
    private ScriptEngineManager nashornScriptManager;

    private static final Log jsLog = LogFactory
            .getLog(JsGraphBuilder.class.getPackage().getName() + ".JsBasedSequence");

    public void init() {
        nashornScriptManager = new ScriptEngineManager();
    }

    public ScriptEngine createEngine(AuthenticationContext authenticationContext) {

        ScriptEngine engine = nashornScriptManager.getEngineByName("nashorn");
        PersistableBindings engineBindings = (PersistableBindings) authenticationContext.getProperty("JS_ENGINE_SCOPE");
        Bindings previousGlobalBindings = (Bindings) authenticationContext.getProperty(JS_BINDING_GLOBAL_SCOPE);
        PersistableBindings globalBindings = null;
        if (engineBindings == null) {
            if (previousGlobalBindings != null) {
                Bindings previousEngineBindings = new PersistableBindings(
                        (Map) previousGlobalBindings.get("nashorn.global"));
                engineBindings = new PersistableBindings(previousEngineBindings);
            } else {
                engineBindings = new PersistableBindings();
            }
        }
        if (globalBindings == null) {
            globalBindings = new PersistableBindings(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
        }
        authenticationContext.setProperty(JS_BINDING_GLOBAL_SCOPE, globalBindings);

        engine.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
        engine.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
        Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
        bindings.put("log", jsLog); //TODO: Depricated. Remove log.x()
        bindings.put("Log", jsLog); //TODO: Depricated. Remove log.x()
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
                (SelectOneFunction) selectAcrFromFunction::evaluate);

        JsLogger jsLogger = new JsLogger();
        bindings.put(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);
        return engine;
    }

    public JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
            Map<Integer, StepConfig> stepConfigMap) {
        JsGraphBuilder result = new JsGraphBuilder(authenticationContext, stepConfigMap,
                createEngine(authenticationContext));
        result.setJsFunctionRegistry(jsFunctionRegistry);
        return result;
    }

    public JsFunctionRegistryImpl getJsFunctionRegistry() {
        return jsFunctionRegistry;
    }

    public void setJsFunctionRegistry(JsFunctionRegistryImpl jsFunctionRegistry) {
        this.jsFunctionRegistry = jsFunctionRegistry;
    }
}
