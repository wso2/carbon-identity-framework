/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.configuration.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.configuration.mgt.core.cache.ResourceByIdCache;
import org.wso2.carbon.identity.configuration.mgt.core.cache.ResourceByNameCache;
import org.wso2.carbon.identity.configuration.mgt.core.cache.ResourceByNameCacheKey;
import org.wso2.carbon.identity.configuration.mgt.core.cache.ResourceCacheEntry;
import org.wso2.carbon.identity.configuration.mgt.core.cache.ResourceByIdCacheKey;
import org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.List;

import static org.wso2.carbon.identity.configuration.mgt.core.util.ConfigurationUtils.handleClientException;

/**
 * This is a wrapper data access object to the default data access object to provide caching functionalities.
 */
public class CachedBackedConfigurationDAO implements ConfigurationDAO {

    private static final Log log = LogFactory.getLog(CachedBackedConfigurationDAO.class);
    private final ConfigurationDAO configurationDAO;
    private final ResourceByIdCache resourceByIdCache;
    private final ResourceByNameCache resourceByNameCache;

    public CachedBackedConfigurationDAO(ConfigurationDAO configurationDAO) {
    
        this.configurationDAO = configurationDAO;
        this.resourceByIdCache = ResourceByIdCache.getInstance();
        this.resourceByNameCache = ResourceByNameCache.getInstance();
    }

    @Override
    public int getPriority() {

        return 2;
    }

    @Override
    public Resources getTenantResources(Condition condition) throws ConfigurationManagementException {

        return configurationDAO.getTenantResources(condition);
    }

    @Override
    public Resource getResourceByName(int tenantId, String resourceTypeId, String name)
            throws ConfigurationManagementException {

        Resource resource = getResourceFromCacheByName(name, tenantId);
        if (resource != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for resource by it's name. Resource name: %s, Tenant id: " +
                        "%d, Resource type id: %s", name, tenantId, resourceTypeId);
                log.debug(message);
            }
            return resource;
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for resource by it's name. Resource name: %s, Tenant id: " +
                        "%d, Resource type id: %s", name, tenantId, resourceTypeId);
                log.debug(message);
            }
            resource = configurationDAO.getResourceByName(tenantId, resourceTypeId, name);
            addResourceToCache(resource);
        }
        return resource;
    }

    @Override
    public Resource getResourceById(String resourceId) throws ConfigurationManagementException {

        Resource resource = getResourceFromCacheById(resourceId, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        if (resource != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache hit for resource by it's id. Resource id: %s", resourceId);
                log.debug(message);
            }
            return resource;
        } else {
            if (log.isDebugEnabled()) {
                String message = String.format("Cache miss for resource by it's id. Resource id: %s", resourceId);
                log.debug(message);
            }
            resource = configurationDAO.getResourceById(resourceId);
            addResourceToCache(resource);
        }
        return resource;
    }

    @Override
    public Resource getTenantResourceById(int tenantId, String resourceId) throws ConfigurationManagementException {

        return configurationDAO.getTenantResourceById(tenantId, resourceId);
    }

    @Override
    public void deleteResourceById(int tenantId, String resourceId) throws ConfigurationManagementException {

        configurationDAO.deleteResourceById(tenantId, resourceId);
        deleteCacheByResourceId(resourceId, tenantId);
    }

    @Override
    public void replaceResourceWithFiles(Resource resource) throws ConfigurationManagementException {

        configurationDAO.replaceResourceWithFiles(resource);
        deleteResourceFromCache(resource);
    }

    @Override
    public void deleteResourceByName(int tenantId, String resourceTypeId, String name)
            throws ConfigurationManagementException {

        configurationDAO.deleteResourceByName(tenantId, resourceTypeId, name);
        deleteCacheByResourceByName(name, tenantId);
    }

    @Override
    public void addResource(Resource resource) throws ConfigurationManagementException {

        configurationDAO.addResource(resource);
        addResourceToCache(resource);
    }

    @Override
    public void replaceResource(Resource resource) throws ConfigurationManagementException {

        configurationDAO.replaceResource(resource);
        deleteResourceFromCache(resource);
    }

    @Override
    public void addResourceType(ResourceType resourceType) throws ConfigurationManagementException {

        configurationDAO.addResourceType(resourceType);
    }

    @Override
    public void replaceResourceType(ResourceType resourceType) throws ConfigurationManagementException {

        configurationDAO.replaceResourceType(resourceType);
    }

    @Override
    public ResourceType getResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException {

        return configurationDAO.getResourceTypeByName(resourceTypeName);
    }

    @Override
    public ResourceType getResourceTypeById(String resourceTypeId) throws ConfigurationManagementException {

        return configurationDAO.getResourceTypeById(resourceTypeId);
    }

    @Override
    public void deleteResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException {

        configurationDAO.deleteResourceTypeByName(resourceTypeName);
    }

    @Override
    public Attribute getAttributeByKey(String resourceId, String attributeKey) throws ConfigurationManagementException {

        return configurationDAO.getAttributeByKey(resourceId, attributeKey);
    }

    @Override
    public void updateAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        configurationDAO.updateAttribute(attributeId, resourceId, attribute);
    }

    @Override
    public void addAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        configurationDAO.addAttribute(attributeId, resourceId, attribute);
    }

    @Override
    public void replaceAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException {

        configurationDAO.replaceAttribute(attributeId, resourceId, attribute);
    }

    @Override
    public void deleteAttribute(String attributeId, String resourceId, String attributeKey)
            throws ConfigurationManagementException {

        configurationDAO.deleteAttribute(attributeId, resourceId, attributeKey);
    }

    @Override
    public void addFile(String fileId, String resourceId, String fileName, InputStream fileStream)
            throws ConfigurationManagementException {

        configurationDAO.addFile(fileId, resourceId, fileName, fileStream);
    }

    @Override
    public InputStream getFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException {

        return configurationDAO.getFileById(resourceType, resourceName, fileId);
    }

    @Override
    public List<ResourceFile> getFiles(String resourceId, String resourceTypeName, String resourceName)
            throws ConfigurationManagementException {

        return configurationDAO.getFiles(resourceId, resourceTypeName, resourceName);
    }

    @Override
    public List<ResourceFile> getFilesByResourceType(String resourceTypeId, int tenantId)
            throws ConfigurationManagementException {

        return configurationDAO.getFilesByResourceType(resourceTypeId, tenantId);
    }

    @Override
    public void deleteFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException {

        configurationDAO.deleteFileById(resourceType, resourceName, fileId);
    }

    @Override
    public void deleteFiles(String resourceId) throws ConfigurationManagementException {

        configurationDAO.deleteFiles(resourceId);
    }

    @Override
    public List getResourcesByType(int tenantId, String resourceTypeId) throws ConfigurationManagementException {

        return configurationDAO.getResourcesByType(tenantId, resourceTypeId);
    }

    @Override
    public boolean isExistingResource(int tenantId, String resourceId) throws ConfigurationManagementException {

        return configurationDAO.isExistingResource(tenantId, resourceId);
    }

    @Override
    public void deleteResourcesByType(int tenantId, String resourceTypeId) throws ConfigurationManagementException {

        List<Resource> resourceList = configurationDAO.getResourcesByType(tenantId, resourceTypeId);
        if (resourceList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No resource found for the resourceTypeId: " + resourceTypeId);
            }
            throw handleClientException(ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCES_DOES_NOT_EXISTS, null);
        }
        configurationDAO.deleteResourcesByType(tenantId, resourceTypeId);
        for (Resource resource : resourceList) {
            deleteResourceFromCache(resource);
        }
    }

    private Resource getResourceFromCacheById(String resourceId, int tenantId)
            throws ConfigurationManagementException {

        try {
            String tenantDomain = ConfigurationManagerComponentDataHolder.getInstance().getRealmService()
                    .getTenantManager().getDomain(tenantId);
            return getResourceFromCacheById(resourceId, tenantDomain);
        } catch (UserStoreException e) {
            throw new ConfigurationManagementException("Error when setting tenant domain. ",
                    ConfigurationConstants.ErrorMessages.ERROR_CODE_UNEXPECTED.getCode(), e);
        }
    }

    private Resource getResourceFromCacheByName(String resourceName, int tenantId)
            throws ConfigurationManagementException {

        try {
            String tenantDomain = ConfigurationManagerComponentDataHolder.getInstance().getRealmService()
                    .getTenantManager().getDomain(tenantId);
            return getResourceFromCacheByName(resourceName, tenantDomain);
        } catch (UserStoreException e) {
            throw new ConfigurationManagementException("Error when setting tenant domain. ",
                    ConfigurationConstants.ErrorMessages.ERROR_CODE_UNEXPECTED.getCode(), e);
        }
    }

    private Resource getResourceFromCacheById(String resourceId, String tenantDomain)
            throws ConfigurationManagementException {

        ResourceByIdCacheKey resourceByIdCacheKey = new ResourceByIdCacheKey(resourceId, tenantDomain);
        ResourceCacheEntry resourceCacheEntry = resourceByIdCache.getValueFromCache(resourceByIdCacheKey);
        if (resourceCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from Resource by id cache. Resource id: %s., Tenant " +
                        "domain %s", resourceId, tenantDomain);
                log.debug(message);
            }
            return resourceCacheEntry.getResource();
        }
        return null;
    }

    private Resource getResourceFromCacheByName(String resourceName, String tenantDomain)
            throws ConfigurationManagementException {

        ResourceByNameCacheKey resourceByNameCacheKey = new ResourceByNameCacheKey(resourceName, tenantDomain);
        ResourceCacheEntry resourceCacheEntry = resourceByNameCache.getValueFromCache(resourceByNameCacheKey);
        if (resourceCacheEntry != null) {
            if (log.isDebugEnabled()) {
                String message = String.format("Entry found from Resource by name cache. Resource id: %s., Tenant" +
                        " domain %s", resourceName, tenantDomain);
                log.debug(message);
            }
            return resourceCacheEntry.getResource();
        }
        return null;
    }

    private void addResourceToCache(Resource resource) throws ConfigurationManagementException {

        if (resource == null) {
            return;
        }
        ResourceByIdCacheKey resourceByIdCacheKey = new ResourceByIdCacheKey(resource.getResourceId(),
                resource.getTenantDomain());
        ResourceByNameCacheKey resourceByNameCacheKey = new ResourceByNameCacheKey(resource.getResourceName(),
                resource.getTenantDomain());
        ResourceCacheEntry resourceCacheEntry = new ResourceCacheEntry(resource);
        if (log.isDebugEnabled()) {
            String message = String.format("Following two cache entries created. 1. Resource by name cache %s, 2." +
                            " Resource by id cache %s. Tenant domain for all caches: %s", resource.getResourceName(),
                    resource.getResourceId(), resource.getTenantDomain());
            log.debug(message);
        }
        resourceByIdCache.addToCache(resourceByIdCacheKey, resourceCacheEntry);
        resourceByNameCache.addToCache(resourceByNameCacheKey, resourceCacheEntry);
    }

    private void deleteResourceFromCache(Resource resource) throws ConfigurationManagementException {

        if (resource == null) {
            return;
        }
        ResourceByIdCacheKey resourceByIdCacheKey = new ResourceByIdCacheKey(resource.getResourceId(),
                resource.getTenantDomain());
        ResourceByNameCacheKey resourceByNameCacheKey = new ResourceByNameCacheKey(resource.getResourceName(),
                resource.getTenantDomain());

        if (log.isDebugEnabled()) {
            String message = String.format("Following two cache entries deleted. 1. Resource by name cache %s, 2." +
                            " Resource by id cache %s. Tenant domain for all caches: %s", resource.getResourceName(),
                    resource.getResourceId(), resource.getTenantDomain());
            log.debug(message);
        }

        resourceByIdCache.clearCacheEntry(resourceByIdCacheKey);
        resourceByNameCache.clearCacheEntry(resourceByNameCacheKey);
    }

    private void deleteCacheByResourceId(String resourceId, int tenantId) throws ConfigurationManagementException {

        Resource resource = getResourceFromCacheById(resourceId, tenantId);
        if (resource == null) {
            return;
        }
        deleteResourceFromCache(resource);
    }

    private void deleteCacheByResourceByName(String resourceName, int tenantId) throws ConfigurationManagementException {

        Resource resource = getResourceFromCacheByName(resourceName, tenantId);
        if (resource == null) {
            return;
        }
        deleteResourceFromCache(resource);
    }
}
