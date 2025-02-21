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

package org.wso2.carbon.identity.user.registration.engine.node;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.engine.util.Constants;
import org.wso2.carbon.identity.user.registration.engine.model.NodeResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;

import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_USER_INPUT_REQUIRED;

/**
 * Implementation of a node specific to prompting user to select a choice out of multiple registration executor options.
 */
public class UserChoiceDecisionNode implements Node {

    private static final Log LOG = LogFactory.getLog(UserChoiceDecisionNode.class);
    private static final String NODE_INPUT = "action-id";

    @Override
    public String getName() {

        return DECISION;
    }

    @Override
    public NodeResponse execute(RegistrationContext context, NodeConfig config) throws RegistrationServerException {

        Map<String, String> inputData = context.getUserInputData();

        if (inputData != null && inputData.containsKey(NODE_INPUT)) {
            String selectedNode = inputData.get(NODE_INPUT);
            for (String nextNodeId : config.getNextNodeIds()) {
                if (nextNodeId.equals(selectedNode)) {
                    config.setNextNodeId(selectedNode);
                    break;
                }
            }
            if (config.getNextNodeId() == null) {
                throw new RegistrationServerException("Cannot find a valid node to proceed.");
            }
        }
        if (config.getNextNodeId() != null) {
            return new NodeResponse(Constants.STATUS_NODE_COMPLETE);
        }
        return new NodeResponse(STATUS_USER_INPUT_REQUIRED);
    }

    @Override
    public NodeResponse rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationFrameworkException {

        LOG.debug("Rollback is not supported for TaskExecutionNode.");
        return null;
    }
}
