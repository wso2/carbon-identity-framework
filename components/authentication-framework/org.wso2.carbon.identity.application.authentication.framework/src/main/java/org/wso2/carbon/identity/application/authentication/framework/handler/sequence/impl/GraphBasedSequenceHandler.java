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
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.DynamicDecisionNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.EndStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.FailNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphBasedSequenceHandler extends DefaultStepBasedSequenceHandler implements SequenceHandler {

    private static final Log log = LogFactory.getLog(GraphBasedSequenceHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        if (graph == null || !graph.isEnabled()) {
            //Handle pre-configured step array
            if (log.isDebugEnabled()) {
                log.debug("Authentication Graph not defined for the application. "
                        + "Performing Step based authentication. Service Provider :" + sequenceConfig
                        .getApplicationId());
            }
            DefaultStepBasedSequenceHandler.getInstance().handle(request, response, context);
            return;
        }
        if (!graph.isBuildSuccessful()) {
            throw new FrameworkException(
                    "Error while building graph from Javascript. Nested exception is: " + graph.getErrorReason());
        }

        boolean isInterrupted = false;
        while (!isInterrupted && !context.getSequenceConfig().isCompleted()) {

            AuthGraphNode currentNode = (AuthGraphNode) context
                    .getProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE);
            if (currentNode == null) {
                isInterrupted = handleInitialize(request, response, context, sequenceConfig, graph);
            } else {
                isInterrupted = handleNode(request, response, context, sequenceConfig, currentNode);
            }
        }
    }

    private boolean handleNode(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
            SequenceConfig sequenceConfig, AuthGraphNode currentNode) throws FrameworkException {
        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, currentNode);
        boolean isInterrupt = false;
        if (currentNode instanceof DynamicDecisionNode) {
            handleDecisionPoint(request, response, context, sequenceConfig, (DynamicDecisionNode) currentNode);
        } else if (currentNode instanceof StepConfigGraphNode) {
            isInterrupt = handleAuthenticationStep(request, response, context, sequenceConfig,
                    (StepConfigGraphNode) currentNode);
            if (!isInterrupt) {
                gotoToNextNode(context, sequenceConfig, currentNode);
            }
        } else if (currentNode instanceof EndStep) {
            handleEndOfSequence(request, response, context, sequenceConfig);
        } else if (currentNode instanceof FailNode) {
            handleAuthFail(request, response, context, sequenceConfig, currentNode);
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

        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
    }

    private void handleEndOfSequence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationContext context, SequenceConfig sequenceConfig) throws FrameworkException {
        if (log.isDebugEnabled()) {
            log.debug("There are no more steps to execute");
        }

        context.getSequenceConfig().setCompleted(true);

        // if no step failed at authentication we should do post authentication work (e.g.
        // claim handling, provision etc)
        if (context.isRequestAuthenticated()) {

            if (log.isDebugEnabled()) {
                log.debug("Request is successfully authenticated");
            }

            handlePostAuthentication(request, response, context);
        }

        // we should get out of steps now.
        if (log.isDebugEnabled()) {
            log.debug("Step processing is completed");
        }
    }

    /**
     * Process FailNode.
     * @param request
     * @param response
     * @param context
     * @param sequenceConfig
     * @param node
     * @throws FrameworkException
     */
    private void handleAuthFail(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
            SequenceConfig sequenceConfig, AuthGraphNode node) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Found a Fail Node in conditional authentication");
        }
        context.setRequestAuthenticated(false);
        context.getSequenceConfig().setCompleted(true);
        //TODO:Need to consider sending error message to provided error page Uri instead of oauth redirect Uri
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

        AuthenticatorFlowStatus flowStatus = (AuthenticatorFlowStatus) request
                .getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);

        if (flowStatus != AuthenticatorFlowStatus.SUCCESS_COMPLETED) {
            stepConfig.setSubjectAttributeStep(false);
            stepConfig.setSubjectIdentifierStep(false);
        }

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
            AuthenticationContext context, SequenceConfig sequenceConfig, DynamicDecisionNode dynamicDecisionNode)
            throws FrameworkException {
        if (dynamicDecisionNode == null) {
            log.error("Dynamic decision node is null");
            return;
        }
        AuthenticatorFlowStatus flowStatus = (AuthenticatorFlowStatus) request
                .getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);
        if (flowStatus != null) {
            switch (flowStatus) {
            case SUCCESS_COMPLETED:
                executeFunction("success", dynamicDecisionNode, context);
                break;
            case FAIL_COMPLETED:
                executeFunction("fail", dynamicDecisionNode, context);
                break;
            case FALLBACK:
                executeFunction("fallback", dynamicDecisionNode, context);
                break;
            }
        }

        AuthGraphNode nextNode = dynamicDecisionNode.getDefaultEdge();
        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
    }

    private void executeFunction(String outcomeName, DynamicDecisionNode dynamicDecisionNode,
            AuthenticationContext context) {
        SerializableJsFunction fn = dynamicDecisionNode.getFunctionMap().get(outcomeName);
        JsGraphBuilder.JsBasedEvaluator jsBasedEvaluator = new JsGraphBuilder.JsBasedEvaluator(fn);
        jsBasedEvaluator.evaluate(context);
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