/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementClientException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.ComplexCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.identity.configuration.mgt.core.search.PrimitiveCondition;
import org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ATTRIBUTE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ATTRIBUTE_IDENTIFIERS_REQUIRED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_ATTRIBUTE_REQUIRED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_FILES_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_FILE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_FILE_IDENTIFIERS_REQUIRED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_GET_DAO;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_INVALID_RESOURCE_ID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_ADD_REQUEST_INVALID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_DELETE_REQUEST_REQUIRED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_GET_REQUEST_INVALID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_REPLACE_REQUEST_INVALID;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages
        .ERROR_CODE_SEARCH_REQUEST_INVALID;
import static org.wso2.carbon.identity.configuration.mgt.core.search.constant.ConditionType.PrimitiveOperator.EQUALS;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.generateUniqueID;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.getFilePath;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.handleClientException;
import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.handleServerException;

/**
 * Resource Manager service implementation.
 */
public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Log log = LogFactory.getLog(ConfigurationManagerImpl.class);
    private List<ConfigurationDAO> configurationDAOS;

    public ConfigurationManagerImpl(ConfigurationManagerConfigurationHolder configurationManagerConfigurationHolder) {

        this.configurationDAOS = configurationManagerConfigurationHolder.getConfigurationDAOS();
    }

    /**
     * {@inheritDoc}
     */
    public Resources getTenantResources(Condition searchCondition) throws ConfigurationManagementException {

        validateSearchRequest(searchCondition);
        Resources resources = getConfigurationDAO().getTenantResources(searchCondition);
        if (resources == null) {
            throw handleClientException(ErrorMessages.ERROR_CODE_RESOURCES_DOES_NOT_EXISTS, null);
        }
        return resources;
    }

    /**
     * {@inheritDoc}
     */
    public Resources getTenantResources(String tenantDomain, Condition searchCondition) throws
            ConfigurationManagementException {

        return getTenantResources(getTenantSpecificSearchCondition(tenantDomain, searchCondition));
    }

    /**
     * Append tenant domain search to then search condition.
     *
     * @param tenantDomain    Tenant domain.
     * @param searchCondition Search condition.
     * @return Condition.
     */
    private Condition getTenantSpecificSearchCondition(String tenantDomain, Condition searchCondition) {

        if (searchCondition != null) {
            Condition tenantCondition = new PrimitiveCondition(ConfigurationConstants
                    .RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN, EQUALS, tenantDomain);
            List<Condition> list = new ArrayList<>();
            list.add(searchCondition);
            list.add(tenantCondition);
            return new ComplexCondition(ConditionType.ComplexOperator.AND, list);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Search condition is null");
            }
            return new PrimitiveCondition(ConfigurationConstants
                    .RESOURCE_SEARCH_BEAN_FIELD_TENANT_DOMAIN, EQUALS, tenantDomain);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resources getResources() throws ConfigurationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Get Resources API is not Implemented yet.");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Resources getResourcesByType(int tenantId, String resourceTypeName) throws ConfigurationManagementException {

        return retrieveResourcesByType(tenantId, resourceTypeName);
    }

    /**
     * {@inheritDoc}
     */
    public Resources getResourcesByType(String resourceTypeName) throws ConfigurationManagementException {

        return retrieveResourcesByType(getTenantId(), resourceTypeName);
    }

    private Resources retrieveResourcesByType(int tenantId, String resourceTypeName) throws ConfigurationManagementException {

        validateResourcesRetrieveRequest(resourceTypeName);
        ResourceType resourceType = getResourceType(resourceTypeName);
        List<Resource> resourceList = this.getConfigurationDAO().getResourcesByType(tenantId, resourceType.getId());
        if (resourceList == null) {
            if (log.isDebugEnabled()) {
                log.debug("No resource found for the resourceTypeName: " + resourceTypeName);
            }
            throw handleClientException(
                    ErrorMessages.ERROR_CODE_RESOURCES_DOES_NOT_EXISTS, resourceTypeName, null);
        }
        return new Resources(resourceList);
    }

    /**
     * {@inheritDoc}
     */
    public Resource getResource(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        return getResource(getTenantId(), resourceTypeName, resourceName);
    }

    @Override
    public Resource getResourceByTenantId(int tenantId, String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        return getResource(tenantId, resourceTypeName, resourceName);
    }

    private Resource getResource(int tenantId, String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        validateResourceRetrieveRequest(resourceTypeName, resourceName);
        ResourceType resourceType = getResourceType(resourceTypeName);
        Resource resource = this.getConfigurationDAO().getResourceByName(tenantId, resourceType.getId(), resourceName);
        if (resource == null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("No resource found for the resource with name: %s in tenant with ID: %s",
                        resourceName, tenantId));
            }
            throw handleClientException(ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS, resourceName, null);
        }
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteResource(String resourceTypeName, String resourceName) throws ConfigurationManagementException {

        validateResourceDeleteRequest(resourceTypeName, resourceName);
        ResourceType resourceType = getResourceType(resourceTypeName);
        this.getConfigurationDAO().deleteResourceByName(getTenantId(), resourceType.getId(), resourceName);
        if (log.isDebugEnabled()) {
            log.debug("Resource: " + resourceName + " is deleted successfully.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resource addResource(String resourceTypeName, ResourceAdd resourceAdd)
            throws ConfigurationManagementException {

        validateResourceCreateRequest(resourceTypeName, resourceAdd);
        Resource resource = generateResourceFromRequest(resourceTypeName, resourceAdd);
        String resourceId = generateUniqueID();
        if (log.isDebugEnabled()) {
            log.debug("Resource id generated: " + resourceId);
        }
        resource.setResourceId(resourceId);
        this.getConfigurationDAO().addResource(resource);
        if (log.isDebugEnabled()) {
            log.debug(resourceAdd.getName() + " resource created successfully.");
        }
        return resource;
    }

    @Override
    public Resource addResource(String resourceTypeName, Resource resource) throws ConfigurationManagementException {

        validateResourceCreateRequest(resourceTypeName, resource);
        String resourceId = generateUniqueID();
        if (log.isDebugEnabled()) {
            log.debug("Resource id generated: " + resourceId);
        }
        resource.setResourceId(resourceId);
        this.getConfigurationDAO().addResource(resource);
        log.info("Resource: " + resource.getResourceName() + " added successfully");
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public Resource replaceResource(String resourceTypeName, ResourceAdd resourceAdd)
            throws ConfigurationManagementException {

        validateResourceReplaceRequest(resourceTypeName, resourceAdd);
        String resourceId = generateResourceId(resourceTypeName, resourceAdd.getName());
        Resource resource = generateResourceFromRequest(resourceTypeName, resourceAdd);
        resource.setResourceId(resourceId);
        this.getConfigurationDAO().replaceResource(resource);
        if (log.isDebugEnabled()) {
            log.debug(resourceAdd.getName() + " resource created successfully.");
        }
        return resource;
    }

    @Override
    public Resource replaceResource(String resourceTypeName, Resource resource)
            throws ConfigurationManagementException {

        validateResourceReplaceRequest(resourceTypeName, resource);
        String resourceId = generateResourceId(resourceTypeName, resource.getResourceName());
        resource.setResourceId(resourceId);
        this.getConfigurationDAO().replaceResourceWithFiles(resource);
        log.info(resource.getResourceName() + " resource created successfully.");
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceType getResourceType(String resourceTypeName) throws ConfigurationManagementException {

        validateResourceTypeRetrieveRequest(resourceTypeName);
        ResourceType resourceType = getConfigurationDAO().getResourceTypeByName(resourceTypeName);
        if (resourceType == null || resourceType.getId() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource Type: " + resourceTypeName + " does not exist.");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS, resourceTypeName);
        }

        if (log.isDebugEnabled()) {
            log.debug("Resource type: " + resourceType.getName() + " retrieved successfully.");
        }
        return resourceType;
    }

    /**
     * {@inheritDoc}
     */
    public void deleteResourceType(String resourceName) throws ConfigurationManagementException {

        validateResourceTypeDeleteRequest(resourceName);
        getConfigurationDAO().deleteResourceTypeByName(resourceName);

        if (log.isDebugEnabled()) {
            log.debug("Resource type: " + resourceName + " is successfully deleted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResourceType addResourceType(ResourceTypeAdd resourceTypeAdd) throws ConfigurationManagementException {

        validateResourceTypeCreateRequest(resourceTypeAdd);
        String resourceTypeID = generateUniqueID();
        if (log.isDebugEnabled()) {
            log.debug("Resource type id generated: " + resourceTypeID);
        }
        ResourceType resourceType = generateResourceTypeFromRequest(resourceTypeAdd, resourceTypeID);
        getConfigurationDAO().addResourceType(resourceType);

        if (log.isDebugEnabled()) {
            log.debug("Resource type: " + resourceType.getName() + " successfully created with the id: "
                    + resourceType.getId());
        }
        return new ResourceType(
                resourceType.getName(),
                resourceType.getId(),
                resourceType.getDescription()
        );
    }

    /**
     * {@inheritDoc}
     */
    public ResourceType replaceResourceType(ResourceTypeAdd resourceTypeAdd) throws ConfigurationManagementException {

        validateResourceTypeReplaceRequest(resourceTypeAdd);
        String resourceTypeID;
        resourceTypeID = generateResourceTypeId(resourceTypeAdd.getName());
        ResourceType resourceType = generateResourceTypeFromRequest(resourceTypeAdd, resourceTypeID);
        getConfigurationDAO().replaceResourceType(resourceType);
        if (log.isDebugEnabled()) {
            log.debug("Resource type: " + resourceType.getName() + " successfully replaced with the id: "
                    + resourceType.getId());
        }
        return new ResourceType(
                resourceType.getName(),
                resourceType.getId(),
                resourceType.getDescription()
        );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAttribute(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException {

        validateAttributeDeleteRequest(resourceTypeName, resourceName, attributeKey);
        Attribute existingAttribute = getAttribute(resourceTypeName, resourceName, attributeKey);
        getConfigurationDAO().deleteAttribute(
                existingAttribute.getAttributeId(), getResourceId(resourceTypeName, resourceName), attributeKey);
        if (log.isDebugEnabled()) {
            log.debug("Attribute: " + attributeKey + " successfully deleted.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Attribute getAttribute(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException {

        validateAttributeGetRequest(resourceTypeName, resourceName, attributeKey);
        String resourceId = getResourceId(resourceTypeName, resourceName);
        Attribute attribute = getConfigurationDAO().getAttributeByKey(resourceId, attributeKey);
        if (attribute == null || attribute.getKey() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource Type: " + attributeKey + " does not exist.");
            }
            throw handleClientException(ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS, attributeKey);
        }

        if (log.isDebugEnabled()) {
            log.debug("Resource type: " + attributeKey + " retrieved successfully.");
        }
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    public Attribute updateAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException {

        validateAttributeRequest(attribute);
        Attribute existingAttribute = getAttribute(resourceTypeName, resourceName, attribute.getKey());
        getConfigurationDAO().updateAttribute(
                existingAttribute.getAttributeId(), getResourceId(resourceTypeName, resourceName), attribute);
        if (log.isDebugEnabled()) {
            log.debug("Attribute: " + attribute.getKey() + " successfully updated.");
        }
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    public Attribute addAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException {

        validateAttributeAddRequest(resourceTypeName, resourceName, attribute.getKey());
        String resourceId = getResourceId(resourceTypeName, resourceName);
        String attributeId = generateUniqueID();
        if (log.isDebugEnabled()) {
            log.debug("Attribute id generated: " + attributeId);
        }
        getConfigurationDAO().addAttribute(attributeId, resourceId, attribute);
        if (log.isDebugEnabled()) {
            log.debug("Attribute: " + attribute.getKey() + " successfully updated.");
        }
        return attribute;
    }

    /**
     * {@inheritDoc}
     */
    public Attribute replaceAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException {

        validateAttributeRequest(attribute);
        String resourceId = getResourceId(resourceTypeName, resourceName);
        String attributeId = generateAttributeId(resourceTypeName, resourceName, attribute);
        getConfigurationDAO().replaceAttribute(attributeId, resourceId, attribute);
        if (log.isDebugEnabled()) {
            log.debug("Attribute: " + attribute.getKey() + " successfully replaced.");
        }
        return attribute;
    }

    private void validateSearchRequest(Condition condition) throws ConfigurationManagementClientException {

        if (condition == null) {
            if (log.isDebugEnabled()) {
                log.debug("Search condition:null is not valid");
            }
            throw handleClientException(ERROR_CODE_SEARCH_REQUEST_INVALID, null);
        }
    }

    @Override
    public void deleteResourcesByType(String resourceTypeName) throws ConfigurationManagementException {

        ResourceType resourceType = getResourceType(resourceTypeName);
        this.getConfigurationDAO().deleteResourcesByType(getTenantId(), resourceType.getId());
        if (log.isDebugEnabled()) {
            log.debug("Resources belongs to Resource Type : " + resourceTypeName + " is deleted successfully.");
        }
    }

    private void validateResourceRetrieveRequest(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceName) || StringUtils.isEmpty(resourceTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid resource identifier with resourceName: " + resourceName
                        + " and resourceTypeName: " + resourceTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_GET_REQUEST_INVALID, null);
        }
    }

    private void validateResourcesRetrieveRequest(String resourceTypeName)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid resource identifier with resourceTypeName: " + resourceTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_GET_REQUEST_INVALID, null);
        }
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private void validateResourceDeleteRequest(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceName) || StringUtils.isEmpty(resourceTypeName)) {
            if (log.isDebugEnabled()) {
                log.debug("Error identifying the resource with resource name: " + resourceName + " and resource type:"
                        + resourceTypeName + ".");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_DELETE_REQUEST_REQUIRED, null);
        }

        if (!isResourceExists(resourceTypeName, resourceName)) {
            if (log.isDebugEnabled()) {
                log.debug("A resource with the name: " + resourceName + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_DOES_NOT_EXISTS, resourceName);
        }
    }

    private Resource generateResourceFromRequest(String resourceTypeName, ResourceAdd resourceAdd) {

        Resource resource = new Resource();
        resource.setTenantDomain(getTenantDomain());
        resource.setResourceName(resourceAdd.getName());
        resource.setResourceType(resourceTypeName);
        resource.setAttributes(resourceAdd.getAttributes());
        return resource;
    }

    private void validateResourceCreateRequest(String resourceTypeName, ResourceAdd resourceAdd)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeName) || !isResourceAddParameterValid(resourceAdd)) {
            throw handleClientException(ERROR_CODE_RESOURCE_ADD_REQUEST_INVALID, null);
        }

        if (isResourceExists(resourceTypeName, resourceAdd.getName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_ALREADY_EXISTS, resourceAdd.getName());
        }
    }

    /**
     * Validate that resource type and resource name are non-empty. Validate file name and streams are non-empty
     * if the resource has files. Set resource type and tenant domain if they are not set to the resource object.
     *
     * @param resourceTypeName Type of the resource.
     * @param resource         The resource to be added.
     * @throws ConfigurationManagementException If resource validation fails.
     */
    private void validateResourceCreateRequest(String resourceTypeName, Resource resource)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeName) || StringUtils.isEmpty(resource.getResourceName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_ADD_REQUEST_INVALID, null);
        }
        if (isResourceExists(resourceTypeName, resource.getResourceName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_ALREADY_EXISTS, resource.getResourceName());
        }
        if (CollectionUtils.isNotEmpty(resource.getFiles())) {
            List<ResourceFile> files = resource.getFiles();
            for (ResourceFile file : files) {
                if (StringUtils.isEmpty(file.getId())) {
                    file.setId(generateUniqueID());
                }
                if (StringUtils.isEmpty(file.getName())) {
                    String fileIdentifiers = String.format("Resource type: %s, resourceName: %s", resourceTypeName,
                            resource.getResourceName());
                    throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
                }
                if (file.getInputStream() == null) {
                    throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED,
                            "File stream is invalid or empty.");
                }
            }
        }
        if (StringUtils.isEmpty(resource.getTenantDomain())) {
            resource.setTenantDomain(getTenantDomain());
        }
        if (StringUtils.isEmpty(resource.getResourceType())) {
            resource.setResourceType(resourceTypeName);
        }
    }

    private boolean isResourceAddParameterValid(ResourceAdd resourceAdd) {

        if (StringUtils.isEmpty(resourceAdd.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Resource name: " + resourceAdd.getName() + " is not valid.");
            }
            return false;
        }
        return true;
    }

    private boolean isResourceExists(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        try {
            getResource(resourceTypeName, resourceName);
        } catch (ConfigurationManagementClientException e) {
            if (isResourceNotExistsError(e)) {
                return false;
            }
            throw e;
        }
        return true;
    }

    private boolean isResourceExistsById(String resourceId) throws ConfigurationManagementException {

        if (StringUtils.isBlank(resourceId)) {
            throw handleClientException(ERROR_CODE_INVALID_RESOURCE_ID, resourceId);
        }
        return this.getConfigurationDAO().isExistingResource(getTenantId(), resourceId);
    }

    private boolean isResourceNotExistsError(ConfigurationManagementClientException e) {

        return ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    private String generateResourceId(
            String resourceTypeName, String resourceName) throws ConfigurationManagementException {

        String resourceId;
        if (isResourceExists(resourceTypeName, resourceName)) {
            resourceId = getResource(resourceTypeName, resourceName).getResourceId();
        } else {
            resourceId = generateUniqueID();
            if (log.isDebugEnabled()) {
                log.debug("Resource id generated: " + resourceId);
            }
        }
        return resourceId;
    }

    private void validateResourceReplaceRequest(String resourceTypeName, ResourceAdd resourceAdd)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeName) || !isResourceAddParameterValid(resourceAdd)) {
            throw handleClientException(ERROR_CODE_RESOURCE_REPLACE_REQUEST_INVALID, null);
        }
    }

    /**
     * Validate that resource type is non-empty. Validate the resource existence.
     * Validate file name and streams are non-empty if the resource has files.
     * Set resource type and tenant domain if they are not set to the resource object.
     *
     * @param resourceTypeName Type of the resource to be replaced.
     * @param resource         The resource to be replaced.
     * @throws ConfigurationManagementException If resource validation fails.
     */
    private void validateResourceReplaceRequest(String resourceTypeName, Resource resource)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeName)) {
            throw handleClientException(ERROR_CODE_RESOURCE_ADD_REQUEST_INVALID, null);
        }
        if (!isResourceExists(resourceTypeName, resource.getResourceName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_DOES_NOT_EXISTS, resource.getResourceName());
        }
        if (CollectionUtils.isNotEmpty(resource.getFiles())) {
            List<ResourceFile> files = resource.getFiles();
            for (ResourceFile file : files) {
                if (StringUtils.isEmpty(file.getId())) {
                    file.setId(generateUniqueID());
                }
                if (StringUtils.isEmpty(file.getName())) {
                    String fileIdentifiers = String.format("Resource type: %s, resourceName: %s", resourceTypeName,
                            resource.getResourceName());
                    throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
                }
                if (file.getInputStream() == null) {
                    throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED,
                            "File stream is invalid or empty.");
                }
            }
        }
        if (StringUtils.isEmpty(resource.getTenantDomain())) {
            resource.setTenantDomain(getTenantDomain());
        }
        if (StringUtils.isEmpty(resource.getResourceType())) {
            resource.setResourceType(resourceTypeName);
        }
    }

    private ResourceType getResourceTypeByIdentifier(String resourceName, String id)
            throws ConfigurationManagementException {

        return StringUtils.isEmpty(id) ? getConfigurationDAO().getResourceTypeByName(resourceName) :
                getConfigurationDAO().getResourceTypeById(id);
    }

    private void validateResourceTypeRetrieveRequest(String resourceName) throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid resource type resourceName: " + resourceName + ".");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED, null);
        }
    }

    private void validateResourceTypeDeleteRequest(String resourceName) throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid resource type resourceName: " + resourceName + ".");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED, resourceName, null);
        }

        if (!isResourceTypeExists(resourceName)) {
            if (log.isDebugEnabled()) {
                log.debug("A resource type with the resourceName: " + resourceName + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS, resourceName);
        }
    }

    private void validateResourceTypeCreateRequest(ResourceTypeAdd resourceTypeAdd)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeAdd.getName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED, null);
        }

        if (isResourceTypeExists(resourceTypeAdd.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("A resource type with the name: " + resourceTypeAdd.getName() + " already exists.");
            }
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_ALREADY_EXISTS, resourceTypeAdd.getName());
        }
    }

    private boolean isResourceTypeExists(String resourceTypeName) throws ConfigurationManagementException {

        try {
            getResourceType(resourceTypeName);
        } catch (ConfigurationManagementClientException e) {
            if (isResourceTypeNotExistError(e)) {
                return false;
            }
            throw e;

        }
        return true;
    }

    private boolean isResourceTypeNotExistError(ConfigurationManagementClientException e) {

        return ERROR_CODE_RESOURCE_TYPE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    private ResourceType generateResourceTypeFromRequest(ResourceTypeAdd resourceTypeCreate, String resourceTypeID) {

        ResourceType resourceType = new ResourceType();
        resourceType.setName(resourceTypeCreate.getName());
        resourceType.setDescription(resourceTypeCreate.getDescription());
        resourceType.setId(resourceTypeID);

        return resourceType;
    }

    private String generateResourceTypeId(String resourceTypeName) throws ConfigurationManagementException {

        String resourceTypeID;
        if (isResourceTypeExists(resourceTypeName)) {
            resourceTypeID = getResourceType(resourceTypeName).getId();
        } else {
            resourceTypeID = generateUniqueID();
            if (log.isDebugEnabled()) {
                log.debug("Resource type id generated: " + resourceTypeID);
            }
        }
        return resourceTypeID;
    }

    private void validateResourceTypeReplaceRequest(ResourceTypeAdd resourceTypeAdd)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceTypeAdd.getName())) {
            throw handleClientException(ERROR_CODE_RESOURCE_TYPE_NAME_REQUIRED, null);
        }
    }

    private void validateAttributeGetRequest(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(resourceName) || StringUtils.isEmpty(resourceTypeName)
                || StringUtils.isEmpty(attributeKey)) {
            String attributeIdentifiers = "resourceName = " + resourceName + ", resourceTypeName = " + resourceTypeName
                    + ", attributeKey = " + attributeKey;
            throw handleClientException(ERROR_CODE_ATTRIBUTE_IDENTIFIERS_REQUIRED, attributeIdentifiers);
        }
    }

    private void validateAttributeRequest(Attribute attribute) throws ConfigurationManagementException {

        if (attribute == null || StringUtils.isEmpty(attribute.getKey())) {
            throw handleClientException(ERROR_CODE_ATTRIBUTE_REQUIRED, null);
        }
    }

    private boolean isAttributeExists(String resourceTypeName, String resourceName, String attributeKey) throws
            ConfigurationManagementException {

        try {
            getAttribute(resourceTypeName, resourceName, attributeKey);
        } catch (ConfigurationManagementClientException e) {
            if (isAttributeNotExistError(e)) {
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    private boolean isAttributeNotExistError(ConfigurationManagementClientException e) {

        return ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    private void validateAttributeAddRequest(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(attributeKey) || StringUtils.isEmpty(attributeKey)) {
            throw handleClientException(ERROR_CODE_ATTRIBUTE_REQUIRED, null);
        }
        if (isAttributeExists(resourceTypeName, resourceName, attributeKey)) {
            throw handleClientException(ERROR_CODE_ATTRIBUTE_ALREADY_EXISTS, attributeKey);
        }
    }

    private void validateAttributeDeleteRequest(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException {

        if (StringUtils.isEmpty(attributeKey) || StringUtils.isEmpty(attributeKey)) {
            throw handleClientException(ERROR_CODE_ATTRIBUTE_REQUIRED, null);
        }
        if (!isAttributeExists(resourceTypeName, resourceName, attributeKey)) {
            throw handleClientException(ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS, attributeKey);
        }
    }

    private String generateAttributeId(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException {

        String attributeId;
        if (isAttributeExists(resourceTypeName, resourceName, attribute.getKey())) {
            attributeId = getAttribute(resourceTypeName, resourceName, attribute.getKey()).getAttributeId();
        } else {
            attributeId = generateUniqueID();
            if (log.isDebugEnabled()) {
                log.debug("Attribute id generated: " + attributeId);
            }
        }
        return attributeId;
    }

    private String getResourceId(String resourceTypeName, String resourceName) throws ConfigurationManagementException {

        return getResource(resourceTypeName, resourceName).getResourceId();
    }

    private String getResourceTypeId(String resourceTypeName) throws ConfigurationManagementException {

        return getResourceType(resourceTypeName).getId();
    }

    /**
     * Select highest priority Resource DAO from an already sorted list of Resource DAOs.
     *
     * @return Highest priority Resource DAO.
     */
    private ConfigurationDAO getConfigurationDAO() throws ConfigurationManagementException {

        if (!this.configurationDAOS.isEmpty()) {
            return configurationDAOS.get(configurationDAOS.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, "configurationDAOs");
        }
    }


    @Override
    public ResourceFile addFile(String resourceTypeName, String resourceName, String fileName, InputStream fileStream)
            throws ConfigurationManagementException {

        validateFileAddRequest(resourceTypeName, resourceName, fileName, fileStream);
        String resourceId = getResourceId(resourceTypeName, resourceName);
        String fileId = generateUniqueID();
        if (log.isDebugEnabled()) {
            log.debug("File id generated: " + fileId + " for the resource name: " + resourceName + " resource type "
                    + "name: " + resourceTypeName);
        }
        getConfigurationDAO().addFile(fileId, resourceId, fileName, fileStream);
        if (log.isDebugEnabled()) {
            log.debug("File: " + fileId + " successfully added for resource name: " + resourceName
                    + " resource type name: " + resourceTypeName);
        }
        return new ResourceFile(fileId, getFilePath(fileId, resourceTypeName, resourceName), fileName);
    }

    @Override
    public List<ResourceFile> getFiles(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        validateRequest(resourceTypeName, resourceName);
        String resourceId = getResourceId(resourceTypeName, resourceName);
        List<ResourceFile> resourceFiles = getConfigurationDAO().getFiles(resourceId, resourceTypeName, resourceName);
        if (CollectionUtils.isEmpty(resourceFiles)) {
            if (log.isDebugEnabled()) {
                log.debug("Resource: " + resourceName + " does not have any files.");
            }
            throw handleClientException(ERROR_CODE_FILES_DOES_NOT_EXISTS, resourceName);
        }
        if (log.isDebugEnabled()) {
            log.debug("Files for the resource: " + resourceName + " retrieved successfully.");
        }
        return resourceFiles;
    }

    @Override
    public List<ResourceFile> getFiles(String resourceTypeName, int tenantId) throws ConfigurationManagementException {

        validateRequest(resourceTypeName);
        String resourceTypeId = getResourceTypeId(resourceTypeName);
        List<ResourceFile> resourceFiles = getConfigurationDAO().getFilesByResourceType(resourceTypeId, tenantId);
        if (CollectionUtils.isEmpty(resourceFiles)) {
            if (log.isDebugEnabled()) {
                log.debug("Resource type: " + resourceTypeName + "  in tenant: " + getTenantDomain() +
                        " does not have any files.");
            }
            throw handleClientException(ERROR_CODE_FILES_DOES_NOT_EXISTS, resourceTypeName);
        }
        if (log.isDebugEnabled()) {
            log.debug("Files for the resource type: " + resourceTypeName + " retrieved successfully.");
        }
        return resourceFiles;
    }

    @Override
    public void deleteFiles(String resourceTypeName, String resourceName) throws ConfigurationManagementException {

        validateRequest(resourceTypeName, resourceName);
        String resourceId = getResourceId(resourceTypeName, resourceName);
        getConfigurationDAO().deleteFiles(resourceId);
        if (log.isDebugEnabled()) {
            log.debug("All the files were deleted in the resource: " + resourceName + ".");
        }
    }

    @Override
    public InputStream getFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException {

        validateRequest(resourceType, resourceName, fileId);
        InputStream fileStream = getConfigurationDAO().getFileById(resourceType, resourceName, fileId);
        if (fileStream == null) {
            if (log.isDebugEnabled()) {
                log.debug("Resource File: " + fileId + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_FILE_DOES_NOT_EXISTS, fileId);
        }
        if (log.isDebugEnabled()) {
            log.debug("Resource file: " + fileId + " retrieved successfully.");
        }
        return fileStream;
    }

    @Override
    public void deleteFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException {

        validateRequest(resourceType, resourceName, fileId);
        validateFileExistence(resourceType, resourceName, fileId);
        getConfigurationDAO().deleteFileById(resourceType, resourceName, fileId);
        if (log.isDebugEnabled()) {
            log.debug("File: " + fileId + " successfully deleted.");
        }
    }

    private void validateRequest(String resourceTypeName, String resourceName)
            throws ConfigurationManagementClientException {

        if (StringUtils.isEmpty(resourceTypeName) || StringUtils.isEmpty(resourceName)) {
            String fileIdentifiers = "resourceType: " + resourceTypeName + ", resourceName: " + resourceName;
            throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
        }
    }

    private void validateRequest(String resourceTypeName) throws ConfigurationManagementClientException {

        if (StringUtils.isEmpty(resourceTypeName)) {
            String fileIdentifiers = "resourceType: " + resourceTypeName;
            throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
        }
    }

    private void validateRequest(String resourceTypeName, String resourceName, String fileId) throws
            ConfigurationManagementClientException {

        if (StringUtils.isEmpty(resourceTypeName) || StringUtils.isEmpty(resourceName) || StringUtils.isEmpty(fileId)) {
            String fileIdentifiers = "resourceType: " + resourceTypeName + ", resourceName: " + resourceName + ", "
                    + "fileId: " + fileId;
            throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
        }
    }

    private void validateFileExistence(String resourceTypeName, String resourceName, String fileId) throws
            ConfigurationManagementException {

        if (!isFileExists(resourceTypeName, resourceName, fileId)) {
            if (log.isDebugEnabled()) {
                log.debug("A file with the id: " + fileId + " does not exists.");
            }
            throw handleClientException(ERROR_CODE_FILE_DOES_NOT_EXISTS, fileId);
        }
    }

    private void validateFileAddRequest(String resourceTypeName, String resourceName, String fileName,
                                        InputStream fileStream) throws ConfigurationManagementClientException {

        if (StringUtils.isEmpty(resourceTypeName) || StringUtils.isEmpty(resourceName) || StringUtils
                .isEmpty(fileName)) {
            String fileIdentifiers =
                    "Resource type: " + resourceTypeName + ", resourceName: " + resourceName + ". " + "FileName: "
                            + fileName;
            throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, fileIdentifiers);
        }
        if (fileStream == null) {
            throw handleClientException(ERROR_CODE_FILE_IDENTIFIERS_REQUIRED, "fileStream is invalid.");
        }
    }

    private boolean isFileExists(String resourceTypeName, String resourceName, String fileId)
            throws ConfigurationManagementException {

        try {
            getFileById(resourceTypeName, resourceName, fileId);
        } catch (ConfigurationManagementClientException e) {
            if (isFileNotExistsError(e)) {
                return false;
            }
            throw e;
        }
        return true;
    }

    private boolean isFileNotExistsError(ConfigurationManagementClientException e) {

        return ERROR_CODE_FILE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode());
    }

    @Override
    public Resource getTenantResourceById(String resourceId) throws ConfigurationManagementException {

        if (StringUtils.isBlank(resourceId)) {
            throw handleClientException(ERROR_CODE_INVALID_RESOURCE_ID, resourceId);
        }
        Resource resource = this.getConfigurationDAO().getTenantResourceById(getTenantId(), resourceId);
        if (resource == null) {
            throw handleClientException(ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS, resourceId);
        }
        return resource;
    }

    @Override
    public void deleteResourceById(String resourceId) throws ConfigurationManagementException {

        if (StringUtils.isBlank(resourceId)) {
            throw handleClientException(ERROR_CODE_INVALID_RESOURCE_ID, resourceId);
        }
        if (isResourceExistsById(resourceId)) {
            this.getConfigurationDAO().deleteResourceById(getTenantId(), resourceId);
            if (log.isDebugEnabled()) {
                log.debug("Resource id: " + resourceId + " in tenant: " + getTenantDomain() + " deleted successfully.");
            }
        } else {
            throw handleClientException(ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS, resourceId);
        }
    }

    @Override
    public void replaceResource(Resource resource) throws ConfigurationManagementException {

        String resourceId = resource.getResourceId();
        if (StringUtils.isBlank(resourceId)) {
            throw handleClientException(ERROR_CODE_INVALID_RESOURCE_ID, resourceId);
        }
        if (isResourceExistsById(resource.getResourceId())) {
            this.getConfigurationDAO().replaceResourceWithFiles(resource);
            if (log.isDebugEnabled()) {
                log.debug("Resource id: " + resourceId + " in tenant: " + getTenantDomain() + " updated successfully.");
            }
        } else {
            throw handleClientException(ErrorMessages.ERROR_CODE_RESOURCE_ID_DOES_NOT_EXISTS, resourceId);
        }
    }
}
