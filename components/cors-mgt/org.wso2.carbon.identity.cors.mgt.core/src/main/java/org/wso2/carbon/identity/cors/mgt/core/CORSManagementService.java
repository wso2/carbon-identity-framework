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
import org.wso2.carbon.identity.cors.mgt.core.model.CORSApplication;
import org.wso2.carbon.identity.cors.mgt.core.model.CORSConfiguration;
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
     * @return List<CORSOrigin> Returns a list of CORS Origins configured by the tenant as Origin objects.
     * @throws CORSManagementServiceException
     */
    List<CORSOrigin> getTenantCORSOrigins(String tenantDomain) throws CORSManagementServiceException;

    /**
     * Get all the CORS Origins belonging to an application.
     *
     * @param applicationId The  application ID that the CORS origin(s) belongs to.
     * @param tenantDomain  The tenant domain.
     * @return List<CORSOrigin> Returns a list of CORS Origins configured by the tenant as Origin objects.
     * @throws CORSManagementServiceException
     */
    List<CORSOrigin> getApplicationCORSOrigins(String applicationId, String tenantDomain)
            throws CORSManagementServiceException;

    /**
     * Set the CORS Origins. This method replaces any existing origins.
     *
     * @param applicationId The  application ID that the CORS origin(s) belongs to.
     * @param origins       A list of CORS origins to be set.
     * @param tenantDomain  The tenant domain.
     * @throws CORSManagementServiceException
     */
    void setCORSOrigins(String applicationId, List<String> origins, String tenantDomain)
            throws CORSManagementServiceException;

    /**
     * Add the CORS Origin(s) to the existing CORS Origins of a tenant.
     *
     * @param applicationId The  application ID that the CORS origin(s) belongs to.
     * @param origins       A list of CORS origins to be added.
     * @param tenantDomain  The tenant domain.
     * @throws CORSManagementServiceException
     */
    void addCORSOrigins(String applicationId, List<String> origins, String tenantDomain)
            throws CORSManagementServiceException;

    /**
     * Delete the CORS Origin(s) from the existing CORS Origin list of the application.
     *
     * @param applicationId The  application ID that the CORS origin(s) belongs to.
     * @param originIds     A list of CORS origin IDs to be deleted.
     * @param tenantDomain  The tenant domain.
     * @throws CORSManagementServiceException
     */
    void deleteCORSOrigins(String applicationId, List<String> originIds, String tenantDomain)
            throws CORSManagementServiceException;

    /**
     * Returns a list of the applications associated with a particular CORS origin.
     *
     * @param corsOriginId The ID of the CORS origin resource.
     * @param tenantDomain The tenant domain.
     * @return The applications that the CORS origin is associated with.
     * @throws CORSManagementServiceException
     */
    List<CORSApplication> getCORSApplicationsByCORSOriginId(String corsOriginId, String tenantDomain)
            throws CORSManagementServiceException;

    /**
     * Get the CORS configurations of a tenant.
     *
     * @param tenantDomain The tenant domain.
     * @return CORSConfiguration Returns an instance of {@code CORSConfiguration} belonging to the tenant.
     * @throws CORSManagementServiceException
     */
    CORSConfiguration getCORSConfiguration(String tenantDomain) throws CORSManagementServiceException;

    /**
     * Set the CORS configurations of a tenant.
     *
     * @param corsConfiguration The {@code CORSConfiguration} object to be set.
     * @param tenantDomain      The tenant domain.
     * @throws CORSManagementServiceException
     */
    void setCORSConfiguration(CORSConfiguration corsConfiguration, String tenantDomain)
            throws CORSManagementServiceException;
}
