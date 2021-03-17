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
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of Graaljs polyglot Context and any related expensive objects.
 */
public class JsPolyglotGraphBuilderFactory implements JsGraphBuilderFactory<Context> {

    private static final Log LOG = LogFactory.getLog(JsPolyglotGraphBuilderFactory.class);
    private static final String JS_BINDING_CURRENT_CONTEXT = "JS_BINDING_CURRENT_CONTEXT";

    public void init() {

    }
    @SuppressWarnings("unchecked")
    public static void restoreCurrentContext(AuthenticationContext authContext, Context context)
            throws FrameworkException {

        Map<String, Object> map = (Map<String, Object>) authContext.getProperty(JS_BINDING_CURRENT_CONTEXT);
        Value bindings = context.getBindings(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE);
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object deserializedValue = FrameworkUtils.fromJsSerializableGraal(entry.getValue(), context);
                bindings.putMember(entry.getKey(), deserializedValue);
            }
        }
    }

    public static void persistCurrentContext(AuthenticationContext authContext, Context context) {

        Value engineBindings = context.getBindings(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE);
        Map<String, Object> persistableMap = new HashMap<>();
        engineBindings.getMemberKeys().forEach((key) -> {
            Value keybinding = engineBindings.getMember(key);
            if (!keybinding.isHostObject()) {
                persistableMap.put(key, FrameworkUtils.toJsSerializableGraal(keybinding));
            }
        });

        authContext.setProperty(JS_BINDING_CURRENT_CONTEXT, persistableMap);
    }

    public Context createEngine(AuthenticationContext authenticationContext) {

        Context context = Context.newBuilder(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE)
                .allowHostAccess(HostAccess.ALL).build();

        Value bindings = context.getBindings(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE);
        JsLogger jsLogger = new JsLogger();
        bindings.putMember(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);
        return context;
    }

    public JsPolyglotGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                                Map<Integer, StepConfig> stepConfigMap) {

        return new JsPolyglotGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext));
    }

    public JsPolyglotGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                                Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode) {

        return new JsPolyglotGraphBuilder(authenticationContext, stepConfigMap,
                createEngine(authenticationContext), currentNode);
    }
}
