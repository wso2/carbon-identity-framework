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

import javax.script.ScriptEngine;

/**
 * ThreadLocal Implementation of the deprecated Nashorn Script Engine.
 * This holds different script engine instances for different
 * threads. Thread safety is achieved by binding the script
 * engine to the current running Thread.
 */
public class ThreadLocalScriptEngineHolder extends BaseThreadLocalScriptEngineHolder {

    protected ClassLoader getClassLoader() {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? NashornScriptEngineFactory.class.getClassLoader() : classLoader;
    }

    protected ScriptEngine createScriptEngine() {

        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        ClassFilter classFilter = new RestrictedClassFilter();
        return factory.getScriptEngine(NASHORN_ARGS, getClassLoader(), classFilter);
    }

}
