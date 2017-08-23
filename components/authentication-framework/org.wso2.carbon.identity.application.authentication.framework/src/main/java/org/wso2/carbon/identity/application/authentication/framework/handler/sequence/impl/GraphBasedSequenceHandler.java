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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDecisionEvaluator2;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthDecisionPointNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.DecisionOutcome;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.EndStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphBasedSequenceHandler extends DefaultStepBasedSequenceHandler implements SequenceHandler {

    private static final Log log = LogFactory.getLog(GraphBasedSequenceHandler.class);
    private static final String PROP_CURRENT_NODE = "Adaptive.Auth.Current.Graph.Node";
    private Map<String, AuthenticationDecisionEvaluator> authenticationDecisionEvaluatorMap = new HashMap<>();
    private volatile boolean isInitialized = false;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {
        lazyInit();
        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        if (graph == null) {
            //Handle pre-configured step array
            if (log.isDebugEnabled()) {
                log.debug("Authentication Graph not defined for the application. "
                        + "Performing Step based authentication. Service Provider :" + sequenceConfig
                        .getApplicationId());
            }
            DefaultStepBasedSequenceHandler.getInstance().handle(request, response, context);
            return;
        }

        boolean isInterrupted = false;
        while (!isInterrupted && !context.getSequenceConfig().isCompleted()) {

            AuthGraphNode currentNode = (AuthGraphNode) context.getProperty(PROP_CURRENT_NODE);
            if (currentNode == null) {
                isInterrupted = handleInitialize(request, response, context, sequenceConfig, graph);
            } else {
                isInterrupted = handleNode(request, response, context, sequenceConfig, currentNode);
            }
        }
    }

    /**
     * Initialize the class on demand.
     * This is done because the IS constructs the sequence handler with reflection, but at the time other contributions
     * from OSGI may not be ready.
     * We can remove the lazy initialize method if we change the SequenceHandler extension point to use OSGI wiring.
     */
    protected void lazyInit() {
        if (!isInitialized) {
            synchronized (this) {
                if (!isInitialized) {
                    List<AuthenticationDecisionEvaluator> evaluatorList = FrameworkServiceDataHolder.getInstance()
                            .getAuthenticationDecisionEvaluators();
                    for (AuthenticationDecisionEvaluator evaluator : evaluatorList) {
                        addDecisionEvaluator(evaluator);
                    }
                    isInitialized = true;
                }
            }
        }
    }

    private boolean handleNode(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
            SequenceConfig sequenceConfig, AuthGraphNode currentNode) throws FrameworkException {
        context.setProperty(PROP_CURRENT_NODE, currentNode);
        boolean isInterrupt = false;
        if (currentNode instanceof AuthDecisionPointNode) {
            handleDecisionPoint(request, response, context, sequenceConfig, (AuthDecisionPointNode) currentNode);
        } else if (currentNode instanceof StepConfigGraphNode) {
            isInterrupt = handleAuthenticationStep(request, response, context, sequenceConfig,
                    (StepConfigGraphNode) currentNode);
            if (!isInterrupt) {
                gotoToNextNode(context, sequenceConfig, currentNode);
            }
        } else if (currentNode instanceof EndStep) {
            handleEndOfSequence(request, response, context, sequenceConfig);
        }
        return isInterrupt;
    }

    private void gotoToNextNode(AuthenticationContext context, SequenceConfig sequenceConfig,
            AuthGraphNode currentNode) {
        AuthGraphNode nextNode = null;
        if (currentNode instanceof StepConfigGraphNode) {
            nextNode = ((StepConfigGraphNode) currentNode).getNext();
        }
        if (nextNode == null) {
            log.error(
                    "No Next node found for the current graph node : " + currentNode.getName() + ", Service Provider: "
                            + context.getServiceProviderName() + " . Ending the authentication flow.");
            nextNode = new EndStep();
        }

        context.setProperty(PROP_CURRENT_NODE, nextNode);
    }

    private void handleEndOfSequence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("There are no more steps to execute");
        }

        // if no step failed at authentication we should do post authentication work (e.g.
        // claim handling, provision etc)
        if (context.isRequestAuthenticated()) {

            if (log.isDebugEnabled()) {
                log.debug("Request is successfully authenticated");
            }

            context.getSequenceConfig().setCompleted(true);
            handlePostAuthentication(request, response, context);

        }

        // we should get out of steps now.
        if (log.isDebugEnabled()) {
            log.debug("Step processing is completed");
        }
    }

    private boolean handleAuthenticationStep(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig, StepConfigGraphNode stepConfigGraphNode)
            throws FrameworkException {

        StepConfig stepConfig = stepConfigGraphNode.getStepConfig();
        if (stepConfig == null) {
            throw new FrameworkException("StepConfig not found while handling the step. Service Provider : " + context
                    .getServiceProviderName());
        }

        // if the current step is completed
        if (stepConfig.isCompleted()) {
            stepConfig.setCompleted(false);
            stepConfig.setRetrying(false);

            // if the request didn't fail during the step execution
            if (context.isRequestAuthenticated()) {
                if (log.isDebugEnabled()) {
                    log.debug("Step " + stepConfig.getOrder() + " is completed. Going to get the next one.");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication has failed in the Step " + (context.getCurrentStep()));
                }
                // if the step contains multiple login options, we should give the user to retry
                // authentication
                if (stepConfig.isMultiOption() && !context.isPassiveAuthenticate()) {
                    stepConfig.setRetrying(true);
                    context.setRequestAuthenticated(true);
                } else {
                    context.getSequenceConfig().setCompleted(true);
                    resetAuthenticationContext(context);
                }
            }

            resetAuthenticationContext(context);
        }

        // if the sequence is not completed, we have work to do.
        if (log.isDebugEnabled()) {
            log.debug("Starting Step: " + stepConfig.getOrder());
        }

        int stepNumber = context.getCurrentStep();
        if (!context.isReturning()) {
            if (stepNumber <= 0) {
                stepNumber = 1;
            } else {
                stepNumber++;
            }
            context.setCurrentStep(stepNumber);
            context.getSequenceConfig().getStepMap().put(stepNumber, stepConfig);
        }

        FrameworkUtils.getStepHandler().handle(request, response, context);

        // if step is not completed, that means step wants to redirect to outside
        if (!stepConfig.isCompleted()) {
            if (log.isDebugEnabled()) {
                log.debug("Step is not complete yet. Redirecting to outside.");
            }
            return true;
        }

        context.setReturning(false);
        return false;
    }

    private void handleDecisionPoint(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig, AuthDecisionPointNode decisionPointNode)
            throws FrameworkException {
        if (decisionPointNode == null) {
            log.error("Decision Point node is null");
            return;
        }
        String nextOutcome = null;

        AuthenticationDecisionEvaluator2 evaluator2 = decisionPointNode.getAuthenticationDecisionEvaluator();
        if(evaluator2 != null) {
            nextOutcome = evaluator2.evaluate(context);
            if (log.isDebugEnabled()) {
                log.debug("Outcome returned as : " + nextOutcome + " by the evaluator : " + decisionPointNode
                        .getEvaluatorName());
            }
        }

        AuthenticationDecisionEvaluator evaluator = authenticationDecisionEvaluatorMap
                .get(decisionPointNode.getEvaluatorName());
        if (evaluator == null) {
            String errorMessage = String.format("No evaluator registered for the evaluator name : %s,"
                            + " at Decision : %s, on Service Provider: %s", decisionPointNode.getEvaluatorName(),
                    decisionPointNode.getName(), sequenceConfig.getApplicationId());
            log.error(errorMessage);
        } else {
            nextOutcome = evaluator.evaluate(context, null, decisionPointNode.getConfig());
            if (log.isDebugEnabled()) {
                log.debug("Outcome returned as : " + nextOutcome + " by the evaluator : " + decisionPointNode
                        .getEvaluatorName());
            }
        }

        AuthGraphNode nextNode = null;
        if (nextOutcome != null) {
            DecisionOutcome outcome = decisionPointNode.getOutcome(nextOutcome);
            if (outcome != null) {
                nextNode = outcome.getDestination();
            } else {
                String errorMessage = String
                        .format("Could not find the next outcome node for the outcome decision result : %s,"
                                        + "  at Decision : %s, on Service Provider: %s", nextOutcome,
                                decisionPointNode.getName(), sequenceConfig.getApplicationId());

                log.error(errorMessage);
            }
        }
        if (nextNode == null) {
            nextNode = decisionPointNode.getDefaultEdge();
            if (log.isDebugEnabled()) {
                String nextNodeName = nextNode == null ? null : nextNode.getName();
                String message = String.format("Selecting default outcome : %s, "
                                + "as evaluator : %s , could not select an outcome at the decision Point : %s", nextNodeName,
                        evaluator, decisionPointNode.getName());
                log.debug(message);
            }
        }

        context.setProperty(PROP_CURRENT_NODE, nextNode);
    }

    public void addDecisionEvaluator(AuthenticationDecisionEvaluator evaluator) {
        authenticationDecisionEvaluatorMap.put(evaluator.getClass().getName(), evaluator);
        authenticationDecisionEvaluatorMap.put(evaluator.getClass().getSimpleName(), evaluator);
    }

    private boolean handleInitialize(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig, AuthenticationGraph graph)
            throws FrameworkException {
        AuthGraphNode startNode = graph.getStartNode();
        if (startNode == null) {
            throw new FrameworkException("Start node is not set for authentication graph:" + graph.getName());
        }
        context.setCurrentStep(0);
        return handleNode(request, response, context, sequenceConfig, startNode);
    }
}