/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.dao;

import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;

/**
 * Perform CRUD operations for {@link CORSConfiguration}.
 */
public interface CORSConfigurationDAO {

    /**
     * Get priority value for the {@link CORSConfigurationDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Get the CORS configuration of a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return
     * @throws CORSManagementServiceServerException
     */
    CORSConfiguration getCORSConfigurationByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException;

    /**
     * Set the CORS configuration of a tenant.
     *
     * @param corsConfiguration The new CORS configuration to be set.
     * @param tenantDomain      The tenant domain.
     * @throws CORSManagementServiceServerException
     */
    void setCORSConfigurationByTenantDomain(CORSConfiguration corsConfiguration, String tenantDomain)
            throws CORSManagementServiceServerException;
}
