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

import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.metadata.FlowExecutorMetadata;
import org.wso2.carbon.identity.flow.execution.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.mgt.Constants.FlowTypes;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Interface for the executor.
 */
public interface Executor {

    /**
     * Get the unique name of the executor.
     *
     * @return Name of the executor.
     */
    String getName();

    /**
     * Execute the logic of the executor.
     *
     * @param context Flow context.
     * @return Executor response.
     * @throws FlowEngineException If an error occurs while executing the executor.
     */
    ExecutorResponse execute(FlowExecutionContext context) throws FlowEngineException;

    /**
     * Get the initiation data of the executor.
     *
     * @return List of initiation data.
     */
    List<String> getInitiationData();

    /**
     * Rollback the executor.
     *
     * @return List of completion data.
     */
    ExecutorResponse rollback(FlowExecutionContext context) throws FlowEngineException;

    /**
     * Declare supported flow types for dynamically registered executors.
     *
     * @return Set of supported flow types.
     */
    default Set<FlowTypes> getSupportedFlowTypes() {

        return Collections.emptySet();
    }

    /**
     * Get the metadata describing how the flow composer should present a dynamically registered executor.
     *
     * @return Executor metadata, or null to let the engine derive defaults from {@link #getName()}.
     */
    default FlowExecutorMetadata getExecutorMetadata() {

        return null;
    }
}
