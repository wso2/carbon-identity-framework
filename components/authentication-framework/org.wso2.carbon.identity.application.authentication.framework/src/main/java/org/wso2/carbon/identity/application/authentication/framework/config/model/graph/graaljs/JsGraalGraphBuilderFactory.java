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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsSerializer;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraalSelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create a Javascript based sequence builder.
 * This factory is there to reuse of GraalJS Polyglot Context and any related expensive objects.
 * <p>
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing GraalJS engine.
 */
public class JsGraalGraphBuilderFactory implements JsBaseGraphBuilderFactory<Context> {

    private static final Log LOG = LogFactory.getLog(JsGraalGraphBuilderFactory.class);
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
                Object deserializedValue = GraalSerializer.getInstance().fromJsSerializable(entry.getValue(), context);
                bindings.putMember(entry.getKey(), deserializedValue);
            }
        }
    }

    public static void persistCurrentContext(AuthenticationContext authContext, Context context) {

        Value engineBindings = context.getBindings(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE);
        Map<String, Object> persistableMap = new HashMap<>();
        engineBindings.getMemberKeys().forEach((key) -> {
            Value binding = engineBindings.getMember(key);
            if (!(binding.isHostObject() && (binding.canExecute() || !binding.hasArrayElements()))) {
                persistableMap.put(key, GraalSerializer.getInstance().toJsSerializable(binding));
            }
        });
        authContext.setProperty(JS_BINDING_CURRENT_CONTEXT, persistableMap);
    }

    public Context createEngine(AuthenticationContext authenticationContext) {

        Context context =
                Context.newBuilder(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE).allowHostAccess(HostAccess.ALL)
                        .option("engine.WarnInterpreterOnly", "false").build();

        Value bindings = context.getBindings(FrameworkConstants.JSAttributes.POLYGLOT_LANGUAGE);
        GraalSelectAcrFromFunction selectAcrFromFunction = new GraalSelectAcrFromFunction();
        bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM, selectAcrFromFunction);
        JsLogger jsLogger = new JsLogger();
        bindings.putMember(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);
        return context;
    }

    @Override
    public JsSerializer getJsUtil() {

        return GraalSerializer.getInstance();
    }

    @Override
    public JsBaseGraphBuilder getCurrentBuilder() {

        return JsGraalGraphBuilder.getCurrentBuilder();
    }

    public JsGraalGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                             Map<Integer, StepConfig> stepConfigMap) {

        return new JsGraalGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext));
    }

    public JsGraalGraphBuilder createBuilder(AuthenticationContext authenticationContext,
                                             Map<Integer, StepConfig> stepConfigMap, AuthGraphNode currentNode) {

        return new JsGraalGraphBuilder(authenticationContext, stepConfigMap, createEngine(authenticationContext),
                currentNode);
    }

}
