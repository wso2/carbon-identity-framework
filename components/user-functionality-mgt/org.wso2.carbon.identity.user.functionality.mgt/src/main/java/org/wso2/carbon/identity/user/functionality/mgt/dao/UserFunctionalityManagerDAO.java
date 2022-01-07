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
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;

/**
 * Perform CRUD operations for functionality mappings.
 */
public interface UserFunctionalityManagerDAO {

    /**
     * Adds a new functionality mapping against a user.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @param functionalityLockStatus {@link FunctionalityLockStatus} to add.
     * @throws UserFunctionalityManagementServerException If error occurs while adding functionality mapping against a
     *                                                    user.
     */
    void addFunctionalityLock(String userId, int tenantId, String functionalityIdentifier,
                              FunctionalityLockStatus functionalityLockStatus)
            throws UserFunctionalityManagementServerException;

    /**
     * Returns the functionality lock status given the functionality identifier, tenant domain and the user id.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @return {@link FunctionalityLockStatus}.
     * @throws UserFunctionalityManagementServerException If error occurs while fetching the
     *                                                    {@link FunctionalityLockStatus}.
     */
    FunctionalityLockStatus getFunctionalityLockStatus(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Updates a user-functionality mapping given the functionality identifier and tenant domain by replacing the
     * existing mapping.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @param functionalityLockStatus Updated functionalityLockStatus object.
     * @throws UserFunctionalityManagementServerException If error occurs while updating the
     *                                                    {@link FunctionalityLockStatus}.
     */
    void updateLockStatusForUser(String userId, int tenantId, String functionalityIdentifier,
                                 FunctionalityLockStatus functionalityLockStatus)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes a user-functionality mapping given the functionality identifier, tenant domain and the user id.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @throws UserFunctionalityManagementServerException If error occurs while deleting the user-functionality mapping.
     */
    void deleteMappingForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the user-functionality mappings of a tenant.
     *
     * @param tenantId Unique identifier for the tenant domain.
     * @throws UserFunctionalityManagementServerException
     */
    void deleteAllMappingsForTenant(int tenantId) throws UserFunctionalityManagementServerException;
}
