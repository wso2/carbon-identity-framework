/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.mgt;

import org.wso2.carbon.identity.flow.mgt.exception.FlowMgtFrameworkException;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

/**
 * Interceptor interface for flow update operations.
 * <p>
 * Implementations can hook into the flow update lifecycle to perform additional
 * processing when a flow is updated. Interceptors are invoked <b>before</b> the
 * graph is persisted to the database, allowing them to:
 * <ul>
 *   <li>Extract data from executor metadata for external storage (e.g., access config overrides
 *       stored as action properties instead of executor metadata).</li>
 *   <li>Strip extracted keys from executor metadata so they are <b>not</b> persisted to the
 *       flow executor metadata table (e.g., to avoid column size limitations).</li>
 * </ul>
 * </p>
 */
public interface FlowUpdateInterceptor {

    /**
     * Called after the flow graph has been built but <b>before</b> it is persisted to the database.
     * Implementations may modify the {@code graphConfig} (e.g., stripping metadata keys) and the
     * modifications will be reflected in the persisted graph.
     *
     * @param flowType    The flow type (e.g., "REGISTRATION", "LOGIN").
     * @param graphConfig The built graph configuration containing node and executor details.
     *                    Mutable — interceptors may modify executor metadata.
     * @param tenantId    The tenant ID.
     * @throws FlowMgtFrameworkException If an error occurs during interception.
     */
    void onFlowUpdate(String flowType, GraphConfig graphConfig, int tenantId) throws FlowMgtFrameworkException;
}
