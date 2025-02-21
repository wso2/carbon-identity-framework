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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.ActionDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.BlockDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ComponentDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.ExecutorDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.StepDTO;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.ActionTypes.EXECUTOR;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.COMPLETE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.EXECUTOR_FOR_PROMPT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NEXT;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.TASK_EXECUTION;

public class FlowConvertor {

    // Define a constant to LOG the information.
    private static final Log LOG = LogFactory.getLog(FlowConvertor.class);

    public static RegistrationFlowConfig getSequence(RegistrationFlowDTO flowDTO) throws RegistrationFrameworkException {

        RegistrationFlowConfig registrationFlowConfig = new RegistrationFlowConfig();
        registrationFlowConfig.setId(UUID.randomUUID().toString());
        registrationFlowConfig.setName("DEFAULT");
        List<NodeEdge> nodeMappings = new ArrayList<>();
        NodeConfig endNode = createUserOnboardingNode();
        registrationFlowConfig.addNodeConfig(endNode);

        for (StepDTO step : flowDTO.getSteps()) {
            if (Constants.StepTypes.VIEW.equals(step.getType())) {
                processViewStep(step, registrationFlowConfig, nodeMappings, endNode.getUuid());
            } else if (Constants.StepTypes.TRIGGER.equals(step.getType())) {
                processTriggerStep(step, registrationFlowConfig, nodeMappings, endNode.getUuid());
            }
        }

        updateNodeMappings(registrationFlowConfig, nodeMappings);

        return registrationFlowConfig;
    }

    private static void processViewStep(StepDTO step, RegistrationFlowConfig registrationFlowConfig,
                                        List<NodeEdge> nodeMappings, String endNodeId) throws RegistrationFrameworkException {

        List<NodeConfig> tempNodesInStep = new ArrayList<>();

        boolean isExecutorEngaged = false;
        for (BlockDTO blockDTO : step.getBlocks()) {
            for (ComponentDTO componentDTO : blockDTO.getComponents()) {
                NodeConfig tempNodeInElement = processElement(componentDTO, isExecutorEngaged);
                if (tempNodeInElement != null) {
                    tempNodesInStep.add(tempNodeInElement);
                    if (EXECUTOR.equals(componentDTO.getAction().getType())) {
                        isExecutorEngaged = true;
                    }
                }
            }
        }

        handleTempNodesInStep(tempNodesInStep, step, registrationFlowConfig, nodeMappings, endNodeId);
    }

    private static NodeConfig processElement(ComponentDTO componentDTO, boolean isExecutorEngaged) throws RegistrationFrameworkException {

        if ("ACTION".equals(componentDTO.getCategory())) {
            if (componentDTO.getAction() == null) {
                throw new RegistrationClientException("Action element should have an action.");
            }
            ActionDTO action = componentDTO.getAction();
            NodeConfig tempNodeInElement = null;

            if (NEXT.equals(action.getType())) {

                tempNodeInElement = createTaskExecutionNode(componentDTO.getId(), new ExecutorDTO(EXECUTOR_FOR_PROMPT));
                tempNodeInElement.setNextNodeId(action.getNextId());
            } else if (EXECUTOR.equals(action.getType())) {
                if (isExecutorEngaged) {
                    throw new RegistrationServerException("Multiple executors are not allowed in a single step.");
                }
                tempNodeInElement = createTaskExecutionNode(componentDTO.getId(), action.getExecutor());
                tempNodeInElement.setNextNodeId(action.getNextId());
            }
            return tempNodeInElement;
        }
        return null;
    }

    private static void handleTempNodesInStep(List<NodeConfig> tempNodesInStep, StepDTO step,
                                              RegistrationFlowConfig registrationFlowConfig,
                                              List<NodeEdge> nodeMappings, String endNodeId) {

        if (tempNodesInStep.size() > 1) {
            NodeConfig decisionNode = createDecisionNode(step.getId());
            for (NodeConfig nodeConfig : tempNodesInStep) {
                String nextNodeId = COMPLETE.equals(nodeConfig.getNextNodeId()) ? endNodeId : nodeConfig.getNextNodeId();
                if (TASK_EXECUTION.equals(nodeConfig.getType()) &&
                        !EXECUTOR_FOR_PROMPT.equals(nodeConfig.getExecutorConfig().getName())) {
                    // Edge from executor node to the next node.
                    NodeEdge nextNodeEdge = new NodeEdge(nodeConfig.getUuid(), nextNodeId, null);
                    nodeMappings.add(nextNodeEdge);
                    nodeConfig.setNextNodeId(null);
                    registrationFlowConfig.addNodeConfig(nodeConfig);

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
            setFirstNodeIfNeeded(registrationFlowConfig, decisionNode);
            registrationFlowConfig.addNodeConfig(decisionNode);
            registrationFlowConfig.addNodePageMapping(decisionNode.getUuid(), step);
        } else if (tempNodesInStep.size() == 1) {

            NodeConfig tempNode = tempNodesInStep.get(0);

            String nextNodeId = COMPLETE.equals(tempNode.getNextNodeId()) ? endNodeId : tempNode.getNextNodeId();
            NodeConfig stepNode = new NodeConfig();
            stepNode.setUuid(step.getId());
            stepNode.setType(tempNode.getType());
            stepNode.setExecutorConfig(tempNode.getExecutorConfig());

            NodeEdge edge = new NodeEdge(stepNode.getUuid(), nextNodeId, tempNode.getUuid());
            nodeMappings.add(edge);
            setFirstNodeIfNeeded(registrationFlowConfig, stepNode);
            registrationFlowConfig.addNodeConfig(stepNode);
            registrationFlowConfig.addNodePageMapping(stepNode.getUuid(), step);
        }
    }

    private static void setFirstNodeIfNeeded(RegistrationFlowConfig registrationFlowConfig, NodeConfig nodeConfig) {

        if (registrationFlowConfig.getFirstNodeId() == null) {
            registrationFlowConfig.setFirstNodeId(nodeConfig.getUuid());
            nodeConfig.setFirstNode(true);
        }
    }

    private static void processTriggerStep(StepDTO step, RegistrationFlowConfig registrationFlowConfig,
                                           List<NodeEdge> nodeMappings, String endNodeId) throws RegistrationFrameworkException {

        if (step.getActionDTO() == null) {
            throw new RegistrationClientException("Trigger step should have an action.");
        }
        ActionDTO action = step.getActionDTO();
        String nextNodeId = COMPLETE.equals(action.getNextId()) ? endNodeId : action.getNextId();

        NodeConfig triggerNode = createTaskExecutionNode(step.getId(), action.getExecutor());
        NodeEdge edge = new NodeEdge(triggerNode.getUuid(), nextNodeId, null );
        nodeMappings.add(edge);

        setFirstNodeIfNeeded(registrationFlowConfig, triggerNode);
        registrationFlowConfig.addNodeConfig(triggerNode);
        registrationFlowConfig.addNodePageMapping(triggerNode.getUuid(), step);
    }

    private static void updateNodeMappings(RegistrationFlowConfig registrationFlowConfig,
                                           List<NodeEdge> nodeMappings) throws RegistrationFrameworkException {

        for (NodeEdge edge : nodeMappings) {
            String nodeId = edge.getSourceNodeId();
            String nextNodeId = edge.getTargetNodeId();

            if (!registrationFlowConfig.getNodeConfigs().containsKey(nodeId)) {
                throw new RegistrationServerException("Node id is not found: " + nodeId);
            }
            if (!registrationFlowConfig.getNodeConfigs().containsKey(nextNodeId)) {
                throw new RegistrationServerException("Next node id is not found: " + nextNodeId);
            }
            registrationFlowConfig.getNodeConfigs().get(nodeId).addEdge(edge);
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
