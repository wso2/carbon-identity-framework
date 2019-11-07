/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;

import java.util.List;

/**
 * Includes operations for applications discoverable in user portal.
 */
public interface DiscoverableApplicationManager {

    /**
     * Returns basic application information of applications that are flagged as discoverable in the given tenant
     * matching given criteria.
     *
     * @param limit        Maximum no of applications to be returned in the result set.
     * @param offset       Zero based index of the first application to be returned in the result set.
     * @param filter       Filter to search for applications.
     * @param sortOrder    Sort order, ascending or descending.
     * @param sortBy       Attribute to sort from.
     * @param tenantDomain Tenant domain to be filtered from.
     * @return List of ApplicationBasicInfo of applications matching the given criteria.
     * @throws IdentityApplicationManagementException
     */
    List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String filter,
                                                                   String sortOrder, String sortBy, String
                                                                           tenantDomain) throws
            IdentityApplicationManagementException;

    /**
     * Returns basic application information of the application matching given resource Id if discoverable.
     *
     * @param resourceId   Unique resource identifier of the application.
     * @param tenantDomain Tenant domain of the application.
     * @return ApplicationBasicInfo including application basic information.
     * @throws IdentityApplicationManagementException
     */
    ApplicationBasicInfo getDiscoverableApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Returns if application matching given resource Id in given tenant is discoverable.
     *
     * @param resourceId   Unique resource identifier of the application.
     * @param tenantDomain Tenant domain of the application.
     * @return True if application is flagged as discoverable, false otherwise.
     * @throws IdentityApplicationManagementException
     */
    boolean isApplicationDiscoverable(String resourceId, String tenantDomain) throws
            IdentityApplicationManagementException;

    /**
     * Returns the count of discoverable applications matching given filter.
     *
     * @param filter       Filter to search for applications (optional).
     * @param tenantDomain
     * @return Count of discoverable applications matching given filter.
     * @throws IdentityApplicationManagementException
     */
    int getCountOfDiscoverableApplications(String filter, String tenantDomain) throws
            IdentityApplicationManagementException;

}
