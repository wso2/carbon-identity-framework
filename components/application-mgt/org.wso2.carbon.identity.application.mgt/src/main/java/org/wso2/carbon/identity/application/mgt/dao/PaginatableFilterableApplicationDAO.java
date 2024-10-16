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

package org.wso2.carbon.identity.application.mgt.dao;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;

/**
 * This interface defines the data access layer API for application pagination and filtering.
 */
public interface PaginatableFilterableApplicationDAO extends ApplicationDAO {

    /**
     * Get all basic application information that falls under the given page number.
     *
     * @return an array of {@link ApplicationBasicInfo} instances within the given page.
     * @throws IdentityApplicationManagementException
     * @Deprecated The logic in pagination is improved to use an offset and a limit. Hence deprecating this method to
     * use {@link PaginatableFilterableApplicationDAO#getApplicationBasicInfo(int, int)} method.
     */
    @Deprecated
    ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(int pageNumber) throws
            IdentityApplicationManagementException;

    /**
     * Get all the basic application information based on the offset and the limit.
     *
     * @param offset Starting index of the count.
     * @param limit  Count value.
     * @return An array of {@link ApplicationBasicInfo} instances within the limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     * @Deprecated This is being deprecated as introducing excludeSystemPortals to exclude system portals in response.
     * use {@link PaginatableFilterableApplicationDAO#getApplicationBasicInfo(int, int, Boolean)}.
     */
    @Deprecated
    ApplicationBasicInfo[] getApplicationBasicInfo(int offset, int limit) throws IdentityApplicationManagementException;

    /**
     * Get all the basic application information based on the offset and the limit.
     *
     * @param offset               Starting index of the count.
     * @param limit                Count value.
     * @param excludeSystemPortals Exclude system portals.
     * @return An array of {@link ApplicationBasicInfo} instances within the limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     */
    default ApplicationBasicInfo[] getApplicationBasicInfo(int offset, int limit, Boolean excludeSystemPortals)
            throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Get all basic application information for a matching filter that falls under the given page number.
     *
     * @return an array of {@link ApplicationBasicInfo} instances matching the given filter within the given page.
     * @throws IdentityApplicationManagementException
     * @Deprecated The logic in pagination is improved to use an offset and a limit. Hence deprecating this method to
     * use {@link PaginatableFilterableApplicationDAO#getApplicationBasicInfo(String, int, int)} method.
     */
    @Deprecated
    ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(int pageNumber, String filter) throws
            IdentityApplicationManagementException;

    /**
     * Get all basic application information for a matching filter based on the offset and the limit.
     *
     * @param filter Application name filter.
     * @param offset Starting index of the count.
     * @param limit  Count value.
     * @return An array of {@link ApplicationBasicInfo} instances matching the given filter within the given limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information based on the
     *                                                given filter within the given limit.
     * @Deprecated This is being deprecated as introducing excludeSystemPortals to exclude system portals in response.
     * use {@link PaginatableFilterableApplicationDAO#getApplicationBasicInfo(String, int, int, Boolean)}.
     */
    @Deprecated
    ApplicationBasicInfo[] getApplicationBasicInfo(String filter, int offset, int limit) throws
            IdentityApplicationManagementException;

    /**
     * Get all basic application information for a matching filter based on the offset and the limit.
     *
     * @param filter               Application name filter.
     * @param offset               Starting index of the count.
     * @param limit                Count value.
     * @param excludeSystemPortals Exclude system portals
     * @return An array of {@link ApplicationBasicInfo} instances matching the given filter within the given limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information based on the
     *                                                given filter within the given limit.
     */
    default ApplicationBasicInfo[] getApplicationBasicInfo(String filter, int offset, int limit,
                                                           Boolean excludeSystemPortals)
            throws IdentityApplicationManagementException {

        return null;
    }

    /**
     * Get count of applications.
     *
     * @return application count in a int value
     * @throws IdentityApplicationManagementException
     */
    int getCountOfAllApplications() throws IdentityApplicationManagementException;

    /**
     * Get count of applications matching the filter.
     *
     * @param filter application search filter
     * @return matched application count in a int value
     * @throws IdentityApplicationManagementException
     * @Deprecated This is being deprecated as introducing excludeSystemPortals to exclude system portals in response.
     * use {@link PaginatableFilterableApplicationDAO#getCountOfApplications(String, Boolean)}.
     */
    @Deprecated
    int getCountOfApplications(String filter) throws IdentityApplicationManagementException;

    /**
     * Get count of applications matching the filter.
     *
     * @param filter               application search filter
     * @param excludeSystemPortals Exclude system portals
     * @return matched application count in a int value
     * @throws IdentityApplicationManagementException
     */
    default int getCountOfApplications(String filter, Boolean excludeSystemPortals) throws
            IdentityApplicationManagementException {

        return 0;
    }

    /**
     * Get all basic application information for a matching filter.
     *
     * @param filter Application name filter
     * @return Application Basic Information array
     * @throws IdentityApplicationManagementException
     */
    ApplicationBasicInfo[] getApplicationBasicInfo(String filter) throws IdentityApplicationManagementException;

}
