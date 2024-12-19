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

package org.wso2.carbon.identity.api.resource.mgt;

import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;

import java.util.List;

/**
 * Interface for managing authorization detail types associated with APIs in a multi-tenant environment.
 * <p> This interface provides methods to add, retrieve, update, and delete authorization detail types for
 * specific APIs in a given tenant domain.</p>
 */
public interface AuthorizationDetailsTypeManager {

    /**
     * Adds a list of authorization detail types for a specific API in the given tenant domain.
     *
     * @param apiId                     The API identifier.
     * @param authorizationDetailsTypes The list of authorization detail types to be added.
     * @param tenantDomain              The tenant domain where the API belongs.
     * @return A list of persisted {@link AuthorizationDetailsType} instances.
     * @throws APIResourceMgtException If an error occurs while adding the authorization detail types.
     */
    List<AuthorizationDetailsType> addAuthorizationDetailsTypes(
            String apiId, List<AuthorizationDetailsType> authorizationDetailsTypes, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Deletes a specific authorization detail type by API ID and type ID in the given tenant domain.
     *
     * @param apiId        The API identifier.
     * @param typeId       The authorization detail type identifier.
     * @param tenantDomain The tenant domain where the API belongs.
     * @throws APIResourceMgtException If an error occurs during deletion.
     */
    void deleteAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Deletes all authorization detail types associated with a specific API in the given tenant domain.
     *
     * @param apiId        The API identifier.
     * @param tenantDomain The tenant domain where the API belongs.
     * @throws APIResourceMgtException If an error occurs during deletion.
     */
    void deleteAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Retrieves a specific authorization detail type by API ID and type ID in the given tenant domain.
     *
     * @param apiId        The API identifier.
     * @param typeId       The authorization detail type identifier.
     * @param tenantDomain The tenant domain where the API belongs.
     * @return The corresponding {@link AuthorizationDetailsType} if found.
     * @throws APIResourceMgtException If an error occurs while retrieving the authorization detail type.
     */
    AuthorizationDetailsType getAuthorizationDetailsTypeByApiIdAndTypeId(String apiId, String typeId,
                                                                         String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Retrieves a list of authorization detail types matching a given filter in the specified tenant domain.
     *
     * @param filter       The filter criteria for the search (could be a type or other identifier).
     * @param tenantDomain The tenant domain where the authorization detail types belong.
     * @return A list of {@link AuthorizationDetailsType} matching the filter.
     * @throws APIResourceMgtException If an error occurs during retrieval.
     */
    List<AuthorizationDetailsType> getAuthorizationDetailsTypes(String filter, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Retrieves a list of authorization detail types for a specific API in the given tenant domain.
     *
     * @param apiId        The API identifier.
     * @param tenantDomain The tenant domain where the API belongs.
     * @return A list of {@link AuthorizationDetailsType} associated with the API.
     * @throws APIResourceMgtException If an error occurs while retrieving the authorization detail types.
     */
    List<AuthorizationDetailsType> getAuthorizationDetailsTypesByApiId(String apiId, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Checks if a specific authorization detail type exists for a given type in the tenant domain.
     *
     * @param filter       The filter criteria for the search (could be a type or other identifier).
     * @param tenantDomain The tenant domain where the API belongs.
     * @return {@code true} if the authorization detail type exists, {@code false} otherwise.
     * @throws APIResourceMgtException If an error occurs during the existence check.
     */
    boolean isAuthorizationDetailsTypeExists(String filter, String tenantDomain) throws APIResourceMgtException;

    /**
     * Checks if a specific authorization detail type exists for a given API in the tenant domain.
     *
     * @param apiId        The API identifier.
     * @param type         The type of authorization detail to check.
     * @param tenantDomain The tenant domain where the API belongs.
     * @return {@code true} if the authorization detail type exists, {@code false} otherwise.
     * @throws APIResourceMgtException If an error occurs during the existence check.
     */
    boolean isAuthorizationDetailsTypeExists(String apiId, String type, String tenantDomain)
            throws APIResourceMgtException;

    /**
     * Replaces a list of authorization detail types by removing the specified types and adding new ones
     * for a specific API in the tenant domain.
     *
     * @param apiId                            The API identifier.
     * @param removedAuthorizationDetailsTypes The list of authorization detail types to be removed.
     * @param addedAuthorizationDetailsTypes   The list of new authorization detail types to be added.
     * @param tenantDomain                     The tenant domain where the API belongs.
     * @throws APIResourceMgtException If an error occurs during the replace operation.
     */
    void updateAuthorizationDetailsTypes(String apiId, List<String> removedAuthorizationDetailsTypes,
                                          List<AuthorizationDetailsType> addedAuthorizationDetailsTypes,
                                          String tenantDomain) throws APIResourceMgtException;

    /**
     * Updates an existing authorization detail type for a specific API in the given tenant domain.
     *
     * @param apiId                    The API identifier.
     * @param authorizationDetailsType The updated {@link AuthorizationDetailsType}.
     * @param tenantDomain             The tenant domain where the API belongs.
     * @throws APIResourceMgtException If an error occurs during the update process.
     */
    void updateAuthorizationDetailsType(String apiId, AuthorizationDetailsType authorizationDetailsType,
                                        String tenantDomain) throws APIResourceMgtException;
}
