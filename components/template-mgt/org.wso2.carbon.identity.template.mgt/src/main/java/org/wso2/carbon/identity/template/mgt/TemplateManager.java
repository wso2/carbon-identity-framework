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
     * This method is used to add a new template.
     *
     * @param template Template element.
     * @return Return template element with template name, description and script.
     * @throws TemplateManagementException Template Management Exception.
     */
    Template addTemplate(Template template) throws TemplateManagementException;

    /**
     * This method is used to get the template by template name and tenant ID.
     *
     * @param templateName Name of the template.
     * @return Template matching the input parameters.
     * @throws TemplateManagementException Template Management Exception.
     */
    Template getTemplateByName(String templateName) throws TemplateManagementException;

    /**
     * This method is used to add a new Template.
     *
     * @param templateName Name of the updated template.
     * @param template     Template element.
     * @return Return the updated Template element.
     * @throws TemplateManagementException Template Management Exception.
     */
    Template updateTemplate(String templateName, Template template) throws TemplateManagementException;

    /**
     * This method is used to delete existing template by template name.
     *
     * @param templateName Name of the template.
     * @throws TemplateManagementException Template Management Exception.
     */
    TemplateInfo deleteTemplate(String templateName) throws TemplateManagementException;

    /**
     * This method is used to get the names and descriptions of all or filtered existing templates.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return Filtered list of TemplateInfo elements.
     * @throws TemplateManagementException Template Management Exception.
     */
    List<TemplateInfo> listTemplates(Integer limit, Integer offset) throws TemplateManagementException;

}
