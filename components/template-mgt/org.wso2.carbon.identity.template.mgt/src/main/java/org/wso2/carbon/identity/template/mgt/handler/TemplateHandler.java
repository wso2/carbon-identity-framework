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

/**
 * Template handler interface.
 */
public interface TemplateHandler extends ReadOnlyTemplateHandler {

    /**
     * This method is used to add a new template as a resource.
     *
     * @param template Template element.
     * @return Unique identifier for the newly created template.
     * @throws TemplateManagementException Template Management Exception.
     */
    String addTemplate(Template template) throws TemplateManagementException;

    /**
     * Update a template given the template id by replacing the existing template object.
     *
     * @param templateId Unique identifier of the the template.
     * @param template   Updated template object.
     * @throws TemplateManagementException If an error occurs while updating the template.
     */
    void updateTemplateById(String templateId, Template template) throws TemplateManagementException;

    /**
     * Delete a template given the template id.
     *
     * @param templateId Unique identifier of the template.
     * @throws TemplateManagementException If an error occurs while deleting the template.
     */
    void deleteTemplateById(String templateId) throws TemplateManagementException;
}
