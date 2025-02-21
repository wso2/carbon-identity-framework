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

package org.wso2.carbon.identity.user.registration.engine.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.node.Node;

/**
 * Represents a sequence of nodes in the registration flow.
 */
public class RegSequence {

    private static final Log LOG = LogFactory.getLog(RegSequence.class);
    private String id;
    private String firstNodeId;
    private Map<String, Node> nodeList;
    private Map<String, String> pageIdList;
//    private Map<String, PageDTO> pageDTOMap;

    public RegSequence() {

        pageIdList = new HashMap<>();
        nodeList = new HashMap<>();
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public RegSequence(String firstNodeId) {

        this.firstNodeId = firstNodeId;
        pageIdList = new HashMap<>();
        nodeList = new HashMap<>();
    }

    public Node getFirstNode() {

        return nodeList.get(firstNodeId);
    }

    public void setFirstNodeId(String firstNodeId) {

        this.firstNodeId = firstNodeId;
    }

    public Map<String, String> getPageIdList() {

        return pageIdList;
    }
//
//    public void addPageDTO(String key, String pageId) {
//
//        this.pageIdList.put(key, pageId);
//    }
//
//    public Map<String, Node> getNodeList() {
//
//        return nodeList;
//    }
//
//    public Node getNodeList(String nodeId) {
//
//        return nodeList.get(nodeId);
//    }
//
//    public void addNode(Node node) {
//
//        this.nodeList.put(node.getNodeId(), node);
//    }
//
//    public void setNodeList(
//            Map<String, Node> nodeList) {
//
//        this.nodeList = nodeList;
//    }
//
//    public PageDTO getPageDTOMap(String pageId) {
//
//        return pageDTOMap.get(pageId);
//    }
//
//    public void setPageDTOMap( Map<String, PageDTO> pageDTOMap) {
//
//        this.pageDTOMap = pageDTOMap;
//    }
//
//    /**
//     * Execute the registration sequence.
//     *
//     * @param context Registration context.
//     * @return Node response.
//     * @throws RegistrationFrameworkException If an error occurs while executing the registration sequence.
//     */
//    public NodeResponse execute(RegistrationContext context) throws RegistrationFrameworkException {
//
//        Node currentNode = context.getCurrentNode();
//        if (currentNode == null) {
//            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
//                              "registration sequence.");
//            currentNode = getFirstNode();
//        }
//
//        while (currentNode != null) {
//
//            NodeResponse nodeResponse = currentNode.execute(context);
//
//            // Sometimes the node execution can be completed but request more data. Ex: CombinedInputCollectorNode.
//            if (STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus()) && !nodeResponse.getInputMetaDataList().isEmpty()) {
//                if (LOG.isDebugEnabled()){
//                    LOG.debug("Current node: "+ currentNode.getNodeId() + " is completed but requires more data.");
//                }
//                currentNode = moveToNextNode(currentNode);
//                nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
//                context.setCurrentNode(currentNode);
//                context.setRequiredMetaData(nodeResponse.getInputMetaDataList());
//                return nodeResponse;
//            } else if (STATUS_USER_CREATED.equals(nodeResponse.getStatus())) {
//                if (LOG.isDebugEnabled()){
//                    LOG.debug("User is successfully registered. Move to next node if there are any.");
//                }
//                currentNode = moveToNextNode(currentNode);
//            } else if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
//                if (LOG.isDebugEnabled()){
//                    LOG.debug("Prompt only node is completed. Move to next node if there are any.");
//                }
//                nodeResponse.setPageDTO(getPageId(currentNode.getNodeId(), nodeResponse.getStatus()));
//                nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
//                currentNode = moveToNextNode(currentNode);
//                context.setCurrentNode(currentNode);
//                return nodeResponse;
//            }else if (!STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus())) {
//                context.setCurrentNode(currentNode);
//                context.setRequiredMetaData(nodeResponse.getInputMetaDataList());
//                if (LOG.isDebugEnabled()){
//                    LOG.debug("User input is required for the current node: " + currentNode.getNodeId());
//                }
//                if (STATUS_EXTERNAL_REDIRECTION.equals(nodeResponse.getStatus())) {
//                    nodeResponse.setStatus(STATUS_EXTERNAL_REDIRECTION);
//                    // TODO: Implement the external redirection sumbit logic.
//                    context.setExecutorStatus(STATUS_ATTR_REQUIRED);
//                } else {
//                    nodeResponse.setPageDTO(getPageId(currentNode.getNodeId(), nodeResponse.getStatus()));
//                    nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
//                }
//                return nodeResponse;
//            } else {
//                currentNode = moveToNextNode(currentNode);
//            }
//        }
//        return handleExitLogic(context);
//    }
//
//    /**
//     * Set the current node as the previous node of the next node and return the next node.
//     *
//     * @param currentNode Current node.
//     * @return Next node.
//     */
//    private Node moveToNextNode(Node currentNode) {
//
////        Node nextNode = currentNode.getNextNode();
//        String nextNodeId = currentNode.getNextNodeId();
//        Node nextNode = getNodeList(nextNodeId);
//        if (nextNode != null) {
//            if(LOG.isDebugEnabled()) {
//                LOG.debug("Current node " + currentNode.getNodeId() + " is completed. "
//                                  + "Moving to the next node: " + nextNodeId
//                                  + " and setting " + currentNode.getNodeId() + " as the previous node.");
//            }
//            nextNode.setPreviousNodeId(currentNode.getNodeId());
//        }
//        return nextNode;
//    }
//
//    // TODO: Implement the exit logic of the registration flow.
//    private NodeResponse handleExitLogic(RegistrationContext context) {
//
//        NodeResponse response = new NodeResponse(STATUS_FLOW_COMPLETE);
//        if (context.getUserAssertion() != null ) {
//            response.setUserAssertion(context.getUserAssertion());
//        }
//        return response;
//    }
//
//    private PageDTO getPageId(String nodeId, String status) throws RegistrationServerException {
//
//        String pageId = null;
//        if (Constants.STATUS_ATTR_REQUIRED.equals(status)) {
//            pageId =  pageIdList.get(nodeId + "_ATTRIBUTE_COLLECTION");
//        } else if (Constants.STATUS_CRED_REQUIRED.equals(status)) {
//            pageId = pageIdList.get(nodeId + "_CREDENTIAL_ONBOARDING");
//        } else if (Constants.STATUS_VERIFICATION_REQUIRED.equals(status)) {
//            pageId =  pageIdList.get(nodeId + "_VERIFICATION");
//        }
//        if (pageId == null) {
//            pageId = pageIdList.get(nodeId + "_INIT");
//            if (pageId == null) {
//                throw new RegistrationServerException("Could not resolve a valid page to be prompt.");
//            }
//        }
//        return pageDTOMap.get(pageId);
//    }

}
