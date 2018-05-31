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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.identity.application.authentication.framework.AsyncCaller;
import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.AsyncReturn;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.DynamicDecisionNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.EndStep;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.FailNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.LongWaitNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.SerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.JsFailureException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.FAIL_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.INCOMPLETE;
import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.SUCCESS_COMPLETED;

public class GraphBasedSequenceHandler extends DefaultStepBasedSequenceHandler implements SequenceHandler {

    private static final Log log = LogFactory.getLog(GraphBasedSequenceHandler.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        String authenticationType = sequenceConfig.getApplicationConfig().getServiceProvider()
            .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        if (graph == null || !graph.isEnabled() || !ApplicationConstants.AUTH_TYPE_FLOW.equals(authenticationType)) {
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
        if (currentNode instanceof LongWaitNode) {
            isInterrupt = handleLongWait(request, response, context, sequenceConfig, (LongWaitNode) currentNode);
        } else if (currentNode instanceof DynamicDecisionNode) {
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
            handleAuthFail(request, response, context, sequenceConfig, (FailNode)currentNode);
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
            if (log.isDebugEnabled()) {
                log.debug("No Next node found for the current graph node : " + currentNode.getName() +
                        ", Service Provider: " + context.getServiceProviderName() +
                        " . Ending the authentication flow.");
            }
            nextNode = new EndStep();
        }

        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
    }

    private void handleEndOfSequence(HttpServletRequest request, HttpServletResponse response,
                                     AuthenticationContext context, SequenceConfig sequenceConfig) throws
            FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("There are no more steps to execute");
        }

        context.getSequenceConfig().setCompleted(true);
        context.setRequestAuthenticated(true);

        // if no step failed at authentication we should do post authentication work (e.g.
        // claim handling, provision etc)

        if (log.isDebugEnabled()) {
            log.debug("Request is successfully authenticated");
        }

        handlePostAuthentication(request, response, context);

        // we should get out of steps now.
        if (log.isDebugEnabled()) {
            log.debug("Step processing is completed");
        }
    }

    /**
     * Process FailNode.
     * @param request HTTP Servlet request
     * @param response HTTP Servlet Response
     * @param context Authentication Context
     * @param sequenceConfig Sequence Config
     * @param node Fail Node
     * @throws FrameworkException
     */
    private void handleAuthFail(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
                                SequenceConfig sequenceConfig, FailNode node) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Found a Fail Node in conditional authentication");
        }
        String errorPage = node.getErrorPageUri();
        String redirectURL = null;
        if (StringUtils.isBlank(errorPage)) {
            errorPage = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
        }
        try {
            URIBuilder uriBuilder = new URIBuilder(errorPage);
            node.getFailureData().forEach(uriBuilder::addParameter);
            redirectURL = uriBuilder.toString();
            response.sendRedirect(redirectURL);
        } catch (IOException e) {
            throw new FrameworkException("Error when redirecting user to " + errorPage, e);
        } catch (URISyntaxException e) {
            throw new FrameworkException("Error when redirecting user to " + errorPage + ". Error page is not a valid "
                + "URL.", e);
        }

        context.setRequestAuthenticated(false);
        context.getSequenceConfig().setCompleted(true);
        request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
        throw new JsFailureException("Error initiated from authentication script. User will be redirected to " +
            redirectURL);
    }

    private boolean handleAuthenticationStep(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context, SequenceConfig sequenceConfig,
                                             StepConfigGraphNode stepConfigGraphNode)
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

        if (flowStatus != SUCCESS_COMPLETED && flowStatus != INCOMPLETE) {
            stepConfig.setSubjectAttributeStep(false);
            stepConfig.setSubjectIdentifierStep(false);
        }

        if (flowStatus == FAIL_COMPLETED) {
            if (stepConfigGraphNode.getNext() instanceof EndStep) {
                stepConfigGraphNode.setNext(new FailNode());
            }
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

    private boolean handleLongWait(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationContext context, SequenceConfig sequenceConfig,
                                   LongWaitNode longWaitNode) throws FrameworkException {

        boolean isWaiting;
        LongWaitStatusStoreService longWaitStatusStoreService =
                FrameworkServiceDataHolder.getInstance().getLongWaitStatusStoreService();
        // TODO: Check why context.getSessionIdentifier() is null.
        String longWaitKey = (String) context.getProperty(FrameworkConstants.LONG_WAIT_KEY);
        LongWaitStatus longWaitStatus = longWaitStatusStoreService.getWait(longWaitKey);
        if (longWaitKey == null || longWaitStatus == null) {
            //This is a initiation of long wait
            longWaitStatus = new LongWaitStatus();
            longWaitKey = UUID.randomUUID().toString();
            context.setProperty(FrameworkConstants.LONG_WAIT_KEY, longWaitKey);
            longWaitStatusStoreService.addWait(longWaitKey, longWaitStatus);
            isWaiting = callExternalSystem(request, response, context, sequenceConfig, longWaitNode);
        } else {
            // This is a continuation of long wait
            isWaiting = LongWaitStatus.Status.COMPLETED != longWaitStatus.getStatus();
            context.removeProperty(FrameworkConstants.LONG_WAIT_KEY);
            longWaitStatusStoreService.removeWait(longWaitKey);
            String outcomeName = (String) context.getProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS);
            Map<String, Object> data = (Map<String, Object>) context.getProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA);
            context.removeProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS);
            context.removeProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA);
            if (outcomeName != null) {
                executeFunction(outcomeName, longWaitNode, context, data);
            }
            AuthGraphNode nextNode = longWaitNode.getDefaultEdge();
            context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
        }
        return isWaiting;
    }

    private void resumeLongWait(HttpServletRequest request, HttpServletResponse response,
                                AuthenticationContext context) throws
            FrameworkException {

        handle(request, response, context);
    }

    private boolean callExternalSystem(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationContext context, SequenceConfig sequenceConfig,
                                       LongWaitNode longWaitNode) throws FrameworkException {

        AsyncProcess asyncProcess = longWaitNode.getAsyncProcess();
        if (asyncProcess == null) {
            return false;
        }
        AsyncCaller caller = asyncProcess.getAsyncCaller();

        //Need to lock the thread since the response should be continued in the same thread.
        //TODO: As the thread is blocked, this can degrade performance. We have to add a mechanism to continue the
        // response from out side in a new thread.
        AsyncReturn asyncReturn = rethrowTriConsumer((authenticationContext, data, result) -> {
            authenticationContext.setProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS, result);
            authenticationContext.setProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA, data);
            synchronized (context) {
                context.notify();
            }
        });

        if (caller != null) {
            FrameworkServiceDataHolder.getInstance().getAsyncSequenceExecutor().exec(caller, asyncReturn, context);
            synchronized (context) {
                try {
                    context.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Error while waiting for the external call the complete. ", e);
                }
            }
            resumeLongWait(request, response, context);
            return true;
        }
        return false;
    }

    private void handleDecisionPoint(HttpServletRequest request, HttpServletResponse response,
                                     AuthenticationContext context, SequenceConfig sequenceConfig,
                                     DynamicDecisionNode dynamicDecisionNode)
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
                executeFunction("onSuccess", dynamicDecisionNode, context);
                break;
            case FAIL_COMPLETED:
                executeFunction("onFail", dynamicDecisionNode, context);
                if (dynamicDecisionNode.getDefaultEdge() instanceof EndStep) {
                    dynamicDecisionNode.setDefaultEdge(new FailNode());
                }
                break;
            case FALLBACK:
                executeFunction("onFallback", dynamicDecisionNode, context);
                break;
            }
        }

        AuthGraphNode nextNode = dynamicDecisionNode.getDefaultEdge();
        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
    }

    private void executeFunction(String outcomeName, DynamicDecisionNode dynamicDecisionNode,
                                 AuthenticationContext context) {

        SerializableJsFunction fn = dynamicDecisionNode.getFunctionMap().get(outcomeName);
        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        JsGraphBuilderFactory jsGraphBuilderFactory = dataHolder.getJsGraphBuilderFactory();
        JsGraphBuilder graphBuilder = jsGraphBuilderFactory.createBuilder(context, context
                .getSequenceConfig().getAuthenticationGraph().getStepMap(), dynamicDecisionNode);
        JsGraphBuilder.JsBasedEvaluator jsBasedEvaluator = graphBuilder.new JsBasedEvaluator(fn);
        jsBasedEvaluator.evaluate(context, (func) -> {
            func.call(null, new JsAuthenticationContext(context));
        });
        if (dynamicDecisionNode.getDefaultEdge() == null) {
            dynamicDecisionNode.setDefaultEdge(new EndStep());
        }
    }

    private void executeFunction(String outcomeName, DynamicDecisionNode dynamicDecisionNode,
                                 AuthenticationContext context, Map<String, Object> data) {

        SerializableJsFunction fn = dynamicDecisionNode.getFunctionMap().get(outcomeName);
        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        JsGraphBuilderFactory jsGraphBuilderFactory = dataHolder.getJsGraphBuilderFactory();
        JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, context
                .getSequenceConfig().getAuthenticationGraph().getStepMap(), dynamicDecisionNode);
        JsGraphBuilder.JsBasedEvaluator jsBasedEvaluator = jsGraphBuilder.new JsBasedEvaluator(fn);
        jsBasedEvaluator.evaluate(context, (func) -> {
            func.call(null, new JsAuthenticationContext(context), new JsParameters(data));
        });
        if (dynamicDecisionNode.getDefaultEdge() == null) {
            dynamicDecisionNode.setDefaultEdge(new EndStep());
        }
    }

    private boolean handleInitialize(HttpServletRequest request, HttpServletResponse response,
                                     AuthenticationContext context, SequenceConfig sequenceConfig,
                                     AuthenticationGraph graph)
            throws FrameworkException {

        AuthGraphNode startNode = graph.getStartNode();
        if (startNode == null) {
            throw new FrameworkException("Start node is not set for authentication graph:" + graph.getName());
        }
        context.setCurrentStep(0);
        return handleNode(request, response, context, sequenceConfig, startNode);
    }

    /**
     * This method allows a BiConsumer which throws exceptions to be used in places which expects a BiConsumer.
     *
     * @param <T>         the type of the input to the function
     * @param <U>         the type of the input to the function
     * @param <V>         the type of the input to the function
     * @param <E>         the type of Exception
     * @param triConsumer instances of the {@code TriConsumerWithExceptions} functional interface
     * @return an instance of the {@code BiConsumer}
     */
    public static <T, U , V , E extends Exception> AsyncReturn rethrowTriConsumer(AsyncReturn triConsumer) {

        return (t, u, v) -> {
            try {
                triConsumer.accept(t, u, v);
            } catch (Exception exception) {
                throwAsUnchecked(exception);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {

        throw (E) exception;
    }
}