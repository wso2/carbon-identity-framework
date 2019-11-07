/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.configuration;

import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import java.util.Set;

/**
 * This interface used to expose user store management functionality as an OSGi Service.
 */

public interface UserStoreConfigService {

    /**
     * Add a userStore {@link UserStoreDTO}.
     * @param userStoreDTO {@link UserStoreDTO} to insert.
     * @throws IdentityUserStoreMgtException
     */
    void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException;

    /**
     * Update the state of the userStore {@link UserStoreDTO}
     * @param userStoreDTO {@link UserStoreDTO} to update
     * @param  isStateChange true, if the update is a user store state change
     * @throws IdentityUserStoreMgtException throws if an error occurred while updating the userStore
     */
    void updateUserStore(UserStoreDTO userStoreDTO, boolean isStateChange) throws IdentityUserStoreMgtException;

    /**
     * Update the name of the userStore domain.
     * @param previousDomainName the domain name to be replaced
     * @param userStoreDTO {@link UserStoreDTO} to update
     * @throws IdentityUserStoreMgtException throws if an error occurred while updating the domain name.
     */
    void updateUserStoreByDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException;

    /**
     * Delete a userStore by domain name
     *
     * @param domain userStore domain name
     */
    void deleteUserStore(String domain) throws IdentityUserStoreMgtException;

    /**
     * Get userStore by domain
     *
     * @param domain userStore domain name
     * @return {@link UserStoreDTO} by given domain
     */
    UserStoreDTO getUserStore(String domain) throws IdentityUserStoreMgtException;

    /**
     * Get all userstores of the given repository.
     *
     * @return an array of {@link UserStoreDTO}
     */
    UserStoreDTO[] getUserStores() throws IdentityUserStoreMgtException;

    /**
     * Get available user store manager implementations
     *
     * @return: Available implementations for user store managers
     */
    Set<String> getAvailableUserStoreClasses() throws IdentityUserStoreMgtException;

    /**
     * Check the connection heath for JDBC userstores
     * @param domainName user store domain name
     * @param driverName the driver name
     * @param connectionURL the connection url
     * @param username the username
     * @param connectionPassword password
     * @param messageID
     * @return true or false
     */
    boolean testRDBMSConnection (String domainName, String driverName, String connectionURL, String username,
                                 String connectionPassword, String messageID) throws IdentityUserStoreMgtException;
}
