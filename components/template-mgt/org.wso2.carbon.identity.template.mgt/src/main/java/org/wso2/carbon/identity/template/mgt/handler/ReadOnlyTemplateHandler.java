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

import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.List;

/**
 * Read only template handler interface.
 */
public interface ReadOnlyTemplateHandler {

    /**
     * Return the template given the template id.
     *
     * @param templateId Unique identifier of the template.
     * @return Template.
     * @throws TemplateManagementException If an error occurs while retrieving the template.
     */
    Template getTemplateById(String templateId) throws TemplateManagementException;

    /**
     * List all the templates of a given type.
     *
     * @param templateType Template type.
     * @param limit  Number of templates required for the list.
     * @param offset Offset of the list of templates.
     * @param searchCondition Filtering conditions.
     * @return List templates.
     * @throws TemplateManagementException If an error occurs while retrieving the templates.
     */
    List<Template> listTemplates(String templateType, Integer limit, Integer offset, Condition
            searchCondition) throws TemplateManagementException;
}
