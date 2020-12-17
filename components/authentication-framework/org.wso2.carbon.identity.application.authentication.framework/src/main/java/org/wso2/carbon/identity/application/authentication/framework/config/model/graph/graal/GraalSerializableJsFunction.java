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

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;

import java.io.IOException;
import java.util.function.Function;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/**
 * Implements serializable function which is a graal object.
 */
public class GraalSerializableJsFunction implements SerializableJsFunction<Context, GraalJsAuthenticationContext> {

    private static final long serialVersionUID = -7605388897997019588L;
    public String source;
    public boolean isFunction;
    private String name;

    public GraalSerializableJsFunction(String source, boolean isFunction) {

        this.source = source;
        this.isFunction = isFunction;
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
            try{
                Context context = Context.getCurrent();
                Value functionAsValue = context.asValue(functionObject);
                if (functionAsValue.canExecute()){
                    String source = (String) functionAsValue.getSourceLocation().getCharacters();
                    return serializePolygot(source, true);
                }
                else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }

    public Object apply(Context polyglotContext, GraalJsAuthenticationContext jsAuthenticationContext) {

        if(isFunction) {
            try {
                polyglotContext.eval(Source.newBuilder("js",
                        " var curfunc = " + getSource(),
                        "src.js").build());
                return polyglotContext.getBindings("js").getMember("curfunc").execute(jsAuthenticationContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public String getSource() {

        return source;
    }

    public void setSource(String source) {

        this.source = source;
    }

    public String getName() {

        return name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    private static GraalSerializableJsFunction serializePolygot(String source, boolean isFunction) {

        return new GraalSerializableJsFunction(source, isFunction);
    }
}
