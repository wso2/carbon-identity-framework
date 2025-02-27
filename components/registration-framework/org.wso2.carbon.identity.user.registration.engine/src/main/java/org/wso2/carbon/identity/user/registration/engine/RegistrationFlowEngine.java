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

package org.wso2.carbon.identity.user.registration.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationStep;
import org.wso2.carbon.identity.user.registration.engine.graph.PagePromptNode;
import org.wso2.carbon.identity.user.registration.engine.graph.TaskExecutionNode;
import org.wso2.carbon.identity.user.registration.engine.graph.UserChoiceDecisionNode;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.DataDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_FIRST_NODE_NODE_FOUND;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_NODE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_PROMPT_ONLY;
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
     * @throws RegistrationFrameworkException If an error occurs while executing the registration sequence.
     */
    public RegistrationStep execute(RegistrationContext context)
            throws RegistrationFrameworkException {

        RegistrationGraphConfig graph = context.getRegGraph();

        String tenantDomain = context.getTenantDomain();
        if (graph.getFirstNodeId() == null) {
            throw handleServerException(ERROR_CODE_FIRST_NODE_NODE_FOUND, graph.getId(), tenantDomain);
        }

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                              "registration sequence.");
            currentNode = graph.getNodeConfigs().get(graph.getFirstNodeId());
        }

        while (currentNode != null) {

            Response nodeResponse = triggerNode(currentNode, context);

            if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                currentNode = moveToNextNode(graph, currentNode);
                return resolveStepDetailsForPrompt(graph, currentNode, context);
            } else if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) && VIEW.equals(nodeResponse.getType())) {
                return resolveStepDetailsForPrompt(graph, currentNode, context);
            } else if (STATUS_INCOMPLETE.equals(nodeResponse.getStatus()) &&
                    REDIRECTION.equals(nodeResponse.getType())) {
                return resolveStepDetailsForRedirection(context, nodeResponse);
            } else {
                currentNode = moveToNextNode(graph, currentNode);
            }
        }
        return new RegistrationStep.Builder().flowStatus(STATUS_COMPLETE).build();
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
     * @throws RegistrationFrameworkException If an error occurs while triggering the node.
     */
    private Response triggerNode(NodeConfig nodeConfig, RegistrationContext context)
            throws RegistrationFrameworkException {

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
                                                         RegistrationContext context) {

        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(VIEW)
                .data(graph.getNodePageMappings().get(currentNode.getId()).getData())
                .build();
        // TODO: Implement the logic to validate the inputs in the page and required data by the executors.
    }

    private RegistrationStep resolveStepDetailsForRedirection(RegistrationContext context, Response response) {

        // TODO: Implement logic to add required inputs after redirection.
        return new RegistrationStep.Builder()
                .flowId(context.getContextIdentifier())
                .flowStatus(STATUS_INCOMPLETE)
                .stepType(REDIRECTION)
                .data(new DataDTO.Builder()
                              .url(response.getAdditionalInfo().get("url"))
                              .build())
                .build();
    }
}
