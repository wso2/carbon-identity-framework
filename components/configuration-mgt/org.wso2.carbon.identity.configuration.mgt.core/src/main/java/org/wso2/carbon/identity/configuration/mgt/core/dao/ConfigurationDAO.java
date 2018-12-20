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
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resource;
import org.wso2.carbon.identity.configuration.mgt.core.model.ResourceType;
import org.wso2.carbon.identity.configuration.mgt.core.model.Resources;
import org.wso2.carbon.identity.configuration.mgt.core.search.Condition;

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
    void deleteAttribute(String attributeId, String resourceId, String attributeKey) throws ConfigurationManagementException;
}
