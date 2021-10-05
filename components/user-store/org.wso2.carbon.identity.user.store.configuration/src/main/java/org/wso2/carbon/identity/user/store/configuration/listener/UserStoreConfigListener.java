/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.configuration.listener;

import org.wso2.carbon.identity.core.util.IdentityCoreConstants.UserStoreState;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Listener Interface for user stores. Listeners will be trigger upon operations on the user stores.
 */
public interface UserStoreConfigListener {

    void onUserStoreNamePreUpdate(int tenantId, String currentUserStoreName, String newUserStoreName)
            throws UserStoreException;

    void onUserStoreNamePostUpdate(int tenantId, String currentUserStoreName, String newUserStoreName) throws
            UserStoreException;

    void onUserStorePreDelete(int tenantId, String userStoreName) throws UserStoreException;

    void onUserStorePostDelete(int tenantId, String userStoreName) throws UserStoreException;

    default void onUserStorePreStateChange(UserStoreState state, int tenantId, String userStoreName)
            throws UserStoreException {
        // Not implemented.
    }

    /**
     * Called before adding a userstore configuration.
     *
     * @param tenantId Tenant id.
     * @param userStoreName Userstore domain name.
     * @throws UserStoreException throws when an error occurs in the listener.
     */
    default void onUserStorePreAdd(int tenantId, String userStoreName) throws UserStoreException {

        // Not implemented.
    }

    /**
     * Called after retrieving a userstore configurations for a given name.
     *
     * @param tenantId Tenant id.
     * @param userStoreDTO Retrieved userstore configuration.
     * @throws UserStoreException throws when an error occurs in the listener.
     */
    default void onUserStorePostGet(int tenantId, UserStoreDTO userStoreDTO) throws UserStoreException {

        // Not implemented.
    }

    /**
     * Called after retrieving all userstore configurations.
     *
     * @param tenantId Tenant id.
     * @param userStoreDTOS Array of userstore configurations.
     * @throws UserStoreException throws when an error occurs in the listener.
     */
    default void onUserStoresPostGet(int tenantId, UserStoreDTO[] userStoreDTOS) throws UserStoreException {

        // Not implemented.
    }

    /**
     * Called before adding a userstore configuration.
     *
     * @param tenantId Tenant id.
     * @param userStoreDTO Userstore configuration to be added.
     * @throws UserStoreException throws when an error occurs in the listener.
     */
    default void onUserStorePreAdd(int tenantId, UserStoreDTO userStoreDTO) throws UserStoreException {

        // Not implemented.
    }

    /**
     * Called before updating a userstore configuration.
     *
     * @param tenantId Tenant id.
     * @param userStoreDTO Userstore configuration to be updated.
     * @param isStateChange Boolean flag denoting whether the
     *                      update is a userstore state change.
     * @throws UserStoreException throws when an error occurs in the listener.
     */
    default void onUserStorePreUpdate(int tenantId, UserStoreDTO userStoreDTO, boolean isStateChange)
            throws UserStoreException {

        // Not implemented.
    }
}
