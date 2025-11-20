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

package org.wso2.carbon.identity.flow.mgt.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.mgt.Constants;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtClientException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.ActionDTO;
import org.wso2.carbon.identity.flow.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.flow.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;
import org.wso2.carbon.identity.flow.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.wso2.carbon.identity.flow.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.flow.mgt.Constants.ActionTypes.NEXT;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.flow.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.flow.mgt.Constants.END_NODE_ID;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_ACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_COMPONENT_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_INFO_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_FOR_BUTTON;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_FIRST_NODE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_MULTIPLE_STEP_EXECUTORS;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_NEXT_ACTION_NOT_FOUND;
import static org.wso2.carbon.identity.flow.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ACTION_TYPE;
import static org.wso2.carbon.identity.flow.mgt.Constants.ExecutorTypes.USER_ONBOARDING;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.END;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.EXECUTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.USER_ONBOARD;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.WEBAUTHN;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleClientException;
import static org.wso2.carbon.identity.flow.mgt.utils.FlowMgtUtils.handleServerException;

/**
 * This class is responsible for building the flow graph.
 */
public class GraphBuilder {

    private static final Log LOG = LogFactory.getLog(GraphBuilder.class);

    private final GraphConfig graphConfig;
    private final Map<String, NodeConfig> nodeMap;
    private final Map<String, StepDTO> stepContentMap;
    private final List<NodeEdge> nodeEdges;

    public GraphBuilder() {

        this.graphConfig = new GraphConfig();
        this.graphConfig.setId(UUID.randomUUID().toString());
        this.nodeMap = new HashMap<>();
        this.stepContentMap = new HashMap<>();
        this.nodeEdges = new ArrayList<>();
    }

    /**
     * Processes and adds steps to the flow graph.
     *
     * @param steps The list of {@link StepDTO} objects representing steps in the flow.
     * @return The current instance of {@link GraphBuilder} for method chaining.
     * @throws FlowMgtFrameworkException If an error occurs while processing the steps.
     */
    public GraphBuilder withSteps(List<StepDTO> steps) throws FlowMgtFrameworkException {

        for (StepDTO step : steps) {
            switch (step.getType()) {
                case VIEW:
                    processViewStep(step);
                    break;
                case REDIRECTION:
                case WEBAUTHN:
                case EXECUTION:
                    processExecutionStep(step);
                    break;
                case USER_ONBOARD:
                    processUserOnboardStep(step);
                    break;
                case END:
                    // Handle the explicitly defined end step.
                    processEndStep(step);
                    break;
                default:
                    throw handleClientException(Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_STEP_TYPE,
                            step.getType());
            }
            stepContentMap.put(step.getId(), step);
        }
        return this;
    }

    /**
     * Builds and returns the final {@link GraphConfig}.
     * The method processes node mappings and determines the first node in the flow.
     *
     * @return The built {@link GraphConfig}.
     * @throws FlowMgtFrameworkException If an error occurs during graph configuration.
     */
    public GraphConfig build() throws FlowMgtFrameworkException {

        resolveGraphEdgesAndFirstNode();
        graphConfig.setNodeConfigs(nodeMap);
        graphConfig.setNodePageMappings(stepContentMap);
        return graphConfig;
    }

    private void processExecutionStep(StepDTO step) throws FlowMgtClientException {

        if (step.getData() == null) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND, step.getId());
        }
        ActionDTO action = step.getData().getAction();
        if (action == null) {
            throw handleClientException(ERROR_CODE_ACTION_DATA_NOT_FOUND, step.getId(), step.getType());
        }
        if (!EXECUTOR.equals(action.getType())) {
            throw handleClientException(ERROR_CODE_INVALID_ACTION_TYPE, action.getType(), step.getId(), step.getType());
        }
        if (action.getExecutor() == null) {
            throw handleClientException(ERROR_CODE_EXECUTOR_INFO_NOT_FOUND, step.getId());
        }

        NodeConfig executionNodeConfig = createTaskExecutionNode(step.getId(), action.getExecutor());
        nodeMap.put(executionNodeConfig.getId(), executionNodeConfig);
        nodeEdges.add(new NodeEdge(executionNodeConfig.getId(), action.getNextId(), null));
    }

    private void processUserOnboardStep(StepDTO step) {

        NodeConfig userOnboardNode = createUserOnboardingNode(step.getId());
        nodeMap.put(userOnboardNode.getId(), userOnboardNode);
        // UserOnboard node is considered as the last node in the flow, hence not adding an edge to the next node.
    }

    private void processViewStep(StepDTO step)
            throws FlowMgtFrameworkException {

        if (step.getData() == null) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND, step.getId());
        }
        List<ComponentDTO> components = step.getData().getComponents();
        if (components == null || components.isEmpty()) {
            throw handleClientException(ERROR_CODE_COMPONENT_DATA_NOT_FOUND, step.getId());
        }
        List<NodeConfig> stepNodes = new ArrayList<>();
        for (ComponentDTO component : components) {
            processComponent(component, stepNodes, step.getId());
        }
        handleTempNodesInStep(stepNodes, step);
    }

    private void processComponent(ComponentDTO component, List<NodeConfig> stepNodes, String stepId)
            throws FlowMgtFrameworkException {

        if (FORM.equals(component.getType())) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing form component: " + component.getId());
            }
            for (ComponentDTO subComponent : component.getComponents()) {
                processComponent(subComponent, stepNodes, stepId);
            }
        } else if (BUTTON.equals(component.getType())) {
            validateStepActions(component.getAction(), stepNodes, component.getId(), stepId);
            stepNodes.add(createNodeFromAction(component.getAction(), component.getId()));
        }
    }

    private void validateStepActions(ActionDTO action, List<NodeConfig> stepNodes, String id, String stepId)
            throws FlowMgtFrameworkException {

        if (action == null) {
            throw handleClientException(ERROR_CODE_INVALID_ACTION_FOR_BUTTON, id);
        }
        if (action.getNextId() == null) {
            throw handleClientException(ERROR_CODE_NEXT_ACTION_NOT_FOUND, id);
        }
        if (EXECUTOR.equals(action.getType())) {
            if (action.getExecutor() == null) {
                throw handleClientException(ERROR_CODE_EXECUTOR_INFO_NOT_FOUND, id);
            }
            if (stepNodes.stream().anyMatch(nodeConfig -> (TASK_EXECUTION.equals(nodeConfig.getType())))) {
                throw handleClientException(ERROR_CODE_MULTIPLE_STEP_EXECUTORS, stepId);
            }
        }
    }

    private NodeConfig createNodeFromAction(ActionDTO action, String componentId)
            throws FlowMgtClientException {

        NodeConfig tempNodeInComponent;
        if (NEXT.equals(action.getType())) {
            tempNodeInComponent = createPagePromptNode(componentId);
        } else if (EXECUTOR.equals(action.getType())) {
            tempNodeInComponent = createTaskExecutionNode(componentId, action.getExecutor());
        } else {
            throw handleClientException(ERROR_CODE_UNSUPPORTED_ACTION_TYPE, action.getType(), componentId);
        }
        tempNodeInComponent.setNextNodeId(action.getNextId());
        return tempNodeInComponent;
    }

    private void handleTempNodesInStep(List<NodeConfig> tempNodesInStep, StepDTO step) {

        NodeConfig stepNode = null;
        if (tempNodesInStep.size() > 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Multiple nodes are derived from the step: " + step.getId() + ". Creating a decision node.");
            }
            NodeConfig decisionNode = createDecisionNode(step.getId());
            for (NodeConfig nodeConfig : tempNodesInStep) {
                if (TASK_EXECUTION.equals(nodeConfig.getType())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("A node with an execution found in the step. Therefore adding it to the node list " +
                                "with id, " + nodeConfig.getId());
                    }
                    this.nodeMap.put(nodeConfig.getId(), nodeConfig);
                    this.nodeEdges.add(new NodeEdge(nodeConfig.getId(), nodeConfig.getNextNodeId(), null));
                    this.nodeEdges.add(new NodeEdge(decisionNode.getId(), nodeConfig.getId(), nodeConfig.getId()));
                    nodeConfig.setNextNodeId(null);
                } else {
                    // Edge from decision node to the next node derived from NEXT actions.
                    this.nodeEdges.add(
                            new NodeEdge(decisionNode.getId(), nodeConfig.getNextNodeId(), nodeConfig.getId()));
                }
            }
            stepNode = decisionNode;
        } else if (tempNodesInStep.size() == 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Only one node derived from the step: " + step.getId() + ". Adding it to the node list.");
            }

            NodeConfig tempNode = tempNodesInStep.get(0);

            stepNode = new NodeConfig.Builder()
                    .id(step.getId())
                    .type(tempNode.getType())
                    .executorConfig(tempNode.getExecutorConfig())
                    .build();
            if (!END_NODE_ID.equalsIgnoreCase(step.getId())) {
                // If the step is not an end step, add an edge to the next node.
                this.nodeEdges.add(new NodeEdge(stepNode.getId(), tempNode.getNextNodeId(), tempNode.getId()));
            }
        }
        nodeMap.put(step.getId(), stepNode);
    }

    private void resolveGraphEdgesAndFirstNode() throws FlowMgtFrameworkException {

        Set<String> referencedNodes = new HashSet<>();
        for (NodeEdge edge : nodeEdges) {
            String targetNodeId = edge.getTargetNodeId();
            referencedNodes.add(targetNodeId);
            if (!nodeMap.containsKey(edge.getSourceNodeId())) {
                throw handleServerException(Constants.ErrorMessages.ERROR_CODE_INVALID_NODE, edge.getSourceNodeId());
            }
            if (StringUtils.isNotBlank(targetNodeId)) {
                if (END_NODE_ID.equalsIgnoreCase(targetNodeId)) {
                    if (!nodeMap.containsKey(targetNodeId)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Edge with target node %s found for source node: %s. "
                                    + "This is considered the last node in the flow.", END_NODE_ID, edge.getSourceNodeId()));
                        }
                        continue;
                    }
                }

                if (!nodeMap.containsKey(targetNodeId)) {
                    throw handleClientException(Constants.ErrorMessages.ERROR_CODE_INVALID_NEXT_STEP, targetNodeId);
                }
            }
            nodeMap.get(edge.getSourceNodeId()).addEdge(edge);
        }

        // Identify the first node: The one NOT in `referencedNodes`.
        List<String> firstNodeIds = new ArrayList<>();
        for (Map.Entry<String, NodeConfig> entry : nodeMap.entrySet()) {
            if (!referencedNodes.contains(entry.getKey())) {
                firstNodeIds.add(entry.getKey());
            }
        }
        if (firstNodeIds.size() != 1) {
            throw handleServerException(ERROR_CODE_INVALID_FIRST_NODE);
        }
        graphConfig.setFirstNodeId(firstNodeIds.get(0));
        nodeMap.get(firstNodeIds.get(0)).setFirstNode(true);
    }

    private NodeConfig createTaskExecutionNode(String id, ExecutorDTO executorDTO) {

        NodeConfig node = new NodeConfig.Builder()
                .id(id)
                .type(TASK_EXECUTION)
                .executorConfig(executorDTO)
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a task execution node " + id + " with executor " + executorDTO.getName() + ".");
        }
        return node;
    }

    private NodeConfig createDecisionNode(String id) {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(id)
                .type(DECISION)
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a decision node " + id + ".");
        }
        return nodeConfig;
    }

    private NodeConfig createUserOnboardingNode(String stepId) {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(stepId)
                .type(TASK_EXECUTION)
                .executorConfig(new ExecutorDTO(USER_ONBOARDING))
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a node with id " + nodeConfig.getId() + " for user onboarding.");
        }
        return nodeConfig;
    }

    private NodeConfig createPagePromptNode(String id) {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(id)
                .type(PROMPT_ONLY)
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a node with id " + nodeConfig.getId() + " to prompt a page.");
        }
        return nodeConfig;
    }

    private void processEndStep(StepDTO step) {

        ActionDTO action = step.getData().getAction();
        NodeConfig endNode;

        // If the action is not defined, consider it as a prompt only node.
        if (action == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No action defined for the end step. Therefore creating a prompt only node.");
            }
            endNode = createPagePromptNode(step.getId());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action defined for the end step. Therefore creating a task execution node.");
            }
            endNode = createTaskExecutionNode(step.getId(), action.getExecutor());
        }
        handleTempNodesInStep(Collections.singletonList(endNode), step);
    }
}
