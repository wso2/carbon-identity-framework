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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.template.mgt.TemplateMgtConstants;
import org.wso2.carbon.identity.template.mgt.exception.TemplateManagementException;
import org.wso2.carbon.identity.template.mgt.function.ResourceToTemplate;
import org.wso2.carbon.identity.template.mgt.function.TemplateToResource;
import org.wso2.carbon.identity.template.mgt.function.TemplateToResourceAdd;
import org.wso2.carbon.identity.template.mgt.handler.TemplateHandler;
import org.wso2.carbon.identity.template.mgt.internal.TemplateManagerDataHolder;
import org.wso2.carbon.identity.template.mgt.model.Template;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.template.mgt.handler.impl.WSTrustConnectorValidator.validateWSTrustTemplateAvailability;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.getTenantDomainFromCarbonContext;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.handleClientException;
import static org.wso2.carbon.identity.template.mgt.util.TemplateMgtUtils.handleServerException;

/**
 * Config store based template handler.
 */
public class ConfigStoreBasedTemplateHandler implements TemplateHandler {

    private static Log log = LogFactory.getLog(ConfigStoreBasedTemplateHandler.class);

    @Override
    public String addTemplate(Template template) throws TemplateManagementException {

        return addTemplateToConfigStore(template);
    }

    @Override
    public Template getTemplateById(String templateId) throws TemplateManagementException {

        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        validateWSTrustTemplateAvailability(templateId);
        try {
            Resource resource = configManager.getTenantResourceById(templateId);
            Template template = new ResourceToTemplate().apply(resource);
            if (resource.getFiles().size() == 1) {
                try (InputStream templateScriptInputStream = configManager
                        .getFileById(resource.getResourceType(), resource.getResourceName(),
                                resource.getFiles().get(0).getId())) {
                    template.setTemplateScript(IOUtils.toString(templateScriptInputStream));
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(template.getTemplateType().toString() + " can have only one templated object. But the "
                            + "template with id: " + templateId + " has " + resource.getFiles().size() + " templated "
                            + "object/s. Therefore templated object is not retrieved.");
                }
            }
            return template;
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NOT_FOUND, e,
                        templateId, getTenantDomainFromCarbonContext());
            }
            throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_RETRIEVE_TEMPLATE_BY_ID, e,
                    templateId, getTenantDomainFromCarbonContext());
        } catch (IOException e) {
            throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_RETRIEVE_TEMPLATE_BY_ID, e,
                    templateId, getTenantDomainFromCarbonContext());
        }
    }

    @Override
    public void updateTemplateById(String templateId, Template template) throws TemplateManagementException {

        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        validateWSTrustTemplateAvailability(templateId);
        try {
            configManager.replaceResource(new TemplateToResource().apply(template));
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS.getCode().equals(
                    e.getErrorCode())) {
                throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NOT_FOUND, e,
                        templateId, getTenantDomainFromCarbonContext());
            } else if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ALREADY_EXISTS.getCode().equals(
                    e.getErrorCode())) {
                throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ALREADY_EXIST, e,
                        templateId, getTenantDomainFromCarbonContext());
            }
            throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_UPDATE_TEMPLATE, e,
                    templateId, getTenantDomainFromCarbonContext());
        }
    }

    @Override
    public void deleteTemplateById(String templateId) throws TemplateManagementException {

        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        validateWSTrustTemplateAvailability(templateId);
        try {
            configManager.deleteResourceById(templateId);
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS.getCode().equals(
                    e.getErrorCode())) {
                throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_NOT_FOUND, e,
                        templateId, getTenantDomainFromCarbonContext());
            }
            throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_DELETE_TEMPLATE_BY_ID, e,
                    templateId, getTenantDomainFromCarbonContext());
        }
    }

    @Override
    public List<Template> listTemplates(String templateType, Integer limit, Integer offset, Condition
            searchCondition) throws TemplateManagementException {

        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        try {
            Resources resourcesList;
            if (searchCondition == null) {
                resourcesList = configManager.getResourcesByType(templateType);
            } else {
                resourcesList = configManager.getTenantResources(searchCondition);
            }
            return resourcesList.getResources().stream().map(resource -> {
                resource.setResourceType(templateType);
                return new ResourceToTemplate().apply(resource);
            }).collect(Collectors.toList());
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e
                    .getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("Template type : '" + templateType + "' has not been created in the database.", e);
                }
                return Collections.emptyList();
            } else if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCES_DOES_NOT_EXISTS.getCode().equals(e
                    .getErrorCode())) {
                if (log.isDebugEnabled()) {
                    String message = "Templates do not exist for template type: " + templateType;
                    if (searchCondition != null) {
                        message = message + ", and search  criteria:" + searchCondition.toString();
                    }
                    log.debug(message, e);
                }
                return Collections.emptyList();
            }
            throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_LIST_TEMPLATES, e, templateType,
                    getTenantDomainFromCarbonContext());
        }
    }

    private String addTemplateToConfigStore(Template template) throws TemplateManagementException {

        if (!isValidTemplateType(template.getTemplateType().toString())) {
            throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_INVALID_TEMPLATE_TYPE,
                    template.getTemplateType().toString());
        }
        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        try {
            String templateId = StringUtils.isNotBlank(template.getTemplateId())
                    ? template.getTemplateId()
                    : UUID.randomUUID().toString();
            Resource resource = configManager
                    .addResource(template.getTemplateType().toString(),
                            new TemplateToResourceAdd().apply(template), templateId);
            configManager.addFile(template.getTemplateType().toString(), template.getTemplateName(),
                    template.getTemplateName() + "_template_object",
                    IOUtils.toInputStream(template.getTemplateScript()));
            return resource.getResourceId();
        } catch (ConfigurationManagementException e) {
            if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_ALREADY_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                throw handleClientException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_TEMPLATE_ALREADY_EXIST, e,
                        template.getTemplateName());
            } else if (ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode()
                    .equals(e.getErrorCode())) {
                // If the template insert is failing due to the relevant resource-type for templates is not existing
                // in the database, create the resource-type and retry the template creation.
                try {
                    createResourceType(template.getTemplateType().toString());
                    return addTemplateToConfigStore(template);
                } catch (ConfigurationManagementException e1) {
                    throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_INSERT_TEMPLATE, e,
                            template.getTemplateName());
                }
            } else {
                throw handleServerException(TemplateMgtConstants.ErrorMessages.ERROR_CODE_INSERT_TEMPLATE, e,
                        template.getTemplateName());
            }
        }
    }

    private boolean isValidTemplateType(String templateType) {

        return EnumUtils.isValidEnum(TemplateMgtConstants.TemplateType.class, templateType);
    }

    private void createResourceType(String templateType) throws ConfigurationManagementException {

        ConfigurationManager configManager = TemplateManagerDataHolder.getInstance().getConfigurationManager();
        ResourceTypeAdd resourceType = new ResourceTypeAdd();
        resourceType.setName(templateType);
        resourceType.setDescription("This is the resource type for " + templateType);
        configManager.addResourceType(resourceType);
    }
}
