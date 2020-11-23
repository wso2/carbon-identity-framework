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
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.NashornJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.functions.library.mgt.exception.FunctionLibraryManagementException;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 *
 * @deprecated Nashorn is depricatd in JDK 11 and onwards. We replaced it with Graal engine.
 */
public class JsNashornGraphBuilder extends JsBaseGraphBuilder implements JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsNashornGraphBuilder.class);

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     * @param scriptEngine          Script engine.
     */
    public JsNashornGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                 ScriptEngine scriptEngine) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
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
    public JsNashornGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                                 ScriptEngine scriptEngine, AuthGraphNode currentNode) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
        this.currentNode = currentNode;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    public JsNashornGraphBuilder createWith(String script) {

        try {
            currentBuilder.set(this);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            Bindings engineBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP, (StepExecutor) this::executeStep);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (BiConsumer<String, Map>)
                    this::sendError);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT,
                    (PromptExecutor) this::addShowPrompt);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB,
                    (LoadExecutor) this::loadLocalLibrary);
            engineBindings.put("exit", (RestrictedFunction) this::exitFunction);
            engineBindings.put("quit", (RestrictedFunction) this::quitFunction);
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap = jsFunctionRegistrar
                        .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(globalBindings::put);
            }
            Invocable invocable = (Invocable) engine;
            engine.eval(FrameworkServiceDataHolder.getInstance().getCodeForRequireFunction());
            engine.eval(script);
            invocable.invokeFunction(FrameworkConstants.JSAttributes.JS_FUNC_ON_LOGIN_REQUEST,
                    new NashornJsAuthenticationContext(authenticationContext));
            JsNashornGraphBuilderFactory.persistCurrentContext(authenticationContext, engine);
        } catch (ScriptException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. Nested exception is: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } catch (NoSuchMethodException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_ON_LOGIN_REQUEST + " function is not defined.");
            result.setError(e);
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } finally {
            clearCurrentBuilder();
        }
        return this;
    }

    @Override
    public AuthenticationDecisionEvaluator getScriptEvaluator(SerializableJsFunction fn) {

        return new JsBasedEvaluator((NashornSerializableJsFunction) fn);
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
     * @param params params
     */
    @SuppressWarnings("unchecked")
    public final void executeStep(int stepId, Object... params) {

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
        if (params.length > 0) {
            // if there are any params provided, last one is assumed to be the event listeners
            if (params[params.length - 1] instanceof Map) {
                attachEventListeners((Map<String, Object>) params[params.length - 1], currentNode);
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }
        }
        if (params.length == 2) {
            // There is an argument with options present
            if (params[0] instanceof Map) {
                Map<String, Object> options = (Map<String, Object>) params[0];
                handleOptions(options, stepConfig);
            }
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
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param params params
     */
    @SuppressWarnings("unchecked")
    public void executeStepInAsyncEvent(int stepId, Object... params) {

        AuthenticationContext context = contextForJs.get();
        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();

        if (log.isDebugEnabled()) {
            log.debug("Execute Step on async event. Step ID : " + stepId);
        }
        AuthenticationGraph graph = context.getSequenceConfig().getAuthenticationGraph();
        if (graph == null) {
            log.error("The graph happens to be null on the sequence config. Can not execute step : " + stepId);
            return;
        }

        StepConfig stepConfig = graph.getStepMap().get(stepId);
        // Inorder to keep original stepConfig as a backup in AuthenticationGraph.
        StepConfig clonedStepConfig = new StepConfig(stepConfig);
        clonedStepConfig
                .applyStateChangesToNewObjectFromContextStepMap(context.getSequenceConfig().getStepMap().get(stepId));
        if (log.isDebugEnabled()) {
            log.debug("Found step for the Step ID : " + stepId + ", Step Config " + clonedStepConfig);
        }
        StepConfigGraphNode newNode = wrap(clonedStepConfig);
        if (currentNode == null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting a new node at the first time. Node : " + newNode.getName());
            }
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }

        if (params.length > 0) {
            // if there is only one param, it is assumed to be the event listeners
            if (params[params.length - 1] instanceof Map) {
                attachEventListeners((Map<String, Object>) params[params.length - 1], newNode);
            } else {
                log.error("Invalid argument and hence ignored. Last argument should be a Map of event listeners.");
            }
        }

        if (params.length == 2) {
            // There is an argument with options present
            if (params[0] instanceof Map) {
                Map<String, Object> options = (Map<String, Object>) params[0];
                handleOptions(options, clonedStepConfig);
            }
        }
    }

    protected Function<Object, SerializableJsFunction> effectiveFunctionSerializer() {

        return v -> NashornSerializableJsFunction.toSerializableForm(v);
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

    /**
     * Creates the StepConfigGraphNode with given StepConfig.
     *
     * @param stepConfig Step Config Object.
     * @return built and wrapped new StepConfigGraphNode.
     */
    private static StepConfigGraphNode wrap(StepConfig stepConfig) {

        return new StepConfigGraphNode(stepConfig);
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

    protected SerializableJsFunction toSerializableForm(Object function) {

        return NashornSerializableJsFunction.toSerializableForm(function);
    }

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public class JsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840344L;
        private NashornSerializableJsFunction jsFunction;

        public JsBasedEvaluator(NashornSerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        public Object evaluate(AuthenticationContext authenticationContext, SerializableJsFunction fn) {

            JsNashornGraphBuilder graphBuilder = JsNashornGraphBuilder.this;
            Object result = null;
            if (jsFunction == null) {
                return null;
            }
            if (jsFunction.isFunction()) {
                ScriptEngine scriptEngine = getEngine(authenticationContext);
                try {
                    currentBuilder.set(graphBuilder);
                    JsNashornGraphBuilderFactory.restoreCurrentContext(authenticationContext, scriptEngine);
                    Bindings globalBindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
                    //Now re-assign the executeStep function to dynamic evaluation
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP,
                            (StepExecutor) graphBuilder::executeStepInAsyncEvent);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR,
                            (BiConsumer<String, Map>) JsNashornGraphBuilder::sendErrorAsync);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SHOW_PROMPT, (PromptExecutor)
                            graphBuilder::addShowPrompt);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_LOAD_FUNC_LIB, (LoadExecutor)
                            graphBuilder::loadLocalLibrary);
                    JsFunctionRegistry jsFunctionRegistry = FrameworkServiceDataHolder.getInstance()
                            .getJsFunctionRegistry();
                    if (jsFunctionRegistry != null) {
                        Map<String, Object> functionMap = jsFunctionRegistry
                                .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                        functionMap.forEach(globalBindings::put);
                    }

                    JsNashornGraphBuilder.contextForJs.set(authenticationContext);


                    result = fn.apply(scriptEngine, new NashornJsAuthenticationContext(authenticationContext));

                    JsNashornGraphBuilderFactory.persistCurrentContext(authenticationContext, scriptEngine);

                    AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                            .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                    if (canInfuse(executingNode)) {
                        infuse(executingNode, dynamicallyBuiltBaseNode.get());
                    }

                } catch (Throwable e) {
                    //We need to catch all the javascript errors here, then log and handle.
                    log.error("Error in executing the javascript for service provider : " + authenticationContext
                            .getServiceProviderName() + ", Javascript Fragment : \n" + jsFunction.getSource(), e);
                    AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                            .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                    FailNode failNode = new FailNode();
                    attachToLeaf(executingNode, failNode);
                } finally {
                    contextForJs.remove();
                    dynamicallyBuiltBaseNode.remove();
                    clearCurrentBuilder();
                }

            } else {
                result = jsFunction.getSource();
            }
            return result;
        }

        private boolean canInfuse(AuthGraphNode executingNode) {

            return executingNode instanceof DynamicDecisionNode && dynamicallyBuiltBaseNode.get() != null;
        }

        private ScriptEngine getEngine(AuthenticationContext authenticationContext) {

            return FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory()
                    .createEngine(authenticationContext);
        }
    }
}
