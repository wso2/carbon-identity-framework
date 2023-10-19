/*
 * Copyright (c) 2019, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.application.mgt;

import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

/**
 * Allows application CRUD operations using unique resourceId.
 */
public interface ApplicationResourceManager {

    /**
     * Retrieve application basic information using the resourceId.
     *
     * @param resourceId   Unique resource identifier of the application
     * @param tenantDomain Tenant domain of the application
     * @return ApplicationBasicInfo containing the basic app information 
     * @throws IdentityApplicationManagementException
     */
    ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException;

    /**
     * Creates an application and returns the created application.
     *
     * @param application  Created application
     * @param tenantDomain Tenant domain of the application
     * @param username     Tenant aware username of the user performing the operation.
     * @return unique application resource id of the application
     * @throws IdentityApplicationManagementException
     */
    String createApplication(ServiceProvider application, String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Retrieve application information using the resourceId.
     *
     * @param resourceId   Unique resource identifier of the application
     * @param tenantDomain Tenant domain of the application
     * @return ServiceProvider containing the app information 
     * @throws IdentityApplicationManagementException
     */
    ServiceProvider getApplicationByResourceId(String resourceId,
                                               String tenantDomain) throws IdentityApplicationManagementException;

    /**
     * Update an application identified by the resourceId.
     *
     * @param resourceId         Unique resource identifier of the application
     * @param updatedApplication Updated application
     * @param tenantDomain       Tenant domain of the application
     * @param username           Tenant aware username of the user performing the operation.
     * @throws IdentityApplicationManagementException
     */
    void updateApplicationByResourceId(String resourceId, ServiceProvider updatedApplication,
                                       String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Delete an application identified by the resourceId.
     *
     * @param resourceId   Unique resource identifier of the application
     * @param tenantDomain Tenant domain of the application
     * @param username     Tenant aware username of the user performing the operation.
     * @throws IdentityApplicationManagementException
     */
    void deleteApplicationByResourceId(String resourceId, String tenantDomain, String username)
            throws IdentityApplicationManagementException;

    /**
     * Retrieve application resource id using the inboundKey and inboundType.
     *
     * @param inboundKey   inboundKey
     * @param inboundType  inboundType
     * @param tenantDomain tenantDomain
     * @return application resourceId
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException
     */
    default String getApplicationResourceIDByInboundKey(String inboundKey, String inboundType,
                                                                String tenantDomain)
            throws IdentityApplicationManagementException {

        return null;
    }
}
