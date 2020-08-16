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

package org.wso2.carbon.identity.user.functionality.mgt;

import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementException;
import org.wso2.carbon.identity.user.functionality.mgt.exception.UserFunctionalityManagementServerException;
import org.wso2.carbon.identity.user.functionality.mgt.model.FunctionalityLockStatus;

import java.util.Map;
import java.util.Set;

/**
 * User functionality manager service interface.
 */
public interface UserFunctionalityManager {

    /**
     * Returns the status of the functionality. Whether the functionality is locked or unlocked, the unlock time,
     * the unlock code and reason, given the functionality identifier, the user id and the tenant id.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @return The status of the functionality.
     * @throws UserFunctionalityManagementException
     */
    FunctionalityLockStatus getLockStatus(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementException;

    /**
     * Returns the properties of the user-functionality mapping. These properties may include invalid attempts counts,
     * functionality lockout counts, etc.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @return The properties of the user-functionality mapping.
     * @throws UserFunctionalityManagementServerException
     */
    Map<String, String> getProperties(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Set the properties of the user-functionality mapping. These properties may include invalid attempts counts,
     * functionality lockout counts, etc.
     *
     * @param userId                      Unique identifier of the user.
     * @param tenantId                    Unique identifier for the tenant domain.
     * @param functionalityIdentifier     Identifier of the the functionality.
     * @param functionalityLockProperties The properties of the user-functionality mapping.
     * @throws UserFunctionalityManagementServerException
     */
    void setProperties(String userId, int tenantId, String functionalityIdentifier,
                       Map<String, String> functionalityLockProperties)
            throws UserFunctionalityManagementServerException;

    /**
     * Lock a functionality given the functionality identifier, user id, tenant id, functionality lock time and the
     * functionality lock code.
     *
     * @param userId                      Unique identifier of the user.
     * @param tenantId                    Unique identifier for the tenant domain.
     * @param functionalityIdentifier     Identifier of the the functionality.
     * @param timeToLock                  The lock time for the functionality in milliseconds. Set -1 to lock
     *                                    indefinitely.
     * @param functionalityLockReasonCode The functionality lock code.
     * @param functionalityLockReason     The functionality lock reason.
     * @throws UserFunctionalityManagementException
     */
    void lock(String userId, int tenantId, String functionalityIdentifier, long timeToLock,
              String functionalityLockReasonCode, String functionalityLockReason)
            throws UserFunctionalityManagementException;

    /**
     * Unlock a functionality given the functionality identifier, the user id and the tenant id.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @throws UserFunctionalityManagementServerException
     */
    void unlock(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the properties that are related to a certain user-functionality mapping identified by the user id,
     * tenant id and the functionality identifier.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @throws UserFunctionalityManagementServerException
     */
    void deleteAllPropertiesForUser(String userId, int tenantId, String functionalityIdentifier)
            throws UserFunctionalityManagementServerException;

    /**
     * Deletes a certain list of properties indicated by the propertiesToDelete set that are related to a certain
     * user-functionality mapping identified by the user id, tenant id and the functionality identifier.
     *
     * @param userId                  Unique identifier of the user.
     * @param tenantId                Unique identifier for the tenant domain.
     * @param functionalityIdentifier Identifier of the the functionality.
     * @param propertiesToDelete      Set of property names to delete.
     * @throws UserFunctionalityManagementServerException
     */
    void deletePropertiesForUser(String userId, int tenantId, String functionalityIdentifier,
                                 Set<String> propertiesToDelete) throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the user-functionality mappings of a tenant.
     *
     * @param tenantId Unique identifier for the tenant domain.
     * @throws UserFunctionalityManagementServerException
     */
    void deleteAllMappingsForTenant(int tenantId) throws UserFunctionalityManagementServerException;

    /**
     * Deletes all the per-user functionality properties for a tenant.
     *
     * @param tenantId Unique identifier for the tenant domain.
     * @throws UserFunctionalityManagementServerException
     */
    void deleteAllPropertiesForTenant(int tenantId) throws UserFunctionalityManagementServerException;
}
