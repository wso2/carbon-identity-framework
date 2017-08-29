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
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator2;
import org.wso2.carbon.identity.application.authentication.framework.JsFunctionRegistrar;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GetAmrArrayFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectAcrFromFunction;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.SelectOneFunction;
import org.wso2.carbon.identity.application.common.model.graph.StepNode;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
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
    private JsFunctionRegistrarImpl jsFunctionRegistrar;
    private static final Log jsLog = LogFactory.getLog(JsGraphBuilder.class.getPackage().getName() + ".JsFunction");

    /**
     * Constructs the builder with the given authentication context.
     *
     * @param authenticationContext  current authentication context.
     * @param stepConfigMap The Step map from the service provider configuration.
     */
    public JsGraphBuilder(AuthenticationContext authenticationContext, Map<Integer, StepConfig> stepConfigMap) {

        this.authenticationContext = authenticationContext;
        stepNamedMap = stepConfigMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
    }

    /**
     * Returns the built graph.
     * @return
     */
    public AuthenticationGraph getGraph() {

        return result;
    }

    /**
     * Creates the graph with the given Script and step map.
     *
     * @param script the Dynamic authentication script.
     */
    public void createWith(String script) {

        //TODO: Find out how to reuse script engine.
        //TODO: Find out how to disable unwanted functions,e.g. Java.x.y.z calls
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        engine.put("execute", (Consumer<String>) this::execute);
        engine.put("makeDecisionWith", (Function<Object, DecisionBuilder>) this::makeDecisionWith);
        engine.put("log", jsLog);

        SelectAcrFromFunction selectAcrFromFunction = new SelectAcrFromFunction();
        engine.put("selectAcrFrom", (SelectOneFunction) selectAcrFromFunction::evaluate);

        GetAmrArrayFunction getAmrArrayFunction = new GetAmrArrayFunction();
        engine.put("getRequestedAmr", (Function<AuthenticationContext, String[]>) getAmrArrayFunction::evaluate);

        if (jsFunctionRegistrar != null) {
            jsFunctionRegistrar.stream(JsFunctionRegistrar.Subsystem.SEQUENCE_HANDLER, entry -> {
                engine.put(entry.getKey(), entry.getValue());
            });
        }

        try {
            Compilable compilable = (Compilable) engine;
            CompiledScript compiledScript = compilable.compile(script);
            JSObject builderFunction = (JSObject) compiledScript.eval();
            builderFunction.call(null, authenticationContext);
        } catch (ScriptException e) {
            //TODO: Find out how to handle script engine errors
            log.error("Error in executing the Javascript.", e);
        }

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
                log.error(currentNode + " is not supported in execute()");
            }
        }
        currentNode = decisionNode;

        AuthenticationDecisionEvaluator2 evaluator2 = new AuthenticationDecisionEvaluator2() {

            @Override
            public String evaluate(AuthenticationContext context) {
                Object result = null;
                try {
                    if (objectMirror instanceof ScriptObjectMirror) {
                        result = ((ScriptObjectMirror) objectMirror).call(this, context);
                    } else {
                        result = String.valueOf(objectMirror);
                    }
                } catch (Throwable t) {
                    //TODO: Handle and create proper Exception
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
     * Attach the new node to end of the destination node.
     * The new node is added to each leaf node of the Tree structure given in the destination node.
     * Effectively this will join all the leaf nodes to new node, converting the tree into a graph.
     *
     * @param destination
     * @param newNode
     */
    private void attachToLeaf(AuthGraphNode destination, StepConfigGraphNode newNode) {

        if (destination instanceof StepConfigGraphNode) {
            StepConfigGraphNode stepConfigGraphNode = ((StepConfigGraphNode) destination);
            if (stepConfigGraphNode.getNext() == null) {
                stepConfigGraphNode.setNext(newNode);
            } else {
                attachToLeaf(stepConfigGraphNode.getNext(), newNode);
            }
        } else if (currentNode instanceof AuthDecisionPointNode) {
            AuthDecisionPointNode decisionPointNode = (AuthDecisionPointNode) currentNode;
            if (decisionPointNode.getDefaultEdge() == null) {
                decisionPointNode.setDefaultEdge(newNode);
            } else {
                attachToLeaf(decisionPointNode.getDefaultEdge(), newNode);
            }
            decisionPointNode.getOutcomes().stream().forEach(o -> attachToLeaf(o.getDestination(), newNode));
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

        StepNode stepNode = new StepNode();
        stepNode.setName("TODO:SetName");
        stepNode.setAuthenticationStep(null);
        StepConfigGraphNode stepConfigGraphNode = new StepConfigGraphNode(stepConfig);
        stepConfigGraphNode.setConfig(stepNode);
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
                decisionNode.putOutcome(outcomeName, new DecisionOutcome(newNode, null));
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

    public void setJsFunctionRegistrar(JsFunctionRegistrarImpl jsFunctionRegistrar) {
        this.jsFunctionRegistrar = jsFunctionRegistrar;
    }
}
