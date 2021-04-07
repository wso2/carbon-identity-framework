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

package org.wso2.carbon.identity.template.mgt;

import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.List;

/**
 * Template manager service interface.
 *
 * @since 1.0.0
 */
public interface TemplateManager {

    /**
     * This method is used to add a new template using the TemplateMgtDAO implementation.
     *
     * @param template Template element.
     * @return Return template element with template name, description and script.
     * @throws TemplateManagementException Template Management Exception.
     * @deprecated use {@link #addTemplate(Template)} ()} instead.
     */
    @Deprecated
    Template addTemplateUsingTemplateMgtDAO(Template template) throws TemplateManagementException;

    /**
     * This method is used to get the template by template name and tenant ID.
     *
     * @param templateName Name of the template.
     * @return Template matching the input parameters.
     * @throws TemplateManagementException Template Management Exception.
     * @deprecated use {@link #getTemplateById(String)} ()} instead.
     */
    @Deprecated
    Template getTemplateByName(String templateName) throws TemplateManagementException;

    /**
     * This method is used to add a new Template.
     *
     * @param templateName Name of the updated template.
     * @param template     Template element.
     * @return Return the updated Template element.
     * @throws TemplateManagementException Template Management Exception.
     * @deprecated use {@link #updateTemplateById(String, Template)} ()} instead.
     */
    @Deprecated
    Template updateTemplate(String templateName, Template template) throws TemplateManagementException;

    /**
     * This method is used to delete existing template by template name.
     *
     * @param templateName Name of the template.
     * @throws TemplateManagementException Template Management Exception.
     * @deprecated use {@link #deleteTemplateById(String)} ()} instead.
     */
    @Deprecated
    TemplateInfo deleteTemplate(String templateName) throws TemplateManagementException;

    /**
     * This method is used to get the names and descriptions of all or filtered existing templates.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return Filtered list of TemplateInfo elements.
     * @throws TemplateManagementException Template Management Exception.
     * @deprecated use {@link #listTemplates(String, Integer, Integer)} instead.
     */
    List<TemplateInfo> listTemplates(Integer limit, Integer offset) throws TemplateManagementException;

    /**
     * This method is used to add a new template as a resource.
     *
     * @param template Template element.
     * @return unique identifier for the newly created template.
     * @throws TemplateManagementException Template Management Exception.
     */
    String addTemplate(Template template) throws TemplateManagementException;

    /**
     * Return the template given the template id.
     *
     * @param templateId unique identifier of the template.
     * @return template.
     * @throws TemplateManagementException if an error occurs while retrieving the template.
     */
    Template getTemplateById(String templateId) throws TemplateManagementException;

    /**
     * Update a template given the template id by replacing the existing template object.
     *
     * @param templateId unique identifier of the the template.
     * @param template   updated template object.
     * @throws TemplateManagementException if an error occurs while updating the template.
     */
    void updateTemplateById(String templateId, Template template) throws TemplateManagementException;

    /**
     * Delete a template given the template id.
     *
     * @param templateId unique identifier of the template.
     * @throws TemplateManagementException if an error occurs while deleting the template.
     */
    void deleteTemplateById(String templateId) throws TemplateManagementException;

    /**
     * List all the templates of a given type.
     *
     * @param templateType template type
     * @param limit        number of templates required for the list.
     * @param offset       offset of the list of templates.
     * @return list templates
     * @throws TemplateManagementException if an error occurs while retrieving the templates.
     * @deprecated use {@link #listTemplates(String, Integer, Integer, Condition)} instead.
     */
    List<Template> listTemplates(String templateType, Integer limit, Integer offset) throws TemplateManagementException;

    /**
     * List all the templates of a given type and search criteria.
     *
     * @param templateType template type
     * @param limit        number of templates required for the list.
     * @param offset       offset of the list of templates.
     * @param searchCondition   Search condition when listing templates.
     * @return list templates
     * @throws TemplateManagementException if an error occurs while retrieving the templates.
     */
    List<Template> listTemplates(String templateType, Integer limit, Integer offset, Condition
            searchCondition) throws TemplateManagementException;
}
