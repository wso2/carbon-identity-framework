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

package org.wso2.carbon.identity.resource.mgt;

import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Locale;

public interface RegistryResourceMgtService {

    /**
     *
     * @param tenantDomain
     * @return
     * @throws RegistryException
     */
    Resource getNewIdentityResource(String tenantDomain) throws RegistryException;

    /**
     *
     * @param path
     * @param tenantDomain
     * @param locale
     * @return
     * @throws IdentityRuntimeException
     */
    Resource getIdentityResource(String path,
                                 String tenantDomain,
                                 Locale locale) throws IdentityRuntimeException;

    /**
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
                             Locale locale) throws IdentityRuntimeException;

    /**
     *
     * @param path
     * @param tenantDomain
     * @return
     * @throws IdentityRuntimeException
     */
    Resource getIdentityResource(String path,
                                 String tenantDomain) throws IdentityRuntimeException;

    /**
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
     *
     * @param path
     * @param tenantDomain
     * @throws IdentityRuntimeException
     */
    void deleteIdentityResource(String path,
                                String tenantDomain) throws IdentityRuntimeException;

}
