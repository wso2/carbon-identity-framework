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

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * ThreadLocal Implementation of the Script Engine.
 * This holds different script engine between different
 * threads. Thread safety is achieved by binding the script
 * engine to the current running Thread
 */
public class ThreadLocalScriptEngineWrapper extends AbstractScriptEngine {

    private static final ThreadLocal<ScriptEngine> threadLocalScriptEngineHolder = new ThreadLocal<>();
    private Bindings engineScopeBindings;
    private Bindings globalScopeBindings;
    private ScriptContext scriptContext;
    @SuppressWarnings("removal")
    private NashornScriptEngineFactory factory;
    private ClassFilter classFilter;
    private static final String[] NASHORN_ARGS = {"--no-java"};

    public ThreadLocalScriptEngineWrapper() {

        init(); //Needs to initialize the Script Engine each time.
    }

    public ScriptEngine init() {

        ScriptEngine scriptEngine = threadLocalScriptEngineHolder.get();
        if (scriptEngine == null) {
            factory = new NashornScriptEngineFactory();
            classFilter = new RestrictedClassFilter();
            scriptEngine = factory.getScriptEngine(NASHORN_ARGS, getClassLoader(), classFilter);
        } else {
            //Clears the existing bindings
            scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
            scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE).clear();
        }
        setScriptContext(scriptEngine);
        //Sets the threadLocal Script Engine
        threadLocalScriptEngineHolder.set(scriptEngine);

        return scriptEngine;
    }

    public ScriptEngine getScriptEngine() {

        return threadLocalScriptEngineHolder.get();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {

        return threadLocalScriptEngineHolder.get().eval(script, context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {

        return threadLocalScriptEngineHolder.get().eval(reader, context);
    }

    @Override
    public Bindings createBindings() {

        return threadLocalScriptEngineHolder.get().createBindings();
    }

    public void setBindings(Bindings bindings, int scope) {

        threadLocalScriptEngineHolder.get().setBindings(bindings, scope);
    }

    @Override
    public Bindings getBindings(int scope) {

        return threadLocalScriptEngineHolder.get().getBindings(scope);
    }

    @Override
    public ScriptEngineFactory getFactory() {

        return threadLocalScriptEngineHolder.get().getFactory();
    }

    private ClassLoader getClassLoader() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? NashornScriptEngineFactory.class.getClassLoader() : classLoader;
    }

    private void setScriptContext(ScriptEngine scriptEngine) {

        scriptContext = new SimpleScriptContext();

        //Sets the engine scope bindings
        engineScopeBindings = scriptEngine.createBindings();
        scriptContext.setBindings(engineScopeBindings, ScriptContext.ENGINE_SCOPE);

        //Sets the global scope bindings
        globalScopeBindings = scriptEngine.createBindings();
        scriptContext.setBindings(globalScopeBindings, ScriptContext.GLOBAL_SCOPE);

        scriptEngine.setContext(this.scriptContext);
    }

}
