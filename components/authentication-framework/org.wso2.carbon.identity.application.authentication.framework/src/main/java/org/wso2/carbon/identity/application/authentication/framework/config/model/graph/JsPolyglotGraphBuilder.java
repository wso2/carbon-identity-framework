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
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;

import java.util.Collections;
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

        ScriptContext context = this.createContext();

        try {
            currentBuilder.set(this);
            engine.eval(FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction(), context);
            engine.eval(script, context);
            JsPolyglotGraphBuilderFactory.restoreCurrentContext(authenticationContext, engine);

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
            JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, engine);
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


    /**
     * Add authentication fail node to the authentication graph during subsequent requests.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public static void sendErrorAsync(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    private static FailNode createFailNode(String url, Map<String, Object> parameterMap) {

        FailNode failNode = new FailNode();
        failNode.setErrorPageUri(url);

        parameterMap.forEach((key, value) -> failNode.getFailureData().put(key, String.valueOf(value)));
        return failNode;
    }

    /**
     * Add authentication fail node to the authentication graph in the initial request.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public void sendError(String url, Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(url, parameterMap);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param stepId Step Id
     */
    /*package private*/ void executeStep(int stepId, Map<String, Object> eventListeners, Map<String, Object> options) {

        StepConfig stepConfig;
        stepConfig = stepNamedMap.get(stepId);

        if (stepConfig == null) {
            log.error("Given Authentication Step :" + stepId + " is not in Environment");
            return;
        }
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
        currentNode = newNode;
        if (eventListeners != null) {
            attachEventListeners(eventListeners, currentNode);
        }
        if (options != null) {
            handleOptions(options, stepConfig);
        }
    }
    private void attachEventListeners(Map<String, Object> eventsMap, AuthGraphNode currentNode) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap, effectiveFunctionSerializer());
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
        }
    }

    @SuppressWarnings("unchecked")
    protected void handleOptions(Map<String, Object> options, StepConfig stepConfig) {

        Object authenticationOptionsObj = options.get(FrameworkConstants.JSAttributes.AUTHENTICATION_OPTIONS);
        if (authenticationOptionsObj instanceof Map) {
            filterOptions((Map<String, Map<String, String>>) authenticationOptionsObj, stepConfig);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Authenticator options not provided or invalid, hence proceeding without filtering");
            }
        }

        Object authenticatorParams = options.get(FrameworkConstants.JSAttributes.AUTHENTICATOR_PARAMS);
        if (authenticatorParams instanceof Map) {
            authenticatorParamsOptions((Map<String, Object>) authenticatorParams, stepConfig);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Authenticator params not provided or invalid, hence proceeding without setting params");
            }
        }
    }

    /**
     * Creates the StepConfigGraphNode with given StepConfig.
     *
     * @param stepConfig Step Config Object.
     * @return built and wrapped new StepConfigGraphNode.
     */
    private static StepConfigGraphNode wrap(StepConfig stepConfig) {

        return new StepConfigGraphNode(stepConfig);
    }

    @Override
    protected Function<Object, SerializableJsFunction> effectiveFunctionSerializer() {

        return v -> GraalSerializableJsFunction.toSerializableForm(v);
    }

    @FunctionalInterface
    public interface StepExecutor {

        void executeStep(Integer stepId, Object... parameterMap);
    }

    @FunctionalInterface
    public interface PromptExecutor {

        void prompt(String template, Object... parameterMap);
    }

    @FunctionalInterface
    public interface RestrictedFunction {

        void exit(Object... arg);
    }

    @FunctionalInterface
    public interface LoadExecutor {

        String loadLocalLibrary(String libraryName) throws FunctionLibraryManagementException;
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
                JsPolyglotGraphBuilderFactory.restoreCurrentContext(authenticationContext, scriptEngine);
                ScriptContext context = JsPolyglotGraphBuilder.this.createContext();

                Compilable compilable = (Compilable) scriptEngine;
                JsPolyglotGraphBuilder.contextForJs.set(authenticationContext);

                result = fn.apply(scriptEngine, new GraalJsAuthenticationContext(authenticationContext));

                JsPolyglotGraphBuilderFactory.persistCurrentContext(authenticationContext, scriptEngine);

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

        private boolean canInfuse(AuthGraphNode executingNode) {

            return executingNode instanceof DynamicDecisionNode && dynamicallyBuiltBaseNode.get() != null;
        }

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
                new ExecuteStepFunctionProxy());
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                this::sendError);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                (JsNashornGraphBuilder.PromptExecutor) this::addShowPrompt);
        bindings.put(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                (JsNashornGraphBuilder.LoadExecutor) this::loadLocalLibrary);
        bindings.put("exit", (JsNashornGraphBuilder.RestrictedFunction) this::exitFunction);
        bindings.put("quit", (JsNashornGraphBuilder.RestrictedFunction) this::quitFunction);
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

    class ExecuteStepFunctionProxy implements ProxyExecutable {

        @Override
        public Object execute(Value... arguments) {

            System.out.println("Execute Step");
            if (arguments == null || arguments.length <= 0) {
                System.out.println("executeStep(n) should have at least the step ID");
                return null;
            }

            int stepId = arguments[0].asInt();
            if (arguments.length == 1) {
                JsPolyglotGraphBuilder.this.executeStep(stepId, Collections.emptyMap(), Collections.emptyMap());
            } else if (arguments.length == 2) {
                Map eventHandlerMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStep(stepId, eventHandlerMap, Collections.emptyMap());
            } else if (arguments.length == 3) {
                Map eventHandlerMap = arguments[2].as(Map.class);
                Map optionsMap = arguments[1].as(Map.class);
                JsPolyglotGraphBuilder.this.executeStep(stepId, eventHandlerMap, optionsMap);
            }

            return null;
        }
    }

    private ScriptEngine getEngine(AuthenticationContext authenticationContext) {

        return this.engine;
    }
}
