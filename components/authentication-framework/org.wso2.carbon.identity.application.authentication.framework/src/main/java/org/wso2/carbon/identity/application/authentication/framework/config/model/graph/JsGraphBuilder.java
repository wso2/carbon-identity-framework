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

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Translate the authentication graph config to runtime model.
 * This is not thread safe. Should be discarded after each build.
 */
public class JsGraphBuilder {

    private static final Log log = LogFactory.getLog(JsGraphBuilder.class);
    private Map<String, StepConfig> stepNamedMap;
    private AuthenticationGraph result = new AuthenticationGraph();
    private AuthGraphNode currentNode = null;
    private AuthenticationContext authenticationContext;
    private ScriptEngine engine;
    private static ThreadLocal<AuthenticationContext> contextForJs = new ThreadLocal<>();
    private static ThreadLocal<AuthGraphNode> dynamicallyBuiltBaseNode = new ThreadLocal<>();
    private static ThreadLocal<JsGraphBuilder> currentBuilder = new ThreadLocal<>();

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext current authentication context.
     * @param stepConfigMap         The Step map from the service provider configuration.
     */
    public JsGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
                          ScriptEngine scriptEngine) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
    }

    /**
     * Returns the built graph.
     *
     * @return AuthenticationGraph built from JsGraphBuilder
     */
    public AuthenticationGraph build() {

        if (result.isBuildSuccessful()) {
            if (currentNode != null && !(currentNode instanceof EndStep)) {
                attachToLeaf(currentNode, new EndStep());
            }
        } else {
            //no need to do anything
            if (log.isDebugEnabled()) {
                log.debug("Not building the graph as the initialization was unsuccessful.");
            }
        }
        return result;
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    public JsGraphBuilder createWith(String script) {

        try {
            currentBuilder.set(this);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP, (Consumer<Map>) this::executeStep);
            globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR, (Consumer<Map>) this::sendError);
            JsFunctionRegistry jsFunctionRegistrar = FrameworkServiceDataHolder.getInstance().getJsFunctionRegistry();
            if (jsFunctionRegistrar != null) {
                Map<String, Object> functionMap = jsFunctionRegistrar
                        .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                functionMap.forEach(globalBindings::put);
            }
            Invocable invocable = (Invocable) engine;
            engine.eval(script);
            invocable.invokeFunction(FrameworkConstants.JSAttributes.JS_FUNC_INITIATE_REQUEST,
                    new JsAuthenticationContext(authenticationContext));
            JsGraphBuilderFactory.persistCurrentContext(authenticationContext, engine);
        } catch (ScriptException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. Nested exception is: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } catch (NoSuchMethodException e) {
            result.setBuildSuccessful(false);
            result.setErrorReason("Error in executing the Javascript. " + FrameworkConstants.JSAttributes
                    .JS_FUNC_INITIATE_REQUEST + " function is not defined.");
            if (log.isDebugEnabled()) {
                log.debug("Error in executing the Javascript.", e);
            }
        } finally {
            currentBuilder.remove();
        }
        return this;
    }

    public static void clear() {

        currentBuilder.remove();
    }

    public static JsGraphBuilder getCurrentBuilder() {

        return currentBuilder.get();
    }

    /**
     * Add authentication fail node to the authentication graph during subsequent requests.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    public static void sendErrorAsync(Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(parameterMap);

        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();
        if (currentNode == null) {
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    private static FailNode createFailNode(Map<String, Object> parameterMap) {

        FailNode failNode = new FailNode();

        for (Map.Entry<String, Object> entry : parameterMap.entrySet()) {
            if (FrameworkConstants.JSAttributes.JS_SHOW_ERROR_PAGE.equals(entry.getKey())) {
                failNode.setShowErrorPage(Boolean.TRUE.equals(entry.getValue()));
            } else if (FrameworkConstants.JSAttributes.JS_PAGE_URI.equals(entry.getKey())) {
                failNode.setErrorPageUri(String.valueOf(entry.getValue()));
            } else {
                failNode.getFailureData().put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return failNode;
    }

    /**
     * Add authentication fail node to the authentication graph in the initial request.
     *
     * @param parameterMap Parameters needed to send the error.
     */
    // TODO: This method works in conditional mode and need to implement separate method for dynamic mode
    public void sendError(Map<String, Object> parameterMap) {

        FailNode newNode = createFailNode(parameterMap);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param parameterMap parameterMap
     */
    public void executeStep(Map<String, Object> parameterMap) {
        //TODO: Use Step Name instead of Step ID (integer)
        StepConfig stepConfig = null;
        Object stepIdObj = parameterMap.get(JsStepConstants.STEP_ID);
        if (stepIdObj instanceof String) {
            String stepId = (String) stepIdObj;
            stepConfig = stepNamedMap.get(stepId);
        }
        if (stepConfig == null) {
            log.error("Given Authentication Step :" + stepIdObj + " is not in Environment");
            return;
        }
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
        currentNode = newNode;
        attachEventListeners((Map<String, Object>) parameterMap.get(JsStepConstants.STEP_EVENT_ON));
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param parameterMap parameterMap
     */
    public static void executeStepInAsyncEvent(Map<String, Object> parameterMap) {
        //TODO: Use Step Name instead of Step ID (integer)
        //TODO: can get the context from ThreadLocal. so that javascript does not have context as a parameter.
        AuthenticationContext context = contextForJs.get();
        AuthGraphNode currentNode = dynamicallyBuiltBaseNode.get();

        Object idObj = parameterMap.get(JsStepConstants.STEP_ID);
        Integer id = idObj instanceof Integer ? (Integer) idObj : Integer.parseInt(String.valueOf(idObj));
        if (log.isDebugEnabled()) {
            log.debug("Execute Step on async event. Step ID : " + id);
        }
        AuthenticationGraph graph = context.getSequenceConfig().getAuthenticationGraph();
        if (graph == null) {
            log.error("The graph happens to be null on the sequence config. Can not execute step : " + id);
            return;
        }

        StepConfig stepConfig = graph.getStepMap().get(id);
        if (log.isDebugEnabled()) {
            log.debug("Found step for the Step ID : " + id + ", Step Config " + stepConfig);
        }
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting a new node at the first time. Node : " + newNode.getName());
            }
            dynamicallyBuiltBaseNode.set(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }

        attachEventListeners((Map<String, Object>) parameterMap.get(JsStepConstants.STEP_EVENT_ON), newNode);
    }

    /**
     * Adds a function to show a prompt in Javascript code.
     *
     * @param parameterMap parameterMap
     */
    public static void addLongWaitProcess(AsyncProcess asyncProcess,
                                          Map<String, Object> parameterMap) {

        addLongWaitProcess(getCurrentBuilder(), asyncProcess, parameterMap);
    }

    private static void addLongWaitProcess(JsGraphBuilder jsGraphBuilder, AsyncProcess asyncProcess,
                                           Map<String, Object> parameterMap) {

        LongWaitNode newNode = new LongWaitNode(asyncProcess);

        Map<String, Object> eventHandlers = (Map<String, Object>) parameterMap.get(JsStepConstants.STEP_EVENT_ON);
        if (eventHandlers != null) {
            addEventListeners(newNode, eventHandlers);
        }
        if (jsGraphBuilder.currentNode == null) {
            jsGraphBuilder.result.setStartNode(newNode);
        } else {
            attachToLeaf(jsGraphBuilder.currentNode, newNode);
        }

        jsGraphBuilder.currentNode = newNode;
    }

    private static void attachEventListeners(Map<String, Object> eventsMap, AuthGraphNode currentNode) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
        }
    }

    private void attachEventListeners(Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        addEventListeners(decisionNode, eventsMap);
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
            currentNode = decisionNode;
        }
    }

    /**
     * Adds all the event listeners to the decision node.
     *
     * @param eventsMap Map of events and event handler functions, which is handled by this execution.
     * @return created Dynamic Decision node.
     */
    private static void addEventListeners(DynamicDecisionNode decisionNode,
                                          Map<String, Object> eventsMap) {

        if (eventsMap == null) {
            return;
        }
        eventsMap.forEach((key, value) -> {
            if (value instanceof ScriptObjectMirror) {
                SerializableJsFunction jsFunction = SerializableJsFunction
                        .toSerializableForm(key, (ScriptObjectMirror) value);
                if (jsFunction != null) {
                    decisionNode.addFunction(key, jsFunction);
                } else {
                    log.error("Event handler : " + key + " is not a function : " + value);
                }
            }
        });
    }

    /**
     * Attach the new node to the destination node.
     * Any immediate branches available in the destination will be re-attached to the new node.
     * New node may be cloned if needed to attach on multiple branches.
     *
     * @param destination Current node.
     * @param newNode New node to attach.
     */
    private static void infuse(AuthGraphNode destination, AuthGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            attachToLeaf(newNode, stepConfigGraphNode.getNext());
            stepConfigGraphNode.setNext(newNode);
        } else if (destination instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) destination;
            attachToLeaf(newNode, dynamicDecisionNode.getDefaultEdge());
            dynamicDecisionNode.setDefaultEdge(newNode);
        } else {
            log.error("Can not infuse nodes in node type : " + destination);
        }

    }

    /**
     * Attach the new node to end of the base node.
     * The new node is added to each leaf node of the Tree structure given in the destination node.
     * Effectively this will join all the leaf nodes to new node, converting the tree into a graph.
     *
     * @param baseNode Base node.
     * @param nodeToAttach Node to attach.
     */
    private static void attachToLeaf(AuthGraphNode baseNode, AuthGraphNode nodeToAttach) {

        if (baseNode instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) baseNode);
            if (stepConfigGraphNode.getNext() == null) {
                stepConfigGraphNode.setNext(nodeToAttach);
            } else {
                attachToLeaf(stepConfigGraphNode.getNext(), nodeToAttach);
            }
        } else if (baseNode instanceof LongWaitNode) {
            LongWaitNode longWaitNode = (LongWaitNode) baseNode;
            longWaitNode.setDefaultEdge(nodeToAttach);
        } else if (baseNode instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) baseNode;
            dynamicDecisionNode.setDefaultEdge(nodeToAttach);
        } else if (baseNode instanceof EndStep) {
            if (log.isDebugEnabled()) {
                log.debug("The destination is an End Step. Unable to attach the node : " + nodeToAttach);
            }
        } else if (baseNode instanceof FailNode) {
            if (log.isDebugEnabled()) {
                log.debug("The destination is an Fail Step. Unable to attach the node : " + nodeToAttach);
            }
        } else {
            log.error("Unknown graph node found : " + baseNode);
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

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public static class JsBasedEvaluator implements AuthenticationDecisionEvaluator {

        private static final long serialVersionUID = 6853505881096840344L;
        private SerializableJsFunction jsFunction;

        public JsBasedEvaluator(SerializableJsFunction jsFunction) {

            this.jsFunction = jsFunction;
        }

        @Override
        public String evaluate(AuthenticationContext authenticationContext, Consumer<JSObject> jsConsumer) {

            String result = null;
            if (jsFunction.isFunction()) {
                ScriptEngine scriptEngine = getEngine(authenticationContext);
                try {
                    JsGraphBuilderFactory.restoreCurrentContext(authenticationContext, scriptEngine);
                    Bindings globalBindings = scriptEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
                    //Now re-assign the executeStep function to dynamic evaluation
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_EXECUTE_STEP,
                            (Consumer<Map>) JsGraphBuilder::executeStepInAsyncEvent);
                    globalBindings.put(FrameworkConstants.JSAttributes.JS_FUNC_SEND_ERROR,
                            (Consumer<Map>) JsGraphBuilder::sendErrorAsync);
                    JsFunctionRegistry jsFunctionRegistry = FrameworkServiceDataHolder.getInstance()
                            .getJsFunctionRegistry();
                    if (jsFunctionRegistry != null) {
                        Map<String, Object> functionMap = jsFunctionRegistry
                                .getSubsystemFunctionsMap(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER);
                        functionMap.forEach(globalBindings::put);
                    }
                    Compilable compilable = (Compilable) scriptEngine;
                    JsGraphBuilder.contextForJs.set(authenticationContext);

                    CompiledScript compiledScript = compilable.compile(jsFunction.getSource());
                    JSObject builderFunction = (JSObject) compiledScript.eval();
                    jsConsumer.accept(builderFunction);

                    JsGraphBuilderFactory.persistCurrentContext(authenticationContext, scriptEngine);
                    //TODO: New method ...
                    AuthGraphNode executingNode = (AuthGraphNode) authenticationContext
                            .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
                    if (canInfuse(executingNode)) {
                        infuse(executingNode, dynamicallyBuiltBaseNode.get());
                    }

                } catch (Throwable e) {
                    //We need to catch all the javascript errors here, then log and handle.
                    //TODO: do proper error handling
                    log.error("Error in executing the javascript for service provider : " + authenticationContext
                            .getServiceProviderName() + ", Javascript Fragment : \n" + jsFunction.getSource(), e);
                } finally {
                    contextForJs.remove();
                    dynamicallyBuiltBaseNode.remove();
                }

            } else {
                result = jsFunction.getSource();
            }
            return result;
        }

        @Deprecated
        public String evaluate(AuthenticationContext authenticationContext) {

            return this.evaluate(authenticationContext, (fn) -> {
                fn.call(null, new JsAuthenticationContext
                        (authenticationContext));
            });
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
