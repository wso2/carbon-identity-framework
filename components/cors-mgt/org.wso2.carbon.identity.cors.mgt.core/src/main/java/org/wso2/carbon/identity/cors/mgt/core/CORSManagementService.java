/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core;

import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.util.List;

/**
 * Service for managing the CORS Origins of a tenant.
 */
public interface CORSManagementService {

    /**
     * Get all the CORS Origins belonging to a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return List<CORSOrigins> Returns a list of CORS Origins configured by the tenant as CORSOrigin objects.
     * @throws CORSManagementServiceException
     */
    List<CORSOrigin> getCORSOrigins(String tenantDomain) throws CORSManagementServiceException;

    /**
     * Set the CORS Origins for a tenant. This method replaces any existing Origins.
     *
     * @param tenantDomain The tenant domain.
     * @param corsOrigins  A list of CORS Origins to be set.
     * @throws CORSManagementServiceException
     */
    void setCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins) throws CORSManagementServiceException;

    /**
     * Add the CORS Origin(s) to the existing CORS Origin list of the tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param corsOrigins  A list of CORS Origins to be added.
     * @throws CORSManagementServiceException
     */
    void addCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins) throws CORSManagementServiceException;

    /**
     * Delete the CORS Origin(s) from the existing CORS Origin list of the tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param corsOrigins  A list of CORS Origins to be deleted.
     * @throws CORSManagementServiceException
     */
    void deleteCORSOrigins(String tenantDomain, List<CORSOrigin> corsOrigins) throws CORSManagementServiceException;
}
