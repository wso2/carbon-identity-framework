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

package org.wso2.carbon.identity.core.persistence.registry;

import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.registry.core.Resource;


public interface RegistryResourceMgtService {

    /**
     * Retrieve a registry resource from tenant registry based on locale.
     * (In cases where the multiple resources of the same type exists for different locales like email templates,
     * challenge questions etc.)
     *
     * @param path
     * @param tenantDomain
     * @param locale
     * @return
     * @throws IdentityRuntimeException
     */
    Resource getIdentityResource(String path,
                                 String tenantDomain,
                                 String locale) throws IdentityRuntimeException;


    /**
     * Add a registry resource into the tenant registry based on locale. (In cases where the multiple
     * resources of the same type exists for different locales like email templates, challenge questions etc.)
     * If a resource already exists in the same path with the same locale an exception will be thrown.
     *
     * @param identityResource
     * @param path
     * @param tenantDomain
     * @param locale
     * @throws IdentityRuntimeException
     */
    void addIdentityResource(Resource identityResource,
                             String path,
                             String tenantDomain,
                             String locale) throws IdentityRuntimeException;


    /**
     * Update and replace a registry resource in a tenant registry based on locale. (In cases where the multiple
     * resources of the same type exists for different locales like email templates, challenge questions etc.)
     * If a resource already exists in the same path with the same locale an exception will be thrown.
     *
     * @param identityResource
     * @param path
     * @param tenantDomain
     * @param locale
     * @throws IdentityRuntimeException
     */
    void putIdentityResource(Resource identityResource,
                             String path,
                             String tenantDomain,
                             String locale) throws IdentityRuntimeException;


    /**
     * Remove a registry resource from a tenant registry based on locale.
     *
     * @param path
     * @param tenantDomain
     * @param locale
     * @throws IdentityRuntimeException
     */
    void deleteIdentityResource(String path,
                                String tenantDomain,
                                String locale) throws IdentityRuntimeException;


    /**
     * Check whether a resource in a specific locale exists in the tenant registry.
     *
     * @param path
     * @param tenantDomain
     * @param locale
     * @return
     * @throws IdentityRuntimeException
     */
    boolean isResourceExists(String path,
                             String tenantDomain,
                             String locale) throws IdentityRuntimeException;


    /**
     * Get a registry resource from a tenant registry.
     *
     * @param path
     * @param tenantDomain
     * @return
     * @throws IdentityRuntimeException
     */
    Resource getIdentityResource(String path,
                                 String tenantDomain) throws IdentityRuntimeException;

    /**
     * Update and replace a registry resource in tenant registry
     *
     * @param identityResource
     * @param path
     * @param tenantDomain
     * @throws IdentityRuntimeException
     */
    void putIdentityResource(Resource identityResource,
                             String path,
                             String tenantDomain) throws IdentityRuntimeException;


    /**
     * Add a registry resource to tenant registry. If a resource already exists at the specified path an exception
     * will be thrown.
     *
     * @param identityResource
     * @param path
     * @param tenantDomain
     * @throws IdentityRuntimeException
     */
    void addIdentityResource(Resource identityResource,
                             String path,
                             String tenantDomain) throws IdentityRuntimeException;

    /**
     * Remove a registry resource from a tenant registry.
     *
     * @param path
     * @param tenantDomain
     * @throws IdentityRuntimeException
     */
    void deleteIdentityResource(String path,
                                String tenantDomain) throws IdentityRuntimeException;


    /**
     * Check whether a resource exists in the given path in the tenant registry.
     *
     * @param path
     * @param tenantDomain
     * @return
     * @throws IdentityRuntimeException
     */
    boolean isResourceExists(String path,
                             String tenantDomain) throws IdentityRuntimeException;


}
