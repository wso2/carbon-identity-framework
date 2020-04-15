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

package org.wso2.carbon.identity.template.mgt.handler.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.handler.ReadOnlyTemplateHandler;
import org.wso2.carbon.identity.template.mgt.internal.TemplateManagerDataHolder;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_OFFSET;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.handleClientException;

/**
 * File based template handler implementation.
 */
public class FileBasedTemplateHandler implements ReadOnlyTemplateHandler {

    private static final Log log = LogFactory.getLog(FileBasedTemplateHandler.class);
    private static final Integer DEFAULT_SEARCH_OFFSET = 0;

    @Override
    public Template getTemplateById(String templateId) throws TemplateManagementException {

        return TemplateManagerDataHolder.getInstance().getFileBasedTemplates().get(templateId);
    }

    @Override
    public List<Template> listTemplates(String templateType, Integer limit, Integer offset, Condition
            searchCondition) throws TemplateManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == null || limit == 0) {
            limit = IdentityUtil.getDefaultItemsPerPage();
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined in the request, default to: " + limit);
            }
        }

        if (offset == null) {
            offset = DEFAULT_SEARCH_OFFSET;
        }

        return TemplateManagerDataHolder.getInstance().getFileBasedTemplates().entrySet().stream()
                .filter(entry -> StringUtils.equals(entry.getValue().getTemplateType().toString(), (templateType)))
                .skip(offset)
                .limit(limit)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * This method is used to validate the pagination parameters.
     *
     * @param limit  Limits the number of templates listed on a page.
     * @param offset Specifies the starting point for the templates to be displayed.
     * @throws TemplateManagementException Template Management Exception.
     */
    private void validatePaginationParameters(Integer limit, Integer offset) throws TemplateManagementClientException {

        if (limit != null && limit < 0) {
            throw handleClientException(ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT, null);
        }
        if (offset != null && offset < 0) {
            throw handleClientException(ERROR_CODE_INVALID_ARGUMENTS_FOR_OFFSET, null);
        }
    }
}
