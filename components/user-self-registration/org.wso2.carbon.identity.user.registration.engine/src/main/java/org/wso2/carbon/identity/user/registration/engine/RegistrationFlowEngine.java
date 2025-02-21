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
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.engine.internal.UserRegistrationServiceDataHolder;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowConfig;
import org.wso2.carbon.identity.user.registration.engine.model.NodeResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.node.Node;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_ATTR_REQUIRED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_EXTERNAL_REDIRECTION;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_FLOW_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_NODE_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_USER_CREATED;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_USER_INPUT_REQUIRED;

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
    public NodeResponse execute(RegistrationContext context, RegistrationFlowConfig sequence) throws RegistrationFrameworkException {

        NodeConfig currentNode = context.getCurrentNode();
        if (currentNode == null) {
            LOG.debug("Current node is not set. Setting the first node as the current node and starting the " +
                              "registration sequence.");
            String firstNodeId = sequence.getFirstNodeId();
            currentNode = sequence.getNodeConfigs().get(firstNodeId);
        }

        while (currentNode != null) {

            NodeResponse nodeResponse = triggerNode(currentNode, context);

            if (STATUS_USER_CREATED.equals(nodeResponse.getStatus())) {
                if (LOG.isDebugEnabled()){
                    LOG.debug("User is successfully registered. Move to next node if there are any.");
                }
                currentNode = moveToNextNode(sequence, currentNode);
            } else if (STATUS_PROMPT_ONLY.equals(nodeResponse.getStatus())) {
                if (LOG.isDebugEnabled()){
                    LOG.debug("Prompt only node is completed. Move to next node if there are any.");
                }
                nodeResponse.setPageDTO(sequence.getNodePageMappings().get(currentNode.getUuid()));
                nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
                currentNode = moveToNextNode(sequence, currentNode);
                context.setCurrentNode(currentNode);
                return nodeResponse;
            }else if (!STATUS_NODE_COMPLETE.equals(nodeResponse.getStatus())) {
                context.setCurrentNode(currentNode);
                if (LOG.isDebugEnabled()){
                    LOG.debug("User input is required for the current node: " + currentNode.getUuid());
                }
                if (STATUS_EXTERNAL_REDIRECTION.equals(nodeResponse.getStatus())) {
                    nodeResponse.setStatus(STATUS_EXTERNAL_REDIRECTION);
                    // TODO: Implement the external redirection sumbit logic.
                    context.setExecutorStatus(STATUS_ATTR_REQUIRED);
                } else {
                    nodeResponse.setPageDTO(sequence.getNodePageMappings().get(currentNode.getUuid()));
                    nodeResponse.setStatus(STATUS_USER_INPUT_REQUIRED);
                }
                return nodeResponse;
            } else {
                currentNode = moveToNextNode(sequence, currentNode);
            }
        }
        return handleExitLogic(context);
    }

    /**
     * Set the current node as the previous node of the next node and return the next node.
     *
     * @param currentNode Current node.
     * @return Next node.
     */
    private NodeConfig moveToNextNode(RegistrationFlowConfig regConfig, NodeConfig currentNode) {

        String nextNodeId = currentNode.getNextNodeId();
        NodeConfig nextNode = regConfig.getNodeConfigs().get(nextNodeId);
        if (nextNode != null) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Current node " + currentNode.getUuid() + " is completed. "
                                  + "Moving to the next node: " + nextNodeId
                                  + " and setting " + currentNode.getUuid() + " as the previous node.");
            }
            nextNode.setPreviousNodeId(currentNode.getUuid());
        }
        return nextNode;
    }

    // TODO: Implement the exit logic of the registration flow.
    private NodeResponse handleExitLogic(RegistrationContext context) {

        NodeResponse response = new NodeResponse(STATUS_FLOW_COMPLETE);
        if (context.getUserAssertion() != null ) {
            response.setUserAssertion(context.getUserAssertion());
        }
        return response;
    }

    private NodeResponse triggerNode(NodeConfig nodeConfig, RegistrationContext context)
            throws RegistrationFrameworkException {

        Node nodeImpl = null;
        for (Node node : UserRegistrationServiceDataHolder.getNodes()) {
            if (nodeConfig.getType().equals(node.getName())) {
                 nodeImpl = node;
                break;
            }
        }

        if (nodeImpl == null) {
            throw new RegistrationServerException("Unsupported node type: " + nodeConfig.getType());
        }
        return nodeImpl.execute(context, nodeConfig);
    }
}
