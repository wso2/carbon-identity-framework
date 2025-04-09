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

package org.wso2.carbon.identity.user.registration.engine.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineServerException;
import org.wso2.carbon.identity.user.registration.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

import static org.wso2.carbon.identity.user.registration.engine.Constants.ERROR;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_REDIRECTION_URL_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.REDIRECT_URL;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleServerException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;

/**
 * Engine to execute the registration flow.
 */
public class RegistrationFlowEngine {

    private static final Log LOG = LogFactory.getLog(RegistrationFlowEngine.class);

    private static final RegistrationFlowEngine instance = new RegistrationFlowEngine();

    private RegistrationFlowEngine() {

    }

    public static RegistrationFlowEngine getInstance() {

        return instance;
    }

    /**
     * Execute the registration sequence.
     *
     * @param context Registration context.
     * @return Node response.
     * @throws RegistrationEngineException If an error occurs while executing the registration sequence.
     */
    public RegistrationStep execute(RegistrationContext context)
            throws RegistrationEngineException {

        RegistrationGraphConfig graph = context.getRegGraph();

        String tenantDomain = context.getTenantDomain();
        if (graph.getFirstNodeId() == null) {
            throw handleServerException(ERROR_CODE_FIRST_NODE_NOT_FOUND, graph.getId(), tenantDomain);
        }

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                    "registration sequence.");
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
            RegistrationStep step = resolveStepDetailsForPrompt(graph, currentNode, context, nodeResponse);
            if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) && VIEW.equals(nodeResponse.getType())) {
                return step;
            }
            if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                context.setCurrentNode(currentNode);
                return step;
            }
        }
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_COMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                        .url(RegistrationFlowEngineUtils.buildMyAccountAccessURL(context.getTenantDomain()))
                        .build())
                .build();
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(RegistrationGraphConfig regConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = regConfig.getNodeConfigs().get(nextNodeId);
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
     * @param context    Registration context.
     * @return Node response.
     * @throws RegistrationEngineException If an error occurs while triggering the node.
     */
    private Response triggerNode(NodeConfig nodeConfig, RegistrationContext context)
            throws RegistrationEngineException {

        switch (nodeConfig.getType()) {
            case Constants.NodeTypes.DECISION:
                return new UserChoiceDecisionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.TASK_EXECUTION:
                return new TaskExecutionNode().execute(context, nodeConfig);
            case Constants.NodeTypes.PROMPT_ONLY:
                return new PagePromptNode().execute(context, nodeConfig);
            default:
                throw handleServerException(ERROR_CODE_UNSUPPORTED_NODE, nodeConfig.getType(),
                        context.getRegGraph().getId(), context.getTenantDomain());
        }
    }

    private RegistrationStep resolveStepDetailsForPrompt(RegistrationGraphConfig graph, NodeConfig currentNode,
                                                         RegistrationContext context, Response response) {

        DataDTO dataDTO = graph.getNodePageMappings().get(currentNode.getId()).getData();
        handleError(dataDTO, response);
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(VIEW)
                .data(dataDTO)
                .build();
    }

    private RegistrationStep resolveStepDetailsForRedirection(RegistrationContext context, Response response)
            throws RegistrationEngineServerException {

        if (response.getAdditionalInfo() == null || response.getAdditionalInfo().isEmpty() ||
                !response.getAdditionalInfo().containsKey(REDIRECT_URL)) {
            throw handleServerException(ERROR_CODE_REDIRECTION_URL_NOT_FOUND);
        }
        String redirectUrl = response.getAdditionalInfo().get(REDIRECT_URL);
        response.getAdditionalInfo().remove(REDIRECT_URL);
        return new RegistrationStep.Builder()
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

    private void handleError(DataDTO dataDTO, Response response) {

        if (StringUtils.isNotBlank(response.getError())) {
            dataDTO.addAdditionalData(ERROR, response.getError());
        }
    }
}
