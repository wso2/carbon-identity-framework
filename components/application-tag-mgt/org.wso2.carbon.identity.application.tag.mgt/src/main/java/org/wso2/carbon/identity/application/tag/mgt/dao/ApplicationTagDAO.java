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

package org.wso2.carbon.identity.application.tag.mgt.dao;

import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagPOST;
import org.wso2.carbon.identity.application.tag.common.model.ApplicationTagsListItem;
import org.wso2.carbon.identity.application.tag.mgt.ApplicationTagMgtException;

import java.util.List;

/**
 * This interface access the data storage layer to store/update and delete application tag configurations.
 */
public interface ApplicationTagDAO {

    /**
     * @param applicationTagDTO Application Tag Model.
     * @param tenantDomain      Tenant Domain.
     * @return Tag Id.
     * @throws ApplicationTagMgtException If an error occurs while creating the Application tag.
     */
    String createApplicationTag(ApplicationTagPOST applicationTagDTO, String tenantDomain)
            throws ApplicationTagMgtException;

    /**
     * Get all application tags when the tags reside in the same tenant of the request initiated.
     *
     * @param tenantDomain Tenant Domain.
     * @return Application Tag object
     * @throws ApplicationTagMgtException If an error occurs while retrieving the Application tag by Id.
     */
    List<ApplicationTagsListItem> getAllApplicationTags(String tenantDomain) throws ApplicationTagMgtException;

    /**
     * Get application tag when the tag resides in the same tenant of the request initiated.
     *
     * @param applicationTagId Application tag id.
     * @param tenantDomain     Tenant Domain.
     * @return Application Tag object
     * @throws ApplicationTagMgtException If an error occurs while retrieving the Application tag by Id.
     */
    ApplicationTagsListItem getApplicationTagById(String applicationTagId, String tenantDomain)
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
    void updateApplicationTag(ApplicationTagPOST applicationTagPatch, String applicationTagId, String tenantDomain)
            throws ApplicationTagMgtException;
}
