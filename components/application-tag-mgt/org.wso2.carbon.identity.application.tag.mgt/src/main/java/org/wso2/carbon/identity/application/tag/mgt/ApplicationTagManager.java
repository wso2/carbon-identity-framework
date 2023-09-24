/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.tag.mgt;

import org.wso2.carbon.identity.application.common.model.ApplicationTag;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsItem;
import org.wso2.carbon.identity.application.common.model.ApplicationTagsListItem;

import java.util.List;

/**
 * Application Tag Manager Interface.
 */
public interface ApplicationTagManager {

    /**
     * Add Application Tag.
     *
     * @param applicationTagDTO Application Tag Model.
     * @param tenantDomain      Tenant Domain.
     * @return Tag Id.
     * @throws ApplicationTagMgtException If an error occurs while creating the Application tag.
     */
    ApplicationTagsItem createApplicationTag(ApplicationTag applicationTagDTO, String tenantDomain)
            throws ApplicationTagMgtException;

    /**
     * Get All Application Tags
     *
     * @param tenantDomain Tenant Domain.
     * @param offset    Offset for pagination.
     * @param limit     Limit.
     * @param filter    Filter query.
     * @return Application Tag List object.
     * @throws ApplicationTagMgtException If an error occurs while retrieving the Application tag by Id.
     */
    List<ApplicationTagsListItem> getAllApplicationTags(String tenantDomain, Integer offset, Integer limit,
                                                        String filter) throws ApplicationTagMgtException;

    /**
     * Get Application Tag.
     *
     * @param applicationTagId Application tag id.
     * @param tenantDomain     Tenant Domain.
     * @return Application Tag object
     * @throws ApplicationTagMgtException If an error occurs while retrieving the Application tag by Id.
     */
    ApplicationTagsItem getApplicationTagById(String applicationTagId, String tenantDomain)
            throws ApplicationTagMgtException;

    /**
     * Delete the Application Tag for the given id.
     *
     * @param applicationTagId Application tag id.
     * @param tenantDomain     Tenant Domain.
     * @throws ApplicationTagMgtException If an error occurs while deleting the Application Tag.
     */
    void deleteApplicationTagById(String applicationTagId, String tenantDomain) throws ApplicationTagMgtException;

    /**
     * Update the Application Tag for the given id.
     *
     * @param applicationTagPatch Application Tag Patch Object.
     * @param applicationTagId    Application tag id.
     * @param tenantDomain        Tenant Domain.
     * @throws ApplicationTagMgtException If an error occurs while updating the Application Tag.
     */
    void updateApplicationTag(ApplicationTag applicationTagPatch, String applicationTagId, String tenantDomain)
            throws ApplicationTagMgtException;

    /**
     * Get count of Application Tags matching the filter.
     *
     * @param filter        Application Tag search filter.
     * @param tenantDomain      Tenant Domain.
     * @return Matched Application Tags count in an int value.
     * @throws ApplicationTagMgtException If an error occurs while retrieving the Application Tags count.
     */
    int getCountOfApplicationTags(String filter, String tenantDomain) throws ApplicationTagMgtException;
}
