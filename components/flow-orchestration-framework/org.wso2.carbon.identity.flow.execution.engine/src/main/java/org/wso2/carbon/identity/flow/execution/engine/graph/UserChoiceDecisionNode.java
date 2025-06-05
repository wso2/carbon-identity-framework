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

package org.wso2.carbon.identity.flow.execution.engine.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.model.NodeResponse;
import org.wso2.carbon.identity.flow.mgt.model.NodeConfig;
import org.wso2.carbon.identity.flow.mgt.model.NodeEdge;

import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.flow.execution.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.flow.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.flow.mgt.Constants.StepTypes.VIEW;

/**
 * Implementation of node that prompts users to choose from multiple flow options.
 */
public class UserChoiceDecisionNode implements Node {

    private static final Log LOG = LogFactory.getLog(UserChoiceDecisionNode.class);

    @Override
    public String getName() {

        return DECISION;
    }

    @Override
    public NodeResponse execute(FlowExecutionContext context, NodeConfig config) throws FlowEngineException {

        String triggeredAction = context.getCurrentActionId();
        if (triggeredAction != null) {
            for (NodeEdge edge : config.getEdges()) {
                if (context.getCurrentActionId().equals(edge.getTriggeringActionId())) {
                    config.setNextNodeId(edge.getTargetNodeId());
                    break;
                }
            }
            context.setCurrentActionId(null);
        }
        if (config.getNextNodeId() != null) {
            return new NodeResponse.Builder().status(STATUS_COMPLETE).build();
        }

        return new NodeResponse.Builder()
                .status(STATUS_INCOMPLETE)
                .type(VIEW)
                .build();
    }

    @Override
    public NodeResponse rollback(FlowExecutionContext context, NodeConfig nodeConfig)
            throws FlowEngineException {

        LOG.debug("Rollback is not supported.");
        return null;
    }
}
