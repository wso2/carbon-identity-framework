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

package org.wso2.carbon.identity.user.registration.mgt.dao;

import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationFrameworkException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationFlowDTO;
import org.wso2.carbon.identity.user.registration.mgt.model.RegistrationGraphConfig;

/**
 * DAO interface for registration flow management.
 */
public interface RegistrationFlowDAO {

    /**
     * Update the default registration flow of the given tenant.
     *
     * @param regFlowConfig The registration flow.
     * @param tenantId      The tenant ID.
     * @param flowName      The flow name.
     * @throws RegistrationFrameworkException If an error occurs while updating the default flow.
     */
    void updateDefaultRegistrationFlowByTenant(RegistrationGraphConfig regFlowConfig, int tenantId, String flowName)
            throws RegistrationFrameworkException;

    /**
     * Get the default registration flow of the given tenant.
     *
     * @param tenantId The tenant ID.
     * @return The registration flow.
     * @throws RegistrationServerException If an error occurs while retrieving the default flow.
     */
    RegistrationFlowDTO getDefaultRegistrationFlowByTenant(int tenantId) throws RegistrationServerException;

    /**
     * Get the default registration graph of the given tenant.
     *
     * @param tenantId The tenant ID.
     * @return The registration graph.
     * @throws RegistrationServerException If an error occurs while retrieving the flow.
     */
    RegistrationGraphConfig getDefaultRegistrationGraphByTenant(int tenantId) throws RegistrationFrameworkException;
}
