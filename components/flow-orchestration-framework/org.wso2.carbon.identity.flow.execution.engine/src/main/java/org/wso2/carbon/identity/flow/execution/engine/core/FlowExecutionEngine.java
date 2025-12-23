/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.flow.execution.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.flow.execution.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionStep;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import java.util.Map;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.ERROR;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_REDIRECTION_URL_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_REQUIRED_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.ErrorMessages.ERROR_CODE_WEBAUTHN_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.WEBAUTHN_DATA;
import static org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils.handleServerException;
import static org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;

/**
 * Engine to execute the  flow.
 */
public class FlowExecutionEngine {

    private static final Log LOG = LogFactory.getLog(FlowExecutionEngine.class);

    private static final FlowExecutionEngine instance = new FlowExecutionEngine();

    private FlowExecutionEngine() {

    }

    public static FlowExecutionEngine getInstance() {

        return instance;
    }

    /**
     * Execute the  flow sequence.
     *
     * @param context Flow context.
     * @return Node response.
     * @throws FlowEngineException If an error occurs while executing the  flow sequence.
     */
    public FlowExecutionStep execute(FlowExecutionContext context)
            throws FlowEngineException {

        GraphConfig graph = context.getGraphConfig();
        String tenantDomain = context.getTenantDomain();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting the " + context.getFlowType() + " flow for the tenant: " + tenantDomain);
        }
        if (graph.getFirstNodeId() == null) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_FIRST_NODE_NOT_FOUND, context.getFlowType(),
                    graph.getId(), tenantDomain);
        }

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                    " flow sequence.");
            currentNode = graph.getNodeConfigs().get(graph.getFirstNodeId());
            context.setCurrentNode(currentNode);
        }

        while (currentNode != null) {
            NodeResponse nodeResponse = triggerNode(currentNode, context);
            context.setCurrentNodeResponse(nodeResponse);
            if (STATUS_COMPLETE.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                continue;
            }
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    REDIRECTION.equals(nodeResponse.getType())) {
                return resolveStepForRedirection(context, nodeResponse);
            }

            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    WEBAUTHN.equals(nodeResponse.getType())) {
                return resolveStepForWebAuthn(context, nodeResponse);
            }

            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    INTERNAL_PROMPT.equals(nodeResponse.getType())) {
                return resolveStepForInternalPrompt(context, nodeResponse);
            }

            FlowExecutionStep step = resolveStepForPrompt(graph, currentNode, context, nodeResponse);

            // If the flow status is complete because the END node was reached, return the step.
            if (STATUS_COMPLETE.equals(step.getFlowStatus())) {
                return step;
            }

            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) && VIEW.equals(nodeResponse.getType())) {
                return step;
            }
            if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                return step;
            }
        }

        // If there are no more nodes to process, mark the flow as complete.
        NodeConfig endNode = graph.getNodeConfigs().get(END_NODE_ID);
        if (endNode == null) {
            return new FlowExecutionStep.Builder()
                    .flowId(context.getContextIdentifier())
                    .flowStatus(STATUS_COMPLETE)
                    .stepType(REDIRECTION)
                    .data(new DataDTO.Builder()
                            .url(FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context))
                            .build())
                    .build();
        }
        return resolveStepForPrompt(graph, context.getGraphConfig().getNodeConfigs().get(END_NODE_ID),
                context, context.getCurrentNodeResponse());
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(GraphConfig graphConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = graphConfig.getNodeConfigs().get(nextNodeId);
        if (nextNode != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Current node " + currentNode.getId() + " is completed. "
                        + "Moving to the next node: " + nextNodeId
                        + " and setting " + currentNode.getId() + " as the previous node.");
            }
            nextNode.setPreviousNodeId(currentNode.getId());
        }
        if (Constants.NodeTypes.DECISION.equals(currentNode.getType())) {
            // If the current node is a decision node, reset the next node ID to null.
            if (LOG.isDebugEnabled()) {
                LOG.debug("Current node " + currentNode.getId() + " is a decision node. " +
                        "Resetting the next node ID to null.");
            }
            currentNode.setNextNodeId(null);
        }
        return nextNode;
    }

    /**
     * Trigger the node.
     *
     * @param nodeConfig Node configuration.
     * @param context    Flow context.
     * @return Node response.
     * @throws FlowEngineException If an error occurs while triggering the node.
     */
    private NodeResponse triggerNode(NodeConfig nodeConfig, FlowExecutionContext context)
            throws FlowEngineException {

        switch (nodeConfig.getType()) {
            case Constants.NodeTypes.DECISION:
                return new UserChoiceDecisionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.TASK_EXECUTION:
                return new TaskExecutionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.PROMPT_ONLY:
                return new PagePromptNode().execute(context, nodeConfig);
            default:
                throw handleServerException(context.getFlowType(), ERROR_CODE_UNSUPPORTED_NODE, nodeConfig.getType(),
                        context.getFlowType(),
                        context.getGraphConfig().getId(), context.getTenantDomain());
        }
    }

    private FlowExecutionStep resolveStepForPrompt(GraphConfig graph, NodeConfig currentNode,
                                                   FlowExecutionContext context, NodeResponse nodeResponse) throws FlowEngineServerException {

        DataDTO dataDTO = graph.getNodePageMappings().get(currentNode.getId()).getData();

        DataDTO finalDataDTO = null;
        if (dataDTO != null) {
            finalDataDTO = new DataDTO.Builder()
                    .components(dataDTO.getComponents())
                    .requiredParams(nodeResponse.getRequiredData())
                    .optionalParams(nodeResponse.getOptionalData())
                    .additionalData(nodeResponse.getAdditionalInfo())
                    .build();
            handleError(finalDataDTO, nodeResponse);
        }

        // When the END node is reached, mark the flow status as COMPLETE, set the step type to REDIRECTION,
        // and assign the redirect URL. Note: all END nodes are expected to be of type PROMPT_ONLY.
        if (END_NODE_ID.equals(currentNode.getId())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Flow: " + context.getContextIdentifier() + " has reached the explicitly defined " +
                        "end node. Changing the flow status to COMPLETE, step type to REDIRECTION and setting " +
                        "the redirect URL.");
            }
            if (finalDataDTO == null ) {
                finalDataDTO = new DataDTO();
            }
            finalDataDTO.setRedirectURL(FlowExecutionEngineUtils.resolveCompletionRedirectionUrl(context));
            return new FlowExecutionStep.Builder()
                    .flowId(context.getContextIdentifier())
                    .flowStatus(STATUS_COMPLETE)
                    .stepType(REDIRECTION)
                    .data(finalDataDTO)
                    .build();
        }

        return new FlowExecutionStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(VIEW)
                .data(finalDataDTO)
                .build();
    }

    private FlowExecutionStep resolveStepForRedirection(FlowExecutionContext context, NodeResponse nodeResponse)
            throws FlowEngineServerException {

        if (nodeResponse.getAdditionalInfo() == null || nodeResponse.getAdditionalInfo().isEmpty() ||
                !nodeResponse.getAdditionalInfo().containsKey(REDIRECT_URL)) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_REDIRECTION_URL_NOT_FOUND);
        }
        String redirectUrl = nodeResponse.getAdditionalInfo().get(REDIRECT_URL);
        nodeResponse.getAdditionalInfo().remove(REDIRECT_URL);
        return new FlowExecutionStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(redirectUrl)
                        .additionalData(nodeResponse.getAdditionalInfo())
                        .requiredParams(nodeResponse.getRequiredData())
                        .optionalParams(nodeResponse.getOptionalData())
                        .build())
                .build();
    }

    private FlowExecutionStep resolveStepForWebAuthn(FlowExecutionContext context, NodeResponse nodeResponse)
            throws FlowEngineServerException {

        if (nodeResponse.getAdditionalInfo() == null || nodeResponse.getAdditionalInfo().isEmpty() ||
                !nodeResponse.getAdditionalInfo().containsKey(WEBAUTHN_DATA)) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_WEBAUTHN_DATA_NOT_FOUND);
        }

        Map<String, Object> webAuthnData = FlowExecutionEngineUtils.getMapFromJSONString(nodeResponse
                .getAdditionalInfo().get(WEBAUTHN_DATA));

        return new FlowExecutionStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(WEBAUTHN)
                .data(new DataDTO.Builder()
                        .webAuthnData(webAuthnData)
                        .requiredParams(nodeResponse.getRequiredData())
                        .optionalParams(nodeResponse.getOptionalData())
                        .build())
                .build();
    }

    private FlowExecutionStep resolveStepForInternalPrompt(FlowExecutionContext context, NodeResponse nodeResponse)
            throws FlowEngineServerException {

        if (nodeResponse.getRequiredData() == null || nodeResponse.getRequiredData().isEmpty()) {
            throw handleServerException(context.getFlowType(), ERROR_CODE_REQUIRED_DATA_NOT_FOUND);
        }
        return new FlowExecutionStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(INTERNAL_PROMPT)
                .data(new DataDTO.Builder()
                        .additionalData(nodeResponse.getAdditionalInfo())
                        .requiredParams(nodeResponse.getRequiredData())
                        .optionalParams(nodeResponse.getOptionalData())
                        .build())
                .build();
    }

    private void handleError(DataDTO dataDTO, NodeResponse nodeResponse) {

        if (StringUtils.isNotBlank(nodeResponse.getError())) {
            dataDTO.addAdditionalData(ERROR, nodeResponse.getError());
        }
    }
}
