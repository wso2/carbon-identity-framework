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

import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtServerException;
import org.wso2.carbon.identity.flow.mgt.model.FlowDTO;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

/**
 * DAO interface for flow management.
 */
public interface FlowDAO {

    /**
     * Update a specific flow of the given tenant.
     *
     * @param flowType      The flow type.
     * @param graphConfig The graph config.
     * @param tenantId      The tenant ID.
     * @param flowName      The flow name.
     * @throws FlowMgtFrameworkException If an error occurs while updating the flow.
     */
    void updateFlow(String flowType, GraphConfig graphConfig, int tenantId, String flowName)
            throws FlowMgtFrameworkException;

    /**
     * Get the specific flow of the given tenant.
     *
     * @param flowType The flow type.
     * @param tenantId The tenant ID.
     * @return The flow.
     * @throws FlowMgtServerException If an error occurs while retrieving the default flow.
     */
    FlowDTO getFlow(String flowType, int tenantId) throws FlowMgtServerException;

    /**
     * Delete the specific flow of the given tenant.
     *
     * @param flowType The flow type.
     * @param tenantId The tenant ID.
     * @throws FlowMgtFrameworkException If an error occurs while deleting the flow.
     */
    void deleteFlow(String flowType, int tenantId) throws FlowMgtFrameworkException;

    /**
     * Get the specific graph of the given tenant.
     *
     * @param flowType The flow type.
     * @param tenantId The tenant ID.
     * @return The graph config.
     * @throws FlowMgtServerException If an error occurs while retrieving the flow.
     */
    GraphConfig getGraphConfig(String flowType, int tenantId) throws FlowMgtFrameworkException;
}
