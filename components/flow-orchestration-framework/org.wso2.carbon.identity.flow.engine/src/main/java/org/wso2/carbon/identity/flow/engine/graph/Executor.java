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

package org.wso2.carbon.identity.flow.engine.graph;

import org.wso2.carbon.identity.flow.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.engine.model.ExecutorResponse;
import org.wso2.carbon.identity.flow.engine.model.FlowContext;

import java.util.List;

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
    ExecutorResponse execute(FlowContext context) throws FlowEngineException;

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
    default ExecutorResponse rollback(FlowContext context) throws FlowEngineException {

        return null;
    }
}
