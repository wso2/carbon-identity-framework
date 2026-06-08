/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ConsentAppMappingService}.
 *
 * <p>Mappings are stored in the config store as resources of type
 * {@code consent-purpose-mapping}. Each resource's name is the purpose UUID and
 * each attribute key is an application resource ID.</p>
 */
public class ConsentAppMappingServiceImpl implements ConsentAppMappingService {

    private static final Log LOG = LogFactory.getLog(ConsentAppMappingServiceImpl.class);

    private static final String RESOURCE_TYPE = "consent-purpose-mapping";

    private static final String ERROR_CODE_ALREADY_MAPPED = "CPM-60001";
    private static final String ERROR_CODE_MAPPING_NOT_FOUND = "CPM-60002";

    @Override
    public List<String> getApplicationsForPurpose(String purposeId) throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving application mappings for consent purpose: " + purposeId);
        }
        try {
            Resource resource = FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .getResource(RESOURCE_TYPE, purposeId, false);
            if (resource == null || resource.getAttributes() == null) {
                return Collections.emptyList();
            }
            return resource.getAttributes().stream().map(Attribute::getKey).toList();
        } catch (ConfigurationManagementClientException e) {
            if (isResourceTypeNotFound(e) || isResourceNotFound(e)) {
                return Collections.emptyList();
            }
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                    "Error retrieving application mappings for consent purpose: " + purposeId, e);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                    "Error retrieving application mappings for consent purpose: " + purposeId, e);
        }
    }

    @Override
    public void addApplicationToPurpose(String purposeId, String applicationId)
            throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding application " + applicationId + " to consent purpose: " + purposeId);
        }
        Resource resource;
        try {
            resource = FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .getResource(RESOURCE_TYPE, purposeId, false);
        } catch (ConfigurationManagementClientException e) {
            if (isResourceNotFound(e)) {
                resource = null;
            } else if (isResourceTypeNotFound(e)) {
                createResourceType();
                resource = null;
            } else {
                throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                        "Error adding application " + applicationId + " to consent purpose: " + purposeId, e);
            }
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                    "Error adding application " + applicationId + " to consent purpose: " + purposeId, e);
        }

        if (resource != null && resource.getAttributes() != null &&
                resource.getAttributes().stream().anyMatch(a -> applicationId.equals(a.getKey()))) {
            throw new ConsentAppMappingException(ERROR_CODE_ALREADY_MAPPED,
                    "Application " + applicationId + " is already mapped to consent purpose: " + purposeId);
        }

        try {
            Attribute attribute = new Attribute(applicationId, applicationId);
            if (resource == null) {
                Resource newResource = new Resource(purposeId, RESOURCE_TYPE);
                newResource.setAttributes(Collections.singletonList(attribute));
                FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                        .addResource(RESOURCE_TYPE, newResource);
            } else {
                FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                        .addAttribute(RESOURCE_TYPE, purposeId, attribute);
            }
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_ADD_RESOURCE.getCode(),
                    "Error adding application " + applicationId + " to consent purpose: " + purposeId, e);
        }
    }

    @Override
    public void removeApplicationFromPurpose(String purposeId, String applicationId)
            throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing application " + applicationId + " from consent purpose: " + purposeId);
        }
        Resource resource;
        try {
            resource = FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .getResource(RESOURCE_TYPE, purposeId, false);
        } catch (ConfigurationManagementClientException e) {
            if (isResourceNotFound(e)) {
                throw new ConsentAppMappingException(ERROR_CODE_MAPPING_NOT_FOUND,
                        "Application " + applicationId + " is not mapped to consent purpose: " + purposeId);
            }
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                    "Error removing application " + applicationId + " from consent purpose: " + purposeId, e);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_GET_RESOURCE.getCode(),
                    "Error removing application " + applicationId + " from consent purpose: " + purposeId, e);
        }

        if (resource == null || resource.getAttributes() == null ||
                resource.getAttributes().stream().noneMatch(a -> applicationId.equals(a.getKey()))) {
            throw new ConsentAppMappingException(ERROR_CODE_MAPPING_NOT_FOUND,
                    "Application " + applicationId + " is not mapped to consent purpose: " + purposeId);
        }

        try {
            FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .deleteAttribute(RESOURCE_TYPE, purposeId, applicationId);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_DELETE_ATTRIBUTE.getCode(),
                    "Error removing application " + applicationId + " from consent purpose: " + purposeId, e);
        }
    }

    @Override
    public List<String> getPurposesForApplication(String applicationId) throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving consent purposes for application: " + applicationId);
        }
        try {
            Resources resources = FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .getResourcesByType(RESOURCE_TYPE);
            if (resources == null || resources.getResources() == null) {
                return Collections.emptyList();
            }
            return resources.getResources().stream()
                    .filter(r -> r.getAttributes() != null &&
                            r.getAttributes().stream().anyMatch(a -> applicationId.equals(a.getKey())))
                    .map(Resource::getResourceName).toList();
        } catch (ConfigurationManagementClientException e) {
            if (isResourceTypeNotFound(e)) {
                return Collections.emptyList();
            }
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_SEARCH_TENANT_RESOURCES.getCode(),
                    "Error retrieving consent purposes for application: " + applicationId, e);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_SEARCH_TENANT_RESOURCES.getCode(),
                    "Error retrieving consent purposes for application: " + applicationId, e);
        }
    }

    @Override
    public void removeAllPurposeMappingsForApplication(String applicationId) throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing all consent purpose mappings for application: " + applicationId);
        }
        List<String> purposeIds = getPurposesForApplication(applicationId);
        for (String purposeId : purposeIds) {
            try {
                FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                        .deleteAttribute(RESOURCE_TYPE, purposeId, applicationId);
            } catch (ConfigurationManagementClientException e) {
                if (!isResourceNotFound(e)) {
                    throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_DELETE_ATTRIBUTE.getCode(),
                            "Error removing application " + applicationId + " from consent purpose: " + purposeId, e);
                }
            } catch (ConfigurationManagementException e) {
                throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_DELETE_ATTRIBUTE.getCode(),
                        "Error removing application " + applicationId + " from consent purpose: " + purposeId, e);
            }
        }
    }

    @Override
    public void removeAllApplicationMappingsForPurpose(String purposeId) throws ConsentAppMappingException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Removing all application mappings for consent purpose: " + purposeId);
        }
        try {
            FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                    .deleteResource(RESOURCE_TYPE, purposeId);
        } catch (ConfigurationManagementClientException e) {
            if (isResourceNotFound(e)) {
                return;
            }
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_DELETE_RESOURCE.getCode(),
                    "Error removing all application mappings for consent purpose: " + purposeId, e);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_DELETE_RESOURCE.getCode(),
                    "Error removing all application mappings for consent purpose: " + purposeId, e);
        }
    }

    private void createResourceType() throws ConsentAppMappingException {

        try {
            ResourceTypeAdd resourceTypeAdd = new ResourceTypeAdd();
            resourceTypeAdd.setName(RESOURCE_TYPE);
            resourceTypeAdd.setDescription("Resource type for consent purpose to application mappings.");
            FrameworkServiceDataHolder.getInstance().getConfigurationManager().addResourceType(resourceTypeAdd);
        } catch (ConfigurationManagementException e) {
            throw new ConsentAppMappingException(ErrorMessages.ERROR_CODE_ADD_RESOURCE_TYPE.getCode(),
                    "Error creating resource type for consent purpose mappings.", e);
        }
    }

    private boolean isResourceNotFound(ConfigurationManagementClientException e) {

        return ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    private boolean isResourceTypeNotFound(ConfigurationManagementClientException e) {

        return ErrorMessages.ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }
}
