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

package org.wso2.carbon.identity.user.registration.mgt.adapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.COMPLETE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.EXECUTOR_FOR_PROMPT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NEXT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;

public class FlowConvertor {

    // Define a constant to LOG the information.
    private static final Log LOG = LogFactory.getLog(FlowConvertor.class);

    public static RegistrationFlowConfig convert(RegistrationFlowDTO flowDTO)
            throws RegistrationFrameworkException {

        RegistrationFlowConfig flowConfig = new RegistrationFlowConfig();
        flowConfig.setId(UUID.randomUUID().toString());

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        Map<String, StepDTO> stepContentMap = new HashMap<>();
        List<NodeEdge> nodeEdges = new ArrayList<>();

        NodeConfig userOnboardingNode = createUserOnboardingNode();
        nodeMap.put(userOnboardingNode.getUuid(), userOnboardingNode);
        String endNodeId = userOnboardingNode.getUuid();

        for (StepDTO step : flowDTO.getSteps()) {
            validateStep(step);
            NodeConfig stepNode;
            if (Constants.StepTypes.VIEW.equals(step.getType())) {
                stepNode = processViewStep(step, nodeMap, nodeEdges, endNodeId);
            } else if (Constants.StepTypes.REDIRECTION.equals(step.getType())) {
                stepNode = processRedirectionStep(step, nodeMap, nodeEdges, endNodeId);
            } else {
                throw new RegistrationClientException("Invalid step type: " + step.getType());
            }
            stepContentMap.put(step.getId(), step);
            setFirstNodeIfNeeded(flowConfig, stepNode);
        }

        updateNodeMappings(nodeMap, nodeEdges);
        flowConfig.setNodeConfigs(nodeMap);
        flowConfig.setNodePageMappings(stepContentMap);
        return flowConfig;
    }

    // TODO: Remove if we don't need this
    private static void validateStep(StepDTO step) throws RegistrationFrameworkException {


    }

    private static NodeConfig processRedirectionStep(StepDTO step, Map<String, NodeConfig> nodeMap,
                                                     List<NodeEdge> nodeMappings, String endNodeId)
            throws RegistrationFrameworkException {

        ActionDTO action = step.getData().getAction();
        if (action == null) {
            throw new RegistrationServerException("Action should be available in the redirection step.");
        }

        NodeConfig redirectionNode;
        if (EXECUTOR.equals(action.getType())) {
            redirectionNode = createTaskExecutionNode(step.getId(), action.getExecutor());
            nodeMap.put(redirectionNode.getUuid(), redirectionNode);
        } else {
            throw new RegistrationClientException("Invalid step configurations. Action type should be " + "EXECUTOR");
        }

        String nextNodeId = COMPLETE.equals(action.getNextId()) ? endNodeId : action.getNextId();

        NodeEdge edge = new NodeEdge(redirectionNode.getUuid(), nextNodeId, null);
        nodeMappings.add(edge);

        return redirectionNode;
    }

    private static NodeConfig processViewStep(StepDTO step, Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeEdges, String endNodeId) throws RegistrationFrameworkException {

        List<NodeConfig> stepNodes = new ArrayList<>();
        List<ComponentDTO> components = step.getData().getComponents();
        if (components == null || components.isEmpty()) {
            throw new RegistrationClientException("Components should be available in the view step.");
        }
        for (ComponentDTO component : components) {
            processComponent(component, stepNodes);
        }
        return handleTempNodesInStep(stepNodes, step, nodeMap, nodeEdges, endNodeId);

    }

    private static void processComponent(ComponentDTO component, List<NodeConfig> stepNodes)
            throws RegistrationFrameworkException {

        if (FORM.equals(component.getType())) {
            for (ComponentDTO subComponent : component.getComponents()) {
                processComponent(subComponent, stepNodes);
            }
        } else if (BUTTON.equals(component.getType()) && component.getAction() != null) {
            validateStepActions(component.getAction(), stepNodes);
            ActionDTO action = component.getAction();
            NodeConfig node = createNodeFromAction(action, component);
            stepNodes.add(node);
        }
    }

    private static void validateStepActions(ActionDTO action, List<NodeConfig> stepNodes) throws RegistrationFrameworkException {

        if (action == null) {
            throw new RegistrationClientException("Button component must have an action");
        }
        if (action.getNextId() == null) {
            throw new RegistrationClientException("Next id must be available in the action.");
        }
        if (EXECUTOR.equals(action.getType())) {
            if (action.getExecutor() == null) {
                throw new RegistrationClientException("Executor details must be available in the action.");
            }
            if (stepNodes.stream().anyMatch(nodeConfig -> (nodeConfig.getType().equals(TASK_EXECUTION)) &&
                    !EXECUTOR_FOR_PROMPT.equals(nodeConfig.getExecutorConfig().getName()))) {
                throw new RegistrationClientException("Multiple executors are not allowed in a single step.");
            }
        }
    }

    private static NodeConfig createNodeFromAction(ActionDTO action, ComponentDTO component)
            throws RegistrationClientException {

        NodeConfig tempNodeInComponent;
        if (NEXT.equals(action.getType())) {
            tempNodeInComponent = createTaskExecutionNode(component.getId(), new ExecutorDTO(EXECUTOR_FOR_PROMPT));
        } else if (EXECUTOR.equals(action.getType())) {
            tempNodeInComponent = createTaskExecutionNode(component.getId(), action.getExecutor());
        } else {
            throw new RegistrationClientException("Invalid action type: " + action.getType());
        }
        tempNodeInComponent.setNextNodeId(action.getNextId());
        return tempNodeInComponent;
    }

    private static NodeConfig handleTempNodesInStep(List<NodeConfig> tempNodesInStep, StepDTO step, Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeMappings, String endNodeId) {

        NodeConfig stepNode = null;
        if (tempNodesInStep.size() > 1) {
            NodeConfig decisionNode = createDecisionNode(step.getId());
            for (NodeConfig nodeConfig : tempNodesInStep) {
                String nextNodeId = COMPLETE.equals(nodeConfig.getNextNodeId()) ? endNodeId : nodeConfig.getNextNodeId();
                if (TASK_EXECUTION.equals(nodeConfig.getType()) &&
                        !EXECUTOR_FOR_PROMPT.equals(nodeConfig.getExecutorConfig().getName())) {
                    // Edge from executor node to the next node.
                    NodeEdge nextNodeEdge = new NodeEdge(nodeConfig.getUuid(), nextNodeId, null);
                    nodeConfig.setNextNodeId(null);
                    nodeMap.put(nodeConfig.getUuid(), nodeConfig);
                    nodeMappings.add(nextNodeEdge);

                    // Edge from decision node to the executor node.
                    NodeEdge decisionEdge = new NodeEdge(decisionNode.getUuid(), nodeConfig.getUuid(),
                            nodeConfig.getUuid());
                    nodeMappings.add(decisionEdge);
                } else {
                    // Edge from decision node to the next node derived from NEXT actions.
                    NodeEdge decisionEdge = new NodeEdge(decisionNode.getUuid(), nextNodeId, nodeConfig.getUuid());
                    nodeMappings.add(decisionEdge);
                }
            }
            stepNode = decisionNode;
        } else if (tempNodesInStep.size() == 1) {

            NodeConfig tempNode = tempNodesInStep.get(0);

            String nextNodeId = COMPLETE.equals(tempNode.getNextNodeId()) ? endNodeId : tempNode.getNextNodeId();
            stepNode = new NodeConfig();
            stepNode.setUuid(step.getId());
            stepNode.setType(tempNode.getType());
            stepNode.setExecutorConfig(tempNode.getExecutorConfig());

            NodeEdge edge = new NodeEdge(stepNode.getUuid(), nextNodeId, tempNode.getUuid());
            nodeMappings.add(edge);
        }
        nodeMap.put(step.getId(), stepNode);
        return stepNode;
    }

    private static void setFirstNodeIfNeeded(RegistrationFlowConfig registrationFlowConfig, NodeConfig nodeConfig) {

        if (registrationFlowConfig.getFirstNodeId() == null) {
            registrationFlowConfig.setFirstNodeId(nodeConfig.getUuid());
            nodeConfig.setFirstNode(true);
        }
    }

    private static void updateNodeMappings(Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeMappings)
            throws RegistrationFrameworkException {

        for (NodeEdge edge : nodeMappings) {
            String nodeId = edge.getSourceNodeId();
            String nextNodeId = edge.getTargetNodeId();

            if (!nodeMap.containsKey(nodeId)) {
                throw new RegistrationServerException("Node id is not found: " + nodeId);
            }
            if (!nodeMap.containsKey(nextNodeId)) {
                throw new RegistrationServerException("Next node id is not found: " + nextNodeId);
            }
            nodeMap.get(nodeId).addEdge(edge);
        }
    }

    private static NodeConfig createTaskExecutionNode(String id, ExecutorDTO executorDTO) {

        NodeConfig node = new NodeConfig();
        node.setUuid(id);
        node.setType(TASK_EXECUTION);
        node.setExecutorConfig(executorDTO);
        return node;
    }

    private static NodeConfig createDecisionNode(String id) {

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setUuid(id);
        nodeConfig.setType(Constants.NodeTypes.DECISION);
        return nodeConfig;
    }

    private static NodeConfig createUserOnboardingNode() {

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setUuid(UUID.randomUUID().toString());
        nodeConfig.setType(TASK_EXECUTION);
        ExecutorDTO executorConfig = new ExecutorDTO(Constants.EXECUTOR_FOR_USER_ONBOARDING);
        nodeConfig.setExecutorConfig(executorConfig);
        return nodeConfig;
    }
}
