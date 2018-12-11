/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.template.mgt.dao;

import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementSQLException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementServerException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.List;

/**
 * Perform CRUD operations for {@link Template}.
 *
 * @since 1.0.0
 */
public interface TemplateManagerDAO {

    /**
     * Add a {@link Template}.
     *
     * @param template {@link Template} to insert.
     * @return Inserted {@link Template}.
     * @throws TemplateManagementException If error occurs while adding the {@link Template}.
     */
    Template addTemplate(Template template) throws TemplateManagementException;

    /**
     * Retrieve {@link Template} by template name and tenant Id.
     *
     * @param templateName name of the {@link Template} to retrieve.
     * @param tenantId     tenant Id of the tenant which the {@link Template} resides.
     * @return {@link Template} for the given name and tenant Id.
     * @throws TemplateManagementException If error occurs while retrieving {@link Template}.
     */
    Template getTemplateByName(String templateName, Integer tenantId) throws TemplateManagementException;

    /**
     * List {@link TemplateInfo} items for a given search criteria.
     *
     * @param tenantId Tenant Id to be searched.
     * @param limit    Maximum number of results expected.
     * @param offset   Result offset.
     * @return List of {@link TemplateInfo} entries.
     * @throws TemplateManagementException If error occurs while searching the Templates.
     */
    List<TemplateInfo> getAllTemplates(Integer tenantId, Integer limit, Integer offset)
            throws TemplateManagementException;

    /**
     * Update a {@link Template}.
     *
     * @param templateName name of the to be updated {@link Template}.
     * @param newTemplate  new {@link Template} to insert.
     * @return Inserted {@link Template}.
     * @throws TemplateManagementException If error occurs while adding the {@link Template}.
     */
    Template updateTemplate(String templateName, Template newTemplate) throws TemplateManagementServerException;

    /**
     * Delete {@link Template} for a given template name and a tenant Id.
     *
     * @param templateName name of the {@link Template} to be deleted.the tenant.
     * @param tenantId     tenant Id of the tenant which the {@link Template} resides.
     * @return TemplateInfo of the deleted {@link Template}.
     * @throws TemplateManagementException If error occurs while deleting the {@link Template}.
     */
    TemplateInfo deleteTemplate(String templateName, Integer tenantId) throws TemplateManagementException;
}
