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
import static org.wso2.carbon.identity.user.registration.mgt.Constants.COMPLETE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ComponentTypes.FORM;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_ACTION_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_COMPONENT_DATA_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_EXECUTOR_INFO_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_FOR_BUTTON;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_NEXT_STEP;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_INVALID_NODE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_MULTIPLE_STEP_EXECUTORS;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_NEXT_ACTION_NOT_FOUND;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_ACTION_TYPE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.REDIRECTION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import static org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.mgt.utils.RegistrationMgtUtils.handleServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;

/**
 * This class is responsible for building the registration flow graph.
 */
public class GraphBuilder {

    private static final Log LOG = LogFactory.getLog(GraphBuilder.class);

    private GraphBuilder() {

    }

    /**
     * Converts the registration flow DTO to a registration graph configuration.
     *
     * @param flowDTO The registration flow DTO.
     * @return The registration graph configuration.
     * @throws RegistrationFrameworkException If an error occurs while converting the flow.
     */
    public static RegistrationGraphConfig convert(RegistrationFlowDTO flowDTO)
            throws RegistrationFrameworkException {

        RegistrationGraphConfig flowConfig = new RegistrationGraphConfig();
        flowConfig.setId(UUID.randomUUID().toString());

        Map<String, NodeConfig> nodeMap = new HashMap<>();
        Map<String, StepDTO> stepContentMap = new HashMap<>();
        List<NodeEdge> nodeEdges = new ArrayList<>();

        String endNodeId = handleEndNode(nodeMap);

        for (StepDTO step : flowDTO.getSteps()) {
            if (step.getData() == null) {
                throw handleClientException(Constants.ErrorMessages.ERROR_CODE_STEP_DATA_NOT_FOUND, step.getId());
            }
            NodeConfig stepNode;
            if (VIEW.equals(step.getType())) {
                stepNode = processViewStep(step, nodeMap, nodeEdges, endNodeId);
            } else if (REDIRECTION.equals(step.getType())) {
                stepNode = processRedirectionStep(step, nodeMap, nodeEdges, endNodeId);
            } else {
                throw handleClientException(Constants.ErrorMessages.ERROR_CODE_UNSUPPORTED_STEP_TYPE, step.getType());
            }
            stepContentMap.put(step.getId(), step);
            setFirstNodeIfNeeded(flowConfig, stepNode);
        }

        updateNodeMappings(nodeMap, nodeEdges);
        flowConfig.setNodeConfigs(nodeMap);
        flowConfig.setNodePageMappings(stepContentMap);
        return flowConfig;
    }

    private static String handleEndNode(Map<String, NodeConfig> nodeMap) {

        NodeConfig userOnboardingNode = createUserOnboardingNode();
        nodeMap.put(userOnboardingNode.getId(), userOnboardingNode);
        return userOnboardingNode.getId();
    }

    private static NodeConfig processRedirectionStep(StepDTO step, Map<String, NodeConfig> nodeMap,
                                                     List<NodeEdge> nodeMappings, String endNodeId)
            throws RegistrationFrameworkException {

        ActionDTO action = step.getData().getAction();
        if (action == null) {
            throw handleClientException(ERROR_CODE_ACTION_DATA_NOT_FOUND, step.getId());
        }
        if (!EXECUTOR.equals(action.getType())) {
            throw handleClientException(ERROR_CODE_INVALID_ACTION_TYPE, action.getType(), step.getId());
        }

        NodeConfig redirectionNode = createTaskExecutionNode(step.getId(), action.getExecutor());
        String nextNodeId = COMPLETE.equals(action.getNextId()) ? endNodeId : action.getNextId();
        nodeMap.put(redirectionNode.getId(), redirectionNode);
        nodeMappings.add(new NodeEdge(redirectionNode.getId(), nextNodeId, null));
        return redirectionNode;
    }

    private static NodeConfig processViewStep(StepDTO step, Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeEdges,
                                              String endNodeId) throws RegistrationFrameworkException {

        List<NodeConfig> stepNodes = new ArrayList<>();
        List<ComponentDTO> components = step.getData().getComponents();
        if (components == null || components.isEmpty()) {
            throw handleClientException(ERROR_CODE_COMPONENT_DATA_NOT_FOUND, step.getId());
        }
        for (ComponentDTO component : components) {
            processComponent(component, stepNodes, step.getId());
        }
        return handleTempNodesInStep(stepNodes, step, nodeMap, nodeEdges, endNodeId);
    }

    private static void processComponent(ComponentDTO component, List<NodeConfig> stepNodes, String stepId)
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

    private static void validateStepActions(ActionDTO action, List<NodeConfig> stepNodes, String id, String stepId)
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

    private static NodeConfig createNodeFromAction(ActionDTO action, String componentId)
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

    private static NodeConfig handleTempNodesInStep(List<NodeConfig> tempNodesInStep, StepDTO step,
                                                    Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeMappings,
                                                    String endNodeId) {

        NodeConfig stepNode = null;
        if (tempNodesInStep.size() > 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Multiple nodes are derived from the step: " + step.getId() + ". Creating a decision node.");
            }
            NodeConfig decisionNode = createDecisionNode(step.getId());
            for (NodeConfig nodeConfig : tempNodesInStep) {
                String nextNodeId =
                        COMPLETE.equals(nodeConfig.getNextNodeId()) ? endNodeId : nodeConfig.getNextNodeId();
                if (TASK_EXECUTION.equals(nodeConfig.getType())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("A node with an execution found in the step. Therefore adding it to the node list " +
                                          "with id, " + nodeConfig.getId());
                    }
                    nodeConfig.setNextNodeId(null);
                    nodeMap.put(nodeConfig.getId(), nodeConfig);
                    nodeMappings.add(new NodeEdge(nodeConfig.getId(), nextNodeId, null));
                    nodeMappings.add(new NodeEdge(decisionNode.getId(), nodeConfig.getId(), nodeConfig.getId()));
                } else {
                    // Edge from decision node to the next node derived from NEXT actions.
                    nodeMappings.add(new NodeEdge(decisionNode.getId(), nextNodeId, nodeConfig.getId()));
                }
            }
            stepNode = decisionNode;
        } else if (tempNodesInStep.size() == 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Only one node derived from the step: " + step.getId() + ". Adding it to the node list.");
            }

            NodeConfig tempNode = tempNodesInStep.get(0);

            String nextNodeId = COMPLETE.equals(tempNode.getNextNodeId()) ? endNodeId : tempNode.getNextNodeId();
            stepNode = new NodeConfig.Builder()
                    .id(step.getId())
                    .type(tempNode.getType())
                    .executorConfig(tempNode.getExecutorConfig())
                    .build();
            nodeMappings.add(new NodeEdge(stepNode.getId(), nextNodeId, tempNode.getId()));
        }
        nodeMap.put(step.getId(), stepNode);
        return stepNode;
    }

    private static void setFirstNodeIfNeeded(RegistrationGraphConfig flowConfig, NodeConfig nodeConfig) {

        if (flowConfig.getFirstNodeId() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "Setting node: " + nodeConfig.getId() + " as the first node in the flow " + flowConfig.getId());
            }
            flowConfig.setFirstNodeId(nodeConfig.getId());
            nodeConfig.setFirstNode(true);
        }
    }

    private static void updateNodeMappings(Map<String, NodeConfig> nodeMap, List<NodeEdge> nodeMappings)
            throws RegistrationFrameworkException {

        for (NodeEdge edge : nodeMappings) {
            String nodeId = edge.getSourceNodeId();
            String nextNodeId = edge.getTargetNodeId();

            if (!nodeMap.containsKey(nodeId)) {
                throw handleServerException(ERROR_CODE_INVALID_NODE, nodeId);
            }
            if (!nodeMap.containsKey(nextNodeId)) {
                throw handleClientException(ERROR_CODE_INVALID_NEXT_STEP, nextNodeId);
            }
            nodeMap.get(nodeId).addEdge(edge);
        }
    }

    private static NodeConfig createTaskExecutionNode(String id, ExecutorDTO executorDTO) {

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

    private static NodeConfig createDecisionNode(String id) {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(id)
                .type(DECISION)
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a decision node " + id + ".");
        }
        return nodeConfig;
    }

    private static NodeConfig createUserOnboardingNode() {

        NodeConfig nodeConfig = new NodeConfig.Builder()
                .id(UUID.randomUUID().toString())
                .type(TASK_EXECUTION)
                .executorConfig(new ExecutorDTO(Constants.EXECUTOR_FOR_USER_ONBOARDING))
                .build();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created a node with id " + nodeConfig.getId() + " for user onboarding.");
        }
        return nodeConfig;
    }

    private static NodeConfig createPagePromptNode(String id) {

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
