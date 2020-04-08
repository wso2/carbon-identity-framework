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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.template.mgt.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.dao.TemplateManagerDAO;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementServerException;
import org.wso2.carbon.identity.template.mgt.internal.TemplateManagerComponent;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.*;
import java.util.Map.Entry;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_FILE_BASED_NOT_SUPPORTED;

/**
 * Template management DAO which reads templates from file system.
 * Modifications (Create, Update, Delete) operations are not supported.
 * Use this DAO to load default templates as immutable artifact.
 */
public class FileBasedTemplateMgtDAO implements TemplateManagerDAO {

    @Override
    public Template addTemplate(Template template) throws TemplateManagementException {
        throw new TemplateManagementServerException(ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getMessage(),
                ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getCode());
    }

    @Override
    public Template getTemplateByName(String templateName, Integer tenantId) throws TemplateManagementException {
        Template template = null;
        Map<TemplateMgtConstants.TemplateType, Map<String, Template>> templateMap = TemplateManagerComponent.getFileBasedTemplates();
        for (Iterator<Entry<TemplateMgtConstants.TemplateType, Map<String, Template>>> iterator = templateMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Map<String, Template> categorizedTemplateMap = iterator.next().getValue();
            for (Iterator<Entry<String, Template>> nextIterator = categorizedTemplateMap.entrySet().iterator(); nextIterator
                    .hasNext(); ) {
                Entry<String, Template> entry = nextIterator.next();
                if (StringUtils.equals(templateName, entry.getValue().getTemplateName())) {
                    template = entry.getValue();
                    break;
                }
            }
        }
        return template;
    }

    @Override
    public List<TemplateInfo> getAllTemplates(Integer tenantId, Integer limit, Integer offset) throws TemplateManagementException {
        Map<TemplateMgtConstants.TemplateType, Map<String, Template>> templateMap = TemplateManagerComponent.getFileBasedTemplates();
        List<TemplateInfo> templateInfo = new ArrayList<>();
        for (Iterator<Entry<TemplateMgtConstants.TemplateType, Map<String, Template>>> iterator = templateMap.entrySet().iterator(); iterator
                .hasNext(); ) {
            Map<String, Template> categorizedTemplateMap = iterator.next().getValue();
            for (Iterator<Entry<String, Template>> nextIterator = categorizedTemplateMap.entrySet().iterator(); nextIterator
                    .hasNext(); ) {
                Entry<String, Template> entry = nextIterator.next();
                TemplateInfo tempInfo = new TemplateInfo(entry.getValue().getTemplateName(), entry.getValue()
                        .getDescription());
                templateInfo.add(tempInfo);
            }
        }
        return templateInfo;
    }

    @Override
    public Template updateTemplate(String templateName, Template newTemplate) throws TemplateManagementServerException {
        throw new TemplateManagementServerException(ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getMessage(),
                ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getCode());
    }

    @Override
    public TemplateInfo deleteTemplate(String templateName, Integer tenantId) throws TemplateManagementException {
        throw new TemplateManagementServerException(ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getMessage(),
                ERROR_CODE_FILE_BASED_NOT_SUPPORTED.getCode());
    }
}
