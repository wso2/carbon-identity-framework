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

import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;
import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import static org.wso2.carbon.identity.user.registration.engine.Constants.STATUS_PROMPT_ONLY;
import static org.wso2.carbon.identity.user.registration.mgt.Constants.NodeTypes.PROMPT_ONLY;

public class PagePromptNode implements Node {

    @Override
    public String getName() {

        return PROMPT_ONLY;
    }

    @Override
    public Response execute(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationEngineException {

        if (nodeConfig.getEdges() != null && !nodeConfig.getEdges().isEmpty()) {
            nodeConfig.setNextNodeId(nodeConfig.getEdges().get(0).getTargetNodeId());
        }
        return new Response.Builder().status(STATUS_PROMPT_ONLY).build();
    }

    @Override
    public Response rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationEngineException {

        return null;
    }
}
