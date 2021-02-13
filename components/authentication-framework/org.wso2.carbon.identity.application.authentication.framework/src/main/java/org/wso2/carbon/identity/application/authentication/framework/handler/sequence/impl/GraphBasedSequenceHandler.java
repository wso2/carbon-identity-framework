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
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.ShowPromptNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.StepConfigGraphNode;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.JsFailureException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.SequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.model.LongWaitStatus;
import org.wso2.carbon.identity.application.authentication.framework.store.LongWaitStatusStoreService;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.FAIL_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.INCOMPLETE;
import static org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus.SUCCESS_COMPLETED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.ADAPTIVE_AUTH_LONG_WAIT_TIMEOUT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.BACK_TO_FIRST_STEP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.JSAttributes.PROP_CURRENT_NODE;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.promptOnLongWait;

public class GraphBasedSequenceHandler extends DefaultStepBasedSequenceHandler implements SequenceHandler {

    private static final Log log = LogFactory.getLog(GraphBasedSequenceHandler.class);
    private static final String PROMPT_DEFAULT_ACTION = "Success";
    private static final String PROMPT_ACTION_PREFIX = "action.";
    private static final String RESPONSE_HANDLED_BY_FRAMEWORK = "hasResponseHandledByFramework";
    public static final String SKIPPED_CALLBACK_NAME = "onSkip";
    public static final String STEP_IDENTIFIER_PARAM = "step";

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context)
            throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Executing the Step Based Authentication...");
        }

        if (isBackToFirstStep(context)) {
            modifyCurrentNodeAsFirstStep(context);
        }
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        String authenticationType = sequenceConfig.getApplicationConfig().getServiceProvider()
            .getLocalAndOutBoundAuthenticationConfig().getAuthenticationType();
        AuthenticationGraph graph = sequenceConfig.getAuthenticationGraph();
        if (graph == null || !graph.isEnabled() || (!ApplicationConstants.AUTH_TYPE_FLOW.equals(authenticationType) &&
                !ApplicationConstants.AUTH_TYPE_DEFAULT.equals(authenticationType))) {
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

    private void modifyCurrentNodeAsFirstStep(AuthenticationContext context) {

        context.removeProperty(BACK_TO_FIRST_STEP);
        if (context.getProperty(PROP_CURRENT_NODE) != null) {
            //Identifier first should be the first step. Other steps will be determine dynamically.
            int size = context.getSequenceConfig().getStepMap().size();
            for (int i = 2; i <= size; i++) {
                context.getSequenceConfig().getStepMap().remove(i);
            }
            AuthGraphNode parentNode = ((AuthGraphNode) context.getProperty(PROP_CURRENT_NODE)).getParent();
            while (parentNode != null && !isFirstStep(parentNode)) {
                if (parentNode instanceof DynamicDecisionNode) {
                    ((DynamicDecisionNode) parentNode).setDefaultEdge(new EndStep());
                }
                parentNode = parentNode.getParent();
            }
            context.setProperty(PROP_CURRENT_NODE, parentNode);
            if (log.isDebugEnabled()) {
                log.debug("Modified current node a parent node which can restart authentication flow" +
                        " from first step.");
            }
        }
    }

    private boolean isBackToFirstStep(AuthenticationContext context) {

        return context.getProperty(BACK_TO_FIRST_STEP) != null && Boolean.parseBoolean(context.getProperty
                (BACK_TO_FIRST_STEP).toString());
    }

    private boolean handleNode(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
                               SequenceConfig sequenceConfig, AuthGraphNode currentNode) throws FrameworkException {

        context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, currentNode);
        boolean isInterrupt = false;
        if (currentNode instanceof ShowPromptNode) {
            isInterrupt = handlePrompt(request, response, context, sequenceConfig, (ShowPromptNode) currentNode);
        } else if (currentNode instanceof LongWaitNode) {
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

    private void displayLongWait(AuthenticationContext context, HttpServletRequest request, HttpServletResponse
            response) throws FrameworkException {

        try {
            String longWaitUrl = ConfigurationFacade.getInstance().getAuthenticationEndpointWaitURL();
            response.sendRedirect(longWaitUrl + "?" + FrameworkConstants.SESSION_DATA_KEY + "=" + context
                    .getContextIdentifier());
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
        } catch (IOException e) {
            throw new FrameworkException("Error while redirecting to wait.do", e);
        }
    }

    /**
     * Handles the prompt. Make redirect to prompt handler (authentication endpoint) URL when this is initial prompt
     * state. Executes the respective event handler function when prompt is returning with user input.
     *
     * @param request Http servlet request
     * @param response Http servlet response
     * @param context Authentication context
     * @param sequenceConfig Authentication sequence config
     * @param promptNode Show prompt node
     * @return true if the execution needs to be stopped and show somethin to the user.
     */
    private boolean handlePrompt(HttpServletRequest request, HttpServletResponse response,
                                 AuthenticationContext context, SequenceConfig sequenceConfig,
                                 ShowPromptNode promptNode) throws FrameworkException {

        boolean isPromptToBeDisplayed = false;
        if (context.isReturning()) {
            String action = PROMPT_DEFAULT_ACTION;
            for (String s : request.getParameterMap().keySet()) {
                if (s.startsWith(PROMPT_ACTION_PREFIX)) {
                    action = s.substring(PROMPT_ACTION_PREFIX.length(), s.length());
                    action = StringUtils.capitalize(action);
                    break;
                }
            }
            action = "on" + action;
            executeFunction(action, promptNode, context);
            AuthGraphNode nextNode = promptNode.getDefaultEdge();
            context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
            context.setReturning(false);
        } else {
            if (promptNode.getHandlerMap().get(ShowPromptNode.preHandler) != null) {
                Object result = evaluateHandler(ShowPromptNode.preHandler, promptNode, context, promptNode
                        .getParameters().get(STEP_IDENTIFIER_PARAM));
                if (Boolean.TRUE.equals(result)) {
                    executeFunction(SKIPPED_CALLBACK_NAME, promptNode, context);
                    AuthGraphNode nextNode = promptNode.getDefaultEdge();
                    context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
                } else {
                    displayPrompt(context, request, response, promptNode);
                    isPromptToBeDisplayed = true;
                }
                return isPromptToBeDisplayed;
            }
            displayPrompt(context, request, response, promptNode);
            isPromptToBeDisplayed = true;
        }
        return isPromptToBeDisplayed;
    }

    private void displayPrompt(AuthenticationContext context, HttpServletRequest request, HttpServletResponse response,
                               ShowPromptNode promptNode) throws FrameworkException {

        try {

            String promptPage = ConfigurationFacade.getInstance().getAuthenticationEndpointPromptURL();
            String tenantDomainQueryString = null;
            if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                tenantDomainQueryString = "tenantDomain=" + context.getTenantDomain();
                promptPage = FrameworkUtils.appendQueryParamsStringToUrl(promptPage, tenantDomainQueryString);
            }
            String redirectUrl = FrameworkUtils.appendQueryParamsStringToUrl(promptPage, "templateId=" +
                    URLEncoder.encode(promptNode.getTemplateId(), StandardCharsets.UTF_8.name()) + "&promptId=" +
                    context.getContextIdentifier());
            if (promptNode.getData() != null) {
                context.addEndpointParams(promptNode.getData());
            }

                response.sendRedirect(redirectUrl);
            AuthenticationResult authenticationResult = new AuthenticationResult();
            request.setAttribute(FrameworkConstants.RequestAttribute.AUTH_RESULT, authenticationResult);
            request.setAttribute(RESPONSE_HANDLED_BY_FRAMEWORK, Boolean.TRUE);
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
        } catch (UnsupportedEncodingException e) {
            throw new FrameworkException("Error while encoding the data to send to prompt page with session data key"
                    + context.getContextIdentifier(), e);
        } catch (IOException e) {
            throw new FrameworkException("Error while redirecting the user for prompt page with session data key"
                    + context.getContextIdentifier(), e);
        }
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
     * @param node Fail Node
     * @throws FrameworkException
     */
    private void handleAuthFail(HttpServletRequest request, HttpServletResponse response, AuthenticationContext context,
                                SequenceConfig sequenceConfig, FailNode node) throws FrameworkException {

        if (log.isDebugEnabled()) {
            log.debug("Found a Fail Node in conditional authentication");
        }

        if (node.isShowErrorPage()) {
            // Set parameters specific to sendError function to context if isShowErrorPage  is true
            String errorPage = node.getErrorPageUri();
            String redirectURL = null;
            if (StringUtils.isBlank(errorPage)) {
                errorPage = ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL();
            }
            try {
                URIBuilder uriBuilder = new URIBuilder(errorPage);
                node.getFailureData().forEach(uriBuilder::addParameter);
                redirectURL = uriBuilder.toString();
                response.sendRedirect(FrameworkUtils.getRedirectURL(redirectURL, request));
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
        } else {
            // If isShowErrorPage is false, set parameters specific to fail function to context.
            setErrorPropertiesToContext(node, context);
        }
    }

    /**
     * Sets error properties to Authentication context and fail authentication.
     */
    private void setErrorPropertiesToContext(FailNode node, AuthenticationContext context) throws FrameworkException {

        Map<String, String> parameterMap = node.getFailureData();

        // If an error code is provided, set it to the context.
        if (parameterMap.containsKey(FrameworkConstants.ERROR_CODE)) {
            context.setProperty(FrameworkConstants.AUTH_ERROR_CODE, parameterMap.get(FrameworkConstants.ERROR_CODE));
        }

        // If an error description is provided, set it to the context.
        if (parameterMap.containsKey(FrameworkConstants.ERROR_MESSAGE)) {
            context.setProperty(FrameworkConstants.AUTH_ERROR_MSG, parameterMap.get(FrameworkConstants.ERROR_MESSAGE));
        }

        // If an error URL is provided, validate is before proceeding.
        if (parameterMap.containsKey(FrameworkConstants.ERROR_URI)) {
            try {
                new URL(parameterMap.get(FrameworkConstants.ERROR_URI));
            } catch (MalformedURLException e) {
                throw new FrameworkException("Error when validating provided errorURI: "
                        + parameterMap.get(FrameworkConstants.ERROR_URI), e);
            }
            // Set the error URL to the context.
            context.setProperty(FrameworkConstants.AUTH_ERROR_URI, parameterMap.get(FrameworkConstants.ERROR_URI));
        }
        context.setRequestAuthenticated(false);
        context.getSequenceConfig().setCompleted(true);
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

        AuthenticatorFlowStatus flowStatus = (AuthenticatorFlowStatus) request
                .getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);

        int stepNumber = context.getCurrentStep();
        if (!context.isReturning()) {
            if (stepNumber <= 0) {
                stepNumber = 1;
            } else if (flowStatus != FAIL_COMPLETED) {
                stepNumber++;
            }
            context.setCurrentStep(stepNumber);
            context.getSequenceConfig().getStepMap().put(stepNumber, stepConfig);
        }


        FrameworkUtils.getStepHandler().handle(request, response, context);

        flowStatus = (AuthenticatorFlowStatus) request
                .getAttribute(FrameworkConstants.RequestParams.FLOW_STATUS);

        if (flowStatus != SUCCESS_COMPLETED && flowStatus != INCOMPLETE && !(FAIL_COMPLETED.equals(flowStatus) &&
                context.isRetrying())) {
            stepConfig.setSubjectAttributeStep(false);
            stepConfig.setSubjectIdentifierStep(false);
        }

        if (flowStatus == FAIL_COMPLETED) {
            if (!(stepConfigGraphNode.getNext() instanceof DynamicDecisionNode)) {
                if (context.isRetrying()) {
                    StepConfigGraphNode newNextNode = new StepConfigGraphNode(stepConfigGraphNode.getStepConfig());
                    newNextNode.setNext(stepConfigGraphNode.getNext());

                    AuthGraphNode parentNode = stepConfigGraphNode.getParent();
                    if (parentNode == null) {
                        parentNode = sequenceConfig.getAuthenticationGraph().getStartNode();
                    }
                    newNextNode.setParent(parentNode);
                    if (parentNode instanceof DynamicDecisionNode) {
                        ((DynamicDecisionNode) parentNode).setDefaultEdge(newNextNode);
                    } else if (parentNode instanceof StepConfigGraphNode) {
                        ((StepConfigGraphNode) parentNode).setNext(newNextNode);
                    }
                    stepConfigGraphNode.setNext(newNextNode);
                } else {
                    stepConfigGraphNode.setNext(new FailNode());
                }
            }
        }
        // if step is not completed, that means step wants to redirect to outside
        if (!stepConfig.isCompleted()) {
            if (log.isDebugEnabled()) {
                log.debug("Step is not complete yet. Redirecting to outside.");
            }
            return true;
        }
        if (context.isPassiveAuthenticate() && !context.isRequestAuthenticated()) {
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
        LongWaitStatus longWaitStatus = longWaitStatusStoreService.getWait(context.getContextIdentifier());
        if (longWaitStatus == null || longWaitStatus.getStatus() == LongWaitStatus.Status.UNKNOWN) {
            //This is a initiation of long wait
            longWaitStatus = new LongWaitStatus();
            int tenantId = IdentityTenantUtil.getTenantId(context.getTenantDomain());
            longWaitStatusStoreService.addWait(tenantId, context.getContextIdentifier(), longWaitStatus);
            isWaiting = callExternalSystem(request, response, context, sequenceConfig, longWaitNode);
            if (promptOnLongWait()) {
                if (isWaiting) {
                    displayLongWait(context, request, response);
                }
            }
        } else {
            context.setReturning(false);
            // This is a continuation of long wait
            isWaiting = LongWaitStatus.Status.COMPLETED != longWaitStatus.getStatus();
            longWaitStatusStoreService.removeWait(context.getContextIdentifier());
            String outcomeName = (String) context.getProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS);
            Map<String, Object> data = (Map<String, Object>) context.getProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA);
            context.removeProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS);
            context.removeProperty(FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA);
            AuthGraphNode nextNode;
            if (outcomeName != null) {
                executeFunction(outcomeName, longWaitNode, context, data);
                nextNode = longWaitNode.getDefaultEdge();
                if (nextNode == null) {
                    log.error("Authentication script does not have applicable event handler for outcome "
                            + outcomeName + " from the long wait process : " + context.getContextIdentifier()
                            + ". So ending the authentication flow. Add the correspoding event handler to the script");
                    nextNode = new FailNode();
                }
            } else {
                log.error("The outcome from the long wait process " + context.getContextIdentifier()
                        + " is null. Because asyncReturn.accept() has not been used properly in the async process flow"
                        + " of the custom function. So ending the authentication flow. Check the flow in the async"
                        + " process flow of the custom function and add asyncReturn.accept() with the corresponding"
                        + " outcome.");
                nextNode = new FailNode();
            }
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

        AsyncReturn asyncReturn = rethrowTriConsumer((authenticationContext, data, result) -> {
            authenticationContext.setProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_STATUS, result);
            authenticationContext.setProperty(
                    FrameworkConstants.JSAttributes.JS_CALL_AND_WAIT_DATA, data);

            if (!promptOnLongWait()) {
                synchronized (context) {
                    context.notify();
                }
            }
        });

        if (caller != null) {
            FrameworkServiceDataHolder.getInstance().getAsyncSequenceExecutor().exec(caller, asyncReturn, context);
            if (!promptOnLongWait()) {
                int waitTimeout = getLongWaitTimeout();
                synchronized (context) {
                    try {
                        context.wait(waitTimeout);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted while waiting for the external call to complete for session " +
                                "data key: " + context.getContextIdentifier() + ". ", e);
                    }
                }
                resumeLongWait(request, response, context);
            }
            return true;
        }
        return false;
    }

    private int getLongWaitTimeout() {

        String waitTimeoutPropValue = IdentityUtil.getProperty(ADAPTIVE_AUTH_LONG_WAIT_TIMEOUT);
        int waitTimeout = 10000;
        try {
            if (waitTimeoutPropValue != null) {
                waitTimeout = Integer.parseInt(waitTimeoutPropValue);
            }
        } catch (NumberFormatException e) {
            //ignore. Default value will be used.
            log.warn("Error while reading the wait timeout. Default value of 10 seconds will be used. ", e);
        }
        return waitTimeout;
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
                    if (dynamicDecisionNode.getFunctionMap().get("onFail") != null) {
                        executeFunction("onFail", dynamicDecisionNode, context);
                    } else {
                        if (context.isRetrying()) {
                            AuthGraphNode nextNode = dynamicDecisionNode.getParent();
                            context.setProperty(FrameworkConstants.JSAttributes.PROP_CURRENT_NODE, nextNode);
                            return;
                        }
                    }
                    if (dynamicDecisionNode.getDefaultEdge() instanceof EndStep) {
                        dynamicDecisionNode.setDefaultEdge(new FailNode());
                    }
                    break;
                case FALLBACK:
                    executeFunction("onFallback", dynamicDecisionNode, context);
                    break;
                case USER_ABORT:
                    executeFunction("onUserAbort", dynamicDecisionNode, context);
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
        jsBasedEvaluator.evaluate(context, (jsConsumer) -> jsConsumer.call(null, new JsAuthenticationContext(context)));
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
        jsBasedEvaluator.evaluate(context,
                (jsConsumer) -> jsConsumer.call(null, new JsAuthenticationContext(context), new JsWritableParameters(data)));
        if (dynamicDecisionNode.getDefaultEdge() == null) {
            dynamicDecisionNode.setDefaultEdge(new EndStep());
        }
    }

    private Object evaluateHandler(String outcomeName, ShowPromptNode dynamicDecisionNode,
                                   AuthenticationContext context, Object stepId) {

        SerializableJsFunction fn = dynamicDecisionNode.getHandlerMap().get(outcomeName);
        FrameworkServiceDataHolder dataHolder = FrameworkServiceDataHolder.getInstance();
        JsGraphBuilderFactory jsGraphBuilderFactory = dataHolder.getJsGraphBuilderFactory();
        JsGraphBuilder graphBuilder = jsGraphBuilderFactory.createBuilder(context, context
                .getSequenceConfig().getAuthenticationGraph().getStepMap(), dynamicDecisionNode);
        JsGraphBuilder.JsBasedEvaluator jsBasedEvaluator = graphBuilder.new JsBasedEvaluator(fn);
        return jsBasedEvaluator.evaluate(context,
                (jsFunction) -> jsFunction.call(null, stepId, new JsAuthenticationContext(context)));
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

    private boolean isFirstStep(AuthGraphNode authGraphNode) {

        if (authGraphNode instanceof DynamicDecisionNode) {
            return false;
        } else if (authGraphNode instanceof StepConfigGraphNode) {
            StepConfig stepConfig = ((StepConfigGraphNode) authGraphNode).getStepConfig();
            if (stepConfig.getOrder() == 1) {
                return true;
            }
        }
        return false;
    }
}
