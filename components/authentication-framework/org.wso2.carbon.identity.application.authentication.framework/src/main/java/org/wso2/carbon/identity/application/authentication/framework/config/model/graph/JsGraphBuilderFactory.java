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
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.store.JavascriptCache;

import java.util.Map;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of Nashorn engine and any related expnsive objects.
 */
public class JsGraphBuilderFactory {

    private JavascriptCache javascriptCache;
    private JsFunctionRegistryImpl jsFunctionRegistry;
    private ScriptEngine engine = null;

    private static final Log jsLog = LogFactory
            .getLog(JsGraphBuilder.class.getPackage().getName() + ".JsBasedSequence");

    public void init() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.put("log", jsLog); //TODO: Depricated. Remove log.x()
        engine.put("Log", jsLog);
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        engine.put("selectAcrFrom", (SelectOneFunction) selectAcrFromFunction::evaluate);
    }

    public JsGraphBuilder createBuilder(AuthenticationContext authenticationContext,
            Map<Integer, StepConfig> stepConfigMap) {
        JsGraphBuilder result =   new JsGraphBuilder(authenticationContext, stepConfigMap, engine);
        result.setJavascriptCache(javascriptCache);
        result.setJsFunctionRegistry(jsFunctionRegistry);
        return result;
    }

    public ScriptEngine getEngine() {
        return engine;
    }

    public JavascriptCache getJavascriptCache() {
        return javascriptCache;
    }

    public JsFunctionRegistryImpl getJsFunctionRegistry() {
        return jsFunctionRegistry;
    }

    public void setJsFunctionRegistry(JsFunctionRegistryImpl jsFunctionRegistry) {
        this.jsFunctionRegistry = jsFunctionRegistry;
    }

    public void setJavascriptCache(JavascriptCache javascriptCache) {
        this.javascriptCache = javascriptCache;
    }
}
