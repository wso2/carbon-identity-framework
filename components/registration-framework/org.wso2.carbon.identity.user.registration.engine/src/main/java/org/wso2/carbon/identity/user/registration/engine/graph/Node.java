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

import org.wso2.carbon.identity.user.registration.engine.exception.RegistrationEngineException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.model.NodeConfig;
import org.wso2.carbon.identity.user.registration.engine.model.Response;
import org.wso2.carbon.identity.user.registration.engine.model.RegistrationContext;

/**
 * Interface for a node in the registration flow graph.
 */
public interface Node {

    String getName();

    /**
     * Execute the node.
     *
     * @param context The registration context.
     * @return The response of the node.
     * @throws RegistrationEngineException If an error occurs while executing the node.
     */
    Response execute(RegistrationContext context, NodeConfig nodeConfig) throws RegistrationEngineException;

    /**
     * Rollback the functionality of the node.
     *
     * @param context The registration context.
     * @return The response of the node.
     */
    Response rollback(RegistrationContext context, NodeConfig nodeConfig) throws RegistrationEngineException;
}
