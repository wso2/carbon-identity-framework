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
import org.openjdk.nashorn.api.scripting.JSObject;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openjdk.nashorn.api.scripting.ScriptUtils;
import org.openjdk.nashorn.internal.runtime.ScriptFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.BaseSerializableJsFunction;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Javascript function wrapper. This allows serialization of a javascript defined function.
 * Since Nashorn is deprecated in JDK 11 and onwards. We replaced it with OpenJDK Nashorn classes.
 */
public class OpenJdkNashornSerializableJsFunction implements BaseSerializableJsFunction {

    private static final Log log = LogFactory.getLog(OpenJdkNashornSerializableJsFunction.class);

    // todo checnge it
    private static final long serialVersionUID = -1234388897997019588L;
    private String source;
    private boolean isFunction;

    public OpenJdkNashornSerializableJsFunction(String source, boolean isFunction) {

        this.source = source;
        this.isFunction = isFunction;
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

    /**
     * This will return the converted SerializableJsFunction if the given  ScriptObjectMirror is a function.
     * @param scriptObjectMirror
     * @return null if the ScriptObjectMirror is not a function.
     */
    public static OpenJdkNashornSerializableJsFunction toSerializableForm(ScriptObjectMirror scriptObjectMirror) {

        if (!scriptObjectMirror.isFunction()) {
            return null;
        }

        //TODO try to get rid of ScriptFunction
        Object unwrapped = ScriptUtils.unwrap(scriptObjectMirror);
        if (unwrapped instanceof ScriptFunction) {
            ScriptFunction scriptFunction = (ScriptFunction) unwrapped;
            boolean isFunction = scriptObjectMirror.isFunction();
            String source = scriptFunction.toSource();

            return new OpenJdkNashornSerializableJsFunction(source, isFunction);
        } else {
            return new OpenJdkNashornSerializableJsFunction(unwrapped.toString(), true);
        }
    }
}
