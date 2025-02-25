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

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.ErrorMessages.ERROR_CODE_UNRESOLVED_NEXT_NODE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_COMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_INCOMPLETE;
import static org.wso2.carbon.identity.user.registration.engine.util.RegistrationFlowEngineUtils.handleClientException;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.DECISION;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.StepTypes.VIEW;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeEdge;

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
    public Response execute(RegistrationContext context, NodeConfig config) throws RegistrationFrameworkException {

        Map<String, String> inputData = context.getUserInputData();

        if (inputData != null && inputData.containsKey(NODE_INPUT)) {
            String triggeredAction = inputData.get(NODE_INPUT);
            if (triggeredAction != null) {
                for (NodeEdge edge : config.getEdges()) {
                    if (triggeredAction.equals(edge.getTriggeringActionId())) {
                        config.setNextNodeId(triggeredAction);
                        break;
                    }
                }
            }
            if (config.getNextNodeId() == null) {
                throw handleClientException(ERROR_CODE_UNRESOLVED_NEXT_NODE, config.getId(), context.getContextIdentifier());
            }
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
            throws RegistrationFrameworkException {

        LOG.debug("Rollback is not supported.");
        return null;
    }
}
