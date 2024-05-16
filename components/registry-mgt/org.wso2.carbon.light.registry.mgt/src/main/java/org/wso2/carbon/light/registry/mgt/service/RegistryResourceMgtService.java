/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.light.registry.mgt.service;

import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.light.registry.mgt.model.Resource;
import org.wso2.carbon.light.registry.mgt.model.ResourceImpl;

public interface RegistryResourceMgtService {

    /**
     * Get a registry resource from a tenant registry.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @return Registry resource.
     * @throws IdentityRuntimeException If an error occurs while retrieving the resource.
     */
    Resource getResource(String path,
                         String tenantDomain) throws IdentityRuntimeException;

    /**
     * Update and replace a registry resource in tenant registry
     *
     * @param resource     Resource to be updated.
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @throws IdentityRuntimeException If an error occurs while updating the resource.
     */
    void putResource(Resource resource,
                     String path,
                     String tenantDomain) throws IdentityRuntimeException;

    /**
     * Add a registry resource to tenant registry. If a resource already exists at the specified path an exception
     * will be thrown.
     *
     * @param resource     Resource to be added.
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @throws IdentityRuntimeException If an error occurs while adding the resource.
     */
    void addResource(Resource resource,
                     String path,
                     String tenantDomain) throws IdentityRuntimeException;

    /**
     * Remove a registry resource from a tenant registry.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @throws IdentityRuntimeException If an error occurs while deleting the resource.
     */
    void deleteResource(String path,
                        String tenantDomain) throws IdentityRuntimeException;

    /**
     * Check whether a resource exists in the given path in the tenant registry.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @return True if the resource exists, false otherwise.
     * @throws IdentityRuntimeException If an error occurs while checking the existence of the resource.
     */
    boolean resourceExists(String path,
                           String tenantDomain) throws IdentityRuntimeException;

}
