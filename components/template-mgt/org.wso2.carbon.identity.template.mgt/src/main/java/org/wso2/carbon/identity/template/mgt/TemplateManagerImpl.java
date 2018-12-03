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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.template.mgt.dao.TemplateManagerDAO;
import org.wso2.carbon.identity.template.mgt.dao.impl.TemplateManagerDAOImpl;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementClientException;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.model.Template;
import org.wso2.carbon.identity.template.mgt.model.TemplateInfo;

import java.util.List;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT_OFFSET;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ALREADY_EXIST;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NAME_INVALID;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NAME_REQUIRED;
import static org.wso2.carbon.identity.template.mgt.TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_SCRIPT_REQUIRED;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.getTenantIdFromCarbonContext;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.handleClientException;

/**
 * Template manager service implementation.
 */
public class TemplateManagerImpl implements TemplateManager {

    private static final Log log = LogFactory.getLog(TemplateManagerImpl.class);
    private static final Integer DEFAULT_SEARCH_LIMIT = 100;

    /**
     * This method is used to add a new template.
     *
     * @param template Template element.
     * @return Return template element with template name, description and script.
     * @throws TemplateManagementException Template Management Exception.
     */
    @Override
    public Template addTemplate(Template template) throws TemplateManagementException {

        validateInputParameters(template);
        if (isTemplateExists(template.getTemplateName())) {
            if (log.isDebugEnabled()) {
                log.debug("A template already exists with the name: " + template.getTemplateName());
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_ALREADY_EXIST, template.getTemplateName());
        }

        TemplateManagerDAO templateManagerDAO = new TemplateManagerDAOImpl();
        return templateManagerDAO.addTemplate(template);
    }

    /**
     * This method is used to get the template by template Name.
     *
     * @param templateName Name of the template.
     * @return Template matching the input parameters.
     * @throws TemplateManagementException Template Management Exception.
     */
    @Override
    public Template getTemplateByName(String templateName) throws TemplateManagementException {

        if (!isTemplateExists(templateName)) {
            if (log.isDebugEnabled()) {
                log.debug("No template found for the name: " + templateName);
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_NAME_INVALID, templateName);
        }
        return fetchSingleTemplate(templateName);
    }

    /**
     * This method is used to update an existing Template.
     *
     * @param templateName Name of the updated template.
     * @param template     Template element.
     * @return Return the updated Template element.
     * @throws TemplateManagementException Template Management Exception.
     */
    @Override
    public Template updateTemplate(String templateName, Template template) throws TemplateManagementException {

        validateInputParameters(template);
        TemplateManagerDAO templateManagerDAO = new TemplateManagerDAOImpl();
        if (!isTemplateExists(templateName)) {
            if (log.isDebugEnabled()) {
                log.debug("No template found for the name: " + templateName);
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_NAME_INVALID, templateName);
        }

        if (!StringUtils.equals(templateName, template.getTemplateName()) &&
                isTemplateExists(template.getTemplateName())) {
            //check if there is another template in the database(other than the template in the path param)
            // with the name of the updated template
            if (log.isDebugEnabled()) {
                log.debug("A template already exists with the name: " + template.getTemplateName());
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_ALREADY_EXIST, template.getTemplateName());
        }

        return templateManagerDAO.updateTemplate(templateName, template);
    }

    /**
     * This method is used to delete existing template by template name.
     *
     * @param templateName Name of the template.
     * @throws TemplateManagementException Template Management Exception.
     */
    @Override
    public TemplateInfo deleteTemplate(String templateName) throws TemplateManagementException {

        if (StringUtils.isBlank(templateName)) {
            if (log.isDebugEnabled()) {
                log.debug("Template Name is not found in the request or invalid Template Name.");
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_NAME_REQUIRED, null);
        }

        if (getTemplateByName(templateName) == null) {
            throw handleClientException(ERROR_CODE_TEMPLATE_NAME_INVALID, templateName);
        }

        TemplateManagerDAO templateManagerDAO = new TemplateManagerDAOImpl();
        if (log.isDebugEnabled()) {
            log.debug("Template deleted successfully. Name: " + templateName);
        }
        return templateManagerDAO.deleteTemplate(templateName, getTenantIdFromCarbonContext());
    }

    /**
     * This method is used to get the names and descriptions of all or filtered existing templates.
     *
     * @param limit  Number of search results.
     * @param offset Start index of the search.
     * @return Filtered list of TemplateInfo elements.
     * @throws TemplateManagementException Template Management Exception.
     */
    @Override
    public List<TemplateInfo> listTemplates(Integer limit, Integer offset) throws TemplateManagementException {

        validatePaginationParameters(limit, offset);

        if (limit == 0) {
            limit = DEFAULT_SEARCH_LIMIT;
            if (log.isDebugEnabled()) {
                log.debug("Limit is not defined in the request, default to: " + limit);
            }
        }

        TemplateManagerDAO templateManagerDAO = new TemplateManagerDAOImpl();
        return templateManagerDAO.getAllTemplates(getTenantIdFromCarbonContext(), limit, offset);
    }

    /**
     * This method is used to get the template by template Name.
     *
     * @param templateName Name of the template.
     * @return Template matching the input parameters.
     * @throws TemplateManagementException Template Management Exception.
     */
    public Template fetchSingleTemplate(String templateName) throws TemplateManagementException {

        TemplateManagerDAO templateManagerDAO = new TemplateManagerDAOImpl();
        return templateManagerDAO.getTemplateByName(templateName.trim(), getTenantIdFromCarbonContext());
    }

    /**
     * This method is used to check whether a template exists with the given name.
     *
     * @param templateName Name of the template.
     * @return true, if an element is found.
     * @throws TemplateManagementException Consent Management Exception.
     */
    private boolean isTemplateExists(String templateName) throws TemplateManagementException {

        return fetchSingleTemplate(templateName) != null;
    }

    /**
     * This method is used to validate the input parameters of a Template.
     *
     * @param template Template element.
     * @throws TemplateManagementException Consent Management Exception.
     */
    private void validateInputParameters(Template template) throws TemplateManagementException {

        if (isBlank(template.getTemplateName())) {
            if (log.isDebugEnabled()) {
                log.debug("Template name cannot be empty.");
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_NAME_REQUIRED, null);
        }

        if (isBlank(template.getTemplateScript())) {
            if (log.isDebugEnabled()) {
                log.debug("Template script cannot be empty.");
            }
            throw handleClientException(ERROR_CODE_TEMPLATE_SCRIPT_REQUIRED, null);
        }

        if (template.getTenantId() == null) {
            template.setTenantId(getTenantIdFromCarbonContext());
        }
    }

    /**
     * This method is used to validate the pagination parameters.
     *
     * @param limit  Limits the number of templates listed on a page.
     * @param offset Specifies the starting point for the templates to be displayed.
     * @throws TemplateManagementException Consent Management Exception.
     */
    private void validatePaginationParameters(Integer limit, Integer offset) throws TemplateManagementClientException {

        if (limit < 0 || offset < 0) {
            throw handleClientException(ERROR_CODE_INVALID_ARGUMENTS_FOR_LIMIT_OFFSET, null);
        }
    }

}
