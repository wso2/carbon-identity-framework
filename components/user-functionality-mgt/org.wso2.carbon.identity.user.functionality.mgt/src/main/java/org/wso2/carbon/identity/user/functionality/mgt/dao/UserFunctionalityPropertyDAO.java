/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.user.functionality.mgt.dao;

import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;

import java.util.Map;
import java.util.Set;

/**
 * Perform CRUD operations for per-user functionality properties.
 */
public interface UserFunctionalityPropertyDAO {

    /**
     * Adds a new functionality lock properties.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the functionality.
     * @param propertiesToAdd         Map of properties to add.
     */
    void addProperties(String userId, int tenantId, String functionalityIdentifier,
                       Map<String, String> propertiesToAdd) throws UserFunctionalityManagementServerException;

    /**
     * Returns all the properties for a user-functionality mapping, given the user id, tenant id, functionality identifier and the property
     * name.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the functionality.
     * @return An array of properties.
     */
    Map<String, String> getAllProperties(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Updates a property for a user-functionality mapping, given the user id, tenant id, functionality identifier and the property name,
     * by replacing the existing property.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the functionality.
     * @param propertiesToUpdate      Map of properties to be updated.
     */
    void updateProperties(String userId, int tenantId, String functionalityIdentifier,
                          Map<String, String> propertiesToUpdate)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes a property for a user-functionality mapping given the user id, tenant id, functionality identifier and the property name.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the functionality.
     * @param propertiesToDelete      Set of property names to be deleted.
     */
    void deletePropertiesForUser(String userId, int tenantId, String functionalityIdentifier, Set<String> propertiesToDelete)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the properties for a user-functionality mapping given the user id, tenant id, functionality identifier and the property
     * name.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the functionality.
     */
    void deleteAllPropertiesForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the per-user functionality properties for a tenant.
     *
     * @param tenantId Unique identifier for the tenant domain.
     * @throws UserFunctionalityManagementServerException
     */
    void deleteAllPropertiesForTenant(int tenantId) throws UserFunctionalityManagementServerException;
}
