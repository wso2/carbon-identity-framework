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

package org.wso2.carbon.identity.user.registration.engine.graph;

import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;

/**
 * Implementation of node that prompts users to choose from multiple registration options.
 */
public class UserChoiceDecisionNode implements Node {

    private static final Log LOG = LogFactory.getLog(UserChoiceDecisionNode.class);

    @Override
    public String getName() {

        return DECISION;
    }

    @Override
    public Response execute(RegistrationContext context, NodeConfig config) throws RegistrationEngineException {

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
            return new Response.Builder().status(STATUS_COMPLETE).build();
        }

        return new Response.Builder()
                .status(STATUS_INCOMPLETE)
                .type(VIEW)
                .build();
    }

    @Override
    public Response rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationEngineException {

        LOG.debug("Rollback is not supported.");
        return null;
    }
}
