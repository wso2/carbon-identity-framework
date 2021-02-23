/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;

import java.util.function.Function;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *  Javascript function wrapper. This allows serialization of a javascript defined function.
 *
 */
public class NashornSerializableJsFunction implements SerializableJsFunction<ScriptEngine> {

    private static final Log log = LogFactory.getLog(NashornSerializableJsFunction.class);
    private static final long serialVersionUID = -7605388897997019588L;
    private String source;
    private boolean isFunction;
    private String name;

    public NashornSerializableJsFunction(String source, boolean isFunction) {

        this.source = source;
        this.isFunction = isFunction;
    }

    @Override
    public Object apply(ScriptEngine scriptEngine, Object... params) {
        Compilable compilable = (Compilable) scriptEngine;
        try {
            CompiledScript compiledScript = compilable.compile(this.getSource());
            JSObject jsObject = (JSObject) compiledScript.eval();
            if (jsObject instanceof ScriptObjectMirror) {
                ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) jsObject;
                if (!scriptObjectMirror.isFunction()) {
                    //TODO: throw exception
                }
                return scriptObjectMirror.call(null, params);
            }
        } catch (ScriptException e) {
            log.error("Error when executing function,", e);
        }
        return null;
    }

    public String getSource() {

        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    public boolean isFunction() {

        return isFunction;
    }

    public void setFunction(boolean function) {

        isFunction = function;
    }

    /**
     * This will return the converted NashornSerializableJsFunction if the given  ScriptObjectMirror is a function.
     * @param functionObject
     * @return null if the ScriptObjectMirror is not a function.
     */
    public static NashornSerializableJsFunction toSerializableForm(Object functionObject) {

        if (functionObject == null) {
            return null;
        }
        if (functionObject instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) functionObject;
            return  serializeNashorn(scriptObjectMirror);
        }
        return null;

    }

    public String getName() {

        return name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    private static NashornSerializableJsFunction serializeGraal(Function functionObject) {
        return new NashornSerializableJsFunction("", true);
    }

    /**.
     * Serialize Nashorn javascript funmction
     * @param scriptObjectMirror
     * @return
     */
    private static NashornSerializableJsFunction serializeNashorn(ScriptObjectMirror scriptObjectMirror) {

        if (!scriptObjectMirror.isFunction()) {
            return null;
        }

        //TODO try to get rid of ScriptFunction
        Object unwrapped = ScriptUtils.unwrap(scriptObjectMirror);
        if (unwrapped instanceof ScriptFunction) {
            ScriptFunction scriptFunction = (ScriptFunction) unwrapped;
            boolean isFunction = scriptObjectMirror.isFunction();
            String source = scriptFunction.toSource();

            return new NashornSerializableJsFunction(source, isFunction);
        } else {
            return new NashornSerializableJsFunction(unwrapped.toString(), true);
        }
    }
}
