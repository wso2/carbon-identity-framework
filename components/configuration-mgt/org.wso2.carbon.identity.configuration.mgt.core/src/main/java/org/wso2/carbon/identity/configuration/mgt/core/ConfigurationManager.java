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

import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.exception.NotImplementedException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceFile;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceTypeAdd;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;

import java.io.InputStream;
import java.util.List;

/**
 * Resource manager service interface.
 */
public interface ConfigurationManager {

    /**
     * This API is used to get resources from all the tenants filtered with the {@link Condition}.
     *
     * @param searchCondition {@link Condition} representing a search filter for resources.
     * @return {@link Resources} object with a collection of resources matching to the given {@link Condition}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     * @deprecated use {@link ConfigurationManager#getTenantResources(String, Condition)} method.
     */
    @Deprecated
    Resources getTenantResources(Condition searchCondition) throws ConfigurationManagementException;

    /**
     * This API is used to get resources from a specific tenant filtered with the {@link Condition}.
     *
     * @param tenantDomain    Tenant domain.
     * @param searchCondition {@link Condition} representing a search filter for resources.
     * @return {@link Resources} object with a collection of resources matching to the given {@link Condition}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resources getTenantResources(String tenantDomain, Condition searchCondition) throws
            ConfigurationManagementException;

    /**
     * This API is used to store a new {@link ResourceType}.
     *
     * @param resourceTypeAdd {@link ResourceType} create request.
     * @return 201 created. Returns created {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    ResourceType addResourceType(ResourceTypeAdd resourceTypeAdd) throws ConfigurationManagementException;

    /**
     * This API is used to replace an existing {@link ResourceType} with the given one or create if not.
     *
     * @param resourceTypeAdd Request to create the {@link ResourceType}.
     * @return 200 ok. Returns replaced {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    ResourceType replaceResourceType(ResourceTypeAdd resourceTypeAdd) throws ConfigurationManagementException;

    /**
     * This API is used to retrieve the {@link ResourceType}.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @return 200 ok. Returns the corresponding {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    ResourceType getResourceType(String resourceTypeName) throws ConfigurationManagementException;

    /**
     * This API is used to delete the {@link ResourceType}.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    void deleteResourceType(String resourceTypeName) throws ConfigurationManagementException;

    /**
     * Get all the resources of the current tenant.
     *
     * @return 200 ok. {@link Resources} object with all the tenant resources.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resources getResources() throws ConfigurationManagementException;

    /**
     * Get all the resources belonging to the given {@link ResourceType} for a given tenant ID.

     * @param tenantId         The ID of the tenant.
     * @param resourceTypeName {@link ResourceType} object name.
     * @return {@link Resources} object with all the resources of the given resource type name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    default Resources getResourcesByType(int tenantId, String resourceTypeName)
            throws ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }

    /**
     * Get all the resources belonging to the given {@link ResourceType}.
     *
     * @param resourceTypeName {@link ResourceType} object name.
     * @return {@link Resources} object with all the resources of the given resource type name.
     * @throws ConfigurationManagementException Configuration Management Exception.
     */
    Resources getResourcesByType(String resourceTypeName) throws ConfigurationManagementException;

    /**
     * This API is used to create the given resource.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceAdd      Request to create the {@link Resource}.
     * @return 201 created. Returns {@link Resource} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Resource addResource(String resourceTypeName, ResourceAdd resourceAdd) throws ConfigurationManagementException;

    /**
     * This API is used to create the given resource including a file.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resource         The {@link Resource}.
     * @return 201 created. Returns {@link Resource} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    default Resource addResource(String resourceTypeName, Resource resource) throws ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }

    /**
     * This API is used to replace the given resource or create if not exists.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceAdd      Request to replace the {@link Resource}.
     * @return 201 created. Returns {@link Resource} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Resource replaceResource(String resourceTypeName, ResourceAdd resourceAdd) throws ConfigurationManagementException;

    /**
     * This API is used to replace the given resource inclduing the file.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resource         The  {@link Resource}.
     * @return 201 created. Returns {@link Resource} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    default Resource replaceResource(String resourceTypeName, Resource resource)
            throws ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }

    /**
     * This API is used to retrieve the given resource.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @return 200 ok. Returns {@link Resource} requested.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Resource getResource(String resourceTypeName, String resourceName) throws ConfigurationManagementException;

    /**
     * This API is used to retrieve the given resource by the tenant ID.
     *
     * @param tenantId         The ID of the tenant.
     * @param resourceTypeName The name of the {@link ResourceType}.
     * @param resourceName     The name of the {@link ResourceType}.
     * @return {@link Resource}
     * @throws ConfigurationManagementException Configuration management exception.
     */
    default Resource getResourceByTenantId(int tenantId, String resourceTypeName, String resourceName) throws
            ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }

    /**
     * This API is used to delete the given resource.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Request to delete the {@link Resource}.
     * @throws ConfigurationManagementException Resource management exception.
     */
    void deleteResource(String resourceTypeName, String resourceName) throws ConfigurationManagementException;

    /**
     * This API is used to create the given attribute.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param attribute        Request to create the {@link Attribute}.
     * @return 201 created. Returns {@link Attribute} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Attribute addAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * This API is used to update the given attribute.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param attribute        Request to update the {@link Attribute}.
     * @return 200 ok. Returns {@link Attribute} updated.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Attribute updateAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * This API is used to replace the given attribute or create if exists.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param attribute        Request to replace the {@link Attribute}.
     * @return 200 ok. Returns {@link Attribute} replaced.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Attribute replaceAttribute(String resourceTypeName, String resourceName, Attribute attribute)
            throws ConfigurationManagementException;

    /**
     * This API is used to retrieve the given attribute.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param attributeKey     Key of the {@link Attribute}.
     * @return 200 ok. Returns {@link Attribute} requested.
     * @throws ConfigurationManagementException Resource management exception.
     */
    Attribute getAttribute(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException;

    /**
     * This API is used to retrieve the given attribute.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param attributeKey     Key of the {@link Attribute}.
     * @throws ConfigurationManagementException Resource management exception.
     */
    void deleteAttribute(String resourceTypeName, String resourceName, String attributeKey)
            throws ConfigurationManagementException;

    /**
     * This API is used to add the given file.
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @param name             Name of the {@link ResourceFile}
     * @param fileStream       {@link InputStream} representing the file.
     * @return 200 ok. Returns {@link ResourceFile} created.
     * @throws ConfigurationManagementException Resource management exception.
     */
    ResourceFile addFile(String resourceTypeName, String resourceName, String name, InputStream fileStream)
            throws ConfigurationManagementException;

    /**
     * This API is used to get all files for the given {@link Resource}
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @return 200 ok. Returns a list of {@link ResourceFile} for the {@link Resource}.
     * @throws ConfigurationManagementException Resource management exception.
     */
    List<ResourceFile> getFiles(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException;

    /**
     * This API is used to get all files for the given resource type
     *
     * @param resourceTypeName Name of the resource type.
     * @param tenantId         Tenant id.
     * @return List of resource file for the resource type.
     * @throws ConfigurationManagementException Resource management exception.
     */
    List<ResourceFile> getFiles(String resourceTypeName, int tenantId)
            throws ConfigurationManagementException;

    /**
     * This API is used to delete all files for the given {@link Resource}
     *
     * @param resourceTypeName Name of the {@link ResourceType}.
     * @param resourceName     Name of the {@link Resource}.
     * @return 200 ok.
     * @throws ConfigurationManagementException Resource management exception.
     */
    void deleteFiles(String resourceTypeName, String resourceName)
            throws ConfigurationManagementException;

    /**
     * This API is used to get the given file.
     *
     * @param resourceType resource type name.
     * @param resourceName resource name.
     * @param fileId       Id representing the file.
     * @return 200 ok. Returns {@link InputStream} of the file requested.
     * @throws ConfigurationManagementException Resource management exception.
     */
    InputStream getFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException;

    /**
     * This API is used to delete the given file.
     *
     * @param resourceType resource type name.
     * @param resourceName resource name.
     * @param fileId       Id representing the file.
     * @return 200 ok.
     * @throws ConfigurationManagementException Resource management exception.
     */
    void deleteFileById(String resourceType, String resourceName, String fileId)
            throws ConfigurationManagementException;

    /**
     * This function is used to get a resource by the resource id.
     *
     * @param resourceId Id representing the resource.
     * @return the resource object corresponding to the resource id.
     * @throws ConfigurationManagementException Configuration management exception.
     */
    Resource getTenantResourceById(String resourceId) throws ConfigurationManagementException;

    /**
     * This function is used to delete the given resource id.
     *
     * @param resourceId Request to delete the {@link Resource}.
     * @throws ConfigurationManagementException Configuration management exception.
     */
    void deleteResourceById(String resourceId) throws ConfigurationManagementException;

    /**
     * This function is used to replace a given resource along with all its file.
     *
     * @param resource resource object.
     * @throws ConfigurationManagementException Configuration management exception.
     */
    void replaceResource(Resource resource) throws ConfigurationManagementException;

    /**
     * This function is used to delete all the resources belongs to given resource type in the current tenant.
     *
     * @param resourceType Request to delete the resources for the given {@link ResourceType}.
     * @throws ConfigurationManagementException Configuration management exception.
     */
    default void deleteResourcesByType(String resourceType) throws ConfigurationManagementException {

        throw new NotImplementedException("This functionality is not implemented.");
    }
}
