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
     * @param tenantDomain The tenant domain.
     * @param appId        The  application ID that the CORS origin(s) belongs to.
     * @return List<CORSOrigin> Returns a list of CORS Origins configured by the tenant as Origin objects.
     * @throws CORSManagementServiceException
     */
    List<CORSOrigin> getApplicationCORSOrigins(String tenantDomain, String appId) throws CORSManagementServiceException;

    /**
     * Set the CORS Origins for a tenant. This method replaces any existing origins.
     *
     * @param tenantDomain The tenant domain.
     * @param origins      A list of CORS origins to be set.
     * @throws CORSManagementServiceException
     */
    void setTenantCORSOrigins(String tenantDomain, List<String> origins) throws CORSManagementServiceException;

    /**
     * Set the CORS Origins for an application. This method replaces any existing origins.
     *
     * @param tenantDomain The tenant domain.
     * @param appId        The  application ID that the CORS origin(s) belongs to.
     * @param origins      A list of CORS origins to be set.
     * @throws CORSManagementServiceException
     */
    void setApplicationCORSOrigins(String tenantDomain, String appId, List<String> origins)
            throws CORSManagementServiceException;

    /**
     * Add the CORS Origin(s) to the existing CORS Origin list of the tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param origins      A list of CORS origins to be added.
     * @throws CORSManagementServiceException
     */
    void addTenantCORSOrigins(String tenantDomain, List<String> origins) throws CORSManagementServiceException;

    /**
     * Add the CORS Origin(s) to the existing CORS Origin list of the application.
     *
     * @param tenantDomain The tenant domain.
     * @param appId        The  application ID that the CORS origin(s) belongs to.
     * @param origins      A list of CORS origins to be added.
     * @throws CORSManagementServiceException
     */
    void addApplicationCORSOrigins(String tenantDomain, String appId, List<String> origins)
            throws CORSManagementServiceException;

    /**
     * Delete the CORS Origin(s) from the existing CORS Origin list of the tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param originIds    A list of CORS origin IDs to be deleted.
     * @throws CORSManagementServiceException
     */
    void deleteTenantCORSOrigins(String tenantDomain, List<String> originIds) throws CORSManagementServiceException;

    /**
     * Delete the CORS Origin(s) from the existing CORS Origin list of the application.
     *
     * @param tenantDomain The tenant domain.
     * @param appId        The  application ID that the CORS origin(s) belongs to.
     * @param originIds    A list of CORS origin IDs to be deleted.
     * @throws CORSManagementServiceException
     */
    void deleteApplicationCORSOrigins(String tenantDomain, String appId, List<String> originIds)
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
     * @param tenantDomain      The tenant domain.
     * @param corsConfiguration The {@code CORSConfiguration} object to be set.
     * @throws CORSManagementServiceException
     */
    void setCORSConfiguration(String tenantDomain, CORSConfiguration corsConfiguration)
            throws CORSManagementServiceException;
}
