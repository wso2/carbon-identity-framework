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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.graal.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.graal.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.ScriptEngine;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public class JsPolyglotGraphBuilder extends JsBaseGraphBuilder implements JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsPolyglotGraphBuilder.class);
    protected Context context;

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param context               Polyglot Context.
     */
    public JsPolyglotGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                  Context context) {

        this.authenticationContext = authenticationContext;
        this.context = context;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param context               polyglot context.
     * @param currentNode           Current authentication graph node.
     */
    public JsPolyglotGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                  Context context, AuthGraphNode currentNode) {

        this.authenticationContext = authenticationContext;
        this.context = context;
        this.currentNode = currentNode;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    @Override
    public JsPolyglotGraphBuilder createWith(String script) {

        try {
            currentBuilder.set(this);
            Value bindings = context.getBindings("js");

            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP, (StepExecutor) this::executeStep);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                    this::sendError);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                    (PromptExecutor) this::addShowPrompt);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                    (LoadExecutor) this::loadLocalLibrary);
            bindings.putMember("exit", (RestrictedFunction) this::exitFunction);
            bindings.putMember("quit", (RestrictedFunction) this::quitFunction);
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap = jsFunctionRegistrar
                        .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(bindings::putMember);
            }
            SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
                    (SelectOneFunction) selectAcrFromFunction::evaluate);
            currentBuilder.set(this);
            context.eval(Source.newBuilder("js",
                    FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction(),
                    "src.js").build());
            context.eval(Source.newBuilder("js",
                    script,
                    "src.js").build());

            Value onLoginRequestFn = bindings.getMember(FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST);
            if (onLoginRequestFn == null) {
                log.error(
                        "Could not find the entry function " + FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST + " \n" + script);
                result.setBuildSuccessful(false);
                result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                        .JS_FUNC_ON_LOGIN_REQUEST + " function is not defined.");
                return this;
            }
            onLoginRequestFn.executeVoid(new GraalJsAuthenticationContext(authenticationContext));
            JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, context);
        } catch (PolyglotException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_ON_LOGIN_REQUEST + " reason, " + e.getMessage());
            result.setError(e);
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        }

        catch (IOException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in building  the Javascript source" + e.getMessage());
            result.setError(e);
            if (log.isDebugEnabled()) {
                log.debug("Error in building the Javascript source", e);
            }
        } finally {
            context.close();
            clearCurrentBuilder();
        }
        return this;
    }

    @Override
    protected Function<Object, SerializableJsFunction> effectiveFunctionSerializer() {

        return GraalSerializableJsFunction::toSerializableForm;
    }

    public AuthenticationDecisionEvaluator getScriptEvaluator(SerializableJsFunction fn) {

        return this.new JsBasedEvaluator(fn);
    }

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public class JsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840344L;
        private SerializableJsFunction jsFunction;

        public JsBasedEvaluator(SerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        public Object evaluate(AuthenticationContext authenticationContext, SerializableJsFunction fn) {

            JsPolyglotGraphBuilder graphBuilder = JsPolyglotGraphBuilder.this;
            Object result = null;
            if (jsFunction == null) {
                return null;
            }
            Context context = getContext(authenticationContext);
            Value bindings = context.getBindings("js");


            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP, (StepExecutor) graphBuilder::executeStepInAsyncEvent);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                    JsPolyglotGraphBuilder::sendErrorAsync);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                    (PromptExecutor) graphBuilder::addShowPrompt);
            bindings.putMember(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                    (LoadExecutor) graphBuilder::loadLocalLibrary);
            bindings.putMember("exit", (RestrictedFunction) graphBuilder::exitFunction);
            bindings.putMember("quit", (RestrictedFunction) graphBuilder::quitFunction);
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap = jsFunctionRegistrar
                        .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(bindings::putMember);
            }

            try {
                currentBuilder.set(graphBuilder);
                JsPolyglotGraphBuilderFactory.restoreCurrentContext(authenticationContext, context);
                JsPolyglotGraphBuilder.contextForJs.set(authenticationContext);

                GraalSerializableJsFunction curr = (GraalSerializableJsFunction) fn;
                result = fn.apply(context, new GraalJsAuthenticationContext(authenticationContext));
                JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, context);

                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                        .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                if (canInfuse(executingNode)) {
                    infuse(executingNode, dynamicallyBuiltBaseNode.get());
                }

            } catch (Throwable e) {
                //We need to catch all the javascript errors here, then log and handle.
                log.error("Error in executing the javascript for service provider : " + authenticationContext
                        .getServiceProviderName() + ", Javascript Fragment : \n" + fn.toString(), e);
                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                        .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                FailNode failNode = new FailNode();
                attachToLeaf(executingNode, failNode);
            } finally {
                context.close();
                contextForJs.remove();
                dynamicallyBuiltBaseNode.remove();
                clearCurrentBuilder();
            }

            return result;
        }


    }

    private Context getContext(AuthenticationContext authenticationContext) {

        return this.context;
    }
}