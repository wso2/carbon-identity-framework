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

package org.wso2.carbon.identity.flow.engine.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.flow.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.flow.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.flow.engine.model.FlowContext;
import org.wso2.carbon.identity.flow.engine.model.FlowStep;
import org.wso2.carbon.identity.flow.engine.model.Response;
import org.wso2.carbon.identity.flow.engine.util.FlowEngineUtils;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.model.DataDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;

import static org.wso2.carbon.identity.flow.engine.Constants.ERROR;
import static org.wso2.carbon.identity.flow.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NOT_FOUND;
import static org.wso2.carbon.identity.flow.engine.Constants.ErrorMessages.ERROR_CODE_INTERACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.engine.Constants.ErrorMessages.ERROR_CODE_REDIRECTION_URL_NOT_FOUND;
import static org.wso2.carbon.identity.flow.engine.Constants.ErrorMessages.ERROR_CODE_REQUIRED_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.flow.engine.Constants.INTERACTION_DATA;
import static org.wso2.carbon.identity.flow.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.flow.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.engine.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.engine.util.FlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERACT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.INTERNAL_PROMPT;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

/**
 * Engine to execute the  flow.
 */
public class FlowEngine {

    private static final Log LOG = LogFactory.getLog(FlowEngine.class);

    private static final FlowEngine instance = new FlowEngine();

    private FlowEngine() {

    }

    public static FlowEngine getInstance() {

        return instance;
    }

    /**
     * Execute the  flow sequence.
     *
     * @param context Flow context.
     * @return Node response.
     * @throws FlowEngineException If an error occurs while executing the  flow sequence.
     */
    public FlowStep execute(FlowContext context)
            throws FlowEngineException {

        GraphConfig graph = context.getGraphConfig();

        String tenantDomain = context.getTenantDomain();
        if (graph.getFirstNodeId() == null) {
            throw handleServerException(ERROR_CODE_FIRST_NODE_NOT_FOUND, graph.getId(), tenantDomain);
        }

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                    " flow sequence.");
            currentNode = graph.getNodeConfigs().get(graph.getFirstNodeId());
            context.setCurrentNode(currentNode);
        }

        while (currentNode != null) {
            Response nodeResponse = triggerNode(currentNode, context);
            context.setCurrentNodeResponse(nodeResponse);
            if (STATUS_COMPLETE.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                continue;
            }
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    REDIRECTION.equals(nodeResponse.getType())) {
                return resolveStepDetailsForRedirection(context, nodeResponse);
            }

            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    INTERACT.equals(nodeResponse.getType())) {
                return resolveStepDetailsForInteraction(context, nodeResponse);
            }

            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    INTERNAL_PROMPT.equals(nodeResponse.getType())) {
                return resolveStepDetailsForProvide(context, nodeResponse);
            }

            FlowStep step = resolveStepDetailsForPrompt(graph, currentNode, context, nodeResponse);
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) && VIEW.equals(nodeResponse.getType())) {
                return step;
            }
            if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                return step;
            }
        }
        return new FlowStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_COMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(FlowEngineUtils.resolveCompletionRedirectionUrl(context))
                        .build())
                .build();
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
    private Response triggerNode(NodeConfig nodeConfig, FlowContext context)
            throws FlowEngineException {

        switch (nodeConfig.getType()) {
            case Constants.NodeTypes.DECISION:
                return new UserChoiceDecisionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.TASK_EXECUTION:
                return new TaskExecutionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.PROMPT_ONLY:
                return new PagePromptNode().execute(context, nodeConfig);
            default:
                throw handleServerException(ERROR_CODE_UNSUPPORTED_NODE, nodeConfig.getType(),
                        context.getGraphConfig().getId(), context.getTenantDomain());
        }
    }

    private FlowStep resolveStepDetailsForPrompt(GraphConfig graph, NodeConfig currentNode,
                                                 FlowContext context, Response response) {

        DataDTO dataDTO = graph.getNodePageMappings().get(currentNode.getId()).getData();
        handleError(dataDTO, response);
        return new FlowStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(VIEW)
                .data(dataDTO)
                .build();
    }

    private FlowStep resolveStepDetailsForRedirection(FlowContext context, Response response)
            throws FlowEngineServerException {

        if (response.getAdditionalInfo() == null || response.getAdditionalInfo().isEmpty() ||
                !response.getAdditionalInfo().containsKey(REDIRECT_URL)) {
            throw handleServerException(ERROR_CODE_REDIRECTION_URL_NOT_FOUND);
        }
        String redirectUrl = response.getAdditionalInfo().get(REDIRECT_URL);
        response.getAdditionalInfo().remove(REDIRECT_URL);
        return new FlowStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(redirectUrl)
                        .additionalData(response.getAdditionalInfo())
                        .requiredParams(response.getRequiredData())
                        .build())
                .build();
    }

    private FlowStep resolveStepDetailsForInteraction(FlowContext context, Response response)
            throws FlowEngineServerException {

        if (response.getAdditionalInfo() == null || response.getAdditionalInfo().isEmpty() ||
                !response.getAdditionalInfo().containsKey(INTERACTION_DATA)) {
            throw handleServerException(ERROR_CODE_INTERACTION_DATA_NOT_FOUND);
        }
        return new FlowStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(INTERACT)
                .data(new DataDTO.Builder()
                        .additionalData(response.getAdditionalInfo())
                        .requiredParams(response.getRequiredData())
                        .build())
                .build();
    }

    private FlowStep resolveStepDetailsForProvide(FlowContext context, Response response)
            throws FlowEngineServerException {

        if (response.getRequiredData() == null || response.getRequiredData().isEmpty()) {
            throw handleServerException(ERROR_CODE_REQUIRED_DATA_NOT_FOUND);
        }
        return new FlowStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(INTERNAL_PROMPT)
                .data(new DataDTO.Builder()
                        .additionalData(response.getAdditionalInfo())
                        .requiredParams(response.getRequiredData())
                        .build())
                .build();
    }

    private void handleError(DataDTO dataDTO, Response response) {

        if (StringUtils.isNotBlank(response.getError())) {
            dataDTO.addAdditionalData(ERROR, response.getError());
        }
    }
}
