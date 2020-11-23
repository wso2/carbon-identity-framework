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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;

import java.util.function.Function;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Implements serializable function which is a graal object.
 */
public class GraalSerializableJsFunction implements SerializableJsFunction<GraalJsAuthenticationContext> {

    private transient Function realFunction;
    private Object guestObject;
    private String name;

    public GraalSerializableJsFunction(Function realFunction) {

        this.realFunction = realFunction;
    }

    /**
     * This will return the converted NashornSerializableJsFunction if the given  ScriptObjectMirror is a function.
     *
     * @param functionObject
     * @return null if the ScriptObjectMirror is not a function.
     */
    public static GraalSerializableJsFunction toSerializableForm(Object functionObject) {

        if (functionObject == null) {
            return null;
        }
        if (functionObject instanceof Function) {
            Function scriptFunction = (Function) functionObject;
            return serializePolygot(scriptFunction);
        }
        return null;

    }

    @Override
    public Object apply(ScriptEngine scriptEngine, GraalJsAuthenticationContext jsAuthenticationContext) {

        if(realFunction != null) {
            return realFunction.apply(new Object[]{jsAuthenticationContext});
        }
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        Object value = bindings.get(name);
        if(value instanceof Function) {
            return ((Function) value).apply(new Object[]{jsAuthenticationContext});
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

    private static GraalSerializableJsFunction serializePolygot(Function realFunction) {

        return new GraalSerializableJsFunction(realFunction);
    }
}
