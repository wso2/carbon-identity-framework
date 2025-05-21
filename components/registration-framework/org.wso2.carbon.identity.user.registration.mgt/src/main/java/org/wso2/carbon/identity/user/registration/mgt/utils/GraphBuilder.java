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

package org.wso2.carbon.identity.user.registration.mgt.utils;

import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.NEXT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_ACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_COMPONENT_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_INFO_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_FIRST_NODE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_FOR_BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_MULTIPLE_STEP_EXECUTORS;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_NEXT_ACTION_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ExecutorTypes.USER_ONBOARDING;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.USER_ONBOARD;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils.handleServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

/**
 * This class is responsible for building the registration flow graph.
 */
public class GraphBuilder {

    private static final Log LOG = LogFactory.getLog(GraphBuilder.class);

    private final RegistrationGraphConfig registrationGraph;
    private final Map<String, NodeConfig> nodeMap;
    private final Map<String, StepDTO> stepContentMap;
    private final List<NodeEdge> nodeEdges;

    public GraphBuilder() {

        this.registrationGraph = new RegistrationGraphConfig();
        this.registrationGraph.setId(UUID.randomUUID().toString());
        this.nodeMap = new HashMap<>();
        this.stepContentMap = new HashMap<>();
        this.nodeEdges = new ArrayList<>();
    }

    /**
     * Processes and adds steps to the registration flow graph.
     *
     * @param steps The list of {@link StepDTO} objects representing steps in the flow.
     * @return The current instance of {@link GraphBuilder} for method chaining.
     * @throws RegistrationFrameworkException If an error occurs while processing the steps.
     */
    public GraphBuilder withSteps(List<StepDTO> steps) throws RegistrationFrameworkException {

        for (StepDTO step : steps) {
            switch (step.getType()) {
                case VIEW:
                    processViewStep(step);
                    break;
                case REDIRECTION:
                    processRedirectionStep(step);
                    break;
                case USER_ONBOARD:
                    processUserOnboardStep(step);
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
     * Builds and returns the final {@link RegistrationGraphConfig}.
     * The method processes node mappings and determines the first node in the flow.
     *
     * @return The built {@link RegistrationGraphConfig}.
     * @throws RegistrationFrameworkException If an error occurs during graph configuration.
     */
    public RegistrationGraphConfig build() throws RegistrationFrameworkException {

        resolveGraphEdgesAndFirstNode();
        registrationGraph.setNodeConfigs(nodeMap);
        registrationGraph.setNodePageMappings(stepContentMap);
        return registrationGraph;
    }

    private void processRedirectionStep(StepDTO step) throws RegistrationFrameworkException {

        if (step.getData() == null) {
            throw handleClientException(Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND, step.getId());
        }
        ActionDTO action = step.getData().getAction();
        if (action == null) {
            throw handleClientException(ERROR_CODE_ACTION_DATA_NOT_FOUND, step.getId());
        }
        if (!EXECUTOR.equals(action.getType())) {
            throw handleClientException(ERROR_CODE_INVALID_ACTION_TYPE, action.getType(), step.getId());
        }
        if (action.getExecutor() == null) {
            throw handleClientException(ERROR_CODE_EXECUTOR_INFO_NOT_FOUND, step.getId());
        }

        NodeConfig redirectionNode = createTaskExecutionNode(step.getId(), action.getExecutor());
        nodeMap.put(redirectionNode.getId(), redirectionNode);
        nodeEdges.add(new NodeEdge(redirectionNode.getId(), action.getNextId(), null));
    }

    private void processUserOnboardStep(StepDTO step) {

        NodeConfig userOnboardNode = createUserOnboardingNode(step.getId());
        nodeMap.put(userOnboardNode.getId(), userOnboardNode);
        // UserOnboard node is considered as the last node in the flow, hence not adding an edge to the next node.
    }

    private void processViewStep(StepDTO step)
            throws RegistrationFrameworkException {

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
            throws RegistrationFrameworkException {

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
            throws RegistrationFrameworkException {

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
            throws RegistrationClientException {

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
            this.nodeEdges.add(new NodeEdge(stepNode.getId(), tempNode.getNextNodeId(), tempNode.getId()));
        }
        nodeMap.put(step.getId(), stepNode);
    }

    private void resolveGraphEdgesAndFirstNode() throws RegistrationFrameworkException {

        Set<String> referencedNodes = new HashSet<>();
        for (NodeEdge edge : nodeEdges) {
            referencedNodes.add(edge.getTargetNodeId());
            if (!nodeMap.containsKey(edge.getSourceNodeId())) {
                throw handleServerException(Constants.ErrorMessages.ERROR_CODE_INVALID_NODE, edge.getSourceNodeId());
            }
            if (StringUtils.isNotEmpty(edge.getTargetNodeId()) && !nodeMap.containsKey(edge.getTargetNodeId())) {
                throw handleClientException(Constants.ErrorMessages.ERROR_CODE_INVALID_NEXT_STEP,
                                            edge.getTargetNodeId());
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
        registrationGraph.setFirstNodeId(firstNodeIds.get(0));
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
}
