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
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsLogger;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.graal.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.graal.SelectOneFunction;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public class JsPolyglotGraphBuilder extends JsBaseGraphBuilder implements JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsPolyglotGraphBuilder.class);

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param scriptEngine          Script engine.
     */
    public JsPolyglotGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                  ScriptEngine scriptEngine) {

        this.authenticationContext = authenticationContext;
        this.engine = scriptEngine;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param scriptEngine          Script engine.
     * @param currentNode           Current authentication graph node.
     */
    public JsPolyglotGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                  ScriptEngine scriptEngine, AuthGraphNode currentNode) {

        this.authenticationContext = authenticationContext;
        this.engine = scriptEngine;
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

        ScriptContext context = this.createContextInitial();

        try {
            currentBuilder.set(this);
            engine.eval(FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction(), context);
            engine.eval(script, context);
            JsPolyglotGraphBuilderFactory.restoreCurrentContext(authenticationContext, context);

            Function<Object[], Void> onLoginRequestFn = (Function) context.getBindings(ScriptContext.ENGINE_SCOPE).get(
                    FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST);

            if (onLoginRequestFn == null) {
                log.error(
                        "Could not find the entry function " + FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST + " \n" + script);
                result.setBuildSuccessful(false);
                result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                        .JS_FUNC_ON_LOGIN_REQUEST + " function is not defined.");
                return this;
            }
            onLoginRequestFn.apply(new Object[]{new GraalJsAuthenticationContext(authenticationContext)});
            JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, context);
        } catch (ScriptException | PolyglotException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_ON_LOGIN_REQUEST + " reason, " + e.getMessage());
            result.setError(e);
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } catch (FrameworkException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in restoring javascript context. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_ON_LOGIN_REQUEST + " reason, " + e.getMessage());
            result.setError(e);
            if (log.isDebugEnabled()) {
                log.debug("Error in restoring the Javascript context.", e);
            }
        } finally {
            clearCurrentBuilder();
        }
        return this;
    }

    @Override
    protected Function<Object, SerializableJsFunction> effectiveFunctionSerializer() {

        return v -> GraalSerializableJsFunction.toSerializableForm(v);
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
            ScriptEngine scriptEngine = getEngine(authenticationContext);
            try {
                currentBuilder.set(graphBuilder);
                ScriptContext context = JsPolyglotGraphBuilder.this.createContext();
                JsPolyglotGraphBuilderFactory.restoreCurrentContext(authenticationContext, context);

                Compilable compilable = (Compilable) scriptEngine;
                JsPolyglotGraphBuilder.contextForJs.set(authenticationContext);

                result = fn.apply(scriptEngine, new GraalJsAuthenticationContext(authenticationContext));

                JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, context);

                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                        .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                if (canInfuse(executingNode)) {
                    infuse(executingNode, dynamicallyBuiltBaseNode.get());
                }

            } catch (Throwable e) {
                e.printStackTrace();
                //We need to catch all the javascript errors here, then log and handle.
                log.error("Error in executing the javascript for service provider : " + authenticationContext
                        .getServiceProviderName() + ", Javascript Fragment : \n" + fn.toString(), e);
                AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                        .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                FailNode failNode = new FailNode();
                attachToLeaf(executingNode, failNode);
            } finally {
                contextForJs.remove();
                dynamicallyBuiltBaseNode.remove();
                clearCurrentBuilder();
            }

            return result;
        }


    }

    private ScriptContext createContextInitial() {

        ScriptContext context = new SimpleScriptContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);

        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true); //
        bindings.put("polyglot.js.allowHostClassLoading", true);

        JsLogger jsLogger = new JsLogger();
        bindings.put(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);

        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP,
                new ExecuteStepProxy());
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                this::sendError);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                (PromptExecutor) this::addShowPrompt);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                (LoadExecutor) this::loadLocalLibrary);
        bindings.put("exit", (RestrictedFunction) this::exitFunction);
        bindings.put("quit", (RestrictedFunction) this::quitFunction);
        JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
        if (jsFunctionRegistrar != null) {
            Map<String, Object> functionMap = jsFunctionRegistrar
                    .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
            functionMap.forEach(bindings::put);
        }
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
                (SelectOneFunction) selectAcrFromFunction::evaluate);

        return context;
    }

    private ScriptContext createContext() {

        ScriptContext context = new SimpleScriptContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);

        Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true); //
        bindings.put("polyglot.js.allowHostClassLoading", true);

        JsLogger jsLogger = new JsLogger();
        bindings.put(FrameworkConstants.JSAttributes.JS_LOG, jsLogger);

        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP,
                new executeStepAsyncProxy());
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                JsPolyglotGraphBuilder::sendErrorAsync);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                (PromptExecutor) this::addShowPrompt);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                (LoadExecutor) this::loadLocalLibrary);
        bindings.put("exit", (RestrictedFunction) this::exitFunction);
        bindings.put("quit", (RestrictedFunction) this::quitFunction);
        JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
        if (jsFunctionRegistrar != null) {
            Map<String, Object> functionMap = jsFunctionRegistrar
                    .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
            functionMap.forEach(bindings::put);
        }
        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SELECT_ACR_FROM,
                (SelectOneFunction) selectAcrFromFunction::evaluate);

        return context;
    }



    class ExecuteStepProxy implements ProxyExecutable {

        @Override
        public Object execute(Value... arguments) {

            System.out.println("Execute Step");
            if (arguments == null || arguments.length <= 0) {
                System.out.println("executeStep(n) should have at least the step ID");
                return null;
            }

            int stepId = arguments[0].asInt();
            if (arguments.length == 1) {
                JsPolyglotGraphBuilder.this.executeStep(stepId);
            } else if (arguments.length == 2) {
                Map eventHandlerMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStep(stepId, eventHandlerMap);
            } else if (arguments.length == 3) {
                Map eventHandlerMap = arguments[2].as(Map.class);
                Map optionsMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStep(stepId, optionsMap, eventHandlerMap);
            }

            return null;
        }
    }

    class executeStepAsyncProxy implements ProxyExecutable{
        @Override
        public Object execute(Value... arguments) {
            System.out.println("Execute Step");
            if (arguments == null || arguments.length <= 0) {
                System.out.println("executeStep(n) should have at least the step ID");
                return null;
            }

            int stepId = arguments[0].asInt();
            if (arguments.length == 1) {
                JsPolyglotGraphBuilder.this.executeStepInAsyncEvent(stepId);
            } else if (arguments.length == 2) {
                Map eventHandlerMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStepInAsyncEvent(stepId, eventHandlerMap);
            } else if (arguments.length == 3) {
                Map eventHandlerMap = arguments[2].as(Map.class);
                Map optionsMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStepInAsyncEvent(stepId, optionsMap, eventHandlerMap);
            }

            return null;
        }
    }


    private ScriptEngine getEngine(AuthenticationContext authenticationContext) {

        return this.engine;
    }
}