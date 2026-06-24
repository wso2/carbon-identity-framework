/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.services;

import org.wso2.carbon.identity.application.authentication.framework.exception.ConsentAppMappingException;

import java.util.List;

/**
 * Service interface for managing consent purpose to application mappings.
 * A mapping indicates that the given consent purpose is enabled for the application,
 * so consent will be enforced during login.
 */
public interface ConsentAppMappingService {

    /**
     * Get the list of application resource IDs mapped to the given consent purpose.
     *
     * @param purposeId UUID of the consent purpose.
     * @return List of application resource IDs.
     * @throws ConsentAppMappingException if an error occurs.
     */
    List<String> getApplicationsForPurpose(String purposeId) throws ConsentAppMappingException;

    /**
     * Map an application to a consent purpose.
     *
     * @param purposeId     UUID of the consent purpose.
     * @param applicationId Resource ID of the application.
     * @throws ConsentAppMappingException if an error occurs or the mapping already exists.
     */
    void addApplicationToPurpose(String purposeId, String applicationId) throws ConsentAppMappingException;

    /**
     * Remove the mapping between an application and a consent purpose.
     *
     * @param purposeId     UUID of the consent purpose.
     * @param applicationId Resource ID of the application.
     * @throws ConsentAppMappingException if an error occurs or the mapping does not exist.
     */
    void removeApplicationFromPurpose(String purposeId, String applicationId) throws ConsentAppMappingException;

    /**
     * Get the list of consent purpose UUIDs mapped to the given application.
     *
     * @param applicationId Resource ID of the application.
     * @return List of consent purpose UUIDs.
     * @throws ConsentAppMappingException if an error occurs.
     */
    List<String> getPurposesForApplication(String applicationId) throws ConsentAppMappingException;

    /**
     * Remove all application mappings for a consent purpose. No-op if no mappings exist.
     *
     * @param purposeId UUID of the consent purpose.
     * @throws ConsentAppMappingException if an error occurs.
     */
    void removeAllApplicationMappingsForPurpose(String purposeId) throws ConsentAppMappingException;

    /**
     * Remove all purpose mappings for an application. No-op if no mappings exist.
     *
     * @param applicationId Resource ID of the application.
     * @throws ConsentAppMappingException if an error occurs.
     */
    void removeAllPurposeMappingsForApplication(String applicationId) throws ConsentAppMappingException;
}
