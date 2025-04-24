/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.NotImplementedException;
import org.wso2.carbon.identity.cors.mgt.core.exception.CORSManagementServiceServerException;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSApplication;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSOrigin;

import java.util.List;

/**
 * Perform CRUD operations for {@link CORSOrigin}.
 */
public interface CORSOriginDAO {

    /**
     * Get priority value for the {@link CORSOriginDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Get the CORS origins by tenant ID.
     *
     * @param tenantId The tenant ID.
     * @return List of CORS origins belonging to the tenant.
     * @throws CORSManagementServiceServerException
     */
    List<CORSOrigin> getCORSOriginsByTenantId(int tenantId)
            throws CORSManagementServiceServerException;

    /**
     * Get the CORS origins by tenant domain.
     *
     * @param tenantDomain The tenant domain.
     * @return List of CORS origins belonging to the tenant.
     * @throws CORSManagementServiceServerException
     */
    default List<CORSOrigin> getCORSOriginsByTenantDomain(String tenantDomain)
            throws CORSManagementServiceServerException {

        throw new NotImplementedException();
    }

    /**
     * Get the CORS origins of a tenant that are associated with a specific application by application ID.
     *
     * @param applicationId The application ID.
     * @param tenantId      The tenant ID.
     * @return List of CORS origins that are associated with the application.
     * @throws CORSManagementServiceServerException
     */
    List<CORSOrigin> getCORSOriginsByApplicationId(int applicationId, int tenantId)
            throws CORSManagementServiceServerException;

    /**
     * Set the CORS origins of an application. This will replace the existing CORS origin list of that application.
     *
     * @param applicationId The application ID.
     * @param corsOrigins   The CORS origins to be set, associated with the application.
     * @param tenantId      The tenant ID.
     * @throws CORSManagementServiceServerException
     */
    void setCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException;

    /**
     * Add the CORS origins of an application. This will append the new origins to the existing CORS origin list
     * of that application.
     *
     * @param applicationId The application ID.
     * @param corsOrigins   The CORS origins to be add, associated with the application.
     * @param tenantId      The tenant ID.
     * @throws CORSManagementServiceServerException
     */
    void addCORSOrigins(int applicationId, List<CORSOrigin> corsOrigins, int tenantId)
            throws CORSManagementServiceServerException;

    /**
     * Delete the CORS origins of an application.
     *
     * @param applicationId The application ID.
     * @param corsOriginIds The CORS origins to be deleted, associated with the application.
     * @param tenantId      The tenant ID.
     * @throws CORSManagementServiceServerException
     */
    void deleteCORSOrigins(int applicationId, List<String> corsOriginIds, int tenantId)
            throws CORSManagementServiceServerException;

    /**
     * Get a list of the applications associated with a particular CORS origin.
     *
     * @param corsOriginId The CORS origin ID.
     * @return A list of the associated referenced to the CORS origin.
     * @throws CORSManagementServiceServerException
     */
    List<CORSApplication> getCORSOriginApplications(String corsOriginId)
            throws CORSManagementServiceServerException;
}
