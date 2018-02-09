/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptFunction;

import java.io.Serializable;

/**
 *  Javascript function wrapper. This allows serialization of a javascript defined function.
 *
 */
public class SerializableJsFunction implements Serializable {

    private String name;
    private String source;
    private boolean isFunction;

    public SerializableJsFunction(String name, String source, boolean isFunction) {

        this.name = name;
        this.source = source;
        this.isFunction = isFunction;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
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
     * This will return the converted SerializableJsFunction if the given  ScriptObjectMirror is a function.
     * @param name
     * @param scriptObjectMirror
     * @return null if the ScriptObjectMirror is not a function.
     */
    public static SerializableJsFunction toSerializableForm(String name, ScriptObjectMirror scriptObjectMirror) {

        if (!scriptObjectMirror.isFunction()) {
            return null;
        }

        //TODO try to get rid of ScriptFunction
        Object unwrapped = ScriptUtils.unwrap(scriptObjectMirror);
        if (unwrapped instanceof ScriptFunction) {
            ScriptFunction scriptFunction = (ScriptFunction) unwrapped;
            boolean isFunction = scriptObjectMirror.isFunction();
            String source = scriptFunction.toSource();

            return new SerializableJsFunction(name, source, isFunction);
        } else {
            return new SerializableJsFunction(name, unwrapped.toString(), true);
        }
    }
}
