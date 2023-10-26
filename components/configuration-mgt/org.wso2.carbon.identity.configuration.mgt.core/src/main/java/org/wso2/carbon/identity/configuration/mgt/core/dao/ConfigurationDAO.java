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

package org.wso2.carbon.identity.configuration.mgt.core.dao;

import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.NotImplementedException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;

import java.io.InputStream;
import java.util.List;

/**
 * Perform CRUD operations for {@link Resource}.
 *
 * @since 1.0.0
 */
public interface ConfigurationDAO {

    /**
     * Get priority value for the {@link ConfigurationDAO}.
     *
     * @return Priority value for the DAO.
     */
    int getPriority();

    /**
     * Get a {@link Resources} object across tenants based on the search filter described by a {@link Condition}.
     *
     * @param condition Search condition.
     * @return Collection of {@link Resource} objects based on the search condition.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resources getTenantResources(Condition condition) throws ConfigurationManagementException;

    /**
     * Returns {@link Resource} by name.
     *
     * @param tenantId       Tenant id of the {@link Resource}.
     * @param resourceTypeId Id of the {@link ResourceType} for the {@link Resource}.
     * @param name           Name of the {@link Resource}.
     * @return {@link Resource} for the given name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resource getResourceByName(int tenantId, String resourceTypeId, String name)
            throws ConfigurationManagementException;

    /**
     * Returns {@link Resource} by id.
     *
     * @param resourceId Id value of the {@link Resource} to be returned.
     * @return {@link Resource} for the given name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resource getResourceById(String resourceId) throws ConfigurationManagementException;

    /**
     * Returns {@link Resource} by id given the tenant id.
     *
     * @param tenantId   Tenant id of the {@link Resource}.
     * @param resourceId Id value of the {@link Resource} to be returned.
     * @return {@link Resource} for the given name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resource getTenantResourceById(int tenantId, String resourceId) throws ConfigurationManagementException;

    /**
     * Delete {@link Resource} by the given resourceName.
     *
     * @param tenantId   Tenant id of the {@link Resource}.
     * @param resourceId Id of the {@link Resource}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void deleteResourceById(int tenantId, String resourceId) throws ConfigurationManagementException;

    /**
     * Update {@link Resource} and {@link ResourceFile} associated to the given resource object.
     *
     * @param resource {@link Resource} object.
     */
    void replaceResourceWithFiles(Resource resource) throws ConfigurationManagementException;

    /**
     * Delete {@link Resource} by the given resourceName.
     *
     * @param tenantId       Tenant id of the {@link Resource}.
     * @param resourceTypeId Id of the {@link ResourceType} for the {@link Resource}.
     * @param name           Name of the {@link Resource}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void deleteResourceByName(int tenantId, String resourceTypeId, String name) throws ConfigurationManagementException;

    /**
     * Add {@link Resource}.
     *
     * @param resource {@link Resource} to be added.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void addResource(Resource resource) throws ConfigurationManagementException;

    /**
     * Replace {@link Resource} or create not exists.
     *
     * @param resource {@link Resource} to be added.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void replaceResource(Resource resource) throws ConfigurationManagementException;

    /**
     * Add {@link ResourceType}.
     *
     * @param resourceType {@link ResourceType} to be added.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void addResourceType(ResourceType resourceType) throws ConfigurationManagementException;

    /**
     * Replace {@link ResourceType}.
     *
     * @param resourceType {@link ResourceType} to be replaced.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void replaceResourceType(ResourceType resourceType) throws ConfigurationManagementException;

    /**
     * Get {@link ResourceType} by name.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @return {@link ResourceType} for the given name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    ResourceType getResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException;

    /**
     * Get {@link ResourceType} by id.
     *
     * @param resourceTypeId Id of the {@link ResourceType}.
     * @return {@link ResourceType} for the given id.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    ResourceType getResourceTypeById(String resourceTypeId) throws ConfigurationManagementException;

    /**
     * Delete {@link ResourceType} by name.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void deleteResourceTypeByName(String resourceTypeName) throws ConfigurationManagementException;

    /**
     * Get {@link Attribute} by name.
     *
     * @param resourceId   Id of the {@link ResourceType}.
     * @param attributeKey Key of the {@link Attribute}.
     * @return {@link Attribute} for the given name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Attribute getAttributeByKey(String resourceId, String attributeKey) throws ConfigurationManagementException;

    /**
     * Update {@link Attribute} by Id.
     *
     * @param attributeId Id of the {@link Attribute}.
     * @param attribute   {@link Attribute} to be updated.
     * @param resourceId  Id of the {@link Resource} this {@link Attribute} belongs to.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void updateAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * Add {@link Attribute}.
     *
     * @param attributeId Id of the {@link Attribute}.
     * @param attribute   {@link Attribute} to be created.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void addAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * Replace {@link Attribute}.
     *
     * @param attributeId Id of the {@link Attribute}.
     * @param attribute   {@link Attribute} to be replaced.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void replaceAttribute(String attributeId, String resourceId, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * Delete {@link Attribute} by key.
     *
     * @param attributeId  Id of the {@link Attribute}.
     * @param attributeKey Key of the {@link Attribute} to be updated.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void deleteAttribute(String attributeId, String resourceId, String attributeKey)
            throws ConfigurationManagementException;

    /**
     * Add a file.
     *
     * @param fileId     Id of the file.
     * @param resourceId Id of the {@link Resource}.
     * @param fileName   Name of the {@link ResourceFile}
     * @param fileStream {@link InputStream} representing the file.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void addFile(String fileId, String resourceId, String fileName, InputStream fileStream)
            throws ConfigurationManagementException;

    /**
     * Get the file.
     *
     * @param resourceType resource type name.
     * @param resourceName resource name.
     * @param fileId       Id of the file.
     * @return {@link InputStream} for the given file id.
     */
    InputStream getFileById(String resourceType, String resourceName, String fileId) throws
            ConfigurationManagementException;

    /**
     * Get files for the {@link Resource}.
     *
     * @param resourceId       Id of the {@link Resource}.
     * @param resourceTypeName Name of the {@link ResourceType}
     * @param resourceName     Name of the {@link Resource}.
     * @return A list of {@link ResourceFile} for the given resource.
     */
    List<ResourceFile> getFiles(String resourceId, String resourceTypeName, String resourceName) throws
            ConfigurationManagementException;

    /**
     * Get files for the {@link ResourceType}.
     *
     * @param resourceTypeId Id of the {@link ResourceType}.
     * @return A list of {@link ResourceFile} for the given resource.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    default List<ResourceFile> getFilesByResourceType(String resourceTypeId) throws ConfigurationManagementException {

        throw new ConfigurationManagementException("This method is not implemented", null);
    }

    /**
     * Get files by resource type Id and tenant Id.
     *
     * @param resourceTypeId Id of the {@link ResourceType}.
     * @param tenantId       Id of the tenant.
     * @return A list of {@link ResourceFile} for the given resource.
     */
    default List<ResourceFile> getFilesByResourceType(String resourceTypeId, int tenantId)
            throws ConfigurationManagementException {

        throw new ConfigurationManagementException("This method is not implemented", null);
    }
    /**
     * Delete the file.
     *
     * @param resourceType resource type name.
     * @param resourceName resource name.
     * @param fileId       Id of the file.
     * @return {@link InputStream} for the given file id.
     */
    void deleteFileById(String resourceType, String resourceName, String fileId) throws
            ConfigurationManagementException;

    /**
     * Delete files for the {@link Resource}.
     *
     * @param resourceId Id of the {@link Resource}.
     * @return A list of {@link ResourceFile} for the given resource.
     */
    void deleteFiles(String resourceId) throws ConfigurationManagementException;

    /**
     * Get resources for the {@link ResourceType}.
     *
     * @param tenantId       Id of the tenant.
     * @param resourceTypeId of the {@link ResourceType}.
     * @return A list of {@link Resource} for the given resource type
     */
    List getResourcesByType(int tenantId, String resourceTypeId)
            throws ConfigurationManagementException;

    /**
     * Validates whether a resource exists with the given resource id in the tenant domain.
     *
     * @param tenantId id of the considered tenant domain.
     * @param resourceId id of the resource.
     * @return whether the resource exists or not.
     * @throws ConfigurationManagementException if an error occurs while validating the resourceId.
     */
    boolean isExistingResource(int tenantId, String resourceId) throws ConfigurationManagementException;

    /**
     * Delete all the resources by the given resource type in the given tenant.
     *
     * @param tenantId       Id of the tenant
     * @param resourceTypeId Id of the {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    default void deleteResourcesByType(int tenantId, String resourceTypeId) throws ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }
}
