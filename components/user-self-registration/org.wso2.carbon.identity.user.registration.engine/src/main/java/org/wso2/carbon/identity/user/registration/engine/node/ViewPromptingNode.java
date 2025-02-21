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

import static org.wso2.carbon.identity.user.registration.engine.util.Constants.STATUS_PROMPT_ONLY;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.Constants;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.engine.model.NodeResponse;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;

/**
 * Node that would prompt a view associated with the node.
 */
public class ViewPromptingNode implements Node {

    private static final Log LOG = LogFactory.getLog(ViewPromptingNode.class);

    @Override
    public String getName() {

        return Constants.NodeTypes.PROMPT;
    }

    @Override
    public NodeResponse execute(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationFrameworkException {

        return new NodeResponse(STATUS_PROMPT_ONLY);
    }

    @Override
    public NodeResponse rollback(RegistrationContext context, NodeConfig nodeConfig)
            throws RegistrationFrameworkException {

        LOG.debug("Rollback is not supported for TaskExecutionNode.");
        return null;
    }
}
