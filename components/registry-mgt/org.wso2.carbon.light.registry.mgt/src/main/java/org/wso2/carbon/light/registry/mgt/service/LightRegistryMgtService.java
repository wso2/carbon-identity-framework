/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.light.registry.mgt.service;

import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.light.registry.mgt.model.Resource;

public interface LightRegistryMgtService {

    /**
     * Get a registry resource.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @return Registry resource.
     * @throws IdentityRuntimeException If an error occurs while retrieving the resource.
     */
    Resource getResource(String path,
                         String tenantDomain) throws IdentityRuntimeException;

    /**
     * Add or update and replace a registry resource.
     *
     * @param resource     Resource to be updated.
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @throws IdentityRuntimeException If an error occurs while updating the resource.
     */
    void addOrUpdateResource(Resource resource,
                             String path,
                             String tenantDomain) throws IdentityRuntimeException;

    /**
     * Remove a registry resource.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @throws IdentityRuntimeException If an error occurs while deleting the resource.
     */
    void deleteResource(String path,
                        String tenantDomain) throws IdentityRuntimeException;

    /**
     * Check whether a registry resource exists in the given path.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain of the resource.
     * @return True if the resource exists, false otherwise.
     * @throws IdentityRuntimeException If an error occurs while checking the existence of the resource.
     */
    boolean resourceExists(String path,
                           String tenantDomain) throws IdentityRuntimeException;

}
