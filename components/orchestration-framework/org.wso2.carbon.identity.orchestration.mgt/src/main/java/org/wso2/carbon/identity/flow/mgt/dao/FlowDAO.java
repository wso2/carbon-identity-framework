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

package org.wso2.carbon.identity.flow.mgt.dao;

import org.wso2.carbon.identity.flow.mgt.exception.OrchestrationFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.OrchestrationServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

/**
 * DAO interface for flow management.
 */
public interface FlowDAO {

    /**
     * Update the specified flow of the given tenant.
     *
     * @param flowConfig The flow.
     * @param tenantId The tenant ID.
     * @param flowName The flow name.
     * @param flowType The type of the flow.
     * @param flowCategory The category of the flow.
     * @param isDefaultFlow Is flow default or not.
     * @throws OrchestrationFrameworkException If an error occurs while updating the flow.
     */
    void updateFlow(GraphConfig flowConfig, int tenantId, String flowName, String flowType,
                    String flowCategory, boolean isDefaultFlow)
            throws OrchestrationFrameworkException;

    /**
     * Get the specified flow of the given tenant.
     *
     * @param tenantId The tenant ID.
     * @param flowType The type of the flow.
     * @return The flow.
     * @throws OrchestrationServerException If an error occurs while retrieving the default flow.
     */
    FlowDTO getFlow(int tenantId, String flowType) throws OrchestrationServerException;

    /**
     * Get the specified graph of the given tenant.
     *
     * @param tenantId The tenant ID.
     * @param flowType The type of the flow.
     * @return The graph.
     * @throws OrchestrationServerException If an error occurs while retrieving the flow.
     */
    GraphConfig getGraph(int tenantId, String flowType) throws OrchestrationFrameworkException;
}
