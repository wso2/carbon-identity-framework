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

package org.wso2.carbon.identity.user.registration.engine.temp;

/**
 * Manager class to handle the registration flow.
 */
public class SequenceManager {

//    public RegSequence loadSequence(String orgId) throws RegistrationServerException {
//
//        RegistrationFlowConfig registrationFlowConfig =
//                UserRegistrationServiceDataHolder.getRegistrationFlowMgtService().retrieveRegistrationSequenceByTenantId(orgId);
//
//        RegSequence sequence = new RegSequence();
//        sequence.setId(regDto.getFlowID());
//        sequence.setPageDTOMap(regDto.getPageDTOs());
//        for (NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
//            Node node;
//
//            if ("EXECUTOR".equals(nodeDTO.getType())) {
//                String executorName = nodeDTO.getProperties().get("EXECUTOR_NAME");
//                if (executorName == null) {
//                    throw new RegistrationServerException(
//                            "Executor ID is not defined for the node: " + nodeDTO.getId());
//                }
//                if (nodeDTO.getNextNodes().size() > 1) {
//                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
//                                                                  nodeDTO.getId());
//                }
//                Executor mappedRegExecutor = null;
//                for (Executor executor : UserRegistrationServiceDataHolder.getExecutors()) {
//                    if (executorName.equals(executor.getName())) {
//                        mappedRegExecutor = executor;
//                        break;
//                    }
//                }
//                if (mappedRegExecutor == null) {
//                    throw new RegistrationServerException("Unsupported executor: " + executorName);
//                }
//                node = new TaskExecutionNode(nodeDTO.getId(), mappedRegExecutor);
//            } else if ("DECISION".equals(nodeDTO.getType())) {
//                if (nodeDTO.getNextNodes().size() < 2) {
//                    throw new RegistrationServerException(
//                            "Less than two next nodes are defined for the decision node: " +
//                                    nodeDTO.getId());
//                }
//                node = new UserChoiceDecisionNode(nodeDTO.getId());
//            } else if ("INPUT".equals(nodeDTO.getType())) {
//                if (nodeDTO.getNextNodes().size() > 1) {
//                    throw new RegistrationServerException("Multiple next nodes are defined for the executor node: " +
//                                                                  nodeDTO.getId());
//                }
//                node = new ViewPromptingNode(nodeDTO.getId());
//            } else {
//                throw new RegistrationServerException("Unsupported node type: " + nodeDTO.getType());
//            }
//
//            for (Map.Entry<String, String> pageDetail : nodeDTO.getPageIds().entrySet()) {
//                String pageRefId = nodeDTO.getId() + "_" + pageDetail.getKey();
//                sequence.addPageDTO(pageRefId, pageDetail.getValue());
//            }
//            sequence.addNode(node);
//        }
//
//        sequence.setFirstNodeId(regDto.getFirstNode());
//
//        for(NodeDTO nodeDTO : regDto.getNodeDTOList().values()) {
//            if (nodeDTO.getNextNodes().size() == 1) {
//                sequence.getNodeList(nodeDTO.getId()).setNextNodeId(nodeDTO.getNextNodes().get(0));
//            } else if (nodeDTO.getNextNodes().size() > 1) {
//                List<Node> nextNodes = new ArrayList<>();
//                for (String nextNodeId : nodeDTO.getNextNodes()) {
//                    nextNodes.add(sequence.getNodeList(nextNodeId));
//                }
//                ((UserChoiceDecisionNode) sequence.getNodeList(nodeDTO.getId())).setNextNodes(nextNodes);
//            }
//        }
//        return sequence;
//    }
}
