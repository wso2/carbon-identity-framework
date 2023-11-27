/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

/**
 * Base class for ThreadLocal Implementation of the Script Engine.
 * This holds different script engine instances between different
 * threads. Thread safety is achieved by binding the script
 * engine to the current running Thread.
 */
public abstract class BaseThreadLocalScriptEngineHolder {

    private static final ThreadLocal<ScriptEngine> threadLocalScriptEngine = new ThreadLocal<>();
    protected static final String[] NASHORN_ARGS = {"--no-java"};

    protected abstract void init();

    /**
     * Get the thread local script engine.
     *
     * @return ScriptEngine
     */
    public ScriptEngine getScriptEngine() {

        return threadLocalScriptEngine.get();
    }

    protected void setScriptEngine(ScriptEngine scriptEngine) {

        threadLocalScriptEngine.set(scriptEngine);
    }

    protected void setScriptContext(ScriptEngine scriptEngine) {

        ScriptContext scriptContext = new SimpleScriptContext();

        //Set the engine scope bindings.
        Bindings engineScopeBindings = scriptEngine.createBindings();
        scriptContext.setBindings(engineScopeBindings, ScriptContext.ENGINE_SCOPE);

        //Set the global scope bindings.
        Bindings globalScopeBindings = scriptEngine.createBindings();
        scriptContext.setBindings(globalScopeBindings, ScriptContext.GLOBAL_SCOPE);

        scriptEngine.setContext(scriptContext);
    }

    protected abstract ClassLoader getClassLoader();
}
