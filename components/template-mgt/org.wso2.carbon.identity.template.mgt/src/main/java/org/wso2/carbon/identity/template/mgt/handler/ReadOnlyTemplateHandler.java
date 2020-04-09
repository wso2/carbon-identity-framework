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

package org.wso2.carbon.identity.template.mgt.handler;

import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.List;

public interface ReadOnlyTemplateHandler {

    /**
     * Return the template given the template id.
     *
     * @param templateId unique identifier of the template.
     * @return template.
     * @throws TemplateManagementException if an error occurs while retrieving the template.
     */
    Template getTemplateById(String templateId) throws TemplateManagementException;

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
     * List all the templates of a given type.
     *
     * @param templateType template type
     * @param limit        number of templates required for the list.
     * @param offset       offset of the list of templates.
     * @return list templates
     * @throws TemplateManagementException if an error occurs while retrieving the templates.
     */
    List<Template> listTemplates(String templateType, Integer limit, Integer offset) throws TemplateManagementException;
}
