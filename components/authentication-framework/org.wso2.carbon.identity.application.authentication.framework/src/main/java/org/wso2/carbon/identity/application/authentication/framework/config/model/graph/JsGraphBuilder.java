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
import jdk.nashorn.api.scripting.ScriptUtils;
import jdk.nashorn.internal.runtime.ScriptFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator2;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistry;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.store.JavascriptCache;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
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
    private JsFunctionRegistryImpl jsFunctionRegistrar;
    private ScriptEngine engine;
    private JavascriptCache javascriptCache;
    private static final String PROP_CURRENT_NODE = "Adaptive.Auth.Current.Graph.Node"; //TODO: same constant
    private static ThreadLocal<AuthenticationContext> contextForJs = new ThreadLocal<>();

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext  current authentication context.
     * @param stepConfigMap The Step map from the service provider configuration.
     */
    public JsGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap,
            ScriptEngine scriptEngine) {

        this.engine = scriptEngine;
        this.authenticationContext = authenticationContext;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue()));
    }

    /**
     * Returns the built graph.
     * @return
     */
    public AuthenticationGraph build() {
        if (currentNode != null && !(currentNode instanceof EndStep)) {
            attachToLeaf(currentNode, new EndStep());
        }
        return result;
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    public JsGraphBuilder createWith(String script) {

        CompiledScript compiledScript = null;
        if (javascriptCache != null) {
            compiledScript = javascriptCache.getScript(authenticationContext.getServiceProviderName());
        }
        try {
            if (compiledScript == null) {
                Compilable compilable = (Compilable) engine;
                //TODO: Think about keeping a cached compiled scripts. May be the last updated timestamp.
                compiledScript = compilable.compile(script);
            }
            Bindings bindings = engine.createBindings();
            bindings.put("executeStep", (Consumer<Map>) this::executeStep);
            bindings.put("makeDecisionWith", (Function<Object, DecisionBuilder>) this::makeDecisionWith);
            if (jsFunctionRegistrar != null) {
                jsFunctionRegistrar.stream(JsFunctionRegistry.Subsystem.SEQUENCE_HANDLER, entry -> {
                    bindings.put(entry.getKey(), entry.getValue());
                });
            }
            javascriptCache.putBindings(authenticationContext.getServiceProviderName(), bindings);

            JSObject builderFunction = (JSObject) compiledScript.eval(bindings);
            builderFunction.call(null, authenticationContext);

            //Now re-assign the executeStep function to dynamic evaluation
            bindings.put("executeStep", (Consumer<Map>) this::executeStepInAsyncEvent);
        } catch (ScriptException e) {
            //TODO: Find out how to handle script engine errors
            log.error("Error in executing the Javascript.", e);
        }
        return this;
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param stepId unique identifier for the step.
     */
    public void execute(String stepId) {
        //TODO: Use Step Name instead of Step ID (integer)
        StepConfig stepConfig = stepNamedMap.get(stepId);
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
        currentNode = newNode;
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param parameterMap parameterMap
     */
    public void executeStep(Map<String, Object> parameterMap) {
        //TODO: Use Step Name instead of Step ID (integer)
        StepConfig stepConfig = stepNamedMap.get(parameterMap.get("id"));
        StepConfigGraphNode newNode = wrap(stepConfig);
        if (currentNode == null) {
            result.setStartNode(newNode);
        } else {
            attachToLeaf(currentNode, newNode);
        }
        currentNode = newNode;
        attachEventListeners((Map<String, Object>) parameterMap.get("on"));
    }

    /**
     * Adds the step given by step ID tp the authentication graph.
     *
     * @param parameterMap parameterMap
     */
    public void executeStepInAsyncEvent(Map<String, Object> parameterMap) {
        //TODO: Use Step Name instead of Step ID (integer)
        //TODO: can get the context from ThreadLocal. so that javascript does not have context as a parameter.
        AuthenticationContext context = contextForJs.get();

        Object idObj = parameterMap.get("id");
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
        AuthGraphNode currentNode = (AuthGraphNode) context.getProperty(PROP_CURRENT_NODE);
        if (currentNode == null) {
            log.error("No current graph node to attach the dynamic executed event.");
        } else {
            infuse(currentNode, newNode);
        }

        attachEventListeners((Map<String, Object>) parameterMap.get("on"), newNode);
    }

    private void attachEventListeners(Map<String, Object> eventsMap, AuthGraphNode currentNode) {
        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        eventsMap.entrySet().stream().forEach(e -> {
            decisionNode.addFunction(e.getKey(), generateFunction(e.getValue()));
        });
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
        }
    }

    private void attachEventListeners(Map<String, Object> eventsMap) {
        if (eventsMap == null) {
            return;
        }
        DynamicDecisionNode decisionNode = new DynamicDecisionNode();
        eventsMap.entrySet().stream().forEach(e -> {
            decisionNode.addFunction(e.getKey(), generateFunction(e.getValue()));
        });
        if (!decisionNode.getFunctionMap().isEmpty()) {
            attachToLeaf(currentNode, decisionNode);
            currentNode = decisionNode;
        }
    }

    private Object generateFunction(Object value) {
        String source = null;
        boolean isFunction = false;
        if (value instanceof ScriptObjectMirror) {
            ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) value;
            ScriptFunction scriptFunction = (ScriptFunction) ScriptUtils.unwrap(scriptObjectMirror);
            isFunction = scriptObjectMirror.isFunction();
            source = scriptFunction.toSource();
        } else {
            source = String.valueOf(value);
        }

        JsBasedEvaluator evaluator2 = new JsBasedEvaluator(source, isFunction);
        return evaluator2;
    }

    /**
     * Javascript exposed function to execute a dynamic decision based on javascript.
     *
     * @param objectMirror Script Object.
     * @return a DecisionBuilder which can be used to further build the graph using the builder pattern.
     */
    public DecisionBuilder makeDecisionWith(final Object objectMirror) {
        AuthDecisionPointNode decisionNode = new AuthDecisionPointNode();
        if (currentNode == null) {
            result.setStartNode(decisionNode);
        } else {
            if (currentNode instanceof StepConfigGraphNode) {
                ((StepConfigGraphNode) currentNode).setNext(decisionNode);
            } else {
                log.error("The Decision Node can not be attached to the Node: " + currentNode);
            }
        }
        currentNode = decisionNode;

        AuthenticationDecisionEvaluator2 evaluator2 = new AuthenticationDecisionEvaluator2() {

            @Override
            public String evaluate(AuthenticationContext context) {
                Object result = null;
                try {
                    if (objectMirror instanceof ScriptObjectMirror) {
                        ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) objectMirror;
                        scriptObjectMirror.getProto();
                        result = ((ScriptObjectMirror) objectMirror).call(this, context);
                    } else {
                        result = String.valueOf(objectMirror);
                    }
                } catch (Throwable t) {
                    //Need to catch all exceptions here and log the error. We can not bubble up the exception as
                    //that will cause the runtime to crash for any user-script errors.
                    //TODO: Handle and create proper Exception
                    log.error(
                            "Unhandled Javascript error occurred while executing dynamic authentication flow in the application: "
                                    + authenticationContext.getServiceProviderName(), t);
                }
                if (result instanceof String) {
                    return (String) result;
                } else {
                    //TODO: throw error
                }
                return null;
            }
        };
        DecisionBuilder builder = new DecisionBuilder(decisionNode, evaluator2, stepNamedMap);
        return builder;
    }

    public DecisionBuilder makeDecisionWith(Function<AuthenticationContext, String> decisionFunction) {

        AuthDecisionPointNode decisionNode = new AuthDecisionPointNode();
        if (currentNode == null) {
            result.setStartNode(decisionNode);
        } else {
            if (currentNode instanceof StepConfigGraphNode) {
                ((StepConfigGraphNode) currentNode).setNext(decisionNode);
            } else {
                log.error(currentNode + " is not supported in execute()");
            }
        }
        currentNode = decisionNode;

        return new DecisionBuilder(decisionNode, context -> decisionFunction.apply(context), stepNamedMap);
    }

    /**
     * Attach the new node to the destination node.
     * Any immediate branches available in the destination will be re-attached to the new node.
     * New node may be cloned if needed to attach on multiple branches.
     *
     * @param destination
     * @param newNode
     */
    private static void infuse(AuthGraphNode destination, AuthGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            attachToLeaf(newNode, stepConfigGraphNode.getNext());
            stepConfigGraphNode.setNext(newNode);
        } else if (destination instanceof AuthDecisionPointNode) {
            AuthDecisionPointNode decisionPointNode = (AuthDecisionPointNode) destination;
            decisionPointNode.getOutcomes().stream().forEach(o -> {
                AuthGraphNode clonedNode = clone(newNode);
                attachToLeaf(clonedNode, o.getDestination());
                o.setDestination(clonedNode);
            });
        } else if (destination instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) destination;
            attachToLeaf(newNode, dynamicDecisionNode.getDefaultEdge());
            dynamicDecisionNode.setDefaultEdge(newNode);
        } else {
            log.error("Can not infuse nodes in node type : " + destination);
        }
    }

    private static AuthGraphNode clone(AuthGraphNode node) {
        if (node instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) node);
            return wrap(stepConfigGraphNode.getStepConfig());
        } else {
            log.error("Clone not implemented for the node type: " + node);
        }
        return null;
    }

    /**
     * Attach the new node to end of the destination node.
     * The new node is added to each leaf node of the Tree structure given in the destination node.
     * Effectively this will join all the leaf nodes to new node, converting the tree into a graph.
     *
     * @param destination
     * @param newNode
     */
    private static void attachToLeaf(AuthGraphNode destination, AuthGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            if (stepConfigGraphNode.getNext() == null) {
                stepConfigGraphNode.setNext(newNode);
            } else {
                attachToLeaf(stepConfigGraphNode.getNext(), newNode);
            }
        } else if (destination instanceof AuthDecisionPointNode) {
            AuthDecisionPointNode decisionPointNode = (AuthDecisionPointNode) destination;
            if (decisionPointNode.getDefaultEdge() == null) {
                decisionPointNode.setDefaultEdge(newNode);
            } else {
                attachToLeaf(decisionPointNode.getDefaultEdge(), newNode);
            }
            decisionPointNode.getOutcomes().stream().forEach(o -> attachToLeaf(o.getDestination(), newNode));
        } else if (destination instanceof DynamicDecisionNode) {
            DynamicDecisionNode dynamicDecisionNode = (DynamicDecisionNode) destination;
            dynamicDecisionNode.setDefaultEdge(newNode);
        } else if (destination instanceof EndStep) {
            if (log.isDebugEnabled()) {
                log.debug("The destination is an End Step. Unable to attach the node : " + newNode);
            }
        } else {
            log.error("Unknown graph node found : " + destination);
        }
    }

    /**
     * Creates the StepConfigGraphNode with given StepConfig.
     *
     * @param stepConfig Step Config Object.
     * @return built and wrapped new StepConfigGraphNode.
     */
    private static StepConfigGraphNode wrap(StepConfig stepConfig) {

        StepConfigGraphNode stepConfigGraphNode = new StepConfigGraphNode(stepConfig);
        return stepConfigGraphNode;
    }

    /**
     * Builder Object to build a decision logic.
     *
     */
    public static class DecisionBuilder {

        private AuthenticationDecisionEvaluator2 evaluatorFunction;
        private AuthDecisionPointNode decisionNode;
        private Map<String, StepConfig> stepNamedMap;

        public DecisionBuilder(AuthDecisionPointNode decisionNode, AuthenticationDecisionEvaluator2 evaluatorFunction,
                Map<String, StepConfig> stepNamedMap) {

            this.decisionNode = decisionNode;
            this.decisionNode.setAuthenticationDecisionEvaluator(evaluatorFunction);
            this.evaluatorFunction = evaluatorFunction;
            this.stepNamedMap = stepNamedMap;
        }

        public ConditionalExecutionBuilder when(String s) {

            return new ConditionalExecutionBuilder(s, this, decisionNode, stepNamedMap);
        }
    }

    /**
     * Build object to build a conditional execution.
     * This builds the graph nodes on the first run of the javascript.
     * This means graph nodes are statically built upon flow initialization.
     *
     */
    public static class ConditionalExecutionBuilder {

        private DecisionBuilder decisionBuilder;
        private AuthDecisionPointNode decisionNode;
        private String outcomeName;
        private Map<String, StepConfig> stepNamedMap;

        public ConditionalExecutionBuilder(String outcomeName, DecisionBuilder decisionBuilder,
                AuthDecisionPointNode decisionNode, Map<String, StepConfig> stepNamedMap) {

            this.outcomeName = outcomeName;
            this.decisionNode = decisionNode;
            this.decisionBuilder = decisionBuilder;
            this.stepNamedMap = stepNamedMap;
        }

        public ConditionalExecutionBuilder thenExecute(String stepId) {

            StepConfig stepConfig = stepNamedMap.get(stepId);
            StepConfigGraphNode newNode = wrap(stepConfig);
            if (outcomeName == null) {
                decisionNode.setDefaultEdge(newNode);
            } else {
                decisionNode.putOutcome(outcomeName, new DecisionOutcome(newNode));
            }
            return this;
        }

        public ConditionalExecutionBuilder when(String s) {

            return new ConditionalExecutionBuilder(s, decisionBuilder, decisionNode, stepNamedMap);
        }

        public ConditionalExecutionBuilder whenNoMatch() {

            return new ConditionalExecutionBuilder(null, decisionBuilder, decisionNode, stepNamedMap);
        }
    }

    /**
     * Javascript based Decision Evaluator implementation.
     * This is used to create the Authentication Graph structure dynamically on the fly while the authentication flow
     * is happening.
     * The graph is re-organized based on last execution of the decision.
     */
    public static class JsBasedEvaluator implements AuthenticationDecisionEvaluator2 {

        private static final long serialVersionUID = 6853505881096840344L;
        private String source;
        private boolean isFunction = false;

        public JsBasedEvaluator(String source, boolean isFunction) {
            this.source = source;
            this.isFunction = isFunction;
        }

        @Override
        public String evaluate(AuthenticationContext authenticationContext) {
            Bindings bindings = getJavascriptCache().getBindings(authenticationContext.getServiceProviderName());
            String result = null;
            if (isFunction) {
                Compilable compilable = (Compilable) getEngine();
                try {
                    JsGraphBuilder.contextForJs.set(authenticationContext);
                    CompiledScript compiledScript = compilable.compile(source);
                    JSObject builderFunction = (JSObject) compiledScript.eval(bindings);
                    Object scriptResult = builderFunction.call(null, authenticationContext);
                } catch (ScriptException e) {
                    //TODO: do proper error handling
                    log.error("Error in executing the javascript for service provider : " + authenticationContext
                            .getServiceProviderName(), e);
                } finally {
                    contextForJs.remove();
                }

            } else {
                result = source;
            } return result;
        }

        private ScriptEngine getEngine() {
            return FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory().getEngine();
        }

        private JavascriptCache getJavascriptCache() {
            return FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory().getJavascriptCache();
        }
    }

    public void setJsFunctionRegistry(JsFunctionRegistryImpl jsFunctionRegistrar) {
        this.jsFunctionRegistrar = jsFunctionRegistrar;
    }

    public void setJavascriptCache(JavascriptCache javascriptCache) {
        this.javascriptCache = javascriptCache;
    }
}
