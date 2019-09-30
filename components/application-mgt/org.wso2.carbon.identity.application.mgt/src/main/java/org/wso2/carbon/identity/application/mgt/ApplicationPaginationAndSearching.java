/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;

/**
 * Application pagination and searching API
 */
public interface ApplicationPaginationAndSearching {

    /**
     * Get all basic application information for a matching filter.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param filter       Application name filter
     * @return Application Basic Information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException;

    /**
     * Get All Application Basic Information with pagination
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param pageNumber   Number of the page
     * @return ApplicationBasicInfo[]
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     * @Deprecated The logic in pagination is improved to use an offset and a limit. Hence deprecating this method to
     * use {@link ApplicationPaginationAndSearching#getApplicationBasicInfo(String, String, int, int)} method.
     */
    @Deprecated
    ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(String tenantDomain, String username, int pageNumber)
            throws IdentityApplicationManagementException;

    /**
     * Get all the basic application information with pagination based on the offset and limit.
     *
     * @param tenantDomain Tenant Domain.
     * @param username     User name.
     * @param offset       Starting index of the count.
     * @param limit        Counting value.
     * @return An array of {@link ApplicationBasicInfo} instances within the limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     */
    ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, int offset, int limit)
            throws IdentityApplicationManagementException;

    /**
     * Get all basic application information for a matching filter with pagination.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param filter       Application name filter
     * @param pageNumber   Number of the page
     * @return Application Basic Information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     * @Deprecated The logic in pagination is improved to use an offset and a limit. Hence deprecating this method to
     * use {@link ApplicationPaginationAndSearching#getApplicationBasicInfo(String, String, String, int, int)} method.
     */
    @Deprecated
    ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(String tenantDomain, String username, int pageNumber, String filter)
            throws IdentityApplicationManagementException;

    /**
     * Get all basic application information for a matching filter with pagination based on the offset and limit.
     *
     * @param tenantDomain Tenant Domain.
     * @param username     User name.
     * @param filter       Application name filter.
     * @param offset       Starting index of the count.
     * @param limit        Counting value.
     * @return An array of {@link ApplicationBasicInfo} instances matching the given filter within the given limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     */
    ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter, int offset,
                                                   int limit) throws IdentityApplicationManagementException;

    /**
     * Get count of all Application Basic Information.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @return int
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    int getCountOfAllApplications(String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Get count of all basic application information for a matching filter.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param filter       Application name filter
     * @return int
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    int getCountOfApplications(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException;

}
